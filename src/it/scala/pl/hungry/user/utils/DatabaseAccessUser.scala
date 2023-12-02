package pl.hungry.user.utils

import cats.effect.IO
import doobie.util.transactor.Transactor
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessUserFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessUser()
}

class DatabaseAccessUser() extends DatabaseAccess
