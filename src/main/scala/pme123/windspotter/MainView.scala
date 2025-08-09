package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}

object MainView {

  def apply(): HtmlElement = {
    div(
      className := "main-container",
      renderExample()
    )
  }

  def renderExample(): HtmlElement = {
    div(
      ImageUploadView()
    )
  }
}
