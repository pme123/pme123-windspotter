package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

// Mirrors custom configurations to a user-selected folder as .json files,
// using the File System Access API (Chromium browsers). The folder handle
// is persisted in IndexedDB so the link survives page reloads; the browser
// may still ask again for permission after a restart.
object ConfigFolderService:

  enum FolderStatus:
    case NotSupported
    case NotLinked
    case NeedsPermission(folderName: String)
    case Linked(folderName: String)

  private val dbName    = "windspotter-fs"
  private val storeName = "handles"
  private val handleKey = "configFolder"

  private var dirHandle: Option[js.Dynamic] = None

  def supported: Boolean =
    js.typeOf(js.Dynamic.global.window.selectDynamic("showDirectoryPicker")) != "undefined"

  val statusVar: Var[FolderStatus] =
    Var(if supported then FolderStatus.NotLinked else FolderStatus.NotSupported)

  val messageVar: Var[Option[String]] = Var(None)

  // Restores a previously linked folder on app start.
  def init(): Unit =
    if supported then
      loadStoredHandle().foreach {
        case Some(handle) =>
          dirHandle = Some(handle)
          queryPermission(handle).foreach {
            case "granted" =>
              statusVar.set(FolderStatus.Linked(handleName(handle)))
              loadFromFolder(handle)
            case _ =>
              statusVar.set(FolderStatus.NeedsPermission(handleName(handle)))
          }
        case None => ()
      }

  // Opens the directory picker (must be called from a user gesture).
  def linkFolder(): Unit =
    js.Dynamic.global.window
      .showDirectoryPicker(js.Dynamic.literal(mode = "readwrite"))
      .asInstanceOf[js.Promise[js.Dynamic]]
      .toFuture
      .onComplete {
        case Success(handle) =>
          dirHandle = Some(handle)
          storeHandle(handle)
          statusVar.set(FolderStatus.Linked(handleName(handle)))
          syncAfterLink(handle)
        case Failure(_) => () // picker cancelled
      }

  // Re-requests permission for the stored folder (user gesture required).
  def reconnect(): Unit =
    dirHandle.foreach { handle =>
      requestPermission(handle).foreach {
        case "granted" =>
          statusVar.set(FolderStatus.Linked(handleName(handle)))
          loadFromFolder(handle)
        case _ => ()
      }
    }

  def unlink(): Unit =
    dirHandle = None
    deleteStoredHandle()
    statusVar.set(FolderStatus.NotLinked)
    messageVar.set(None)

  private def linkedHandle: Option[js.Dynamic] =
    statusVar.now() match
      case FolderStatus.Linked(_) => dirHandle
      case _                      => None

  // ── mirroring (called from ConfigService) ─────────────────────────────

  def writeConfig(config: WebcamConfig): Unit =
    linkedHandle.foreach(writeConfigTo(_, config).failed.foreach { e =>
      dom.console.error(s"Failed to write config file for '${config.name}': ${e.getMessage}")
    })

  def deleteConfigFile(name: String): Unit =
    linkedHandle.foreach { dir =>
      dir
        .removeEntry(fileNameFor(name))
        .asInstanceOf[js.Promise[Unit]]
        .toFuture
        .recover { case _ => () } // file may not exist
    }

  // ── sync ──────────────────────────────────────────────────────────────

  // After linking: configs already in the folder win over local ones with
  // the same name; local-only configs are written out as files.
  private def syncAfterLink(handle: js.Dynamic): Unit =
    readFolderConfigs(handle)
      .map { folderConfigs =>
        ConfigService.mergeFromFolder(folderConfigs)
        ConfigService.customConfigsVar.now().foreach(writeConfig)
        setMessage(folderConfigs.length)
      }
      .failed
      .foreach(e => dom.console.error(s"Folder sync failed: ${e.getMessage}"))

  private def loadFromFolder(handle: js.Dynamic): Unit =
    readFolderConfigs(handle)
      .map { folderConfigs =>
        ConfigService.mergeFromFolder(folderConfigs)
        setMessage(folderConfigs.length)
      }
      .failed
      .foreach(e => dom.console.error(s"Loading configs from folder failed: ${e.getMessage}"))

  private def setMessage(count: Int): Unit =
    if count > 0 then
      messageVar.set(Some(s"Loaded $count configuration(s) from the folder."))

  private def readFolderConfigs(dir: js.Dynamic): Future[List[WebcamConfig]] =
    val iterator = dir.values().asInstanceOf[js.Dynamic]
    def loop(acc: List[WebcamConfig]): Future[List[WebcamConfig]] =
      iterator.next().asInstanceOf[js.Promise[js.Dynamic]].toFuture.flatMap { step =>
        if step.done.asInstanceOf[Boolean] then Future.successful(acc)
        else
          val entry    = step.value.asInstanceOf[js.Dynamic]
          val kind     = entry.kind.asInstanceOf[String]
          val fileName = entry.name.asInstanceOf[String]
          if kind == "file" && fileName.endsWith(".json") then
            readFileText(entry).flatMap { text =>
              ConfigJson.fromJson(text) match
                case Right(cfg) => loop(cfg :: acc)
                case Left(error) =>
                  dom.console.warn(s"Skipping '$fileName' in config folder: $error")
                  loop(acc)
            }
          else loop(acc)
      }
    loop(List.empty).map(_.reverse)

  private def readFileText(fileHandle: js.Dynamic): Future[String] =
    fileHandle
      .getFile()
      .asInstanceOf[js.Promise[dom.File]]
      .toFuture
      .flatMap(_.text().toFuture)

  private def writeConfigTo(dir: js.Dynamic, config: WebcamConfig): Future[Unit] =
    val json = ConfigJson.toJson(config)
    for
      fileHandle <- dir
        .getFileHandle(fileNameFor(config.name), js.Dynamic.literal(create = true))
        .asInstanceOf[js.Promise[js.Dynamic]]
        .toFuture
      writable <- fileHandle.createWritable().asInstanceOf[js.Promise[js.Dynamic]].toFuture
      _        <- writable.write(json).asInstanceOf[js.Promise[Unit]].toFuture
      _        <- writable.close().asInstanceOf[js.Promise[Unit]].toFuture
    yield ()

  private def fileNameFor(configName: String): String =
    configName.replaceAll("""[\\/:*?"<>|]""", "-") + ".json"

  private def handleName(handle: js.Dynamic): String =
    handle.name.asInstanceOf[String]

  // ── permissions ───────────────────────────────────────────────────────

  private def queryPermission(handle: js.Dynamic): Future[String] =
    handle
      .queryPermission(js.Dynamic.literal(mode = "readwrite"))
      .asInstanceOf[js.Promise[String]]
      .toFuture

  private def requestPermission(handle: js.Dynamic): Future[String] =
    handle
      .requestPermission(js.Dynamic.literal(mode = "readwrite"))
      .asInstanceOf[js.Promise[String]]
      .toFuture

  // ── IndexedDB storage for the folder handle ───────────────────────────

  private def openDb(): Future[js.Dynamic] =
    val promise = Promise[js.Dynamic]()
    val request = js.Dynamic.global.window.indexedDB.open(dbName, 1)
    request.onupgradeneeded = js.Any.fromFunction1 { (_: js.Any) =>
      request.result.createObjectStore(storeName)
      ()
    }
    request.onsuccess = js.Any.fromFunction1 { (_: js.Any) =>
      promise.success(request.result.asInstanceOf[js.Dynamic])
      ()
    }
    request.onerror = js.Any.fromFunction1 { (_: js.Any) =>
      promise.failure(new RuntimeException("Could not open IndexedDB"))
      ()
    }
    promise.future

  private def storeHandle(handle: js.Dynamic): Unit =
    openDb().foreach { db =>
      db.transaction(js.Array(storeName), "readwrite")
        .objectStore(storeName)
        .put(handle, handleKey)
    }

  private def loadStoredHandle(): Future[Option[js.Dynamic]] =
    openDb()
      .flatMap { db =>
        val promise = Promise[Option[js.Dynamic]]()
        val request = db
          .transaction(js.Array(storeName), "readonly")
          .objectStore(storeName)
          .get(handleKey)
        request.onsuccess = js.Any.fromFunction1 { (_: js.Any) =>
          val result = request.result
          promise.success(
            if js.isUndefined(result) || result == null then None
            else Some(result.asInstanceOf[js.Dynamic])
          )
          ()
        }
        request.onerror = js.Any.fromFunction1 { (_: js.Any) =>
          promise.success(None)
          ()
        }
        promise.future
      }
      .recover { case _ => None }

  private def deleteStoredHandle(): Unit =
    openDb().foreach { db =>
      db.transaction(js.Array(storeName), "readwrite")
        .objectStore(storeName)
        .delete(handleKey)
    }

end ConfigFolderService
