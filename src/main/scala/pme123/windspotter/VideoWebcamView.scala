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
        } else if (webcam.url.contains(".m3u8") || webcam.url.contains("stream")) {
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
              dom.console.log(s"🎬 Initializing HLS for ${webcam.name}")

              // Check if HLS.js is available
              if (js.isUndefined(js.Dynamic.global.Hls)) {
                dom.console.log(s"❌ HLS.js library not loaded for ${webcam.name}")
                // Fallback: try direct video source
                video.src = webcam.url
                video.play()
                dom.console.log(s"✅ Direct video playback attempted for ${webcam.name}")
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
