package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.Licenser

class LicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val licenses = Licenser()
            .setLibrary(
                "Kotlin",
                "https://kotlinlang.org/",
                License.APACHE
            )
            .setLibrary(
                "AndroidX and Jetpack Libraries",
                "https://developer.android.com/jetpack/androidx",
                License.APACHE
            )
            .setLibrary(
                "Firebase",
                "https://github.com/firebase/firebase-android-sdk",
                License.APACHE
            )
            .setLibrary(
                "POI",
                "https://poi.apache.org/",
                License.APACHE
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
        webView.loadData(licenses.htmlContent, "text/html; charset=UTF-8", null)

        setContentView(webView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        private fun Licenser.setLibrary(title: String, url: String, license: Int) =
            setLibrary(Library(title, url, license))
    }
}