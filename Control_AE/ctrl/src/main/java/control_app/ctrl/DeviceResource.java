package control_app.ctrl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DeviceResource extends CoapResource{
	
	//public boolean changed;
	//public Integer lastVal;
	public int targetValue;			//Target value for the measure I'm performing
	public ArrayList<String> controlActuator;	//Type of actuator that I can use
	public int numActuators;		//Number of target type of actuators
	
	public DeviceResource(String name) {
	    super(name);
	    setObservable(false);
	}
	
	public DeviceResource(String name, int targetValue, ArrayList<String> controlActuator) {
		super(name);
		setObservable(false);
		
		this.targetValue = targetValue;
		this.controlActuator = controlActuator;
	}
	
	    
    /*public void handlePOST(CoapExchange exchange) {
    	String path_resource = exchange.getRequestOptions().getUriPathString();//in the case of the lab04 exercise this returns the path of the resource in the coap server(monitor)
    	//in fact it returned monitor because we only had one resource whose name was monitor
    	//this is also the name of the resource right? yes
    	System.out.println(path_resource);//the path of the resource i could have gotten by this.name,right?
    	System.out.println(exchange.getRequestText());
        //JSONObject obj = new JSONObject(exchange.getRequestText());
        System.out.println("received notific");
        //obj.keys()
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
					//if (p.getNodeType() == Node.ELEMENT_NODE) {
					Element con = (Element) p;
					System.out.println(con.getTextContent());
					val = Integer.parseInt(con.getTextContent());
					System.out.println("[INFO] valore e "+val);
					
					String[] subpaths = path_resource.split("/");
					String sector = subpaths[0];
					String type = subpaths[2];
					
					int actuation;
					
					if(type.equals("temperature")) {
						actuation = Controls.temperatureControl(val, this.targetValue, this.numActuators);
						
					}else if(type.equals("humidity")) {
						actuation = Controls.humidityControl(val, this.targetValue, this.numActuators);
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
    
    }*/

}
