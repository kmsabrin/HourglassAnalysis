package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class DistributionAnalysis {
	
	public static void printCentralityDistribution(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-distribution-" + filePath + ".txt"));

		for (String s: dependencyDAG.functions) {
			pw.println(dependencyDAG.geometricMeanPagerankCentrality.get(s));
		}	
		
		pw.close();
	}
	
	public static void printLocationVsCentrality(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//loc-vs-centrality-" + filePath + ".txt"));

//		for scatter and average using smooth unique
		for (String s : dependencyDAG.geometricMeanPagerankCentrality.keySet()) {
			pw.println(dependencyDAG.location.get(s) + "\t" + dependencyDAG.geometricMeanPagerankCentrality.get(s));
		}
		
		pw.close();
	}
	
	public static void printTargetDependencyDistribution(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//target-dependency-distribution-" + filePath + ".txt"));

		int max = 0;
		for (String s: dependencyDAG.functions) {
			if (!dependencyDAG.serves.containsKey(s)) {
				pw.println(dependencyDAG.serversReachable.get(s).size());
				if (dependencyDAG.serversReachable.get(s).size() > max) max = dependencyDAG.serversReachable.get(s).size();
			}
		}	
		System.out.println("max: " + max);
		
		pw.close();
	}
	
	public static void printCentralityCCDF(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-ccdf-" + filePath + ".txt"));

		Map<Double, Double> histogram = new TreeMap<Double, Double>();
		Map<Double, Double> CDF = new TreeMap<Double, Double>();
		
		for (String s: dependencyDAG.functions) {
			double v = dependencyDAG.harmonicMeanPagerankCentrality.get(s);	
			
			if (histogram.containsKey(v)) {
				histogram.put(v, histogram.get(v) + 1.0);
			}
			else {
				histogram.put(v, 1.0);
			}
		}
		
		// CDF: Cumulative Distribution Function
		double cumSum = 0;
		for (double d: histogram.keySet()) {
			double v = histogram.get(d);
			cumSum += v;
			CDF.put(d, cumSum / dependencyDAG.functions.size());
		}
		
		// CCDF: Complementary CDF
		for (double d: CDF.keySet()) {
			pw.println(d + "\t" + (1.0 - CDF.get(d)));

		}
		
		pw.close();
	}
}
