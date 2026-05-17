package lui.e2e

class TwoWayBindingSuite extends E2ESuite {

  test("Modal close button (×) closes the modal — the internal close path") {
    gotoSlug("modal")
    page.locator("button:has-text('Open dialog')").click()
    val title = page.getByText("Confirm action").first()
    title.waitFor()
    // The Modal header renders a CloseButton when dismissible (default).
    // It's a <button> with × as its text (per CloseButton). Click it.
    val closeBtn = page
      .locator("button:has-text('×')")
      .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasNotText("Dialog"))
      .first()
    closeBtn.click()
    page.waitForCondition(() => !title.isVisible())
  }

  test("Modal backdrop click closes (dismissible default)") {
    gotoSlug("modal")
    page.locator("button:has-text('Open dialog')").click()
    val title = page.getByText("Confirm action").first()
    title.waitFor()
    // Click the backdrop: the modal root is position:fixed with display:flex.
    // Clicking it (away from the card) emits close via the modal's onClick.
    page.evaluate(
      """() => {
        |  const root = Array.from(document.querySelectorAll('div'))
        |    .find(d => d.style && d.style.position === 'fixed'
        |             && d.style.display === 'flex'
        |             && d.style.zIndex === '40');
        |  if (root) root.click();
        |}""".stripMargin
    )
    page.waitForCondition(() => !title.isVisible())
  }

  test("Accordion clicking twice returns the body to collapsed") {
    gotoSlug("accordion")
    val header = page.locator("button:has-text('Pre-processing')")
    header.waitFor()
    def height(): Int = page.evaluate(
      """() => {
        |  const b = Array.from(document.querySelectorAll('button'))
        |    .find(el => el.textContent.includes('Pre-processing'));
        |  return b.nextElementSibling.clientHeight;
        |}""".stripMargin
    ).asInstanceOf[Number].intValue()
    assertEquals(height(), 0)
    header.click()
    page.waitForCondition(() => height() > 0)
    header.click()
    page.waitForCondition(() => height() == 0)
  }
}
