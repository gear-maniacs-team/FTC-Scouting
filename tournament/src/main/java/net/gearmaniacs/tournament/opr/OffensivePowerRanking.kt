package net.gearmaniacs.tournament.opr

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.gearmaniacs.core.model.BaseTeam
import net.gearmaniacs.core.model.Match
import net.gearmaniacs.core.model.RankedTeam

object OffensivePowerRanking {

    /**
     * Creates a list with all the team numbers from the matches
     *
     * @param teamsList Combines the extracted team numbers with the team names from this list
     */
    private fun getAllTeams(
        matchList: List<Match>,
        teamsList: List<BaseTeam>
    ): List<BaseTeam> {
        val allTeams = HashSet<Int>(matchList.size)

        matchList.forEach {
            with(allTeams) {
                add(it.redAlliance.firstTeam)
                add(it.redAlliance.secondTeam)

                add(it.blueAlliance.firstTeam)
                add(it.blueAlliance.secondTeam)
            }
        }

        return allTeams.asSequence()
            .filter { teamNumber -> teamNumber > 0 }
            .map { teamNumber ->
                BaseTeam(
                    teamNumber,
                    // If available add the name to the team number
                    teamsList.find { it.id == teamNumber }?.name.orEmpty()
                )
            }
            .toList()
    }

    /**
     * Computes Offensive Power Ranking (OPR) using the MMSE method.
     * The MMSE method is always stable (unlike the traditional least-squares OPR calculation)
     * and does a better job of predicting future unknown match scores.
     * As the number of matches at an event gets large, the OPR values computed for the MMSE method
     * converge to those computed using the traditional method.
     * The MMSE parameter is a function of how random the scores are in each game.
     * If an alliance scores virtually the same amount every time they play, the MMSE parameter should
     * be close to 0.
     * If an alliance's score varies substantially from match to match due to randomness in their
     * ability, their opponent's ability, or random game factors, then the MMSE parameter should be
     * larger.
     * For a typical game, an MMSE parameter of 1-3 is recommended.
     * Using an MMSE parameter of exactly 0 causes the computed OPR values to be identical to the
     * traditional OPR values.
     *
     * @param matchList List of all matches
     * @param teamList  List of Team number and names, is used to
     * @param mmse      MMSE adjustment parameter (0=normal OPR, 1-3 recommended)
     * @returns A list of [RankedTeam], null if failed
     */
    suspend fun computeMMSE(
        matchList: List<Match>,
        teamList: List<BaseTeam> = emptyList(),
        mmse: Double = DEFAULT_MMSE_SCALAR
    ): List<RankedTeam>? {
        require(matchList.isNotEmpty())
        require(mmse >= 0)

        val allTeams = getAllTeams(matchList, teamList)
        val teamCount = allTeams.size

        val playedMatches =
            matchList.filter { it.redAlliance.score > 0 || it.blueAlliance.score > 0 }
        val playedMatchesCount = playedMatches.size

        val redAlliancesScore = Matrix(playedMatchesCount, teamCount)
        val blueAlliancesScore = Matrix(playedMatchesCount, teamCount)
        val redMatchesScore = DoubleArray(playedMatchesCount)
        val blueMatchesScore = DoubleArray(playedMatchesCount)

        var totalScore = 0.0
        playedMatches.forEachIndexed { index, match ->
            val redScore = match.redAlliance.score.toDouble()
            redAlliancesScore[index, allTeams.indexOf(match.redAlliance.firstTeam)] = 1.0
            redAlliancesScore[index, allTeams.indexOf(match.redAlliance.secondTeam)] = 1.0

            val blueScore = match.blueAlliance.score.toDouble()
            blueAlliancesScore[index, allTeams.indexOf(match.blueAlliance.firstTeam)] = 1.0
            blueAlliancesScore[index, allTeams.indexOf(match.blueAlliance.secondTeam)] = 1.0

            redMatchesScore[index] = redScore
            blueMatchesScore[index] = blueScore

            totalScore += redScore + blueScore
        }

        val alliancesMatrix = Matrix(2 * playedMatchesCount, teamCount)
        val matchesMatrix = Matrix(2 * playedMatchesCount, 1)

        alliancesMatrix.setMatrix(0, playedMatchesCount - 1, 0, teamCount - 1, redAlliancesScore)
        alliancesMatrix.setMatrix(
            playedMatchesCount,
            2 * playedMatchesCount - 1,
            0,
            teamCount - 1,
            blueAlliancesScore
        )

        val meanAllianceScore = totalScore / (playedMatchesCount * ALLIANCES_PER_MATCH)
        val meanTeamScore = meanAllianceScore / TEAMS_PER_ALLIANCE
        for (i in 0 until playedMatchesCount) {
            redMatchesScore[i] = redMatchesScore[i] - meanAllianceScore
            blueMatchesScore[i] = blueMatchesScore[i] - meanAllianceScore
        }

        matchesMatrix.setRows(0, playedMatchesCount - 1, 0, redMatchesScore)
        matchesMatrix.setRows(playedMatchesCount, 2 * playedMatchesCount - 1, 0, blueMatchesScore)

        return coroutineScope {
            // Compute inverse of matches matrix (alliancesMatrix' * alliancesMatrix + I * mmse)
            val matchMatrixInverse = async {
                try {
                    (alliancesMatrix.transpose() * alliancesMatrix).plus(Matrix.identity(teamCount) * mmse)
                        .inverse()
                } catch (e: Exception) {
                    null
                }
            }

            // Compute OPRs
            val temp = alliancesMatrix.transpose() * matchesMatrix
            val oprMatrix = (matchMatrixInverse.await() ?: return@coroutineScope null) * temp

            allTeams.mapIndexed { index, team ->
                RankedTeam(
                    team.id,
                    team.name.orEmpty(),
                    score = oprMatrix[index, 0] + meanTeamScore
                )
            }.sortedDescending()
        }
    }

    private fun List<BaseTeam>.indexOf(teamId: Int): Int {
        return indexOfFirst {
            it.id == teamId
        }
    }

    /**
     * Computes Offensive Power Raking
     *
     * @see [computeMMSE]
     */
    suspend fun computeOpr(
        matchList: List<Match>,
        teamList: List<BaseTeam> = emptyList()
    ) = computeMMSE(matchList, teamList, 0.0)

    private const val ALLIANCES_PER_MATCH = 2
    private const val TEAMS_PER_ALLIANCE = 2
    private const val DEFAULT_MMSE_SCALAR = 2.0
}
