package lui.devserver

import com.sun.net.httpserver.{HttpExchange, HttpServer}
import java.net.InetSocketAddress
import java.nio.file.{Files, Path, Paths}

object DevServer {

  private val mime: Map[String, String] = Map(
    "html" -> "text/html; charset=utf-8",
    "htm" -> "text/html; charset=utf-8",
    "js" -> "application/javascript; charset=utf-8",
    "mjs" -> "application/javascript; charset=utf-8",
    "map" -> "application/json; charset=utf-8",
    "json" -> "application/json; charset=utf-8",
    "css" -> "text/css; charset=utf-8",
    "svg" -> "image/svg+xml",
    "png" -> "image/png",
    "jpg" -> "image/jpeg",
    "jpeg" -> "image/jpeg",
    "gif" -> "image/gif",
    "ico" -> "image/x-icon",
    "woff" -> "font/woff",
    "woff2" -> "font/woff2",
    "wasm" -> "application/wasm"
  )

  def start(args: Seq[String]): Unit = {
    val rootArg = args.headOption.getOrElse("example/public")
    val root = Paths.get(rootArg).toAbsolutePath.normalize
    val port = sys.env
      .get("PORT")
      .orElse(args.drop(1).headOption)
      .map(_.toInt)
      .getOrElse(8080)
    val server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0)
    server.createContext("/", ex => handle(root, ex))
    server.setExecutor(null)
    server.start()
    println(s"dev server: http://localhost:$port  (serving $root)")
  }

  private def handle(root: Path, ex: HttpExchange): Unit = {
    val rawPath = ex.getRequestURI.getPath
    val rel = {
      val stripped = rawPath.stripPrefix("/")
      if (stripped.isEmpty) "index.html" else stripped
    }
    val resolved = root.resolve(rel).normalize
    println(s"${ex.getRequestMethod} $rawPath")
    if (!resolved.startsWith(root)) {
      respond(ex, 403, "text/plain; charset=utf-8", "forbidden".getBytes("UTF-8"))
    } else if (!Files.isRegularFile(resolved)) {
      respond(ex, 404, "text/plain; charset=utf-8", s"not found: $rel".getBytes("UTF-8"))
    } else {
      val ext = {
        val idx = rel.lastIndexOf('.')
        if (idx < 0) "" else rel.substring(idx + 1).toLowerCase
      }
      val ct = mime.getOrElse(ext, "application/octet-stream")
      val body = Files.readAllBytes(resolved)
      respond(ex, 200, ct, body)
    }
  }

  private def respond(ex: HttpExchange, status: Int, ct: String, body: Array[Byte]): Unit = {
    ex.getResponseHeaders.set("Content-Type", ct)
    ex.sendResponseHeaders(status, body.length.toLong)
    val os = ex.getResponseBody
    try {
      os.write(body)
    } finally {
      os.close()
    }
  }
}

@main def run(args: String*): Unit = DevServer.start(args)
