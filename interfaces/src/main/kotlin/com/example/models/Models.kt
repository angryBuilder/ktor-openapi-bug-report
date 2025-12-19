package com.example.models

/**
 * Data models that trigger bugs in Ktor OpenAPI plugin when used across module boundaries.
 *
 * Key problematic patterns:
 * 1. Interface with `val v: Any` property + @JvmInline value class implementations
 * 2. Sealed class with `val v: Any` property
 * 3. Deeply nested constraint structures
 */

// ============================================================================
// PATTERN 1: Interface with `val v: Any` + @JvmInline value class implementations
// ============================================================================

interface ConstraintFilterValue {
    val v: Any
}

@JvmInline
value class IntListConstraintFilterValue(val value: List<Int>) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class IntConstraintFilterValue(val value: Int) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class DoubleListConstraintFilterValue(val value: List<Double>) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class DoubleConstraintFilterValue(val value: Double) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class StringConstraintFilterValue(val value: String) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class StringListConstraintFilterValue(val value: List<String>) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class BoolConstraintFilterValue(val value: Boolean) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

@JvmInline
value class BoolListConstraintFilterValue(val value: List<Boolean>) : ConstraintFilterValue {
    override val v: Any
        get() = value
}

// ============================================================================
// PATTERN 2: Deeply nested constraint structure using the interface
// ============================================================================

data class ConstraintFilter(
    val operator: String?,
    val fieldname: String?,
    val value: ConstraintFilterValue?
)

data class ConstraintBody(
    val filters: List<ConstraintFilter>?
)

data class ConstraintChoice(
    val name: String?,
    val value: String?
)

data class ParameterConstraints(
    val default: String?,
    val value_field: String?,
    val name_field: String?,
    val url: String?,
    val body: ConstraintBody?,
    val method: String?,
    val choices: List<ConstraintChoice>?
)

data class DefinitionParameter(
    val type: String?,
    val id: String?,
    val name: String?,
    val constraints: ParameterConstraints?,
    val help: String?,
    val input_name: String?,
)

data class TaskDefinition(
    val id: String?,
    val description: String?,
    val action_type: String?,
    val valid: Boolean?,
    val group_id: String?,
    val name: String?,
    val parameters: Array<DefinitionParameter>?
)

// ============================================================================
// PATTERN 3: Sealed class with `val v: Any` property
// ============================================================================

@JvmInline
value class LocationName(val name: String)

@JvmInline
value class ParamName(val name: String)

@JvmInline
value class ParamLabel(val label: String)

enum class ParamType {
    String, Int, Double, Boolean, Location
}

data class LocationType(val type_id: Int)

data class LocationDefinition(
    val name: LocationName,
    val locationType: LocationType
)

sealed class ParamValue(
    val v: Any,
    val type: ParamType
)

class StringParamValue(v: String) : ParamValue(v, ParamType.String)
class IntParamValue(v: Int) : ParamValue(v, ParamType.Int)
class BooleanParamValue(v: Boolean) : ParamValue(v, ParamType.Boolean)
class LocationParamValue(v: LocationDefinition) : ParamValue(v, ParamType.Location)

data class TaskParameter(
    val name: ParamName,
    val label: ParamLabel?,
    val value: ParamValue
)

// ============================================================================
// PATTERN 4: Aggregate types
// ============================================================================

@JvmInline
value class TaskName(val name: String)

data class TaskPriority(val priority: Int)
data class HighTaskPriority(val high_priority: Boolean)

data class TaskRequest(
    val name: TaskName,
    var assignee: String?,
    val parameters: MutableList<TaskParameter>,
    var prio: TaskPriority? = null,
    var high_prio: HighTaskPriority? = null,
    var startDate: String? = null,
)

data class ScheduledTask(
    var prio: TaskPriority? = null,
    var high_prio: HighTaskPriority? = null,
    var task_id: String,
    val schedule_guid: String,
    val task_name: TaskName,
    val schedule_id: Int,
    var assignee: String?,
    var startDate: String? = null,
)

// ============================================================================
// PATTERN 5: Device with parameters
// ============================================================================

data class DeviceStatus(
    val device_name: String?,
    val state: String?,
    val battery_percentage: Double?,
)

data class Device(
    val id: Int?,
    val ip: String?,
    val group_id: String?,
    val status: DeviceStatus?,
    val parameters: Array<DefinitionParameter>?
)

// ============================================================================
// API Response types
// ============================================================================

data class DeviceListResponse(
    val devices: List<Device>
)

data class TaskDefinitionResponse(
    val definition: TaskDefinition
)

data class ScheduleTaskRequest(
    val task: TaskRequest
)

data class ScheduleTaskResponse(
    val scheduled: ScheduledTask
)
