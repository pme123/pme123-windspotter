package pme123.windspotter

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import org.scalajs.dom.{File, FileReader}
import scala.scalajs.js.Date
import scala.concurrent.duration.*



object ImageUploadView:

  def apply(): HtmlElement =
    val selectedImageVar    = Var[Option[ImageData]](None)
    val imageUrlVar         = Var("")
    val autoRefreshVar      = Var(true) // Start auto-refresh immediately
    val lastUpdateVar       = Var[Option[String]](None)
    val currentWebcamUrlVar = Var[Option[String]](None)
    val imageHistoryVar     = Var[List[ImageData]](List.empty) // Keep last 10 images

    // Default webcam URL - simple static URL that updates every 5 minutes
    val defaultWebcamUrl = "http://bhsboots.myhostpoint.ch/kamera04.jpg"

    val element = renderImageUploadCard(
      selectedImageVar,
      imageUrlVar,
      autoRefreshVar,
      lastUpdateVar,
      currentWebcamUrlVar,
      imageHistoryVar,
      defaultWebcamUrl
    )

    // Start loading immediately after element is created
    dom.window.setTimeout(
      () =>
        dom.console.log("🚀 Starting automatic webcam loading...")
        loadLatestWebcamImage(
          defaultWebcamUrl,
          selectedImageVar,
          lastUpdateVar,
          currentWebcamUrlVar,
          imageHistoryVar
        )
        setupAutoRefresh(
          defaultWebcamUrl,
          selectedImageVar,
          lastUpdateVar,
          currentWebcamUrlVar,
          imageHistoryVar,
          autoRefreshVar
        )
      ,
      100
    ) // Small delay to ensure DOM is ready

    element
  end apply

  def renderImageUploadCard(
      selectedImageVar: Var[Option[ImageData]],
      imageUrlVar: Var[String],
      autoRefreshVar: Var[Boolean],
      lastUpdateVar: Var[Option[String]],
      currentWebcamUrlVar: Var[Option[String]],
      imageHistoryVar: Var[List[ImageData]],
      defaultWebcamUrl: String
  ): HtmlElement =
    Card(
      _.slots.header := CardHeader(
        _.titleText    := "Webcam Analysis",
        _.subtitleText := "Experiments with Webcam Images"
      ),
      div(
        className := "card-content",
        div(
          className := "image-upload-section",

          // Auto-refresh webcam section
          div(
            className := "upload-method webcam-section",
            Title(
              "Webcam Sisikon"
            ),
            // Webcam image display within the panel
            div(
              className := "webcam-image-section",
              child <-- selectedImageVar.signal.map {
                case Some(imageData) =>
                  div(
                    className := "webcam-image-container",
                    img(
                      src       := imageData.dataUrl,
                      className := "webcam-image",
                      alt       := "Live webcam feed",
                      onClick --> { _ =>
                        val history = imageHistoryVar.now()
                        val currentIndex = history.indexWhere(_.dataUrl == imageData.dataUrl)
                        val index = if (currentIndex >= 0) Some(currentIndex) else None
                        showImageOverlay(
                          imageData.dataUrl,
                          if (history.nonEmpty) Some(history) else None,
                          index,
                          if (history.nonEmpty) Some((newImage: ImageData) => selectedImageVar.set(Some(newImage))) else None
                        )
                      }
                    )
                  )
                case None            =>
                  div(
                    className := "webcam-loading",
                    p("🔄 Loading webcam image..."),
                    p("The live webcam feed will appear here automatically.")
                  )
              }
            ),
            // Thumbnail gallery component
            child <-- Signal.combine(imageHistoryVar.signal, selectedImageVar.signal).map {
              case (history, selectedImage) =>
                if (history.nonEmpty) {
                  ThumbnailGallery(
                    history,
                    selectedImage,
                    (newImage: ImageData) => selectedImageVar.set(Some(newImage)),
                    showImageOverlay
                  )
                } else {
                  emptyNode
                }
            },
            div(
              className := "webcam-footer",
              a(
                className := "webcam-footer",
                href      := ("https://www.bhs.swiss"),
                "https://www.bhs.swiss"
              )
            )
          )
        )
      )
    )

  private def readImageFile(file: File, imageVar: Var[Option[ImageData]]): Unit =
    val reader = new FileReader()
    reader.onload = (_: dom.Event) =>
      val dataUrl   = reader.result.asInstanceOf[String]
      val imageData = ImageData(
        name = file.name,
        url = "data:file",
        dataUrl = dataUrl
      )
      imageVar.set(Some(imageData))
    reader.addEventListener(
      "error",
      (_: dom.Event) =>
        dom.console.error("Error reading file")
    )
    reader.readAsDataURL(file)
  end readImageFile

  private def loadImageFromUrl(url: String, imageVar: Var[Option[ImageData]]): Unit =
    // Create a temporary image element to test if URL is valid
    val img = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]

    img.onload = (_: dom.Event) =>
      // Extract filename from URL
      val fileName  = url.split("/").lastOption.getOrElse("image")
      val imageData = ImageData(
        name = fileName,
        url = url,
        dataUrl = url
      )
      imageVar.set(Some(imageData))

    img.addEventListener(
      "error",
      (_: dom.Event) =>
        dom.console.error(s"Failed to load image from URL: $url")
        dom.window.alert("Failed to load image from URL. Please check the URL and try again.")
    )

    img.src = url
  end loadImageFromUrl

  private def generateCurrentWebcamUrl(baseUrl: String): String =
    val now   = new Date()
    val year  = now.getFullYear().toInt
    val month = f"${now.getMonth().toInt + 1}%02d"
    val day   = f"${now.getDate().toInt}%02d"
    val hour  = f"${now.getHours().toInt}%02d"

    // Determine which 10-minute interval image to fetch
    val currentMinute = now.getMinutes().toInt
    val minuteTens    = if currentMinute % 10 >= 2 then
      // If we're at X:X2 or later, fetch current interval (e.g., at 14:42, fetch 1340)
      currentMinute / 10
    else
      // If we're at X:X0 or X:X1, fetch previous interval to be safe
      if currentMinute >= 10 then (currentMinute / 10) - 1 else 5

    val adjustedHour = if minuteTens == 5 && currentMinute < 10 then
      // Handle hour boundary (e.g., at 14:00, fetch 1350 from previous hour)
      if now.getHours().toInt > 0 then f"${now.getHours().toInt - 1}%02d" else "23"
    else
      hour

    val timeString = s"$adjustedHour${minuteTens}0" // Image is stored as hhm0

    // Replace the date/time part in the URL
    baseUrl.replaceAll(
      "img=\\d{4}/\\d{2}/\\d{2}/\\d{4}",
      s"img=$year/$month/$day/$timeString"
    )
  end generateCurrentWebcamUrl

  private def loadLatestWebcamImage(
      baseUrl: String,
      imageVar: Var[Option[ImageData]],
      lastUpdateVar: Var[Option[String]],
      currentWebcamUrlVar: Var[Option[String]],
      imageHistoryVar: Var[List[ImageData]]
  ): Unit =
    val now        = new Date()
    val timeString = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
    val timestamp  = now.getTime().toLong

    // Add cache-busting parameter to ensure fresh image
    val currentUrl = s"$baseUrl?t=$timestamp"

    dom.console.log(s"🔗 Loading webcam URL: $currentUrl")
    dom.console.log(s"⏰ Current time: $timeString")

    // Update the current URL variable immediately
    currentWebcamUrlVar.set(Some(currentUrl))

    val img = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]

    // Don't use crossorigin to avoid CORS issues
    // img.setAttribute("crossorigin", "anonymous")

    img.onload = (_: dom.Event) =>
      dom.console.log(s"✅ Webcam image loaded successfully: $currentUrl")
      val imageData = ImageData(
        name = s"Webcam_${timeString}.jpg",
        url = currentUrl,
        dataUrl = currentUrl
      )
      dom.console.log(s"📝 Setting image data: ${imageData.name}")
      imageVar.set(Some(imageData))
      lastUpdateVar.set(Some(timeString))

      // Add to history (keep last 10 images, newest at the end)
      val currentHistory = imageHistoryVar.now()

      // Add to history (keep last 10 images, newest at the end)
      val newHistory = (currentHistory :+ imageData).takeRight(10)
      imageHistoryVar.set(newHistory)

      if (currentHistory.isEmpty) {
        dom.console.log(s"🎯 FIRST IMAGE RECEIVED - Starting image history")
      } else {
        dom.console.log(s"✅ NEW IMAGE RECEIVED - Added to history, now have ${newHistory.length} images")
      }

      dom.console.log(s"✅ Image variable updated, should be visible now")

    img.addEventListener(
      "error",
      (_: dom.Event) =>
        dom.console.error(s"❌ Failed to load webcam image: $currentUrl")
        dom.console.log(s"💡 The webcam might be offline or the URL might be incorrect")
    )

    img.src = currentUrl
  end loadLatestWebcamImage

  private def generateFallbackWebcamUrl(baseUrl: String): String =
    val now   = new Date()
    val year  = now.getFullYear().toInt
    val month = f"${now.getMonth().toInt + 1}%02d"
    val day   = f"${now.getDate().toInt}%02d"
    val hour  = f"${now.getHours().toInt}%02d"

    // Try the previous 10-minute interval as fallback
    val currentMinuteTens  = (now.getMinutes().toInt / 10)
    val fallbackMinuteTens = if currentMinuteTens > 0 then currentMinuteTens - 1 else 5
    val fallbackHour       = if currentMinuteTens == 0 && now.getHours().toInt > 0 then
      f"${now.getHours().toInt - 1}%02d"
    else if currentMinuteTens == 0 && now.getHours().toInt == 0 then
      "23" // Previous day's last hour - simplified fallback
    else
      hour

    val timeString = s"$fallbackHour${fallbackMinuteTens}0"

    baseUrl.replaceAll(
      "img=\\d{4}/\\d{2}/\\d{2}/\\d{4}",
      s"img=$year/$month/$day/$timeString"
    )
  end generateFallbackWebcamUrl

  private def setupAutoRefresh(
      baseUrl: String,
      imageVar: Var[Option[ImageData]],
      lastUpdateVar: Var[Option[String]],
      currentWebcamUrlVar: Var[Option[String]],
      imageHistoryVar: Var[List[ImageData]],
      autoRefreshVar: Var[Boolean]
  ): Unit =
    def scheduleNext(): Unit =
      dom.window.setTimeout(
        () =>
          if autoRefreshVar.now() then
            loadLatestWebcamImage(baseUrl, imageVar, lastUpdateVar, currentWebcamUrlVar, imageHistoryVar)
            scheduleNext(),
        2 * 60 * 1000
      ) // 2 minutes in milliseconds

    scheduleNext()
  end setupAutoRefresh

  def showImageOverlay(
    imageUrl: String,
    images: Option[List[ImageData]] = None,
    currentIndex: Option[Int] = None,
    onImageChange: Option[ImageData => Unit] = None
  ): Unit =
    dom.console.log(s"🔍 showImageOverlay called with:")
    dom.console.log(s"   - imageUrl: $imageUrl")
    dom.console.log(s"   - images count: ${images.map(_.length).getOrElse(0)}")
    dom.console.log(s"   - currentIndex: $currentIndex")
    dom.console.log(s"   - onImageChange provided: ${onImageChange.isDefined}")
    // Create overlay container
    val overlay = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
    overlay.className = "image-overlay"

    // Create close button
    val closeButton = dom.document.createElement("button").asInstanceOf[dom.HTMLButtonElement]
    closeButton.className = "overlay-close-button"
    closeButton.textContent = "✕"
    closeButton.onclick = (_: dom.Event) =>
      dom.document.body.removeChild(overlay)

    // Create full-size image
    val fullImage = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]
    fullImage.className = "overlay-image"
    fullImage.src = imageUrl
    fullImage.alt = "Full size webcam image"

    // Create slideshow controls if images are provided
    val slideshowControls = (images, currentIndex) match
      case (Some(imageList), Some(initialIndex)) if imageList.length > 1 =>
        dom.console.log(s"✅ Creating slideshow controls for ${imageList.length} images, starting at index $initialIndex")
        val controlsDiv = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
        controlsDiv.className = "overlay-slideshow-controls"

        // Track current index in overlay
        var currentOverlayIndex = initialIndex

        // Previous button
        val prevButton = dom.document.createElement("button").asInstanceOf[dom.HTMLButtonElement]
        prevButton.className = "overlay-nav-button overlay-prev-button"
        prevButton.textContent = "‹"

        // Next button
        val nextButton = dom.document.createElement("button").asInstanceOf[dom.HTMLButtonElement]
        nextButton.className = "overlay-nav-button overlay-next-button"
        nextButton.textContent = "›"

        // Slide counter
        val counterSpan = dom.document.createElement("span").asInstanceOf[dom.HTMLSpanElement]
        counterSpan.className = "overlay-slide-counter"
        counterSpan.textContent = s"${initialIndex + 1} / ${imageList.length}"

        // Set up navigation
        prevButton.onclick = (_: dom.Event) =>
          dom.console.log(s"⬅️ Previous button clicked, current index: $currentOverlayIndex")
          currentOverlayIndex = if currentOverlayIndex > 0 then currentOverlayIndex - 1 else imageList.length - 1
          val newImage = imageList(currentOverlayIndex)
          dom.console.log(s"🔄 Changing to image: ${newImage.name}")
          dom.console.log(s"🔗 New image URL: ${newImage.dataUrl.take(50)}...")
          fullImage.src = newImage.dataUrl
          counterSpan.textContent = s"${currentOverlayIndex + 1} / ${imageList.length}"
          onImageChange.foreach(_(newImage))
          dom.console.log(s"✅ Updated to image ${currentOverlayIndex + 1}/${imageList.length}")

        nextButton.onclick = (_: dom.Event) =>
          dom.console.log(s"➡️ Next button clicked, current index: $currentOverlayIndex")
          currentOverlayIndex = if currentOverlayIndex < imageList.length - 1 then currentOverlayIndex + 1 else 0
          val newImage = imageList(currentOverlayIndex)
          dom.console.log(s"🔄 Changing to image: ${newImage.name}")
          dom.console.log(s"🔗 New image URL: ${newImage.dataUrl.take(50)}...")
          fullImage.src = newImage.dataUrl
          counterSpan.textContent = s"${currentOverlayIndex + 1} / ${imageList.length}"
          onImageChange.foreach(_(newImage))
          dom.console.log(s"✅ Updated to image ${currentOverlayIndex + 1}/${imageList.length}")

        controlsDiv.appendChild(prevButton)
        controlsDiv.appendChild(counterSpan)
        controlsDiv.appendChild(nextButton)
        dom.console.log(s"✅ Slideshow controls created and assembled")
        Some(controlsDiv)
      case _ =>
        dom.console.log(s"❌ No slideshow controls created - not enough images or missing parameters")
        None

    // Add click to close functionality (only on overlay background)
    overlay.onclick = (_: dom.Event) =>
      dom.document.body.removeChild(overlay)

    // Prevent image and controls click from closing overlay
    fullImage.onclick = (e: dom.Event) =>
      e.stopPropagation()

    slideshowControls.foreach(controls =>
      controls.onclick = (e: dom.Event) =>
        e.stopPropagation()
    )

    // Assemble overlay
    overlay.appendChild(closeButton)
    overlay.appendChild(fullImage)
    slideshowControls.foreach(overlay.appendChild)

    // Add to page
    dom.document.body.appendChild(overlay)
  end showImageOverlay





end ImageUploadView
