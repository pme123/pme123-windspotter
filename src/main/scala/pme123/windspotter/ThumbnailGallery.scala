package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object ThumbnailGallery {

  def apply(
    imageHistory: List[ImageData],
    selectedImage: Option[ImageData],
    onImageSelect: ImageData => Unit,
    showImageOverlay: (String, Option[List[ImageData]], Option[Int], Option[ImageData => Unit]) => Unit
  ): HtmlElement = {
    val isPlayingVar = Var(false)
    val playIndexVar = Var(0)

    div(
      className := "thumbnail-gallery",
      if (imageHistory.nonEmpty) {
          div(
            className := "thumbnail-container",
            div(
              className := "thumbnail-header",
              div(
                className := "thumbnail-grid",
                imageHistory.zipWithIndex.map { case (imageData, index) =>
                  div(
                    className := "thumbnail-item",
                    img(
                      src := imageData.dataUrl,
                      className := "thumbnail-image",
                      alt := s"Image ${index + 1}",
                      title := imageData.name,
                      onClick --> { _ =>
                        isPlayingVar.set(false) // Stop playing when manually selecting
                        onImageSelect(imageData)
                        // Open overlay with slideshow capability
                        dom.console.log(s"🖼️ Opening overlay for image ${index + 1}/${imageHistory.length}")
                        showImageOverlay(
                          imageData.dataUrl,
                          Some(imageHistory),
                          Some(index),
                          Some((newImage: ImageData) => {
                            dom.console.log(s"🔄 Overlay changed to: ${newImage.name}")
                            onImageSelect(newImage)
                          })
                        )
                      }
                    ),
                    p(
                      className := "thumbnail-time",
                      imageData.name.split("_").lastOption.getOrElse("").replace(".jpg", "")
                    )
                  )
                }
              ),
              button(
                className := "play-button",
                child <-- isPlayingVar.signal.map { isPlaying =>
                  if (isPlaying) "⏸️" else "▶️"
                },
                onClick --> { _ =>
                  val currentlyPlaying = isPlayingVar.now()
                  if (currentlyPlaying) {
                    // Stop playing
                    isPlayingVar.set(false)
                  } else {
                    // Start playing
                    if (imageHistory.nonEmpty) {
                      isPlayingVar.set(true)
                      playIndexVar.set(0)
                      startSlideshow(imageHistory, onImageSelect, isPlayingVar, playIndexVar)
                    }
                  }
                }
              )
            )
          )
      } else {
        emptyNode
      }
    )
  }

  private def startSlideshow(
    images: List[ImageData],
    onImageSelect: ImageData => Unit,
    isPlayingVar: Var[Boolean],
    playIndexVar: Var[Int]
  ): Unit = {
    dom.console.log(s"🎬 Starting slideshow with ${images.length} images")
    
    def playNext(): Unit = {
      if (isPlayingVar.now() && images.nonEmpty) {
        val currentIndex = playIndexVar.now()
        val nextIndex = (currentIndex + 1) % images.length
        
        // Show the current image
        onImageSelect(images(currentIndex))
        playIndexVar.set(nextIndex)
        
        dom.console.log(s"🎬 Playing image ${currentIndex + 1}/${images.length}")
        
        // Schedule next image after 2 seconds
        dom.window.setTimeout(() => {
          playNext()
        }, 2000)
      }
    }
    
    // Start the slideshow
    if (images.nonEmpty) {
      playNext()
    } else {
      dom.console.log("🎬 No images to play")
      isPlayingVar.set(false)
    }
  }
}
