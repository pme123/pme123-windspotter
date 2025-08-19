package pme123.windspotter

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure, Try}
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.matching.Regex

object ScrapedWebcamService:

  def scrapeAndLoadImage(
    webcam: Webcam,
    stateVar: Var[WebcamState],
    loadingEnabledVar: Var[Boolean] = Var(true)
  ): Unit = {
    // Check if loading is enabled
    if (!loadingEnabledVar.now()) {
      dom.console.log(s"⚫ Loading disabled for ${webcam.name} - skipping scrape")
      return
    }
    webcam.scrapingConfig match {
      case Some(config) =>
        dom.console.log(s"🕷️ Starting scrape for ${webcam.name}")
        dom.console.log(s"🕷️ Page URL: ${config.pageUrl}")
        dom.console.log(s"🕷️ Image regex: ${config.imageRegex}")
        
        scrapeImageUrl(config).foreach {
          case Success(imageUrl) =>
            dom.console.log(s"✅ Found image URL: $imageUrl")
            loadScrapedImage(webcam, imageUrl, stateVar)
          case Failure(ex) =>
            dom.console.error(s"❌ Failed to scrape image URL: ${ex.getMessage}")
            // Show error in UI
            showScrapingError(webcam, ex.getMessage, stateVar)
        }
      case None =>
        dom.console.error(s"❌ No scraping config found for ${webcam.name}")
        showScrapingError(webcam, "No scraping configuration found", stateVar)
    }
  }

  private def scrapeImageUrl(config: ScrapingConfig): Future[Try[String]] = {
    // Use a CORS proxy to fetch the webpage content
    val proxyUrl = s"https://api.codetabs.com/v1/proxy?quest=${js.URIUtils.encodeURIComponent(config.pageUrl)}"
    
    dom.console.log(s"🕷️ Fetching page via proxy: $proxyUrl")
    
    dom.fetch(proxyUrl, new dom.RequestInit {
      method = dom.HttpMethod.GET
      headers = {
        val headers = new dom.Headers()
        headers.append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        headers.append("User-Agent", "Mozilla/5.0 (compatible; WindSpotter/1.0)")
        headers
      }
    }).toFuture.flatMap { response =>
      if (response.ok) {
        response.text().toFuture.map { html =>
          dom.console.log(s"🕷️ Received HTML content (${html.length} chars)")
          extractImageUrl(html, config)
        }
      } else {
        dom.console.error(s"❌ HTTP error: ${response.status} ${response.statusText}")
        Future.successful(Failure(new Exception(s"HTTP ${response.status}: ${response.statusText}")))
      }
    }.recover {
      case ex =>
        dom.console.error(s"❌ Network error: ${ex.getMessage}")
        Failure(ex)
    }
  }

  private def extractImageUrl(html: String, config: ScrapingConfig): Try[String] = {
    Try {
      val regex = new Regex(config.imageRegex)
      
      dom.console.log(s"🕷️ Searching HTML for pattern: ${config.imageRegex}")
      // Find all matches
      val matches = regex.findAllIn(html).toList
      dom.console.log(s"🕷️ Found ${matches.length} matches")
      
      if (matches.isEmpty) {
        // Log a sample of the HTML for debugging
        val sample = if (html.length > 500) html.take(500) + "..." else html
        dom.console.log(s"🕷️ HTML sample: $sample")
        throw new Exception(s"No image URLs found matching pattern: ${config.imageRegex}")
      }
      
      // Get the first (most recent) match
      val imageUrl = matches.head
      dom.console.log(s"🕷️ Selected image URL: $imageUrl")
      
      // Handle relative URLs
      config.baseUrl match {
        case Some(base) if !imageUrl.startsWith("http") =>
          val fullUrl = if (imageUrl.startsWith("/")) s"$base$imageUrl" else s"$base/$imageUrl"
          dom.console.log(s"🕷️ Converted to full URL: $fullUrl")
          fullUrl
        case _ =>
          imageUrl
      }
    }
  }

  private def loadScrapedImage(
    webcam: Webcam,
    imageUrl: String,
    stateVar: Var[WebcamState]
  ): Unit = {
    val now = new js.Date()
    val timeString = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
    
    dom.console.log(s"🖼️ Loading scraped image: $imageUrl")
    
    // Update current URL immediately
    val currentState = stateVar.now()
    stateVar.set(currentState.copy(currentUrl = Some(imageUrl)))
    
    val img = dom.document.createElement("img").asInstanceOf[dom.HTMLImageElement]
    
    img.onload = (_: dom.Event) => {
      dom.console.log(s"✅ ${webcam.name} scraped image loaded successfully")
      
      val imageData = ImageData(
        name = s"${webcam.name}_${timeString}.jpg",
        url = imageUrl,
        dataUrl = imageUrl
      )

      val state = stateVar.now()
      val newHistory = (state.imageHistory :+ imageData).takeRight(10) // Keep last 10 images

      stateVar.set(state.copy(
        selectedImage = Some(imageData),
        imageHistory = newHistory,
        lastUpdate = Some(timeString)
      ))
      
      dom.console.log(s"✅ Scraped image added to history for ${webcam.name}")
    }
    
    img.addEventListener("error", (_: dom.Event) => {
      dom.console.error(s"❌ Failed to load scraped image: $imageUrl")
      showScrapingError(webcam, s"Failed to load image: $imageUrl", stateVar)
    })
    
    img.src = imageUrl
  }

  private def showScrapingError(
    webcam: Webcam,
    errorMessage: String,
    stateVar: Var[WebcamState]
  ): Unit = {
    val now = new js.Date()
    val timeString = f"${now.getHours().toInt}%02d:${now.getMinutes().toInt}%02d"
    
    // Create a placeholder error image
    val canvas = dom.document.createElement("canvas").asInstanceOf[dom.HTMLCanvasElement]
    canvas.width = 400
    canvas.height = 300
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Draw error placeholder
    ctx.fillStyle = "#ffebee"
    ctx.fillRect(0, 0, 400, 300)
    ctx.fillStyle = "#c62828"
    ctx.font = "16px Arial"
    ctx.textAlign = "center"
    ctx.fillText(s"❌ ${webcam.name}", 200, 120)
    ctx.fillText("Scraping Error", 200, 150)
    ctx.font = "12px Arial"
    ctx.fillText(errorMessage, 200, 180)
    ctx.fillText(s"Time: $timeString", 200, 200)

    val dataUrl = canvas.toDataURL("image/jpeg", 0.8)

    val imageData = ImageData(
      name = s"${webcam.name}_error_${timeString}.jpg",
      url = "error",
      dataUrl = dataUrl
    )

    val state = stateVar.now()
    stateVar.set(state.copy(
      selectedImage = Some(imageData),
      lastUpdate = Some(s"$timeString (Error)")
    ))

    dom.console.log(s"❌ Error placeholder created for ${webcam.name}")
  }

end ScrapedWebcamService
