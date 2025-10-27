package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.BooleanAsAttrPresenceCodec
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.DynamicImplicits.truthValue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object VideoWebcamView {

  def apply(
    webcam: Webcam,
    showImageOverlay: (String, Option[List[ImageData]], Option[Int], Option[ImageData => Unit]) => Unit
  ): HtmlElement = {

    div(
      className := "video-webcam-view",
      
      // Webcam title
      Title(
        _.size := TitleLevel.H3,
        className := "webcam-title",
        webcam.name
      ),
      
      // Video container
      div(
        className := "video-container",
        if (webcam.url.contains("youtube") || webcam.url.contains("youtu.be")) {
          // For YouTube videos, show a message and link
          div(
            className := "video-placeholder",
            p("🎥 Live Video Stream"),
            p("Click below to view the live stream:"),
            a(
              href := webcam.url,
              target := "_blank",
              className := "video-link",
              "Open Live Stream"
            )
          )
        } else if (webcam.url.contains(".m3u8") || webcam.url.contains("stream") || webcam.url.contains(".mp4")) {
          // For HLS/video streams, use HTML5 video element with HLS.js
          videoTag(
            idAttr := s"video-${webcam.name.replaceAll("[^a-zA-Z0-9]", "")}",
            className := "video-player",
            widthAttr := 800,
            heightAttr := 450,
            htmlAttr("autoplay", BooleanAsAttrPresenceCodec) := true,
            htmlAttr("muted", BooleanAsAttrPresenceCodec) := true,
            htmlAttr("controls", BooleanAsAttrPresenceCodec) := true,
            onMountCallback(ctx => {
              val video = ctx.thisNode.ref.asInstanceOf[dom.HTMLVideoElement]
              dom.console.log(s"🎬 Initializing video for ${webcam.name}")
              dom.console.log(s"📹 Video URL: ${webcam.url}")

              // Add event listeners for debugging
              video.addEventListener("loadstart", (_: dom.Event) => {
                dom.console.log(s"📥 Video load started for ${webcam.name}")
              })

              video.addEventListener("loadeddata", (_: dom.Event) => {
                dom.console.log(s"✅ Video data loaded for ${webcam.name}")
              })

              video.addEventListener("canplay", (_: dom.Event) => {
                dom.console.log(s"▶️ Video can play for ${webcam.name}")
              })

              video.addEventListener("error", (e: dom.Event) => {
                val videoError = video.error
                if (videoError != null) {
                  val errorCode = videoError.code
                  val errorMessage = errorCode match {
                    case 1 => "MEDIA_ERR_ABORTED - The video download was aborted"
                    case 2 => "MEDIA_ERR_NETWORK - A network error occurred"
                    case 3 => "MEDIA_ERR_DECODE - The video is corrupted or not supported"
                    case 4 => "MEDIA_ERR_SRC_NOT_SUPPORTED - The video format is not supported"
                    case _ => s"Unknown error code: $errorCode"
                  }
                  dom.console.log(s"❌ Video error for ${webcam.name}: $errorMessage")
                  dom.console.log(s"   Error details: code=$errorCode, message=${videoError}")
                } else {
                  dom.console.log(s"❌ Video error for ${webcam.name}: Unknown error")
                }
              })

              // Handle different video types
              if (webcam.url.contains(".mp4")) {
                // Direct MP4 video
                dom.console.log(s"🎥 Loading MP4 video for ${webcam.name}")
                video.src = webcam.url
                video.load() // Force reload
                video.play()
                dom.console.log(s"✅ MP4 video playback attempted for ${webcam.name}")
              } else if (webcam.url.contains(".m3u8")) {
                // HLS stream
                if (js.isUndefined(js.Dynamic.global.Hls)) {
                  dom.console.log(s"❌ HLS.js library not loaded for ${webcam.name}")
                  // Fallback: try direct video source
                  video.src = webcam.url
                  video.play()
                  dom.console.log(s"✅ Direct HLS playback attempted for ${webcam.name}")
                } else {
                  // Initialize HLS.js
                  try {
                    if (js.Dynamic.global.Hls.isSupported()) {
                      val hls = js.Dynamic.newInstance(js.Dynamic.global.Hls)()
                      hls.loadSource(webcam.url)
                      hls.attachMedia(video)
                      hls.on(js.Dynamic.global.Hls.Events.MANIFEST_PARSED, () => {
                        dom.console.log(s"✅ HLS manifest loaded for ${webcam.name}")
                        video.play()
                      })
                      hls.on(js.Dynamic.global.Hls.Events.ERROR, (event: js.Any, data: js.Any) => {
                        dom.console.log(s"❌ HLS error for ${webcam.name}:", data)
                      })
                    } else if (video.canPlayType("application/vnd.apple.mpegurl") != "") {
                      // Safari native HLS support
                      video.src = webcam.url
                      video.play()
                    } else {
                      dom.console.log(s"❌ HLS not supported for ${webcam.name}")
                    }
                  } catch {
                    case e: Exception =>
                      dom.console.log(s"❌ HLS initialization error for ${webcam.name}:", e.getMessage)
                  }
                }
              } else {
                // Generic video stream
                dom.console.log(s"🎬 Loading generic video stream for ${webcam.name}")
                video.src = webcam.url
                video.play()
                dom.console.log(s"✅ Generic video playback attempted for ${webcam.name}")
              }
            }),
            "Your browser does not support HLS video streams."
          )
          } else {
            // For other video sources, try iframe
            iframe(
              src := webcam.url,
              className := "video-iframe",
              width := "100%",
              height := "450"
            )
          }
      ),
      
      // Footer with webcam info
      div(
        className := "webcam-footer",
        div(
          className := "footer-left",
          span("🔴 Live Video Stream")
        ),
        a(
          className := "footer-right",
          href := webcam.footer,
          target := "_blank",
          webcam.footer
        )
      )
    )
  }
}
