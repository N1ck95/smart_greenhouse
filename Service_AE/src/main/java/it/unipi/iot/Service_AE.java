package it.unipi.iot;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Service_AE extends CoapServer{
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "niccolo-mn-cse";		//Middle node id
	final static String middle_name = "niccolo-mn-name";	//Middle node name
	final static int BROKER_PORT = 6001;
	//final static String broker_uri = "coap://127.0.0.1:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String broker_uri = "coap://[fd00::2]:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String AE_ID = "Service_AE_ID";			//Id of the Application Entity
	final static String AE_name = "Service_AE";				//Name of the Application Entity
	final static int PORT = 6000;							//Port of this AE
	
	public static void main(String[] args) throws InterruptedException {
		final BrokerCoAP broker = new BrokerCoAP(broker_uri, BROKER_PORT);
		//Tree devices = new Tree("Service_AE");
		final OneM2M middle_node = new OneM2M(middle_ip, middle_id, middle_name);	//To interact with middle_node
		final Service_AE server = new Service_AE();
		server.addEndpoint(new CoapEndpoint(new InetSocketAddress(PORT)));
		server.start();
		
		final Semaphore topic_sem = new Semaphore(1);
		final ArrayList<Topic> newTopics = new ArrayList<Topic>();
		
		//final Semaphore publishSensor_sem = new Semaphore(1);
		final ArrayList<CI> publishSensor = new ArrayList<CI>();
		
		//Create the Application Entity on the middle node
		try {
			middle_node.createAE(AE_ID, AE_name);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		
		System.out.println("Subscribing to broker");
			CoapClient client = new CoapClient(broker_uri + "/ps");	//Subscribe to have notifications about topics
			@SuppressWarnings("unused")
			CoapObserveRelation relation = client.observeAndWait(
					new CoapHandler() {
						public void onLoad(CoapResponse response) {
							String content = response.getResponseText();
							if(response.getCode() == ResponseCode.CONTENT) {
								//Response have format: </topic1>,</topic2>,...,</well-known/.core>
								JSONObject JSONpayload = new JSONObject(content);
								JSONArray topics = JSONpayload.getJSONArray("topics");
								
								//Acquire semaphore
								try {
									topic_sem.acquire();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								for(Object topicObj : topics) {
									JSONObject obj = (JSONObject) topicObj;
									//String topic = topicObj.toString();
									Topic topic = new Topic(obj.getString("topic"), obj.getInt("cf"));
									//String topic = obj.getString("topic");	//Path of the topic
									//int cf = obj.getInt("cf");	//Content format of the topic
									System.out.println(topic.topic);
									
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
					Topic topic = newTopics.get(0);
					newTopics.remove(0);
					
					String[] path = topic.topic.split("/");
					if(path[0].equals(AE_name)) {
						//Is a topic that I have to link in OneM2M
						String sector = path[1];
						String type = path[2];
						String model = path[3];
						String MAC = path[4];
						
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
								new SensorCoapHandler(topic.topic, topic.cf) {
									
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
							if(server.getRoot().getChild(MAC) == null) {
								//resource to handle this container not jet created
								server.add(new CoapResource("Service_AE").add(new CoapResource(sector).add(new CoapResource(type).add(new CoapResource(model).add(new CoapResource(MAC) {
										@SuppressWarnings("unused")	//Is used remotely
										public void handlePOST(CoapExchange exchange){
											exchange.respond(ResponseCode.CREATED);
											String payload = exchange.getRequestText();
											System.out.println("[DEBUG] actuator update: " + payload);
											
											DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
										    DocumentBuilder builder;
											try {
												builder = factory.newDocumentBuilder();
												InputSource is = new InputSource(new StringReader(payload));
												Document xmlDoc = builder.parse(is);
												NodeList conList = xmlDoc.getElementsByTagName("con");
												if(conList.getLength() == 1) {
													String val = conList.item(0).getTextContent();
													System.out.println("[DEBUG] actuator new value: " + val);
													broker.publish("/ps" + this.getPath() + this.getName(), val);	//publish the update 
												}
											} catch (ParserConfigurationException e) {
												System.err.println("[ERROR] Error creating DocumentBuilder for XML");
											} catch (SAXException e) {
												//Error parsing XML document
												System.err.println("[ERROR] Error parsing XML document: " + e.getMessage());
											} catch (IOException e) {
												//IO error parsing XML document
												System.err.println("[ERROR] I/O error: " + e.getMessage());
											}
										}
								})))));
							}
							middle_node.subscribe(topic.topic, String.valueOf(PORT), "Service_AE/" + sector + "/" + type + "/" + model + "/" + MAC);	//Subscribe to the given topic on the MN, the resource that will handle updates have the same name of topic
						}
					}
				}
				topic_sem.release();	//release semaphore
				
				//While that checks if there are ContentInstances from sensor to publish on OneM2M
				while(publishSensor.isEmpty() == false) {
					CI contentInstance = publishSensor.get(0);
					publishSensor.remove(0);
					middle_node.publishContentInstance(contentInstance.topic, contentInstance.value);
				}
				
				Thread.sleep(500);	//wait before checking again
			}
		
	}

}
