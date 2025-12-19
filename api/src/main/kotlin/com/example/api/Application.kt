package com.example.api

import com.example.controllers.DeviceController
import com.example.models.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Multi-module Ktor application demonstrating bugs in OpenAPI plugin.
 *
 * Module dependency chain: api -> controllers -> interfaces
 * This cross-module dependency may trigger the bugs that don't appear in single-file projects.
 *
 * Bug 1 (Primary): Kotlin daemon crash with AssertionError:
 *   "Cannot add a performance measurements because it's already finalized"
 *
 * Bug 2 (Secondary): After daemon fallback, StackOverflowError in
 *   JsonSchema.asJsonSchema() when analyzing complex type hierarchies.
 */
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val controller = DeviceController()

    routing {
        // Endpoint returning Device with the problematic interface hierarchy
        get("/api/devices") {
            val response = controller.getDevices()
            call.respond(response)
        }

        // Endpoint using task definition with multiple filter value types
        get("/api/tasks/definitions") {
            val response = controller.getTaskDefinition()
            call.respond(response)
        }

        // Endpoint using sealed class with `val v: Any`
        get("/api/scheduled") {
            val response = controller.getScheduledTask()
            call.respond(response)
        }

        // Alternative routes that construct responses inline (like the production code)
        get("/api/devices/inline") {
            val response = DeviceListResponse(
                devices = listOf(
                    Device(
                        id = 1,
                        ip = "192.168.1.100",
                        group_id = "group-1",
                        status = DeviceStatus(
                            device_name = "Device-1",
                            state = "Ready",
                            battery_percentage = 85.5
                        ),
                        parameters = arrayOf(
                            DefinitionParameter(
                                type = "string",
                                id = "name",
                                name = "Device Name",
                                constraints = ParameterConstraints(
                                    default = null,
                                    value_field = "guid",
                                    name_field = "name",
                                    url = "/api/locations",
                                    body = ConstraintBody(
                                        filters = listOf(
                                            ConstraintFilter(
                                                operator = "eq",
                                                fieldname = "type",
                                                value = StringConstraintFilterValue("location")
                                            )
                                        )
                                    ),
                                    method = "GET",
                                    choices = null
                                ),
                                help = null,
                                input_name = "device_name"
                            )
                        )
                    )
                )
            )
            call.respond(response)
        }

        // Simple endpoint (works fine)
        get("/api/health") {
            call.respondText("OK")
        }
    }
}
