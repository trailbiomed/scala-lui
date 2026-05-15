package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Render `text` with any occurrences of `query` wrapped in `Mark`. Case-insensitive. */
object Highlight {
  def apply(text: String, query: String): HtmlElement = {
    val parts = split(text, query)
    span(typo.body, parts.map {
      case Highlighted(s) => Mark(s)
      case Plain(s)       => span(s)
    })
  }

  private sealed trait Part { def s: String }
  private final case class Highlighted(s: String) extends Part
  private final case class Plain(s: String) extends Part

  private def split(text: String, query: String): List[Part] = {
    if (query.isEmpty || text.isEmpty) List(Plain(text))
    else {
      val ql = query.toLowerCase
      val lower = text.toLowerCase
      val buf = scala.collection.mutable.ListBuffer.empty[Part]
      var i = 0
      while (i < text.length) {
        val idx = lower.indexOf(ql, i)
        if (idx < 0) {
          buf += Plain(text.substring(i))
          i = text.length
        } else {
          if (idx > i) buf += Plain(text.substring(i, idx))
          buf += Highlighted(text.substring(idx, idx + query.length))
          i = idx + query.length
        }
      }
      buf.toList
    }
  }
}
