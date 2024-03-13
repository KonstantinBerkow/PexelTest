package io.github.konstantinberkow.pexeltest.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

private const val API_KEY = "D9UsBoL2G2Xe1yUkCMb9cPvNFgHQnzxm2hR54NehbPiivcm3BUrpgErw"
private const val AUTH_HEADER = "Authorization: $API_KEY"

interface PexelApi {

    @GET("curated")
    @Headers(AUTH_HEADER)
    suspend fun curatedPhotos(
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): PexelPhotoPage

    @GET("photos/{id}")
    @Headers(AUTH_HEADER)
    suspend fun getPhotoInfo(
        @Path("id") id: Long
    ): PexelPhoto
}
