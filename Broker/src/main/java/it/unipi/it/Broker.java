package it.unipi.it;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.server.resources.Resource;

public class Broker extends CoapServer{
	final static int PORT = 6001;		//Port used by the Broker service
	
	public static void main(String[] args) {
		Broker server = new Broker();
		server.addEndpoint(new CoapEndpoint(new InetSocketAddress(PORT)));
		server.start();
		server.add(new RootResource("ps"));	//Root resource
		
		System.out.println("Childrens:");
		Collection<Resource> childrens = server.getRoot().getChildren();
		Iterator<Resource> it = childrens.iterator();
		while(it.hasNext()) {
			System.out.println("Children: " + it.next().getName());
		}
	
	}
}