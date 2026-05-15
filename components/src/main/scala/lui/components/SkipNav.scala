package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.style.*

/** Keyboard-only "skip to main content" link. Hidden until focused. */
object SkipNav {
  def apply(targetId: String, label: String = "Skip to main content"): HtmlElement = {
    val focused = Var(false)
    a(
      href := s"#$targetId",
      focused.signal.styled { (t, f) =>
        if (f)
          css.position("fixed") ++
            css.raw("top", spacing.md.toCss) ++
            css.raw("left", spacing.md.toCss) ++
            css.padding(spacing.sm, spacing.lg) ++
            css.background(t.brand) ++
            css.color(t.onBrand) ++
            css.borderRadius(radius.md) ++
            css.fontWeight(FontWeight.SemiBold) ++
            css.zIndex(100) ++
            css.raw("text-decoration", "none")
        else
          css.position("absolute") ++
            css.width(Length.px(1)) ++ css.height(Length.px(1)) ++
            css.padding(Length.zero) ++
            css.overflow("hidden") ++
            css.raw("clip", "rect(0,0,0,0)") ++
            css.raw("white-space", "nowrap") ++
            css.raw("border", "0")
      },
      onFocus.mapTo(true) --> focused.writer,
      onBlur.mapTo(false) --> focused.writer,
      label
    )
  }
}
