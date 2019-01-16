package control_app.ctrl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

public class Resource extends CoapResource{
	
	//public boolean changed;
	//public Integer lastVal;
	
	
	public Resource(String name) {
	    super(name);
	    setObservable(true);
	    
	   // changed = false;
	    //lastVal = new Integer(0);
	    }
	    public void handleGET(CoapExchange exchange) {
	    	
	    	
	        Response response = new Response(ResponseCode.CONTENT);
	        List<String> ret = new ArrayList<String>();
	        ret = exchange.getRequestOptions().getUriQuery();
	      
	     
	        exchange.respond(response);
	    }
	    public void handlePOST(CoapExchange exchange) {
	    /* your stuff */
	    	//what is exchange?
	    	exchange.respond(ResponseCode.CREATED);
	    	String path_resource = exchange.getRequestOptions().getUriPathString();//in the case of the lab04 exercise this returns the path of the resource in the coap server(monitor)
	    	//in fact it returned monitor because we only had one resource whose name was monitor
	    	//this is also the name of the resource right? yes
	    	System.out.println(path_resource);//the path of the resource i could have gotten by this.name,right?
	    	byte[] content = exchange.getRequestPayload();
	        String contentStr = new String(content);
	        JSONObject obj = new JSONObject(contentStr);
	        //obj.keys()
	        int val;
	        val = Integer.parseInt(obj.getJSONObject("m2m:sgn").getJSONObject("m2m:nev").getJSONObject("m2m:rep").getJSONObject("m2m:cin").getString("con"));
	        System.out.println(val);
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
	        	
	        }
	    
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
	    
	    
	    }

}
