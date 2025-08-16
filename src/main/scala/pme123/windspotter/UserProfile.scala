package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object UserProfile:

  def apply(): HtmlElement =
    div(
      className := "user-profile",
      child <-- AuthService.currentUserVar.signal.map {
        case Some(user) =>
          div(
            className := "user-info",
            span(
              className := "user-name",
              user.name.getOrElse(user.login)
            ),/*
            " | ",
            Button(
              _.design := ButtonDesign.Transparent,
              _.icon := IconName.`log`,
              "Sign Out",
              onClick --> { _ =>
                AuthService.logout()
              }
            ),
            " | ",
            Button(
              _.design := ButtonDesign.Transparent,
              _.icon := IconName.`refresh`,
              "Force Logout",
              onClick --> { _ =>
                AuthService.forceLogout()
              }
            ) */
          )
        case None =>
          emptyNode // No sign in link when not authenticated
      }
    )

end UserProfile
