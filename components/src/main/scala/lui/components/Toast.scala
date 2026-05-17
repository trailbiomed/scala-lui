package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import scala.scalajs.js

/** Global toast channel. Mount `Toast()` once at the app root. From anywhere
  * else call `Toast.show(...)` / `Toast.success(...)` / `Toast.error(...)` etc.
  * Errors use `aria-live="assertive"` so screen readers announce them promptly;
  * other variants use `aria-live="polite"`. */
object Toast {

  enum Variant { case Info, Success, Warning, Error }

  final case class Action(label: String, run: () => Unit)

  final case class Message(
      id: Long,
      text: String,
      variant: Variant,
      durationMs: Double,
      action: Option[Action]
  )

  private val bus: EventBus[Message] = new EventBus[Message]
  private var nextId: Long = 0L

  /** Push a plain `Info` toast with the default duration. Kept for backward
    * compatibility with `Toast.show("Saved.")` callers. */
  def show(text: String): Unit = push(text, Variant.Info, 2400.0, None)

  def info(text: String, durationMs: Double = 2400.0, action: Option[Action] = None): Unit =
    push(text, Variant.Info, durationMs, action)

  def success(text: String, durationMs: Double = 2400.0, action: Option[Action] = None): Unit =
    push(text, Variant.Success, durationMs, action)

  def warning(text: String, durationMs: Double = 3600.0, action: Option[Action] = None): Unit =
    push(text, Variant.Warning, durationMs, action)

  def error(text: String, durationMs: Double = 6000.0, action: Option[Action] = None): Unit =
    push(text, Variant.Error, durationMs, action)

  private def push(text: String, v: Variant, durationMs: Double, action: Option[Action]): Unit = {
    nextId += 1
    bus.writer.onNext(Message(nextId, text, v, durationMs, action))
  }

  def apply(): HtmlElement = {
    val visibleVar: Var[Vector[Message]] = Var(Vector.empty)

    val root = div()

    // Container is fixed at bottom-center. Each toast is a row; up to 5 stacked.
    root.amend(
      themed(_ =>
        css.position("fixed") ++
          css.bottom(Length.px(28)) ++
          css.left(Length.pct(50)) ++
          css.raw("transform", "translateX(-50%)") ++
          css.zIndex(300) ++
          stack.col(spacing.md) ++
          css.alignItems("center") ++
          css.raw("pointer-events", "none")
      ),
      bus.events --> Observer[Message] { msg =>
        visibleVar.update(_.takeRight(4) :+ msg)
        if (msg.durationMs > 0) {
          val _ = js.timers.setTimeout(msg.durationMs) {
            visibleVar.update(_.filterNot(_.id == msg.id))
          }
        }
      },
      children <-- visibleVar.signal.map(_.map(toastEl(_, visibleVar)).toList)
    )

    root
  }

  private def toastEl(msg: Message, vis: Var[Vector[Message]]): HtmlElement = {
    val live = msg.variant match {
      case Variant.Error => "assertive"
      case _             => "polite"
    }
    val r = msg.variant match {
      case Variant.Error => "alert"
      case _             => "status"
    }
    val node = div()
    node.amend(
      Device.reducedMotion.styled { (t, reduce) =>
        val (bg, fg, bd) = colorsFor(t, msg.variant)
        val trans = if (reduce) css.raw("transition", "none") else css.transition("opacity", 180)
        css.background(bg) ++
          css.color(fg) ++
          css.padding(Length.px(10), Length.px(18)) ++
          css.borderRadius(radius.md) ++
          css.fontSize(fontSizes.lg) ++
          css.fontWeight(FontWeight.Medium) ++
          css.border(Length.px(1), BorderStyle.Solid, bd) ++
          css.raw("box-shadow", "0 4px 20px rgba(0,0,0,0.32)") ++
          css.raw("pointer-events", "auto") ++
          css.raw("white-space", "nowrap") ++
          stack.row(spacing.md) ++
          css.alignItems("center") ++
          trans
      },
      role := r,
      aria.live := live,
      span(msg.text),
      msg.action match {
        case Some(a) =>
          button(
            typ := "button",
            themed(t =>
              css.background(Color.transparent) ++
                css.color(t.onBrand) ++
                css.border(Length.px(1), BorderStyle.Solid, t.onBrand.alpha(0.4)) ++
                css.borderRadius(radius.sm) ++
                css.padding(Length.px(2), spacing.md) ++
                css.fontWeight(FontWeight.SemiBold) ++
                css.fontSize(fontSizes.md) ++
                css.cursor("pointer") ++
                css.raw("font-family", "inherit") ++
                css.raw("outline", "none")
            ),
            a.label,
            onClick.preventDefault.mapToUnit --> Observer[Unit] { _ =>
              a.run()
              vis.update(_.filterNot(_.id == msg.id))
            }
          )
        case None => emptyNode
      },
      CloseButton(
        CloseButton.size := CloseButton.Size.Small,
        CloseButton.ariaLabel := "Dismiss notification",
        CloseButton.click.foreach { _ =>
          vis.update(_.filterNot(_.id == msg.id))
        }
      )
    )
    node
  }

  private def colorsFor(t: Theme, v: Variant): (lui.style.Color, lui.style.Color, lui.style.Color) = {
    v match {
      case Variant.Info =>
        if (t.isDark) (t.surface, t.text, t.border)
        else (palette.slate800, palette.white, palette.slate800)
      case Variant.Success => (t.success, t.onBrand, t.successBorder)
      case Variant.Warning => (t.warning, palette.white, t.warningBorder)
      case Variant.Error   => (t.danger, palette.white, t.dangerBorder)
    }
  }
}
