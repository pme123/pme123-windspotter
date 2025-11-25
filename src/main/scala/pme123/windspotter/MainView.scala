  package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView:

  def apply(
    selectedWebcamGroupVar: Var[WebcamGroup]
  ): HtmlElement =
    // Initialize webcam states and slideshow controls for all webcams
    val allWebcams   = groups.getAllWebcams
    val webcamStates = allWebcams.map { webcam =>
      webcam -> Var(WebcamState(webcam))
    }.toMap

    val slideshowControls = allWebcams.map { webcam =>
      webcam -> Var(false)
    }.toMap

    // Track which webcam groups have been loaded
    val loadedGroups = scala.collection.mutable.Set[String]()

    // Helper function to start webcams for a group
    def startWebcamsForGroup(webcamGroup: WebcamGroup): Unit = {
      if (!loadedGroups.contains(webcamGroup.name)) {
        dom.console.log(s"🚀 Starting auto-refresh for all webcams in ${webcamGroup.name}")
        loadedGroups.add(webcamGroup.name)

        webcamGroup.webcams.foreach { webcam =>
          webcamStates.get(webcam).foreach { stateVar =>
            webcam.webcamType match {
              case WebcamType.ScrapedWebcam =>
                dom.console.log(s"🕷️ Starting auto-scraping for ${webcam.name}")
                ScrapedWebcamService.scrapeAndLoadImage(webcam, stateVar)
                ScrapedWebcamService.startAutoScraping(webcam, stateVar)
              case WebcamType.VideoWebcam =>
                dom.console.log(s"📹 Skipping auto-refresh for video webcam: ${webcam.name}")
              case WebcamType.IframeWebcam =>
                dom.console.log(s"🖼️ Skipping auto-refresh for iframe webcam: ${webcam.name}")
              case _ =>
                dom.console.log(s"🚀 Starting auto-refresh for ${webcam.name}")
                WebcamService.startAutoRefresh(webcam, stateVar)
            }
          }
        }
      }
    }

    // Start webcams for the default group immediately
    dom.window.setTimeout(() => {
      val defaultGroup = selectedWebcamGroupVar.now()
      dom.console.log(s"🎬 Starting default webcam group: ${defaultGroup.name}")
      startWebcamsForGroup(defaultGroup)
    }, 100)

    // Stop all slideshows and start auto-refresh for webcams when switching webcam groups
    selectedWebcamGroupVar.signal.foreach { newWebcamGroup =>
      slideshowControls.values.foreach(_.set(false))
      dom.console.log(s"🛑 Stopped all slideshows when switching to ${newWebcamGroup.name}")

      // Start auto-refresh for all webcams in the newly selected group (only once per group)
      startWebcamsForGroup(newWebcamGroup)
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

              if (tabIndex < groups.webcamGroups.length) {
                val selectedWebcamGroup = groups.webcamGroups(tabIndex)
                dom.console.log(s"🏔️ Selecting webcam group by index: ${selectedWebcamGroup.name}")

                selectedWebcamGroupVar.set(selectedWebcamGroup)
              } else {
                dom.console.log(s"❌ Tab index $tabIndex out of range for ${groups.webcamGroups.length} webcam groups")
              }
            },
            groups.webcamGroups.zipWithIndex.map { case (webcamGroup, index) =>
              Seq(
                Tab(
                  _.text     := webcamGroup.name,
                  _.selected := (webcamGroup == groups.getDefaultWebcamGroup),
                  WebcamGroupView(webcamGroup, webcamStates, slideshowControls, ImageOverlayView.showImageOverlay)
                )
              )
            }
          )
        )
      )
    )
  end apply
end MainView
