package lui.e2e

class FormsSuite extends E2ESuite {

  test("TextInput two-way binding mirrors typed text") {
    gotoSlug("text-input")
    val input = page.locator("input").first()
    input.waitFor()
    input.fill("")
    input.fill("hello world")
    page.locator("text=value = 'hello world'").waitFor()
  }

  test("Textarea binding mirrors typed text") {
    gotoSlug("textarea")
    val ta = page.locator("textarea").first()
    ta.waitFor()
    ta.fill("line one\nline two")
    assertEquals(ta.inputValue(), "line one\nline two")
  }

  test("NumberInput stepper increments the value") {
    gotoSlug("number-input")
    page.locator("text=value = 8.0").waitFor()
    // NumberInput uses ▴ (up) and ▾ (down) glyphs for its steppers.
    val upBtn = page.locator("button:has-text('▴')").first()
    upBtn.waitFor()
    upBtn.click()
    page.locator("text=value = 9.0").waitFor()
    upBtn.click()
    page.locator("text=value = 10.0").waitFor()
  }

  test("PasswordInput renders as type=password and accepts text") {
    gotoSlug("password-input")
    val input = page.locator("input[type='password']").first()
    input.waitFor()
    input.fill("secret123")
    assertEquals(input.inputValue(), "secret123")
  }

  test("PinInput auto-advances and updates the entered string") {
    gotoSlug("pin-input")
    val firstCell = page.locator("input").first()
    firstCell.waitFor()
    firstCell.click()
    page.keyboard().press("1")
    page.keyboard().press("2")
    page.locator("text=entered: '12'").waitFor()
  }

  test("TagsInput accepts a new tag on Enter") {
    gotoSlug("tags-input")
    page.locator("text=scala, laminar").waitFor()
    val input = page.locator("input").first()
    input.click()
    input.pressSequentially("scalajs")
    page.keyboard().press("Enter")
    page.locator("text=scala, laminar, scalajs").waitFor()
  }

  test("Dropdown native select changes the value") {
    gotoSlug("dropdown")
    val sel = page.locator("select").first()
    sel.waitFor()
    sel.selectOption("cherry")
    assertEquals(sel.inputValue(), "cherry")
  }

  test("SegmentedControl click switches the active button styling") {
    gotoSlug("segmented-control")
    val daBtn = page.locator("button:has-text('Day')")
    val wkBtn = page.locator("button:has-text('Week')")
    daBtn.waitFor()
    val initialDay  = daBtn.getAttribute("style")
    val initialWeek = wkBtn.getAttribute("style")
    daBtn.click()
    page.waitForCondition { () =>
      val s = daBtn.getAttribute("style")
      s != null && s != initialDay
    }
    // Sanity check: Week's style also changed (was active, now inactive).
    assertNotEquals(wkBtn.getAttribute("style"), initialWeek)
  }

  test("RadioGroup click changes which option is filled") {
    gotoSlug("radio-group")
    val smallOpt = page.locator("[role='radio']:has-text('Small')").first()
    smallOpt.waitFor()
    val before = smallOpt.locator("span").first().getAttribute("style")
    smallOpt.click()
    page.waitForCondition { () =>
      smallOpt.locator("span").first().getAttribute("style") != before
    }
  }

  test("Slider renders with the bound value display") {
    gotoSlug("slider")
    // Drag is brittle in CI; assert the display shows the bound starting value
    // and that the slider element is present.
    page.locator("text=40%").first().waitFor()
    page.locator("text=Threshold").first().waitFor()
  }

  test("Editable swaps into an input on click and commits on Enter") {
    gotoSlug("editable")
    val preview = page.locator("text=Click me to edit").first()
    preview.waitFor()
    preview.click()
    val editor = page.locator("input").first()
    editor.waitFor()
    editor.fill("renamed")
    page.keyboard().press("Enter")
    page.locator("text=renamed").first().waitFor()
  }
}
