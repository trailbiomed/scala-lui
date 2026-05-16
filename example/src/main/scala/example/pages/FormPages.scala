package example.pages

import com.raquo.laminar.api.L.{Mod as _, *}
import example.PageTemplate
import lui.*
import lui.style.*
import lui.components.*

object FormPages {

  def calendar(): HtmlElement = PageTemplate(
    title = "Calendar",
    summary = "Month-grid date selector. Pure presentation — no popover, no trigger. " +
      "Wrap with `DatePicker` for the click-to-open behavior."
  )(
    PageTemplate.section("Two-way binding")(
      PageTemplate.codedDemo(
        "Calendar.value <-->",
        """val picked = Var[Option[Day]](None)
          |Calendar(Calendar.value <--> picked)""".stripMargin
      )({
        val picked = Var[Option[Day]](Some(Day.today))
        div(
          stack.col(spacing.md) ++ css.alignItems("flex-start"),
          Calendar(Calendar.value <--> picked),
          span(typo.hint, child.text <-- picked.signal.map(o => s"value = ${o.map(_.iso).getOrElse("None")}"))
        )
      })
    ),
    PageTemplate.section("Bounded range + custom predicate")(
      PageTemplate.codedDemo(
        "min / max / disabledFn",
        """val picked = Var[Option[Day]](None)
          |Calendar(
          |  Calendar.value <--> picked,
          |  Calendar.min := Some(Day.today.addDays(-7)),
          |  Calendar.max := Some(Day.today.addDays(21)),
          |  // disable weekends:
          |  Calendar.disabledFn := (d => d.dayOfWeekMon >= 5)
          |)""".stripMargin
      )({
        val picked = Var[Option[Day]](None)
        Calendar(
          Calendar.value <--> picked,
          Calendar.min := Some(Day.today.addDays(-7)),
          Calendar.max := Some(Day.today.addDays(21)),
          Calendar.disabledFn := ((d: Day) => d.dayOfWeekMon >= 5)
        )
      })
    ),
    PageTemplate.section("Sunday-first week")(
      PageTemplate.codedDemo(
        "weekStart := 0",
        """Calendar(Calendar.value <--> picked, Calendar.weekStart := 0)""".stripMargin
      )({
        val picked = Var[Option[Day]](None)
        Calendar(Calendar.value <--> picked, Calendar.weekStart := 0)
      })
    ),
    PageTemplate.behavior(
      "Today is outlined with the brand color (no fill). The selected date fills with brand color.",
      "Out-of-month days are dimmed but clickable; clicking jumps to that month and selects.",
      "min/max bound the selectable range. disabledFn is an extra predicate on top of that.",
      "Set bordered := false when embedding inside another bordered container (e.g. a Popover). DatePicker does this automatically."
    ),
    PageTemplate.propsTable(
      ("value",      "InOut[Option[Day]]",    "Selected date. None means nothing selected."),
      ("month",      "InOut[Day]",            "Pivot date (year + month displayed). Two-way so the parent can navigate."),
      ("weekStart",  "In[Int]",               "0 = Sunday-first, 1 = Monday-first (default)."),
      ("min",        "In[Option[Day]]",       "Earliest selectable date, inclusive."),
      ("max",        "In[Option[Day]]",       "Latest selectable date, inclusive."),
      ("disabledFn", "In[Day => Boolean]",    "Extra predicate; days returning true are dim and unclickable."),
      ("bordered",   "In[Boolean]",           "Draw the outer border + radius. Default true.")
    )
  )

  def datePicker(): HtmlElement = PageTemplate(
    title = "DatePicker",
    summary = "Click-to-open date input. Trigger displays the value (or placeholder); the popover hosts a Calendar."
  )(
    PageTemplate.section("Two-way binding")(
      PageTemplate.codedDemo(
        "DatePicker.value <-->",
        """val due = Var[Option[Day]](None)
          |DatePicker(
          |  DatePicker.value <--> due,
          |  DatePicker.placeholder := "Pick a date…"
          |)""".stripMargin
      )({
        val due = Var[Option[Day]](None)
        div(
          stack.col(spacing.md) ++ css.alignItems("flex-start"),
          DatePicker(
            DatePicker.value <--> due,
            DatePicker.placeholder := "Pick a date…"
          ),
          span(typo.hint, child.text <-- due.signal.map(o => s"value = ${o.map(_.iso).getOrElse("None")}"))
        )
      })
    ),
    PageTemplate.section("Bounded range")(
      PageTemplate.codedDemo(
        "min / max",
        """DatePicker(
          |  DatePicker.value <--> due,
          |  DatePicker.min := Some(Day.today),
          |  DatePicker.max := Some(Day.today.addDays(30))
          |)""".stripMargin
      )({
        val due = Var[Option[Day]](None)
        DatePicker(
          DatePicker.value <--> due,
          DatePicker.min := Some(Day.today),
          DatePicker.max := Some(Day.today.addDays(30))
        )
      })
    ),
    PageTemplate.section("Disabled")(
      PageTemplate.codedDemo(
        "DatePicker.disabled := true",
        """DatePicker(
          |  DatePicker.value := Some(Day.today),
          |  DatePicker.disabled := true
          |)""".stripMargin
      )(
        DatePicker(
          DatePicker.value := Some(Day.today),
          DatePicker.disabled := true
        )
      )
    ),
    PageTemplate.behavior(
      "Selecting a day in the popover writes through to value and closes the popover.",
      "Opening the popover jumps the calendar to the month of the current value, or today's month if value is None.",
      "min / max are forwarded to the wrapped Calendar.",
      "The trigger shows the ISO date (YYYY-MM-DD) when there's a value; otherwise it shows the placeholder dimmed."
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[Option[Day]]", "Selected date."),
      ("placeholder", "In[String]",         "Trigger text when value is None. Default \"YYYY-MM-DD\"."),
      ("disabled",    "In[Boolean]",        "Disable the trigger button."),
      ("width",       "In[Length]",         "Trigger width. Default 180px."),
      ("min",         "In[Option[Day]]",    "Earliest selectable date."),
      ("max",         "In[Option[Day]]",    "Latest selectable date.")
    )
  )

  def textInput(): HtmlElement = PageTemplate(
    title = "TextInput",
    summary = "Single-line text input. A common starting point for any form."
  )(
    PageTemplate.section("Two-way binding")(
      PageTemplate.codedDemo(
        "TextInput.value <-->",
        """val text = Var("Hello")
          |TextInput(
          |  TextInput.value <--> text,
          |  TextInput.placeholder := "Enter text…"
          |)""".stripMargin
      )({
        val v = Var("Hello")
        div(stack.col(spacing.sm),
          TextInput(TextInput.value <--> v, TextInput.placeholder := "Enter text…"),
          span(typo.hint, child.text <-- v.signal.map(s => s"value = '$s'"))
        )
      })
    ),
    PageTemplate.section("Invalid / disabled")(
      PageTemplate.codedDemo(
        "TextInput.invalid / .disabled",
        """TextInput(TextInput.value <--> text, TextInput.invalid := true)
          |TextInput(TextInput.placeholder := "Disabled", TextInput.disabled := true)""".stripMargin
      )({
        val v = Var("oops")
        div(stack.row(spacing.md) ++ stack.wrap,
          TextInput(TextInput.value <--> v, TextInput.invalid := true),
          TextInput(TextInput.placeholder := "Disabled", TextInput.disabled := true)
        )
      })
    ),
    PageTemplate.section("Alignment & width")(
      PageTemplate.codedDemo(
        "TextInput.align / .width",
        """TextInput(TextInput.placeholder := "Right",
          |  TextInput.align := TextAlign.Right,
          |  TextInput.width := Length.px(160))""".stripMargin
      )(
        div(stack.row(spacing.md),
          TextInput(TextInput.placeholder := "Left",  TextInput.width := Length.px(160)),
          TextInput(TextInput.placeholder := "Right", TextInput.align := TextAlign.Right, TextInput.width := Length.px(160))
        )
      )
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]",    "Two-way binding."),
      ("placeholder", "String",           "Placeholder text."),
      ("disabled",    "Boolean",          "Disables the input."),
      ("invalid",     "Boolean",          "Renders the danger border + shadow."),
      ("variant",     "Text | Number",    "Sets the underlying input type."),
      ("align",       "TextAlign",        "Text alignment."),
      ("width",       "Length",           "Width of the input.")
    )
  )

  def textarea(): HtmlElement = PageTemplate(
    title = "Textarea",
    summary = "Multi-line text input."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Textarea",
        """val body = Var("Multiline content\nspans two lines.")
          |Textarea(
          |  Textarea.value <--> body,
          |  Textarea.rows := 3,
          |  Textarea.placeholder := "Description"
          |)""".stripMargin
      )({
        val v = Var("Multiline content\nspans two lines.")
        Textarea(Textarea.value <--> v, Textarea.rows := 3, Textarea.placeholder := "Description")
      })
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]", "Two-way binding."),
      ("rows",        "Int",           "Visible row count."),
      ("placeholder", "String",        "Placeholder text."),
      ("disabled",    "Boolean",       "Disables the input."),
      ("invalid",     "Boolean",       "Renders the danger border + shadow."),
      ("resizable",   "Boolean",       "Allow vertical resize. Default true.")
    )
  )

  def numberInput(): HtmlElement = PageTemplate(
    title = "NumberInput",
    summary = "Numeric input with stepper buttons. Value is a Double."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "NumberInput",
        """val n = Var(8.0)
          |NumberInput(
          |  NumberInput.value <--> n,
          |  NumberInput.min := 0,
          |  NumberInput.max := 32
          |)""".stripMargin
      )({
        val v = Var(8.0)
        div(stack.row(spacing.lg) ++ css.alignItems("center"),
          NumberInput(NumberInput.value <--> v, NumberInput.min := 0, NumberInput.max := 32),
          span(typo.hint, child.text <-- v.signal.map(d => f"value = $d%.1f"))
        )
      })
    ),
    PageTemplate.behavior(
      "Renders as `<input type=\"text\" inputmode=\"decimal\">` to suppress the browser's native spinner; the stepper buttons are part of the component.",
      "Min/max clamp on blur and on each stepper press."
    ),
    PageTemplate.propsTable(
      ("value",   "InOut[Double]", "Two-way binding."),
      ("min",     "Double",        "Min value (clamped on blur and via buttons)."),
      ("max",     "Double",        "Max value."),
      ("step",    "Double",        "Increment used by the stepper. Default 1.0."),
      ("disabled","Boolean",       "Disables the input."),
      ("width",   "Length",        "Width of the control.")
    )
  )

  def passwordInput(): HtmlElement = PageTemplate(
    title = "PasswordInput",
    summary = "Password text input with a reveal toggle."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "PasswordInput",
        """val pw = Var("hunter2")
          |PasswordInput(
          |  PasswordInput.value <--> pw,
          |  PasswordInput.placeholder := "Password"
          |)""".stripMargin
      )({
        val v = Var("hunter2")
        PasswordInput(PasswordInput.value <--> v, PasswordInput.placeholder := "Password")
      })
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]", "Two-way binding."),
      ("placeholder", "String",        "Placeholder text."),
      ("disabled",    "Boolean",       "Disables the input."),
      ("invalid",     "Boolean",       "Renders the danger border + shadow.")
    )
  )

  def pinInput(): HtmlElement = PageTemplate(
    title = "PinInput",
    summary = "Fixed-length code entry split into N single-character inputs."
  )(
    PageTemplate.section("Length 4")(
      PageTemplate.codedDemo(
        "PinInput",
        """val code = Var("")
          |PinInput(
          |  PinInput.value <--> code,
          |  PinInput.length := 4
          |)""".stripMargin
      )({
        val v = Var("")
        div(stack.col(spacing.sm),
          PinInput(PinInput.value <--> v, PinInput.length := 4),
          span(typo.hint, child.text <-- v.signal.map(s => s"entered: '$s'"))
        )
      })
    ),
    PageTemplate.section("Masked (length 6)")(
      PageTemplate.codedDemo(
        "PinInput.mask",
        """PinInput(
          |  PinInput.value <--> code,
          |  PinInput.length := 6,
          |  PinInput.mask := true
          |)""".stripMargin
      )({
        val v = Var("")
        PinInput(PinInput.value <--> v, PinInput.length := 6, PinInput.mask := true)
      })
    ),
    PageTemplate.behavior(
      "Typing a character auto-advances focus to the next cell.",
      "Pasting a multi-character string fills cells from the focused position onward and moves focus to the last filled cell.",
      "Setting `mask := true` renders type=password — useful for OTP entry."
    ),
    PageTemplate.propsTable(
      ("value",  "InOut[String]", "Two-way binding."),
      ("length", "Int",           "Number of cells."),
      ("mask",   "Boolean",       "Mask input characters.")
    )
  )

  def tagsInput(): HtmlElement = PageTemplate(
    title = "TagsInput",
    summary = "Multi-value input that produces chip tokens. Type and press Enter or comma to commit."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "TagsInput",
        """val tags = Var(Seq("scala", "laminar"))
          |TagsInput(
          |  TagsInput.value <--> tags,
          |  TagsInput.placeholder := "Add language…"
          |)""".stripMargin
      )({
        val v = Var(Seq("scala", "laminar"))
        div(stack.col(spacing.sm),
          TagsInput(TagsInput.value <--> v, TagsInput.placeholder := "Add language…"),
          span(typo.hint, child.text <-- v.signal.map(_.mkString(", ")))
        )
      })
    ),
    PageTemplate.behavior(
      "Enter or comma commits the current draft.",
      "Backspace on an empty draft removes the last tag.",
      "Duplicates are skipped on commit."
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[Seq[String]]", "Two-way binding."),
      ("placeholder", "String",             "Placeholder text."),
      ("disabled",    "Boolean",            "Disables the input.")
    )
  )

  def editable(): HtmlElement = PageTemplate(
    title = "Editable",
    summary = "Click-to-edit text. Inline edit mode commits on Enter or blur; Escape cancels."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Editable",
        """val title = Var("Click me to edit")
          |Editable(
          |  Editable.value <--> title,
          |  Editable.placeholder := "Empty"
          |)""".stripMargin
      )({
        val v = Var("Click me to edit")
        Editable(Editable.value <--> v, Editable.placeholder := "Empty")
      })
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]",  "Two-way binding."),
      ("placeholder", "String",         "Shown when the value is empty."),
      ("editing",     "InOut[Boolean]", "Edit-mode state — useful for programmatic enter/exit.")
    )
  )

  def fileUpload(): HtmlElement = PageTemplate(
    title = "FileUpload",
    summary = "Drag-and-drop dropzone wrapping a hidden file input."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "FileUpload",
        """val files = Var(Seq.empty[dom.File])
          |FileUpload(
          |  FileUpload.files <--> files,
          |  FileUpload.multiple := true,
          |  FileUpload.accept := "image/*"
          |)""".stripMargin
      )({
        val files = Var(Seq.empty[org.scalajs.dom.File])
        div(stack.col(spacing.md),
          FileUpload(FileUpload.files <--> files, FileUpload.multiple := true, FileUpload.accept := "image/*"),
          span(typo.hint, child.text <-- files.signal.map(fs => s"${fs.size} file(s)"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("files",    "InOut[Seq[dom.File]]", "Selected files."),
      ("multiple", "Boolean",              "Allow multiple files."),
      ("accept",   "String",               "MIME-type filter (e.g. image/*)."),
      ("label",    "String",               "Visible dropzone caption.")
    )
  )

  def checkbox(): HtmlElement = PageTemplate(
    title = "Checkbox",
    summary = "Single binary choice with a label."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Checkbox",
        """val on = Var(true)
          |Checkbox(Checkbox.label := "Enable batch correction", Checkbox.checked <--> on)
          |Checkbox(Checkbox.label := "Normalize before split")
          |Checkbox(Checkbox.label := "Disabled option", Checkbox.disabled := true)""".stripMargin
      )({
        val a = Var(true)
        div(stack.col(spacing.md),
          Checkbox(Checkbox.label := "Enable batch correction", Checkbox.checked <--> a),
          Checkbox(Checkbox.label := "Normalize before split"),
          Checkbox(Checkbox.label := "Disabled option", Checkbox.disabled := true)
        )
      })
    ),
    PageTemplate.propsTable(
      ("label",    "String",         "Label text."),
      ("checked",  "InOut[Boolean]", "Two-way binding."),
      ("disabled", "Boolean",        "Disables interaction.")
    )
  )

  def checkboxCard(): HtmlElement = PageTemplate(
    title = "CheckboxCard",
    summary = "Checkbox presented as a clickable card with a title and description."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "CheckboxCard",
        """CheckboxCard(
          |  CheckboxCard.title := "Save metadata",
          |  CheckboxCard.description := "Include sample sheet + run config in the bundle.",
          |  CheckboxCard.checked <--> save
          |)""".stripMargin
      )({
        val a = Var(true)
        val b = Var(false)
        SimpleGrid(columns = 2, gap = spacing.md)(
          CheckboxCard(
            CheckboxCard.title := "Save metadata",
            CheckboxCard.description := "Include sample sheet + run config in the bundle.",
            CheckboxCard.checked <--> a
          ),
          CheckboxCard(
            CheckboxCard.title := "Encrypt at rest",
            CheckboxCard.description := "Adds about 2–3% to total upload time.",
            CheckboxCard.checked <--> b
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("title",       "String",         "Card title."),
      ("description", "String",         "Card description."),
      ("checked",     "InOut[Boolean]", "Two-way binding."),
      ("disabled",    "Boolean",        "Disables the card.")
    )
  )

  def radioGroup(): HtmlElement = PageTemplate(
    title = "RadioGroup",
    summary = "Mutually-exclusive choice."
  )(
    PageTemplate.section("Vertical (default)")(
      PageTemplate.codedDemo(
        "RadioGroup",
        """val choice = Var("medium")
          |RadioGroup(
          |  RadioGroup.value <--> choice,
          |  RadioGroup.options := Seq(
          |    "small"  -> "Small",
          |    "medium" -> "Medium",
          |    "large"  -> "Large"
          |  )
          |)""".stripMargin
      )({
        val v = Var("medium")
        RadioGroup(
          RadioGroup.value <--> v,
          RadioGroup.options := Seq("small" -> "Small", "medium" -> "Medium", "large" -> "Large")
        )
      })
    ),
    PageTemplate.section("Horizontal")(
      PageTemplate.codedDemo(
        "RadioGroup.orientation := Horizontal",
        """RadioGroup(
          |  RadioGroup.value <--> v,
          |  RadioGroup.orientation := RadioGroup.Orientation.Horizontal,
          |  RadioGroup.options := Seq("day" -> "Day", "week" -> "Week", "month" -> "Month")
          |)""".stripMargin
      )({
        val v = Var("day")
        RadioGroup(
          RadioGroup.value <--> v,
          RadioGroup.orientation := RadioGroup.Orientation.Horizontal,
          RadioGroup.options := Seq("day" -> "Day", "week" -> "Week", "month" -> "Month")
        )
      })
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]",       "Two-way binding to the selected key."),
      ("options",     "Seq[(String,String)]","(key, label) pairs."),
      ("orientation", "Vertical|Horizontal", "Layout direction."),
      ("disabled",    "Boolean",             "Disables the whole group.")
    )
  )

  def radioCard(): HtmlElement = PageTemplate(
    title = "RadioCard",
    summary = "Card-style radio group."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "RadioCard",
        """val plan = Var("balanced")
          |RadioCard(
          |  RadioCard.value <--> plan,
          |  RadioCard.options := Seq(
          |    RadioCard.Option("fast",     "Fast",     "Optimized for shortest wall-clock time."),
          |    RadioCard.Option("balanced", "Balanced", "Recommended default."),
          |    RadioCard.Option("thorough", "Thorough", "Higher quality, slower.")
          |  )
          |)""".stripMargin
      )({
        val v = Var("balanced")
        RadioCard(
          RadioCard.value <--> v,
          RadioCard.options := Seq(
            RadioCard.Option("fast",     "Fast",     "Optimized for shortest wall-clock time."),
            RadioCard.Option("balanced", "Balanced", "Recommended default."),
            RadioCard.Option("thorough", "Thorough", "Higher quality, slower.")
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("value",       "InOut[String]",         "Selected key."),
      ("options",     "Seq[RadioCard.Option]", "Option(key, title, description) entries."),
      ("orientation", "Vertical|Horizontal",   "Layout direction."),
      ("disabled",    "Boolean",               "Disables the whole group.")
    )
  )

  def segmentedControl(): HtmlElement = PageTemplate(
    title = "SegmentedControl",
    summary = "Row of mutually-exclusive buttons. Pick one of N."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "SegmentedControl",
        """val period = Var("week")
          |SegmentedControl(
          |  SegmentedControl.value <--> period,
          |  SegmentedControl.options := Seq(
          |    "day" -> "Day", "week" -> "Week", "month" -> "Month", "year" -> "Year"
          |  )
          |)""".stripMargin
      )({
        val v = Var("week")
        SegmentedControl(
          SegmentedControl.value <--> v,
          SegmentedControl.options := Seq("day" -> "Day", "week" -> "Week", "month" -> "Month", "year" -> "Year")
        )
      })
    ),
    PageTemplate.behavior(
      "Use this instead of RadioGroup when the options are short, primary-ish toggles."
    ),
    PageTemplate.propsTable(
      ("value",   "InOut[String]",        "Selected key."),
      ("options", "Seq[(String,String)]", "(key, label) pairs."),
      ("disabled","Boolean",              "Disables the whole control.")
    )
  )

  def dropdown(): HtmlElement = PageTemplate(
    title = "Dropdown",
    summary = "Themed wrapper around the native <select>."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Dropdown",
        """val fruit = Var("apple")
          |Dropdown(
          |  Dropdown.value <--> fruit,
          |  Dropdown.options := Seq(
          |    "apple"  -> "Apple",
          |    "banana" -> "Banana",
          |    "cherry" -> "Cherry"
          |  )
          |)""".stripMargin
      )({
        val v = Var("apple")
        Dropdown(
          Dropdown.value <--> v,
          Dropdown.options := Seq(
            "apple"      -> "Apple",
            "banana"     -> "Banana",
            "cherry"     -> "Cherry",
            "date"       -> "Date",
            "elderberry" -> "Elderberry",
            "fig"        -> "Fig"
          )
        )
      })
    ),
    PageTemplate.behavior(
      "Uses the browser's native dropdown — best for mobile and accessibility.",
      "For a richer searchable picker, build on Popover."
    ),
    PageTemplate.propsTable(
      ("value",   "InOut[String]",        "Selected key."),
      ("options", "Seq[(String,String)]", "(key, label) pairs."),
      ("disabled","Boolean",              "Disables the control."),
      ("width",   "Length",               "Width of the control.")
    )
  )

  def toggle(): HtmlElement = PageTemplate(
    title = "Toggle",
    summary = "On/off switch. A more affirmative alternative to a single Checkbox."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Toggle",
        """val sync = Var(true)
          |Toggle(Toggle.checked <--> sync)
          |Toggle(Toggle.disabled := true)""".stripMargin
      )({
        val a = Var(true)
        div(stack.col(spacing.md),
          div(stack.row(spacing.md), Toggle(Toggle.checked <--> a), span(typo.body, "Sync to cluster")),
          div(stack.row(spacing.md), Toggle(Toggle.checked := false), span(typo.muted, "Quiet mode")),
          div(stack.row(spacing.md), Toggle(Toggle.disabled := true), span(typo.muted, "Disabled"))
        )
      })
    ),
    PageTemplate.propsTable(
      ("checked",  "InOut[Boolean]", "Two-way binding."),
      ("disabled", "Boolean",        "Disables the switch.")
    )
  )

  def slider(): HtmlElement = PageTemplate(
    title = "Slider",
    summary = "Pointer-driven numeric slider. Avoids native <input type=range>'s styling limitations."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Slider",
        """val threshold = Var(40.0)
          |Slider(
          |  Slider.value <--> threshold,
          |  Slider.min := 0, Slider.max := 100, Slider.step := 1,
          |  Slider.width := Length.px(280)
          |)""".stripMargin
      )({
        val v = Var(40.0)
        div(stack.col(spacing.sm) ++ css.width(Length.px(280)),
          div(stack.between(),
            span(typo.label, "Threshold"),
            span(typo.muted, child.text <-- v.signal.map(d => f"$d%.0f%%"))
          ),
          Slider(Slider.value <--> v, Slider.min := 0, Slider.max := 100, Slider.step := 1, Slider.width := Length.px(280))
        )
      })
    ),
    PageTemplate.propsTable(
      ("value",    "InOut[Double]", "Two-way binding."),
      ("min",      "Double",        "Min value."),
      ("max",      "Double",        "Max value."),
      ("step",     "Double",        "Stepping increment."),
      ("disabled", "Boolean",       "Disables the slider."),
      ("width",    "Length",        "Track width.")
    )
  )

  def field(): HtmlElement = PageTemplate(
    title = "Field",
    summary = "Label + control + hint/error scaffold around any input."
  )(
    PageTemplate.section("With hint")(
      PageTemplate.codedDemo(
        "Field with hint",
        """Field(
          |  Field.label := "Email",
          |  Field.hint := "We never share this.",
          |  Field.required := true,
          |  Field.control(TextInput(TextInput.value <--> email))
          |)""".stripMargin
      )({
        val v = Var("")
        Field(
          Field.label := "Email",
          Field.hint := "We never share this.",
          Field.required := true,
          Field.control(TextInput(TextInput.value <--> v, TextInput.placeholder := "you@org.com"))
        )
      })
    ),
    PageTemplate.section("With error")(
      PageTemplate.codedDemo(
        "Field with error",
        """Field(
          |  Field.label := "Email",
          |  Field.error := "Must contain @.",
          |  Field.control(TextInput(TextInput.value <--> email, TextInput.invalid := true))
          |)""".stripMargin
      )({
        val v = Var("not-an-email")
        Field(
          Field.label := "Email",
          Field.error := "Must contain @.",
          Field.control(TextInput(TextInput.value <--> v, TextInput.invalid := true))
        )
      })
    ),
    PageTemplate.behavior(
      "Error message takes precedence over hint — useful for inline validation flows."
    ),
    PageTemplate.propsTable(
      ("label",    "String",  "Field label."),
      ("hint",     "String",  "Hint shown under the control."),
      ("error",    "String",  "Error message — replaces the hint when non-empty."),
      ("required", "Boolean", "Show a red asterisk after the label."),
      ("control",  "Slot",    "The input element itself.")
    )
  )

  def fieldset(): HtmlElement = PageTemplate(
    title = "Fieldset",
    summary = "Grouped fields with a legend."
  )(
    PageTemplate.section("Demo")(
      PageTemplate.codedDemo(
        "Fieldset",
        """Fieldset(
          |  Fieldset.legend := "Contact",
          |  Fieldset.hint := "All fields are optional.",
          |  Fieldset.body(
          |    Field(Field.label := "Email",   Field.control(TextInput(TextInput.value <--> email))),
          |    Field(Field.label := "Message", Field.control(Textarea(Textarea.value <--> msg, Textarea.rows := 3)))
          |  )
          |)""".stripMargin
      )({
        val a = Var("hi@org.com")
        val b = Var("Hello!")
        Fieldset(
          Fieldset.legend := "Contact",
          Fieldset.hint := "All fields are optional.",
          Fieldset.body(
            Field(Field.label := "Email",   Field.control(TextInput(TextInput.value <--> a))),
            Field(Field.label := "Message", Field.control(Textarea(Textarea.value <--> b, Textarea.rows := 3)))
          )
        )
      })
    ),
    PageTemplate.propsTable(
      ("legend", "String", "Group title."),
      ("hint",   "String", "Optional hint under the legend."),
      ("body",   "Slot",   "The grouped fields.")
    )
  )
}
