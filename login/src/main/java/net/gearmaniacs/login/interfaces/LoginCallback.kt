package net.gearmaniacs.login.interfaces

import net.gearmaniacs.core.model.UserData

interface LoginCallback {

    fun onLogin(email: String, password: String)

    fun onRegister(userData: UserData, email: String, password: String)

    fun switchFragment()

    fun isWorking(): Boolean
}
