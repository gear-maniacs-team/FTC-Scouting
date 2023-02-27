package net.gearmaniacs.tournament.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import net.gearmaniacs.core.model.enums.ColorMark
import net.gearmaniacs.core.model.enums.StartZone
import net.gearmaniacs.core.model.match.Alliance
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.Team
import net.gearmaniacs.tournament.csv.CsvFields.MATCH_COLUMNS
import net.gearmaniacs.tournament.csv.CsvFields.TEAM_COLUMNS

internal object CsvImport {

    fun importTeams(content: String): List<Team> {
        return csvReader().readAllWithHeader(content).map {
            Team(
                key = "",
                tournamentKey = "",
                number = it[TEAM_COLUMNS[0]].unquote().toInt(),
                name = it[TEAM_COLUMNS[1]].unquote(),
                autonomousScore = it[TEAM_COLUMNS[2]].unquote().toInt(),
                teleOpScore = it[TEAM_COLUMNS[3]].unquote().toInt(),
                colorMark = ColorMark.fromString(it[TEAM_COLUMNS[4]].unquote()),
                startZone = StartZone.fromString(it[TEAM_COLUMNS[5]].unquote()),
                notes = it[TEAM_COLUMNS[6]]?.trim('\"', '\'').takeIf { !it.isNullOrBlank() },
            )
        }
    }

    private fun String?.unquote() = this?.trim('\"', '\'').orEmpty()

    @Throws(NumberFormatException::class)
    fun importMatch(content: String): List<Match> {
        return csvReader().readAllWithHeader(content).map {
            Match(
                key = "",
                tournamentKey = "",
                id = it[MATCH_COLUMNS[0]].unquote().toInt(),
                redAlliance = Alliance(
                    firstTeam = it[MATCH_COLUMNS[1]].unquote().toInt(),
                    secondTeam = it[MATCH_COLUMNS[2]].unquote().toInt(),
                    score = it[MATCH_COLUMNS[3]].unquote().toInt(),
                ),
                blueAlliance = Alliance(
                    firstTeam = it[MATCH_COLUMNS[4]].unquote().toInt(),
                    secondTeam = it[MATCH_COLUMNS[5]].unquote().toInt(),
                    score = it[MATCH_COLUMNS[6]].unquote().toInt(),
                ),
            )
        }
    }
}
