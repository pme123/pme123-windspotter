package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

// Manages webcam configurations:
// - The default configuration is defined in source code (WebcamData.scala).
// - Custom configurations are stored as JSON in localStorage and can be
//   imported from / exported to JSON files.
// - The active configuration selection is persisted across sessions.
object ConfigService:

  private val configsKey = "windspotter.configs"
  private val activeKey  = "windspotter.activeConfig"

  val defaultConfigName = "Default"

  lazy val defaultConfig: WebcamConfig =
    WebcamConfig(defaultConfigName, groups.webcamGroups)

  val customConfigsVar: Var[List[WebcamConfig]] = Var(loadCustomConfigs())

  val activeConfigVar: Var[WebcamConfig] = Var {
    val activeName = Option(dom.window.localStorage.getItem(activeKey))
    activeName
      .flatMap(name => allConfigs.find(_.name == name))
      .getOrElse(defaultConfig)
  }

  val allConfigsSignal: Signal[List[WebcamConfig]] =
    customConfigsVar.signal.map(defaultConfig :: _)

  def allConfigs: List[WebcamConfig] =
    defaultConfig :: customConfigsVar.now()

  def findConfig(name: String): Option[WebcamConfig] =
    allConfigs.find(_.name == name)

  def isDefault(name: String): Boolean =
    name == defaultConfigName

  // Returns a name that does not collide with any existing configuration.
  def uniqueName(base: String): String =
    val names = allConfigs.map(_.name).toSet
    if !names.contains(base) then base
    else
      LazyList
        .from(2)
        .map(i => s"$base $i")
        .find(n => !names.contains(n))
        .get

  // Adds a new custom configuration or replaces the one with the same name.
  def saveConfig(config: WebcamConfig): Unit =
    if !isDefault(config.name) then
      val current = customConfigsVar.now()
      val updated =
        if current.exists(_.name == config.name) then
          current.map(c => if c.name == config.name then config else c)
        else current :+ config
      customConfigsVar.set(updated)
      persist()
      ConfigFolderService.writeConfig(config)
      // Keep the active configuration in sync when it was edited
      if activeConfigVar.now().name == config.name then activate(config)

  // Replaces the configuration stored under oldName (handles renaming).
  def saveConfigAs(oldName: String, config: WebcamConfig): Unit =
    if oldName != config.name && !isDefault(oldName) then
      val wasActive = activeConfigVar.now().name == oldName
      customConfigsVar.update(_.map(c => if c.name == oldName then config else c))
      persist()
      ConfigFolderService.deleteConfigFile(oldName)
      if wasActive then activate(config)
    saveConfig(config)

  def deleteConfig(name: String): Unit =
    if !isDefault(name) then
      customConfigsVar.update(_.filterNot(_.name == name))
      persist()
      ConfigFolderService.deleteConfigFile(name)
      if activeConfigVar.now().name == name then activate(defaultConfig)

  // Merges configurations read from the linked folder: folder versions win
  // over local ones with the same name, local-only configs are kept.
  def mergeFromFolder(folderConfigs: List[WebcamConfig]): Unit =
    val valid = folderConfigs.filterNot(c => c.name.trim.isEmpty || isDefault(c.name))
    if valid.nonEmpty then
      val current = customConfigsVar.now()
      val merged  =
        current.map(c => valid.find(_.name == c.name).getOrElse(c)) ++
          valid.filterNot(c => current.exists(_.name == c.name))
      customConfigsVar.set(merged)
      persist()
      // Refresh the active configuration if the folder changed it
      val active = activeConfigVar.now()
      if !isDefault(active.name) then
        merged.find(_.name == active.name).foreach { updated =>
          if updated != active then activate(updated)
        }

  def activate(config: WebcamConfig): Unit =
    // Stop all running refresh timers before the UI is rebuilt
    WebcamService.stopAllAutoRefresh()
    ScrapedWebcamService.stopAllScraping()
    dom.window.localStorage.setItem(activeKey, config.name)
    activeConfigVar.set(config)

  private def persist(): Unit =
    dom.window.localStorage.setItem(
      configsKey,
      ConfigJson.toJson(customConfigsVar.now())
    )

  private def loadCustomConfigs(): List[WebcamConfig] =
    Option(dom.window.localStorage.getItem(configsKey))
      .map { json =>
        ConfigJson.configListFromJson(json) match
          case Right(configs) => configs.filterNot(c => isDefault(c.name))
          case Left(error)    =>
            dom.console.error(s"Failed to load stored configurations: $error")
            List.empty
      }
      .getOrElse(List.empty)

end ConfigService
