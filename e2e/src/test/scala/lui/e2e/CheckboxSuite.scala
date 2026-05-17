package lui.e2e

class CheckboxSuite extends E2ESuite {

  test("clicking a checkbox toggles the check mark") {
    gotoSlug("checkbox")
    val cb = page.locator("[role='checkbox']:has-text('Normalize before split')")
    cb.waitFor()
    assertEquals(cb.locator("text=✓").count(), 0)
    cb.click()
    cb.locator("text=✓").waitFor()
    cb.click()
    page.waitForCondition(() => cb.locator("text=✓").count() == 0)
  }

  test("disabled checkbox does not toggle when clicked") {
    gotoSlug("checkbox")
    val cb = page.locator("[role='checkbox']:has-text('Disabled option')")
    cb.waitFor()
    assertEquals(cb.locator("text=✓").count(), 0)
    cb.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true))
    page.waitForTimeout(150)
    assertEquals(cb.locator("text=✓").count(), 0)
  }
}
