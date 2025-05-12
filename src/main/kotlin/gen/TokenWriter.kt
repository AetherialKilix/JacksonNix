package net.aetherialkilix.jackson.nix.gen

import net.aetherialkilix.jackson.nix.NixGenerator

sealed class TokenWriter {
    fun startArray() = writeToken(GeneratorToken.START_ARRAY)
    fun endArray() = writeToken(GeneratorToken.END_ARRAY)
    fun startObject() = writeToken(GeneratorToken.START_OBJECT)
    fun endObject(entries: Int) = writeToken(GeneratorToken.END_OBJECT(entries))
    fun name(name: String) = writeToken(GeneratorToken.NAME(name))
    fun value(raw: String) =
        writeToken(GeneratorToken.VALUE(raw))
    fun multilineValue(raw: String?, supplier: (() -> String)? = null) =
        writeToken(GeneratorToken.MULTILINE_VALUE(raw, supplier))
    fun nameValueSeparator() = writeToken(GeneratorToken.NAME_VALUE_SEPERATOR)
    fun arrayValueSeparator() = writeToken(GeneratorToken.ARRAY_VALUE_SEPERATOR)
    fun objectEntrySeparator() = writeToken(GeneratorToken.OBJECT_ENTRY_SEPERATOR)

    abstract fun writeToken(token: GeneratorToken)
    abstract fun flushTokens()

    data class Simple(val generator: NixGenerator) : TokenWriter() {
        override fun writeToken(token: GeneratorToken) = token.write(generator)
        override fun flushTokens() { /** no need to flush */}
    }
}