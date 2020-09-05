package net.gearmaniacs.login.interfaces

import net.gearmaniacs.core.model.UserTeam

internal interface LoginCallback : LoginBaseCallback {

    fun onSignIn(email: String, password: String)

    fun onRegister(userTeam: UserTeam, email: String, password: String)

    fun showBaseFragment()

    fun isWorking(): Boolean
}
