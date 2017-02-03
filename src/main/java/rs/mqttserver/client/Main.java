package rs.mqttserver.client;


import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class Main {

	final static Logger log = Logger.getLogger(Main.class);

	private static void init() {
		String log4jConfigFile = System.getProperty("user.dir")
				+ File.separator + "log4j.properties";
		PropertyConfigurator.configure(log4jConfigFile);
		log.info("Logger initialized...");
	}

	public static void main(String[] args) throws Exception {
		init();
		mqttReceive();
	}

	public static void mqttReceive() {
		try {
			MqttAsyncClient pcli = MqttClientFactory
					.getConnectedMqttAsyncClient();
		
		} catch (MqttException e) {
			log.error(e.getMessage(), e);
		} 

	}



}
