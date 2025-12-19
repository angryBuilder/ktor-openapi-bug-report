package com.example.controllers

import com.example.models.*

/**
 * Controller that uses types from the interfaces module.
 * This simulates the cross-module dependency pattern in the production codebase.
 */
class DeviceController {

    fun getDevices(): DeviceListResponse {
        return DeviceListResponse(
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
    }

    fun getTaskDefinition(): TaskDefinitionResponse {
        return TaskDefinitionResponse(
            definition = TaskDefinition(
                id = "def-1",
                description = "Test task definition",
                action_type = "move",
                valid = true,
                group_id = "group-1",
                name = "Move to Location",
                parameters = arrayOf(
                    DefinitionParameter(
                        type = "location",
                        id = "target",
                        name = "Target Location",
                        constraints = ParameterConstraints(
                            default = null,
                            value_field = "guid",
                            name_field = "name",
                            url = "/api/locations",
                            body = ConstraintBody(
                                filters = listOf(
                                    ConstraintFilter(
                                        operator = "in",
                                        fieldname = "type_id",
                                        value = IntListConstraintFilterValue(listOf(0, 7))
                                    )
                                )
                            ),
                            method = "GET",
                            choices = null
                        ),
                        help = "Select target location",
                        input_name = "target_location"
                    )
                )
            )
        )
    }

    fun getScheduledTask(): ScheduleTaskResponse {
        return ScheduleTaskResponse(
            scheduled = ScheduledTask(
                prio = TaskPriority(50),
                high_prio = HighTaskPriority(false),
                task_id = "task-1",
                schedule_guid = "guid-123",
                task_name = TaskName("Transport Task"),
                schedule_id = 1,
                assignee = "Worker-1",
                startDate = null
            )
        )
    }
}
