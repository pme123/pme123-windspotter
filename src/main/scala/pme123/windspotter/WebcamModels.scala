package pme123.windspotter

case class Webcam(
  name: String,
  url: String,
  reloadInMin: Int,
  footer: String
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
  
  val lakes = List(
    Lake(
      name = "Urnersee",
      webcams = List(
        Webcam(
          name = "Sisikon Bootshafen Nord",
          url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera04.jpg",
          reloadInMin = 2,
          footer = "https://www.bhs.swiss"
        ),
        Webcam(
          name = "Isleten",
          url = "https://meteo.windsurfing-urnersee.ch/webcam_isleten.jpg",
          reloadInMin = 5,
          footer = "https://windsurfing-urnersee.ch"
        ),
        Webcam(
          name = "Gruonbach",
          url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_bucht.jpg",
          reloadInMin = 5,
          footer = "https://surfclub-uri.ch"
        ),
        Webcam(
          name = "Axenegg",
          url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_axenegg.jpg",
          reloadInMin = 5,
          footer = "https://surfclub-uri.ch"
        ),
        Webcam(
          name = "Bolzbach",
          url = "https://www.energieuri.ch/wp-content/uploads/webcam/live.jpg",
          reloadInMin = 2,
          footer = "https://www.seedorf-uri.ch"
        ),
        Webcam(
          name = "Sisikon Bootshafen Süd",
          url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera05.jpg",
          reloadInMin = 2,
          footer = "https://www.bhs.swiss"
        ),
        Webcam(
          name = "Windsurfing Urnersee",
          url = "https://meteo.windsurfing-urnersee.ch/webcam_rechts.jpg",
          reloadInMin = 2,
          footer = "https://windsurfing-urnersee.ch"
        )
      )
    ),
    Lake(
      name = "Test Lake",
      webcams = List(
        Webcam(
          name = "Test Webcam 1",
          url = "https://picsum.photos/800/600",
          reloadInMin = 2,
          footer = "https://picsum.photos"
        ),
        Webcam(
          name = "Test Webcam 2",
          url = "https://picsum.photos/900/700",
          reloadInMin = 3,
          footer = "https://picsum.photos"
        )
      )
    )
  )

  def getDefaultLake: Lake = lakes.head

  def getAllWebcams: List[Webcam] = lakes.flatMap(_.webcams)

  def findLakeByName(name: String): Option[Lake] =
    lakes.find(_.name == name)
}
