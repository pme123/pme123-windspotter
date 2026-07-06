package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String] = Array.empty): Unit =
    lazy val appContainer = dom.document.querySelector("#app")
    ConfigFolderService.init()
    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",
      HeaderBar(),
      ConfigEditorDialog(),
      div(
        className := "main-content",
        // Rebuild the main view whenever another configuration is activated
        child <-- ConfigService.activeConfigVar.signal.map { config =>
          val visibleConfig = config.visible
          visibleConfig.groups match
            case Nil =>
              div(
                className := "empty-config-hint",
                s"The configuration '${config.name}' has no visible webcam groups. ",
                "Open the configuration editor (gear icon) to add or show some."
              )
            case firstGroup :: _ =>
              MainView(visibleConfig, Var(firstGroup))
        }
      ),
      footerTag(
        className := "z9-footer",
        div(
          className := "z9-footer-inner",
          div(className := "z9-copyright", "© 2026 z9nai GmbH // Alle Rechte vorbehalten"),
          div(
            className := "footer-right",
            DatenschutzDialog(),
            span(className := "footer-sep-mono", "//"),
            a(
              href      := "mailto:hallo@z9nai.ch",
              className := "z9-mail-icon",
              title     := "hallo@z9nai.ch",
              svg.svg(
                svg.viewBox        := "0 0 24 24",
                svg.fill           := "none",
                svg.stroke         := "currentColor",
                svg.strokeWidth    := "2",
                svg.strokeLineCap  := "round",
                svg.strokeLineJoin := "round",
                svg.width  := "16",
                svg.height := "16",
                svg.rect(svg.width := "20", svg.height := "16", svg.x := "2", svg.y := "4", svg.rx := "2"),
                svg.path(svg.d := "m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7")
              )
            )
          )
        )
      )
    )
end Main
