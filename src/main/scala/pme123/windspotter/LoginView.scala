package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object LoginView:

  def apply(): HtmlElement =
    dom.console.log("🔐 Creating LoginView")
    div(
      className := "login-container",
      Card(
        _.slots.header := CardHeader(
          _.titleText := "Authentication Required",
          _.subtitleText := "Please sign in with your GitHub account to access the application"
        ),
        div(
          className := "login-content",
          div(
            className := "login-description",
            p("This application requires GitHub authentication to access wind condition data and personalized features."),
            p("Your GitHub account will be used only for authentication purposes.")
          ),
          div(
            className := "login-actions",
            Button(
              _.design := ButtonDesign.Emphasized,
              _.icon := IconName.`source-code`,
              "Sign in with GitHub",
              onClick --> { _ =>
                AuthService.login()
              }
            ),
            div(
              className := "demo-section",
              p(
                className := "demo-text",
                "Or try the demo mode:"
              ),
              Button(
                _.design := ButtonDesign.Default,
                _.icon := IconName.`play`,
                "Demo Login",
                onClick --> { _ =>
                  AuthService.demoLogin()
                }
              )
            )
          )
        )
      )
    )

end LoginView
