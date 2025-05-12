package net.aetherialkilix.jackson.nix

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.json.JsonWriteContext
import net.aetherialkilix.jackson.nix.gen.CollapsingTokenWriter
import net.aetherialkilix.jackson.nix.gen.TokenWriter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Writer
import java.math.BigDecimal
import java.math.BigInteger

class NixGenerator(
    private val writer: Writer,
    val ioContext: IOContext,
    val factory: NixFactory
) : JsonGenerator() {

    private val features =
        (factory.codec as? NixMapper)?.activeSerializationFeatures() ?: NixMapperFeature.Serialization.defaults

    private val multilineStrings = NixMapperFeature.Serialization.MULTILINE_STRING in features

    private val tokenWriter: TokenWriter =
        if (NixMapperFeature.Serialization.COLLAPSE_OBJECTS in features) CollapsingTokenWriter(this)
        else TokenWriter.Simple(this)

    override fun version() = Version(1, 0, 0, "beta", "net.aetherialkilix", "jackson-dataformat-nix")

    // -- codec -- \\
    internal var codec: ObjectCodec? = factory.codec
    override fun setCodec(oc: ObjectCodec?) = also { codec = oc }
    override fun getCodec() = codec
    // -- jackson features (unsupported) -- \\
    internal var jacksonFeatures: Int = Feature.collectDefaults()
    override fun enable(f: Feature) = this.also { jacksonFeatures = jacksonFeatures or f.mask }
    override fun disable(f: Feature) = this.also { jacksonFeatures = jacksonFeatures and f.mask.inv() }
    override fun isEnabled(f: Feature?) = f?.enabledIn(jacksonFeatures) == true
    override fun getFeatureMask() = jacksonFeatures
    @Deprecated("Deprecated in Jackson")
    override fun setFeatureMask(values: Int) = also { jacksonFeatures = values }
    // -- context -- \\
    internal var writeContext: JsonWriteContext = JsonWriteContext.createRootContext(null)
    override fun getOutputContext() = writeContext
    internal fun handleValue() = apply {
        when (writeContext.writeValue()) {
            // within [root, object or array] and first value
            JsonWriteContext.STATUS_OK_AS_IS -> {}
            // within object, without name
            JsonWriteContext.STATUS_EXPECT_NAME -> _reportError("Cannot write value without writing name first.")
            // within object, after name
            JsonWriteContext.STATUS_OK_AFTER_COLON -> tokenWriter.nameValueSeparator()
            // within array, non-first value
            JsonWriteContext.STATUS_OK_AFTER_COMMA -> tokenWriter.arrayValueSeparator()
            // within root, non-first value
            JsonWriteContext.STATUS_OK_AFTER_SPACE -> _reportError("Nix object is not allowed to have multiple root values.")
            else -> _reportError("WriteContext returned unknown state.")
        }
    }
    internal fun handleFieldName(name: String) = apply {
        when (writeContext.writeFieldName(name)) {
            // within [root, object or array] and first value
            JsonWriteContext.STATUS_OK_AS_IS -> {}
            // within object, after name
            JsonWriteContext.STATUS_EXPECT_VALUE -> _reportError("Cannot write name after name without writing value first.")
            // within object, after value name
            JsonWriteContext.STATUS_OK_AFTER_COMMA-> tokenWriter.objectEntrySeparator()
            // within root, non-first value
            JsonWriteContext.STATUS_OK_AFTER_SPACE -> _reportError("Nix object is not allowed to have multiple root values.")
            else -> _reportError("WriteContext returned unknown state.")
        }
    }
    // -- pretty printing -- \\
    internal var prettyPrinter: PrettyPrinter = (codec as? NixMapper)?.prettyPrinter ?: NixPrettyPrinter.createCompact()
    internal val nixPrettyPrinter; get() = (prettyPrinter as? NixPrettyPrinter)
    override fun setPrettyPrinter(pp: PrettyPrinter?) = apply {
        prettyPrinter = pp ?: NixPrettyPrinter.createCompact()
    }
    override fun getPrettyPrinter() = prettyPrinter
    override fun useDefaultPrettyPrinter() = also { prettyPrinter = NixPrettyPrinter() }
    // -- closeable -- \\
    private var closed = false
    override fun isClosed() = closed
    override fun close() {
        closed = true; flush()
    }
    override fun flush() {
        tokenWriter.flushTokens()
        writer.flush()
    }
    // -- raw writing -- \\
    override fun writeRaw(text: String) = writer.write(text)
    override fun writeRaw(text: String, offset: Int, len: Int) = writer.write(text, offset, len)
    override fun writeRaw(text: CharArray, offset: Int, len: Int) = writer.write(text, offset, len)
    override fun writeRaw(char: Char) = writer.write(char.toString()) // toString handles UTF-8 correctly
    // -- raw writing (+ context state) -- \\
    override fun writeRawValue(text: String) =
        handleValue().writeRaw(text)
    override fun writeRawValue(text: String, offset: Int, len: Int) =
        handleValue().writeRaw(text, offset, len)
    override fun writeRawValue(text: CharArray, offset: Int, len: Int) =
        handleValue().writeRaw(text, offset, len)
    // -- writing number values -- \\
    private fun writeAnyNumber(number: Number?) = handleValue().tokenWriter.value(number.toString())
    override fun writeNumber(number: Int)  = writeAnyNumber(number)
    override fun writeNumber(number: Long) = writeAnyNumber(number)
    override fun writeNumber(number: Double) = writeAnyNumber(number)
    override fun writeNumber(number: Float) = writeAnyNumber(number)
    override fun writeNumber(number: BigInteger) = writeAnyNumber(number)
    override fun writeNumber(number: BigDecimal) = writeAnyNumber(number)
    override fun writeNumber(encodedValue: String?) =
        _reportUnsupportedOperation("Writing non-standard numbers is not supported by NixGenerator")
    // -- writing simple primitives -- \\
    override fun writeBoolean(state: Boolean) = handleValue().tokenWriter.value(state.toString())
    override fun writeNull() = handleValue().tokenWriter.value("null")
    override fun writeFieldName(name: String?) {
        name ?: throw _constructWriteException("Property name may not be null")
        handleFieldName(name)
        writeContext.writeFieldName(name)
        tokenWriter.name(name)
    }
    override fun writeFieldName(name: SerializableString?) =
        writeFieldName(name?.value)
    override fun writeStartArray() {
        handleValue()
        writeContext = writeContext.createChildArrayContext()
        tokenWriter.startArray()
    }
    override fun writeEndArray() {
        writeContext = writeContext.clearAndGetParent()
        tokenWriter.endArray()
    }
    override fun writeStartObject() {
        handleValue()
        writeContext = writeContext.createChildObjectContext()
        tokenWriter.startObject()
    }
    override fun writeEndObject() {
        val entries = writeContext.entryCount
        writeContext = writeContext.clearAndGetParent()
        tokenWriter.endObject(entries)
    }

    override fun writeString(text: String) {
        handleValue()
        if (multilineStrings && text.contains('\n')) {
            return tokenWriter.multilineValue("''\n${text.replace("''", "''''")}\n''")
        } else {
            val raw = StringBuilder()
            raw.append('"')
            text.forEach { char ->
                when (char) {
                    '\\' -> raw.append("\\\\")
                    '"' -> raw.append("\\\"")
                    '$' -> raw.append("\\$")
                    '\n' -> raw.append("\\n")
                    '\r' -> raw.append("\\r")
                    '\t' -> raw.append("\\t")
                    // control characters are printed as \x00 to \x1F
                    in '\u0000'..'\u001F' -> raw.append("\\x%02x".format(char.code))
                    else -> raw.append(char)
                }
            }
            raw.append('"')
            tokenWriter.value(raw.toString())
        }
    }

    override fun writeString(buffer: CharArray, offset: Int, len: Int) =
        writeString(String(buffer,  offset, len))

    override fun writeString(text: SerializableString) =
        writeString(text.value)

    override fun writeRawUTF8String(buffer: ByteArray?, offset: Int, len: Int) {
        _reportUnsupportedOperation("writeRawUTF8String is not supported by NixGenerator")
    }

    override fun writeUTF8String(buffer: ByteArray?, offset: Int, len: Int) {
        _reportUnsupportedOperation("writeUTF8String is not supported by NixGenerator")
    }

    private fun warnBinaryStream() {
        if (! features.containsAll(setOf(
            NixMapperFeature.Serialization.WARN_ON_BINARY_DATA,
            NixMapperFeature.Serialization.COLLAPSE_OBJECTS,
        ))) return
        val handler = (codec as? NixMapper)?.warningHandler ?: NixMapper.DEFAULT_WARNING_HANDLER
        handler.accept("""
            NixGenerator#writeBinary invoked with InputStream.
            This action will break NixFeature.Serialization.COLLAPSE_OBJECTS to some extent.
            
            Please refrain from using this method, while COLLAPSE_OBJECTS is enabled.
            Or acknowledge and disable this warning using NixMapper.disable(NixFeature.Serialization.WARN_ON_BINARY_DATA)
        """.trimIndent())
    }

    override fun writeBinary(variant: Base64Variant?, data: ByteArray, offset: Int, len: Int) {
        handleValue()
        val actualVariant = variant ?: Base64Variants.getDefaultVariant()
        val buffer = data.copyOfRange(offset, offset + len)
        val encoded = actualVariant.encode(
            buffer, false,
            nixPrettyPrinter?.newLine ?: System.lineSeparator()
        )
        writeString(encoded) // encode as a normal string
    }

    override fun writeBinary(
        b64variant: Base64Variant?,
        data: InputStream,
        dataLength: Int
    ): Int {
        handleValue()
        warnBinaryStream()
        tokenWriter.flushTokens()

        val encoding = b64variant ?: Base64Variants.MIME
        val readBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val byteStream = ByteArrayOutputStream()
        var totalRead = 0

        while (true) {
            val read = data.read(readBuffer)
            if (read == -1) break
            byteStream.write(readBuffer, 0, read)
            totalRead += read
        }

        writeString(encoding.encode(
            byteStream.toByteArray(), false,
            nixPrettyPrinter?.newLine ?: System.lineSeparator()
        ))
        return totalRead
    }

    fun writeArray(iterable: Iterable<*>) {
        writeStartArray()
        iterable.forEach(::writeObject)
        writeEndArray()
    }

    override fun writeObject(pojo: Any?) {
        handleValue()
        when (pojo) {
            null -> writeNull()
            is Number -> writeAnyNumber(pojo)
            is Boolean -> writeBoolean(pojo)
            is String -> writeString(pojo)
            is Iterable<*> -> writeArray(pojo)
            is Iterator<*> -> writeArray(Iterable { pojo })
            is ByteArray -> writeBinary(pojo)
            is IntArray -> writeArray(pojo.asIterable())
            is DoubleArray -> writeArray(pojo.asIterable())
            is LongArray -> writeArray(pojo.asIterable())
            is FloatArray -> writeArray(pojo.asIterable())
            is ShortArray -> writeArray(pojo.asIterable())
            is CharArray -> writeArray(pojo.asIterable())
            else -> _reportError("kirby not supported")
        }
    }

    override fun writeTree(rootNode: TreeNode?) {
        rootNode ?: return writeNull()
        handleValue()
        _reportError("forest not supported")
    }

}