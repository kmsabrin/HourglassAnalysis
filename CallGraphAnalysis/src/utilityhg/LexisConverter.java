package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class LexisConverter {
	
	public static void convert(String fileName) throws Exception {
		Scanner scanner = new Scanner(new File("lexis_graphs\\" + fileName));
		PrintWriter pw = new PrintWriter(new File("lexis_graphs\\" + fileName + "_converted.txt"));
		
		int targetIndex = 0;
		while (scanner.hasNext()) {
			String s = scanner.nextLine();
			int arrowIndex = s.indexOf("->");
//			System.out.println(s+ "\t" + arrowIndex);
			
			String des = s.substring(0, arrowIndex - 1);
			if (des.equals("N0")) {
				++targetIndex;
				des = "T" + targetIndex;
			}
			
			String rest = s.substring(arrowIndex + 4);
			String srcs[] = rest.split("\\s+");
			HashMap<String, Integer> weights = new HashMap();
			for (String r : srcs) {
//				System.out.println(r);
				if (weights.containsKey(r)) {
					weights.put(r, weights.get(r) + 1);
				}
				else {
					weights.put(r, 1);
				}
			}
			
			for (String r: weights.keySet()) {
				pw.println(r + "\t" + des + "\t" + weights.get(r));
				if (weights.get(r) > 1) {
					System.out.println(r + "\t" + des + "\t"  + weights.get(r));
				}
			}
		}

		pw.close();
		scanner.close();
	}
	
	public static void main(String[] args) throws Exception {
		convert("7_g.txt");
	}

}
