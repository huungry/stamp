package pl.hungry.restaurant

import org.scalacheck.{Arbitrary, Gen}
import pl.hungry.Generators
import pl.hungry.restaurant.domain.{RestaurantEmail, RestaurantName}
import pl.hungry.restaurant.routers.in.CreateRestaurantRequest

trait RestaurantGenerators extends Generators {
  def createRestaurantRequestGen: Gen[CreateRestaurantRequest] = {
    val createRestaurantRequestAuxGen: Arbitrary[CreateRestaurantRequest] = Arbitrary {
      for {
        email <- emailGen
        name  <- nonEmptyStringGen
      } yield CreateRestaurantRequest(RestaurantEmail(email), RestaurantName(name))
    }

    createRestaurantRequestAuxGen.arbitrary
  }
}
