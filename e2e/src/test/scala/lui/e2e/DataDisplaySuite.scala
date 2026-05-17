package lui.e2e

class DataDisplaySuite extends E2ESuite {

  test("Clipboard button label flips to ✓ Copied after click") {
    gotoSlug("clipboard")
    val btn = page.locator("button:has-text('Copy ID')")
    btn.waitFor()
    // navigator.clipboard requires permissions in headless Chromium.
    page.context().grantPermissions(java.util.List.of("clipboard-read", "clipboard-write"))
    btn.click()
    page.locator("button:has-text('Copied')").waitFor()
  }

  test("Table renders header columns and one row per record") {
    gotoSlug("table")
    page.locator("th:has-text('Run')").waitFor()
    page.locator("th:has-text('Status')").waitFor()
    page.locator("td:has-text('demo_run_2026')").waitFor()
    page.locator("td:has-text('Queued')").waitFor()
  }

  test("DataList renders key/value pairs") {
    gotoSlug("data-list")
    // Each text appears in both the live demo and the code-block illustration.
    page.locator("text=Run ID").first().waitFor()
    page.locator("text=demo_run_2026").first().waitFor()
  }

  test("Timeline renders the items in order") {
    gotoSlug("timeline")
    page.locator("text=Reference imported").first().waitFor()
    page.locator("text=Compute complete").first().waitFor()
    page.locator("text=Review").first().waitFor()
  }
}
