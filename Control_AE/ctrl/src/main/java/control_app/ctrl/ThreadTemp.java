package control_app.ctrl;

import java.util.ArrayList;
import java.util.List;

public class ThreadTemp extends Thread{
	
	String name;
	int[] values;
	private int array_len = 5;
	
	
	
	public  ThreadTemp(String name) {
		this.name = name;
		values = new int[array_len];
		int i;
		for(i = 0; i<array_len;i++) {
			values[i] = 0;
		}
		
	        
	}
	
	public void run(int target, int num_actuators, GlobalSharedVar global, AE_Control adn) {
   	 	//body of thread
		int i = 0;//do i need to make i static so that it keeps its value on the next round of execution of the thread
		int j = 0;
		int k = 0;
		int difference = 0;
		int media = 0;
		String path_act = "";
		String sector;
		String measure;
		int index;
		index = this.name.indexOf("Sector");
		sector = this.name.substring(index, index+6);//+6 so that we get the word sector but also the number of the sect
		index = this.name.indexOf('/');//since in the thread name we only have one '/', we know that after it
		//follows the name of the measure i.e temp,humid,light,...
		measure = this.name.substring(index+1);
		List<String> actuators = new ArrayList<String>();
		while (true) {
		
			values[i%array_len] = global.s.get();//GlobalSharedVar.s.get();
			i++;
		
		
			for(j = 0; j<num_actuators; j++) {
				if (measure.equals("Temp")) {
					path_act = sector + "/" + "Actuators" + "/" + "Fans" + "/" + "Fan" + j;
				}
				if (measure.equals("Light")) {
					path_act = sector + "/" + "Actuators" + "/" + "Lamps" + "/" + "Lamp" + j;
				}
				if (measure.equals("Humid")) {
					path_act = sector + "/" + "Actuators" + "/" + "Sprinklers" + "/" + "Sprinkler" + j;
				}//are the sprinklers the right actuators for the humidity?
				if (measure.equals("SoilMoist")) {
					path_act = sector + "/" + "Actuators" + "/" + "Irrigators" + "/" + "Irrigator" + j;
				}//are the irrigators the right actuators for the soilmoist?
				
				actuators.add(path_act);
			}
			for(j = 0; j<values.length; j++) {
				media += values[j];
			}
			media /= values.length;
			int increment = 0;
			k = 0;
			difference = media - target;
			if (media>target) {
				if (difference > 5) {
					//turn on the appropriate actuator(turn on one actuator)
					//if (actuators.get(k) != null)
					//post from actuators.get(k)
					adn.createContentInstance(actuators.get(k), "1");
					k++;
				}
				if (difference > 10) {
					//turn on the the second actuator
					//post from actuators.get(k)
					adn.createContentInstance(actuators.get(k), "1");
					k++;
				}
				if (difference > 20) {
					//turn on all the other actuators
					adn.createContentInstance(actuators.get(k), "1");
					k++;
				}
				
					
			}
		
			while (media > target) {
			
				//post to the appropriate actuator 
				//increment ++;
				//the control is on/off right?
			
			}
			//turn off the actuators
			//post for every actuator.get(i)
			for(j = 0; j<num_actuators; j++) {
				adn.createContentInstance(actuators.get(j), "0");
			}
		
		}
		
   }

}
