package pl.hungry.user

import cats.effect.IO
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import pl.hungry.auth.routers.AuthRouter.BearerEndpoint
import pl.hungry.auth.services.PasswordService
import pl.hungry.user.repositories.{UserRepository, UserRepositoryDoobie}
import pl.hungry.user.routers.UserRouter
import pl.hungry.user.services.{CreateUserService, FindMeService, UserInternalService}

final case class UserModule(routes: UserRouter, userInternalService: UserInternalService)

object UserModule {
  def make(
    transactor: Transactor[IO],
    passwordService: PasswordService,
    bearerEndpoint: BearerEndpoint
  ): UserModule = {
    val userRepository: UserRepository[ConnectionIO] = new UserRepositoryDoobie
    val createUserService: CreateUserService         = new CreateUserService(userRepository, passwordService, transactor)
    val findMeService: FindMeService                 = new FindMeService(userRepository, transactor)
    val userInternalService: UserInternalService     = new UserInternalService(userRepository)
    val userRouter                                   = new UserRouter(bearerEndpoint, createUserService, findMeService)

    UserModule(userRouter, userInternalService)
  }
}
