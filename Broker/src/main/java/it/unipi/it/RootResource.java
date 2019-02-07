package it.unipi.it;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

public class RootResource extends CoapResource{
	private ArrayList<MACSector> association;
	
	public RootResource(String name) {
		super(name);
		
		loadAssociation();
		
		//CREATE TOPICS FOR TESTING
		/*this.add(new TopicResource("Service_AE/Sector1/sensor/humidity/AY5CS34", MediaTypeRegistry.APPLICATION_JSON));
		this.add(new TopicResource("Service_AE/Sector3/actuator/irrigator/TJ8XG95", MediaTypeRegistry.TEXT_PLAIN));
		TopicResource Pippo = new TopicResource("pippo", MediaTypeRegistry.APPLICATION_JSON)
		this.add(Pippo);*/
		this.add(new CoapResource("Service_AE").add(new CoapResource("Sector1").add(new CoapResource("sensor").add(new CoapResource("humidity").add(new TopicResource("AY5CS34", MediaTypeRegistry.TEXT_PLAIN))))));
		this.add(new CoapResource("Service_AE").add(new CoapResource("Sector2").add(new CoapResource("actuator").add(new CoapResource("irrigator").add(new TopicResource("TJ8XG95", MediaTypeRegistry.TEXT_PLAIN))))));
	}
	
	private void loadAssociation() {
		association = new ArrayList<MACSector>();
		
		URL filePath = RootResource.class.getResource("association.txt");
		File file = new File(filePath.getFile());
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			while((line = br.readLine()) != null) {
				String[] el = line.split(" ");	//Each line is formatted as: "<MAC> <sector>"
				association.add(new MACSector(el[0], el[1]));
			}
			
			System.out.println("[INFO] association list loaded. " + association.size() + " elements.");
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] Unable to locate association file. Error: " + e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("[ERROR] Fail reading association file. Error: " + e);
		}
	}
	
	private String getSector(String MAC) {
		for(int i = 0; i < association.size(); i++) {
			if(association.get(i).MAC.equals(MAC)) {
				return association.get(i).sector;
			}
		}
		return null;
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
		
		if(path.length == 1) {
			//topic to configure a specific device
			String MAC = path[1];
			this.add(new TopicResource(MAC, MediaTypeRegistry.TEXT_PLAIN));
			exchange.respond(ResponseCode.CREATED);
			
			//Check if this MAC is associated to a sector
			String sector = getSector(MAC);
			if(sector != null) {
				//MAC associated to a sector
				TopicResource res = (TopicResource) this.getChild(MAC);
				res.value = sector;
				
				res.notifyObservers();	//Notify subscribers of this 
			}else {
				System.out.println("[DEBUG] MAC not associated to a sector: " + MAC);
			}
		}else {
			//topic to receive/send notifications from/to device

			Resource res = this;
			for(int i = 0; i < path.length; i++) {
				Iterator<Resource> childrens = res.getChildren().iterator();
				while(childrens.hasNext()) {
					Resource child = childrens.next();
					if(child.getName().equals(path[i])) {
						res = child;
						break;
					}
				}
				if(res == this) {
					//resource does not exists
					Resource tmp;
					if(i == (path.length - 1)) {
						//The topic to create is the MAC
						tmp = new TopicResource(path[i], ct);
					}else {
						//The resource to create is an intermediate
						tmp = new CoapResource(path[i]);
					}
					res.add(tmp);
					res = tmp;	//Update actual exploring resource
					this.notifyObserverRelations(null);
				}
			}
			
		}
		/*if(!topicExists(topic)) {
			//Topic still not exists
			this.add(new TopicResource(path[0], Integer.parseInt(ct)));
			exchange.respond(ResponseCode.CREATED, this.getName() + "/" + topic);
			this.notifyObserverRelations(null);
		}else {
			//Topic already exists
			exchange.respond(ResponseCode.FORBIDDEN);
		}*/
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
	
	@Deprecated
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
