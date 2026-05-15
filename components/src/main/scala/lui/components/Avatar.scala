package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Avatar private[components] (val root: HtmlElement) extends Component {
  private[components] val nameVar: Var[String] = Var("")
  private[components] val srcVar: Var[String] = Var("")
  private[components] val sizeVar: Var[Avatar.Size] = Var(Avatar.Size.Md)
  private[components] val shapeVar: Var[Avatar.Shape] = Var(Avatar.Shape.Circle)
}

/** Circular or square avatar. Renders the image at `src` if non-empty; otherwise falls back
  * to the first letter of each space-separated word in `name` (up to two — "Alice Brown"
  * → "AB"). Background and text color use brand tokens so the fallback always reads. */
object Avatar extends ComponentFactory[Avatar] {

  enum Size { case Xs, Sm, Md, Lg, Xl }
  enum Shape { case Circle, Square }

  val name = Prop.in[String, Avatar](_.nameVar)
  val src = Prop.in[String, Avatar](_.srcVar)
  val size = Prop.in[Size, Avatar](_.sizeVar)
  val shape = Prop.in[Shape, Avatar](_.shapeVar)

  override protected def build: Avatar = {
    val root = div()
    val el = new Avatar(root)

    root.amend(
      Signal.combine(el.sizeVar.signal, el.shapeVar.signal, el.srcVar.signal).styled {
        case (t, (sz, sh, srcUrl)) =>
          val dim = dimFor(sz)
          val fsz = fontFor(sz)
          val rad = sh match {
            case Shape.Circle => radius.pill
            case Shape.Square => radius.md
          }
          val base =
            stack.centerAll ++
              css.width(dim) ++ css.height(dim) ++
              css.borderRadius(rad) ++
              css.fontSize(fsz) ++
              css.fontWeight(FontWeight.SemiBold) ++
              css.background(t.brandSoft) ++
              css.color(t.brand) ++
              css.border(Length.px(1), BorderStyle.Solid, t.border) ++
              css.overflow("hidden") ++
              css.raw("user-select", "none") ++
              stack.noShrink

          if (srcUrl.nonEmpty)
            base ++
              css.raw("background-image", s"url('$srcUrl')") ++
              css.raw("background-size", "cover") ++
              css.raw("background-position", "center") ++
              css.raw("color", "transparent")
          else base
      },
      child.text <-- Signal.combine(el.srcVar.signal, el.nameVar.signal).map {
        case (srcUrl, _) if srcUrl.nonEmpty => ""
        case (_, n)                         => initials(n)
      }
    )
    el
  }

  private def initials(name: String): String = {
    val parts = name.trim.split("\\s+").filter(_.nonEmpty)
    val letters = parts.take(2).map(_.head.toUpper).mkString
    if (letters.nonEmpty) letters else "?"
  }

  private def dimFor(s: Size): Length = s match {
    case Size.Xs => Length.px(20)
    case Size.Sm => Length.px(26)
    case Size.Md => Length.px(32)
    case Size.Lg => Length.px(40)
    case Size.Xl => Length.px(56)
  }

  private def fontFor(s: Size): Length = s match {
    case Size.Xs => fontSizes.xs
    case Size.Sm => fontSizes.sm
    case Size.Md => fontSizes.md
    case Size.Lg => fontSizes.lg
    case Size.Xl => fontSizes.xxl
  }
}
