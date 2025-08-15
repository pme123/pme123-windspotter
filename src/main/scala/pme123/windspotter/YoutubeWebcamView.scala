package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js

object YoutubeWebcamView:

  def apply(
      webcam: Webcam,
      stateVar: Var[WebcamState],
      showImageOverlay: (
          String,
          Option[List[ImageData]],
          Option[Int],
          Option[ImageData => Unit]
      ) => Unit,
      slideshowControlVar: Var[Boolean]
  ): HtmlElement =

    val state = stateVar.signal

    div(
      className := "image-upload-section",

      // Webcam section (matching regular webcam structure)
      div(
        className := "upload-method webcam-section",
        div(
          className := "webcam-header",
          Title(
            className := "webcam-title",
            webcam.name
          )
        ),

        // YouTube webcam display (replacing webcam-image-section)
        div(
          className := "webcam-image-section",
          div(
            className := "webcam-image-container",
            div(
              className := "youtube-container",
              idAttr    := s"youtube-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}",
              onMountCallback(ctx =>
                val container = ctx.thisNode.ref
                dom.console.log(s"📺 Initializing YouTube webcam for ${webcam.name}")

                createYoutubeEmbed(container, webcam)

                dom.console.log(s"✅ YouTube webcam initialized for ${webcam.name}")
              )
            )
          )
        ),

        // Footer with webcam info (matching regular webcam structure)
        div(
          className := "webcam-footer",
          div(
            className := "footer-left",
            span("Youtube Live Stream")
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

  private def createYoutubeEmbed(container: dom.Element, webcam: Webcam): Unit =
    // Clear container
    container.innerHTML = ""

    // Extract YouTube video ID from URL
    val videoId = extractYoutubeVideoId(webcam.url)
    
    if (videoId.nonEmpty) {
      // Create YouTube iframe embed
      val iframe = dom.document.createElement("iframe").asInstanceOf[dom.HTMLIFrameElement]
      iframe.style.width = "100%"
      iframe.style.height = "533px"
      iframe.style.border = "none"
      iframe.style.borderRadius = "8px"
      iframe.style.overflow = "hidden"
      
      // YouTube embed URL with autoplay and other parameters
      val embedUrl = s"https://www.youtube.com/embed/${videoId}?autoplay=1&mute=1&controls=1&rel=0&modestbranding=1"
      iframe.src = embedUrl
      iframe.setAttribute("allowfullscreen", "true")
      iframe.setAttribute("allow", "autoplay; encrypted-media")
      
      // Add the iframe to the container
      container.appendChild(iframe)
      
      dom.console.log(s"✅ YouTube iframe created for ${webcam.name} with video ID: $videoId")
    } else {
      // Fallback: show error message
      val errorDiv = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
      errorDiv.className = "webcam-error"
      errorDiv.innerHTML = s"❌ Invalid YouTube URL: ${webcam.url}"
      container.appendChild(errorDiv)
      
      dom.console.log(s"❌ Could not extract YouTube video ID from: ${webcam.url}")
    }
  end createYoutubeEmbed

  private def extractYoutubeVideoId(url: String): String =
    // Handle different YouTube URL formats
    if (url.contains("youtube.com/watch?v=")) {
      val startIndex = url.indexOf("v=") + 2
      val endIndex = url.indexOf("&", startIndex)
      if (endIndex > startIndex) {
        url.substring(startIndex, endIndex)
      } else {
        url.substring(startIndex)
      }
    } else if (url.contains("youtu.be/")) {
      val startIndex = url.indexOf("youtu.be/") + 9
      val endIndex = url.indexOf("?", startIndex)
      if (endIndex > startIndex) {
        url.substring(startIndex, endIndex)
      } else {
        url.substring(startIndex)
      }
    } else if (url.contains("youtube.com/embed/")) {
      val startIndex = url.indexOf("embed/") + 6
      val endIndex = url.indexOf("?", startIndex)
      if (endIndex > startIndex) {
        url.substring(startIndex, endIndex)
      } else {
        url.substring(startIndex)
      }
    } else {
      // Assume the URL is just the video ID
      url
    }
  end extractYoutubeVideoId



end YoutubeWebcamView
