package in.blacklotus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jakewharton.fliptables.FlipTableConverters;

import in.blacklotus.utils.Utils;

public class Duplicates {
	
	private static final String INPUT_FILE_NAME = "input.csv";
	
	public static void main(String[] args) {
		
		List<String> inputList = Utils.readDupsInput(INPUT_FILE_NAME);
		
		HashMap<String, ArrayList<String>> duplicates = Utils.getDuplicates(args[0], args.length == 2 ? args[1] : null);
		
		List<String[]> tmp = new ArrayList<>();
		
		for(String key : duplicates.keySet()) {
			
			if(inputList.contains(key)) {
				
				if(duplicates.get(key) != null) {
					
					String[] files = duplicates.get(key).toArray(new String[0]);
					
					if(files.length > 1) {
						
						for(int i = 0; i < files.length; i++) {
							
							if(i == 0) {
								
								tmp.add(new String[]{key + "(" + files.length + ")", files[i]});
							
							} else {
								
								tmp.add(new String[]{"", files[i]});
							}
						}
					}
				}
			}
		}
		
		String headers[] = new String[]{"SYMBOL", "FILE"};
		
		String[][] data = new String[tmp.size()][];

		for (int i = 0; i < tmp.size(); i++) {

			data[i] = tmp.get(i);
		}
		
		Utils.writeDuplicatesToFile(data, args[0]);
		
		System.out.println(FlipTableConverters.fromObjects(headers, data));
	}
}
