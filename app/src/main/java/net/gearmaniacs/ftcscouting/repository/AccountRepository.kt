package net.gearmaniacs.ftcscouting.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.utils.UserDataPreferences
import net.gearmaniacs.ftcscouting.R
import javax.inject.Inject
import kotlin.coroutines.resume

class AccountRepository @Inject constructor(
    private val userDataPreferences: UserDataPreferences
) {

    suspend fun updateUserData(userData: UserData): Int {
        userDataPreferences.userTeamNumber.set(userData.id)
        userDataPreferences.userTeamName.set(userData.teamName)

        if (!Firebase.isLoggedIn)
            return R.string.team_details_updated

        val task = Firebase.database
            .getReference(DatabasePaths.KEY_USERS)
            .child(Firebase.auth.currentUser!!.uid)
            .setValue(userData)

        return suspendCancellableCoroutine { cont ->

            task.addOnCompleteListener {
                cont.resume(
                    if (it.isSuccessful) R.string.team_details_updated else R.string.team_details_update_error
                )
            }

        }
    }

}
