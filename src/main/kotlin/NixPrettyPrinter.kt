package net.aetherialkilix.jackson.nix

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.PrettyPrinter
import org.jetbrains.annotations.Contract

private fun <T> Int.isZero(then: T, `else`: T): T = if (this == 0) then else `else`

class NixPrettyPrinter(
    val isCompact: Boolean = false,
    val indentation: String = "  ",
    val newLine: String = System.lineSeparator()
) : PrettyPrinter {

    companion object {
        @JvmStatic @Contract("-> new")
        fun createCompact() = NixPrettyPrinter(true)
        @JvmStatic @Contract("-> new")
        fun createDefault() = NixPrettyPrinter()
        @JvmStatic @Contract("-> new") @JvmOverloads
        fun createTabIndented(count: Int = 1) = NixPrettyPrinter(indentation = "\t".repeat(count))
        @JvmStatic @Contract("-> new") @JvmOverloads
        fun createSpaceIndented(count: Int = 2) = NixPrettyPrinter(indentation = " ".repeat(count))
    }

    var depth = 0; set(value) { field = value.coerceAtLeast(0) }
    fun get() = indentation.repeat(depth)
    fun inc() = indentation.repeat(++depth)
    fun dec() = indentation.repeat(--depth)

    val get; get() = newLine + get()
    val inc; get() = newLine + inc()
    val dec; get() = newLine + dec()

    private fun JsonGenerator.pretty(
        compact: Char, complex: String
    ) = if (isCompact) writeRaw(compact) else writeRaw(complex)
    private fun JsonGenerator.pretty(
        compact: String, complex: String
    ) = if (isCompact) writeRaw(compact) else writeRaw(complex)

    // compact: ' ', non-compact: "\n<indent>"
    override fun writeRootValueSeparator(gen: JsonGenerator) =
        gen.pretty(' ', get)
    // compact: '{', non-compact: "{\n<indent++>"
    override fun writeStartObject(gen: JsonGenerator) =
        gen.pretty('{', "{$inc")
    // compact: '}', non-compact: "}"
    override fun writeEndObject(gen: JsonGenerator, count: Int) =
        gen.pretty(count.isZero("}", ";}"), count.isZero("}", ";$dec}"))
    // compact: ';', non-compact: ";"
    override fun writeObjectEntrySeparator(gen: JsonGenerator) =
        gen.pretty(';', ";$get")
    // compact: '=', non-compact: " = "
    override fun writeObjectFieldValueSeparator(gen: JsonGenerator) =
        gen.pretty('=', " = ")
    // compact: '[', non-compact: "[\n<indent++>"
    override fun writeStartArray(gen: JsonGenerator) =
        gen.pretty('[', "[$inc")
    // compact ']', non-compact: "]\n<--indent>"
    override fun writeEndArray(gen: JsonGenerator, values: Int) =
        gen.pretty(']', "$dec]")
    // compact: ' ', non-compact: "\n<indent>"
    override fun writeArrayValueSeparator(gen: JsonGenerator) =
        gen.pretty(' ', get)
    // unused
    override fun beforeArrayValues(gen: JsonGenerator) {}
    // unused
    override fun beforeObjectEntries(gen: JsonGenerator) {}

}