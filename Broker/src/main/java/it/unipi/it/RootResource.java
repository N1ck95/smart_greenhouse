package it.unipi.it;

import java.util.Iterator;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

public class RootResource extends CoapResource{
	public RootResource(String name) {
		super(name);
		
		//CREATE TOPICS FOR TESTING
		this.add(new TopicResource("Service_AE/Sector1/sensor/humidity/AY5CS34", MediaTypeRegistry.APPLICATION_JSON));
		this.add(new TopicResource("Service_AE/Sector3/actuator/irrigator/TJ8XG95", MediaTypeRegistry.TEXT_PLAIN));
	}
	
	public void test() {
		this.getParent().add(new RootResource("test"));
		this.add(new RootResource("test2"));
	}
	
	public void handlePOST(CoapExchange exchange) {
		//Create request have a payload forletemat: "<topic>;ct=content_format" 
		//where topic is the name of the topic to create
		String request = exchange.getRequestText();
		String[] payload = request.split(";");
		String topic = payload[0];
		topic = topic.replace("<", "");
		topic = topic.replace(">", "");
		String ct = payload[1];
		ct = ct.replace("ct=", "");	//isolate the content type code
		if(!topicExists(topic)) {
			//Topic still not exists
			this.add(new TopicResource(topic, Integer.parseInt(ct)));
			exchange.respond(ResponseCode.CREATED, this.getName() + "/" + topic);
		}else {
			//Topic already exists
			exchange.respond(ResponseCode.FORBIDDEN);
		}
	}
	
	private boolean topicExists(String topic) {
		Iterator<Resource> i = this.getChildren().iterator();
		while(i.hasNext()) {
			if(i.next().getName().equals(topic)) {
				return true;
			}
		}
		return false;
	}
}
