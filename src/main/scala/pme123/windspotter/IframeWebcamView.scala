package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object IframeWebcamView:

  def apply(
      webcam: Webcam,
      stateVar: Var[WebcamState],
      showImageOverlay: (
          String,
          Option[List[ImageData]],
          Option[Int],
          Option[ImageData => Unit]
      ) => Unit,
      slideshowControlVar: Var[Boolean],
      loadingEnabledVar: Var[Boolean] = Var(true)
  ): HtmlElement =

    // Reactive variable to control iframe reloading
    val iframeSrcVar = Var(webcam.url)

    div(
      className := "image-upload-section",

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
            // Refresh button for iframe webcams
            child <-- loadingEnabledVar.signal.map { loadingEnabled =>
              div(
                className := (if loadingEnabled then "webcam-reload-button-custom"
                              else "webcam-reload-button-disabled"),
                title := (if loadingEnabled then "Reload webcam iframe"
                          else "Loading disabled - enable loading toggle first"),
                onClick --> { _ =>
                  if loadingEnabled then
                    dom.console.log(s"🔄 Manual iframe reload requested for ${webcam.name}")
                    // Force iframe reload by updating the src with a timestamp
                    val baseUrl = webcam.url
                    val separator = if baseUrl.contains("?") then "&" else "?"
                    val newSrc = s"$baseUrl${separator}t=${System.currentTimeMillis()}"
                    iframeSrcVar.set(newSrc)
                  else
                    dom.console.log(s"⚫ Iframe reload blocked - loading disabled for ${webcam.name}")
                },
                Icon(_.name := IconName.`refresh`)
              )
            }
          )
        ),

        // Webcam iframe display
        div(
          className := "webcam-image-section",
          child <-- loadingEnabledVar.signal.map { loadingEnabled =>
            if (loadingEnabled) {
              div(
                className := "webcam-image-container",
                iframe(
                  src <-- iframeSrcVar.signal,
                  className := "webcam-iframe",
                  width := "100%",
                  styleAttr := "border: none; border-radius: 8px; aspect-ratio: 16/9; min-height: 400px; max-height: 70vh; width: 100%; display: block;",
                  title := s"${webcam.name} Webcam",
                  onClick --> { _ =>
                    // Show webcam in overlay for fullscreen view
                    WebOverlayView.showWebOverlay(webcam.name, iframeSrcVar.now())
                  }
                )
              )
            } else {
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
            }
          }
        ),

        // Footer with webcam info (matching WebcamView structure)
        div(
          className := "webcam-footer",
          div(
            className := "footer-left",
            span(
              "Live webcam feed",
              webcam.mainPageLink
                .map { overlayUrl =>
                  span(
                    " | Better view: ",
                    a(
                      className := "footer-left",
                      href := overlayUrl,
                      target := "_blank",
                      "Go to Main Page"
                    )
                  )
                }
                .toSeq
            )
          ),
          a(
            className := "footer-right",
            href := webcam.footer,
            target := "_blank",
            webcam.footer
          )
        )
      )
    )
