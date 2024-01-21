package net.gearmaniacs.tournament.utils

import net.gearmaniacs.database.model.team.Team

internal fun Sequence<Team>.filterTeamsByQuery(query: String): Sequence<Team> {
    val pattern = "(?i).*(${query}).*".toPattern()

    return filter {
        pattern.matcher(it.number.toString() + ' ' + it.name.orEmpty()).matches()
    }
}
