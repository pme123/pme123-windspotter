package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.scalajs.js.Date

object WebcamService {

  def generateWebcamUrl(webcam: Webcam): String = {
    webcam.url match {
      case url if url.contains("foto-webcam.eu") =>
        // Handle time-based webcam URLs
        generateTimeBasedUrl(url)
      case url if url.contains("api.codetabs.com") =>
        // Handle codetabs proxy URLs - add timestamp and URL encode
        val timestamp = new Date().getTime().toLong
        val originalUrl = url.split("quest=").last
        val proxiedUrlWithTimestamp = s"$originalUrl?t=$timestamp"
        val encodedUrl = java.net.URLEncoder.encode(proxiedUrlWithTimestamp, "UTF-8")
        url.replace(originalUrl, encodedUrl)
      case url =>
        // Handle simple URLs with cache busting
        val timestamp = new Date().getTime().toLong
        s"$url?t=$timestamp"
    }
  }

  private def generateTimeBasedUrl(baseUrl: String): String = {
    val now = new Date()
    val year = now.getFullYear().toInt
    val month = f"${now.getMonth().toInt + 1}%02d"
    val day = f"${now.getDate().toInt}%02d"
    val hour = f"${now.getHours().toInt}%02d"
    
    // Determine which 10-minute interval image to fetch
    val currentMinute = now.getMinutes().toInt
    val minuteTens = if (currentMinute % 10 >= 2) {
      // If we're at X:X2 or later, fetch current interval
      currentMinute / 10
    } else {
      // If we're at X:X0 or X:X1, fetch previous interval to be safe
      if (currentMinute >= 10) (currentMinute / 10) - 1 else 5
    }
    
    val adjustedHour = if (minuteTens == 5 && currentMinute < 10) {
      // Handle hour boundary
      if (now.getHours().toInt > 0) f"${now.getHours().toInt - 1}%02d" else "23"
    } else {
      hour
    }
    
    val timeString = s"$adjustedHour${minuteTens}0" // Image is stored as hhm0
    
    // Replace the date/time part in the URL
    baseUrl.replaceAll(
      "img=\\d{4}/\\d{2}/\\d{2}/\\d{4}",
      s"img=$year/$month/$day/$timeString"
    )
  }

  def loadWebcamImage(
    webcam: Webcam,
    stateVar: Var[WebcamState]
  ): Unit = {
    val now = new Date()
    val timeString = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
    val currentUrl = generateWebcamUrl(webcam)
    
    dom.console.log(s"🔗 Loading ${webcam.name}: $currentUrl")
    dom.console.log(s"⏰ Current time: $timeString")
    
    // Update current URL immediately
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(currentUrl = Some(currentUrl)))
    
    val img = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]
    
    img.onload = (_: dom.Event) => {
      dom.console.log(s"✅ ${webcam.name} image loaded successfully")
      val imageData = ImageData(
        name = s"${webcam.name}_${timeString}.jpg",
        url = currentUrl,
        dataUrl = currentUrl
      )
      
      val state = stateVar.now()
      val newHistory = (state.imageHistory :+ imageData).takeRight(10)
      
      stateVar.set(state.copy(
        selectedImage = Some(imageData),
        imageHistory = newHistory,
        lastUpdate = Some(timeString)
      ))
      
      if (state.imageHistory.isEmpty) {
        dom.console.log(s"🎯 FIRST IMAGE RECEIVED - ${webcam.name}")
      } else {
        dom.console.log(s"✅ NEW IMAGE RECEIVED - ${webcam.name}, now have ${newHistory.length} images")
      }
    }
    
    img.addEventListener("error", (_: dom.Event) => {
      dom.console.error(s"❌ Failed to load ${webcam.name} image: $currentUrl")
    })
    
    img.src = currentUrl
  }

  def setupAutoRefresh(
    webcam: Webcam,
    stateVar: Var[WebcamState]
  ): Unit = {
    def scheduleNext(): Unit = {
      dom.window.setTimeout(() => {
        val currentState = stateVar.now()
        if (currentState.isAutoRefresh) {
          loadWebcamImage(webcam, stateVar)
          scheduleNext()
        }
      }, webcam.reloadInMin * 60 * 1000) // Convert minutes to milliseconds
    }
    
    scheduleNext()
  }

  def startAutoRefresh(webcam: Webcam, stateVar: Var[WebcamState]): Unit = {
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(isAutoRefresh = true))
    
    // Load first image immediately
    loadWebcamImage(webcam, stateVar)
    
    // Setup auto-refresh
    setupAutoRefresh(webcam, stateVar)
    
    dom.console.log(s"🚀 Started auto-refresh for ${webcam.name} (every ${webcam.reloadInMin} min)")
  }

  def stopAutoRefresh(stateVar: Var[WebcamState]): Unit = {
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(isAutoRefresh = false))
    dom.console.log(s"⏹️ Stopped auto-refresh")
  }
}
