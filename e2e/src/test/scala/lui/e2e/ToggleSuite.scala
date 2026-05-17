package lui.e2e

class ToggleSuite extends E2ESuite {

  test("clicking the toggle moves the thumb (translateX changes)") {
    gotoSlug("toggle")
    // Demo has three toggles in order: (1) initial=true, (2) initial=false, (3) disabled.
    // Pick the second one — starts unchecked, clickable.
    val toggle = page.locator("[role='switch']").nth(1)
    toggle.waitFor()
    val thumbStyleBefore = toggle.locator("span").first().getAttribute("style")
    assert(
      thumbStyleBefore != null && thumbStyleBefore.contains("translateX(2px)"),
      s"unexpected initial thumb style: $thumbStyleBefore"
    )
    toggle.click()
    page.waitForCondition { () =>
      val s = toggle.locator("span").first().getAttribute("style")
      s != null && s.contains("calc(") // checked → translateX(calc(36px - 16px - 2px))
    }
  }
}
