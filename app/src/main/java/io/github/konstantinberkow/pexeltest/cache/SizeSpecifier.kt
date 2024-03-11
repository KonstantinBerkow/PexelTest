package io.github.konstantinberkow.pexeltest.cache

enum class SizeSpecifier(val intValue: Int, val stringValue: String) {
    ORIGINAL(0, "original"),
    LARGE(1, "large"),
    SMALL(2, "small");

    companion object {

        fun fromInt(value: Int): SizeSpecifier? =
            when (value) {
                ORIGINAL.intValue -> ORIGINAL
                LARGE.intValue -> LARGE
                SMALL.intValue -> SMALL
                else -> null
            }

        fun fromString(value: String): SizeSpecifier? =
            when (value) {
                ORIGINAL.stringValue -> ORIGINAL
                LARGE.stringValue -> LARGE
                SMALL.stringValue -> SMALL
                else -> null
            }
    }
}
