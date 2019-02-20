package ctrl_in;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONArray;
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
	
	public  ArrayList<String> Discovery(String cse) throws Exception{
		  
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
	    System.out.println("[DEBUG] payload discovery response: " + response);
	    System.out.println("[DEBUG] code discovery response: " + responseBody.getCode().toString());
	    
	    if(responseBody.isSuccess()) {
		    ArrayList<String> final_paths = new ArrayList<String>();
	
		    JSONObject jsonPayload = new JSONObject(response);
		    String[] paths = jsonPayload.getString("m2m:uril").split(" ");
		    
		    System.out.println("[DEBUG] number of elements in discovery: " + paths.length);
		    
		    for(int i = 0; i < paths.length; i++) {
		    	String[] subpaths = paths[i].toString().split("/");
		    	//System.out.println(subpaths.length);
		    	if(subpaths.length == 8) {
		    		//Is a full path to a sensor
		    		//System.out.println(subpaths[0]);
		    		String cleaned = paths[i].replace("/" + subpaths[1] + "/" + subpaths[2] + "/" + subpaths[3] + "/", "");
		    		System.out.println("DEBUG: cleaned = " + cleaned);
		    		final_paths.add(cleaned);
		    	}
		    }
		    
		    return final_paths;
	    }else {
	    	throw new Exception("Discovery returned error code: " + responseBody.getCode().toString() + " message: " + response);
	    	
	    }
	  }
}
