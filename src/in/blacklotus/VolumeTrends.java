package in.blacklotus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.Symbol;
import in.blacklotus.model.VolumeTrend;
import in.blacklotus.model.VolumeTrendData;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.MultiComparator;
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class VolumeTrends {

	private static final String INPUT_FILE_NAME = "input.csv";

	private static int NO_VALUES = 20;

	private static List<String> SORT_KEY;

	private static final String[] SORT_KEYS = { "PriR", "VolR" };

	private static String TREND = null;

	private static String SMAR = null;

	private static int TREND_COUNT = 2;

	private static int SURE = 10;

	private static double VOLUME_DIFF = 0;

	private static boolean processing = false;

	private static int percentage = 0;

	private static ArrayList<String> errorList = new ArrayList<>();

	public static void main(String[] args) {

		final List<Symbol> symbolList = new ArrayList<>();

		Map<String, List<String>> params = Utils.parseArguments(args);

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		if (params.containsKey("count")) {

			try {

				TREND_COUNT = Integer.parseInt(params.get("count").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid COUNT value. Proceeding with default value 2   ***");
			}
		}

		if (params.containsKey("vol")) {

			try {

				VOLUME_DIFF = Double.parseDouble(params.get("vol").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid VOL value. Proceeding with default value 0   ***");
			}
		}

		if (params.containsKey("sort")) {

			SORT_KEY = params.get("sort");

			SORT_KEY = Utils.getValidSortKeys(SORT_KEY, SORT_KEYS);
		}

		if (params.containsKey("trend")) {

			TREND = params.get("trend").get(0);
		}

		if (params.containsKey("smar")) {

			SMAR = params.get("smar").get(0);
		}

		if (params.containsKey("sure")) {

			try {

				SURE = Integer.parseInt(params.get("sure").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid SURE value. Proceeding with default value 10   ***");
			}
		}

		if (params.containsKey("symbol")) {

			List<String> input = params.get("symbol");

			for (String s : input) {

				Symbol symbol = new Symbol(s);

				symbolList.add(symbol);
			}

		} else {

			symbolList.clear();

			symbolList.addAll(Utils.readInput(INPUT_FILE_NAME));
		}

		if (symbolList.isEmpty()) {

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.println("Please provide Stock names either as input.csv or command line arguments");

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			return;
		}

		if (TREND != null) {

			processVolumeTrendData(symbolList);
		}

		if (!errorList.isEmpty()) {

			Utils.writeErrorsToFile(errorList);
		}

	}

	private static void processVolumeTrendData(List<Symbol> symbolList) {

		List<VolumeTrend> processedTrendList = new ArrayList<>();

		processing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				showProgress();
			}

		}).start();

		for (int i = 0; i < symbolList.size(); i++) {

			Symbol symbol = symbolList.get(i);

			try {

				VolumeTrend trendDetail = getTrendDetails(symbol.getName());

				if (trendDetail != null) {

					boolean filter = true;

					filter = filter && trendDetail.isValidVolumeTrend(TREND_COUNT);

					if (SMAR != null) {

						filter = filter && trendDetail.applySMARFilter(SMAR);
					}

					if (filter) {

						processedTrendList.add(trendDetail);
					}

				}
			} catch (Exception e) {

				errorList.add("============================================================================");
				errorList.add(symbol.getName());
				errorList.add("============================================================================");
				errorList.add(e.getMessage());
				errorList.add("============================================================================");
			}

			percentage = (i + 1) * 100 / symbolList.size();
		}

		processing = false;

		String[] headers = new String[] { "SNO", "SYMBOL", "LOW10/20", "PRICE", "HIGH10/20", "$PRICAGE", "%PRICAGE",
				"$LOHIDIF", "SMA10", "%LOW10", "%HIGH10", "VOLUME", "%VOLCAGE", "VolR", "PriR", "SMAR", "SUPT",
				"REST", "%SUPT", "%REST", "SRDIF" };

		List<String[]> tmp = new ArrayList<>();

		for (int i = 0; i < processedTrendList.size(); i++) {

			processedTrendList.get(i).assignData();
		}

		if (SORT_KEY != null) {

			List<Comparator<VolumeTrend>> comparators = new ArrayList<>();

			for (String key : SORT_KEY) {

				Comparator<VolumeTrend> comparatorForKey = getComparatorForKey(key);

				if (comparatorForKey != null) {

					comparators.add(comparatorForKey);
				}
			}

			MultiComparator.sort(processedTrendList, comparators);
		}

		for (int i = 0; i < processedTrendList.size(); i++) {

			String[] printableVolumes = processedTrendList.get(i).toPrintableStrings(i + 1, VOLUME_DIFF);

			for (int j = 0; j < printableVolumes.length; j++) {

				tmp.add(printableVolumes[j].split("_"));
			}
		}

		String[][] data = new String[tmp.size()][];

		for (int i = 0; i < tmp.size(); i++) {

			data[i] = tmp.get(i);
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));

		writeTrendsToFile(headers, processedTrendList);
	}

	public static VolumeTrend getTrendDetails(String stockName) {

		VolumeTrend trend = new VolumeTrend();

		Double[] closeValues;

		Double[] lowValues;

		Double[] highValues;

		Long[] volumes;

		long[] timestamps;

		Metadata metaData;

		YahooFinanceAPI service = NetworkUtils.getYahooFinanceAPIService();

		Call<ResponseBody> data = service.getStockInfo(stockName);

		try {

			Response<ResponseBody> execute = data.execute();

			if (execute.code() != 200) {

				errorList.add(stockName + " ---> No data found, symbol may be delisted");

				return new VolumeTrend(stockName);
			}

			String responseString = execute.body().string();

			YahooResponse yahooResponse = new Gson().fromJson(responseString, YahooResponse.class);

			metaData = yahooResponse.getChart().getResult()[0].getMeta();

			timestamps = yahooResponse.getChart().getResult()[0].getTimestamp();

			closeValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getClose();

			if (closeValues == null) {

				return null;

			} else if (closeValues.length < NO_VALUES) {

				// NO_VALUES = closeValues.length;
			}

			volumes = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getVolume();

			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();

			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			trend.setType(TREND.toUpperCase());

			trend.setCurrency(metaData.getCurrency());

			trend.setSymbol(metaData.getSymbol());

			trend.setName(metaData.getExchangeName());

			trend.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			trend.setVolume(volumes[volumes.length - 1]);

			double nowPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? -9999
					: (trend.getNow() - closeValues[closeValues.length - 2]) * 100
							/ closeValues[closeValues.length - 2];

			trend.setNowPercent(nowPercent);

			double volumeChangePercent = (NO_VALUES < 2 || volumes.length < 2
					|| volumes[closeValues.length - 2] == null) ? -9999
							: (trend.getVolume() - volumes[volumes.length - 2]) * 100 / volumes[volumes.length - 2];

			trend.setVolumeChangePercent(volumeChangePercent);

			int high10Index = Utils.getHighIndex(highValues, 10);

			trend.setHigh10(highValues[high10Index]);

			int high20Index = Utils.getHighIndex(highValues, NO_VALUES);

			trend.setHigh20(highValues[high20Index]);

			int low10Index = Utils.getLowIndex(lowValues, 10);

			trend.setLow10(lowValues[low10Index]);

			int low20Index = Utils.getLowIndex(lowValues, NO_VALUES);

			trend.setLow20(lowValues[low20Index]);

			trend.calculateHigh10Percenttage();

			trend.calculateHigh20Percenttage();

			trend.calculateLow10Percenttage();

			trend.calculateLow20Percenttage();

			trend.calculateLowHighDiff();

			trend.calculateLowHighDiffPercent();

			trend.setSma10(SMA10(closeValues));

			trend.setSmar(SMAR(trend.getNow(), trend.getSma10()));

			trend.setSupt(SUPT(closeValues, SURE));

			trend.setRest(REST(closeValues, SURE));

			trend.calculateSuptPercenttage();

			trend.calculatRestPercenttage();

			trend.calculateSRDiff();

			trend.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			trend.setHigh10Date(new Date(timestamps[high10Index] * 1000L));

			trend.setHigh20Date(new Date(timestamps[high20Index] * 1000L));

			trend.setLow10Date(new Date(timestamps[low10Index] * 1000L));

			trend.setLow20Date(new Date(timestamps[low20Index] * 1000L));

			trend.setTrend(getVolumeTrend(closeValues, trend.getLow10(), trend.getLow20(), trend.getLow10Date(),
					trend.getLow20Date(), trend.getHigh10(), trend.getHigh20(), trend.getHigh10Date(),
					trend.getHigh20Date(), timestamps, volumes, TREND_COUNT));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return trend;
	}

	private static double SMA10(Double[] closeValues) {

		double sum = 0;

		int count = 0;

		while (count < 10) {

			if (closeValues.length - count - 2 < 0) {

				break;
			}

			sum += closeValues[closeValues.length - count - 2];

			count++;
		}

		return sum / count;
	}

	private static String SMAR(Double now, double SMA10) {

		if (now > SMA10) {

			return "above";

		} else if (now < SMA10) {

			return "below";

		} else {

			return "flat";
		}
	}

	private static double SUPT(Double[] closeValues, int sure) {

		ArrayList<Double> sortedValues = new ArrayList<>();

		int count = 0;

		while (count < sure) {

			if (closeValues.length - count - 2 < 0) {

				break;
			}

			sortedValues.add(closeValues[closeValues.length - count - 2]);

			count++;
		}

		Collections.sort(sortedValues);

		Double sum = Double.valueOf(0);

		for (int i = 0; i < Math.min(sortedValues.size(), 4); i++) {

			sum += sortedValues.get(i);
		}

		return sum / Math.min(sortedValues.size(), 4);
	}

	private static double REST(Double[] closeValues, int sure) {

		ArrayList<Double> sortedValues = new ArrayList<>();

		int count = 0;

		while (count < sure) {

			if (closeValues.length - count - 2 < 0) {

				break;
			}

			sortedValues.add(closeValues[closeValues.length - count - 2]);

			count++;
		}

		Collections.sort(sortedValues);

		Double sum = Double.valueOf(0);

		for (int i = 0; i < Math.min(sortedValues.size(), 4); i++) {

			sum += sortedValues.get(sortedValues.size() - i - 1);
		}

		return sum / Math.min(sortedValues.size(), 4);
	}

	private static List<VolumeTrendData> getVolumeTrend(Double closeValues[], Double low10, Double low20,
			Date low10Date, Date low20Date, Double high10, Double high20, Date high10Date, Date high20Date,
			long timestamps[], Long volumes[], int TREND_COUNT) {

		ArrayList<VolumeTrendData> values = new ArrayList<>();

		int count = 0;

		if (closeValues.length >= TREND_COUNT) {

			while (count < TREND_COUNT) {

				if (closeValues.length <= count) {

					break;
				}

				values.add(new VolumeTrendData(closeValues[closeValues.length - count - 1], low10, low20, low10Date,
						low20Date, high10, high20, high10Date, high20Date,
						timestamps[timestamps.length - count - 1] * 1000L, volumes[volumes.length - count - 1]));

				count++;
			}
		}

		return values;
	}

	private static Comparator<VolumeTrend> getComparatorForKey(String key) {

		if (key.equalsIgnoreCase(SORT_KEYS[0])) {

			return new Comparator<VolumeTrend>() {

				@Override
				public int compare(VolumeTrend s1, VolumeTrend s2) {

					return Integer.compare(s1.getPriceRank(), s2.getPriceRank());
				}
			};

		} else if (key.equalsIgnoreCase(SORT_KEYS[1])) {

			return new Comparator<VolumeTrend>() {

				@Override
				public int compare(VolumeTrend s1, VolumeTrend s2) {

					return Integer.compare(s1.getVolumeRank(), s2.getVolumeRank());
				}
			};

		} else {

			return null;
		}
	}

	private static void writeTrendsToFile(String[] headers, List<VolumeTrend> stocks) {

		try {

			File file = Utils.generateOutputFile("voltrend", Utils.generateOutputDir());

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			String header = "";

			for (int i = 0; i < headers.length - 1; i++) {

				header += headers[i] + ",";
			}

			header += headers[headers.length - 1];

			writer.println(header);

			for (int i = 0; i < stocks.size(); i++) {

				String[] printableVolumes = stocks.get(i).toPrintableStrings(i + 1, VOLUME_DIFF);

				for (int j = 0; j < printableVolumes.length; j++) {

					String tmp = printableVolumes[j];

					tmp = tmp.replaceAll(",", "");

					writer.println(tmp.replaceAll("_", ","));
				}
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static void showProgress() {

		char[] animationChars = new char[] { '|', '/', '-', '\\' };

		int i = 0;

		while (processing) {

			System.out.print(" Processing: " + percentage + "% " + animationChars[i++ % 4] + "\r");

			try {

				Thread.sleep(100);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

		System.out.println("                                                                      ");
	}

}
