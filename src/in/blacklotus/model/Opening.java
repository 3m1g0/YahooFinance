package in.blacklotus.model;

public class Opening extends Stock {

	private double gainClose;
	
	private double gainClosePercent;
	
	private double gainHigh;
	
	private double gainHighPercent;
	
	private double volumeChangePercent2;

	public Opening(String stockName) {
		super(stockName);
	}

	public Opening() {
	}

	public double getGainClose() {
		return gainClose;
	}

	public void setGainClose(double gainClose) {
		this.gainClose = gainClose;
	}

	public double getGainClosePercent() {
		return gainClosePercent;
	}

	public void setGainClosePercent(double gainClosePercent) {
		this.gainClosePercent = gainClosePercent;
	}

	public double getGainHigh() {
		return gainHigh;
	}

	public void setGainHigh(double gainHigh) {
		this.gainHigh = gainHigh;
	}

	public double getGainHighPercent() {
		return gainHighPercent;
	}

	public void setGainHighPercent(double gainHighPercent) {
		this.gainHighPercent = gainHighPercent;
	}

	public double getVolumeChangePercent2() {
		return volumeChangePercent2;
	}

	public void setVolumeChangePercent2(double columeChangePercent2) {
		this.volumeChangePercent2 = columeChangePercent2;
	}
}
