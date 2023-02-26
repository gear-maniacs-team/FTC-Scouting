package net.gearmaniacs.login.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import net.gearmaniacs.login.R

internal class SignInTab : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        Column {
            LargeTopAppBar(
                title = {
                    Image(painterResource(R.drawable.app_background), contentDescription = null)
                },
                navigationIcon = {
                    IconButton(onClick = { nav.pop() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), null)
                    }
                },
                actions = {

                }
            )
        }
    }
}
