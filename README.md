# mqtt2ros_android_example
This repository provides an example of how to let an Android device communicate with a ROS2-based device using MQTT protocol.
The Android device can only send and receive MQTT messages, while the other device sends and receives only ROS messages.

## Setup
### Configuration of the ROS-based device
1. Install ROS2 on your device.
   * The installation steps can vary depending on the device and ROS2 distribution that are being used. Please, refer to this [link](https://docs.ros.org/en/rolling/Releases.html) for the available distributions and follow the installation steps.
    For this example, [`ROS2Humble`](https://docs.ros.org/en/rolling/Releases/Release-Humble-Hawksbill.html) is used and installed on a device running Ubuntu 22.04 through WSL2.
    The installation instructions can be found [here](https://docs.ros.org/en/humble/Installation/Ubuntu-Install-Debians.html).
   
   * Set up the ROS2 environment by running: source /opt/ros/humble/setup.bash.
    To avoid doing this every time you open a new terminal, modify your `~/.bashrc` file by adding the following line:

     ```
     source /opt/ros/humble/setup.bash
     ```
     Then, either close the terminal and open it again, or run:
     ```
     source ~/.bashrc
     ```

2. Install [`mqtt_client`](https://wiki.ros.org/mqtt_client) ROS package needed to enable connected ROS-based devices to exchange ROS messages via an MQTT broker using the MQTT protocol, and exchange primitive messages with MQTT clients running on devices not based on ROS.
   * Run the following commands:
     
     ```
     sudo apt update
     sudo apt install ros-$ROS_DISTRO-mqtt-client -y
     ```
     Here, `$ROS_DISTRO` refers to the ROS2 distribution installed in the first step. In this example, `$ROS_DISTRO=humble`.
  
3. Install an MQTT broker.
    * In this case, [`Eclipse Mosquitto`](https://mosquitto.org/) is used, and it can be downloaded from [here](https://mosquitto.org/download/). \
    Once installed, you may need to configure the firewall to allow incoming connections to the MQTT broker if you do not intend to use `localhost`.
    For this example, the following commands were used to install `Eclipse Mosquitto`:

      ```
      sudo apt-get update
      sudo apt-get install mosquitto -y
      ```
