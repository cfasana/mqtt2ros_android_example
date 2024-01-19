package com.h_fluently.mqtt2ros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.eclipse.paho.client.mqttv3.*;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
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
    private boolean subscribed = false;
    private int msg_number = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBtn = findViewById(R.id.connect_btn);
        disconnectBtn = findViewById(R.id.disconnect_btn);
        subscribeBtn = findViewById(R.id.subscribe_btn);
        publishBtn = findViewById(R.id.publish_btn);
        connectionStatusBtn = findViewById(R.id.connection_status_btn);;

        ros2mqttTxtview = findViewById(R.id.subscribe_txtview);
        ros2mqttTxtview.setMovementMethod(new ScrollingMovementMethod());
        mqtt2rosTxtview = findViewById(R.id.publish_txtview);
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
                        Log.e(TAG, Log.getStackTraceString(exception));
                        runOnUiThread(()->{
                            connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.error));
                        });
                    }
                }, new MqttCallbackExtended() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        if (cause==null){
                            Log.w(TAG, "Connection to MQTT server was lost due to an explicit request to disconnect.");
                        }
                        else {
                            Log.e(TAG, "Connection to MQTT server was lost due to the following cause");
                            Log.e(TAG, "CAUSE: " + cause);
                            runOnUiThread(() -> {
                                connectionStatusBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                            });
                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.i(TAG, "A new message was received on topic '" + topic + "'");
                        runOnUiThread(() -> {
                            String newMsg = new String(message.getPayload());
                            String text =  newMsg + "\n" + ros2mqttTxtview.getText().toString();
                            SpannableString spannableString = new SpannableString(text);
                            ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                            spannableString.setSpan(redSpan, 0, newMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ForegroundColorSpan blackSpan = new ForegroundColorSpan(Color.BLACK);
                            spannableString.setSpan(blackSpan, newMsg.length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ros2mqttTxtview.setText(spannableString);
                        });
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.i(TAG, "A new message was sent to the MQTT server");
                        runOnUiThread(() -> {
                            mqtt2rosTxtview.setText("Published new message (" + msg_number + ")");
                            msg_number+=1;
                        });
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
            if (!subscribed) {
                try {
                    mqttClient.subscribe(Constants.MQTT_SUBSCRIPTION_TOPIC, Constants.MQTT_QOS, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(TAG, "Subscription to the topic was successful");
                            subscribed = true;
                            runOnUiThread(() -> {
                                subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.success));
                                subscribeStatusTxtview.setText("Success");
                                subscribeBtn.setText(getResources().getString(R.string.unsubscribe));
                                ros2mqttTxtview.setText("");
                            });
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.e(TAG, "Subscription to the topic failed");
                            runOnUiThread(() -> {
                                subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                subscribeStatusTxtview.setText("Failed");
                            });
                        }
                    });

                } catch (MqttException e) {
                    Log.e(TAG, "Subscription to the topic failed");
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(() -> {
                        subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        subscribeStatusTxtview.setText("Failed");
                    });
                }
            }
            else{
                try {
                    mqttClient.unsubscribe(Constants.MQTT_SUBSCRIPTION_TOPIC, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(TAG, "Unsubscribed from topic was successful");
                            subscribed = false;
                            runOnUiThread(() -> {
                                subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                                subscribeStatusTxtview.setText("");
                                ros2mqttTxtview.setText(getResources().getString(R.string.subscribe_txtview_text));
                                subscribeBtn.setText(getResources().getString(R.string.subscribe_to_mqtt_topic));
                            });
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.e(TAG, "Unsubscribed from topic failed");
                            runOnUiThread(() -> {
                                subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                                subscribeStatusTxtview.setText("Failed");
                            });
                        }
                    });
                } catch (MqttException e) {
                    Log.e(TAG, "Unsubscribed from topic failed");
                    Log.e(TAG, e.getMessage());
                    runOnUiThread(() -> {
                        subscribeStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.error));
                        subscribeStatusTxtview.setText("Failed");
                    });
                }
            }

        });

        publishBtn.setOnClickListener(view -> {
            try {
                mqttClient.publish(Constants.MQTT_PUBLISHED_TOPIC, "(" + msg_number + ")" + Constants.MQTT_PUBLISHED_MSG, Constants.MQTT_QOS, Constants.MQTT_RETAINED, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Published message to the topic successfully");
                        runOnUiThread(()->{
                            publishStatusTxtview.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.success));
                            publishStatusTxtview.setText("Success");
                            mqtt2rosTxtview.setText("");
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