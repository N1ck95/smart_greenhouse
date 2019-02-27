package ctrl_in;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;





public class AE_CONTROL_IN {
	final static String in_ip = "127.0.0.1";			//Middle node ip
	final static String in_id = "in-cse";		//Middle node id
	final static String in_name = "in-name";	//Middle node name
	final static String AE_IN_control_name = "Control_AE_IN";
	final static String AE_IN_name_security = "Security_AE_IN";
	
	final static String AE_name_control_MN = "Control_AE";				//Name of the Application Entity
	final static String AE_name_security_MN = "Security_AE";
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "mn-cse";		//Middle node id
	final static String middle_name = "mn-name";	//Middle node name
	
	public void createSubscription(String cse, String ResourceName, String notificationUrl){
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", ResourceName);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		System.out.println(response);
				
	}

	public static void main(String[] args) {
		
		//create one application to run on the infrastructure node
		AE_CONTROL_IN ctrl_app = new AE_CONTROL_IN();
    	//create an instance that provides methods needed by the application entities
    	final AE_Control adn = new AE_Control();
    	//create applications entities for control and security
    	//the port for the infrastructure node is 5684
		AE ae = adn.createAE_Control("coap://127.0.0.1:5684/~/" + in_id, "ControlAppIn-ID", AE_IN_control_name);
		AE ae_security = adn.createAE_Control("coap://127.0.0.1:5684/~/in-cse", "SecurityAppIn-ID", AE_IN_name_security);
		
		int i = 0;
		int num_resources_mn = 0;	//number of resources i.e paths returned by the discovery
		
		//create lists for the sensor and actuators paths
		List<String> resources_paths_mn = new ArrayList<String>();
		List<String> actuators_paths_mn = new ArrayList<String>();
		//list for the discovery result 
		List<String> discovery = new ArrayList<String>();
		
		//perform discovery on the middle node
		try {
			discovery = adn.Discovery("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + "Service_AE");//this is the port of the middle node?
		}catch(Exception e) {
			System.err.println("[ERROR] " + e);
			System.out.println("[INFO] Terminating program");
			System.exit(1);
		}
		System.out.println(discovery.get(0));
		//get the total number of resources returned by the discovery
		num_resources_mn = discovery.size();
		System.out.println("[DEBUG] num of resources: " + num_resources_mn);
		
		String pom;
		//exclude the actuators from the list returned by the discovery
		for (i = 0; i< num_resources_mn; i++) {
			if (discovery.get(i).contains("sensor")) {
				pom = discovery.get(i);
				resources_paths_mn.add(pom);
			}else {
				pom = discovery.get(i);
				actuators_paths_mn.add(pom);
			}
		}

		num_resources_mn = resources_paths_mn.size();
		System.out.println("[DEBUG] discovered devices: " + num_resources_mn);
		
		//find the number of sectors
		
		String sensor_path;
		final ArrayList<Sector> sectors = new ArrayList<Sector>();
		for (i = 0; i< num_resources_mn; i++) {
			String[] subpaths = resources_paths_mn.get(i).split("/");
			boolean exists = false;
			for(int j = 0; j < sectors.size(); j++) {
				//new sector
				if(sectors.get(j).sectorName.equals(subpaths[0])) {
					exists = true;
					break;
				}
			}
			if(exists == false) {
				System.out.println(resources_paths_mn.get(i));
				System.out.println(subpaths[0]);
				sectors.add(new Sector(subpaths[0]));
			}
		}
		System.out.println("[INFO] number of sectors: " + sectors.size());
		int k = 0;
		for (i = 0; i< num_resources_mn; i++) {
			String[] subpaths = resources_paths_mn.get(i).split("/");
			String type = subpaths[2];	//get the type (temp, humidity,...)
			String sector = subpaths[0];
			System.out.println(type);
			System.out.println(resources_paths_mn.get(i));
			//i assume there are no duplicates in the paths of the sensors
			if (type.equals("temperature")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).temp_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
				
			}
			if (type.equals("humidity")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).humid_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
				
			}
			if (type.equals("light")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).light_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
			}
			if (type.equals("soil_moistiure")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).soilmoist_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
			}
			if (type.equals("smoke")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).smoke_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
			}
			if (type.equals("movement")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).pir_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
			}
			if (type.equals("camera")) {
				for (k=0;k<sectors.size();k++) {
					if (sector.equals(sectors.get(k).sectorName)) {
						sectors.get(k).cam_sens.add(resources_paths_mn.get(i));
						break;
					}
				}
			}
			
		}
		System.out.println(sectors.get(0).temp_sens.get(0));
		
		//create lists for target values for the physical quantities temperature, humidity, light and soil moistiure					
		List<String> target_temp = new ArrayList<String>();
		List<String> target_humid = new ArrayList<String>();
		List<String> target_light = new ArrayList<String>();
		List<String> target_soilmoist = new ArrayList<String>();
		

		int num_sectors = sectors.size();
		String target;
		//read the target values specified by the user
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		
		for(i = 0;i < num_sectors; i++) {
			System.out.println("Enter the target temp for sector " + sectors.get(i).sectorName);
			target = reader.next();
			target_temp.add(target);
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name_control_MN + "/" + sectors.get(i).sectorName + "/"+ "TargetTemp", target);
			System.out.println("in node prompt"+"coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name_control_MN + "/" + sectors.get(i).sectorName + "/"+ "TargetTemp");
			System.out.println("Enter the target humidity for sector " + sectors.get(i).sectorName);
			target = reader.next();
			target_humid.add(target);
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name_control_MN + "/" + sectors.get(i).sectorName + "/"+ "TargetHumid", target);
			
			System.out.println("Enter the target light for sector " + sectors.get(i).sectorName);
			target = reader.next();
			target_light.add(target);
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name_control_MN + "/" + sectors.get(i).sectorName + "/"+ "TargetLight", target);
			
			System.out.println("Enter the target soil moisture for sector " + sectors.get(i).sectorName);
			target = reader.next();
			target_soilmoist.add(target);
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name_control_MN + "/" + sectors.get(i).sectorName + "/"+ "TargetSoil", target);
		}
		
		reader.close();
		
		int j = 0;
		String[] sub_p;
		String sens_name;
		
		//create the containers in the control_ae in the infrastructure node
		
		for (i = 0; i< num_sectors ;i++) {
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/", sectors.get(i).sectorName);
			
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/", "Temperature");
			for(j = 0; j < sectors.get(i).temp_sens.size();j++) {
				sub_p = sectors.get(i).temp_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/" + "Temperature",sens_name );
			}
			
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/", "Humidity");
			for(j = 0; j < sectors.get(i).humid_sens.size();j++) {
				sub_p = sectors.get(i).humid_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/" + "Humidity",sens_name );
			}
			
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/", "Light");
			for(j = 0; j < sectors.get(i).light_sens.size();j++) {
				sub_p = sectors.get(i).light_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/" + "Light",sens_name );
			}
			
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/", "Soil_Moistiure");
			for(j = 0; j < sectors.get(i).soilmoist_sens.size();j++) {
				sub_p = sectors.get(i).soilmoist_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/" + "Soil_Moistiure",sens_name );
			}
						
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/", "Camera");
			for(j = 0; j < sectors.get(i).cam_sens.size();j++) {
				sub_p = sectors.get(i).cam_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_control_name + "/" + sectors.get(i).sectorName + "/" + "Camera",sens_name );
			}
		}
		
		//create the containers in the security_ae in the infrastructure node
		
		for (i = 0; i<num_sectors;i++) {
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_name_security + "/", sectors.get(i).sectorName);
			
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_name_security + "/" + sectors.get(i).sectorName + "/", "Movement");
			for(j = 0; j < sectors.get(i).pir_sens.size();j++) {
				sub_p = sectors.get(i).pir_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_name_security + "/" + sectors.get(i).sectorName + "/" + "Movement",sens_name );
			}
						
			adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_name_security + "/" + sectors.get(i).sectorName + "/", "Camera");
			for(j = 0; j < sectors.get(i).cam_sens.size();j++) {
				sub_p = sectors.get(i).cam_sens.get(j).split("/");
				sens_name = sub_p[3];
				adn.createContainer("coap://127.0.0.1:5684/~/in-cse/in-name" + "/" + AE_IN_name_security + "/" + sectors.get(i).sectorName + "/" + "Camera",sens_name );
			}
		}
			
		
		
		
		//create the new monitor server needed for subscription on port 5686
		CoapServer server = new CoapServer(5686);
		
		
		for(i = 0; i< num_resources_mn; i++) {
			
			//first create the resource in the monitor
			//as many resources in the coap monitor as there are sensor resources returned by the discovery
			
			String[] parts = resources_paths_mn.get(i).split("/");
			String sector = parts[0];
			//add the new resource in the coap monitor in such a way that its path is the same as the path of the sensor resource returned by the discovery
			server.add(new CoapResource(parts[0]).add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new CoapResource(parts[3]) {
				
				public void handlePOST(CoapExchange exchange) {
			    	String path_resource = exchange.getRequestOptions().getUriPathString();//in the case of the lab04 exercise this returns the path of the resource in the coap server(monitor)
			    	//in fact it returned monitor because we only had one resource whose name was monitor
			    	//this is also the name of the resource right? yes
			    	System.out.println(path_resource);
			    	System.out.println(exchange.getRequestText());
			        System.out.println("received notific");
			      
			        //parse the published value
			        
			        int val;
			        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			        try {
						DocumentBuilder builder = factory.newDocumentBuilder();
						try {
							InputStream inputStream = new ByteArrayInputStream(exchange.getRequestText().getBytes());
						    Document doc = builder.parse(inputStream);
							NodeList item_list = doc.getElementsByTagName("con");
							if (item_list.getLength() ==1) {
								Node p = item_list.item(0);
								Element con = (Element) p;
								System.out.println(con.getTextContent());
								val = Integer.parseInt(con.getTextContent());
								System.out.println("[INFO] valore e "+val);
								
								String[] subpaths = path_resource.split("/");
								String sector = subpaths[0];
								String type = subpaths[2];	
								String sensor_name = subpaths[3];
								
								if(type.equals("temperature")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_control_name + "/" + sector + "/" + "Temperature" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								if(type.equals("humidity")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_control_name + "/" + sector + "/" + "Humidity" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								
								if(type.equals("light")) {
	
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_control_name + "/" + sector + "/" + "Light" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								if(type.equals("soil_moistiure")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_control_name + "/" + sector + "/" + "Soil_Moistiure" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								if(type.equals("smoke")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_name_security + "/" + sector + "/" + "Smoke" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								if(type.equals("movement")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//create content instance in the appropriate container
											adn.createContentInstance("coap://127.0.0.1:5684/~/" + in_id + "/" + in_name + "/" + AE_IN_name_security + "/" + sector + "/" + "Movement" + "/" + sensor_name, Integer.toString(val));
											break;
										}
									}
								}
								
								
								
							}
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        exchange.respond(ResponseCode.CREATED);
			    
			    }
				
			}))));
		}
		
		//start the coap monitor
		server.start();
		
		//Subscribe to the sensors to receive updates
		//coap monitor on port 5686
		for(i = 0; i< num_resources_mn; i++) {
			System.out.println(resources_paths_mn.get(i));
			ctrl_app.createSubscription("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + resources_paths_mn.get(i),
			resources_paths_mn.get(i).replaceAll("/", "-"), "coap://127.0.0.1:5686/" + resources_paths_mn.get(i));
		}	
		
	
	}

}
