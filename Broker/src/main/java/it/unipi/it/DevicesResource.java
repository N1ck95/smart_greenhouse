package it.unipi.it;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class DevicesResource extends CoapResource{
	private ArrayList<Subscriber> subscribers;	//Array list containing all subscribers to this topic
	private ArrayList<String> topics;	//Array list containing all topics names
	
	public DevicesResource(String name) {
		super(name);
		subscribers = new ArrayList<Subscriber>();
		topics = new ArrayList<String>();
	}
	
	public void handleGET(CoapExchange exchange) {
		OptionSet options = exchange.getRequestOptions();
		Integer observe = options.getObserve();
		if(observe != null) {
			if(observe == 0) {
				//subscribe request --> Add host to list of subscribers
				String addr = exchange.getSourceAddress().getHostAddress();
				Subscriber sub = new Subscriber(addr, observe);
				subscribers.add(sub);
				if(topics.size() > 0) {
					
				}else {
					
					//reply = new Response(ResponseCode.)
				}
			}
		}
	}
	
	public void notifySubs() {
		for(Subscriber subscriber : subscribers) {
			notifySub(subscriber);
		}
	}
	
	private void notifySub(Subscriber sub) {
		CoapClient client = new CoapClient(sub.getAddress());
		Request request = new Request(Code.POST);	//TODO: check
		request.getOptions().addUriQuery("Observe="+sub.getObserve());	//TODO: check
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		
		//request.setPayload(payload);
		CoapResponse response = client.advanced(request);
	}
	
	public void handelPOST(CoapExchange exchange) {
		
	}
}
