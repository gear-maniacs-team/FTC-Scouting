package net.gearmaniacs.ftcscouting

import android.app.Application
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@Suppress("unused")
@HiltAndroidApp
class ScoutingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FragmentManager.enableNewStateManager(true)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
