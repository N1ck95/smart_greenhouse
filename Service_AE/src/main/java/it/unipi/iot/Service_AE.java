package it.unipi.iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONObject;

public class Service_AE extends CoapServer{
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "niccolo-mn-cse";		//Middle node id
	final static String middle_name = "niccolo-mn-name";	//Middle node name
	final static String broker_uri = "coap://127.0.0.1:5683";			//Broker ip
	final static String AE_ID = "Service_AE_ID";			//Id of the Application Entity
	final static String AE_name = "Service_AE";				//Name of the Application Entity
	
	public static void main(String[] args) throws InterruptedException {
		final BrokerCoAP broker = new BrokerCoAP(broker_uri);
		//Tree devices = new Tree("Service_AE");
		final OneM2M middle_node = new OneM2M(middle_ip, middle_id, middle_name);	//To interact with middle_node
		final Service_AE server = new Service_AE();
		
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
			CoapClient client = new CoapClient(broker_uri + "/.well-known/core");	//Subscribe to have notifications about topics
			CoapObserveRelation relation = client.observeAndWait(
					new CoapHandler() {
						public void onLoad(CoapResponse response) {
							String content = response.getResponseText();
//							System.out.println("OnLoad");
//							System.out.println(response.getCode().toString());
							if(response.getCode() == ResponseCode.CONTENT) {
								//Response have format: </topic1>,</topic2>,...,</well-known/.core>
								String[] topics = content.split(",");
								for(String topic : topics) {
									topic = topic.replace("</", "");
									topic = topic.replace(">", "");
									System.out.println(topic);
									String[] path = topic.split("/");
									if(path[1].equals(AE_name)) {
										//Is a topic that I have to link in OneM2M
										String sector = path[2];
										String type = path[3];
										String model = path[4];
										String MAC = path[5];
										
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
											middle_node.createContainer(AE_name + "/" + sector + "/" + type, model);
										}catch(Exception e) {
											System.err.println(e);
										}
										
										try {
											middle_node.createContainer(AE_name + "/" + sector + "/" + type + "/" + model, MAC);	//create the device container
										}catch(Exception e) {
											System.err.println(e);
										}
										
										if(type.equals("sensor")) {
											//devices.addSensor(MAC, sector);	//TODO: check if can be avoided
											//Is a sensor --> I also have to subscribe to this topic
											CoapClient sensorClient = new CoapClient(broker_uri + topic);
											CoapObserveRelation sensorRelation = sensorClient.observe(
												new SensorCoapHandler(topic) {
													
													public void onLoad(CoapResponse response) {
														String content = response.getResponseText();
														if(response.getCode() == ResponseCode.CONTENT) {
															//Update corresponding container with the retrieved content
															middle_node.publishContentInstance(topic, content);
														}
													}
													
													public void onError() {
														System.err.println("Error on observing topic " + this.topic);
													}
												}
											);
										}else {
											//devices.addActuator(MAC, sector);	//Todo: check if can be avoided
											//Is an actuator --> subscribe to the container just created
											//create a resource in the CoapMonitor to handle updates of this container
											if(server.getRoot().getChild(topic) == null) {
												//resource to handle this container not jet created
												server.add(
														new CoapResource(topic) {
															public void handlePost(CoapExchange exchange){
																exchange.respond(ResponseCode.CREATED);
																String payload = exchange.getRequestText();
																broker.publish(this.getName(), payload);	//publish the update 
															}
														});
											}
											middle_node.subscribe(topic, topic);	//Subscribe to the given topic on the MN, the resource that will handle updates have the same name of topic
											//middle_node.subscribe(AE_name + "/" + sector + "/" + type + "/" + MAC, topic);	//The resource that will receive updates is called as device MAC
										}
									}
								}
							}
						}
				
						public void onError() {
							System.err.println("Response error");
						}
					}	
			);
			
			while(true) {
				Thread.sleep(500);
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
