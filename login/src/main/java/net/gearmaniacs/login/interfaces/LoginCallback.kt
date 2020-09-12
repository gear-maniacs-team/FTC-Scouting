package net.gearmaniacs.login.interfaces

internal interface LoginCallback {

    fun showSignInFragment()

    fun showRegisterFragment()

    fun showBaseFragment()

    fun useOfflineAccount()

    fun finishActivity()
}
