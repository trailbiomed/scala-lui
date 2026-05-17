package lui.e2e

import com.microsoft.playwright.{BrowserContext, Page}
import munit.FunSuite
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/** One test per docs slug. Each test navigates to `#<slug>`, waits for the
 *  page-template `<h1>` to render, and fails if any `console.error` or
 *  uncaught page error fired during navigation. Slugs are discovered at
 *  suite-construction time by scraping the sidebar's `[data-slug]` items,
 *  so adding a `DocEntry` automatically grows the matrix — no edits here. */
class AllPagesSmokeSuite extends FunSuite {

  // Boot the shared server + browser eagerly so we can discover slugs
  // before tests are registered.
  E2EFixture.ensureStarted()

  private val slugs: Seq[String] = discoverSlugs()

  private def discoverSlugs(): Seq[String] = {
    val ctx  = E2EFixture.browser.newContext()
    val page = ctx.newPage()
    try {
      val _ = page.navigate(E2EFixture.baseUrl)
      val items = page.locator("[data-slug]")
      items.first().waitFor()
      items.all().asScala.toSeq.map(_.getAttribute("data-slug")).distinct
    } finally {
      try page.close() catch { case _: Throwable => () }
      try ctx.close()  catch { case _: Throwable => () }
    }
  }

  // One Context + Page per test for isolation. Errors collected per test.
  private var ctx: BrowserContext            = null
  private var page: Page                     = null
  private val errors: mutable.ArrayBuffer[String] = mutable.ArrayBuffer.empty

  override def beforeEach(context: BeforeEach): Unit = {
    errors.clear()
    ctx  = E2EFixture.browser.newContext()
    page = ctx.newPage()
    page.onPageError(err => errors += s"pageerror: $err")
    page.onConsoleMessage { msg =>
      if (msg.`type`() == "error") errors += s"console.error: ${msg.text()}"
    }
  }

  override def afterEach(context: AfterEach): Unit = {
    try if (page != null) page.close() catch { case _: Throwable => () }
    try if (ctx  != null) ctx.close()  catch { case _: Throwable => () }
  }

  slugs.foreach { slug =>
    test(s"renders #$slug without runtime errors") {
      val _ = page.navigate(s"${E2EFixture.baseUrl}/#$slug")
      // Every docs page uses PageTemplate which renders an <h1> title.
      page.locator("h1").first().waitFor()
      if (errors.nonEmpty) {
        fail(s"#$slug emitted runtime errors:\n${errors.mkString("\n")}")
      }
    }
  }
}
