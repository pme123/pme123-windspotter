package pme123.windspotter

sealed trait WebcamType
case object ImageWebcam extends WebcamType
case object VideoWebcam extends WebcamType
case object WindyWebcam extends WebcamType
case object YoutubeWebcam extends WebcamType

case class Webcam(
                   name: String,
                   url: String,
                   reloadInMin: Int,
                   footer: String,
                   liveVideoLink: Option[String] = None,
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
  val silvaplanaMultsWebcam      = Webcam(
    name = "Silvaplana Mulets",
    url = "https://hd-auth.skylinewebcams.com/live.m3u8?a=t9018ai5oko7gonu3aviiugh22",
    reloadInMin = 0, // Videos don't need reloading
    footer = "https://www.silvaplana.ch",
    webcamType = VideoWebcam
  )
  val silvaplanaSurfcenterWebcam = Webcam(
    name = "Silvaplana Surfcenter",
    url = "https://hd-auth.skylinewebcams.com/live.m3u8",
    reloadInMin = 0, // Videos don't need reloading
    footer = "https://www.silvaplana.ch",
    webcamType = VideoWebcam
  )

  // Comersee

  val dervioWebcam = Webcam(
    name = "Dervio",
    url = "1564004172", // Store just the Windy webcam ID
    reloadInMin = 10,    // Refresh every 10 minutes
    footer = "https://windy.com",
    liveVideoLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/dervio-valmadrera"),
    webcamType = WindyWebcam
  )
  val cremiaWebcam = Webcam(
    name = "Cremia",
    url = "1564003897", // Store just the Windy webcam ID
    reloadInMin = 10,    // Refresh every 10 minutes
    footer = "https://windy.com",
    liveVideoLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WindyWebcam
  )
  val colicoWebcam = Webcam(
    name = "Colico",
    url = "1564003197", // Store just the Windy webcam ID
    reloadInMin = 10,    // Refresh every 10 minutes
    footer = "https://windy.com",
    liveVideoLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WindyWebcam
  )
  val leccoWebcam = Webcam(
    name = "Lecco",
    url = "1748447411", // Store just the Windy webcam ID
    reloadInMin = 10,    // Refresh every 10 minutes
    footer = "https://windy.com",
    liveVideoLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/vista-lecco"),
    webcamType = WindyWebcam
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
    reloadInMin = 0, // YouTube videos don't need reloading
    footer = "https://www.youtube.com/@yachtclubimmensee",
    webcamType = YoutubeWebcam
  )
  val aegeriWebcam = Webcam(
    name = "Aegeri",
    url = "https://scae.ch/webcam/image.jpg",
    reloadInMin = 5,
    footer = "https://scae.ch"
  )
  val walchwilWebcam = Webcam(
    name = "Walchwil",
    url = "https://api.codetabs.com/v1/proxy?quest=http://109.164.203.165/record/current.jpg",
    reloadInMin = 5,
    footer = "https://www.weisszahnarzt.ch",
    liveVideoLink = Some("http://109.164.203.165/cgi-bin/guestimage.html")
  )
  val chamWebcam = Webcam(
    name = "Cham",
    url = "https://www.webcam.scc.ch/image_large.jpg",
    reloadInMin = 5,
    footer = "https://www.webcam.scc.ch"
  )
  // Sempachersee
  val eichWebcam = Webcam(
    name = "Eich",
    url = "https://windsurfclubeich.ch/webcam/hikvision_current.jpg",
    reloadInMin = 1,
    footer = "https://www.windsurfclubeich.ch"
  )

  // Lakes

  val urnersee = Lake(
    name = "Urnersee",
    webcams = List(
      sisikonBootshafenNordWebcam,
      isletenWebcam,
      gruonbachWebcam,
      axeneggWebcam,
      // bolzbachWebcam,
      sisikonBootshafenSuedWebcam
      // windsurfingUrnerseeWebcam
    )
  )

  val silvaplana = Lake(
    name = "Silvaplana",
    webcams = List(
      silvaplanaMultsWebcam,
      silvaplanaSurfcenterWebcam
    )
  )

  val comersee = Lake(
    name = "Comersee",
    webcams = List(
      dervioWebcam,
      cremiaWebcam,
      colicoWebcam,
      domasoWebcam
    )
  )

  val zugersee = Lake(
    name = "Zugerseen",
    webcams = List(
      immenseeWebcam,
      walchwilWebcam,
      chamWebcam,
      aegeriWebcam,

    )
  )

  val sempachersee = Lake(
    name = "Sempachersee",
    webcams = List(
      eichWebcam
    )
  )

  val lakes = List(
    zugersee,
    urnersee,
    comersee,
    sempachersee,
    // silvaplana,
  )

  def getDefaultLake: Lake = lakes.head

  def getAllWebcams: List[Webcam] = lakes.flatMap(_.webcams)

  def findLakeByName(name: String): Option[Lake] =
    lakes.find(_.name == name)
end WebcamData
