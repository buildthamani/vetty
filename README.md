# Vetty

Compile-time JSON schema generation and runtime API response validation for Android.

Vetty generates JSON schemas from your data classes at compile time using KSP, then validates API responses against those schemas at runtime — catching contract violations before they cause crashes.

## Setup

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the BOM and dependencies:

```kotlin
// build.gradle.kts
dependencies {
    implementation(platform("com.github.buildthamani.vetty:vetty-bom:1.0.0"))
    implementation("com.github.buildthamani.vetty:vetty-retrofit")
    implementation("com.github.buildthamani.vetty:vetty-overlay") // optional UI
}
```

Apply the Gradle plugin:

```kotlin
// build.gradle.kts (app module)
plugins {
    id("app.thamani.vetty")
}

// or via classpath
buildscript {
    dependencies {
        classpath("com.github.buildthamani.vetty:vetty-gradle-plugin:1.0.0")
    }
}
```

## Usage

### 1. Annotate your response classes

```kotlin
@VettySchema(route = "/api/users", method = RouteMethod.GET)
@Serializable
data class UsersResponse(
    val users: List<User>
)

data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

### 2. Add the interceptor to OkHttp

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(VettyInterceptor())
    .build()
```

That's it. Vetty will automatically:
- Generate JSON schemas at compile time via KSP
- Load schemas when the interceptor is first created
- Validate every API response against its schema
- Report mismatches without crashing your app

### 3. Optional: Show validation UI

Add the overlay module to display schema violations in a debug overlay:

```kotlin
VettyOverlay {
    // your app content
}
```

## Modules

| Module | Description |
|---|---|
| `vetty-annotations` | `@VettySchema` annotation |
| `vetty-processor` | KSP processor for schema generation |
| `vetty-gradle-plugin` | Gradle plugin that wires KSP + schema resources |
| `vetty-core` | Schema loading and validation engine |
| `vetty-retrofit` | OkHttp/Retrofit interceptor |
| `vetty-presentation` | Shared UI components (Compose) |
| `vetty-overlay` | Debug overlay for displaying violations |
| `vetty-bom` | Bill of Materials for version alignment |

## Requirements

- Android API 26+
- Kotlin 2.x
- AGP 9+

## License

Apache License 2.0
