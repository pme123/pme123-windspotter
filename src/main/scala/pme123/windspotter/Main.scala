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

    // Initialize authentication
    AuthService.initialize()

    renderOnDomContentLoaded(appContainer, page)
  end main

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",
      HeaderBar(),
      child <-- AuthService.isAuthenticatedVar.signal.map { isAuthenticated =>
        dom.console.log(s"🔐 Rendering based on auth state: $isAuthenticated")
        if (isAuthenticated) {
          dom.console.log("🔐 Showing MainView")
          div(
            className := "main-content",
            MainView()
          )
        } else {
          dom.console.log("🔐 Showing LoginView")
          div(
            className := "main-content login-required",
            LoginView()
          )
        }
      },
      div(
        className := "footer",
        p("Built with Scala.js + Laminar + UI5 Web Components")
      )
    )
end Main
