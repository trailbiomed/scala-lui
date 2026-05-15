package example.coffee

import lui.style.Color

/** Domain types for the coffee-machine demo. Pure data, no Laminar. */

enum BrewMethod(val label: String, val baseSeconds: Int) {
  case Espresso   extends BrewMethod("Espresso",   25)
  case Ristretto  extends BrewMethod("Ristretto",  18)
  case Lungo      extends BrewMethod("Lungo",      35)
  case Americano  extends BrewMethod("Americano",  40)
  case Filter     extends BrewMethod("Filter",    180)
  case ColdBrew   extends BrewMethod("Cold brew", 720)
}

enum CupSize(val label: String, val ml: Int) {
  case Small  extends CupSize("Small",  120)
  case Medium extends CupSize("Medium", 200)
  case Large  extends CupSize("Large",  340)
}

enum MilkKind(val label: String) {
  case None      extends MilkKind("None")
  case Whole     extends MilkKind("Whole")
  case Skim      extends MilkKind("Skim")
  case Oat       extends MilkKind("Oat")
  case Almond    extends MilkKind("Almond")
  case Soy       extends MilkKind("Soy")
}

enum AddOn(val label: String, val cents: Int) {
  case Vanilla     extends AddOn("Vanilla syrup",    40)
  case Caramel     extends AddOn("Caramel syrup",    40)
  case Hazelnut    extends AddOn("Hazelnut syrup",   40)
  case Cinnamon    extends AddOn("Cinnamon dusting", 30)
  case Cocoa       extends AddOn("Cocoa dusting",    30)
  case ExtraShot   extends AddOn("Extra shot",       80)
  case Decaf       extends AddOn("Decaf",             0)
}

enum BrewStage(val label: String) {
  case Idle       extends BrewStage("Idle")
  case Grinding   extends BrewStage("Grinding")
  case Heating    extends BrewStage("Heating water")
  case Pouring    extends BrewStage("Pouring")
  case Frothing   extends BrewStage("Frothing milk")
  case Finishing  extends BrewStage("Finishing")
  case Done       extends BrewStage("Done")
}

enum OrderStatus(val label: String) {
  case Queued    extends OrderStatus("Queued")
  case Brewing   extends OrderStatus("Brewing")
  case Ready     extends OrderStatus("Ready")
  case PickedUp  extends OrderStatus("Picked up")
  case Cancelled extends OrderStatus("Cancelled")
}

/** A bean with metadata. */
final case class Bean(
    id: String,
    name: String,
    origin: String,
    roast: Roast,
    intensity: Int,  // 1-10
    chip: Color
)

enum Roast(val label: String) {
  case Light  extends Roast("Light")
  case Medium extends Roast("Medium")
  case Dark   extends Roast("Dark")
}

/** Nested milk configuration. */
final case class MilkConfig(
    kind: MilkKind,
    amountMl: Int,        // 0..200
    foamPct: Int          // 0..100
)

/** Composed recipe: every choice that yields a cup of coffee. */
final case class Recipe(
    bean: Bean,
    method: BrewMethod,
    cup: CupSize,
    shots: Int,                  // 1..3
    temperatureC: Int,           // 4..96
    milk: MilkConfig,
    addons: Set[AddOn],
    sugarPackets: Int            // 0..6
) {
  def estimatedSeconds: Int = {
    val base = method.baseSeconds + shots * 4 + milk.amountMl / 25
    base + addons.size * 2
  }
  def estimatedCents: Int = {
    val perShot = 120
    val cupMod = cup match {
      case CupSize.Small => 0; case CupSize.Medium => 20; case CupSize.Large => 40
    }
    val milkMod = milk.kind match {
      case MilkKind.None => 0
      case MilkKind.Whole | MilkKind.Skim => 30
      case _ => 50
    }
    val addonCents = addons.toList.map(_.cents).sum
    shots * perShot + cupMod + milkMod + addonCents + sugarPackets * 0
  }
}

/** A queued or completed order. */
final case class Order(
    id: Int,
    customer: String,
    recipe: Recipe,
    status: OrderStatus,
    placedAtMs: Double
)
