package it.unipi.iot;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

public class ADN {
	private static final int RESPONSE_STATUS_CODE = 265;
	private static final int ACCEPTED = 1000;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	int val = 12;
	private void start() {
		AE ae = this.createAE("coap://127.0.0.1:5683/~/mn-cse", "TempApp");
		Container container = this.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/TempApp", "DATA");
		scheduler.scheduleAtFixedRate(publisher, 10, 10, TimeUnit.SECONDS);
		
		
	}
	private AE createAE(String cse, String rn){
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
		obj.put("api","TempApp-ID");
		obj.put("rr","true");
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:ae", obj);
		String body = root.toString();
		System.out.println(body);
		req.setPayload(body);
		CoapResponse response = client.advanced(req);
		for(Option opt : response.getOptions().asSortedList()) {
			if(opt.getNumber() == RESPONSE_STATUS_CODE) {
				if(opt.getIntegerValue() == ACCEPTED) {
					String responseBody = new String(response.getPayload());
					System.out.println(responseBody);
					JSONObject resp = new JSONObject(responseBody);
					JSONObject container = (JSONObject) resp.get("m2m:ae");
					ae.setRn((String) container.get("rn"));
					ae.setTy((Integer) container.get("ty"));
					ae.setRi((String) container.get("ri"));
					ae.setPi((String) container.get("pi"));
					ae.setCt((String) container.get("ct"));
					ae.setLt((String) container.get("lt"));
					break;
				} else {
					String responseBody = new String(response.getPayload());
					System.out.println("Response Status Code: " + opt.getIntegerValue());
					System.out.println(responseBody);
				}
			}
		}
		return ae;

	}
	
	private Container createContainer(String cse, String rn){
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
		CoapResponse response = client.advanced(req);
		for(Option opt : response.getOptions().asSortedList()) {
			if(opt.getNumber() == RESPONSE_STATUS_CODE) {
				if(opt.getIntegerValue() == ACCEPTED) {
					String responseBody = new String(response.getPayload());
					System.out.println(responseBody);
					JSONObject resp = new JSONObject(responseBody);
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
					break;
				} else {
					String responseBody = new String(response.getPayload());
					System.out.println("Response Status Code: " + opt.getIntegerValue());
					System.out.println(responseBody);
				}
			}
		}
		
		
		return container;
	}
	
	final Runnable publisher = new Runnable() {
	   public void run() { 
		   String cse = "coap://127.0.0.1:5683/~/mn-cse/mn-name/TempApp/DATA";
		   
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
			content.put("con", String.valueOf(val));
			JSONObject root = new JSONObject();
			root.put("m2m:cin", content);
			String body = root.toString();
			System.out.println(body);
			req.setPayload(body);
			CoapResponse responseBody = client.advanced(req);
			
			String response = new String(responseBody.getPayload());
			System.out.println(response);
			val++;
	   }
	 };
	
	public static void main(String args[]) {
		ADN adn = new ADN();
		adn.start();
		for(;;);
	}	
}