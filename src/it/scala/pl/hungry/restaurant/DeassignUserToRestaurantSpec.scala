package pl.hungry.restaurant

import pl.hungry.BaseItTest
import pl.hungry.main.AppBuilder.AppModules
import pl.hungry.restaurant.domain.Position

class DeassignUserToRestaurantSpec extends BaseItTest with RestaurantGenerators {

  abstract class TestCase(appModules: AppModules = defaultTestAppModules) {
    val (db, endpoints) = buildTestCaseSetup[DatabaseAccessRestaurant](appModules, new DatabaseAccessRestaurantFactory)
  }

  it should "not deassign user from restaurant by employee" in new TestCase {
    val (owner, token, restaurant)        = endpoints.createUserAndRestaurant()
    val (createEmployeeRequest, employee) = endpoints.registerUser()
    val employeeToken                     = endpoints.login(createEmployeeRequest.email, createEmployeeRequest.password)
    val restaurantUser                    = endpoints.assignUserToRestaurant(token, employee.id, restaurant.id, Position.Employee)
    db.findActiveRestaurantUser(employee.id).value shouldBe (restaurant.id, employee.id, restaurantUser.position)

    endpoints
      .sendDeleteRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/users/${owner.id.value}", bearerOpt = Some(employeeToken))
      .body
      .shouldIncludeErrorMessage("Only managers can deassign users")

    db.findActiveRestaurantUser(owner.id).value shouldBe (restaurant.id, owner.id, Position.Manager)
  }

  it should "deassign user from restaurant by manager" in new TestCase {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, user)              = endpoints.registerUser()
    val restaurantUser         = endpoints.assignUserToRestaurant(token, user.id, restaurant.id, Position.Employee)
    db.findActiveRestaurantUser(user.id).value shouldBe (restaurant.id, user.id, restaurantUser.position)

    endpoints
      .sendDeleteRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/users/${user.id.value}", bearerOpt = Some(token))
      .code
      .code shouldBe 204

    db.findActiveRestaurantUser(user.id) shouldBe None
  }
}
