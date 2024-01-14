package pl.hungry.auth

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.LoggerFactory
import pl.hungry.auth.repositories.{AuthRepository, AuthRepositoryDoobie, UserRefreshTokenRepository, UserRefreshTokenRepositoryDoobie}
import pl.hungry.auth.routers.AuthRouter
import pl.hungry.auth.services.{AuthService, LoginService, PasswordService, RefreshTokenService}
import pl.hungry.main.AppConfig.JwtConfig

final case class AuthModule(routes: AuthRouter, passwordService: PasswordService)

object AuthModule {
  def make(transactor: Transactor[IO], config: JwtConfig)(implicit logger: LoggerFactory[IO]): AuthModule = {
    val authRepository: AuthRepository[ConnectionIO]                         = new AuthRepositoryDoobie
    val userRefreshTokenRepository: UserRefreshTokenRepository[ConnectionIO] = new UserRefreshTokenRepositoryDoobie
    val authService: AuthService                                             = new AuthService(authRepository, transactor, config)
    val passwordService: PasswordService                                     = new PasswordService
    val refreshTokenService: RefreshTokenService                             = new RefreshTokenService(userRefreshTokenRepository, transactor)
    val loginService: LoginService = new LoginService(authService, authRepository, passwordService, refreshTokenService, transactor)
    val authRouter: AuthRouter     = new AuthRouter(authService, loginService)

    AuthModule(authRouter, passwordService)
  }
}
