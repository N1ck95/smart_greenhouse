package it.unipi.iot;

public class Tree {
	private Node root;
	
	public Tree(String root_name) {
		root = new Node(null, root_name);
	}
	
	public void addSector(String name) {
		if(root.getSon(name) == null) {
			//sector does not exists --> create
			Node sector = new Node(root, name);
			root.addSon(sector);
		}
	}
	
	public boolean checkSector(String name) {
		Node n = root.getSon(name);
		if(n == null) {
			return false;
		}else {
			return true;
		}
	}
	
	/*
	 * Creates a leaf for a sensor with the given name
	 * @param: name - name of the sensor
	 * @param: sector - name of the sector the sensor belongs to
	 * @return : boolean - true if the sensor has been created, false if already exists
	 */
	public boolean addSensor(String name, String sector) {
		Node sector_node = root.getSon(sector);
		if(sector_node == null) {
			//Sector does not exists --> create it
			sector_node = new Node(root, sector);
			root.addSon(sector_node);
		}
		
		Node sensor_node = sector_node.getSon("Sensors");
		if(sensor_node == null) {
			//Node 'Sensors' does not exists
			sensor_node = new Node(sector_node, "Sensors");
			sector_node.addSon(sensor_node);
		}
		
		Node device = sensor_node.getSon(name);
		if(device == null) {
			//Sensor node still not exists --> create it
			device = new Node(sensor_node, name);
			sensor_node.addSon(device);
			return true;
		}
		return false;
	}
	
	/*
	 * Creates a leaf for an actuator with the given name
	 * @param: name - name of the actuator
	 * @param: sector - name of the sector the sensor belongs to
	 * @return : boolean - true if the actuator has been created, false if already exists
	 */
	public boolean addActuator(String name, String sector) {
		Node sector_node = root.getSon(sector);
		if(sector_node == null) {
			//Sector does not exists --> create it
			sector_node = new Node(root, sector);
			root.addSon(sector_node);
		}
		
		Node actuators_node = sector_node.getSon("Actuators");
		if(actuators_node == null) {
			//Node 'Actuator' does not exists
			actuators_node = new Node(sector_node, "Actuators");
			sector_node.addSon(actuators_node);
		}
		
		Node device = actuators_node.getSon(name);
		if(device == null) {
			//Actuator node still not exists --> create it
			device = new Node(actuators_node, name);
			actuators_node.addSon(device);
			return true;
		}
		return false;
	}
}