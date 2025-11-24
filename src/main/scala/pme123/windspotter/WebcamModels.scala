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
