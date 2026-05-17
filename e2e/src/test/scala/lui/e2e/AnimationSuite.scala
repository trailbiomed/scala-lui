package lui.e2e

class AnimationSuite extends E2ESuite {

  test("Spinner rotates: transform: rotate(...) changes between samples") {
    gotoSlug("spinner")
    // Spinner is a styled <div> with inline `transform: rotate(Ndeg)` driven
    // by a 33ms setInterval. Sample twice with a delay.
    def firstSpinnerTransform(): String = page.evaluate(
      """() => {
        |  const d = Array.from(document.querySelectorAll('div'))
        |    .find(e => e.style && e.style.borderRadius === '50%'
        |             && e.style.transform && e.style.transform.startsWith('rotate'));
        |  return d ? d.style.transform : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    val a = firstSpinnerTransform()
    Thread.sleep(150)
    val b = firstSpinnerTransform()
    assert(a != b, s"expected Spinner rotation to advance; both samples = $a")
  }

  test("Toast auto-dismisses after ~2400ms (text clears)") {
    gotoSlug("toast")
    page.locator("button:has-text('Show toast')").click()
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('div'))
        |  .some(d => d.style && d.style.position === 'fixed'
        |             && d.style.opacity === '1'
        |             && (d.textContent || '').startsWith('Toast #'))""".stripMargin
    )
    // Toast.show clears text after 2400ms — assert with a manual loop so we
    // can extend the wait past Playwright's default poll interval.
    val deadline = System.currentTimeMillis() + 5000
    var cleared = false
    while (System.currentTimeMillis() < deadline && !cleared) {
      cleared = page.evaluate(
        """() => {
          |  const t = Array.from(document.querySelectorAll('div'))
          |    .find(d => d.style && d.style.position === 'fixed'
          |               && d.style.transition && d.style.transition.includes('opacity'));
          |  return !!t && t.style.opacity === '0';
          |}""".stripMargin
      ).asInstanceOf[Boolean]
      if (!cleared) Thread.sleep(150)
    }
    assert(cleared, "Toast never cleared (opacity:0) within 5s")
  }
}
