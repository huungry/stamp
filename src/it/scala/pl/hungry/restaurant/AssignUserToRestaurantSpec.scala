package pl.hungry.restaurant

import io.circe.syntax._
import pl.hungry.BaseItTest
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.{Position, RestaurantUser}
import pl.hungry.restaurant.routers.in.AssignUserToRestaurantRequest
import pl.hungry.restaurant.utils.{DatabaseAccessRestaurant, DatabaseAccessRestaurantFactory, RestaurantGenerators}

class AssignUserToRestaurantSpec extends BaseItTest with RestaurantGenerators {

  import pl.hungry.restaurant.protocols.RestaurantCodecs._

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessRestaurant](appModules, new DatabaseAccessRestaurantFactory)
  }

  it should "not assign user to restaurant by employee" in new TestCase {
    val (_, ownerToken, restaurant)       = endpoints.createUserAndRestaurant()
    val (createEmployeeRequest, employee) = endpoints.registerUser()
    val (_, userToBeAssigned)             = endpoints.registerUser()
    val restaurantUser                    = endpoints.assignUserToRestaurant(ownerToken, employee.id, restaurant.id, Position.Employee)
    db.findActiveRestaurantUser(employee.id).value shouldBe (restaurant.id, employee.id, restaurantUser.position)

    val employeeToken = endpoints.login(createEmployeeRequest.email, createEmployeeRequest.password)
    val request       = AssignUserToRestaurantRequest(userId = userToBeAssigned.id, position = Position.Employee)

    endpoints
      .sendPostRequest(
        path = s"http://test.com/restaurants/${restaurant.id.value}/users",
        body = request.asJson.noSpaces,
        bearerOpt = Some(employeeToken)
      )
      .body
      .shouldIncludeErrorMessage("Only managers can assign users")

    db.findActiveRestaurantUser(userToBeAssigned.id) shouldBe None
  }

  it should "assign user to restaurant by manager" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, user)              = endpoints.registerUser()
    val request                = AssignUserToRestaurantRequest(userId = user.id, position = Position.Employee)

    val response = endpoints.sendPostRequest(
      path = s"http://test.com/restaurants/${restaurant.id.value}/users",
      body = request.asJson.noSpaces,
      bearerOpt = Some(token)
    )

    response.body.shouldDeserializeTo[RestaurantUser]: Unit

    db.findActiveRestaurantUser(user.id).value shouldBe (restaurant.id, user.id, request.position)
  }
}
