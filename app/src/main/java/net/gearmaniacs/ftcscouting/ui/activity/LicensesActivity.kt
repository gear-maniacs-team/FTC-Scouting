package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.Licenser

class LicensesActivity : AppCompatActivity() {

    companion object {
        private fun Licenser.setLibrary(title: String, url: String, license: Int) =
            setLibrary(Library(title, url, license))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val licenser = Licenser()
            .setLibrary(
                "Kotlin",
                "https://kotlinlang.org/",
                License.APACHE2
            )
            .setLibrary(
                "Android Support Libraries",
                "https://developer.android.com/topic/libraries/support-library",
                License.APACHE2
            )
            .setLibrary(
                "Firebase",
                "https://github.com/firebase/firebase-android-sdk",
                License.APACHE2
            )
            .setLibrary(
                "Material Chooser",
                "https://github.com/TheLuckyCoder/MaterialChooser",
                License.APACHE2
            )
            .setLibrary(
                "POI",
                "https://poi.apache.org/",
                License.APACHE2
            )
            .setLibrary(
                "Material Intro",
                "https://github.com/heinrichreimer/material-intro",
                License.MIT
            )
            .setLibrary(
                "Licenser",
                "https://github.com/marcoscgdev/Licenser",
                License.MIT
            )

        val webView = WebView(this)
        webView.loadData(licenser.htmlContent, "text/html; charset=UTF-8", null)

        setContentView(webView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}