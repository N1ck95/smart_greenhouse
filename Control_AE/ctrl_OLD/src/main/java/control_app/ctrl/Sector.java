package control_app.ctrl;

import java.util.ArrayList;

public class Sector {
	public ArrayList<String> fans;
	public ArrayList<String> irrigators_humid;
	public ArrayList<String> irrigators_soilm;
	public ArrayList<String> lamps;
	public ArrayList<String> sprinklers;
	public ArrayList<String> alarms;
	public String sectorName;
	public int targetTemp;
	public int targetHumidity;
	public int targetLight;
	public int targetSoil;
	
	public Sector(String sectorName) {
		this.sectorName = sectorName;
		fans = new ArrayList<String>();
		irrigators_humid = new ArrayList<String>();
		irrigators_soilm = new ArrayList<String>();
		lamps = new ArrayList<String>();
		sprinklers = new ArrayList<String>();
		alarms = new ArrayList<String>();
		
		
	}
}
