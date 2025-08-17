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
      slideshowControlVar: Var[Boolean] = Var(false)
  ): HtmlElement =

    // Handle different webcam types
    webcam.webcamType match
    case VideoWebcam   =>
      return VideoWebcamView(webcam, showImageOverlay)
    case WindyWebcam   =>
      return WindyWebcamView(webcam, stateVar, showImageOverlay, slideshowControlVar)
    case YoutubeWebcam =>
      return YoutubeWebcamView(webcam, stateVar, showImageOverlay, slideshowControlVar)
    case ImageWebcam   =>
      // Continue with existing image webcam logic
    end match

    // Only start auto-refresh if this webcam doesn't have any images yet
    val currentState = stateVar.now()
    if currentState.imageHistory.isEmpty && !currentState.isAutoRefresh then
      dom.window.setTimeout(
        () =>
          dom.console.log(s"🚀 Starting automatic loading for ${webcam.name}...")
          WebcamService.startAutoRefresh(webcam, stateVar)
        ,
        100
      )
    else
      dom.console.log(
        s"📋 ${webcam.name} already has ${currentState.imageHistory.length} images, not reloading"
      )
    end if
    div(
      className := "image-upload-section",

      // Webcam section
      div(
        className := "upload-method webcam-section",
        div(
          className := "webcam-header",
          Title(
            className := "webcam-title",
            webcam.name
          )
        ),

        // Webcam image display
        div(
          className := "webcam-image-section",
          child <-- stateVar.signal.map(_.selectedImage).map {
            case Some(imageData) =>
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
            case None            =>
              div(
                className := "webcam-loading",
                div(
                  className := "loading-spinner"
                )
              )
          }
        ),

        // Thumbnail gallery component
        child <-- stateVar.signal.map { state =>
          if state.imageHistory.nonEmpty then
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
