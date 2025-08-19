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

  // Initialize shared state for the entire app
  private val selectedLakeVar = Var(WebcamData.getDefaultLake)
  private val lakeLoadingStates = WebcamData.lakes.map { lake =>
    lake -> Var(false)
  }.toMap

  // Log initial state
  dom.console.log(s"🏔️ Initial selected lake: ${selectedLakeVar.now().name}")
  dom.console.log(s"🗺️ Available lakes: ${WebcamData.lakes.map(_.name).mkString(", ")}")

  private lazy val page =
    div(
      width := "100%",
      height := "100%",
      className := "app-container",
      HeaderBar(),
      child <-- AuthService.isAuthenticatedVar.signal.combineWith(AuthService.isAuthorizedVar.signal).map {
        case (isAuthenticated, isAuthorized) =>
          dom.console.log(s"🔐 Rendering based on auth state: $isAuthenticated, authorized: $isAuthorized")
          if (!isAuthenticated) {
            dom.console.log("🔐 Showing LoginView")
            div(
              className := "main-content login-required",
              LoginView()
            )
          } else if (!isAuthorized) {
            dom.console.log("🔐 Showing UnauthorizedView")
            div(
              className := "main-content login-required",
              UnauthorizedView()
            )
          } else {
            dom.console.log("🔐 Showing MainView")
            div(
              className := "main-content",
              MainView(selectedLakeVar, lakeLoadingStates)
            )
          }
      },
      div(
        className := "footer",
        p("Built with Scala.js + Laminar + UI5 Web Components")
      )
    )
end Main
