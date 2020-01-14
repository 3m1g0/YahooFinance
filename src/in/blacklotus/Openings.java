package in.blacklotus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import in.blacklotus.model.Opening;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.DatabaseUtils;
import in.blacklotus.utils.NetworkUtils;
import in.blacklotus.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Openings {

	public static boolean SAVE_TO_DATABASE = false;

	private static final String HEADER = "SNO,SYMBOL,OPEN,CLOSE,LOW,HIGH,GAINCLOSE,%GAINCLOSE,GAINHIGH,%GAINHIGH,%VOLCAGE2";

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

		if (params.containsKey("db")) {

			try {

				SAVE_TO_DATABASE = Boolean.parseBoolean(params.get("db").get(0));

			} catch (Exception e) {

				System.out.println("***   Invalid DB flag. Proceeding without saving to database   ***");
			}
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

			HashSet<String> lowHighSymbols = new HashSet<>();

			HashSet<String> pricetrendSymbols = new HashSet<>();

			HashSet<String> voltrendSymbols = new HashSet<>();

			for (String path : files) {

				if (path.contains("pricetrend") && path.endsWith(".csv")) {

					if (type == null || path.contains(type)) {

						List<String> symbols = Utils.readSymbolsFromOutputCSV(new File(path));

						pricetrendSymbols.addAll(symbols);
					}
				}

				if (path.contains("voltrend") && path.endsWith(".csv")) {

					if (type == null || path.contains(type)) {

						List<String> symbols = Utils.readSymbolsFromOutputCSV(new File(path));

						voltrendSymbols.addAll(symbols);
					}
				}

				if (path.contains("lowhigh") && path.endsWith(".csv")) {

					if (type == null || path.contains(type)) {

						List<String> symbols = Utils.readSymbolsFromOutputCSV(new File(path));

						lowHighSymbols.addAll(symbols);
					}
				}

			}

			List<Opening> lowHighStocks = new ArrayList<>();

			List<Opening> priceStocks = new ArrayList<>();

			List<Opening> volumeStocks = new ArrayList<>();

			int index = 0;

			if (!lowHighSymbols.isEmpty()) {

				System.out.println("================================================");
				System.out.println("                   LOWHIGH                      ");
				System.out.println("================================================");
			}

			for (String symbol : lowHighSymbols) {

				Opening stockDetails = getStockDetails(symbol);

				if (stockDetails != null) {

					lowHighStocks.add(stockDetails);

					index++;
				}

				percentage = (index) * 100 / lowHighSymbols.size();
			}

			if (!lowHighStocks.isEmpty()) {

				DatabaseUtils.saveOpeningValue(lowHighStocks, SAVE_TO_DATABASE, "lowhigh10", mDate);
			}

			index = 0;

			if (!pricetrendSymbols.isEmpty()) {

				System.out.println("================================================");
				System.out.println("                   PRITREND                     ");
				System.out.println("================================================");
			}

			for (String symbol : pricetrendSymbols) {

				Opening stockDetails = getStockDetails(symbol);

				if (stockDetails != null) {

					priceStocks.add(stockDetails);

					index++;
				}

				percentage = (index) * 100 / pricetrendSymbols.size();
			}

			if (!priceStocks.isEmpty()) {

				DatabaseUtils.saveOpeningValue(priceStocks, SAVE_TO_DATABASE, "pritrend", mDate);
			}

			index = 0;

			if (!voltrendSymbols.isEmpty()) {

				System.out.println("================================================");
				System.out.println("                   VOLTREND                     ");
				System.out.println("================================================");
			}

			for (String symbol : voltrendSymbols) {

				Opening stockDetails = getStockDetails(symbol);

				if (stockDetails != null) {

					volumeStocks.add(stockDetails);

					index++;
				}

				percentage = (index) * 100 / voltrendSymbols.size();
			}

			if (!volumeStocks.isEmpty()) {

				DatabaseUtils.saveOpeningValue(volumeStocks, SAVE_TO_DATABASE, "voltrend", mDate);
			}

			List<Opening> stocks = new ArrayList<>();

			stocks.addAll(lowHighStocks);

			stocks.addAll(priceStocks);

			stocks.addAll(volumeStocks);

			writeOpeningsToFile(stocks);

		} catch (NoSuchFileException e) {

			System.out.println("No Symbols found for the date " + outputDir);

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			processing = false;
		}
	}

	private static Opening getStockDetails(String stockName) {

		Opening opening = new Opening();

		Double[] closeValues;

		Double[] openValues;

		Double[] lowValues;

		Double[] highValues;

		Long[] volumes;

		Metadata metaData;

		YahooFinanceAPI service = NetworkUtils.getYahooFinanceAPIService();

		Calendar c = Calendar.getInstance();
		
		c.setTime(mDate);
		
		long fromTime = c.getTimeInMillis() / 1000;

		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {

			c.add(Calendar.DAY_OF_YEAR, 3);

		} else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {

			c.add(Calendar.DAY_OF_YEAR, 2);

		} else {

			c.add(Calendar.DAY_OF_YEAR, 1);
		}

		if (isToday(c.getTime())) {

			Calendar now = Calendar.getInstance();

			now.setTime(new Date());

			c.add(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));

		} else {

			c.add(Calendar.HOUR_OF_DAY, 19);
		}
		
		Call<ResponseBody> data = service.getStockInfo(stockName, fromTime,
				c.getTimeInMillis() / 1000);
		
		try {

			Response<ResponseBody> execute = data.execute();

			if (execute.code() != 200) {

				errorList.add(stockName + " ---> No data found, symbol may be delisted");

				return new Opening(stockName);
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

			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();

			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			volumes = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getVolume();

			opening.setCurrency(metaData.getCurrency());

			opening.setSymbol(metaData.getSymbol());

			opening.setName(metaData.getExchangeName());

			opening.setOpen(openValues[openValues.length - 1] == null ? -9999 : openValues[openValues.length - 1]);

			opening.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			opening.setLow20(lowValues[lowValues.length - 1]);

			opening.setHigh20(highValues[highValues.length - 1]);

			opening.setVolume(volumes[volumes.length - 1]);
			
			double Price = closeValues[closeValues.length - 2];
			
			opening.setGainClose(opening.getNow() - Price);
			
			opening.setGainClosePercent(opening.getGainClose() * 100 / Price);
			
			opening.setGainHigh(opening.getHigh20() - Price);
			
			opening.setGainHighPercent(opening.getGainHigh() * 100 / Price);
			
			opening.setVolumeChangePercent2((volumes[volumes.length - 1] - volumes[volumes.length - 2]) * 100 / volumes[volumes.length - 2]);

		} catch (IOException e) {

			e.printStackTrace();
		}

		return opening;
	}

	private static void writeOpeningsToFile(List<Opening> stocks) {

		try {

			String filenamePrefix = "openings" + (type == null ? "" : "_" + type);

			File file = generateOutputFile(filenamePrefix, Utils.generateOutputDir(mDate));

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			writer.println(HEADER);

			for (int i = 0; i < stocks.size(); i++) {

				Opening stock = stocks.get(i);

				writer.println(String.format("%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%%,%.2f,%.2f%%,%.2f%%", (i + 1), stock.getSymbol(), 
						stock.getOpen(), stock.getNow(), stock.getLow20(), stock.getHigh20(), stock.getGainClose(), stock.getGainClosePercent(),
						stock.getGainHigh(), stock.getGainHighPercent(), stock.getVolumeChangePercent2()));
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static File generateOutputFile(String type, File outputDir) {

		SimpleDateFormat sdf = new SimpleDateFormat("_MMMM_dd_yyyy_hh_mm_ss_aaa");

		String outputFileName = type + sdf.format(new Date()) + ".csv";

		File outputFile = new File(outputDir, outputFileName);

		if (!outputFile.exists()) {

			try {

				outputFile.createNewFile();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return outputFile;
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

	private static boolean isToday(Date date) {

		Calendar today = Calendar.getInstance();

		Calendar specifiedDate = Calendar.getInstance();

		specifiedDate.setTime(date);

		return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
				&& today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
				&& today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
	}
}
