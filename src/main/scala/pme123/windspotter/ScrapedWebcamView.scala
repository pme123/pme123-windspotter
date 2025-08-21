package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.Future

object ScrapedWebcamView:

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

    val state = stateVar.signal



    div(
      className := "image-upload-section",

      // Auto-scrape when loading is enabled and manage periodic scraping
      onMountCallback { ctx =>
        loadingEnabledVar.signal.foreach { isEnabled =>
          if (isEnabled) {
            if (stateVar.now().selectedImage.isEmpty) {
              dom.console.log(s"🔄 Auto-scraping ${webcam.name} on loading enable")
              ScrapedWebcamService.scrapeAndLoadImage(webcam, stateVar, loadingEnabledVar)
            }
            // Start periodic scraping
            ScrapedWebcamService.startAutoScraping(webcam, stateVar, loadingEnabledVar)
          } else {
            // Stop periodic scraping when loading is disabled
            ScrapedWebcamService.stopAutoScraping(webcam)
          }
        }(using ctx.owner)
      },

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
            // Custom reload button using same style as regular WebcamView
            child <-- loadingEnabledVar.signal.map { loadingEnabled =>
              div(
                className := (if loadingEnabled then "webcam-reload-button-custom" else "webcam-reload-button-disabled"),
                title     := (if loadingEnabled then "Scrape current image and add to thumbnails" else "Loading disabled - enable loading toggle first"),
                onClick --> { _ =>
                  if loadingEnabled then
                    dom.console.log(s"🔄 Manual scrape requested for webcam ${webcam.name}")
                    ScrapedWebcamService.scrapeAndLoadImage(webcam, stateVar, loadingEnabledVar)
                  else
                    dom.console.log(s"⚫ Scrape blocked - loading disabled for ${webcam.name}")
                },
                Icon(_.name := IconName.`refresh`)
              )
            }
          )
        ),

        // Webcam image display (matching regular webcam structure)
        div(
          className := "webcam-image-section",
          child <-- loadingEnabledVar.signal.combineWith(stateVar.signal.map(_.selectedImage)).map {
            case (false, _) =>
              // Loading disabled - show placeholder
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
                    "Scraping Disabled"
                  ),
                  div(
                    className := "disabled-subtitle",
                    "Enable loading toggle to scrape webcam images"
                  )
                )
              )
            case (true, Some(imageData)) =>
              // Loading enabled and image available
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
            case (true, None) =>
              // Loading enabled but no image yet
              div(
                className := "webcam-loading",
                div(
                  className := "loading-spinner"
                )
              )
          }
        ),

        // Thumbnail gallery component (using the same component as regular webcam)
        child <-- loadingEnabledVar.signal.combineWith(stateVar.signal).map { (loadingEnabled, state) =>
          if loadingEnabled && state.imageHistory.nonEmpty then
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

        // Footer with webcam info (matching regular webcam structure)
        div(
          className := "webcam-footer",
          div(
            className := "footer-left",
            child <-- stateVar.signal.map(_.lastUpdate).map:
              case Some(time) =>
                span(
                  time,
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
                span("Click refresh to scrape image")
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

end ScrapedWebcamView
