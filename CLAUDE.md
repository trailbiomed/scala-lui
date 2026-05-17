# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

**lui** is a typed UI component library for [Laminar](https://laminar.dev) (Scala.js), built with three deliberate constraints:

1. **No external CSS.** No `.css` files, no `<style>` blocks, no class names. All styling is set as inline `style="..."` via Scala-side `Style` values. See `DESIGN.md` §3.
2. **No npm. No Vite.** Build is sbt only; serve is a JDK-only static HTTP server in `devserver/`.
3. **Scala 3 with braces only.** Significant-indentation syntax is disabled via `-no-indent`. Use `class Foo { … }`, `if (c) { … } else { … }`, `match { case … => … }`. `enum`, `given`/`using`, extension methods, opaque types are all available.

## Dev loop

Two terminals:

```bash
sbt ~fastLinkJS       # incremental Scala.js compile → example/public/scripts/
sbt devserver/run     # serves example/public/ on http://localhost:8080
```

**Important:** `sbt compile` alone does NOT refresh the browser. It produces `.class` files but not the JS bundle. The bundle only updates when `fastLinkJS` runs. If you don't have `sbt ~fastLinkJS` running, every code change requires a manual `sbt example/fastLinkJS` (otherwise the browser keeps loading stale chunks).

Compile-only check (useful when iterating without a browser): `sbt compile` or, when Metals MCP is connected, `mcp__metals__compile-full`.

## Scala code style
1. functional
2. no inline comments, make code legible without comments, even for those comment never reference the state before your change
3. braceful
4. always prefer built-in lui components, design tokens, and other abstractions whenever available.
5. minimize the use of amend(), and set()
6. laminar tag factories accept both Seq[Modifier[]] and Modifier[], no need for `val seq: Seq[Mod].. ; div(seq*)`
7. minimize inline Observer[]{}, .now, .set, .update; prefer --> , contramap, filter, a bus

## Project layout

```
core/                                  Library — DSL primitives and styling
  src/main/scala/lui/
    Component.scala                    trait Component + ComponentFactory + Mod type
    Prop.scala                         In / Out / InOut + Prop.in/out/inOut helpers
    Interactive.scala                  hover/focus/pressed Vars; Interactive.on(host)
    Device.scala                       Device.inputMode / Device.viewportWidth signals
    style/
      Style.scala                      Decl, opaque Style, css.* builders
      Color.scala                      RGB(a) with .toCss and .alpha
      Length.scala                     opaque Length + Length.px(n)/pct(n)/em(d) factories
      enums.scala                      FontWeight, BorderStyle, Display, TextAlign
      tokens.scala                     palette, radius, spacing, fontSizes, breakpoints
      Theme.scala                      Theme case class + .light / .dark + Theme.signal
      presets.scala                    stack.{col,row,between,…}, typo.{h1,h2,…}, surface.{card,dim}
      ThemedStyle.scala                ThemedStyle (Modifier), signal.styled extension, themed(...)

components/                            Stock components — all extend Component
  src/main/scala/lui/components/       21 files: Button, Card, Tag, …, Surface, Spinner

example/                               Demo app — two design surfaces wired together
  public/index.html                    Minimal HTML; body reset is the only inline page-style
  public/scripts/                      Linker output (gitignored)
  src/main/scala/example/
    Main.scala                         @main + App router + light/dark toggle + Toast mount

devserver/                             ~80-line JDK HttpServer subproject
  src/main/scala/lui/devserver/Main.scala

```

Add new components both tothe example/ documentation page and to the LUI_SKILL.md machine readable markdown file.

## Component contract (the load-bearing pattern)

Every component looks like this — five concerns, in order:

```scala
final class Foo private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar  = Var("")          // 1. internal state Vars
  private[components] val clickBus  = new EventBus[Unit]
}

object Foo extends ComponentFactory[Foo] {             // 2. extend ComponentFactory[Foo]
  val label = Prop.in[String, Foo](_.labelVar)         // 3. props via Prop.in/out/inOut
  val click = Prop.out[Unit, Foo](_.clickBus)

  override protected def build: Foo = {                // 4. build root + amend defaults
    val root = div()
    val el = new Foo(root)
    root.amend(
      el.interact.state.styled { (t, i) => /* theme + state → Style */ },
      typo.label,
      child.text <-- el.labelVar.signal,
      onClick.mapToUnit --> el.clickBus.writer
    )
    el                                                 // 5. return el — ComponentFactory folds mods*
  }
}
```

Ceremony that no longer exists (don't add it back):

- `mods.foreach(_(el))` — provided by `ComponentFactory.apply`.
- `el.interact.track` — `interact` is a `lazy val` on `Component`; listeners install on first access.
- `val xStyle: Signal[Style] = Signal.combine(Theme.signal, X).map { … }` + a wrapper — replaced by inline `X.styled { (t, x) => … }`.

## Critical Scala/Laminar gotchas

These bite repeatedly:

1. **Always alias `Mod` out of Laminar's wildcard import:**
   ```scala
   import com.raquo.laminar.api.L.{Mod as _, *}
   ```
   Laminar exports its own `Mod` type alias; without this hide, our `Mod[El]` type and Laminar's clash.

2. **`css.*` is namespaced, not wildcard-imported.** Laminar's `L.*` exports CSS property setters (`background`, `padding`, `color`, …) that collide. Always write `css.background(...)`, `css.padding(...)`. Never `import lui.style.css.*`.

3. **Use `Length.px(n)` / `Length.pct(n)` / `Length.em(d)` factories** for any one-off length. Don't use bare `5.px` outside `lui.style.tokens.scala` — Laminar's CSS-length trait exports identically-named `.px` extensions on `Int` that shadow ours and resolve to a different type.

4. **`Signal[Style]` is NOT a Modifier.** A static `Style` is a Modifier (drops straight into a tag), and `ThemedStyle` is a Modifier (so `div(typo.h1, …)` works), but a raw `Signal[Style]` needs to go through the `signal.styled((t, a) => Style)` extension. There's no implicit conversion — Modifier's contravariance defeats it for nested tag types.

5. **Field-name collisions with Laminar exports.** Inside a component object, if your prop val is named the same as a Laminar export (`disabled`, `placeholder`, `value`, `onClick`), the local val shadows. Either rename the prop or alias the Laminar name on import:
   ```scala
   import com.raquo.laminar.api.L.{Mod as _, value as htmlValue, disabled as htmlDisabled, *}
   ```

6. **HTML tag names.** Laminar 17 exposes plain names for most tags (`label`, `select`, `option`). The exceptions disambiguate clashes — `headerTag` instead of `header`, `selectTag` is also accepted. When in doubt, ask Metals: `mcp__metals__inspect com.raquo.laminar.api.Laminar`.

7. **`-Xfatal-warnings`** is on. `-Wunused:all` and `-Wvalue-discard` will fail the build for unused vals, unused params, discarded non-Unit return values. Use `val _ = render(...)` to silence intentional discards.

8. **State keyed by data, not by derived strings.** When wiring "expand the clicked row" UI, key the open-state `Var[Option[K]]` by something unique to the row (a barcode, an ID). Don't key by a name derived from grouping axes — duplicate groups can produce identical names and you'll see two panels open at once.

## Styling pattern guide

Three layers, smallest to largest:

- **Static decls** — `css.background(palette.teal600) ++ css.padding(spacing.lg)`. Just a `Style`; passes directly into any tag.
- **Themed decls** — `typo.h1`, `surface.card`, or `themed(t => css.color(t.brand))`. Each is a `ThemedStyle` (a Modifier). Compose with `++`: `typo.h1 ++ css.margin(Length.px(0))`. CSS last-wins, so later decls override earlier ones (e.g. `typo.label ++ css.fontWeight(FontWeight.SemiBold)` upgrades `Medium → SemiBold`).
- **State-driven** — `someSignal.styled { (t, a) => Style }` returns a Modifier that re-emits inline style on every state or theme change.

Reuse via these presets:

- `stack.col(gap)` / `stack.row(gap)` / `stack.between(gap)` — common flex shapes (`row`/`between` set `align-items: center`).
- `stack.grow` / `stack.noShrink` / `stack.wrap` / `stack.centerAll` — flex-child verbs.
- `typo.{eyebrow,h1,h2,label,body,muted,hint}` — themed text presets.
- `surface.card` / `surface.dim` — themed background panels.

`Surface.interactive(pad, rad, click, extra)(content*)` factory in `components/` builds the "hover-bordered clickable card" pattern in one call. Used by `Result.moduleRow`, `Workbench.refSourceOption`, `ExperimentMapView.experimentChip`, `ConditionsView.sampleRow`.

## Theme rules for components

Never hard-code `palette.*` colors in components — they break dark mode. Use the semantic theme tokens:

- Backgrounds: `t.bg`, `t.surface`, `t.surfaceDim`, `t.backdrop`
- Borders: `t.border`, `t.borderActive`
- Text: `t.text`, `t.textMuted`, `t.textSubtle`
- Brand: `t.brand`, `t.brandSoft`, `t.brandHover`, `t.onBrand`
- Status: `t.success`/`successSoft`/`successBorder`, `t.warning`/…, `t.danger`/…, `t.info`/…

The palette tokens are only for data-bearing visuals (e.g. annotation strips in `Result.heatmapTab`) where the color encodes a category, not chrome.

## Don't

- Don't create `*.css`, `*.scss`, or `<style>` blocks.
- Don't use `cls := "..."` or `className`. The `class` HTML attribute is unused.
- Don't customize `:focus` styling — let the browser's native focus ring through. Only `:hover` and pressed-like state are simulated via the JS-backed `Interactive` helper.
- Don't add `@keyframes`. For continuous animation use a JS `setInterval` driving a `Var[Double]` (see `Spinner`, `StatusBadge.pulsing`).
- Don't reach for `npm install`. The build is sbt-only.
- Don't add a new `Var` and `In` declaration without `Prop.in[V, El](_.xxxVar)`. The two-line pattern is dead.
- Don't write `mods.foreach(_(el)); el` in a component factory. `ComponentFactory` does it; the inherited `final def apply` ensures it's never forgotten.

## Other files of note

- **`DESIGN.md`** — current architectural walkthrough. Read this when adding non-trivial abstractions or trying to understand why the codebase is shaped the way it is.
- **`NEXT.md`** — the original planning document. Mostly accurate as architecture but some details (API names, `themed` usage, prop helpers, `Length` factories) have evolved during implementation. Treat as historical context, not as the spec.
- **`README.md`** — three-line dev-loop pointer.
