package com.h_fluently.mqtt2ros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.eclipse.paho.client.mqttv3.*;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private Button connectBtn;
    private Button disconnectBtn;
    private Button subscribeBtn;
    private Button publishBtn;
    private Button connectionStatusBtn;
    private TextView ros2mqttTxtview;
    private TextView mqtt2rosTxtview;
    private TextView subscribeStatusTxtview;
    private TextView publishStatusTxtview;
    private MQTTClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connect_btn);
        disconnectBtn = findViewById(R.id.disconnect_btn);
        subscribeBtn = findViewById(R.id.subscribe_btn);
        publishBtn = findViewById(R.id.publish_btn);
        connectionStatusBtn = findViewById(R.id.connection_status_btn);;

        ros2mqttTxtview = findViewById(R.id.ros2mqtt_txtview);
        mqtt2rosTxtview = findViewById(R.id.mqtt2ros_txtview2);
        subscribeStatusTxtview = findViewById(R.id.subscribe_status_txtview);
        publishStatusTxtview = findViewById(R.id.publish_status_txtview);

        resetGUI();
        mqttClient = new MQTTClient(getApplicationContext(), Constants.MQTT_BROKER_URI, Constants.MQTT_CLIENT_ID);

        connectBtn.setOnClickListener(view -> {
            try {
                mqttClient.connect(Constants.MQTT_USERNAME, Constants.MQTT_PSW, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Connection to the MQTT server was successful");
                        runOnUiThread(()->{
                            connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.success));
                        });
                        enableMessageExchangeButtons();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Unable to connect to the MQTT server");
                        runOnUiThread(()->{
                            connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                        });
                    }
                }, new MqttCallbackExtended() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "Connection to MQTT server was lost due to the following cause");
                        Log.e(TAG, "CAUSE: " + cause);
                        runOnUiThread(()->{
                            connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                        });
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.i(TAG, "A new message was received on topic '" + topic + "'");
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.i(TAG, "A new message was sent to the MQTT server");
                    }

                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        Log.i(TAG, "Connection to the MQTT server at '" + serverURI + "' completed successfully.");
                    }
                });
            } catch (MqttException e) {
                Log.i(TAG, "An error occurred");
                Log.i(TAG, e.getMessage());
                runOnUiThread(()->{
                    connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                });
            }
        });

        disconnectBtn.setOnClickListener(view ->{
            try {
                mqttClient.disconnect(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Disconnection from the MQTT server was successful");
                        resetGUI();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Failed to disconnect from the MQTT server");
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "Failed to disconnect from the MQTT server");
                Log.e(TAG, e.getMessage());
            }
        });

        subscribeBtn.setOnClickListener(view -> {
            try {
                mqttClient.subscribe(Constants.MQTT_SUBSCRIPTION_TOPIC, Constants.MQTT_QOS, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Subscription to the topic was successful");
                        runOnUiThread(()->{
                            subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.success));
                            subscribeStatusTxtview.setText("Success");
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Subscription to the topic failed");
                        runOnUiThread(()->{
                            subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                            subscribeStatusTxtview.setText("Failed");
                        });
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "Subscription to the topic failed");
                Log.e(TAG, e.getMessage());
                runOnUiThread(()->{
                    subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                    subscribeStatusTxtview.setText("Failed");
                });
            }
        });

        publishBtn.setOnClickListener(view -> {
            try {
                mqttClient.publish(Constants.MQTT_PUBLISHED_TOPIC, Constants.MQTT_PUBLISHED_MSG, Constants.MQTT_QOS, Constants.MQTT_RETAINED, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Published message to the topic successfully");
                        runOnUiThread(()->{
                            publishStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.success));
                            publishStatusTxtview.setText("Success");
                        });
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Publishing message to the topic failed");
                        runOnUiThread(()->{
                            publishStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                            publishStatusTxtview.setText("Failed");
                        });
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "Publishing message to the topic failed");
                Log.e(TAG, e.getMessage());
                runOnUiThread(()->{
                    publishStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                    publishStatusTxtview.setText("Failed");
                });
            }
        });
    }

    public void resetGUI(){
        runOnUiThread(()->{
            connectBtn.setEnabled(true);
            connectBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.enabled));
            disconnectBtn.setEnabled(false);
            disconnectBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            subscribeBtn.setEnabled(false);
            subscribeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            publishBtn.setEnabled(false);
            publishBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            mqtt2rosTxtview.setText(R.string.publish_txtview_text);
            ros2mqttTxtview.setText(R.string.subscribe_txtview_text);
            subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            subscribeStatusTxtview.setText("");
            publishStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            publishStatusTxtview.setText("");
        });
    }

    public void enableMessageExchangeButtons(){
        runOnUiThread(()->{
            connectBtn.setEnabled(false);
            connectBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.grey));
            disconnectBtn.setEnabled(true);
            disconnectBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.enabled));
            subscribeBtn.setEnabled(true);
            subscribeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.subscribe_enabled));
            publishBtn.setEnabled(true);
            publishBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.publish_enabled));
        });
    }
}