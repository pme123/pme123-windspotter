package pme123.windspotter

import pme123.windspotter.WebcamType.{IframeWebcam, ImageWebcam}

object groups:
  import webcams as wc

  def getDefaultWebcamGroup: WebcamGroup = webcamGroups.head

  def getAllWebcams: List[Webcam] = webcamGroups.flatMap(_.webcams)

  def findWebcamGroupByName(name: String): Option[WebcamGroup] =
    webcamGroups.find(_.name == name)

  lazy val webcamGroups = List(
    urnersee,
    central,
    west,
    east,
    italy,
    france,
    winter
  )

  lazy val urnersee = WebcamGroup(
    name = "Urnersee",
    webcams = List(
      wc.urnersee.sisikonBootshafenNord,
      wc.urnersee.isleten,
      wc.urnersee.gruonbach,
      wc.urnersee.axenegg,
      wc.urnersee.brunnen,
      wc.urnersee.bolzbach,
      wc.urnersee.sisikonBootshafenSued,
      wc.urnersee.windsurfingUrnersee
    )
  )
  lazy val central  = WebcamGroup(
    name = "Central",
    webcams = List(
      wc.central.immensee,
      wc.central.aegeri,
      wc.central.walchwil,
      wc.central.cham,
      wc.central.zug,
      wc.central.eich,
      wc.central.alpnacher
    )
  )
  lazy val east     = WebcamGroup(
    name = "East",
    webcams = List(
      wc.east.sihlsee,
      wc.east.silvaplana,
      wc.east.piposBar,
      wc.east.lindau,
      wc.east.walensee,
      wc.east.staefa
    )
  )

  lazy val west = WebcamGroup(
    name = "West",
    webcams = List(
      wc.west.stBlaise,
      wc.west.concise,
      wc.west.biseNoire,
      wc.west.bielersee
    )
  )

  lazy val italy  = WebcamGroup(
    name = "Italy",
    webcams = List(
      wc.italy.dervio,
      wc.italy.cremia,
      wc.italy.colico,
      wc.italy.lecco,
      wc.italy.domaso,
      wc.italy.malcesine,
      wc.italy.caporeamolo,
      wc.italy.torbole,
      wc.italy.tignale
    )
  )
  lazy val france = WebcamGroup(
    name = "France",
    webcams = List(
      wc.france.almanarreHyeres,
      wc.france.estagnets,
      wc.france.madrague,
      wc.france.carro,
      wc.france.sixFoursLeBrusc,
      wc.france.leJai,
      wc.france.grauDuRoiPlageSud
    )
  )
  lazy val winter = WebcamGroup(
    name = "Winter",
    webcams = List(
      wc.winter.riemenstalden,
      wc.winter.wildspitz,
      wc.winter.rigiRotstock,
      wc.winter.rigiKulm,
      wc.winter.rigiScheidegg,
      wc.winter.eggberge,
      wc.winter.oberalppass,
      wc.winter.realp,
      wc.winter.naetschen
    )
  )
end groups

object webcams:
  object urnersee:

    lazy val isleten = Webcam(
      name = "Isleten",
      url = "https://meteo.windsurfing-urnersee.ch/webcam_isleten.jpg",
      reloadInMin = 5,
      footer = "https://windsurfing-urnersee.ch"
    )

    lazy val gruonbach = Webcam(
      name = "Gruonbach",
      url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_bucht.jpg",
      reloadInMin = 5,
      footer = "https://surfclub-uri.ch"
    )

    lazy val axenegg = Webcam(
      name = "Axenegg",
      url = "https://elbeato.bplaced.net/webcamSurfclub/webcam_axenegg.jpg",
      reloadInMin = 5,
      footer = "https://surfclub-uri.ch"
    )

    lazy val brunnen = Webcam(
      name = "Brunnen",
      url = "https://www.foto-webcam.eu/webcam/brunnen/current/816.jpg",
      reloadInMin = 5,
      footer = "https://www.foto-webcam.eu/webcam/brunnen"
    )

    lazy val bolzbach = Webcam(
      name = "Bolzbach",
      url = "https://www.energieuri.ch/wp-content/uploads/webcam/live.jpg",
      reloadInMin = 5,
      footer = "https://www.seedorf-uri.ch"
    )

    lazy val sisikonBootshafenSued = Webcam(
      name = "Sisikon Bootshafen Süd",
      url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera05.jpg",
      reloadInMin = 2,
      footer = "https://www.bhs.swiss"
    )

    lazy val sisikonBootshafenNord = Webcam(
      name = "Sisikon Bootshafen Nord",
      url = "https://api.codetabs.com/v1/proxy?quest=http://bhsboots.myhostpoint.ch/kamera04.jpg",
      reloadInMin = 2,
      footer = "https://www.bhs.swiss"
    )

    lazy val windsurfingUrnersee = Webcam(
      name = "Windsurfing Urnersee",
      url = "https://meteo.windsurfing-urnersee.ch/webcam_rechts.jpg",
      reloadInMin = 2,
      footer = "https://windsurfing-urnersee.ch"
    )
  end urnersee

  object central:
    // Zugersee
    lazy val immensee = Webcam(
      name = "Immensee",
      url = "FDJcAc0zOl8", // Example YouTube video ID - replace with actual webcam stream
      reloadInMin = 0,     // YouTube videos don't need reloading
      footer = "https://www.youtube.com/@yachtclubimmensee",
      webcamType = WebcamType.YoutubeWebcam
    )
    lazy val aegeri   = Webcam(
      name = "Aegerisee",
      url = "https://scae.ch/webcam/image.jpg",
      reloadInMin = 5,
      footer = "https://scae.ch"
    )

    lazy val walchwil = Webcam(
      name = "Walchwil",
      url = "https://api.codetabs.com/v1/proxy?quest=http://109.164.203.165/record/current.jpg",
      reloadInMin = 5,
      footer = "https://www.weisszahnarzt.ch",
      mainPageLink = Some("http://109.164.203.165/cgi-bin/guestimage.html")
    )

    lazy val cham = Webcam(
      name = "Cham",
      url = "https://www.webcam.scc.ch/image_large.jpg",
      reloadInMin = 5,
      footer = "https://www.webcam.scc.ch"
    )

    lazy val zug       = Webcam(
      name = "Zug",
      url = "https://www.barile.ch/yczug/yczug/cam1.jpg",
      reloadInMin = 2,
      footer = "https://www.yczug.ch"
    )
    // Sempachersee / Alpnachersee
    lazy val eich      = Webcam(
      name = "Eich Sempachersee",
      url = "https://windsurfclubeich.ch/webcam/hikvision_current.jpg",
      reloadInMin = 1,
      footer = "https://windsurfclubeich.ch"
    )
    lazy val alpnacher = Webcam(
      name = "Alpnachersee",
      url = "https://webcam.waverocker.org/livCam.jpg?1755370546255",
      reloadInMin = 5,
      footer = "https://www.surfstation-alpnachersee.ch"
    )

  end central

  object east:
    lazy val sihlsee = Webcam(
      name = "Sihlsee",
      url = "https://www.verkehrsverein-euthal.ch/webcam-steinbach",
      reloadInMin = 10,
      footer = "https://www.verkehrsverein-euthal.ch",
      mainPageLink = Some("https://www.verkehrsverein-euthal.ch/webcam-steinbach"),
      webcamType = WebcamType.ScrapedWebcam,
      scrapingConfig = Some(ScrapingConfig(
        pageUrl = "https://www.verkehrsverein-euthal.ch/webcam-steinbach",
        imageRegex = """/modules/webcam2/[^"]+\.jpg""",        // Match the webcam image path
        baseUrl = Some("https://www.verkehrsverein-euthal.ch") // Base URL for relative paths
      ))
    )

    lazy val silvaplana = Webcam(
      name = "Silvaplana",
      url = "https://www.kitesailing.ch/spot/webcam",
      reloadInMin = 0,
      footer = "https://www.kitesailing.ch",
      mainPageLink = Some("https://www.kitesailing.ch/spot/webcam"),
      webcamType = WebcamType.IframeWebcam
    )

    lazy val piposBar = Webcam(
      name = "Pipo's Bar",
      url = "https://webcam.pipos-bar.ch/mega.jpg",
      reloadInMin = 5,
      footer = "https://webcam.pipos-bar.ch"
    )

    lazy val lindau = Webcam(
      name = "Lindau",
      url = "https://www.badschachen.de/webcam/bild.jpg",
      reloadInMin = 5,
      footer = "https://www.badschachen.de"
    )

    lazy val walensee = Webcam(
      name = "Walensee - Tiefenwinkel",
      url = "https://www.walensee.cam/webcam/Tiefenwinkel%20-%20Wölkli",
      reloadInMin = 0, // No need to reload for iframe
      footer = "https://www.walensee.cam",
      mainPageLink = Some("https://www.walensee.cam/webcam/Tiefenwinkel%20-%20Wölkli"),
      webcamType = WebcamType.IframeWebcam
    )

    lazy val staefa = Webcam(
      name = "Stäfa",
      url = "https://api.codetabs.com/v1/proxy?quest=http://scstaefa.noip.me:8080/snap.jpeg",
      reloadInMin = 5,
      footer = "https://www.scstaefa.ch"
    )
  end east

  object west:
    lazy val stBlaise = Webcam(
      name = "St. Blaise",
      url = "https://lacdeneuchatel.roundshot.com/",
      reloadInMin = 0, // No need to reload for iframe
      footer = "https://lacdeneuchatel.roundshot.com",
      mainPageLink = Some("https://lacdeneuchatel.roundshot.com/"),
      webcamType = WebcamType.IframeWebcam
    )

    lazy val concise = Webcam(
      name = "Concise",
      url = "1558446770", // Windy webcam ID
      reloadInMin = 10,   // Refresh every 10 minutes
      footer = "https://windy.com",
      mainPageLink = Some("http://46.14.58.189/mjpg/video.mjpg"),
      webcamType = WebcamType.WindyWebcam
    )

    lazy val bielersee = Webcam(
      name = "Bielersee",
      url =
        "https://api.codetabs.com/v1/proxy?quest=https://bielersee.live/latestuploads/cams/hafenbiel.jpg",
      reloadInMin = 5,
      footer = "https://bielersee.live",
      webcamType = WebcamType.ImageWebcam
    )

    lazy val biseNoire = Webcam(
      name = "Bise Noire",
      url = "https://www.bisenoire.ch/webcam/Bisenoire_BIG_00.jpg",
      reloadInMin = 5,
      footer = "https://www.bisenoire.ch",
      mainPageLink = Some("https://www.bisenoire.ch/webcam/"),
      webcamType = WebcamType.ImageWebcam
    )
  end west

  object italy:
    lazy val dervio = Webcam(
      name = "Dervio",
      url = "1564004172", // Store just the Windy webcam ID
      reloadInMin = 10,   // Refresh every 10 minutes
      footer = "https://windy.com",
      mainPageLink =
        Some("https://vedetta.org/webcam/italia/lombardia/lecco/dervio- lazy valmadrera"),
      webcamType = WebcamType.WindyWebcam
    )

    lazy val cremia = Webcam(
      name = "Cremia",
      url = "1564003897", // Store just the Windy webcam ID
      reloadInMin = 10,   // Refresh every 10 minutes
      footer = "https://windy.com",
      mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
      webcamType = WebcamType.WindyWebcam
    )

    lazy val colico = Webcam(
      name = "Colico",
      url = "1564003197", // Store just the Windy webcam ID
      reloadInMin = 10,   // Refresh every 10 minutes
      footer = "https://windy.com",
      mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/colico-piano"),
      webcamType = WebcamType.WindyWebcam
    )

    lazy val lecco = Webcam(
      name = "Lecco",
      url = "1748447411", // Store just the Windy webcam ID
      reloadInMin = 10,   // Refresh every 10 minutes
      footer = "https://windy.com",
      mainPageLink = Some("https://vedetta.org/webcam/italia/lombardia/lecco/vista-lecco"),
      webcamType = WebcamType.WindyWebcam
    )

    lazy val domaso = Webcam(
      name = "Domaso",
      url = "https://domasocamping.com/webcam/panorama.jpg", // Store just the Windy webcam ID
      reloadInMin = 1,                                       // Refresh every 10 minutes
      footer = "https://domasocamping.com"
    )

    lazy val malcesine = Webcam(
      name = "Malcesine",
      url = "https://addicted-sports.com/fileadmin/webcam/gardasee/current/full.jpg",
      reloadInMin = 10,
      footer = "https://addicted-sports.com",
      mainPageLink = Some("https://gardasee.webcam/de/malcesine-webcams.html")
    )

    lazy val caporeamolo = Webcam(
      name = "Capo Reamol",
      url = "https://addicted-sports.com/fileadmin/webcam/caporeamol/current/full.jpg",
      reloadInMin = 10,
      footer = "https://addicted-sports.com",
      mainPageLink = Some("https://gardasee.webcam/de/limone-webcams.html")
    )

    lazy val torbole = Webcam(
      name = "Torbole",
      url =
        "https://windinfo.eu/fileadmin/user_upload/webcam_upload/gardasee/shaka-torbole-aktuell-1280.jpg",
      reloadInMin = 10,
      footer = "https://windinfo.eu",
      mainPageLink = Some("https://gardasee.webcam/de/torbole-webcams.html")
    )

    lazy val tignale = Webcam(
      name = "Tignale",
      url = "https://pradelafam.net/cam/cam.jpg",
      reloadInMin = 10,
      footer = "https://pradelafam.net",
      mainPageLink = Some("https://gardasee.webcam/de/tignale-webcams.html")
    )
  end italy

  object france:
    lazy val almanarreHyeres = Webcam(
      name = "Hyères - L'Almanarre",
      url = "https://www.vision-environnement.com/live/player/hyeres30.php",
      reloadInMin = 0,
      footer = "https://vision-environnement.com",
      webcamType = WebcamType.IframeWebcam
    )

    lazy val estagnets = Webcam(
      name = "Hyères - Les Estagnets",
      url = "https://www.vision-environnement.com/live/player/hyereskite.php",
      reloadInMin = 0,
      footer = "https://vision-environnement.com",
      webcamType = WebcamType.IframeWebcam
    )
    lazy val madrague  = Webcam(
      name = "Hyères - La Madrague",
      url = "https://www.vision-environnement.com/live/player/madrague0.php",
      reloadInMin = 10,
      footer = "https://vision-environnement.com",
      webcamType = WebcamType.IframeWebcam
    )
    lazy val carro     = Webcam(
      name = "Carro",
      url = "https://www.skaping.com/marseille/spot-de-carro/video",
      reloadInMin = 0, // No need to reload for iframe
      footer = "https://www.skaping.com",
      mainPageLink = Some("https://www.skaping.com/marseille/spot-de-carro/video"),
      webcamType = WebcamType.IframeWebcam
    )

    lazy val sixFoursLeBrusc = Webcam(
      name = "Six Fours - Le Brusc",
      url =
        "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html",
      reloadInMin = 5,
      footer = "https://www.winds-up.com",
      mainPageLink = Some(
        "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html"
      ),
      webcamType = WebcamType.ScrapedWebcam,
      scrapingConfig = Some(ScrapingConfig(
        pageUrl =
          "https://www.winds-up.com/spot-six-fours-le-brusc-windsurf-kitesurf-49-webcam-live.html",
        imageRegex = """/webcam/49/49_\d+_\.jpg""", // Match just the path, stop at word boundaries
        baseUrl = Some("https://img.winds-up.com")  // Complete base URL
      ))
    )

    lazy val leJai = Webcam(
      name = "Le Jaï",
      url = "https://img.winds-up.com/webcam/26/26_1734700800_.jpg",
      reloadInMin = 5,
      footer = "https://www.winds-up.com",
      mainPageLink = Some(
        "https://www.winds-up.com/spot-le-jai-windsurf-kitesurf-26-webcam-live.html"
      ),
      webcamType = WebcamType.ScrapedWebcam,
      scrapingConfig = Some(ScrapingConfig(
        pageUrl =
          "https://www.winds-up.com/spot-le-jai-windsurf-kitesurf-26-webcam-live.html",
        imageRegex = """/webcam/26/26_\d+_\.jpg""", // Match just the path, stop at word boundaries
        baseUrl = Some("https://img.winds-up.com")  // Complete base URL
      ))
    )

    lazy val grauDuRoiPlageSud = Webcam(
      name = "Grau du Roi - Plage Sud",
      url = "https://grauduroi.roundshot.com/plage/",
      reloadInMin = 0, // No need to reload for iframe
      footer = "https://letsgrau.com",
      mainPageLink = Some("https://letsgrau.com/webcam-grau-du-roi/"),
      webcamType = WebcamType.IframeWebcam
    )
  end france

  object winter:
    lazy val riemenstalden = Webcam(
      name = "Riemenstalden",
      url = "https://www.spilau.ch/webcam/spilau.jpg",
      reloadInMin = 5,
      footer = "https://www.spilau.ch"
    )

    lazy val wildspitz = Webcam(
      name = "Wildspitz",
      url = "https://wildspitz.roundshot.com",
      reloadInMin = 0,
      footer = "https://wildspitz.ch",
      mainPageLink = Some("https://wildspitz.roundshot.com/#"),
      webcamType = IframeWebcam
    )

    lazy val rigiRotstock = Webcam(
      name = "Rigi Rotstock",
      url = "https://feed.yellow.camera/rigi-rotstock",
      reloadInMin = 5,
      footer = "https://www.rigi.ch",
      webcamType = IframeWebcam
    )

    lazy val rigiKulm = Webcam(
      name = "Rigi Kulm",
      url = "https://rigi.roundshot.com",
      reloadInMin = 0,
      footer = "https://www.rigi.ch",
      mainPageLink = Some("https://rigi.roundshot.com/#"),
      webcamType = IframeWebcam
    )

    lazy val rigiScheidegg = Webcam(
      name = "Rigi Scheidegg",
      url = "https://feed.yellow.camera/rigi-scheidegg",
      reloadInMin = 0,
      footer = "https://www.rigi.ch",
      mainPageLink = Some("https://feed.yellow.camera/rigi-scheidegg"),
      webcamType = IframeWebcam
    )

    lazy val eggberge = Webcam(
      name = "Eggberge",
      url = "https://www.eggberge.ch/fileadmin/user_upload/images/webcam/webcam.jpg",
      reloadInMin = 5,
      footer = "https://www.eggberge.ch"
    )

    lazy val oberalppass = Webcam(
      name = "Oberalppass",
      url = "https://webcam.skiarena.ch/oberalppass/image.jpg",
      reloadInMin = 5,
      footer = "https://www.andermatt-sedrun-disentis.ch"
    )

    lazy val realp = Webcam(
      name = "Realp",
      url = "https://assets2.webcam.io/w/9GYx3z/latest_hd.jpg",
      reloadInMin = 5,
      footer = "https://www.andermatt-sedrun-disentis.ch"
    )

    lazy val naetschen = Webcam(
      name = "Nätschen",
      url = "https://webcam.skiarena.ch/naetschen/image.jpg",
      reloadInMin = 5,
      footer = "https://www.andermatt-sedrun-disentis.ch"
    )

  end winter
end webcams
