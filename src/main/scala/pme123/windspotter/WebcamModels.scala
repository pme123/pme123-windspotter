package pme123.windspotter

sealed trait WebcamType
case object ImageWebcam extends WebcamType
case object VideoWebcam extends WebcamType

case class Webcam(
  name: String,
  url: String,
  reloadInMin: Int,
  footer: String,
  webcamType: WebcamType = ImageWebcam
)

case class Lake(
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

object WebcamData {

  // Individual Webcam Variables
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

  val skylineVideoWebcam = Webcam(
    name = "Skyline Video Stream",
    url = "https://hd-auth.skylinewebcams.com/live.m3u8",
    reloadInMin = 0, // Videos don't need reloading
    footer = "https://www.skylinewebcams.com",
    webcamType = VideoWebcam
  )

  // Individual Lake Variables
  val urnersee = Lake(
    name = "Urnersee",
    webcams = List(
      sisikonBootshafenNordWebcam,
      isletenWebcam,
      gruonbachWebcam,
      axeneggWebcam,
     // bolzbachWebcam,
      sisikonBootshafenSuedWebcam,
     // windsurfingUrnerseeWebcam
    )
  )

  val silvaplana = Lake(
    name = "Silvaplana",
    webcams = List(
      skylineVideoWebcam
    )
  )

  val lakes = List(
    urnersee,
  //  silvaplana
  )

  def getDefaultLake: Lake = lakes.head

  def getAllWebcams: List[Webcam] = lakes.flatMap(_.webcams)

  def findLakeByName(name: String): Option[Lake] =
    lakes.find(_.name == name)
}
