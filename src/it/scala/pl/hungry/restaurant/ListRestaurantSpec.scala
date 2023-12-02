package pl.hungry.restaurant

import pl.hungry.BaseItTest
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.Restaurant
import pl.hungry.restaurant.utils.{DatabaseAccessRestaurant, DatabaseAccessRestaurantFactory, RestaurantGenerators}

class ListRestaurantSpec extends BaseItTest with RestaurantGenerators {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessRestaurant](appModules, new DatabaseAccessRestaurantFactory)
  }

  it should "list user's related restaurants" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    endpoints.createUserAndRestaurant(): Unit

    val listResult: List[Restaurant] =
      endpoints.sendGetRequest(path = "http://test.com/restaurants", bearerOpt = Some(token)).body.shouldDeserializeTo[List[Restaurant]]

    listResult should contain theSameElementsAs List(restaurant)
  }
}
