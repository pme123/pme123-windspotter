package pme123.windspotter

sealed trait WebcamType
case object ImageWebcam   extends WebcamType
case object VideoWebcam   extends WebcamType
case object WindyWebcam   extends WebcamType
case object YoutubeWebcam extends WebcamType

case class Webcam(
    name: String,
    url: String,
    reloadInMin: Int,
    footer: String,
    mainPageLink: Option[String] = None,
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
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/dervio-valmadrera"),
    webcamType = WindyWebcam
  )
  val cremiaWebcam = Webcam(
    name = "Cremia",
    url = "1564003897", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WindyWebcam
  )
  val colicoWebcam = Webcam(
    name = "Colico",
    url = "1564003197", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
    webcamType = WindyWebcam
  )
  val leccoWebcam  = Webcam(
    name = "Lecco",
    url = "1748447411", // Store just the Windy webcam ID
    reloadInMin = 10,   // Refresh every 10 minutes
    footer = "https://windy.com",
    mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/vista-lecco"),
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
    reloadInMin = 0,     // YouTube videos don't need reloading
    footer = "https://www.youtube.com/@yachtclubimmensee",
    webcamType = YoutubeWebcam
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
    webcamType = YoutubeWebcam
  )

  val estagnetsWebcam = Webcam(
    name = "Hyères - Les Estagnets",
    url = "Tf5fxg4rWfE",
    reloadInMin = 10,
    footer = "https://vision-environnement.com",
    webcamType = YoutubeWebcam
  )
  val madragueWebcam  = Webcam(
    name = "Hyères - La Madrague",
    url = "ZlN7i9XP0x0",
    reloadInMin = 10,
    footer = "https://vision-environnement.com",
    webcamType = YoutubeWebcam
  )
  val carroWebcam     = Webcam(
    name = "Carro",
    url = "k9gyMLsi_sE",
    reloadInMin = 5,
    footer = "https://vision-environnement.com",
    webcamType = YoutubeWebcam
  )
  // Lakes

  val urnersee = Lake(
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
      leccoWebcam,
      dervioWebcam,
      cremiaWebcam,
      colicoWebcam,
      domasoWebcam
    )
  )

  val gardasee = Lake(
    name = "Gardasee",
    webcams = List(
      malcesineWebcam,
      caporeamoloWebcam,
      torboleWebcam,
      tignaleWebcam
    )
  )

  val southOfFrance = Lake(
    name = "South of France",
    webcams = List(
      almanarreHyeresWebcam,
      estagnetsWebcam,
      madragueWebcam,
      carroWebcam
    )
  )

  val zugersee = Lake(
    name = "Zugerseen",
    webcams = List(
      immenseeWebcam,
      walchwilWebcam,
      zugWebcam,
      chamWebcam,
      aegeriWebcam
    )
  )

  val sempachersee = Lake(
    name = "Luzern",
    webcams = List(
      eichWebcam,
      alpnacherWebcam
    )
  )

  val lakes = List(
    urnersee,
    zugersee,
    sempachersee,
    // silvaplana,
    comersee,
    gardasee,
    southOfFrance
  )

  def getDefaultLake: Lake = lakes.head

  def getAllWebcams: List[Webcam] = lakes.flatMap(_.webcams)

  def findLakeByName(name: String): Option[Lake] =
    lakes.find(_.name == name)

end WebcamData
