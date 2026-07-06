package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom

object MainView:

  def apply(
    config: WebcamConfig,
    selectedWebcamGroupVar: Var[WebcamGroup]
  ): HtmlElement =
    // Initialize webcam states and slideshow controls for all webcams
    val allWebcams   = config.groups.flatMap(_.webcams).distinct
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
      // Tab bar
      div(
        className := "tabs",
        config.groups.map { webcamGroup =>
          button(
            className <-- selectedWebcamGroupVar.signal.map(sel =>
              if sel == webcamGroup then "tab active" else "tab"
            ),
            onClick --> { _ => selectedWebcamGroupVar.set(webcamGroup) },
            span(className := "tab-dot ok"),
            span(className := "tab-name", webcamGroup.name)
          )
        }
      ),
      // Tab content panel
      div(
        className := "weather-container",
        child <-- selectedWebcamGroupVar.signal.map { webcamGroup =>
          WebcamGroupView(webcamGroup, webcamStates, slideshowControls, ImageOverlayView.showImageOverlay)
        }
      )
    )
  end apply
end MainView
