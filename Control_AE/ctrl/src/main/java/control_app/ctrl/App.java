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
		AE ae = adn.createAE_Control("coap://127.0.0.1:5684/~/Natalia-mn-cse", "ControlApp-ID", "Control_AE");
		
		
		Integer i = new Integer(0);
		Integer num_sectors = new Integer(4);
		Integer num_resources_mn = new Integer(0);
		String[] resources_paths_mn = new String[100];
		String[] resources_mn_full_path_mn = new String[100];
		//the service_ae is created by niccolo, i dont create it, i just do a discovery of its resources
		//the argument in the discovery should be just the port of the middle node without the service_ae or with it?
		resources_paths_mn = adn.Discovery("coap://127.0.0.1:5684//~/Natalia-mn-cse/Service_AE");//this is the port of the middle node?
		num_resources_mn = resources_paths_mn.length;
		
		
		//Container control_app_cont = new Container();
		//control_app_cont = adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE");
		//mi sottoscrivo sul coap monitor server per tutti i contenitori dal applicazione dei servizi(quelli che mi servono)
		//quelli che mi servono per laplication entity del control sono tutti aparte di contenitore per la camera e per il pir
		//quelli che mi servono per laplicazione del control sono i contenitori della camera e del pir
		//post per tutti i contenitori con il payload settato 
		
		List<String> target_temp = new ArrayList<String>();
		List<String> target_humid = new ArrayList<String>();
		List<String> target_light = new ArrayList<String>();
		List<String> target_soilmoist = new ArrayList<String>();
		for (i = 0; i< num_sectors ;i++) {
			adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE", "Sector"+ i);
			adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i, "TargetTemp");
			adn.createContentInstance("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i + "/TargetTemp", target_temp.get(i));
			adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i, "TargetHum");
			adn.createContentInstance("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i + "/TargetHumid", target_humid.get(i));
			adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i, "TargetLight");
			adn.createContentInstance("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i + "/TargetLight", target_light.get(i));
			adn.createContainer("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i, "TargetSoilMoist");
			adn.createContentInstance("coap://127.0.0.1:5684/~/Natalia-mn-cse/Natalia-mn-name/Control_AE/Sector" + i + "/TargetSoilMoist", target_soilmoist.get(i));
		}
		
		for(i = 0; i< num_resources_mn; i++) {
			resources_mn_full_path_mn[i] = "coap://127.0.0.1:5683/~/Natalia-mn-cse/Natalia-mn-name/Service_AE/" + resources_paths_mn[i];
		}
				
		//String monitor_port = "coap://127.0.0.1:5685/";
		Server_Class monitor = new Server_Class();
		
		//List <Resource> resources_monitor;
		for(i = 0; i< num_resources_mn; i++) {
			//first create the resource in the monitor
			//should the resource name be "monitor/" + resources_paths_mn[i] or just resources_paths_mn[i]?
			monitor.addResource(resources_paths_mn[i]);	
		}
		monitor.start();
		for(i = 0; i< num_resources_mn; i++) {
		ctrl_app.createSubscription("coap://127.0.0.1:5683/Natalia-mn-cse/Natalia-mn-name/Service_AE/" + resources_paths_mn[i],
				resources_paths_mn[i], "coap://127.0.0.1:5685/" + resources_paths_mn[i]);
		}	
		
		//fare instanze dei threads
		//fare variabili globali
		int n_act_sect1_fans;
		int n_act_sect2_fans;
		int n_act_sect3_fans;
		int n_act_sect4_fans;
		int n_act_sect5_fans;
		
		int n_act_sect1_lamps;
		int n_act_sect2_lamps;
		int n_act_sect3_lamps;
		int n_act_sect4_lamps;
		int n_act_sect5_lamps;
		
		ThreadTemp Sector1_Temp = new ThreadTemp("Sector1/Temp");
		Sector1_Temp.run(Integer.parseInt(target_temp.get(0)), n_act_sect1_fans, Globals.Sector1_Temp, adn);
		ThreadTemp Sector2_Temp = new ThreadTemp("Sector2/Temp");
		ThreadTemp Sector3_Temp = new ThreadTemp("Sector3/Temp");
		ThreadTemp Sector4_Temp = new ThreadTemp("Sector4/Temp");
		ThreadTemp Sector5_Temp = new ThreadTemp("Sector5/Temp");
		
		ThreadTemp Sector1_Humid = new ThreadTemp("Sector1/Humid");
		ThreadTemp Sector2_Humid = new ThreadTemp("Sector2/Humid");
		ThreadTemp Sector3_Humid = new ThreadTemp("Sector3/Humid");
		ThreadTemp Sector4_Humid = new ThreadTemp("Sector4/Humid");
		ThreadTemp Sector5_Humid = new ThreadTemp("Sector5/Humid");
		
		ThreadTemp Sector1_Light = new ThreadTemp("Sector1/Light");
		ThreadTemp Sector2_Light = new ThreadTemp("Sector2/Light");
		ThreadTemp Sector3_Light = new ThreadTemp("Sector3/Light");
		ThreadTemp Sector4_Light = new ThreadTemp("Sector4/Light");
		ThreadTemp Sector5_Light = new ThreadTemp("Sector5/Light");
		
		ThreadTemp Sector1_SoilMoist = new ThreadTemp("Sector1/SoilMoist");
		ThreadTemp Sector2_SoilMoist = new ThreadTemp("Sector2/SoilMoist");
		ThreadTemp Sector3_SoilMoist = new ThreadTemp("Sector3/SoilMoist");
		ThreadTemp Sector4_SoilMoist = new ThreadTemp("Sector4/SoilMoist");
		ThreadTemp Sector5_SoilMoist = new ThreadTemp("Sector5/SoilMoist");
		
		
		
    }
}
