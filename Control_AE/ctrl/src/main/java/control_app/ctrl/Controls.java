package control_app.ctrl;

import java.util.ArrayList;

public class Controls {
	
	//Returns the list of actuations to perform on each actuator
	public static ArrayList<CI> temperatureControl(int val, int targetValue, ArrayList<String> actuators) {
		//TODO: insert temperature control
		//body of thread
		int k = 0;
		int m = 0;
		int j = 0;
		int difference = 0;
		int meta = actuators.size()/2;
		System.out.println("meta is "+ meta);
		System.out.println("target value is "+ targetValue);
		m = meta;
		ArrayList<CI> turn_on_act = new ArrayList<CI>();
		difference = val - targetValue;
		System.out.println("[INFO] Difference is " + difference);
		if (val>targetValue) {
			if (difference > 0) {
				if (k < meta) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(k),"1"));
					k++;
				}
			}
			if (difference > 10) {
				if (k < meta) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(k),"1"));
					k++;
				}
			}
			if (difference > 20) {
				if (k < meta) {
					for(j = k; j<meta; j++) {
						turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(j),"1"));
					}
				}
			}
		}else
		if (val<targetValue) {
			if (difference < 0) {
				if (m < actuators.size()) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(m),"1"));
					m++;
				}
			}
			if (difference < -10) {
				if (m < actuators.size()) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(m),"1"));
					m++;
				}
			}
			if (difference < -20) {
				if (m < actuators.size()) {
					for(j = m; j<actuators.size(); j++) {
						turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(j),"1"));
					}
				}
			}
		}
		//turn off all the actuators when the desired value is reached
		if (val == targetValue) {
			for (j = 0;j<actuators.size();j++) {
				turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(j),"0"));
			}
		}
		return turn_on_act;
	}
	
	public static ArrayList<CI> humidityControl(int val, int targetValue, ArrayList<String> actuators) {
		return Controls.temperatureControl(val, targetValue, actuators);
	}
	public static ArrayList<CI> soilmControl(int val, int targetValue, ArrayList<String> actuators) {
		return Controls.temperatureControl(val, targetValue, actuators);
	}
	public static ArrayList<CI> lightControl(int val, int targetValue, ArrayList<String> actuators) {
		
		int k = 0;
		int j = 0;
		int difference = 0;
		
		ArrayList<CI> turn_on_act = new ArrayList<CI>();
		difference = val - targetValue;
		System.out.println("[INFO] Difference is " + difference);
		if (val>targetValue) {
			System.out.println("[INFO] Can control only low light conditions");
		}else
		if (val<targetValue) {
			if (difference < 0) {
				if (k < actuators.size()) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(k),"1"));
					k++;
				}
			}
			if (difference < -10) {
				if (k < actuators.size()) {
					turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(k),"1"));
					k++;
				}
			}
			if (difference < -20) {
				if (k < actuators.size()) {
					for(j = k; j<actuators.size(); j++) {
						turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(j),"1"));
					}
				}
			}
		}
		//turn off all the actuators when the desired value is reached
		if (val == targetValue) {
			for (j = 0;j<actuators.size();j++) {
				turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(j),"0"));
			}
		}
		return turn_on_act;
	}
	public static ArrayList<CI> smokeControl(int val, int targetValue, ArrayList<String> actuators) {
		//if there is a fire, turn on all the sprinklers regardless of the fire so that we dont risk expansion of the fire
		int i = 0;
		ArrayList<CI> turn_on_act = new ArrayList<CI>();
		if (val == 1) {
			for (i = 0; i< actuators.size(); i++) {
				turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(i),"1"));
			}
		} else //if there is no smoke i.e no fire anymore
		if (val == 0) {
			turn_on_act.add(new CI("coap://127.0.0.1:5683/~/mn-cse/mn-name/Service_AE/" + actuators.get(i),"0"));
		}
		return turn_on_act;
	}
	public static ArrayList<CI> movementControl(int val, int targetValue, ArrayList<String> actuators) {
		
		
		return Controls.smokeControl(val, targetValue, actuators);
	}
}
