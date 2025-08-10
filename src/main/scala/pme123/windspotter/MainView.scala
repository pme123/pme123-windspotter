package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView:

  def apply(): HtmlElement =
    // Initialize webcam states and slideshow controls for all webcams
    val allWebcams   = WebcamData.getAllWebcams
    val webcamStates = allWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val slideshowControls = allWebcams.map { webcam =>
      webcam -> Var(false)
    }.toMap

    val selectedLakeVar = Var(WebcamData.getDefaultLake)

    // Stop all slideshows when switching lakes
    selectedLakeVar.signal.foreach { newLake =>
      slideshowControls.values.foreach(_.set(false))
      dom.console.log(s"🛑 Stopped all slideshows when switching to ${newLake.name}")
    }(OneTimeOwner(() => ()))

    div(
      className := "main-container",

      // Constrained wrapper for entire TabContainer
      div(
        className := "constrained-wrapper",
        // Lake tabs
        TabContainer(
          className := "lake-tabs",
          WebcamData.lakes.map { lake =>
            Seq(
              Tab(
                _.text     := lake.name,
                _.selected := (lake == WebcamData.getDefaultLake),
                LakeView(lake, webcamStates, slideshowControls, ImageUploadView.showImageOverlay)
              )
            )
          }
        )
      )
    )
  end apply
end MainView
