package it.unipi.it;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

public class TestBroker extends CoapServer{
	static DevicesResource devices = new DevicesResource("devices");
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestBroker server = new TestBroker();
		/*System.out.println("root: " + server.getRoot().getName());
		System.out.println("Childrens: ");
		Collection<Resource> childrens = server.getRoot().getChild(".well-known").getChild("core").getChildren();
		Iterator<Resource> it = childrens.iterator();
		while(it.hasNext()) {
			System.out.println("Children: " + it.next().getName());
		}*/
		if(server.getRoot().getChild(".well-known").getChild("core").isObservable()) {
			System.out.println(".well-known/core observable");
		}else {
			System.out.println(".well-known/core not observable");
		}
		/*Set<String> attr = server.getRoot().getChild(".well-known").getChild("core").getAttributes().getAttributeKeySet();
		Iterator<String> it = attr.iterator();
		System.out.println("Attributes: ");
		while(it.hasNext()) {
			System.out.println("Attr: " + it.next());
		}*/
		//server.getRoot().getChild(".well-known").getChild("core").getAttributes().setAttribute(attr, value);;
		/*if(server.getRoot().getChild(".well-known").getChild("core").isObservable()) {
			System.out.println(".well-known/core observable");
		}else {
			System.out.println(".well-known/core not observable");
		}*/
		
		System.out.println("Childrens:");
		Collection<Resource> childrens = server.getRoot().getChildren();
		Iterator<Resource> it = childrens.iterator();
		while(it.hasNext()) {
			System.out.println("Children: " + it.next().getName());
		}
		
		server.start();
		server.add(devices);
		
		System.out.println("Childrens:");
		childrens = server.getRoot().getChildren();
		it = childrens.iterator();
		while(it.hasNext()) {
			System.out.println("Children: " + it.next().getName());
		}
		/*attr = server.getRoot().getChild("devices").getAttributes().getAttributeKeySet();
		it = attr.iterator();
		System.out.println("Attributes: ");
		while(it.hasNext()) {
			System.out.println("Attr: " + it.next());
		}*/
	}

}
