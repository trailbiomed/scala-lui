---
name: lui-app
version: 1.0.0
description: |
  How to write Scala.js + Laminar applications with the `lui` component library.
  Covers project setup, naming conventions, the inline-style model, the
  Component contract, common Laminar/Scala-3 gotchas, and state-management
  patterns. Use when authoring or reviewing code that imports from
  `lui`, `lui.components`, or `lui.style`.
license: MIT
---

# Building apps with lui

`lui` is a typed UI component library for [Laminar](https://laminar.dev)
(Scala.js). Use this skill when the project depends on `lui-components` (or
`lui-core`) and you're writing app-level UI code.

## Three constraints to keep in mind

1. **No external CSS.** No `.css` files, no `<style>` blocks, no class names.
   Styles are set inline via Scala-side `Style` values.
2. **No npm.** Build is sbt (or `scala --power package` for small projects).
3. **Scala 3, braces only.** Significant-indentation syntax is off. Use
   `class Foo { … }`, `if (c) { … } else { … }`. `enum`, `given`, extension
   methods, opaque types are available.

## Minimum project

```scala
//> using scala 3.3.7
//> using platform scala-js
//> using jsModuleKind module
//> using dep io.github.pityka::lui-components::<version>

import com.raquo.laminar.api.L.{Mod as _, *}
import org.scalajs.dom
import lui.*
import lui.components.*
import lui.style.*

@main def main(): Unit = {
  val mount = dom.document.getElementById("app")
  Theme.signal.foreach { t =>
    dom.document.body.style.backgroundColor = t.bg.toCss
    dom.document.body.style.color = t.text.toCss
  }(unsafeWindowOwner)
  val _ = render(mount, App())
}

def App(): HtmlElement =
  div(
    css.padding(spacing.xxl) ++ stack.col(spacing.lg),
    Heading(1)("Hello, lui."),
    Button(
      Button.label <-- Theme.signal.map(t =>
        if (t.isDark) "☀ light" else "☾ dark"
      ),
      Button.click.foreach(_ => Theme.toggle())
    )
  )
```

A minimal `index.html` needs only `<div id="app"></div>` and a `<script
type="module">` pointing at the linker output. No stylesheet links.

## Module map

| Package | Purpose |
|---|---|
| `lui` | `Component`, `ComponentFactory`, `Prop` (props DSL), `Interactive`, `Device` |
| `lui.style` | `Theme`, `palette`, `spacing`, `radius`, `fontSizes`, `breakpoints`, `Length`, `Color`, `css.*` builders, `Style`, `ThemedStyle`, `themed()`, `stack.*`, `typo.*`, `surface.*` |
| `lui.components` | All UI components (`Button`, `TextInput`, `Modal`, …) |

## Three layers of styling

1. **Static `Style`** — built from `css.*` (e.g. `css.padding(spacing.lg) ++
   css.background(palette.teal600)`). Drops into any tag as a Modifier.
2. **`ThemedStyle`** — built with `themed(t => Style)` or via the `typo.*`,
   `surface.*` presets. Resolves to a Style at render time using the current
   `Theme`. Also a Modifier.
3. **`signal.styled((t, a) => Style)`** — for state-driven styles. Returns a
   Modifier that re-emits inline style whenever the signal or theme changes.

Compose with `++`. Composition rules:

```
Style       ++ Style       = Style
ThemedStyle ++ Style        = ThemedStyle
Style       ++ ThemedStyle  = ThemedStyle  (via extension)
ThemedStyle ++ ThemedStyle  = ThemedStyle
```

CSS is last-wins inside a single Style:

```scala
typo.label ++ css.fontWeight(FontWeight.SemiBold)   // upgrades the weight
```

## Theme tokens vs palette

- **Semantic tokens** (`t.surface`, `t.text`, `t.brand`, `t.danger`, …) flip
  with the theme. Use these for chrome.
- **Raw palette** (`palette.teal600`, `palette.red300`, …) is for data
  encoding (status strips, category colors). Don't reach for these when a
  semantic token would do.

## The Component contract

Every reusable component follows the same five-part pattern:

```scala
final class Foo private[components] (val root: HtmlElement) extends Component {
  private[components] val labelVar = Var("")
  private[components] val clickBus = new EventBus[Unit]
}

object Foo extends ComponentFactory[Foo] {
  val label = Prop.in[String, Foo](_.labelVar)
  val click = Prop.out[Unit, Foo](_.clickBus)

  override protected def build: Foo = {
    val root = div()
    val el = new Foo(root)
    root.amend(
      el.interact.state.styled { (t, i) => /* theme + state → Style */ },
      typo.label,
      child.text <-- el.labelVar.signal,
      onClick.mapToUnit --> el.clickBus.writer
    )
    el
  }
}
```

- Internal state is `Var`s and `EventBus`es, kept `private[components]`.
- Props are declared via `Prop.in`, `Prop.inOut`, `Prop.out`. They expose
  `:=`, `<--`, `-->`, `<-->` to callers.
- `build` is called by `ComponentFactory.apply`, which folds user `Mod`s
  onto the result. Never write `mods.foreach(_(el)); el` yourself.

## Critical Laminar/Scala-3 gotchas

These come up repeatedly. Internalize them.

1. **Hide Laminar's `Mod` alias.** Laminar exports its own `Mod` type that
   collides with `lui.Mod`. Always:
   ```scala
   import com.raquo.laminar.api.L.{Mod as _, *}
   ```

2. **`css.*` is namespaced, not wildcard.** Laminar's `L.*` already exports
   property setters named `background`, `padding`, `color`, …. Write
   `css.background(…)`, never `import lui.style.css.*`.

3. **`Length.px(n)`, not `5.px`.** Laminar's CSS-length traits also export
   `.px` on `Int`/`Double` and shadow the token versions outside
   `lui.style.tokens`.

4. **One `Style` modifier per element.** `Style.apply` calls
   `styleAttr := toCss`, which **replaces** the entire inline style. Two
   separate `Style` modifiers on the same element race; the second wipes
   out the first. Compose with `++` into a single `Style`:
   ```scala
   div(stack.row(spacing.md) ++ css.alignItems("flex-start"), …)
   ```
   not
   ```scala
   div(stack.row(spacing.md), css.alignItems("flex-start"), …)  // broken
   ```

5. **`Signal[Style]` is not a Modifier.** A static `Style` is a Modifier; a
   `ThemedStyle` is a Modifier. A raw `Signal[Style]` is not. Route it
   through the `signal.styled((t, a) => Style)` extension.

6. **Field-name collisions with Laminar exports.** Inside a component, if
   your prop is named `disabled`, `placeholder`, `value`, `download`, etc.,
   the local val shadows Laminar's identically-named export. Alias on import:
   ```scala
   import com.raquo.laminar.api.L.{
     Mod as _,
     value as htmlValue, disabled as htmlDisabled,
     *
   }
   ```

7. **Some tag names disambiguate.** Laminar 17 uses `headerTag` (not
   `header`), `markTag` (not `mark`), `emTag`, `labelTag`. When in doubt,
   check `com.raquo.laminar.api.Laminar` or use Metals.

8. **`-Xfatal-warnings` is on.** `-Wvalue-discard` will fail the build for
   discarded non-Unit returns. Silence intentional discards with `val _ =
   sideEffectingCall(…)`.

9. **`Var.signal.changes` doesn't dedupe.** Two-way bindings (e.g. multiple
   `TextInput.value <--> sharedVar`) will loop unless the writer side is
   distinct. `Prop.inOut` already pipes through `.distinct`; mimic that
   when you build your own two-way bindings.

10. **Components don't satisfy `HtmlElement`.** `Button(...)` returns a
    `Button` (a `Component`, a `Modifier`), not an `HtmlElement`. If a
    helper returns `HtmlElement`, either wrap with `div(Button(...))` or
    widen the return type to `Modifier[HtmlElement]`. For `children <-- …`
    you do need `Node`/`HtmlElement` specifically; use `.root` to extract
    it from a Component.

## State management patterns

- **Single source of truth in `Var`s.** Compose derived state via
  `Signal.combine(...).map(...)`. Don't duplicate the same logical value
  in two Vars.
- **Round-trip via `var.writer.contramap[T](fn)`.** When a control emits a
  different type than your Var (e.g. SegmentedControl emits `String` but
  your Var is `Var[BrewMethod]`):
  ```scala
  SegmentedControl.value <-- methodVar.signal.map(_.toString),
  SegmentedControl.value --> methodVar.writer.contramap[String] { s =>
    BrewMethod.values.find(_.toString == s).getOrElse(BrewMethod.Espresso)
  }
  ```
  Prefer this over `Observer[T](v => myVar.set(...))`; it's the same thing
  with less ceremony.
- **`Signal.now()` is package-private.** Inside an `Owner` you can read
  with `signal.now()`, but from action methods on your state class, build
  a `def currentX(): X` helper that reads from the underlying Vars.
- **Persist state across page changes** by lifting `Var`s to module-level
  `lazy val`s instead of allocating them in `apply()`. Page remounts wipe
  local Vars. Or bubble up application state into higher level Vars. 
  Each component should own its state in their own Vars.

## Layout primitives

When in doubt: `stack.col(gap)` / `stack.row(gap)` for flex, `SimpleGrid` /
`SimpleGrid.autoFit` for grid, `Container` for page gutters, `Wrap` for
flex-wrap-with-gap.

`stack.*` presets:
- Containers: `stack.col(gap)`, `stack.row(gap)`, `stack.between(gap)`,
  `stack.centerAll`.
- Child verbs: `stack.grow`, `stack.noShrink`, `stack.wrap`.

`surface` vs `Surface`:
- `surface` (lowercase, `lui.style.surface`) — `ThemedStyle` values
  (`surface.card`, `surface.dim`) you compose into your own elements.
- `Surface` (capital, `lui.components.Surface`) — element builder
  (`Surface.interactive(...)`) that returns a clickable themed `div`.

## Animation

No `@keyframes`. For animation, drive a `Var[Double]` from a JS
`setInterval` and bind a style off its signal. See `Spinner`,
`StatusBadge(pulsing := true)` for reference.

## Don't

- Don't create `.css` or `.scss` files, or `<style>` blocks.
- Don't use `cls := "..."` or className.
- Don't customize `:focus`; let the browser's focus ring through.
  Hover/pressed states use the `Interactive` helper.
- Don't reach for `npm install`.
- Don't add prop declarations without `Prop.in/inOut/out` — the two-line
  pattern (`val v = Var(...); val x = In(...)`) is dead.
- Don't write `mods.foreach(_(el)); el` in a component factory;
  `ComponentFactory.apply` does it for you.

## Live API lookup (cellar)

The hosted lui docs are an SPA, so they don't grep cleanly from the
terminal. When you need to verify a member, parameter, or signature, use
the `cellar` CLI instead. It pulls Scala sources directly from the published
jar's class files. Examples:

```bash
# Get the full lui-components API.
cellar get-external io.github.pityka::lui-components::<version>

# Drill into a specific component (e.g. Button).
cellar get-external io.github.pityka::lui-components::<version> \
  --filter lui.components.Button

# Same for the foundations.
cellar get-external io.github.pityka::lui-components::<version> \
  --filter lui.style
```

Pair cellar with this skill: this file gives you the patterns, cellar gives
you the exact signatures of the version you're on.

For *Laminar* itself (signal combinators, event modifiers, `child <--`
nuances), use the same pattern against `com.raquo::laminar`.

#### Cellar: Project-aware commands (run from project root)

For querying the current project's code and dependencies (auto-detects build tool):

    cellar get [--module <name>] <fqn>       # single symbol
    cellar list [--module <name>] <package>  # explore a package
    cellar search [--module <name>] <query>  # find by name

- Mill/sbt projects: `--module` is required (e.g. `--module lib`, `--module core`)
- scala-cli projects: `--module` is not supported (omit it)
- `--no-cache`: skip classpath cache, re-extract from build tool
- `--java-home`: override JRE classpath

## Components and props

Two shapes of component exist:

- **Component classes** declare props via `Prop.in/inOut/out` and are built
  through a `ComponentFactory[T]`. Use `Name(Name.prop := value, ...)`.
- **Object helpers** (no Prop declarations) are plain factory functions:
  `Name(arg1, arg2)(content*)` or `Name.method(...)(content*)`.

### Buttons & triggers

| Component | Props (kind) | Notes |
|---|---|---|
| `Button` | `label:in`, `variant:in (Primary/Secondary/Ghost)`, `size:in (Small/Medium)`, `disabled:in`, `loading:in`, `click:out` | Default action surface. |
| `IconButton` | `icon:in`, `ariaLabel:in`, `variant:in`, `size:in`, `disabled:in`, `click:out` | Always pair with `ariaLabel`. |
| `CloseButton` | `size:in (Default/Small)`, `disabled:in`, `ariaLabel:in`, `click:out` | × dismiss control. |
| `DownloadTrigger` | `label:in`, `href:in`, `filename:in` | `<a download>` styled as a primary button. |

### Form controls

| Component | Props (kind) | Notes |
|---|---|---|
| `TextInput` | `value:inOut`, `placeholder:in`, `disabled:in`, `invalid:in`, `variant:in (Text/Number)`, `align:in`, `width:in` | |
| `Textarea` | `value:inOut`, `placeholder:in`, `disabled:in`, `invalid:in`, `rows:in`, `width:in`, `resizable:in` | |
| `NumberInput` | `value:inOut[Double]`, `min:in`, `max:in`, `step:in`, `disabled:in`, `width:in` | Uses `type=text` + stepper to hide native spinners. |
| `PasswordInput` | `value:inOut`, `placeholder:in`, `disabled:in`, `invalid:in`, `width:in` | Reveal button built-in. |
| `PinInput` | `value:inOut`, `length:in`, `mask:in` | Paste fills cells from focused position onward. |
| `TagsInput` | `value:inOut[Seq[String]]`, `placeholder:in`, `disabled:in` | Enter/comma commits draft. |
| `Editable` | `value:inOut`, `placeholder:in`, `editing:inOut` | Click-to-edit text. |
| `FileUpload` | `files:inOut[Seq[dom.File]]`, `multiple:in`, `accept:in`, `label:in` | Drag-and-drop. |
| `Checkbox` | `checked:inOut`, `disabled:in`, `label:in` | |
| `CheckboxCard` | `title:in`, `description:in`, `checked:inOut`, `disabled:in` | Card-shaped checkbox. |
| `RadioGroup` | `value:inOut`, `options:in[Seq[(String,String)]]`, `disabled:in`, `orientation:in` | |
| `RadioCard` | `value:inOut`, `options:in[Seq[RadioCard.Option]]`, `disabled:in`, `orientation:in` | Card-shaped radio. |
| `SegmentedControl` | `value:inOut`, `options:in`, `disabled:in` | Pick-one-of-N button row. |
| `Dropdown` | `value:inOut`, `options:in`, `disabled:in`, `width:in` | Native `<select>` wrapper. |
| `Toggle` | `checked:inOut`, `disabled:in` | Switch. |
| `Slider` | `value:inOut[Double]`, `min:in`, `max:in`, `step:in`, `disabled:in`, `width:in` | Pointer-driven, no `<input range>`. |
| `Field` | `label:in`, `hint:in`, `error:in`, `required:in`, `control(slot)` | Form scaffold; pass the input via `Field.control(...)`. |
| `Fieldset` | `legend:in`, `hint:in`, `body(slot)` | |

### Disclosure

| Component | Props (kind) | Notes |
|---|---|---|
| `Accordion` | `title:in`, `summary:in`, `open:inOut`, `body(slot)` | |
| `Collapsible` | `open:inOut`, `body(slot)` | No header; bring your own toggle. |
| `Tabs` | `tabs:in[Seq[(String,String)]]`, `active:inOut`, `variant:in` | |
| `Breadcrumb` | `items:in[Seq[(String,String)]]`, `select:out` | Emits selected key. |
| `Pagination` | `page:inOut[Int]`, `totalPages:in`, `siblings:in` | 1-based. |
| `Steps` | `steps:in[Seq[String]]`, `current:in[Int]`, `orientation:in` | 0-based current. |

### Feedback

| Component | Props (kind) | Notes |
|---|---|---|
| `Alert` | `title:in`, `variant:in (Info/Success/Warning/Danger)`, `dismissible:in`, `dismiss:out`, `body(slot)` | |
| `EmptyState` | `icon:in`, `title:in`, `description:in`, `action(slot)` | |
| `Spinner` | `size:in` | JS-interval animated. |
| `ProgressBar` | `value:in[Double 0..1]`, `variant:in`, `height:in`, `indeterminate:in` | |
| `ProgressCircle` | `value:in[Double 0..1]`, `variant:in`, `size:in`, `thickness:in`, `showLabel:in` | |
| `Skeleton` | `width:in`, `height:in`, `cornerRadius:in`, `animated:in` | |
| `Toast` | `Toast()` to mount, `Toast.show(msg: String)` to push | Mount once at the app root. |

### Data display

| Component | Props (kind) | Notes |
|---|---|---|
| `Avatar` | `name:in`, `src:in`, `size:in (Xs..Xl)`, `shape:in (Circle/Square)` | Initials from name. |
| `Badge` | `label:in`, `variant:in (Brand/Success/Warning/Danger/Info/Neutral)`, `dot:in` | Counts and dots. |
| `Card` | `interactive:in`, `padding:in`, `click:out`, `children(slot)` | |
| `Tag` | `label:in`, `variant:in (Interesting/Warning/Neutral)`, `removable:in`, `remove:out` | Category chip. |
| `StatusBadge` | `label:in`, `variant:in (Running/Queued/Success/Warning)`, `pulsing:in` | Lifecycle states. |
| `Stat` | `label:in`, `value:in`, `unit:in`, `hint:in`, `trend:in (Up/Down/None)` | Big-number tile. |
| `Timeline` | `items:in[Seq[Timeline.Item]]` | `Item(title, meta, body)`. |
| `DataList` | `items:in[Seq[(String,String)]]`, `orientation:in` | Key/value pairs. |
| `Table` | `columns:in[Seq[String]]`, `rows:in[Seq[Seq[String]]]`, `striped:in` | Strings only. |
| `Clipboard` | `value:in`, `label:in` | Copy-to-clipboard button. |

### Overlays

| Component | Props (kind) | Notes |
|---|---|---|
| `Modal` | `open:inOut`, `title:in`, `width:in`, `body(slot)` | Centered dialog. |
| `Drawer` | `open:inOut`, `width:in`, `title:in`, `side:in (Left/Right)`, `body(slot)` | Side panel. |
| `Tooltip` | `label:in`, `placement:in (Top/Right/Bottom/Left)`, `trigger(slot)` | Hover-only. |
| `Popover` | `open:inOut`, `placement:in`, `trigger(slot)`, `body(slot)` | Click-toggled; building block for the next three. |
| `Menu` | `items:in[Seq[Menu.Item]]`, `select:out[String]`, `trigger(slot)` | `Item(key, label, icon, danger)`. |
| `HoverCard` | `placement:in`, `trigger(slot)`, `body(slot)` | Hover-open Popover. |
| `ToggleTip` | `label:in`, `placement:in`, `trigger(slot)` | Click-open Tooltip. |

### Typography & icons

| Helper | Form | Notes |
|---|---|---|
| `Heading` | `Heading(level: Int = 1)(content*)` | level 1–4. |
| `Text` | `Text(content*)` plus `Text.body/.muted/.hint/.label/.eyebrow` | Themed spans. |
| `Link` | `Link(Link.href := …, Link.external := …, Link.variant := Brand/Muted/Plain, Link.children(...))` | |
| `Listing` | `Listing(style = Bulleted/Numbered/None, gap = …)(items*)` + `Listing.item(content*)` | |
| `Blockquote` | `Blockquote(content*)` | |
| `Mark` | `Mark(content*)` | Yellow highlight. |
| `Em` | `Em(content*)` | Italic emphasis. |
| `Highlight` | `Highlight(text: String, query: String)` | Wraps every match of `query` in `Mark`. |
| `Code` | `Code(Code.text := …, Code.block := …)` | Inline chip or block. |
| `Kbd` | `Kbd(Kbd.key := …)` | Keyboard-key chip. |
| `Icon` | `Icon(size = …, color = Some(Color))(glyph*)` | |
| `Checkmark` | `Checkmark(size = …)` | |
| `Radiomark` | `Radiomark(size = …)` | |
| `ColorSwatch` | `ColorSwatch(c: Color, size, rounded)` | |

### Layout primitives (object factories)

| Helper | Form | Notes |
|---|---|---|
| `Container` | `Container(maxWidth, pad)(content*)` | Centered page gutter. |
| `Center` | `Center(content*)` / `Center.absolute(content*)` | |
| `Bleed` | `Bleed(inline, block)(content*)` | Negative-margin escape. |
| `AspectRatio` | `AspectRatio(ratio = 16.0/9.0)(content*)` | |
| `Wrap` | `Wrap(gap, align)(content*)` | Flex-wrap row. |
| `Group` | `Group(content*)` | Zero-gap attached row. |
| `SimpleGrid` | `SimpleGrid(columns, gap)(content*)` / `SimpleGrid.autoFit(minChildWidth, gap)(content*)` | |
| `ScrollArea` | `ScrollArea(maxHeight, direction)(content*)` | |
| `Divider` | `Divider(Divider.orientation := …, Divider.label := …)` | |
| `Surface` | `Surface.interactive(pad, rad, click, extra)(content*)` | Clickable themed div. |
| `ActionBar` | `ActionBar(content*)` | Sticky bottom bar. |
| `VisuallyHidden` | `VisuallyHidden(content*)` | Screen-reader-only. |
| `SkipNav` | `SkipNav(targetId, label = "Skip to main content")` | |

### App-specific primitives

| Component | Props (kind) |
|---|---|
| `PageHeader` | `title:in`, `back:in`, `onBack:out`, `right(slot)` |
| `SectionLabel` | `text:in` |
| `MetricCell` | `value:in`, `score:in`, `bar:in[Option[Double]]`, `state:in (Idle/Active/Running/Queued)`, `click:out` |
| `ReferenceCard` | `name:in`, `icon:in`, `sourceLabel:in`, `sampleCount:in`, `organism:in`, `description:in`, `lastUsed:in`, `click:out` |

## Design tokens & style primitives (`lui.style`)

### `css.*` — typed Style builders (each returns a one-decl `Style`)

| Group | Builders |
|---|---|
| Color | `background(Color)`, `color(Color)`, `borderColor(Color)`, `boxShadow(String)` |
| Length | `width`, `height`, `minWidth`, `maxWidth`, `minHeight`, `maxHeight`, `padding(l)`, `padding(v, h)`, `margin(l)`, `gap(l)` |
| Border | `border(w, BorderStyle, Color)`, `borderRadius(Length)`, `borderBottom(w, BorderStyle, Color)` |
| Type | `fontSize(Length)`, `fontWeight(FontWeight)`, `letterSpacing(Length)`, `textTransform(String)`, `textAlign(TextAlign)`, `lineHeight(Double)`, `whiteSpace(String)` |
| Flex/layout | `display(Display)`, `flexDirection(String)`, `alignItems(String)`, `justifyContent(String)`, `flexWrap(String)`, `flexShrink(Int)`, `flexGrow(Int)` |
| Position | `position(String)`, `top/right/bottom/left(Length)`, `zIndex(Int)` |
| Anim | `transition(prop: String, ms: Int)`, `transform(String)` |
| Misc | `cursor(String)`, `opacity(Double)`, `overflow/overflowX/overflowY(String)` |
| Escape | `raw(prop: String, value: String)` — use only when no typed builder exists |

### `stack.*` — pure-layout shortcuts (returns `Style`)

| Member | Effect |
|---|---|
| `stack.col(gap)` | `display: flex; flex-direction: column; gap` |
| `stack.row(gap)` | `display: flex; align-items: center; gap` |
| `stack.between(gap)` | `row(gap)` + `justify-content: space-between` |
| `stack.centerAll` | `display: flex; align-items: center; justify-content: center` |
| `stack.wrap` | `flex-wrap: wrap` |
| `stack.noShrink` | `flex-shrink: 0` |
| `stack.grow` | `flex-grow: 1` |

### `typo.*` — themed text presets (each is a `ThemedStyle`)

| Preset | Size / weight / color |
|---|---|
| `typo.eyebrow` | `fontSizes.xs`, Bold, uppercase + letter-spacing, `t.textSubtle` |
| `typo.h1` | `fontSizes.display`, SemiBold, `t.text` |
| `typo.h2` | `fontSizes.xxxl`, SemiBold, `t.text` |
| `typo.label` | `fontSizes.lg`, Medium, `t.text` |
| `typo.body` | `fontSizes.lg`, `t.text` |
| `typo.muted` | `fontSizes.md`, `t.textMuted` |
| `typo.hint` | `fontSizes.sm`, `t.textSubtle` |

### `surface.*` — themed background presets (each is a `ThemedStyle`)

| Preset | Effect |
|---|---|
| `surface.card` | `t.surface` background, 1.5px `t.border`, `radius.xl` |
| `surface.dim` | `t.surfaceDim` background, 1px `t.border`, `radius.md` |

(Don't confuse with the capitalized `Surface` *component* — see Layout primitives above.)

### `spacing` — discrete length scale

| Token | Value |
|---|---|
| `spacing.xs` | 4 px |
| `spacing.sm` | 6 px |
| `spacing.md` | 8 px |
| `spacing.lg` | 12 px |
| `spacing.xl` | 16 px |
| `spacing.xxl` | 24 px |
| `spacing.xxxl` | 32 px |

### `radius` — border-radius scale

| Token | Use |
|---|---|
| `radius.sm` (6 px) | Tags, inline chips, small inputs, code chips |
| `radius.md` (8 px) | Buttons, text inputs, dropdowns, popovers, alerts |
| `radius.lg` (12 px) | Cards, dropzones, drawers, popover-as-card |
| `radius.xl` (16 px) | Hero surfaces (used by `surface.card`) |
| `radius.pill` (9999 px) | Avatars, status dots, slider thumbs, brand pills |

### `fontSizes` — type scale

| Token | Px | Used by |
|---|---|---|
| `fontSizes.xs` | 10 | Eyebrows, smallest hints |
| `fontSizes.sm` | 11 | Hints |
| `fontSizes.md` | 12 | Muted text |
| `fontSizes.lg` | 13 | Body, labels |
| `fontSizes.xl` | 14 | Buttons, inputs |
| `fontSizes.xxl` | 16 | h2 |
| `fontSizes.xxxl` | 18 | Section titles |
| `fontSizes.display` | 20 | h1 |

### `breakpoints` — responsive thresholds

| Token | Px |
|---|---|
| `breakpoints.sm` | 640 |
| `breakpoints.md` | 768 |
| `breakpoints.lg` | 1024 |
| `breakpoints.xl` | 1280 |

Drive responsive logic from `Device.viewportWidth.signal`, but prefer
flex/grid (`SimpleGrid.autoFit`, `Wrap`) when possible.

### `Length` — opaque CSS length

| Factory | Returns |
|---|---|
| `Length.px(Int)` / `Length.px(Double)` | `Npx` |
| `Length.pct(Int)` | `N%` |
| `Length.em(Double)` | `Nem` |
| `Length.rem(Double)` | `Nrem` |
| `Length.auto` | `auto` |
| `Length.zero` | `0` |
| `Length.raw(String)` | escape hatch |

### `Color` — RGB(A)

| Form | Notes |
|---|---|
| `Color(r, g, b, a = 1.0)` | constructor; alpha 0..1 |
| `Color.hex("#rrggbb")` | full-opacity from hex |
| `Color.transparent` | rgba(0,0,0,0) |
| `c.alpha(d: Double)` | new color with replaced alpha |
| `c.toCss` | `rgb(…)` or `rgba(…)` |

### `palette.*` — raw color stops (use for data encoding, not chrome)

| Family | Stops |
|---|---|
| Brand (teal) | `teal50`, `teal100`, `teal200`, `teal400`, `teal500`, `teal600`, `teal700`, `teal900` |
| Neutrals (slate) | `white`, `slate50`–`slate900` |
| Success (emerald) | `emerald50`, `emerald300`, `emerald600`, `emerald700` |
| Danger (red) | `red50`, `red300`, `red600`, `red800` |
| Info (blue) | `blue50`, `blue300`, `blue600` |
| Warning (amber) | `amber50`, `amber100`, `amber300`, `amber700`, `amber800` |
| Backdrop | `palette.backdrop` (semi-transparent black) |

### `Theme` — semantic tokens (`t.*` inside `themed { t => … }`)

| Group | Tokens |
|---|---|
| Surfaces | `t.bg`, `t.surface`, `t.surfaceDim`, `t.backdrop` |
| Borders | `t.border`, `t.borderActive` |
| Text | `t.text`, `t.textMuted`, `t.textSubtle` |
| Brand | `t.brand`, `t.brandSoft`, `t.brandHover`, `t.onBrand` |
| Status — success | `t.success`, `t.successSoft`, `t.successBorder` |
| Status — warning | `t.warning`, `t.warningSoft`, `t.warningBorder` |
| Status — danger | `t.danger`, `t.dangerSoft`, `t.dangerBorder` |
| Status — info | `t.info`, `t.infoSoft`, `t.infoBorder` |

Other Theme members: `t.name: String`, `t.isDark: Boolean`. Global control
via `Theme.current: Var[Theme]`, `Theme.signal: Signal[Theme]`,
`Theme.setLight()` / `Theme.setDark()` / `Theme.toggle()`.

### Enums

| Enum | Cases |
|---|---|
| `FontWeight` | `Regular` (400), `Medium` (500), `SemiBold` (600), `Bold` (700) |
| `BorderStyle` | `Solid`, `Dashed`, `None` |
| `Display` | `Block`, `Flex`, `InlineFlex`, `Grid`, `None` |
| `TextAlign` | `Left`, `Center`, `Right` |
