  package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView:

  def apply(
    selectedWebcamGroupVar: Var[WebcamGroup],
    webcamGroupLoadingStates: Map[WebcamGroup, Var[Boolean]]
  ): HtmlElement =
    // Initialize webcam states and slideshow controls for all webcams
    val allWebcams   = WebcamData.getAllWebcams
    val webcamStates = allWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val slideshowControls = allWebcams.map { webcam =>
      webcam -> Var(false)
    }.toMap

    // Stop all slideshows when switching webcam groups
    selectedWebcamGroupVar.signal.foreach { newWebcamGroup =>
      slideshowControls.values.foreach(_.set(false))
      dom.console.log(s"🛑 Stopped all slideshows when switching to ${newWebcamGroup.name}")
    }(using OneTimeOwner(() => ()))

    div(
      className := "main-container",

      // Constrained wrapper for entire TabContainer with toggle
      div(
        className := "constrained-wrapper",

        // Tabs bar with toggle on the right
        div(
          className := "tabs-bar-container",

          // Webcam Group tabs
          TabContainer(
            className := "webcam-group-tabs",
            _.events.onTabSelect.map(_.detail.tab) --> { tab =>
              // Get the tab index instead of text content to avoid getting all the content
              val tabContainer = tab.parentElement
              val allTabs = tabContainer.querySelectorAll("ui5-tab")
              val tabIndex = (0 until allTabs.length).find(i => allTabs(i) == tab).getOrElse(0)

              dom.console.log(s"📋 Tab selected at index: $tabIndex")

              if (tabIndex < WebcamData.webcamGroups.length) {
                val selectedWebcamGroup = WebcamData.webcamGroups(tabIndex)
                dom.console.log(s"🏔️ Selecting webcam group by index: ${selectedWebcamGroup.name}")

                // Auto-enable loading for the selected webcam group if it's currently disabled
                val loadingStateVar = webcamGroupLoadingStates.getOrElse(selectedWebcamGroup, Var(false))
                if (!loadingStateVar.now()) {
                  dom.console.log(s"🔄 Auto-enabling loading for ${selectedWebcamGroup.name}")
                  loadingStateVar.set(true)
                }

                selectedWebcamGroupVar.set(selectedWebcamGroup)
              } else {
                dom.console.log(s"❌ Tab index $tabIndex out of range for ${WebcamData.webcamGroups.length} webcam groups")
              }
            },
            WebcamData.webcamGroups.zipWithIndex.map { case (webcamGroup, index) =>
              Seq(
                Tab(
                  _.text     := webcamGroup.name,
                  _.selected := (webcamGroup == WebcamData.getDefaultWebcamGroup),
                  WebcamGroupView(webcamGroup, webcamStates, slideshowControls, webcamGroupLoadingStates, ImageOverlayView.showImageOverlay)
                )
              )
            }
          ),

          // Loading toggle on the right
          div(
            className := "tabs-toggle-container",
            child <-- selectedWebcamGroupVar.signal.map { selectedWebcamGroup =>
              dom.console.log(s"🔄 Toggle now controlling: ${selectedWebcamGroup.name}")
              val loadingStateVar = webcamGroupLoadingStates.getOrElse(selectedWebcamGroup, Var(false))
              dom.console.log(s"🎛️ Current loading state for ${selectedWebcamGroup.name}: ${loadingStateVar.now()}")
              div(
                className := "tabs-toggle-wrapper",
                Switch(
                  _.checked <-- loadingStateVar.signal,
                  _.events.onChange.mapTo(loadingStateVar.now()).map(!_) --> { newState =>
                    dom.console.log(s"🎛️ Toggle changed for ${selectedWebcamGroup.name}: $newState")
                    loadingStateVar.writer.onNext(newState)
                  },
                  _.design := SwitchDesign.Graphical
                )
              )
            }
          )
        )
      )
    )
  end apply
end MainView
