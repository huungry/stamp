package pl.hungry.auth.domain

final case class LoginResponse(token: JwtToken, refreshToken: Option[RefreshToken])
