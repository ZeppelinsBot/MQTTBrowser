package com.mbusino.mqttexplorer.viewmodel

import androidx.lifecycle.ViewModel
import com.mbusino.mqttexplorer.data.TopicNode
import com.mbusino.mqttexplorer.mqtt.MqttManager
import com.mbusino.mqttexplorer.mqtt.SubscribeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TreeViewModel : ViewModel() {

    private val mqttManager = MqttManager.getInstance()

    val topicTree: StateFlow<TopicNode> = mqttManager.topicTree

    val subscribeMode: StateFlow<SubscribeMode> = mqttManager.subscribeMode

    val pathFilter: StateFlow<String> = mqttManager.pathFilter

    /** Non-null while a reconnect in path mode waits for the Ja/Nein decision. */
    val reconnectDecision: StateFlow<String?> = mqttManager.reconnectDecision

    private val _expandedNodes = MutableStateFlow(setOf<String>())
    val expandedNodes: StateFlow<Set<String>> = _expandedNodes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * While a filter is active every matching branch is expanded automatically,
     * so _expandedNodes alone can no longer describe what is visible. Paths the
     * user collapsed by hand during an active filter are collected here instead:
     * they survive recomposition (incoming messages) and are dropped together
     * with the filter, restoring the pre-filter expansion.
     */
    private val _collapsedWhileFiltering = MutableStateFlow(setOf<String>())
    val collapsedWhileFiltering: StateFlow<Set<String>> = _collapsedWhileFiltering.asStateFlow()

    fun toggleNode(path: String) {
        if (_searchQuery.value.isNotBlank()) {
            val current = _collapsedWhileFiltering.value.toMutableSet()
            if (!current.remove(path)) current.add(path)
            _collapsedWhileFiltering.value = current
            return
        }
        val current = _expandedNodes.value.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _expandedNodes.value = current
    }

    fun expandAll(node: TopicNode) {
        if (_searchQuery.value.isNotBlank()) {
            // The filter already expands every match — undo a manual collapse.
            _collapsedWhileFiltering.value = emptySet()
            return
        }
        val paths = mutableSetOf<String>()
        collectPaths(node, paths)
        _expandedNodes.value = paths
    }

    fun collapseAll() {
        if (_searchQuery.value.isNotBlank()) {
            // Collapse every branch the filter currently shows.
            val visible = mutableSetOf<String>()
            collectMatchingPaths(topicTree.value, visible)
            _collapsedWhileFiltering.value = visible
            return
        }
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

    /** Branch paths that remain visible under the current search. */
    private fun collectMatchingPaths(node: TopicNode, paths: MutableSet<String>) {
        if (_searchQuery.value.isBlank()) return
        for (child in node.sortedChildren) {
            if (hasMatchingDescendant(child)) {
                paths.add(child.fullPath)
                collectMatchingPaths(child, paths)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) _collapsedWhileFiltering.value = emptySet()
    }

    /** True when the node itself or any topic below it matches the current search. */
    fun hasMatchingDescendant(node: TopicNode): Boolean {
        if (_searchQuery.value.isBlank()) return true
        if (node.fullPath.contains(_searchQuery.value, ignoreCase = true)) return true
        return node.sortedChildren.any { hasMatchingDescendant(it) }
    }

    fun subscribe(topic: String) {
        mqttManager.subscribeToWildcard(topic)
    }

    /** Switch to path mode: unsubscribe "#", clear tree, subscribe "<path>/#". */
    fun switchToPathMode(path: String): Boolean = mqttManager.subscribeToPathMode(path)

    /** Back to wildcard mode: unsubscribe path filter, subscribe "#" (tree kept). */
    fun switchToWildcardMode() = mqttManager.switchToWildcardMode()

    /** Reconnect popup: true = Ja (wildcard), false = Nein (keep saved filter). */
    fun resolveReconnectDecision(useWildcard: Boolean) = mqttManager.resolveReconnectDecision(useWildcard)

    fun unsubscribe(topic: String) {
        mqttManager.unsubscribe(topic)
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retain: Boolean = false): Boolean {
        return mqttManager.publish(topic, payload, qos, retain)
    }
}
