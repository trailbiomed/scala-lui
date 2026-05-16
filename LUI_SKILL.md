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
  // One-shot global reset: box-sizing: border-box (everywhere) +
  // font smoothing on <body>. Without box-sizing, `width: 100%` +
  // padding overflows the parent and rounded corners get clipped
  // by `overflow: hidden`; without smoothing, system-ui text on
  // macOS Retina reads notably heavier than Chakra's default.
  reset.install()
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
| `lui` | `Component`, `ComponentFactory`, `Prop` (props DSL), `Interactive`, `Device`, `Day` |
| `lui.style` | `Theme`, `palette`, `spacing`, `radius`, `fontSizes`, `breakpoints`, `Length`, `Color`, `css.*` builders, `Style`, `ThemedStyle`, `themed()`, `stack.*`, `typo.*`, `surface.*`, `reset.install()` |
| `lui.components` | All UI components (`Button`, `TextInput`, `Modal`, …) |
| `lui.plot` (sbt module `lui-plot`, opt-in) | `Plot[K]` — wraps `nspl-canvas-js` and `nspl-svg-js`. Use `Plot.canvas(initial)` or `Plot.svg(initial)` (same prop API; pick the backend) to bind `K` by type inference, then call the returned bundle's `apply(mods*)`. |

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

### Slots: caller-supplied DOM into a named region

`Prop.in[V]` is for reactive *values* (`String`, `Boolean`, enums). When
the thing a caller needs to put into your component is **DOM** —
children, event handlers, conditional `child <-- ...` bindings — use a
**slot method** instead.

A slot is just an internal `HtmlElement` stashed in the Component plus a
static helper on the companion that returns a `Mod[El]` whose only job
is to `amend` caller content onto that internal element:

```scala
final class Modal private[components] (
    val root: HtmlElement,
    private[components] val bodySlot: HtmlElement,   // the named region
) extends Component

object Modal extends ComponentFactory[Modal] {
  def body(content: Modifier[HtmlElement]*): Mod[Modal] = el =>
    el.bodySlot.amend(content*)
}
```

Callers write `Modal(Modal.body(p("hello"), Button(...)))`. The slot
accepts arbitrary Modifiers — components, attributes, `child <-- sig`,
`onClick --> ...`, anything that goes inside any other tag. The
framework doesn't know about slots; the name is a convention.

Use slots for body/header/footer regions of cards, modals, drawers,
popovers — anywhere the chrome is fixed and the contents vary by
caller.

### Recursive composition: state lives in the parent

Once components nest more than one level deep, the natural pattern is:
**the parent owns the source-of-truth `Var`; each child only has a Prop
that's bound to the parent's `Var`**. The child's internal `*Var`
becomes a proxy — `Prop.in` writes the parent's signal into it on each
emission; `Prop.out` raises the child's bus to a sink the parent wires
up. State flows down via `<--`, up via `-->`, two-way via `<-->`.

```scala
// At the parent:
final class App private[components] (val root: HtmlElement) extends Component {
  private[components] val refs:      Var[List[Ref]]      = Var(seed)
  private[components] val activeId:  Var[Option[String]] = Var(None)
  private[components] val selectBus: EventBus[String]    = new EventBus
}

object App extends ComponentFactory[App] {
  override protected def build: App = {
    val el = new App(div())
    el.root.amend(
      Sidebar(
        Sidebar.refs     <-- el.refs.signal,       // down
        Sidebar.activeId <-- el.activeId.signal,   // down
        Sidebar.select   --> el.selectBus.writer,  // up
      ),
      // Two downstream effects from one upstream emission: route both
      // off the shared bus rather than re-emitting the upstream event
      // from two `Sidebar.select --> ...` bindings.
      el.selectBus.events.map(Some(_)) --> el.activeId.writer,
      el.selectBus.events
        .compose(_.withCurrentValueOf(el.refs.signal))
        .map { case (id, refs) =>
          refs.map(r => if (r.id == id) r.touch() else r)
        }
        --> el.refs.writer,
    )
    el
  }
}
```

For dynamic lists where each item is its own Component, use
`Signal[List[T]].split(_.id) { (key, init, sig) => ChildComponent(...).root }`
— `split` only re-instantiates the item Components whose key changed,
so per-item internal state (hover, focus, expanded) survives across list
updates.

### Reaching for internal buses across components in the same package

Most cross-component wiring goes through Props (`Prop.in/out/inOut`),
but occasionally the parent needs to consume *two* downstream effects
from a single child event, or splice an internal stream into another
component's processing. Because component internals are
`private[components]`, the parent in the same package can read a child's
`*Bus.events` or `*Var.signal` directly:

```scala
val saveBtn  = Button(Button.label := "Save", …)
val cancelBtn = Button(Button.label := "Cancel", …)
root.amend(
  saveBtn,
  cancelBtn,
  saveBtn.clicks.compose(_.withCurrentValueOf(formVar.signal)) --> savedBus.writer,
  cancelBtn.clicks.mapTo(false) --> openVar.writer,
)
```

This stays inside the contract — components don't expose internal buses
publicly; the access only works between components in the same
`private[components]` scope.

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

11. **Don't put `display: flex` on `<th>` or `<td>`.** Table cells
    default to `display: table-cell`; overriding that with `flex` (e.g.
    via `stack.col(gap)`) takes the cell out of the table layout — the
    header row floats *next to* the body instead of above it. Wrap the
    cell's content in a child `<div>` and apply `stack.col` there:
    ```scala
    th(
      css.padding(...),
      div(stack.col(spacing.xs), label, filterInput)  // flex on the div, not the th
    )
    ```

12. **`InOut` props have no `.map`/`.foreach` — only `Out` does.** If
    the parent wants to transform the outbound value of an `InOut`
    prop (e.g. tag each emission with a key before forwarding to a
    shared bus), wire the outbound side as a separate `-->` binding to
    a `writer.contramap` Observer:
    ```scala
    // Bind the inbound:
    HeaderCell.filterValue <-- valuesByField.signal.map(_.getOrElse(field, "")),
    // Adapt + forward the outbound:
    HeaderCell.filterValue --> changesBus.writer.contramap[String](v => (field, v)),
    ```
    Don't try `HeaderCell.filterValue.map(...) -->` — `InOut` doesn't
    surface a transform method. Adapting on the writer is the
    binding-style equivalent.

13. **`LockedEventKey` doesn't expose `.collect` / `.mapTo` directly.**
    `onClick.mapToUnit` returns a `LockedEventKey`, not an `EventStream`.
    Chain stream combinators *inside* `.compose(...)`:
    ```scala
    // wrong — .collect is not a member of LockedEventKey
    onClick.mapToUnit
      .compose(_.withCurrentValueOf(openVar.signal))
      .collect { case true => () }
      --> closeBus.writer

    // right — collect inside the compose block
    onClick.mapToUnit.compose(
      _.withCurrentValueOf(openVar.signal).collect { case true => () }
    ) --> closeBus.writer
    ```

14. **`withCurrentValueOf` collapses `Unit` and flattens tuples**
    (Composition typeclass). Pattern-match the result accordingly:
    | Stream type | + signal type | Composed `EventStream[…]` |
    |---|---|---|
    | `EventStream[Unit]` | `Signal[A]` | `EventStream[A]` (Unit drops) |
    | `EventStream[A]` (non-tuple) | `Signal[B]` | `EventStream[(A, B)]` |
    | `EventStream[(A, B)]` | `Signal[C]` | `EventStream[(A, B, C)]` (flat) |
    | `EventStream[A]` | `Signal[B], Signal[C]` | `EventStream[(A, B, C)]` |
    | `EventStream[Unit]` | `Signal[B], Signal[C]` | `EventStream[(B, C)]` |
    So `Button.clicks.compose(_.withCurrentValueOf(sourceVar.signal))` is
    just `EventStream[Source]` — no `._2` projection needed.

## State management patterns

- **Single source of truth in `Var`s.** Compose derived state via
  `Signal.combine(...).map(...)`. Don't duplicate the same logical value
  in two Vars.
- **Prefer binding-style over imperative writes.** Instead of
  `someVar.set(x)` / `someVar.update(...)` inside an `Observer`, build an
  `EventStream` whose emissions are the new value and route it with
  `--> someVar.writer`. Stream-based code is composable, distinct-on-the-
  way-out by default with `Prop.inOut`, and reads top-to-bottom.
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
  with less ceremony. The same trick adapts the outbound side of an
  `InOut` prop when you need to tag emissions before forwarding (see
  gotcha #12).
- **Sample current values with `withCurrentValueOf` instead of `.now()`.**
  When a stream event needs to read another Var's current value (e.g.
  "on click, build the next list from the previous one"):
  ```scala
  buttonClicks
    .compose(_.withCurrentValueOf(refsVar.signal))
    .map { case (id, refs) =>
      refs.map(r => if (r.id == id) r.touch() else r)
    }
    --> refsVar.writer
  ```
  Cleaner than reading `refsVar.now()` inside an `Observer`, and works
  outside of an explicit `Owner`.
- **Two effects from one event = two `-->` bindings.** If an upstream
  emission needs to update two Vars, subscribe twice rather than chaining
  inside an Observer:
  ```scala
  val nextSort: EventStream[(SortKey, SortDir)] = headerClicks
    .compose(_.withCurrentValueOf(keyVar.signal, dirVar.signal))
    .map { case (target, k, d) =>
      if (target == k) (k, flip(d)) else (target, SortDir.Asc)
    }
  // Subscribe twice — each `-->` is its own subscription, both fire.
  nextSort.map(_._1) --> keyVar.writer
  nextSort.map(_._2) --> dirVar.writer
  ```
- **Async data → Var via `EventStream.fromFuture`.** Don't write
  `future.onComplete { case Success(x) => v.set(...) }`. Wrap the future
  into a stream and bind:
  ```scala
  val ready: Future[Loadable[A]] =
    apiCall().map(Loadable.Loaded(_)).recover { case t => Loadable.Failed(t.getMessage) }
  EventStream.fromFuture(ready) --> samplesVar.writer
  ```
- **Auto-clearing toasts / timers → `flatMapSwitch` + `delay`.** Replace
  `setTimeout` callbacks that call `var.set(None)` later with:
  ```scala
  toastBus.events.map(Some(_)) --> toastVar.writer
  toastBus.events
    .flatMapSwitch(_ => EventStream.fromValue(None).delay(3000))
    --> toastVar.writer
  ```
  `flatMapSwitch` cancels any pending clear when a new toast arrives, so
  the latest message always gets its full duration.
- **Simulated latency / mock timers → `EventStream.delay`.** Same pattern
  for a "the LLM is thinking" 600 ms pause — the immediate side-effects
  (`loading := true`, clear prior result) wire off the trigger bus; the
  delayed side-effect (compute + show result) goes through
  `trigger.delay(600).map(parse) --> resultVar.writer`.
- **`Signal.now()` is package-private.** Inside an `Owner` you can read
  with `signal.now()`, but from action methods on your state class, build
  a `def currentX(): X` helper that reads from the underlying Vars.
- **Persist state across page changes.** Each component should own its
  state in its own Vars. When the same logical state spans children, lift
  it to the common parent (see "Recursive composition" above) — the
  parent's Var survives child re-mounts, the children re-bind on the way
  in.

## Persisting state across navigation

Three options, in order of preference:

1. **Sibling chrome that's mounted once.** Anything outside the
   swappable area — header, sidebar, footer, toast bar — sits as a
   sibling of the `child <-- viewSig.map(panelFor)` binder, not
   inside it. It's mounted at app boot and stays mounted across nav.
   The slot pattern (`Workbench.header(...)`, `Workbench.sidebar(...)`,
   `Workbench.main(...)`) makes this explicit: only `main` gets a
   `child <-- ...` swap; the rest are filled once.

2. **Lift state up.** When a panel under the swap area needs its
   semantic state (filters, search text, sort order, draft form
   inputs) to survive nav-away/nav-back, put the `Var`s on a
   long-lived ancestor (App) and feed the panel via Props. The panel
   itself can be torn down and rebuilt freely — its props re-bind on
   mount, so the user sees the previous values. This is the default
   path for state that's *legitimately shared* with the rest of the
   app anyway (the URL-style state of the whole workbench).

3. **Mount once, toggle visibility (`Show`).** When a panel needs to
   keep its **identity** — internal Component state that's painful
   to lift (e.g. virtualized list scroll position, deeply nested
   tab-internal sort keys), a long-running subscription it owns
   (polling stream, websocket), or a third-party widget that resists
   remount (canvas, embedded iframe) — wrap each panel in a `Show`
   sibling and switch which one is visible:
   ```scala
   div(
     stack.col(spacing.lg),
     Show(
       Show.visible <-- viewVar.signal.map(_ == View.Explore),
       Show.content(ExplorePanel(... bindings ...)),
     ),
     Show(
       Show.visible <-- viewVar.signal.map(_ == View.Analyses),
       Show.content(AnalysesPanel(... bindings ...)),
     ),
   )
   ```
   `Show` mounts each subtree once at build time and toggles between
   `display: contents` (transparent — children inherit the parent's
   flex/grid layout) and `display: none`. Subscriptions, internal
   `Var`s, scroll positions, and any third-party imperatively-mounted
   widget all stay alive while hidden.

   Cost: every wrapped panel is alive from app boot — initial data
   fetches fire, intervals tick, DOM exists in memory. For cheap
   panels that's fine; for heavy ones (big chart libs, large
   payloads) prefer option (2) and lift state up to a Var that
   survives the remount.

   Same trick scales to tabs *inside* a long-lived modal — keep
   each tab Component mounted once as a `Show`-wrapped child of the
   modal, toggle on `state.activeTab == TabId.X`. The modal's open
   lifecycle still tears them all down on dismiss.

## Layout primitives

When in doubt: `stack.col(gap)` / `stack.row(gap)` for flex, `SimpleGrid` /
`SimpleGrid.autoFit` for grid, `Container` for page gutters, `Wrap` for
flex-wrap-with-gap.

`stack.*` presets:
- Containers: `stack.col(gap)`, `stack.row(gap)`, `stack.between(gap)`,
  `stack.centerAll`.
- Child verbs: `stack.grow`, `stack.noShrink`, `stack.wrap`, `stack.fill`.

`stack.fill` is the "fill remaining space + unlock the min-content
floor" preset — `flex: 1 1 0; min-width: 0; min-height: 0`. Reach for
it when a flex child needs to scroll its own overflow or wrap an
ellipsis text node; without the `min-*: 0` unlock the child refuses to
shrink below its content's intrinsic size and either grows the parent
or breaks the truncation.

`css.*` also exposes a few compound presets for recurring multi-decl
combinations: `css.ellipsis` (single-line truncation), `css.selectNone`
(disable text selection), `css.italic`, `css.pointerNone` (disable
pointer events — for decorative overlays). These compose with `++` just
like the typed builders.

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
| `Chip` | `label:in`, `active:in`, `disabled:in`, `click:out` | Pill-shaped toggle. Parent owns "which is active" and feeds each chip its `active <-- ...`. |
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
| `Editable` | `value:inOut`, `placeholder:in`, `editing:inOut`, `variant:in (Body/Heading)` | Click-to-edit text. Click the preview, *or* set `editing := true` externally (e.g. from a "Rename" button) — the draft is seeded from `value` and the input is focused either way. Enter commits, Escape cancels, blur commits. `Heading` variant renders a 16-px semibold preview and a bottom-border-only input for click-to-rename-a-title patterns. |
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
| `Calendar` | `value:inOut[Option[Day]]`, `month:inOut[Day]`, `weekStart:in (0=Sun, 1=Mon)`, `min:in[Option[Day]]`, `max:in[Option[Day]]`, `disabledFn:in[Day => Boolean]`, `bordered:in` | Month-grid date selector. No popover — pure presentation. `weekStart` defaults to 1 (Mon-first). |
| `DatePicker` | `value:inOut[Option[Day]]`, `placeholder:in`, `disabled:in`, `width:in`, `min:in[Option[Day]]`, `max:in[Option[Day]]` | Click-to-open date input; built on `Popover` + `Calendar`. Trigger shows the ISO date or placeholder. |

### Disclosure

| Component | Props (kind) | Notes |
|---|---|---|
| `Accordion` | `title:in`, `summary:in`, `open:inOut`, `body(slot)` | |
| `Collapsible` | `open:inOut`, `body(slot)` | No header; bring your own toggle. |
| `Show` | `visible:in`, `content(slot)` | Persistent show/hide. Mounts content once; toggles `display: contents` ↔ `display: none`. Wrapped subtree keeps its internal state, subscriptions, scroll position. See "Persisting state across navigation". |
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
| `Modal` | `open:inOut`, `title:in`, `width:in`, `dismissible:in`, `close:out`, `body(slot)`, `footer(slot)` | Centered dialog. Built-in close × when `dismissible` (default true); footer bar only mounts when `Modal.footer(...)` is supplied. |
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
| `Icon` | `Icon(size = …, color = Some(Color))(glyph*)` | Wrapper. `glyph` is anything — string, emoji, `icons.*`, custom SVG. |
| `icons` | `icons.check`, `icons.search`, `icons.trash`, … | Curated Lucide glyph set (24×24, stroke-based, MIT). Returns `SvgElement`. Recolors via parent `css.color(...)`. Add new ones in `components/.../icons.scala`. |
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

### Plotting (`lui.plot`, sbt module `lui-plot`)

Opt-in subproject that wraps the nspl canvas and SVG renderers. Adds `io.github.pityka::nspl-canvas-js` and `nspl-svg-js` to your classpath; the rest of `lui` doesn't pay for them.

Two factories with identical prop APIs; pick the backend by which one you call and which implicit renderer is in scope:

```scala
import org.nspl.*
import org.nspl.canvasrenderer.*   // for Plot.canvas — brings Renderer[K, CanvasRC]
// or
import org.nspl.svgrenderer.*      // for Plot.svg    — brings Renderer[K, SvgRC]
import lui.plot.*

val data  = (0 to 100).map(i => (i.toDouble, math.sin(i * 0.2)))
val build = xyplot(data -> line())(par.xlab("i").ylab("sin"))

val P = Plot.canvas(build)         // or Plot.svg(build)
P(
  P.width  := 600,
  P.height := 250,
  P.shapeClick.map(e => describe(e.id)) --> msg.writer,
  P.build  <-- buildSignal         // optional — each emission re-renders via nspl's rAF updater
)
```

Backend tradeoffs:
- **`Plot.canvas`** — single DOM node. Fast for many points (thousands+). No per-shape CSS.
- **`Plot.svg`** — one DOM node per shape. Vector output (clean print, copy into Illustrator/Figma, screen reader-friendly). Slower past a few hundred shapes.

`Plot[K]` props (all live on the `Plot.canvas[K]` bundle, *not* on the `Plot` companion):

| Prop | Kind | Notes |
|---|---|---|
| `build` | `InOut[Build[K]]` | Initial comes from `Plot.canvas(initial)`. Emissions on `<-- src` feed nspl's in-place updater; the accumulated zoom/pan event store survives swaps. |
| `width`, `height` | `In[Int]` | Read once at construction. Resizing requires recreating the plot. |
| `enableScroll`, `enableDrag`, `enableCrosshair` | `In[Boolean]` | Mouse-wheel zoom / drag-pan / crosshair overlay. |
| `click` | `Out[Identifier]` | Plot-area mousedown (the bare area, not a shape). |
| `hover`, `unhover`, `shapeClick` | `Out[Plot.PlotEvent]` | `PlotEvent(id, point, MouseEvent)`. The `Point` is in canvas space (same frame as `PlotAreaIdentifier.bounds`). |
| `select` | `Out[collection.Seq[Identifier]]` | shift+drag rectangle. |

**Gotchas:**
- nspl uses `collection.Seq`, not `scala.Seq`. The `select` prop reflects that.
- The hover callback fires at the browser's mousemove rate (coalesced via rAF). Keep observers cheap.
- Inside `Plot.canvas(buildSignal.now())`, you can't call `.now()` on a derived signal (it's package-private). Extract the build function and call it directly: `Plot.canvas(buildFor(freq.now()))`.

## Design tokens & style primitives (`lui.style`)

### `css.*` — typed Style builders (each returns a one-decl `Style`)

| Group | Builders |
|---|---|
| Color | `background(Color)`, `color(Color)`, `borderColor(Color)`, `boxShadow(String)` |
| Length | `width`, `height`, `minWidth`, `maxWidth`, `minHeight`, `maxHeight`, `padding(l)`, `padding(v, h)`, `margin(l)`, `gap(l)` |
| Border | `border(w, BorderStyle, Color)`, `borderRadius(Length)`, `borderTop/Right/Bottom/Left(w, BorderStyle, Color)` |
| Type | `fontSize(Length)`, `fontWeight(FontWeight)`, `fontStyle(String)`, `letterSpacing(Length)`, `textTransform(String)`, `textAlign(TextAlign)`, `textOverflow(String)`, `lineHeight(Double)`, `whiteSpace(String)`, `userSelect(String)` |
| Flex/layout | `display(Display)`, `flexDirection(String)`, `alignItems(String)`, `justifyContent(String)`, `flexWrap(String)`, `flexShrink(Int)`, `flexGrow(Int)`, `flex(grow, shrink, basis)` |
| Position | `position(String)`, `top/right/bottom/left(Length)`, `zIndex(Int)` |
| Anim | `transition(prop: String, ms: Int)`, `transform(String)` |
| Misc | `cursor(String)`, `opacity(Double)`, `overflow/overflowX/overflowY(String)`, `pointerEvents(String)` |
| Compound presets | `ellipsis`, `selectNone`, `italic`, `pointerNone` — multi-decl Styles for recurring combinations |
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
| `stack.fill` | `flex: 1 1 0; min-width: 0; min-height: 0` — fill remaining + unlock the min-content floor (for scrollable / ellipsis children) |

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

### `Day` — calendar date primitive

A small case class `Day(year, month, day)` in the root `lui` package. Used by
`Calendar` and `DatePicker`. No `java.time` polyfill — `js.Date` does the heavy
lifting internally.

| Member | Effect |
|---|---|
| `Day(y, m, d)` | Constructor. `month` is 1..12, `day` is 1..31. |
| `Day.today` | Today in local time. |
| `Day.ofEpochDay(n)` | From days-since-1970-01-01. |
| `Day.fromIso("YYYY-MM-DD")` | Parse; returns `Option[Day]`. |
| `Day.daysInMonth(y, m)` | Static helper; respects leap years. |
| `Day.monthNames`, `Day.monthShort`, `Day.weekdaysMon`, `Day.weekdaysSun` | Label vectors. |
| `.iso` | `"YYYY-MM-DD"`. |
| `.daysInMonth`, `.firstOfMonth` | |
| `.dayOfWeekMon` (0=Mon..6=Sun), `.dayOfWeekSun` (0=Sun..6=Sat) | |
| `.addDays(n)`, `.addMonths(n)` | `addMonths` clamps day-of-month to the new month's length. |
| `.toEpochDay` | Days since 1970-01-01. |
| Extends `Ordered[Day]` | `<`, `>`, `compare` work directly. |

### Enums

| Enum | Cases |
|---|---|
| `FontWeight` | `Regular` (400), `Medium` (500), `SemiBold` (600), `Bold` (700) |
| `BorderStyle` | `Solid`, `Dashed`, `None` |
| `Display` | `Block`, `Flex`, `InlineFlex`, `Grid`, `None` |
| `TextAlign` | `Left`, `Center`, `Right` |
