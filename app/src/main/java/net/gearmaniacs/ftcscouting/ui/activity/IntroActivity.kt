package net.gearmaniacs.ftcscouting.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.app.SlideFragment
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.justTry
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.R
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : IntroActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    class TermsFragment : SlideFragment() {

        private lateinit var cbAcceptedTerms: CheckBox

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = inflater.inflate(R.layout.intro_slide_terms, container, false)

            cbAcceptedTerms = view.findViewById(R.id.mi_description)
            view.findViewById<TextView>(R.id.tv_open_link).setOnClickListener {
                try {
                    val url = "https://gearmaniacs.ro/terms-and-conditions/"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    context?.longToast("No application can handle this request. Please install a web browser")
                    e.printStackTrace()
                }
            }

            return view
        }

        override fun canGoForward(): Boolean = cbAcceptedTerms.isChecked
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appPreferences.seenIntroPref.get()) {
            finish()
            return
        }

        isButtonBackVisible = true
        isButtonNextVisible = true
        buttonNextFunction = BUTTON_NEXT_FUNCTION_NEXT_FINISH
        buttonBackFunction = BUTTON_BACK_FUNCTION_BACK

        setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in)

        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_title_1)
                .description(R.string.intro_desc_1)
                .image(R.drawable.ic_intro_1)
                .background(R.color.color_intro_1)
                .backgroundDark(R.color.color_dark_intro_1)
                .layout(R.layout.intro_slide)
                .build()
        )

        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_title_2)
                .description(R.string.intro_desc_2)
                .image(R.drawable.ic_intro_2)
                .background(R.color.color_intro_2)
                .backgroundDark(R.color.color_dark_intro_2)
                .layout(R.layout.intro_slide)
                .build()
        )

        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_title_3)
                .description(R.string.intro_desc_3)
                .image(R.drawable.ic_intro_3)
                .background(R.color.color_intro_3)
                .backgroundDark(R.color.color_dark_intro_3)
                .layout(R.layout.intro_slide)
                .build()
        )

        val termsSlide = FragmentSlide.Builder()
            .background(R.color.color_intro_4)
            .backgroundDark(R.color.color_dark_intro_4)
            .fragment(TermsFragment())
            .build()

        addSlide(termsSlide)

        addOnNavigationBlockedListener { position, _ ->
            val contentView = findViewById<View>(android.R.id.content)

            if (contentView != null) {
                val slide = getSlide(position)

                if (slide === termsSlide) {
                    Snackbar.make(
                        contentView,
                        R.string.accept_terms_and_conditions,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onBackPressed() {
        justTry {
            goToLastSlide()
        }
    }
}
