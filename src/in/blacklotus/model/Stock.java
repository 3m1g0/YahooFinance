package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stock {

	public static final String[] KEYS = { "NOW", "LOW10", "HIGH10", "%LOW10", "%HIGH10", "%TODAY", "%MOVE", "%DIFFER" };

	private static final String[] OPERATORS = { "<=", "<", ">=", ">", "==", "!=" };

	private String currency;

	private String name;

	private String symbol;

	private double now;
	
	private double open;

	private double yesterday;

	private double pricage;

	private Long volume;

	private double low10;

	private double low20;

	private double high10;

	private double high20;

	private double nowPercent;

	private double low10Percent;

	private double low20Percent;

	private double high10Percent;

	private double high20Percent;

	private Date nowDate;

	private Date high10Date;

	private Date high20Date;

	private Date low10Date;

	private Date low20Date;

	private int low10Index;

	private int low20Index;

	private int high10Index;

	private int high20Index;

	private double volumeChangePercent;

	private double lowHighDiff;

	private double lowHighDiffPercent;

	private double sma10;

	private double supt;

	private double rest;

	private String smar;

	private double suptPercent;

	private double restPercent;

	private double srdif;

	private double dchg10;

	private double dchgPercent;
	
	private int prir;
	
	private int volr;

	protected SimpleDateFormat sdf;

	public Stock() {

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public Stock(String symbol) {
		this.symbol = symbol;
	}

	public void calculateHigh10Percenttage() {
		this.high10Percent = (this.now - this.high10) / this.high10 * 100;
	}

	public void calculateHigh20Percenttage() {
		this.high20Percent = (this.now - this.high20) / this.high20 * 100;
	}

	public void calculateLow10Percenttage() {
		this.low10Percent = (this.now - this.low10) / this.low10 * 100;
	}

	public void calculateLow20Percenttage() {
		this.low20Percent = (this.now - this.low20) / this.low20 * 100;
	}

	public void calculateLowHighDiff() {
		this.lowHighDiff = (this.high10 - this.low10);
	}

	public void calculateLowHighDiffPercent() {
		this.lowHighDiffPercent = (this.high10 - this.low10) / this.now * 100;
	}

	public void calculateSuptPercenttage() {
		this.suptPercent = (this.now - this.supt) / this.now * 100;
	}

	public void calculatRestPercenttage() {
		this.restPercent = (this.now - this.rest) / this.now * 100;
	}

	public void calculateSRDiff() {
		this.srdif = (this.supt - this.rest);
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

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getYesterday() {
		return yesterday;
	}

	public void setYesterday(double yesterday) {
		this.yesterday = yesterday;
	}

	public double getPricage() {
		return pricage;
	}

	public void setPricage(double pricage) {
		this.pricage = pricage;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public double getLow10() {
		return low10;
	}

	public void setLow10(double low) {
		this.low10 = low;
	}

	public double getLow20() {
		return low20;
	}

	public void setLow20(double low) {
		this.low20 = low;
	}

	public double getHigh10() {
		return high10;
	}

	public void setHigh10(double high) {
		this.high10 = high;
	}

	public double getHigh20() {
		return high20;
	}

	public void setHigh20(double high) {
		this.high20 = high;
	}

	public double getNowPercent() {
		return nowPercent;
	}

	public void setNowPercent(double nowPercent) {
		this.nowPercent = nowPercent;
	}

	public double getLow10Percent() {
		return low10Percent;
	}

	public void setLow10Percent(double low10Percent) {
		this.low10Percent = low10Percent;
	}

	public double getLow20Percent() {
		return low20Percent;
	}

	public void setLow20Percent(double lowPercent) {
		this.low20Percent = lowPercent;
	}

	public double getHigh10Percent() {
		return high10Percent;
	}

	public void setHigh10Percent(double high10Percent) {
		this.high10Percent = high10Percent;
	}

	public double getHigh20Percent() {
		return high20Percent;
	}

	public void setHigh20Percent(double highPercent) {
		this.high20Percent = highPercent;
	}

	public Date getNowDate() {
		return nowDate;
	}

	public void setNowDate(Date nowDate) {
		this.nowDate = nowDate;
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

	public void setHigh20Date(Date highDate) {
		this.high20Date = highDate;
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

	public void setLow20Date(Date lowDate) {
		this.low20Date = lowDate;
	}

	public double getVolumeChangePercent() {
		return volumeChangePercent;
	}

	public void setVolumeChangePercent(double differ) {
		this.volumeChangePercent = differ;
	}

	public double getLowHighDiff() {
		return lowHighDiff;
	}

	public void setLowHighDiff(double lowHighDiff) {
		this.lowHighDiff = lowHighDiff;
	}

	public double getLowHighDiffPercent() {
		return lowHighDiffPercent;
	}

	public void setLowHighDiffPercent(double move) {
		this.lowHighDiffPercent = move;
	}

	public double getSma10() {
		return sma10;
	}

	public void setSma10(double sma10) {
		this.sma10 = sma10;
	}

	public double getSupt() {
		return supt;
	}

	public void setSupt(double supt) {
		this.supt = supt;
	}

	public double getRest() {
		return rest;
	}

	public void setRest(double rest) {
		this.rest = rest;
	}

	public String getSmar() {
		return smar;
	}

	public void setSmar(String smar) {
		this.smar = smar;
	}

	public double getSuptPercent() {
		return suptPercent;
	}

	public void setSuptPercent(double suptPercent) {
		this.suptPercent = suptPercent;
	}

	public double getRestPercent() {
		return restPercent;
	}

	public void setRestPercent(double restPercent) {
		this.restPercent = restPercent;
	}

	public double getSrdif() {
		return srdif;
	}

	public void setSrdif(double srdif) {
		this.srdif = srdif;
	}

	public double getDchg10() {
		return dchg10;
	}

	public void setDchg10(double dchg10) {
		this.dchg10 = dchg10;
	}

	public double getDchgPercent() {
		return dchgPercent;
	}

	public void setDchgPercent(double dchgPercent) {
		this.dchgPercent = dchgPercent;
	}

	public int getLow10Index() {
		return low10Index;
	}

	public void setLow10Index(int low10Index) {
		this.low10Index = low10Index;
	}

	public int getLow20Index() {
		return low20Index;
	}

	public void setLow20Index(int low20Index) {
		this.low20Index = low20Index;
	}

	public int getHigh10Index() {
		return high10Index;
	}

	public void setHigh10Index(int high10Index) {
		this.high10Index = high10Index;
	}

	public int getHigh20Index() {
		return high20Index;
	}

	public void setHigh20Index(int high20Index) {
		this.high20Index = high20Index;
	}

	public int getPrir() {
		return prir;
	}

	public void setPrir(int prir) {
		this.prir = prir;
	}

	public int getVolr() {
		return volr;
	}

	public void setVolr(int volr) {
		this.volr = volr;
	}

	protected double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	protected String getPrintableData(double value) {

		if (value == -9999) {

			return "-";

		} else if (value == Integer.MIN_VALUE) {

			return "-";

		} else if (value > 0) {

			return "+" + value + "%";

		} else {

			return value + "%";
		}
	}
	
	protected String getPrintableDataValue(double value) {

		if (value == -9999) {

			return "-";

		} else if (value == Integer.MIN_VALUE) {

			return "-";

		} else if (value > 0) {

			return "+" + value;

		} else {

			return String.valueOf(value);
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

	public boolean applyDropFilter(int drop) {

		if (low10Index <= drop) {

			return false;
		}

		if (high10Index <= drop) {

			return false;
		}

		return true;
	}

	public boolean applyCentFilter(int cent) {

		return (low10Percent > cent);
	}

	public boolean applyLtenFilter(String lten) {

		if ("low".equalsIgnoreCase(lten)) {

			return Math.abs(low10Percent) < Math.abs(high10Percent);

		} else if ("high".equalsIgnoreCase(lten)) {

			return Math.abs(low10Percent) > Math.abs(high10Percent);

		} else {

			return false;
		}
	}

	public boolean applyLoHiDifFilter(int[] lohidif) {

		return name != null && !(lowHighDiffPercent > lohidif[0] && lowHighDiffPercent < lohidif[1]);
	}

	public boolean applySMARFilter(String SMAR) {

		return smar != null && SMAR.equalsIgnoreCase(smar);
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

			return getLow10();

		} else if (KEYS[2].equals(key)) {

			return getHigh10();

		} else if (KEYS[3].equals(key)) {

			return getLow10Percent();

		} else if (KEYS[4].equals(key)) {

			return getHigh10Percent();

		} else if (KEYS[5].equals(key)) {

			return getNowPercent();

		} else if (KEYS[6].equals(key)) {

			return getLowHighDiffPercent();

		} else if (KEYS[7].equals(key)) {

			return getVolumeChangePercent();

		} else {

			return -9999;
		}

	}

	public int volumeRank() {

		double diffPercent = this.volumeChangePercent;

		if (diffPercent > 150) {

			return 1;

		} else if (diffPercent > 100 && diffPercent <= 150) {

			return 2;

		} else if (diffPercent > 70 && diffPercent <= 100) {

			return 3;

		} else if (diffPercent > 40 && diffPercent <= 70) {

			return 4;

		} else if (diffPercent > 0 && diffPercent <= 40) {

			return 5;

		} else {

			return 9;
		}
	}

	public int priceRank() {

		double value = this.nowPercent;

		if (value > 5) {

			return 1;

		} else if (value > 3 && value <= 5) {

			return 2;

		} else if (value > 1 && value <= 3) {

			return 3;

		} else if (value >= 0 && value <= 1) {

			return 4;

		} else {

			return 9;
		}
	}

	public String toPrintableString(int index) {

		if (name == null) {

			return String.format("%d,%s,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-,-", index, symbol);
		}

		return String.format("%d,%s,%s,%s,%s,%s,%s,%s,S: %s,R: %s,%s,S: %s%%,R: %s%%,%s,L: %s,H: %s,%s,V: %s,T: %s,T: %s,V: %s,P: %s,%s", index,
				this.symbol, toPrintableLow(0), round(this.now), toPrintableHigh(0),
				getPrintableDataValue(round(this.pricage)), getPrintableData(round(this.nowPercent)),
				round(this.lowHighDiff), round(getSupt()), round(getRest()), round(getSrdif()), round(getSuptPercent()),
				round(getRestPercent()), getSmar(), getPrintableData(round(this.low10Percent)),
				getPrintableData(round(this.high10Percent)), getPrintableData(round(this.lowHighDiffPercent)),
				getPrintableData(round(this.volumeChangePercent)), getPrintableDataValue(round(this.dchg10)),
				getPrintableData(round(this.dchgPercent)), volumeRank(), priceRank(), this.symbol);

	}

	public String toPrintableLow(int index) {

		return String.format("%s: %s", sdf.format(this.low10Date), round(this.low10));
	}

	public String toPrintableHigh(int index) {

		return String.format("%s: %s", sdf.format(this.high10Date), round(this.high10));
	}

	@Override
	public String toString() {

		return String.format("%s,%.2f,%.2f,%.2f,%.2f,%d,%.2f,%.2f,%s,%s,%.2f,%.2f,%.2f", this.symbol, round(this.now),
				round(this.low20), round(this.high20), round(this.nowPercent), round(this.lowHighDiff),
				round(this.low10Percent), round(this.high10Percent), sdf.format(this.low20Date),
				sdf.format(this.high20Date), round(this.lowHighDiffPercent), round(this.volumeChangePercent),
				volumeRank(), priceRank());
	}

}
