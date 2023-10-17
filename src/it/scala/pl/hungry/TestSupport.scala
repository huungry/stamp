package pl.hungry

import io.circe.{Decoder, parser}
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers.include
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.reflect.ClassTag

/** Based on https://github.com/softwaremill/bootzooka */
trait TestSupport extends EitherValues {
  implicit class RichEither(result: Either[String, String]) {
    def shouldDeserializeTo[T: Decoder: ClassTag]: T =
      result.flatMap(parser.parse).flatMap(_.as[T]).value

    def shouldIncludeErrorMessage(message: String): Unit =
      result.swap.value should include(message)
  }
}
