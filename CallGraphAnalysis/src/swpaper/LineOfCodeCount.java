package swpaper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class LineOfCodeCount {
	static List<String> sourceFilePath = new ArrayList<String>();
	static HashMap<String, Integer> functionNumLines = new HashMap();
	
	public static String containsFunctionName(String line, Set<String> functionNames) {
		String tokens[] = line.split("\\s+");
		if (tokens[0].startsWith("\t") || tokens[0].startsWith(" ") || tokens[0].length() < 1) {
			return null;
		}
		for (int i = 1; i < tokens.length; ++i) {
			if (tokens[i].contains("(")) {
				String fName = "";
				if (tokens[i].charAt(0) == '*') {
					fName = tokens[i].substring(1, tokens[i].indexOf('('));
				}
				else {
					fName = tokens[i].substring(0, tokens[i].indexOf('('));
				}
				if (functionNames.contains(fName)) {
					return fName;
				}
			}
		}
		return null;
	}
	
	public static void parseFile(String fileName, Set<String> functionNames) throws Exception {
    	Scanner scan = new Scanner(new File(fileName));
    	Set<String> found = new HashSet();
    	int currentLine = -1;
    	int lastFoundLine = -1;
    	String lastFoundFunc = "";
    	while (scan.hasNext()) {
    		String line = scan.nextLine();
    		++currentLine;
    		if (line.endsWith(";")) continue;
    		if (line.contains("=")) continue;
    		String r = containsFunctionName(line, functionNames);
    		if (r != null) {
    			found.add(r);
    			if (lastFoundLine > 0) {
    				functionNumLines.put(lastFoundFunc, currentLine - lastFoundLine - 1);
//    				System.out.println(lastFoundFunc + "\t" + (currentLine - lastFoundLine - 1));
    			}
    			lastFoundLine = currentLine;
    			lastFoundFunc = r;
    		}
    	}
   
    	int leafCount  = 0;
    	for (String s: functionNames) {
    		if (!found.contains(s)) {
    			if (s.endsWith("@plt")) {
    				++leafCount;
    				continue;
    			}
//    			System.out.println(s); // not found
    		}
    	}
//    	System.out.println(found.size() + "\t" + leafCount + "\t" + functionNames.size());
	}
	
	// directory traverse
	public static void showFiles(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            showFiles(file.listFiles()); // Calls same method again.
	        } else {
	            sourceFilePath.add(file.getAbsolutePath());
	        }
	    }
	}
	
	public static void main(String[] args, HashSet<String> functionNames) throws Exception {
		File[] files = new File("C:/Users/Lupu/git/CallGraphAnalysis/CallGraphAnalysis/openssh_callgraphs").listFiles();
	    showFiles(files);
	    for (String s: sourceFilePath) {
			if (!s.endsWith(".c")) continue;
	    	System.out.println(s);	    	
	    	Scanner scan = new Scanner(new File(s));
	    	while (scan.hasNext()) {
	    		String line = scan.nextLine();
	    		if (line.endsWith(";")) continue;
	    	}
		}    
	}
}
