package lui

import com.raquo.laminar.keys.AriaAttr
import com.raquo.laminar.codecs.StringAsIsCodec

/** Aria attributes not exposed as named members on Laminar's `aria.*`. Constructed
  * directly so they behave like any other typed Laminar attr (`:=`, `<--`).
  *
  * Note: prefer Laminar's `aria.label`, `aria.expanded`, `aria.labelledBy`, etc.
  * when available; only reach for these when the attribute is missing upstream
  * or has a stricter codec than what we need (e.g. `aria.hasPopup: Boolean`
  * vs. the spec which also allows `"menu" | "dialog" | "listbox" | …`). */
object AriaExtras {

  /** `aria-modal="true|false"` — denotes a modal-style overlay. */
  val ariaModal: AriaAttr[String] = new AriaAttr[String]("modal", StringAsIsCodec)

  /** `aria-haspopup` as a String (allows the full enum: "menu", "dialog",
    * "listbox", "tree", "grid", "true", "false"). */
  val ariaHasPopupStr: AriaAttr[String] = new AriaAttr[String]("haspopup", StringAsIsCodec)

  /** `aria-orientation="horizontal|vertical"` — already typed in Laminar but
    * re-exposed here for terseness. */
  val ariaOrientation: AriaAttr[String] = new AriaAttr[String]("orientation", StringAsIsCodec)

  /** `aria-expanded` as a String. Laminar's typed `aria.expanded: Boolean` uses
    * an attribute-presence codec, so `false` removes the attribute entirely
    * instead of writing `aria-expanded="false"`. The latter is what ATs (and
    * tests) actually check, so use this when both states need to be observable
    * in the DOM. */
  val ariaExpandedStr: AriaAttr[String] = new AriaAttr[String]("expanded", StringAsIsCodec)
}
