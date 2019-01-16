package control_app.ctrl;


import java.net.URI;
import java.net.URISyntaxException;

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
		//what should the response look like and what should it contain?
		System.out.println(response);
		//why we do what we do below?
		JSONObject resp = new JSONObject(response);
		JSONObject container = (JSONObject) resp.get("m2m:ae");
		ae.setRn((String) container.get("rn"));
		ae.setTy((Integer) container.get("ty"));
		ae.setRi((String) container.get("ri"));
		ae.setPi((String) container.get("pi"));
		ae.setCt((String) container.get("ct"));
		ae.setLt((String) container.get("lt"));
    
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
	    System.out.println(response);
	      
	  }
	  public void Discovery(String cse){
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
	    System.out.println(response);
	    
	    
	    
	      
	  }

}
