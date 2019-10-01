package net.gearmaniacs.login.utils

import net.gearmaniacs.core.model.User

interface LoginCallback {

    fun onLogin(email: String, password: String)

    fun onRegister(user: User, email: String, password: String)

    fun switchFragment()

    fun isWorking(): Boolean
}
