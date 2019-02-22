package it.unipi.iot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.eclipse.californium.core.server.resources.Resource;
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
	final static String middle_id = "mn-cse";		//Middle node id
	final static String middle_name = "mn-name";	//Middle node name
	final static int BROKER_PORT = 6001;
	//final static String broker_uri = "coap://127.0.0.1:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String broker_uri = "coap://[fd00::2]:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String AE_ID = "Service_AE_ID";			//Id of the Application Entity
	final static String AE_name = "Service_AE";				//Name of the Application Entity
	final static int PORT = 6000;							//Port of this AE
	
	static ArrayList<MACPath> association = new ArrayList<MACPath>();
	
	private static void loadAssociation() {
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
				String[] el = line.split(" ");	//Each line is formatted as: "<MAC> <path>"
				association.add(new MACPath(el[0], el[1]));
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
	
	public static void main(String[] args) throws InterruptedException {
		final BrokerCoAP broker = new BrokerCoAP(broker_uri);
		//Tree devices = new Tree("Service_AE");
		final OneM2M middle_node = new OneM2M(middle_ip, middle_id, middle_name);	//To interact with middle_node
		final Service_AE server = new Service_AE();
		
		server.addEndpoint(new CoapEndpoint(new InetSocketAddress(PORT)));
		server.start();
		
		loadAssociation();
		
		System.out.println("[INFO] Server started");
		
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
		
		System.out.println("[INFO] Subscribing to broker");
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
									//System.out.println(topic.topic);
									
									newTopics.add(topic);	//Is a shared variable
								}
								
								topic_sem.release();	//Release semaphore
							}
						}
				
						public void onError() {
							System.err.println("[ERROR] Response error on observe relation with /ps");
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
					
					String[] path;
					String fullpath = null;
					
					for(int i = 0; i < association.size(); i++) {
						if(association.get(i).MAC.equals(topic.topic)) {
							//MAC associated to a sector and a device
							fullpath = association.get(i).path;
						}
					}
					
					if(fullpath == null) {
						System.out.println("[WARN] device with MAC: " + topic.topic + " not associated to any path");
					}else {
						path = fullpath.split("/");
					
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
								System.out.println("[WARN] Failure creating sector container: " + e);
							}
							
							try {
								middle_node.createContainer(AE_name + "/" + sector, type);	//Create the type container
							}catch(Exception e) {
								System.out.println("[WARN] Failure creating type container: " + e);
							}
							
							try {
								middle_node.createContainer(AE_name + "/" + sector + "/" + type, model);
							}catch(Exception e) {
								System.out.println("[WARN] Failure creating model container: " + e);
							}
							
							try {
								middle_node.createContainer(AE_name + "/" + sector + "/" + type + "/" + model, MAC);	//create the device container
							}catch(Exception e) {
								System.out.println("[WARN] Failure creating MAC container: " + e);
							}
							
							if(type.equals("sensor")) {
								//devices.addSensor(MAC, sector);	//TODO: check if can be avoided
								//Is a sensor --> I also have to subscribe to this topic
								CoapClient sensorClient = new CoapClient(broker_uri + "/ps/" + MAC);
								System.out.println("[INFO] Observing: " + broker_uri + "/ps/" + MAC);
								Request req = new Request(Code.GET);
								req.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
								req.setObserve();
								req.setURI(sensorClient.getURI());
								
								@SuppressWarnings("unused")
								CoapObserveRelation sensorRelation = sensorClient.observe(req,
									new SensorCoapHandler(fullpath, topic.cf) {
										
										public void onLoad(CoapResponse response) {
											String content = response.getResponseText();
											if(response.getCode() == ResponseCode.CONTENT) {
												//Update corresponding container with the retrieved content
												System.out.println("[INFO] Update received for topic " + this.topic + " value: " + content);
												
												publishSensor.add(new CI(topic, content));
											}else {
												System.out.println("[WARN] Response from observing sensor: " + response.getCode());
											}
										}
										
										public void onError() {
											System.err.println("[ERROR] Error on observing topic " + this.topic);
										}
									});
								System.out.println("[INFO] Created observing relation with " + model + " sensor " + MAC);
							}else {
								//devices.addActuator(MAC, sector);	//Todo: check if can be avoided
								//Is an actuator --> subscribe to the container just created
								//create a resource in the CoapMonitor to handle updates of this container
								
								Resource res = server.getRoot();
								Resource parent;
								for(int i = 0; i < 5; i++) {
									parent = res;
									Iterator<Resource> childrens = res.getChildren().iterator();
									while(childrens.hasNext()) {
										Resource child = childrens.next();
										if(child.getName().equals(path[i])) {
											res = child;
											//Subpath exists
										}
									}
									
									if(res == parent) {
										Resource tmp;
										if(i == 4) {
											//Topic is MAC
											tmp = new CoapResource(MAC) {
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
															broker.publish("/ps/" + this.getName(), val);	//publish the update 
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
											};
										}else {
											tmp = new CoapResource(path[i]);
										}
										res.add(tmp);
										res = tmp;	//Update exploring resource
									}
								}
								
								//Subscribe to the given topic on the MN, the resource that will handle updates have the same name of topic
								try {
									middle_node.subscribe(fullpath, String.valueOf(PORT), "Service_AE/" + sector + "/" + type + "/" + model + "/" + MAC);
									System.out.println("[INFO] Subscribed to actuator container " + fullpath);
								} catch (Exception e) {
									System.err.println("[ERROR] " + e.getMessage());
								}
							}
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
