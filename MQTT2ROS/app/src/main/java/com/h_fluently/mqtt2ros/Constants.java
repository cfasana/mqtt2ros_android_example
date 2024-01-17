package com.h_fluently.mqtt2ros;

public class Constants {
    public static final String MQTT_BROKER_URI = "tcp://localhost:1883";
    public static final String MQTT_USERNAME = "";
    public static final String MQTT_PSW = "";
    public static final int MQTT_QOS = 2;
    public static final boolean MQTT_RETAINED = false;
    public static final String MQTT_CLIENT_ID = "";
    public static final String MQTT_SUBSCRIPTION_TOPIC = "mqtt/ros2mqtt";
    public static final String MQTT_PUBLISHED_TOPIC = "mqtt/mqtt2ros";
    public static final String MQTT_PUBLISHED_MSG = "This topic is sent from MQTT.";
}
