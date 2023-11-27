package pl.hungry.user

import cats.effect.IO
import doobie.util.transactor.Transactor
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

import scala.annotation.unused

class DatabaseAccessUserFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessUser(transactor)
}

class DatabaseAccessUser(@unused xa: Transactor[IO]) extends DatabaseAccess
