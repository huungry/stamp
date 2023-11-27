package pl.hungry.auth

import cats.effect.IO
import doobie.util.transactor.Transactor
import pl.hungry.{DatabaseAccess, DatabaseAccessFactory}

import scala.annotation.unused

class DatabaseAccessAuthFactory extends DatabaseAccessFactory {
  override def create(transactor: Transactor[IO]): DatabaseAccess =
    new DatabaseAccessAuth(transactor)
}

class DatabaseAccessAuth(@unused xa: Transactor[IO]) extends DatabaseAccess
