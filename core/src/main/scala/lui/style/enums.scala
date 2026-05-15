package lui.style

enum FontWeight(val toCss: String) {
  case Regular extends FontWeight("400")
  case Medium extends FontWeight("500")
  case SemiBold extends FontWeight("600")
  case Bold extends FontWeight("700")
}

enum BorderStyle(val toCss: String) {
  case Solid extends BorderStyle("solid")
  case Dashed extends BorderStyle("dashed")
  case None extends BorderStyle("none")
}

enum Display(val toCss: String) {
  case Block extends Display("block")
  case Flex extends Display("flex")
  case InlineFlex extends Display("inline-flex")
  case Grid extends Display("grid")
  case None extends Display("none")
  /** The element disappears from the box tree — its children act as
    * direct children of the element's parent. Use for "transparent
    * wrapper" elements like a `Show` toggle that mustn't break the
    * parent's flex / grid layout when visible. */
  case Contents extends Display("contents")
}

enum TextAlign(val toCss: String) {
  case Left extends TextAlign("left")
  case Center extends TextAlign("center")
  case Right extends TextAlign("right")
}
