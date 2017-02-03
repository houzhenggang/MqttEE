package rs.mqttserver.client;



import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import rs.mqttserver.client.Subscribe;

public class Subscribers {

	final static Logger log = Logger.getLogger(Subscribers.class);
	
	/*
	Example how to use: 
	
	@Subscribe(topic="test/+/+",qos=1)
	public void t4(String topic, MqttMessage msg) throws InterruptedException{
		log.info("Enter: test/+/+ - "+topic);
		log.info("Leave: test/+/+ - "+topic);
	}
	
	@Subscribe(topic="test/+/+/+/test",qos=1)
	public void t5(String topic, MqttMessage msg) throws InterruptedException{
		log.info("Enter: test/+/+/+/test - "+topic);
		log.info("Leave: test/+/+/+/test - "+topic);
	}
	
	
	@Subscribe(topic="test/test1/test3",qos=1)
	public void t7(String topic, MqttMessage msg) throws InterruptedException{
		log.info("Enter: test/test1 - "+topic);
		log.info("Leave: test/test1 - "+topic);
	}*/

}
