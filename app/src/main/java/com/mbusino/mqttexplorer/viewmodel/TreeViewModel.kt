package com.mbusino.mqttexplorer.viewmodel

import androidx.lifecycle.ViewModel
import com.mbusino.mqttexplorer.data.TopicNode
import com.mbusino.mqttexplorer.mqtt.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TreeViewModel : ViewModel() {

    private val mqttManager = MqttManager.getInstance()

    val topicTree: StateFlow<TopicNode> = mqttManager.topicTree

    private val _expandedNodes = MutableStateFlow(setOf<String>())
    val expandedNodes: StateFlow<Set<String>> = _expandedNodes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun toggleNode(path: String) {
        val current = _expandedNodes.value.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _expandedNodes.value = current
    }

    fun expandAll(node: TopicNode) {
        val paths = mutableSetOf<String>()
        collectPaths(node, paths)
        _expandedNodes.value = paths
    }

    fun collapseAll() {
        _expandedNodes.value = emptySet()
    }

    private fun collectPaths(node: TopicNode, paths: MutableSet<String>) {
        for (child in node.sortedChildren) {
            if (child.children.isNotEmpty()) {
                paths.add(child.fullPath)
                collectPaths(child, paths)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun matchesSearch(node: TopicNode): Boolean {
        if (_searchQuery.value.isBlank()) return true
        return node.fullPath.contains(_searchQuery.value, ignoreCase = true)
    }

    fun hasMatchingDescendant(node: TopicNode): Boolean {
        if (_searchQuery.value.isBlank()) return true
        if (node.fullPath.contains(_searchQuery.value, ignoreCase = true)) return true
        return node.sortedChildren.any { hasMatchingDescendant(it) }
    }

    fun subscribe(topic: String) {
        mqttManager.subscribeToWildcard(topic)
    }

    fun unsubscribe(topic: String) {
        mqttManager.unsubscribe(topic)
    }
}
