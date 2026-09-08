package lui.style

class ContrastSuite extends munit.FunSuite {

  test("black on white is 21:1") {
    assertEqualsDouble(Contrast.ratio(Color(0, 0, 0), palette.white), 21.0, 0.01)
  }

  test("a color against itself is 1:1") {
    assertEqualsDouble(Contrast.ratio(palette.teal700, palette.teal700), 1.0, 0.01)
  }

  test("a translucent foreground is composited onto its background") {
    val half = palette.white.alpha(0.5)
    assertEqualsDouble(
      Contrast.ratio(half, Color(0, 0, 0)),
      Contrast.ratio(Color(128, 128, 128), Color(0, 0, 0)),
      0.02
    )
  }

  Theme.all.foreach { t =>
    test(s"${t.name} theme meets WCAG AA on every text pairing") {
      val failures = Contrast.failures(t)
      assert(failures.isEmpty, failures.map(_.describe).mkString("\n"))
    }
  }
}
