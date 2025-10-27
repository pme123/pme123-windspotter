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

    // Special debugging for Bolzbach to track duplicate calls
    if (webcam.name.contains("Bolzbach")) {
      dom.console.log(s"🚨 BOLZBACH LOAD ATTEMPT: ${webcam.name}")
      dom.console.log(s"   - URL: $currentUrl")
      dom.console.log(s"   - Time: $timeString")
      dom.console.log(s"   - Current state history size: ${stateVar.now().imageHistory.length}")
    }

    dom.console.log(s"🔗 Loading ${webcam.name}: $currentUrl")
    dom.console.log(s"⏰ Current time: $timeString")
    
    // Update current URL immediately
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(currentUrl = Some(currentUrl)))
    
    val img = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]
    
    img.onload = (_: dom.Event) => {
      dom.console.log(s"✅ ${webcam.name} image loaded successfully")
      // For Bolzbach, keep the original URL that was successfully loaded
      val finalDataUrl = currentUrl

      val imageData = ImageData(
        name = s"${webcam.name}_${timeString}.jpg",
        url = currentUrl,
        dataUrl = finalDataUrl
      )

      val state = stateVar.now()

      // Special debugging for Bolzbach webcam
      if (webcam.name.contains("Bolzbach")) {
        dom.console.log(s"🔍 Bolzbach Debug:")
        dom.console.log(s"   - Current URL: $currentUrl")
        dom.console.log(s"   - Image name: ${imageData.name}")
        dom.console.log(s"   - Image dimensions: ${img.naturalWidth}x${img.naturalHeight}")
        dom.console.log(s"   - Image file size estimate: ${img.naturalWidth * img.naturalHeight}")
        dom.console.log(s"   - Current history size: ${state.imageHistory.length}")
        dom.console.log(s"   - Existing URLs: ${state.imageHistory.map(_.url).mkString(", ")}")

        // Check if this is actually a different image by comparing with the last one
        if (state.imageHistory.nonEmpty) {
          val lastImage = state.imageHistory.last
          dom.console.log(s"   - Last image name: ${lastImage.name}")
          dom.console.log(s"   - URLs are different: ${lastImage.url != currentUrl}")
          dom.console.log(s"   - Time difference: ${timeString} vs ${lastImage.name.split("_").last.replace(".jpg", "")}")
        }

        dom.console.log(s"   - THEORY: Bolzbach server might be serving the same image content despite different URLs")
        dom.console.log(s"   - SUGGESTION: Try increasing reload interval to 5+ minutes to match server update frequency")
      }

      val newHistory = (state.imageHistory :+ imageData).takeRight(10)

      // Extra debugging for Bolzbach to check if images are actually being stored uniquely
      if (webcam.name.contains("Bolzbach")) {
        dom.console.log(s"📚 Bolzbach History Check:")
        dom.console.log(s"   - About to store image: ${imageData.name}")
        dom.console.log(s"   - Image URL: ${imageData.url}")
        dom.console.log(s"   - DataURL starts with: ${imageData.dataUrl.take(50)}...")
        dom.console.log(s"   - New history will have ${newHistory.length} images")
        newHistory.zipWithIndex.foreach { case (img, idx) =>
          dom.console.log(s"   - History[$idx]: ${img.name} -> ${img.url}")
        }
      }

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
    // Skip auto-refresh for video webcams
    if (webcam.webcamType == WebcamType.VideoWebcam) {
      dom.console.log(s"📹 Skipping auto-refresh for video webcam: ${webcam.name}")
      return
    }

    // Handle Windy webcams differently
    if (webcam.webcamType == WebcamType.WindyWebcam) {
      dom.console.log(s"🌬️ Starting auto-capture for Windy webcam: ${webcam.name}")
      startWindyAutoCapture(webcam, stateVar)
      return
    }

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

  private def startWindyAutoCapture(webcam: Webcam, stateVar: Var[WebcamState]): Unit = {
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(isAutoRefresh = true))

    // Capture first image after a delay to let Windy load
    dom.window.setTimeout(() => {
      captureWindyWebcamImage(webcam, stateVar)
    }, 3000)

    // Set up interval for regular captures
    def scheduleNext(): Unit = {
      dom.window.setTimeout(() => {
        val state = stateVar.now()
        if (state.isAutoRefresh) {
          captureWindyWebcamImage(webcam, stateVar)
          scheduleNext()
        }
      }, webcam.reloadInMin * 60 * 1000)
    }

    scheduleNext()
    dom.console.log(s"Started auto-capture for ${webcam.name} (every ${webcam.reloadInMin} min)")
  }

  def captureWindyWebcamImage(webcam: Webcam, stateVar: Var[WebcamState]): Unit = {
    dom.console.log(s"📸 Auto-capturing Windy webcam image for ${webcam.name}")

    val now = new Date()
    val timeString = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
    val imageName = s"${webcam.name}_$timeString.jpg"

    // Create a placeholder canvas for now
    val canvas = dom.document.createElement("canvas").asInstanceOf[dom.HTMLCanvasElement]
    canvas.width = 400
    canvas.height = 300
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Draw placeholder
    ctx.fillStyle = "#e3f2fd"
    ctx.fillRect(0, 0, 400, 300)
    ctx.fillStyle = "#1976d2"
    ctx.font = "16px Arial"
    ctx.textAlign = "center"
    ctx.fillText(s"🌬️ ${webcam.name}", 200, 140)
    ctx.fillText(s"Captured: $timeString", 200, 170)

    val dataUrl = canvas.toDataURL("image/jpeg", 0.8)

    val imageData = ImageData(
      name = imageName,
      url = webcam.url,
      dataUrl = dataUrl
    )

    val state = stateVar.now()
    val newHistory = (state.imageHistory :+ imageData).takeRight(10)

    stateVar.set(state.copy(
      selectedImage = Some(imageData),
      imageHistory = newHistory,
      lastUpdate = Some(timeString)
    ))

    dom.console.log(s"✅ Windy webcam image captured for ${webcam.name}")
  }
}
