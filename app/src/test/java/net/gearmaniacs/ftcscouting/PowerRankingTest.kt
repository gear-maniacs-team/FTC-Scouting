package net.gearmaniacs.ftcscouting

import kotlinx.coroutines.runBlocking
import net.gearmaniacs.core.model.match.Alliance
import net.gearmaniacs.core.model.match.Match
import net.gearmaniacs.core.model.team.RankedTeam
import net.gearmaniacs.tournament.opr.OffensivePowerRanking
import org.junit.Test

class PowerRankingTest {

    private val redAlliances = listOf(
        Alliance(57, 130, 212), Alliance(66, 98, 184),
        Alliance(38, 110, 248), Alliance(124, 44, 112),
        Alliance(43, 85, 179), Alliance(101, 51, 202),
        Alliance(27, 137, 173), Alliance(94, 99, 87),
        Alliance(19, 81, 55), Alliance(90, 11, 177),
        Alliance(80, 15, 120), Alliance(38, 99, 240),
        Alliance(90, 113, 143), Alliance(146, 19, 244),
        Alliance(42, 7, 307), Alliance(81, 47, 128),
        Alliance(68, 52, 196), Alliance(40, 8, 184),
        Alliance(111, 34, 168), Alliance(77, 25, 198),
        Alliance(87, 13, 140), Alliance(119, 133, 185),
        Alliance(94, 108, 159), Alliance(8, 56, 210),
        Alliance(65, 1, 288), Alliance(57, 121, 250),
        Alliance(146, 28, 207), Alliance(112, 128, 290),
        Alliance(52, 89, 216), Alliance(23, 62, 320),
        Alliance(104, 85, 279), Alliance(92, 101, 215),
        Alliance(7, 71, 290), Alliance(98, 111, 287),
        Alliance(28, 47, 130), Alliance(44, 133, 95),
        Alliance(52, 8, 235), Alliance(45, 89, 235),
        Alliance(138, 130, 222), Alliance(43, 135, 45),
        Alliance(57, 38, 362), Alliance(80, 87, 52),
        Alliance(124, 141, 279), Alliance(67, 2, 197),
        Alliance(128, 121, 381), Alliance(108, 85, 188),
        Alliance(112, 40, 35), Alliance(89, 43, 135),
        Alliance(15, 25, 141), Alliance(87, 51, 240),
        Alliance(141, 23, 317), Alliance(38, 90, 293),
        Alliance(2, 66, 252), Alliance(77, 81, 281),
        Alliance(138, 92, 177), Alliance(63, 44, 93),
        Alliance(56, 11, 170), Alliance(121, 101, 324),
        Alliance(47, 124, 241), Alliance(104, 113, 79),
        Alliance(111, 65, 328), Alliance(137, 146, 335),
        Alliance(13, 19, 68), Alliance(133, 110, 187),
        Alliance(23, 28, 238), Alliance(71, 2, 305),
        Alliance(67, 27, 126), Alliance(101, 56, 190),
        Alliance(25, 42, 290), Alliance(135, 130, 136),
        Alliance(99, 80, 60), Alliance(87, 108, 92),
        Alliance(1, 68, 385), Alliance(119, 62, 262),
        Alliance(94, 63, 197), Alliance(77, 67, 397),
        Alliance(138, 7, 250), Alliance(98, 42, 315),
        Alliance(92, 112, 167), Alliance(104, 56, 65),
        Alliance(68, 45, 290), Alliance(15, 66, 189),
        Alliance(146, 40, 308), Alliance(94, 119, 285),
        Alliance(28, 27, 84), Alliance(13, 11, 177),
        Alliance(34, 23, 308), Alliance(51, 65, 193),
        Alliance(63, 133, 152), Alliance(128, 141, 350)
    )

    private val blueAlliances = listOf(
        Alliance(8, 57, 217), Alliance(67, 45, 206),
        Alliance(34, 63, 133), Alliance(63, 51, 110),
        Alliance(7, 40, 230), Alliance(65, 146, 307),
        Alliance(71, 119, 227), Alliance(42, 119, 386),
        Alliance(99, 68, 326), Alliance(11, 111, 314),
        Alliance(130, 67, 105), Alliance(119, 56, 146),
        Alliance(98, 25, 110), Alliance(113, 112, 188),
        Alliance(99, 1, 238), Alliance(19, 98, 59),
        Alliance(137, 65, 189), Alliance(43, 128, 182),
        Alliance(65, 62, 160), Alliance(92, 110, 201),
        Alliance(44, 101, 72), Alliance(80, 92, 228),
        Alliance(27, 135, 89), Alliance(42, 68, 292),
        Alliance(40, 90, 243), Alliance(66, 138, 175),
        Alliance(19, 27, 50), Alliance(111, 28, 260),
        Alliance(108, 138, 97), Alliance(2, 104, 235),
        Alliance(108, 34, 111), Alliance(71, 67, 227),
        Alliance(68, 80, 315), Alliance(141, 47, 186),
        Alliance(85, 2, 270), Alliance(135, 141, 161),
        Alliance(110, 137, 197), Alliance(130, 87, 155),
        Alliance(15, 121, 171), Alliance(13, 137, 102),
        Alliance(51, 94, 226), Alliance(25, 1, 299),
        Alliance(113, 124, 172), Alliance(34, 135, 172),
        Alliance(45, 77, 327), Alliance(56, 112, 162),
        Alliance(23, 66, 335), Alliance(11, 42, 374),
        Alliance(52, 7, 241), Alliance(133, 71, 147),
        Alliance(28, 1, 186), Alliance(62, 63, 184),
        Alliance(89, 104, 137), Alliance(27, 13, 123),
        Alliance(45, 128, 266), Alliance(13, 77, 331),
        Alliance(133, 23, 260), Alliance(110, 146, 315),
        Alliance(81, 15, 51), Alliance(62, 94, 230),
        Alliance(112, 90, 157), Alliance(89, 98, 162),
        Alliance(40, 38, 283), Alliance(7, 77, 402),
        Alliance(52, 138, 187), Alliance(15, 92, 169),
        Alliance(121, 8, 228), Alliance(85, 57, 327),
        Alliance(124, 45, 244), Alliance(81, 104, 222),
        Alliance(47, 44, 59), Alliance(66, 43, 192),
        Alliance(128, 113, 323), Alliance(51, 34, 141),
        Alliance(11, 141, 210), Alliance(137, 57, 256),
        Alliance(121, 38, 270), Alliance(110, 52, 69),
        Alliance(25, 135, 195), Alliance(90, 71, 243),
        Alliance(47, 87, 113), Alliance(62, 99, 61),
        Alliance(130, 108, 116), Alliance(81, 89, 191),
        Alliance(113, 80, 155), Alliance(101, 43, 141),
        Alliance(8, 19, 247), Alliance(2, 124, 267),
        Alliance(1, 85, 129), Alliance(44, 111, 240)
    )

    @Test
    fun checkPowerRanking_firstAndLastTeam() {
        val firstResult = RankedTeam(number = 77, name = "", score = 232.48538)
        val lastResult = RankedTeam(number = 44, name = "", score = -2.6085517)

        val matches = ArrayList<Match>(redAlliances.size)

        for (i in redAlliances.indices) {
            matches.add(Match("", "", i, redAlliances[i], blueAlliances[i]))
        }

        runBlocking {
            val results = OffensivePowerRanking.computeOpr(matches)
            results ?: throw IllegalStateException("OPR could not be computed")

            results.forEach {
                println(it.score)
            }

            assert(results.first() == firstResult)
            assert(results.last() == lastResult)
        }
    }
}