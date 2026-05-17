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

  test("Toast auto-dismisses after ~2400ms (row removed from the DOM)") {
    gotoSlug("toast")
    page.locator("button:has-text('Show toast')").click()
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('[aria-live]'))
        |  .some(d => (d.textContent || '').startsWith('Toast #'))""".stripMargin
    )
    // Default Info duration is 2400ms; the row is removed after that. Allow up
    // to 5s for slow runners.
    val deadline = System.currentTimeMillis() + 5000
    var cleared = false
    while (System.currentTimeMillis() < deadline && !cleared) {
      cleared = page.evaluate(
        """() => !Array.from(document.querySelectorAll('[aria-live]'))
          |  .some(d => (d.textContent || '').startsWith('Toast #'))""".stripMargin
      ).asInstanceOf[Boolean]
      if (!cleared) Thread.sleep(150)
    }
    assert(cleared, "Toast never cleared within 5s")
  }
}
