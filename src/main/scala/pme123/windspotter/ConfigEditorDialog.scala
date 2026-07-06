package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js

// Modal editor for webcam configurations:
// - select / activate / duplicate / delete configurations
// - structured editing of groups and webcams (incl. picking from the
//   built-in webcam library)
// - raw JSON view with import / export as .json files
object ConfigEditorDialog:

  private val openVar             = Var(false)
  private val selectedNameVar     = Var(ConfigService.defaultConfigName)
  private val draftVar            = Var(ConfigService.defaultConfig)
  private val selectedGroupIdxVar = Var(0)
  private val expandedCamVar      = Var(Option.empty[Int])
  private val jsonModeVar         = Var(false)
  private val jsonTextVar         = Var("")
  private val statusVar           = Var(Option.empty[(String, Boolean)])

  private val readOnlySignal: Signal[Boolean] =
    selectedNameVar.signal.map(ConfigService.isDefault).distinct

  private val selectedGroupSignal: Signal[Option[WebcamGroup]] =
    draftVar.signal
      .combineWith(selectedGroupIdxVar.signal)
      .map { case (cfg, idx) => cfg.groups.lift(idx) }

  // All webcams of the built-in default configuration, offered as a library
  private lazy val library: List[(String, Webcam)] =
    ConfigService.defaultConfig.groups.flatMap { group =>
      group.webcams.map(cam => s"${group.name} / ${cam.name}" -> cam)
    }

  def open(): Unit =
    selectConfig(ConfigService.activeConfigVar.now().name)
    openVar.set(true)

  def apply(): HtmlElement =
    div(
      child <-- openVar.signal.map {
        case false => emptyNode
        case true  => dialog()
      }
    )

  // ── state helpers ──────────────────────────────────────────────────────

  private def selectConfig(name: String): Unit =
    val cfg = ConfigService.findConfig(name).getOrElse(ConfigService.defaultConfig)
    selectedNameVar.set(cfg.name)
    draftVar.set(cfg)
    selectedGroupIdxVar.set(0)
    expandedCamVar.set(None)
    jsonModeVar.set(false)
    statusVar.set(None)

  private def setMessage(text: String, isError: Boolean): Unit =
    statusVar.set(Some(text -> isError))

  private def isDirty: Boolean =
    ConfigService.findConfig(selectedNameVar.now()) match
      case Some(saved) => ConfigJson.toJson(saved) != ConfigJson.toJson(draftVar.now())
      case None        => true

  private def confirmDiscard(): Boolean =
    !isDirty || dom.window.confirm("Discard unsaved changes?")

  private def trySave(): Boolean =
    val oldName = selectedNameVar.now()
    if ConfigService.isDefault(oldName) then true // built-in, nothing to save
    else
      val name = draftVar.now().name.trim
      if name.isEmpty then
        setMessage("The configuration name must not be empty.", true)
        false
      else if ConfigService.isDefault(name) then
        setMessage(s"'$name' is reserved for the built-in configuration.", true)
        false
      else if name != oldName && ConfigService.findConfig(name).isDefined then
        setMessage(s"A configuration named '$name' already exists.", true)
        false
      else
        val draft = draftVar.now().copy(name = name)
        draftVar.set(draft)
        ConfigService.saveConfigAs(oldName, draft)
        selectedNameVar.set(name)
        setMessage(s"Saved '$name'.", false)
        true

  private def activateSelected(): Unit =
    if trySave() then
      val cfg =
        ConfigService.findConfig(draftVar.now().name).getOrElse(ConfigService.defaultConfig)
      ConfigService.activate(cfg)
      setMessage(s"Activated '${cfg.name}'.", false)

  private def createConfig(base: WebcamConfig, baseName: String): Unit =
    val cfg = base.copy(name = ConfigService.uniqueName(baseName))
    ConfigService.saveConfig(cfg)
    selectConfig(cfg.name)

  private def deleteSelected(): Unit =
    val name = selectedNameVar.now()
    if !ConfigService.isDefault(name) &&
      dom.window.confirm(s"Delete configuration '$name'?")
    then
      ConfigService.deleteConfig(name)
      selectConfig(ConfigService.activeConfigVar.now().name)

  // ── draft update helpers ──────────────────────────────────────────────

  private def updateGroups(f: List[WebcamGroup] => List[WebcamGroup]): Unit =
    draftVar.update(cfg => cfg.copy(groups = f(cfg.groups)))

  private def updateGroup(idx: Int)(f: WebcamGroup => WebcamGroup): Unit =
    updateGroups(_.zipWithIndex.map((g, i) => if i == idx then f(g) else g))

  private def updateWebcam(gIdx: Int, wIdx: Int)(f: Webcam => Webcam): Unit =
    updateGroup(gIdx) { group =>
      group.copy(webcams = group.webcams.zipWithIndex.map((w, i) => if i == wIdx then f(w) else w))
    }

  private def moveItem[A](list: List[A], idx: Int, delta: Int): List[A] =
    val newIdx = idx + delta
    if idx < 0 || idx >= list.length || newIdx < 0 || newIdx >= list.length then list
    else
      val item = list(idx)
      list.patch(idx, Nil, 1).patch(newIdx, List(item), 0)

  // ── import / export ───────────────────────────────────────────────────

  private def exportSelected(): Unit =
    val cfg  = draftVar.now()
    val json = ConfigJson.toJson(cfg)
    val bag  = js.Dynamic.literal("type" -> "application/json").asInstanceOf[dom.BlobPropertyBag]
    val blob = new dom.Blob(js.Array(json: dom.BlobPart), bag)
    val url  = dom.URL.createObjectURL(blob)
    val anchor = dom.document.createElement("a").asInstanceOf[dom.html.Anchor]
    anchor.href = url
    anchor.setAttribute("download", s"${cfg.name}.json")
    dom.document.body.appendChild(anchor)
    anchor.click()
    dom.document.body.removeChild(anchor)
    dom.URL.revokeObjectURL(url)

  private def importFile(file: dom.File): Unit =
    val reader = new dom.FileReader()
    reader.onload = _ =>
      ConfigJson.fromJson(reader.result.asInstanceOf[String]) match
        case Right(cfg) =>
          val baseName =
            if cfg.name.trim.isEmpty || ConfigService.isDefault(cfg.name) then "Imported"
            else cfg.name.trim
          val named = cfg.copy(name = ConfigService.uniqueName(baseName))
          ConfigService.saveConfig(named)
          selectConfig(named.name)
          setMessage(s"Imported '${named.name}'.", false)
        case Left(error) =>
          setMessage(s"Import failed: $error", true)
    reader.readAsText(file)

  // ── view ──────────────────────────────────────────────────────────────

  private def close(): Unit =
    if confirmDiscard() then openVar.set(false)

  private def dialog(): HtmlElement =
    div(
      className := "info-modal-overlay",
      onClick --> { _ => close() },
      div(
        className := "info-modal config-modal",
        onClick --> { ev => ev.stopPropagation() },
        div(
          className := "info-modal-header",
          span(className := "info-modal-title", "Webcam Configurations"),
          button(
            className := "info-modal-close",
            "✕",
            onClick --> { _ => close() }
          )
        ),
        div(
          className := "info-modal-body",
          toolbar(),
          folderRow(),
          nameRow(),
          tabsRow(),
          child <-- jsonModeVar.signal.distinct.map {
            case true  => jsonPanel()
            case false => editorPanel()
          },
          footerRow()
        )
      )
    )

  private def toolbar(): HtmlElement =
    val importInput = input(
      typ       := "file",
      accept    := ".json,application/json",
      display   := "none",
      onChange --> { ev =>
        val inputEl = ev.target.asInstanceOf[dom.html.Input]
        if inputEl.files.length > 0 then importFile(inputEl.files(0))
        inputEl.value = ""
      }
    )
    div(
      className := "cfg-toolbar",
      span(className := "cfg-toolbar-label", "Configuration:"),
      select(
        className := "cfg-select",
        onChange --> { ev =>
          val sel = ev.target.asInstanceOf[dom.html.Select]
          if confirmDiscard() then selectConfig(sel.value)
          else sel.value = selectedNameVar.now()
        },
        children <-- ConfigService.allConfigsSignal.map(_.map { cfg =>
          option(
            value := cfg.name,
            cfg.name,
            selected <-- selectedNameVar.signal.map(_ == cfg.name)
          )
        })
      ),
      child <-- ConfigService.activeConfigVar.signal
        .combineWith(selectedNameVar.signal)
        .map { case (active, selected) => active.name == selected }
        .distinct
        .map {
          case true  => span(className := "cfg-active-badge", "● active")
          case false => emptyNode
        },
      div(
        className := "cfg-toolbar-actions",
        button(
          className := "cfg-btn",
          "New",
          title := "Create a new empty configuration",
          onClick --> { _ =>
            if confirmDiscard() then
              createConfig(WebcamConfig("", List.empty), "My Configuration")
          }
        ),
        button(
          className := "cfg-btn",
          "Duplicate",
          title := "Create an editable copy of the selected configuration",
          onClick --> { _ => createConfig(draftVar.now(), s"${draftVar.now().name} Copy") }
        ),
        button(
          className := "cfg-btn danger",
          "Delete",
          disabled <-- readOnlySignal,
          onClick --> { _ => deleteSelected() }
        ),
        button(
          className := "cfg-btn",
          "Export",
          title := "Download the selected configuration as a JSON file",
          onClick --> { _ => exportSelected() }
        ),
        button(
          className := "cfg-btn",
          "Import",
          title := "Import a configuration from a JSON file",
          onClick --> { _ => importInput.ref.click() }
        ),
        importInput
      )
    )

  private def folderRow(): HtmlElement =
    import ConfigFolderService.FolderStatus
    div(
      className := "cfg-folder",
      child <-- ConfigFolderService.statusVar.signal.map {
        case FolderStatus.NotSupported =>
          span(
            className := "cfg-folder-status muted",
            "Configurations are stored in this browser. ",
            "Saving to a folder is not supported by this browser — use Export/Import to keep JSON files."
          )
        case FolderStatus.NotLinked =>
          span(
            className := "cfg-folder-status",
            span(className := "muted", "Configurations are stored in this browser. "),
            button(
              className := "cfg-btn",
              "Link folder…",
              title := "Also save your configurations as JSON files in a folder of your choice",
              onClick --> { _ => ConfigFolderService.linkFolder() }
            )
          )
        case FolderStatus.NeedsPermission(folderName) =>
          span(
            className := "cfg-folder-status",
            span(className := "muted", s"Folder '$folderName' is linked but needs permission. "),
            button(
              className := "cfg-btn",
              "Reconnect",
              onClick --> { _ => ConfigFolderService.reconnect() }
            ),
            button(
              className := "cfg-btn",
              "Unlink",
              onClick --> { _ => ConfigFolderService.unlink() }
            )
          )
        case FolderStatus.Linked(folderName) =>
          span(
            className := "cfg-folder-status",
            span(className := "cfg-folder-linked", s"✓ Saving to folder '$folderName'"),
            child <-- ConfigFolderService.messageVar.signal.map {
              case Some(msg) => span(className := "muted", s" — $msg")
              case None      => emptyNode
            },
            button(
              className := "cfg-btn",
              "Unlink",
              onClick --> { _ => ConfigFolderService.unlink() }
            )
          )
      }
    )

  private def nameRow(): HtmlElement =
    div(
      child <-- readOnlySignal.map {
        case true =>
          div(
            className := "cfg-hint",
            "The default configuration is built into the app and read-only. ",
            "Use 'Duplicate' to create your own editable copy — for example one for holidays or winter."
          )
        case false =>
          div(
            className := "cfg-field cfg-name-field",
            label(className := "cfg-field-label", "Name"),
            input(
              className := "cfg-input",
              typ       := "text",
              value <-- draftVar.signal.map(_.name).distinct,
              onChange.mapToValue --> { v => draftVar.update(_.copy(name = v)) }
            )
          )
      }
    )

  private def tabsRow(): HtmlElement =
    div(
      className := "cfg-tabs",
      button(
        className := "cfg-tab",
        cls("active") <-- jsonModeVar.signal.map(!_),
        "Editor",
        onClick --> { _ => jsonModeVar.set(false) }
      ),
      button(
        className := "cfg-tab",
        cls("active") <-- jsonModeVar.signal,
        "JSON",
        onClick --> { _ =>
          jsonTextVar.set(ConfigJson.toJson(draftVar.now()))
          jsonModeVar.set(true)
        }
      )
    )

  // ── structured editor ─────────────────────────────────────────────────

  private def editorPanel(): HtmlElement =
    div(
      className := "cfg-editor",
      groupsPanel(),
      webcamsPanel()
    )

  private def groupsPanel(): HtmlElement =
    div(
      className := "cfg-groups",
      div(className := "cfg-panel-title", "Groups"),
      div(
        className := "cfg-group-list",
        children <-- draftVar.signal
          .map(_.groups.zipWithIndex)
          .split(_._2) { (idx, _, sig) => groupRow(idx, sig.map(_._1)) }
      ),
      button(
        className := "cfg-btn",
        disabled <-- readOnlySignal,
        "+ Add group",
        onClick --> { _ =>
          updateGroups(_ :+ WebcamGroup("New Group", List.empty))
          selectedGroupIdxVar.set(draftVar.now().groups.length - 1)
          expandedCamVar.set(None)
        }
      )
    )

  private def groupRow(idx: Int, groupSig: Signal[WebcamGroup]): HtmlElement =
    div(
      className := "cfg-group-row",
      cls("selected") <-- selectedGroupIdxVar.signal.map(_ == idx),
      cls("cfg-hidden") <-- groupSig.map(_.hidden),
      onClick --> { _ =>
        if selectedGroupIdxVar.now() != idx then
          selectedGroupIdxVar.set(idx)
          expandedCamVar.set(None)
      },
      input(
        className := "cfg-input cfg-group-name",
        typ       := "text",
        disabled <-- readOnlySignal,
        value <-- groupSig.map(_.name),
        onChange.mapToValue --> { v => updateGroup(idx)(_.copy(name = v)) }
      ),
      span(className := "cfg-count", child.text <-- groupSig.map(_.webcams.length.toString)),
      button(
        className := "cfg-icon-btn",
        disabled <-- readOnlySignal,
        title <-- groupSig.map(g => if g.hidden then "Show group" else "Hide group"),
        child.text <-- groupSig.map(g => if g.hidden then "⊘" else "👁"),
        onClick.stopPropagation --> { _ => updateGroup(idx)(g => g.copy(hidden = !g.hidden)) }
      ),
      button(
        className := "cfg-icon-btn",
        disabled <-- readOnlySignal,
        title := "Move up",
        "↑",
        onClick.stopPropagation --> { _ => updateGroups(moveItem(_, idx, -1)) }
      ),
      button(
        className := "cfg-icon-btn",
        disabled <-- readOnlySignal,
        title := "Move down",
        "↓",
        onClick.stopPropagation --> { _ => updateGroups(moveItem(_, idx, 1)) }
      ),
      button(
        className := "cfg-icon-btn",
        disabled <-- readOnlySignal,
        title := "Delete group",
        "✕",
        onClick.stopPropagation --> { _ =>
          updateGroups(_.patch(idx, Nil, 1))
          selectedGroupIdxVar.update(i => math.max(0, math.min(i, draftVar.now().groups.length - 1)))
          expandedCamVar.set(None)
        }
      )
    )

  private def webcamsPanel(): HtmlElement =
    div(
      className := "cfg-webcams",
      div(
        className := "cfg-panel-title",
        child.text <-- selectedGroupSignal.map {
          case Some(group) => s"Webcams in '${group.name}'"
          case None        => "Webcams"
        }
      ),
      div(
        className := "cfg-webcam-list",
        children <-- draftVar.signal
          .combineWith(selectedGroupIdxVar.signal)
          .map { case (cfg, gIdx) =>
            cfg.groups.lift(gIdx).map(_.webcams.zipWithIndex).getOrElse(Nil)
          }
          .split(_._2) { (idx, _, sig) => webcamRow(idx, sig.map(_._1)) }
      ),
      addWebcamRow()
    )

  private def webcamRow(idx: Int, camSig: Signal[Webcam]): HtmlElement =
    def upd(f: Webcam => Webcam): Unit =
      updateWebcam(selectedGroupIdxVar.now(), idx)(f)

    div(
      className := "cfg-webcam-row",
      cls("cfg-hidden") <-- camSig.map(_.hidden),
      div(
        className := "cfg-webcam-head",
        onClick --> { _ =>
          expandedCamVar.update(cur => if cur.contains(idx) then None else Some(idx))
        },
        span(className := "cfg-webcam-name", child.text <-- camSig.map(_.name)),
        span(className := "cfg-webcam-type", child.text <-- camSig.map(_.webcamType.toString)),
        button(
          className := "cfg-icon-btn",
          disabled <-- readOnlySignal,
          title <-- camSig.map(cam => if cam.hidden then "Show webcam" else "Hide webcam"),
          child.text <-- camSig.map(cam => if cam.hidden then "⊘" else "👁"),
          onClick.stopPropagation --> { _ => upd(cam => cam.copy(hidden = !cam.hidden)) }
        ),
        button(
          className := "cfg-icon-btn",
          disabled <-- readOnlySignal,
          title := "Move up",
          "↑",
          onClick.stopPropagation --> { _ =>
            updateGroup(selectedGroupIdxVar.now())(g => g.copy(webcams = moveItem(g.webcams, idx, -1)))
          }
        ),
        button(
          className := "cfg-icon-btn",
          disabled <-- readOnlySignal,
          title := "Move down",
          "↓",
          onClick.stopPropagation --> { _ =>
            updateGroup(selectedGroupIdxVar.now())(g => g.copy(webcams = moveItem(g.webcams, idx, 1)))
          }
        ),
        button(
          className := "cfg-icon-btn",
          title := "Edit",
          child.text <-- expandedCamVar.signal.map(cur => if cur.contains(idx) then "▲" else "✎"),
          onClick.stopPropagation --> { _ =>
            expandedCamVar.update(cur => if cur.contains(idx) then None else Some(idx))
          }
        ),
        button(
          className := "cfg-icon-btn",
          disabled <-- readOnlySignal,
          title := "Remove webcam",
          "✕",
          onClick.stopPropagation --> { _ =>
            updateGroup(selectedGroupIdxVar.now())(g => g.copy(webcams = g.webcams.patch(idx, Nil, 1)))
            expandedCamVar.set(None)
          }
        )
      ),
      child <-- expandedCamVar.signal.map(_.contains(idx)).distinct.map {
        case false => emptyNode
        case true  => webcamForm(camSig, upd)
      }
    )

  private def webcamForm(camSig: Signal[Webcam], upd: (Webcam => Webcam) => Unit): HtmlElement =
    def scraping(cam: Webcam): ScrapingConfig =
      cam.scrapingConfig.getOrElse(ScrapingConfig("", ""))
    def updScraping(f: ScrapingConfig => ScrapingConfig): Unit =
      upd(cam => cam.copy(scrapingConfig = Some(f(scraping(cam)))))

    div(
      className := "cfg-webcam-form",
      textField("Name", camSig.map(_.name), v => upd(_.copy(name = v))),
      div(
        className := "cfg-field",
        label(className := "cfg-field-label", "Type"),
        select(
          className := "cfg-input",
          disabled <-- readOnlySignal,
          onChange.mapToValue --> { v => upd(_.copy(webcamType = WebcamType.valueOf(v))) },
          WebcamType.values.toList.map { tpe =>
            option(
              value := tpe.toString,
              tpe.toString,
              selected <-- camSig.map(_.webcamType == tpe)
            )
          }
        )
      ),
      textField("URL / video ID", camSig.map(_.url), v => upd(_.copy(url = v))),
      div(
        className := "cfg-field",
        label(className := "cfg-field-label", "Reload (minutes, 0 = off)"),
        input(
          className := "cfg-input",
          typ       := "number",
          minAttr   := "0",
          stepAttr  := "1",
          disabled <-- readOnlySignal,
          value <-- camSig.map(_.reloadInMin.toString),
          onChange.mapToValue --> { v => upd(_.copy(reloadInMin = v.toIntOption.getOrElse(0))) }
        )
      ),
      textField("Footer link", camSig.map(_.footer), v => upd(_.copy(footer = v))),
      textField(
        "Main page link (optional)",
        camSig.map(_.mainPageLink.getOrElse("")),
        v => upd(_.copy(mainPageLink = Option(v.trim).filter(_.nonEmpty)))
      ),
      child <-- camSig.map(_.webcamType == WebcamType.ScrapedWebcam).distinct.map {
        case false => emptyNode
        case true  =>
          div(
            className := "cfg-webcam-form cfg-scraping-form",
            textField(
              "Scrape page URL",
              camSig.map(cam => scraping(cam).pageUrl),
              v => updScraping(_.copy(pageUrl = v))
            ),
            textField(
              "Image regex",
              camSig.map(cam => scraping(cam).imageRegex),
              v => updScraping(_.copy(imageRegex = v))
            ),
            textField(
              "Base URL (optional)",
              camSig.map(cam => scraping(cam).baseUrl.getOrElse("")),
              v => updScraping(_.copy(baseUrl = Option(v.trim).filter(_.nonEmpty)))
            )
          )
      }
    )

  private def textField(
      labelText: String,
      valueSig: Signal[String],
      commit: String => Unit
  ): HtmlElement =
    div(
      className := "cfg-field",
      label(className := "cfg-field-label", labelText),
      input(
        className := "cfg-input",
        typ       := "text",
        disabled <-- readOnlySignal,
        value <-- valueSig,
        onChange.mapToValue --> commit
      )
    )

  private def addWebcamRow(): HtmlElement =
    val noGroupOrReadOnly =
      readOnlySignal
        .combineWith(selectedGroupSignal)
        .map { case (readOnly, group) => readOnly || group.isEmpty }
    div(
      className := "cfg-add-webcam",
      select(
        className := "cfg-select",
        disabled <-- noGroupOrReadOnly,
        option(value := "", "Add from library…"),
        library.map { case (key, _) => option(value := key, key) },
        onChange --> { ev =>
          val sel = ev.target.asInstanceOf[dom.html.Select]
          val key = sel.value
          sel.value = ""
          library.find(_._1 == key).foreach { case (_, cam) =>
            updateGroup(selectedGroupIdxVar.now())(g => g.copy(webcams = g.webcams :+ cam))
          }
        }
      ),
      button(
        className := "cfg-btn",
        disabled <-- noGroupOrReadOnly,
        "+ New webcam",
        onClick --> { _ =>
          val gIdx = selectedGroupIdxVar.now()
          updateGroup(gIdx)(g =>
            g.copy(webcams = g.webcams :+ Webcam("New Webcam", "", 5, ""))
          )
          expandedCamVar.set(
            draftVar.now().groups.lift(gIdx).map(_.webcams.length - 1)
          )
        }
      )
    )

  // ── JSON panel ────────────────────────────────────────────────────────

  private def jsonPanel(): HtmlElement =
    div(
      className := "cfg-json",
      textArea(
        className := "cfg-json-area",
        readOnly <-- readOnlySignal,
        value <-- jsonTextVar,
        onInput.mapToValue --> jsonTextVar
      ),
      button(
        className := "cfg-btn",
        disabled <-- readOnlySignal,
        "Apply JSON",
        onClick --> { _ =>
          ConfigJson.fromJson(jsonTextVar.now()) match
            case Right(cfg) =>
              draftVar.set(cfg)
              selectedGroupIdxVar.set(0)
              expandedCamVar.set(None)
              jsonModeVar.set(false)
              setMessage("JSON applied — press Save to persist.", false)
            case Left(error) =>
              setMessage(s"Invalid JSON: $error", true)
        }
      )
    )

  private def footerRow(): HtmlElement =
    div(
      className := "cfg-footer",
      div(
        className := "cfg-message",
        child <-- statusVar.signal.map {
          case None => emptyNode
          case Some((text, isError)) =>
            span(className := (if isError then "cfg-msg error" else "cfg-msg ok"), text)
        }
      ),
      div(
        className := "cfg-footer-actions",
        button(
          className := "cfg-btn",
          disabled <-- readOnlySignal,
          "Save",
          onClick --> { _ => trySave() }
        ),
        button(
          className := "cfg-btn primary",
          "Activate",
          title := "Save and switch the app to this configuration",
          onClick --> { _ => activateSelected() }
        )
      )
    )

end ConfigEditorDialog
