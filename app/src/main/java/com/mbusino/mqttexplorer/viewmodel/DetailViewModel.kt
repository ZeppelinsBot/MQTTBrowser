package com.mbusino.mqttexplorer.viewmodel

import androidx.lifecycle.ViewModel
import com.mbusino.mqttexplorer.data.TopicMessage
import com.mbusino.mqttexplorer.mqtt.MqttManager

class DetailViewModel : ViewModel() {

    private val mqttManager = MqttManager.getInstance()

    fun getMessagesForTopic(topicPath: String): List<TopicMessage> {
        return mqttManager.getMessagesForTopic(topicPath)
    }
}
