package pme123.windspotter

import upickle.default.*

// JSON (de)serialization for webcam configurations.
// Fields with default values are omitted when writing, so the JSON stays
// compact and hand-editable.
object ConfigJson:

  given ReadWriter[WebcamType] =
    readwriter[String].bimap(_.toString, WebcamType.valueOf)

  given ReadWriter[ScrapingConfig] = macroRW
  given ReadWriter[Webcam]         = macroRW
  given ReadWriter[WebcamGroup]    = macroRW
  given ReadWriter[WebcamConfig]   = macroRW

  def toJson(config: WebcamConfig): String =
    write(config, indent = 2)

  def toJson(configs: List[WebcamConfig]): String =
    write(configs)

  def fromJson(json: String): Either[String, WebcamConfig] =
    try Right(read[WebcamConfig](json))
    catch case e: Throwable => Left(e.getMessage)

  def configListFromJson(json: String): Either[String, List[WebcamConfig]] =
    try Right(read[List[WebcamConfig]](json))
    catch case e: Throwable => Left(e.getMessage)

end ConfigJson
