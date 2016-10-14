package swpaper;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PatchAnalysis {
	public static HashMap<String, Integer> getPatchedFunctions(String filePath, Set<String> functions) throws Exception {
		Scanner scan = new Scanner(new File(filePath));
		
		HashMap<String, Integer> possiblePatchedFunctions = new HashMap<String, Integer>();
		
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (!line.startsWith("@@")) {
				continue;
			}
				
			String tokens[] = line.split("\\s+");
			int nLines = Integer.parseInt(tokens[1].substring(tokens[1].indexOf(",") + 1));
			for (String s: tokens) {
				String f = s;
				if (s.indexOf("(") > -1) {
					f = s.substring(0, s.indexOf("("));
				}
				if (possiblePatchedFunctions.containsKey(f)) {
					possiblePatchedFunctions.put(f, possiblePatchedFunctions.get(f) + nLines);
				}
				else {
					possiblePatchedFunctions.put(f, nLines);
				}
			}
		}
		
		HashSet<String> notFunctionName = new HashSet<String>();
		for (String s : possiblePatchedFunctions.keySet()) {
			if (functions.contains(s)) {
//				System.out.println(s);
			}
			else {
				notFunctionName.add(s);
			}
		}

		for (String s: notFunctionName) {
			possiblePatchedFunctions.remove(s);
		}
		scan.close();
		return possiblePatchedFunctions;
	}
}
