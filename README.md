Orama Cloud Kotlin Client
---

## Installing using Maven

To install, make sure to enable Maven repository and include the dependency in your build file `gradle.build.kts`.

```
repositories {
   mavenCentral()
}

dependencies {
   implementation "com.orama:oramasearch-client-kotlin:$kotlin_client_version"
}
```

## Usage

First, you need to do is to instantiate the client passing your `endpoint` and `apiKey`.
After that, use the `SearchParams` to prepare you query.
Now, just call the `client.search` method with the parameters and a proper callback handler.

```kotlin
val client = OramaClient(
    endpoint = "https://cloud.orama.run/v1/indexes/orama-docs-bzo330",
    apiKey = "NKiqTJnwnKsQCdxN7RyOBJgeoW5hJ594"
)

val searchParams = ClientSearchParams.builder(
    term = "integrate Orama",
    mode = ClientSearchParams.Mode.FULLTEXT
)
    .limit(10)
    .offset(0)
    .build()

client.search(searchParams) { response, error ->
    if (!error) {
        // ... your code
    }
}
```