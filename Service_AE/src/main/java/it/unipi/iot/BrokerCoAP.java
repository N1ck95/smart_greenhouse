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
	
	public BrokerCoAP(String broker_ip) {
		this.broker_ip = broker_ip;
	}
	
	public String subscribe(String topic) throws Exception {
		CoapClient client = new CoapClient(broker_ip + "/" + topic);
		Request request = new Request(Code.GET);
		request.getOptions().addUriQuery("Observe=10");
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
	}
	
	public void publish(String uri, String content) {
		CoapClient client = new CoapClient(uri);
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		
		CoapResponse response = client.advanced(request);
		
		if(response.getCode() == ResponseCode.CREATED) {
			//Successful publish and topic created
		}else if(response.getCode() == ResponseCode.CHANGED) {
			//Successful publish
		}else if(response.getCode() == ResponseCode.BAD_REQUEST) {
			//Malformed request
		}else if(response.getCode() == ResponseCode.UNAUTHORIZED) {
			//Authorization failure
		}else if(response.getCode() == ResponseCode.NOT_FOUND) {
			//Topic does not exists
		}else if(response.getCode().codeClass == 4 && response.getCode().codeDetail == 29) {
			//Too many requests: reduce rate of publishes
		}
	}
}
