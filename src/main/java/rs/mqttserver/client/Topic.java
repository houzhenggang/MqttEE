package rs.mqttserver.client;

public class Topic {
	private String topic;
	private int qos;
	
	public Topic(String topic,int qos) {
		this.topic=topic;
		this.qos=qos;
	}
	
	public int getQos() {
		return qos;
	}
	public String getTopic() {
		return topic;
	}
	public void setQos(int qos) {
		this.qos = qos;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
}
