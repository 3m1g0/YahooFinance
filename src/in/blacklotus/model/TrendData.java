package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrendData {

	private Double value;
	
	private Double volume;

	private long timestamp;

	private SimpleDateFormat sdf;

	public TrendData(Double value, long timestamp, Double volume) {

		this.value = value;
		
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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public String toPrintableString() {

		return String.format("%s: %s", sdf.format(new Date(this.timestamp)), round(this.value));
	}

	private double round(double value) {
		
		return Math.round(value * 100.0) / 100.0;
	}

}
