package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object LakeView {

  def apply(
    lake: Lake,
    webcamStates: Map[Webcam, Var[WebcamState]],
    slideshowControls: Map[Webcam, Var[Boolean]],
    showImageOverlay: (String, Option[List[ImageData]], Option[Int], Option[ImageData => Unit]) => Unit
  ): HtmlElement = {

    div(
      className := "lake-view",
      
      // Webcams displayed vertically
      div(
        className := "webcams-vertical",
        lake.webcams.map { webcam =>
          (webcamStates.get(webcam), slideshowControls.get(webcam)) match {
            case (Some(stateVar), Some(slideshowControlVar)) =>
              div(
                className := "webcam-vertical-item",
                WebcamView(webcam, stateVar, showImageOverlay, slideshowControlVar)
              )
            case _ =>
              div(
                className := "webcam-error",
                s"Error: State not found for ${webcam.name}"
              )
          }
        }
      )
    )
  }
}
