package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class DistributionAnalysis {
	
	public static void printCentralityDistribution(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-distribution-" + filePath + ".txt"));

		for (String s: dependencyDAG.functions) {
			pw.println(dependencyDAG.geometricMeanPathCentrality.get(s));
		}	
		
		pw.close();
	}
	
	public static void printLocationVsCentrality(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//loc-vs-centrality-" + filePath + ".txt"));

//		for scatter and average using smooth unique
		for (String s : dependencyDAG.geometricMeanPathCentrality.keySet()) {
			pw.println(dependencyDAG.location.get(s) + "\t" + dependencyDAG.geometricMeanPathCentrality.get(s));
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
	
	public static TreeMap<Double, Double> getCentralityCCDF(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-ccdf-" + filePath + ".txt"));

		Map<Double, Double> histogram = new TreeMap<Double, Double>();
		Map<Double, Double> CDF = new TreeMap<Double, Double>();
		
		for (String s: dependencyDAG.functions) {
			double v = dependencyDAG.normalizedPathCentrality.get(s);	
			
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
		TreeMap<Double, Double> ccdfMap = new TreeMap();
		for (double d: CDF.keySet()) {
			double ccdfP = 1.0 - CDF.get(d);
			pw.println(d + "\t" + ccdfP);
			ccdfMap.put(d, ccdfP);
		}
		
		pw.close();
		return ccdfMap;
	}
	
	public static void printPathCentralityVsPageRankScatter(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//pcentrality-vs-pagerank-" + filePath + ".txt"));

		for (String s : dependencyDAG.functions) {
			pw.println(dependencyDAG.harmonicMeanPathCentrality.get(s) + "\t" + dependencyDAG.harmonicMeanPagerankCentrality.get(s));
		}
		
		pw.close();
	}
	
	private static HashMap<String, Integer> rankData(DependencyDAG dependencyDAG, HashMap<String, Double> values) {
		HashMap<String, Integer> nodeRank = new HashMap();
		TreeMultimap<Double, String> sortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
			sortedNodes.put(values.get(s), s);
		}
		
		int rank = 1;
		for (double v: sortedNodes.keySet()) {
			Collection<String> nodes = sortedNodes.get(v);
			for (String s: nodes) {
				nodeRank.put(s, rank);
			}
			++rank;
		}
		return nodeRank;
	}
	
	public static void printCentralityRanks(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//pcentrality-and-pagerank-" + filePath + ".txt"));
		
		HashMap<String, Integer> pCGMRank = rankData(dependencyDAG, dependencyDAG.geometricMeanPathCentrality);
		HashMap<String, Integer> pCHMRank = rankData(dependencyDAG, dependencyDAG.harmonicMeanPathCentrality);
		HashMap<String, Integer> pRGMRank = rankData(dependencyDAG, dependencyDAG.geometricMeanPagerankCentrality);
		HashMap<String, Integer> pRHMRank = rankData(dependencyDAG, dependencyDAG.harmonicMeanPagerankCentrality);
		
		for (String s: dependencyDAG.functions) {
			pw.println(s + "\t" + pCGMRank.get(s) + "\t" + pCHMRank.get(s) + "\t" + pRGMRank.get(s) + "\t" + pRHMRank.get(s));
		}
		pw.close();
		
		for (String s: dependencyDAG.functions) {
			System.out.print(s + "\t" + dependencyDAG.geometricMeanPathCentrality.get(s) + "\t" + dependencyDAG.harmonicMeanPathCentrality.get(s));
			System.out.println("\t" + dependencyDAG.geometricMeanPagerankCentrality.get(s) + "\t" + dependencyDAG.harmonicMeanPagerankCentrality.get(s));
		}
	}
	
	public static void getAveragePathLenth(DependencyDAG dependencyDAG) {
		double avgPLength = 0;
		double knt = 0;
		for (String s: dependencyDAG.functions) {
			if (!dependencyDAG.serves.containsKey(s)) {
				avgPLength += dependencyDAG.avgSourceDepth.get(s);
				++knt;
//				System.out.println(s + " " + dependencyDAG.avgSourceDepth.get(s));
			}
		}
		System.out.println("Average Path Length: " + avgPLength / knt);
	}
	
	public static void getReachabilityCount(DependencyDAG dependencyDAG) {
		double overFlowed = 0;
		double participant = 0;
		double rSum = 0;
		for (String s: dependencyDAG.functions) {
//			if (dependencyDAG.serves.containsKey(s) && dependencyDAG.depends.containsKey(s)) {
				double reachable = 0, possible = 0;
//				System.out.print(s);
//				System.out.print(" reaches:");
				for (String r: dependencyDAG.dependentsReachable.get(s)) {
					if (dependencyDAG.serves.containsKey(r)) {
						++reachable;
//						System.out.print(" " + r);
					}
				}
				
//				System.out.print(" canReach:");
				double u = Double.parseDouble(s);
				for (String r: dependencyDAG.functions) {
					double v = Double.parseDouble(r);
					if (dependencyDAG.serves.containsKey(r) && v < u) {
						++possible;
//						System.out.print(" " + r);
					}
				}
//				System.out.println();
				
				double r = reachable / possible;
				rSum += r;
				if (r > 0.9) ++overFlowed;
//				System.out.println(s + "\t" + reachable + "\t" + possible);
				
				++participant;
//			}
		}
		
//		System.out.println(overFlowed / participant);
		
		System.out.println(rSum / participant);
	}
}
