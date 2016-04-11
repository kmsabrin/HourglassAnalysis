package Final;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

public class DistributionAnalysis {
	static HashSet<String> visited;
	static int wccSize;
	
	private static void WCCHelper(String s, DependencyDAG dependencyDAG) {
//		if (visited.contains(s)) return;
		
		++wccSize;
		visited.add(s);
		
		if (dependencyDAG.serves.containsKey(s)) {
			for (String r: dependencyDAG.serves.get(s)) {
				if (!visited.contains(r)) {
					WCCHelper(r, dependencyDAG);
				}
			}
		}
		
		if (dependencyDAG.depends.containsKey(s)) {
			for (String r: dependencyDAG.depends.get(s)) {
				if (!visited.contains(r)) {
					WCCHelper(r, dependencyDAG);
				}
			}
		}
	}
	
	public static void findWeaklyConnectedComponents(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//largestWCC-" + filePath + ".txt"));

		int largestWCCSize = 0;
		String largestWCCSeed = "";
		
		visited = new HashSet();
		int nWCC = 0;
		for (String s : dependencyDAG.nodes) {
			if (!visited.contains(s)) {
				wccSize = 0;
				++nWCC;
				WCCHelper(s, dependencyDAG);
				System.out.println("Component " + nWCC + " with size " + wccSize);
				if (wccSize > largestWCCSize) {
					largestWCCSize = wccSize;
					largestWCCSeed = s;
				}
			}
		}
		
		System.out.println("Largest WCC size: " + largestWCCSize);
		visited.clear();
		WCCHelper(largestWCCSeed, dependencyDAG);
		for (String s: visited) {
			pw.println(s);
		}
		pw.close();
	}
	
	public static void printCentralityDistribution(DependencyDAG dependencyDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-distribution-" + filePath + ".txt"));

		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s) && dependencyDAG.depends.containsKey(s)) { // only intermediate nodes			
				pw.println(dependencyDAG.normalizedPathCentrality.get(s) * dependencyDAG.nTotalPath);
			}
		}	
		
		pw.close();
	}
	
	public static TreeMap<Double, Double> getCentralityCCDF(DependencyDAG dependencyDAG, String filePath, int key) throws Exception {		
		String[] centrality = {"p-", "i-", "hpr-", "gpr-"};
		
		PrintWriter pw = new PrintWriter(new File("analysis//centrality-ccdf-" + centrality[key - 1] + filePath + ".txt"));

		Map<Double, Double> histogram = new TreeMap<Double, Double>();
		Map<Double, Double> CDF = new TreeMap<Double, Double>();
		
		for (String s: dependencyDAG.nodes) {
			double v = 0;
			if (key == 1) v = dependencyDAG.normalizedPathCentrality.get(s); //* dependencyDAG.nTotalPath;
			if (key == 2) v = dependencyDAG.iCentrality.get(s);
			if (key == 3) v = dependencyDAG.harmonicMeanPagerankCentrality.get(s);
			if (key == 4) v = dependencyDAG.geometricMeanPagerankCentrality.get(s);
			
			if (histogram.containsKey(v)) {
				histogram.put(v, histogram.get(v) + 1.0);
			}
			else {
				histogram.put(v, 1.0);
			}
		}
		
		// CDF: Cumulative Distribution Function
		double cumulativeSum = 0;
		for (double d: histogram.keySet()) {
			double v = histogram.get(d);
//			System.out.println(d + "\t" + v);
			cumulativeSum += v;
			CDF.put(d, cumulativeSum / dependencyDAG.nodes.size());
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
	
	public static double getPathLength(DependencyDAG dependencyDAG) {
		double avgPLength = 0;
		int knt = 0;
		double pathLengths[] = new double[(int)dependencyDAG.nTargets];
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.serves.containsKey(s)) {
				avgPLength += dependencyDAG.avgSourceDepth.get(s);
				pathLengths[knt++] = dependencyDAG.avgSourceDepth.get(s);
//				System.out.println(s + " " + dependencyDAG.avgSourceDepth.get(s));
			}
		}
		
//		System.out.println("Average Path Length: " + avgPLength / knt);
//		System.out.println("Median Path Length: " + StatUtils.percentile(pathLengths, 50));
//		System.out.println("Max Path Length: " + StatUtils.max(pathLengths));
		System.out.println("Mean Path Length: " + StatUtils.mean(pathLengths));
		System.out.println("STD Path Length: " + Math.sqrt(StatUtils.variance(pathLengths)));
		
		return StatUtils.percentile(pathLengths, 50);
	}	
	
	public static TreeMap<Integer, Integer> getDegreeHistogram(DependencyDAG dependencyDAG) {
		double inDegrees[] = new double[dependencyDAG.nodes.size()];
		double outDegrees[] = new double[dependencyDAG.nodes.size()];
		
		TreeMap<Integer, Integer> inDegreeHistogram = new TreeMap();
		TreeMap<Integer, Integer> outDegreeHistogram = new TreeMap();
		
		int kin = 0;
		int kout = 0;
		double inSum = 0;
		double outSum = 0;
		for (String s: dependencyDAG.nodes) {
			int iDeg = dependencyDAG.inDegree.get(s);
			int oDeg = dependencyDAG.outDegree.get(s);
//			System.out.println(s + "\t" + iDeg + "\t" + oDeg);
			if (inDegreeHistogram.containsKey(iDeg)) {
				int v = inDegreeHistogram.get(iDeg);
				inDegreeHistogram.put(iDeg, v + 1);
			}
			else inDegreeHistogram.put(iDeg, 1);
		
			if (outDegreeHistogram.containsKey(oDeg)) {
				int v = outDegreeHistogram.get(oDeg);
				outDegreeHistogram.put(oDeg, v + 1);
			}
			else outDegreeHistogram.put(oDeg, 1);

			if (dependencyDAG.isIntermediate(s) || dependencyDAG.isTarget(s)) {
				inDegrees[kin++] = iDeg;
				inSum++;
//				System.out.println("in Adding: " + iDeg + " for " + s);
			}
			
			if (dependencyDAG.isIntermediate(s) || dependencyDAG.isSource(s)) {
				outDegrees[kout++] = oDeg;
				outSum++;
//				System.out.println("out Adding: " + oDeg);
			}
		}
		

		inDegrees = Arrays.copyOf(inDegrees, kin);
		outDegrees = Arrays.copyOf(outDegrees, kout);
		
		for (double i: inDegrees) {
//			System.out.println((int)i);
		}
		
		System.out.println("------------------------\n\n");
		
		for (double o: outDegrees) {
//			System.out.println((int)o);
		}
		
		for (int i: inDegreeHistogram.keySet()) {
			if (i < 1) continue;
//			System.out.println(i + "\t" + (inDegreeHistogram.get(i) / inSum));
		}
		
		System.out.println("------------------------");
		
		for (int i: outDegreeHistogram.keySet()) {
			if (i < 1) continue;
//			System.out.println(i + "\t" + (outDegreeHistogram.get(i) / outSum));
		}
		
//		System.out.println(StatUtils.percentile(inDegrees, 50) + "\t" + StatUtils.mean(inDegrees));
		System.out.println(" Indegree:" + " Mean: " + StatUtils.mean(inDegrees) + " Variance: " + StatUtils.variance(inDegrees));
//		System.out.println(" Indegree:" + " Mean: " + StatUtils.mean(inDegrees) + " StD: " + Math.sqrt(StatUtils.variance(inDegrees)));
//		System.out.println("Outdegree:" + " Mean: " + StatUtils.mean(outDegrees) + " StD: " + Math.sqrt(StatUtils.variance(outDegrees)));
//		
//		System.out.println(" Indegree:" + " 25p: " + StatUtils.percentile(inDegrees, 10) +  " 75p: " + StatUtils.percentile(inDegrees, 90) );
//		System.out.println("Outdegree:" + " 25p: " + StatUtils.percentile(outDegrees, 10) + " 75p: " + StatUtils.percentile(outDegrees, 90) );
	
		return inDegreeHistogram;
	}
	
	
	public static void getDegreeHistogramSpecialized(DependencyDAG dependencyDAG) {
		TreeMap<Integer, Integer> inDegreeHistogramNearTarget = new TreeMap();
		TreeMap<Integer, Integer> inDegreeHistogramNearSource = new TreeMap();
		
		int maxDeg = 0;
		double nearSourceDegSum = 0;
		double nearTargetDegSum = 0;
		for (String s: dependencyDAG.nodes) {
			int iDeg = dependencyDAG.inDegree.get(s);
			if (dependencyDAG.location.get(s) > 0 && dependencyDAG.location.get(s) < 0.4) {
				if (iDeg > maxDeg) maxDeg = iDeg;
				nearSourceDegSum += iDeg;
				System.out.println(iDeg);
				if (inDegreeHistogramNearSource.containsKey(iDeg)) {
					int v = inDegreeHistogramNearSource.get(iDeg);
					inDegreeHistogramNearSource.put(iDeg, v + 1);
				} 
				else
					inDegreeHistogramNearSource.put(iDeg, 1);
			}
			
			if (dependencyDAG.location.get(s) > 0.9) {
				if (iDeg > maxDeg) maxDeg = iDeg;
				nearTargetDegSum += iDeg;
//				System.out.println(iDeg);
				if (inDegreeHistogramNearTarget.containsKey(iDeg)) {
					int v = inDegreeHistogramNearTarget.get(iDeg);
					inDegreeHistogramNearTarget.put(iDeg, v + 1);
				} 
				else
					inDegreeHistogramNearTarget.put(iDeg, 1);
			}
		}
		
//		System.out.println("Indeg\tNear-Source\tNear-Target");
//		for (int i = 1; i <= maxDeg; ++i) {
//			System.out.print(i);
//			if (inDegreeHistogramNearSource.containsKey(i)) {
//				System.out.print("\t" + (inDegreeHistogramNearSource.get(i) * 1.0 / nearSourceDegSum));
//			}
//			else {
//				System.out.print("\t" + "0");
//			}
//			
//			if (inDegreeHistogramNearTarget.containsKey(i)) {
//				System.out.print("\t" + (inDegreeHistogramNearTarget.get(i) * 1.0 / nearTargetDegSum));
//			}
//			else {
//				System.out.print("\t" + "0");
//			}
//			
//			System.out.println();
//		}
		
//		for (int i: inDegreeHistogramNearSource.keySet()) {
//			System.out.println(i + "\t" + inDegreeHistogramNearSource.get(i));
//		}
//		
//		System.out.println("------------------------");
//		System.out.println("------------------------");
//		
//		for (int i: inDegreeHistogramNearTarget.keySet()) {
//			System.out.println(i + "\t" + inDegreeHistogramNearTarget.get(i));
//		}
	}
	
	
	public static void getAverageInOutDegree(DependencyDAG dependencyDAG) {
		double inDeg = 0;
		double outDeg = 0;
		
		double avgInVsOut = 0;
		for (String s: dependencyDAG.nodes) {
			inDeg += dependencyDAG.inDegree.get(s);
			outDeg += dependencyDAG.outDegree.get(s);
			avgInVsOut += inDeg * 1.0 / outDeg;
		}
		
		System.out.println("Avg  In Deg: " + (1.0 * inDeg / dependencyDAG.nodes.size()));
		System.out.println("Avg Out Deg: " + (1.0 * outDeg / dependencyDAG.nodes.size()));
		System.out.println("Avg In/Out Deg: " + (1.0 *  avgInVsOut / dependencyDAG.nodes.size()));
	}
	
	
	public static void printEdgeList(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("edgelist_graphs//" + filePath + "_edgelist_indexed.txt"));
		
		HashMap<String, Integer> nodeIndex = new HashMap();
		int i = 0;
		for (String s: dependencyDAG.nodes) {
			System.out.println(s + "\t" + i);
			nodeIndex.put(s, i++);			
		}
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				for (String r: dependencyDAG.serves.get(s)) {
//					pw.println(s + "\t" + r);
					pw.println(nodeIndex.get(s) + "\t" + nodeIndex.get(r));
				}
			}
		}
		pw.close();
	}
	
		
	public static void printAllCentralities(DependencyDAG dependencyDAG, String filePath) throws Exception {
//		order: node indeg outdeg closeness betwenness location path
		
		Scanner scanner = new Scanner(new File("python_centralities//" + filePath + "_centralities.txt"));
		PrintWriter pw = new PrintWriter(new File("analysis//" + filePath + "_all_centralities.txt"));
		
		while (scanner.hasNext()) {
			String node = scanner.next();
			double indeg_c = scanner.nextDouble();
			double outdeg_c = scanner.nextDouble();
			double closeness_c = scanner.nextDouble();
			double betwenness_c = scanner.nextDouble();
			if (dependencyDAG.nodes.contains(node) == false) continue;
//			pw.println(node + " " + indeg_c + " " + outdeg_c + " " + closeness_c + " " + betwenness_c + " " + dependencyDAG.location.get(node) + " " + dependencyDAG.normalizedPathCentrality.get(node));
			pw.println(indeg_c + "\t" + outdeg_c + "\t" + closeness_c + "\t" + betwenness_c + "\t" + dependencyDAG.location.get(node) + "\t" + dependencyDAG.normalizedPathCentrality.get(node));
		}
		
		scanner.close();
		pw.close();
	}
	 
	
	public static void findNDirectSrcTgtBypasses(DependencyDAG dependencyDAG, String filePath) {
		int knt = 0;
		for (String n: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(n) && dependencyDAG.serves.containsKey(n)) {
				for (String r: dependencyDAG.serves.get(n)) {
					if (dependencyDAG.isTarget(r)) {
						++knt;
					}
				}
			}
		}
		
		dependencyDAG.nDirectSourceTargetEdges = knt;
//		System.out.println("Direct source to target edges: " + knt);
	}
}