package net.gearmaniacs.tournament.opr

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower

class PowerRanking(
    teamsList: List<Team>,
    private val matchesList: List<Match>
) {

    private val teamPowerList = populateTeamPowerList(teamsList)

    /**
     * Creates a list with all the team numbers from the matches
     *
     * @param teamsList Combines the extracted team numbers with the team names from this list
     */
    private fun populateTeamPowerList(teamsList: List<Team>): List<TeamPower> {
        val allTeams = HashSet<Int>(matchesList.size)

        matchesList.forEach {
            with(allTeams) {
                add(it.redAlliance.firstTeam)
                add(it.redAlliance.secondTeam)

                add(it.blueAlliance.firstTeam)
                add(it.blueAlliance.secondTeam)
            }
        }

        val result = ArrayList<TeamPower>(allTeams.size)

        allTeams.forEach { teamId ->
            if (teamId > 0) {
                val foundTeamName = teamsList.find { it.id == teamId }?.name

                // If available add the name to the team
                result.add(TeamPower(teamId, foundTeamName.orEmpty()))
            }
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

                matchesList.forEach {
                    if (it.redAlliance.containsTeam(verticalTeam) &&
                        it.redAlliance.containsTeam(horizontalTeam)
                    ) {
                        count++
                    }

                    if (it.blueAlliance.containsTeam(verticalTeam) &&
                        it.blueAlliance.containsTeam(horizontalTeam)
                    ) {
                        count++
                    }
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
            val id = team.id
            var value = 0.0

            matchesList.forEach {
                if (it.redAlliance.containsTeam(id))
                    value += it.redAlliance.score

                if (it.blueAlliance.containsTeam(id))
                    value += it.blueAlliance.score
            }

            array[index] = value
        }

        return array
    }

    suspend fun generatePowerRankings(): List<TeamPower> = coroutineScope {
        val invertedMatches = async { Matrix.invert(generateMatchesMatrix()) }
        val scores = getScoreArray()

        val powerArray = Matrix.multiply(invertedMatches.await(), scores)

        powerArray.forEachIndexed { index, power ->
            teamPowerList[index].power = power.toFloat()
        }

        teamPowerList.sortedByDescending { it.power }
    }
}
