package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Navbar private[components] (
    val root: HtmlElement,
    private[components] val startContainer: HtmlElement,
    private[components] val centerContainer: HtmlElement,
    private[components] val endContainer: HtmlElement
) extends Component {
  private[components] val stickyVar: Var[Boolean] = Var(true)
  private[components] val borderedVar: Var[Boolean] = Var(true)
  private[components] val variantVar: Var[Navbar.Variant] = Var(Navbar.Variant.Solid)
  private[components] val sizeVar: Var[Navbar.Size] = Var(Navbar.Size.Md)
}

object Navbar extends ComponentFactory[Navbar] {

  enum Variant { case Solid, Subtle, Transparent, Brand }
  enum Size { case Sm, Md, Lg }

  val sticky = Prop.in[Boolean, Navbar](_.stickyVar)
  val bordered = Prop.in[Boolean, Navbar](_.borderedVar)
  val variant = Prop.in[Variant, Navbar](_.variantVar)
  val size = Prop.in[Size, Navbar](_.sizeVar)

  def start(content: Modifier[HtmlElement]*): Mod[Navbar] = el =>
    el.startContainer.amend(content*)

  def center(content: Modifier[HtmlElement]*): Mod[Navbar] = el =>
    el.centerContainer.amend(content*)

  def end(content: Modifier[HtmlElement]*): Mod[Navbar] = el =>
    el.endContainer.amend(content*)

  private def heightFor(s: Size): Length = s match {
    case Size.Sm => Length.px(48)
    case Size.Md => Length.px(56)
    case Size.Lg => Length.px(64)
  }

  private def horizontalPadFor(s: Size): Length = s match {
    case Size.Sm => spacing.xl
    case Size.Md => spacing.xxl
    case Size.Lg => spacing.xxl
  }

  override protected def build: Navbar = {
    val startContainer = div(stack.row(spacing.md))
    val centerContainer = div(stack.row(spacing.md) ++ stack.grow ++ css.justifyContent("center"))
    val endContainer = div(stack.row(spacing.md))

    val root = navTag(startContainer, centerContainer, endContainer)
    val el = new Navbar(root, startContainer, centerContainer, endContainer)

    root.amend(
      Signal
        .combine(el.stickyVar.signal, el.borderedVar.signal, el.variantVar.signal, el.sizeVar.signal)
        .styled { case (t, (isSticky, isBordered, v, sz)) =>
          val bg = v match {
            case Variant.Solid       => t.surface
            case Variant.Subtle      => t.surfaceDim
            case Variant.Transparent => Color.transparent
            case Variant.Brand       => t.brand
          }
          val borderColor = v match {
            case Variant.Brand => t.brandHover
            case _             => t.border
          }
          val borderDecl =
            if (isBordered) css.borderBottom(Length.px(1), BorderStyle.Solid, borderColor)
            else Style.empty
          val stickyDecl =
            if (isSticky) css.position("sticky") ++ css.top(Length.px(0))
            else Style.empty
          val shadowDecl = v match {
            case Variant.Solid => css.raw("box-shadow", "0 2px 4px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06)")
            case Variant.Brand => css.raw("box-shadow", "0 2px 4px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.08)")
            case _             => Style.empty
          }
          val fgDecl = v match {
            case Variant.Brand => fg.set(t.onBrand)
            case _             => Style.empty
          }
          css.background(bg) ++
            fgDecl ++
            borderDecl ++
            shadowDecl ++
            css.height(heightFor(sz)) ++
            css.padding(Length.px(0), horizontalPadFor(sz)) ++
            stack.row(spacing.xl) ++
            css.raw("box-sizing", "border-box") ++
            css.zIndex(30) ++
            stickyDecl
        }
    )

    el
  }
}
