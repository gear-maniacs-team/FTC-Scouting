package net.gearmaniacs.ftcscouting

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.gearmaniacs.ftcscouting.ui.activity.AccountActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountActivityTest {

    @Rule
    @JvmField
    val activity = ActivityScenarioRule(AccountActivity::class.java)

    @Rule
    @JvmField
    val animationsRule = DisableAnimationsRule()

    @Test
    fun testUserTeam() {
        withId(R.id.et_team_number).perform(click(), typeText("420"))
        withId(R.id.et_team_name).perform(click(), typeText("Test Team Name"))
        withId(R.id.btn_update_user_team) perform click()
    }
}
