package com.example.sporthub

import com.google.gson.annotations.SerializedName


data class FootballResponse(
    @SerializedName("response") val response: List<MatchResponse>,
    @SerializedName("errors") val errors: Any?
)

data class MatchResponse(
    @SerializedName("fixture") val fixture: FixtureInfo,
    @SerializedName("league") val league: LeagueInfo,
    @SerializedName("teams") val teams: TeamsInfo,
    @SerializedName("goals") val goals: GoalsInfo,
    @SerializedName("score") val score: ScoreInfo
)

data class FixtureInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: StatusInfo
)

data class StatusInfo(
    @SerializedName("long") val long: String,
    @SerializedName("short") val short: String,
    @SerializedName("elapsed") val elapsed: Int?
)

data class LeagueInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("flag") val flag: String?,
    @SerializedName("season") val season: Int?
)

data class TeamsInfo(
    @SerializedName("home") val home: TeamModel,
    @SerializedName("away") val away: TeamModel
)

data class TeamModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("winner") val winner: Boolean?
)

data class GoalsInfo(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)

data class ScoreInfo(
    @SerializedName("fulltime") val fulltime: GoalsInfo
)
data class LeagueItem(
    val id: String,
    val name: String,
    val logo: String? = null,
    val flag: String? = null,
    val sport: String = "Football"
)

val appLeaguesList = listOf(
    LeagueItem("39", "Premier League", flag = "https://media.api-sports.io/flags/gb.svg"),
    LeagueItem("140", "La Liga", flag = "https://media.api-sports.io/flags/es.svg"),
    LeagueItem("135", "Serie A", flag = "https://media.api-sports.io/flags/it.svg"),
    LeagueItem("78", "Bundesliga", flag = "https://media.api-sports.io/flags/de.svg"),
    LeagueItem("61", "Ligue 1", flag = "https://media.api-sports.io/flags/fr.svg"),
    LeagueItem("2", "Champions League", logo = "https://media.api-sports.io/football/leagues/2.png"),
    LeagueItem("3", "Europa League", logo = "https://media.api-sports.io/football/leagues/3.png"),
    LeagueItem("848", "Conference League", logo = "https://media.api-sports.io/football/leagues/848.png"),
    LeagueItem("333", "Ukrainian Premier League", flag = "https://media.api-sports.io/flags/ua.svg")
)
data class EventsResponse(@SerializedName("response") val response: List<MatchEvent>)
data class MatchEvent(
    @SerializedName("time") val time: EventTime,
    @SerializedName("team") val team: TeamModel,
    @SerializedName("player") val player: EventPlayer,
    @SerializedName("assist") val assist: EventPlayer?,
    @SerializedName("type") val type: String,
    @SerializedName("detail") val detail: String
)
data class EventTime(@SerializedName("elapsed") val elapsed: Int, @SerializedName("extra") val extra: Int?)
data class EventPlayer(@SerializedName("id") val id: Int?, @SerializedName("name") val name: String?)

data class TeamSearchResponse(@SerializedName("response") val response: List<TeamSearchItem>)
data class TeamSearchItem(@SerializedName("team") val team: TeamModel)

data class PlayerSearchResponse(@SerializedName("response") val response: List<PlayerSearchItem>)
data class PlayerSearchItem(
    @SerializedName("player") val player: PlayerInfo,
    @SerializedName("statistics") val statistics: List<PlayerStats>
)
data class PlayerInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?,
    @SerializedName("nationality") val nationality: String?,
    @SerializedName("photo") val photo: String?
)
data class PlayerStats(@SerializedName("team") val team: TeamModel)

data class SquadResponse(@SerializedName("response") val response: List<SquadTeam>)
data class SquadTeam(@SerializedName("team") val team: TeamModel, @SerializedName("players") val players: List<SquadPlayer>)
data class SquadPlayer(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: Int?,
    @SerializedName("position") val position: String?,
    @SerializedName("photo") val photo: String?
)

data class StandingsResponse(@SerializedName("response") val response: List<LeagueStandingsResponse>)
data class LeagueStandingsResponse(@SerializedName("league") val league: LeagueStandingsData)
data class LeagueStandingsData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("standings") val standings: List<List<StandingEntry>>
)
data class StandingEntry(
    @SerializedName("rank") val rank: Int,
    @SerializedName("team") val team: TeamModel,
    @SerializedName("points") val points: Int,
    @SerializedName("goalsDiff") val goalsDiff: Int,
    @SerializedName("form") val form: String?,
    @SerializedName("all") val stats: StandingStats
)
data class StandingStats(@SerializedName("played") val played: Int)