package pl.hungry.restaurant

import pl.hungry.BaseItTest
import pl.hungry.restaurant.domain.Restaurant

class ListRestaurantSpec extends BaseItTest with RestaurantGenerators {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._

  it should "list user's related restaurants" in {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    endpoints.createUserAndRestaurant(): Unit

    val listResult =
      endpoints.sendGetRequest(path = "http://test.com/restaurants", bearerOpt = Some(token)).body.shouldDeserializeTo[List[Restaurant]]

    listResult should contain theSameElementsAs List(restaurant)
  }
}
