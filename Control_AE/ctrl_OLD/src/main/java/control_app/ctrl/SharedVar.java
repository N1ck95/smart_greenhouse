package control_app.ctrl;

public class SharedVar {
	
	public int val;
	public Boolean state = false;
	
	public SharedVar() {
		val = 0; 
		state = false;
	}
	
	public synchronized void set(int n) {
		try {
			while (state) {
	        wait();
	        }
	    } catch (InterruptedException ix) {
	    	ix.printStackTrace();
	      }
	    val = n;
	    state = true;
	    notifyAll();
	   }
	public synchronized int get() {
	    try {
	      while (!state) {
	      wait();
	      }
	    } catch (InterruptedException ix) {
	    	ix.printStackTrace();
	      }
	    state = false;
	    notifyAll();
	    return val;
	}
}