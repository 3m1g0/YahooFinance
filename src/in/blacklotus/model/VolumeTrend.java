package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.blacklotus.utils.Utils;

public class VolumeTrend extends Stock {

	private String type;

	private List<VolumeTrendData> trends;

	public VolumeTrend() {

		String pattern = "MMM dd";

		sdf = new SimpleDateFormat(pattern);
	}

	public VolumeTrend(String stockName) {
		super(stockName);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<VolumeTrendData> getTrend() {
		return trends;
	}

	public void setTrend(List<VolumeTrendData> trends) {
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

		List<VolumeTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			VolumeTrendData VolumeTrendData = this.trends.get(i);

			if (VolumeTrendData.getValue() >= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(VolumeTrendData);
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

		List<VolumeTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			VolumeTrendData VolumeTrendData = this.trends.get(i);

			if (VolumeTrendData.getValue() <= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(VolumeTrendData);
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

		List<VolumeTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			VolumeTrendData VolumeTrendData = this.trends.get(i);

			if (VolumeTrendData.getVolume() >= this.trends.get(i - 1).getVolume()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(VolumeTrendData);
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

		List<VolumeTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			VolumeTrendData VolumeTrendData = this.trends.get(i);

			if (VolumeTrendData.getVolume() <= this.trends.get(i - 1).getVolume()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(VolumeTrendData);
			}
		}

		temp.add(0, this.trends.get(0));

		this.trends = temp;

		return true;
	}

	public void sortByRank() {

		Collections.sort(this.trends, new Comparator<VolumeTrendData>() {

			@Override
			public int compare(VolumeTrendData s1, VolumeTrendData s2) {

				return -Double.compare(Math.abs(s1.getVolumeDiffPercentage()), Math.abs(s2.getVolumeDiffPercentage()));
			}
		});

	}

	public void assignData() {

		for (int i = 0; i < this.trends.size(); i++) {

			this.trends.get(i).setPriceDiff(this.getPriceChangeForIndex(i));

			this.trends.get(i).setPriceDiffPercent(this.getPercentagePriceChangeForIndex(i));

			this.trends.get(i).setVolumeDiff(this.getVolumeChangeForIndex(i));

			this.trends.get(i).setVolumeDiffPercentage(this.getPercentageVolumeChangeForIndex(i));

			this.trends.get(i).setRank(this.getRankForVolumePercentDiff(this.trends.get(i).getVolumeDiffPercentage()));
		}
	}

	private Double getPriceChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return Double.MIN_VALUE;

		} else {

			return this.trends.get(index).getValue() - this.trends.get(index + 1).getValue();
		}
	}

	private Double getPercentagePriceChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return Double.MIN_VALUE;

		} else {

			return (this.trends.get(index).getValue() - this.trends.get(index + 1).getValue()) * 100.00
					/ this.trends.get(index + 1).getValue();
		}
	}

	private Long getVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return Long.MIN_VALUE;

		} else {

			return this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume();
		}
	}

	private Double getPercentageVolumeChangeForIndex(int index) {

		if (index == trends.size() - 1) {

			return Double.MIN_VALUE;

		} else {

			return (this.trends.get(index).getVolume() - this.trends.get(index + 1).getVolume()) * 100.00
					/ this.trends.get(index + 1).getVolume();
		}
	}

	private int getRankForVolumePercentDiff(Double value) {

		Double diffPercent = Math.abs(value);

		if (diffPercent > 150) {

			return 1;

		} else if (diffPercent > 100 && diffPercent <= 150) {

			return 2;

		} else if (diffPercent > 70 && diffPercent <= 100) {

			return 3;

		} else if (diffPercent > 40 && diffPercent <= 70) {

			return 4;

		} else {

			return 5;
		}
	}

	public String[] toPrintableStrings(int index, double threshold) {

		List<String> printableTrendsList = new ArrayList<>();

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (Math.abs(getPercentageVolumeChangeForIndex(i)) >= threshold) {

				if (printableTrendsList.isEmpty()) {

					tmp = String.format("%d_%s_%s_%s_%s_%s_%s_%s_%d", index, this.getSymbol(),
							this.trends.get(i).toPrintableString(), this.trends.get(i).toPrintablePriceChange(),
							this.trends.get(i).toPrintablePriceChangePercent(),
							Utils.formattedVolume(this.trends.get(i).getVolume()),
							this.trends.get(i).toPrintableVolumeChange(),
							this.trends.get(i).toPrintableVolumeChangePercent(), this.trends.get(i).getRank());
				} else {

					tmp = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%d", " ", " ", this.trends.get(i).toPrintableString(),
							this.trends.get(i).toPrintablePriceChange(),
							this.trends.get(i).toPrintablePriceChangePercent(),
							Utils.formattedVolume(this.trends.get(i).getVolume()),
							this.trends.get(i).toPrintableVolumeChange(),
							this.trends.get(i).toPrintableVolumeChangePercent(), this.trends.get(i).getRank());
				}

				printableTrendsList.add(tmp);
			}
		}

		if (!printableTrendsList.isEmpty()) {

			printableTrendsList
					.add(String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "));
		}

		return printableTrendsList.toArray(new String[] {});
	}
}
