package net.gearmaniacs.ftcscouting.ui.fragments.login

import net.gearmaniacs.ftcscouting.model.User

interface LoginCallback {

    fun onLogin(email: String, password: String)

    fun onRegister(user: User, email: String, password: String)

    fun switchFragment()
}
