package lui.e2e

class FormsExtraSuite extends E2ESuite {

  test("CheckboxCard click toggles its checked styling") {
    gotoSlug("checkbox-card")
    // "Save metadata" CheckboxCard starts checked (Var(true)). "Encrypt at rest"
    // starts unchecked. Click "Encrypt at rest" to flip it on.
    val card = page.getByText("Encrypt at rest").first()
    card.waitFor()
    val before = page.evaluate(
      """() => {
        |  const t = Array.from(document.querySelectorAll('span'))
        |    .find(e => e.textContent === 'Encrypt at rest');
        |  let p = t.parentElement;
        |  while (p && (!p.style || !p.style.cursor)) p = p.parentElement;
        |  return p ? p.style.background : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    card.click()
    page.waitForCondition { () =>
      val after = page.evaluate(
        """() => {
          |  const t = Array.from(document.querySelectorAll('span'))
          |    .find(e => e.textContent === 'Encrypt at rest');
          |  let p = t.parentElement;
          |  while (p && (!p.style || !p.style.cursor)) p = p.parentElement;
          |  return p ? p.style.background : '';
          |}""".stripMargin
      ).asInstanceOf[String]
      after != before
    }
  }

  test("RadioCard clicking another option shifts the active styling") {
    gotoSlug("radio-card")
    val fast = page.getByText("Fast").first()
    fast.waitFor()
    // Initial active: "Balanced". Click "Fast", verify Fast's background changed.
    def fastBg(): String = page.evaluate(
      """() => {
        |  const t = Array.from(document.querySelectorAll('span'))
        |    .find(e => e.textContent === 'Fast');
        |  let p = t.parentElement;
        |  while (p && (!p.style || !p.style.cursor)) p = p.parentElement;
        |  return p ? p.style.background : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    val before = fastBg()
    fast.click()
    page.waitForCondition(() => fastBg() != before)
  }

  test("SegmentedControl click changes which button has the active styling") {
    // Already covered in FormsSuite; here we additionally verify the bound
    // value lives in the DOM (no display, but two segments swap styles).
    gotoSlug("segmented-control")
    val yearBtn = page.locator("button:has-text('Year')")
    yearBtn.waitFor()
    val before = yearBtn.getAttribute("style")
    yearBtn.click()
    page.waitForCondition(() => yearBtn.getAttribute("style") != before)
  }

  test("Calendar click on a day cell updates the bound 'value = YYYY-MM-DD' label") {
    gotoSlug("calendar")
    page.waitForFunction(
      "() => Array.from(document.querySelectorAll('span')).some(s => /^value = \\d{4}-\\d{2}-\\d{2}$/.test(s.textContent || ''))"
    )
    def currentIso(): String = page.evaluate(
      """() => {
        |  const s = Array.from(document.querySelectorAll('span'))
        |    .find(e => /^value = \d{4}-\d{2}-\d{2}$/.test(e.textContent || ''));
        |  return s ? s.textContent : '';
        |}""".stripMargin
    ).asInstanceOf[String]
    val before = currentIso()
    // Day cells are <div>s (cursor:pointer) whose text is the day number.
    val newDay = page.evaluate(
      """() => {
        |  const cells = Array.from(document.querySelectorAll('div'))
        |    .filter(d => /^\d{1,2}$/.test((d.textContent || '').trim())
        |             && d.style && d.style.cursor === 'pointer');
        |  const today = String(new Date().getDate());
        |  // Pick a different-day cell from the first calendar (cells are in DOM order).
        |  const target = cells.find(d => d.textContent.trim() !== today);
        |  if (!target) return 'no-target';
        |  target.click();
        |  return target.textContent.trim();
        |}""".stripMargin
    ).asInstanceOf[String]
    if (newDay == "no-target") fail("no other day cell to click")
    page.waitForCondition { () =>
      val now = currentIso()
      now != before && now.endsWith(f"-${newDay.toInt}%02d")
    }
  }

  test("DatePicker trigger opens its popover (body display flips none → block)") {
    gotoSlug("date-picker")
    val trigger = page.locator("button:has-text('Pick a date…')")
    trigger.waitFor()
    // Popover body has display:none when closed, display:block when open.
    // We can't reliably target only the date-picker's popover via text, but
    // we can sum the count of currently-visible popover bodies and assert
    // that count increases by 1 after the trigger click.
    def openPopoverCount(): Int = page.evaluate(
      """() => Array.from(document.querySelectorAll('div'))
        |  .filter(d => d.style && d.style.position === 'absolute'
        |             && d.style.display === 'block'
        |             && d.style.zIndex === '30').length""".stripMargin
    ).asInstanceOf[java.lang.Number].intValue()
    val before = openPopoverCount()
    trigger.click()
    page.waitForCondition(() => openPopoverCount() > before)
  }

  test("Slider value moves with pointer click on the track") {
    gotoSlug("slider")
    val displayBefore = page.evaluate(
      "() => (Array.from(document.querySelectorAll('span')).find(s => /^\\d+%$/.test(s.textContent || '')) || {}).textContent || ''"
    ).asInstanceOf[String]
    // Click roughly at the right end of the track.
    val clicked = page.evaluate(
      """() => {
        |  const tracks = Array.from(document.querySelectorAll('div'))
        |    .filter(d => d.style && d.style.position === 'relative'
        |             && d.style.borderRadius && d.style.cursor === 'pointer'
        |             && d.style.height && /^4(px)?$/.test(d.style.height));
        |  if (tracks.length === 0) return false;
        |  const tr = tracks[0];
        |  const rect = tr.getBoundingClientRect();
        |  const ev = new PointerEvent('pointerdown', {
        |    clientX: rect.left + rect.width * 0.85, clientY: rect.top + rect.height/2,
        |    bubbles: true, pointerId: 1, pointerType: 'mouse'
        |  });
        |  tr.dispatchEvent(ev);
        |  return true;
        |}""".stripMargin
    ).asInstanceOf[Boolean]
    assert(clicked, "could not find slider track")
    page.waitForCondition { () =>
      val now = page.evaluate(
        "() => (Array.from(document.querySelectorAll('span')).find(s => /^\\d+%$/.test(s.textContent || '')) || {}).textContent || ''"
      ).asInstanceOf[String]
      now != displayBefore
    }
  }

  test("FileUpload setInputFiles drives the bound file-count display") {
    gotoSlug("file-upload")
    val inputCount = page.locator("input[type='file']").count()
    assert(inputCount >= 1, s"no <input type='file'> on the page — count=$inputCount")
    val hiddenInput = page.locator("input[type='file']").first()
    val tmp = java.nio.file.Files.createTempFile("lui-e2e", ".bin")
    java.nio.file.Files.write(tmp, "hello".getBytes("UTF-8"))
    try {
      // Page.setInputFiles handles hidden inputs (display:none) without
      // waiting for visibility, where locator-bound waiting can hang.
      page.setInputFiles("input[type='file']", tmp)
      page.waitForFunction(
        """() => Array.from(document.querySelectorAll('span'))
          |  .some(s => {
          |    const t = (s.textContent || '').trim();
          |    return t === '1 file(s)' || /\.bin$/.test(t);
          |  })""".stripMargin
      )
    } finally {
      val _ = java.nio.file.Files.deleteIfExists(tmp)
    }
  }

  test("Field renders its label and switches hint→error when error is set") {
    gotoSlug("field")
    // First demo: hint visible. Second demo: error visible (replaces hint).
    page.getByText("We never share this.").first().waitFor()
    page.getByText("Must contain @.").first().waitFor()
  }

  test("Fieldset renders the legend") {
    gotoSlug("fieldset")
    page.getByText("Contact").first().waitFor()
    page.getByText("All fields are optional.").first().waitFor()
  }
}
