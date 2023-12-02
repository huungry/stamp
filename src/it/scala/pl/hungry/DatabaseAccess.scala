package pl.hungry

import cats.effect.IO
import doobie.util.transactor.Transactor

trait DatabaseAccess

trait DatabaseAccessFactory {
  def create(transactor: Transactor[IO]): DatabaseAccess
}
