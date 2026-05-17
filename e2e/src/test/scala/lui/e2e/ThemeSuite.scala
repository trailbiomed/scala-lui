package lui.e2e

class ThemeSuite extends E2ESuite {

  test("ThemePicker switches the body background between light and dark") {
    gotoSlug("button") // ThemePicker is in the global header; any slug works.
    def bodyBg(): String = page.evaluate(
      "() => document.body.style.backgroundColor"
    ).asInstanceOf[String]
    val lightBg = bodyBg()
    assert(lightBg.nonEmpty, "expected an inline background-color on <body> (set by Theme.signal)")
    val trigger = page.locator("button:has-text('Light')").first()
    trigger.waitFor()
    trigger.click()
    page.getByText("Dark").first().waitFor()
    page.locator("text=☾ Dark").first().click()
    page.waitForCondition(() => bodyBg() != lightBg)
  }

  test("ThemePicker can switch back to Light after switching to Dark") {
    gotoSlug("button")
    def bodyBg(): String = page.evaluate(
      "() => document.body.style.backgroundColor"
    ).asInstanceOf[String]
    val initial = bodyBg()
    // open → Dark
    page.locator("button:has-text('Light')").first().click()
    page.locator("text=☾ Dark").first().click()
    page.waitForCondition(() => bodyBg() != initial)
    // open again → Light
    page.locator("button:has-text('Dark')").first().click()
    page.locator("text=☀ Light").first().click()
    page.waitForCondition(() => bodyBg() == initial)
  }
}
