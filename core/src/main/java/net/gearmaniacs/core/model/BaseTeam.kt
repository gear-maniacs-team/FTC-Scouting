package net.gearmaniacs.core.model

import androidx.room.Ignore
import com.google.firebase.database.Exclude

open class BaseTeam(
    /* TODO: Change to "number" */
    @Ignore @Exclude
    open val id: Int,
    @Ignore @Exclude
    open val name: String? = null
)
