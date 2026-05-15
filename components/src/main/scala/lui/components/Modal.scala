package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Modal private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement,
    private[components] val footerSlot: HtmlElement,
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(380))
  private[components] val titleVar: Var[String] = Var("")
  private[components] val dismissibleVar: Var[Boolean] = Var(true)
  private[components] val hasFooterVar: Var[Boolean] = Var(false)
  private[components] val closeBus: EventBus[Unit] = new EventBus

  /** Fires whenever the user dismisses the modal — backdrop click,
    * the built-in close button, or any external sink wired to
    * `Modal.close`. Parents that already bind `open <--> openVar`
    * don't need this; it's exposed for side-effecting on dismiss
    * (e.g. resetting a draft form). */
  val closes: EventStream[Unit] = closeBus.events
}

object Modal extends ComponentFactory[Modal] {

  val open  = Prop.inOut[Boolean, Modal](_.openVar)
  val title = Prop.in[String, Modal](_.titleVar)
  val width = Prop.in[Length, Modal](_.widthVar)

  /** When `false`, clicks on the backdrop do not close the modal and
    * the built-in close button is hidden. Defaults to `true`. */
  val dismissible = Prop.in[Boolean, Modal](_.dismissibleVar)

  /** Fires on every dismiss action. The modal also flips `openVar`
    * to false internally, so most callers wiring `open <--> openVar`
    * don't need this. */
  val close = Prop.out[Unit, Modal](_.closeBus)

  /** Body slot — main scrollable content area between the title row
    * and the footer. */
  def body(content: Modifier[HtmlElement]*): Mod[Modal] = el =>
    el.bodySlot.amend(content*)

  /** Footer slot — sits below the body inside the card, separated by
    * a top border on a dim surface. Typical contents: a row of
    * action buttons (`Cancel`, `Save`, …). When **no** footer mods
    * are supplied, the footer bar is not rendered at all. */
  def footer(content: Modifier[HtmlElement]*): Mod[Modal] = el => {
    el.hasFooterVar.writer.onNext(true)
    el.footerSlot.amend(content*)
  }

  override protected def build: Modal = {
    val bodySlot   = div()
    val footerSlot = div()
    val cardEl     = div()
    val root       = div(cardEl)

    val el = new Modal(root, bodySlot, footerSlot)

    root.amend(
      el.openVar.signal.styled { (t, isOpen) =>
        css.position("fixed") ++
          css.top(Length.px(0)) ++
          css.right(Length.px(0)) ++
          css.bottom(Length.px(0)) ++
          css.left(Length.px(0)) ++
          css.background(t.backdrop) ++
          css.zIndex(40) ++
          css.display(if (isOpen) Display.Flex else Display.None) ++
          css.alignItems("center") ++
          css.justifyContent("center") ++
          css.padding(spacing.xl)
      },
      onClick.mapToUnit
        .filter(_ => el.openVar.now() && el.dismissibleVar.now())
        --> el.closeBus.writer,
      el.closeBus.events.mapTo(false) --> el.openVar.writer,
    )

    cardEl.amend(
      el.widthVar.signal.styled { (t, w) =>
        css.background(t.surface) ++
          css.borderRadius(radius.xl) ++
          css.width(w) ++
          css.maxWidth(Length.pct(100)) ++
          css.boxShadow("0 24px 60px rgba(0,0,0,0.32)") ++
          stack.col() ++
          css.overflow("hidden")
      },
      onClick.stopPropagation.mapToUnit --> Observer[Unit](_ => ()),
      // Title row: title text + (when dismissible) the close button.
      div(
        themed(t =>
          stack.between(spacing.md) ++
            css.alignItems("flex-start") ++
            css.padding(spacing.md, spacing.xxl) ++
            css.borderBottom(Length.px(1), BorderStyle.Solid, t.border)
        ),
        div(
          themed(t =>
            css.fontSize(fontSizes.xxl) ++
              css.fontWeight(FontWeight.SemiBold) ++
              css.color(t.text)
          ),
          child.text <-- el.titleVar.signal,
        ),
        child.maybe <-- el.dismissibleVar.signal.map { d =>
          if (d) Some(CloseButton(CloseButton.click.mapTo(()) --> el.closeBus.writer).root)
          else None
        },
      ),
      // Body
      div(
        css.padding(spacing.lg, spacing.xxl),
        bodySlot,
      ),
      // Footer — wrap only mounted when the caller used `Modal.footer(...)`.
      child.maybe <-- el.hasFooterVar.signal.map { has =>
        if (has)
          Some(
            div(
              themed(t =>
                stack.row(spacing.md) ++
                  css.justifyContent("flex-end") ++
                  css.padding(spacing.md, spacing.xxl) ++
                  css.background(t.surfaceDim) ++
                  css.borderTop(Length.px(1), BorderStyle.Solid, t.border)
              ),
              footerSlot,
            )
          )
        else None
      },
    )

    el
  }
}
