package com.callunlimited

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callunlimited.sip.SipService
import com.callunlimited.ui.CallViewModel
import com.callunlimited.ui.DialerScreen
import com.callunlimited.ui.InCallScreen
import com.callunlimited.ui.theme.CallUnlimitedTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startSipService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        firebaseAnalytics = Firebase.analytics
        
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
        )

        setContent {
            CallUnlimitedTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500) // Splash delay
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    MainContent()
                }
            }
        }
    }

    @Composable
    fun SplashScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = "Splash Icon",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

    @Composable
    fun MainContent() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val viewModel: CallViewModel = hiltViewModel()
            val callState by viewModel.callState.collectAsState()

            if (callState == null) {
                DialerScreen(viewModel)
            } else {
                InCallScreen(viewModel)
            }
        }
    }

    private fun startSipService() {
        val intent = Intent(this, SipService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
