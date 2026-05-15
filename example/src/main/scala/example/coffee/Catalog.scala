package example.coffee

import lui.style.palette

/** Static seed data for the demo. */
object Catalog {

  val beans: Seq[Bean] = Seq(
    Bean("ethiopia-yirg", "Yirgacheffe",      "Ethiopia",  Roast.Light,  6, palette.amber300),
    Bean("colombia-supr", "Supremo",          "Colombia",  Roast.Medium, 5, palette.amber700),
    Bean("brazil-santos", "Santos",           "Brazil",    Roast.Medium, 7, palette.red600),
    Bean("indo-mandheli", "Mandheling",       "Indonesia", Roast.Dark,   9, palette.slate700),
    Bean("kenya-aa",      "AA",               "Kenya",     Roast.Medium, 8, palette.emerald600),
    Bean("italy-blend",   "Italian Roast",    "Blend",     Roast.Dark,  10, palette.slate900)
  )

  val defaultRecipe: Recipe = Recipe(
    bean = beans(1),
    method = BrewMethod.Espresso,
    cup = CupSize.Small,
    shots = 2,
    temperatureC = 92,
    milk = MilkConfig(MilkKind.None, 0, 0),
    addons = Set.empty,
    sugarPackets = 0
  )

  /** Three favorites for the customer dropdown. */
  val sampleFavorites: Map[String, Recipe] = Map(
    "Morning latte" -> defaultRecipe.copy(
      bean = beans(0),
      method = BrewMethod.Espresso,
      cup = CupSize.Medium,
      shots = 2,
      milk = MilkConfig(MilkKind.Oat, 140, 20),
      addons = Set(AddOn.Vanilla)
    ),
    "Afternoon cortado" -> defaultRecipe.copy(
      bean = beans(2),
      method = BrewMethod.Espresso,
      cup = CupSize.Small,
      shots = 2,
      milk = MilkConfig(MilkKind.Whole, 60, 35)
    ),
    "Evening filter" -> defaultRecipe.copy(
      bean = beans(4),
      method = BrewMethod.Filter,
      cup = CupSize.Large,
      shots = 1,
      milk = MilkConfig(MilkKind.None, 0, 0),
      addons = Set(AddOn.Decaf)
    )
  )

  val sampleOrders: Seq[Order] = Seq(
    Order(101, "Sam",    sampleFavorites("Morning latte"),       OrderStatus.Brewing,  System.currentTimeMillis.toDouble - 60_000),
    Order(102, "Avery",  sampleFavorites("Afternoon cortado"),   OrderStatus.Queued,   System.currentTimeMillis.toDouble - 30_000),
    Order(103, "Jules",  defaultRecipe,                          OrderStatus.Queued,   System.currentTimeMillis.toDouble - 12_000)
  )

  val sampleHistory: Seq[Order] = Seq(
    Order(99,  "Sam",   sampleFavorites("Morning latte"),     OrderStatus.PickedUp,  System.currentTimeMillis.toDouble - 6 * 3600_000),
    Order(98,  "Avery", sampleFavorites("Evening filter"),    OrderStatus.PickedUp,  System.currentTimeMillis.toDouble - 24 * 3600_000),
    Order(97,  "Jules", defaultRecipe,                        OrderStatus.Cancelled, System.currentTimeMillis.toDouble - 48 * 3600_000)
  )
}
