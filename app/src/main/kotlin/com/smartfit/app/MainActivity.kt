package com.smartfit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.smartfit.app.data.local.datastore.UserPreferences
import com.smartfit.app.navigation.SmartFitNavGraph
import com.smartfit.app.service.StepCounterService
import com.smartfit.app.ui.theme.SmartFitTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class  MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true || Build.VERSION.SDK_INT < 29) {
            checkUserAndStartService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")
        enableEdgeToEdge()

        val userPrefs = UserPreferences(applicationContext)

        setContent {
            val isDarkTheme by userPrefs.isDarkTheme.collectAsState(initial = true)
            val isLoggedIn by userPrefs.isLoggedIn.collectAsState(initial = false)

            // Permissions and Service logic moved inside setContent to ensure Compose is ready
            LaunchedEffect(Unit) {
                checkPermissions()
            }

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    checkUserAndStartService()
                }
            }

            Log.d(TAG, "Rendering with isDarkTheme=$isDarkTheme")

            SmartFitTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                SmartFitNavGraph(
                    navController = navController,
                    isDarkTheme   = isDarkTheme,
                    onThemeChange = { }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            checkUserAndStartService()
        }
    }

    private fun checkUserAndStartService() {
        val userPrefs = UserPreferences(applicationContext)
        lifecycleScope.launch {
            if (userPrefs.isLoggedIn.first()) {
                startStepCounterService()
            }
        }
    }

    private fun startStepCounterService() {
        val intent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
