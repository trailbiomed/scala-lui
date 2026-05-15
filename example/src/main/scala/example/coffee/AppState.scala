package example.coffee

import com.raquo.laminar.api.L.{Mod as _, *}

/** Layered, nested state for the coffee-machine demo.
  *
  * Single source of truth lives in the leaf-most `Var`s. Composite views (Recipe, MachineState)
  * are derived as `Signal`s so the UI never reads from stale snapshots.
  *
  * Each `Var` is the smallest unit of change for its slice. Updating bean does not update
  * milk and vice versa.
  */
final class AppState {

  // -- Customer ---------------------------------------------------------------
  val customerName: Var[String] = Var("Guest")
  val favorites:    Var[Map[String, Recipe]] = Var(Catalog.sampleFavorites)

  // -- Recipe slices (each independently editable) ----------------------------
  val bean:         Var[Bean]       = Var(Catalog.defaultRecipe.bean)
  val method:       Var[BrewMethod] = Var(Catalog.defaultRecipe.method)
  val cup:          Var[CupSize]    = Var(Catalog.defaultRecipe.cup)
  val shots:        Var[Int]        = Var(Catalog.defaultRecipe.shots)
  val temperatureC: Var[Int]        = Var(Catalog.defaultRecipe.temperatureC)
  val milkKind:     Var[MilkKind]   = Var(Catalog.defaultRecipe.milk.kind)
  val milkAmount:   Var[Int]        = Var(Catalog.defaultRecipe.milk.amountMl)
  val milkFoam:     Var[Int]        = Var(Catalog.defaultRecipe.milk.foamPct)
  val addons:       Var[Set[AddOn]] = Var(Catalog.defaultRecipe.addons)
  val sugar:        Var[Int]        = Var(Catalog.defaultRecipe.sugarPackets)

  /** Composed milk configuration. Combined separately to keep the main combine under arity 9. */
  val milk: Signal[MilkConfig] =
    Signal.combine(milkKind.signal, milkAmount.signal, milkFoam.signal)
      .map { case (k, a, f) => MilkConfig(k, a, f) }

  /** Derived signal: a fully composed Recipe from the slices above. */
  val recipe: Signal[Recipe] =
    Signal.combine(
      bean.signal, method.signal, cup.signal, shots.signal,
      temperatureC.signal, milk, addons.signal, sugar.signal
    ).map { case (b, m, c, s, t, ml, ad, sg) => Recipe(b, m, c, s, t, ml, ad, sg) }

  /** Snapshot of the current recipe, reading directly from the underlying Vars.
    * Used by action methods (Signal.now() is package-private). */
  def currentRecipe(): Recipe = Recipe(
    bean.now(), method.now(), cup.now(), shots.now(), temperatureC.now(),
    MilkConfig(milkKind.now(), milkAmount.now(), milkFoam.now()),
    addons.now(), sugar.now()
  )

  // -- Queue & history --------------------------------------------------------
  val nextOrderId: Var[Int]        = Var(200)
  val queue:       Var[Seq[Order]] = Var(Catalog.sampleOrders)
  val history:     Var[Seq[Order]] = Var(Catalog.sampleHistory)

  // -- Machine status ---------------------------------------------------------
  val waterMl:    Var[Int]                = Var(1400)              // out of 2000
  val hopperPct:  Var[Map[String, Int]]   = Var(
    Catalog.beans.map(b => b.id -> (50 + (b.name.hashCode % 50).abs)).toMap
  )
  val cleaningCyclesLeft: Var[Int]        = Var(28)

  // Brew progress, when an order is being made
  val currentBrew: Var[Option[BrewState]] = Var(None)

  /** Convenience: total cents for the queue + current recipe. */
  val totalPendingCents: Signal[Int] =
    Signal.combine(queue.signal, recipe).map { case (q, r) =>
      q.filter(_.status == OrderStatus.Queued).map(_.recipe.estimatedCents).sum + r.estimatedCents
    }

  // -- Actions ----------------------------------------------------------------

  /** Add the currently-composed recipe to the queue. */
  def enqueueCurrent(): Unit = {
    val r = currentRecipe()
    val o = Order(
      id = nextOrderId.now(),
      customer = customerName.now(),
      recipe = r,
      status = OrderStatus.Queued,
      placedAtMs = scala.scalajs.js.Date.now()
    )
    queue.update(_ :+ o)
    nextOrderId.update(_ + 1)
  }

  /** Load a saved favorite into the recipe slices. */
  def loadFavorite(name: String): Unit =
    favorites.now().get(name).foreach(setRecipe)

  /** Save the current recipe under `name`. Overwrites if it exists. */
  def saveFavorite(name: String): Unit = {
    val r = currentRecipe()
    favorites.update(_.updated(name, r))
  }

  /** Replace every recipe slice from a Recipe value. */
  def setRecipe(r: Recipe): Unit = {
    bean.set(r.bean)
    method.set(r.method)
    cup.set(r.cup)
    shots.set(r.shots)
    temperatureC.set(r.temperatureC)
    milkKind.set(r.milk.kind)
    milkAmount.set(r.milk.amountMl)
    milkFoam.set(r.milk.foamPct)
    addons.set(r.addons)
    sugar.set(r.sugarPackets)
  }

  /** Cancel an order by id; only works if the order is still Queued. */
  def cancel(id: Int): Unit =
    queue.update(_.map(o =>
      if (o.id == id && o.status == OrderStatus.Queued) o.copy(status = OrderStatus.Cancelled)
      else o
    ))

  /** Pick up a Ready order. Moves it to history. */
  def pickUp(id: Int): Unit = {
    val (matched, rest) = queue.now().partition(_.id == id)
    matched.headOption.foreach { o =>
      queue.set(rest)
      history.update(o.copy(status = OrderStatus.PickedUp) +: _)
    }
  }

  /** Mark the next queued order as brewing. Demo helper. */
  def brewNext(): Unit = {
    val q = queue.now()
    q.find(_.status == OrderStatus.Queued).foreach { o =>
      val updated = q.map(x => if (x.id == o.id) x.copy(status = OrderStatus.Brewing) else x)
      queue.set(updated)
      currentBrew.set(Some(BrewState(o, pct = 0.0, stage = BrewStage.Grinding)))
    }
  }
}

/** Live brew progress. Updated by a JS interval (no @keyframes, no animation libs). */
final case class BrewState(order: Order, pct: Double, stage: BrewStage)
