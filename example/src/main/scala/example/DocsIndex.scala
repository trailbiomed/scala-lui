package example

import com.raquo.laminar.api.L.{Mod as _, *}
import example.pages.*

final case class DocEntry(slug: String, label: String, render: () => HtmlElement)
final case class DocCategory(label: String, entries: Seq[DocEntry])

object DocsIndex {

  val categories: Seq[DocCategory] = Seq(
    DocCategory("Getting started", Seq(
      DocEntry("overview",        "Overview",         () => OverviewPage()),
      DocEntry("getting-started", "Getting started",  () => GettingStartedPage()),
      DocEntry("component-model", "Component model",  () => ComponentModelPage())
    )),
    DocCategory("Foundations", Seq(
      DocEntry("theme",       "Theme tokens",     () => FoundationsPages.theme()),
      DocEntry("palette",     "Color palette",    () => FoundationsPages.palettePage()),
      DocEntry("spacing",     "Spacing scale",    () => FoundationsPages.spacingPage()),
      DocEntry("radius",      "Border radius",    () => FoundationsPages.radiusPage()),
      DocEntry("font-sizes",  "Type scale",       () => FoundationsPages.fontSizesPage()),
      DocEntry("breakpoints", "Breakpoints",      () => FoundationsPages.breakpointsPage()),
      DocEntry("length",      "Length factories", () => FoundationsPages.length()),
      DocEntry("color",       "Color type",       () => FoundationsPages.color()),
      DocEntry("css",         "css builders",     () => FoundationsPages.cssBuilders()),
      DocEntry("style",       "Style & themed",   () => FoundationsPages.style()),
      DocEntry("stack",       "stack presets",    () => FoundationsPages.stackPage()),
      DocEntry("typo",        "typo presets",     () => FoundationsPages.typoPage()),
      DocEntry("surface-preset", "surface presets", () => FoundationsPages.surfacePage()),
      DocEntry("interactive", "Interactive",      () => FoundationsPages.interactive())
    )),
    DocCategory("Layout", Seq(
      DocEntry("container",       "Container",       () => LayoutPages.container()),
      DocEntry("center",          "Center",          () => LayoutPages.center()),
      DocEntry("bleed",           "Bleed",           () => LayoutPages.bleed()),
      DocEntry("aspect-ratio",    "Aspect Ratio",    () => LayoutPages.aspectRatio()),
      DocEntry("wrap",            "Wrap",            () => LayoutPages.wrap()),
      DocEntry("group",           "Group",           () => LayoutPages.group()),
      DocEntry("simple-grid",     "SimpleGrid",      () => LayoutPages.simpleGrid()),
      DocEntry("scroll-area",     "ScrollArea",      () => LayoutPages.scrollArea()),
      DocEntry("divider",         "Divider",         () => LayoutPages.divider()),
      DocEntry("surface",         "Surface",         () => LayoutPages.surfacePage()),
      DocEntry("action-bar",      "ActionBar",       () => LayoutPages.actionBar()),
      DocEntry("visually-hidden", "VisuallyHidden",  () => LayoutPages.visuallyHidden()),
      DocEntry("skip-nav",        "SkipNav",         () => LayoutPages.skipNav())
    )),
    DocCategory("Typography", Seq(
      DocEntry("heading",     "Heading",    () => TypographyPages.heading()),
      DocEntry("text",        "Text",       () => TypographyPages.text()),
      DocEntry("link",        "Link",       () => TypographyPages.link()),
      DocEntry("listing",     "Listing",    () => TypographyPages.listing()),
      DocEntry("blockquote",  "Blockquote", () => TypographyPages.blockquote()),
      DocEntry("mark",        "Mark",       () => TypographyPages.mark()),
      DocEntry("em",          "Em",         () => TypographyPages.em()),
      DocEntry("highlight",   "Highlight",  () => TypographyPages.highlight()),
      DocEntry("code",        "Code",       () => TypographyPages.code()),
      DocEntry("kbd",         "Kbd",        () => TypographyPages.kbd())
    )),
    DocCategory("Buttons", Seq(
      DocEntry("button",            "Button",          () => ButtonPages.button()),
      DocEntry("icon-button",       "IconButton",      () => ButtonPages.iconButton()),
      DocEntry("close-button",      "CloseButton",     () => ButtonPages.closeButton()),
      DocEntry("download-trigger",  "DownloadTrigger", () => ButtonPages.downloadTrigger())
    )),
    DocCategory("Forms", Seq(
      DocEntry("text-input",         "TextInput",         () => FormPages.textInput()),
      DocEntry("textarea",           "Textarea",          () => FormPages.textarea()),
      DocEntry("number-input",       "NumberInput",       () => FormPages.numberInput()),
      DocEntry("password-input",     "PasswordInput",     () => FormPages.passwordInput()),
      DocEntry("pin-input",          "PinInput",          () => FormPages.pinInput()),
      DocEntry("tags-input",         "TagsInput",         () => FormPages.tagsInput()),
      DocEntry("editable",           "Editable",          () => FormPages.editable()),
      DocEntry("file-upload",        "FileUpload",        () => FormPages.fileUpload()),
      DocEntry("checkbox",           "Checkbox",          () => FormPages.checkbox()),
      DocEntry("checkbox-card",      "CheckboxCard",      () => FormPages.checkboxCard()),
      DocEntry("radio-group",        "RadioGroup",        () => FormPages.radioGroup()),
      DocEntry("radio-card",         "RadioCard",         () => FormPages.radioCard()),
      DocEntry("segmented-control",  "SegmentedControl",  () => FormPages.segmentedControl()),
      DocEntry("dropdown",           "Dropdown",          () => FormPages.dropdown()),
      DocEntry("toggle",             "Toggle",            () => FormPages.toggle()),
      DocEntry("slider",             "Slider",            () => FormPages.slider()),
      DocEntry("field",              "Field",             () => FormPages.field()),
      DocEntry("fieldset",           "Fieldset",          () => FormPages.fieldset())
    )),
    DocCategory("Data display", Seq(
      DocEntry("avatar",         "Avatar",        () => DataDisplayPages.avatar()),
      DocEntry("badge",          "Badge",         () => DataDisplayPages.badge()),
      DocEntry("card",           "Card",          () => DataDisplayPages.card()),
      DocEntry("tag",            "Tag",           () => DataDisplayPages.tag()),
      DocEntry("status-badge",   "StatusBadge",   () => DataDisplayPages.statusBadge()),
      DocEntry("stat",           "Stat",          () => DataDisplayPages.stat()),
      DocEntry("timeline",       "Timeline",      () => DataDisplayPages.timeline()),
      DocEntry("data-list",      "DataList",      () => DataDisplayPages.dataList()),
      DocEntry("table",          "Table",         () => DataDisplayPages.table()),
      DocEntry("clipboard",      "Clipboard",     () => DataDisplayPages.clipboard())
    )),
    DocCategory("Icons", Seq(
      DocEntry("icon",          "Icon",         () => IconPages.icon()),
      DocEntry("checkmark",     "Checkmark",    () => IconPages.checkmark()),
      DocEntry("radiomark",     "Radiomark",    () => IconPages.radiomark()),
      DocEntry("color-swatch",  "ColorSwatch",  () => IconPages.colorSwatch())
    )),
    DocCategory("Disclosure", Seq(
      DocEntry("accordion",   "Accordion",   () => DisclosurePages.accordion()),
      DocEntry("collapsible", "Collapsible", () => DisclosurePages.collapsible()),
      DocEntry("tabs",        "Tabs",        () => DisclosurePages.tabs()),
      DocEntry("breadcrumb",  "Breadcrumb",  () => DisclosurePages.breadcrumb()),
      DocEntry("pagination",  "Pagination",  () => DisclosurePages.pagination()),
      DocEntry("steps",       "Steps",       () => DisclosurePages.steps())
    )),
    DocCategory("Feedback", Seq(
      DocEntry("alert",            "Alert",           () => FeedbackPages.alert()),
      DocEntry("empty-state",      "EmptyState",      () => FeedbackPages.emptyState()),
      DocEntry("spinner",          "Spinner",         () => FeedbackPages.spinner()),
      DocEntry("progress-bar",     "ProgressBar",     () => FeedbackPages.progressBar()),
      DocEntry("progress-circle",  "ProgressCircle",  () => FeedbackPages.progressCircle()),
      DocEntry("skeleton",         "Skeleton",        () => FeedbackPages.skeleton()),
      DocEntry("toast",            "Toast",           () => FeedbackPages.toast())
    )),
    DocCategory("Overlays", Seq(
      DocEntry("modal",       "Modal",       () => OverlayPages.modal()),
      DocEntry("drawer",      "Drawer",      () => OverlayPages.drawer()),
      DocEntry("tooltip",     "Tooltip",     () => OverlayPages.tooltip()),
      DocEntry("popover",     "Popover",     () => OverlayPages.popover()),
      DocEntry("menu",        "Menu",        () => OverlayPages.menu()),
      DocEntry("hover-card",  "HoverCard",   () => OverlayPages.hoverCard()),
      DocEntry("toggle-tip",  "ToggleTip",   () => OverlayPages.toggleTip())
    )),
    DocCategory("Application primitives", Seq(
      DocEntry("page-header",       "PageHeader",       () => AppPages.pageHeader()),
      DocEntry("section-label",     "SectionLabel",     () => AppPages.sectionLabel()),
      DocEntry("metric-cell",       "MetricCell",       () => AppPages.metricCell()),
      DocEntry("reference-card",    "ReferenceCard",    () => AppPages.referenceCard())
    ))
  )

  val all: Map[String, DocEntry] =
    categories.flatMap(_.entries).map(e => e.slug -> e).toMap

  val firstSlug: String = categories.head.entries.head.slug
}
