import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PatchAnalysis {
	
	public HashSet<String> getPatchedFunctions(String filePath, Set<String> functions) throws Exception {
		Scanner scan = new Scanner(new File(filePath));
		
		HashSet<String> possibleFunctionNameToken = new HashSet();
		
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (!line.startsWith("@@")) {
				continue;
			}
				
			String tokens[] = line.split("\\s+");
			for (String s: tokens) {
				if (s.indexOf("(") > -1) {
					possibleFunctionNameToken.add(s.substring(0, s.indexOf("(")));
				}
				else {
					possibleFunctionNameToken.add(s);
				}
			}
			
		}
		
		HashSet<String> notFunctionNameToken = new HashSet();
		for (String s : possibleFunctionNameToken) {
			if (functions.contains(s)) {
				System.out.println(s);
			}
			else {
				notFunctionNameToken.add(s);
			}
		}

		possibleFunctionNameToken.removeAll(notFunctionNameToken);
		scan.close();
		return possibleFunctionNameToken;
	}
}
