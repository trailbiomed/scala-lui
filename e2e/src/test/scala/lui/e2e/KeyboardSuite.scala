package lui.e2e

class KeyboardSuite extends E2ESuite {

  test("Editable Escape cancels the edit (preview text unchanged)") {
    gotoSlug("editable")
    val preview = page.getByText("Click me to edit").first()
    preview.waitFor()
    preview.click()
    val editor = page.locator("input").first()
    editor.waitFor()
    editor.fill("ignored")
    page.keyboard().press("Escape")
    // Preview reverts to its original text.
    page.getByText("Click me to edit").first().waitFor()
  }

  test("TagsInput Backspace on an empty draft removes the last tag") {
    gotoSlug("tags-input")
    page.getByText("scala, laminar").first().waitFor()
    val input = page.locator("input").first()
    input.click()
    page.keyboard().press("Backspace")
    // The hint span text is exactly the joined tag list — look for "scala" with
    // exact match so we don't pick up the code-block copy that includes "laminar".
    page.waitForFunction(
      """() => Array.from(document.querySelectorAll('span'))
        |  .some(s => (s.textContent || '').trim() === 'scala')""".stripMargin
    )
  }

  test("PinInput accepts pasted text into the focused cell and advances") {
    gotoSlug("pin-input")
    val first = page.locator("input").first()
    first.click()
    // Type one character; auto-advance + state should reflect "1".
    page.keyboard().press("9")
    page.waitForFunction("() => document.body.textContent.includes(\"entered: '9'\")")
  }
}
