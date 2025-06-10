package me.shwetagoyal.sliidesampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import me.shwetagoyal.sliidesampleapp.presentation.theme.SliideSampleAppTheme
import me.shwetagoyal.sliidesampleapp.presentation.userlist.composable.UserListScreenRoot

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SliideSampleAppTheme {
                UserListScreenRoot()
            }
        }
    }
}
