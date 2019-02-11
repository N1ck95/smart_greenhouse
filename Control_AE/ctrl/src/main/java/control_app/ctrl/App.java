package control_app.ctrl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

import java.util.Scanner;
/**
 * Hello world!
 *
 */
public class App 
{
	public void createSubscription(String cse, String ResourceName, String notificationUrl){
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", ResourceName);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
		System.out.println(response);
				
	}
    public static void main( String[] args )
    {
    	App ctrl_app = new App();
    	
    	AE_Control adn = new AE_Control();
    	//create applications entities for control and security
		AE ae = adn.createAE_Control("coap://127.0.0.1:5683/~/mn-cse", "ControlApp-ID", "Control_AE");
		//AE ae_security = adn.createAE_Control("coap://127.0.0.1:5683/~/mn-cse", "SecurityApp-ID", "Security_AE");
		AE ae_prova = adn.createAE_Control("coap://127.0.0.1:5683/~/mn-cse", "provaApp-ID", "Prova_AE");
		//create containers to test the application(in fact these containers are unneeded, they are just for test
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE", "Sector1");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1", "Sensor");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Sensor", "Humid");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Sensor/Humid", "Sensor0");
		adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Sensor/Humid/Sensor0", "10");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE", "Sector2");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2", "Sensor");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor", "Temp");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor/Temp", "Sensor0");
		adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor/Temp/Sensor0", "8");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1", "Actuators");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators", "Irrigators");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators/Irrigators", "Irrigator0");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators/Irrigators", "Irrigator1");
		
		
		Integer i = new Integer(0);
		
		Integer num_sectors = new Integer(2);
		Integer num_resources_mn = new Integer(0);//number of resources i.e paths returned by the discovery
		//String[] resources_paths_mn = new String[100];
		//String[] resources_mn_full_path_mn = new String[100];
		 List<String> resources_paths_mn = new ArrayList<String>();
		 List<String> resources_paths_mn_security = new ArrayList<String>();
		 List<String> discovery = new ArrayList<String>();
		 List<String> resources_mn_full_path_mn = new ArrayList<String>();
		//the service_ae is created by niccolo, i dont create it, i just do a discovery of its resources
		//the argument in the discovery should be just the port of the middle node without the service_ae or with it?
		//perform the discovery
		discovery = adn.Discovery("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");//this is the port of the middle node?
		System.out.println(discovery.get(0));
		System.out.println(discovery.get(1));
		System.out.println(discovery.get(2));
		System.out.println(discovery.get(3));
		num_resources_mn = discovery.size();
		System.out.println(num_resources_mn);
		String pom;
		//exclude the actuators from the list returned by the discovery
		for (i = 0; i< num_resources_mn; i++) {
			if (discovery.get(i).contains("Sensor")) {
				pom = discovery.get(i);
				resources_paths_mn.add(pom);
				}
		} 
		System.out.println(resources_paths_mn.get(0));
		System.out.println(resources_paths_mn.get(1));
		num_resources_mn = resources_paths_mn.size();
		System.out.println(num_resources_mn);
		//find the number of sectors
		/*for (i = 0; i< num_resources_mn; i++) {
			if (i>0) {
				if ((f_paths.get(i).substring(0,7).equals(f_paths.get(i-1).substring(0, 7)))==false) {
					num++;
				}
			}
		}*/
		//Container control_app_cont = new Container();
		//control_app_cont = adn.createContainer("coap://127.0.0.1:5684/~/mn-cse/mn-name/Control_AE");
		//mi sottoscrivo sul coap monitor server per tutti i contenitori dal applicazione dei servizi(quelli che mi servono)
		//quelli che mi servono per laplication entity del control sono tutti aparte di contenitore per la camera e per il pir
		//quelli che mi servono per laplicazione del control sono i contenitori della camera e del pir
		//post per tutti i contenitori con il payload settato 
		
		List<String> target_temp = new ArrayList<String>();
		List<String> target_humid = new ArrayList<String>();
		List<String> target_light = new ArrayList<String>();
		List<String> target_soilmoist = new ArrayList<String>();



		String target;
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		for(i = 0;i < num_sectors; i++) {
			System.out.println("Enter the target temp for sector " + i);
			target = reader.next();
			target_temp.add(target);
			
			System.out.println("Enter the target humidity for sector " + i);
			target = reader.next();
			target_humid.add(target);
			
			System.out.println("Enter the target light for sector " + i);
			target = reader.next();
			target_light.add(target);
			
			System.out.println("Enter the target soil moisture for sector " + i);
			target = reader.next();
			target_soilmoist.add(target);
			
		}
		
		reader.close();
		for (i = 0; i< num_sectors ;i++) {
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE", "Sector"+ i);
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i, "TargetTemp");
			adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i + "/TargetTemp", target_temp.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i, "TargetHumid");
			adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i + "/TargetHumid", target_humid.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i, "TargetLight");
			adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i + "/TargetLight", target_light.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i, "TargetSoilMoist");
			adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Control_AE/Sector" + i + "/TargetSoilMoist", target_soilmoist.get(i));
		}
		//create the containers in the security application entity
		/*for (i = 0; i< num_sectors ;i++) {
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Security_AE", "Sector"+ i);
			adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Security_AE/Sector" + i, "MovementAlarmStatus");
			
		}*/
		
		//this for is not needed
		for(i = 0; i< num_resources_mn; i++) {
			//qui metto il nome della application entity su cui voglio fare la sottoscrizione?
			resources_mn_full_path_mn.add("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/" + resources_paths_mn.get(i));
			//resources_mn_full_path_mn[i] = "coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + resources_paths_mn[i];
		}
				
		//String monitor_port = "coap://127.0.0.1:5685/";
		Server_Class monitor = new Server_Class();
		
		//List <Resource> resources_monitor;
		for(i = 0; i< num_resources_mn; i++) {
			//first create the resource in the monitor
			//should the resource name be "monitor/" + resources_paths_mn[i] or just resources_paths_mn[i]?
			//monitor.addResource(resources_paths_mn[i]);	
			monitor.addResource(resources_paths_mn.get(i));
		}
		monitor.start();
		
		
		for(i = 0; i< num_resources_mn; i++) {
			System.out.println(resources_paths_mn.get(i));
		ctrl_app.createSubscription("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/" + resources_paths_mn.get(i),
				resources_paths_mn.get(i).replaceAll("/", "_"), "coap://127.0.0.1:5685/" + resources_paths_mn.get(i));
		}	
		int k = 0;
		
		//fare instanze dei threads
		//fare variabili globali
		//this should be put into an array which will have the same size as the number of sectors
		//int[] n_act_fans = new int[num_sectors];
		//int[] n_act_lamps = new int[num_sectors];
		//int[] n_act_irrig_humid = new int[num_sectors];
		//int[] n_act_irrig_soilmoist = new int[num_sectors];
		//int[] n_act_sprinklers = new int[num_sectors];
		//int[] n_act_alarms = new int[num_sectors];
		
		int n_act_sect1_fans = 0;
		int n_act_sect2_fans = 0;
		int n_act_sect3_fans = 0;
		int n_act_sect4_fans = 0;
		int n_act_sect5_fans = 0;
		
		int n_act_sect1_lamps = 0;
		int n_act_sect2_lamps = 0;
		int n_act_sect3_lamps = 0;
		int n_act_sect4_lamps = 0;
		int n_act_sect5_lamps = 0;
		//these below should be irrig_humid
		int n_act_sect1_irrig = 2;
		int n_act_sect2_irrig = 0;
		int n_act_sect3_irrig = 0;
		int n_act_sect4_irrig = 0;
		int n_act_sect5_irrig = 0;
		/*
		int n_act_sect1_irrig_soilmoist = 2;
		int n_act_sect2_irrig_soilmoist = 0;
		int n_act_sect3_irrig_soilmoist = 0;
		int n_act_sect4_irrig_soilmoist = 0;
		int n_act_sect5_irrig_soilmoist = 0;*/
		
		int n_act_sect1_sprinklers = 0;
		int n_act_sect2_sprinklers = 0;
		int n_act_sect3_sprinklers = 0;
		int n_act_sect4_sprinklers = 0;
		int n_act_sect5_sprinklers = 0;
		
		int n_act_sect1_alarms = 0;
		int n_act_sect2_alarms = 0;
		int n_act_sect3_alarms = 0;
		int n_act_sect4_alarms = 0;
		int n_act_sect5_alarms = 0;
		
		/*ThreadTemp Sector1_Temp = new ThreadTemp("Sector1/Temp");
		Sector1_Temp.run(Integer.parseInt(target_temp.get(0)), n_act_sect1_fans, Globals.Sector1_Temp, adn);
		ThreadTemp Sector2_Temp = new ThreadTemp("Sector2/Temp");
		ThreadTemp Sector3_Temp = new ThreadTemp("Sector3/Temp");
		ThreadTemp Sector4_Temp = new ThreadTemp("Sector4/Temp");
		ThreadTemp Sector5_Temp = new ThreadTemp("Sector5/Temp");*/
		
		ThreadTemp Sector1_Humid = new ThreadTemp("Sector1/Humid", Integer.parseInt(target_humid.get(0)), n_act_sect1_irrig, Globals.Sector1_Humid, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		Sector1_Humid.start();
		//ThreadTemp Sector2_Humid = new ThreadTemp("Sector2/Humid");
		//ThreadTemp Sector3_Humid = new ThreadTemp("Sector3/Humid");
		//ThreadTemp Sector4_Humid = new ThreadTemp("Sector4/Humid");
		//ThreadTemp Sector5_Humid = new ThreadTemp("Sector5/Humid");
		
		/*ThreadTemp Sector1_Light = new ThreadTemp("Sector1/Light");
		ThreadTemp Sector2_Light = new ThreadTemp("Sector2/Light");
		ThreadTemp Sector3_Light = new ThreadTemp("Sector3/Light");
		ThreadTemp Sector4_Light = new ThreadTemp("Sector4/Light");
		ThreadTemp Sector5_Light = new ThreadTemp("Sector5/Light");
		
		ThreadTemp Sector1_SoilMoist = new ThreadTemp("Sector1/SoilMoist");
		ThreadTemp Sector2_SoilMoist = new ThreadTemp("Sector2/SoilMoist");
		ThreadTemp Sector3_SoilMoist = new ThreadTemp("Sector3/SoilMoist");
		ThreadTemp Sector4_SoilMoist = new ThreadTemp("Sector4/SoilMoist");
		ThreadTemp Sector5_SoilMoist = new ThreadTemp("Sector5/SoilMoist");
		
		SecurityThread Sector1_Smoke = new ThreadTemp("Sector1/Smoke_Sensors", n_act_sect1_sprinklers, Globals Sector1_Smoke, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector2_Smoke = new ThreadTemp("Sector2/Smoke_Sensors", n_act_sect2_sprinklers, Globals Sector2_Smoke, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector3_Smoke = new ThreadTemp("Sector3/Smoke_Sensors", n_act_sect3_sprinklers, Globals Sector3_Smoke, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector4_Smoke = new ThreadTemp("Sector4/Smoke_Sensors", n_act_sect4_sprinklers, Globals Sector4_Smoke, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector5_Smoke = new ThreadTemp("Sector5/Smoke_Sensors", n_act_sect5_sprinklers, Globals Sector5_Smoke, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		
		SecurityThread Sector1_PIR = new ThreadTemp("Sector1/PIR", n_act_sect1_alarms, Globals Sector1_PIR, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector2_PIR = new ThreadTemp("Sector2/PIR", n_act_sect2_alarms, Globals Sector2_PIR, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector3_PIR = new ThreadTemp("Sector3/PIR", n_act_sect3_alarms, Globals Sector3_PIR, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector4_PIR = new ThreadTemp("Sector4/PIR", n_act_sect4_alarms, Globals Sector4_PIR, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		SecurityThread Sector5_PIR = new ThreadTemp("Sector5/PIR", n_act_sect5_alarms, Globals Sector5_PIR, adn, "coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE");
		
		*/
		
		while(true) {
		adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Sensor/Humid/Sensor0", Integer.toString(k));
		k++;
		//adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor/Temp/Sensor1", "8");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		
		
    }
}
