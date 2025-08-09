package pme123.windspotter

case class Webcam(
  name: String,
  url: String,
  reloadInMin: Int
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
  
  val availableWebcams = List(
    Webcam(
      name = "Webcam Sisikon",
      url = "http://bhsboots.myhostpoint.ch/kamera04.jpg",
      reloadInMin = 1
    ),
    Webcam(
      name = "Webcam Brunnen",
      url = "https://www.foto-webcam.eu/webcam/include/dlimg.php?wc=brunnen&img=2025/08/09/1320&h=7276&res=hd",
      reloadInMin = 10
    ),
    Webcam(
      name = "Test Webcam",
      url = "https://picsum.photos/800/600",
      reloadInMin = 2
    )
  )
  
  def getDefaultWebcam: Webcam = availableWebcams.head
  
  def findWebcamByName(name: String): Option[Webcam] = 
    availableWebcams.find(_.name == name)
}
