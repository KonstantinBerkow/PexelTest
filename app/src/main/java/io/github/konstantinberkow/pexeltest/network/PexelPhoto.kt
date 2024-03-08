package io.github.konstantinberkow.pexeltest.network

data class PexelPhoto(
    val id: Long,
    val photographer: String,
    val photographerUrl: String,
    val averageColor: Int,
    val src: Map<String, String>
)
