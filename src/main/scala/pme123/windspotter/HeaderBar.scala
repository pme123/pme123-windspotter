package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object HeaderBar:

  def apply(): HtmlElement =
    Bar(
      _.design := BarDesign.Header,
      _.slots.endContent := span(
        Link(
          _.href := "https://pme123.github.io/pme123-weather",
          _.target := LinkTarget._blank,
          "Weather"
        ),
        " | ",
        Link(
          _.href := "https://github.com/pme123/pme123-windspotter",
          _.target := LinkTarget._blank,
          "Github"
        )
      ),
      Title(_.size := TitleLevel.H4, "feensturm's Windspotter")
    )

end HeaderBar
