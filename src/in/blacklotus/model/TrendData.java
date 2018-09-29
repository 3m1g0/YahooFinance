package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrendData {

	private Double value;
	
	private Double low20;
	
	private Date lowDate;
	
	private Double high20;
	
	private Date highDate;
	
	private long volume;

	private long timestamp;

	private SimpleDateFormat sdf;

	public TrendData(Double value, Double low20, Date lowDate, Double high20, Date highDate, long timestamp, long volume) {

		this.value = value;
		
		this.low20 = low20;
		
		this.lowDate = lowDate;
		
		this.high20 = high20;
		
		this.highDate = highDate;
		
		this.volume = volume;

		this.timestamp = timestamp;

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getLow20() {
		return low20;
	}

	public void setLow20(Double low20) {
		this.low20 = low20;
	}
	
	public Date getLowDate() {
		return lowDate;
	}

	public void setLowDate(Date lowDate) {
		this.lowDate = lowDate;
	}

	public Double getHigh20() {
		return high20;
	}

	public void setHigh20(Double high20) {
		this.high20 = high20;
	}

	public Date getHighDate() {
		return highDate;
	}

	public void setHighDate(Date highDate) {
		this.highDate = highDate;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public String toPrintableString() {

		return String.format("%s: %s", sdf.format(new Date(this.timestamp)), round(this.value));
	}
	
	public String toPrintableLow() {

		return String.format("%s: %s", sdf.format(this.lowDate), round(this.low20));
	}
	
	public String toPrintableHigh() {

		return String.format("%s: %s", sdf.format(this.highDate), round(this.high20));
	}

	private double round(double value) {
		
		return Math.round(value * 100.0) / 100.0;
	}

}
