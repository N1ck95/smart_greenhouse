package it.unipi.it;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.CodeClass;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class TopicResource extends CoapResource{
	int contentType;	//Accepted content type for this topic
	String value;
	
	public TopicResource(String name, int ct) {
		super(name);
		contentType = ct;
		value = null;
	}
	
	public void handlePOST(CoapExchange exchange) {
		//Publish request
		int ct = exchange.getRequestOptions().getContentFormat();
		if(this.contentType == ct) {
			this.value = exchange.getRequestText();
			this.notifyAll();	//TODO: check
			exchange.respond(ResponseCode.CHANGED);
		}else {
			exchange.respond(ResponseCode.BAD_REQUEST);
		}
	}
	
	public void handleGET(CoapExchange exchange) {
		//Read request
		if(value == null) {
			//No content published on this topic
			//TODO: check how to create a custom response code (need 2.07 for NO_CONTENT)
			//exchange.respond((CodeClass.SUCCESS_RESPONSE,7));
		}else {
			if(exchange.getRequestOptions().isContentFormat(contentType)) {
				exchange.respond(ResponseCode.CONTENT, value);
			}else {
				exchange.respond(ResponseCode.UNSUPPORTED_CONTENT_FORMAT);
			}
		}
	}
	
	public void handleDELETE(CoapExchange exchange) {
		//Remove request
		this.delete();
		exchange.respond(ResponseCode.DELETED);
	}
	
}
