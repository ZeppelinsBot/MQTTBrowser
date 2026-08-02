package com.mbusino.mqttexplorer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.mbusino.mqttexplorer.data.TopicMessage
import com.mbusino.mqttexplorer.viewmodel.DetailViewModel
import com.mbusino.mqttexplorer.viewmodel.TreeViewModel
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    treeViewModel: TreeViewModel,
    topicPath: String,
    topicName: String,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var messages by remember { mutableStateOf(viewModel.getMessagesForTopic(topicPath)) }
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    var diffEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(topicPath) {
        while (true) {
            messages = viewModel.getMessagesForTopic(topicPath)
            delay(500)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, com.mbusino.mqttexplorer.R.drawable.ic_logo).asImageBitmap(),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .height(40.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = topicPath
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = if (diffEnabled) "Diff ON" else "Diff OFF",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (diffEnabled) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { diffEnabled = !diffEnabled }
                            .padding(horizontal = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "${messages.size} messages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(messages) { index, message ->
                        val previousMessage = if (index < messages.size - 1) messages[index + 1] else null
                        val isExpanded = expandedStates[index] ?: false

                        MessageCard(
                            message = message,
                            previousMessage = previousMessage,
                            isExpanded = isExpanded,
                            diffEnabled = diffEnabled,
                            onToggleExpand = {
                                expandedStates[index] = !isExpanded
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(message.payload))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: TopicMessage,
    previousMessage: TopicMessage?,
    isExpanded: Boolean,
    diffEnabled: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit
) {
    val isJson = isJsonPayload(message.payload)
    val formattedPayload = if (isJson) formatJson(message.payload) else message.payload
    val lineCount = formattedPayload.lines().size
    val needsExpand = lineCount > 8 || formattedPayload.length > 600

    // Pre-truncate: cut text to 8 lines when collapsed (reliable, no Compose maxLines quirks)
    val truncatedPayload = if (needsExpand && !isExpanded) {
        formattedPayload.lines().take(8).joinToString("\n") + "\n…"
    } else {
        formattedPayload
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header: timestamp + copy + expand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.formattedTime + if (previousMessage != null) "  ${formatDelta(message.timestamp, previousMessage.timestamp)}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    if (needsExpand) {
                        IconButton(onClick = onToggleExpand) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Payload content — image or text
            if (message.isImage && message.rawPayload != null) {
                // Render image from raw bytes
                val bitmap = remember(message.rawPayload) {
                    BitmapFactory.decodeByteArray(message.rawPayload, 0, message.rawPayload.size)?.asImageBitmap()
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "MQTT Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Text(
                        text = "[Image — decode failed]",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (isJson && diffEnabled && previousMessage != null && isJsonPayload(previousMessage.payload)) {
                val diffText = buildJsonDiff(message.payload, previousMessage.payload, isExpanded, needsExpand)
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            } else if (isJson) {
                Text(
                    text = truncatedPayload,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            } else {
                Text(
                    text = if (needsExpand && !isExpanded) {
                        message.payload.lines().take(8).joinToString("\n") + "\n…"
                    } else {
                        message.payload
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Expand hint
            if (needsExpand && !isExpanded) {
                Text(
                    text = "▶ tap to expand",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { onToggleExpand() }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

private fun formatDelta(currentMs: Long, previousMs: Long): String {
    val diffMs = currentMs - previousMs
    if (diffMs < 0) return ""
    return when {
        diffMs < 1000 -> "+${diffMs}ms"
        diffMs < 60_000 -> "+${diffMs / 1000}s"
        diffMs < 3600_000 -> "+${diffMs / 60_000}m"
        else -> "+${diffMs / 3600_000}h"
    }
}

private fun isJsonPayload(payload: String): Boolean {
    val trimmed = payload.trim()
    return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
           (trimmed.startsWith("[") && trimmed.endsWith("]"))
}

private fun formatJson(payload: String): String {
    return try {
        JSONObject(payload).toString(2)
    } catch (_: Exception) {
        try {
            JSONArray(payload).toString(2)
        } catch (_: Exception) {
            payload
        }
    }
}

private fun buildJsonDiff(
    currentPayload: String,
    previousPayload: String,
    isExpanded: Boolean,
    needsExpand: Boolean
): AnnotatedString {
    val currentJson = try { JSONObject(currentPayload) } catch (_: Exception) { null }
    val previousJson = try { JSONObject(previousPayload) } catch (_: Exception) { null }

    if (currentJson == null || previousJson == null) {
        return AnnotatedString(formatJson(currentPayload))
    }

    val changedColor = SpanStyle(
        color = androidx.compose.ui.graphics.Color(0xFF2E7D32), // Dark green
        fontWeight = FontWeight.Bold
    )
    val unchangedColor = SpanStyle(
        color = androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Grey
    )
    val keyColor = SpanStyle(
        color = androidx.compose.ui.graphics.Color(0xFF1565C0) // Blue
    )

    // Build the full diff string first
    val fullDiff = buildAnnotatedString {
        append("{\n")

        val keys = currentJson.keys().asSequence().toList()
        for ((i, key) in keys.withIndex()) {
            val currentValue = currentJson.get(key).toString()
            val previousValue = previousJson.opt(key)?.toString()
            val changed = currentValue != previousValue

            append("  ")
            withStyle(keyColor) { append("\"$key\"") }
            append(": ")

            if (changed) {
                withStyle(changedColor) { append(formatValue(currentJson.get(key))) }
            } else {
                withStyle(unchangedColor) { append(formatValue(currentJson.get(key))) }
            }

            if (i < keys.size - 1) append(",")
            append("\n")
        }

        append("}")
    }

    // Pre-truncate: cut at 8 actual lines when collapsed
    if (needsExpand && !isExpanded) {
        val text = fullDiff.toString()
        val truncated = text.lines().take(8).joinToString("\n") + "\n…"
        return AnnotatedString(truncated)
    }
    return fullDiff
}

private fun formatValue(value: Any?): String {
    return when (value) {
        is JSONObject -> value.toString(2)
        is JSONArray -> value.toString(2)
        is String -> "\"$value\""
        else -> value.toString()
    }
}
