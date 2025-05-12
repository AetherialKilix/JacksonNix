package net.aetherialkilix.jackson.nix.gen

import net.aetherialkilix.jackson.nix.NixGenerator

sealed class GeneratorToken(
    val write: (NixGenerator) -> Unit,
    val stringRepresentation: () -> String
) {
    object START_ARRAY : GeneratorToken(
        { it.prettyPrinter.writeStartArray(it) },
        { "StartArray" }
    )
    object END_ARRAY : GeneratorToken(
        { it.prettyPrinter.writeEndArray(it, 0) },
        { "EndArray" }
    )
    object START_OBJECT : GeneratorToken(
        { it.prettyPrinter.writeStartObject(it) },
        { "StartObject" }
    )
    class END_OBJECT(val entries: Int) : GeneratorToken(
        { it.prettyPrinter.writeEndObject(it, entries) },
        { "EndObject($entries)" }
    )
    object NAME_VALUE_SEPERATOR : GeneratorToken(
        { it.prettyPrinter.writeObjectFieldValueSeparator(it) },
        { "NameValueSeparator" }
    )
    object ARRAY_VALUE_SEPERATOR : GeneratorToken(
        { it.prettyPrinter.writeArrayValueSeparator(it) },
        { "ArrayValueSeparator" }
    )
    object OBJECT_ENTRY_SEPERATOR : GeneratorToken(
        { it.prettyPrinter.writeObjectEntrySeparator(it) },
        { "ObjectEntrySeparator" }
    )
    class NAME(val name: String) : GeneratorToken(
        { it.writeRaw(name) },
        { "PropertyName($name)" }
    )
    open class VALUE(write: (NixGenerator) -> Unit, stringRepresentation: () -> String) :
        GeneratorToken(write, stringRepresentation)
    {
        constructor(raw: String) : this(
            { it.writeRaw(raw) },
            { "Value($raw)" }
        )
    }
    class MULTILINE_VALUE(raw: String?, supplier: (() -> String)?) : VALUE(
        {
            val value = raw ?: supplier?.invoke() ?: error("Either raw value or supplier must be non-null!")
            val indent = it.nixPrettyPrinter?.get ?: "\n"
            it.writeRaw(value.replace("\n", indent))
        },
        { "MultilineValue(${raw ?: "<not yet known>"})" }
    )

    private val string by lazy { stringRepresentation() }
    override fun toString() = string

}