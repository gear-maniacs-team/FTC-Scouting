package net.gearmaniacs.ftcscouting.opr

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.gearmaniacs.core.model.Alliance
import net.gearmaniacs.core.model.Team
import net.gearmaniacs.core.model.TeamPower
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.HashSet

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
            val foundTeam = teamsList.find { it.id == teamId }

            // If available add the name to the team
            if (foundTeam == null) {
                result.add(TeamPower(teamId, ""))
            } else {
                result.add(TeamPower(teamId, foundTeam.name.orEmpty()))
            }
        }

        return result
    }

    private fun generateMatrix(): Array<IntArray> {
        val matrix = Array(teamPowerList.size + 1) { IntArray(teamPowerList.size + 1) }

        // Set team number on first row and column
        teamPowerList.forEachIndexed { index, team ->
            matrix[0][index + 1] = team.id
            matrix[index + 1][0] = team.id
        }

        for (i in 1 until matrix.size) {
            for (j in 1 until matrix[i].size) {
                val verticalTeam = matrix[0][j]
                val horizontalTeam = matrix[i][0]
                var count = 0

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

    private fun getMatchesTable(): Array<DoubleArray> {
        val matrix = generateMatrix()
        val array = Array(teamPowerList.size) { DoubleArray(teamPowerList.size) }

        // Remove the team numbers from the first row and column
        // and convert the IntArray to a DoubleArray
        for (i in 1 until matrix.size)
            for (j in 1 until matrix[i].size)
                array[i - 1][j - 1] = matrix[i][j].toDouble()

        return array
    }

    private fun getScoreArray(): DoubleArray {
        val array = DoubleArray(teamPowerList.size)

        teamPowerList.forEachIndexed { index, team ->
            var value = 0

            redAlliances.asSequence()
                .filter { it.containsTeam(team.id) }
                .forEach { value += it.score }

            blueAlliances.asSequence()
                .filter { it.containsTeam(team.id) }
                .forEach { value += it.score }

            array[index] = value.toDouble()
        }

        return array
    }

    suspend fun generatePowerRankings(): List<TeamPower> = coroutineScope {
        val matches = async { Matrix.invert(getMatchesTable()) }
        val scores = getScoreArray()

        val powerArray = Matrix.multiply(matches.await(), scores)

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
