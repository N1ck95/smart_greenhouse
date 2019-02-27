package control_app.ctrl;

import java.util.ArrayList;
import java.util.List;

public class SecurityThread extends Thread{
	

	String name;
	GlobalSharedVar global; 
	AE_Control adn;
	String path_ae;
	int num_actuators;
	
	
	public  SecurityThread(String name, int num_actuators, GlobalSharedVar global, AE_Control adn, String path_ae) {
		this.name = name;
		this.global = global;
		this.adn = adn;
		this.path_ae = path_ae;
		this.num_actuators = num_actuators;
	

	}
	
	public void run() {
		String path_act = "";
		String sector;
		String measure;
		int index;
		int shared;
		int j = 0;
		index = this.name.indexOf("Sector");
		sector = this.name.substring(index, index+7);//+6 so that we get the word sector but also the number of the sect
		index = this.name.indexOf('/');//since in the thread name we only have one '/', we know that after it
		//follows the name of the measure i.e temp,humid,light,...
		measure = this.name.substring(index+1);
		List<String> actuators = new ArrayList<String>();
		for(j = 0; j<num_actuators; j++) {
			if (measure.equals("Smoke_Sensors")) {
				path_act = path_ae + "/" + sector + "/" + "Actuators" + "/" + "Sprinklers" + "/" + "Sprinkler" + j;
			}
			if (measure.equals("PIR")) {
				path_act = path_ae + "/" + sector + "/" + "Actuators" + "/" + "Alarms" + "/" + "Alarm" + j;
			}
			 
			actuators.add(path_act);
		}
		
		System.out.println(actuators.get(0));
		System.out.println(actuators.get(1));
		while (true) {
			shared = global.s.get();//GlobalSharedVar.s.get();
			if (shared == 1) {
				if (measure.equals("PIR")) {
					adn.createContentInstance(path_ae + "/" + sector + "/" + "MovementAlarmStatus", "1");
				}
				for(j = 0; j<num_actuators; j++) {
					adn.createContentInstance(actuators.get(j), "1");
				}
			}
					
			if (shared == 0) {
				if (measure.equals("PIR")) {
					adn.createContentInstance(path_ae + "/" + sector + "/" + "MovementAlarmStatus", "0");
				}
				for(j = 0; j<num_actuators; j++) {
					adn.createContentInstance(actuators.get(j), "0");
				}
			}	
		}
				
	}
}
