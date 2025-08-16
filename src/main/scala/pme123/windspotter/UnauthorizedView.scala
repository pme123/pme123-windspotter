package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object UnauthorizedView:

  def apply(): HtmlElement =
    dom.console.log("🔐 Creating UnauthorizedView")
    div(
      className := "unauthorized-container",
      Card(
        _.slots.header := CardHeader(
          _.titleText := "Access Denied",
          _.subtitleText := "You are not authorized to access this application"
        ),
        div(
          className := "unauthorized-content",
          div(
            className := "unauthorized-description",
            p("You have successfully authenticated with GitHub, but your account is not authorized to access this application."),
            p("If you believe this is an error, please contact the application administrator."),
            child <-- AuthService.currentUserVar.signal.map {
              case Some(user) =>
                p(
                  className := "user-info-text",
                  s"Authenticated as: ${user.name.getOrElse(user.login)} (${user.login})"
                )
              case None =>
                emptyNode
            }
          ),
          div(
            className := "unauthorized-actions",
            Button(
              _.design := ButtonDesign.Default,
              _.icon := IconName.`log`,
              "Sign Out",
              onClick --> { _ =>
                AuthService.logout()
              }
            )
          )
        )
      )
    )

end UnauthorizedView
