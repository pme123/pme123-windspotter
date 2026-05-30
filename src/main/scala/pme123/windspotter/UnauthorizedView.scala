package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object UnauthorizedView:

  def apply(): HtmlElement =
    dom.console.log("🔐 Creating UnauthorizedView")
    div(
      className := "unauthorized-container",
      div(
        className := "login-content",
        h2(styleAttr := "margin: 0 0 8px; font-size: 1.1rem; color: var(--text);", "Access Denied"),
        div(
          className := "unauthorized-description",
          p("Your GitHub account is not authorized to access this application."),
          p("If you believe this is an error, please contact the administrator."),
          child <-- AuthService.currentUserVar.signal.map {
            case Some(user) =>
              p(className := "user-info-text",
                s"Authenticated as: ${user.name.getOrElse(user.login)} (${user.login})")
            case None => emptyNode
          }
        ),
        div(
          className := "unauthorized-actions",
          button(
            styleAttr := "background: var(--surface2); color: var(--text); border: 1px solid var(--border); border-radius: 8px; padding: 8px 18px; font-size: 0.82rem; cursor: pointer;",
            "Sign Out",
            onClick --> { _ => AuthService.logout() }
          )
        )
      )
    )

end UnauthorizedView
