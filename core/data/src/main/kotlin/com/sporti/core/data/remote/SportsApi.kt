package com.sporti.core.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface SportsApi {
    @GET("eventsnextleague.php")
    suspend fun upcomingEvents(@Query("id") leagueId: String): EventsResponse
}

data class EventsResponse(val events: List<EventDto>?)

data class EventDto(
    @SerializedName("idEvent") val id: String?,
    @SerializedName("strLeague") val league: String?,
    @SerializedName("strSport") val sport: String?,
    @SerializedName("strHomeTeam") val homeTeam: String?,
    @SerializedName("strAwayTeam") val awayTeam: String?,
    @SerializedName("intHomeScore") val homeScore: String?,
    @SerializedName("intAwayScore") val awayScore: String?,
    @SerializedName("strTimestamp") val timestamp: String?,
    @SerializedName("strStatus") val status: String?,
    @SerializedName("strVenue") val venue: String?,
    @SerializedName("strThumb") val thumbnail: String?,
    @SerializedName("strPoster") val poster: String?,
)
