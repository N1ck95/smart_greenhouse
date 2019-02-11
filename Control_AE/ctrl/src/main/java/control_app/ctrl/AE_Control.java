package control_app.ctrl;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

public class AE_Control {
	public AE createAE_Control(String cse, String api, String rn){
		AE ae = new AE();
		URI uri = null;
		try {
			uri = new URI(cse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 2));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("api", api);
		obj.put("rr","true");
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:ae", obj);
		String body = root.toString();
		System.out.println(body);
		req.setPayload(body);
		//with the command below we specify that with the request that we send from the client, we want also to receive
		//a synchronous response message
		CoapResponse responseBody = client.advanced(req);
		
		
			String response = new String(responseBody.getPayload());
			System.out.println(response);
			
		
		
		
		//what should the response look like and what should it contain?
		
		//why we do what we do below?
	if (responseBody.isSuccess()) {
		JSONObject resp = new JSONObject(response);
		JSONObject container = (JSONObject) resp.get("m2m:ae");
		ae.setRn((String) container.get("rn"));
		
	}
		return ae;

  }
	public Container createContainer(String cse, String rn){
	    Container container = new Container();
	    URI uri = null;
	    try {
	      uri = new URI(cse);
	    } catch (URISyntaxException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    CoapClient client = new CoapClient(uri);
	    Request req = Request.newPost();
	    req.getOptions().addOption(new Option(267, 3));
	    req.getOptions().addOption(new Option(256, "admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    JSONObject obj = new JSONObject();
	    obj.put("rn", rn);
	    JSONObject root = new JSONObject();
	    root.put("m2m:cnt", obj);
	    String body = root.toString();
	    System.out.println(body);
	    req.setPayload(body);
	    CoapResponse responseBody = client.advanced(req);
	    
	    String response = new String(responseBody.getPayload());
	    System.out.println(response);
	    if (responseBody.isSuccess()) {
	    JSONObject resp = new JSONObject(response);
	    JSONObject cont = (JSONObject) resp.get("m2m:cnt");
	    container.setRn((String) cont.get("rn"));
		container.setTy((Integer) cont.get("ty"));
		container.setRi((String) cont.get("ri"));
		container.setPi((String) cont.get("pi"));
		container.setCt((String) cont.get("ct"));
		container.setLt((String) cont.get("lt"));
		container.setSt((Integer) cont.get("st"));
		container.setOl((String) cont.get("ol"));
		container.setLa((String) cont.get("la"));
	    }
	    return container;
	  }
	public void createContentInstance(String cse, String mycon){
	    URI uri = null;
	    try {
	      uri = new URI(cse);
	    } catch (URISyntaxException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    CoapClient client = new CoapClient(uri);
	    Request req = Request.newPost();
	    req.getOptions().addOption(new Option(267, 4));
	    req.getOptions().addOption(new Option(256, "admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    JSONObject content = new JSONObject();
	    content.put("cnf","message");
	    JSONObject con = new JSONObject();
	    content.put("con",mycon);
	    JSONObject root = new JSONObject();
	    root.put("m2m:cin", content);
	    String body = root.toString();
	    System.out.println(body);
	    req.setPayload(body);
	    CoapResponse responseBody = client.advanced(req);
	    //the payload needs always to be a string
	    String response = new String(responseBody.getPayload());
	    //System.out.println(response);
	      
	  }
	public int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
	  public  List<String> Discovery(String cse){
		  
	    URI uri = null;
	    try {
	      uri = new URI(cse);
	    } catch (URISyntaxException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	    }
	    CoapClient client = new CoapClient(uri);
	    Request req = Request.newGet();
	    req.getOptions().addOption(new Option(256, "admin:admin"));
	    req.getOptions().addUriQuery("fu=1");
	    req.getOptions().addUriQuery("rty=3");
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    CoapResponse responseBody = client.advanced(req);
	    
	    String response = new String(responseBody.getPayload());
	    System.out.println("response e " + response);
	    
	    String substr_response = response.substring(19);
	    System.out.println(substr_response);
	    String[] parts = substr_response.split(" ");
	    
	   
	    int i = 0;
	    int j = 0;
	    int k = 0;
	    List<String> full_paths = new ArrayList<String>();
	    //String[] full_paths = new String[8];
	  
	   
	    Integer isPresentTemp = new Integer(0);
	    Integer isPresentTempCount = new Integer(0);
	    Integer isPresentHumidCount = new Integer(0);
	    Integer isPresentHumid = new Integer(0);
	    Integer isPresentLightCount = new Integer(0);
	    Integer isPresentLight = new Integer(0);
	    Integer isPresentSoilMCount = new Integer(0);
	    Integer isPresentSoilM = new Integer(0);
	    Integer isPresentSmokeCount = new Integer(0);
	    Integer isPresentSmoke = new Integer(0);
	    Integer isPresentPirCount = new Integer(0);
	    Integer isPresentPir = new Integer(0);
	    Integer isPresentCameraCount = new Integer(0);
	    Integer isPresentCamera = new Integer(0);
	    Integer isPresentFanCount = new Integer(0);
	    Integer isPresentFan = new Integer(0);
	    Integer isPresentLampCount = new Integer(0);
	    Integer isPresentLamp = new Integer(0);
	    Integer isPresentIrrigCount = new Integer(0);
	    Integer isPresentIrrig = new Integer(0);
	    
	   
	    Integer change = new Integer(0);
	    int count = 0;
	    int change_sens_act = 0;
	    if (parts[0].contains("Sensor"))
	    	change_sens_act = 1;
	    if (parts[0].contains("Actuators"))
	    	change_sens_act = 2;
	    for(i = 0; i< parts.length; i++) {
	    	count = 0;
	    	count = countOccurrences(parts[i], '/');
	    	/*if (parts[i].contains("Sensor") && (change_sens_act==2) )
	    	{
	    		change_sens_act = 3;
	    		isPresentTempCount = 0;
	    		isPresentTemp = 0;
	    		isPresentHumidCount = 0;
	    		isPresentHumid = 0;
	    		isPresentLightCount = 0;
	    		isPresentLight = 0;
	    		isPresentSoilMCount = 0;
	    		isPresentSoilM = 0;
	    		isPresentSmokeCount = 0;
	    		isPresentSmoke = 0;
	    		isPresentPirCount = 0;
	    		isPresentPir = 0;
	    		isPresentCameraCount = 0;
	    		isPresentCamera = 0;
	    		isPresentLamp = 0;
	    		isPresentLampCount = 0;
	    		isPresentFan = 0;
	    		isPresentFanCount = 0;
	    		isPresentIrrig = 0;
	    		isPresentIrrigCount = 0;
	    	}
	    	if (parts[i].contains("Actuators") && (change_sens_act==1) )
	    	{
	    		change_sens_act = 3;
	    		isPresentTempCount = 0;
	    		isPresentTemp = 0;
	    		isPresentHumidCount = 0;
	    		isPresentHumid = 0;
	    		isPresentLightCount = 0;
	    		isPresentLight = 0;
	    		isPresentSoilMCount = 0;
	    		isPresentSoilM = 0;
	    		isPresentSmokeCount = 0;
	    		isPresentSmoke = 0;
	    		isPresentPirCount = 0;
	    		isPresentPir = 0;
	    		isPresentCameraCount = 0;
	    		isPresentCamera = 0;
	    		isPresentLamp = 0;
	    		isPresentLampCount = 0;
	    		isPresentFan = 0;
	    		isPresentFanCount = 0;
	    		isPresentIrrig = 0;
	    		isPresentIrrigCount = 0;
	    	}*/
	    	
	    	if ((parts[i].contains("Sector2")) && (change == 0)  ) {
	    		
	    		change = 1;
	    		isPresentTempCount = 0;
	    		isPresentTemp = 0;
	    		isPresentHumidCount = 0;
	    		isPresentHumid = 0;
	    		isPresentLightCount = 0;
	    		isPresentLight = 0;
	    		isPresentSoilMCount = 0;
	    		isPresentSoilM = 0;
	    		isPresentSmokeCount = 0;
	    		isPresentSmoke = 0;
	    		isPresentPirCount = 0;
	    		isPresentPir = 0;
	    		isPresentCameraCount = 0;
	    		isPresentCamera = 0;
	    		isPresentLamp = 0;
	    		isPresentLampCount = 0;
	    		isPresentFan = 0;
	    		isPresentFanCount = 0;
	    		isPresentIrrig = 0;
	    		isPresentIrrigCount = 0;
	    		
	    		
	    	}
	    	
	    	if ((parts[i].contains("Sector3")) && (change == 1)  ) {
	    		
	    		change = 2;
	    		isPresentTempCount = 0;
	    		isPresentTemp = 0;
	    		isPresentHumidCount = 0;
	    		isPresentHumid = 0;
	    		isPresentLightCount = 0;
	    		isPresentLight = 0;
	    		isPresentSoilMCount = 0;
	    		isPresentSoilM = 0;
	    		isPresentSmokeCount = 0;
	    		isPresentSmoke = 0;
	    		isPresentPirCount = 0;
	    		isPresentPir = 0;
	    		isPresentCameraCount = 0;
	    		isPresentCamera = 0;
	    		isPresentLamp = 0;
	    		isPresentLampCount = 0;
	    		isPresentFan = 0;
	    		isPresentFanCount = 0;
	    		isPresentIrrig = 0;
	    		isPresentIrrigCount = 0;
	    		
	    	}
	    	if ((parts[i].contains("Sector4")) && (change == 2)  ) {
	    		
	    		change = 3;
	    		isPresentTempCount = 0;
	    		isPresentTemp = 0;
	    		isPresentHumidCount = 0;
	    		isPresentHumid = 0;
	    		isPresentLightCount = 0;
	    		isPresentLight = 0;
	    		isPresentSoilMCount = 0;
	    		isPresentSoilM = 0;
	    		isPresentSmokeCount = 0;
	    		isPresentSmoke = 0;
	    		isPresentPirCount = 0;
	    		isPresentPir = 0;
	    		isPresentCameraCount = 0;
	    		isPresentCamera = 0;
	    		isPresentLamp = 0;
	    		isPresentLampCount = 0;
	    		isPresentFan = 0;
	    		isPresentFanCount = 0;
	    		isPresentIrrig = 0;
	    		isPresentIrrigCount = 0;
	    		
	    	}
	    	
	    	if (isPresentTemp == 1) {
	    		
	    		isPresentTempCount = 2;
	    		
	    		
	    	}
	    	if (isPresentHumid == 1) {
	    		
	    		isPresentHumidCount = 2;
	    		
	    		
	    	}
	    	if (isPresentLight == 1) {
	    		
	    		isPresentLightCount = 2;
	    		
	    		
	    	}
	    	if (isPresentSoilM == 1) {
	    		
	    		isPresentSoilMCount = 2;
	    		
	    		
	    	}
	    
	    	
	    	if (isPresentCamera == 1) {
	    		
	    		isPresentCameraCount = 2;
	    		
	    		
	    	}
	    	if (isPresentFan == 1) {
	    		
	    		isPresentFanCount = 2;
	    		
	    		
	    	}
	    	if (isPresentLamp == 1) {
	    		
	    		isPresentLampCount = 2;
	    		
	    		
	    	}
	    	if (isPresentIrrig == 1) {
	    		
	    		isPresentIrrigCount = 2;
	    		
	    		
	    	}
	    	if ((parts[i].contains("Temp"))  ) {
	    		isPresentTemp =1;
	    		
	    		if ((isPresentTempCount == 2) && (count>6)) {
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentTemp = 2;
		    		j++;
		    		
		    		
	    		}
	    		
	    		
	    	}
	    	if ((parts[i].contains("Humid"))  ) {
	    		isPresentHumid =1;
	    		
	    		if ((isPresentHumidCount == 2)&& (count>6)) {
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentHumid = 2;
		    		j++;
		    		
		    		
	    		}
	    	}
	    	if ((parts[i].contains("Light")) ) {
	    		isPresentLight =1;
	    		
	    		if ((isPresentLightCount == 2)&& (count>6)) {
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentLight = 2;
		    		j++;
		    		
		    		
	    		}
	    	}
	    	if ((parts[i].contains("SoilMoist")) ) {
	    		isPresentSoilM =1;
	    		
	    		if ((isPresentSoilMCount == 2)&& (count>6)) {
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentSoilM = 2;
		    		j++;
		    		
		    		
	    		}
	    	}
	    	
	    	//will the camera be present in the control app?
	    	if ((parts[i].contains("Camera")) ) {
	    		isPresentCamera =1;
	    		
	    		if ((isPresentCameraCount == 2)&& (count>6)) {
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentCamera = 2;
		    		j++;
		    		
		    		
	    		}
	    	}
	    	//actuators part
	    	if ((parts[i].contains("Fan")) ) {
	    		isPresentFan =1;
	    		
	    		if ((isPresentFanCount == 2)&& (count>6)) {
	    			//actuators[k] = parts[i];
	    			//k++;
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentFan = 2;
		    		j++;
		    		
		    		
		    		
	    		}
	    	}
	    	if ((parts[i].contains("Lamp")) ) {
	    		isPresentLamp =1;
	    		
	    		if ((isPresentLampCount == 2)&& (count>6)) {
	    			//actuators[k] = parts[i];
	    			//k++;
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentLamp = 2;
		    		j++;
		    		
		    		
		    		
	    		}
	    	} 
	    	
	    	
	    	if ((parts[i].contains("Irrigators"))  ) {
	    		
	    		isPresentIrrig =1;
	    		
	    		if ((isPresentIrrigCount == 2)&& (count>6)) {
	    			//actuators[k] = parts[i];
	    			//k++;
	    			//full_paths[j] = parts[i];
	    			full_paths.add(parts[i]);
		    		isPresentIrrig = 2;
		    		j++;
		    		
		    		
		    		
	    		}
	    	}
	    	//the sprinklers is for the app of security right? i should remove it right?
	    	if ((parts[i].contains("Sprinklers")) ) {
	    		
    			//actuators[k] = parts[i];
	    		k++;
	    	}
	    	
	    	
	    }
	   
	    Integer indexSector = new Integer(0);
	    indexSector = full_paths.get(0).indexOf("Sector");
	    
	   
	    
	    List<String> final_paths = new ArrayList<String>();
	   // String[] final_paths = new String[2];
	    int quot_index = 0;
	    String help;
	    for(i = 0; i< full_paths.size(); i++) {
	    	//if (full_paths[i]!= null) {
	    	//final_paths.add(full_paths.get(i).substring(indexSector));
	    	help = full_paths.get(i).substring(indexSector);
	    	//quot_index = final_paths.get(i).lastIndexOf('"');
	    	//quot_index = help.lastIndexOf('"');
	    	//final_paths.add(final_paths.get(i).substring(0, quot_index-1));
	    	//final_paths.add(help.substring(0, quot_index));
	    	final_paths.add(help);
	    	//}
	    }
	    quot_index = final_paths.get(i-1).lastIndexOf('"');
	    help = final_paths.get(i-1).substring(0, quot_index);
	    final_paths.set(i-1, help);
	    //int lastOcc = final_paths[1].lastIndexOf(']');
	   // final_paths[1] = final_paths[1].substring(0, lastOcc-1);
	    return final_paths;
		
	    
	    
	      
	  }

}
