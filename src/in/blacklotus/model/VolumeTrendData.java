package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import in.blacklotus.utils.Utils;

public class VolumeTrendData {

	private Double value;

	private Double low10;

	private Double low20;

	private Date low10Date;

	private Date low20Date;

	private Double high10;

	private Double high20;

	private Date high10Date;

	private Date high20Date;

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

	public VolumeTrendData(Double value, Double low10, Double low20, Date low10Date, Date low20Date, Double high10,
			Double high20, Date high10Date, Date high20Date, long timestamp, long volume) {

		this.value = value;

		this.low10 = low10;

		this.low20 = low20;

		this.low10Date = low10Date;

		this.low20Date = low20Date;

		this.high10 = high10;

		this.high20 = high20;

		this.high10Date = high10Date;

		this.high20Date = high20Date;

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

	public Double getLow10() {
		return low10;
	}

	public void setLow10(Double low10) {
		this.low10 = low10;
	}

	public Double getLow20() {
		return low20;
	}

	public void setLow20(Double low20) {
		this.low20 = low20;
	}

	public Date getLow10Date() {
		return low10Date;
	}

	public void setLow10Date(Date low10Date) {
		this.low10Date = low10Date;
	}

	public Date getLow20Date() {
		return low20Date;
	}

	public void setLow20Date(Date low20Date) {
		this.low20Date = low20Date;
	}

	public Double getHigh10() {
		return high10;
	}

	public void setHigh10(Double high10) {
		this.high10 = high10;
	}

	public Double getHigh20() {
		return high20;
	}

	public void setHigh20(Double high20) {
		this.high20 = high20;
	}

	public Date getHigh10Date() {
		return high10Date;
	}

	public void setHigh10Date(Date high10Date) {
		this.high10Date = high10Date;
	}

	public Date getHigh20Date() {
		return high20Date;
	}

	public void setHigh20Date(Date high20Date) {
		this.high20Date = high20Date;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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

	public int getVolumeRank() {
		return volumeRank;
	}

	public void setVolumeRank(int rank) {
		this.volumeRank = rank;
	}
	
	public int getPriceRank() {
		return priceRank;
	}

	public void setPriceRank(int priceRank) {
		this.priceRank = priceRank;
	}

	public String toPrintableString() {

		return String.format("%s: %s", sdf.format(new Date(this.timestamp)), round(this.value));
	}

	public String toPrintableLow(int index) {

		if (index == 0) {

			return String.format("%s: %s", sdf.format(this.low10Date), round(this.low10));

		} else if (index == 1) {

			return String.format("%s: %s", sdf.format(this.low20Date), round(this.low20));

		} else {

			return " ";
		}
	}

	public String toPrintableHigh(int index) {

		if (index == 0) {

			return String.format("%s: %s", sdf.format(this.high10Date), round(this.high10));

		} else if (index == 1) {

			return String.format("%s: %s", sdf.format(this.high20Date), round(this.high20));

		} else {

			return " ";
		}
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

			return String.format("%.2f%%", priceDiffPercent);
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

			return String.format("V: %.2f%%", volumeDiffPercentage);
		}
	}

	private double round(double value) {

		return Math.round(value * 100.0) / 100.0;
	}

	@Override
	public String toString() {

		return new Gson().toJson(this);
	}

	public int getDayRank() {
		return dayRank;
	}

	public void setDayRank(int dayRank) {
		this.dayRank = dayRank;
	}
}
