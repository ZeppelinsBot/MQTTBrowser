package com.mbusino.mqttexplorer.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TopicMessage(
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
) {
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
        get() = children.values.sortedBy { it.name }

    companion object {
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
