package lui.components

import com.raquo.laminar.api.L.{Mod as _, *}

/** Curated, hand-ported [Lucide](https://lucide.dev) icons (MIT-licensed). Each icon is a
  * 24×24 stroke-based SVG that uses `currentColor`, so the parent's `css.color(...)` tints
  * it. Wrap with [[Icon]] to size and align consistently with the rest of the library:
  *
  * {{{
  * Icon(size = Length.px(20))(icons.check)
  * span(themed(t => css.color(t.danger)), icons.trash)
  * }}}
  *
  * Add new icons by copying their `<path>` / `<polyline>` / etc. from lucide.dev and
  * dropping them into [[make]]. The wrapper supplies viewBox, stroke, line caps, and
  * 100%/100% sizing so the consumer only sees the geometry. */
object icons {

  private def make(parts: Modifier[SvgElement]*): SvgElement = {
    val root = svg.svg(
      svg.viewBox := "0 0 24 24",
      svg.fill := "none",
      svg.stroke := "currentColor",
      svg.strokeWidth := "2",
      svg.strokeLineCap := "round",
      svg.strokeLineJoin := "round",
      svg.width := "100%",
      svg.height := "100%"
    )
    root.amend(parts*)
    root
  }

  // --- Navigation -----------------------------------------------------------
  def chevronLeft: SvgElement  = make(svg.path(svg.d := "m15 18-6-6 6-6"))
  def chevronRight: SvgElement = make(svg.path(svg.d := "m9 18 6-6-6-6"))
  def chevronUp: SvgElement    = make(svg.path(svg.d := "m18 15-6-6-6 6"))
  def chevronDown: SvgElement  = make(svg.path(svg.d := "m6 9 6 6 6-6"))

  def arrowLeft: SvgElement  = make(svg.path(svg.d := "M19 12H5"), svg.path(svg.d := "m12 19-7-7 7-7"))
  def arrowRight: SvgElement = make(svg.path(svg.d := "M5 12h14"), svg.path(svg.d := "m12 5 7 7-7 7"))
  def arrowUp: SvgElement    = make(svg.path(svg.d := "M12 19V5"), svg.path(svg.d := "m5 12 7-7 7 7"))
  def arrowDown: SvgElement  = make(svg.path(svg.d := "M12 5v14"), svg.path(svg.d := "m19 12-7 7-7-7"))

  def externalLink: SvgElement = make(
    svg.path(svg.d := "M15 3h6v6"),
    svg.path(svg.d := "M10 14 21 3"),
    svg.path(svg.d := "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6")
  )

  // --- Common actions -------------------------------------------------------
  def x: SvgElement     = make(svg.path(svg.d := "M18 6 6 18"), svg.path(svg.d := "m6 6 12 12"))
  def check: SvgElement = make(svg.path(svg.d := "M20 6 9 17l-5-5"))
  def plus: SvgElement  = make(svg.path(svg.d := "M5 12h14"), svg.path(svg.d := "M12 5v14"))
  def minus: SvgElement = make(svg.path(svg.d := "M5 12h14"))

  def search: SvgElement = make(
    svg.circle(svg.cx := "11", svg.cy := "11", svg.r := "8"),
    svg.path(svg.d := "m21 21-4.3-4.3")
  )

  def menu: SvgElement = make(
    svg.line(svg.x1 := "4", svg.y1 := "6",  svg.x2 := "20", svg.y2 := "6"),
    svg.line(svg.x1 := "4", svg.y1 := "12", svg.x2 := "20", svg.y2 := "12"),
    svg.line(svg.x1 := "4", svg.y1 := "18", svg.x2 := "20", svg.y2 := "18")
  )

  def moreHorizontal: SvgElement = make(
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "1"),
    svg.circle(svg.cx := "19", svg.cy := "12", svg.r := "1"),
    svg.circle(svg.cx := "5",  svg.cy := "12", svg.r := "1")
  )

  def filter: SvgElement = make(
    svg.polygon(svg.points := "22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3")
  )

  def refresh: SvgElement = make(
    svg.path(svg.d := "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"),
    svg.polyline(svg.points := "21 3 21 8 16 8"),
    svg.path(svg.d := "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"),
    svg.polyline(svg.points := "3 21 3 16 8 16")
  )

  // --- Status ---------------------------------------------------------------
  def info: SvgElement = make(
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "10"),
    svg.path(svg.d := "M12 16v-4"),
    svg.path(svg.d := "M12 8h.01")
  )

  def alertTriangle: SvgElement = make(
    svg.path(svg.d := "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"),
    svg.path(svg.d := "M12 9v4"),
    svg.path(svg.d := "M12 17h.01")
  )

  def alertCircle: SvgElement = make(
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "10"),
    svg.path(svg.d := "M12 8v4"),
    svg.path(svg.d := "M12 16h.01")
  )

  def checkCircle: SvgElement = make(
    svg.path(svg.d := "M22 11.08V12a10 10 0 1 1-5.93-9.14"),
    svg.polyline(svg.points := "22 4 12 14.01 9 11.01")
  )

  def xCircle: SvgElement = make(
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "10"),
    svg.path(svg.d := "m15 9-6 6"),
    svg.path(svg.d := "m9 9 6 6")
  )

  // --- Content / files ------------------------------------------------------
  def eye: SvgElement = make(
    svg.path(svg.d := "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"),
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "3")
  )

  def pencil: SvgElement = make(
    svg.path(svg.d := "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174V20h3.826z"),
    svg.path(svg.d := "m15 5 4 4")
  )

  def trash: SvgElement = make(
    svg.path(svg.d := "M3 6h18"),
    svg.path(svg.d := "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"),
    svg.path(svg.d := "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"),
    svg.line(svg.x1 := "10", svg.y1 := "11", svg.x2 := "10", svg.y2 := "17"),
    svg.line(svg.x1 := "14", svg.y1 := "11", svg.x2 := "14", svg.y2 := "17")
  )

  def copy: SvgElement = make(
    svg.rect(svg.x := "8", svg.y := "8", svg.width := "14", svg.height := "14", svg.rx := "2", svg.ry := "2"),
    svg.path(svg.d := "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2")
  )

  def download: SvgElement = make(
    svg.path(svg.d := "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"),
    svg.polyline(svg.points := "7 10 12 15 17 10"),
    svg.line(svg.x1 := "12", svg.y1 := "15", svg.x2 := "12", svg.y2 := "3")
  )

  def upload: SvgElement = make(
    svg.path(svg.d := "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"),
    svg.polyline(svg.points := "17 8 12 3 7 8"),
    svg.line(svg.x1 := "12", svg.y1 := "3", svg.x2 := "12", svg.y2 := "15")
  )

  def file: SvgElement = make(
    svg.path(svg.d := "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"),
    svg.polyline(svg.points := "14 2 14 8 20 8")
  )

  def folder: SvgElement = make(
    svg.path(svg.d := "M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z")
  )

  def link: SvgElement = make(
    svg.path(svg.d := "M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"),
    svg.path(svg.d := "M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71")
  )

  // --- Identity / system ----------------------------------------------------
  def home: SvgElement = make(
    svg.path(svg.d := "m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"),
    svg.polyline(svg.points := "9 22 9 12 15 12 15 22")
  )

  def user: SvgElement = make(
    svg.path(svg.d := "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"),
    svg.circle(svg.cx := "12", svg.cy := "7", svg.r := "4")
  )

  def users: SvgElement = make(
    svg.path(svg.d := "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"),
    svg.circle(svg.cx := "9", svg.cy := "7", svg.r := "4"),
    svg.path(svg.d := "M22 21v-2a4 4 0 0 0-3-3.87"),
    svg.path(svg.d := "M16 3.13a4 4 0 0 1 0 7.75")
  )

  def settings: SvgElement = make(
    svg.path(svg.d := "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"),
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "3")
  )

  def logOut: SvgElement = make(
    svg.path(svg.d := "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"),
    svg.polyline(svg.points := "16 17 21 12 16 7"),
    svg.line(svg.x1 := "21", svg.y1 := "12", svg.x2 := "9", svg.y2 := "12")
  )

  def lock: SvgElement = make(
    svg.rect(svg.x := "3", svg.y := "11", svg.width := "18", svg.height := "11", svg.rx := "2", svg.ry := "2"),
    svg.path(svg.d := "M7 11V7a5 5 0 0 1 10 0v4")
  )

  // --- Misc -----------------------------------------------------------------
  def sun: SvgElement = make(
    svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "4"),
    svg.path(svg.d := "M12 2v2"),
    svg.path(svg.d := "M12 20v2"),
    svg.path(svg.d := "m4.93 4.93 1.41 1.41"),
    svg.path(svg.d := "m17.66 17.66 1.41 1.41"),
    svg.path(svg.d := "M2 12h2"),
    svg.path(svg.d := "M20 12h2"),
    svg.path(svg.d := "m6.34 17.66-1.41 1.41"),
    svg.path(svg.d := "m19.07 4.93-1.41 1.41")
  )

  def moon: SvgElement = make(
    svg.path(svg.d := "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z")
  )

  def star: SvgElement = make(
    svg.polygon(svg.points := "12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26")
  )

  def heart: SvgElement = make(
    svg.path(svg.d := "M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z")
  )

  def bell: SvgElement = make(
    svg.path(svg.d := "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"),
    svg.path(svg.d := "M10.3 21a1.94 1.94 0 0 0 3.4 0")
  )

  def calendar: SvgElement = make(
    svg.rect(svg.x := "3", svg.y := "4", svg.width := "18", svg.height := "18", svg.rx := "2", svg.ry := "2"),
    svg.line(svg.x1 := "16", svg.y1 := "2", svg.x2 := "16", svg.y2 := "6"),
    svg.line(svg.x1 := "8",  svg.y1 := "2", svg.x2 := "8",  svg.y2 := "6"),
    svg.line(svg.x1 := "3",  svg.y1 := "10", svg.x2 := "21", svg.y2 := "10")
  )

  def apps: SvgElement = make(
    svg.rect(svg.x := "3",  svg.y := "3",  svg.width := "7", svg.height := "7", svg.rx := "1"),
    svg.rect(svg.x := "14", svg.y := "3",  svg.width := "7", svg.height := "7", svg.rx := "1"),
    svg.rect(svg.x := "14", svg.y := "14", svg.width := "7", svg.height := "7", svg.rx := "1"),
    svg.rect(svg.x := "3",  svg.y := "14", svg.width := "7", svg.height := "7", svg.rx := "1")
  )

  def mail: SvgElement = make(
    svg.rect(svg.x := "2", svg.y := "4", svg.width := "20", svg.height := "16", svg.rx := "2"),
    svg.polyline(svg.points := "22 6 12 13 2 6")
  )
}
