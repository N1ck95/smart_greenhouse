package it.unipi.iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONObject;

public class OneM2M {
	private String node_ip;
	private String node_id;
	private String node_name;
	
	public OneM2M(String node_ip, String node_id, String node_name) {
		this.node_ip = node_ip;
		this.node_id = node_id;
		this.node_name = node_name;
	}
	
	public void createAE(String AE_ID, String AE_name) throws Exception {
		CoapClient client = new CoapClient(this.node_ip + "/~/" + this.node_id);
		
		//Create the application entity
		JSONObject payload = new JSONObject();
		JSONObject obj = new JSONObject();
		obj.put("api", AE_ID);	//Application id
		obj.put("rn", AE_name);		//Resource name
		obj.put("rr", "true");				//Request reachability
		payload.put("m2m:ae", obj);
		
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267, 2));	//Set resource type to application entity
		request.getOptions().addOption(new Option(256, "admin:admin"));
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.setPayload(payload.toString());
		
		CoapResponse response = client.advanced(request);
		
		if(ResponseCode.isSuccess(response.getCode()) == false) {
			throw new Exception("Error: " + response.getResponseText());
		}
	}
	
	public void createContainer(String path, String container_name) throws Exception {
		CoapClient client = new CoapClient("coap://" + this.node_ip + "/~/" + this.node_id + "/" + this.node_name + "/" + path);
		
		//Create the new payload
		JSONObject payload = new JSONObject();
		JSONObject obj = new JSONObject();
		payload = new JSONObject();
		obj = new JSONObject();
		obj.put("rn", container_name);
		payload.put("m2m:cnt", obj);
		
		//create the new request
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267, 3));	//Set resource type to 3 = Container
		request.getOptions().addOption(new Option(256, "admin:admin"));	//Set username and password to authenticate
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);	//Set that we will accept only json responses
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.setPayload(payload.toString());	//Set the payload as String
		
		CoapResponse response = client.advanced(request);
		
		if(ResponseCode.isSuccess(response.getCode()) == false) {
			throw new Exception("Error creating container: " + response.getResponseText());
		}
	}
	
	public void publishContentInstance(String path, String value) {
		CoapClient client = new CoapClient(this.node_ip + "/~/" + this.node_id + "/" + this.node_name + "/" + path);
		
		//Create the new payload
		JSONObject payload = new JSONObject();
		JSONObject obj = new JSONObject();
		payload = new JSONObject();
		obj = new JSONObject();
		obj.put("cnf", "reading");	//content format
		obj.put("con", value);		//content of the instance
		payload.put("m2m:cin", obj);
		
		//configure the new request
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(267, 4));	//Set resource type to 4 = ContentInstance
		request.getOptions().addOption(new Option(256, "admin:admin"));	//Set username and password to authenticate
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);	//Set that we will accept only json responses
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.setPayload(payload.toString());	//Set the payload as String
		
		CoapResponse response = client.advanced(request);
		if(response.isSuccess()) {
			//Successfull
		}
	}
	
	public void subscribe(String path, String serverPort, String resource) throws Exception {
		CoapClient client = new CoapClient(this.node_ip + "/~/" + this.node_id + "/" + this.node_name + "/" + path);
		
		Request request = new Request(Code.POST);
		request.getOptions().addOption(new Option(256, "admin:admin"));
		request.getOptions().addOption(new Option(267, 23));	//Resource type is notification = 23
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_XML);
		JSONObject payload = new JSONObject();
		JSONObject obj = new JSONObject();
		
		obj.put("rn", "Monitor");
		obj.put("nu", "coap://" + this.node_ip + ":" + serverPort + "/" + resource);
		obj.put("nct", 2);
		payload.put("m2m:sub", obj);
		
		request.setPayload(payload.toString());
		
		CoapResponse resp = client.advanced(request);
		if(resp.isSuccess() == false) {
			throw new Exception("error subscribing to resource " + path + " error: " + resp.getResponseText());
		}
	}
}
