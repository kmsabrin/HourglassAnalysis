package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

import Remodeled.DependencyDAG.PathCentralityComparator;

public class WaistDetection {
	static HashSet<String> topRemovedWaistNodes = new HashSet();
	static double pathCoverageTau = 0.94;
	static HashMap<String, Double> averageWaistRank;
	
	static double waistSize;
	static double nodeCoverage;
	static double effectiveNodeCoverage;
	static double hourglassness;
	static double hourglassSymmetry;
	static boolean thresholdSatisfied;
	
	static ArrayList<Double> ws = new ArrayList();
	static ArrayList<Double> pc = new ArrayList();
	
	static ArrayList<HashSet<String>> waistSets = new ArrayList();
	static double minWaistSize;
	static int randomPerturbation = 3;
	static HashSet<String> uniqueWaistNodes = new HashSet();
	
	public static void heuristicWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
//		PrintWriter pw0 = new PrintWriter(new File("analysis//path-cover-2-" + filePath + ".txt"));
		PrintWriter pw1 = new PrintWriter(new File("analysis//coverage-threshold-" + filePath + ".txt"));

		double cumulativePathsTraversed = 0;
		double tPath = dependencyDAG.nTotalPath;
		int nodeRank = 0;
		
		HashSet<String> tiedNodeSet = new HashSet();
		HashSet<String> tempTiedNodeSet = new HashSet();
		
		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, "xxx");
//		System.out.println("Direct Tubes:" + DependencyDAG.nDirectSourceTargetEdges);
		
		while (true) {
			double maxPathThrough = 0;
			String maxPathNode = "";
			int ties = 0;
			
			PriorityQueue<String> pq = new PriorityQueue(randomPerturbation, dependencyDAG.new PathCentralityComparator());
			for (String s : dependencyDAG.nodes) {
//				skip if source or target
				if (dependencyDAG.isSource(s) || dependencyDAG.isTarget(s)) {
					continue;
				}

				pq.add(s);
				
//				find the node with largest through path
				double nPathThrough = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
				if (nPathThrough > maxPathThrough) {
					maxPathThrough = nPathThrough;
					maxPathNode = s;
					
					ties = 0;
					tempTiedNodeSet.clear();
					tempTiedNodeSet.add(s);
				}
				// count ties
				else if (Math.abs(nPathThrough - maxPathThrough) < 0.0001) { // is a tie
					++ties;
					tempTiedNodeSet.add(s);
				}
			}
			
			if (DependencyDAG.isSynthetic == true) {
				if (maxPathThrough == 0) {
					break;
				}
			}
			
//			System.out.println("Max centrality node: " + maxPathNode + " with unique paths: " + maxPathThrough);
//			System.out.println("Source path: " + dependencyDAG.numOfSourcePath.get(maxPathNode) + " Target path: " + dependencyDAG.numOfTargetPath.get(maxPathNode));
			
//			print tie information
			++nodeRank;
			if (ties > 0) {
//				System.out.println("Tie for maximum: " + tempTiedNodeSet.size() + " at node rank: " + nodeRank);
//				randomly pick one of the largest centrality nodes
				maxPathNode = new ArrayList<String>(tempTiedNodeSet).get(new Random(System.nanoTime()).nextInt(tempTiedNodeSet.size()));
//				System.out.println("Randomly chosen node: " + maxPathNode);	
//				tiedNodeSet.addAll(tempTiedNodeSet);
			}
	
			/*****random top-k *****/
			maxPathNode = (String)pq.toArray()[new Random(System.nanoTime()).nextInt(randomPerturbation)];
			maxPathThrough = dependencyDAG.numOfSourcePath.get(maxPathNode) * dependencyDAG.numOfTargetPath.get(maxPathNode);
			
//			record the largest through path node
			cumulativePathsTraversed += maxPathThrough;			
//			System.out.println(maxPathThrough / tPath * 100.0);
//			System.out.println(maxPathNode + "\t" + maxPathThrough + "\t" + cumulativePathsTraversed);

//			add to waist
			topRemovedWaistNodes.add(maxPathNode);
			
//			update average waist entry rank
			if (averageWaistRank.containsKey(maxPathNode)) {
				double currentRank = averageWaistRank.get(maxPathNode);
				averageWaistRank.put(maxPathNode, (currentRank + nodeRank) * 0.5 );
			}
			else {
				averageWaistRank.put(maxPathNode, nodeRank * 1.0);
			}

//			if all paths have been traversed (except direct s-t edges), then break out
//			if (cumulativePathsTraversed >= ((tPath - dependencyDAG.nDirectSourceTargetEdges) * pathCoverageTau)) {
//				break;
//			}
			
			pw1.println(topRemovedWaistNodes.size() + "\t" + (cumulativePathsTraversed / tPath));
			ws.add(topRemovedWaistNodes.size() * 1.0);
			pc.add(cumulativePathsTraversed / tPath);
			
			if (cumulativePathsTraversed >= (tPath * pathCoverageTau)) {
				thresholdSatisfied = true;
				break;
			}
			
			if (topRemovedWaistNodes.size() >= (dependencyDAG.nodes.size() - dependencyDAG.nSources - dependencyDAG.nTargets)) {
				break;
			}

//			remove the largest through path node, recompute through paths for all remaining nodes
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
		}
		
//		System.out.println("Waist size: " + topRemovedWaistNodes.size() + " with nodes: " + topRemovedWaistNodes);
		
//		for (String s: tiedNodeSet) {
//			if (!topRemovedWaistNodes.contains(s)) {
//				System.out.println(s);
//			}
//		}
		
		if (thresholdSatisfied) {
			if (topRemovedWaistNodes.size() < minWaistSize) {
				minWaistSize = topRemovedWaistNodes.size();
				waistSets.clear();
				uniqueWaistNodes.clear();
				waistSets.add(new HashSet(topRemovedWaistNodes));
				uniqueWaistNodes.addAll(topRemovedWaistNodes);
			}
			else if (topRemovedWaistNodes.size() == minWaistSize) {
				waistSets.add(new HashSet(topRemovedWaistNodes));
				uniqueWaistNodes.addAll(topRemovedWaistNodes);
			}
		}
		
		pw1.close();
	}
	
	public static void pathCoverageThresholdDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		averageWaistRank = new HashMap();
		pathCoverageTau = 1.0;
		ws = new ArrayList();
		pc = new ArrayList();
		heuristicWaistDetection(dependencyDAG, filePath);
		
		double x1 = ws.get(0);
		double y1 = pc.get(0);
		double x2 = ws.get(ws.size() - 1);
		double y2 = pc.get(pc.size() - 1);
		double maxDistance = 0;
		double minWS = 1;
		double tau = 0;
		double crossX = -1;
		double crossY = -1;
		double maxD = -1;
//		System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);
		for (int i = 0; i < ws.size(); ++i) {
			double x0 = ws.get(i);
			double y0 = pc.get(i);
			
			double distance = Math.abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1)) / Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
//			double distance = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1 - y2 * x1) / Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));   
			if (distance > maxDistance) {
				maxDistance = distance;
				minWS = x0;
				tau = y0;
				double d12 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
				double u = ((x0 - x1) * (x2 - x1) + (y0 - y1) * (y2 - y1)) / d12;
				crossX = x1 + u * (x2 - x1);
				crossY = y1 + u * (y2 - y1);	
				maxD = (x0 - crossX) * (x0 - crossX) + (y0 - crossY) * (y0 - crossY);
				maxD = Math.sqrt(maxD);
			}
		}
		
//		System.out.println("Max D: " + maxD);
		System.out.println(minWS + " with distance " + maxDistance + " and tau " + tau + " and intersected at " + crossX + "," + crossY);
		
		/*
		PrintWriter pw1 = new PrintWriter(new File("analysis//coverage-threshold-" + filePath + ".txt"));
//		PrintWriter pw2 = new PrintWriter(new File("analysis//node-coverage-threshold-" + filePath + ".txt"));
		
		for (pathCoverageTau = 0.50; pathCoverageTau < 1.0; pathCoverageTau += 0.01) {
			topRemovedWaistNodes.clear();
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			
			averageWaistRank = new HashMap();
			heuristicWaistDetection(dependencyDAG, filePath);
			int currentWaistSize = topRemovedWaistNodes.size();
//			pw1.println(currentWaistSize + "\t" + pathCoverageTau + "\t" + ((1.0 - pathCoverageTau) * currentWaistSize));
			
			pw1.println(currentWaistSize + "\t" + pathCoverageTau);
//			double nodeCoverage = getNodeCoverage(dependencyDAG);
//			pw1.println(currentWaistSize + "\t" + pathCoverageTau + "\t" + nodeCoverage);
		}
		
		pw1.close();
		*/
	}
	
	public static void randomizedWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		HashMap<String, Integer> nodeFrequencyInWaist = new HashMap();
		HashMap<Integer, Integer> waistSizeFrequencey = new HashMap();
		averageWaistRank = new HashMap();
		topRemovedWaistNodes.clear();
		thresholdSatisfied = false;
		
		int nRuns = 500;
		minWaistSize = 100000;
		averageWaistRank = new HashMap();
		for (int i = 1; i <= nRuns; ++i) {
			topRemovedWaistNodes.clear();
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			
			
			heuristicWaistDetection(dependencyDAG, filePath);
			int currentWaistSize = topRemovedWaistNodes.size();
			if (waistSizeFrequencey.containsKey(currentWaistSize)) {
				int freq = waistSizeFrequencey.get(currentWaistSize);
				waistSizeFrequencey.put(currentWaistSize, freq + 1);
			}
			else waistSizeFrequencey.put(currentWaistSize, 1);
			
			for (String n: topRemovedWaistNodes) {
				if (nodeFrequencyInWaist.containsKey(n)) {
					int	freq = nodeFrequencyInWaist.get(n);
					nodeFrequencyInWaist.put(n, freq + 1);
				}
				else {
					nodeFrequencyInWaist.put(n, 1);
				}
			}			
		}
		
		System.out.println("Unique Waist Size: " + uniqueWaistNodes.size());
		for (String s: uniqueWaistNodes) {
			System.out.println(s);
		}

//		System.out.println("-+-+-+-+-+-+-+-+-+-+-+");
//		System.out.println("Num of different waist " + waistSets.size());
//		System.out.println("All Waists");
//		for (HashSet<String> hs: waistSets) {
//			for (String s: hs) {
//				System.out.print(s + "  ");
//			}
//			System.out.println();
//		}
//		System.out.println("-+-+-+-+-+-+-+-+-+-+-+");
		
		for (int i: waistSizeFrequencey.keySet()) {
//			System.out.println("Waist size " + i + "\t with frequency " + (waistSizeFrequencey.get(i) * 1.0 / nRuns));
		}
//		System.out.println("-- --");
		if (thresholdSatisfied) {
			waistSize = Collections.min(waistSizeFrequencey.keySet());
		}
		else {
			waistSize = 0;
		}
		
		topRemovedWaistNodes.clear();
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();
		for (String n: nodeFrequencyInWaist.keySet()) {
//			System.out.println(n + "\t" + (nodeFrequencyInWaist.get(n) * 1.0 / nRuns) + "\t" + averageWaistRank.get(n) /*+ "\t" + dependencyDAG.normalizedPathCentrality.get(n)*/);
		}
		
		System.out.println("Waist Size: " + waistSize);
		getNodeCoverage(dependencyDAG);
//		nodeCentralityWRTWaist(dependencyDAG);
	}
	
	public static void computeSymmetry(DependencyDAG dependencyDAG) {
		
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

	public static double getNodeCoverage(DependencyDAG dependencyDAG) {
		HashSet<String> waistNodeCoverage = new HashSet();

		for (String s : averageWaistRank.keySet()) {
			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
//			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);

			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes are using her
//			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);
		}

//		System.out.println("Waist Size: " + averageWaistRank.size());
//		System.out.print("Waist Node Coverage: " + waistNodeCoverage.size() + " of " + dependencyDAG.nodes.size() + " i.e. ");
//		System.out.println(waistNodeCoverage.size() * 100.0 / dependencyDAG.nodes.size() + "%%");
		
		
//		System.out.println("Intermediate Nodes Not Covered: ");
//		HashSet<String> waistNodeNotCoverage = new HashSet();
//		int sum = 0;
		double notCovered = dependencyDAG.nodes.size() - waistNodeCoverage.size();
		
		double notCoveredSource = 0;
		double notCoveredTarget = 0;
		double notCoveredMiddle = 0;
		double notCoveredSpecialSource = 0;
		double notCoveredSpecialTarget = 0;
		for (String s: dependencyDAG.nodes) {
			if (!waistNodeCoverage.contains(s)) {
				if(dependencyDAG.isSource(s)) 
				{
					++notCoveredSource;
					
					int f = 0;
					if (dependencyDAG.serves.containsKey(s)) {
						for (String r : dependencyDAG.serves.get(s)) {
							if (dependencyDAG.isIntermediate(r)) {
								f = 1;
								break;
							}
						}
					}
					if (f == 0) ++notCoveredSpecialSource; // only serves targets or none
				}
				else if (dependencyDAG.isTarget(s)) {
					++notCoveredTarget;
					
					int f = 0;
					for (String r: dependencyDAG.depends.get(s)) {
						if(dependencyDAG.isIntermediate(r)) {
							f = 1;
							break;
						}
					}
					if (f == 0) ++notCoveredSpecialTarget; // only depends on sources
				}
				else {
					++notCoveredMiddle;
				}
				
//					System.out.println(s);
//					if (dependencyDAG.serves.containsKey(s)) {
//						for (String r: dependencyDAG.serves.get(s)) {
////							System.out.print(r + "," + dependencyDAG.normalizedPathCentrality.get(r) + " - ");
//						}
//					}
//					System.out.println();
//					
//					if (dependencyDAG.depends.containsKey(s)) {
//						for (String r: dependencyDAG.depends.get(s)) {
////							System.out.print(r + "," + dependencyDAG.normalizedPathCentrality.get(r) + " - ");
//						}
//					}
//					System.out.println("\n");
//					dependencyDAG.visited.clear();
//					dependencyDAG.kounter = 0;
//					dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
//					dependencyDAG.visited.remove(s); // remove ifself
//					waistNodeNotCoverage.addAll(dependencyDAG.visited);
//
////					dependencyDAG.visited.clear();
////					dependencyDAG.kounter = 0;
//					dependencyDAG.reachableDownwardsNodes(s); // how many nodes are using her
////					dependencyDAG.visited.remove(s); // remove ifself
//					waistNodeNotCoverage.addAll(dependencyDAG.visited);
//
//					sum += dependencyDAG.kounter;
//					System.out.println(s + " did not cover " + dependencyDAG.kounter);
//					System.out.println("-- " + s + "\tinDeg: " + dependencyDAG.inDegree.get(s) + "\toutDeg: " + dependencyDAG.outDegree.get(s));
//				}
			}
		}
		
//		System.out.println((notCoveredSource / notCovered) + "\t" + (notCoveredTarget / notCovered) + "\t" + (notCoveredMiddle / notCovered));
		nodeCoverage = waistNodeCoverage.size() * 1.0 / dependencyDAG.nodes.size();
		System.out.println("Node Coverage: " + nodeCoverage);
		effectiveNodeCoverage = waistNodeCoverage.size() * 1.0 / (dependencyDAG.nodes.size() - notCoveredSpecialSource - notCoveredSpecialTarget);
		System.out.println("Effective Node Coverage: " + effectiveNodeCoverage);
//		System.out.println("Total Not Covered: " + waistNodeNotCoverage.size() + "\t but kounter: " + sum);

		
		double minST = Math.min(dependencyDAG.nSources, dependencyDAG.nTargets);
//		System.out.println("nonSTinWaist: " + (topKNodes.size() - STNodes));
//		System.out.println("MinST " + minST + " Waist Size " + averageWaistRank.size());
//		if (waistSize == 0) {
//			hourglassness = -1001;
//		}
//		else if ((waistSize - 1.0) >= minST) {
//			hourglassness = 0;
//		}
//		else 
		{ 
			hourglassness = 1.0 - ((waistSize - 1.0) / minST);
		}
		System.out.println("Hourglassness: " + hourglassness);
		
		return waistNodeCoverage.size() * 1.0 / dependencyDAG.nodes.size();
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

	public static void nodeCentralityWRTWaist(DependencyDAG dependencyDAG) {
		double uSet = dependencyDAG.nTargets; 
		double dSet = dependencyDAG.nSources;
		
		for (String s: dependencyDAG.nodes) {
			int servesWaist = 0, dependsOnWaist = 0;
			if (dependencyDAG.serves.containsKey(s) && dependencyDAG.depends.containsKey(s)) { // intermediate node
				if (!averageWaistRank.containsKey(s)) { // not a waist node
					dependencyDAG.loadRechablity(s); // find reachable nodes
					
					for (String r: averageWaistRank.keySet()) {
						if (dependencyDAG.dependentsReachable.get(s).contains(r)) { // serving some waist node
							++servesWaist;
						}
				
						if (dependencyDAG.serversReachable.get(s).contains(r)) { // depending on some waist node
							++dependsOnWaist;
						}
					}
			
					System.out.println(s + "\t" + (dependencyDAG.normalizedPathCentrality.get(s) / pathCoverageTau) + "\t" + servesWaist + "\t" + dependsOnWaist);
				}
			}
			
			if (servesWaist >= dependsOnWaist) {
				++dSet;
			}
			else {
				++uSet;
			}
		}
		
		System.out.println("Symmetry: " + ((uSet - dSet) / (uSet + dSet)));
	}
}
