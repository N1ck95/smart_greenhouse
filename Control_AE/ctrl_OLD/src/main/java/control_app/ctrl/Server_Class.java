package control_app.ctrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
//should the server be a thread like we did here?
public class Server_Class extends Thread{
	CoapServer server;
	  public  Server_Class() {
	       server = new CoapServer(5685);
	        
	    }

	    public void addResource(String resource) {
	    	
	    	
	    	
	    	String[] parts = resource.split("/");
	    	//server.add(new CoapResource("Prova_AE").add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new CoapResource(parts[3])))));
	    	server.add(new CoapResource(parts[0]).add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new Resource(parts[3])))));
	    	//server.add(new Resource(resource));
	    
	    }
	    
	    public void addResource(String resource, List<Sector> sectors) {
	    	
	    	
	    	String[] parts = resource.split("/");
	    	//server.add(new CoapResource("Prova_AE").add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new CoapResource(parts[3])))));
	    	server.add(new CoapResource(parts[0]).add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new Resource(parts[3])))));
	    	//server.add(new Resource(resource));
	    
	    }
	    
	    public void run() {
	    	 server.start();
	    }

}
