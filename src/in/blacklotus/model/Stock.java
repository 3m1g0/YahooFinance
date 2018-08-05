package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stock {

	private String currency;

	private String name;

	private String symbol;

	private double now;

	private double low;

	private double high;

	private double nowPercent;

	private double lowPercent;

	private double highPercent;

	private Date highDate;

	private Date lowDate;

	private double differ;

	private double move;

	private SimpleDateFormat sdf;

	public Stock() {

		String pattern = "MM/dd/yyyy";

		sdf = new SimpleDateFormat(pattern);
	}

	public void calculateHighPercenttage() {
		this.highPercent = (this.now - this.high) / this.high * 100;
	}

	public void calculateLowPercenttage() {
		this.lowPercent = (this.now - this.low) / this.low * 100;
	}

	public void calculateDiffer() {
		this.differ = (this.high - this.now) / this.high * 100;
	}

	public void calculateMove() {
		this.move = (this.high - this.low) / this.now * 100;
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

	public double getNow() {
		return now;
	}

	public void setNow(double close) {
		this.now = close;
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

	public double getNowPercent() {
		return nowPercent;
	}

	public void setNowPercent(double nowPercent) {
		this.nowPercent = nowPercent;
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

	public double getDiffer() {
		return differ;
	}

	public void setDiffer(double differ) {
		this.differ = differ;
	}

	public double getMove() {
		return move;
	}

	public void setMove(double move) {
		this.move = move;
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
	
	public String toPrintableString() {

		return String.format("%-6s,%-6s,%-6s,%-6s,%-6s,%-6s,%-6s,%-6s,%-6s,%-6s,%-6s", this.symbol,
				round(this.now), round(this.low), round(this.high), round(this.nowPercent), round(this.lowPercent),
				round(this.highPercent), sdf.format(this.lowDate), sdf.format(this.highDate), round(this.move),
				round(this.differ));

	}
	
	@Override
	public String toString() {

		return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s,%.2f,%.2f", this.symbol,
				round(this.now), round(this.low), round(this.high), round(this.nowPercent), round(this.lowPercent),
				round(this.highPercent), sdf.format(this.lowDate), sdf.format(this.highDate), round(this.move),
				round(this.differ));

	}

}
