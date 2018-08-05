package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.Stock;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.NetworkUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class YahooFinance {

	private static int NO_VALUES = 20;

	private static final String INPUT_FILE_NAME = "input.csv";

	private static String outputDir;

	private static String outputFileName;

	public static void main(String[] args) {

		List<String> symbolList = new ArrayList<>();

		String[] stocks = null;

		if (args.length > 0) {

			NO_VALUES = Integer.parseInt(args[0]);

		}

		if (args.length > 1) {

			stocks = args[1].split(",");

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

		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		for (String stock : stocks) {

			stock = stock.trim();

			if (!"".equals(stock)) {

				symbolList.add(stock);

				System.out.print(stock + ",");
			}
		}

		System.out.println();

//		System.out.println(
//				"--------------------------------------------------------------------------------------------------------");
//
//		System.out.println("SYMBOL\tNOW\tLOW20\tHIGH20\t%TODAY\t%LOW\t%HIGH\tLOW-DATE\tHIGH-DATE\t%MOVE\t%DIFFER");
//
//		System.out.println(
//				"--------------------------------------------------------------------------------------------------------");

		List<Stock> stocksList = new ArrayList<>();

		for (String stock : symbolList) {

			Stock stockDetail = getStockDetails(stock);

			if (stockDetail != null) {

				stocksList.add(stockDetail);
			}
		}
		
		String[] headers = { "SYMBOL", "NOW", "LOW20", "HIGH20", "%CHANGE TODAY", "%LOW", "%HIGH", "LOW-DATE", "HIGH-DATE", "%MOVE", "%DIFFER" };
		String[][] data = new String[stocksList.size()][];
		for(int i = 0; i < stocksList.size(); i++) {
			data[i] = stocksList.get(i).toString().split(",");
		}
		System.out.println(FlipTableConverters.fromObjects(headers, data));

		writeToFile(stocksList);

//		System.out.println(
//				"--------------------------------------------------------------------------------------------------------");
//
//		System.out.println(
//				"------------------------------------ Finished creating CSV file ----------------------------------------");
//
//		System.out.println(
//				"--------------------------------------------------------------------------------------------------------");
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

			// System.out.println(responseString);

			YahooResponse yahooResponse = new Gson().fromJson(responseString, YahooResponse.class);

			// System.out.println(yahooResponse.toString());

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

			stock.setNow(closeValues[closeValues.length - 1] == null ? 0 : closeValues[closeValues.length - 1]);

			double nowPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? 0
					: (closeValues[closeValues.length - 2] - stock.getNow()) * 100
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

//			System.out.println(stock.toPrintableString().replaceAll(",", "\t"));

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

			return input.split(",");

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

			writer.println(
					"SYMBOL,NOW,LOW20,HIGH20,%(+/-) CHANGE TODAY, %(+/-) FROM LOW20,%(+/-) FROM HIGH20,LOW-DATE,HIGH-DATE,%MOVE,%DIFFER");

			for (Stock stock : stocks) {

				writer.println(stock.toString());
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static int getHighIndex(Double[] data) {

		int max = data.length - NO_VALUES;

		for (int i = data.length - NO_VALUES + 1; i < data.length; i++) {

			if (data[i] == null) {

				continue;
			}

			if (data[i] > data[max]) {

				max = i;
			}
		}

		return max;
	}

	private static int getLowIndex(Double[] data) {

		int min = data.length - NO_VALUES;

		for (int i = data.length - NO_VALUES + 1; i < data.length; i++) {

			if (data[i] == null) {

				continue;
			}

			if (data[i] < data[min]) {

				min = i;
			}
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
}
