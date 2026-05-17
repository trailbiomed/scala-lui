package lui.e2e

class TabsSuite extends E2ESuite {

  test("clicking a tab updates the bound active key") {
    gotoSlug("tabs")
    // Demo binds `active <--> Var("modules")` and shows the current key as text.
    page.locator("text=active: modules").waitFor()
    page.locator("button:has-text('Top genes')").click()
    page.locator("text=active: topgenes").waitFor()
    page.locator("button:has-text('Clustering')").click()
    page.locator("text=active: cluster").waitFor()
  }
}
