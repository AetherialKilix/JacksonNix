package net.aetherialkilix.jackson.nix

import java.util.*

sealed interface NixMapperFeature {
    val default: Boolean

    enum class Serialization(override val default: Boolean): NixMapperFeature {
        /**
         * If enabled, single-value objects like this:
         * ```nix
         * {
         *   a = {
         *     b = {
         *       c = "myValue"
         *   }
         * }
         * ```
         * would collapse to this:
         * ```nix
         * {
         *   a.b.c = "myValue"
         * }
         * ```
         * default: `enabled`
         */
        COLLAPSE_OBJECTS(true),
        /**
         * If enabled, strings containing one or multiple newline (`\n`) characters like
         * ```nix
         * myMessage = "This message uses multiple lines.\nHowever the\n{{default syntax}}\nis hard to read."
         * ```
         * will be serialized as mutliline strings like this:
         * ```nix
         * myMessage = ''
         *   This message uses multiple lines.
         *   However the
         *   {{default syntax}}
         *   is hard to read.
         *''
         * ```
         * default: `enabled`
         */
        MULTILINE_STRING(true),
        /**
         * If enabled, and [com.fasterxml.jackson.core.JsonGenerator.writeBinary] is used,
         * an error will be reported to the respective [NixMapper.warningHandler].
         * (or [NixMapper.DEFAULT_WARNING_HANDLER] if unset)
         */
        WARN_ON_BINARY_DATA(true),

        ;companion object {
            @JvmStatic @get:JvmName("DEFAULTS")
            val defaults; get() = EnumSet.allOf(Serialization::class.java)
        }
    }

    enum class Deserialization(override val default: Boolean) : NixMapperFeature {
        /**
         * If enabled, deserialization will follow imports like
         * ```nix
         * {
         *   myMessage = "Hello",
         *   myComplexObject = import ./complex.nix
         * }
         * ```
         * otherwise an exception will be thrown during serialization.
         *
         * default: `false`
         */
        FOLLOW_IMPORTS(false)

        ;companion object {
            @JvmStatic @get:JvmName("DEFAULTS")
            val defaults; get() =
                EnumSet.noneOf(Deserialization::class.java)

        }
    }

}