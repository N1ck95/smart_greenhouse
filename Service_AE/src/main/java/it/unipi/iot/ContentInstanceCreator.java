package it.unipi.iot;

public class ContentInstanceCreator {
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "niccolo-mn-cse";		//Middle node id
	final static String middle_name = "niccolo-mn-name";	//Middle node name
	final static int BROKER_PORT = 6001;
	final static String broker_uri = "coap://127.0.0.1:" + String.valueOf(BROKER_PORT);			//Broker ip
	final static String AE_ID = "Service_AE_ID";			//Id of the Application Entity
	final static String AE_name = "Service_AE";				//Name of the Application Entity
	final static int PORT = 6000;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final OneM2M middle_node = new OneM2M(middle_ip, middle_id, middle_name);	//To interact with middle_node
		middle_node.publishContentInstance("Service_AE/Sector2/actuator/irrigator/TJ8XG95", "off");
	}

}