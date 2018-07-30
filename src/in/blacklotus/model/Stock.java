package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stock {

	private String currency;

	private String name;

	private String symbol;

	private double close;

	private double low;

	private double high;

	private double lowPercent;

	private double highPercent;

	private Date highDate;

	private Date lowDate;

	private SimpleDateFormat sdf;

	public Stock() {

		String pattern = "dd/MM/yyyy";

		sdf = new SimpleDateFormat(pattern);
	}

	public void calculateHighPercenttage() {
		this.highPercent = (this.high - this.close) / this.high * 100;
	}

	public void calculateLowPercenttage() {
		this.lowPercent = (this.low - this.close) / this.low * 100;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLowPercent() {
		return lowPercent;
	}

	public void setLowPercent(double lowPercent) {
		this.lowPercent = lowPercent;
	}

	public double getHighPercent() {
		return highPercent;
	}

	public void setHighPercent(double highPercent) {
		this.highPercent = highPercent;
	}

	public Date getHighDate() {
		return highDate;
	}

	public void setHighDate(Date highDate) {
		this.highDate = highDate;
	}

	public Date getLowDate() {
		return lowDate;
	}

	public void setLowDate(Date lowDate) {
		this.lowDate = lowDate;
	}

	private double round(double value) {

		return Math.round(value * 100.0) / 100.0;
	}

	@Override
	public String toString() {

		return this.name + "," + this.symbol + "," + this.currency + "," + round(this.close) + "," + round(this.high)
				+ "," + round(this.highPercent) + "," + sdf.format(this.highDate) + "," + round(this.low) + ","
				+ round(this.lowPercent) + "," + sdf.format(this.lowDate);
	}

}
