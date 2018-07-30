package in.blacklotus.model;

public class Quote {

	private double[] low;
	
	private double[] high;
	
	private double[] open;
	
	private double[] close;
	
	public Quote() {
	}

	public double[] getLow() {
		return low;
	}

	public void setLow(double[] low) {
		this.low = low;
	}

	public double[] getHigh() {
		return high;
	}

	public void setHigh(double[] high) {
		this.high = high;
	}

	public double[] getOpen() {
		return open;
	}

	public void setOpen(double[] open) {
		this.open = open;
	}

	public double[] getClose() {
		return close;
	}

	public void setClose(double[] close) {
		this.close = close;
	}	
}
