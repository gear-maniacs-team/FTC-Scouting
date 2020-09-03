package net.gearmaniacs.login.interfaces

internal interface LoginBaseCallback {

    fun showSignInFragment()

    fun showRegisterFragment()

    fun useOfflineAccount()
}
