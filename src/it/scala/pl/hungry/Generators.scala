package pl.hungry

import eu.timepit.refined.api.Refined
import eu.timepit.refined.scalacheck.arbitraryRefType
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.utils.refinements.Refinements.EmailRestrictions

trait Generators {

  def emailGen: Gen[String Refined EmailRestrictions] = {
    val emailAuxGen: Arbitrary[String Refined EmailRestrictions] = arbitraryRefType(emailStringGenerator)
    emailAuxGen.arbitrary
  }

  def nonEmptyStringGen: Gen[NonEmptyString] = {
    val nonEmptyStringAuxGen: Arbitrary[NonEmptyString] = arbitraryRefType(nonEmptyStringGenerator)
    nonEmptyStringAuxGen.arbitrary
  }

  def posIntGen: Gen[PosInt] = {
    val posIntAuxGen: Arbitrary[PosInt] = arbitraryRefType(posIntGenerator)
    posIntAuxGen.arbitrary
  }

  private def nonEmptyStringGenerator: Gen[String] = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)
  private def emailStringGenerator: Gen[String] = for {
    username <- nonEmptyStringGen
    domain   <- Gen.oneOf("one.com", "yahoo.com", "xdd.com")
  } yield s"$username@$domain"
  private def posIntGenerator: Gen[Int] = Gen.posNum[Int]
}
