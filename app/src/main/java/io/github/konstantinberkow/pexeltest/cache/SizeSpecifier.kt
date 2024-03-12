package io.github.konstantinberkow.pexeltest.cache

enum class SizeSpecifier(val longValue: Long, val stringValue: String) {
    ORIGINAL(0, "original"),
    LARGE(1, "large"),
    SMALL(2, "small");

    companion object {

        fun fromString(value: String): SizeSpecifier? =
            when (value) {
                ORIGINAL.stringValue -> ORIGINAL
                LARGE.stringValue -> LARGE
                SMALL.stringValue -> SMALL
                else -> null
            }
    }
}
