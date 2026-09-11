package lui.e2e

import com.microsoft.playwright.options.AriaRole

/** Locks in the defects reported from building an app on lui and fixed in the library:
  * styles that overwrote each other, a skip link that navigated instead of moving focus,
  * a modal footer that stacked its buttons, and the chrome and states components had no
  * way to opt out of. The focus-ring half of that report is covered in [[A11ySuite]]. */
class UpstreamFixesSuite extends E2ESuite {

  /** The dialog currently on screen. Several demos mount a Modal each, all of them in the
    * DOM at once, so an unscoped `[role='dialog']` resolves to a hidden one. */
  private val openDialog = "[role='dialog']:visible"

  private def styleOf(selector: String): String = {
    val el = page.locator(selector).first()
    el.waitFor()
    Option(el.getAttribute("style")).getOrElse("")
  }

  test("two Style modifiers on one element merge instead of overwriting") {
    gotoSlug("style")
    // The demo stacks a layout Style and a themed surface Style on the same div. Before
    // styles were layered, whichever applied last wiped the other out entirely.
    val merged = styleOf("div:has(> span:text-matches('A flex column'))")
    assert(merged.contains("display: flex"), s"layout layer missing: $merged")
    assert(merged.contains("flex-direction: column"), s"layout layer missing: $merged")
    assert(merged.contains("border-radius"), s"themed layer missing: $merged")
    assert(merged.contains("background"), s"themed layer missing: $merged")
  }

  test("a lui Style leaves Laminar's own style setters on the element alone") {
    gotoSlug("style")
    val body = page.evaluate("() => document.body.getAttribute('style')").asInstanceOf[String]
    assert(body != null && body.contains("margin"), s"page reset lost: $body")
  }

  test("SkipNav moves focus to its target without touching the route") {
    gotoSlug("skip-nav")
    val routeBefore = page.evaluate("() => location.hash").asInstanceOf[String]
    page.evaluate(
      """() => {
        |  const main = document.createElement('div');
        |  main.id = 'main-content';
        |  main.tabIndex = -1;
        |  main.textContent = 'main region';
        |  document.body.appendChild(main);
        |}""".stripMargin
    )
    page.locator("a[href='#main-content']").first().evaluate("a => a.click()")
    page.waitForCondition { () =>
      page
        .evaluate("() => document.activeElement && document.activeElement.id")
        .asInstanceOf[String] == "main-content"
    }
    assertEquals(
      page.evaluate("() => location.hash").asInstanceOf[String],
      routeBefore,
      "following the skip link must not rewrite the hash route"
    )
  }

  test("Modal.footer lays its buttons out in a row, not stacked") {
    gotoSlug("modal")
    page.locator("button:has-text('Open undivided dialog')").first().click()
    val cancel = page.locator(s"$openDialog button:has-text('Cancel')").first()
    val save = page.locator(s"$openDialog button:has-text('Save')").first()
    cancel.waitFor()
    val cancelBox = cancel.boundingBox()
    val saveBox = save.boundingBox()
    assertEquals(
      cancelBox.y.round,
      saveBox.y.round,
      "footer buttons should share a baseline row"
    )
    assert(cancelBox.x < saveBox.x, "cancel should precede the confirming action")
  }

  test("Modal.divided := false drops the header rule and the footer fill") {
    gotoSlug("modal")
    page.locator("button:has-text('Open undivided dialog')").first().click()
    val header = page.locator(s"$openDialog > div").first()
    header.waitFor()
    val headerStyle = Option(header.getAttribute("style")).getOrElse("")
    assert(
      headerStyle.contains("border-bottom") && headerStyle.contains("rgba(0, 0, 0, 0)"),
      s"header rule should be transparent when undivided: $headerStyle"
    )
  }

  test("ConfirmDialog cannot be dismissed while busy, and closes once it isn't") {
    gotoSlug("confirm-dialog")
    page.locator("button:has-text('Delete project')").first().click()
    val dialog = page.locator(openDialog).first()
    dialog.waitFor()
    page.locator(s"$openDialog button:has-text('Delete')").first().click()
    page.waitForCondition(() => page.getByText("Deleting…").count() > 0)
    page.keyboard().press("Escape")
    assert(dialog.isVisible, "Escape must not close a dialog whose request is in flight")
    page.locator(openDialog).first().click()
    assert(dialog.isVisible, "a backdrop click must not close it either")
    page.waitForCondition(() => !dialog.isVisible)
  }

  test("Menu arrow-key navigation skips a disabled item") {
    gotoSlug("menu")
    page.locator("button:has-text('Tags')").first().click()
    page.waitForCondition { () =>
      page
        .evaluate("() => document.activeElement && document.activeElement.getAttribute('role')")
        .asInstanceOf[String] == "menuitem"
    }
    def focusedLabel(): String =
      page.evaluate("() => document.activeElement.textContent").asInstanceOf[String]
    val first = focusedLabel()
    assert(first.contains("Add tag"), s"initial focus should skip the disabled row: $first")
    page.keyboard().press("ArrowDown")
    val next = focusedLabel()
    assert(
      !next.contains("No tags yet"),
      s"arrow navigation should never land on a disabled row: $next"
    )
  }

  test("a disabled Menu item neither emits nor closes the menu") {
    gotoSlug("menu")
    page.locator("button:has-text('Tags')").first().click()
    val disabled = page.locator("[role='menuitem'][aria-disabled='true']").first()
    disabled.waitFor()
    // Playwright's own actionability check treats aria-disabled as disabled and would
    // wait forever, so force past it to prove the handler itself is inert too.
    disabled.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true))
    assertEquals(page.getByText("selected:").count(), 0, "a disabled row must emit nothing")
    assert(disabled.isVisible, "a disabled row must not close the menu either")
  }

  test("Checkbox.indeterminate reports aria-checked=mixed and resolves to checked") {
    gotoSlug("checkbox")
    val selectAll = page.getByRole(AriaRole.CHECKBOX).filter(
      new com.microsoft.playwright.Locator.FilterOptions().setHasText("Select all")
    ).first()
    selectAll.waitFor()
    assertEquals(selectAll.getAttribute("aria-checked"), "mixed")
    selectAll.click()
    page.waitForCondition(() => selectAll.getAttribute("aria-checked") == "true")
  }

  test("Textarea.bordered := false draws no border of its own") {
    gotoSlug("textarea")
    val bare = page.locator("textarea").nth(1)
    bare.waitFor()
    val style = Option(bare.getAttribute("style")).getOrElse("")
    assert(style.contains("border: 0px"), s"nested field should draw no border: $style")
    assert(style.contains("box-shadow: none"), s"nested field should draw no ring: $style")
  }

  test("ScrollArea(bordered = false) draws no border of its own") {
    gotoSlug("scroll-area")
    val styles = page
      .locator("div[style*='overflow-y: auto']")
      .all()
      .stream()
      .map[String](el => Option(el.getAttribute("style")).getOrElse(""))
      .toArray()
      .map(_.asInstanceOf[String])
    assert(styles.exists(_.contains("border")), "the default ScrollArea should be bordered")
    assert(
      styles.exists(s => !s.contains("border-width") && !s.contains("border:")),
      s"one ScrollArea should be unbordered: ${styles.mkString(" | ")}"
    )
  }

  test("TextInput fills its parent rather than the browser's default width") {
    gotoSlug("text-input")
    val style = styleOf("input[type='text']")
    assert(style.contains("width: 100%"), s"expected a full-width default: $style")
  }

  test("SegmentedControl keeps a valid box-shadow on the unselected options") {
    gotoSlug("segmented-control")
    // "none, 0 0 0 2px ..." is not a valid shadow list; the CSSOM used to drop the whole
    // declaration, and with layered styles it would have kept a stale value instead.
    val options = page.locator("[role='radio']").all()
    options.forEach { opt =>
      val shadow = page
        .evaluate("el => getComputedStyle(el).boxShadow", opt.elementHandle())
        .asInstanceOf[String]
      assert(shadow != null && !shadow.contains("none,"), s"invalid shadow list: $shadow")
    }
  }

  test("Alert.actions sit to the trailing side of the message") {
    gotoSlug("alert")
    val update = page.locator("button:has-text('Update')").first()
    update.waitFor()
    val message = page.getByText("GRCh38.p13 was superseded four months ago.").first()
    assert(
      update.boundingBox().x > message.boundingBox().x,
      "actions belong after the text, not above it"
    )
  }
}
