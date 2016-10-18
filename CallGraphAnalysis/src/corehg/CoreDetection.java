package corehg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;

public class CoreDetection {
	public static HashSet<String> topRemovedWaistNodes = new HashSet();
	public static HashMap<String, Double> averageCoreRank;
	public static HashMap<String, Double> averagePathCovered;
	public static double pathCoverageTau = 0.9;
	
	public static double nodeCoverage;
	public static double hScore;
	public static double weightedCoreLocation = 0;

	public static boolean fullTraverse = false;
	
	public static HashMap<TreeSet<String>, Double> coreSet;
	
	public static HashMap<Integer, HashSet<HashSet<String>>> visitedCoreByDepth = new HashMap();
	public static double minCoreSize;
	public static double maxCoreCoverage;

	public static HashSet<String> coreNodeCoverage;
	public static HashSet<String> coreServerCoverage;
	public static HashSet<String> coreDependentCoverage;
	
	public static TreeSet<String> sampleCore;	
	public static HashMap<String, Double> coreWeights;
	public static HashMap<String, Double> representativeLocation; 
	
	private static double getMedianPESLocation(TreeSet<String> PENodes, DependencyDAG dependencyDAG) {
		double v[] = new double[PENodes.size()];
		int k = 0;
		for (String s: PENodes) {
			v[k++] = dependencyDAG.location.get(s);
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
			
//		System.out.println("WTF  " + tiedMaxPathCentralityNodes);
//		System.out.println(maxPathCovered + "\t" + totalPath);
//		/** Detect exact path equivalent nodes - 2 **/
		HashSet<String> alreadyInPES = new HashSet();
		int nTiedNodes = 0;
		HashMap<Integer, TreeSet<String>> pathEquivalentNodeSet2 = new HashMap();
		HashSet<String> forIteratingTiedMaxPathCentralityNodes = new HashSet(tiedMaxPathCentralityNodes);
		for (String s : tiedMaxPathCentralityNodes) {
			if (alreadyInPES.contains(s)) {
				continue;
			}
			
			topRemovedWaistNodes.add(s);
//			System.out.println("Blocking: " + s);
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();

			TreeSet<String> PESet = new TreeSet();
			PESet.add(s);
			alreadyInPES.add(s);
			for (String r: forIteratingTiedMaxPathCentralityNodes) {
				if (alreadyInPES.contains(r)) {
					continue;
				}
//				if (dependencyDAG.numOfSourcePath.get(r) == 0 || dependencyDAG.numOfTargetPath.get(r) == 0) { // has been disconnected
				if (dependencyDAG.nodePathThrough.get(r) < 1.0) {// has been disconnected
					PESet.add(r);
					alreadyInPES.add(r);
				}
			}
//			System.out.println("Adding " + PESet + " at " + nTiedNodes);
			pathEquivalentNodeSet2.put(nTiedNodes++, new TreeSet(PESet));
			topRemovedWaistNodes.remove(s);
		}
		
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
			if (!FlattenNetwork.isProcessingFlat & true) {
				System.out.println(representative + "\t" + ((cumulativePathCovered + maxPathCovered) / totalPath));
			}
			
//			analysis
//			update average waist entry rank and path contribution
			if (averageCoreRank.containsKey(representative)) {
				double currentRank = averageCoreRank.get(representative);
				averageCoreRank.put(representative, (currentRank + nodeRank) * 0.5 );
				averagePathCovered.put(representative, (averagePathCovered.get(representative) + maxPathCovered) * 0.5);
			}
			else {
				averageCoreRank.put(representative, nodeRank * 1.0);
				averagePathCovered.put(representative, maxPathCovered);
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
	
	private static void init() {
		topRemovedWaistNodes = new HashSet();
		averageCoreRank = new HashMap();
		averagePathCovered = new HashMap();
		
		coreSet = new HashMap();
		
		visitedCoreByDepth = new HashMap();
		minCoreSize = 10e10;
		maxCoreCoverage = -1;

		coreNodeCoverage = new HashSet();
		coreServerCoverage =  new HashSet();
		coreDependentCoverage =  new HashSet();
		
		representativeLocation = new HashMap();
	}
	
	public static void getCore(DependencyDAG dependencyDAG, String filePath) {
		init();
		
//		Compute through paths for all nodes
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();
		
		traverseTreeHelper(dependencyDAG, 0, dependencyDAG.nTotalPath, 1, 1);
		
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
		sampleCore = coreSet.keySet().iterator().next();
		getNodeCoverage(dependencyDAG, sampleCore);
//		getNodeCoverage2(dependencyDAG, sampleCore);
		coreLocationAnalysis(dependencyDAG);
				
		if (!FlattenNetwork.isProcessingFlat & true) {
			System.out.println("Sample Core: " + sampleCore);
			System.out.println("Number of coreSet: " + optimalCoreCount);
			System.out.println("Min core size: " + minCoreSize);
			System.out.println("Node Coverage: " + nodeCoverage);
			System.out.println("WeightedCoreLocation: " + weightedCoreLocation);
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
//		double coveredSource = 0;
//		double coveredTarget = 0;
//		double coveredIntermediate = 0;
		for (String s: dependencyDAG.nodes) {
			dependencyDAG.checkReach(s);
			// check if a node is in a source/target path
			if (dependencyDAG.canReachSource && dependencyDAG.canReachTarget) {
				++denominator;
				if (coreNodeCoverage.contains(s)) {
					++numerator;
//					if (dependencyDAG.isIntermediate(s)) ++coveredIntermediate;
//					if (dependencyDAG.isSource(s)) ++coveredSource;
//					if (dependencyDAG.isTarget(s)) ++coveredTarget;
				}
			}
		}
		nodeCoverage = numerator / denominator;
//		System.out.println(nodeCoverage + "\t" + numerator + "\t" + denominator);
//		System.out.println(coveredT + "\t" + coveredI + "\t" + coveredS);
	}
	
	private static void coreLocationAnalysis(DependencyDAG dependencyDAG) {
		coreWeights = new HashMap();
		weightedCoreLocation = 0;
		double corePathContribution = 0;
		for (String s: sampleCore) {
//			weightedCoreLocation += dependencyDAG.location.get(s) * averagePathCovered.get(s);
			weightedCoreLocation += representativeLocation.get(s) * averagePathCovered.get(s);
			corePathContribution += averagePathCovered.get(s);
//			System.out.println(s + " -- " + representativeLocation.get(s) + " -- " + averagePathCovered.get(s));
		}
//		System.out.println();
		
		weightedCoreLocation /= corePathContribution;
//		System.out.println(weightedCoreLocation);
		
		for (String s: sampleCore) {
			double loc = representativeLocation.get(s);
			double weight = averagePathCovered.get(s) / corePathContribution;
//			System.out.println(loc + "\t" + weight);
			coreWeights.put(s, weight);
			
			for (double i = 0; i < weight * 100; ++i) {
//				System.out.println(loc);
			}
		}
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
