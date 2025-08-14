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

                // Add overlay functionality with delayed setup to wait for Windy script
                webcam.overlayLink match
                case Some(overlayUrl) =>
                  setupOverlayHandler(container, webcam, overlayUrl)
                case None             =>
                  dom.console.log(s"ℹ️ No overlay link configured for ${webcam.name}")
                end match

                // Start auto-refresh if configured
                if webcam.reloadInMin > 0 then
                  startWindyAutoRefresh(webcam, stateVar)

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
            span(s"Reloads every ${webcam.reloadInMin} minutes")
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
    windyLink.setAttribute("data-auto-play", "1")
    windyLink.setAttribute("data-force-full-screen-on-overlay-play", "0")
    windyLink.setAttribute("data-interactive", "1")
    windyLink.setAttribute("target", "_blank")
    windyLink.textContent = webcam.name

    // Add the link to the container
    container.appendChild(windyLink)
  end createWindyEmbed

  private def refreshWindyWebcam(webcam: Webcam, stateVar: Var[WebcamState]): Unit =
    dom.console.log(s"🔄 Refreshing Windy webcam: ${webcam.name}")

    val containerId = s"windy-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}"
    val container   = dom.document.getElementById(containerId)

    if container != null then
      // Recreate the Windy embed to force refresh
      createWindyEmbed(container, webcam)

      // Update last refresh time
      val now          = new js.Date()
      val timeString   = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
      val currentState = stateVar.now()
      stateVar.set(currentState.copy(lastUpdate = Some(timeString)))

      dom.console.log(s"✅ Windy webcam refreshed: ${webcam.name}")
    else
      dom.console.log(s"❌ Windy container not found for ${webcam.name}")
    end if
  end refreshWindyWebcam

  private def setupOverlayHandler(
      container: dom.Element,
      webcam: Webcam,
      overlayUrl: String
  ): Unit =
    dom.console.log(s"🔧 Setting up overlay handler for ${webcam.name}")

    def trySetupHandler(attempts: Int): Unit =
      if attempts > 0 then
        // Look for any clickable elements in the container
        val clickableElements = container.querySelectorAll("a, iframe, div, *")

        if clickableElements.length > 0 then
          dom.console.log(
            s"📍 Found ${clickableElements.length} elements in container, setting up overlay handler"
          )

          // Add event listener to container with capture phase
          container.addEventListener(
            "click",
            (e: dom.Event) =>
              dom.console.log(s"🎯 Click intercepted on ${webcam.name} container!")
              e.preventDefault()
              e.stopPropagation()
              e.stopImmediatePropagation()
              dom.console.log(s"🌐 Opening overlay for ${webcam.name}: $overlayUrl")
              WebOverlayView.showWebOverlay(webcam.name, overlayUrl)
            ,
            useCapture = true
          ) // Use capture phase to intercept before other handlers

          dom.console.log(s"✅ Overlay handler set up for ${webcam.name}")
        else
          // Windy content not loaded yet, try again
          dom.console.log(s"⏳ Windy content not ready, retrying... (${attempts} attempts left)")
          dom.window.setTimeout(() => trySetupHandler(attempts - 1), 500)
        end if
      else
        dom.console.log(
          s"❌ Failed to set up overlay handler for ${webcam.name} after multiple attempts"
        )

    // Start trying to set up the handler
    trySetupHandler(10) // Try up to 10 times with 500ms intervals
  end setupOverlayHandler

  private def startWindyAutoRefresh(webcam: Webcam, stateVar: Var[WebcamState]): Unit =
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(isAutoRefresh = true))

    def scheduleNext(): Unit =
      dom.window.setTimeout(
        () =>
          val state = stateVar.now()
          if state.isAutoRefresh then
            refreshWindyWebcam(webcam, stateVar)
            scheduleNext()
        ,
        webcam.reloadInMin * 60 * 1000
      ) // Convert minutes to milliseconds

    scheduleNext()
    dom.console.log(s"🔄 Started auto-refresh for ${webcam.name} (every ${webcam.reloadInMin} min)")
  end startWindyAutoRefresh
end WindyWebcamView
