package com.mbusino.mqttexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.mbusino.mqttexplorer.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mbusino.mqttexplorer.BuildConfig
import com.mbusino.mqttexplorer.data.ConnectionSettings
import com.mbusino.mqttexplorer.mqtt.ConnectionState
import com.mbusino.mqttexplorer.ui.theme.ConnectedGreen
import com.mbusino.mqttexplorer.ui.theme.ConnectingYellow
import com.mbusino.mqttexplorer.ui.theme.DisconnectedRed
import com.mbusino.mqttexplorer.viewmodel.ConnectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,
    onConnected: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val savedConnections by viewModel.savedConnections.collectAsStateWithLifecycle()
    val brokerUrl by viewModel.brokerUrl.collectAsStateWithLifecycle()
    val port by viewModel.port.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val connectionName by viewModel.connectionName.collectAsStateWithLifecycle()
    val tls by viewModel.tls.collectAsStateWithLifecycle()
    val trustAll by viewModel.trustAll.collectAsStateWithLifecycle()
    val caCertUri by viewModel.caCertUri.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<ConnectionSettings?>(null) }

    val certPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onCaCertUriChange(it.toString()) }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            viewModel.saveCurrentConnection()
            onConnected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_logo).asImageBitmap(),
                            contentDescription = "MQTT Browser Logo",
                            modifier = Modifier
                                .height(40.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text("MQTT Browser")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Version info
            item {
                Text(
                    text = "MQTT Browser v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Connection status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (connectionState) {
                            ConnectionState.CONNECTED -> ConnectedGreen.copy(alpha = 0.1f)
                            ConnectionState.CONNECTING -> ConnectingYellow.copy(alpha = 0.1f)
                            ConnectionState.ERROR -> DisconnectedRed.copy(alpha = 0.1f)
                            ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (connectionState) {
                            ConnectionState.CONNECTED -> {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = ConnectedGreen
                                )
                                Text("Connected", color = ConnectedGreen)
                            }
                            ConnectionState.CONNECTING -> {
                                CircularProgressIndicator(modifier = Modifier.height(20.dp))
                                Text("Connecting...")
                            }
                            ConnectionState.ERROR -> {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = DisconnectedRed
                                )
                                Text("Connection error", color = DisconnectedRed)
                            }
                            ConnectionState.DISCONNECTED -> {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Disconnected")
                            }
                        }
                    }
                }
            }

            // Connection form
            item {
                Text(
                    text = "Connection Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = connectionName,
                    onValueChange = { viewModel.onConnectionNameChange(it) },
                    label = { Text("Connection Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = brokerUrl,
                    onValueChange = { viewModel.onBrokerUrlChange(it) },
                    label = { Text("Broker Host") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("192.168.1.8") }
                )
            }

            item {
                OutlinedTextField(
                    value = port,
                    onValueChange = { viewModel.onPortChange(it) },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("1883") }
                )
            }

            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text("Username (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Password (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            // TLS toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TLS Encryption", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (tls) "ssl://" else "tcp://",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = tls,
                                onCheckedChange = { viewModel.onTlsChange(it) }
                            )
                        }
                        if (tls) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onTrustAllChange(!trustAll) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = trustAll,
                                    onCheckedChange = { viewModel.onTrustAllChange(it) }
                                )
                                Column {
                                    Text("Trust all certificates", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "For self-signed certs (e.g. Homelab)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (tls && !trustAll) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { certPickerLauncher.launch(arrayOf("application/x-pem-file", "application/x-x509-ca-cert", "*/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(if (caCertUri.isBlank()) "Import CA Certificate" else "CA Certificate imported ✓")
                            }
                            if (caCertUri.isNotBlank()) {
                                Text(
                                    text = caCertUri.substringAfterLast('/').take(40),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.connect() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = connectionState != ConnectionState.CONNECTING
                ) {
                    Text("Connect")
                }
            }

            // Saved connections
            if (savedConnections.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Connections",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(savedConnections) { connection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadConnection(connection) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = connection.name,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "${connection.brokerUrl}:${connection.port}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (connection.username.isNotBlank()) {
                                    Text(
                                        text = "User: ${connection.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { showDeleteDialog = connection }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete connection",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { connection ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Connection") },
            text = { Text("Delete saved connection \"${connection.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteConnection(connection.name)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


