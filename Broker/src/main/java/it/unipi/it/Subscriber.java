package it.unipi.it;

public class Subscriber {
	private String address;
	private Integer observe;
	
	public Subscriber(String addr, Integer obs) {
		setAddress(addr);
		setObserve(obs);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getObserve() {
		return observe;
	}

	public void setObserve(Integer observe) {
		this.observe = observe;
	}
}
