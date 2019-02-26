package it.unipi.it;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		this.setObservable(true);
		
		//loadAssociation();
		
		//CREATE TOPICS FOR TESTING
		/*this.add(new TopicResource("Service_AE/Sector1/sensor/humidity/AY5CS34", MediaTypeRegistry.APPLICATION_JSON));
		this.add(new TopicResource("Service_AE/Sector3/actuator/irrigator/TJ8XG95", MediaTypeRegistry.TEXT_PLAIN));
		TopicResource Pippo = new TopicResource("pippo", MediaTypeRegistry.APPLICATION_JSON)
		this.add(Pippo);*/
		//this.add(new CoapResource("Service_AE").add(new CoapResource("Sector1").add(new CoapResource("sensor").add(new CoapResource("humidity").add(new TopicResource("AY5CS34", MediaTypeRegistry.TEXT_PLAIN))))));
		//this.add(new CoapResource("Service_AE").add(new CoapResource("Sector2").add(new CoapResource("actuator").add(new CoapResource("irrigator").add(new TopicResource("TJ8XG95", MediaTypeRegistry.TEXT_PLAIN))))));
		//this.getChild("Service_AE").add(new CoapResource("Sector1").add(new CoapResource("sensor").add(new CoapResource("humidity").add(new TopicResource("AY5CS34", MediaTypeRegistry.TEXT_PLAIN)))));
		
		
		//this.add(new TopicResource("AY5CS34", MediaTypeRegistry.TEXT_PLAIN));
		//this.add(new TopicResource("temp_sens1", MediaTypeRegistry.TEXT_PLAIN));
	}
	
	private void loadAssociation() {
		association = new ArrayList<MACSector>();
		
		//URL filePath = RootResource.class.getResource("association.txt");
		//File file = new File(filePath.getFile());
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream fileInput = classLoader.getResourceAsStream("association.txt");
		
		if(fileInput == null) {
			System.out.println("[WARN] Association file not found");
			return;
		}
		
		try {
			//BufferedReader br = new BufferedReader(new FileReader(file));
			InputStreamReader sr = new InputStreamReader(fileInput);
			BufferedReader br = new BufferedReader(sr);
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
		
		System.out.println("[DEBG] Received POST request from " + exchange.getSourceAddress().getHostAddress() + " payload: " + request);
		
		boolean exist = false;
		
		Iterator it = this.getChildren().iterator();
		while(it.hasNext()) {
			Resource child = (Resource) it.next();
			if(child.getName().equals(topic)) {
				exist = true;
				break;
			}
		}
		
		if(exist == true) {
			exchange.respond(ResponseCode.FORBIDDEN);
		}else {
			this.add(new TopicResource(topic, ct));
			exchange.respond(ResponseCode.CREATED);
			this.notifyObserverRelations(null);
		}
		
		/*if(path.length == 1) {
			//topic to configure a specific device
			String MAC = path[0];
			this.add(new TopicResource(MAC, MediaTypeRegistry.TEXT_PLAIN));
			//TODO: add in payload the URI of created resource
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
			boolean created = false;
			Resource res = this;
			Resource parent = this;
			System.out.println("[DEBUG] requested topic: " + topic);
			for(int i = 0; i < path.length; i++) {
				parent = res;
				Iterator<Resource> childrens = res.getChildren().iterator();
				while(childrens.hasNext()) {
					Resource child = childrens.next();
					if(child.getName().equals(path[i])) {
						res = child;
						System.out.println("[DEBUG] subpath exists: " + child.getName());
					}
				}
				
				if(res == parent) {
					//resource does not exists
					System.out.println("[DEBUG] subpath not exist: " + path[i]);
					Resource tmp;
					if(i == (path.length - 1)) {
						//The topic to create is the MAC
						tmp = new TopicResource(path[i], ct);
						created = true;
					}else {
						//The resource to create is an intermediate
						tmp = new CoapResource(path[i]);
					}
					res.add(tmp);
					res = tmp;	//Update actual exploring resource
					this.notifyObserverRelations(null);
				}
			}
			
			if(created) {
				exchange.respond(ResponseCode.CREATED);
			}else {
				exchange.respond(ResponseCode.BAD_REQUEST);	//TODO: check
			}
		}*/
		
	}
	
	public void handleGET(CoapExchange exchange) {
		System.out.println("GET REQUEST");
		JSONObject jsonPayload = new JSONObject();
		JSONArray topics = new JSONArray();
		Iterator<Resource> it = this.getChildren().iterator();
		while(it.hasNext()) {
			Resource res = it.next();
			
			JSONObject topicObj = new JSONObject();
			topicObj.put("topic", res.getName());
			topicObj.put("cf", ((TopicResource) res).contentType);
			topics.put(topicObj);
			/*
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
								TopicResource MAC = (TopicResource) it_MAC.next();
								String topic = res.getName() + "/" + sector.getName() + "/" + type.getName() + "/" + model.getName() + "/" + MAC.getName();
								JSONObject topicObj = new JSONObject();
								topicObj.put("topic", topic);
								topicObj.put("cf", MAC.contentType);
								topics.put(topicObj);
							}
						}
					}
				}
			}
			*/
			
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
