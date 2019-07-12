package net.gearmaniacs.ftcscouting.ui.fragments.login

import net.gearmaniacs.ftcscouting.data.User

interface LoginInterface {

    fun onLogin(email: String, password: String)

    fun onRegister(user: User, email: String, password: String)

    fun switchFragment()
}
