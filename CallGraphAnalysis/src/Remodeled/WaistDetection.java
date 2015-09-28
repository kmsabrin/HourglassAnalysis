package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class WaistDetection {
	static HashSet<String> topRemovedWaistNodes = new HashSet();
	static double pathCoverageTau = 0.98;

	public static void heuristicWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//path-cover-2-" + filePath + ".txt"));
		double cumulativePathsTraversed = 0;
		double tPath = dependencyDAG.nTotalPath;
		int nodeRank = 0;
		
		HashSet<String> tiedNodeSet = new HashSet();
		HashSet<String> tempTiedNodeSet = new HashSet();
		
		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, "xxx");
		
		while (true) {
			double maxPathThrough = 0;
			String maxPathNode = "";
			int ties = 0;
			
			for (String s : dependencyDAG.nodes) {
				// skip if source or target
				if (!dependencyDAG.serves.containsKey(s) || !dependencyDAG.depends.containsKey(s)) {
					continue;
				}

				// find the node with largest through path
				double nPathThrough = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
//				System.out.print(s + ":" + nPathThrough + ":" + maxPathThrough + " ");
				if (nPathThrough > maxPathThrough) {
					maxPathThrough = nPathThrough;
					maxPathNode = s;
					ties = 0;
					tempTiedNodeSet.clear();
				}
				// count ties
				else if (Math.abs(nPathThrough - maxPathThrough) < 0.0001) { // is a tie
					++ties;
					tempTiedNodeSet.add(s);
				}
			}
//			System.out.println();
			System.out.println("Max centrality node: " + maxPathNode);
			
			// print tie information
			++nodeRank;
			if (ties > 0) {
				System.out.println("Tie for maximum: " + (ties+1) + " at node rank: " + nodeRank);
				tiedNodeSet.addAll(tempTiedNodeSet);
//				randomly pick one of the largest centrality nodes
				maxPathNode = new ArrayList<String>(tempTiedNodeSet).get(new Random(System.nanoTime()).nextInt(tempTiedNodeSet.size()));
				System.out.println("Randomly chosen node: " + maxPathNode);
			}
			
			
			// record the largest through path node
			cumulativePathsTraversed += maxPathThrough;
//			System.out.println(maxPathNode + "\t" + maxPathThrough + "\t" + cumulativePathsTraversed);

			// add to waist
			topRemovedWaistNodes.add(maxPathNode);

			// if all paths have been traversed (except direct s-t edges), then break out
			if (cumulativePathsTraversed >= ((tPath - dependencyDAG.nDirectSourceTargetEdges) * pathCoverageTau)) {
				break;
			}

			// remove the largest through path node, recompute through paths for all remaining nodes
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
		}
		
		for (String s: tiedNodeSet) {
			if (!topRemovedWaistNodes.contains(s)) {
//				System.out.println(s);
			}
		}
		
		System.out.println("Waist size: " + topRemovedWaistNodes.size() + " with nodes: " + topRemovedWaistNodes);
	}

	/*
	 * public static void runPCWaistDetection(DependencyDAG dependencyDAG,
	 * String filePath) throws Exception { PrintWriter pw = new PrintWriter(new
	 * File("analysis//path-cover-" + filePath + ".txt"));
	 * 
	 * TreeMultimap<Double, String> centralitySortedNodes =
	 * TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
	 * for (String s : dependencyDAG.nodes) { if
	 * (dependencyDAG.depends.containsKey(s) &&
	 * dependencyDAG.serves.containsKey(s)) {
	 * centralitySortedNodes.put(dependencyDAG.normalizedPathCentrality.get(s),
	 * s); } }
	 * 
	 * int k = 500; ArrayList<String> tempTopKNodes; double tPath =
	 * dependencyDAG.nTotalPath; topKNodes = new HashSet(); tempTopKNodes = new
	 * ArrayList();
	 * 
	 * for (double pC : centralitySortedNodes.keySet()) { Collection<String>
	 * nodes = centralitySortedNodes.get(pC); for (String s : nodes) {
	 * tempTopKNodes.add(s); }
	 * 
	 * if (tempTopKNodes.size() >= k) { // break; } }
	 * 
	 * TreeMultimap<Double, String> pathCoverageSortedNodes =
	 * TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
	 * double individualCumulativePaths = 0; double pathCoverage = 0; for
	 * (String s : tempTopKNodes) { dependencyDAG.numOfTargetPath.clear();
	 * dependencyDAG.numOfSourcePath.clear();
	 * dependencyDAG.loadPathStatistics(); double individualPaths =
	 * dependencyDAG.numOfTargetPath.get(s) *
	 * dependencyDAG.numOfSourcePath.get(s); individualCumulativePaths +=
	 * individualPaths; topKNodes.add(s); pathCoverage =
	 * individualCumulativePaths / tPath;
	 * 
	 * 
	 * // if (individualPaths > 0.000001) { // skip the nodes covering zero
	 * unique paths pathCoverageSortedNodes.put(individualPaths / tPath, s); }
	 * 
	 * // if (pathCoverage + 0.00000001 >= pathCoverageTau) { // break; // }
	 * 
	 * // if (topKNodes.size() > 100) { // break; // } }
	 * 
	 * 
	 * // System.out.println("New Waist !!! " + pathCoverageSortedNodes.size());
	 * // System.out.println("Centrality Sorted Path Coverage Waist Size: " +
	 * topKNodes.size());
	 * 
	 * tempTopKNodes.clear(); topKNodes.clear();
	 * 
	 * for (double pC : pathCoverageSortedNodes.keySet()) { Collection<String>
	 * nodes = pathCoverageSortedNodes.get(pC); for (String s : nodes) {
	 * tempTopKNodes.add(s); }
	 * 
	 * // if (tempTopKNodes.size() >= k) { // break; // } }
	 * 
	 * individualCumulativePaths = 0; double zeroPathContributors = 0; for
	 * (String s : tempTopKNodes) { dependencyDAG.numOfTargetPath.clear();
	 * dependencyDAG.numOfSourcePath.clear();
	 * dependencyDAG.loadPathStatistics(); double individualPaths =
	 * dependencyDAG.numOfTargetPath.get(s) *
	 * dependencyDAG.numOfSourcePath.get(s);
	 * 
	 * individualCumulativePaths += individualPaths; topKNodes.add(s);
	 * pathCoverage = individualCumulativePaths / tPath;
	 * pw.println(topKNodes.size() + " " + pathCoverage);
	 * 
	 * // System.out.println(s); // System.out.println(s + "\t" +
	 * individualPaths + "\t" + (individualCumulativePaths / (tPath -
	 * dependencyDAG.nDirectSourceTargetEdges))); System.out.println(s + "\t" +
	 * individualPaths + "\t" + individualCumulativePaths);
	 * 
	 * 
	 * // System.out.println("Individual path coverage of " + s + " : " +
	 * individualPaths);
	 * 
	 * if (individualPaths < 0.000001) { // continue; // break;
	 * ++zeroPathContributors; }
	 * 
	 * if (individualCumulativePaths >= tPath -
	 * dependencyDAG.nDirectSourceTargetEdges) { //
	 * System.out.println(individualCumulativePaths + "vs" + (tPath -
	 * dependencyDAG.nDirectSourceTargetEdges)); break; }
	 * 
	 * if (pathCoverage + 1e-5 >= pathCoverageTau) { // break; }
	 * 
	 * 
	 * 
	 * // if (topKNodes.size() > 100) { // break; // } }
	 * 
	 * System.out.println("Zero path contributors: " + zeroPathContributors);
	 * System.out.println("Achieved path coverge: " + pathCoverage);
	 * System.out.println("Path convered: " + individualCumulativePaths);
	 * getONodes(dependencyDAG); pw.close(); }
	 */

	public static void getONodes(DependencyDAG dependencyDAG) {
		HashSet<String> waistNodeCoverage = new HashSet();

		int STNodes = 0;

		for (String s : topRemovedWaistNodes) {
			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using
													// her
			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);

			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes are
														// using her
			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);

			if (!dependencyDAG.serves.containsKey(s)
					|| !dependencyDAG.depends.containsKey(s)) {
				++STNodes;
			}
		}

		System.out.println("Waist Size: " + topRemovedWaistNodes.size());
		System.out.print("Waist Node Coverage: " + waistNodeCoverage.size()
				+ " of " + dependencyDAG.nodes.size() + " i.e. ");
		System.out.println(waistNodeCoverage.size() * 100.0
				/ dependencyDAG.nodes.size() + "%%");

		double nonSTinWaist = (topRemovedWaistNodes.size() - STNodes) * 1.0
				/ topRemovedWaistNodes.size();
		double minST = Math.min(dependencyDAG.nSources, dependencyDAG.nTargets);
		// System.out.println("nonSTinWaist: " + (topKNodes.size() - STNodes));
		double hourglassnessScore = nonSTinWaist
				* ((minST - Math.min(topRemovedWaistNodes.size() - STNodes, minST) + 1.0) / minST);
		// System.out.println("Hourglassness: " + hourglassnessScore);
	}
	
	public static void runPRWaistDetection(DependencyDAG dependencyDAG) {
		/*
		TreeMultimap<Double, String> pagerankSortedNodes = TreeMultimap.create(
				Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.nodes) {
			if (dependencyDAG.depends.containsKey(s)
					&& dependencyDAG.serves.containsKey(s)) {
				pagerankSortedNodes.put(
						dependencyDAG.harmonicMeanPagerankCentrality.get(s), s);
			}
		}

		int k = 100;
		ArrayList<String> tempTopKNodes;
		while (true) {
			topKNodes = new HashSet();
			tempTopKNodes = new ArrayList();

			for (double prC : pagerankSortedNodes.keySet()) {
				Collection<String> nodes = pagerankSortedNodes.get(prC);
				for (String s : nodes) {
					topKNodes.add(s);
					tempTopKNodes.add(s);
					System.out.print(s + " " + prC);
					System.out.println(" "
							+ dependencyDAG.pagerankTargetCompression.get(s)
							+ " "
							+ dependencyDAG.pagerankSourceCompression.get(s));
				}

				if (topKNodes.size() >= k) {
					break;
				}
			}

			double individualizedCumulativeTargetCompression = 0;
			double individualizedCumulativeSourceCompression = 0;
			for (String s : tempTopKNodes) {
				topKNodes.remove(s);
				dependencyDAG.pagerankTargetCompression.clear();
				dependencyDAG.pagerankSourceCompression.clear();
				dependencyDAG.loadPagerankCentralityMetric();
				individualizedCumulativeTargetCompression += dependencyDAG.pagerankTargetCompression
						.get(s);
				individualizedCumulativeSourceCompression += dependencyDAG.pagerankSourceCompression
						.get(s);
				topKNodes.add(s);
				// System.out.println(s + " " +
				// dependencyDAG.pagerankTargetCompression.get(s) + " " +
				// dependencyDAG.pagerankSourceCompression.get(s));
			}

			System.out.println("Top-" + k + " "
					+ individualizedCumulativeTargetCompression + "\t"
					+ individualizedCumulativeSourceCompression);
			// System.out.println("### ### ###");

			if (individualizedCumulativeTargetCompression >= 0.9
					&& individualizedCumulativeSourceCompression >= 0.9) {
				break;
			}

			if (k >= 100) {
				System.out.println("No waist");
				break;
			}

			k += 10;
		}*/
	}
	
}
