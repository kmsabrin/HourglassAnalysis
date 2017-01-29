package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class AgonyAnalysis {
	
	public static void printAgonyNetwork() throws Exception {
		HashMap<String, Integer> labelIdMap = new HashMap();
		
		Scanner scanner = new Scanner(new File("metabolic_networks//rat-links.txt"));
		PrintWriter pw = new PrintWriter(new File("metabolic_networks//rat.edges"));
		int id = 0;
		while (scanner.hasNext()) {
			String server = scanner.next();
			String dependent = scanner.next();
			if (!labelIdMap.containsKey(server)) {
				labelIdMap.put(server, id++);
			}
			if (!labelIdMap.containsKey(dependent)) {
				labelIdMap.put(dependent, id++);
			}
			System.out.println(labelIdMap.get(server) + "\t" + labelIdMap.get(dependent));
			pw.println(labelIdMap.get(server) + "\t" + labelIdMap.get(dependent));
		}
		scanner.close();
		pw.close();

		pw = new PrintWriter(new File("metabolic_networks//rat.nodes"));
		for (String s: labelIdMap.keySet()) {
			System.out.println(labelIdMap.get(s) + "\t" + s);
			pw.println(labelIdMap.get(s) + "\t" + s);
		}
		pw.close();
	}
	
	public static void getAgonyCompliantNetwork() throws Exception {
		HashMap<Integer, String> idLabelMap = new HashMap();
		HashMap<Integer, Integer> idRankMap = new HashMap();

		Scanner scanner = new Scanner(new File("metabolic_networks//rat.nodes"));
		while (scanner.hasNext()) {
			int id = scanner.nextInt();
			String label = scanner.next();
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		scanner = new Scanner(new File("metabolic_networks//rat.ranks"));
		while (scanner.hasNext()) {
			int id = scanner.nextInt();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
		}
		scanner.close();
		
		scanner = new Scanner(new File("metabolic_networks//rat.edges"));
		PrintWriter pw = new PrintWriter(new File("metabolic_networks//rat.agony.network"));
		while (scanner.hasNext()) {
			int substrate = scanner.nextInt();
			int product = scanner.nextInt();

			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			
			if (substrateRank < productRank) {
				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				pw.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
			}
		}
		pw.close();
		scanner.close();
	}
	
	public static void main(String[] args) throws Exception {
		printAgonyNetwork();
		getAgonyCompliantNetwork();
	}
}
