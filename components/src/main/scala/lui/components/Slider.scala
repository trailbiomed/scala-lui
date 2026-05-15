package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*
import org.scalajs.dom
import scala.scalajs.js

final class Slider private[components] (val root: HtmlElement) extends Component {
  private[components] val valueVar: Var[Double] = Var(0.0)
  private[components] val minVar: Var[Double] = Var(0.0)
  private[components] val maxVar: Var[Double] = Var(100.0)
  private[components] val stepVar: Var[Double] = Var(1.0)
  private[components] val disabledVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(200))
  private[components] val dragging: Var[Boolean] = Var(false)
}

/** Continuous-or-stepped numeric input rendered as a draggable thumb on a track. Avoids the
  * native `<input type="range">` because its thumb styling requires `::-webkit-slider-thumb`
  * pseudo-elements that can't be set inline. Pointer-driven; captures the pointer on
  * mousedown so dragging works even when the cursor leaves the track. */
object Slider extends ComponentFactory[Slider] {

  val value = Prop.inOut[Double, Slider](_.valueVar)
  val min = Prop.in[Double, Slider](_.minVar)
  val max = Prop.in[Double, Slider](_.maxVar)
  val step = Prop.in[Double, Slider](_.stepVar)
  val disabled = Prop.in[Boolean, Slider](_.disabledVar)
  val width = Prop.in[Length, Slider](_.widthVar)

  private val trackHeight: Length = Length.px(4)
  private val thumbSize: Length = Length.px(16)

  override protected def build: Slider = {
    val fill = span()
    val thumb = span()
    val track = div(fill, thumb)
    val root = div(track)
    val el = new Slider(root)

    def fraction: Double = {
      val v = el.valueVar.now()
      val lo = el.minVar.now()
      val hi = el.maxVar.now()
      if (hi <= lo) 0.0 else math.max(0.0, math.min(1.0, (v - lo) / (hi - lo)))
    }

    def setFromClientX(clientX: Double): Unit = {
      val rect = track.ref.getBoundingClientRect()
      val width = rect.width
      if (width <= 0) ()
      else {
        val raw = (clientX - rect.left) / width
        val frac = math.max(0.0, math.min(1.0, raw))
        val lo = el.minVar.now()
        val hi = el.maxVar.now()
        val st = el.stepVar.now()
        val rawValue = lo + frac * (hi - lo)
        val stepped =
          if (st > 0) lo + math.round((rawValue - lo) / st) * st
          else rawValue
        val clamped = math.max(lo, math.min(hi, stepped))
        el.valueVar.set(clamped)
      }
    }

    root.amend(
      el.widthVar.signal.styled { (_, w) =>
        css.display(Display.InlineFlex) ++
          css.alignItems("center") ++
          css.width(w) ++
          css.padding(Length.px(8), Length.px(0)) ++
          css.raw("touch-action", "none")
      }
    )

    track.amend(
      el.disabledVar.signal.styled { (t, d) =>
        css.position("relative") ++
          css.width(Length.pct(100)) ++
          css.height(trackHeight) ++
          css.background(t.surfaceDim) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border) ++
          css.borderRadius(radius.pill) ++
          css.cursor(if (d) "not-allowed" else "pointer")
      },
      onPointerDown --> Observer[dom.PointerEvent] { ev =>
        if (!el.disabledVar.now()) {
          ev.preventDefault()
          track.ref.asInstanceOf[js.Dynamic].setPointerCapture(ev.pointerId)
          el.dragging.set(true)
          setFromClientX(ev.clientX)
        }
      },
      onPointerMove --> Observer[dom.PointerEvent] { ev =>
        if (el.dragging.now() && !el.disabledVar.now()) {
          setFromClientX(ev.clientX)
        }
      },
      onPointerUp --> Observer[dom.PointerEvent] { ev =>
        if (el.dragging.now()) {
          track.ref.asInstanceOf[js.Dynamic].releasePointerCapture(ev.pointerId)
          el.dragging.set(false)
        }
      },
      onPointerCancel --> Observer[dom.PointerEvent] { _ =>
        el.dragging.set(false)
      }
    )

    fill.amend(
      Signal
        .combine(el.valueVar.signal, el.minVar.signal, el.maxVar.signal, el.disabledVar.signal)
        .styled { case (t, (_, _, _, d)) =>
          val pct = fraction * 100.0
          val bg = if (d) t.textSubtle else t.brand
          css.position("absolute") ++
            css.raw("top", "0") ++ css.raw("left", "0") ++
            css.height(Length.pct(100)) ++
            css.raw("width", s"$pct%") ++
            css.background(bg) ++
            css.borderRadius(radius.pill) ++
            css.raw("pointer-events", "none")
        }
    )

    thumb.amend(
      Signal
        .combine(
          el.valueVar.signal,
          el.minVar.signal,
          el.maxVar.signal,
          el.disabledVar.signal,
          el.dragging.signal
        )
        .styled { case (t, (_, _, _, d, drag)) =>
          val pct = fraction * 100.0
          val bg = if (d) t.surfaceDim else t.surface
          val bd = if (d) t.border else t.brand
          val shadow =
            if (drag) s"0 0 0 6px ${t.brand.alpha(0.18).toCss}"
            else "0 1px 3px rgba(0,0,0,0.18)"
          css.position("absolute") ++
            css.raw("left", s"$pct%") ++
            css.raw("top", "50%") ++
            css.width(thumbSize) ++ css.height(thumbSize) ++
            css.borderRadius(radius.pill) ++
            css.background(bg) ++
            css.border(Length.px(2), BorderStyle.Solid, bd) ++
            css.raw("transform", "translate(-50%, -50%)") ++
            css.raw("pointer-events", "none") ++
            css.raw("box-shadow", shadow) ++
            css.transition("box-shadow", 120) ++
            css.raw("box-sizing", "border-box")
        }
    )

    el
  }
}
