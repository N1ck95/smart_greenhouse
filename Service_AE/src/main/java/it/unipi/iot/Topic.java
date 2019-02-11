package it.unipi.iot;

public class Topic {
	public String topic;	//content type
	public int cf;	//content format
	
	public Topic(String topic, int cf) {
		this.topic = topic;
		this.cf = cf;
	}
}
