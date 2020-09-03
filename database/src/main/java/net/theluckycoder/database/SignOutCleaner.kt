package net.theluckycoder.database

import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.gearmaniacs.core.utils.AppPreferences
import javax.inject.Inject

class SignOutCleaner {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var appPreferences: AppPreferences

    fun run() = GlobalScope.launch(Dispatchers.IO) {
        database.clearAllTables()
        appPreferences.isLoggedIn.set(false)
    }
}
