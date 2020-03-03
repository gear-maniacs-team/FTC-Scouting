package net.gearmaniacs.ftcscouting.ui.activity

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
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.ftcscouting.BuildConfig
import net.gearmaniacs.ftcscouting.R

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

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        return MaterialAboutList.Builder()
            .addCard(getAppCard())
            .addCard(getTeamCard())
            .addCard(getAuthorCard())
            .build()
    }

    override fun getActivityTitle(): CharSequence? = getString(R.string.title_about)

    private fun getAppCard() = MaterialAboutCard.Builder()
        .addItem(
            MaterialAboutTitleItem(
                getString(R.string.app_name),
                "© 2019 Gear Maniacs",
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
                OpenEmailAction(this, APP_EMAIL, getString(R.string.app_name))
            )
        )
        .addItem(
            MaterialAboutActionItem(
                getString(R.string.about_licenses),
                null,
                getDrawable(R.drawable.ic_about_licenses),
                MaterialAboutItemOnClickAction { startActivity<LicensesActivity>() }
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
        .build()

    private fun getAuthorCard() = MaterialAboutCard.Builder()
        .title(getString(R.string.about_author))
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
                AUTHOR_WEBSITE,
                getDrawable(R.drawable.ic_about_website),
                OpenUrlAction(this, AUTHOR_WEBSITE)
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
                "GitHub",
                AUTHOR_GITHUB,
                getDrawable(R.drawable.ic_about_github),
                OpenEmailAction(this, AUTHOR_GITHUB, getString(R.string.app_name))
            )
        )
        .build()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        private const val APP_EMAIL = "gearmaniacsteam@gmail.com"
        private const val DEVELOPER_EMAIL = "mail@theluckycoder.net"

        private const val GEAR_MANIACS_WEBSITE = "https://gearmaniacs.ro/"
        private const val PRIVACY_POLICY = "https://gearmaniacs.ro/privacy-policy/"
        private const val AUTHOR_WEBSITE = "http://theluckycoder.net/"
        private const val AUTHOR_GITHUB = "https://github.com/TheLuckyCoder/"
    }

}