package net.gearmaniacs.ftcscouting.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.ftcscouting.BuildConfig
import net.gearmaniacs.ftcscouting.R

@AndroidEntryPoint
class AboutActivity : ComponentActivity() {

    private fun openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun openEmail(email: String, title: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, title)
        }

        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                Scaffold(Modifier.fillMaxSize(), bottomBar = {
                    BottomAppBar(actions = {
                        IconButton(onClick = { finish() }) {
                            Icon(painterResource(R.drawable.ic_arrow_back), null)
                        }
                    })
                }) { paddingValues ->
                    Column(
                        Modifier
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppCard()
                        TeamCard()
                        AuthorCard()
                    }
                }
            }
        }
    }

    @Composable
    private fun Item(
        title: String, summary: String?, iconResId: Int, action: (() -> Unit)? = null
    ) {
        val mod = if (action != null) Modifier.clickable(
            role = Role.Button, onClick = action
        ) else Modifier
        ListItem(modifier = mod,
            headlineContent = { Text(title) },
            supportingContent = if (summary != null) {
                { Text(summary) }
            } else null,
            leadingContent = {
                Icon(painterResource(iconResId), contentDescription = null)
            })
    }

    @Composable
    private fun Item(titleId: Int, summary: String?, iconResId: Int, action: (() -> Unit)? = null) {
        Item(stringResource(titleId), summary, iconResId, action)
    }

    @Composable
    private fun AppCard() = OutlinedCard(Modifier.fillMaxWidth()) {
        ListItem(headlineContent = { Text(stringResource(R.string.app_name)) },
            supportingContent = { Text("© 2019-2023 Gear Maniacs") },
            leadingContent = {
                ResourcesCompat.getDrawable(
                    LocalContext.current.resources, R.mipmap.ic_launcher, LocalContext.current.theme
                )?.let { drawable ->
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888,
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)

                    Icon(
                        bitmap.asImageBitmap(), null, Modifier.size(48.dp), tint = Color.Unspecified
                    )
                }
            })

        Item(R.string.version, BuildConfig.VERSION_NAME, R.drawable.ic_about_version)
        Item(R.string.about_privacy_policy, PRIVACY_POLICY, R.drawable.ic_about_privacy_policy) {
            openUrl(PRIVACY_POLICY)
        }
        Item(R.string.about_app_feedback, APP_EMAIL, R.drawable.ic_about_email) {
            openEmail(APP_EMAIL, "FTC Scouting App")
        }
    }

    @Composable
    private fun TeamCard() = OutlinedCard(Modifier.fillMaxWidth()) {
        Text(
            "Team",
            fontSize = 17.5.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontWeight = FontWeight.SemiBold
        )

        Item("Gear Maniacs", "The Most Motivated Robotics Team", R.drawable.ic_about_team)
        Item(R.string.about_visit_website, GEAR_MANIACS_WEBSITE, R.drawable.ic_about_website) {
            openUrl(GEAR_MANIACS_WEBSITE)
        }
        Item(
            R.string.about_follow_instagram, GEAR_MANIACS_INSTAGRAM, R.drawable.ic_about_instagram
        ) {
            openUrl(GEAR_MANIACS_INSTAGRAM)
        }
    }

    @Composable
    private fun AuthorCard() = OutlinedCard(Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.about_developer),
            fontSize = 17.5.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            fontWeight = FontWeight.SemiBold
        )

        Item("Filea Răzvan", "TheLuckyCoder", R.drawable.ic_about_author)
//        Item(R.string.about_visit_website, DEVELOPER_WEBSITE, R.drawable.ic_about_website) {
//            openUrl(DEVELOPER_WEBSITE)
//        }
        Item(R.string.about_contact, DEVELOPER_EMAIL, R.drawable.ic_about_email) {
            openEmail(DEVELOPER_EMAIL, "Gear Maniacs FTC Scouting App")
        }
        Item("GitHub", DEVELOPER_GITHUB, R.drawable.ic_about_github) {
            openUrl(DEVELOPER_GITHUB)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        private const val APP_EMAIL = "gearmaniacsteam@gmail.com"
        private const val DEVELOPER_EMAIL = "razvan.filea@gmail.com"

        private const val GEAR_MANIACS_WEBSITE = "https://gearmaniacs.ro"
        private const val GEAR_MANIACS_INSTAGRAM = "https://instagram.com/gearmaniacsteam"
        private const val PRIVACY_POLICY = "https://gearmaniacs.ro/privacy-policy"

        //        private const val DEVELOPER_WEBSITE = "http://theluckycoder.net"
        private const val DEVELOPER_GITHUB = "https://github.com/TheLuckyCoder"
    }
}
