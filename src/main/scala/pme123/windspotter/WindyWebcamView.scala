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
            span("Live webcam - reload button refreshes page")
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

    // Create the Windy webcam link element with hardcoded attributes
    val windyLink = dom.document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]
    windyLink.setAttribute("name", "windy-webcam-timelapse-player")
    windyLink.setAttribute("data-id", webcam.url) // webcam.url contains the Windy ID
    windyLink.setAttribute("data-play", "live")
    windyLink.setAttribute("data-loop", "0")
    windyLink.setAttribute("data-auto-play", "0")
    windyLink.setAttribute("data-force-full-screen-on-overlay-play", "0")
    windyLink.setAttribute("data-interactive", "1")
    windyLink.setAttribute("href", s"https://windy.com/webcams/${webcam.url}")
    windyLink.setAttribute("target", "_blank")
    windyLink.textContent = webcam.name

    // Add the link to the container
    container.appendChild(windyLink)
  end createWindyEmbed

  private def reloadWindyPlayer(webcam: Webcam): Unit = {
    dom.console.log(s"🔄 Reloading Windy player for ${webcam.name}")

    // Since recreating the embed doesn't work well, let's just reload the page
    // This is the most reliable way to refresh the Windy player
    dom.window.location.reload()
  }

end WindyWebcamView
