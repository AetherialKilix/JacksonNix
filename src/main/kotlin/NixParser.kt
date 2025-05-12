package net.aetherialkilix.jackson.nix

import com.fasterxml.jackson.core.Base64Variant
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.core.io.IOContext
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger

class NixParser(
    private val reader: Reader,
    private val context: IOContext,
    private val factory: NixFactory
) : JsonParser() {
    init { TODO("NixParser is not yet implemented.") }

    override fun getCodec(): ObjectCodec? {
        TODO("Not yet implemented")
    }

    override fun setCodec(oc: ObjectCodec?) {
        TODO("Not yet implemented")
    }

    override fun version(): Version? {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun isClosed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getParsingContext(): JsonStreamContext? {
        TODO("Not yet implemented")
    }

    override fun getCurrentLocation(): JsonLocation? {
        TODO("Not yet implemented")
    }

    override fun getTokenLocation(): JsonLocation? {
        TODO("Not yet implemented")
    }

    override fun nextToken(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun nextValue(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun skipChildren(): JsonParser? {
        TODO("Not yet implemented")
    }

    override fun getCurrentToken(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun getCurrentTokenId(): Int {
        TODO("Not yet implemented")
    }

    override fun hasCurrentToken(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasTokenId(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasToken(t: JsonToken?): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearCurrentToken() {
        TODO("Not yet implemented")
    }

    override fun getLastClearedToken(): JsonToken? {
        TODO("Not yet implemented")
    }

    override fun overrideCurrentName(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getCurrentName(): String? {
        TODO("Not yet implemented")
    }

    override fun getText(): String? {
        TODO("Not yet implemented")
    }

    override fun getTextCharacters(): CharArray? {
        TODO("Not yet implemented")
    }

    override fun getTextLength(): Int {
        TODO("Not yet implemented")
    }

    override fun getTextOffset(): Int {
        TODO("Not yet implemented")
    }

    override fun hasTextCharacters(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getNumberValue(): Number? {
        TODO("Not yet implemented")
    }

    override fun getNumberType(): NumberType? {
        TODO("Not yet implemented")
    }

    override fun getIntValue(): Int {
        TODO("Not yet implemented")
    }

    override fun getLongValue(): Long {
        TODO("Not yet implemented")
    }

    override fun getBigIntegerValue(): BigInteger? {
        TODO("Not yet implemented")
    }

    override fun getFloatValue(): Float {
        TODO("Not yet implemented")
    }

    override fun getDoubleValue(): Double {
        TODO("Not yet implemented")
    }

    override fun getDecimalValue(): BigDecimal? {
        TODO("Not yet implemented")
    }

    override fun getBinaryValue(bv: Base64Variant?): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun getValueAsString(def: String?): String? {
        TODO("Not yet implemented")
    }

}