package in.blacklotus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.model.Symbol;
import in.blacklotus.model.PriceTrend;
import in.blacklotus.utils.Utils;

public class VolumeTrends {

	private static final String INPUT_FILE_NAME = "input.csv";

	private static int NO_VALUES = 20;

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

			processTrendData(symbolList);
		}

		if (!errorList.isEmpty()) {

			Utils.writeErrorsToFile(errorList);
		}

	}

	private static void processTrendData(List<Symbol> symbolList) {

		List<PriceTrend> processedTrendList = new ArrayList<>();

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
				PriceTrend trendDetail = Utils.getTrendDetails(symbol.getName(), NO_VALUES, TREND, TREND_COUNT,
						errorList);

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
				"%VOLUMECHANGE" };

		List<String[]> tmp = new ArrayList<>();

		for (int i = 0; i < processedTrendList.size(); i++) {

			String[] printableVolumes = processedTrendList.get(i).toPrintableVolumeStrings(i + 1, VOLUME_DIFF);

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

	private static void writeTrendsToFile(String[] headers, List<PriceTrend> stocks) {

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
