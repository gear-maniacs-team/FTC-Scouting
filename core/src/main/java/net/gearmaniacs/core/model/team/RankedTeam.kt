package net.gearmaniacs.core.model.team

data class RankedTeam(
    override val number: Int,
    override val name: String,
    val score: Double
) : BaseTeam(number, name), Comparable<RankedTeam> {

    override fun compareTo(other: RankedTeam): Int = score.compareTo(other.score)
}
