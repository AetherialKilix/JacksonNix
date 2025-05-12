package net.aetherialkilix.jackson.nix

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.core.format.InputAccessor
import com.fasterxml.jackson.core.format.MatchStrength
import com.fasterxml.jackson.core.io.IOContext
import java.io.*
import java.nio.charset.StandardCharsets

class NixFactory : JsonFactory() {

    companion object {
        val importPattern = Regex("""^\s*import\s+[(<.\w]""")
        val letPattern = Regex("""^\s*let\s""")

        val digits = ('0'..'9').toSet()
        val blockBeginnings = setOf('[', '{')
        val quotes = setOf('\'', '"')
        val legalFirstWords = setOf("null", "false", "true")
        val numberPrefixes = setOf('-', '.')
        val validFirstCharacters = buildSet<Char> {
            addAll(digits)
            addAll(blockBeginnings)
            addAll(quotes)
            addAll(legalFirstWords.map(String::first))
            addAll(numberPrefixes)
        }
    }

    override fun getFormatName(): String = "nix"

    override fun _createParser(inStream: InputStream, ctxt: IOContext) =
        NixParser(InputStreamReader(inStream), ctxt, this)
    override fun _createParser(reader: Reader, ctxt: IOContext) =
        NixParser(reader, ctxt, this)
    override fun _createUTF8Generator(outStream: OutputStream, ctxt: IOContext) =
        NixGenerator(OutputStreamWriter(outStream, StandardCharsets.UTF_8), ctxt, this)
    override fun _createGenerator(writer: Writer, ctxt: IOContext) =
        NixGenerator(writer, ctxt, this)

    override fun hasFormat(acc: InputAccessor): MatchStrength {
        val buffer = StringBuilder()
        // collect up to the first 64 bytes
        while (buffer.length <= 64) {
            var b: Int
            try {
                if (! acc.hasMoreBytes()) break
                b = acc.nextByte().toInt() and 0xFF
            } catch (_: EOFException) { break }
            val c = b.toChar()
            buffer.append(c)
        }
        if (buffer.isBlank()) return MatchStrength.NO_MATCH

        val trimmed = buffer.toString().trimStart()

        return when {
            letPattern.containsMatchIn(trimmed) -> MatchStrength.SOLID_MATCH
            importPattern.containsMatchIn(trimmed) -> MatchStrength.SOLID_MATCH
            trimmed.firstOrNull() in validFirstCharacters -> MatchStrength.INCONCLUSIVE
            else -> MatchStrength.NO_MATCH
        }
    }


    override fun canHandleBinaryNatively(): Boolean = false

    override fun requiresPropertyOrdering(): Boolean = false
}
