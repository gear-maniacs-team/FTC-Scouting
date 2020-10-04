package net.gearmaniacs.ftcscouting.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.ftcscouting.BuildConfig
import net.gearmaniacs.ftcscouting.R

@AndroidEntryPoint
@SuppressLint("UseCompatLoadingForDrawables")
class AboutActivity : MaterialAboutActivity() {

    private class OpenUrlAction(
        private val context: Context,
        private val url: String
    ) : MaterialAboutItemOnClickAction {

        override fun onClick() {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }

    private class OpenEmailAction(
        private val context: Context,
        private val email: String,
        private val title: String? = null
    ) : MaterialAboutItemOnClickAction {

        override fun onClick() {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, title)
            }

            if (intent.resolveActivity(context.packageManager) != null)
                context.startActivity(intent)
        }
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList =
        MaterialAboutList.Builder()
            .addCard(getAppCard())
            .addCard(getTeamCard())
            .addCard(getAuthorCard())
            .build()

    override fun getActivityTitle(): CharSequence? = getString(R.string.title_about)

    private fun getAppCard() = MaterialAboutCard.Builder()
        .addItem(
            MaterialAboutTitleItem(
                getString(R.string.app_name),
                "© 2019-2020 Gear Maniacs",
                getDrawable(R.mipmap.ic_launcher)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.version),
                BuildConfig.VERSION_NAME,
                getDrawable(R.drawable.ic_about_version)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_privacy_policy),
                PRIVACY_POLICY,
                getDrawable(R.drawable.ic_about_privacy_policy),
                OpenUrlAction(this, PRIVACY_POLICY)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_app_feedback),
                APP_EMAIL,
                getDrawable(R.drawable.ic_about_email),
                OpenEmailAction(this, APP_EMAIL, "FTC Scouting App")
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_licenses),
                null,
                getDrawable(R.drawable.ic_about_licenses),
                ::showLicensesDialog
            )
        )
        .build()

    private fun getTeamCard() = MaterialAboutCard.Builder()
        .title("Team")
        .addItem(
            MaterialAboutActionItem(
                "Gear Maniacs",
                "The Most Motivated Robotics Team",
                getDrawable(R.drawable.ic_about_team)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_visit_website),
                GEAR_MANIACS_WEBSITE,
                getDrawable(R.drawable.ic_about_website),
                OpenUrlAction(this, GEAR_MANIACS_WEBSITE)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_follow_instagram),
                GEAR_MANIACS_INSTAGRAM,
                getDrawable(R.drawable.ic_about_instagram),
                OpenUrlAction(this, GEAR_MANIACS_INSTAGRAM)
            )
        )
        .build()

    private fun getAuthorCard() = MaterialAboutCard.Builder()
        .title(getString(R.string.about_developer))
        .addItem(
            MaterialAboutActionItem(
                "Filea Răzvan",
                "TheLuckyCoder",
                getDrawable(R.drawable.ic_about_author)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_visit_website),
                DEVELOPER_WEBSITE,
                getDrawable(R.drawable.ic_about_website),
                OpenUrlAction(this, DEVELOPER_WEBSITE)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_contact),
                DEVELOPER_EMAIL,
                getDrawable(R.drawable.ic_about_email),
                OpenEmailAction(this, DEVELOPER_EMAIL, getString(R.string.app_name))
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_other_apps),
                DEVELOPER_GOOGLE_PLAY,
                getDrawable(R.drawable.ic_about_play_store),
                OpenUrlAction(this, DEVELOPER_GOOGLE_PLAY)
            )
        )
        .addItem(
            MaterialAboutActionItem(
                "GitHub",
                DEVELOPER_GITHUB,
                getDrawable(R.drawable.ic_about_github),
                OpenUrlAction(this, DEVELOPER_GITHUB)
            )
        )
        .build()

    private fun showLicensesDialog() {
        val dialog = LicenserDialog(this).apply {
            setTitle(R.string.about_licenses)
            setNeutralButton(android.R.string.ok, null)
            setLibrary(
                "Kotlin",
                "https://kotlinlang.org/",
                License.APACHE2
            )
            setLibrary(
                "Kotlin Coroutines",
                "https://github.com/Kotlin/kotlinx.coroutines",
                License.APACHE2
            )
            setLibrary(
                "AndroidX and Jetpack Libraries",
                "https://developer.android.com/jetpack/androidx",
                License.APACHE2
            )
            setLibrary(
                "Firebase",
                "https://github.com/firebase/firebase-android-sdk",
                License.APACHE2
            )
            setLibrary(
                "POI",
                "https://poi.apache.org/",
                License.APACHE2
            )
            setLibrary(
                "Material Intro",
                "https://github.com/heinrichreimer/material-intro",
                License.MIT
            )
            setLibrary(
                "Material About Library",
                "https://github.com/daniel-stoneuk/material-about-library",
                License.MIT
            )
            setLibrary(
                "Licenser",
                "https://github.com/marcoscgdev/Licenser",
                License.MIT
            )
        }

        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        private fun LicenserDialog.setLibrary(title: String, url: String, license: License) =
            setLibrary(Library(title, url, license))

        private const val APP_EMAIL = "gearmaniacsteam@gmail.com"
        private const val DEVELOPER_EMAIL = "mail@theluckycoder.net"

        private const val GEAR_MANIACS_WEBSITE = "https://gearmaniacs.ro"
        private const val GEAR_MANIACS_INSTAGRAM = "https://instagram.com/gearmaniacsteam"
        private const val PRIVACY_POLICY = "https://gearmaniacs.ro/privacy-policy"
        private const val DEVELOPER_WEBSITE = "http://theluckycoder.net"
        private const val DEVELOPER_GOOGLE_PLAY =
            "https://play.google.com/store/apps/dev?id=7253118641640283145"
        private const val DEVELOPER_GITHUB = "https://github.com/TheLuckyCoder"
    }
}
