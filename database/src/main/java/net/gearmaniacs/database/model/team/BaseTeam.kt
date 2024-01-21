package net.gearmaniacs.database.model.team

import androidx.room.Ignore

open class BaseTeam(
    @Ignore
    open val number: Int,
    @Ignore
    open val name: String? = null
)
