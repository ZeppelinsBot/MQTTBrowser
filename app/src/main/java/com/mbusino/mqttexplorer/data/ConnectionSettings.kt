package com.mbusino.mqttexplorer.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ConnectionSettings(
    val name: String,
    val brokerUrl: String,
    val port: String = "1883",
    val username: String = "",
    val password: String = ""
) {
    val fullUrl: String
        get() = "tcp://$brokerUrl:$port"
}

object ConnectionStorage {
    private const val PREFS_NAME = "mqtt_connections"
    private const val KEY_CONNECTIONS = "connections_list"
    private val gson = Gson()

    fun getConnections(prefs: android.content.SharedPreferences): List<ConnectionSettings> {
        val json = prefs.getString(KEY_CONNECTIONS, "[]") ?: "[]"
        val type = object : TypeToken<List<ConnectionSettings>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveConnection(prefs: android.content.SharedPreferences, settings: ConnectionSettings) {
        val connections = getConnections(prefs).toMutableList()
        val existingIndex = connections.indexOfFirst { it.name == settings.name }
        if (existingIndex >= 0) {
            connections[existingIndex] = settings
        } else {
            connections.add(settings)
        }
        prefs.edit().putString(KEY_CONNECTIONS, gson.toJson(connections)).apply()
    }

    fun deleteConnection(prefs: android.content.SharedPreferences, name: String) {
        val connections = getConnections(prefs).toMutableList()
        connections.removeAll { it.name == name }
        prefs.edit().putString(KEY_CONNECTIONS, gson.toJson(connections)).apply()
    }
}
