package net.gearmaniacs.tournament.opr

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class PowerRanking(
    teamsList: List<Team>,
    private val redAlliances: List<Alliance>,
    private val blueAlliances: List<Alliance>
) {

    private val teamPowerList = populateTeamPowerList(teamsList)

    /**
     * Creates a list with all the team numbers from the matches
     *
     * @param teamsList Combines the extracted team numbers with the team names from this list
     */
    private fun populateTeamPowerList(teamsList: List<Team>): List<TeamPower> {
        val allTeams = HashSet<Int>(redAlliances.size)

        redAlliances.forEach {
            allTeams.add(it.firstTeam)
            allTeams.add(it.secondTeam)
        }

        blueAlliances.forEach {
            allTeams.add(it.firstTeam)
            allTeams.add(it.secondTeam)
        }

        val result = ArrayList<TeamPower>(allTeams.size)

        allTeams.forEach { teamId ->
            val foundTeamName = teamsList.find { it.id == teamId }?.name

            // If available add the name to the team
            result.add(TeamPower(teamId, foundTeamName.orEmpty()))
        }

        return result
    }

    /*
     * Generates a Matrix which specifies which teams played as an alliance
     */
    private fun generateMatchesMatrix(): Array<DoubleArray> {
        val matrix = Array(teamPowerList.size) { DoubleArray(teamPowerList.size) }

        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                val horizontalTeam = teamPowerList[i].id
                val verticalTeam = teamPowerList[j].id
                var count = 0.0

                redAlliances.forEach {
                    if (it.containsTeam(verticalTeam) && it.containsTeam(horizontalTeam))
                        count++
                }

                blueAlliances.forEach {
                    if (it.containsTeam(verticalTeam) && it.containsTeam(horizontalTeam))
                        count++
                }

                matrix[i][j] = count
            }
        }

        return matrix
    }

    /*
     * Returns an Array containing the sum of all the alliances score each team played in per team
     */
    private fun getScoreArray(): DoubleArray {
        val array = DoubleArray(teamPowerList.size)

        teamPowerList.forEachIndexed { index, team ->
            var value = 0

            redAlliances.forEach {
                if (it.containsTeam(team.id))
                    value += it.score
            }

            blueAlliances.forEach {
                if (it.containsTeam(team.id))
                    value += it.score
            }

            array[index] = value.toDouble()
        }

        return array
    }

    suspend fun generatePowerRankings(): List<TeamPower> = coroutineScope {
        val invertedMatches = async { Matrix.invert(generateMatchesMatrix()) }
        val scores = getScoreArray()

        val powerArray = Matrix.multiply(invertedMatches.await(), scores)

        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = '.'
        }

        powerArray.forEachIndexed { index, power ->
            teamPowerList[index].power = decimalFormat.format(power).toFloat()
        }

        teamPowerList.sortedByDescending { it.power }
    }
}
