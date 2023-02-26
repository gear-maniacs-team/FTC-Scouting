package net.gearmaniacs.tournament.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.core.model.team.Team

object CsvExport {

    fun exportTeams(teamList: List<Team>): String {
        val list = teamList.map {
            listOf<Any?>(
                it.number,
                it.name,
                it.autonomousScore,
                it.teleOpScore,
                it.colorMark.toString(),
                it.startZone.toString(),
                it.notes,
            )
        }.toMutableList()
        list.add(0, CsvFields.TEAM_COLUMNS)

        return csvWriter().writeAllAsString(list)
    }

    fun exportMatches(matchList: List<Match>): String {
        val list = matchList.map {
            listOf<Any?>(
                it.id,
                it.redAlliance.firstTeam,
                it.redAlliance.secondTeam,
                it.redAlliance.score,
                it.blueAlliance.firstTeam,
                it.blueAlliance.secondTeam,
                it.blueAlliance.score,
            )
        }.toMutableList()
        list.add(0, CsvFields.MATCH_COLUMNS)

        return csvWriter().writeAllAsString(list)
    }

    fun exportOpr(powerList: List<RankedTeam>): String {
        val list = powerList.map {
            listOf<Any?>(
                it.number,
                it.name,
                it.score,
            )
        }.toMutableList()
        list.add(0, CsvFields.LEADERBOARD_COLUMNS)

        return csvWriter().writeAllAsString(list)
    }
}