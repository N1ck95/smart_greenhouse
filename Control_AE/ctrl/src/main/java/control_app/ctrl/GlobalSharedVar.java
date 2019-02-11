package control_app.ctrl;

public class GlobalSharedVar {
	
	public String name;
	public static SharedVar s;
	
	public GlobalSharedVar(String name) {
		this.s = new SharedVar();
		this.name = name;
	} 
	/*
	public static SharedVar Sector1_Temp;
	public static SharedVar Sector2_Temp;
	public static SharedVar Sector3_Temp;
	public static SharedVar Sector4_Temp;
	public static SharedVar Sector5_Temp;
	
	public static SharedVar Sector1_Humid;
	public static SharedVar Sector2_Humid;
	public static SharedVar Sector3_Humid;
	public static SharedVar Sector4_Humid;
	public static SharedVar Sector5_Humid;
*/

}
