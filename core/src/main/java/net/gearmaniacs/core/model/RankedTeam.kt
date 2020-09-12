package net.gearmaniacs.core.model

data class RankedTeam(
    override val id: Int,
    override val name: String,
    // TODO: Make val
    var score: Double
) : BaseTeam(id, name), Comparable<RankedTeam> {

    override fun compareTo(other: RankedTeam): Int = score.compareTo(other.score)
}
