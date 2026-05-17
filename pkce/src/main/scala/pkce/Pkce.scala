package pkce

import com.raquo.laminar.api.L.{Signal, Var}
import org.scalajs.dom
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8Array}

/** OpenID Connect (OAuth 2.0 Authorization Code + PKCE) client for a Scala.js SPA.
  *
  * Standards: RFC 6749 (OAuth 2.0), RFC 7636 (PKCE), OpenID Connect Core 1.0, OpenID Connect
  * RP-Initiated Logout 1.0. The library hardcodes no provider-specific paths — pass
  * [[Pkce.Endpoints]] from your IDP's discovery document (`/.well-known/openid-configuration`).
  * Tested against AWS Cognito; compatible with Auth0, Okta, Keycloak, Google, Azure AD, and
  * anything else that conforms.
  *
  * Why PKCE: the SPA client has no secret. PKCE binds the code exchange to this browser session via
  * a `code_verifier` / `code_challenge` pair, so an attacker who intercepts the authorization code
  * cannot exchange it for tokens without also having the verifier.
  *
  * **tokens are in-memory only.** `sessionStorage` is touched exclusively for the single-use PKCE
  * `verifier` and CSRF `state` values that must survive the redirect to the IDP and back; both are
  * cleared as soon as the exchange completes. A page reload drops the tokens — the app must call
  * [[signIn]] again. The IDP's session cookie (typically `httpOnly`, domain-scoped) recognizes the
  * user and redirects back silently (no login form) when its session is still valid.
  *
  * Flow:
  *
  *   - [[signIn]] mints `code_verifier` + S256 `code_challenge`, stashes the verifier + state in
  *     `sessionStorage`, and navigates to `endpoints.authorize`.
  *   - The IDP redirects back to `${origin}${redirectPath}?code=…&state=…`.
  *     [[handleCallbackIfPresent]] detects the code on app boot, POSTs to `endpoints.token` with
  *     the stashed verifier, clears verifier + state from `sessionStorage`, sets the in-memory
  *     token Vars, replaces the URL with `postLogoutPath`, and resolves `true`.
  *   - [[idToken]] / [[accessToken]] are reactive `Signal[Option[String]]`s. Bind them to your
  *     routing / API client. [[claims]] is a derived signal that decodes the id_token to
  *     [[Pkce.Claims]] for UI display.
  *   - [[signOut]] clears tokens and bounces through `endpoints.endSession` per OIDC RP-Initiated
  *     Logout 1.0 (`id_token_hint` + `post_logout_redirect_uri`).
  *
  * {{{
  * val pkce = new Pkce(Pkce.Endpoints(
  *   authorize  = "https://idp.example.com/oauth2/authorize",
  *   token      = "https://idp.example.com/oauth2/token",
  *   endSession = "https://idp.example.com/oauth2/logout"
  * ), clientId = "my-spa-client")
  *
  * def App(
  *     claims:      Signal[Option[Pkce.Claims]],
  *     accessToken: Signal[Option[String]],
  *     signOut:     () => Nothing
  * ): HtmlElement = ???
  *
  * pkce.handleCallbackIfPresent().onComplete {
  *   case Success(true) =>
  *     // Post-callback path — tokens are set, App can read them via the Signals.
  *     render(mount, App(pkce.claims, pkce.accessToken, () => pkce.signOut()))
  *   case Success(false) =>
  *     // Cold boot, no callback in URL — bounce through the IDP. If the IDP's
  *     // session cookie is alive the user comes back silently with a fresh code
  *     // and this `Success(true)` branch runs on the next load.
  *     pkce.signIn()
  *   case Failure(err) =>
  *     render(mount, ErrorView(err.getMessage))
  * }
  * }}}
  *
  * @param endpoints
  *   The three OIDC endpoints, from the IDP's discovery document.
  * @param clientId
  *   The OAuth 2.0 client_id for this SPA.
  * @param redirectPath
  *   Path portion of the redirect URI; combined with `window.location.origin`. Must be one of the
  *   app client's "Allowed callback URLs".
  * @param postLogoutPath
  *   Path the user lands on after sign-out (and after the callback URL is replaced). Must be one of
  *   the app client's "Allowed sign-out URLs".
  * @param scope
  *   OAuth scope string. Defaults to `openid email profile`. Must include `openid` for OIDC.
  * @param refreshLeadTime
  *   How long before the `access_token`'s `exp` to fire the background refresh. Default 5 minutes —
  *   gives retries headroom if the token endpoint is slow or transiently unavailable. Set to
  *   `Duration.Zero` to disable timer-driven refresh (the app can still call [[refresh]] manually).
  * @param keys
  *   `sessionStorage` key names. Override to scope multiple clients per origin.
  */
final class Pkce(
    endpoints: Pkce.Endpoints,
    clientId: String,
    redirectPath: String = "/auth/callback",
    postLogoutPath: String = "/",
    scope: String = "openid email profile",
    refreshLeadTime: FiniteDuration = 5.minutes,
    keys: Pkce.StorageKeys = Pkce.StorageKeys.default
) {
  import Pkce.*

  private val idTokenVar: Var[Option[String]] = Var(None)
  private val accessTokenVar: Var[Option[String]] = Var(None)

  // The refresh_token is the only token we keep at rest somewhere — but only
  // in this same Var (memory).
  private val refreshTokenVar: Var[Option[String]] = Var(None)

  // Handle for the pending `setTimeout` that fires the next refresh. Cancelled
  // on sign-out and replaced on every successful (re-)scheduling.
  private var refreshTimer: Option[Int] = None

  /** Reactive `id_token`. Use for client-side UI (decoded via [[claims]]). Never send to your API
    * as a Bearer — your API should validate access tokens.
    */
  val idToken: Signal[Option[String]] = idTokenVar.signal

  /** Reactive `access_token`. Wire into your HTTP client's Bearer attach.
    *
    * The token is a JWT issued by your IDP. Your backend **must** validate it on every request —
    * verify the signature against the IDP's JWKS (`endpoints.jwks` from the discovery document),
    * check `exp`, `iss`, and pin `aud` / `client_id` against your configured SPA app client.
    */
  val accessToken: Signal[Option[String]] = accessTokenVar.signal

  /** Decoded claims of the current `id_token`. Recomputed whenever the token changes. `None` when
    * signed out or when the JWT is unparseable.
    */
  val claims: Signal[Option[Pkce.Claims]] =
    idToken.map(_.flatMap(Pkce.claims))

  val signedIn: Signal[Boolean] = idToken.map(_.isDefined)

  /** Redirect to the IDP's authorization endpoint. The Future never produces a value because the
    * page unloads before that can happen.
    */
  def signIn(): Future[Nothing] = {
    val verifier = randomUrlSafe(64)
    val state = randomUrlSafe(16)
    dom.window.sessionStorage.setItem(keys.verifier, verifier)
    dom.window.sessionStorage.setItem(keys.state, state)
    sha256Base64Url(verifier).map { challenge =>
      val q = encodeForm(
        Map(
          "response_type" -> "code",
          "client_id" -> clientId,
          "redirect_uri" -> redirectUri,
          "scope" -> scope,
          "state" -> state,
          "code_challenge" -> challenge,
          "code_challenge_method" -> "S256"
        )
      )
      dom.window.location.href = s"${endpoints.authorize}?$q"
      throw new IllegalStateException("Pkce.signIn: navigation should have torn this stack down")
    }
  }

  /** Clear local tokens and bounce through the IDP's end-session endpoint per OIDC RP-Initiated
    * Logout 1.0. `id_token_hint` carries the current id_token (if any) so the IDP can identify
    * which session to terminate.
    */
  def signOut(): Nothing = {
    cancelRefreshTimer()
    val idHint = idTokenVar.now()
    idTokenVar.set(None)
    accessTokenVar.set(None)
    refreshTokenVar.set(None)
    val params = Map(
      "client_id" -> clientId,
      "post_logout_redirect_uri" -> logoutUri
    ) ++ idHint.map("id_token_hint" -> _)
    val q = encodeForm(params)
    dom.window.location.href = s"${endpoints.endSession}?$q"
    throw new IllegalStateException("Pkce.signOut: navigation should have torn this stack down")
  }

  /** Exchange the cached `refresh_token` for a fresh `id_token` + `access_token`.
    *
    * Called automatically by the background timer set up after each successful exchange — fires
    * `refreshLeadTime` before the current `access_token`'s `exp`. Exposed for tests and for manual
    * recovery paths (e.g. retry after a 401).
    *
    * Fails if no refresh_token is in memory (no prior sign-in this JS heap) or if the IDP rejects
    * the grant (expired / revoked / rotated away). On failure the timer is **not** rescheduled —
    * the access_token will simply expire naturally, and the next API call sees a 401 the app can
    * handle. The in-memory tokens are left in place so any inflight requests still work for the
    * remaining lead-time window.
    */
  def refresh(): Future[Unit] = refreshTokenVar.now() match {
    case None =>
      Future.failed(new IllegalStateException("Pkce.refresh: no refresh_token in memory"))
    case Some(rt) =>
      postTokenRequest(
        Map(
          "grant_type" -> "refresh_token",
          "client_id" -> clientId,
          "refresh_token" -> rt
        )
      ).map(setTokens)
  }

  /** Boot-time hook: when the URL holds a `code=…` param, complete the exchange.
    *
    * Returns a Future that:
    *   - resolves with `true` when a callback was detected and tokens were stored (caller should
    *     re-render from a clean route — the URL is replaced with `${origin}${postLogoutPath}`);
    *   - resolves with `false` when no callback was in the URL;
    *   - fails on protocol error (state mismatch, missing verifier, exchange rejected by the IDP).
    */
  def handleCallbackIfPresent(): Future[Boolean] = {
    val loc = dom.window.location
    val params =
      if (loc.search.nonEmpty && loc.search.contains("code")) parseQueryString(loc.search)
      else parseQueryString(loc.hash)

    params.get("code") match {
      case None => Future.successful(false)
      case Some(code) =>
        val storedState = Option(dom.window.sessionStorage.getItem(keys.state))
        if (storedState != params.get("state")) {
          Future.failed(new RuntimeException("OAuth state mismatch — possible CSRF, aborting."))
        } else {
          Option(dom.window.sessionStorage.getItem(keys.verifier)) match {
            case None =>
              Future.failed(new RuntimeException("missing PKCE verifier in sessionStorage"))
            case Some(verifier) =>
              exchangeCodeForTokens(code, verifier).map { resp =>

                dom.window.sessionStorage.removeItem(keys.verifier)
                dom.window.sessionStorage.removeItem(keys.state)
                setTokens(resp)

                val cleanUrl = dom.window.location.origin + postLogoutPath
                dom.window.history.replaceState((), "", cleanUrl)
                true
              }
          }
        }
    }
  }

  private def redirectUri: String = dom.window.location.origin + redirectPath
  private def logoutUri: String = dom.window.location.origin + postLogoutPath

  private def setTokens(resp: TokenResponse): Unit = {
    idTokenVar.set(Some(resp.idToken))
    accessTokenVar.set(resp.accessToken)
    // OIDC providers rotate the refresh_token only when explicitly configured; the common case is
    // "absent from the response, keep the original". So only overwrite when a new one is supplied.
    resp.refreshToken.foreach(t => refreshTokenVar.set(Some(t)))
    scheduleNextRefresh()
  }

  private def scheduleNextRefresh(): Unit = {
    cancelRefreshTimer()
    if (refreshLeadTime > scala.concurrent.duration.Duration.Zero) {
      val maybeDelayMs = for {
        accTok <- accessTokenVar.now()
        expSec <- Pkce.decodeExp(accTok)
      } yield {
        val nowSec = System.currentTimeMillis() / 1000L
        math.max(0L, (expSec - nowSec - refreshLeadTime.toSeconds) * 1000L)
      }
      maybeDelayMs.foreach { delayMs =>
        val handle = dom.window.setTimeout(
          () => { val _ = refresh(); () },
          delayMs.toDouble
        )
        refreshTimer = Some(handle)
      }
    }
  }

  private def cancelRefreshTimer(): Unit = refreshTimer.foreach { handle =>
    dom.window.clearTimeout(handle)
    refreshTimer = None
  }

  private def exchangeCodeForTokens(code: String, verifier: String): Future[TokenResponse] =
    postTokenRequest(
      Map(
        "grant_type" -> "authorization_code",
        "client_id" -> clientId,
        "code" -> code,
        "redirect_uri" -> redirectUri,
        "code_verifier" -> verifier
      )
    )

  private def postTokenRequest(body: Map[String, String]): Future[TokenResponse] = {
    val init = new dom.RequestInit {}
    init.method = dom.HttpMethod.POST
    val headers = new dom.Headers()
    headers.append("Content-Type", "application/x-www-form-urlencoded")
    init.headers = headers
    init.body = encodeForm(body)

    dom.fetch(endpoints.token, init).toFuture.flatMap { res =>
      if (!res.ok)
        Future.failed(new RuntimeException(s"token request failed: HTTP ${res.status}"))
      else
        res.text().toFuture.map { txt =>
          val parsed = js.JSON.parse(txt)
          val idTok = parsed.id_token.asInstanceOf[js.UndefOr[String]].toOption.getOrElse {
            throw new RuntimeException("token response missing id_token")
          }
          val accTok = parsed.access_token.asInstanceOf[js.UndefOr[String]].toOption
          val refTok = parsed.refresh_token.asInstanceOf[js.UndefOr[String]].toOption
          TokenResponse(idTok, accTok, refTok)
        }
    }
  }

  private def randomUrlSafe(numBytes: Int): String = {
    val arr = new Uint8Array(numBytes)
    dom.window.asInstanceOf[js.Dynamic].crypto.getRandomValues(arr)
    base64UrlEncode(uint8ToBytes(arr))
  }

  private def sha256Base64Url(input: String): Future[String] = {
    val textEnc = js.Dynamic.newInstance(js.Dynamic.global.TextEncoder)()
    val data = textEnc.encode(input).asInstanceOf[Uint8Array]
    val subtle = dom.window.asInstanceOf[js.Dynamic].crypto.subtle
    subtle
      .digest("SHA-256", data)
      .asInstanceOf[js.Promise[ArrayBuffer]]
      .toFuture
      .map(ab => base64UrlEncode(uint8ToBytes(new Uint8Array(ab))))
  }
}

object Pkce {

  final case class Endpoints(authorize: String, token: String, endSession: String)

  object Endpoints {

    /** Cognito helper
      * @param domain
      *   Hosted-UI base URL without trailing slash. Examples:
      *   `https://my-app.auth.us-east-1.amazoncognito.com`, `https://auth.example.com`.
      */
    def cognito(domain: String): Endpoints = Endpoints(
      authorize = s"$domain/oauth2/authorize",
      token = s"$domain/oauth2/token",
      endSession = s"$domain/oauth2/logout"
    )

    /** Cognito helper
      *
      *   - `prefix` is the "Cognito domain" prefix on the user pool's App integration → Domain.
      *   - `region` is the AWS region the user pool lives in (e.g. `us-east-1`, `eu-west-2`). It's
      *     also the part before the underscore in the user pool ID (`<region>_<random>`).
      *
      * For a custom domain, use [[cognito(String)]] instead.
      */
    def cognito(prefix: String, region: String): Endpoints =
      cognito(s"https://$prefix.auth.$region.amazoncognito.com")
  }

  final case class Claims(sub: String, email: Option[String], rawPayload: String)

  final case class StorageKeys(verifier: String, state: String)

  object StorageKeys {
    val default: StorageKeys = StorageKeys(
      verifier = "pkce.verifier",
      state = "pkce.state"
    )
  }

  def claims(jwt: String): Option[Claims] = {
    val parts = jwt.split('.')
    if (parts.length < 2) None
    else {
      try {
        val bytes = base64UrlDecode(parts(1))
        val payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
        val parsed = js.JSON.parse(payload)
        parsed.sub.asInstanceOf[js.UndefOr[String]].toOption.map { sub =>
          val email = parsed.email.asInstanceOf[js.UndefOr[String]].toOption
          Claims(sub, email, payload)
        }
      } catch {
        case _: Throwable => None
      }
    }
  }

  def decodeExp(jwt: String): Option[Long] = {
    val parts = jwt.split('.')
    if (parts.length < 2) None
    else {
      try {
        val bytes = base64UrlDecode(parts(1))
        val payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
        val parsed = js.JSON.parse(payload)
        parsed.exp.asInstanceOf[js.UndefOr[Double]].toOption.map(_.toLong)
      } catch {
        case _: Throwable => None
      }
    }
  }

  def parseQueryString(s: String): Map[String, String] = {
    val stripped = s.stripPrefix("?").stripPrefix("#")
    if (stripped.isEmpty) Map.empty
    else
      stripped
        .split('&')
        .iterator
        .filter(_.contains('='))
        .map { kv =>
          val idx = kv.indexOf('=')
          val k = kv.substring(0, idx)
          val v = kv.substring(idx + 1)
          js.URIUtils.decodeURIComponent(k) -> js.URIUtils.decodeURIComponent(v)
        }
        .toMap
  }

  def encodeForm(params: Map[String, String]): String =
    params.iterator
      .map { case (k, v) =>
        s"${js.URIUtils.encodeURIComponent(k)}=${js.URIUtils.encodeURIComponent(v)}"
      }
      .mkString("&")

  def base64UrlEncode(bytes: Array[Byte]): String =
    java.util.Base64.getUrlEncoder.withoutPadding.encodeToString(bytes)

  def base64UrlDecode(s: String): Array[Byte] = {
    val padded = (s.length % 4) match {
      case 2 => s + "=="
      case 3 => s + "="
      case _ => s
    }
    java.util.Base64.getUrlDecoder.decode(padded)
  }

  private[pkce] final case class TokenResponse(
      idToken: String,
      accessToken: Option[String],
      refreshToken: Option[String]
  )

  private[pkce] def uint8ToBytes(arr: Uint8Array): Array[Byte] = {
    val out = new Array[Byte](arr.length)
    var i = 0
    while (i < arr.length) { out(i) = arr(i).toByte; i += 1 }
    out
  }
}
