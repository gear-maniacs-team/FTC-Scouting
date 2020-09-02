package net.gearmaniacs.ftcscouting

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import android.view.View
import org.hamcrest.Matcher

infix fun Matcher<View>.perform(viewActions: ViewAction) {
    Espresso.onView(this).perform(viewActions)
}

fun Matcher<View>.perform(vararg viewActions: ViewAction) {
    Espresso.onView(this).perform(*viewActions)
}

fun goBack() {
    Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
}
