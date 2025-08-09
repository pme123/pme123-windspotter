package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object WebcamView {

  def apply(
    webcam: Webcam,
    stateVar: Var[WebcamState],
    showImageOverlay: (String, Option[List[ImageData]], Option[Int], Option[ImageData => Unit]) => Unit
  ): HtmlElement = {

    // Start auto-refresh immediately
    dom.window.setTimeout(() => {
      dom.console.log(s"🚀 Starting automatic loading for ${webcam.name}...")
      WebcamService.startAutoRefresh(webcam, stateVar)
    }, 100)

    Card(
      className := "webcam-card",
      div(
        className := "card-content",
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
                    p(s"🔄 Loading ${webcam.name}..."),
                    p("The live webcam feed will appear here automatically.")
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
                  showImageOverlay
                )
              } else {
                emptyNode
              }
            },
            
            // Status display
            div(
              className := "webcam-status",
              child <-- Signal.combine(
                stateVar.signal.map(_.isAutoRefresh),
                stateVar.signal.map(_.lastUpdate),
                stateVar.signal.map(_.currentUrl)
              ).map { 
                case (isActive, lastUpdate, currentUrl) =>
                  if (isActive) {
                    div(
                      className := "status-active",
                      s"🟢 Auto-refresh active (every ${webcam.reloadInMin} min)",
                      lastUpdate.map(time => p(className := "last-update", s"Last update: $time")).getOrElse(emptyNode),
                      currentUrl.map(url => 
                        div(
                          className := "current-url",
                          p(className := "url-label", "Current URL:"),
                          p(className := "url-text", url)
                        )
                      ).getOrElse(emptyNode)
                    )
                  } else if (lastUpdate.isDefined || currentUrl.isDefined) {
                    div(
                      className := "status-inactive",
                      "⚫ Auto-refresh stopped",
                      lastUpdate.map(time => p(className := "last-update", s"Last update: $time")).getOrElse(emptyNode),
                      currentUrl.map(url => 
                        div(
                          className := "current-url",
                          p(className := "url-label", "Current URL:"),
                          p(className := "url-text", url)
                        )
                      ).getOrElse(emptyNode)
                    )
                  } else {
                    div(className := "status-inactive", "⚫ No image loaded")
                  }
              }
            ),
            
            // Footer with webcam info
            div(
              className := "webcam-footer",
              a(
                href := webcam.url,
                target := "_blank",
                s"${webcam.url.split("/").take(3).mkString("/")} (Auto-updates every ${webcam.reloadInMin} minutes)"
              )
            )
          )
        )
      )
    )
  }
}
