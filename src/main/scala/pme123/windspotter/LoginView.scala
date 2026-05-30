package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object LoginView:

  def apply(): HtmlElement =
    dom.console.log("🔐 Creating LoginView")
    div(
      className := "login-container",
      div(
        className := "login-content",
        h2(styleAttr := "margin: 0 0 8px; font-size: 1.1rem; color: var(--text);", "Authentication Required"),
        p(styleAttr := "margin: 0 0 24px; font-size: 0.82rem; color: var(--muted);",
          "Sign in with your GitHub account to access wind conditions and personalized features."),
        div(
          className := "login-actions",
          button(
            styleAttr := "background: var(--accent); color: #0f172a; border: none; border-radius: 8px; padding: 10px 22px; font-size: 0.88rem; font-weight: 700; cursor: pointer; transition: opacity 0.15s;",
            "Sign in with GitHub",
            onClick --> { _ => AuthService.login() }
          ),
          {
            val isLocalhost = dom.window.location.origin.contains("localhost") ||
                              dom.window.location.origin.contains("127.0.0.1")
            if (isLocalhost)
              div(
                className := "demo-section",
                p(className := "demo-text", "Or try demo mode:"),
                button(
                  styleAttr := "background: var(--surface2); color: var(--text); border: 1px solid var(--border); border-radius: 8px; padding: 8px 18px; font-size: 0.82rem; cursor: pointer;",
                  "Demo Login",
                  onClick --> { _ => AuthService.demoLogin() }
                )
              )
            else emptyNode
          }
        )
      )
    )

end LoginView
