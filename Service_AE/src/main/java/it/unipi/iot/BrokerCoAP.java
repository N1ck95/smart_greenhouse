package it.unipi.iot;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;


/**
 * @author Borgioli Niccolò
 * This class contains the functions needed to interact with the broker
 */
public class BrokerCoAP {
	private String broker_ip;
	private String broker_port;
	
	public BrokerCoAP(String broker_ip, int broker_port) {
		this.broker_ip = broker_ip;
		this.broker_port = String.valueOf(broker_port);
	}
	
	/*public String subscribe(String topic) throws Exception {
		CoapClient client = new CoapClient(broker_ip + "/" + topic);
		Request request = new Request(Code.GET);
		request.getOptions().addUriQuery("Observe=0");
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		CoapResponse response = client.advanced(request);
		
		if(response.getCode() == ResponseCode.CONTENT) {
			//Successfully subscribed. Published content returned
			return response.getResponseText();
		}else if(response.getCode().codeClass == 2 && response.getCode().codeDetail == 7) {
			//TODO: check response code verify for NO CONTENT (response code 2.07)
			return null;
		}else if(response.getCode() == ResponseCode.BAD_REQUEST) {
			//Malformed request
			throw new Exception("Malformed request");
		}else if(response.getCode() == ResponseCode.UNAUTHORIZED) {
			//Authorization failure
			throw new Exception("Authorization failure");
		}else if(response.getCode() == ResponseCode.NOT_FOUND) {
			//Topic does not exists
			throw new Exception("Not found");
		}else if(response.getCode() == ResponseCode.UNSUPPORTED_CONTENT_FORMAT) {
			//Unsupported content format (the requested topic has a different content format from the one specified)
			throw new Exception("Unsupported format");
		}else {
			throw new Exception("Unexpected response code");
		}
	}*/
	
	/*public String subscribeSensor(String topic, CoapHandler handler) {
		CoapClient client = new CoapClient(broker_ip + "/" + topic);
		CoapObserveRelation relation = client.observe(
				new CoapHandler() {
					public void onLoad(CoapResponse response) {
						String content = response.getResponseText();
						
					}
					
					public void onError() {
						
					}
				});
	}*/
	
	public void publish(String uri, String content) {
		String addr = this.broker_ip + uri;
		CoapClient client = new CoapClient(addr);
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		
		System.out.println("Publishing on broker: " + addr);
		CoapResponse response = client.advanced(request);
		System.out.println("Response code forom broker: " + response.getCode());
		System.out.println("Response from broker publish: " + response.getResponseText());
		
		
		if(response.getCode() == ResponseCode.CREATED) {
			//Successful publish and topic created
			System.out.println("[CREATED]Created content on broker");
		}else if(response.getCode() == ResponseCode.CHANGED) {
			//Successful publish
			System.out.println("[CHANGED]Content on broker changed");
		}else if(response.getCode() == ResponseCode.BAD_REQUEST) {
			//Malformed request
			System.err.println("[BAD_REQUEST]Publish failed on broker");
		}else if(response.getCode() == ResponseCode.UNAUTHORIZED) {
			//Authorization failure
			System.err.println("[UNAUTHORIZED]Publish on broker failed");
		}else if(response.getCode() == ResponseCode.NOT_FOUND) {
			//Topic does not exists
			System.err.println("[NOT_FOUND] Publish on broker failed");
		}else if(response.getCode().codeClass == 4 && response.getCode().codeDetail == 29) {
			//Too many requests: reduce rate of publishes
		}
	}
}
