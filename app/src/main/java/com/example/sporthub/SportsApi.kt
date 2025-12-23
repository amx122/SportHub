package com.example.sporthub

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SportsApi {
    @GET("fixtures")
    suspend fun getFixturesByDateRaw(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Europe/Kiev"
    ): FootballResponse
    @GET("fixtures/events")
    suspend fun getMatchEvents(@Query("fixture") fixtureId: String): EventsResponse
    @GET("fixtures/headtohead")
    suspend fun getHeadToHead(@Query("h2h") h2h: String, @Query("last") last: Int = 10): FootballResponse
    @GET("standings")
    suspend fun getLeagueTable(
        @Query("league") leagueId: String,
        @Query("season") season: String
    ): StandingsResponse
    @GET("players/squads")
    suspend fun getTeamSquad(@Query("team") teamId: String): SquadResponse
    @GET("fixtures")
    suspend fun getTeamLastMatches(
        @Query("team") teamId: String,
        @Query("last") last: Int = 5,
        @Query("timezone") timezone: String = "Europe/Kiev"
    ): FootballResponse
    @GET("fixtures")
    suspend fun getTeamFixtures(
        @Query("team") teamId: String,
        @Query("next") next: Int = 5,
        @Query("timezone") timezone: String = "Europe/Kiev"
    ): FootballResponse

    @GET("fixtures")
    suspend fun getTeamResults(
        @Query("team") teamId: String,
        @Query("last") last: Int = 5,
        @Query("timezone") timezone: String = "Europe/Kiev"
    ): FootballResponse
    @GET("teams")
    suspend fun searchTeams(@Query("search") name: String): TeamSearchResponse

    @GET("players")
    suspend fun searchPlayers(@Query("search") name: String, @Query("season") season: String = "2025"): PlayerSearchResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://v3.football.api-sports.io/"
    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("x-apisports-key", "7fc06dde2d5aef0609ff7183fcc6358a")
            .build()
        chain.proceed(request)
    }.build()
    val api: SportsApi by lazy { Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(SportsApi::class.java) }
}