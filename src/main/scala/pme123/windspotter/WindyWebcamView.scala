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
          div(
            className := "webcam-title-row",
            Title(
              className := "webcam-title",
              webcam.name
            ),
            // Custom button using UI5 Icon but with custom styling
            div(
              className := "webcam-reload-button-custom",
              title     := "Reload Windy Player",
              onClick --> { _ =>
                reloadWindyPlayer(webcam)
              },
              Icon(_.name := IconName.`refresh`)
            ),
            // Capture button for adding current view to thumbnails
            div(
              className := "webcam-capture-button-custom",
              title     := "Capture current view and add to thumbnails",
              onClick --> { _ =>
                dom.console.log(s"🔄 Manual capture requested for Windy webcam ${webcam.name}")
                WebcamService.captureWindyWebcamImage(webcam, stateVar)
              },
              Icon(_.name := IconName.`camera`)
            )
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
            span("Use reload button to refresh"),
            webcam.mainPageLink
              .map: videoUrl =>
                span(
                  " | For live video: ",
                  a(
                    className := "footer-left",
                    href      := videoUrl,
                    target    := "_blank",
                    "Go to Main Page"
                  )
                )
              .toSeq
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

  private def createWindyEmbed(container: dom.Element, webcam: Webcam): Unit =
    // Clear container
    container.innerHTML = ""

    // Create an iframe that loads a minimal HTML page with just the Windy webcam
    val iframe = dom.document.createElement("iframe").asInstanceOf[dom.HTMLIFrameElement]
    iframe.style.width = "100%"
    iframe.style.height = "533px"
    iframe.style.border = "none"
    iframe.style.borderRadius = "8px"
    iframe.style.overflow = "hidden"

    // Create the HTML content for the iframe with the Windy webcam
    val windyHtml =
      s"""
      <!DOCTYPE html>
      <html>
      <head>
        <script async type="text/javascript" src="https://webcams.windy.com/webcams/public/embed/v2/script/player.js"></script>
      </head>
      <body style="margin: 0; padding: 0; background: #f0f0f0; height: 100%">
        <a name="windy-webcam-timelapse-player"
           data-id="${webcam.url}"
           data-play="day"
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

  private def reloadWindyPlayer(webcam: Webcam): Unit =
    dom.console.log(s"🔄 Reloading Windy player for ${webcam.name}")

    val containerId = s"windy-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}"
    val container   = dom.document.getElementById(containerId)

    if container != null then
      // Find the iframe in the container
      val iframe = container.querySelector("iframe").asInstanceOf[dom.HTMLIFrameElement]
      if iframe != null then
        // Reload the iframe by recreating it
        createWindyEmbed(container, webcam)
        dom.console.log(s"✅ Windy player iframe reloaded for ${webcam.name}")
      else
        dom.console.log(s"❌ Windy iframe not found for ${webcam.name}")
      end if
    else
      dom.console.log(s"❌ Windy container not found for ${webcam.name}")
    end if
  end reloadWindyPlayer

end WindyWebcamView
