package it.unipi.iot;

import org.eclipse.californium.core.CoapHandler;

public abstract class SensorCoapHandler implements CoapHandler{
	public String topic;
	public int cf;	//content format
	
	public SensorCoapHandler(String topic, int cf) {
		this.topic = topic;
		this.cf = cf;
	}
}
