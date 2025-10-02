package org.example


import kotlinx.serialization.Serializable
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
///
import org.example.password_bcrypt as Password_bcrypt

@Serializable
data class Item(val id: Int, val name: String, val passwod: String)

@Serializable
data class Passwod_html( val passwod: String)

object Products : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
}

fun main() {

    fun addItem(id: Int, name: String) {
        transaction {
            Products.insert {
                it[this.id] = id
                it[this.name] = name
            }
        }
    }
    fun deleteItemById(productId: Int) {
        transaction {
            Products.deleteWhere { Products.id eq productId }
        }
    }
    fun infoItem(){
        transaction {
            for (product in Products.selectAll()) {
                println("${product[Products.id]} - ${product[Products.name]}")
            }
        }
    }
    fun infoItem(id: Int){
        transaction {
            val product = Products.select { Products.id eq id }.singleOrNull()

            if (product != null) {
                val name = product[Products.name]
                println("$id - $name")
            } else {
                println("Product with id=$id not found")
            }
        }
    }
    fun doesTheItemExist(id: Int): Boolean {
        val product = Products.select { Products.id eq id }.singleOrNull()

        if (product != null) {
            return true
        } else {
            return false
        }
    }

    embeddedServer(Netty, port = 8080) {

        Database.connect(
            url = "jdbc:postgresql://localhost:5432/dbstore",
            driver = "org.postgresql.Driver",
            user = "storekeeper",
            password = "1234561"
        )

        install(StatusPages){
            exception<Throwable> { call, cause ->
                    call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
            }
            status(HttpStatusCode.NotFound) { call, status ->
                call.respondText(text = "404: Page Not Found", status = status)
            }
        }
        install(ContentNegotiation) {
            json()
        }

        routing {

            var passwod_main = Password_bcrypt()

            get("/items") {
                call.respond(infoItem())
            }

            get("/items/{id}") {
                val idParam = call.parameters["id"]
                val id = idParam?.toIntOrNull()
                if (id == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id parameter"))
                }

                val item = infoItem(id)
                if (item == null) {
                    return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                }
                call.respond(item)
            }

            post("/items") {
                val newItem = try {
                    call.receive<Item>()
                } catch(e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
                }
                if (passwod_main.isValid(newItem.passwod)) {

                    if (doesTheItemExist(newItem.id) != true) {
                        return@post call.respond(
                            HttpStatusCode.Conflict,
                            mapOf("error" to "Item with this id already exists")
                        )
                    }
                    addItem(newItem.id, newItem.name)
                    call.respond(HttpStatusCode.Created, newItem)

                }
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
            }

            post("/adding_a_password"){
                val newUser = try {
                    call.receive<Passwod_html>()
                }catch(e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
                }
                passwod_main.hashPassword(newUser.passwod)
            }

            delete("/items") {
                val newUser = try {
                    call.receive<Passwod_html>()
                }catch(e: Exception) {
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
                }
                if (passwod_main.isValid(newUser.passwod))
                {
                    val idParam = call.request.queryParameters["id"]
                    val id = idParam?.toIntOrNull()
                    if (id == null) {
                        return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id query parameter"))
                    }
                    deleteItemById(id);

                    call.respond(HttpStatusCode.NoContent)
                }
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON"))
            }

        }
    }.start(wait = true)
}