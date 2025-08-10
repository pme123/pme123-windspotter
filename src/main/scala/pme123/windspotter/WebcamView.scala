package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object WebcamView {

  def apply(
    webcam: Webcam,
    stateVar: Var[WebcamState],
    showImageOverlay: (String, Option[List[ImageData]], Option[Int], Option[ImageData => Unit]) => Unit,
    slideshowControlVar: Var[Boolean] = Var(false)
  ): HtmlElement = {

    // Only start auto-refresh if this webcam doesn't have any images yet
    val currentState = stateVar.now()
    if (currentState.imageHistory.isEmpty && !currentState.isAutoRefresh) {
      dom.window.setTimeout(() => {
        dom.console.log(s"🚀 Starting automatic loading for ${webcam.name}...")
        WebcamService.startAutoRefresh(webcam, stateVar)
      }, 100)
    } else {
      dom.console.log(s"📋 ${webcam.name} already has ${currentState.imageHistory.length} images, not reloading")
    }
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
                        val state = stateVar.now()
                        val history = state.imageHistory
                        val currentIndex = history.indexWhere(_.dataUrl == imageData.dataUrl)
                        val index = if (currentIndex >= 0) Some(currentIndex) else None
                        showImageOverlay(
                          imageData.dataUrl,
                          if (history.nonEmpty) Some(history) else None,
                          index,
                          if (history.nonEmpty) Some((newImage: ImageData) => {
                            val currentState = stateVar.now()
                            stateVar.set(currentState.copy(selectedImage = Some(newImage)))
                          }) else None
                        )
                      }
                    )
                  )
                case None =>
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
              if (state.imageHistory.nonEmpty) {
                ThumbnailGallery(
                  state.imageHistory,
                  state.selectedImage,
                  (newImage: ImageData) => {
                    val currentState = stateVar.now()
                    stateVar.set(currentState.copy(selectedImage = Some(newImage)))
                  },
                  showImageOverlay,
                  slideshowControlVar
                )
              } else {
                emptyNode
              }
            },

            // Footer with webcam info
            div(
              className := "webcam-footer",
              a(
                href := webcam.footer,
                target := "_blank",
                webcam.footer
              )
            )
          )
        )
  }
}
