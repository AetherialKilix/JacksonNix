package net.aetherialkilix.jackson.nix

import org.junit.jupiter.api.Test
import java.io.StringWriter
import kotlin.test.assertEquals

class NixGeneratorTest {

    private fun generate(value: Any): String {
        val writer = StringWriter()
        val mapper = NixMapper()
        mapper.writeValue(writer, value)
        return writer.toString()
    }

    @Test
    fun `test simple number collapse`() {
        val input = mapOf("a" to mapOf("b" to mapOf("c" to 42)))
        val output = generate(input)
        assertEquals("{a.b.c=42;}", output.trim())
    }

    @Test
    fun `test simple boolean collapse`() {
        val input = mapOf("flag" to mapOf("enabled" to true))
        val output = generate(input)
        assertEquals("{flag.enabled=true;}", output.trim())
    }

    @Test
    fun `test null value`() {
        val input = mapOf("config" to mapOf("path" to null))
        val output = generate(input)
        assertEquals("{config.path=null;}", output.trim())
    }

    @Test
    fun `test array with primitives`() {
        val input = mapOf("list" to listOf(1, 2, 3))
        val output = generate(input)
        assertEquals("{list=[1 2 3];}", output.trim())
    }

    @Test
    fun `test mixed collapse and sibling`() {
        val input = mapOf(
            "a" to mapOf(
                "b" to mapOf("c" to 42),
                "d" to false
            )
        )
        val output = generate(input)
        assertEquals("{a={b.c=42;d=false;};}", output.trim())
    }

    @Test
    fun `test multiple root keys`() {
        val input = mapOf(
            "foo" to mapOf("x" to 1),
            "bar" to null
        )
        val output = generate(input)
        assertEquals("{foo.x=1;bar=null;}", output.trim())
    }
}
