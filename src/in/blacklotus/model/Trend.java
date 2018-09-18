package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.List;

public class Trend extends Stock {

	private String type;

	private List<TrendData> trends;

	public Trend() {

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<TrendData> getTrend() {
		return trends;
	}

	public void setTrend(List<TrendData> trends) {
		this.trends = trends;
	}

	public boolean isValidTrend() {
		
		if("UP".equalsIgnoreCase(this.type)) {
			
			return this.isUptrend();
		
		} else if("DOWN".equalsIgnoreCase(this.type)) {
			
			return this.isDowntrend();
		} 
		
		return false;
	}

	private boolean isUptrend() {
		
		if (this.trends.size() < 2) {

			return false;
		}

		for (int i = 1; i < this.trends.size(); i++) {
			
			if (this.trends.get(i).getValue() <= this.trends.get(i - 1).getValue()) {

				return false;
			}
		}

		return true;
	}

	private boolean isDowntrend() {

		if (this.trends.size() < 2) {

			return false;
		}

		for (int i = 1; i < this.trends.size(); i++) {

			if (this.trends.get(i).getValue() >= this.trends.get(i - 1).getValue()) {

				return false;
			}
		}

		return true;
	}

	private String getChangeForIndex(int index) {

		if (index == 0) {

			return "-";
		} else {

			return String.format("%.2f", this.trends.get(index).getValue() - this.trends.get(index - 1).getValue());
		}
	}

	public String[] toPrintableStrings(int index) {

		String[] printableTrends = new String[this.trends.size()];

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (i == 0) {

				tmp = String.format("%d,%s,%s: %s,%s,%s,%s", index, this.getSymbol(), sdf.format(this.getNowDate()),
						round(this.getNow()), this.trends.get(i).toPrintableString(), this.getChangeForIndex(i),
						round(this.trends.get(i).getVolume()));
			} else {

				tmp = String.format("%s,%s,%s,%s,%s,%s", "", "", "", this.trends.get(i).toPrintableString(),
						this.getChangeForIndex(i), round(this.trends.get(i).getVolume()));
			}

			printableTrends[i] = tmp;
		}

		return printableTrends;
	}
}
