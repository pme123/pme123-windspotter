package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView:

  def apply(
    selectedLakeVar: Var[Lake],
    lakeLoadingStates: Map[Lake, Var[Boolean]]
  ): HtmlElement =
    // Initialize webcam states and slideshow controls for all webcams
    val allWebcams   = WebcamData.getAllWebcams
    val webcamStates = allWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val slideshowControls = allWebcams.map { webcam =>
      webcam -> Var(false)
    }.toMap

    // Stop all slideshows when switching lakes
    selectedLakeVar.signal.foreach { newLake =>
      slideshowControls.values.foreach(_.set(false))
      dom.console.log(s"🛑 Stopped all slideshows when switching to ${newLake.name}")
    }(OneTimeOwner(() => ()))

    div(
      className := "main-container",

      // Constrained wrapper for entire TabContainer with toggle
      div(
        className := "constrained-wrapper",

        // Tabs bar with toggle on the right
        div(
          className := "tabs-bar-container",

          // Lake tabs
          TabContainer(
            className := "lake-tabs",
            _.events.onTabSelect.map(_.detail.tab) --> { tab =>
              // Get the tab index instead of text content to avoid getting all the content
              val tabContainer = tab.parentElement
              val allTabs = tabContainer.querySelectorAll("ui5-tab")
              val tabIndex = (0 until allTabs.length).find(i => allTabs(i) == tab).getOrElse(0)

              dom.console.log(s"📋 Tab selected at index: $tabIndex")

              if (tabIndex < WebcamData.lakes.length) {
                val selectedLake = WebcamData.lakes(tabIndex)
                dom.console.log(s"🏔️ Selecting lake by index: ${selectedLake.name}")
                selectedLakeVar.set(selectedLake)
              } else {
                dom.console.log(s"❌ Tab index $tabIndex out of range for ${WebcamData.lakes.length} lakes")
              }
            },
            WebcamData.lakes.zipWithIndex.map { case (lake, index) =>
              Seq(
                Tab(
                  _.text     := lake.name,
                  _.selected := (lake == WebcamData.getDefaultLake),
                  LakeView(lake, webcamStates, slideshowControls, lakeLoadingStates, ImageOverlayView.showImageOverlay)
                )
              )
            }
          ),

          // Loading toggle on the right
          div(
            className := "tabs-toggle-container",
            child <-- selectedLakeVar.signal.map { selectedLake =>
              dom.console.log(s"🔄 Toggle now controlling: ${selectedLake.name}")
              val loadingStateVar = lakeLoadingStates.getOrElse(selectedLake, Var(false))
              dom.console.log(s"🎛️ Current loading state for ${selectedLake.name}: ${loadingStateVar.now()}")
              div(
                className := "tabs-toggle-wrapper",
                Switch(
                  _.checked <-- loadingStateVar.signal,
                  _.events.onChange.mapTo(loadingStateVar.now()).map(!_) --> { newState =>
                    dom.console.log(s"🎛️ Toggle changed for ${selectedLake.name}: $newState")
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
