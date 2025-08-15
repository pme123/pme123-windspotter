package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js

import scala.scalajs.js.annotation.JSExportTopLevel

object Main:

  @JSExportTopLevel("main")
  def main(args: Array[String] = Array.empty): Unit =
    lazy val appContainer = dom.document.querySelector("#app")
    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",
      Bar(
        _.design := BarDesign.Header,
        _.slots.endContent := span(
          Link(
            _.href := "https://pme123.github.io/pme123-weather",
            _.target := LinkTarget._blank,
            "Weather"
          ),
          " | ",
          Link(
            _.href := "https://github.com/pme123/pme123-windspotter",
            _.target := LinkTarget._blank,
            "GitHub"
          )
        ),
        Title(_.size := TitleLevel.H4, "PME123 Windspotter")
      ),
      div(
        className := "main-content",
        MainView()
      ),
      div(
        className := "footer",
        p("Built with Scala.js + Laminar + UI5 Web Components")
      )
    )
end Main
