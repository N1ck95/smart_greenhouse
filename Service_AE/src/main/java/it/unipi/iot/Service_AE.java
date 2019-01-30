package it.unipi.iot;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONArray;
import org.json.JSONObject;

public class Service_AE extends CoapServer{
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "niccolo-mn-cse";		//Middle node id
	final static String middle_name = "niccolo-mn-name";	//Middle node name
	final static int BROKER_PORT = 6001;
	final static String broker_uri = "coap://127.0.0.1:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String AE_ID = "Service_AE_ID";			//Id of the Application Entity
	final static String AE_name = "Service_AE";				//Name of the Application Entity
	final static int PORT = 6000;							//Port of this AE
	
	public static void main(String[] args) throws InterruptedException {
		final BrokerCoAP broker = new BrokerCoAP(broker_uri);
		//Tree devices = new Tree("Service_AE");
		final OneM2M middle_node = new OneM2M(middle_ip, middle_id, middle_name);	//To interact with middle_node
		final Service_AE server = new Service_AE();
		server.addEndpoint(new CoapEndpoint(new InetSocketAddress(PORT)));
		server.start();
		
		final Semaphore topic_sem = new Semaphore(1);
		final ArrayList<String> newTopics = new ArrayList<String>();
		
		//final Semaphore publishSensor_sem = new Semaphore(1);
		final ArrayList<CI> publishSensor = new ArrayList<CI>();
		
		//Create the Application Entity on the middle node
		try {
			middle_node.createAE(AE_ID, AE_name);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		
		//TODO: create the CoapMonitor to receive updates about container changes
		//TODO: create the CoapServer to receive notifications about topic changes
		
		//TODO: perform a subscription to the 'sensors' topic of broker to receive updates about newly available topics
		//try {
		System.out.println("Subscribing to broker");
			CoapClient client = new CoapClient(broker_uri + "/ps");	//Subscribe to have notifications about topics
			@SuppressWarnings("unused")
			CoapObserveRelation relation = client.observeAndWait(
					new CoapHandler() {
						public void onLoad(CoapResponse response) {
							String content = response.getResponseText();
//							System.out.println("OnLoad");
//							System.out.println(response.getCode().toString());
							if(response.getCode() == ResponseCode.CONTENT) {
								//Response have format: </topic1>,</topic2>,...,</well-known/.core>
								JSONObject JSONpayload = new JSONObject(content);
								JSONArray topics = JSONpayload.getJSONArray("topics");
								//String[] topics = content.split(",");
								System.out.println("Response for subscribe");
								
								//Acquire semaphore
								try {
									topic_sem.acquire();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								for(Object topicObj : topics) {
									String topic = topicObj.toString();
									System.out.println(topic);
									
									newTopics.add(topic);	//Is a shared variable
								}
								
								topic_sem.release();	//Release semaphore
							}
						}
				
						public void onError() {
							System.err.println("Response error");
						}
					}	
			);
			
			while(true) {
				topic_sem.acquire();	//Acquire semaphore
				while(newTopics.isEmpty() == false) {
					//The list of topics is not empty --> try to add topics to OneM2M
					
					//Extract and remove the first element from the list (is a shared variable)
					String topic = newTopics.get(0);
					newTopics.remove(0);
					
					String[] path = topic.split("/");
					if(path[0].equals(AE_name)) {
						//Is a topic that I have to link in OneM2M
						String sector = path[1];
						String type = path[2];
						String model = path[3];
						String MAC = path[4];
						
						
						System.out.println("valid topic");
						//Create the containers for this device on OneM2M
						
						try {
							middle_node.createContainer(AE_name, sector);	//create the Sector container
						}catch(Exception e) {
							System.err.println("Error creating sector container: " + e);
						}
						
						try {
							middle_node.createContainer(AE_name + "/" + sector, type);	//Create the type container
						}catch(Exception e) {
							System.err.println("Error creating type container: " + e);
						}
						
						try {
							middle_node.createContainer(AE_name + "/" + sector + "/" + type, model);
						}catch(Exception e) {
							System.err.println("Error creating model container: " + e);
						}
						
						try {
							middle_node.createContainer(AE_name + "/" + sector + "/" + type + "/" + model, MAC);	//create the device container
						}catch(Exception e) {
							System.err.println("Error creating MAC container: " + e);
						}
						
						if(type.equals("sensor")) {
							//devices.addSensor(MAC, sector);	//TODO: check if can be avoided
							//Is a sensor --> I also have to subscribe to this topic
							CoapClient sensorClient = new CoapClient(broker_uri + "/ps/" + topic);
							System.out.println("Observing: " + broker_uri + "/ps/" + topic);
							Request req = new Request(Code.GET);
							req.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
							req.setObserve();
							req.setURI(sensorClient.getURI());
							
							@SuppressWarnings("unused")
							CoapObserveRelation sensorRelation = sensorClient.observe(req,
								new SensorCoapHandler(topic) {
									
									public void onLoad(CoapResponse response) {
										String content = response.getResponseText();
										if(response.getCode() == ResponseCode.CONTENT) {
											//Update corresponding container with the retrieved content
											System.out.println("Update received for topic " + this.topic + " value: " + content);
											
											publishSensor.add(new CI(topic, content));
										}else {
											System.out.println("Response from observing sensor: " + response.getCode());
										}
									}
									
									public void onError() {
										System.err.println("Error on observing topic " + this.topic);
									}
								});
							System.out.println("Created observing relation with sensor");
						}else {
							//devices.addActuator(MAC, sector);	//Todo: check if can be avoided
							//Is an actuator --> subscribe to the container just created
							//create a resource in the CoapMonitor to handle updates of this container
							if(server.getRoot().getChild(topic) == null) {
								//resource to handle this container not jet created
								server.add(
										new CoapResource(topic) {
											@SuppressWarnings("unused")	//Is used remotely
											public void handlePost(CoapExchange exchange){
												exchange.respond(ResponseCode.CREATED);
												String payload = exchange.getRequestText();
												broker.publish(this.getName(), payload);	//publish the update 
											}
										});
							}
							middle_node.subscribe(topic, topic);	//Subscribe to the given topic on the MN, the resource that will handle updates have the same name of topic
						}
					}
				}
				topic_sem.release();	//release semaphore
				
				while(publishSensor.isEmpty() == false) {
					CI contentInstance = publishSensor.get(0);
					publishSensor.remove(0);
					middle_node.publishContentInstance(contentInstance.topic, contentInstance.value);
				}
				
				Thread.sleep(500);	//wait before checking again
			}
			
			/*String resp = broker.subscribe("devices");
			String[] devs = resp.split("\n", 0);
			for(String device : devs) {
				String[] path = device.split("/", 0);
				String sector = path[1];	//Sector
				String type = path[2];	//Type (sensor or actuator)
				String MAC = path[3];	//MAC of the device
				
				//Create the containers for this device on OneM2M
				try {
					middle_node.createContainer(AE_name, sector);	//create the Sector container
				}catch(Exception e) {
					System.err.println(e);
				}
				
				try {
					middle_node.createContainer(AE_name + "/" + sector, type);	//Create the type container
				}catch(Exception e) {
					System.err.println(e);
				}
				
				try {
					middle_node.createContainer(AE_name + "/" + sector + "/" + type, MAC);	//create the device container
				}catch(Exception e) {
					System.err.println(e);
				}
				
				if(type.equals("sensor")) {
					//devices.addSensor(MAC, sector);	//TODO: check if can be avoided
					//Is a sensor --> I also have to subscribe to this topic
					try {
						String res = broker.subscribe(device);
						if(res != null) {
							//content already published on this topic --> create a Content Instance
							middle_node.publishContentInstance(AE_name + "/" + sector + "/" + type + "/" + MAC, res);
						}
					}catch(Exception e) {
						if(e.getMessage().equals("Not found")) {
							//Error: topic not found
							System.err.println("Topic '" + device + "' not found");
						}else {
							//Generic error: e
							System.err.println("Error subscribing: " + e.getMessage());
						}
					}
				}else {
					//devices.addActuator(MAC, sector);	//Todo: check if can be avoided
					//Is an actuator --> subscribe to the container just created
					middle_node.subscribe(AE_name + "/" + sector + "/" + type + "/" + MAC, MAC);	//The resource that will receive updates is called as device MAC
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}*/
		
		
		/*CoapClient client = new CoapClient(middle_ip + "/~/" + middle_node);
		
		//Create the application entity
		JSONObject payload = new JSONObject();
		JSONObject obj = new JSONObject();
		obj.put("api", "Service_AE_ID");	//Application id
		obj.put("rn", "Service_AE");		//Resource name
		obj.put("rr", "true");				//Request reachability
		payload.put("m2m:ae", obj);
		
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267, 2));	//Set resource type to application entity
		request.getOptions().addOption(new Option(256, "admin:admin"));
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.setPayload(payload.toString());
		
		CoapResponse response = client.advanced(request);
		
		if(ResponseCode.isSuccess(response.getCode())) {
			System.out.println("Response: " + response.getResponseText());
		}else {
			System.err.println("Error creating the ApplicationEntity: " + response.getResponseText());
		}*/
		
		
	}

}
