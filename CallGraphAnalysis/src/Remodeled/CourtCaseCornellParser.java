package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;

public class CourtCaseCornellParser {
	static TreeSet<String> caseNames;
	static HashSet<String> caseIDs;
	static HashMap<String, String> caseNameID;
	static HashMap<String, String> caseNameMatching;
	static String caseTopic = "pension";
	
	public static void htmlParse(String filePath) throws Exception {
		Scanner scanner = new Scanner(new File(filePath));
		PrintWriter pw = new PrintWriter(new File("supremecourt_networks//case-" + caseTopic + "-name.txt"));
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("<td class=\"swtitle\">")) {
//				System.out.println(line);
				int startIndex = line.indexOf("html\">");
				int endIndex = line.indexOf("</a>");
				String name = line.substring(startIndex + 6, endIndex);
				if (name.length() < 2) continue;
//				System.out.println(name);
				name = name.toLowerCase();
				caseNames.add(name);
			}
		}
		
		for (String c: caseNames) {
			pw.println(c);
		}
		
		System.out.println("Cases Found: " + caseNames.size());
		scanner.close();
		pw.close();
	}
	
	public static void findUSCaseNum(String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("supremecourt_networks//case-" + caseTopic + "-id-parsed.txt"));
		Scanner scanner = new Scanner(new File(filePath));

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.toLowerCase();
			if (line.indexOf("v.") < 0) continue; // no information
			
			for (String c: caseNames) {
				int kntPrefix = 0;
				int kntSuffix = 0;
				
				String tokens[] = c.split("[ .,\n\t]");
				
//				if (line.startsWith("25347") && c.startsWith("roe")){
//				for (String s: tokens) {
//					System.out.print("[" + s + "]" + "\t");
//				}
//				System.out.println();
//				System.out.println(line);
//				}
				
				int index = 0;
				int flg = 0;
				for (index = 0; index < tokens.length; ++index) {
					String s = tokens[index];
					if (s.equals("v")) {
						flg = 1;
					}
					
					if (s.length() < 2) continue; // skip space and single characters
					if (s.equals("of")) continue;
					if (s.equals("for")) continue;
					if (s.equals("corp")) continue;
					if (s.equals("inc")) continue;
					if (NumberUtils.isNumber(s)) continue;
					
//					if (line.startsWith("25347") && c.startsWith("roe")) {
//						System.out.println("Looking for " + s);
//					}
					
					int matchIndex = -1;
					if (flg == 0) {
						matchIndex = line.substring(0, line.indexOf("v.")).indexOf(s);
					}
					else {
						matchIndex = line.indexOf(s, line.indexOf("v."));
					} 
					
//					if (line.startsWith("25347") && c.startsWith("roe")) {
//						System.out.println(" ??? " + s + "\t" + matchIndex);
//						System.out.println(kntPrefix + "\t" + kntSuffix);
//					}
					
					if (matchIndex > -1) {
//						System.out.println(s + "\t" + matchIndex + "\t" + line);
//						if (matchIndex > 0 && line.charAt(matchIndex - 1) != ' ') continue; // not exact match
//						if (line.charAt(matchIndex + s.length()) != ' ' && line.charAt(matchIndex + s.length()) != ',') continue; // not exact match
						if (flg == 0) {
							++kntPrefix;
						}
						else {
							++kntSuffix;
						}
					}
					
					
				}
				
//				if (line.startsWith("25347") && c.startsWith("roe")) {
//					System.out.println("???");
//					System.out.println(kntPrefix + "\t" + kntSuffix);
//				}
				
				if (kntPrefix > 0 && kntSuffix > 0) {
//					System.out.println(c + " ### " + line);
					tokens = line.split("[,]");
//					System.out.println(line + " has num " + tokens[0]);
//					caseIDs.add(tokens[0]);
					caseNameID.put(c, tokens[0]);
					caseNameMatching.put(c, line);
				}
			}
			
//			break;
		}
		
		for (String s: caseNameID.keySet()) {
			System.out.println(s + "\t" + caseNameMatching.get(s));
		}
		
		for (String s: caseNames) {
			if (!caseNameID.containsKey(s)) {
				System.out.println("[NOT FOUND]: " + s);
			}
		}
		
		for (String s: caseNameID.keySet()) {
			System.out.println(caseNameID.get(s));
			pw.println(caseNameID.get(s));
		}
		
		scanner.close();
		pw.close();
	}
	
	public static void loadCuratedCaseIDs() throws Exception {
		caseIDs = new HashSet();
		Scanner scanner = new Scanner(new File("supremecourt_networks//case-" + caseTopic + "-id-curated.txt"));
		while (scanner.hasNext()) {
			String line = scanner.next();
//			System.out.println(line);
			caseIDs.add(line);
		}
		
		scanner.close();
	}

	public static void evokeCaseByNameParsing() throws Exception {
		caseNames = new TreeSet();
		caseIDs = new HashSet();
		caseNameID = new HashMap();
		caseNameMatching = new HashMap();
		htmlParse("supremecourt_networks//case-" + caseTopic + ".html");
		findUSCaseNum("supremecourt_networks//judicial.csv");
	}
	
	public static void main(String[] args) throws Exception {
		evokeCaseByNameParsing();
	}
}
