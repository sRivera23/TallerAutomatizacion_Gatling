package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation {

  // 1. Configuración HTTP
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  // 2. Feeder para generar datos únicos de contacto
  val contactFeeder = Iterator.continually(
    Map(
      "firstName" -> s"Nombre${scala.util.Random.nextInt(1000)}",
      "lastName" -> s"Apellido${scala.util.Random.nextInt(1000)}",
      "birthdate" -> "1970-01-01",
      "email" -> (s"user${System.currentTimeMillis()}${scala.util.Random.nextInt(1000)}@fake.com"),
      "phone" -> (s"800${scala.util.Random.nextInt(9999999).toString.padTo(7, '0')}"),
      "street1" -> "1 Main St.",
      "street2" -> "Apartment A",
      "city" -> "Anytown",
      "stateProvince" -> "KS",
      "postalCode" -> "12345",
      "country" -> "USA"
    )
  )

  // 3. Escenario: login y creación de contacto
  val scn = scenario("Login and Create Unique Contact")
    .exec(http("Login")
      .post("users/login")
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken"))
    )
    .feed(contactFeeder) // Alimentar datos únicos
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

  // 4. Carga del escenario
  setUp(
    scn.inject(rampUsers(10).during(20))
  ).protocols(httpConf)
}
