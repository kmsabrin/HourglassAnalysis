package corehg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import neuro.ManagerNeuro;

import org.apache.commons.math3.stat.StatUtils;

public class CoreDetection {
	public static boolean viewCore = true;
	public static boolean viewStat = false;
	public static HashSet<String> topRemovedWaistNodes = new HashSet();
	public static HashMap<String, Double> averageCoreRank;
	public static HashMap<String, Double> averagePathCovered;
	public static double pathCoverageTau = 0.9;
	
	public static double nodeCoverage;
	public static double hScore;
	public static double weightedCoreLocation = 0;

	public static boolean fullTraverse = false;
	public static boolean inCore = false;
	
	public static HashMap<TreeSet<String>, Double> coreSet;
	
	public static HashMap<Integer, HashSet<HashSet<String>>> visitedCoreByDepth = new HashMap();
	public static double minCoreSize;
	public static double maxCoreCoverage;

	public static HashSet<String> coreNodeCoverage;
	public static HashSet<String> coreServerCoverage;
	public static HashSet<String> coreDependentCoverage;
	
	public static TreeSet<String> sampleCores; // both real and flat
	public static TreeSet<String> realCores; // only real
	public static HashMap<String, Double> coreWeights;
	public static HashMap<String, Double> representativeLocation; 
	
	private static double getMedianPESLocation(TreeSet<String> PENodes, DependencyDAG dependencyDAG) {
		double v[] = new double[PENodes.size()];
		int k = 0;
		for (String s: PENodes) {
//			v[k++] = dependencyDAG.lengthPathLocation.get(s);
			v[k++] = dependencyDAG.numPathLocation.get(s); // be careful!
		}
		return StatUtils.percentile(v, 50);
	}
	
	private static void traverseTreeHelper(DependencyDAG dependencyDAG, double cumulativePathCovered, double totalPath, int nodeRank, double nLeaves) {
//		check/mark visited state
		if (nodeRank > 0) {
			if (visitedCoreByDepth.containsKey(nodeRank)) {
				if (visitedCoreByDepth.get(nodeRank).contains(topRemovedWaistNodes)) {
					// visited before
					return;
				}
				else {
					visitedCoreByDepth.get(nodeRank).add(new HashSet(topRemovedWaistNodes));
				}
			}
			else {
				HashSet<HashSet<String>> markedCores = new HashSet();
				markedCores.add(new HashSet(topRemovedWaistNodes));
				visitedCoreByDepth.put(nodeRank, markedCores);
			}
		}
		
		if (!(cumulativePathCovered < (totalPath * pathCoverageTau))) {
//			System.out.println("HERE " + "\t" + cumulativePathCovered + "\t" + totalPath + "\t" + pathCoverageTau);
//			print current core
//			System.out.println("Core: " + topRemovedWaistNodes);
			coreSet.put(new TreeSet(topRemovedWaistNodes), cumulativePathCovered);			
//			System.out.println((++currentLeaf) + "\t" + nLeaves + "\t" + topRemovedWaistNodes.size());
			if (topRemovedWaistNodes.size() < minCoreSize) {
				minCoreSize = topRemovedWaistNodes.size();
				maxCoreCoverage = cumulativePathCovered;
			}
			else  if (Math.abs(topRemovedWaistNodes.size() - minCoreSize) < 0.0001) {
				if (cumulativePathCovered > maxCoreCoverage) {
					maxCoreCoverage = cumulativePathCovered;
				}
			}
			return;
		}
		
		
//		recompute through paths for all nodes
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();

		double maxPathCovered = -1;
		String maxPathCoveredNode = "";
		int numOfTies = 0;
		HashSet<String> tiedMaxPathCentralityNodes = new HashSet();
		for (String s : dependencyDAG.nodes) {
//			find the node with largest through path
			
			/* cancer data - special case */
			if (s.equals("miR429") || s.equals("dummy-in") || s.equals("dummy-out")) continue;
			
			/* celegans - special case */
			if (DependencyDAG.isCelegans) {
				if (s.equals("1000") || s.equals("2000")) {
					continue;
				}
			}
			
//			double numPathCovered = 0;
//			if (dependencyDAG.numOfSourcePath.containsKey(s) && dependencyDAG.numOfTargetPath.containsKey(s)) {
//				numPathCovered = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
//			}
			
			double numPathCovered = dependencyDAG.nodePathThrough.get(s);
			
			if (numPathCovered > maxPathCovered) {
				maxPathCovered = numPathCovered;
				maxPathCoveredNode = s;
				
				numOfTies = 0;
				tiedMaxPathCentralityNodes.clear();
				tiedMaxPathCentralityNodes.add(s);
			}
//			update tied nodes list
			else if (Math.abs(numPathCovered - maxPathCovered) < 0.001) { // is a tie
				++numOfTies;
				tiedMaxPathCentralityNodes.add(s);
			}
		}
			
//		System.out.println("WTF-1  " + tiedMaxPathCentralityNodes);
//		System.out.println("WTF-2  " + topRemovedWaistNodes);
//		System.out.println("Covering: " + maxPathCovered + "\t" + cumulativePathCovered + "\t" + totalPath);
//		/** Detect exact path equivalent nodes - 2 **/
		HashSet<String> alreadyInPES = new HashSet();
		int nTiedNodes = 0;
		HashMap<Integer, TreeSet<String>> pathEquivalentNodeSet2 = new HashMap();
		HashSet<String> forIteratingTiedMaxPathCentralityNodes = new HashSet(tiedMaxPathCentralityNodes);
		for (String s : tiedMaxPathCentralityNodes) {
			/*
			if (alreadyInPES.contains(s)) {
				continue;
			}
			
			topRemovedWaistNodes.add(s);
//			System.out.println("Blocking: " + s);
//			dependencyDAG.numOfTargetPath.clear();
//			dependencyDAG.numOfSourcePath.clear();
//			dependencyDAG.loadPathStatistics();
						
			TreeSet<String> PESet = new TreeSet();
			PESet.add(s);
			alreadyInPES.add(s);

			for (String r: forIteratingTiedMaxPathCentralityNodes) {
				if (alreadyInPES.contains(r)) {
					continue;
				}
//				if (dependencyDAG.numOfSourcePath.get(r) == 0 || dependencyDAG.numOfTargetPath.get(r) == 0) { // has been disconnected
				
				dependencyDAG.checkReach(r);
				if (!dependencyDAG.canReachSource && !dependencyDAG.canReachTarget) {
//				if (dependencyDAG.nodePathThrough.get(r) < 1.0) {// has been disconnected
					PESet.add(r);
					alreadyInPES.add(r);
				}
			}
			topRemovedWaistNodes.remove(s);
//			System.out.println("Adding " + PESet + " at " + nTiedNodes);
			pathEquivalentNodeSet2.put(nTiedNodes++, new TreeSet(PESet));
			*/
		}
		
		// why?
		pathEquivalentNodeSet2.put(nTiedNodes++, new TreeSet(tiedMaxPathCentralityNodes));
		
//		System.out.println("Rank " + nodeRank); 
//		for (HashSet<String> equivalanceKey: pathEquivalentNodeSet.keySet()) {
//		for (int equivalanceKey: pathEquivalentNodeSet2.keySet()) {
//			TreeSet<String> equivalentNodes = pathEquivalentNodeSet2.get(equivalanceKey);
//			System.out.print(" " + equivalentNodes);
//		}
//		System.out.println();
//		if (pathEquivalentNodeSet2.size() > 1) {
//			System.out.println("Node rank: " + nodeRank + " branching for " + pathEquivalentNodeSet2.size());
//		}
		
		for (int equivalenceKey: pathEquivalentNodeSet2.keySet()) {
//			System.out.print("Rank " + nodeRank);
//			randomize retrieval
//			int randomKey = new Random(System.nanoTime()).nextInt(nTiedNodes);
//			System.out.println(nTiedNodes + "\t" + randomKey + "\t" + equivalenceKey);
			int randomKey = equivalenceKey;
			
			TreeSet<String> equivalentNodes = pathEquivalentNodeSet2.get(randomKey);
//			System.out.println(equivalentNodes + "\t" + maxPathCovered);
			String representative = equivalentNodes.first();
//			System.out.println("Chosen: " + representative 
//					+ " centrality: " + dependencyDAG.nodePathThrough.get(representative)
//					+ " location: " + dependencyDAG.location.get(representative));
//			if (equivalentNodes.size() > 1) representative += "+";
//			add to waist and remove from the network
			topRemovedWaistNodes.add(representative);
			representativeLocation.put(representative, getMedianPESLocation(equivalentNodes, dependencyDAG));
			if (!FlatNetwork.isProcessingFlat & viewCore) {
				System.out.println(representative + "\t" + ((cumulativePathCovered + maxPathCovered) / totalPath) + "\t" + dependencyDAG.numPathLocation.get(representative));
//				System.out.println((nodeRank + "\t" + (cumulativePathCovered + maxPathCovered) / totalPath));
			}
			
			if (!FlatNetwork.isProcessingFlat) {
//				analysis
//				update average waist entry rank and path contribution
//				for full traversal the average will make sensem, for single traverse it's the single rank			
				if (averageCoreRank.containsKey(representative)) {
					double currentRank = averageCoreRank.get(representative);
					averageCoreRank.put(representative, (currentRank + nodeRank) * 0.5 );
					averagePathCovered.put(representative, (averagePathCovered.get(representative) + maxPathCovered) * 0.5);
				}
				else {
					averageCoreRank.put(representative, nodeRank * 1.0);
					averagePathCovered.put(representative, maxPathCovered);
				}
			}
			
//			recurse
			traverseTreeHelper(dependencyDAG, cumulativePathCovered + maxPathCovered, totalPath, nodeRank + 1, nLeaves * pathEquivalentNodeSet2.size());

//			remove from waist
			topRemovedWaistNodes.remove(representative);

//			conditional break for full recursive traversal.
			if (fullTraverse == false || DependencyDAG.isSynthetic == true) {
				break;
			}
//			break;
		}
	}
	
	public static void init() {
		topRemovedWaistNodes = new HashSet();
		
		coreSet = new HashMap();
		
		visitedCoreByDepth = new HashMap();
		minCoreSize = 10e10;
		maxCoreCoverage = -1;
		
		representativeLocation = new HashMap();
		sampleCores = new TreeSet();
		
		fullTraverse = false;
		
		if (!FlatNetwork.isProcessingFlat) {
			realCores = new TreeSet();
			averageCoreRank = new HashMap();
			averagePathCovered = new HashMap();
			coreNodeCoverage = new HashSet();
			coreServerCoverage =  new HashSet();
			coreDependentCoverage =  new HashSet();
			coreWeights = new HashMap();
		}
	}
	
	public static void getCore(DependencyDAG dependencyDAG, String filePath) {
		init();
		
//		if (true) {
//			getCore2(dependencyDAG);
//			return;
//		}
		
//		Compute through paths for all nodes
		inCore = true;
//		dependencyDAG.numOfTargetPath.clear();
//		dependencyDAG.numOfSourcePath.clear();
//		dependencyDAG.loadPathStatistics();

		/* for neuro case start */
//		int disconnectedInterNeurons[] = {4,5,7,9,19,20,21,22,34,36,44,53,85,93,125,272};
//		TreeSet<String> blockedNodeSet = new TreeSet();
//		for (int i: disconnectedInterNeurons) {
//			blockedNodeSet.add(Integer.toString(i));
//		}
//		topRemovedWaistNodes.addAll(blockedNodeSet);
		/* for neuro case end */

		traverseTreeHelper(dependencyDAG, 0, dependencyDAG.nTotalPath, 1, 1);
		
		topRemovedWaistNodes.clear();
//		System.out.println(coreSet.size() + "\t" + coreSet);
//		Remove sub-optimal cores 
//		Keep the core with min size first and then max coverage
		int optimalCoreCount = 0;
		HashSet<TreeSet<String>> suboptimalCores = new HashSet();
		for (TreeSet<String> hsS: coreSet.keySet()) {
			if (Math.abs(hsS.size() - minCoreSize) < 0.001 && Math.abs(coreSet.get(hsS) - maxCoreCoverage) < 0.001) {
				++optimalCoreCount;
			}
			else {
				suboptimalCores.add(hsS);
			}
		}
		coreSet.remove(suboptimalCores);
		
//		Analysis: find (average) Jaccard distance among valid cores		
		if (coreSet.size() > 1) {
			int sz = coreSet.size();
			double distances[] = new double[(sz * (sz - 1)) / 2];
			ArrayList<TreeSet<String>> traversalList = new ArrayList(coreSet.keySet());
			int idx = 0;
			for (int i = 0; i < traversalList.size(); ++i) {
				for (int j = i + 1; j < traversalList.size(); ++j) {
					distances[idx++] = jaccardDistance(traversalList.get(i), traversalList.get(j));
				}
			}	
//			System.out.println("Mean: " + StatUtils.mean(distances) + " St D: " + Math.sqrt(StatUtils.variance(distances)));
		}
		
//		Use the first core as a sample
		sampleCores = coreSet.keySet().iterator().next();
		if (!FlatNetwork.isProcessingFlat) {
//			System.out.println("Am i even here?");
			realCores = new TreeSet(sampleCores);
			getNodeCoverage(dependencyDAG, sampleCores);
//			getNodeCoverage2(dependencyDAG, sampleCore);
			coreLocationAnalysis(dependencyDAG);
//			getCoreNeighborhood(dependencyDAG, sampleCore);
		}
		
//		sampleCore.removeAll(blockedNodeSet);
//		System.out.println(sampleCore);


//		System.out.println("Sample Core: " + sampleCores);
		if (!FlatNetwork.isProcessingFlat & viewStat) {
			System.out.println("Sample Core: " + sampleCores);
			if (DependencyDAG.isCelegans) {
				for (String s: sampleCores) {
					System.out.println(s + "\t" + ManagerNeuro.getType(s) + "\t" + dependencyDAG.getNodeType(s));
				}
			}
			System.out.println("Number of coreSet: " + optimalCoreCount);
			System.out.println("Min core size: " + minCoreSize);
			System.out.println("Node Coverage: " + nodeCoverage);
			System.out.println("WeightedCoreLocation: " + weightedCoreLocation);
		}
		
		inCore = false;
	}
	
	private static void getCoreNeighborhood(DependencyDAG dependencyDAG, TreeSet<String> sampleCore) {
		HashSet<String> neighbors = new HashSet();
		for (String s: sampleCore) {
			if (dependencyDAG.depends.containsKey(s)) {
				for (String r: dependencyDAG.depends.get(s)) {
					System.out.println(r + "\t" + s);
					neighbors.add(r);
				}
			}
			
			if (dependencyDAG.serves.containsKey(s)) {
				for (String r: dependencyDAG.ancestors.get(s)) {
					System.out.println(s + "\t" + r);
					neighbors.add(r);
				}
			}
		}
		
		System.out.println(neighbors.size());
	}
	
	public static void getCentralEdgeSubgraph(DependencyDAG dependencyDAG) {
		/*
		Collections.sort(dependencyDAG.edgePathCentrality);
		double startingTotalPath = dependencyDAG.nTotalPath;
		System.out.println(dependencyDAG.nTotalPath);
		ArrayList<Edge> tempEdges = new ArrayList(dependencyDAG.edgePathCentrality);		
		for (Edge e: tempEdges) {
			System.out.print(e.source + "\t" + e.target + "\t" + (e.pathCentrality / startingTotalPath));
			dependencyDAG.removeEdge(e.source, e.target);
			dependencyDAG.loadPathStatistics();
			System.out.println("\t" + dependencyDAG.nTotalPath);
			double pathCover = startingTotalPath - dependencyDAG.nTotalPath;
			if (!(pathCover < Math.floor(startingTotalPath * pathCoverageTau))) {
				break;
			}
		}
		*/
	}
	
	public static void getCore2(DependencyDAG dependencyDAG) {
		TreeMap<Double, ArrayList<String>> pathCentralitySortedNodes = new TreeMap();
		
		for (String s: dependencyDAG.nodes) {
			double pathCentrality = -1 *  dependencyDAG.nodePathThrough.get(s);
			if (pathCentralitySortedNodes.containsKey(pathCentrality)) {
				pathCentralitySortedNodes.get(pathCentrality).add(s);
			}
			else {
				ArrayList<String> stringList = new ArrayList();
				stringList.add(s);
				pathCentralitySortedNodes.put(pathCentrality, stringList);
			}
		}
		
		ArrayList<String> cores = new ArrayList();
		double startingTotalPath = dependencyDAG.nTotalPath;
		int rank = 1;
		for (double d: pathCentralitySortedNodes.keySet()) {
			cores.addAll(pathCentralitySortedNodes.get(d));
			topRemovedWaistNodes.addAll(pathCentralitySortedNodes.get(d));
//			System.out.println("Adding " + pathCentralitySortedNodes.get(d));
			if (pathCentralitySortedNodes.get(d).size() > 1) {
//				System.out.println("Tied: " + pathCentralitySortedNodes.get(d).size() + " at " + rank);
			}
			dependencyDAG.loadPathStatistics();
			double pathCover = startingTotalPath - dependencyDAG.nTotalPath;
//			System.out.println("numRemainingPath " + dependencyDAG.nTotalPath + " Cumulative cover " + pathCover);
			if (!(pathCover < Math.floor(startingTotalPath * pathCoverageTau))) {
				break;
			}
			
			++rank;
		}
		
		sampleCores = new TreeSet(cores);
		CoreDetection.minCoreSize = sampleCores.size();
		
		topRemovedWaistNodes.clear();
		dependencyDAG.loadPathStatistics();
		getNodeCoverage(dependencyDAG, sampleCores);
		coreLocationAnalysis2(dependencyDAG);
				
		if (!FlatNetwork.isProcessingFlat && true) {
			System.out.println("Sample Core: " + sampleCores);
			System.out.println("Min core size: " + minCoreSize);
			System.out.println("Node Coverage: " + nodeCoverage);
			System.out.println("WeightedCoreLocation: " + weightedCoreLocation); // does not apply
		}
	}
	
	private static void getNodeCoverage2(DependencyDAG dependencyDAG, TreeSet<String> sampleCore) {
		coreNodeCoverage = new HashSet();
		coreServerCoverage = new HashSet();
		coreDependentCoverage = new HashSet();
		for (String s : sampleCore) {
			dependencyDAG.visited.clear();
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
			coreNodeCoverage.addAll(dependencyDAG.visited);
			coreDependentCoverage.addAll(dependencyDAG.visited);
			
			dependencyDAG.visited.clear();
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes are using her
			coreNodeCoverage.addAll(dependencyDAG.visited);
			coreServerCoverage.addAll(dependencyDAG.visited);
		}
		
		/****************/
		HashSet<String> toRemove = new HashSet();
		for (String s: coreDependentCoverage) {
			dependencyDAG.checkReach(s);
			if (!dependencyDAG.canReachSource || !dependencyDAG.canReachTarget) {
				toRemove.add(s);
			}
		}
		coreDependentCoverage.removeAll(toRemove);
		
		toRemove.clear();
		for (String s: coreServerCoverage) {
			dependencyDAG.checkReach(s);
			if (!dependencyDAG.canReachSource || !dependencyDAG.canReachTarget) {
				toRemove.add(s);
			}
		}
		coreServerCoverage.removeAll(toRemove);
		/******************/
		
//		coreNodeCoverage.clear();
//		coreNodeCoverage.addAll(coreDependentCoverage);
//		coreNodeCoverage.addAll(coreServerCoverage);
		nodeCoverage = coreNodeCoverage.size() * 1.0 / dependencyDAG.nodes.size();
		
//		System.out.println(nodeCoverage + "\t" + coreNodeCoverage.size() + "\t" +  dependencyDAG.nodes.size());
	}
	
	private static void getNodeCoverage(DependencyDAG dependencyDAG, TreeSet<String> sampleCore) {
		coreNodeCoverage = new HashSet();
		for (String s : sampleCore) {
			dependencyDAG.visited.clear();
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
			coreNodeCoverage.addAll(dependencyDAG.visited);
			dependencyDAG.visited.clear();
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes is used by her
			coreNodeCoverage.addAll(dependencyDAG.visited);
		}
	
		double numerator = 0;
		double denominator = 0;
		double coveredSource = 0;
		double coveredTarget = 0;
		double coveredIntermediate = 0;
		double vTarget = 0;
		double vIntermediate = 0;
		double vSource = 0;
		for (String s: dependencyDAG.nodes) {
			dependencyDAG.checkReach(s);
			// check if a node is in a source/target path
			if (dependencyDAG.canReachSource && dependencyDAG.canReachTarget) {
				++denominator;
				if (dependencyDAG.isIntermediate(s)) ++vIntermediate;
				if (dependencyDAG.isSource(s)) ++vSource;
				if (dependencyDAG.isTarget(s)) ++vTarget;
				if (coreNodeCoverage.contains(s)) {
					++numerator;
					if (dependencyDAG.isIntermediate(s)) ++coveredIntermediate;
					if (dependencyDAG.isSource(s)) ++coveredSource;
					if (dependencyDAG.isTarget(s)) ++coveredTarget;
				}
			}
			
//			if (coreNodeCoverage.contains(s)) {
//				if (dependencyDAG.isIntermediate(s)) ++coveredIntermediate;
//				if (dependencyDAG.isSource(s)) ++coveredSource;
//				if (dependencyDAG.isTarget(s)) ++coveredTarget;
//			}
		}
		nodeCoverage = numerator / denominator;
//		System.out.println("X: " + nodeCoverage + "\t" + numerator + "\t" + denominator);
//		System.out.println("Y: " + coveredTarget + "\t" + coveredIntermediate + "\t" + coveredSource);
//		double totalUncover = denominator - numerator;
//		System.out.println(((vTarget - coveredTarget)/totalUncover) 
//				+ "\t" + ((vIntermediate - coveredIntermediate)/totalUncover) 
//				+ "\t" + ((vSource - coveredSource)/totalUncover));
	}
	
	private static void coreLocationAnalysis(DependencyDAG dependencyDAG) {
		coreWeights = new HashMap();
		weightedCoreLocation = 0;
		double corePathContribution = 0;
		for (String s: sampleCores) {
//			System.out.println(s + " -- " + representativeLocation.get(s) + " -- " + averagePathCovered.get(s));
//			weightedCoreLocation += dependencyDAG.location.get(s) * averagePathCovered.get(s);
			weightedCoreLocation += representativeLocation.get(s) * averagePathCovered.get(s);
			corePathContribution += averagePathCovered.get(s);
		}
//		System.out.println();
		
		weightedCoreLocation /= corePathContribution;
//		System.out.println(weightedCoreLocation);
		
		for (String s: sampleCores) {
			double loc = representativeLocation.get(s);
			double weight = averagePathCovered.get(s) / corePathContribution;
//			System.out.println(loc + "\t" + weight);
			coreWeights.put(s, weight);
			
			for (double i = 0; i < weight * 100; ++i) {
//				System.out.println(loc);
			}
		}
	}
	
	private static void coreLocationAnalysis2(DependencyDAG dependencyDAG) {
		coreWeights = new HashMap();
		double coreSumCentrality = 0;
		for (String s: sampleCores) {
			coreSumCentrality += dependencyDAG.normalizedPathCentrality.get(s);
		}

		weightedCoreLocation = 0;
		for (String s: sampleCores) {
			double location = dependencyDAG.numPathLocation.get(s);
			double weight = dependencyDAG.normalizedPathCentrality.get(s) / coreSumCentrality;
			weightedCoreLocation += location * weight;
			coreWeights.put(s, weight);
		}
		
		weightedCoreLocation /= coreSumCentrality;
//		System.out.println(weightedCoreLocation);
	}
	
	private static boolean verifyCore(DependencyDAG dependencyDAG, TreeSet<String> testCore) {
		double requiredPathCover = dependencyDAG.nTotalPath * CoreDetection.pathCoverageTau;
		double cumulativePathCover = 0;
		init();
		for (String s: testCore) {
//			recompute through paths for all nodes
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			
			double numPathCovered = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
			cumulativePathCover += numPathCovered;
			topRemovedWaistNodes.add(s);
		}
		
//		System.out.println(requiredPathCover + "\t" + cumulativePathCover);
		return !(requiredPathCover > cumulativePathCover);
	}
	
	private static double jaccardDistance(Set<String> a, Set<String> b) {
		HashSet<String> union = new HashSet();
		union.addAll(a);
		union.addAll(b);
		HashSet<String> intersection = new HashSet(a);
		intersection.retainAll(b);
//		System.out.println(a + "\t" + b + "\t" + (intersection.size() * 1.0 / union.size()));
		return intersection.size() * 1.0 / union.size();
	}
}
