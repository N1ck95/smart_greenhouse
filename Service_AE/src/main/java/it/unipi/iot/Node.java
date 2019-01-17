package it.unipi.iot;

import java.util.ArrayList;

public class Node {
	private Node parent;
	private ArrayList<Node> sons;
	private String path;
	private String name;
	
	public Node(Node parent, String name) {
		this.parent = parent;
		if(this.parent != null) {
			this.path = parent.getPath() + '/' + name;
		}else {
			this.path = name;
		}
		this.name = name;
		this.sons = new ArrayList<Node>();
	}
	
	public void addSon(Node son) {
		this.sons.add(son);
	}
	
	public Node getParent() {
		return parent;
	}
	
	public Node getSon(String name) {
		for(int i = 0; i < sons.size(); i++) {
			if(sons.get(i).getName().equals(name)) {
				//Son found
				return sons.get(i);
			}
		}
		return null;
	}
	
	public boolean removeSon(int i) {
		try {
			this.sons.remove(i);
			return true;
		}catch(IndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getName() {
		return this.name;
	}
}
