package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import scala.scalajs.js
import scala.scalajs.js.timers.SetIntervalHandle

final class Skeleton private[components] (val root: HtmlElement) extends Component {
  private[components] val widthVar: Var[Length] = Var(Length.pct(100))
  private[components] val heightVar: Var[Length] = Var(Length.px(12))
  private[components] val radiusVar: Var[Length] = Var(radius.sm)
  private[components] val animatedVar: Var[Boolean] = Var(true)
}

/** A pulsing placeholder block for content that is still loading. Stack multiple to mock a
  * paragraph or card. Set `animated := false` for static placeholders (cheaper, no timer). */
object Skeleton extends ComponentFactory[Skeleton] {

  val width = Prop.in[Length, Skeleton](_.widthVar)
  val height = Prop.in[Length, Skeleton](_.heightVar)
  val cornerRadius = Prop.in[Length, Skeleton](_.radiusVar)
  val animated = Prop.in[Boolean, Skeleton](_.animatedVar)

  override protected def build: Skeleton = {
    val root = div()
    val el = new Skeleton(root)

    val opacity: Var[Double] = Var(1.0)
    var handle: js.UndefOr[SetIntervalHandle] = js.undefined

    root.amend(
      Signal
        .combine(
          el.widthVar.signal,
          el.heightVar.signal,
          el.radiusVar.signal,
          el.animatedVar.signal,
          opacity.signal
        )
        .styled { case (t, (w, h, r, anim, op)) =>
          css.display(Display.Block) ++
            css.width(w) ++ css.height(h) ++
            css.borderRadius(r) ++
            css.background(t.surfaceDim) ++
            css.border(Length.px(1), BorderStyle.Solid, t.border) ++
            css.opacity(if (anim) op else 1.0) ++
            css.transition("opacity", 700)
        },
      onMountUnmountCallback(
        mount = _ => {
          handle = js.timers.setInterval(700.0) {
            if (el.animatedVar.now()) {
              opacity.update(o => if (o > 0.75) 0.45 else 1.0)
            }
          }
        },
        unmount = _ => {
          handle.foreach(js.timers.clearInterval)
          handle = js.undefined
        }
      )
    )
    el
  }
}
