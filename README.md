Orama Cloud Kotlin Client
---

This repository contains Orama's Cloud Multiplatform Kotlin Client.
Developers can use this library to interact with Orama using Kotlin in multiple plataforms,

## Installing using Maven

To install, make sure to enable Maven repository and include the dependency in your build file `gradle.build.kts`.

```
repositories {
   mavenCentral()
}

dependencies {
   implementation "com.orama:oramasearch-client-kotlin:$OramaClientVersion"
}
```

## Usage

Performing full-text, vector, or hybrid search:

First, you need to do is to instantiate the client passing your `endpoint` and `apiKey`.
After that, use the `SearchParams` to prepare you query.
Now, just call the `client.search` method with the parameters and a proper callback handler.

```kotlin
val client = OramaClient(
    endpoint = "<ORAMA CLOUD URL>",
    apiKey = "<ORAMA CLOUD API KEY>"
)

launch {
    client.search(
        SearchParams.builder(
            term = "install",
            mode = Mode.FULLTEXT // Modes: FULLTEXT, VECTOR and HYBRID
        ).build(),
        object : SearchEventListener {
            override fun onComplete(results: SearchResponse) {
                // Do something with results 
            }

            override fun onError(error: String) {
                // Handle error
            }
        }
    )
}
```

Performing an answer session:

```kotlin
val client = OramaClient(
    endpoint = "<ORAMA CLOUD URL>",
    apiKey = "<ORAMA CLOUD API KEY>"
)

val answerParams = AnswerParams(
    userContext = null,
    oramaClient = client,
    inferenceType = InferenceType.DOCUMENTATION,
    initialMessages = mutableListOf<Message>()
)

val answerSession = AnswerSession(
    answerParams = answerParams,
    events = object : AnswerEventListener {
        override fun onStateChange(interactions: MutableList<Interaction>) {
            // Process list of interaction post-response
        }

        override fun onMessageChange(data: String) {
            // Process the incoming streaming 
        }
    }
)

runBlocking {
    answerSession.ask(AskParams(
        query = "What's the best movie to watch with the family?"
    ))
}
```

## Todo

- [ ] Include Tests & commonTest subset
- [ ] Search results: replace generic maps for proper serializable types

## License

[Apache 2.0](/LICENSE.md)

