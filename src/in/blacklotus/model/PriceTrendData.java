package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import in.blacklotus.utils.Utils;

public class PriceTrendData {

	private Double value;
	
	private Double low20;
	
	private Date lowDate;
	
	private Double high20;
	
	private Date highDate;
	
	private long volume;

	private long timestamp;
	
	private Double priceDiff;
	
	private Double priceDiffPercent;
	
	private Long volumeDiff;
	
	private Double volumeDiffPercentage;
	
	private int priceRank;
	
	private int volumeRank;
	
	private int dayRank;

	private SimpleDateFormat sdf;

	public PriceTrendData(Double value, Double low20, Date lowDate, Double high20, Date highDate, long timestamp, long volume) {

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
	
	public Double getPriceDiff() {
		return priceDiff;
	}

	public void setPriceDiff(Double priceDiff) {
		this.priceDiff = priceDiff;
	}

	public Double getPriceDiffPercent() {
		return priceDiffPercent;
	}

	public void setPriceDiffPercent(Double priceDiffPercent) {
		this.priceDiffPercent = priceDiffPercent;
	}

	public Long getVolumeDiff() {
		return volumeDiff;
	}

	public void setVolumeDiff(Long volumeDiff) {
		this.volumeDiff = volumeDiff;
	}

	public Double getVolumeDiffPercentage() {
		return volumeDiffPercentage;
	}

	public void setVolumeDiffPercentage(Double volumeDiffPercentage) {
		this.volumeDiffPercentage = volumeDiffPercentage;
	}

	public int getPriceRank() {
		return priceRank;
	}

	public void setPriceRank(int priceRank) {
		this.priceRank = priceRank;
	}

	public int getVolumeRank() {
		return volumeRank;
	}

	public void setVolumeRank(int volumeRank) {
		this.volumeRank = volumeRank;
	}

	public int getDayRank() {
		return dayRank;
	}

	public void setDayRank(int dayRank) {
		this.dayRank = dayRank;
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
	
	public String toPrintablePriceChange() {

		if (priceDiff == Double.MIN_VALUE) {

			return "-";

		} else {

			return String.format("%.2f", priceDiff);
		}
	}
	
	public String toPrintablePriceChangePercent() {

		if (priceDiffPercent == Double.MIN_VALUE) {

			return "-";

		} else {

			return String.format("%%%.2f", priceDiffPercent);
		}
	}
	
	public String toPrintableVolumeChange() {

		if (volumeDiff == Long.MIN_VALUE) {

			return "-";

		} else {

			return Utils.formattedVolume(volumeDiff);
		}
	}
	
	public String toPrintableVolumeChangePercent() {

		if (volumeDiffPercentage == Double.MIN_VALUE) {

			return "-";

		} else {

			return String.format("%%%.2f", volumeDiffPercentage);
		}
	}

	private double round(double value) {
		
		return Math.round(value * 100.0) / 100.0;
	}

	@Override
	public String toString() {

		return new Gson().toJson(this);
	}
}
