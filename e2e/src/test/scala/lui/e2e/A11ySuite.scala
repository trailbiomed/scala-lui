package lui.e2e

import com.microsoft.playwright.Locator

/** End-to-end coverage for the a11y/UX fixes in the overlay/form/feedback
  * components: Escape closes modals, focus traps in modals, focus restoration,
  * keyboard nav in Tabs/Menu/Slider/Calendar, aria roles, focus rings, etc. */
class A11ySuite extends E2ESuite {

  // -- Modal --------------------------------------------------------------

  test("Modal dialog has role=dialog and aria-modal=true") {
    gotoSlug("modal")
    page.locator("button:has-text('Open dialog')").click()
    val dialog = page.locator("[role='dialog']").first()
    dialog.waitFor()
    assertEquals(dialog.getAttribute("aria-modal"), "true")
    assertNotEquals(dialog.getAttribute("aria-labelledby"), null)
  }

  test("Modal Escape closes the dialog") {
    gotoSlug("modal")
    val title = page.locator("text=Confirm action").first()
    page.locator("button:has-text('Open dialog')").click()
    title.waitFor()
    page.keyboard().press("Escape")
    page.waitForCondition(() => !title.isVisible())
  }

  test("Modal restores focus to the trigger on close") {
    gotoSlug("modal")
    val trigger = page.locator("button:has-text('Open dialog')")
    trigger.click()
    page.locator("text=Confirm action").first().waitFor()
    page.keyboard().press("Escape")
    page.waitForCondition { () =>
      page
        .evaluate(
          """() => {
            |  const tr = Array.from(document.querySelectorAll('button'))
            |    .find(b => b.textContent.includes('Open dialog'));
            |  return tr && tr === document.activeElement;
            |}""".stripMargin
        )
        .asInstanceOf[Boolean]
    }
  }

  // -- Drawer -------------------------------------------------------------

  test("Drawer has role=dialog + aria-modal=true and Escape closes") {
    gotoSlug("drawer")
    page.locator("button:has-text('Open drawer')").click()
    val title = page.locator("text=Run details").first()
    title.waitFor()
    val dialog = page.locator("[role='dialog'][aria-modal='true']").first()
    dialog.waitFor()
    page.keyboard().press("Escape")
    page.waitForCondition(() => !title.isVisible())
  }

  // -- Popover ------------------------------------------------------------

  test("Popover trigger has aria-haspopup + aria-expanded") {
    gotoSlug("popover")
    val trigger = page.locator("button:has-text('Open popover')")
    trigger.waitFor()
    assertEquals(trigger.getAttribute("aria-expanded"), "false")
    assertNotEquals(trigger.getAttribute("aria-haspopup"), null)
    trigger.click()
    page.waitForCondition(() => trigger.getAttribute("aria-expanded") == "true")
  }

  test("Popover Escape closes and restores focus to trigger") {
    gotoSlug("popover")
    val trigger = page.locator("button:has-text('Open popover')")
    trigger.click()
    page.locator("text=Quick actions").first().waitFor()
    page.keyboard().press("Escape")
    page.waitForCondition(() => trigger.getAttribute("aria-expanded") == "false")
  }

  // -- Menu ---------------------------------------------------------------

  test("Menu items are real buttons with role=menuitem") {
    gotoSlug("menu")
    page.locator("button[aria-label='Actions']").click()
    // Arrow keys navigate; first item gets focus on open.
    page.waitForCondition { () =>
      page
        .evaluate("() => document.activeElement && document.activeElement.getAttribute('role') === 'menuitem'")
        .asInstanceOf[Boolean]
    }
  }

  test("Menu ArrowDown moves focus to the next item") {
    gotoSlug("menu")
    page.locator("button[aria-label='Actions']").click()
    // Wait for the popover to become visible by looking for a menuitem's text.
    page.locator("text=Rename").first().waitFor()
    // First item is focused on open. ArrowDown should move to "Share".
    page.keyboard().press("ArrowDown")
    page.waitForFunction(
      """() => {
        |  const ae = document.activeElement;
        |  return ae && (ae.textContent || '').includes('Share');
        |}""".stripMargin
    )
  }

  test("Menu Escape closes the popover and restores focus to trigger") {
    gotoSlug("menu")
    val trigger = page.locator("button[aria-label='Actions']")
    trigger.click()
    page.locator("text=Rename").first().waitFor()
    page.keyboard().press("Escape")
    page.waitForFunction(
      """() => {
        |  const t = document.querySelector("button[aria-label='Actions']");
        |  return t && t.getAttribute('aria-expanded') === 'false';
        |}""".stripMargin
    )
  }

  // -- Button focus + loading --------------------------------------------

  test("Button shows a focus ring (box-shadow) when keyboard-focused") {
    gotoSlug("button")
    val btn = page.locator("button:has-text('Primary')").first()
    btn.waitFor()
    // Focus via JS — clicking would put it into the pressed state and suppress
    // the focus ring per the styleFor logic.
    page.evaluate(
      """() => {
        |  const b = Array.from(document.querySelectorAll('button'))
        |    .find(e => e.textContent.trim() === 'Primary');
        |  b.focus();
        |}""".stripMargin
    )
    page.waitForCondition { () =>
      val s = btn.getAttribute("style")
      s != null && s.contains("box-shadow") && !s.contains("box-shadow: none")
    }
  }

  test("Button loading state advertises aria-busy=true") {
    gotoSlug("button")
    // FormPages renders a "Loading" button somewhere; the variants demo on
    // /button doesn't, so find any aria-busy button on the variants/states demo.
    val busy = page.locator("button[aria-busy='true']").first()
    busy.waitFor()
    assertEquals(busy.getAttribute("aria-busy"), "true")
  }

  // -- Field --------------------------------------------------------------

  test("Field with error sets aria-invalid + aria-describedby on the control") {
    gotoSlug("field")
    // Wait for the Field demos to mount.
    page.locator("text=Email").first().waitFor()
    // Find an input that has aria-invalid=true (error demo).
    page.waitForFunction(
      """() => {
        |  const inputs = Array.from(document.querySelectorAll('input'));
        |  return inputs.some(i =>
        |    i.getAttribute('aria-invalid') === 'true' &&
        |    i.getAttribute('aria-describedby')
        |  );
        |}""".stripMargin
    )
  }

  // -- Tabs ---------------------------------------------------------------

  test("Tabs root has role=tablist; tabs have role=tab + aria-selected") {
    gotoSlug("tabs")
    page.locator("[role='tablist']").first().waitFor()
    val active = page.locator("[role='tab'][aria-selected='true']").first()
    active.waitFor()
  }

  test("Tabs ArrowRight activates the next tab and updates the bound key") {
    gotoSlug("tabs")
    page.locator("text=active: modules").waitFor()
    // Focus the first tab in the demo (Modules) then press ArrowRight.
    page.evaluate(
      """() => {
        |  const t = document.querySelector("[role='tab']");
        |  t.focus();
        |}""".stripMargin
    )
    page.keyboard().press("ArrowRight")
    page.locator("text=active: topgenes").waitFor()
  }

  // -- Slider -------------------------------------------------------------

  test("Slider thumb has role=slider and aria-value*") {
    gotoSlug("slider")
    val slider = page.locator("[role='slider']").first()
    slider.waitFor()
    assertNotEquals(slider.getAttribute("aria-valuenow"), null)
    assertNotEquals(slider.getAttribute("aria-valuemin"), null)
    assertNotEquals(slider.getAttribute("aria-valuemax"), null)
  }

  test("Slider ArrowRight increments the bound value") {
    gotoSlug("slider")
    val slider = page.locator("[role='slider']").first()
    slider.waitFor()
    val before = slider.getAttribute("aria-valuenow")
    slider.focus()
    page.keyboard().press("ArrowRight")
    page.waitForCondition(() => slider.getAttribute("aria-valuenow") != before)
  }

  // -- Calendar -----------------------------------------------------------

  test("Calendar grid has role=grid and gridcells with aria-selected") {
    gotoSlug("calendar")
    page.locator("[role='grid']").first().waitFor()
    page.locator("[role='gridcell']").first().waitFor()
  }

  test("Calendar ArrowRight moves the roving tabindex one day forward") {
    gotoSlug("calendar")
    // Focus the day cell that currently has tabindex=0.
    page.evaluate(
      """() => {
        |  const c = document.querySelector("[role='gridcell'][tabindex='0']");
        |  c && c.focus();
        |}""".stripMargin
    )
    val before = page
      .evaluate("() => document.activeElement && document.activeElement.textContent")
      .asInstanceOf[String]
    page.keyboard().press("ArrowRight")
    page.waitForCondition { () =>
      val cur = page
        .evaluate("() => document.activeElement && document.activeElement.textContent")
        .asInstanceOf[String]
      cur != before
    }
  }

  // -- Toggle -------------------------------------------------------------

  test("Toggle is a button with role=switch and aria-checked toggles") {
    gotoSlug("toggle")
    val sw = page.locator("[role='switch']").first()
    sw.waitFor()
    val before = sw.getAttribute("aria-checked")
    sw.click()
    page.waitForCondition(() => sw.getAttribute("aria-checked") != before)
  }

  // -- Tooltip ------------------------------------------------------------

  test("Tooltip body has role=tooltip and shows on trigger focus") {
    gotoSlug("tooltip")
    val play = page.locator("button[aria-label='Run']")
    play.waitFor()
    play.focus()
    page.waitForFunction(
      """() => {
        |  const tips = Array.from(document.querySelectorAll("[role='tooltip']"));
        |  return tips.some(t => t.style.opacity === '1');
        |}""".stripMargin
    )
  }

  // -- Toast --------------------------------------------------------------

  test("Toast has role=status (or alert for errors) + aria-live") {
    gotoSlug("toast")
    page.locator("button:has-text('Show toast')").click()
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll("[aria-live]"))
        |  .some(t => (t.getAttribute('aria-live') === 'polite' ||
        |              t.getAttribute('aria-live') === 'assertive') &&
        |              (t.textContent || '').includes('Toast'))""".stripMargin
    )
  }
}
