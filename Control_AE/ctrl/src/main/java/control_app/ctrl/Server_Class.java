package control_app.ctrl;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
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
	      
	      server.add(new Resource(resource));
	     
	    }
	    public void run() {
	    	 server.start();
	    }

}
