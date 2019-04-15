package in.blacklotus.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;

import in.blacklotus.utils.Utils;

public class Earning {

	private static SimpleDateFormat sd = new SimpleDateFormat("MMM dd");

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

	private String symbol;

	private ArrayList<Quarter> quarters;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public ArrayList<Quarter> getQuarters() {
		return quarters;
	}

	public void setQuarters(ArrayList<Quarter> quarters) {
		this.quarters = quarters;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	public static class Quarter {

		private String date;

		private double consensusEstimate;

		private double reportEps;

		private String earningsHistory;

		private double epsEst;

		private double epsActual;

		private double difference;

		private double surprise;

		private ArrayList<EarningData> earningData;

		private double low10;
		
		private String lowDate;

		private double high10;
		
		private String highDate;
		
		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public double getConsensusEstimate() {
			return consensusEstimate;
		}

		public void setConsensusEstimate(double consensusEstimate) {
			this.consensusEstimate = consensusEstimate;
		}

		public double getReportEps() {
			return reportEps;
		}

		public void setReportEps(double reportEps) {
			this.reportEps = reportEps;
		}

		public String getEarningsHistory() {
			return earningsHistory;
		}

		public void setEarningsHistory(String earningsHistory) {
			this.earningsHistory = earningsHistory;
		}

		public double getEpsEst() {
			return epsEst;
		}

		public void setEpsEst(double epsEst) {
			this.epsEst = epsEst;
		}

		public double getEpsActual() {
			return epsActual;
		}

		public void setEpsActual(double epsActual) {
			this.epsActual = epsActual;
		}

		public double getDifference() {
			return difference;
		}

		public void setDifference(double difference) {
			this.difference = difference;
		}

		public double getSurprise() {
			return surprise;
		}

		public void setSurprise(double surprise) {
			this.surprise = surprise;
		}

		public ArrayList<EarningData> getEarningData() {
			return earningData;
		}

		public void setEarningData(ArrayList<EarningData> earningData) {
			this.earningData = earningData;
		}

		public String getLow10() {

			Date mDate = null;
			
			if (this.low10 == 0) {

				int lowIndex = getLowIndex();

				this.low10 = this.earningData.get(lowIndex).getLow();
				
				try {

					mDate = sdf.parse(earningData.get(lowIndex).getDate());
					
					this.lowDate = sd.format(mDate);

				} catch (ParseException e) {

				}

			}

			return String.format("%s : %s", lowDate, this.low10);
		}

		public String getHigh10() {

			Date mDate = null;
			
			if (this.high10 == 0) {

				int highIndex = getHighIndex();

				this.high10 = this.earningData.get(highIndex).getHigh();

				try {

					mDate = sdf.parse(earningData.get(highIndex).getDate());
					
					this.highDate = sd.format(mDate);

				} catch (ParseException e) {

				}

			}

			return String.format("%s : %s", highDate, this.high10);
		}

		private int getHighIndex() {

			int max = 0;

			for (int i = 1; i < earningData.size(); i++) {

				if (earningData.get(i).getHigh() > earningData.get(max).getHigh()) {

					max = i;
				}
			}

			return max;
		}

		private int getLowIndex() {

			int min = 0;

			for (int i = 1; i < earningData.size(); i++) {

				if (earningData.get(i).getLow() < earningData.get(min).getLow()) {

					min = i;
				}
			}

			return min;
		}

		private String getPriceChangeForIndex(int index) {

			if (index == earningData.size() - 1) {

				return " ";

			} else {

				return String.format("%.2f",
						this.earningData.get(index).getClose() - this.earningData.get(index + 1).getClose());
			}
		}

		private String getPercentagePriceChangeForIndex(int index) {

			if (index == earningData.size() - 1) {

				return " ";

			} else {

				return String.format("P:%.2f%%",
						(this.earningData.get(index).getClose() - this.earningData.get(index + 1).getClose()) * 100.00
								/ this.earningData.get(index + 1).getClose());
			}
		}

		// private String getVolumeChangeForIndex(int index) {
		//
		// if (index == earningData.size() - 1) {
		//
		// return " ";
		//
		// } else {
		//
		// return String.format("%d", this.earningData.get(index).getVolume() -
		// this.earningData.get(index + 1).getVolume());
		// }
		// }

		private String getPercentageVolumeChangeForIndex(int index) {

			if (index == earningData.size() - 1) {

				return " ";

			} else {

				return String.format("V:%.2f%%",
						(this.earningData.get(index).getVolume() - this.earningData.get(index + 1).getVolume()) * 100.00
								/ this.earningData.get(index + 1).getVolume());
			}
		}

		@Override
		public String toString() {
			return new Gson().toJson(this);
		}

		public String[] toPrintableString(String index, String symbol) {

			String[] data = new String[earningData.size()];

			for (int i = 0; i < earningData.size(); i++) {

				Date mDate = null;

				try {

					mDate = sdf.parse(earningData.get(i).getDate());

				} catch (ParseException e) {

				}

				if (i == 0) {

					data[i] = String.format("%s,%s,%s,%s,%s : %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%%", index, symbol,
							getLow10(), getHigh10(), sd.format(mDate),
							Utils.round(earningData.get(i).getClose()), getPriceChangeForIndex(i),
							getPercentagePriceChangeForIndex(i), getPercentageVolumeChangeForIndex(i), " ",
							earningsHistory, Utils.round(consensusEstimate), Utils.round(reportEps),
							Utils.round(epsEst), Utils.round(epsActual), Utils.round(difference),
							Utils.round(surprise));

				} else {

					data[i] = String.format("%s,%s,%s,%s,%s : %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", " ", " ", " ", " ",
							sd.format(mDate), Utils.round(earningData.get(i).getClose()), getPriceChangeForIndex(i),
							getPercentagePriceChangeForIndex(i), getPercentageVolumeChangeForIndex(i), " ", " ", " ",
							" ", " ", " ", " ", " ");
				}
			}

			return data;
		}
	}
}
