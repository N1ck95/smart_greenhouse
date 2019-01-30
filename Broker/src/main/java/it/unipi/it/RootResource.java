package it.unipi.it;

import java.util.Iterator;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

public class RootResource extends CoapResource{
	public RootResource(String name) {
		super(name);
		
		//CREATE TOPICS FOR TESTING
		/*this.add(new TopicResource("Service_AE/Sector1/sensor/humidity/AY5CS34", MediaTypeRegistry.APPLICATION_JSON));
		this.add(new TopicResource("Service_AE/Sector3/actuator/irrigator/TJ8XG95", MediaTypeRegistry.TEXT_PLAIN));
		TopicResource Pippo = new TopicResource("pippo", MediaTypeRegistry.APPLICATION_JSON)
		this.add(Pippo);*/
		this.add(new CoapResource("Service_AE").add(new CoapResource("Sector1").add(new CoapResource("sensor").add(new CoapResource("humidity").add(new TopicResource("AY5CS34", MediaTypeRegistry.TEXT_PLAIN))))));
		
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
		String[] path = topic.split("/");
		if(!topicExists(topic)) {
			//Topic still not exists
			this.add(new TopicResource(path[0], Integer.parseInt(ct)));
			exchange.respond(ResponseCode.CREATED, this.getName() + "/" + topic);
			this.notifyObserverRelations(null);
		}else {
			//Topic already exists
			exchange.respond(ResponseCode.FORBIDDEN);
		}
	}
	
	public void handleGET(CoapExchange exchange) {
		System.out.println("GET REQUEST");
		JSONObject jsonPayload = new JSONObject();
		JSONArray topics = new JSONArray();
		Iterator<Resource> it = this.getChildren().iterator();
		while(it.hasNext()) {
			Resource res = it.next();
			if(res.getName().equals("Service_AE")) {
				Iterator<Resource> it_sect = res.getChildren().iterator();
				while(it_sect.hasNext()) {
					Resource sector = it_sect.next();
					Iterator<Resource> it_type = sector.getChildren().iterator();
					while(it_type.hasNext()) {
						Resource type = it_type.next();
						Iterator<Resource> it_model = type.getChildren().iterator();
						while(it_model.hasNext()) {
							Resource model = it_model.next();
							Iterator<Resource> it_MAC = model.getChildren().iterator();
							while(it_MAC.hasNext()) {
								Resource MAC = it_MAC.next();
								String topic = res.getName() + "/" + sector.getName() + "/" + type.getName() + "/" + model.getName() + "/" + MAC.getName();
								topics.put(topic);
							}
						}
					}
				}
			}
		}
		jsonPayload.put("topics", topics);
		exchange.respond(ResponseCode.CONTENT, jsonPayload.toString());
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
