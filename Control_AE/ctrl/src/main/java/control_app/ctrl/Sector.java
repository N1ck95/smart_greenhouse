package control_app.ctrl;

import java.util.ArrayList;

public class Sector {
	public ArrayList<String> fans;
	public ArrayList<String> irrigators;
	public ArrayList<String> lamps;
	public ArrayList<String> sprinklers;
	public ArrayList<String> alarms;
	public String sectorName;
	
	public Sector(String sectorName) {
		this.sectorName = sectorName;
		fans = new ArrayList<String>();
		irrigators = new ArrayList<String>();
		lamps = new ArrayList<String>();
		sprinklers = new ArrayList<String>();
		alarms = new ArrayList<String>();
	}
}
