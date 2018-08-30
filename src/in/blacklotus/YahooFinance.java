package in.blacklotus;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.Stock;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Scheduler;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class YahooFinance {

	final static Map<String, List<String>> params = new HashMap<>();

	private static int NO_VALUES = 20;

	private static String SORT_KEY = "DEFAULT";

	private static String FILTER = null;

	private static int REPEAT = -1;

	private static final String[] SORT_KEYS = { "NOW", "LOW20", "HIGH20", "%LOW20", "%HIGH20", "%TODAY", "%MOVE",
			"%DIFFER" };

	private static final String HEADER = "SL.NO.,SYMBOL,NOW,LOW20,HIGH20,%(+/-) CHANGE TODAY, %(+/-) FROM LOW20,%(+/-) FROM HIGH20,LOW-DATE,HIGH-DATE,%MOVE,%DIFFER";

	private static final String INPUT_FILE_NAME = "input.csv";

	private static String[] percentages;

	private static String outputDir;

	private static String outputFileName;

	private static boolean processing = false;

	private static boolean refreshing = false;

	private static long nextUpdateTime = 0L;

	private static int percentage = 0;

	public static void main(String[] args) {

		List<String> symbolList = new ArrayList<>();

		List<String> percentageList = new ArrayList<>();

		String[] stocks = null;

		parseArguments(args);

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		if (params.containsKey("count")) {

			try {

				NO_VALUES = Integer.parseInt(params.get("count").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid COUNT value. Proceeding with default value 20   ***");
			}
		}

		if (params.containsKey("sort")) {

			SORT_KEY = params.get("sort").get(0);

			if (!isValidSortKey(SORT_KEY)) {

				System.out.println("***   Sort KEY must be one of " + Arrays.toString(SORT_KEYS) + "  ***");

				System.out.println("***   Proceeding without any sort option  ***");
			}
		}

		if (params.containsKey("filter")) {

			FILTER = params.get("filter").get(0);
		}

		if (params.containsKey("repeat")) {

			try {

				REPEAT = Integer.parseInt(params.get("repeat").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid REPEAT value. Aborting scheduler   ***");
			}
		}

		if (params.containsKey("symbol")) {

			stocks = params.get("symbol").toArray(new String[] {});

		} else {

			stocks = readInput();
		}

		if (stocks == null) {

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.println("Please provide Stock names either as input.csv or command line arguments");

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			return;
		}

		processData(symbolList, percentageList, stocks, false);

		if (REPEAT > 0) {
			
			nextUpdateTime = System.currentTimeMillis() + 60 * REPEAT * 1000;

			refreshing = true;

			showRefreshing();

			final String[] mStocks = stocks;

			Scheduler scheduler = new Scheduler(REPEAT, new Scheduler.SchedulerCallback() {

				@Override
				public void onTrigger() {

					nextUpdateTime = System.currentTimeMillis() + REPEAT * 60 * 1000;

					processData(symbolList, percentageList, mStocks, true);

					refreshing = true;

					showRefreshing();
				}
			});

			scheduler.start();

		}
	}

	private static void processData(List<String> symbolList, List<String> percentageList, String[] stocks,
			boolean applyRepeat) {

		symbolList.clear();

		percentageList.clear();

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		System.out.print("Fetching details for: ");

		for (int i = 0; i < stocks.length; i++) {

			String stock = stocks[i];

			stock = stock.trim();

			if (!"".equals(stock)) {

				symbolList.add(stock);

				if (percentages != null && i < percentages.length) {

					percentageList.add(percentages[i]);

					System.out.print(percentages[i] + "-");
				}

				System.out.print(stock + ",");
			}
		}

		System.out.println();

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		List<Stock> stocksList = new ArrayList<>();

		refreshing = false;

		processing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				showProgress();
			}

		}).start();

		for (int i = 0; i < symbolList.size(); i++) {

			String stock = symbolList.get(i);

			Stock stockDetail = getStockDetails(stock);

			if (stockDetail != null) {

				if (stockDetail.applyFilter(FILTER)) {

					if (applyRepeat && REPEAT > 0) {

						if (stockDetail.applyRepeat(percentageList.get(i))) {

							stocksList.add(stockDetail);
						}

					} else {

						stocksList.add(stockDetail);
					}
				}
			}

			percentage = (i + 1) * 100 / symbolList.size();
		}

		processing = false;

		if (!"DEFAULT".equals(SORT_KEY)) {

			Collections.sort(stocksList, new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					if (SORT_KEYS[0].equals(SORT_KEY)) {

						return Double.compare(s1.getNow(), s2.getNow());

					} else if (SORT_KEYS[1].equals(SORT_KEY)) {

						return Double.compare(s1.getLow(), s2.getLow());

					} else if (SORT_KEYS[2].equals(SORT_KEY)) {

						return Double.compare(s1.getHigh(), s2.getHigh());

					} else if (SORT_KEYS[3].equals(SORT_KEY)) {

						return Double.compare(s1.getLowPercent(), s2.getLowPercent());

					} else if (SORT_KEYS[4].equals(SORT_KEY)) {

						return Double.compare(s1.getHighPercent(), s2.getHighPercent());

					} else if (SORT_KEYS[5].equals(SORT_KEY)) {

						return Double.compare(s1.getNowPercent(), s2.getNowPercent());

					} else if (SORT_KEYS[6].equals(SORT_KEY)) {

						return Double.compare(s1.getMove(), s2.getMove());

					} else if (SORT_KEYS[7].equals(SORT_KEY)) {

						return Double.compare(s1.getDiffer(), s2.getDiffer());

					} else {

						return s1.getSymbol().compareTo(s2.getSymbol());
					}
				}
			});
		}

		String[] headers = HEADER.split(",");

		String[][] data = new String[stocksList.size()][];

		for (int i = 0; i < stocksList.size(); i++) {

			data[i] = stocksList.get(i).toPrintableString(i + 1).split(",");
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));
		
		if(applyRepeat) {
			
//			Utils.sendEmail(FlipTableConverters.fromObjects(headers, data));
			
			if (SystemTray.isSupported()) {
	            try {
					Utils.displayTray();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (AWTException e) {
					e.printStackTrace();
				}
	        } else {
	            System.err.println("System tray not supported!");
	        }
		}

		writeToFile(stocksList);
	}

	private static Stock getStockDetails(String stockName) {

		Stock stock = new Stock();

		Double[] closeValues;

		Double[] lowValues;

		Double[] highValues;

		long[] timestamps;

		Metadata metaData;

		YahooFinanceAPI service = NetworkUtils.getYahooFinanceAPIService();

		Call<ResponseBody> data = service.getStockInfo(stockName);

		try {

			String responseString = data.execute().body().string();

			YahooResponse yahooResponse = new Gson().fromJson(responseString, YahooResponse.class);

			metaData = yahooResponse.getChart().getResult()[0].getMeta();

			timestamps = yahooResponse.getChart().getResult()[0].getTimestamp();

			closeValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getClose();

			if (closeValues == null) {

				return null;
			} else if (closeValues.length < NO_VALUES) {

				NO_VALUES = closeValues.length;
			}

			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();

			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			stock.setCurrency(metaData.getCurrency());

			stock.setSymbol(metaData.getSymbol());

			stock.setName(metaData.getExchangeName());

			stock.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			double nowPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? -9999
					: (stock.getNow() - closeValues[closeValues.length - 2]) * 100
							/ closeValues[closeValues.length - 2];

			stock.setNowPercent(nowPercent);

			int highIndex = getHighIndex(highValues);

			stock.setHigh(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues);

			stock.setLow(lowValues[lowIndex]);

			stock.calculateHighPercenttage();

			stock.calculateLowPercenttage();

			stock.calculateDiffer();

			stock.calculateMove();

			stock.setHighDate(new Date(timestamps[highIndex] * 1000L));

			stock.setLowDate(new Date(timestamps[lowIndex] * 1000L));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return stock;
	}

	private static String[] readInput() {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		String input1 = null;

		String input2 = null;

		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(inputFile));

			input1 = reader.readLine();

			input2 = reader.readLine();

			reader.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

		if (input1 != null) {

			if (input2 != null) {

				percentages = input2.split(",");
			}

			return input1.split(",");

		} else {

			return null;
		}

	}

	private static void writeToFile(List<Stock> stocks) {

		try {

			generateOutputDir();

			generateOutputFile();

			File file = new File(outputDir, outputFileName);

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			writer.println(HEADER);

			for (int i = 0; i < stocks.size(); i++) {

				Stock stock = stocks.get(i);

				writer.println(stock.toPrintableString(i + 1));
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static int getHighIndex(Double[] data) {

		long count = 0;

		int max = data.length - 1;

		int index = data.length - 2;

		while (index > -1 && count < NO_VALUES) {

			if (data[index] == null) {

				index--;

				continue;
			}

			if (data[index] > data[max]) {

				max = index;

			}

			count++;

			index--;
		}

		return max;
	}

	private static int getLowIndex(Double[] data) {

		int count = 0;

		int min = data.length - 1;

		int index = data.length - 2;

		while (index > -1 && count < NO_VALUES) {

			if (data[index] == null) {

				index--;

				continue;
			}

			if (data[index] < data[min]) {

				min = index;

			}

			count++;

			index--;
		}

		return min;
	}

	private static void generateOutputDir() {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy");

		outputDir = sdf.format(new Date());

		File dir = new File(outputDir);

		if (!dir.exists()) {

			dir.mkdir();
		}
	}

	private static void generateOutputFile() {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy_hh_mm_aaa");

		outputFileName = "20Day_" + sdf.format(new Date()) + ".csv";

		File outputFile = new File(outputDir, outputFileName);

		if (!outputFile.exists()) {

			try {

				outputFile.createNewFile();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private static void parseArguments(String args[]) {

		List<String> options = null;

		for (int i = 0; i < args.length; i++) {

			final String a = args[i];

			if (a.charAt(0) == '-') {

				if (a.length() < 2) {

					System.err.println("Error at argument " + a);

					return;
				}

				options = new ArrayList<>();

				params.put(a.substring(1), options);

			} else if (options != null) {

				options.add(a);

			} else {

				System.err.println("Illegal parameter usage");

				return;
			}
		}
	}

	private static boolean isValidSortKey(String key) {

		for (String k : SORT_KEYS) {

			if (k.equals(key))

				return true;
		}

		return false;
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

		System.out.println("                          ");
	}

	private static void showRefreshing() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				while (refreshing) {

					String duration = Utils.toDuration(nextUpdateTime - System.currentTimeMillis());
					
					System.out.print(" Refreshing in " + duration + "                          \r");

					try {

						Thread.sleep(100);

					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}

				System.out.println("                          ");
			}

		}).start();
	}
}
