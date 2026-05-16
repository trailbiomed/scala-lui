package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.style.*
import lui.components.*

object ComponentModelPage {

  def apply(): HtmlElement = PageTemplate(
    title = "Component model",
    summary = "How to assemble lui components into applications that scale beyond a single screen."
  )(

    // -----------------------------------------------------------------------
    PageTemplate.section("The five-part contract")(
      PageTemplate.paragraph(
        "Every reusable component in lui follows the same shape. Five concerns in order: " +
          "an internal state class extending Component, a companion extending ComponentFactory[T], " +
          "Prop declarations, a build method that constructs the root, and the el value returned at the end."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """final class Foo private[components] (val root: HtmlElement) extends Component {
            |  private[components] val labelVar = Var("")          // 1. internal state Vars
            |  private[components] val clickBus = new EventBus[Unit]
            |}
            |
            |object Foo extends ComponentFactory[Foo] {            // 2. extend ComponentFactory[Foo]
            |  val label = Prop.in[String, Foo](_.labelVar)        // 3. props via Prop.in / .out / .inOut
            |  val click = Prop.out[Unit, Foo](_.clickBus)
            |
            |  override protected def build: Foo = {               // 4. build root + amend defaults
            |    val root = div()
            |    val el = new Foo(root)
            |    root.amend(
            |      el.interact.state.styled { (t, i) => /* theme + state -> Style */ },
            |      typo.label,
            |      child.text <-- el.labelVar.signal,
            |      onClick.mapToUnit --> el.clickBus.writer
            |    )
            |    el                                                // 5. return el
            |  }
            |}""".stripMargin
      ),
      PageTemplate.paragraph(
        "Consumers use only the prop DSL: := for static, <-- for inbound, --> for outbound, <--> for two-way."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Prop kinds")(
      PageTemplate.paragraph(
        "Props are the typed surface of a component. They route caller-supplied values into the " +
          "internal Vars and EventBuses without exposing them."
      ),
      Listing()(
        Listing.item(span(typo.body, b("Prop.in[V, El]"), " — inbound only. Caller sets a value or binds a Signal. Use for label, disabled, variant, options.")),
        Listing.item(span(typo.body, b("Prop.out[V, El]"), " — outbound only. Caller subscribes to events. Use for click, submit, dismiss.")),
        Listing.item(span(typo.body, b("Prop.inOut[V, El]"), " — two-way. Caller can read and write, distinct-on-the-way-out by default. Use for value, open, checked, active."))
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// Static value:
            |Button(Button.label := "Save")
            |
            |// Bind from a Signal (one-way in):
            |Button(Button.label <-- mode.signal.map(m => if (m.isDark) "Light" else "Dark"))
            |
            |// Subscribe to an Out (one-way out):
            |Button(Button.click.foreach(_ => Theme.toggle()))
            |Button(Button.click --> someBus.writer)
            |
            |// Two-way to a Var:
            |TextInput(TextInput.value <--> draftVar)""".stripMargin
      ),
      PageTemplate.paragraph(
        "InOut props expose <-- and --> separately as well, which matters when the outbound side " +
          "needs adapting before forwarding. See gotcha 12 below."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Slots: caller-supplied DOM into named regions")(
      PageTemplate.paragraph(
        "Prop.in is for reactive values (String, Boolean, enums). When a caller needs to put " +
          "DOM into your component — children, event handlers, conditional child <-- bindings — " +
          "expose a slot method instead."
      ),
      PageTemplate.paragraph(
        "A slot is an internal HtmlElement stashed in the Component plus a static helper " +
          "on the companion that returns a Mod[El] whose only job is to amend caller content onto " +
          "that internal element."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """final class Modal private[components] (
            |    val root: HtmlElement,
            |    private[components] val bodySlot: HtmlElement   // the named region
            |) extends Component
            |
            |object Modal extends ComponentFactory[Modal] {
            |  def body(content: Modifier[HtmlElement]*): Mod[Modal] = el =>
            |    el.bodySlot.amend(content*)
            |}
            |
            |// Caller-side:
            |Modal(
            |  Modal.open <--> openVar,
            |  Modal.body(
            |    p("Are you sure?"),
            |    Button(Button.label := "Confirm", Button.click --> confirmBus.writer)
            |  )
            |)""".stripMargin
      ),
      PageTemplate.paragraph(
        "Slots accept arbitrary Modifiers — components, attributes, child <-- sig, onClick --> ... — " +
          "anything that goes inside any other tag. Use slots for body / header / footer regions of cards, modals, " +
          "drawers, popovers — anywhere the chrome is fixed and the contents vary by caller."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Recursive composition: state lives in the parent")(
      PageTemplate.paragraph(
        "Once components nest more than one level deep, the natural pattern is that the parent " +
          "owns the source-of-truth Var and each child has only a Prop bound to it. The child's " +
          "internal *Var becomes a proxy: Prop.in writes the parent's signal into it on each " +
          "emission, Prop.out raises the child's bus to a sink the parent wires up."
      ),
      PageTemplate.paragraph(
        "State flows down via <--, up via -->, two-way via <-->."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """final class App private[components] (val root: HtmlElement) extends Component {
            |  private[components] val refs:      Var[List[Ref]]      = Var(seed)
            |  private[components] val activeId:  Var[Option[String]] = Var(None)
            |  private[components] val selectBus: EventBus[String]    = new EventBus
            |}
            |
            |object App extends ComponentFactory[App] {
            |  override protected def build: App = {
            |    val el = new App(div())
            |    el.root.amend(
            |      Sidebar(
            |        Sidebar.refs     <-- el.refs.signal,       // down
            |        Sidebar.activeId <-- el.activeId.signal,   // down
            |        Sidebar.select   --> el.selectBus.writer   // up
            |      ),
            |      // Two downstream effects from one upstream emission: route both off the
            |      // shared bus rather than re-emitting from two `Sidebar.select --> ...` bindings.
            |      el.selectBus.events.map(Some(_)) --> el.activeId.writer,
            |      el.selectBus.events
            |        .compose(_.withCurrentValueOf(el.refs.signal))
            |        .map { case (id, refs) =>
            |          refs.map(r => if (r.id == id) r.touch() else r)
            |        }
            |        --> el.refs.writer
            |    )
            |    el
            |  }
            |}""".stripMargin
      ),
      PageTemplate.paragraph(
        "For dynamic lists where each item is its own Component, use split. It only re-instantiates " +
          "the item Components whose key changed, so per-item internal state (hover, focus, expanded) " +
          "survives across list updates."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// Signal[List[Ref]] -> stable Components per id
            |refs.signal.split(_.id) { (key, init, sig) =>
            |  RefRow(RefRow.data <-- sig).root
            |}""".stripMargin
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Three styling layers")(
      PageTemplate.paragraph(
        "Components do not own bespoke CSS; they compose three reusable layers. Smallest to largest:"
      ),
      Listing()(
        Listing.item(span(typo.body, b("Static Style"), " — built from css.* (e.g. css.padding(spacing.lg) ++ css.background(palette.teal600)). Drops into any tag as a Modifier.")),
        Listing.item(span(typo.body, b("ThemedStyle"), " — built with themed(t => Style) or via typo.* / surface.* presets. Resolves to a Style at render time using the current Theme. Also a Modifier.")),
        Listing.item(span(typo.body, b("signal.styled((t, a) => Style)"), " — for state-driven styles. Returns a Modifier that re-emits inline style whenever the signal or theme changes."))
      ),
      PageTemplate.paragraph("Compose with ++. Composition rules:"),
      Code(
        Code.block := true,
        Code.text :=
          """Style       ++ Style       = Style
            |ThemedStyle ++ Style       = ThemedStyle
            |Style       ++ ThemedStyle = ThemedStyle    // via extension
            |ThemedStyle ++ ThemedStyle = ThemedStyle""".stripMargin
      ),
      PageTemplate.paragraph(
        "CSS is last-wins inside a single Style. typo.label ++ css.fontWeight(FontWeight.SemiBold) " +
          "upgrades the weight from Medium to SemiBold."
      ),
      PageTemplate.paragraph(
        "Never hard-code palette.* colors in components — they break dark mode. Use semantic theme " +
          "tokens (t.bg, t.surface, t.text, t.brand, t.success, …). Raw palette is for data " +
          "encoding (status strips, category colors) where the color is the meaning, not chrome."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("State management patterns")(
      PageTemplate.paragraph(
        "These patterns reappear across every nontrivial lui app. Internalize them before reaching " +
          "for ad-hoc Observers."
      ),

      Heading(4)("Single source of truth in Vars"),
      PageTemplate.paragraph(
        "Compose derived state via Signal.combine(...).map(...). Do not duplicate the same logical " +
          "value in two Vars; pick one canonical owner and derive the rest."
      ),

      Heading(4)("Prefer binding-style over imperative writes"),
      PageTemplate.paragraph(
        "Instead of someVar.set(x) inside an Observer, build an EventStream whose emissions are the " +
          "new value and route it with --> someVar.writer. Stream-based code is composable, " +
          "distinct-on-the-way-out by default with Prop.inOut, and reads top-to-bottom."
      ),

      Heading(4)("Round-trip via writer.contramap"),
      PageTemplate.paragraph(
        "When a control emits a different type than your Var (e.g. SegmentedControl emits String " +
          "but your Var is Var[BrewMethod]):"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """SegmentedControl.value <-- methodVar.signal.map(_.toString),
            |SegmentedControl.value --> methodVar.writer.contramap[String] { s =>
            |  BrewMethod.values.find(_.toString == s).getOrElse(BrewMethod.Espresso)
            |}""".stripMargin
      ),

      Heading(4)("Sample current values with withCurrentValueOf"),
      PageTemplate.paragraph(
        "When a stream event needs to read another Var's current value (on click, build the next " +
          "list from the previous), prefer compose(_.withCurrentValueOf(...)) over reading .now() " +
          "inside an Observer:"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """buttonClicks
            |  .compose(_.withCurrentValueOf(refsVar.signal))
            |  .map { case (id, refs) =>
            |    refs.map(r => if (r.id == id) r.touch() else r)
            |  }
            |  --> refsVar.writer""".stripMargin
      ),

      Heading(4)("Two effects from one event = two --> bindings"),
      PageTemplate.paragraph(
        "If an upstream emission needs to update two Vars, subscribe twice rather than chaining " +
          "inside an Observer. Each --> is its own subscription, both fire."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """val nextSort = headerClicks
            |  .compose(_.withCurrentValueOf(keyVar.signal, dirVar.signal))
            |  .map { case (target, k, d) =>
            |    if (target == k) (k, flip(d)) else (target, SortDir.Asc)
            |  }
            |nextSort.map(_._1) --> keyVar.writer
            |nextSort.map(_._2) --> dirVar.writer""".stripMargin
      ),

      Heading(4)("Async data → Var via EventStream.fromFuture"),
      Code(
        Code.block := true,
        Code.text :=
          """val ready: Future[Loadable[A]] =
            |  apiCall()
            |    .map(Loadable.Loaded(_))
            |    .recover { case t => Loadable.Failed(t.getMessage) }
            |
            |EventStream.fromFuture(ready) --> samplesVar.writer""".stripMargin
      ),

      Heading(4)("Auto-clearing toasts / timers"),
      PageTemplate.paragraph(
        "Replace setTimeout callbacks that call var.set(None) later with flatMapSwitch + delay. " +
          "flatMapSwitch cancels any pending clear when a new toast arrives, so the latest message " +
          "always gets its full duration."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """toastBus.events.map(Some(_)) --> toastVar.writer
            |toastBus.events
            |  .flatMapSwitch(_ => EventStream.fromValue(None).delay(3000))
            |  --> toastVar.writer""".stripMargin
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Persisting state across navigation")(
      PageTemplate.paragraph(
        "Three options, in order of preference."
      ),

      Heading(4)("1. Sibling chrome that's mounted once"),
      PageTemplate.paragraph(
        "Anything outside the swappable area — header, sidebar, footer, toast bar — sits as a " +
          "sibling of the child <-- viewSig.map(panelFor) binder, not inside it. It is mounted at " +
          "app boot and stays mounted across nav. The slot pattern (Workbench.header(...), " +
          "Workbench.sidebar(...), Workbench.main(...)) makes this explicit: only main gets a " +
          "child <-- swap; the rest are filled once."
      ),

      Heading(4)("2. Lift state up"),
      PageTemplate.paragraph(
        "When a panel under the swap area needs its semantic state (filters, search text, sort " +
          "order, draft form inputs) to survive nav-away/nav-back, put the Vars on a long-lived " +
          "ancestor (App) and feed the panel via Props. The panel itself can be torn down and " +
          "rebuilt freely — its props re-bind on mount, so the user sees the previous values."
      ),

      Heading(4)("3. Mount once, toggle visibility (Show)"),
      PageTemplate.paragraph(
        "When a panel needs to keep its identity — internal Component state that's painful to " +
          "lift (virtualized list scroll position, deeply nested tab-internal sort keys), a " +
          "long-running subscription it owns (polling stream, websocket), or a third-party widget " +
          "that resists remount (canvas, embedded iframe) — wrap each panel in a Show sibling and " +
          "switch which one is visible."
      ),
      Code(
        Code.block := true,
        Code.text :=
          """div(
            |  stack.col(spacing.lg),
            |  Show(
            |    Show.visible <-- viewVar.signal.map(_ == View.Explore),
            |    Show.content(ExplorePanel(... bindings ...))
            |  ),
            |  Show(
            |    Show.visible <-- viewVar.signal.map(_ == View.Analyses),
            |    Show.content(AnalysesPanel(... bindings ...))
            |  )
            |)""".stripMargin
      ),
      PageTemplate.paragraph(
        "Show mounts each subtree once at build time and toggles between display: contents " +
          "(transparent — children inherit the parent's flex/grid layout) and display: none. " +
          "Subscriptions, internal Vars, scroll positions, and any third-party imperatively-mounted " +
          "widget all stay alive while hidden."
      ),
      PageTemplate.paragraph(
        "Cost: every wrapped panel is alive from app boot — initial data fetches fire, intervals " +
          "tick, DOM exists in memory. For cheap panels that's fine; for heavy ones prefer option (2) " +
          "and lift state up to a Var that survives the remount."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Critical Laminar / Scala-3 gotchas")(
      PageTemplate.paragraph(
        "These bite repeatedly. Read once now, save yourself an hour later."
      ),

      Heading(4)("1. Hide Laminar's Mod alias"),
      Code(
        Code.block := true,
        Code.text := """import com.raquo.laminar.api.L.{Mod as _, *}"""
      ),
      PageTemplate.paragraph(
        "Laminar exports its own Mod type alias that collides with lui.Mod."
      ),

      Heading(4)("2. css.* is namespaced, not wildcard-imported"),
      PageTemplate.paragraph(
        "Laminar's L.* already exports property setters named background, padding, color, etc. " +
          "Write css.background(...), never import lui.style.css.*."
      ),

      Heading(4)("3. Length.px(n), not 5.px"),
      PageTemplate.paragraph(
        "Laminar's CSS-length traits also export .px on Int/Double and shadow the token versions " +
          "outside lui.style.tokens. Always use Length.px / .pct / .em factories."
      ),

      Heading(4)("4. One Style modifier per element"),
      PageTemplate.paragraph(
        "Style.apply calls styleAttr := toCss, which replaces the entire inline style. Two separate " +
          "Style modifiers on the same element race; the second wipes out the first. Compose with ++ " +
          "into a single Style:"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// right:
            |div(stack.row(spacing.md) ++ css.alignItems("flex-start"), …)
            |
            |// broken:
            |div(stack.row(spacing.md), css.alignItems("flex-start"), …)""".stripMargin
      ),

      Heading(4)("5. Signal[Style] is not a Modifier"),
      PageTemplate.paragraph(
        "A static Style is a Modifier; a ThemedStyle is a Modifier. A raw Signal[Style] is not. " +
          "Route it through the signal.styled((t, a) => Style) extension."
      ),

      Heading(4)("6. Field-name collisions with Laminar exports"),
      PageTemplate.paragraph(
        "Inside a component, if your prop is named disabled, placeholder, value, download, etc., " +
          "the local val shadows Laminar's identically-named export. Alias on import:"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """import com.raquo.laminar.api.L.{
            |  Mod as _,
            |  value as htmlValue, disabled as htmlDisabled,
            |  *
            |}""".stripMargin
      ),

      Heading(4)("7. Some tag names disambiguate"),
      PageTemplate.paragraph(
        "Laminar 17 uses headerTag (not header), markTag (not mark), emTag, labelTag. When in " +
          "doubt, check com.raquo.laminar.api.Laminar or use Metals inspect."
      ),

      Heading(4)("8. -Xfatal-warnings is on"),
      PageTemplate.paragraph(
        "-Wvalue-discard fails the build for discarded non-Unit returns. Silence intentional " +
          "discards with val _ = sideEffectingCall(...)."
      ),

      Heading(4)("9. Var.signal.changes does not dedupe"),
      PageTemplate.paragraph(
        "Two-way bindings (multiple TextInput.value <--> sharedVar) will loop unless the writer " +
          "side is distinct. Prop.inOut already pipes through .distinct; mimic that when you build " +
          "your own two-way bindings."
      ),

      Heading(4)("10. Components do not satisfy HtmlElement"),
      PageTemplate.paragraph(
        "Button(...) returns a Button (a Component, a Modifier), not an HtmlElement. If a helper " +
          "returns HtmlElement, either wrap with div(Button(...)) or widen the return type to " +
          "Modifier[HtmlElement]. For children <-- ... you need Node/HtmlElement specifically; use " +
          ".root to extract it from a Component."
      ),

      Heading(4)("11. Don't put display: flex on <th> or <td>"),
      PageTemplate.paragraph(
        "Table cells default to display: table-cell; overriding that with flex (via stack.col(gap)) " +
          "takes the cell out of the table layout — the header row floats next to the body instead " +
          "of above it. Wrap the cell's content in a child <div> and apply stack.col there."
      ),

      Heading(4)("12. InOut props have no .map / .foreach — only Out does"),
      PageTemplate.paragraph(
        "If the parent wants to transform the outbound value of an InOut prop, wire the outbound " +
          "side as a separate --> binding to a writer.contramap Observer:"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// Bind the inbound:
            |HeaderCell.filterValue <-- valuesByField.signal.map(_.getOrElse(field, "")),
            |// Adapt + forward the outbound:
            |HeaderCell.filterValue --> changesBus.writer.contramap[String](v => (field, v))""".stripMargin
      ),

      Heading(4)("13. LockedEventKey does not expose .collect / .mapTo directly"),
      PageTemplate.paragraph(
        "onClick.mapToUnit returns a LockedEventKey, not an EventStream. Chain stream combinators " +
          "inside .compose(...):"
      ),
      Code(
        Code.block := true,
        Code.text :=
          """// wrong — .collect is not a member of LockedEventKey
            |onClick.mapToUnit
            |  .compose(_.withCurrentValueOf(openVar.signal))
            |  .collect { case true => () }
            |  --> closeBus.writer
            |
            |// right — collect inside the compose block
            |onClick.mapToUnit.compose(
            |  _.withCurrentValueOf(openVar.signal).collect { case true => () }
            |) --> closeBus.writer""".stripMargin
      ),

      Heading(4)("14. withCurrentValueOf collapses Unit and flattens tuples"),
      PageTemplate.paragraph(
        "Composition typeclass: EventStream[Unit] + Signal[A] composes to EventStream[A] (Unit " +
          "drops). EventStream[A] + Signal[B] composes to EventStream[(A, B)]. EventStream[(A, B)] " +
          "+ Signal[C] composes to EventStream[(A, B, C)] (flat, not nested). So " +
          "Button.clicks.compose(_.withCurrentValueOf(sourceVar.signal)) is just EventStream[Source] — " +
          "no ._2 projection needed."
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("Don't")(
      Listing()(
        Listing.item(span(typo.body, "Don't create .css or .scss files, or <style> blocks.")),
        Listing.item(span(typo.body, "Don't use cls := \"...\" or className. The class HTML attribute is unused.")),
        Listing.item(span(typo.body, "Don't customize :focus styling — let the browser's native focus ring through. Only :hover and pressed-like state are simulated via the JS-backed Interactive helper.")),
        Listing.item(span(typo.body, "Don't add @keyframes. For continuous animation use a JS setInterval driving a Var[Double] (see Spinner, StatusBadge.pulsing).")),
        Listing.item(span(typo.body, "Don't reach for npm install. The build is sbt-only.")),
        Listing.item(span(typo.body, "Don't add a new Var and In declaration without Prop.in[V, El](_.xxxVar). The two-line pattern is dead.")),
        Listing.item(span(typo.body, "Don't write mods.foreach(_(el)); el in a component factory. ComponentFactory does it."))
      )
    ),

    // -----------------------------------------------------------------------
    PageTemplate.section("From here")(
      Listing()(
        Listing.item(span(typo.body, "Read ", b("Foundations / Style & themed"), " for the inline-style model in depth.")),
        Listing.item(span(typo.body, "Read ", b("Foundations / Interactive"), " for hover / pressed state plumbing.")),
        Listing.item(span(typo.body, "Browse any component page — the demos exercise the patterns above."))
      )
    )
  )

  private def b(s: String): HtmlElement =
    span(themed(t => css.fontWeight(FontWeight.SemiBold) ++ css.color(t.text)), s)
}
