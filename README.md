# Ktor OpenAPI Plugin - Bug Report

## Summary

The Ktor OpenAPI Gradle plugin (version 3.3.3) crashes with an `AssertionError` when generating OpenAPI specifications in multi-module projects where types are defined in dependency modules. More info in ticket [KTOR-9147](https://youtrack.jetbrains.com/issue/KTOR-9147)

**Primary Bug**: Kotlin daemon crash with `AssertionError: Cannot add a performance measurements because it's already finalized`

## Environment

- **Kotlin**: 2.2.20
- **Ktor**: 3.3.3
- **io.ktor.plugin**: 3.3.3
- **Gradle**: 8.11+
- **JDK**: 21

## Steps to Reproduce

1. Clone this project
2. Run: `./gradlew clean :api:buildOpenApi`
3. Observe the daemon crash error

## Project Structure

This is a multi-module project that reproduces the bug:

```
ktor-openapi-bug-report/
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Includes: interfaces, controllers, api
├── interfaces/               # Module with type definitions
│   └── src/main/kotlin/com/example/models/Models.kt
├── controllers/              # Module depending on interfaces (api dependency)
│   └── src/main/kotlin/com/example/controllers/DeviceController.kt
└── api/                      # Module with OpenAPI plugin and routes
    └── src/main/kotlin/com/example/api/Application.kt
```

**Dependency chain**: `api` -> `controllers` -> `interfaces`

## Expected Behavior

The OpenAPI specification should be generated successfully without daemon crashes.

## Actual Behavior

The Kotlin compiler daemon crashes with:

```
e: Daemon compilation failed: null
Caused by: java.lang.AssertionError: Cannot add a performance measurements because it's already finalized
    at org.jetbrains.kotlin.util.PerformanceManager.ensureNotFinalizedAndSameThread(PerformanceManager.kt:382)
    at org.jetbrains.kotlin.util.PerformanceManager.measureSideTime$compiler_common(PerformanceManager.kt:305)
    at org.jetbrains.kotlin.util.PerformanceManagerKt.tryMeasureSideTime(PerformanceManager.kt:406)
    at org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClass$Factory.create$frontend_common_jvm(VirtualFileKotlinClass.kt:73)
    ...
    at io.ktor.openapi.model.JsonSchema$Companion.asJsonSchema(JsonSchema.kt:77)
    at io.ktor.openapi.OpenApiExtension.saveSpecification(OpenApiExtension.kt:59)
```

The build may succeed after falling back to non-daemon compilation, but the daemon crash is a bug that should be fixed.

## Root Cause Analysis

The bug occurs when the Ktor OpenAPI plugin tries to resolve types from compiled JAR dependencies:

1. `OpenApiExtension.saveSpecification()` is called during compilation
2. It invokes `JsonSchema.asJsonSchema()` to generate schema definitions
3. When resolving types from dependency modules (compiled JARs), the FIR symbol provider tries to load class metadata via `VirtualFileKotlinClass`
4. `PerformanceManager.measureSideTime()` is called, but the manager has already been finalized
5. This throws `AssertionError: Cannot add a performance measurements because it's already finalized`

The issue is that the OpenAPI plugin accesses compiler internals (`PerformanceManager`) at an inappropriate lifecycle stage when processing types from dependency JARs.

## Key Insight

**The bug only manifests in multi-module builds** where:
- Types are defined in one module (e.g., `:interfaces`)
- Routes using those types are defined in another module (e.g., `:api`) with the OpenAPI plugin
- The types are compiled into JAR files before the OpenAPI plugin runs

Single-file projects or single-module projects do NOT trigger this bug.

## Type Patterns in the Reproduction

### Pattern 1: Interface with `val v: Any` + @JvmInline implementations
```kotlin
interface ConstraintFilterValue {
    val v: Any
}

@JvmInline
value class StringConstraintFilterValue(val value: String) : ConstraintFilterValue {
    override val v: Any
        get() = value
}
```

### Pattern 2: Sealed class with `val v: Any`
```kotlin
sealed class ParamValue(
    val v: Any,
    val type: ParamType
)
```

### Pattern 3: Nested structures referencing the interface
```kotlin
data class ConstraintFilter(
    val operator: String?,
    val fieldname: String?,
    val value: ConstraintFilterValue?  // References interface with `val v: Any`
)
```

## Related

In larger production projects, a secondary bug may occur after the daemon fallback:
- `StackOverflowError` in `JsonSchema.asJsonSchema()` due to infinite type recursion
- This happens when types have circular references or `Any` type properties
