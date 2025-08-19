package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object WebcamView:

  def apply(
      webcam: Webcam,
      stateVar: Var[WebcamState],
      showImageOverlay: (
          String,
          Option[List[ImageData]],
          Option[Int],
          Option[ImageData => Unit]
      ) => Unit,
      slideshowControlVar: Var[Boolean] = Var(false),
      loadingEnabledVar: Var[Boolean] = Var(true)
  ): HtmlElement =

    // Handle different webcam types
    webcam.webcamType match
    case VideoWebcam   =>
      return VideoWebcamView(webcam, showImageOverlay, loadingEnabledVar)
    case WindyWebcam   =>
      return WindyWebcamView(
        webcam,
        stateVar,
        showImageOverlay,
        slideshowControlVar,
        loadingEnabledVar
      )
    case YoutubeWebcam =>
      return YoutubeWebcamView(
        webcam,
        stateVar,
        showImageOverlay,
        slideshowControlVar,
        loadingEnabledVar
      )
    case ScrapedWebcam =>
      return ScrapedWebcamView(
        webcam,
        stateVar,
        showImageOverlay,
        slideshowControlVar,
        loadingEnabledVar
      )
    case ImageWebcam   =>
      // Continue with existing image webcam logic
    end match


    div(
      className := "image-upload-section",

      // Auto-load when loading is enabled and no image exists yet
      onMountCallback { ctx =>
        loadingEnabledVar.signal.foreach { isEnabled =>
          val currentState = stateVar.now()
          if (isEnabled && currentState.imageHistory.isEmpty && !currentState.isAutoRefresh) {
            dom.console.log(s"🔄 Auto-loading ${webcam.name} on loading enable")
            WebcamService.loadWebcamImage(webcam, stateVar, loadingEnabledVar)
            // Also start auto-refresh for regular webcams
            dom.window.setTimeout(
              () => {
                dom.console.log(s"🚀 Starting automatic refresh for ${webcam.name}...")
                WebcamService.startAutoRefresh(webcam, stateVar)
              },
              100
            )
          }
        }(ctx.owner)
      },

      // Webcam section
      div(
        className := "upload-method webcam-section",
        div(
          className := "webcam-header",
          div(
            className := "webcam-title-row",
            Title(
              className := "webcam-title",
              webcam.name
            ),
            // Custom reload button using same style as WindyWebcamView
            child <-- loadingEnabledVar.signal.map { loadingEnabled =>
              div(
                className := (if loadingEnabled then "webcam-reload-button-custom"
                              else "webcam-reload-button-disabled"),
                title     := (if loadingEnabled then "Load current image and add to thumbnails"
                          else "Loading disabled - enable loading toggle first"),
                onClick --> { _ =>
                  if loadingEnabled then
                    dom.console.log(s"🔄 Manual reload requested for ${webcam.name}")
                    WebcamService.loadWebcamImage(webcam, stateVar, loadingEnabledVar)
                  else
                    dom.console.log(s"⚫ Reload blocked - loading disabled for ${webcam.name}")
                },
                Icon(_.name := IconName.`refresh`)
              )
            }
          )
        ),

        // Webcam image display
        div(
          className := "webcam-image-section",
          child <-- loadingEnabledVar.signal.combineWith(stateVar.signal.map(_.selectedImage)).map {
            case (false, _)              =>
              // Loading disabled - show placeholder
              div(
                className := "webcam-disabled-placeholder",
                div(
                  className := "disabled-content",
                  div(
                    className := "disabled-icon",
                    "⚫"
                  ),
                  div(
                    className := "disabled-text",
                    "Loading Disabled"
                  ),
                  div(
                    className := "disabled-subtitle",
                    "Enable loading toggle to view webcam"
                  )
                )
              )
            case (true, Some(imageData)) =>
              // Loading enabled and image available
              div(
                className := "webcam-image-container",
                img(
                  src       := imageData.dataUrl,
                  className := "webcam-image",
                  alt       := s"${webcam.name} feed",
                  onClick --> { _ =>
                    val state        = stateVar.now()
                    val history      = state.imageHistory
                    val currentIndex = history.indexWhere(_.dataUrl == imageData.dataUrl)
                    val index        = if currentIndex >= 0 then Some(currentIndex) else None
                    showImageOverlay(
                      imageData.dataUrl,
                      if history.nonEmpty then Some(history) else None,
                      index,
                      if history.nonEmpty then
                        Some((newImage: ImageData) =>
                          val currentState = stateVar.now()
                          stateVar.set(currentState.copy(selectedImage = Some(newImage)))
                        )
                      else None
                    )
                  }
                )
              )
            case (true, None)            =>
              // Loading enabled but no image yet
              div(
                className := "webcam-loading",
                div(
                  className := "loading-spinner"
                )
              )
          }
        ),

        // Thumbnail gallery component
        child <-- loadingEnabledVar.signal.combineWith(stateVar.signal).map {
          (loadingEnabled, state) =>
            if loadingEnabled && state.imageHistory.nonEmpty then
              ThumbnailGallery(
                state.imageHistory,
                state.selectedImage,
                (newImage: ImageData) =>
                  val currentState = stateVar.now()
                  stateVar.set(currentState.copy(selectedImage = Some(newImage)))
                ,
                showImageOverlay,
                slideshowControlVar
              )
            else
              emptyNode
        },

        // Footer with webcam info
        div(
          className := "webcam-footer",
          div(
            className := "footer-left",
            child <-- stateVar.signal.map(_.lastUpdate).map:
              case Some(time) =>
                val nextLoadTime = calculateNextLoadTime(time, webcam.reloadInMin)
                span(
                  s"Next load: $nextLoadTime",
                  webcam.mainPageLink
                    .map: overlayUrl =>
                      span(
                        " | Better view: ",
                        a(
                          className := "footer-left",
                          href      := overlayUrl,
                          target    := "_blank",
                          "Go to Main Page"
                        )
                      )
                    .toSeq
                )
              case None       =>
                span("Loading...")
          ),
          a(
            className := "footer-right",
            href      := webcam.footer,
            target    := "_blank",
            webcam.footer
          )
        )
      )
    )
  end apply

  private def calculateNextLoadTime(lastUpdateTime: String, reloadInMin: Int): String =
    try
      // Parse the last update time (format: "HH:MM")
      val parts   = lastUpdateTime.split(":")
      val hours   = parts(0).toInt
      val minutes = parts(1).toInt

      // Calculate next load time
      val totalMinutes = hours * 60 + minutes + reloadInMin
      val nextHours    = (totalMinutes / 60) % 24
      val nextMinutes  = totalMinutes        % 60

      f"$nextHours%02d:$nextMinutes%02d"
    catch
      case _: Exception => "Unknown"
end WebcamView
