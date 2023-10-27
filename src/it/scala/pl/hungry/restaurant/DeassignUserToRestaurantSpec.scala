package pl.hungry.restaurant

import pl.hungry.BaseItTest
import pl.hungry.restaurant.domain.Position

class DeassignUserToRestaurantSpec extends BaseItTest with RestaurantGenerators {

  it should "not deassign user from restaurant by employee" in {
    val (owner, token, restaurant)        = endpoints.createUserAndRestaurant()
    val (createEmployeeRequest, employee) = endpoints.registerUser()
    val employeeToken                     = endpoints.login(createEmployeeRequest.email, createEmployeeRequest.password)
    val restaurantUser                    = endpoints.assignUserToRestaurant(token, employee.id, restaurant.id, Position.Employee)
    db.findActiveRestaurantUser(employee.id).value shouldBe (restaurant.id, employee.id, restaurantUser.position): Unit

    endpoints
      .sendDeleteRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/users/${owner.id.value}", bearerOpt = Some(employeeToken))
      .body
      .shouldIncludeErrorMessage("Only managers can deassign users"): Unit

    db.findActiveRestaurantUser(owner.id).value shouldBe (restaurant.id, owner.id, Position.Manager)
  }

  it should "deassign user from restaurant by manager" in {
    val (_, token, restaurant) = endpoints.createUserAndRestaurant()
    val (_, user)              = endpoints.registerUser()
    val restaurantUser         = endpoints.assignUserToRestaurant(token, user.id, restaurant.id, Position.Employee)
    db.findActiveRestaurantUser(user.id).value shouldBe (restaurant.id, user.id, restaurantUser.position): Unit

    endpoints
      .sendDeleteRequest(path = s"http://test.com/restaurants/${restaurant.id.value}/users/${user.id.value}", bearerOpt = Some(token))
      .code.code shouldBe 204: Unit

    db.findActiveRestaurantUser(user.id) shouldBe None
  }
}
