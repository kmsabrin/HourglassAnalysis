package Remodeled;

import java.io.File;
import java.io.PrintWriter;

public class DistributionAnalysis {
	
	public static void printCentralityDistribution(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-distribution-" + filePath + ".txt"));

		for (String s: callDAG.functions) {
			pw.println(callDAG.centrality.get(s));
		}	
		
		pw.close();
	}
	
	public static void printLocationVsCentrality(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//loc-vs-centrality-" + filePath + ".txt"));

//		for scatter and average using smooth unique
		for (String s : callDAG.centrality.keySet()) {
			pw.println(callDAG.location.get(s) + "\t" + callDAG.centrality.get(s));
		}
		
		pw.close();
	}
}
