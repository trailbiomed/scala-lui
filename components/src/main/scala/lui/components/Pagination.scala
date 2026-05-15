package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class Pagination private[components] (val root: HtmlElement) extends Component {
  private[components] val pageVar: Var[Int] = Var(1)
  private[components] val totalPagesVar: Var[Int] = Var(1)
  private[components] val siblingsVar: Var[Int] = Var(1)
}

/** Numbered-page navigation. `page` is 1-indexed and two-way bindable. `totalPages` is the
  * total page count; `siblings` is how many page numbers to show on each side of the current
  * page (default 1 → renders `1 … 4 [5] 6 … 10`). Pages out of bounds are clamped. */
object Pagination extends ComponentFactory[Pagination] {

  val page = Prop.inOut[Int, Pagination](_.pageVar)
  val totalPages = Prop.in[Int, Pagination](_.totalPagesVar)
  val siblings = Prop.in[Int, Pagination](_.siblingsVar)

  override protected def build: Pagination = {
    val root = navTag()
    val el = new Pagination(root)

    root.amend(
      themed(_ => stack.row(spacing.xs)),
      children <-- Signal
        .combine(el.pageVar.signal, el.totalPagesVar.signal, el.siblingsVar.signal)
        .map { case (current, total, sibs) =>
          buildItems(current, math.max(1, total), math.max(0, sibs)).map {
            case PageItem.Prev =>
              navBtn("‹", "Previous", current > 1, () => el.pageVar.update(p => math.max(1, p - 1)))
            case PageItem.Next =>
              navBtn("›", "Next", current < total, () => el.pageVar.update(p => math.min(total, p + 1)))
            case PageItem.Num(n) =>
              numBtn(n, n == current, () => el.pageVar.set(n))
            case PageItem.Ellipsis =>
              span(
                themed(t => css.padding(Length.px(0), spacing.sm) ++ css.color(t.textSubtle)),
                "…"
              )
          }.toList
        }
    )
    el
  }

  private enum PageItem {
    case Prev
    case Next
    case Num(n: Int)
    case Ellipsis
  }

  private def buildItems(current: Int, total: Int, sibs: Int): Seq[PageItem] = {
    val cur = math.max(1, math.min(total, current))
    val pages = scala.collection.mutable.ListBuffer[Int]()
    pages += 1
    val start = math.max(2, cur - sibs)
    val end = math.min(total - 1, cur + sibs)
    for (p <- start to end) pages += p
    if (total > 1) pages += total

    val deduped = pages.distinct.toList.sorted
    val withGaps = scala.collection.mutable.ListBuffer[PageItem]()
    var prev = 0
    deduped.foreach { p =>
      if (prev != 0 && p - prev > 1) withGaps += PageItem.Ellipsis
      withGaps += PageItem.Num(p)
      prev = p
    }
    PageItem.Prev +: withGaps.toList :+ PageItem.Next
  }

  private def navBtn(glyph: String, ariaLbl: String, enabled: Boolean, go: () => Unit): HtmlElement = {
    val hovered = Var(false)
    button(
      typ := "button",
      aria.label := ariaLbl,
      hovered.signal.styled { (t, hov) =>
        val (bg, fg, bd) =
          if (!enabled) (Color.transparent, t.textSubtle, t.border)
          else if (hov) (t.surfaceDim, t.text, t.border)
          else (t.surface, t.textMuted, t.border)
        cellStyle(bg, fg, bd, enabled)
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onClick.mapToUnit.filter(_ => enabled) --> Observer[Unit](_ => go()),
      glyph
    )
  }

  private def numBtn(n: Int, isCurrent: Boolean, go: () => Unit): HtmlElement = {
    val hovered = Var(false)
    button(
      typ := "button",
      aria.current <-- Signal.fromValue(if (isCurrent) "page" else ""),
      hovered.signal.styled { (t, hov) =>
        val (bg, fg, bd) =
          if (isCurrent) (t.brand, t.onBrand, t.brand)
          else if (hov) (t.surfaceDim, t.text, t.border)
          else (t.surface, t.textMuted, t.border)
        cellStyle(bg, fg, bd, enabled = true) ++
          (if (isCurrent) css.fontWeight(FontWeight.SemiBold) else Style.empty)
      },
      onMouseEnter.mapTo(true) --> hovered.writer,
      onMouseLeave.mapTo(false) --> hovered.writer,
      onClick.mapToUnit.filter(_ => !isCurrent) --> Observer[Unit](_ => go()),
      n.toString
    )
  }

  private def cellStyle(bg: Color, fg: Color, bd: Color, enabled: Boolean): Style =
    stack.centerAll ++
      css.minWidth(Length.px(32)) ++ css.height(Length.px(32)) ++
      css.padding(Length.px(0), spacing.md) ++
      css.fontSize(fontSizes.lg) ++
      css.fontWeight(FontWeight.Medium) ++
      css.color(fg) ++
      css.background(bg) ++
      css.border(Length.px(1), BorderStyle.Solid, bd) ++
      css.borderRadius(radius.sm) ++
      css.cursor(if (enabled) "pointer" else "not-allowed") ++
      css.raw("font-family", "inherit") ++
      css.transition("background", 120)
}
