package pl.hungry.auth.utils

import cats.effect.IO
import doobie.util.transactor.Transactor
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

class DatabaseAccessAuthFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessAuth()
}

class DatabaseAccessAuth() extends DatabaseAccess
