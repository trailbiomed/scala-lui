package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ConfirmDialog private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement
) extends Component {
  private[components] val openVar: Var[Boolean] = Var(false)
  private[components] val titleVar: Var[String] = Var("")
  private[components] val messageVar: Var[String] = Var("")
  private[components] val confirmLabelVar: Var[String] = Var("Confirm")
  private[components] val busyLabelVar: Var[String] = Var("")
  private[components] val cancelLabelVar: Var[String] = Var("Cancel")
  private[components] val busyVar: Var[Boolean] = Var(false)
  private[components] val destructiveVar: Var[Boolean] = Var(false)
  private[components] val confirmBus: EventBus[Unit] = new EventBus[Unit]
  private[components] val dismissBus: EventBus[Unit] = new EventBus[Unit]
}

/** A `Modal` that already owns its footer: a cancel button, a confirm button, the confirm
  * button's progress label, and the rule that the dialog cannot be dismissed while the
  * action it started is in flight.
  *
  * That last part is why this exists rather than being three lines at each call site. Once
  * `confirm` has fired the request has gone, and closing the window does not recall it — so
  * while `busy` is true there is no Escape, no backdrop click, no ×, and neither button
  * responds. Set `busy` for the span of the request and clear `open` when it returns.
  *
  * {{{
  *   ConfirmDialog(
  *     ConfirmDialog.open <--> confirming,
  *     ConfirmDialog.title := "Delete project",
  *     ConfirmDialog.message := "This removes the project and every run under it.",
  *     ConfirmDialog.confirmLabel := "Delete",
  *     ConfirmDialog.busyLabel := "Deleting…",
  *     ConfirmDialog.destructive := true,
  *     ConfirmDialog.busy <-- deleting.signal,
  *     ConfirmDialog.confirm.withCurrentValueOf(selected.signal) --> deleteProject
  *   )
  * }}} */
object ConfirmDialog extends ComponentFactory[ConfirmDialog] {

  val open = Prop.inOut[Boolean, ConfirmDialog](_.openVar)
  val title = Prop.in[String, ConfirmDialog](_.titleVar)

  /** One line of body text. For anything richer, use `body(...)`. */
  val message = Prop.in[String, ConfirmDialog](_.messageVar)

  val confirmLabel = Prop.in[String, ConfirmDialog](_.confirmLabelVar)

  /** What the confirm button reads while `busy`. Defaults to `confirmLabel` with an
    * ellipsis. */
  val busyLabel = Prop.in[String, ConfirmDialog](_.busyLabelVar)

  val cancelLabel = Prop.in[String, ConfirmDialog](_.cancelLabelVar)

  /** True for as long as the confirmed action is in flight. Locks the dialog shut and
    * switches the confirm button to `busyLabel`. */
  val busy = Prop.in[Boolean, ConfirmDialog](_.busyVar)

  /** Styles the confirm button as a destructive action. */
  val destructive = Prop.in[Boolean, ConfirmDialog](_.destructiveVar)

  /** Fires once each time the confirm button is activated. */
  val confirm = Prop.out[Unit, ConfirmDialog](_.confirmBus)

  /** Fires when the user backs out — cancel, Escape, backdrop, or the close button. */
  val dismissed = Prop.out[Unit, ConfirmDialog](_.dismissBus)

  /** Body content in place of `message`. */
  def body(content: Modifier[HtmlElement]*): Mod[ConfirmDialog] = el =>
    el.bodySlot.amend(content*)

  override protected def build: ConfirmDialog = {
    val bodySlot = div(
      typo.body ++ css.lineHeight(1.5)
    )
    // The dialog itself is position: fixed, so this host exists only to own it.
    // `display: contents` keeps it from contributing a row or a gap wherever the
    // caller places the component.
    val root = div(css.display(Display.Contents))
    val el = new ConfirmDialog(root, bodySlot)

    bodySlot.amend(child.text <-- el.messageVar.signal)

    val confirmButton = Button(
      Button.variant <-- el.destructiveVar.signal.map { d =>
        if (d) Button.Variant.Danger else Button.Variant.Primary
      },
      // Not `Button.loading`: that hides the label to keep the button's width, and the
      // progress label is the whole point of the busy state here.
      Button.label <-- Signal
        .combine(el.busyVar.signal, el.confirmLabelVar.signal, el.busyLabelVar.signal)
        .map { case (isBusy, label, busyText) =>
          if (!isBusy) label
          else if (busyText.nonEmpty) busyText
          else s"$label…"
        },
      Button.disabled <-- el.busyVar.signal,
      Button.click --> el.confirmBus.writer
    )

    val cancelButton = Button(
      Button.variant := Button.Variant.Secondary,
      Button.label <-- el.cancelLabelVar.signal,
      Button.disabled <-- el.busyVar.signal,
      Button.click --> el.dismissBus.writer
    )

    root.amend(
      Modal(
        Modal.width := Length.px(420),
        Modal.divided := false,
        Modal.open <--> el.openVar,
        Modal.title <-- el.titleVar.signal,
        Modal.busy <-- el.busyVar.signal,
        Modal.body(bodySlot),
        Modal.footer(cancelButton, confirmButton),
        Modal.close --> el.dismissBus.writer
      ),
      el.dismissBus.events.mapTo(false) --> el.openVar.writer
    )

    el
  }
}
