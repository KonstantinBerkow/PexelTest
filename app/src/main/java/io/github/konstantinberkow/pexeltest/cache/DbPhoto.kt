package io.github.konstantinberkow.pexeltest.cache

data class DbPhoto(
    val id: Long,
    val authorName: String,
    val averageColor: Long,
    val src: Map<String, String>,
)
