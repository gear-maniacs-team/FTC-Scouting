package net.gearmaniacs.login.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.login.R
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@AndroidEntryPoint
class IntroActivity : ComponentActivity() {

    private class Slide(
        @ColorRes val background: Int,
        @ColorRes val backgroundDark: Int,
        val content: @Composable () -> Unit,
    )

    private val slides = listOf(
        Slide(R.color.color_intro_1, R.color.color_dark_intro_1) {
            PresentationSlide(R.string.intro_title_1, R.string.intro_desc_1, R.drawable.img_intro_1)
        },
        Slide(R.color.color_intro_2, R.color.color_dark_intro_2) {
            PresentationSlide(R.string.intro_title_2, R.string.intro_desc_2, R.drawable.img_intro_2)
        },
        Slide(R.color.color_intro_3, R.color.color_dark_intro_3) {
            PresentationSlide(R.string.intro_title_3, R.string.intro_desc_3, R.drawable.img_intro_3)
        },

        Slide(R.color.color_intro_4, R.color.color_dark_intro_4) {
            TermsSlide()
        },
    )

    @Inject
    lateinit var appPreferences: AppPreferences

    private var termsAccepted by mutableStateOf(false)
    private val pagerState = PagerState(0, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (runBlocking { appPreferences.seenIntro() }) {
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this) {
            lifecycleScope.launch {
                pagerState.animateScrollToPage(0)
            }
        }

        setContent {
            AppTheme {
                val slide = slides[pagerState.currentPage]

                val backgroundColor by animateColorAsState(colorResource(slide.background))

                Surface(color = backgroundColor) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        HorizontalPager(
                            pageCount = slides.size,
                            state = pagerState,
                            modifier = Modifier.align(Alignment.Center)
                        ) { page ->
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .wrapContentSize()
                            ) {
                                slides[page].content()
                            }
                        }

                        BottomSection()
                    }
                }
            }
        }
    }

    @Composable
    private fun BoxScope.BottomSection() {
        val scope = rememberCoroutineScope()

        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage != 0,
                modifier = Modifier.align(Alignment.BottomStart),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage - 1
                        )
                    }
                }) {
                    Icon(painterResource(R.drawable.ic_arrow_back), null)
                }
            }

            AnimatedVisibility(
                visible = pagerState.currentPage != slides.lastIndex,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage - 1
                        )
                    }
                }) {
                    Icon(painterResource(R.drawable.ic_arrow_forward), null)
                }
            }

            AnimatedVisibility(
                visible = pagerState.currentPage == slides.lastIndex && termsAccepted,
                modifier = Modifier.align(Alignment.BottomEnd),
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        appPreferences.setSeenIntro(true)
                        finish()
                    }
                }) {
                    Icon(painterResource(R.drawable.ic_done), null)
                }
            }
        }
    }

    @Composable
    private fun PresentationSlideContent(
        modifier: Modifier, @StringRes title: Int,
        @StringRes description: Int,
        @DrawableRes image: Int,
    ) {
        Image(
            painterResource(image),
            contentDescription = null,
            modifier = modifier
                .padding(16.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
        )

        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(title),
                fontSize = 19.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(description),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun PresentationSlide(
        @StringRes title: Int,
        @StringRes description: Int,
        @DrawableRes image: Int,
    ) {
        Box(Modifier.padding(32.dp)) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PresentationSlideContent(Modifier.fillMaxWidth(), title, description, image)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PresentationSlideContent(Modifier.fillMaxHeight(), title, description, image)
                }
            }
        }
    }

    @Composable
    private fun TermsSlide() {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.terms_and_conditions),
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
            )

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.read_terms_and_conditions),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.Blue,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        try {
                            val url = "https://gearmaniacs.ro/terms-and-conditions/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast
                                .makeText(
                                    this@IntroActivity,
                                    "No application can handle this request. Please install a web browser",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                            e.printStackTrace()
                        }
                    })

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .toggleable(
                        value = termsAccepted,
                        onValueChange = { termsAccepted = !termsAccepted },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = null
                )

                Text(
                    text = stringResource(R.string.agree_terms_and_conditions),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
