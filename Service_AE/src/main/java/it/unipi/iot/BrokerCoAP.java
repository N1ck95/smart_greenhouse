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
	
	public void publish(String uri, String content) {
		String addr = this.broker_ip + uri;
		CoapClient client = new CoapClient(addr);
		Request request = new Request(Code.PUT);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		request.getOptions().setAccept(MediaTypeRegistry.TEXT_PLAIN);
		request.setPayload(content);
		
		System.out.println("[DEBUG] Publishing on broker: " + addr);
		CoapResponse response = client.advanced(request);
		System.out.println("[DEBUG] Response code forom broker: " + response.getCode());
		System.out.println("[DEBUG] Response from broker publish: " + response.getResponseText());
		
		
		if(response.getCode() == ResponseCode.CREATED) {
			//Successful publish and topic created
			System.out.println("[DEBUG]Created content on broker");
		}else if(response.getCode() == ResponseCode.CHANGED) {
			//Successful publish
			System.out.println("[DEBUG]Content on broker changed");
		}else if(response.getCode() == ResponseCode.BAD_REQUEST) {
			//Malformed request
			System.err.println("[DEBUG]Publish failed on broker");
		}else if(response.getCode() == ResponseCode.UNAUTHORIZED) {
			//Authorization failure
			System.err.println("[DEBUG]Publish on broker failed");
		}else if(response.getCode() == ResponseCode.NOT_FOUND) {
			//Topic does not exists
			System.err.println("[DEBUG] Publish on broker failed");
		}else if(response.getCode().codeClass == 4 && response.getCode().codeDetail == 29) {
			//Too many requests: reduce rate of publishes
		}
	}
}
