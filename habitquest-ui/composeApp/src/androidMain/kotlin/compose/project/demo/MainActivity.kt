package compose.project.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import compose.project.demo.contexts.habits.infrastructure.calendar.rememberHabitCalendarLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent { App(habitCalendarLauncher = rememberHabitCalendarLauncher()) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
