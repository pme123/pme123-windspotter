package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.Future

enum WebcamType:
  case ImageWebcam, VideoWebcam, WindyWebcam, YoutubeWebcam, ScrapedWebcam, IframeWebcam

case class Webcam(
    name: String,
    url: String,
    reloadInMin: Int,
    footer: String,
    mainPageLink: Option[String] = None,
    webcamType: WebcamType = WebcamType.ImageWebcam,
    scrapingConfig: Option[ScrapingConfig] = None
)

case class ScrapingConfig(
    pageUrl: String,               // The webpage to scrape
    imageRegex: String,            // Regex pattern to find image URLs
    baseUrl: Option[String] = None // Base URL to prepend to relative image URLs
)

case class WebcamGroup(
    name: String,
    webcams: List[Webcam]
)

case class ImageData(
    name: String,
    url: String,
    dataUrl: String
)

case class WebcamState(
    webcam: Webcam,
    selectedImage: Option[ImageData] = None,
    imageHistory: List[ImageData] = List.empty,
    isAutoRefresh: Boolean = false,
    lastUpdate: Option[String] = None,
    currentUrl: Option[String] = None
)

object WebcamData:

  // Urnersee

  val sisikonBootshafenNordWebcam = Webcam(
    name = "Sisikon Bootshafen Nord",
    url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera04.jpg",
    reloadInMin = 2,
    footer = "https://www.bhs.swiss"
  )

  val isletenWebcam = Webcam(
    name = "Isleten",
    url = "https://meteo.windsurfing-urnersee.ch/webcam_isleten.jpg",
    reloadInMin = 5,
    footer = "https://windsurfing-urnersee.ch"
  )

  val gruonbachWebcam = Webcam(
    name = "Gruonbach",
    url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_bucht.jpg",
    reloadInMin = 5,
    footer = "https://surfclub-uri.ch"
  )

  val axeneggWebcam = Webcam(
    name = "Axenegg",
    url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_axenegg.jpg",
    reloadInMin = 5,
    footer = "https://surfclub-uri.ch"
  )

  val bolzbachWebcam = Webcam(
    name = "Bolzbach",
    url = "https://www.energieuri.ch/wp-content/uploads/webcam/live.jpg",
    reloadInMin = 5,
    footer = "https://www.seedorf-uri.ch"
  )

  val sisikonBootshafenSuedWebcam = Webcam(
    name = "Sisikon Bootshafen Süd",
    url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera05.jpg",
    reloadInMin = 2,
    footer = "https://www.bhs.swiss"
  )

  val windsurfingUrnerseeWebcam = Webcam(
    name = "Windsurfing Urnersee",
    url = "https://meteo.windsurfing-urnersee.ch/webcam_rechts.jpg",
    reloadInMin = 2,
    footer = "https://windsurfing-urnersee.ch"
  )

  // Silvaplana
  val silvaplanaMuletsWebcam      = Webcam(
    name = "Silvaplana Mulets",
    url = "https://hd-auth.skylinewebcams.com/livee.m3u8?a=ogpvks1vrujm74qcolu8ngnv50",
    reloadInMin = 0, // Videos don't need reloading
    footer = "https://www.skylinewebcams.com",
    mainPageLink = Some("https://www.skylinewebcams.com/webcam/schweiz/graubunden/silvaplana/silvaplana.html"),
    webcamType = WebcamType.VideoWebcam
  )

  // Alternative iframe-based Silvaplana webcam (more reliable for skylinewebcams.com)
  val silvaplanaIframeWebcam = Webcam(
    name = "Silvaplana (Iframe)",
    url = "https://www.skylinewebcams.com/webcam/schweiz/graubunden/silvaplana/silvaplana.html",
    reloadInMin = 0, // No need to reload for iframe
    footer = "https://www.skylinewebcams.com",
    mainPageLink = Some("https://www.skylinewebcams.com/webcam/schweiz/graubunden/silvaplana/silvaplana.html"),
    webcamType = WebcamType.IframeWebcam
  )



  // Kitesailing.ch webcam (iframe approach)
  val silvaplanaWebcam = Webcam(
    name = "Silvaplana",
    url = "https://www.kitesailing.ch/spot/webcam",
    reloadInMin = 0,
    footer = "https://www.kitesailing.ch",
    mainPageLink = Some("https://www.kitesailing.ch/spot/webcam"),
    webcamType = WebcamType.IframeWebcam
  )

  // Comersee

  val dervioWebcam = Webcam(
    name = "Dervio",
    url = "1564004172", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/dervio-valmadrera"),
    webcamType = WebcamType.WindyWebcam
  )
  val cremiaWebcam = Webcam(
    name = "Cremia",
    url = "1564003897", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WebcamType.WindyWebcam
  )
  val colicoWebcam = Webcam(
    name = "Colico",
    url = "1564003197", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WebcamType.WindyWebcam
  )
  val leccoWebcam  = Webcam(
    name = "Lecco",
    url = "1748447411", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/vista-lecco"),
    webcamType = WebcamType.WindyWebcam
  )
  val domasoWebcam = Webcam(
    name = "Domaso",
    url = "https://domasocamping.com/webcam/panorama.jpg", // Store just the Windy webcam ID
    reloadInMin = 1,                                       // Refresh every 10 minutes
    footer = "https://domasocamping.com"
  )

  // Zugersee
  val immenseeWebcam = Webcam(
    name = "Immensee",
    url = "FDJcAc0zOl8", // Example YouTube video ID - replace with actual webcam stream
    reloadInMin = 0,     // YouTube videos don't need reloading
    footer = "https://www.youtube.com/@yachtclubimmensee",
    webcamType = WebcamType.YoutubeWebcam
  )
  val aegeriWebcam   = Webcam(
    name = "Aegerisee",
    url = "https://scae.ch/webcam/image.jpg",
    reloadInMin = 5,
    footer = "https://scae.ch"
  )
  val walchwilWebcam = Webcam(
    name = "Walchwil",
    url = "https://api.codetabs.com/v1/proxy?quest=http://109.164.203.165/record/current.jpg",
    reloadInMin = 5,
    footer = "https://www.weisszahnarzt.ch",
    mainPageLink = Some("http://109.164.203.165/cgi-bin/guestimage.html")
  )
  val chamWebcam     = Webcam(
    name = "Cham",
    url = "https://www.webcam.scc.ch/image_large.jpg",
    reloadInMin = 5,
    footer = "https://www.webcam.scc.ch"
  )
  val zugWebcam      = Webcam(
    name = "Zug",
    url = "https://www.barile.ch/yczug/yczug/cam1.jpg",
    reloadInMin = 2,
    footer = "https://www.yczug.ch"
  )
  // Vierwaldstättersee (additional)
  val brunnenWebcam  = Webcam(
    name = "Brunnen",
    url = "https://www.foto-webcam.eu/webcam/brunnen/current/816.jpg",
    reloadInMin = 5,
    footer = "https://www.foto-webcam.eu/webcam/brunnen"
  )

  // Sempachersee / Alpnachersee
  val eichWebcam      = Webcam(
    name = "Eich Sempachersee",
    url = "https://windsurfclubeich.ch/webcam/hikvision_current.jpg",
    reloadInMin = 1,
    footer = "https://windsurfclubeich.ch"
  )
  val alpnacherWebcam = Webcam(
    name = "Alpnachersee",
    url = "https://webcam.waverocker.org/livCam.jpg?1755370546255",
    reloadInMin = 5,
    footer = "https://www.surfstation-alpnachersee.ch"
  )

  // Gardasee
  val malcesineWebcam   = Webcam(
    name = "Malcesine",
    url = "https://addicted-sports.com/fileadmin/webcam/gardasee/current/full.jpg",
    reloadInMin = 10,
    footer = "https://addicted-sports.com",
    mainPageLink = Some("https://gardasee.webcam/de/malcesine-webcams.html")
  )
  val caporeamoloWebcam = Webcam(
    name = "Capo Reamol",
    url = "https://addicted-sports.com/fileadmin/webcam/caporeamol/current/full.jpg",
    reloadInMin = 10,
    footer = "https://addicted-sports.com",
    mainPageLink = Some("https://gardasee.webcam/de/limone-webcams.html")
  )

  val tignaleWebcam = Webcam(
    name = "Tignale",
    url = "https://pradelafam.net/cam/cam.jpg",
    reloadInMin = 10,
    footer = "https://pradelafam.net",
    mainPageLink = Some("https://gardasee.webcam/de/tignale-webcams.html")
  )

  val torboleWebcam = Webcam(
    name = "Torbole",
    url =
      "https://windinfo.eu/fileadmin/user_upload/webcam_upload/gardasee/shaka-torbole-aktuell-1280.jpg",
    reloadInMin = 10,
    footer = "https://windinfo.eu",
    mainPageLink = Some("https://gardasee.webcam/de/torbole-webcams.html")
  )

  // South of France
  val almanarreHyeresWebcam = Webcam(
    name = "Hyères - L'Almanarre",
    url = "6nXr-WCejHc",
    reloadInMin = 10,
    footer = "https://vision-environnement.com",
    webcamType = WebcamType.YoutubeWebcam
  )

  val estagnetsWebcam = Webcam(
    name = "Hyères - Les Estagnets",
    url = "Tf5fxg4rWfE",
    reloadInMin = 10,
    footer = "https://vision-environnement.com",
    webcamType = WebcamType.YoutubeWebcam
  )
  val madragueWebcam  = Webcam(
    name = "Hyères - La Madrague",
    url = "ZlN7i9XP0x0",
    reloadInMin = 10,
    footer = "https://vision-environnement.com",
    webcamType = WebcamType.YoutubeWebcam
  )
  val carroWebcam = Webcam(
    name = "Carro",
    url = "https://www.skaping.com/marseille/spot-de-carro/video",
    reloadInMin = 0, // No need to reload for iframe
    footer = "https://www.skaping.com",
    mainPageLink = Some("https://www.skaping.com/marseille/spot-de-carro/video"),
    webcamType = WebcamType.IframeWebcam
  )
  val grauDuRoiPlageSudWebcam = Webcam(
    name = "Grau du Roi - Plage Sud",
    url = "https://grauduroi.roundshot.com/plage/",
    reloadInMin = 0, // No need to reload for iframe
    footer = "https://letsgrau.com",
    mainPageLink = Some("https://letsgrau.com/webcam-grau-du-roi/"),
    webcamType = WebcamType.IframeWebcam
  )

  // Westschweiz
  val stBlaiseWebcam = Webcam(
    name = "St. Blaise",
    url = "https://lacdeneuchatel.roundshot.com/",
    reloadInMin = 0, // No need to reload for iframe
    footer = "https://lacdeneuchatel.roundshot.com",
    mainPageLink = Some("https://lacdeneuchatel.roundshot.com/"),
    webcamType = WebcamType.IframeWebcam
  )
  val biseNoireWebcam = Webcam(
    name = "Bise Noire",
    url = "https://www.bisenoire.ch/webcam/Bisenoire_BIG_00.jpg",
    reloadInMin = 5,
    footer = "https://www.bisenoire.ch",
    mainPageLink = Some("https://www.bisenoire.ch/webcam/"),
    webcamType = WebcamType.ImageWebcam
  )


  val sixFoursLeBruscWebcam = Webcam(
    name = "Six Fours - Le Brusc",
    url = "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html",
    reloadInMin = 5,
    footer = "https://www.winds-up.com",
    mainPageLink = Some(
      "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html"
    ),
    webcamType = WebcamType.ScrapedWebcam,
    scrapingConfig = Some(ScrapingConfig(
      pageUrl =
        "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html",
      imageRegex = """/webcam/49/49_\d+_\.jpg""", // Match just the path, stop at word boundaries
      baseUrl = Some("https://img.winds-up.com")  // Complete base URL
    ))
  )
  val leJaiWebcam = Webcam(
    name = "Le Jaï",
    url = "https://img.winds-up.com/webcam/26/26_1734700800_.jpg",
    reloadInMin = 5,
    footer = "https://www.winds-up.com",
    mainPageLink = Some(
      "https://www.winds-up.com/spot-le-jai-windsurf-kitesurf-26-webcam-live.html"
    ),
    webcamType = WebcamType.ScrapedWebcam,
    scrapingConfig = Some(ScrapingConfig(
      pageUrl =
        "https://www.winds-up.com/spot-le-jai-windsurf-kitesurf-26-webcam-live.html",
      imageRegex = """/webcam/26/26_\d+_\.jpg""", // Match just the path, stop at word boundaries
      baseUrl = Some("https://img.winds-up.com")  // Complete base URL
    ))
  )
  // Lakes

  val urnersee = WebcamGroup(
    name = "Urnersee",
    webcams = List(
      sisikonBootshafenNordWebcam,
      isletenWebcam,
      gruonbachWebcam,
      axeneggWebcam,
      brunnenWebcam,
      // bolzbachWebcam,
      sisikonBootshafenSuedWebcam
      // windsurfingUrnerseeWebcam
    )
  )

  val east = WebcamGroup(
    name = "East",
    webcams = List(
      silvaplanaWebcam,
    )
  )

  val central = WebcamGroup(
    name = "Central",
    webcams = List(
      immenseeWebcam,
      walchwilWebcam,
      zugWebcam,
      chamWebcam,
      aegeriWebcam,
      eichWebcam,
      alpnacherWebcam
    )
  )

  val west = WebcamGroup(
    name = "West",
    webcams = List(
      stBlaiseWebcam,
      biseNoireWebcam
    )
  )

  val italy = WebcamGroup(
    name = "Italy",
    webcams = List(
      leccoWebcam,
      dervioWebcam,
      cremiaWebcam,
      colicoWebcam,
      domasoWebcam,
      // Gardasee
      malcesineWebcam,
      caporeamoloWebcam,
      torboleWebcam,
      tignaleWebcam
    )
  )

  val france = WebcamGroup(
    name = "France",
    webcams = List(
      almanarreHyeresWebcam,
      estagnetsWebcam,
      madragueWebcam,
      carroWebcam,
      sixFoursLeBruscWebcam,
      leJaiWebcam,
      grauDuRoiPlageSudWebcam,
    )
  )

  val webcamGroups = List(
    urnersee,
    central,
    west,
    east,
    italy,
    france
  )

  def getDefaultWebcamGroup: WebcamGroup = webcamGroups.head

  def getAllWebcams: List[Webcam] = webcamGroups.flatMap(_.webcams)

  def findWebcamGroupByName(name: String): Option[WebcamGroup] =
    webcamGroups.find(_.name == name)

end WebcamData
