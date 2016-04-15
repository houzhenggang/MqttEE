package rs.mqttserver.client;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import rs.mqttserver.client.LoggingService.LogLevel;

public class MqttClientFactory {

	static String broker;
	static String clientId;
	static MemoryPersistence persistence;
	private static MqttAsyncClient client;

	final static Logger log = Logger.getLogger(MqttClientFactory.class);

	public static synchronized MqttAsyncClient getMqttAsyncClient()
			throws MqttException {
		if (client == null) {
			broker = Prop.getInstance().getValue("mqtt_broker");
			clientId = (Prop.getInstance().getValue("mqtt_clientid") + "_" + MqttClient
					.generateClientId());
			persistence = new MemoryPersistence();

			client = new MqttAsyncClient(broker, clientId, persistence);
		}
		return client;
	}

	public static synchronized MqttAsyncClient getConnectedMqttAsyncClient()
			throws MqttException {
		if (client == null || !client.isConnected()) {
			client = getMqttAsyncClient();
			log.info("Connecting to broker...");

			/*It�s important to note that the callback needs to be set before the client connects to the MQTT broker,
			 *  otherwise you could lose messages, especially after resuming a persistent session.*/
			
			MqttDispatcher.init();
			String[] t=MqttDispatcher.getTopicsArray();
			int[] q = MqttDispatcher.getQosList();
			
			client.setCallback(new MqttDispatcher());
			
			try{
			client.connect(MqttClientFactory.getMqttConnectOptions());
			}catch(MqttException e){
				log.error(e.getMessage(),e);
			}
			

			// AsyncClient!
			while (!client.isConnected()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(),e);
					return client;
				}
				try{
				if(!client.isConnected())client.connect(MqttClientFactory.getMqttConnectOptions());	
				}catch(MqttException e){
					log.error(e.getMessage(),e);
				}
			}
			
			log.info("Connected to broker!");
			client.subscribe(t,q);
			LoggingService.alert(LogLevel.INFO, "Connected!");

		}
		return client;
	}

	public static MqttConnectOptions getMqttConnectOptions() {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		connOpts.setCleanSession(Boolean.parseBoolean(Prop.getInstance()
				.getValue("mqtt_clear_session")));
		connOpts.setWill(Prop.getInstance().getValue("mqtt_lwt_topic"),
				Prop.getInstance().getValue("mqtt_lwt_msg").getBytes(), Integer.parseInt(Prop.getInstance().getValue("mqtt_lwt_qos")), true);
		connOpts.setKeepAliveInterval(Integer.parseInt(Prop.getInstance().getValue("mqtt_keepalive")));
		return connOpts;
	}
}
