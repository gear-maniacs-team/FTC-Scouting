package net.gearmaniacs.login.interfaces

import net.gearmaniacs.core.model.UserData

internal interface LoginCallback : LoginBaseCallback {

    fun onSignIn(email: String, password: String)

    fun onRegister(userData: UserData, email: String, password: String)

    fun showBaseFragment()

    fun isWorking(): Boolean
}
