package com.nikodindon.retrocade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nikodindon.retrocade.core.ads.MonetizationController
import com.nikodindon.retrocade.core.ui.RetrocadeTheme
import com.nikodindon.retrocade.navigation.RetrocadeNavApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var monetizationController: MonetizationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetrocadeTheme {
                RetrocadeNavApp()
            }
        }
        monetizationController.ensureInitialized(this)
    }
}
