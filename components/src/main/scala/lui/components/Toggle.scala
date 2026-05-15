package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Toggle private[components] (val root: HtmlElement) extends Component {
  private[components] val checkedVar: Var[Boolean] = Var(false)
  private[components] val disabledVar: Var[Boolean] = Var(false)
}

object Toggle extends ComponentFactory[Toggle] {

  val checked = Prop.inOut[Boolean, Toggle](_.checkedVar)

  val disabled = Prop.in[Boolean, Toggle](_.disabledVar)

  private val trackWidth: Length = Length.px(36)
  private val trackHeight: Length = Length.px(20)
  private val thumbSize: Length = Length.px(16)

  override protected def build: Toggle = {
    val root = label()
    val el = new Toggle(root)

    root.amend(
      Signal.combine(el.checkedVar.signal, el.disabledVar.signal).styled { case (t, (on, d)) =>
        val bg = (on, d) match {
          case (_, true)  => t.surfaceDim
          case (true, _)  => t.brand
          case (false, _) => t.border
        }
        css.position("relative") ++
          stack.row() ++
          css.width(trackWidth) ++
          css.height(trackHeight) ++
          css.borderRadius(radius.pill) ++
          css.background(bg) ++
          css.cursor(if (d) "not-allowed" else "pointer") ++
          css.transition("background", 150)
      },
      span(el.checkedVar.signal.styled { (t, on) =>
        val tx = if (on) trackWidth.toCss + " - " + thumbSize.toCss + " - 2px" else "2px"
        css.position("absolute") ++
          css.top(Length.px(2)) ++
          css.width(thumbSize) ++
          css.height(thumbSize) ++
          css.borderRadius(radius.pill) ++
          css.background(if (on) t.onBrand else t.surface) ++
          css.transition("transform", 150) ++
          css.raw("transform", if (on) s"translateX(calc($tx))" else "translateX(2px)") ++
          css.raw("box-shadow", "0 1px 2px rgba(0,0,0,0.25)")
      }),
      onClick.mapToUnit.filter(_ => !el.disabledVar.now()) -->
        Observer[Unit](_ => el.checkedVar.update(c => !c))
    )

    el
  }
}
