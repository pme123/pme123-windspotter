package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}

object HeaderBar:

  def apply(): HtmlElement =
    div(
      className := "header",
      a(
        href      := "https://z9nai.ch",
        target    := "_blank",
        rel       := "noopener",
        className := "z9-brand-link",
        div(
          className := "z9-logo-wrap",
          img(
            src       := "https://z9nai.ch/assets/logo_new-DwOlNuuy.png",
            alt       := "z9nai",
            className := "z9-logo-color"
          ),
          img(
            src       := "https://z9nai.ch/assets/logo_new_white-BXK2S0Ym.png",
            alt       := "z9nai",
            className := "z9-logo-white"
          )
        ),
        h1("Windspotter"),
        div(className := "z9-byline", "by z9nai GmbH")
      ),
      div(
        className := "header-right",
        button(
          className := "header-link header-icon-btn",
          title     := "Configurations",
          onClick --> { _ => ConfigEditorDialog.open() },
          svg.svg(
            svg.viewBox        := "0 0 24 24",
            svg.width          := "18",
            svg.height         := "18",
            svg.fill           := "none",
            svg.stroke         := "currentColor",
            svg.strokeWidth    := "2",
            svg.strokeLineCap  := "round",
            svg.strokeLineJoin := "round",
            svg.path(
              svg.d := "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"
            ),
            svg.circle(svg.cx := "12", svg.cy := "12", svg.r := "3")
          )
        ),
        span(className := "header-sep", " | "),
        a(
          href      := "https://pme123.github.io/pme123-weather",
          target    := "_blank",
          rel       := "noopener",
          className := "header-link",
          "Weather"
        ),
        span(className := "header-sep", " | "),
        a(
          href      := "https://pme123.github.io/pme123-windalert/",
          target    := "_blank",
          rel       := "noopener",
          className := "header-link",
          "Wind Alert"
        ),
        span(className := "header-sep", " | "),
        a(
          href      := "https://github.com/pme123/pme123-windspotter",
          target    := "_blank",
          rel       := "noopener",
          className := "header-link",
          title     := "GitHub",
          svg.svg(
            svg.viewBox := "0 0 24 24",
            svg.width   := "20",
            svg.height  := "20",
            svg.fill    := "currentColor",
            svg.path(
              svg.d := "M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"
            )
          )
        ),
      )
    )

end HeaderBar
