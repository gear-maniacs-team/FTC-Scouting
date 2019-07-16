package net.gearmaniacs.core.model

data class Tournament(
    val name: String
) : DatabaseClass<Tournament>() {

    override fun compareTo(other: Tournament): Int = name.compareTo(other.name)
}
