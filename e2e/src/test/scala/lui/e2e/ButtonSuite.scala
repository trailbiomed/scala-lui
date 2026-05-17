package lui.e2e

class ButtonSuite extends E2ESuite {

  test("button page renders all three variants") {
    gotoSlug("button")
    page.locator("button:has-text('Primary')").waitFor()
    page.locator("button:has-text('Secondary')").waitFor()
    page.locator("button:has-text('Ghost')").waitFor()
  }

  test("clicking the demo button updates the counter") {
    gotoSlug("button")
    page.locator("text=clicked 0 times").waitFor()
    page.locator("button:has-text('Click me')").click()
    page.locator("text=clicked 1 times").waitFor()
    page.locator("button:has-text('Click me')").click()
    page.locator("text=clicked 2 times").waitFor()
  }

  test("disabled button advertises aria-disabled=true and does not click") {
    gotoSlug("button")
    val disabled = page.locator("button[aria-disabled='true']:has-text('Disabled')")
    disabled.waitFor()
    // No counter on the variants/states section — the disabled button just
    // shouldn't throw when force-clicked, and aria stays true.
    disabled.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true))
    assertEquals(disabled.getAttribute("aria-disabled"), "true")
  }
}
