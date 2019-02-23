package it.unipi.it;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class TopicResource extends CoapResource{
	int contentType;	//Accepted content type for this topic
	public String value;
	
	public TopicResource(String name, int ct) {
		super(name);
		this.setObservable(true);
		contentType = ct;
		value = null;
	}
	
	public TopicResource(String name, String ct) {
		super(name);
		this.setObservable(true);
		contentType = Integer.parseInt(ct);
		value = null;
	}
	
	public void handlePUT(CoapExchange exchange) {
		//Publish request
		int ct = exchange.getRequestOptions().getContentFormat();
		System.out.println("Received PUT request");
		if(exchange.getRequestOptions().isContentFormat(this.contentType) || exchange.getRequestOptions().hasContentFormat() == false) {
			this.value = exchange.getRequestText();
			exchange.respond(ResponseCode.CHANGED);
			this.notifyObserverRelations(null);
		}else {
			System.out.println("Received cf: " + ct);
			System.out.println("My content format is: " + contentType);
			exchange.respond(ResponseCode.BAD_REQUEST);
		}
	}
	
	public void handleGET(CoapExchange exchange) {
		//Read request
		if(value == null) {
			//No content published on this topic
			//TODO: check how to create a custom response code (need 2.07 for NO_CONTENT)
			exchange.respond(ResponseCode.NO_CONTENT);
		}else {
			if(exchange.getRequestOptions().isAccept(contentType) || exchange.getRequestOptions().hasAccept() == false){
			//if(exchange.getRequestOptions().isContentFormat(contentType)) {
				exchange.respond(ResponseCode.CONTENT, value);
			}else {
				System.out.println("Received accept: " + exchange.getRequestOptions().getAccept());
				System.out.println("My content format is: " + contentType);
				exchange.respond(ResponseCode.UNSUPPORTED_CONTENT_FORMAT);
			}
		}
	}
	
	public void handleDELETE(CoapExchange exchange) {
		//Remove request
		this.delete();
		exchange.respond(ResponseCode.DELETED);
	}
	
	public void notifyObservers() {
		this.notifyObserverRelations(null);
	}
	
}
