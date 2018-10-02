package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import in.blacklotus.utils.Utils;

public abstract class Trend extends Stock {

	private String type;

	protected List<TrendData> trends;

	public Trend() {

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public Trend(String stockName) {
		super(stockName);
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

	public boolean isValidTrend(int count) {

		if ("UP".equalsIgnoreCase(this.type)) {

			return this.isUptrend(count);

		} else if ("DOWN".equalsIgnoreCase(this.type)) {

			return this.isDowntrend(count);
		}

		return false;
	}

	public boolean isValidVolumeTrend(int count) {

		if ("UP".equalsIgnoreCase(this.type)) {

			return this.isVolumeUptrend(count);

		} else if ("DOWN".equalsIgnoreCase(this.type)) {

			return this.isVolumeDowntrend(count);
		}

		return false;
	}

	protected boolean isUptrend(int count) {

		if (this.trends.size() < count) {

			return false;
		}

		List<TrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			TrendData trendData = this.trends.get(i);

			if (trendData.getValue() >= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(trendData);
			}
		}

		temp.add(0, this.trends.get(0));

		this.trends = temp;

		return true;
	}

	protected boolean isDowntrend(int count) {

		if (this.trends.size() < count) {

			return false;
		}

		List<TrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			TrendData trendData = this.trends.get(i);

			if (trendData.getValue() <= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(trendData);
			}
		}

		temp.add(0, this.trends.get(0));

		this.trends = temp;

		return true;
	}

	protected boolean isVolumeUptrend(int count) {

		if (this.trends.size() < count) {

			return false;
		}

		List<TrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			TrendData trendData = this.trends.get(i);

			if (trendData.getVolume() >= this.trends.get(i - 1).getVolume()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(trendData);
			}
		}

		temp.add(0, this.trends.get(0));

		this.trends = temp;

		return true;
	}

	protected boolean isVolumeDowntrend(int count) {

		if (this.trends.size() < count) {

			return false;
		}

		List<TrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			TrendData trendData = this.trends.get(i);

			if (trendData.getVolume() <= this.trends.get(i - 1).getVolume()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(trendData);
			}
		}

		temp.add(0, this.trends.get(0));

		this.trends = temp;

		return true;
	}

	protected String getChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%.2f", (this.trends.get(index).getValue() - this.trends.get(index + 1).getValue()));
		}
	}

	protected String getPercentageChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%%%.2f",
					Math.abs((this.trends.get(index).getValue() - this.trends.get(index + 1).getValue()) * 100.00
							/ this.trends.get(index + 1).getValue()));
		}
	}

	protected String getVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return Utils.formattedVolume(this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume());
		}
	}

	protected String getParcentageVolChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%%%.2f", getPercentageVolumeChangeForIndex(index));
		}
	}

	protected double getPercentageVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return -1;

		} else {

			return (this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume()) * 100.00
					/ this.trends.get(index + 1).getVolume();
		}
	}

	public abstract String[] toPrintableStrings(int index, double threshold);
}
