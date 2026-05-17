package lui.e2e

class AccordionSuite extends E2ESuite {

  test("clicking the header reveals body content") {
    gotoSlug("accordion")
    val header = page.locator("button:has-text('Pre-processing')")
    header.waitFor()
    // The body container is the <div> sibling of the header button. Its
    // max-height animates 0 → 1000px. Use clientHeight as the truth source —
    // Playwright's isVisible() ignores `max-height: 0; overflow: hidden`.
    def containerHeight(): Int =
      page
        .evaluate(
          """() => {
            |  const b = Array.from(document.querySelectorAll('button'))
            |    .find(el => el.textContent.includes('Pre-processing'));
            |  return b.nextElementSibling.clientHeight;
            |}""".stripMargin
        )
        .asInstanceOf[Number]
        .intValue()

    assertEquals(containerHeight(), 0, "expected accordion body collapsed initially")
    header.click()
    page.waitForCondition(() => containerHeight() > 0)
    header.click()
    page.waitForCondition(() => containerHeight() == 0)
  }
}
