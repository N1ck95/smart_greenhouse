package it.unipi.iot;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public abstract class SensorCoapHandler implements CoapHandler{
	public String topic;
	
	public SensorCoapHandler(String topic) {
		this.topic = topic;
	}
}
