package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stock {

	public static final String[] KEYS = { "NOW", "LOW20", "HIGH20", "%LOW20", "%HIGH20", "%TODAY", "%MOVE", "%DIFFER" };

	private static final String[] OPERATORS = { "<=", "<", ">=", ">", "==", "!=" };

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

	private String getPrintableData(double value) {
		if (value == -9999) {
			return "-";
		} else if (value > 0) {
			return "+" + value + "%";
		} else {
			return value + "%";
		}
	}
	
	public boolean applyRepeatFilter(Symbol symbol) {
		
		try {
			
			double mPrice = Double.parseDouble(symbol.getPrice());
			
			double mDelta = Double.parseDouble(symbol.getDelta());
			
			double deltaPrice = mPrice * (mDelta / 100);
			
			return (Math.abs(this.now - mPrice) <= deltaPrice);
			
		} catch (Exception e) {
			
			return false;
		}
		
	}

	public boolean applyFilter(String filter) {

		boolean status = true;

		if (filter != null) {

			String split[] = null;

			if (filter.contains("&&")) {

				split = filter.split("&&");
				
			} else if (filter.contains("||")) {

				split = filter.split("\\|\\|");
				
			} else {

				split = new String[] { filter };
			}

			if (split != null && split.length > 0) {

				status = evaluate(split[split.length - 1]);

				for (int i = split.length - 2; i >= 0; i--) {

					if (filter.contains("&&")) {

						status = status && evaluate(split[i]);

					} else if (filter.contains("||")) {

						status = status || evaluate(split[i]);
					}
				}
			}
		}

		return status;
	}

	private boolean evaluate(String expression) {

		if (expression != null) {

			expression = expression.trim();
			
			String operator = getOperator(expression);

			double operands[] = null;

			if (operator != null) {

				operands = getOperands(expression, operator);

				if (OPERATORS[0].equals(operator)) {

					return operands[0] <= operands[1];

				} else if (OPERATORS[1].equals(operator)) {

					return operands[0] < operands[1];

				} else if (OPERATORS[2].equals(operator)) {

					return operands[0] >= operands[1];

				} else if (OPERATORS[3].equals(operator)) {

					return operands[0] > operands[1];

				} else if (OPERATORS[4].equals(operator)) {

					return operands[0] == operands[1];

				} else if (OPERATORS[5].equals(operator)) {

					return operands[0] != operands[1];

				} else {

					return true;
				}

			} else {

				return true;
			}

		} else {

			return true;
		}

	}

	private String getOperator(String expression) {

		for (String op : OPERATORS) {

			if (expression.contains(op)) {

				return op;
			}
		}

		return null;
	}

	private double[] getOperands(String expression, String operator) {

		double lhs = -9999;

		double rhs = -9999;

		String split[] = expression.split(operator);

		if (split.length == 2) {

			lhs = round(getValueForKey(split[0]));

			rhs = Double.parseDouble(split[1]);
		}

		return new double[] { lhs, rhs };
	}

	private double getValueForKey(String key) {

		if (KEYS[0].equals(key)) {

			return getNow();

		} else if (KEYS[1].equals(key)) {

			return getLow();

		} else if (KEYS[2].equals(key)) {

			return getHigh();

		} else if (KEYS[3].equals(key)) {

			return getLowPercent();

		} else if (KEYS[4].equals(key)) {

			return getHighPercent();

		} else if (KEYS[5].equals(key)) {

			return getNowPercent();

		} else if (KEYS[6].equals(key)) {

			return getMove();

		} else if (KEYS[7].equals(key)) {

			return getDiffer();

		} else {

			return -9999;
		}

	}

	public String toPrintableString(int index) {

		return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", index, this.symbol, round(this.now), round(this.low),
				round(this.high), getPrintableData(round(this.nowPercent)), getPrintableData(round(this.lowPercent)),
				getPrintableData(round(this.highPercent)), sdf.format(this.lowDate), sdf.format(this.highDate),
				getPrintableData(round(this.move)), getPrintableData(round(this.differ)));

	}

	@Override
	public String toString() {

		return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s,%.2f,%.2f", this.symbol, round(this.now),
				round(this.low), round(this.high), round(this.nowPercent), round(this.lowPercent),
				round(this.highPercent), sdf.format(this.lowDate), sdf.format(this.highDate), round(this.move),
				round(this.differ));

	}

}
