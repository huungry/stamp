package pl.hungry.auth.protocols

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.auth.domain._
import pl.hungry.auth.routers.in.LoginRequest

object AuthCodecs {
  import pl.hungry.user.protocols.UserCodecs._
  import pl.hungry.utils.refinements.RefinementsCodecs._

  implicit val loginRequestCodec: Codec[LoginRequest]   = deriveCodec
  implicit val jwtContentCodec: Codec[JwtContent]       = deriveCodec
  implicit val loginResponseCodec: Codec[LoginResponse] = deriveCodec

  implicit val userAgentCodec: Codec[UserAgent]       = deriveUnwrappedCodec
  implicit val refreshTokenCodec: Codec[RefreshToken] = deriveUnwrappedCodec
  implicit val jwtTokenCodec: Codec[JwtToken]         = deriveUnwrappedCodec
}
