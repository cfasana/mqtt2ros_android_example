# mqtt2ros_android_example
This repository provides an example of how to establish a communication between an MQTT-based device and a ROS2-based device.
The example was created using the following setup:
  * MQTT-based agent:
    * Android device
    * [`Eclipse Paho Android Service`](https://github.com/eclipse/paho.mqtt.android) is chosen to use MQTT and added to Gradle dependencies in the application
  * ROS-based agent:
    * Laptop running Ubuntu 22.04 through WSL2 on a Windows device
    * [`ROS2 Humble`](https://docs.ros.org/en/rolling/Releases/Release-Humble-Hawksbill.html) is chosen as ROS distribution
    * [`mqtt_client`](https://wiki.ros.org/mqtt_client) ROS package is used to map MQTT topics to ROS ones and viceversa

In the following, a description of how to configure the environment and run the example is provided. The steps can be slightly adapted to run the example with a different setup (_e.g.,_ using another ROS distribution).

**N.B.** If you plan to use `Android SDK >= 31`, have a look at the final section of this documentation about `Using Android SDK >= 31`.

## Disclaimer
This repository is intended for educational and demonstrative purposes only. It is not designed or tested for use in critical or production environments. The code and concepts presented here should be used with caution, and the author is not liable for any consequences that may arise from improper or unintended use.
**Do not** rely on this repository for critical or sensitive applications including but not limited to:
- Medical devices
- Industrial control systems

## Environment configuration
### Configuration of the ROS-based device
1. Install ROS2 on your device.
   * The installation steps can vary depending on the device and ROS2 distribution that are being used. Please, refer to this [link](https://docs.ros.org/en/rolling/Releases.html) for the available distributions and follow the installation steps.
    For this example, `WSL2 - Ubuntu 22.04` is started and `ROS2Humble` is installed in the environment following the installation instructions provided [here](https://docs.ros.org/en/humble/Installation/Ubuntu-Install-Debians.html).
   
   * Set up the ROS2 environment by running:
     
     ```
     source /opt/ros/$ROS_DISTRO/setup.bash.
     ```
     To avoid repeating this step this every time you open a new terminal, modify your `~/.bashrc` file by adding the previous command
     Then, either close the terminal and open it again, or run:
     ```
     source ~/.bashrc
     ```
     In the first command, `$ROS_DISTRO` refers to the installed ROS2 distribution. In this example, `$ROS_DISTRO=humble`.
 
2. Install an MQTT broker to allow exchanging MQTT messages.
    * In this case, [`Eclipse Mosquitto`](https://mosquitto.org/) is used, and it can be downloaded from [here](https://mosquitto.org/download/). \
    Once installed, you may need to configure the firewall to allow incoming connections to the MQTT broker.
    For this example, `mosquitto` is installed on Windows 11.
    * To allow connections to `mosquitto` without the need of providing credentials, it is necessary to modify the configuration file of the MQTT broker.
      To do this, proceed as follows:
        * Navigate to `Mosquitto` installation folder (default is `C:\Program Files\mosquitto`)
        * Create a new configuration (`.conf`) file (_e.g._ my_config_file.conf)
        * Insert the following lines in the file and save it:
          
          ```
          listener 1883
          allow_anonymous true
          ```
          
      This will be used later on when starting the MQTT broker. The specified parameters are used to define the port on which mosquitto will wait for connections and the fact that anyone can connect to `Mosquitto` without the need of an authentication mechanism (**N.B.** this is not a good option in real scenarios).
    * Open a terminal and try to run the following commands to check that everything works:
      
      ```
      cd %MOSQUITTO_FOLDER%
      mosquitto -v -c %CONFIG_FILE%
      ```
      
      Here, `%MOSQUITTO_FOLDER%` is `Mosquitto` installation folder, while `%CONFIG_FILE%` is the path to the previously created configuration file.
      If everything works, the output should be something like this:
      
      ![mosquitto_running](https://github.com/cfasana/mqtt2ros_android_example/assets/143723410/5aceaa5d-68ba-4b47-8c73-edce59968192)
 
  
      
3. Install the `mqtt_client` ROS package needed to enable connected ROS-based devices to exchange ROS messages via an MQTT broker using the MQTT protocol, and exchange primitive messages with MQTT clients running on devices not based on ROS.
   * Run the following commands:
     
     ```
     sudo apt update
     sudo apt install ros-$ROS_DISTRO-mqtt-client -y
     ```
     
   Once installed, you can follow [this tutorial](https://wiki.ros.org/mqtt_client) to check that you can exchange MQTT messages successfully.
   **N.B.** If you are running on WSL2 and Mosquitto is installed on Windows, you need to modify the `mqtt_client` configuration `.yaml`, by adding the IP of your Windows machine, as done later in the instructions.

### Configuration of the MQTT-based device
1. Install [Android Studio](https://developer.android.com/studio) on your device.
   **N.B.** If you want to install Android Studio on WSL2, you can follow [this tutorial](https://www.youtube.com/watch?v=XJ0dI2SYHIE).
2. Open a terminal and clone this repository:
   
   ```
   git clone https://github.com/cfasana/mqtt2ros_android_example
   cd mqtt2ros_android_example
   ```
   
4. Run `Android Studio` and open this project
5. Connect the Android device to your laptop and check that it is detected by Android Studio
6. Modify the `Constants.java` class based on your setup:
   
   ```
   public static final String MQTT_BROKER_URI = "tcp://<HOST_IP>:1883";
   public static final String MQTT_USERNAME = "";
   public static final String MQTT_PSW = "";
   public static final int MQTT_QOS = 2;
   public static final boolean MQTT_RETAINED = false;
   public static final String MQTT_CLIENT_ID = "";
   public static final String MQTT_SUBSCRIPTION_TOPIC = "<SUBSCRIBE_TOPIC>";
   public static final String MQTT_PUBLISHED_TOPIC = "<PUBLISH_TOPIC>";
   public static final String MQTT_PUBLISHED_MSG = "This topic is sent from MQTT.";
   ```
   
   The meaning of these parameters is the following:
    * `MQTT_BROKER_URI`: this is the URI of the MQTT broker. It is needed to let the application connect to it in order to exchange MQTT messages with other devices. Substitute `<HOST_IP>` with the IP of the machine on which `Mosquitto` was installed.
    * `MQTT_USERNAME`: this is the username that is used for client authentication. If `allow_anonymous true` is present in the configuration file, it can be left empty.
    * `MQTT_PSW`: this is the password that is used for client authentication. If `allow_anonymous true` is present in the configuration file, it can be left empty.
      **N.B.** it should never be hardcoded, but placed in a secure place.
    * `MQTT_QOS`: this is the MQQT required Quality of Service (QoS). See more [here](https://mosquitto.org/man/mqtt-7.html).
    * `MQTT_RETAINED`: this specifyes whether messages should be retained by the broker. See more [here](https://mosquitto.org/man/mqtt-7.html).
    * `MQTT_CLIENT_ID`: the client id can be used to keep track of a specific connection to the broker.
    * `MQTT_SUBSCRIPTION_TOPIC`: this is the name of the MQTT topic to which the application should subscribe.
    * `MQTT_PUBLISHED_TOPIC`: this is the name of the MQTT topic on which the application will publish messages.
    * `MQTT_PUBLISHED_MSG`: this is the message that will be published by the application on `MQTT_PUBLISHED_TOPIC`.

## Application and MQTT testing
1) Start `Mosquitto` on the machine on which it was installed:
      
      ```
      cd %MOSQUITTO_FOLDER%
      mosquitto -v -c %CONFIG_FILE%
      ```
      
2) In the `Constants.java` file, Put the same string (_e.g.,_ mqtt_topic) in place of `<SUBSCRIBE_TOPIC>` and `<PUBLISH_TOPIC>`, and by updating the other constants (_e.g.,_ substitute `<HOST_IP>` with the IP of the machine on which Mosquitto is running.
3) Run the application
   * Click the `connect` button to connect to `Mosquitto`. A green light is shown if the connection succeeds
   * Click the `subscribe` button to subscribe to the MQTT topic
   * Click the `publish` button to publish a message on the topic. If everything works, you should see that the message is received and printed on the screen
     
     https://github.com/cfasana/mqtt2ros_android_example/assets/143723410/bd1fa96c-f64d-4e2c-bf65-a9da798438e4

## MQTT2ROS Application Testing
1) Start `Mosquitto` on the machine on which it was installed:
      
      ```
      cd %MOSQUITTO_FOLDER%
      mosquitto -v -c %CONFIG_FILE%
      ```
      
2) Create a new ROS workspace and a directory to contain the `mqtt_client` configuration `.yaml` file:

   ```
   cd ~ && mkdir mqtt2ros_ws
   cd mqtt2ros_ws && mkdir mqtt_client_config
   cd mqtt_client_config
   ```
3) Create the new `.yaml` file and insert the following text into it:
   ```
   mqtt_client:
   ros__parameters:
     broker:
       host: <HOST_IP> # Replace with the IP address of the device on which Mosquitto is running
       port: 1883 # Replace with the port on which Mosquitto is waiting for connections
     bridge:
       ros2mqtt:
         ros_topics:
           - /ros/ros2mqtt_msg # This is a topic on which messages will be published by a ROS-based device
         /ros/ros2mqtt_msg:
           mqtt_topic: mqtt/ros2mqtt_msg # The mqtt_client maps the above ROS topic to this MQTT topic to which an MQTT-based device will subscribe
           primitive: true
       mqtt2ros:
         mqtt_topics:
           - mqtt/mqtt2ros_msg # This is a topic on which messages will be published by an MQTT-based device
         mqtt/mqtt2ros_msg:
           ros_topic: /ros/mqtt2ros_msg # The mqtt_client maps the above MQTT topic to this ROS topic to which a ROS-based device will subscribe
           primitive: true
   ```
   Replace `<HOST_IP>` with the IP of the machine on which you will run the MQTT broker.
   This files is used to tell `mqtt_client` how to reach the broker and how to map ROS topics to MQTT topics and viceversa.
   In this case, the messages received on the ROS topic `/ros/ros2mqtt_msg` are published on the MQTT topic `mqtt/ros2mqtt_msg`.
   Viceversa, the messages received on the MQTT topic `mqtt/mqtt2ros_msg` are published on the ROS topic `/ros/mqtt2ros_msg`.
4) Start the `mqtt_client`:
   ```
   ros2 launch mqtt_client standalone.launch.ros2.xml params_file:=<PATH_TO_YAML_FILE>
   ```
   In this case, `PATH_TO_YAML_FILE` was set to `mqtt_client_config/mqtt_client_config.yaml`
5) Create a bash file to repeatedly publish ROS messaged:
   ```
   cd ..
   nano ros_publisher.sh
   ```
   
   Insert the following text in the file and save it:
   
   ```
   #!/bin/bash

   topic_name="/ros/ros2mqtt_msg"
   message_number=1
   
   while true; do
     message="$message_number) This message comes from a ROS-based device"
     ros2 topic pub $topic_name std_msgs/msg/String "{data: \"$message\"}" --once
     ((message_number++))
     sleep 1
   done
   ```
   
   Make the file executable:
   
   ```
   chmod +x ros_publisher.sh
   ```

   This script allows to execution the command `ros2 topic pub $topic_name std_msgs/msg/String "{data: \"$message\"}" --once` every second. This command publishes one ROS message on the given topic and then exits.
   Note that `topic_name` is set to the same value used in the `mqtt_client` configuration file.
   **N.B.** Instead of using this script, you could as well write a ROS node to publish the data.
7) Open the Android application in Android studio
8) In the `Constants.java` file, set the `HOST_IP` value, and set `<SUBSCRIBE_TOPIC>="mqtt/ros2mqtt_msg"` and `<PUBLISH_TOPIC>="mqtt/mqtt2ros_msg"`.
   **N.B.** The name of the topics are the same used for the MQTT topics in the `mqtt_client` configuration file.
9) Run the application
   * Click the `connect` button to connect to `Mosquitto`. A green light is shown if the connection succeeds
   * Click the `subscribe` button to subscribe to the MQTT topic
   * Click the `publish` button to publish a message on the topic.
10) In two different terminals, run the following commands to start a ROS publisher and a ROS subscriber:
    ```
    ./ros_publisher.sh
    ```
    ```
    ros2 topic echo /ros/mqtt2ros_msg
    ```
    **N.B.** Also in this last case, the topic name is set to the same value as that specified in the `mqtt_client` configuration file.

    If everything works, the following should happen:
     * Every time the ROS publisher publishes a message, the Android application should receive it and print it in the textview below the subscribe button.
     * Every time you click the publish button on the Android application, the message should be received by the ROS-based device and written in the terminal.

    Have a look at the video below to see how everything works:
    https://github.com/cfasana/mqtt2ros_android_example/assets/143723410/4021b027-ff90-415e-91dc-0caeb686c027

## Using Android SDK >= 31
If you would like to use `Android SDK >= 31`, you will need to deal with the issue highlighted [here](https://github.com/eclipse/paho.mqtt.android/issues/485#issue-1384943553).
As explained in the referenced issue, to solve this problem you can build the MQTT libraries from source.

Just copy the files that you find in the `libs` folder of this repository inside `MQTT2ROS/app/libs`. Then, from Android Studio, navigate to the folder, right click on each library and select `Add as library`.
Finally, edit the app `.gradle` file as follows:
 1) Remove the following lines:
    ```
    implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
    implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
    ```
 3) Add the following line:
    ```
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    ```

   
