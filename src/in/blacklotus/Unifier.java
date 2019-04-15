package in.blacklotus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import in.blacklotus.model.Command;
import in.blacklotus.model.YahooResponse;

public class Unifier {

	public static final Map<String, YahooResponse> responseMap = new LinkedHashMap<>();

	public static boolean SAVE_TO_DATABASE = false;
	
	public static String UNIFIER_DIRECTORY = "";

	private static final String INPUT_FILE_NAME = "unifier.csv";
	
	public static HashMap<String, Integer> lowHighDups = new HashMap<>();
	
	public static HashMap<String, Integer> priDups = new HashMap<>();
	
	public static HashMap<String, Integer> volDups = new HashMap<>();

	public static void main(String[] args) {
		
		Map<String, List<String>> params = parseArguments(args);

		ArrayList<Command> commands = readInput();
		
		System.out.println(
				"--------------------------------------------------------------------------------------------------------");

		if (params.containsKey("db")) {

			try {

				SAVE_TO_DATABASE = Boolean.parseBoolean(params.get("db").get(0));

			} catch (Exception e) {

				System.out.println("***   Invalid DB flag. Proceeding without saving to database   ***");
			}
		}

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

				System.out.println("**** INVALID COMMAND " + command.printRawCommand() + " ****");
			}
		}
		
//		Utils.writeDuplicatesToFile(lowHighDups, priDups, volDups);
	}

	private static ArrayList<Command> readInput() {

		File inputFile = new File(INPUT_FILE_NAME);

		if (!inputFile.exists()) {
			
			System.out.println("**** UNIFIER.CSV not Found ****");

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

}
