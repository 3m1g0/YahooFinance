package in.blacklotus.model;

public class Symbol {

	private String name;

	private String price;

	private String delta;

	private double now;

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

	public double getNow() {
		return now;
	}

	public void setNow(double now) {
		this.now = now;
	}

	public String toPrintableString(int slNo) {
		return String.format(
				"%d,[$%.2f] of [%s] is around target price: $%s & target percent: %s%%",
				slNo, now, name, price, delta);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
