package com.mbusino.mqttexplorer.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TopicMessage(
    val payload: String,
    val rawPayload: ByteArray? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isImage: Boolean
        get() {
            val bytes = rawPayload ?: return false
            if (bytes.size < 4) return false
            // JPEG: FF D8 FF
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) return true
            // PNG: 89 50 4E 47
            if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) return true
            // GIF: 47 49 46 38
            if (bytes.size >= 4 && bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte()) return true
            // BMP: 42 4D
            if (bytes.size >= 2 && bytes[0] == 0x42.toByte() && bytes[1] == 0x4D.toByte()) return true
            // WebP: 52 49 46 46 ... 57 45 42 50
            if (bytes.size >= 12 && bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() && bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() && bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte()) return true
            return false
        }
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

class TopicNode(
    val name: String,
    val fullPath: String
) {
    val children = mutableMapOf<String, TopicNode>()
    var lastMessage: TopicMessage? = null
    var messageCount: Int = 0
        private set
    val messageHistory = mutableListOf<TopicMessage>()

    // Branch stats — computed after buildTree
    var branchLeafCount: Int = 0
    var branchMessageCount: Int = 0

    fun addMessage(payload: String) {
        val msg = TopicMessage(payload)
        lastMessage = msg
        messageCount++
        messageHistory.add(0, msg)
        if (messageHistory.size > 500) {
            messageHistory.removeAt(messageHistory.size - 1)
        }
    }

    val sortedChildren: List<TopicNode>
        get() = children.values.sortedWith(compareBy { naturalKey(it.name) })

    companion object {
        /** Encodes a name as a padded string for natural sorting.
         *  "abc2" -> "abc\t0000000002" so "abc2" < "abc10" */
        private fun naturalKey(name: String): String {
            val sb = StringBuilder()
            var i = 0
            while (i < name.length) {
                if (name[i].isDigit()) {
                    var j = i
                    while (j < name.length && name[j].isDigit()) j++
                    // Pad number to 12 digits for correct lexicographic order
                    val num = name.substring(i, j)
                    sb.append('\t')
                    sb.append(num.padStart(12, '0'))
                    i = j
                } else {
                    var j = i
                    while (j < name.length && !name[j].isDigit()) j++
                    sb.append(name.substring(i, j).lowercase())
                    i = j
                }
            }
            return sb.toString()
        }

        fun buildTree(messages: Map<String, List<TopicMessage>>): TopicNode {
            val root = TopicNode("root", "")
            for ((topic, msgs) in messages) {
                val parts = topic.split("/")
                var current = root
                val pathParts = mutableListOf<String>()
                for (part in parts) {
                    pathParts.add(part)
                    val path = pathParts.joinToString("/")
                    val child = current.children.getOrPut(part) {
                        TopicNode(part, path)
                    }
                    current = child
                }
                // Add messages to leaf node
                for (msg in msgs.reversed()) {
                    current.messageHistory.add(msg)
                }
                if (msgs.isNotEmpty()) {
                    current.lastMessage = msgs.last()
                    current.messageCount = msgs.size
                    if (current.messageHistory.size > 500) {
                        current.messageHistory.subList(500, current.messageHistory.size).clear()
                    }
                }
            }
            // Compute branch stats recursively
            computeBranchStats(root)
            return root
        }

        private fun computeBranchStats(node: TopicNode): Pair<Int, Int> {
            // Returns (leafCount, messageCount) for this subtree
            if (node.children.isEmpty()) {
                // Leaf node
                node.branchLeafCount = if (node.messageCount > 0) 1 else 0
                node.branchMessageCount = node.messageCount
                return Pair(node.branchLeafCount, node.branchMessageCount)
            }
            var totalLeaves = 0
            var totalMessages = 0
            for (child in node.children.values) {
                val (leaves, msgs) = computeBranchStats(child)
                totalLeaves += leaves
                totalMessages += msgs
            }
            // Include this node's own messages if it has any
            if (node.messageCount > 0) {
                totalLeaves += 1
                totalMessages += node.messageCount
            }
            node.branchLeafCount = totalLeaves
            node.branchMessageCount = totalMessages
            return Pair(totalLeaves, totalMessages)
        }
    }
}
