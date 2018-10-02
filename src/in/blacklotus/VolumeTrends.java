package in.blacklotus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class VolumeTrends {

	private static final String INPUT_FILE_NAME = "input.csv";

	private static int NO_VALUES = 20;

	private static String SORT_KEY = "DEFAULT";

	private static final String[] SORT_KEYS = { "VOLUME" };

	private static String TREND = null;

	private static int TREND_COUNT = 2;

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

			SORT_KEY = params.get("sort").get(0);

			if (!Utils.isValidSortKey(SORT_KEY, SORT_KEYS)) {

				System.out.println("***   Sort KEY must be one of " + Arrays.toString(SORT_KEYS) + "  ***");

				System.out.println("***   Proceeding without any sort option  ***");
			}
		}

		if (params.containsKey("trend")) {

			TREND = params.get("trend").get(0);
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

					if (trendDetail.isValidVolumeTrend(TREND_COUNT)) {

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

		String[] headers = new String[] { "#", "SYMBOL", "PRICE", "PRICEDIFF", "%PRICECHANGE", "VOLUME", "VOLUMEDIFF",
				"%VOLUMECHANGE", "VolR" };

		List<String[]> tmp = new ArrayList<>();

		for (int i = 0; i < processedTrendList.size(); i++) {

			processedTrendList.get(i).assignData();

			if ("VOLUME".equals(SORT_KEY)) {

				processedTrendList.get(i).sortByRank();
			}

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

			int highIndex = Utils.getHighIndex(highValues, NO_VALUES);

			trend.setHigh(highValues[highIndex]);

			int lowIndex = Utils.getLowIndex(lowValues, NO_VALUES);

			trend.setLow(lowValues[lowIndex]);

			trend.calculateHighPercenttage();

			trend.calculateLowPercenttage();

			trend.calculateMove();

			trend.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			trend.setHighDate(new Date(timestamps[highIndex] * 1000L));

			trend.setLowDate(new Date(timestamps[lowIndex] * 1000L));

			trend.setTrend(getVolumeTrend(closeValues, trend.getLow(), trend.getLowDate(), trend.getHigh(),
					trend.getHighDate(), timestamps, volumes, TREND_COUNT));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return trend;
	}

	private static List<VolumeTrendData> getVolumeTrend(Double closeValues[], Double low20, Date lowDate, Double high20,
			Date highDate, long timestamps[], Long volumes[], int TREND_COUNT) {

		ArrayList<VolumeTrendData> values = new ArrayList<>();

		int count = 0;

		if (closeValues.length >= TREND_COUNT) {

			while (count < TREND_COUNT) {

				if (closeValues.length <= count) {

					break;
				}

				values.add(new VolumeTrendData(closeValues[closeValues.length - count - 1], low20, lowDate, high20,
						highDate, timestamps[timestamps.length - count - 1] * 1000L,
						volumes[volumes.length - count - 1]));

				count++;
			}
		}

		return values;
	}

	private static void writeTrendsToFile(String[] headers, List<VolumeTrend> stocks) {

		try {

			File file = Utils.generateOutputFile("voltrends", Utils.generateOutputDir());

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
