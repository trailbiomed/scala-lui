package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}
import lui.*
import lui.style.*

final class ThemePicker private[components] (val root: HtmlElement) extends Component

/** A theme chooser built on `Menu`. Renders a Ghost button trigger labeled with the
  * active theme and a click-toggled list of all themes shipped by `lui.style.Theme`.
  * Selecting an item writes through to `Theme.current`. */
object ThemePicker extends ComponentFactory[ThemePicker] {

  private val entries: Seq[(Theme, Menu.Item)] = Seq(
    Theme.light   -> Menu.Item(key = "light",   label = "☀ Light"),
    Theme.dark    -> Menu.Item(key = "dark",    label = "☾ Dark"),
    Theme.monokai -> Menu.Item(key = "monokai", label = "✦ Monokai")
  )

  private val byKey: Map[String, Theme] = entries.map { case (t, i) => i.key -> t }.toMap
  private val items: Seq[Menu.Item] = entries.map(_._2)
  private val labelByKey: Map[String, String] = entries.map { case (t, i) => t.name -> i.label }.toMap

  override protected def build: ThemePicker = {
    val menu = Menu(
      Menu.trigger(
        Button(
          Button.variant := Button.Variant.Ghost,
          Button.size := Button.Size.Small,
          Button.label <-- Theme.signal.map(t => labelByKey.getOrElse(t.name, t.name))
        )
      ),
      Menu.items := items,
      Menu.select --> Observer[String] { key =>
        byKey.get(key).foreach(Theme.current.set)
      }
    )
    new ThemePicker(menu.root)
  }
}
