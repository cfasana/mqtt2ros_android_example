package com.h_fluently.mqtt2ros;
import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class MQTTClient {
    private MqttAndroidClient mqttClient;

    public MQTTClient(Context context, String brokerURI, String clientId){
        mqttClient = new MqttAndroidClient(context, brokerURI, clientId);
    }

    public void connect(String username, String psw, IMqttActionListener connectCallback, MqttCallback mqttCallback) throws MqttException {
        mqttClient.setCallback(mqttCallback);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(psw.toCharArray());

        mqttClient.connect(options, null, connectCallback);
    }

    public void disconnect(IMqttActionListener disconnectCallback) throws MqttException {
        mqttClient.disconnect(null, disconnectCallback);
    }

    public void publish(String topicName, String msg, int qualityOfService, boolean retainMsg, IMqttActionListener publishCallback) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(msg.getBytes());
        mqttMessage.setQos(qualityOfService);
        mqttMessage.setRetained(retainMsg);

        mqttClient.publish(topicName, mqttMessage, null, publishCallback);
    }

    public void subscribe(String topicName, int qualityOfService, IMqttActionListener subscribeCallback) throws MqttException {
        mqttClient.subscribe(topicName, qualityOfService, null, subscribeCallback);
    }

    public void unsubscribe(String topicName, IMqttActionListener unsubscribeCallback) throws MqttException {
        mqttClient.unsubscribe(topicName, null, unsubscribeCallback);
    }
}
