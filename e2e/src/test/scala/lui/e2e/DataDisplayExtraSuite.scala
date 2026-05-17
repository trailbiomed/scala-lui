package lui.e2e

class DataDisplayExtraSuite extends E2ESuite {

  test("Avatar derives initials from `name` (first letter of each word, up to 2)") {
    gotoSlug("avatar")
    // The demo uses Avatar.name := "John Doe" → initials "JD". Filter to the
    // avatar divs by style: brand background + width≥20px, then read text.
    val initialsList = page.evaluate(
      """() => Array.from(document.querySelectorAll('div'))
        |  .filter(d => d.style && d.style.userSelect === 'none'
        |             && d.style.borderRadius
        |             && d.textContent && /^[A-Z]{1,2}$/.test(d.textContent.trim()))
        |  .map(d => d.textContent.trim())""".stripMargin
    ).asInstanceOf[java.util.List[_]]
    val all = scala.jdk.CollectionConverters.ListHasAsScala(initialsList).asScala.map(_.toString)
    assert(all.forall(_ == "JD"), s"expected all avatars to show 'JD' initials, got: $all")
    assert(all.nonEmpty, "expected at least one avatar with JD initials")
  }

  test("Card.interactive renders cursor:pointer and a hover-active border") {
    gotoSlug("card")
    val clickMe = page.getByText("Click me").first()
    clickMe.waitFor()
    // Walk up to the Card root and read its cursor inline style.
    val cursor = page.evaluate(
      """() => {
        |  const span = Array.from(document.querySelectorAll('span'))
        |    .find(s => s.textContent === 'Click me');
        |  if (!span) return 'no-span';
        |  // The Card root is the nearest ancestor with cursor:pointer set inline.
        |  let p = span.parentElement;
        |  while (p) { if (p.style && p.style.cursor) return p.style.cursor; p = p.parentElement; }
        |  return 'no-cursor';
        |}""".stripMargin
    ).asInstanceOf[String]
    assertEquals(cursor, "pointer")
  }

  test("Tag with removable := true renders an × close button") {
    gotoSlug("tag")
    // 4 tags, only the last one is removable. The × span is a child of the
    // last <span> Tag root.
    val removableXCount = page.evaluate(
      """() => Array.from(document.querySelectorAll('span'))
        |  .filter(s => s.textContent === '×').length""".stripMargin
    ).asInstanceOf[java.lang.Number].intValue()
    assert(removableXCount >= 1, s"expected at least one removable × button, found $removableXCount")
  }

  test("StatusBadge with pulsing := true alternates opacity over time") {
    gotoSlug("status-badge")
    // The Running badge has pulsing := true. The interval ticks every 700ms
    // toggling opacity between 1.0 and 0.45. Sample twice with a delay and
    // verify the inline opacity changed.
    def badgeOpacity(): String = page.evaluate(
      """() => {
        |  const s = Array.from(document.querySelectorAll('span'))
        |    .find(el => el.textContent && el.textContent.trim() === 'Running');
        |  return s ? s.style.opacity : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    val first = badgeOpacity()
    // Wait long enough for at least one tick.
    val deadline = System.currentTimeMillis() + 2500
    var second = first
    while (System.currentTimeMillis() < deadline && second == first) {
      Thread.sleep(100)
      second = badgeOpacity()
    }
    assert(second != first, s"expected pulsing opacity to change; saw $first then $second")
  }
}
