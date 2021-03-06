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

	public List<VolumeTrendData> getTrends() {
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

	public int getPriceRank() {

		return this.trends.get(0).getPriceRank();
	}

	public int getVolumeRank() {

		return this.trends.get(0).getVolumeRank();
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

			this.trends.get(i).setPriceRank(this.getRankForPricePercentDiff(this.trends.get(i).getPriceDiffPercent()));

			this.trends.get(i)
					.setVolumeRank(this.getRankForVolumePercentDiff(this.trends.get(i).getVolumeDiffPercentage()));
			
			this.trends.get(i).setDayRank(this.getRankForDaysCount(this.trends.size()));
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

	public int getRankForPricePercentDiff(Double diffPercent) {

		if (diffPercent > 5) {

			return 1;

		} else if (diffPercent > 3 && diffPercent <= 5) {

			return 2;

		} else if (diffPercent > 1 && diffPercent <= 3) {

			return 3;

		} else if (diffPercent >= 0 && diffPercent <= 1) {

			return 4;

		} else {

			return 9;
		}
	}

	private int getRankForVolumePercentDiff(Double diffPercent) {

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
	
	private int getRankForDaysCount(int value) {

		return value - 1;
	}

	public String[] toPrintableStrings(int index, double threshold) {

		List<String> printableTrendsList = new ArrayList<>();

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (Math.abs(getPercentageVolumeChangeForIndex(i)) >= threshold) {

				if (printableTrendsList.isEmpty()) {

					tmp = String.format("%d_%s_%s_%s_%s_%s_%s_%s_T: %s_T: %s_S: %s_R: %s_%s_S: %s%%_R: %s%%_%s_%s_L: %s_H: %s_%s_%s_V: %s_P: %s_%s_%s",
							index, this.getSymbol(), this.trends.get(i).toPrintableLow(i),
							this.trends.get(i).toPrintableString(), this.trends.get(i).toPrintableHigh(i),
							this.trends.get(i).toPrintablePriceChange(),
							this.trends.get(i).toPrintablePriceChangePercent(), round(this.getLowHighDiff()),
							getPrintableDataValue(round(this.getDchg10())), getPrintableData(round(this.getDchgPercent())),
							round(this.getSupt()), round(this.getRest()), round(this.getSrdif()),
							round(this.getSuptPercent()), round(this.getRestPercent()), getSmar(), this.getNewHigh(),
							getPrintableData(round(this.getLow10Percent())),
							getPrintableData(round(this.getHigh10Percent())),
							Utils.formattedVolume(this.trends.get(i).getVolume()),
							this.trends.get(i).toPrintableVolumeChangePercent(), this.trends.get(i).getVolumeRank(), 
							this.getRankForPricePercentDiff(this.trends.get(i).getPriceDiffPercent()), 
							this.trends.get(i).getDayRank(), getSymbol());
				} else {

					tmp = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s", " ",
							" ", this.trends.get(i).toPrintableLow(i), this.trends.get(i).toPrintableString(),
							this.trends.get(i).toPrintableHigh(i), this.trends.get(i).toPrintablePriceChange(),
							this.trends.get(i).toPrintablePriceChangePercent(), " ", " ", " ", " ", " ", " ", " ", " ",
							" ", " ", " ", " ", Utils.formattedVolume(this.trends.get(i).getVolume()),
							this.trends.get(i).toPrintableVolumeChangePercent(), " ", " ", " ", " ", " ", " ", " ", " ");
				}

				printableTrendsList.add(tmp);
			}
		}

		if (!printableTrendsList.isEmpty()) {

			printableTrendsList.add(String.format(
					"%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s", " ", " ", " ", " ", " ",
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "));
		}

		return printableTrendsList.toArray(new String[] {});
	}
}
