package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import in.blacklotus.utils.Utils;

public class PriceTrend extends Stock {

	private String type;

	private List<TrendData> trends;

	public PriceTrend() {

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public PriceTrend(String stockName) {
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

	private boolean isUptrend(int count) {

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

	private boolean isDowntrend(int count) {

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

	private boolean isVolumeUptrend(int count) {

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

	private boolean isVolumeDowntrend(int count) {

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

	private String getChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%.2f", (this.trends.get(index).getValue() - this.trends.get(index + 1).getValue()));
		}
	}

	private String getPercentageChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%%%.2f",
					Math.abs((this.trends.get(index).getValue() - this.trends.get(index + 1).getValue()) * 100.00
							/ this.trends.get(index + 1).getValue()));
		}
	}

	private String getVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return Utils.formattedVolume(this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume());
		}
	}

	private String getParcentageVolChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return "-";

		} else {

			return String.format("%%%.2f", getPercentageVolumeChangeForIndex(index));
		}
	}

	private double getPercentageVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return -1;

		} else {

			return (this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume()) * 100.00
					/ this.trends.get(index + 1).getVolume();
		}
	}

	public String[] toPrintableStrings(int index) {

		String[] printableTrends = new String[this.trends.size() + 1];

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (i == 0) {

				tmp = String.format("%d_%s_%s_%s_%s_%s_%s_%s_%s", index, this.getSymbol(),
						this.trends.get(i).toPrintableLow(), this.trends.get(i).toPrintableHigh(),
						this.trends.get(i).toPrintableString(), this.getChangeForIndex(i),
						getPercentageChangeForIndex(i), Utils.formattedVolume(this.trends.get(i).getVolume()),
						getParcentageVolChangeForIndex(i));
			} else {

				tmp = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s", "", "", "", "",
						this.trends.get(i).toPrintableString(), this.getChangeForIndex(i),
						getPercentageChangeForIndex(i), Utils.formattedVolume(this.trends.get(i).getVolume()),
						getParcentageVolChangeForIndex(i));
			}

			printableTrends[i] = tmp;
		}

		printableTrends[this.trends.size()] = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s", " ", " ", " ", " ", " ", " ",
				" ", " ", " ");

		return printableTrends;
	}

	public String[] toPrintableVolumeStrings(int index, double threshold) {

		List<String> printableTrendsList = new ArrayList<>();

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (Math.abs(getPercentageVolumeChangeForIndex(i)) >= threshold) {

				if (printableTrendsList.isEmpty()) {

					tmp = String.format("%d_%s_$%s_%s_%s_%s_%s_%s", index, this.getSymbol(),
							this.trends.get(i).toPrintableString(), this.getChangeForIndex(i),
							getPercentageChangeForIndex(i), Utils.formattedVolume(this.trends.get(i).getVolume()),
							getVolumeChangeForIndex(i), getParcentageVolChangeForIndex(i));
				} else {

					tmp = String.format("%s_%s_$%s_%s_%s_%s_%s_%s", " ", " ", this.trends.get(i).toPrintableString(),
							this.getChangeForIndex(i), getPercentageChangeForIndex(i),
							Utils.formattedVolume(this.trends.get(i).getVolume()), getVolumeChangeForIndex(i),
							getParcentageVolChangeForIndex(i));
				}

				printableTrendsList.add(tmp);
			}
		}

		if (!printableTrendsList.isEmpty()) {

			printableTrendsList
					.add(String.format("%s_%s_%s_%s_%s_%s_%s_%s", " ", " ", " ", " ", " ", " ", " ", " ", " "));
		}

		return printableTrendsList.toArray(new String[] {});
	}
}
