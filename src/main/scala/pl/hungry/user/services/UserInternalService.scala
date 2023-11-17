package pl.hungry.user.services

import doobie.ConnectionIO
import pl.hungry.user.domain.{UserId, UserView}
import pl.hungry.user.repositories.UserRepository

class UserInternalService(userRepository: UserRepository[ConnectionIO]) {
  def find(userId: UserId): ConnectionIO[Option[UserView]] = userRepository.find(userId).map(_.map(UserView.from))
}
