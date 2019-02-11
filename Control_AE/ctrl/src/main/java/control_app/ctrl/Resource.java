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

public class Resource extends CoapResource{
	
	//public boolean changed;
	//public Integer lastVal;
	int i;
	
	public Resource(String name) {
	    super(name);
	    setObservable(true);
	    i = 0;
	    
	   // changed = false;
	    //lastVal = new Integer(0);
	    }
	
	    public void handleGET(CoapExchange exchange) {
	    	
	    	
	        /*Response response = new Response(ResponseCode.CONTENT);
	        List<String> ret = new ArrayList<String>();
	        ret = exchange.getRequestOptions().getUriQuery();
	      
	     
	        exchange.respond(response);*/
	    	/*Integer num = 0;
			try {
				if(exchange.getRequestOptions().getURIQueryCount() != 1) {
					exchange.respond(ResponseCode.BAD_REQUEST);
					return;
				}
				String q = exchange.getRequestOptions().getUriQuery().get(0);
				System.out.println(q);
				q = q.split("=")[1];
				System.out.println(q);
				num = Integer.parseInt(q);
				num = num*num;
				
			}catch (NumberFormatException e) {
				exchange.respond(ResponseCode.BAD_REQUEST);
				return;
			}catch (ArrayIndexOutOfBoundsException e1) {
				exchange.respond(ResponseCode.BAD_REQUEST);
				return;
			}
			
			Response response = new Response(ResponseCode.CONTENT);
			
			if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_XML){
				response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_XML);
				response.setPayload("<value>" + num.toString() + "</value>");
			}
			else if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
				response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
				JSONObject dataset = new JSONObject();
				dataset.put("value", num);
				response.setPayload(dataset.toString());
				//response.setPayload(dataset.toJSONString());
			}
			exchange.respond(response);*/
	    	exchange.respond("hello world");
	    	
	    }
	    public void handlePOST(CoapExchange exchange) {
	    /* your stuff */
	    	//what is exchange?
	    	
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
					System.out.println("valore e "+val);
					
					if (path_resource.contains("Sector1")) {
			        	if (path_resource.contains("Temp")) {
			        		Globals.Sector1_Temp.s.set(val);
			        		
			        	}
			        	if (path_resource.contains("Humid")) {
			        		Globals.Sector1_Humid.s.set(val);
			        	}
			        	if (path_resource.contains("Light")) {
			        		Globals.Sector1_Light.s.set(val);
			        	}
			        	if (path_resource.contains("SoilMoist")) {
			        		Globals.Sector1_SoilMoist.s.set(val);
			        	}
			        	/*if (path_resource.contains("Smoke_Sensors")) {
			        		Globals.Sector1_Smoke.s.set(val);
			        	}
			        	if (path_resource.contains("PIR")) {
			        		Globals.Sector1_PIR.s.set(val);
			        	}
			        	*/
			        }
			        if (path_resource.contains("Sector2")) {
			        	if (path_resource.contains("Temp")) {
			        		Globals.Sector2_Temp.s.set(val);
			        		
			        	}
			        	if (path_resource.contains("Humid")) {
			        		Globals.Sector2_Humid.s.set(val);
			        	}
			        	if (path_resource.contains("Light")) {
			        		Globals.Sector2_Light.s.set(val);
			        	}
			        	if (path_resource.contains("SoilMoist")) {
			        		Globals.Sector2_SoilMoist.s.set(val);
			        	}
			        	/*if (path_resource.contains("Smoke_Sensors")) {
		        		Globals.Sector2_Smoke.s.set(val);
		        	}
		        	if (path_resource.contains("PIR")) {
		        		Globals.Sector2_PIR.s.set(val);
		        	}
		        	*/
			        	
			        }
			        if (path_resource.contains("Sector3")) {
			        	if (path_resource.contains("Temp")) {
			        		Globals.Sector3_Temp.s.set(val);
			        		
			        	}
			        	if (path_resource.contains("Humid")) {
			        		Globals.Sector3_Humid.s.set(val);
			        	}
			        	if (path_resource.contains("Light")) {
			        		Globals.Sector3_Light.s.set(val);
			        	}
			        	if (path_resource.contains("SoilMoist")) {
			        		Globals.Sector3_SoilMoist.s.set(val);
			        	}
			        	/*if (path_resource.contains("Smoke_Sensors")) {
		        		Globals.Sector3_Smoke.s.set(val);
		        	}
		        	if (path_resource.contains("PIR")) {
		        		Globals.Sector3_PIR.s.set(val);
		        	}
		        	*/
			        	
			        }
			        if (path_resource.contains("Sector4")) {
			        	if (path_resource.contains("Temp")) {
			        		Globals.Sector4_Temp.s.set(val);
			        		
			        	}
			        	if (path_resource.contains("Humid")) {
			        		Globals.Sector4_Humid.s.set(val);
			        	}
			        	if (path_resource.contains("Light")) {
			        		Globals.Sector4_Light.s.set(val);
			        	}
			        	if (path_resource.contains("SoilMoist")) {
			        		Globals.Sector4_SoilMoist.s.set(val);
			        	}
			        	/*if (path_resource.contains("Smoke_Sensors")) {
		        		Globals.Sector4_Smoke.s.set(val);
		        	}
		        	if (path_resource.contains("PIR")) {
		        		Globals.Sector4_PIR.s.set(val);
		        	}
		        	*/
			        	
			        }
			        if (path_resource.contains("Sector5")) {
			        	if (path_resource.contains("Temp")) {
			        		Globals.Sector5_Temp.s.set(val);
			        		
			        	}
			        	if (path_resource.contains("Humid")) {
			        		Globals.Sector5_Humid.s.set(val);
			        	}
			        	if (path_resource.contains("Light")) {
			        		Globals.Sector5_Light.s.set(val);
			        	}
			        	if (path_resource.contains("SoilMoist")) {
			        		Globals.Sector5_SoilMoist.s.set(val);
			        	}
			        	/*if (path_resource.contains("Smoke_Sensors")) {
		        		Globals.Sector5_Smoke.s.set(val);
		        	}
		        	if (path_resource.contains("PIR")) {
		        		Globals.Sector5_PIR.s.set(val);
		        	}
		        	*/
			        }	
					
					
					
					}
					//}
					
					System.out.println("got con");
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
	       // try {
	        	//if (this.i>1) {
		        //val = Integer.parseInt(obj.getJSONObject("m2m:sgn").getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:cin").getString("con"));
		       // System.out.println(val);
		       /* if (path_resource.contains("Sector1")) {
		        	if (path_resource.contains("Temp")) {
		        		Globals.Sector1_Temp.s.set(val);
		        		
		        	}
		        	if (path_resource.contains("Humid")) {
		        		Globals.Sector1_Humid.s.set(val);
		        	}
		        	if (path_resource.contains("Light")) {
		        		Globals.Sector1_Light.s.set(val);
		        	}
		        	if (path_resource.contains("SoilMoist")) {
		        		Globals.Sector1_SoilMoist.s.set(val);
		        	}
		        	
		        }
		        if (path_resource.contains("Sector2")) {
		        	if (path_resource.contains("Temp")) {
		        		Globals.Sector2_Temp.s.set(val);
		        		
		        	}
		        	if (path_resource.contains("Humid")) {
		        		Globals.Sector2_Humid.s.set(val);
		        	}
		        	if (path_resource.contains("Light")) {
		        		Globals.Sector2_Light.s.set(val);
		        	}
		        	if (path_resource.contains("SoilMoist")) {
		        		Globals.Sector2_SoilMoist.s.set(val);
		        	}
		        	
		        }
		        if (path_resource.contains("Sector3")) {
		        	if (path_resource.contains("Temp")) {
		        		Globals.Sector3_Temp.s.set(val);
		        		
		        	}
		        	if (path_resource.contains("Humid")) {
		        		Globals.Sector3_Humid.s.set(val);
		        	}
		        	if (path_resource.contains("Light")) {
		        		Globals.Sector3_Light.s.set(val);
		        	}
		        	if (path_resource.contains("SoilMoist")) {
		        		Globals.Sector3_SoilMoist.s.set(val);
		        	}
		        	
		        }
		        if (path_resource.contains("Sector4")) {
		        	if (path_resource.contains("Temp")) {
		        		Globals.Sector4_Temp.s.set(val);
		        		
		        	}
		        	if (path_resource.contains("Humid")) {
		        		Globals.Sector4_Humid.s.set(val);
		        	}
		        	if (path_resource.contains("Light")) {
		        		Globals.Sector4_Light.s.set(val);
		        	}
		        	if (path_resource.contains("SoilMoist")) {
		        		Globals.Sector4_SoilMoist.s.set(val);
		        	}
		        	
		        }
		        if (path_resource.contains("Sector5")) {
		        	if (path_resource.contains("Temp")) {
		        		Globals.Sector5_Temp.s.set(val);
		        		
		        	}
		        	if (path_resource.contains("Humid")) {
		        		Globals.Sector5_Humid.s.set(val);
		        	}
		        	if (path_resource.contains("Light")) {
		        		Globals.Sector5_Light.s.set(val);
		        	}
		        	if (path_resource.contains("SoilMoist")) {
		        		Globals.Sector5_SoilMoist.s.set(val);
		        	}
		        	
		        }*/
	        	//} 
		       // this.i = this.i+1;
	        	//System.out.println(this.i);
	       // }
	        //	catch (Exception e) {}
	        	
	        	
	       //String name_resource = this.getName(); this isnt the same as the path_resource?
	        //System.out.println(name_resource);
	        
	      // global.s.set(val);
	    //the code below is not needed
	    /*
	    if name_resource.contains("Temp")
	    	if name_resource.contains("sector1)
	    			wake up the thread for temperature for that sector
	    			wakeupthread_sect1_temp
	    			global.s.set(val);
	    			GlobalSharedTempSect1.s.set(val);
	    			
	    	if name_resource.contains("sector2)
	    	    	wake up the thread for temperature for that sector
	    	    	wakeupthread_sect2_temp
	    	    	GlobalSharedTempSect2.s.set(val);*/
	        exchange.respond(ResponseCode.CREATED);
	    
	    }

}
