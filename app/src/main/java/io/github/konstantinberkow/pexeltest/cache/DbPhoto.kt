package io.github.konstantinberkow.pexeltest.cache

data class DbPhoto(
    val id: Long,
    val authorName: String,
    val averageColor: Int,
    val src: Map<String, String>,
)
