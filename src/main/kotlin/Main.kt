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

@Serializable
data class Item(val id: Int, val name: String)

fun main() {
    embeddedServer(Netty, port = 8080) {
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
            val items = mutableListOf<Item>(
                Item(1, "box"),
                Item(2, "book"),
                Item(3, "telephone"),
                Item(4, "bag"),
                Item(6, "pen"),
            )

            get("/items") {
                call.respond(items)
            }

            get("/items/{id}") {
                val idParam = call.parameters["id"]
                val id = idParam?.toIntOrNull()
                if (id == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id parameter"))
                }

                val item = items.find { it.id == id }
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
                if (items.any { it.id == newItem.id }) {

                    return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "Item with this id already exists"))
                }
                items.add(newItem)
                call.respond(HttpStatusCode.Created, newItem)
            }

            delete("/items") {
                val idParam = call.request.queryParameters["id"]
                val id = idParam?.toIntOrNull()
                if (id == null) {
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing or invalid id query parameter"))
                }
                val removed = items.removeIf { it.id == id }
                if (!removed) {
                    return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))
                }
                call.respond(HttpStatusCode.NoContent)
            }

        }
    }.start(wait = true)
}