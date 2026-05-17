package lui.e2e

import com.microsoft.playwright.{Browser, BrowserType, Playwright}
import com.sun.net.httpserver.HttpServer
import java.nio.file.{Files, Paths}
import java.util.concurrent.atomic.AtomicBoolean
import lui.devserver.DevServer

/** Single shared dev server + Playwright Browser for the whole test JVM.
 *  Idempotent: every suite calls [[ensureStarted]] in `beforeAll`. */
object E2EFixture {
  private val initialized = new AtomicBoolean(false)
  @volatile private var server: HttpServer = null
  @volatile private var pw: Playwright     = null
  @volatile var browser: Browser           = null
  @volatile var baseUrl: String            = null

  def ensureStarted(): Unit = {
    if (initialized.compareAndSet(false, true)) {
      val root = Paths.get("example/public").toAbsolutePath.normalize
      val scripts = root.resolve("scripts")
      require(
        Files.isDirectory(scripts),
        s"missing $scripts — run `sbt example/fastLinkJS` first (the e2e/test task should do this for you)"
      )
      server  = DevServer.serve(root, 0, "127.0.0.1")
      baseUrl = s"http://127.0.0.1:${server.getAddress.getPort}"
      pw      = Playwright.create()
      browser = pw.chromium().launch(
        new BrowserType.LaunchOptions().setHeadless(true)
      )
      Runtime.getRuntime.addShutdownHook(new Thread(() => {
        try if (browser != null) browser.close() catch { case _: Throwable => () }
        try if (pw      != null) pw.close()      catch { case _: Throwable => () }
        try if (server  != null) server.stop(0)  catch { case _: Throwable => () }
      }))
    }
  }
}
