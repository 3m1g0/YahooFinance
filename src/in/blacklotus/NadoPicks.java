package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import in.blacklotus.model.YahooResponse;

public class NadoPicks {

	public static final Map<String, YahooResponse> responseMap = new LinkedHashMap<>();

	public static boolean SAVE_TO_DATABASE = false;

	private static final String INPUT_FILE_NAME = "unifier.csv";

	public static void main(String[] args) {

		Map<String, String[]> inputMap = readInput();

		SAVE_TO_DATABASE = true;

		for (String key : inputMap.keySet()) {

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.println("                                  " + key.toUpperCase());

			switch (key) {

			case "lowhigh10":

				LowHigh10.main(inputMap.get(key));

				break;

			case "pritrend":

				PriceTrends.main(inputMap.get(key));

				break;

			case "voltrend":

				VolumeTrends.main(inputMap.get(key));

				break;

			default:

				System.out.println("**** INVALID COMMAND " + key);
			}
		}
	}

	private static Map<String, String[]> readInput() {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		Map<String, String[]> inputMap = new LinkedHashMap<>();

		String input = null;

		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(inputFile));

			input = reader.readLine();

			while (input != null) {

				String split[] = input.split(" ");

				if (split != null && split.length > 0) {

					try {

						inputMap.put(split[0],
								split.length == 0 ? new String[] {} : Arrays.copyOfRange(split, 1, split.length));

					} catch (Exception e) {

					}

					input = reader.readLine();
				}
			}

			reader.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

		return inputMap;
	}

}
