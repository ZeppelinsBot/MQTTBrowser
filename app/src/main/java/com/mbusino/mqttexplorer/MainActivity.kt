package com.mbusino.mqttexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mbusino.mqttexplorer.mqtt.ConnectionState
import com.mbusino.mqttexplorer.mqtt.MqttManager
import com.mbusino.mqttexplorer.ui.screens.ConnectionScreen
import com.mbusino.mqttexplorer.ui.screens.DetailScreen
import com.mbusino.mqttexplorer.ui.screens.TreeScreen
import com.mbusino.mqttexplorer.ui.theme.MQTTExplorerTheme
import com.mbusino.mqttexplorer.viewmodel.ConnectionViewModel
import com.mbusino.mqttexplorer.viewmodel.DetailViewModel
import com.mbusino.mqttexplorer.viewmodel.TreeViewModel
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MQTTExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MQTTExplorerNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MqttManager.getInstance().disconnect()
    }
}

@Composable
fun MQTTExplorerNavHost() {
    val navController = rememberNavController()
    val connectionViewModel: ConnectionViewModel = viewModel()
    val treeViewModel: TreeViewModel = viewModel()
    val detailViewModel: DetailViewModel = viewModel()
    val mqttManager = MqttManager.getInstance()

    // Auto-reconnect when app comes back from background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!mqttManager.isConnected() && mqttManager.hasLastSettings()) {
                    mqttManager.reconnect()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(navController = navController, startDestination = "connection") {
        composable("connection") {
            ConnectionScreen(
                viewModel = connectionViewModel,
                onConnected = {
                    navController.navigate("tree") {
                        popUpTo("connection") { inclusive = true }
                    }
                }
            )
        }

        composable("tree") {
            TreeScreen(
                viewModel = treeViewModel,
                connectionViewModel = connectionViewModel,
                onTopicClick = { path, name ->
                    val encodedPath = URLEncoder.encode(path, "UTF-8")
                    val encodedName = URLEncoder.encode(name, "UTF-8")
                    navController.navigate("detail/$encodedPath/$encodedName")
                },
                onDisconnect = {
                    connectionViewModel.disconnect()
                    navController.navigate("connection") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "detail/{topicPath}/{topicName}",
            arguments = listOf(
                navArgument("topicPath") { type = NavType.StringType },
                navArgument("topicName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val topicPath = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("topicPath") ?: "",
                "UTF-8"
            )
            val topicName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("topicName") ?: "",
                "UTF-8"
            )
            DetailScreen(
                viewModel = detailViewModel,
                treeViewModel = treeViewModel,
                topicPath = topicPath,
                topicName = topicName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
