package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation {

  // 1. Configuración HTTP
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  // 2. Feeder desde archivo CSV con datos realistas
  val contactFeeder = csv("contacts.csv").random

  // 3. Escenario: login y creación de múltiples contactos desde CSV
  val scn = scenario("Login and Create Contacts from CSV")
    .exec(http("Login")
      .post("users/login")
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken"))
    )
    .repeat(10) { // Crea 10 contactos por usuario
      feed(contactFeeder)
        .exec(http("Create Contact")
          .post("contacts")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody(
            """{
              "firstName": "${firstName}",
              "lastName": "${lastName}",
              "birthdate": "${birthdate}",
              "email": "${email}",
              "phone": "${phone}",
              "street1": "${street1}",
              "street2": "${street2}",
              "city": "${city}",
              "stateProvince": "${stateProvince}",
              "postalCode": "${postalCode}",
              "country": "${country}"
            }"""
          )).asJson
          .check(status.is(201))
        )
    }

  // 4. Carga del escenario
  setUp(
    scn.inject(rampUsers(1).during(10)) // 1 usuario = 10 contactos
  ).protocols(httpConf)
}
