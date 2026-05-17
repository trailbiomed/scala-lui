package lui.e2e

import com.microsoft.playwright.{BrowserContext, Page}
import munit.FunSuite

/** Per-test fresh BrowserContext + Page; shared Browser + dev server. */
abstract class E2ESuite extends FunSuite {

  private var ctx: BrowserContext = null
  protected var page: Page        = null

  override def beforeAll(): Unit = E2EFixture.ensureStarted()

  override def beforeEach(context: BeforeEach): Unit = {
    ctx  = E2EFixture.browser.newContext()
    page = ctx.newPage()
  }

  override def afterEach(context: AfterEach): Unit = {
    try if (page != null) page.close() catch { case _: Throwable => () }
    try if (ctx  != null) ctx.close()  catch { case _: Throwable => () }
  }

  protected def baseUrl: String = E2EFixture.baseUrl

  /** Navigate to a docs slug (e.g. "button" → http://.../#button) and wait
   *  for the page to settle. `page.navigate` returns at `load`, but Scala.js
   *  bootstraps + commits initial signals on the next tick. Single-locator
   *  waits race the commit; instead poll `document.body.textContent.length`
   *  until it stops growing — that observably maps to "all bound signals
   *  have flushed". */
  protected def gotoSlug(slug: String): Unit = {
    val _ = page.navigate(s"$baseUrl/#$slug")
    page.locator("h1").first().waitFor()
    val _ = page.waitForFunction(
      """() => {
        |  const len = document.body.textContent.length;
        |  if (len < 200) return false;
        |  if (window.__luiE2eLastLen === len) return true;
        |  window.__luiE2eLastLen = len;
        |  return false;
        |}""".stripMargin
    )
  }
}
