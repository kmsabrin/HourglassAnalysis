package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import corehg.DependencyDAG;

public class DistributionAnalysis {
	static HashSet<String> visited;
	static int wccSize;

	public static void getDegreeStatistics(String edgeFilePath) throws Exception {
		Scanner scanner = new Scanner(new File(edgeFilePath));
		HashMap<String, Integer> inDeg = new HashMap();
		HashMap<String, Integer> outDeg = new HashMap();
		
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			String wgt = scanner.next();
			
			if (inDeg.containsKey(dst)) {
				inDeg.put(dst, inDeg.get(dst) + 1);
			}
			else {
				inDeg.put(dst, 1);
			}
			
			if (outDeg.containsKey(src)) {
				outDeg.put(src, outDeg.get(src) + 1);
			}
			else {
				outDeg.put(src, 1);
			}
		}
		
		for (String s: inDeg.keySet()) {
			if (outDeg.containsKey(s)) {
				if (inDeg.get(s) / outDeg.get(s) == 1) {
//					System.out.println(inDeg.get(s) + "\t" + outDeg.get(s));
					System.out.println(s);
				}
			}
		}
		
		scanner.close();
	}
	
	private static void WCCHelper(String s, DependencyDAG dependencyDAG) {
		// if (visited.contains(s)) return;
		++wccSize;
		visited.add(s);

		if (dependencyDAG.serves.containsKey(s)) {
			for (String r : dependencyDAG.serves.get(s)) {
				if (!visited.contains(r)) {
					WCCHelper(r, dependencyDAG);
				}
			}
		}

		if (dependencyDAG.depends.containsKey(s)) {
			for (String r : dependencyDAG.depends.get(s)) {
				if (!visited.contains(r)) {
					WCCHelper(r, dependencyDAG);
				}
			}
		}
	}

	public static void findWeaklyConnectedComponents(
			DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//largestWCC-"
				+ filePath + ".txt"));

		int largestWCCSize = 0;
		String largestWCCSeed = "";

		visited = new HashSet();
		int nWCC = 0;
		for (String s : dependencyDAG.nodes) {
			if (!visited.contains(s)) {
				wccSize = 0;
				++nWCC;
				WCCHelper(s, dependencyDAG);
				System.out.println("Component " + nWCC + " with size "
						+ wccSize);
				if (wccSize > largestWCCSize) {
					largestWCCSize = wccSize;
					largestWCCSeed = s;
				}
			}
		}

		System.out.println("Largest WCC size: " + largestWCCSize);
		visited.clear();
		WCCHelper(largestWCCSeed, dependencyDAG);
		for (String s : visited) {
			pw.println(s);
		}
		pw.close();
	}

	public static void printCentralityDistribution(DependencyDAG dependencyDAG,
			String filePath, int key) throws Exception {
		String[] dist = { "", "centrality", "outdegree" };
		PrintWriter pw = new PrintWriter(new File("analysis//"+ dist[key] + "-distribution-" + filePath + ".txt"));

		for (String s : dependencyDAG.nodes) {
			if (key == 1) {
//				if (dependencyDAG.serves.containsKey(s) && dependencyDAG.depends.containsKey(s)) { // only intermediate nodes
					pw.println(dependencyDAG.nodePathThrough.get(s));
//				}
			}
			else if (key == 2) {
				pw.println(dependencyDAG.outDegree.get(s));
			}
		}

		pw.close();
	}

	public static TreeMap<Double, Double> getDistributionCCDF(
			DependencyDAG dependencyDAG, String filePath, int key)
			throws Exception {
		String[] dist = { "", "centrality", "outdegree", "edgeWeight" };
		PrintWriter pw = new PrintWriter(new File("analysis//"+ dist[key] + "-ccdf-" + filePath + ".txt"));

		Map<Double, Double> histogram = new TreeMap<Double, Double>();
		Map<Double, Double> CDF = new TreeMap<Double, Double>();

		if (key == 3) { // special case: celegans neuro
			for (String s : dependencyDAG.edgeWeights.keySet()) {
				double v = dependencyDAG.edgeWeights.get(s);
				if (histogram.containsKey(v)) {
					histogram.put(v, histogram.get(v) + 1.0);
				} else {
					histogram.put(v, 1.0);
				}
			}

			// CDF: Cumulative Distribution Function
			double cumulativeSum = 0;
			for (double d : histogram.keySet()) {
				double v = histogram.get(d);
				// System.out.println(d + "\t" + v);
				cumulativeSum += v;
				CDF.put(d, cumulativeSum / dependencyDAG.edgeWeights.size());
			}
		}
		else {
			for (String s : dependencyDAG.nodes) {
				if (DependencyDAG.isCelegans && (s.equals("1000") || s.equals("2000"))) continue;
				double v = 0;
				if (key == 1)
//					v = dependencyDAG.normalizedPathCentrality.get(s); // dependencyDAG.nTotalPath;
				    v = dependencyDAG.nodePathThrough.get(s);
				else if (key == 2)
					v = dependencyDAG.outDegree.get(s);
				
				if (histogram.containsKey(v)) {
					histogram.put(v, histogram.get(v) + 1.0);
				} else {
					histogram.put(v, 1.0);
				}
			}

			// CDF: Cumulative Distribution Function
			double cumulativeSum = 0;
			for (double d : histogram.keySet()) {
				double v = histogram.get(d);
				// System.out.println(d + "\t" + v);
				cumulativeSum += v;
				CDF.put(d, cumulativeSum / dependencyDAG.nodes.size());
			}
		}

		// CCDF: Complementary CDF
		TreeMap<Double, Double> ccdfMap = new TreeMap();
		for (double d : CDF.keySet()) {
			double ccdfP = 1.0 - CDF.get(d);
			pw.println(d + "\t" + ccdfP);
			ccdfMap.put(d, ccdfP);
		}

		pw.close();
		return ccdfMap;
	}
	
	public static double getPathLength(DependencyDAG dependencyDAG) {	
//		int knt = 0;
//		double pathLengths[] = new double[(int) dependencyDAG.nSources];
//		double pathLengths[] = new double[(int) dependencyDAG.nTargets];
		double sumOfPathLengths = 0;
		for (String s : dependencyDAG.nodes) {
//			if (dependencyDAG.isSource(s) && !Double.isNaN(dependencyDAG.avgTargetDepth.get(s))) {
//				pathLengths[knt++] = dependencyDAG.avgTargetDepth.get(s);
//				System.out.println(s + " " + dependencyDAG.avgTargetDepth.get(s));
//				System.out.println(knt + "\t" + pathLengths[knt - 1]);
//			}
			
			if (dependencyDAG.isTarget(s) && !Double.isNaN(dependencyDAG.avgTargetDepth.get(s))) {
//				pathLengths[knt++] = dependencyDAG.avgSourceDepth.get(s);
//				System.out.println(s + " " + dependencyDAG.avgSourceDepth.get(s));
//				System.out.println(knt + "\t" + pathLengths[knt - 1]);
				sumOfPathLengths += dependencyDAG.avgSourceDepth.get(s) * dependencyDAG.numOfSourcePath.get(s);
			}
		}

//		System.out.println("Mean Path Length: " + sumOfPathLengths / dependencyDAG.nTotalPath);
//		System.out.println("Mean Path Length: " + StatUtils.mean(pathLengths));
//		System.out.println("STD Path Length: " + Math.sqrt(StatUtils.variance(pathLengths)));
//		System.out.println("Median Path Length: " + StatUtils.percentile(pathLengths, 50));
//		System.out.println("Max Path Length: " + StatUtils.max(pathLengths));

//		return StatUtils.percentile(pathLengths, 50);
//		return StatUtils.mean(pathLengths);
//		System.out.println(sumOfPathLengths / dependencyDAG.nTotalPath);
		return sumOfPathLengths / dependencyDAG.nTotalPath;
	}

	public static TreeMap<Integer, Integer> getDegreeHistogram(
			DependencyDAG dependencyDAG) {
		double inDegrees[] = new double[dependencyDAG.nodes.size()];
		double outDegrees[] = new double[dependencyDAG.nodes.size()];

		TreeMap<Integer, Integer> inDegreeHistogram = new TreeMap();
		TreeMap<Integer, Integer> outDegreeHistogram = new TreeMap();

		int kin = 0;
		int kout = 0;
		double inSum = 0;
		double outSum = 0;
		for (String s : dependencyDAG.nodes) {
			int iDeg = dependencyDAG.inDegree.get(s);
			int oDeg = dependencyDAG.outDegree.get(s);
			// System.out.println(s + "\t" + iDeg + "\t" + oDeg);
			if (inDegreeHistogram.containsKey(iDeg)) {
				int v = inDegreeHistogram.get(iDeg);
				inDegreeHistogram.put(iDeg, v + 1);
			} else
				inDegreeHistogram.put(iDeg, 1);

			if (outDegreeHistogram.containsKey(oDeg)) {
				int v = outDegreeHistogram.get(oDeg);
				outDegreeHistogram.put(oDeg, v + 1);
			} else
				outDegreeHistogram.put(oDeg, 1);

			if (dependencyDAG.isIntermediate(s) || dependencyDAG.isTarget(s)) {
				inDegrees[kin++] = iDeg;
				inSum++;
				// System.out.println("in Adding: " + iDeg + " for " + s);
			}

			if (dependencyDAG.isIntermediate(s) || dependencyDAG.isSource(s)) {
				outDegrees[kout++] = oDeg;
				outSum++;
				// System.out.println("out Adding: " + oDeg);
			}
		}

		inDegrees = Arrays.copyOf(inDegrees, kin);
		outDegrees = Arrays.copyOf(outDegrees, kout);

		for (double i : inDegrees) {
			// System.out.println((int)i);
		}

		System.out.println("------------------------\n\n");

		for (double o : outDegrees) {
			// System.out.println((int)o);
		}

		for (int i : inDegreeHistogram.keySet()) {
			if (i < 1)
				continue;
			// System.out.println(i + "\t" + (inDegreeHistogram.get(i) /
			// inSum));
		}

		System.out.println("------------------------");

		for (int i : outDegreeHistogram.keySet()) {
//			if (i < 1)
//				continue;
//			 System.out.println(i + "\t" + (outDegreeHistogram.get(i) / outSum));
			System.out.println(i + "\t" + (outDegreeHistogram.get(i)));
		}

		// System.out.println(StatUtils.percentile(inDegrees, 50) + "\t" +
		// StatUtils.mean(inDegrees));
		System.out.println(" Indegree:" + " Mean: " + StatUtils.mean(inDegrees)
				+ " Variance: " + StatUtils.variance(inDegrees));
		// System.out.println(" Indegree:" + " Mean: " +
		// StatUtils.mean(inDegrees) + " StD: " +
		// Math.sqrt(StatUtils.variance(inDegrees)));
		// System.out.println("Outdegree:" + " Mean: " +
		// StatUtils.mean(outDegrees) + " StD: " +
		// Math.sqrt(StatUtils.variance(outDegrees)));
		//
		// System.out.println(" Indegree:" + " 25p: " +
		// StatUtils.percentile(inDegrees, 10) + " 75p: " +
		// StatUtils.percentile(inDegrees, 90) );
		// System.out.println("Outdegree:" + " 25p: " +
		// StatUtils.percentile(outDegrees, 10) + " 75p: " +
		// StatUtils.percentile(outDegrees, 90) );

		return inDegreeHistogram;
	}

	public static void getDegreeHistogramSpecialized(DependencyDAG dependencyDAG) {
		TreeMap<Integer, Integer> inDegreeHistogramNearTarget = new TreeMap();
		TreeMap<Integer, Integer> inDegreeHistogramNearSource = new TreeMap();

		int maxDeg = 0;
		double nearSourceDegSum = 0;
		double nearTargetDegSum = 0;
		for (String s : dependencyDAG.nodes) {
			int iDeg = dependencyDAG.inDegree.get(s);
			if (dependencyDAG.lengthPathLocation.get(s) > 0
					&& dependencyDAG.lengthPathLocation.get(s) < 0.4) {
				if (iDeg > maxDeg)
					maxDeg = iDeg;
				nearSourceDegSum += iDeg;
				System.out.println(iDeg);
				if (inDegreeHistogramNearSource.containsKey(iDeg)) {
					int v = inDegreeHistogramNearSource.get(iDeg);
					inDegreeHistogramNearSource.put(iDeg, v + 1);
				} else
					inDegreeHistogramNearSource.put(iDeg, 1);
			}

			if (dependencyDAG.lengthPathLocation.get(s) > 0.9) {
				if (iDeg > maxDeg)
					maxDeg = iDeg;
				nearTargetDegSum += iDeg;
				// System.out.println(iDeg);
				if (inDegreeHistogramNearTarget.containsKey(iDeg)) {
					int v = inDegreeHistogramNearTarget.get(iDeg);
					inDegreeHistogramNearTarget.put(iDeg, v + 1);
				} else
					inDegreeHistogramNearTarget.put(iDeg, 1);
			}
		}

		// System.out.println("Indeg\tNear-Source\tNear-Target");
		// for (int i = 1; i <= maxDeg; ++i) {
		// System.out.print(i);
		// if (inDegreeHistogramNearSource.containsKey(i)) {
		// System.out.print("\t" + (inDegreeHistogramNearSource.get(i) * 1.0 /
		// nearSourceDegSum));
		// }
		// else {
		// System.out.print("\t" + "0");
		// }
		//
		// if (inDegreeHistogramNearTarget.containsKey(i)) {
		// System.out.print("\t" + (inDegreeHistogramNearTarget.get(i) * 1.0 /
		// nearTargetDegSum));
		// }
		// else {
		// System.out.print("\t" + "0");
		// }
		//
		// System.out.println();
		// }

		// for (int i: inDegreeHistogramNearSource.keySet()) {
		// System.out.println(i + "\t" + inDegreeHistogramNearSource.get(i));
		// }
		//
		// System.out.println("------------------------");
		// System.out.println("------------------------");
		//
		// for (int i: inDegreeHistogramNearTarget.keySet()) {
		// System.out.println(i + "\t" + inDegreeHistogramNearTarget.get(i));
		// }
	}

	public static void getAverageInOutDegree(DependencyDAG dependencyDAG) {
		double inDeg = 0;
		double outDeg = 0;

		double avgInVsOut = 0;
		for (String s : dependencyDAG.nodes) {
			inDeg += dependencyDAG.inDegree.get(s);
			outDeg += dependencyDAG.outDegree.get(s);
			avgInVsOut += inDeg * 1.0 / outDeg;
		}

		System.out.println("Avg  In Deg: "
				+ (1.0 * inDeg / dependencyDAG.nodes.size()));
		System.out.println("Avg Out Deg: "
				+ (1.0 * outDeg / dependencyDAG.nodes.size()));
		System.out.println("Avg In/Out Deg: "
				+ (1.0 * avgInVsOut / dependencyDAG.nodes.size()));
	}

	public static void printEdgeList(DependencyDAG dependencyDAG,
			String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("edgelist_graphs//"
				+ filePath + "_edgelist_indexed.txt"));

		HashMap<String, Integer> nodeIndex = new HashMap();
		int i = 0;
		for (String s : dependencyDAG.nodes) {
			System.out.println(s + "\t" + i);
			nodeIndex.put(s, i++);
		}

		for (String s : dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				for (String r : dependencyDAG.serves.get(s)) {
					// pw.println(s + "\t" + r);
					pw.println(nodeIndex.get(s) + "\t" + nodeIndex.get(r));
				}
			}
		}
		pw.close();
	}

	public static void printAllCentralities(DependencyDAG dependencyDAG,
			String filePath) throws Exception {
		// order: node indeg outdeg closeness betwenness location path

		Scanner scanner = new Scanner(new File("python_centralities//"
				+ filePath + "_centralities.txt"));
		PrintWriter pw = new PrintWriter(new File("analysis//" + filePath
				+ "_all_centralities.txt"));

		while (scanner.hasNext()) {
			String node = scanner.next();
			double deg_c = scanner.nextDouble();
			double eigen_c = scanner.nextDouble();
			double closeness_c = scanner.nextDouble();
			double betwenness_c = scanner.nextDouble();
			if (dependencyDAG.nodes.contains(node) == false)
				continue;
			// pw.println(node + " " + indeg_c + " " + outdeg_c + " " +
			// closeness_c + " " + betwenness_c + " " +
			// dependencyDAG.location.get(node) + " " +
			// dependencyDAG.normalizedPathCentrality.get(node));
			pw.println(deg_c + "\t" + eigen_c + "\t" + closeness_c + "\t"
					+ betwenness_c + "\t" + dependencyDAG.lengthPathLocation.get(node)
					+ "\t" + dependencyDAG.normalizedPathCentrality.get(node));
		}

		scanner.close();
		pw.close();
	}

	public static void findNDirectSrcTgtBypasses(DependencyDAG dependencyDAG,
			String filePath) {
		int knt = 0;
		for (String n : dependencyDAG.nodes) {
			if (dependencyDAG.isSource(n)
					&& dependencyDAG.serves.containsKey(n)) {
				for (String r : dependencyDAG.serves.get(n)) {
					if (dependencyDAG.isTarget(r)) {
						++knt;
					}
				}
			}
		}

		dependencyDAG.nDirectSourceTargetEdges = knt;
		System.out.println("Direct source to target edges: " + knt);
	}
	
	public static void getLocationHistogram(DependencyDAG dependencyDAG) {
		HashMap<Double, Double> hist = new HashMap();
		for (String s: dependencyDAG.nodes) {
			double loc = dependencyDAG.numPathLocation.get(s);
			if (hist.containsKey(loc)) {
				hist.put(loc, hist.get(loc) + 1.0);
			}
			else {
				hist.put(loc, 1.0);
			}
		}
		
		for (double d: hist.keySet()) {
			System.out.println(d + "\t" + (hist.get(d) / dependencyDAG.nodes.size()));
		}
	}
	
	public static void getLocationColorWeightedHistogram(DependencyDAG dependencyDAG) {
		double binWidth = 0.1;
		int numBin = (int)(1.0 / binWidth) + 2;
		int binKount[] = new int[numBin];
		
		ArrayList< ArrayList<String> > colorValues = new ArrayList();
		for (int i = 0; i < numBin; ++i) {
			colorValues.add(new ArrayList<String>());
		}
		
		for (String s: dependencyDAG.nodes) {
			double loc = dependencyDAG.numPathLocation.get(s);
			int binLocation = -1;
			if (dependencyDAG.isSource(s)) {
				binLocation = 0;
			}
			else if (dependencyDAG.isTarget(s)) {
				binLocation = numBin - 1;
			}
			else {
				binLocation = 1 + (int)(loc / binWidth);
			}
			binKount[binLocation]++;
			
			colorValues.get(binLocation).add(s);
		}
		
		int matrixMaxHeight = 0;
		for (int i = 0; i < numBin; ++i) {
//			System.out.println((i + 1) + "\t" + binKount[i]);
			if (binKount[i] > matrixMaxHeight) {
				matrixMaxHeight = binKount[i];
			}
		}
		matrixMaxHeight++;
		
		double colorMatrixValue[][] = new double[matrixMaxHeight][numBin];
		String colorMatrixName[][] = new String[matrixMaxHeight][numBin];
		
		int midIndex = matrixMaxHeight / 2;
		for (int i = 0; i < numBin; ++i) {
//			ArrayList<Double> aList = colorValues.get(i);
			TreeMultimap<Double, String> sortedStrings = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
			for (String s: colorValues.get(i)) {
				sortedStrings.put(dependencyDAG.normalizedPathCentrality.get(s), s);
			}
			if (sortedStrings.size() < 1) continue;
			
			ArrayList<Double> aListValue = new ArrayList(sortedStrings.keys());
			ArrayList<String> aListName = new ArrayList(sortedStrings.values());
			int k = 0;
			colorMatrixValue[midIndex + k][i] = aListValue.get(0);
			colorMatrixName[midIndex + k][i] = aListName.get(0);
			++k;
			for (int j = 1; j < aListValue.size(); ++j) {
				colorMatrixValue[midIndex + k][i] = aListValue.get(j);
				colorMatrixName[midIndex + k][i] = aListName.get(j);
				if (j + 1 < aListValue.size()) {
					colorMatrixValue[midIndex - k][i] = aListValue.get(j + 1);
					colorMatrixName[midIndex - k][i] = aListName.get(j + 1);
					++k;
					++j;
				}
				else {
					break;
				}
			}
		}
		
		for (int i = 0; i < matrixMaxHeight; ++i) {
			for (int j = 0; j < numBin; ++j) {
				if (colorMatrixValue[i][j] != 0) {
//					double truncated = ((int)colorMatrixValue[i][j] * 1000) / 1000.0;
//					System.out.print(idNeuronMap.get(colorMatrixName[i][j]) + " (" + truncated + ")\t");
					System.out.print(colorMatrixValue[i][j] + "\t");
				}
				else {
					System.out.print(" " + "\t");
				}
			}
			System.out.println();
		}
	}
}
