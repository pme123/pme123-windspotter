package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView {

  def apply(): HtmlElement = {
    // Initialize webcam states and slideshow controls
    val webcamStates = WebcamData.availableWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val slideshowControls = WebcamData.availableWebcams.map { webcam =>
      webcam -> Var(false)
    }.toMap

    val selectedWebcamVar = Var(WebcamData.getDefaultWebcam)

    // Stop all slideshows when switching webcams
    selectedWebcamVar.signal.foreach { newWebcam =>
      slideshowControls.values.foreach(_.set(false))
      dom.console.log(s"🛑 Stopped all slideshows when switching to ${newWebcam.name}")
    }(OneTimeOwner(() => ()))

    div(
      className := "main-container",

      // Webcam selector
      div(
        className := "webcam-selector",
        Label("Select Webcam:"),
        select(
          className := "webcam-select",
          onChange.mapToValue --> { value =>
            WebcamData.findWebcamByName(value).foreach(selectedWebcamVar.set)
          },
          WebcamData.availableWebcams.map { webcam =>
            option(
              value := webcam.name,
              selected := (webcam == WebcamData.getDefaultWebcam),
              webcam.name
            )
          }
        )
      ),

      // Dynamic webcam view
      child <-- selectedWebcamVar.signal.map { selectedWebcam =>
        (webcamStates.get(selectedWebcam), slideshowControls.get(selectedWebcam)) match {
          case (Some(stateVar), Some(slideshowControlVar)) =>
            WebcamView(selectedWebcam, stateVar, ImageUploadView.showImageOverlay, slideshowControlVar)
          case _ =>
            div("Error: Webcam not found")
        }
      }
    )
  }
}
