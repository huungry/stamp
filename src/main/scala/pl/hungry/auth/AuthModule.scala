package pl.hungry.auth

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.repositories.{AuthRepository, AuthRepositoryDoobie}
import pl.hungry.auth.routers.AuthRouter
import pl.hungry.auth.services.{AuthService, LoginService, PasswordService}

final case class AuthModule(routes: AuthRouter, passwordService: PasswordService)

object AuthModule {
  def make(transactor: Transactor[IO]): AuthModule = {
    val authRepository: AuthRepository[ConnectionIO] = new AuthRepositoryDoobie
    val authService: AuthService                     = new AuthService(authRepository, transactor)
    val passwordService: PasswordService             = new PasswordService
    val loginService: LoginService                   = new LoginService(authService, authRepository, passwordService, transactor)
    val authRouter: AuthRouter                       = new AuthRouter(authService, loginService)

    AuthModule(authRouter, passwordService)
  }
}
