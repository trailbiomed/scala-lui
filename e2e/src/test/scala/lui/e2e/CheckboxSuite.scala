package lui.e2e

class CheckboxSuite extends E2ESuite {

  test("clicking a checkbox label toggles the check mark") {
    gotoSlug("checkbox")
    // "Normalize before split" starts unchecked (no <--> binding to a Var(true)).
    val cb = page.locator("label:has-text('Normalize before split')")
    cb.waitFor()
    assertEquals(cb.locator("text=✓").count(), 0)
    cb.click()
    cb.locator("text=✓").waitFor()
    cb.click()
    page.waitForCondition(() => cb.locator("text=✓").count() == 0)
  }

  test("disabled checkbox does not toggle when clicked") {
    gotoSlug("checkbox")
    val cb = page.locator("label:has-text('Disabled option')")
    cb.waitFor()
    assertEquals(cb.locator("text=✓").count(), 0)
    cb.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true))
    // Give the click a moment to (not) take effect.
    page.waitForTimeout(150)
    assertEquals(cb.locator("text=✓").count(), 0)
  }
}
