package com.mbusino.mqttexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbusino.mqttexplorer.data.TopicNode
import com.mbusino.mqttexplorer.mqtt.ConnectionState
import com.mbusino.mqttexplorer.ui.components.TopicTreeItem
import com.mbusino.mqttexplorer.viewmodel.ConnectionViewModel
import com.mbusino.mqttexplorer.ui.theme.ConnectedGreen
import com.mbusino.mqttexplorer.viewmodel.TreeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeScreen(
    viewModel: TreeViewModel,
    connectionViewModel: ConnectionViewModel,
    onTopicClick: (String, String) -> Unit,
    onDisconnect: () -> Unit
) {
    val topicTree by viewModel.topicTree.collectAsState()
    val expandedNodes by viewModel.expandedNodes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val connectionState by connectionViewModel.connectionState.collectAsState()
    var showSubscribeDialog by remember { mutableStateOf(false) }
    var subscribeTopic by remember { mutableStateOf("") }
    var showPublishDialog by remember { mutableStateOf(false) }
    var publishTopic by remember { mutableStateOf("") }
    var publishPayload by remember { mutableStateOf("") }
    var publishQos by remember { mutableStateOf(1) }
    var publishRetain by remember { mutableStateOf(false) }
    var publishResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, com.mbusino.mqttexplorer.R.drawable.ic_logo).asImageBitmap(),
                            contentDescription = "MQTT Browser Logo",
                            modifier = Modifier
                                .height(40.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column {
                            Text("MQTT Browser")
                            if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR) {
                                Text(
                                    text = "Disconnected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (connectionState == ConnectionState.CONNECTED) {
                        IconButton(onClick = { viewModel.expandAll(topicTree) }) {
                            Icon(Icons.Default.UnfoldMore, contentDescription = "Expand All", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        IconButton(onClick = { viewModel.collapseAll() }) {
                            Icon(Icons.Default.UnfoldLess, contentDescription = "Collapse All", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR) {
                        IconButton(onClick = { connectionViewModel.reconnect() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reconnect", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Default.Close, contentDescription = "Disconnect", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        },
        floatingActionButton = {
            if (connectionState == ConnectionState.CONNECTED) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showPublishDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Publish message")
                    }
                    FloatingActionButton(onClick = { showSubscribeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Subscribe to topic")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Reconnect banner when disconnected
            if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { connectionViewModel.reconnect() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Reconnect")
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Filter topics...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Topic tree
            val filteredTree = filterTree(topicTree, viewModel)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (topicTree.sortedChildren.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No topics received yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Connect to a broker and subscribe to topics.\nNew messages will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    renderTreeNodes(
                        nodes = filteredTree,
                        expandedNodes = expandedNodes,
                        depth = 0,
                        onToggle = { viewModel.toggleNode(it) },
                        onNavigate = { path, name -> onTopicClick(path, name) },
                        onNavigateInternal = { path, name -> onTopicClick(path, name) },
                        onPublish = { path ->
                            publishTopic = path
                            showPublishDialog = true
                        }
                    )
                }
            }
        }
    }

    // Subscribe dialog
    if (showSubscribeDialog) {
        AlertDialog(
            onDismissRequest = { showSubscribeDialog = false },
            title = { Text("Subscribe to Topic") },
            text = {
                OutlinedTextField(
                    value = subscribeTopic,
                    onValueChange = { subscribeTopic = it },
                    label = { Text("Topic or wildcard") },
                    placeholder = { Text("e.g. MBusino/#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (subscribeTopic.isNotBlank()) {
                            viewModel.subscribe(subscribeTopic)
                            subscribeTopic = ""
                            showSubscribeDialog = false
                        }
                    }
                ) {
                    Text("Subscribe")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSubscribeDialog = false
                    subscribeTopic = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Publish dialog
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text("Publish Message") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = publishTopic,
                        onValueChange = { publishTopic = it },
                        label = { Text("Topic") },
                        placeholder = { Text("e.g. MBusino/test") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = publishPayload,
                        onValueChange = { publishPayload = it },
                        label = { Text("Payload") },
                        placeholder = { Text("Message content") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 6
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("QoS:", style = MaterialTheme.typography.bodyMedium)
                        listOf(0, 1, 2).forEach { qos ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.RadioButton(
                                    selected = publishQos == qos,
                                    onClick = { publishQos = qos }
                                )
                                Text("$qos", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Retain", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = publishRetain,
                            onCheckedChange = { publishRetain = it }
                        )
                    }
                    publishResult?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (it.startsWith("✓")) ConnectedGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (publishTopic.isNotBlank()) {
                            val ok = viewModel.publish(publishTopic, publishPayload, publishQos, publishRetain)
                            publishResult = if (ok) "✓ Published (${publishPayload.toByteArray().size} bytes)" else "✗ Publish failed"
                        }
                    }
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPublishDialog = false
                    publishTopic = ""
                    publishPayload = ""
                    publishQos = 1
                    publishRetain = false
                    publishResult = null
                }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun filterTree(root: TopicNode, viewModel: TreeViewModel): List<TopicNode> {
    return root.sortedChildren.filter { child ->
        viewModel.hasMatchingDescendant(child) || viewModel.matchesSearch(child)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderTreeNodes(
    nodes: List<TopicNode>,
    expandedNodes: Set<String>,
    depth: Int,
    onToggle: (String) -> Unit,
    onNavigate: (String, String) -> Unit,
    onNavigateInternal: (String, String) -> Unit,
    onPublish: (String) -> Unit
) {
    for (node in nodes) {
        val isExpanded = expandedNodes.contains(node.fullPath) || node.fullPath.isEmpty()
        item(key = node.fullPath) {
            TopicTreeItem(
                node = node,
                depth = depth,
                isExpanded = isExpanded,
                onToggle = { onToggle(node.fullPath) },
                onNavigate = { onNavigate(node.fullPath, node.name) },
                onPublish = { onPublish(node.fullPath) }
            )
        }
        if (isExpanded && node.children.isNotEmpty()) {
            renderTreeNodes(
                nodes = node.sortedChildren,
                expandedNodes = expandedNodes,
                depth = depth + 1,
                onToggle = onToggle,
                onNavigate = onNavigate,
                onNavigateInternal = onNavigateInternal,
                onPublish = onPublish
            )
        }
    }
}
