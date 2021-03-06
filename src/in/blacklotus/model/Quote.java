package in.blacklotus.model;

public class Quote {

	private Double[] low;
	
	private Double[] high;
	
	private Double[] open;
	
	private Double[] close;
	
	private Long[] volume;
	
	public Quote() {
	}

	public Double[] getLow() {
		return low;
	}

	public void setLow(Double[] low) {
		this.low = low;
	}

	public Double[] getHigh() {
		return high;
	}

	public void setHigh(Double[] high) {
		this.high = high;
	}

	public Double[] getOpen() {
		return open;
	}

	public void setOpen(Double[] open) {
		this.open = open;
	}

	public Double[] getClose() {
		return close;
	}

	public void setClose(Double[] close) {
		this.close = close;
	}

	public Long[] getVolume() {
		return volume;
	}

	public void setVolume(Long[] volume) {
		this.volume = volume;
	}	
}
