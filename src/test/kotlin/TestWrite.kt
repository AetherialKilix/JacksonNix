package net.aetherialkilix.jackson.nix

fun main() {
    // these are the default settings (just to show the options)
    val mapper = NixMapper()
        .enable(NixMapperFeature.Serialization.COLLAPSE_OBJECTS)
        .enable(NixMapperFeature.Serialization.MULTILINE_STRING)
        .enable(NixMapperFeature.Serialization.WARN_ON_BINARY_DATA) // binary Base64 data breaks COLLAPSE_OBJECTS
        .printSpaceIndented(2)

    mapper.writeValue(System.out, mapOf(
        "message" to "Hello!",
        "modules" to mapOf("custom" to mapOf("enabled" to true)),
        "my" to mapOf(
            "demoObject" to mapOf(
                "says" to """
                    lorem ipsum
                    dolor sit amet
                """.trimIndent(),
                "when" to 69
            )
        )
    ))
}