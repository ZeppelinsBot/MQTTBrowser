package com.mbusino.mqttexplorer.mqtt

import android.util.Log
import com.mbusino.mqttexplorer.data.ConnectionSettings
import com.mbusino.mqttexplorer.data.TopicMessage
import com.mbusino.mqttexplorer.data.TopicNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttSecurityException
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.concurrent.ConcurrentHashMap

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class MqttManager private constructor() {

    companion object {
        private const val TAG = "MqttManager"

        @Volatile
        private var instance: MqttManager? = null

        fun getInstance(): MqttManager {
            return instance ?: synchronized(this) {
                instance ?: MqttManager().also { instance = it }
            }
        }
    }

    private var client: MqttAsyncClient? = null
    private var lastSettings: ConnectionSettings? = null
    private val topicMessages = ConcurrentHashMap<String, MutableList<TopicMessage>>()
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _topicTree = MutableStateFlow(TopicNode("root", ""))
    val topicTree: StateFlow<TopicNode> = _topicTree.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentConnection = MutableStateFlow<ConnectionSettings?>(null)
    val currentConnection: StateFlow<ConnectionSettings?> = _currentConnection.asStateFlow()

    private val _subscribedTopics = MutableStateFlow(setOf<String>())
    val subscribedTopics: StateFlow<Set<String>> = _subscribedTopics.asStateFlow()

    fun connect(settings: ConnectionSettings) {
        if (_connectionState.value == ConnectionState.CONNECTING) return

        disconnect()
        lastSettings = settings

        _currentConnection.value = settings
        _connectionState.value = ConnectionState.CONNECTING
        _errorMessage.value = null

        doConnect(settings)
    }

    fun reconnect() {
        val settings = lastSettings ?: return
        if (_connectionState.value == ConnectionState.CONNECTING) return

        disconnect()

        _currentConnection.value = settings
        _connectionState.value = ConnectionState.CONNECTING
        _errorMessage.value = null

        doConnect(settings)
    }

    private fun doConnect(settings: ConnectionSettings) {
        try {
            val serverUri = settings.fullUrl
            val clientId = "MQTTBrowser_${System.currentTimeMillis()}"
            val persistence = MemoryPersistence()

            client = MqttAsyncClient(serverUri, clientId, persistence).apply {
                setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        Log.w(TAG, "Connection lost", cause)
                        _connectionState.value = ConnectionState.DISCONNECTED
                        _errorMessage.value = "Connection lost: ${cause?.message ?: "Unknown error"}"
                    }

                    override fun messageArrived(topic: String, message: MqttMessage) {
                        val payload = String(message.payload)
                        handleIncomingMessage(topic, payload)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Not used for subscribe-only
                    }
                })
            }

            val connectOptions = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true
                if (settings.username.isNotBlank()) {
                    userName = settings.username
                }
                if (settings.password.isNotBlank()) {
                    password = settings.password.toCharArray()
                }
            }

            client?.connect(connectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(TAG, "Connected to $serverUri")
                    _connectionState.value = ConnectionState.CONNECTED
                    subscribeToWildcard("#")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Connect failed", exception)
                    _connectionState.value = ConnectionState.ERROR
                    _errorMessage.value = "Connection failed: ${exception?.message ?: "Unknown error"}"
                }
            })

        } catch (e: MqttSecurityException) {
            Log.e(TAG, "Security error connecting", e)
            _connectionState.value = ConnectionState.ERROR
            _errorMessage.value = "Security error: ${e.message}"
        } catch (e: MqttException) {
            Log.e(TAG, "Error connecting", e)
            _connectionState.value = ConnectionState.ERROR
            _errorMessage.value = "Error: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error connecting", e)
            _connectionState.value = ConnectionState.ERROR
            _errorMessage.value = "Error: ${e.message}"
        }
    }

    fun subscribeToWildcard(topic: String) {
        try {
            client?.subscribe(topic, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(TAG, "Subscribed to $topic")
                    _subscribedTopics.value = _subscribedTopics.value + topic
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Subscribe failed for $topic", exception)
                    _errorMessage.value = "Subscribe failed: ${exception?.message}"
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Error subscribing", e)
            _errorMessage.value = "Subscribe error: ${e.message}"
        }
    }

    fun unsubscribe(topic: String) {
        try {
            client?.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(TAG, "Unsubscribed from $topic")
                    _subscribedTopics.value = _subscribedTopics.value - topic
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Unsubscribe failed for $topic", exception)
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Error unsubscribing", e)
        }
    }

    private fun handleIncomingMessage(topic: String, payload: String) {
        val msg = TopicMessage(payload)
        val messages = topicMessages.getOrPut(topic) { mutableListOf() }
        messages.add(msg)
        if (messages.size > 500) {
            messages.removeAt(0)
        }
        _topicTree.value = TopicNode.buildTree(topicMessages.toMap())
    }

    fun getMessagesForTopic(topicPath: String): List<TopicMessage> {
        return topicMessages[topicPath]?.toList()?.reversed() ?: emptyList()
    }

    fun disconnect() {
        try {
            client?.let { mqttClient ->
                if (mqttClient.isConnected) {
                    mqttClient.disconnect()
                }
                mqttClient.close()
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Error disconnecting", e)
        }
        client = null
        topicMessages.clear()
        _topicTree.value = TopicNode("root", "")
        _connectionState.value = ConnectionState.DISCONNECTED
        _currentConnection.value = null
        _subscribedTopics.value = emptySet()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isConnected(): Boolean {
        return client?.isConnected == true
    }

    fun hasLastSettings(): Boolean {
        return lastSettings != null
    }
}
