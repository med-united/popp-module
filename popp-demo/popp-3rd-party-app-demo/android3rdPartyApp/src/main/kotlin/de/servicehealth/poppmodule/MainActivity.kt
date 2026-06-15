package de.servicehealth.poppmodule.demo.thirdparty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.servicehealth.poppmodule.demo.App
import de.servicehealth.poppmodule.demo.thirdparty.can.createSecureCanStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(canStore = createSecureCanStore(applicationContext))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
