package lui.components

import com.raquo.laminar.api.L.{Mod as _, href as htmlHref, *}
import lui.*
import lui.style.*

final class DownloadTrigger private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar: Var[String] = Var("Download")
  private[components] val hrefVar: Var[String] = Var("")
  private[components] val filenameVar: Var[String] = Var("")
}

/** An anchor styled like a button that triggers a file download. */
object DownloadTrigger extends ComponentFactory[DownloadTrigger] {

  val label = Prop.in[String, DownloadTrigger](_.labelVar)
  val href = Prop.in[String, DownloadTrigger](_.hrefVar)
  val filename = Prop.in[String, DownloadTrigger](_.filenameVar)

  override protected def build: DownloadTrigger = {
    val root = a()
    val el = new DownloadTrigger(root)

    root.amend(
      el.interact.state.styled { (t, i) =>
        val bg = if (i.hovered) t.brandHover else t.brand
        stack.row(spacing.sm) ++
          css.padding(Length.px(7), spacing.xl) ++
          css.background(bg) ++
          css.color(t.onBrand) ++
          css.border(Length.px(0), BorderStyle.None, Color.transparent) ++
          css.borderRadius(radius.md) ++
          css.fontWeight(FontWeight.SemiBold) ++
          css.fontSize(fontSizes.xl) ++
          css.cursor("pointer") ++
          css.raw("text-decoration", "none") ++
          css.transition("background", 120)
      },
      htmlHref <-- el.hrefVar.signal,
      download <-- el.filenameVar.signal,
      span("⤓"),
      span(child.text <-- el.labelVar.signal)
    )
    el
  }
}
