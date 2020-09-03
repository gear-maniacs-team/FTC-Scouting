package net.gearmaniacs.ftcscouting

import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import net.gearmaniacs.ftcscouting.ui.activity.MainActivity
import net.gearmaniacs.ftcscouting.ui.adapter.TournamentAdapter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    val activity = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val animationsRule = DisableAnimationsRule()

    @Test
    fun testSetup() {
        activity.scenario.onActivity {
            runBlocking {
                it.appPreferences.isLoggedIn.setAndCommit(true)
                it.appPreferences.seenIntro.setAndCommit(true)
            }
        }
    }

    @Test
    fun createTournament() {
        val fab = withId(R.id.fab_new_tournament)
        fab perform click()
        withId(R.id.btn_tournament_cancel) perform click()

        fab perform click()
        withId(R.id.et_tournament_name).perform(click(), typeText("Test Tournament"))
        withId(R.id.btn_tournament_action) perform click()

        fab perform click()
        withId(R.id.et_tournament_name).perform(click(), typeText("Tournament To Delete"))
        withId(R.id.btn_tournament_action) perform click()
    }

    @Test
    fun deleteTournament() {
        withId(R.id.rv_tournament).perform(
            RecyclerViewActions.actionOnItemAtPosition<TournamentAdapter.TournamentViewHolder>(
                1,
                longClick()
            )
        )

        withId(android.R.id.button1) perform click()
    }
}
