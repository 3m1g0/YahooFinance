package in.blacklotus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.Stock;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.DatabaseUtils;
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Openings {

	private static Date mDate;
	
	private static String type = null;

	private static ArrayList<String> errorList = new ArrayList<>();

	private static boolean processing = false;

	private static int percentage = 0;

	public static void main(String[] args) {

		Map<String, List<String>> params = Utils.parseArguments(args);
		
		if (params.containsKey("type")) {

			type = params.get("type").get(0);
		}

		if (params.containsKey("date")) {

			String inputDate = params.get("date").get(0);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			try {

				mDate = sdf.parse(inputDate);
				
			} catch (ParseException e) {

				System.out.println("*** Enter the date in yyyy-MM-dd format ***");
			}

		} else {

			Calendar c = Calendar.getInstance();

			c.add(Calendar.DAY_OF_MONTH, -1);
			
			mDate = c.getTime();
		}

		saveOpenValues();
	}

	public static void saveOpenValues() {

		ArrayList<String> files = new ArrayList<>();

		processing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {

				showProgress();
			}

		}).start();

		String outputDir = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy");

			outputDir = sdf.format(mDate);

			Files.find(Paths.get(outputDir), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())
					.forEach(f -> files.add(f.toString()));

			HashSet<String> mSymbols = new HashSet<>();

			for (String path : files) {
				
				if (!path.contains("error")) {

					if (type == null || path.contains(type)) {
						
						List<String> symbols = Utils.readSymbolsFromOutputCSV(new File(path));

						mSymbols.addAll(symbols);
					}
				}
			}

			mSymbols.remove(" symbol may be delisted");

			List<Stock> stocks = new ArrayList<>();

			int index = 0;

			for (String symbol : mSymbols) {
				
				Stock stockDetails = getStockDetails(symbol);

				stocks.add(stockDetails);

				index++;

				percentage = (index) * 100 / mSymbols.size();
			}

			if (!stocks.isEmpty()) {

				DatabaseUtils.saveOpeningValue(stocks);
			}

		} catch (NoSuchFileException e) {

			System.out.println("No Symbols found for the date " + outputDir);

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			processing = false;
		}
	}

	private static Stock getStockDetails(String stockName) {

		Stock stock = new Stock();

		Double[] closeValues;

		Double[] openValues;

		Long[] volumes;

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

			if (Unifier.responseMap.get(stockName) == null) {

				Unifier.responseMap.put(stockName, new Gson().fromJson(responseString, YahooResponse.class));

			}

			yahooResponse = Unifier.responseMap.get(stockName);

			metaData = yahooResponse.getChart().getResult()[0].getMeta();

			closeValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getClose();

			if (closeValues == null) {

				return null;

			}

			openValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getOpen();

			volumes = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getVolume();

			stock.setCurrency(metaData.getCurrency());

			stock.setSymbol(metaData.getSymbol());

			stock.setName(metaData.getExchangeName());

			stock.setOpen(openValues[openValues.length - 1] == null ? -9999 : openValues[openValues.length - 1]);

			stock.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			stock.setVolume(volumes[volumes.length - 1]);

		} catch (IOException e) {

			e.printStackTrace();
		}

		return stock;
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
