import io.circe.generic.auto._
import io.circe.parser._

class NlpDecoder {

  def decodeNlpResponse(body: String): Either[String, CoreNlpResult] = {
    decode[CoreNlpResult](body) match {
      case Left(error) => Left(s"Failed to decode response with error: $error")
      case Right(value) => Right(value)
    }
  }

  def decodeCoreferenceResult(body: String): Either[String, CoreferenceResult] = {
    decode[CoreferenceResult](body) match {
      case Left(error) => Left(s"Failed to decode response with error: $error")
      case Right(value) => Right(value)
    }
  }
}
