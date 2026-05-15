package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class EmptyState private[components] (
    val root: HtmlElement,
    private[components] val actionSlot: HtmlElement
) extends Component {
  private[components] val iconVar: Var[String] = Var("")
  private[components] val titleVar: Var[String] = Var("")
  private[components] val descriptionVar: Var[String] = Var("")
}

/** A centered placeholder for empty lists, tables, or panels. The `action` slot is for a
  * call-to-action — typically a `Button` to create the first item. */
object EmptyState extends ComponentFactory[EmptyState] {

  val icon = Prop.in[String, EmptyState](_.iconVar)
  val title = Prop.in[String, EmptyState](_.titleVar)
  val description = Prop.in[String, EmptyState](_.descriptionVar)

  /** Slot for the call-to-action element(s). */
  def action(content: Modifier[HtmlElement]*): Mod[EmptyState] = el =>
    el.actionSlot.amend(content*)

  override protected def build: EmptyState = {
    val iconEl = div()
    val titleEl = div()
    val descEl = div()
    val actionSlot = div(stack.row(spacing.md))
    val root = div(iconEl, titleEl, descEl, actionSlot)
    val el = new EmptyState(root, actionSlot)

    root.amend(
      themed(_ =>
        stack.col(spacing.lg) ++
          css.alignItems("center") ++
          css.padding(spacing.xxxl, spacing.xl) ++
          css.textAlign(TextAlign.Center)
      )
    )

    iconEl.amend(
      el.iconVar.signal.styled { (t, ic) =>
        css.display(if (ic.nonEmpty) Display.Flex else Display.None) ++
          css.alignItems("center") ++ css.justifyContent("center") ++
          css.width(Length.px(48)) ++ css.height(Length.px(48)) ++
          css.borderRadius(radius.pill) ++
          css.background(t.surfaceDim) ++
          css.color(t.textMuted) ++
          css.fontSize(fontSizes.display) ++
          css.border(Length.px(1), BorderStyle.Solid, t.border)
      },
      child.text <-- el.iconVar.signal
    )

    titleEl.amend(
      el.titleVar.signal.styled { (t, ttl) =>
        css.fontSize(fontSizes.xxl) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.color(t.text) ++
          css.display(if (ttl.nonEmpty) Display.Block else Display.None)
      },
      child.text <-- el.titleVar.signal
    )

    descEl.amend(
      el.descriptionVar.signal.styled { (t, d) =>
        css.fontSize(fontSizes.lg) ++
          css.color(t.textMuted) ++
          css.maxWidth(Length.px(360)) ++
          css.lineHeight(1.5) ++
          css.display(if (d.nonEmpty) Display.Block else Display.None)
      },
      child.text <-- el.descriptionVar.signal
    )

    el
  }
}
