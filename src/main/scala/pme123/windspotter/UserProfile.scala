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
            img(
              src := user.avatar_url,
              className := "user-avatar",
              alt := s"${user.login} avatar"
            ),
            span(
              className := "user-name",
              user.name.getOrElse(user.login)
            ),
            Button(
              _.design := ButtonDesign.Transparent,
              _.icon := IconName.`log`,
              _.tooltip := "Sign out",
              onClick --> { _ =>
                AuthService.logout()
              }
            )
          )
        case None =>
          Button(
            _.design := ButtonDesign.Transparent,
            _.icon := IconName.`source-code`,
            "Sign in",
            onClick --> { _ =>
              AuthService.login()
            }
          )
      }
    )

end UserProfile
