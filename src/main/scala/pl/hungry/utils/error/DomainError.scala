package pl.hungry.utils.error

import io.circe.syntax._
import io.circe.{Decoder, DecodingFailure, Encoder, Json}

trait DomainError {
  val message: String
}

final case class DecodeError(message: String) extends DomainError

object DomainErrorCodecs {
  implicit def errorEncoder[T <: DomainError]: Encoder[T] = Encoder.instance[T] { error =>
    Json.obj("message" -> error.message.asJson)
  }

  implicit def errorDecoder[T <: DomainError]: Decoder[T] = Decoder.instance[T] { cursor =>
    cursor.as[String].flatMap { _ =>
      Left(DecodingFailure("Unexpected decoder for DomainError", cursor.history))
    }
  }
}
