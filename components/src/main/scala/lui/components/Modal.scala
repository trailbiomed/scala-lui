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
  private[components] val busyVar: Var[Boolean] = Var(false)
  private[components] val dividedVar: Var[Boolean] = Var(true)
  private[components] val hasFooterVar: Var[Boolean] = Var(false)
  private[components] val closeBus: EventBus[Unit] = new EventBus

  /** `dismissible` and not `busy`. */
  private[components] val canDismiss: Signal[Boolean] =
    Signal.combine(dismissibleVar.signal, busyVar.signal).map { case (d, b) => d && !b }

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

  /** While `true` no Escape, backdrop click or close button dismisses the dialog,
    * whatever `dismissible` says. Set it for the span of a request the dialog started,
    * which closing the window would not recall. */
  val busy = Prop.in[Boolean, Modal](_.busyVar)

  /** The rule under the header and the footer's fill and rule. `true` by default; turn it
    * off for a short dialog that would otherwise read as three stacked bands. */
  val divided = Prop.in[Boolean, Modal](_.dividedVar)

  val close = Prop.out[Unit, Modal](_.closeBus)

  /** Apply arbitrary modifiers to the dialog card, the element inside the backdrop that
    * carries the header, body and footer. */
  def attr(mods: Modifier[HtmlElement]*): Mod[Modal] = el => el.cardEl.amend(mods*)

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
    val footerSlot = div(stack.row(spacing.md) ++ css.justifyContent("flex-end"))
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
        .filter(_ => el.openVar.now() && el.dismissibleVar.now() && !el.busyVar.now())
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
        el.dividedVar.signal.styled { (t, dividedOn) =>
          stack.between(spacing.md) ++
            css.alignItems("flex-start") ++
            css.padding(spacing.md, spacing.xxl) ++
            css.borderBottom(
              Length.px(1),
              BorderStyle.Solid,
              if (dividedOn) t.border else Color.transparent
            )
        },
        div(
          idAttr := labelId,
          themed(t =>
            css.fontSize(fontSizes.xxl) ++
              css.fontWeight(FontWeight.SemiBold) ++
              css.color(t.text)
          ),
          child.text <-- el.titleVar.signal,
        ),
        child.maybe <-- el.canDismiss.map { d =>
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
              el.dividedVar.signal.styled { (t, dividedOn) =>
                css.padding(spacing.md, spacing.xxl) ++
                  css.background(if (dividedOn) t.surfaceDim else t.surface) ++
                  css.borderTop(
                    Length.px(1),
                    BorderStyle.Solid,
                    if (dividedOn) t.border else Color.transparent
                  )
              },
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
      close = () => if (el.dismissibleVar.now() && !el.busyVar.now()) el.closeBus.writer.onNext(()),
      trapFocus = true
    )

    el
  }
}
