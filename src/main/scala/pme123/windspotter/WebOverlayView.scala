package pme123.windspotter

import org.scalajs.dom
import scala.scalajs.js

object WebOverlayView {

  def showWebOverlay(title: String, url: String): Unit = {
    dom.console.log(s"🌐 Opening web overlay for: $title -> $url")
    
    // Create overlay container
    val overlay = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
    overlay.className = "web-overlay"
    
    // Create overlay content
    val overlayContent = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
    overlayContent.className = "web-overlay-content"
    
    // Create header with title and close button
    val header = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
    header.className = "web-overlay-header"
    
    val titleElement = dom.document.createElement("h3").asInstanceOf[dom.HTMLHeadingElement]
    titleElement.textContent = title
    titleElement.className = "web-overlay-title"
    
    val closeButton = dom.document.createElement("button").asInstanceOf[dom.HTMLButtonElement]
    closeButton.className = "web-overlay-close-button"
    closeButton.textContent = "✕"
    closeButton.onclick = (_: dom.Event) => {
      if (overlay.parentNode != null && overlay.parentNode == dom.document.body) {
        dom.document.body.removeChild(overlay)
      }
    }
    
    header.appendChild(titleElement)
    header.appendChild(closeButton)
    
    // Create iframe for the web content
    val iframe = dom.document.createElement("iframe").asInstanceOf[dom.HTMLIFrameElement]
    iframe.src = url
    iframe.className = "web-overlay-iframe"
    iframe.setAttribute("frameborder", "0")
    iframe.setAttribute("allowfullscreen", "true")
    
    // Assemble the overlay
    overlayContent.appendChild(header)
    overlayContent.appendChild(iframe)
    overlay.appendChild(overlayContent)
    
    // Add click to close functionality (only on overlay background)
    overlay.onclick = (e: dom.Event) => {
      if (e.target == overlay) {
        if (overlay.parentNode != null && overlay.parentNode == dom.document.body) {
          dom.document.body.removeChild(overlay)
        }
      }
    }
    
    // Prevent content clicks from closing overlay
    overlayContent.onclick = (e: dom.Event) => {
      e.stopPropagation()
    }
    
    // Add to document
    dom.document.body.appendChild(overlay)
    
    dom.console.log(s"✅ Web overlay opened for: $title")
  }
}
