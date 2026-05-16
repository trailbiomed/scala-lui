package pkce

import java.nio.charset.StandardCharsets
import scala.scalajs.js

/** Tests for the pure (non-DOM) helpers in `Pkce`'s companion. The instance class itself reads
  * `dom.window.sessionStorage` at construction time; covering it would require a `jsdom` test env,
  * which isn't worth pulling in for this surface area.
  */
class PkceSuite extends munit.FunSuite {

  // ── base64url codec ───────────────────────────────────────────────────

  test("base64url encode/decode roundtrip preserves every byte value") {
    val bytes = (0 to 255).map(_.toByte).toArray
    val encoded = Pkce.base64UrlEncode(bytes)
    val decoded = Pkce.base64UrlDecode(encoded)
    assertEquals(decoded.toSeq, bytes.toSeq)
  }

  test("base64url encode omits padding and '+'/'/' chars") {
    // 64 bytes ⇒ standard base64 would produce 88 chars including padding; the
    // url variant must use '-'/'_' and drop trailing '='.
    val encoded = Pkce.base64UrlEncode(Array.tabulate(64)(_.toByte))
    assert(!encoded.contains('='), s"unexpected '=' in $encoded")
    assert(!encoded.contains('+'), s"unexpected '+' in $encoded")
    assert(!encoded.contains('/'), s"unexpected '/' in $encoded")
  }

  test("base64url decode accepts unpadded input of any residue length") {
    // Encoding lengths 1, 2, 3 bytes hit each of the (len % 4) padding branches.
    Seq(Array[Byte](0x01), Array[Byte](0x01, 0x02), Array[Byte](0x01, 0x02, 0x03)).foreach { in =>
      val out = Pkce.base64UrlDecode(Pkce.base64UrlEncode(in))
      assertEquals(out.toSeq, in.toSeq, s"roundtrip mismatch for ${in.toSeq}")
    }
  }

  // ── query string parsing ──────────────────────────────────────────────

  test("parseQueryString handles search-prefixed input") {
    val out = Pkce.parseQueryString("?code=abc&state=xyz")
    assertEquals(out, Map("code" -> "abc", "state" -> "xyz"))
  }

  test("parseQueryString handles hash-prefixed input") {
    val out = Pkce.parseQueryString("#a=1&b=2")
    assertEquals(out, Map("a" -> "1", "b" -> "2"))
  }

  test("parseQueryString URL-decodes values") {
    val out = Pkce.parseQueryString("?email=foo%40bar.com")
    assertEquals(out, Map("email" -> "foo@bar.com"))
  }

  test("parseQueryString preserves '=' inside values (split on first '=' only)") {
    // The original bug: split('=').padTo(2, "") truncated tokens carrying '='.
    // Base64 padding routinely lands here.
    val out = Pkce.parseQueryString("?token=abc=def==")
    assertEquals(out, Map("token" -> "abc=def=="))
  }

  test("parseQueryString returns empty map for empty / pseudo-empty input") {
    assertEquals(Pkce.parseQueryString(""),  Map.empty[String, String])
    assertEquals(Pkce.parseQueryString("?"), Map.empty[String, String])
    assertEquals(Pkce.parseQueryString("#"), Map.empty[String, String])
  }

  test("parseQueryString skips entries that have no '='") {
    val out = Pkce.parseQueryString("?a=1&malformed&b=2")
    assertEquals(out, Map("a" -> "1", "b" -> "2"))
  }

  // ── form encoding ─────────────────────────────────────────────────────

  test("encodeForm percent-encodes spaces and reserved chars") {
    val out = Pkce.encodeForm(Map("scope" -> "openid email profile"))
    assertEquals(out, "scope=openid%20email%20profile")
  }

  test("encodeForm ↔ parseQueryString roundtrip with tricky values") {
    val in = Map("a" -> "1", "b" -> "= space &", "c" -> "x=y=z")
    val encoded = Pkce.encodeForm(in)
    val decoded = Pkce.parseQueryString("?" + encoded)
    assertEquals(decoded, in)
  }

  // ── JWT claims extraction ─────────────────────────────────────────────

  private def jwt(payloadJson: String): String = {
    val header  = Pkce.base64UrlEncode("""{"alg":"RS256"}""".getBytes(StandardCharsets.UTF_8))
    val payload = Pkce.base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8))
    val sig     = Pkce.base64UrlEncode(Array.fill(8)(0.toByte))
    s"$header.$payload.$sig"
  }

  test("claims extracts sub + email from a synthetic id_token") {
    val token = jwt("""{"sub":"u-123","email":"a@b.com","aud":"cli","exp":9999999999}""")
    val c     = Pkce.claims(token).get
    assertEquals(c.sub,   "u-123")
    assertEquals(c.email, Some("a@b.com"))
    assert(c.rawPayload.contains("\"sub\":\"u-123\""))
  }

  test("claims returns None when payload has no sub") {
    val token = jwt("""{"email":"a@b.com"}""")
    assertEquals(Pkce.claims(token), None)
  }

  test("claims returns None for a token with fewer than 2 segments") {
    assertEquals(Pkce.claims("just-one-segment"), None)
    assertEquals(Pkce.claims(""),                  None)
  }

  test("claims returns None for an undecodable payload segment") {
    // '!' is not a base64url character → java.util.Base64 throws.
    assertEquals(Pkce.claims("aaa.!!!.bbb"), None)
  }

  test("claims returns None when payload is not valid JSON") {
    val payload = Pkce.base64UrlEncode("not-json".getBytes(StandardCharsets.UTF_8))
    assertEquals(Pkce.claims(s"aaa.$payload.bbb"), None)
  }

  test("claims survives UTF-8 multi-byte characters in payload") {
    val token = jwt("""{"sub":"u-123","email":"jürgen@bär.de"}""")
    val c     = Pkce.claims(token).get
    assertEquals(c.email, Some("jürgen@bär.de"))
  }

  // ── RFC 7636 §4.2 PKCE conformance vector ─────────────────────────────

  test("RFC 7636 §4.2 vector: base64url(SHA-256(verifier)) == challenge") {
    // The single spec-mandated PKCE test vector. The library's own
    // `sha256Base64Url` goes through `crypto.subtle.digest` which is
    // browser-only, so we re-use Node's `crypto` for the SHA-256 step and
    // feed the resulting bytes through our own `base64UrlEncode` — that's
    // the encoder under test.
    val verifier  = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
    val challenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"

    val crypto = js.Dynamic.global.require("crypto")
    val hex    = crypto.createHash("sha256").update(verifier).digest("hex").asInstanceOf[String]
    val bytes  = hex.grouped(2).map(Integer.parseInt(_, 16).toByte).toArray

    assertEquals(Pkce.base64UrlEncode(bytes), challenge)
  }

  // ── decodeExp ─────────────────────────────────────────────────────────

  test("decodeExp extracts exp from a synthetic id_token") {
    val token = jwt("""{"sub":"u-123","exp":1700000000}""")
    assertEquals(Pkce.decodeExp(token), Some(1700000000L))
  }

  test("decodeExp returns None when exp is absent") {
    val token = jwt("""{"sub":"u-123"}""")
    assertEquals(Pkce.decodeExp(token), None)
  }

  test("decodeExp returns None for malformed jwts") {
    assertEquals(Pkce.decodeExp("just-one-segment"), None)
    assertEquals(Pkce.decodeExp("aaa.!!!.bbb"),       None)
  }
}
