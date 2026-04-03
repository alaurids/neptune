package com.example.projectneptune

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

// 1. Data model for the DFO API response
data class DfoResponse(
    @SerializedName("features") val features: List<DfoFeature>? = null
)

data class DfoFeature(
    @SerializedName("attributes") val attributes: Map<String, Any>? = null,
    @SerializedName("geometry") val geometry: DfoGeometry? = null,
    @SerializedName("centroid") val centroid: DfoCentroid? = null
)

data class DfoGeometry(
    @SerializedName("x") val x: Any? = null,
    @SerializedName("y") val y: Any? = null,
    // Triple nested list for the 'rings' found in Layer 0
    @SerializedName("rings") val rings: List<List<List<Any>>>? = null
)

data class DfoCentroid(
    @SerializedName("x") val x: Any? = null,
    @SerializedName("y") val y: Any? = null
)

// 2. Data model for Wikipedia/Wiki data (if you decide to use live scraping later)
data class WikiResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("extract") val extract: String? = null,
    @SerializedName("description") val description: String? = null
)

// 3. The API Interface
interface DfoApiService {
    @GET
    suspend fun getShellfishData(
        @Url url: String, // This allows us to pass Layer 0 or any other URL dynamically
        @Query("where") where: String = "1=1",
        @Query("outFields") outFields: String = "*",
        @Query("f") f: String = "json",
        @Query("returnGeometry") returnGeometry: String = "true",
        @Query("outSR") outSR: String = "4326",
        @Query("returnCentroid") returnCentroid: String = "false",
        @Query("resultRecordCount") resultRecordCount: Int = 50
    ): DfoResponse

    @GET
    suspend fun getWikiData(@Url url: String): WikiResponse
}