package pl.hungry.auth.protocols

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import pl.hungry.auth.domain.JwtToken
import pl.hungry.auth.routers.in.LoginRequest

object AuthCodecs {
  import pl.hungry.user.protocols.UserCodecs._

  implicit val loginRequestCodec: Codec[LoginRequest] = deriveCodec
  implicit val jwtTokenCodec: Codec[JwtToken]         = deriveCodec
}
