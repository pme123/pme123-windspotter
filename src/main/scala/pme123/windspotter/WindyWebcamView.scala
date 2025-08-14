package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js

object WindyWebcamView:

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
          ),
          Button(
            _.design := ButtonDesign.Transparent,
            _.icon := IconName.`refresh`,
            _.tooltip := "Reload Windy Player",
            onClick --> { _ =>
              reloadWindyPlayer(webcam)
            },
            "Reload"
          )
        ),

        // Windy webcam display (replacing webcam-image-section)
        div(
          className := "webcam-image-section",
          div(
            className := "webcam-image-container",
            div(
              className := "windy-container",
              idAttr    := s"windy-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}",
              onMountCallback(ctx =>
                val container = ctx.thisNode.ref
                dom.console.log(s"🌬️ Initializing Windy webcam for ${webcam.name}")

                createWindyEmbed(container, webcam)

                dom.console.log(s"✅ Windy webcam initialized for ${webcam.name}")
              )
            )
          )
        ),

        // Footer with webcam info (matching regular webcam structure)
        div(
          className := "webcam-footer",
          div(
            className := "footer-left",
            span("Live webcam - reload button refreshes player")
          ),
          webcam.overlayLink
            .map: overlayUrl =>
              a(
                className := "footer-center",
                href      := overlayUrl,
                target    := "_blank",
                "Go to Main Page"
              )
            .toSeq,
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

  private def createWindyEmbed(container: dom.Element, webcam: Webcam): Unit =
    // Clear container
    container.innerHTML = ""

    // Create an iframe that loads a minimal HTML page with just the Windy webcam
    val iframe = dom.document.createElement("iframe").asInstanceOf[dom.HTMLIFrameElement]
    iframe.style.width = "100%"
    iframe.style.height = "400px"
    iframe.style.border = "none"
    iframe.style.borderRadius = "8px"

    // Create the HTML content for the iframe with the Windy webcam
    val windyHtml = s"""
      <!DOCTYPE html>
      <html>
      <head>
        <script async type="text/javascript" src="https://webcams.windy.com/webcams/public/embed/v2/script/player.js"></script>
      </head>
      <body style="margin: 0; padding: 0; background: #f0f0f0;">
        <a name="windy-webcam-timelapse-player"
           data-id="${webcam.url}"
           data-play="live"
           data-loop="0"
           data-auto-play="0"
           data-force-full-screen-on-overlay-play="0"
           data-interactive="1"
           href="https://windy.com/webcams/${webcam.url}"
           target="_blank">
          ${webcam.name}
        </a>
      </body>
      </html>
    """

    // Set the iframe content
    iframe.setAttribute("srcdoc", windyHtml)

    // Add the iframe to the container
    container.appendChild(iframe)
  end createWindyEmbed

  private def reloadWindyPlayer(webcam: Webcam): Unit = {
    dom.console.log(s"🔄 Reloading Windy player for ${webcam.name}")

    val containerId = s"windy-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}"
    val container = dom.document.getElementById(containerId)

    if (container != null) {
      // Find the iframe in the container
      val iframe = container.querySelector("iframe").asInstanceOf[dom.HTMLIFrameElement]
      if (iframe != null) {
        // Reload the iframe by recreating it
        createWindyEmbed(container, webcam)
        dom.console.log(s"✅ Windy player iframe reloaded for ${webcam.name}")
      } else {
        dom.console.log(s"❌ Windy iframe not found for ${webcam.name}")
      }
    } else {
      dom.console.log(s"❌ Windy container not found for ${webcam.name}")
    }
  }

end WindyWebcamView
