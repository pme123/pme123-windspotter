package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}

object MainView {

  def apply(): HtmlElement = {
    // Initialize webcam states
    val webcamStates = WebcamData.availableWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val selectedWebcamVar = Var(WebcamData.getDefaultWebcam)

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
        webcamStates.get(selectedWebcam) match {
          case Some(stateVar) =>
            WebcamView(selectedWebcam, stateVar, ImageUploadView.showImageOverlay)
          case None =>
            div("Error: Webcam not found")
        }
      }
    )
  }
}
