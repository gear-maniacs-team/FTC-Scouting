package net.gearmaniacs.login.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.utils.UserTeamPreferences
import net.gearmaniacs.login.R
import javax.inject.Inject
import kotlin.coroutines.resume

class AccountRepository @Inject constructor(
    private val userDataPreferences: UserTeamPreferences
) {

    val userTeamFlow = userDataPreferences.userTeamFlow

    suspend fun updateUserData(userTeam: UserTeam): Int {
        userDataPreferences.updateUserTeam(userTeam)

        if (!Firebase.isLoggedIn)
            return R.string.team_details_updated

        val task = Firebase.database
            .getReference(DatabasePaths.KEY_USERS)
            .child(Firebase.auth.currentUser!!.uid)
            .setValue(userTeam)

        return suspendCancellableCoroutine { cont ->

            task.addOnCompleteListener {
                cont.resume(
                    if (it.isSuccessful) R.string.team_details_updated else R.string.team_details_update_error
                )
            }

        }
    }

}
