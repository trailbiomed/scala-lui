package lui.components

import com.raquo.laminar.api.L.{
  Mod as _,
  multiple as htmlMultiple,
  accept as htmlAccept,
  *
}
import lui.*
import lui.style.*
import org.scalajs.dom

final class FileUpload private[components] (val root: HtmlElement) extends Component {
  private[components] val filesVar: Var[Seq[dom.File]] = Var(Seq.empty)
  private[components] val multipleVar: Var[Boolean] = Var(false)
  private[components] val acceptVar: Var[String] = Var("")
  private[components] val labelVar: Var[String] = Var("Drop files or click to browse")
  private[components] val draggingVar: Var[Boolean] = Var(false)
}

/** Drag-and-drop file dropzone wrapping a hidden `<input type="file">`. */
object FileUpload extends ComponentFactory[FileUpload] {

  val files = Prop.inOut[Seq[dom.File], FileUpload](_.filesVar)
  val multiple = Prop.in[Boolean, FileUpload](_.multipleVar)
  val accept = Prop.in[String, FileUpload](_.acceptVar)
  val label = Prop.in[String, FileUpload](_.labelVar)

  override protected def build: FileUpload = {
    val hidden = input(typ := "file")
    val root = div()
    val el = new FileUpload(root)

    def setFromList(list: dom.FileList): Unit = {
      if (list != null) {
        val coll = (0 until list.length).map(i => list.item(i))
        el.filesVar.set(coll)
      }
    }

    root.amend(
      Signal.combine(el.draggingVar.signal, el.interact.state).styled {
        case (t, (dragging, i)) =>
          val active = dragging || i.hovered
          stack.col(spacing.sm) ++
            css.alignItems("center") ++
            css.justifyContent("center") ++
            css.padding(spacing.xxl) ++
            css.border(
              Length.px(1.5),
              if (dragging) BorderStyle.Solid else BorderStyle.Dashed,
              if (active) t.brand else t.border
            ) ++
            css.borderRadius(radius.lg) ++
            css.background(if (dragging) t.brandSoft else t.surface) ++
            css.cursor("pointer") ++
            css.transition("border-color", 150)
      },
      onClick.mapToUnit --> Observer[Unit](_ => hidden.ref.click()),
      onDragOver.preventDefault.mapTo(true) --> el.draggingVar.writer,
      onDragLeave.mapTo(false) --> el.draggingVar.writer,
      onDrop.preventDefault --> Observer[dom.DragEvent] { ev =>
        el.draggingVar.set(false)
        if (ev.dataTransfer != null) setFromList(ev.dataTransfer.files)
      },
      div(
        themed(t => css.fontSize(Length.px(28)) ++ css.color(t.textMuted)),
        "⤓"
      ),
      span(typo.body, child.text <-- el.labelVar.signal),
      span(
        typo.hint,
        child.text <-- el.filesVar.signal.map { fs =>
          if (fs.isEmpty) ""
          else if (fs.size == 1) fs.head.name
          else s"${fs.size} files selected"
        }
      ),
      hidden
    )

    hidden.amend(
      css.raw("display", "none"),
      htmlMultiple <-- el.multipleVar.signal,
      htmlAccept <-- el.acceptVar.signal,
      onChange --> Observer[dom.Event] { ev =>
        setFromList(ev.target.asInstanceOf[dom.HTMLInputElement].files)
      }
    )

    el
  }
}
