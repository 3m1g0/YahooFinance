package in.blacklotus.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.blacklotus.utils.Utils;

public class PriceTrend extends Stock {

	private String type;

	private List<PriceTrendData> trends;

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

	public List<PriceTrendData> getTrends() {
		return trends;
	}

	public void setTrend(List<PriceTrendData> trends) {
		this.trends = trends;
	}

	public boolean isValidPriceTrend(int count) {

		if ("UP".equalsIgnoreCase(this.type)) {

			return this.isUptrend(count);

		} else if ("DOWN".equalsIgnoreCase(this.type)) {

			return this.isDowntrend(count);
		}

		return false;
	}

	private boolean isUptrend(int count) {

		if (this.trends.size() < count) {

			return false;
		}

		List<PriceTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			PriceTrendData PriceTrendData = this.trends.get(i);

			if (PriceTrendData.getValue() >= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(PriceTrendData);
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

		List<PriceTrendData> temp = new ArrayList<>();

		for (int i = 1; i < this.trends.size(); i++) {

			PriceTrendData PriceTrendData = this.trends.get(i);

			if (PriceTrendData.getValue() <= this.trends.get(i - 1).getValue()) {

				if (i < count) {

					return false;

				} else {

					break;
				}

			} else {

				temp.add(PriceTrendData);
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

	public int getDayRank() {

		return this.trends.get(0).getDayRank();
	}

	public void sortByPriceRank() {

		Collections.sort(this.trends, new Comparator<PriceTrendData>() {

			@Override
			public int compare(PriceTrendData s1, PriceTrendData s2) {

				return -Double.compare(Math.abs(s1.getPriceDiffPercent()), Math.abs(s2.getPriceDiffPercent()));
			}
		});

	}

	public void sortByVolumeRank() {

		Collections.sort(this.trends, new Comparator<PriceTrendData>() {

			@Override
			public int compare(PriceTrendData s1, PriceTrendData s2) {

				return -Double.compare(Math.abs(s1.getVolumeDiffPercentage()), Math.abs(s2.getVolumeDiffPercentage()));
			}
		});

	}

	public void sortByDaysRank() {

		Collections.sort(this.trends, new Comparator<PriceTrendData>() {

			@Override
			public int compare(PriceTrendData s1, PriceTrendData s2) {

				return Integer.compare(s1.getDayRank(), s2.getDayRank());
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

	private int getRankForPricePercentDiff(Double diffPercent) {

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

		return value;
	}

	public String[] toPrintableStrings(int index) {

		List<String> printableTrendsList = new ArrayList<>();

		for (int i = 0; i < this.trends.size(); i++) {

			String tmp;

			if (printableTrendsList.isEmpty()) {

				tmp = String.format(
						"%d_%s_%s_%s_%s_%s_%s_%s_S: %s_R: %s_%s_S: %s%%_R: %s%%_%s_L: %s_H: %s_%s_%s_T: %s_T: %s_%s_%s_%s_%s",
						index, this.getSymbol(), this.trends.get(i).toPrintableLow(i),
						this.trends.get(i).toPrintableString(), this.trends.get(i).toPrintableHigh(i),
						this.trends.get(i).toPrintablePriceChange(), this.trends.get(i).toPrintablePriceChangePercent(),
						round(this.getLowHighDiff()), round(this.getSupt()), round(this.getRest()),
						round(this.getSrdif()), round(this.getSuptPercent()), round(this.getRestPercent()), getSmar(),
						getPrintableData(round(this.getLow10Percent())),
						getPrintableData(round(this.getHigh10Percent())),
						Utils.formattedVolume(this.trends.get(i).getVolume()),
						this.trends.get(i).toPrintableVolumeChangePercent(),
						getPrintableDataValue(round(this.getDchg10())), getPrintableData(round(this.getDchgPercent())),
						this.trends.get(i).getVolumeRank(), this.trends.get(i).getPriceRank(),
						this.trends.get(i).getDayRank(), this.getSymbol());
			} else {

				tmp = String.format("%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s", " ", " ",
						this.trends.get(i).toPrintableLow(i), this.trends.get(i).toPrintableString(),
						this.trends.get(i).toPrintableHigh(i), this.trends.get(i).toPrintablePriceChange(),
						this.trends.get(i).toPrintablePriceChangePercent(), " ", " ", " ", " ", " ", " ", " ", " ", " ",
						Utils.formattedVolume(this.trends.get(i).getVolume()),
						this.trends.get(i).toPrintableVolumeChangePercent(), " ", " ", " ", " ", " ", " ");
			}

			printableTrendsList.add(tmp);
		}

		if (!printableTrendsList.isEmpty()) {

			printableTrendsList.add(String.format(
					"%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s", " ", " ", " ", " ", " ",
					" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " "));
		}

		return printableTrendsList.toArray(new String[] {});
	}
}
