package net.gearmaniacs.core.utils

import android.content.Context
import com.tfcporciuncula.flow.FlowSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import net.gearmaniacs.core.model.UserData
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserDataPreferences @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val preferences =
        FlowSharedPreferences(context.getSharedPreferences("user_data", Context.MODE_PRIVATE))

    val userTeamNumber = preferences.getInt(USER_TEAM_NUMBER, -1)

    val userTeamName = preferences.getString(USER_TEAM_NAME, "")

    val userTeam = UserData(userTeamNumber.get(), userTeamName.get())

    val userTeamFlow = userTeamNumber.asFlow()
        .combine(userTeamName.asFlow()) { id: Int, name: String ->
            UserData(id, name)
        }

    private companion object {
        private const val USER_TEAM_NUMBER = "user_team_number"
        private const val USER_TEAM_NAME = "user_team_name"
    }
}
