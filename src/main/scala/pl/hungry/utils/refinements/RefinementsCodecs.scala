package pl.hungry.utils.refinements

import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import pl.hungry.utils.refinements.Refinements.Email

object RefinementsCodecs {
  implicit val nonEmptyStringDecoder: Decoder[NonEmptyString] = refinedDecoder
  implicit val nonEmptyStringEncoder: Encoder[NonEmptyString] = refinedEncoder

  implicit val posIntDecoder: Decoder[PosInt] = refinedDecoder
  implicit val posIntEncoder: Encoder[PosInt] = refinedEncoder

  implicit val emailEncoder: Encoder[Email] = refinedEncoder
  implicit val emailDecoder: Decoder[Email] = refinedDecoder
}
