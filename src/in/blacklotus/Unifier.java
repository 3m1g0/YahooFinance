package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import in.blacklotus.model.Command;
import in.blacklotus.model.YahooResponse;
import in.blacklotus.utils.Utils;

public class Unifier {

	public static final Map<String, YahooResponse> responseMap = new LinkedHashMap<>();

	public static boolean SAVE_TO_DATABASE = false;

	private static final String INPUT_FILE_NAME = "unifier.csv";
	
	public static HashMap<String, Integer> duplicates = new HashMap<>();

	public static void main(String[] args) {

		ArrayList<Command> commands = readInput();

		SAVE_TO_DATABASE = true;

		for (Command command : commands) {

			System.out.println(
					"--------------------------------------------------------------------------------------------------------");

			System.out.println("                                  " + command.printRawCommand());

			switch (command.getName()) {

			case "lowhigh10":

				LowHigh10.main(command.getArgs());

				break;

			case "pritrend":

				PriceTrends.main(command.getArgs());

				break;

			case "voltrend":

				VolumeTrends.main(command.getArgs());

				break;

			default:

				System.out.println("**** INVALID COMMAND " + command.printRawCommand());
			}
		}
		
		Utils.writeDuplicatesToFile(duplicates);
	}

	private static ArrayList<Command> readInput() {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {

			return null;
		}

		ArrayList<Command> commandsList = new ArrayList<>();

		String input = null;

		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(inputFile));

			input = reader.readLine();

			while (input != null) {

				String split[] = input.split(" ");

				if (split != null && split.length > 0) {

					try {
						
						Command command = new Command(split[0], split.length == 0 ? new String[] {} : Arrays.copyOfRange(split, 1, split.length));

						commandsList.add(command);

					} catch (Exception e) {

					}

					input = reader.readLine();
				}
			}

			reader.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

		return commandsList;
	}

}
