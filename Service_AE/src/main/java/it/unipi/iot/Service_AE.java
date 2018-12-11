package it.unipi.iot;

import org.eclipse.californium.core.CoapClient;

public class Service_AE {
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_node = "niccolo-mn-cse";		//Middle node id
	
	public static void main(String[] args) {
		CoapClient client = new CoapClient(middle_ip + "/~/" + middle_node);
		
		
	}

}
