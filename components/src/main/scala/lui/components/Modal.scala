package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Modal private[components] (
    val root: HtmlElement,
    private[components] val cardEl: HtmlElement,
    private[components] val bodySlot: HtmlElement,
    private[components] val footerSlot: HtmlElement,
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val widthVar: Var[Length] = Var(Length.px(380))
  private[components] val titleVar: Var[String] = Var("")
  private[components] val dismissibleVar: Var[Boolean] = Var(true)
  private[components] val hasFooterVar: Var[Boolean] = Var(false)
  private[components] val closeBus: EventBus[Unit] = new EventBus

  /** Fires whenever the user dismisses the modal — backdrop click, Escape, the
    * built-in close button, or any external sink wired to `Modal.close`. */
  val closes: EventStream[Unit] = closeBus.events
}

object Modal extends ComponentFactory[Modal] {

  val open  = Prop.inOut[Boolean, Modal](_.openVar)
  val title = Prop.in[String, Modal](_.titleVar)
  val width = Prop.in[Length, Modal](_.widthVar)

  /** When `false`, clicks on the backdrop and Escape do not close the modal,
    * and the built-in close button is hidden. Defaults to `true`. */
  val dismissible = Prop.in[Boolean, Modal](_.dismissibleVar)

  val close = Prop.out[Unit, Modal](_.closeBus)

  def body(content: Modifier[HtmlElement]*): Mod[Modal] = el =>
    el.bodySlot.amend(content*)

  def footer(content: Modifier[HtmlElement]*): Mod[Modal] = el => {
    el.hasFooterVar.writer.onNext(true)
    el.footerSlot.amend(content*)
  }

  private val titleId: () => String = {
    var n = 0
    () => { n += 1; s"lui-modal-title-$n" }
  }

  override protected def build: Modal = {
    val bodySlot   = div()
    val footerSlot = div()
    val cardEl     = div()
    val root       = div(cardEl)

    val el = new Modal(root, cardEl, bodySlot, footerSlot)
    val labelId = titleId()

    root.amend(
      Signal
        .combine(el.openVar.signal, Device.reducedMotion)
        .styled { case (t, (isOpen, _)) =>
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
      // Backdrop click closes when dismissible. Card stops propagation below.
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
      role := "dialog",
      AriaExtras.ariaModal := "true",
      aria.labelledBy := labelId,
      onClick.stopPropagation.mapToUnit --> Observer[Unit](_ => ()),
      div(
        themed(t =>
          stack.between(spacing.md) ++
            css.alignItems("flex-start") ++
            css.padding(spacing.md, spacing.xxl) ++
            css.borderBottom(Length.px(1), BorderStyle.Solid, t.border)
        ),
        div(
          idAttr := labelId,
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
      div(
        css.padding(spacing.lg, spacing.xxl),
        bodySlot,
      ),
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

    // Escape + focus trap + initial focus + restore. Only fires Escape→close
    // when the modal is dismissible.
    Overlay.install(
      containerEl = cardEl,
      openSignal = el.openVar.signal,
      close = () => if (el.dismissibleVar.now()) el.closeBus.writer.onNext(()),
      trapFocus = true
    )

    el
  }
}
