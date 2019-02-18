package ctrl_in;

import java.util.ArrayList;

public class Sector {
	public ArrayList<String> temp_sens;
	public ArrayList<String> humid_sens;
	public ArrayList<String> light_sens;
	public ArrayList<String> soilmoist_sens;
	public ArrayList<String> smoke_sens;
	public ArrayList<String> pir_sens;
	public ArrayList<String> cam_sens;
	public String sectorName;
	public int targetTemp;
	public int targetHumidity;
	public int targetLight;
	public int targetSoil;
	
	public Sector(String sectorName) {
		this.sectorName = sectorName;
		temp_sens = new ArrayList<String>();
		humid_sens = new ArrayList<String>();
		light_sens = new ArrayList<String>();
		soilmoist_sens = new ArrayList<String>();
		smoke_sens = new ArrayList<String>();
		pir_sens = new ArrayList<String>();
		cam_sens = new ArrayList<String>();
		
		
	}
}
