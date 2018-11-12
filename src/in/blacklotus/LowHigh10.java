package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.PriceTrend;
import in.blacklotus.model.PriceTrendData;
import in.blacklotus.model.Stock;
import in.blacklotus.model.Symbol;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.DatabaseUtils;
import in.blacklotus.utils.MultiComparator;
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Scheduler;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class LowHigh10 {

	final static Map<String, List<String>> params = new HashMap<>();

	private static int NO_VALUES = 10;

	private static List<String> SORT_KEY;

	private static String FILTER = null;

	private static String REPEAT = null;

	private static String TREND = null;

	private static int TREND_COUNT = 3;

	private static String SMAR = null;

	private static int SURE = 10;

	private static int DROP = Integer.MIN_VALUE;

	private static String LTEN;

	private static int[] LOHIDIF;

	private static int CENT = Integer.MAX_VALUE;

	private static int repeat = -1;

	private static final String[] SORT_KEYS = { "PRICE", "LOW10", "HIGH10", "%LOW10", "%HIGH10", "%TODAY", "%LOHIDIF",
			"PriR", "VolR", "SMAR" };

	private static final String HEADER = "SNO,SYMBOL,LOW10,PRICE,HIGH10,$PRICAGE,%NOW,$LOHIDIF,SUPT,REST,$SRDIF,%SUPT,%REST,SURE,%LOW10,%HIGH10,%LOHIDIF,%VOLCAGE,TENDCHG,%TENDCHG,VolR,PriR,TICKER";

	private static final String INPUT_FILE_NAME = "input.csv";

	private static final String INPUT_SCHEDULER_FILE_NAME = "input-scheduler.csv";

	private static String outputDir;

	private static String outputFileName;

	private static boolean processing = false;

	private static boolean refreshing = false;

	private static long nextUpdateTime = 0L;

	private static int percentage = 0;

	private static ArrayList<String> errorList = new ArrayList<>();

	public static void main(String[] args) {

		final List<Symbol> symbolList = new ArrayList<>();

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

			SORT_KEY = params.get("sort");

			SORT_KEY = Utils.getValidSortKeys(SORT_KEY, SORT_KEYS);
		}

		if (params.containsKey("filter")) {

			FILTER = params.get("filter").get(0);
		}

		if (params.containsKey("smar")) {

			SMAR = params.get("smar").get(0);
		}

		if (params.containsKey("trend")) {

			TREND = params.get("trend").get(0);

			TREND_COUNT = NO_VALUES;

			NO_VALUES = 20;
		}

		if (params.containsKey("repeat")) {

			try {

				REPEAT = params.get("repeat").get(0);

				String temp = REPEAT.replace("m", "");

				temp = temp.replace("h", "");

				temp = temp.replace("d", "");

				repeat = Integer.parseInt(temp);

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid REPEAT value. Aborting scheduler   ***");
			}
		}

		if (params.containsKey("drop")) {

			try {

				DROP = Integer.parseInt(params.get("drop").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid DROP value. Proceeding with default value -1   ***");
			}
		}

		if (params.containsKey("lten")) {

			LTEN = params.get("lten").get(0);
		}

		if (params.containsKey("lohidif")) {

			List<String> temp = params.get("lohidif");

			if (temp.size() == 2) {

				int low = -1, high = -1;

				try {

					low = Integer.parseInt(temp.get(0));

					high = Integer.parseInt(temp.get(1));

					LOHIDIF = new int[2];

					LOHIDIF[0] = low;

					LOHIDIF[1] = high;

				} catch (NumberFormatException e) {

					System.out.println("***   Invalid LOHIDIF value. Proceeding without filter  ***");
				}
			}

		}

		if (params.containsKey("sure")) {

			try {

				SURE = Integer.parseInt(params.get("sure").get(0));

			} catch (NumberFormatException e) {

				System.out.println("***   Invalid SURE value. Proceeding with default value 10   ***");
			}
		}

		if (params.containsKey("cent")) {

			try {

				CENT = Integer.parseInt(params.get("cent").get(0));

			} catch (NumberFormatException e) {

				System.out.println(
						"***   Invalid CENT value. Proceeding with default value" + Integer.MAX_VALUE + "   ***");
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

			if (REPEAT != null) {

				symbolList.addAll(readSchedulerInput());

			} else {

				symbolList.addAll(readInput());
			}
		}

		if (symbolList.isEmpty()) {

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.println("Please provide Stock names either as input.csv or command line arguments");

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			return;
		}

		if (repeat > 0) {

			nextUpdateTime = System.currentTimeMillis() + calculateInterval();

			refreshing = true;

			showRefreshing();

			Scheduler scheduler = new Scheduler(repeat, getTimeUnit(), new Scheduler.SchedulerCallback() {

				@Override
				public void onTrigger() {

					nextUpdateTime = System.currentTimeMillis() + calculateInterval();

					processRepeatData(symbolList);

					refreshing = true;

					showRefreshing();
				}
			});

			scheduler.start();

		} else if (TREND != null) {

			processTrendData(symbolList);

		} else {

			processData(symbolList);
		}

		if (!errorList.isEmpty()) {

			writeErrorsToFile();
		}
	}

	private static void processData(List<Symbol> symbolList) {

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

			Symbol symbol = symbolList.get(i);

			try {
				Stock stockDetail = getStockDetails(symbol.getName());

				if (stockDetail != null) {

					boolean filter = true;

					filter = filter && stockDetail.applyFilter(FILTER);

					if (DROP > Integer.MIN_VALUE) {

						filter = filter && stockDetail.applyDropFilter(DROP);
					}

					if (CENT < Integer.MAX_VALUE) {

						filter = filter && stockDetail.applyCentFilter(CENT);
					}

					if (LTEN != null) {

						filter = filter && stockDetail.applyLtenFilter(LTEN);
					}

					if (LOHIDIF != null) {

						filter = filter && stockDetail.applyLoHiDifFilter(LOHIDIF);
					}

					if (SMAR != null) {

						filter = filter && stockDetail.applySMARFilter(SMAR);
					}

					if (filter) {

						stocksList.add(stockDetail);
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

		if (SORT_KEY != null) {

			List<Comparator<Stock>> comparators = new ArrayList<>();

			for (String key : SORT_KEY) {

				Comparator<Stock> comparatorForKey = getComparatorForKey(key);

				if (comparatorForKey != null) {

					comparators.add(comparatorForKey);
				}
			}

			MultiComparator.sort(stocksList, comparators);
		}

		String[] headers = HEADER.split(",");

		String[][] data = new String[stocksList.size()][];

		for (int i = 0; i < stocksList.size(); i++) {

			data[i] = stocksList.get(i).toPrintableString(i + 1).split(",");
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));

		writeToFile(stocksList);

		if (NadoPicks.SAVE_TO_DATABASE) {

			DatabaseUtils.saveLowHigh10(stocksList);
		}
	}

	private static void processRepeatData(List<Symbol> symbolList) {

		List<Symbol> processedSymbolList = new ArrayList<>();

		refreshing = false;

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
				Stock stockDetail = getStockDetails(symbol.getName());

				if (stockDetail != null) {

					if (stockDetail.applyRepeatFilter(symbol)) {

						symbol.setNow(stockDetail.getNow());

						processedSymbolList.add(symbol);
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

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy hh:mm aaa");

		String[] headers = new String[] { "#", sdf.format(new Date()) };

		String[][] data = new String[processedSymbolList.size()][];

		for (int i = 0; i < processedSymbolList.size(); i++) {

			data[i] = processedSymbolList.get(i).toPrintableString(i + 1).split(",");
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));

		Utils.displayTray(headers, data);
	}

	private static void processTrendData(List<Symbol> symbolList) {

		List<PriceTrend> processedTrendList = new ArrayList<>();

		refreshing = false;

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
				PriceTrend trendDetail = getTrendDetails(symbol.getName());

				if (trendDetail != null) {

					if (trendDetail.isValidPriceTrend(TREND_COUNT)) {

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

		String[] headers = new String[] { "#", "SYMBOL", "LOW20", "HIGH20", TREND.toUpperCase() + "TREND", "$CHANGE",
				"%$CHANGE", "VOLUME", "%VOLCAGE" };

		List<String[]> tmp = new ArrayList<>();

		for (int i = 0; i < processedTrendList.size(); i++) {

			for (int j = 0; j < processedTrendList.get(i).toPrintableStrings(i + 1).length; j++) {

				tmp.add(processedTrendList.get(i).toPrintableStrings(i + 1)[j].split("_"));
			}
		}

		String[][] data = new String[tmp.size()][];

		for (int i = 0; i < tmp.size(); i++) {

			data[i] = tmp.get(i);
		}

		System.out.println(FlipTableConverters.fromObjects(headers, data));

		writeTrendsToFile(headers, processedTrendList);
	}

	private static Stock getStockDetails(String stockName) {

		Stock stock = new Stock();

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

				return new Stock(stockName);
			}

			String responseString = execute.body().string();

			YahooResponse yahooResponse;

			if (NadoPicks.responseMap.get(stockName) == null) {

				NadoPicks.responseMap.put(stockName, new Gson().fromJson(responseString, YahooResponse.class));

			}

			yahooResponse = NadoPicks.responseMap.get(stockName);

			metaData = yahooResponse.getChart().getResult()[0].getMeta();

			timestamps = yahooResponse.getChart().getResult()[0].getTimestamp();

			closeValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getClose();

			if (closeValues == null) {

				return null;

			} else if (closeValues.length < NO_VALUES) {

				NO_VALUES = closeValues.length;
			}

			volumes = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getVolume();

			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();

			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			stock.setCurrency(metaData.getCurrency());

			stock.setSymbol(metaData.getSymbol());

			stock.setName(metaData.getExchangeName());

			stock.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			stock.setVolume(volumes[volumes.length - 1]);

			double pricage = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? -Integer.MIN_VALUE : (stock.getNow() - closeValues[closeValues.length - 2]);

			stock.setPricage(pricage);

			double nowPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? -Integer.MIN_VALUE
					: (stock.getNow() - closeValues[closeValues.length - 2]) * 100
							/ closeValues[closeValues.length - 2];

			stock.setNowPercent(nowPercent);

			double dchg = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null
					|| NO_VALUES < 10 || closeValues.length < 10 || closeValues[closeValues.length - 10] == null)
							? Integer.MIN_VALUE
							: (closeValues[closeValues.length - 2] - closeValues[closeValues.length - 10]);

			stock.setDchg10(dchg);

			double dchgPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null
					|| NO_VALUES < 10 || closeValues.length < 10 || closeValues[closeValues.length - 10] == null)
							? Integer.MIN_VALUE
							: (closeValues[closeValues.length - 2] - closeValues[closeValues.length - 10]) * 100
									/ closeValues[closeValues.length - 2];

			stock.setDchgPercent(dchgPercent);

			double volumeChangePercent = (NO_VALUES < 2 || volumes.length < 2
					|| volumes[closeValues.length - 2] == null) ? -9999
							: (stock.getVolume() - volumes[volumes.length - 2]) * 100 / volumes[volumes.length - 2];

			stock.setVolumeChangePercent(volumeChangePercent);

			int highIndex = getHighIndex(highValues);

			stock.setHigh10Index(highValues.length - highIndex);

			stock.setHigh10(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues);

			stock.setLow10Index(lowValues.length - lowIndex);

			stock.setLow10(lowValues[lowIndex]);

			stock.calculateHigh10Percenttage();

			stock.calculateLow10Percenttage();

			stock.calculateLowHighDiff();

			stock.calculateLowHighDiffPercent();

			stock.setSma10(SMA10(closeValues));

			stock.setSupt(SUPT(closeValues, SURE));

			stock.setRest(REST(closeValues, SURE));

			stock.setSmar(SMAR(stock.getNow(), stock.getSupt(), stock.getRest()));

			stock.calculateSuptPercenttage();

			stock.calculatRestPercenttage();

			stock.calculateSRDiff();

			stock.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			stock.setHigh10Date(new Date(timestamps[highIndex] * 1000L));

			stock.setLow10Date(new Date(timestamps[lowIndex] * 1000L));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return stock;
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

	private static String SMAR(Double now, double SUPT, double REST) {

		if (now > SUPT) {

			return "Above";

		} else if (now < SUPT) {

			return "Below";

		} else {

			if (now > REST) {

				return "Match | Resist";

			} else {

				return "Match";
			}
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

	private static PriceTrend getTrendDetails(String stockName) {

		PriceTrend trend = new PriceTrend();

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

				return new PriceTrend(stockName);
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

			int highIndex = getHighIndex(highValues);

			trend.setHigh20(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues);

			trend.setLow20(lowValues[lowIndex]);

			trend.calculateHigh20Percenttage();

			trend.calculateLow20Percenttage();

			trend.calculateLowHighDiffPercent();

			trend.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			trend.setHigh20Date(new Date(timestamps[highIndex] * 1000L));

			trend.setLow20Date(new Date(timestamps[lowIndex] * 1000L));

			trend.setTrend(getTrend(closeValues, trend.getLow20(), trend.getLow20Date(), trend.getHigh20(),
					trend.getHigh20Date(), timestamps, volumes));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return trend;
	}

	private static Comparator<Stock> getComparatorForKey(String key) {

		if (SORT_KEYS[0].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getNow(), s2.getNow());
				}
			};

		} else if (SORT_KEYS[1].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getLow10(), s2.getLow10());
				}
			};

		} else if (SORT_KEYS[2].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getHigh10(), s2.getHigh10());
				}
			};

		} else if (SORT_KEYS[3].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getLow10Percent(), s2.getLow10Percent());
				}
			};

		} else if (SORT_KEYS[4].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getHigh10Percent(), s2.getHigh10Percent());
				}
			};

		} else if (SORT_KEYS[5].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getNowPercent(), s2.getNowPercent());
				}
			};

		} else if (SORT_KEYS[6].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.getLowHighDiffPercent(), s2.getLowHighDiffPercent());
				}
			};

		} else if (SORT_KEYS[7].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.priceRank(), s2.priceRank());
				}
			};

		} else if (SORT_KEYS[8].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return Double.compare(s1.volumeRank(), s2.volumeRank());
				}
			};

		} else if (SORT_KEYS[9].equalsIgnoreCase(key)) {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					if (s1.getSmar() == null) {

						return 1;

					} else if (s2.getSmar() == null) {

						return -1;

					} else {

						return s1.getSmar().compareTo(s2.getSmar());
					}
				}
			};

		} else {

			return new Comparator<Stock>() {

				@Override
				public int compare(Stock s1, Stock s2) {

					return s1.getSymbol().compareTo(s2.getSymbol());
				}
			};
		}
	}

	private static List<Symbol> readInput() {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		List<Symbol> inputList = new ArrayList<>();

		String input = null;

		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(inputFile));

			input = reader.readLine();

			reader.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

		if (input != null) {

			String split[] = input.split(",");

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.print("Fetching details for: ");

			for (int i = 0; i < split.length; i++) {

				String stock = split[i];

				stock = stock.trim();

				if (!"".equals(stock)) {

					Symbol symbol = new Symbol(stock);

					inputList.add(symbol);

					System.out.print(stock + ",");
				}
			}

			System.out.println();

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");
		}

		return inputList;
	}

	private static List<Symbol> readSchedulerInput() {

		File inputFile = new File(INPUT_SCHEDULER_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		List<Symbol> inputList = new ArrayList<>();

		String input = null;

		BufferedReader reader;

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		System.out.print("Fetching details for: ");

		try {

			reader = new BufferedReader(new FileReader(inputFile));

			input = reader.readLine();

			while (input != null) {

				String split[] = input.split(",");

				if (split != null && split.length == 3 && !"".equals(split[0].trim())) {

					try {

						Symbol symbol = new Symbol(split[0].trim(), split[1], split[2]);

						inputList.add(symbol);

						System.out.print(split[0] + ",");

					} catch (Exception e) {

					}

					input = reader.readLine();
				}
			}

			reader.close();

			System.out.println();

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

		} catch (IOException e) {

			e.printStackTrace();
		}

		return inputList;
	}

	private static void writeToFile(List<Stock> stocks) {

		try {

			generateOutputDir();

			generateOutputFile("10Day_");

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

	private static void writeTrendsToFile(String[] headers, List<PriceTrend> stocks) {

		try {

			generateOutputDir();

			generateOutputFile("pricetrends");

			File file = new File(outputDir, outputFileName);

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

				for (int j = 0; j < stocks.get(i).toPrintableStrings(i + 1).length; j++) {

					String tmp = stocks.get(i).toPrintableStrings(i + 1)[j];

					tmp = tmp.replaceAll(",", "");

					writer.println(tmp.replaceAll("_", ","));
				}
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static void writeErrorsToFile() {

		try {

			generateOutputDir();

			generateOutputFile("errors_");

			File file = new File(outputDir, outputFileName);

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			for (int i = 0; i < errorList.size(); i++) {

				writer.println(errorList.get(i));
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

	private static List<PriceTrendData> getTrend(Double closeValues[], Double low20, Date lowDate, Double high20,
			Date highDate, long timestamps[], Long volumes[]) {

		ArrayList<PriceTrendData> values = new ArrayList<>();

		int count = 0;

		if (closeValues.length >= TREND_COUNT) {

			while (count < 10) {

				if (closeValues.length <= count) {

					break;
				}

				// values.add(new PriceTrendData(closeValues[closeValues.length
				// - count - 1], low20, lowDate, high20, highDate,
				// timestamps[timestamps.length - count - 1] * 1000L,
				// volumes[volumes.length - count - 1]));

				count++;
			}
		}

		return values;
	}

	private static void generateOutputDir() {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy");

		outputDir = sdf.format(new Date());

		File dir = new File(outputDir);

		if (!dir.exists()) {

			dir.mkdir();
		}
	}

	private static void generateOutputFile(String type) {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy_hh_mm_aaa");

		outputFileName = type + sdf.format(new Date()) + ".csv";

		File outputFile = new File(outputDir, outputFileName);

		if (!outputFile.exists()) {

			try {

				outputFile.createNewFile();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private static TimeUnit getTimeUnit() {

		if (REPEAT.endsWith("m")) {

			return TimeUnit.MINUTES;

		} else if (REPEAT.endsWith("h")) {

			return TimeUnit.HOURS;

		} else if (REPEAT.endsWith("d")) {

			return TimeUnit.DAYS;

		} else {

			return TimeUnit.MINUTES;
		}
	}

	private static long calculateInterval() {

		if (REPEAT.endsWith("m")) {

			return 60 * repeat * 1000;

		} else if (REPEAT.endsWith("h")) {

			return 60 * 60 * repeat * 1000;

		} else if (REPEAT.endsWith("d")) {

			return 24 * 60 * 60 * repeat * 1000;

		} else {

			return 60 * repeat * 1000;
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

				System.out.println("                                                                           ");
			}

		}).start();
	}
}
