package Final;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;

import org.apache.commons.math3.stat.correlation.Covariance;

public class WaistDetection {
	static HashSet<String> topRemovedWaistNodes = new HashSet();
	static HashMap<String, Double> averageWaistRank;
	static HashMap<String, Double> averagePathCovered;
 	static double pathCoverageTau = 0.94;
	
	static double nodeCoverage;
	static double effectiveNodeCoverage;
	static double hourglassness;
	static double hourglassSymmetry;
	static double waistSize;
	static boolean thresholdSatisfied;
	
	static ArrayList<Double> ws = new ArrayList();
	static ArrayList<Double> pc = new ArrayList();
	
	static ArrayList<HashSet<String>> waistSets = new ArrayList();
	static double minWaistSize;
	static int randomPerturbation = 1;
	static HashSet<String> uniqueWaistNodes = new HashSet();
	
	static double weightedCoreLocation = 0;
	static boolean printInfo = false;
	
	static HashSet<TreeSet<String>> coreSet;
	
	static double currentLeaf;

	public static void traverseTieTreeHelper(DependencyDAG dependencyDAG, double cumulativePathCovered, double totalPath, double nodeRank, double nLeaves) {
		if (!(cumulativePathCovered < (totalPath * pathCoverageTau))) {
//			print current core
//			System.out.println("Core: " + topRemovedWaistNodes);
			coreSet.add(new TreeSet(topRemovedWaistNodes));			
			System.out.println((++currentLeaf) /*+ "\t" + nLeaves + "\t" + topRemovedWaistNodes.size()*/);
			return;
		}
		
//		recompute through paths for all nodes
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();

		double maxPathCovered = 0;
		String maxPathCoveredNode = "";
		int numOfTies = 0;
		HashSet<String> tiedMaxPathCentralityNodes = new HashSet();
		for (String s : dependencyDAG.nodes) {
//			find the node with largest through path
			double numPathCovered = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
			if (numPathCovered > maxPathCovered) {
				maxPathCovered = numPathCovered;
				maxPathCoveredNode = s;
				
				numOfTies = 0;
				tiedMaxPathCentralityNodes.clear();
				tiedMaxPathCentralityNodes.add(s);
			}
//			update ties
			else if (Math.abs(numPathCovered - maxPathCovered) < 0.0001) { // is a tie
				++numOfTies;
				tiedMaxPathCentralityNodes.add(s);
			}
		}
					
		HashMap<HashSet<String>, TreeSet<String>> pathEquivalentNodeSet = new HashMap();
//		/** Detect exact path equivalent nodes **/
		for (String s : tiedMaxPathCentralityNodes) {
			dependencyDAG.loadRechablity(s);
			HashSet<String> nodesReachable = dependencyDAG.nodesReachable.get(s);
			if (pathEquivalentNodeSet.containsKey(nodesReachable)) {
				pathEquivalentNodeSet.get(nodesReachable).add(s);
			}
			else {
				TreeSet<String> firstElement = new TreeSet();
				firstElement.add(s);
				pathEquivalentNodeSet.put(nodesReachable, firstElement);
			}
		}
		
//		for (HashSet<String> equivalanceKey: pathEquivalentNodeSet.keySet()) {
//			System.out.println("Rank " + nodeRank);
//			TreeSet<String> equivalentNodes = pathEquivalentNodeSet.get(equivalanceKey);
//			System.out.println(equivalentNodes);
//		}
		
		if (pathEquivalentNodeSet.size() > 1) {
//			System.out.println("Node rank: " + nodeRank + " branching for " + pathEquivalentNodeSet.size());
		}
		
		for (HashSet<String> equivalanceKey: pathEquivalentNodeSet.keySet()) {
//			System.out.println("Rank " + nodeRank);
			TreeSet<String> equivalentNodes = pathEquivalentNodeSet.get(equivalanceKey);
//			System.out.println(equivalentNodes);
			String representative = equivalentNodes.first();
//			if (equivalentNodes.size() > 1) representative += "+";
//			add to waist and remove from the network
			topRemovedWaistNodes.add(representative);
//			recurse
			traverseTieTreeHelper(dependencyDAG, cumulativePathCovered + maxPathCovered, totalPath, nodeRank + 1, nLeaves * pathEquivalentNodeSet.size());
//			remove from waist
			topRemovedWaistNodes.remove(representative);
			
//			break;
		}
	}
	
	public static void traverseTieTree(DependencyDAG dependencyDAG, String filePath) {
		topRemovedWaistNodes.clear();
		
//		compute through paths for all nodes
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();
		
		coreSet = new HashSet();
		System.out.println("Path Covrerge Threshold: " + pathCoverageTau);
		traverseTieTreeHelper(dependencyDAG, 0, dependencyDAG.nTotalPath, 1, 1);
		
//		double kount = 1;
		currentLeaf = 0;
		System.out.println("Number of coreSet: " + coreSet.size());
		for (TreeSet<String> hsS: coreSet) {
			System.out.println(hsS);
		}
	}
	
	public static void heuristicWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
//		PrintWriter pw0 = new PrintWriter(new File("analysis//path-cover-2-" + filePath + ".txt"));
		PrintWriter pw1 = new PrintWriter(new File("analysis//coverage-threshold-" + filePath + ".txt"));

		double cumulativePathsCovered = 0;
		double tPath = dependencyDAG.nTotalPath;
		int nodeRank = 0;
		
		HashSet<String> tiedNodeSet = new HashSet();
		HashSet<String> tiedMaxPathCentralityNodes = new HashSet();
		
//		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, "xxx");
//		System.out.println("Direct Tubes:" + DependencyDAG.nDirectSourceTargetEdges);
		
		while (true) {
			double maxPathCovered = 0;
			String maxPathCoveredNode = "";
			int numOfTies = 0;
			
			PriorityQueue<String> pq = new PriorityQueue(randomPerturbation, dependencyDAG.new PathCentralityComparator());
			
			for (String s : dependencyDAG.nodes) {
				/*
				skip if source or target
				if (dependencyDAG.isSource(s) || dependencyDAG.isTarget(s)) {
					continue;
				}
				*/

				pq.add(s);
				
//				find the node with largest through path
				double numPathCovered = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
				if (numPathCovered > maxPathCovered) {
					maxPathCovered = numPathCovered;
					maxPathCoveredNode = s;
					
					numOfTies = 0;
					tiedMaxPathCentralityNodes.clear();
					tiedMaxPathCentralityNodes.add(s);
				}
//				update ties
				else if (Math.abs(numPathCovered - maxPathCovered) < 0.0001) { // is a tie
					++numOfTies;
					tiedMaxPathCentralityNodes.add(s);
				}
			}
			
			/** Detect exact path equivalent nodes **/
			if (printInfo) {
				System.out.print("Tied nodes: ");
				for (String s : tiedMaxPathCentralityNodes) {
					System.out.print(s + "  ");
					dependencyDAG.loadRechablity(s);
				}
				System.out.println();

				for (String s : tiedMaxPathCentralityNodes) {
					for (String r : tiedMaxPathCentralityNodes) {
						if (!s.equals(r)) {
							if (dependencyDAG.nodesReachable.get(s).equals(dependencyDAG.nodesReachable.get(r))) {
								System.out.println("Path equivalent: " + s + " & " + r);
							}
						}
					}
				}
			}
			/** --- --- --- **/
			
//			not required anymore after considering source/targets for core
			if (DependencyDAG.isSynthetic == true) {
				if (maxPathCovered == 0) {
					break;
				}
			}
			
			//System.out.println("Max centrality node: " + maxPathNode + " with unique paths: " + maxPathThrough);
			//System.out.println("Source path: " + dependencyDAG.numOfSourcePath.get(maxPathNode) + " Target path: " + dependencyDAG.numOfTargetPath.get(maxPathNode));
			

			++nodeRank;
			if (numOfTies > 0) {
//				print tie information
//				System.out.println("Node Rank: " + nodeRank + " Tied for maximum: " + tiedMaxPathCentralityNodes.size());
				
//				randomly pick one of the largest centrality nodes
				maxPathCoveredNode = new ArrayList<String>(tiedMaxPathCentralityNodes).get(new Random(System.nanoTime()).nextInt(tiedMaxPathCentralityNodes.size()));
				//System.out.println("Randomly chosen node: " + maxPathNode);	
				//tiedNodeSet.addAll(tempTiedNodeSet);
			}
	
//			/*****random top-k *****/
			/*
			maxPathNode = (String)pq.toArray()[new Random(System.nanoTime()).nextInt(randomPerturbation)];
			maxPathThrough = dependencyDAG.numOfSourcePath.get(maxPathNode) * dependencyDAG.numOfTargetPath.get(maxPathNode);
			*/
			
//			record the largest through path node
			cumulativePathsCovered += maxPathCovered;			
//			System.out.println("Cumulative Coverage: " + cumulativePathsCovered / tPath * 100.0);
			//System.out.println(maxPathNode + "\t" + maxPathThrough + "\t" + cumulativePathsTraversed);

//			System.out.println("Node Rank: " + nodeRank + " Tied for maximum: " + tiedMaxPathCentralityNodes.size() + " Cumulative Coverage: " + (cumulativePathsCovered  / tPath));

//			add to waist
			topRemovedWaistNodes.add(maxPathCoveredNode);
			
//			update average waist entry rank and path contribution
			if (averageWaistRank.containsKey(maxPathCoveredNode)) {
				double currentRank = averageWaistRank.get(maxPathCoveredNode);
				averageWaistRank.put(maxPathCoveredNode, (currentRank + nodeRank) * 0.5 );
				averagePathCovered.put(maxPathCoveredNode, (averagePathCovered.get(maxPathCoveredNode) + maxPathCovered) * 0.5);
			}
			else {
				averageWaistRank.put(maxPathCoveredNode, nodeRank * 1.0);
				averagePathCovered.put(maxPathCoveredNode, maxPathCovered);
			}

//			if all paths have been traversed (except direct s-t edges), then break out
			//if (cumulativePathsTraversed >= ((tPath - dependencyDAG.nDirectSourceTargetEdges) * pathCoverageTau)) {
			//	break;
			//}
			
			pw1.println(topRemovedWaistNodes.size() + "\t" + (cumulativePathsCovered / tPath));
			ws.add(topRemovedWaistNodes.size() * 1.0);
			pc.add(cumulativePathsCovered / tPath);
			
			if (cumulativePathsCovered >= (tPath * pathCoverageTau)) {
				thresholdSatisfied = true;
				break;
			}
			
			//if (topRemovedWaistNodes.size() >= (dependencyDAG.nodes.size() - dependencyDAG.nSources - dependencyDAG.nTargets)) {
			//	break;
			//}

//			remove the largest through path node, recompute through paths for all remaining nodes
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
		}
		
		//System.out.println("Waist size: " + topRemovedWaistNodes.size() + " with nodes: " + topRemovedWaistNodes);
		
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
		averagePathCovered = new HashMap();
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
			if (!(distance < maxDistance)) {
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
		pathCoverageTau = tau;
		System.out.println("Core-size: " + minWS + " Path-coverage-threshold: " + tau);
//		System.out.println("Min-disatnce-line distance: " +  maxDistance + " intersected at: " + crossX + "," + crossY);
		
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
		printInfo = true;
		
		averagePathCovered = new HashMap();
//		ws = new ArrayList();
//		pc = new ArrayList();
//		
		
		uniqueWaistNodes.clear();
		ws.clear();
		pc.clear();
		waistSets.clear();
		
		int nRuns = 1;
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
		
//		System.out.println("Unique Waist Size: " + uniqueWaistNodes.size());
//		for (String s: uniqueWaistNodes) {
//			System.out.println(s + "\t" + dependencyDAG.location.get(s));
//		}

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
			System.out.println(n + "\t" + (nodeFrequencyInWaist.get(n) * 1.0 / nRuns) + "\t" + averageWaistRank.get(n) /*+ "\t" + dependencyDAG.normalizedPathCentrality.get(n)*/);
		}
		
//		System.out.println("Waist Size: " + waistSize);
		if (waistSize > 0) {
			getNodeCoverage(dependencyDAG);
		}
//		nodeCentralityWRTWaist(dependencyDAG);
		
		coreLocationAnalysis(dependencyDAG);
	}

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
//			System.out.println(s + "\t" + averageWaistRank.get(s) + "\t" + dependencyDAG.location.get(s));
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
//		System.out.println("Node Coverage: " + nodeCoverage);
//		effectiveNodeCoverage = waistNodeCoverage.size() * 1.0 / (dependencyDAG.nodes.size() - notCoveredSpecialSource - notCoveredSpecialTarget);
//		System.out.println("Effective Node Coverage: " + effectiveNodeCoverage);
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
//		System.out.println("Hourglassness: " + hourglassness);
		
		return waistNodeCoverage.size() * 1.0 / dependencyDAG.nodes.size();
	}
	
//	public static void nodeCentralityWRTWaist(DependencyDAG dependencyDAG) {
//		double uSet = dependencyDAG.nTargets; 
//		double dSet = dependencyDAG.nSources;
//		
//		for (String s: dependencyDAG.nodes) {
//			int servesWaist = 0, dependsOnWaist = 0;
//			if (dependencyDAG.serves.containsKey(s) && dependencyDAG.depends.containsKey(s)) { // intermediate node
//				if (!averageWaistRank.containsKey(s)) { // not a waist node
//					dependencyDAG.loadRechablity(s); // find reachable nodes
//					
//					for (String r: averageWaistRank.keySet()) {
//						if (dependencyDAG.dependentsReachable.get(s).contains(r)) { // serving some waist node
//							++servesWaist;
//						}
//				
//						if (dependencyDAG.serversReachable.get(s).contains(r)) { // depending on some waist node
//							++dependsOnWaist;
//						}
//					}
//			
//					System.out.println(s + "\t" + (dependencyDAG.normalizedPathCentrality.get(s) / pathCoverageTau) + "\t" + servesWaist + "\t" + dependsOnWaist);
//				}
//			}
//			
//			if (servesWaist >= dependsOnWaist) {
//				++dSet;
//			}
//			else {
//				++uSet;
//			}
//		}
//		
//		System.out.println("Symmetry: " + ((uSet - dSet) / (uSet + dSet)));
//	}
	
	public static void coreLocationAnalysis(DependencyDAG dependencyDAG) {
//		double coreLocations[] = new double[averageWaistRank.size()];	
//		int index = 0;
//		int histogramBins[] = new int[11];
		weightedCoreLocation = 0;
		double corePathContribution = 0;
		for (String s: averageWaistRank.keySet()) {
//			double loc = dependencyDAG.location.get(s);
//			coreLocations[index++] = loc;
//			int binIndex = (int)(loc * 10);
//			histogramBins[binIndex]++;
//			System.out.println(averageWaistRank.get(s) + "\t" + loc);
			
			weightedCoreLocation += dependencyDAG.location.get(s) * averagePathCovered.get(s);
			corePathContribution += averagePathCovered.get(s);
		}
		
		weightedCoreLocation /= corePathContribution;
//		System.out.println("WeightedCoreLocation: " + weightedCoreLocation);
		
//		for (int i = 0; i < 11; ++i) {
//			System.out.println("Bin-" + i + " Frequency " + histogramBins[i]);
//		}
//		System.out.println(StatUtils.percentile(coreLocations, 10) + "\t" + StatUtils.percentile(coreLocations, 50) + "\t" + StatUtils.percentile(coreLocations, 90));
		
		for (String s: averageWaistRank.keySet()) {
			double loc = dependencyDAG.location.get(s);
			double weight = averagePathCovered.get(s) / corePathContribution;
//			System.out.println(loc + "\t" + weight);
		}
	}
	
}
