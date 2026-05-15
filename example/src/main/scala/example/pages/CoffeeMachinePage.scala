package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import example.coffee.*
import lui.*
import lui.style.*
import lui.components.*
import scala.scalajs.js

/** A deliberately-elaborate demo: a pick-and-choose coffee-machine UI.
  *
  * Built to exercise lui at scale: nested `Var` slices feeding a derived `Recipe` signal,
  * a JS-interval-driven brew ticker, modal confirmation, a queue table, and a history
  * timeline.
  *
  * State and types live in [[example.coffee]]; this file is presentation only. */
object CoffeeMachinePage {

  // Singletons that survive page unmount/remount. Without these, navigating to docs and
  // back would reset every Var.
  private lazy val app: AppState = {
    val a = new AppState()
    startBrewTicker(a)
    a
  }
  private lazy val drawerOpen: Var[Boolean] = Var(false)
  private lazy val confirmOpen: Var[Boolean] = Var(false)
  private lazy val pendingName: Var[String] = Var("Custom #1")

  def apply(): HtmlElement = {
    // Touch the lazy val so the ticker installs on first visit.
    val _ = app
    PageTemplate(
      title = "Coffee machine",
      summary = "An over-engineered demo with layered state. Pick a bean, dial in the recipe, queue it."
    )(
      machineBanner(app),
      SimpleGrid(columns = 2, gap = spacing.lg)(
        customerCard(app, drawerOpen),
        machineCard(app)
      ),
      Heading(2)("Build a recipe"),
      beanPicker(app),
      SimpleGrid(columns = 2, gap = spacing.lg)(
        methodCard(app),
        cupCard(app)
      ),
      SimpleGrid(columns = 2, gap = spacing.lg)(
        shotsCard(app),
        temperatureCard(app)
      ),
      SimpleGrid(columns = 2, gap = spacing.lg)(
        milkCard(app),
        addonsCard(app)
      ),
      orderSummary(app, confirmOpen),
      Heading(2)("Active queue"),
      queueTable(app),
      Heading(2)("Recent orders"),
      historyTimeline(app),
      favoritesDrawer(app, drawerOpen),
      confirmModal(app, confirmOpen, pendingName)
    )
  }

  // ===========================================================================
  // Banner / status
  // ===========================================================================

  private def machineBanner(app: AppState): Modifier[HtmlElement] =
    div(
      app.waterMl.signal.styled { (t, w) =>
        val lowWater = w < 400
        stack.between(spacing.md) ++
          css.padding(spacing.lg, spacing.xl) ++
          css.borderRadius(radius.lg) ++
          css.background(if (lowWater) t.warningSoft else t.brandSoft) ++
          css.border(Length.px(1), BorderStyle.Solid,
            if (lowWater) t.warningBorder else t.border)
      },
      div(
        stack.row(spacing.md),
        span(css.fontSize(Length.px(32)), "☕"),
        div(
          stack.col(spacing.xs),
          span(typo.label, "Espresso Lab"),
          span(typo.muted, child.text <-- app.currentBrew.signal.map {
            case Some(b) => s"Brewing #${b.order.id} for ${b.order.customer}: ${b.stage.label}"
            case None    => "Idle. Ready to brew."
          })
        )
      ),
      div(
        stack.row(spacing.md),
        Button(
          Button.label := "Brew next in queue",
          Button.variant := Button.Variant.Primary,
          Button.size := Button.Size.Small,
          Button.click.foreach(_ => app.brewNext())
        )
      )
    )

  // ===========================================================================
  // Customer card
  // ===========================================================================

  private def customerCard(app: AppState, drawerOpen: Var[Boolean]): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "Customer"),
        div(
          stack.row(spacing.md) ++ css.alignItems("center"),
          Avatar(
            Avatar.name <-- app.customerName.signal,
            Avatar.size := Avatar.Size.Lg
          ),
          div(
            stack.col(spacing.xs) ++ css.raw("flex", "1 1 0"),
            Field(
              Field.label := "Name",
              Field.control(TextInput(TextInput.value <--> app.customerName))
            )
          )
        ),
        div(
          stack.row(spacing.md) ++ stack.wrap,
          Button(
            Button.label := "Load favorite…",
            Button.variant := Button.Variant.Secondary,
            Button.size := Button.Size.Small,
            Button.click.foreach(_ => drawerOpen.set(true))
          )
        )
      )
    ))

  // ===========================================================================
  // Machine card
  // ===========================================================================

  private def machineCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "Machine status"),
        div(
          stack.col(spacing.sm),
          // water progress
          div(
            stack.between(),
            span(typo.muted, "Water"),
            span(typo.hint, child.text <-- app.waterMl.signal.map(w => s"$w / 2000 ml"))
          ),
          progressBarFromMl(app.waterMl.signal, max = 2000, warningBelow = 400),
          // cleaning cycles
          div(
            stack.between(),
            span(typo.muted, "Cleaning cycles left"),
            span(typo.label, child.text <-- app.cleaningCyclesLeft.signal.map(_.toString))
          ),
          // current brew
          child.maybe <-- app.currentBrew.signal.map {
            case None => None
            case Some(_) => Some(
              div(
                stack.col(spacing.xs) ++ css.raw("margin-top", spacing.md.toCss),
                div(stack.between(),
                  span(typo.muted, "Brewing"),
                  StatusBadge(
                    StatusBadge.label <-- app.currentBrew.signal.map(_.fold("")(_.stage.label)),
                    StatusBadge.variant := StatusBadge.Variant.Running,
                    StatusBadge.pulsing := true
                  )
                ),
                ProgressBar(ProgressBar.value <-- app.currentBrew.signal.map(_.fold(0.0)(_.pct)))
              )
            )
          }
        )
      )
    ))

  private def progressBarFromMl(s: Signal[Int], max: Int, warningBelow: Int): Modifier[HtmlElement] = {
    val v = s.map(ml => math.min(1.0, ml.toDouble / max.toDouble))
    val variantS = s.map(ml =>
      if (ml < warningBelow) ProgressBar.Variant.Warning else ProgressBar.Variant.Brand
    )
    ProgressBar(ProgressBar.value <-- v, ProgressBar.variant <-- variantS)
  }

  // ===========================================================================
  // Bean picker
  // ===========================================================================

  private def beanPicker(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "1. Bean"),
        SimpleGrid.autoFit(minChildWidth = Length.px(220), gap = spacing.md)(
          Catalog.beans.map(b => beanCard(b, app))
        )
      )
    ))

  private def beanCard(b: Bean, app: AppState): HtmlElement = {
    val root = div()
    val interact = Interactive.on(root)
    root.amend(
      Signal.combine(app.bean.signal, interact.state).styled { case (t, (current, i)) =>
        val selected = current.id == b.id
        val bd =
          if (selected) t.brand
          else if (i.hovered) t.borderActive
          else t.border
        stack.col(spacing.sm) ++
          css.padding(spacing.md) ++
          css.borderRadius(radius.md) ++
          css.border(Length.px(1.5), BorderStyle.Solid, bd) ++
          css.background(if (selected) t.brandSoft else t.surface) ++
          css.cursor("pointer") ++
          css.transition("border-color", 120)
      },
      onClick.mapToUnit --> Observer[Unit](_ => app.bean.set(b)),
      div(stack.row(spacing.sm) ++ css.alignItems("center"),
        ColorSwatch(b.chip, Length.px(24)),
        span(typo.label, b.name)
      ),
      div(stack.between(),
        span(typo.hint, b.origin),
        Tag(Tag.label := b.roast.label, Tag.variant := Tag.Variant.Neutral)
      ),
      div(stack.row(spacing.xs),
        span(typo.hint, "Intensity"),
        intensityDots(b.intensity)
      ),
      div(stack.between() ++ css.alignItems("center"),
        span(typo.hint, "Hopper"),
        span(typo.label, child.text <-- app.hopperPct.signal.map(_.getOrElse(b.id, 0).toString + "%"))
      )
    )
    root
  }

  private def intensityDots(n: Int): HtmlElement =
    div(
      stack.row(Length.px(2)),
      (1 to 10).map { i =>
        span(themed(t =>
          css.width(Length.px(6)) ++ css.height(Length.px(6)) ++
            css.borderRadius(radius.pill) ++
            css.background(if (i <= n) t.brand else t.border) ++
            css.raw("display", "inline-block")
        ))
      }
    )

  // ===========================================================================
  // Method / cup / shots / temperature cards
  // ===========================================================================

  private def methodCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "2. Brewing method"),
        SegmentedControl(
          SegmentedControl.value <-- app.method.signal.map(_.toString),
          SegmentedControl.value --> app.method.writer.contramap[String] { s =>
            BrewMethod.values.find(_.toString == s).getOrElse(BrewMethod.Espresso)
          },
          SegmentedControl.options := BrewMethod.values.toSeq.map(m => m.toString -> m.label)
        )
      )
    ))

  private def cupCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "3. Cup size"),
        RadioCard(
          RadioCard.value <-- app.cup.signal.map(_.toString),
          RadioCard.value --> app.cup.writer.contramap[String] { s =>
            CupSize.values.find(_.toString == s).getOrElse(CupSize.Small)
          },
          RadioCard.orientation := RadioCard.Orientation.Horizontal,
          RadioCard.options := CupSize.values.toSeq.map(c =>
            RadioCard.Option(c.toString, c.label, s"${c.ml} ml")
          )
        )
      )
    ))

  private def shotsCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "4. Shots"),
        div(stack.between(),
          span(typo.label, "Espresso shots"),
          span(typo.muted, child.text <-- app.shots.signal.map(s => s"$s × 25 ml"))
        ),
        Slider(
          Slider.value <-- app.shots.signal.map(_.toDouble),
          Slider.value --> app.shots.writer.contramap[Double](_.toInt),
          Slider.min := 1, Slider.max := 3, Slider.step := 1,
          Slider.width := Length.pct(100)
        ),
        SegmentedControl(
          SegmentedControl.value <-- app.shots.signal.map(_.toString),
          SegmentedControl.value --> app.shots.writer.contramap[String](_.toIntOption.getOrElse(1)),
          SegmentedControl.options := Seq("1" -> "Single", "2" -> "Double", "3" -> "Triple")
        )
      )
    ))

  private def temperatureCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "5. Temperature"),
        div(stack.between(),
          span(typo.label, "Brew temperature"),
          span(typo.muted, child.text <-- app.temperatureC.signal.map(t => s"$t °C"))
        ),
        Slider(
          Slider.value <-- app.temperatureC.signal.map(_.toDouble),
          Slider.value --> app.temperatureC.writer.contramap[Double](_.toInt),
          Slider.min := 4, Slider.max := 96, Slider.step := 1,
          Slider.width := Length.pct(100)
        ),
        div(stack.row(spacing.md),
          Tag(Tag.label := "Iced",  Tag.variant := Tag.Variant.Neutral),
          Tag(Tag.label := "Warm",  Tag.variant := Tag.Variant.Neutral),
          Tag(Tag.label := "Hot",   Tag.variant := Tag.Variant.Interesting)
        )
      )
    ))

  // ===========================================================================
  // Milk panel
  // ===========================================================================

  private def milkCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "6. Milk"),
        RadioGroup(
          RadioGroup.value <-- app.milkKind.signal.map(_.toString),
          RadioGroup.value --> app.milkKind.writer.contramap[String] { s =>
            MilkKind.values.find(_.toString == s).getOrElse(MilkKind.None)
          },
          RadioGroup.orientation := RadioGroup.Orientation.Horizontal,
          RadioGroup.options := MilkKind.values.toSeq.map(m => m.toString -> m.label)
        ),
        // Amount + foam, hidden when MilkKind.None
        child.maybe <-- app.milkKind.signal.map {
          case MilkKind.None => None
          case _ => Some(
            div(
              stack.col(spacing.sm) ++ css.raw("margin-top", spacing.md.toCss),
              div(stack.between(),
                span(typo.label, "Amount"),
                span(typo.muted, child.text <-- app.milkAmount.signal.map(a => s"$a ml"))
              ),
              Slider(
                Slider.value <-- app.milkAmount.signal.map(_.toDouble),
                Slider.value --> app.milkAmount.writer.contramap[Double](_.toInt),
                Slider.min := 0, Slider.max := 200, Slider.step := 10,
                Slider.width := Length.pct(100)
              ),
              div(stack.between(),
                span(typo.label, "Foam"),
                span(typo.muted, child.text <-- app.milkFoam.signal.map(f => s"$f %"))
              ),
              Slider(
                Slider.value <-- app.milkFoam.signal.map(_.toDouble),
                Slider.value --> app.milkFoam.writer.contramap[Double](_.toInt),
                Slider.min := 0, Slider.max := 100, Slider.step := 5,
                Slider.width := Length.pct(100)
              )
            )
          )
        }
      )
    ))

  // ===========================================================================
  // Add-ons
  // ===========================================================================

  private def addonsCard(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "7. Add-ons"),
        SimpleGrid(columns = 2, gap = spacing.sm)(
          AddOn.values.toSeq.map(a => addonChip(a, app))
        ),
        div(
          stack.col(spacing.xs) ++ css.raw("margin-top", spacing.md.toCss),
          div(stack.between(),
            span(typo.label, "Sugar"),
            span(typo.muted, child.text <-- app.sugar.signal.map(s => s"$s packet${if (s == 1) "" else "s"}"))
          ),
          NumberInput(
            NumberInput.value <-- app.sugar.signal.map(_.toDouble),
            NumberInput.value --> app.sugar.writer.contramap[Double](_.toInt),
            NumberInput.min := 0, NumberInput.max := 6, NumberInput.step := 1,
            NumberInput.width := Length.px(140)
          )
        )
      )
    ))

  private def addonChip(a: AddOn, app: AppState): HtmlElement = {
    val root = label()
    val interact = Interactive.on(root)
    root.amend(
      Signal.combine(app.addons.signal, interact.state).styled { case (t, (set, i)) =>
        val on = set.contains(a)
        stack.row(spacing.sm) ++
          css.padding(spacing.sm, spacing.md) ++
          css.borderRadius(radius.sm) ++
          css.cursor("pointer") ++
          css.border(Length.px(1), BorderStyle.Solid,
            if (on) t.brand else if (i.hovered) t.borderActive else t.border) ++
          css.background(if (on) t.brandSoft else t.surface)
      },
      onClick.mapToUnit --> Observer[Unit] { _ =>
        app.addons.update(s => if (s.contains(a)) s - a else s + a)
      },
      span(typo.body, a.label),
      div(css.raw("flex", "1 1 auto")),
      span(typo.hint, s"+${cents(a.cents)}")
    )
    root
  }

  // ===========================================================================
  // Summary + actions
  // ===========================================================================

  private def orderSummary(
      app: AppState,
      confirmOpen: Var[Boolean]
  ): Modifier[HtmlElement] =
    Card(Card.padding := spacing.xl, Card.children(
      div(
        stack.col(spacing.lg),
        div(
          stack.between(),
          SectionLabel(SectionLabel.text := "Summary"),
          Tag(
            Tag.label <-- app.recipe.map(r => s"${r.bean.name} · ${r.method.label}"),
            Tag.variant := Tag.Variant.Interesting
          )
        ),
        SimpleGrid(columns = 3, gap = spacing.lg)(
          Stat(
            Stat.label := "PRICE",
            Stat.value <-- app.recipe.map(r => cents(r.estimatedCents)),
            Stat.unit := "",
            Stat.hint <-- app.totalPendingCents.map(c => s"queue total ${cents(c)}")
          ),
          Stat(
            Stat.label := "TIME",
            Stat.value <-- app.recipe.map(r => s"${r.estimatedSeconds}"),
            Stat.unit := "s",
            Stat.hint := "wall-clock estimate"
          ),
          Stat(
            Stat.label := "SHOTS",
            Stat.value <-- app.recipe.map(_.shots.toString),
            Stat.unit := "",
            Stat.hint <-- app.recipe.map(r =>
              s"${r.shots * 25} ml espresso"
            )
          )
        ),
        // Bag of tags showing every selection
        div(
          stack.row(spacing.xs) ++ stack.wrap,
          children <-- app.recipe.map(recipeTagSet)
        ),
        div(
          stack.row(spacing.md) ++ stack.wrap ++ css.justifyContent("flex-end"),
          Button(
            Button.label := "Save as favorite…",
            Button.variant := Button.Variant.Ghost,
            Button.click.foreach(_ => confirmOpen.set(true))
          ),
          Button(
            Button.label := "Add to queue",
            Button.variant := Button.Variant.Secondary,
            Button.click.foreach(_ => app.enqueueCurrent())
          ),
          Button(
            Button.label := "Brew now",
            Button.variant := Button.Variant.Primary,
            Button.click.foreach { _ =>
              app.enqueueCurrent()
              app.brewNext()
            }
          )
        )
      )
    ))

  private def recipeTagSet(r: Recipe): List[HtmlElement] = {
    def t(label: String, variant: Tag.Variant = Tag.Variant.Neutral): HtmlElement =
      Tag(Tag.label := label, Tag.variant := variant).root
    val base = List(
      t(r.bean.name),
      t(r.method.label),
      t(r.cup.label),
      t(s"${r.shots} shot${if (r.shots == 1) "" else "s"}"),
      t(s"${r.temperatureC} °C")
    )
    val milkTag =
      if (r.milk.kind == MilkKind.None) Nil
      else List(t(s"${r.milk.kind.label} · ${r.milk.amountMl} ml · ${r.milk.foamPct}% foam"))
    val sugarTag =
      if (r.sugarPackets == 0) Nil
      else List(t(s"sugar ×${r.sugarPackets}"))
    val addonTags = r.addons.toList.sortBy(_.ordinal).map(a => t(a.label, Tag.Variant.Interesting))
    base ++ milkTag ++ sugarTag ++ addonTags
  }

  // ===========================================================================
  // Queue + history
  // ===========================================================================

  private def queueTable(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "Queue"),
        Table(
          Table.striped := true,
          Table.columns := Seq("#", "Customer", "Recipe", "Status", "Time", "Price"),
          Table.rows <-- app.queue.signal.map { os =>
            os.map { o =>
              Seq(
                s"#${o.id}",
                o.customer,
                shortRecipe(o.recipe),
                o.status.label,
                s"${o.recipe.estimatedSeconds}s",
                cents(o.recipe.estimatedCents)
              )
            }
          }
        )
      )
    ))

  private def historyTimeline(app: AppState): Modifier[HtmlElement] =
    Card(Card.children(
      div(
        stack.col(spacing.md),
        SectionLabel(SectionLabel.text := "History"),
        Timeline(
          Timeline.items <-- app.history.signal.map { hs =>
            hs.map { o =>
              Timeline.Item(
                title = s"#${o.id} for ${o.customer}",
                meta = s"${o.status.label} · ${shortRecipe(o.recipe)}",
                body = s"${cents(o.recipe.estimatedCents)} · ${o.recipe.estimatedSeconds}s"
              )
            }
          }
        )
      )
    ))

  private def shortRecipe(r: Recipe): String =
    s"${r.bean.name} · ${r.method.label} · ${r.cup.label}"

  // ===========================================================================
  // Favorites drawer + save modal
  // ===========================================================================

  private def favoritesDrawer(app: AppState, open: Var[Boolean]): Modifier[HtmlElement] =
    Drawer(
      Drawer.open <--> open,
      Drawer.side := Drawer.Side.Left,
      Drawer.width := Length.px(360),
      Drawer.title := "Favorites",
      Drawer.body(
        div(stack.col(spacing.md),
          children <-- app.favorites.signal.map { favs =>
            favs.toList.sortBy(_._1).map { case (name, r) =>
              favoriteRow(name, r, app, open)
            }
          }
        )
      )
    )

  private def favoriteRow(
      name: String,
      r: Recipe,
      app: AppState,
      drawerOpen: Var[Boolean]
  ): HtmlElement =
    Surface.interactive(
      pad = spacing.md,
      rad = radius.md,
      click = Observer[Unit] { _ =>
        app.loadFavorite(name)
        drawerOpen.set(false)
      }
    )(
      div(stack.col(spacing.xs),
        span(typo.label, name),
        span(typo.muted, shortRecipe(r)),
        div(stack.row(spacing.xs),
          Tag(Tag.label := r.milk.kind.label, Tag.variant := Tag.Variant.Neutral),
          Tag(Tag.label := s"${r.shots} shot${if (r.shots == 1) "" else "s"}", Tag.variant := Tag.Variant.Neutral),
          Tag(Tag.label := cents(r.estimatedCents), Tag.variant := Tag.Variant.Interesting)
        )
      )
    )

  private def confirmModal(
      app: AppState,
      open: Var[Boolean],
      nameVar: Var[String]
  ): Modifier[HtmlElement] =
    Modal(
      Modal.open <--> open,
      Modal.title := "Save as favorite",
      Modal.body(
        div(stack.col(spacing.md),
          Field(
            Field.label := "Name",
            Field.hint := "If a favorite with this name already exists, it will be overwritten.",
            Field.control(TextInput(TextInput.value <--> nameVar))
          ),
          div(stack.row(spacing.md) ++ css.justifyContent("flex-end"),
            Button(
              Button.label := "Cancel",
              Button.variant := Button.Variant.Ghost,
              Button.click.foreach(_ => open.set(false))
            ),
            Button(
              Button.label := "Save",
              Button.variant := Button.Variant.Primary,
              Button.click.foreach { _ =>
                app.saveFavorite(nameVar.now())
                open.set(false)
              }
            )
          )
        )
      )
    )

  // ===========================================================================
  // Brew ticker — drives the brew progress via setInterval
  // ===========================================================================

  /** A JS interval that bumps the current brew's progress every 250ms.
    *
    * No animation libs, no @keyframes. The whole UI re-renders from the Var change. */
  private def startBrewTicker(app: AppState): Unit = {
    val _ = js.timers.setInterval(250.0) {
      app.currentBrew.now() match {
        case None => ()
        case Some(b) =>
          val nextPct = math.min(1.0, b.pct + 0.012)
          val stage =
            if (nextPct < 0.18) BrewStage.Grinding
            else if (nextPct < 0.30) BrewStage.Heating
            else if (nextPct < 0.65) BrewStage.Pouring
            else if (nextPct < 0.85) BrewStage.Frothing
            else if (nextPct < 1.0)  BrewStage.Finishing
            else BrewStage.Done
          if (nextPct >= 1.0) {
            // Finish: move the order to history, drop current brew
            val finished = b.order.copy(status = OrderStatus.Ready)
            app.queue.update(_.map(o =>
              if (o.id == finished.id) finished else o
            ))
            app.currentBrew.set(None)
            // Slightly consume water and beans
            app.waterMl.update(w => math.max(0, w - 50))
            app.hopperPct.update(m =>
              m.updatedWith(b.order.recipe.bean.id)(_.map(p => math.max(0, p - 2)))
            )
          } else {
            app.currentBrew.set(Some(b.copy(pct = nextPct, stage = stage)))
          }
      }
    }
  }

  // ===========================================================================
  // Tiny helpers
  // ===========================================================================

  private def cents(c: Int): String = {
    val whole = c / 100
    val frac = c % 100
    f"€$whole%d.$frac%02d"
  }
}
