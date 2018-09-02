package in.blacklotus.model;

public class Symbol {

	private String name;

	private String price;

	private String delta;
	
	private String now;

	public Symbol() {
	}
	
	public Symbol(String name) {
		
		this.name = name;
	}

	public Symbol(String name, String price, String delta) {
		
		this.name = name;
		
		this.price = price;
		
		this.delta = delta;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getDelta() {
		return delta;
	}

	public void setDelta(String delta) {
		this.delta = delta;
	}
	
	public String getNow() {
		return now;
	}

	public void setNow(double now) {
		this.now = String.valueOf(now);
	}
	
	public String toPrintableString(int slNo) {
		return String.format("%d,%s is the current price of %s with target price: %s and delta: %s", slNo, now, name, price, delta);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
