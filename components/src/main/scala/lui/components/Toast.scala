package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import scala.scalajs.js

/** Global toast channel. Call `Toast.show("message")` from anywhere; the singleton element
  * (mounted via `Toast()`) listens and displays the message. */
object Toast {

  private val bus: EventBus[String] = new EventBus[String]

  def show(message: String): Unit = bus.writer.onNext(message)

  private val durationMs: Double = 2400.0

  def apply(): HtmlElement = {
    val current: Var[Option[String]] = Var(None)
    var handle: js.UndefOr[js.timers.SetTimeoutHandle] = js.undefined

    val root = div()

    root.amend(
      current.signal.styled { (t, opt) =>
        val visible = opt.isDefined
        val (bg, fg) =
          if (t.isDark) (t.surface, t.text) else (palette.slate800, palette.white)
        css.position("fixed") ++
          css.bottom(Length.px(28)) ++
          css.left(Length.pct(50)) ++
          css.zIndex(300) ++
          css.background(bg) ++
          css.color(fg) ++
          css.padding(Length.px(10), Length.px(18)) ++
          css.borderRadius(radius.md) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(FontWeight.Medium) ++
          css.raw("box-shadow", "0 4px 20px rgba(0,0,0,0.32)") ++
          css.opacity(if (visible) 1.0 else 0.0) ++
          css.transition("opacity", 180) ++
          css.raw("transform", "translateX(-50%)") ++
          css.raw("pointer-events", "none") ++
          css.raw("white-space", "nowrap")
      },
      child.text <-- current.signal.map(_.getOrElse("")),
      bus.events --> Observer[String] { msg =>
        handle.foreach(js.timers.clearTimeout)
        current.set(Some(msg))
        handle = js.timers.setTimeout(durationMs) {
          current.set(None)
          handle = js.undefined
        }
      }
    )

    root
  }
}
