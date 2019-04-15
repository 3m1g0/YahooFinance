package in.blacklotus.utils;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.gson.Gson;

import in.blacklotus.Unifier;
import in.blacklotus.YahooFinanceApp;
import in.blacklotus.api.YahooFinanceAPI;
import in.blacklotus.model.Metadata;
import in.blacklotus.model.PriceTrend;
import in.blacklotus.model.Stock;
import in.blacklotus.model.Symbol;
import in.blacklotus.model.YahooResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Utils {

	public static final List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365), TimeUnit.DAYS.toMillis(30),
			TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
			TimeUnit.SECONDS.toMillis(1));

	public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

	public static String toDuration(long duration) {

		long seconds = duration / 1000;

		long minutes = seconds / 60;

		long hours = minutes / 60;

		long days = hours / 24;

		hours = hours % 24;

		minutes = minutes % 60;

		seconds = seconds % 60;

		StringBuffer sb = new StringBuffer();

		if (days > 0) {

			sb.append(days).append(days == 1 ? " day " : " days ");
		}

		if (hours > 0) {

			sb.append(hours).append(hours == 1 ? " hour " : " hours ");
		}

		if (minutes > 0) {

			sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
		}

		if (seconds > 0) {

			sb.append(seconds).append(seconds == 1 ? " second " : " seconds ");
		}

		return sb.toString();
	}

	public static void sendEmail(String body, String from, String pwd, String to) {

		Properties props = new Properties();

		props.put("mail.smtp.host", "smtp.gmail.com");

		props.put("mail.smtp.socketFactory.port", "465");

		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		props.put("mail.smtp.auth", "true");

		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication(from, pwd);
			}
		});

		try {

			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress("teja.tangaturi@gmail.com"));

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			message.setSubject("Hello");

			message.setText(body);

			Transport.send(message);

			System.out.println("message sent successfully");

		} catch (MessagingException e) {

			throw new RuntimeException(e);
		}
	}

	public static void displayTray() throws AWTException, MalformedURLException {

		SystemTray tray = SystemTray.getSystemTray();

		Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
		// Image image =
		// Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

		TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");

		trayIcon.setImageAutoSize(true);

		trayIcon.setToolTip("System tray icon demo");

		tray.add(trayIcon);

		trayIcon.displayMessage("Hello, World", "notification demo", MessageType.INFO);
	}

	public static void displayTray(String[] headers, String[][] data) {

		EventQueue.invokeLater(new Runnable() {

			public void run() {

				try {

					YahooFinanceApp window = new YahooFinanceApp();

					window.show(headers, data);

				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});
	}

	public static double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public static String formattedNumber(long number) {
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}

	public static String formattedVolume(long volume) {

		if (Math.abs(volume) > 999999) {

			return NumberFormat.getNumberInstance(Locale.US).format(round(volume * 1.00 / 1000000)) + "M";

		} else {

			return NumberFormat.getNumberInstance(Locale.US).format(volume);
		}
	}

	public static Map<String, List<String>> parseArguments(String args[]) {

		Map<String, List<String>> params = new HashMap<>();

		List<String> options = null;

		for (int i = 0; i < args.length; i++) {

			final String a = args[i];

			if (a.charAt(0) == '-') {

				if (a.length() < 2) {

					System.err.println("Error at argument " + a);

					return params;
				}

				options = new ArrayList<>();

				params.put(a.substring(1), options);

			} else if (options != null) {

				options.add(a);

			} else {

				System.err.println("Illegal parameter usage");

				return params;
			}
		}

		return params;
	}

	public static boolean isValidSortKey(String key, String[] SORT_KEYS) {

		for (String k : SORT_KEYS) {

			if (k.equalsIgnoreCase(key))

				return true;
		}

		return false;
	}

	public static List<String> getValidSortKeys(List<String> keys, String[] SORT_KEYS) {

		List<String> mKeys = new ArrayList<>();

		String excludes = "";

		for (String k : keys) {

			if (isValidSortKey(k, SORT_KEYS))

				mKeys.add(k);

			else {

				excludes += k + " ";
			}
		}

		if (!"".equals(excludes)) {

			System.out.println("***   Sort KEY must be one of " + Arrays.toString(SORT_KEYS) + "  ***");

			System.out.println("***   Proceeding without " + excludes + "  ***");
		}

		return mKeys;
	}

	public static List<Symbol> readInput(String INPUT_FILE_NAME) {

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
	
	public static List<String> readDupsInput(String INPUT_FILE_NAME) {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		List<String> inputList = new ArrayList<>();

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

					inputList.add(stock);

					System.out.print(stock + ",");
				}
			}

			System.out.println();

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");
		}

		return inputList;
	}

	public static Stock getStockDetails(String stockName, int NO_VALUES, List<String> errorList) {

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

			YahooResponse yahooResponse = new Gson().fromJson(responseString, YahooResponse.class);

			metaData = yahooResponse.getChart().getResult()[0].getMeta();

			timestamps = yahooResponse.getChart().getResult()[0].getTimestamp();

			closeValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getClose();

			if (closeValues == null) {

				return null;

			}

			volumes = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getVolume();

			lowValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getLow();

			highValues = yahooResponse.getChart().getResult()[0].getIndicators().getQuote()[0].getHigh();

			stock.setCurrency(metaData.getCurrency());

			stock.setSymbol(metaData.getSymbol());

			stock.setName(metaData.getExchangeName());

			stock.setNow(closeValues[closeValues.length - 1] == null ? -9999 : closeValues[closeValues.length - 1]);

			stock.setVolume(volumes[volumes.length - 1]);

			double nowPercent = (NO_VALUES < 2 || closeValues.length < 2 || closeValues[closeValues.length - 2] == null)
					? -9999
					: (stock.getNow() - closeValues[closeValues.length - 2]) * 100
							/ closeValues[closeValues.length - 2];

			stock.setNowPercent(nowPercent);

			double volumeChangePercent = (NO_VALUES < 2 || volumes.length < 2
					|| volumes[closeValues.length - 2] == null) ? -9999
							: (stock.getVolume() - volumes[volumes.length - 2]) * 100 / volumes[volumes.length - 2];

			stock.setVolumeChangePercent(volumeChangePercent);

			int highIndex = getHighIndex(highValues, NO_VALUES);

			stock.setHigh20(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues, NO_VALUES);

			stock.setLow20(lowValues[lowIndex]);

			stock.calculateHigh20Percenttage();

			stock.calculateLow20Percenttage();

			stock.calculateLowHighDiffPercent();

			stock.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			stock.setHigh20Date(new Date(timestamps[highIndex] * 1000L));

			stock.setLow20Date(new Date(timestamps[lowIndex] * 1000L));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return stock;
	}

	public static PriceTrend getTrendDetails(String stockName, int NO_VALUES, String TREND, int TREND_COUNT,
			String TREND_TYPE, List<String> errorList) {

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

			int highIndex = getHighIndex(highValues, NO_VALUES);

			trend.setHigh20(highValues[highIndex]);

			int lowIndex = getLowIndex(lowValues, NO_VALUES);

			trend.setLow20(lowValues[lowIndex]);

			trend.calculateHigh20Percenttage();

			trend.calculateLow20Percenttage();

			trend.calculateLowHighDiffPercent();

			trend.setNowDate(new Date(timestamps[timestamps.length - 1] * 1000L));

			trend.setHigh20Date(new Date(timestamps[highIndex] * 1000L));

			trend.setLow20Date(new Date(timestamps[lowIndex] * 1000L));

		} catch (IOException e) {

			e.printStackTrace();
		}

		return trend;
	}

	public static int getHighIndex(Double[] data, int mCount) {

		long count = 0;

		int max = data.length - 1;

		int index = data.length - 2;

		while (index > -1 && count < mCount) {

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

	public static int getLowIndex(Double[] data, int mCount) {

		int count = 0;

		int min = data.length - 1;

		int index = data.length - 2;

		while (index > -1 && count < mCount) {

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

	public static File generateOutputDir() {

		SimpleDateFormat sdf = new SimpleDateFormat("MMMM_dd_yyyy");

		String outputDir = sdf.format(new Date());

		String suffix = Unifier.UNIFIER_DIRECTORY;

		File dir = new File(outputDir + suffix);

		if (!dir.exists()) {

			dir.mkdirs();
		}

		return dir;
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

	public static void writeErrorsToFile(ArrayList<String> errorList) {

		try {

			File file = generateOutputFile("errors_", generateOutputDir());

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

	public static void writeDuplicatesToFile(String[][] dups, String dir) {

		try {

			File file = generateOutputFile("dups_", new File(dir));

			if (!file.exists()) {

				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(new FileWriter(file));

			writer.println("SYMBOL,FILE");
			
			for (String[] data : dups) {

				writer.println(data[0] + "," +data[1]);
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static void addToLowHighDups(String ticker) {

		if (Unifier.lowHighDups.containsKey(ticker)) {

			Unifier.lowHighDups.put(ticker, Unifier.lowHighDups.get(ticker) + 1);

		} else {

			Unifier.lowHighDups.put(ticker, 1);
		}
	}

	public static void addToPriDups(String ticker) {

		if (Unifier.priDups.containsKey(ticker)) {

			Unifier.priDups.put(ticker, Unifier.priDups.get(ticker) + 1);

		} else {

			Unifier.priDups.put(ticker, 1);
		}
	}

	public static void addToVolDups(String ticker) {

		if (Unifier.volDups.containsKey(ticker)) {

			Unifier.volDups.put(ticker, Unifier.volDups.get(ticker) + 1);

		} else {

			Unifier.volDups.put(ticker, 1);
		}
	}

	public static HashMap<String, ArrayList<String>> getDuplicates(String OUTPUT_DIR, String extension) {

		File inputDir = new File(OUTPUT_DIR);

		HashMap<String, ArrayList<String>> duplicates = new HashMap<>();

		if (inputDir.exists()) {

			File files[] = inputDir.listFiles();

			for (File file : files) {

				if (!file.getName().startsWith("error")) {
					
					if(extension != null && !file.getName().endsWith(extension)) {
						
						continue;
					}

					List<String> tickersList = readSymbolsFromOutputCSV(file);

					for (String ticker : tickersList) {

						if (ticker != null) {
							
							if (duplicates.containsKey(ticker)) {

								duplicates.get(ticker).add(file.getName());

							} else {

								ArrayList<String> list = new ArrayList<>();
								
								list.add(file.getName());
								
								duplicates.put(ticker, list);
							}
						}
					}
				}
			}

		} else {

			System.out.println(inputDir + " does not exist");
		}

		return duplicates;
	}

	public static List<String> readSymbolsFromOutputCSV(File inputFile) {

		List<String> inputList = new ArrayList<>();

		if (inputFile.exists()) {

			String input = null;

			BufferedReader reader;

			try {
				
				reader = new BufferedReader(new FileReader(inputFile));

				input = reader.readLine();

				while (input != null) {
					
					String split[] = input.split(",");

					if (split.length > 1 && split[1] != null && !split[1].trim().isEmpty() && !"SYMBOL".equalsIgnoreCase(split[1])) {

						inputList.add(split[1]);
					}

					input = reader.readLine();
				}

				reader.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return inputList;
	}
	
	public static String toISODate(Date date) {
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS\'Z\'"); // Quoted "Z" to indicate UTC, no timezone offset
		
		df.setTimeZone(tz);
		
		return df.format(date);
	}
}