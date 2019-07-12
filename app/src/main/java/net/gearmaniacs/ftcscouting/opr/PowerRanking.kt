package net.gearmaniacs.ftcscouting.opr

import kotlinx.coroutines.*
import net.gearmaniacs.ftcscouting.data.Alliance
import net.gearmaniacs.ftcscouting.data.Team
import net.gearmaniacs.ftcscouting.data.TeamPower
import java.text.DecimalFormat

class PowerRanking(
    private val teams: List<Team>,
    private val redAlliances: List<Alliance>,
    private val blueAlliances: List<Alliance>
) {

    private fun generateMatrix(): Array<IntArray> {
        val matrix = Array(teams.size + 1) { IntArray(teams.size + 1) }

        // Set team number on first row and column
        teams.forEachIndexed { index, team ->
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
        val array = Array(teams.size) { DoubleArray(teams.size) }

        // Remove the team numbers from the first row and column
        // and convert the IntArray to a DoubleArray
        for (i in 1 until matrix.size)
            for (j in 1 until matrix[i].size)
                array[i - 1][j - 1] = matrix[i][j].toDouble()

        return array
    }

    private fun getScoreArray(): DoubleArray {
        val array = DoubleArray(teams.size)

        teams.forEachIndexed { index, team ->
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

        powerArray
            .mapIndexed { index, power ->
                TeamPower(teams[index].id, teams[index].name.orEmpty(), decimalFormat.format(power).toFloat())
            }
            .toMutableList()
            .sortedByDescending { it.power }
    }
}
