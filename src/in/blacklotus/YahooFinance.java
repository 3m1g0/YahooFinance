package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.Stock;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.NetworkUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class YahooFinance {

	private static final String OUTPUT_FILE_NAME = "stocks.csv";

	private static final String INPUT_FILE_NAME = "input.csv";

	public static void main(String[] args) {

		String[] stocks = null;

		if (args.length > 0) {

			stocks = args[0].split(",");

		} else {

			stocks = readInput();

		}

		if (stocks == null) {

			System.out.println("Please provide Stock names");

			return;
		}

		System.out.println("------------------------------------------------------------------------------------------");
		
		for(String stock: stocks) {
			
			System.out.print(stock + ",");
		}
		
		System.out.println();
		
		System.out.println("------------------------------------------------------------------------------------------");

		List<Stock> stocksList = new ArrayList<>();

		for (String stock : stocks) {

			stocksList.add(getStockDetails(stock));
		}

		writeToFile(stocksList);
		
		System.out.println("------------------------------------------------------------------------------------------");

		System.out.println("----------------------------- Finished creating CSV file ---------------------------------");
		
		System.out.println("------------------------------------------------------------------------------------------");
	}

	private static Stock getStockDetails(String stockName) {

		Stock stock = new Stock();

		double[] closeValues;
		
		double[] lowValues;
		
		double[] highValues;

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
			
			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();
			
			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			stock.setCurrency(metaData.getCurrency());

			stock.setSymbol(metaData.getSymbol());

			stock.setName(metaData.getExchangeName());

			stock.setClose(closeValues[closeValues.length - 1]);

			int highIndex = getHighIndex(highValues);

			stock.setHigh(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues);

			stock.setLow(lowValues[lowIndex]);

			stock.calculateHighPercenttage();

			stock.calculateLowPercenttage();

			stock.setHighDate(new Date(timestamps[highIndex] * 1000L));

			stock.setLowDate(new Date(timestamps[lowIndex] * 1000L));

			System.out.println(stock.toString().replaceAll(",", "\t"));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return stock;
	}

	private static String[] readInput() {

		String input = null;

		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(INPUT_FILE_NAME));

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

			File file = new File(OUTPUT_FILE_NAME);

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE_NAME));

			writer.println(
					"NAME,SYMBOL,CURRENCY,CLOSING-VALUE,HIGH-VALUE,HIGH-PERCENT,HIGH-DATE,LOW-VALUE,LOW-PERCENT,LOW-DATE");

			for (Stock stock : stocks) {

				writer.println(stock.toString());
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private static int getHighIndex(double[] data) {

		int max = 0;

		for (int i = 1; i < data.length; i++) {

			if (data[i] > data[max]) {

				max = i;
			}
		}

		return max;
	}

	private static int getLowIndex(double[] data) {

		int min = 0;

		for (int i = 1; i < data.length; i++) {

			if (data[i] < data[min]) {

				min = i;
			}
		}

		return min;
	}
}
