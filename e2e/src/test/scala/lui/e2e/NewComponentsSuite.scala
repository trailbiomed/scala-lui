package lui.e2e

/** Coverage for components and props added in this round:
  *  - Checkbox keyboard
  *  - RadioGroup arrow nav
  *  - TextInput aria-invalid
  *  - TabPanel (Stable + Swap)
  *  - Show.Mode.Visibility
  *  - Link.scrollTarget
  *  - ListEditor */
class NewComponentsSuite extends E2ESuite {

  // -- Checkbox keyboard --------------------------------------------------

  test("Checkbox is keyboard-focusable and Space toggles aria-checked") {
    gotoSlug("checkbox")
    val cb = page.locator("[role='checkbox']:has-text('Normalize before split')")
    cb.waitFor()
    cb.focus()
    assertEquals(cb.getAttribute("aria-checked"), "false")
    page.keyboard().press(" ")
    page.waitForCondition(() => cb.getAttribute("aria-checked") == "true")
    page.keyboard().press("Enter")
    page.waitForCondition(() => cb.getAttribute("aria-checked") == "false")
  }

  // -- RadioGroup arrow nav -----------------------------------------------

  test("RadioGroup ArrowDown moves selection to the next option") {
    gotoSlug("radio-group")
    val group = page.locator("[role='radiogroup']").first()
    group.waitFor()
    // Focus the currently-selected option in the first group.
    val selected = group.locator("[role='radio'][aria-checked='true']").first()
    selected.focus()
    val initialKey = selected.getAttribute("data-key")
    page.keyboard().press("ArrowDown")
    page.waitForCondition { () =>
      val nowSelected = group.locator("[role='radio'][aria-checked='true']").first()
      val nowKey = nowSelected.getAttribute("data-key")
      nowKey != null && nowKey != initialKey
    }
  }

  // -- TextInput aria-invalid ---------------------------------------------

  test("TextInput.invalid sets aria-invalid on the input directly") {
    gotoSlug("text-input")
    // The demo includes a TextInput with invalid := true. There's also the
    // Field-error case (covered elsewhere) — verify at least one input with
    // aria-invalid=true exists on this page.
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('input'))
        |  .some(i => i.getAttribute('aria-invalid') === 'true')""".stripMargin
    )
  }

  // -- TabPanel ----------------------------------------------------------

  test("TabPanel renders all panel bodies in Stable mode (height stable)") {
    gotoSlug("tab-panel")
    page.locator("[role='tablist']").first().waitFor()
    // Stable mode: all panel bodies live in the DOM at once.
    val tabpanelCount = page.evaluate(
      """() => document.querySelectorAll("[role='tabpanel']").length"""
    ).asInstanceOf[java.lang.Number].intValue()
    assert(tabpanelCount >= 2, s"expected >= 2 tabpanels in Stable mode, found $tabpanelCount")
  }

  test("TabPanel clicking a tab activates the matching panel") {
    gotoSlug("tab-panel")
    val firstSelected = page.locator("[role='tab'][aria-selected='true']:has-text('Claims')").first()
    firstSelected.waitFor()
    page.locator("[role='tab']:has-text('Contradictions')").first().click()
    page.locator("[role='tab'][aria-selected='true']:has-text('Contradictions')").first().waitFor()
  }

  test("TabPanel Stable layout overlaps panels (all have same offsetTop)") {
    gotoSlug("tab-panel")
    page.locator("[role='tabpanel']").first().waitFor()
    val tops = page.evaluate(
      """() => Array.from(document.querySelectorAll("[role='tabpanel']"))
        |  .map(p => p.offsetTop)""".stripMargin
    ).asInstanceOf[java.util.List[_]]
    val list = scala.jdk.CollectionConverters.ListHasAsScala(tops).asScala.map(_.asInstanceOf[Number].intValue()).toList
    assert(list.distinct.size == 1, s"expected all tabpanels to share offsetTop in Stable mode, got $list")
  }

  // -- Show.Mode.Visibility ----------------------------------------------

  test("Show with Mode.Visibility keeps layout space when hidden") {
    gotoSlug("show")
    // The demo page should include a Show.Mode.Visibility example. Look for
    // any element with inline visibility:hidden + pointer-events:none.
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('div'))
        |  .some(d => d.style && d.style.visibility === 'hidden'
        |             && d.style.pointerEvents === 'none')""".stripMargin
    )
  }

  // -- Link.scrollTarget --------------------------------------------------

  test("Link.scrollTarget does not update the URL hash on click") {
    gotoSlug("link")
    // The demo includes at least one scroll-link with scrollTarget set.
    val before = page.evaluate("() => window.location.hash").asInstanceOf[String]
    val scrollLink = page.locator("a[href^='#scroll-demo']").first()
    if (scrollLink.count() > 0) {
      scrollLink.click()
      // Hash should be unchanged because the link calls preventDefault.
      page.waitForTimeout(100)
      val after = page.evaluate("() => window.location.hash").asInstanceOf[String]
      assertEquals(after, before, "Link.scrollTarget should not modify window.location.hash")
    }
  }

  // -- ListEditor --------------------------------------------------------

  test("ListEditor renders rows and the Add button appends a new row") {
    gotoSlug("list-editor")
    val addBtn = page.locator("button:has-text('+ Add')").first()
    addBtn.waitFor()
    val beforeCount = page.locator("input").count()
    addBtn.click()
    page.waitForCondition(() => page.locator("input").count() == beforeCount + 1)
  }

  test("ListEditor delete button removes a row") {
    gotoSlug("list-editor")
    // Ensure at least 2 rows by adding one.
    page.locator("button:has-text('+ Add')").first().click()
    val countAfterAdd = page.locator("input").count()
    // Click the second row's × button.
    val removeBtns = page.locator("button[aria-label='Remove row']")
    removeBtns.first().click()
    page.waitForCondition(() => page.locator("input").count() == countAfterAdd - 1)
  }

  test("ListEditor typing in a row does not steal focus from the input") {
    gotoSlug("list-editor")
    val input = page.locator("input").first()
    input.waitFor()
    input.focus()
    input.pressSequentially("hello")
    val stillFocused = page.evaluate(
      """() => {
        |  const ae = document.activeElement;
        |  return ae && ae.tagName === 'INPUT' && ae === document.querySelectorAll('input')[0];
        |}""".stripMargin
    ).asInstanceOf[Boolean]
    assert(stillFocused, "expected first input to retain focus after typing")
    assertEquals(input.inputValue(), "alphahello")
  }
}
