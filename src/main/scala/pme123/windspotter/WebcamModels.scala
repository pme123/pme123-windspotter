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
    scrapingConfig: Option[ScrapingConfig] = None,
    hidden: Boolean = false
)

case class ScrapingConfig(
    pageUrl: String,               // The webpage to scrape
    imageRegex: String,            // Regex pattern to find image URLs
    baseUrl: Option[String] = None // Base URL to prepend to relative image URLs
)

case class WebcamGroup(
    name: String,
    webcams: List[Webcam],
    hidden: Boolean = false
)

case class WebcamConfig(
    name: String,
    groups: List[WebcamGroup]
):
  // The configuration as shown in the app: hidden groups/webcams removed
  def visible: WebcamConfig =
    copy(groups =
      groups
        .filterNot(_.hidden)
        .map(group => group.copy(webcams = group.webcams.filterNot(_.hidden)))
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
