package net.aetherialkilix.jackson.nix

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.EnumSet
import java.util.function.Consumer

class NixMapper(
    serializationFeatures: Set<NixMapperFeature.Serialization> = NixMapperFeature.Serialization.defaults,
    deserializationFeatures: Set<NixMapperFeature.Deserialization> = NixMapperFeature.Deserialization.defaults,
) : ObjectMapper(NixFactory()) {

    companion object {
        /**
         * The default for [net.aetherialkilix.jackson.nix.NixMapper.warningHandler]
         * prints warning to STDOUT in the format "NixMapper WARNING: %s"
         */
        var DEFAULT_WARNING_HANDLER: Consumer<String> = Consumer { println("NixMapper WARNING: $it") }
    }

    /** defines how whitespaces should be layed out */
    var prettyPrinter: NixPrettyPrinter? = null
    /** defines how warnings are handled, see [DEFAULT_WARNING_HANDLER] */
    var warningHandler: Consumer<String> = DEFAULT_WARNING_HANDLER

    private val enabledSerializationFeatures: EnumSet<NixMapperFeature.Serialization> =
        EnumSet.copyOf(serializationFeatures)

    private val enabledDeserializationFeatures: EnumSet<NixMapperFeature.Deserialization> =
        EnumSet.copyOf(deserializationFeatures)

    fun printCompact() = apply { prettyPrinter = null }
    fun printTabIndented(tabs: Int = 1) = apply { prettyPrinter = NixPrettyPrinter.createTabIndented(tabs) }
    fun printSpaceIndented(spaces: Int = 2) = apply { prettyPrinter = NixPrettyPrinter.createSpaceIndented(spaces) }

    fun enable(feature: NixMapperFeature): NixMapper = apply {
        when (feature) {
            is NixMapperFeature.Serialization -> enabledSerializationFeatures.add(feature)
            is NixMapperFeature.Deserialization -> enabledDeserializationFeatures.add(feature)
        }
    }

    fun disable(feature: NixMapperFeature): NixMapper = apply {
        when (feature) {
            is NixMapperFeature.Serialization -> enabledSerializationFeatures.remove(feature)
            is NixMapperFeature.Deserialization -> enabledDeserializationFeatures.remove(feature)
        }
    }

    fun isEnabled(feature: NixMapperFeature): Boolean = when (feature) {
        is NixMapperFeature.Serialization -> feature in enabledSerializationFeatures
        is NixMapperFeature.Deserialization -> feature in enabledDeserializationFeatures
    }

    fun activeSerializationFeatures() = EnumSet.copyOf(enabledSerializationFeatures)
    fun activeDeserializationFeatures() = EnumSet.copyOf(enabledDeserializationFeatures)
}
