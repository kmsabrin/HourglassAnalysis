package Final;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;

public class CoreDetection {
	static HashSet<String> topRemovedWaistNodes = new HashSet();
	static HashMap<String, Double> averageCoreRank;
	static HashMap<String, Double> averagePathCovered;
 	static double pathCoverageTau = 0.90;
	
	static double nodeCoverage;
	static double hScore;
	static double weightedCoreLocation = 0;

	static boolean fullTraverse = false;
	static boolean thresholdSatisfied;
	static double effectiveNodeCoverage;
	static double hourglassSymmetry;
	static double waistSize;
	
	static ArrayList<Double> ws = new ArrayList();
	static ArrayList<Double> pc = new ArrayList();
	
	static ArrayList<HashSet<String>> waistSets = new ArrayList();
	static double minWaistSize;
	static int randomPerturbation = 1;
	static HashSet<String> uniqueWaistNodes = new HashSet();
	
	static boolean printInfo = false;
	
	static HashMap<TreeSet<String>, Double> coreSet;
	
	static double currentLeaf;
	
	static HashMap<Integer, HashSet<HashSet<String>>> visitedCoreByDepth = new HashMap();
	static double minCoreSize;
	static double maxCoreCoverage;

	static HashSet<String> coreNodeCoverage;
	static HashSet<String> coreServerCoverage;
	static HashSet<String> coreDependentCoverage;
	
	static double hScoreDenominator = 1;
	
	static TreeSet<String> sampleCore;
	
	static HashMap<String, Double> representativeLocation; 
	
	private static double getMedianPESLocation(TreeSet<String> PENodes, DependencyDAG dependencyDAG) {
//		System.out.println("USED");
		double v[] = new double[PENodes.size()];
		
		int k = 0;
		for (String s: PENodes) {
			v[k++] = dependencyDAG.location.get(s);
		}
		
		return StatUtils.percentile(v, 50);
	}
	
	public static void traverseTreeHelper(DependencyDAG dependencyDAG, double cumulativePathCovered, double totalPath, int nodeRank, double nLeaves) {
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

		double maxPathCovered = 0;
		String maxPathCoveredNode = "";
		int numOfTies = 0;
		HashSet<String> tiedMaxPathCentralityNodes = new HashSet();
		for (String s : dependencyDAG.nodes) {
//			find the node with largest through path
			double numPathCovered = dependencyDAG.numOfSourcePath.get(s) * dependencyDAG.numOfTargetPath.get(s);
//			System.out.print(s + "--" + numPathCovered + "\t");
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
//		System.out.println();
					
//		HashMap<HashSet<String>, TreeSet<String>> pathEquivalentNodeSet = new HashMap();
////		/** Detect exact path equivalent nodes - 1 **/
//		for (String s : tiedMaxPathCentralityNodes) {
//			dependencyDAG.loadRechablity(s);
//			HashSet<String> nodesReachable = dependencyDAG.nodesReachable.get(s);
//			if (pathEquivalentNodeSet.containsKey(nodesReachable)) {
//				pathEquivalentNodeSet.get(nodesReachable).add(s);
//			}
//			else {
//				TreeSet<String> firstElement = new TreeSet();
//				firstElement.add(s);
//				pathEquivalentNodeSet.put(nodesReachable, firstElement);
//			}
//		}
		
//		double meanLocation = 0;
//		for (String s: tiedMaxPathCentralityNodes) {
//			meanLocation += dependencyDAG.location.get(s);
//		}
//		meanLocation /= tiedMaxPathCentralityNodes.size();
		
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
				if (dependencyDAG.numOfSourcePath.get(r) == 0 || dependencyDAG.numOfTargetPath.get(r) == 0) { // has been disconnected
					PESet.add(r);
					alreadyInPES.add(r);
				}
			}
//			System.out.println("Adding " + PESet + " at " + nTiedNodes);
			pathEquivalentNodeSet2.put(nTiedNodes++, new TreeSet(PESet));
			topRemovedWaistNodes.remove(s);
		}
		
//		System.out.println("Rank " + nodeRank); 
////		for (HashSet<String> equivalanceKey: pathEquivalentNodeSet.keySet()) {
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
//			System.out.println("Chosen: " + representative);
//			if (equivalentNodes.size() > 1) representative += "+";
//			add to waist and remove from the network
			topRemovedWaistNodes.add(representative);
			representativeLocation.put(representative, getMedianPESLocation(equivalentNodes, dependencyDAG));
			
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

			if (fullTraverse == false || dependencyDAG.isSynthetic == true) {
				break;
			}
			
//			break;
		}
	}
	
	private static void init() {
		topRemovedWaistNodes = new HashSet();
		averageCoreRank = new HashMap();
		averagePathCovered = new HashMap();
				
		ws = new ArrayList();
		pc = new ArrayList();
		
		waistSets = new ArrayList();
		uniqueWaistNodes = new HashSet();
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
		
//		System.out.println(topRemovedWaistNodes);
//		compute through paths for all nodes
		dependencyDAG.numOfTargetPath.clear();
		dependencyDAG.numOfSourcePath.clear();
		dependencyDAG.loadPathStatistics();
		
//		System.out.println("Path Covrerage Threshold: " + pathCoverageTau);
		traverseTreeHelper(dependencyDAG, 0, dependencyDAG.nTotalPath, 1, 1);
		
//		double kount = 1;
		currentLeaf = 0;
		int optimalCoreCount = 0;
//		System.out.println(coreSet.size());
//		System.out.println(minCoreSize + " --- " + maxCoreCoverage);
		HashSet<TreeSet<String>> suboptimalCores = new HashSet();
		for (TreeSet<String> hsS: coreSet.keySet()) {
//			System.out.println(hsS.size() + "\t" + coreSet.get(hsS));
			if (Math.abs(hsS.size() - minCoreSize) < 0.0001 && Math.abs(coreSet.get(hsS) - maxCoreCoverage) < 0.0001) { 
//				System.out.println(hsS.size() + "\t" + coreSet.get(hsS));
//				System.out.println(hsS);
				++optimalCoreCount;
			}
			else {
//				remove??
				suboptimalCores.add(hsS);
			}
		}
		coreSet.remove(suboptimalCores);
		
//		System.out.println(maxCoreCoverage + "\t" + minCoreSize + "\t" + optimalCoreCount);
		
		if (coreSet.size() > 1) {
			// find average jaccard distance between cores
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
		
//		double minST = Math.min(dependencyDAG.nSources, dependencyDAG.nTargets);
//		System.out.println(minST);
//		hScoreDenominator = minST;
//		hScore = 1.0 - ((minCoreSize - 1.0) / minST);
//		double hScore2 = 1.0 - ((minCoreSize - 1.0) / (1.0 * Math.min(coreDependentCoverage.size(), coreServerCoverage.size())));
//		hScore = hScore2;
//		hScoreDenominator = 1.0 * Math.min(coreDependentCoverage.size(), coreServerCoverage.size());

//		System.out.println(coreSet.size());
		sampleCore = coreSet.keySet().iterator().next();
		System.out.println("Sample Core: " + sampleCore);
		
		/* not needed because we are not utilizing the path statistics, only reachability and location*/
//		topRemovedWaistNodes.clear();
//		dependencyDAG.numOfTargetPath.clear();
//		dependencyDAG.numOfSourcePath.clear();
//		dependencyDAG.loadPathStatistics();
//		getNodeCoverage2(dependencyDAG, sampleCore);
		getNodeCoverage(dependencyDAG, sampleCore);
		coreLocationAnalysis(dependencyDAG);
				
		if (!FlattenNetwork.isProcessingFlat) {
//			System.out.println("Number of coreSet: " + optimalCoreCount);
//			System.out.println("Min core size: " + minCoreSize);
//			System.out.println("Node Coverage: " + nodeCoverage);
//			System.out.println("WeightedCoreLocation: " + weightedCoreLocation);
		}
	}
	
/*	public static void heuristicWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
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
				
				skip if source or target
				if (dependencyDAG.isSource(s) || dependencyDAG.isTarget(s)) {
					continue;
				}
				

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
			
//			 Detect exact path equivalent nodes
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
	
//			random top-k
			
			maxPathNode = (String)pq.toArray()[new Random(System.nanoTime()).nextInt(randomPerturbation)];
			maxPathThrough = dependencyDAG.numOfSourcePath.get(maxPathNode) * dependencyDAG.numOfTargetPath.get(maxPathNode);
			
			
//			record the largest through path node
			cumulativePathsCovered += maxPathCovered;			
//			System.out.println("Cumulative Coverage: " + cumulativePathsCovered / tPath * 100.0);
			//System.out.println(maxPathNode + "\t" + maxPathThrough + "\t" + cumulativePathsTraversed);

//			System.out.println("Node Rank: " + nodeRank + " Tied for maximum: " + tiedMaxPathCentralityNodes.size() + " Cumulative Coverage: " + (cumulativePathsCovered  / tPath));

//			add to waist
			topRemovedWaistNodes.add(maxPathCoveredNode);
			
//			update average waist entry rank and path contribution
			if (averageCoreRank.containsKey(maxPathCoveredNode)) {
				double currentRank = averageCoreRank.get(maxPathCoveredNode);
				averageCoreRank.put(maxPathCoveredNode, (currentRank + nodeRank) * 0.5 );
				averagePathCovered.put(maxPathCoveredNode, (averagePathCovered.get(maxPathCoveredNode) + maxPathCovered) * 0.5);
			}
			else {
				averageCoreRank.put(maxPathCoveredNode, nodeRank * 1.0);
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
*/	

/*	public static void pathCoverageThresholdDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		topRemovedWaistNodes.clear();
		averageCoreRank = new HashMap();
		averagePathCovered = new HashMap();
		ws = new ArrayList();
		pc = new ArrayList();
		pathCoverageTau = 1.0;

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
//		System.out.println("Core-size: " + minWS);
		System.out.println("Path-coverage-threshold: " + tau);
	}
*/	

/*	public static void randomizedWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		HashMap<String, Integer> nodeFrequencyInWaist = new HashMap();
		HashMap<Integer, Integer> waistSizeFrequencey = new HashMap();
		averageCoreRank = new HashMap();
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
		averageCoreRank = new HashMap();
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
			System.out.println(n + "\t" + (nodeFrequencyInWaist.get(n) * 1.0 / nRuns) + "\t" + averageCoreRank.get(n) + "\t" + dependencyDAG.normalizedPathCentrality.get(n));
		}
		
//		System.out.println("Waist Size: " + waistSize);
		if (waistSize > 0) {
//			getNodeCoverage(dependencyDAG);
		}
//		nodeCentralityWRTWaist(dependencyDAG);
		
		coreLocationAnalysis(dependencyDAG);
	}
*/
	
	public static void getNodeCoverage2(DependencyDAG dependencyDAG, TreeSet<String> sampleCore) {
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
		
		System.out.println(nodeCoverage + "\t" + coreNodeCoverage.size() + "\t" +  dependencyDAG.nodes.size());
	}
	
	public static void getNodeCoverage(DependencyDAG dependencyDAG, TreeSet<String> sampleCore) {
		coreNodeCoverage = new HashSet();
		for (String s : sampleCore) {
			System.out.println(dependencyDAG.isIntermediate(s));
			dependencyDAG.visited.clear();
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
			coreNodeCoverage.addAll(dependencyDAG.visited);
			dependencyDAG.visited.clear();
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes are using her
			coreNodeCoverage.addAll(dependencyDAG.visited);
		}
	
		double numerator = 0;
		double denominator = 0;
		double coveredS = 0;
		double coveredT = 0;
		double coveredI = 0;
		for (String s: dependencyDAG.nodes) {
			dependencyDAG.checkReach(s);
			if (dependencyDAG.canReachSource && dependencyDAG.canReachTarget) {
				++denominator;
				if (coreNodeCoverage.contains(s)) {
					++numerator;
					if (dependencyDAG.isIntermediate(s)) ++coveredI;
					if (dependencyDAG.isSource(s)) ++coveredS;
					if (dependencyDAG.isTarget(s)) ++coveredT;
				}
			}
		}
		
		nodeCoverage = numerator / denominator;
		
		System.out.println(nodeCoverage + "\t" + numerator + "\t" + denominator);
		System.out.println(coveredT + "\t" + coveredI + "\t" + coveredS);
	}
	
/*	public static void nodeCentralityWRTWaist(DependencyDAG dependencyDAG) {
		
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
*/	
	
	public static void coreLocationAnalysis(DependencyDAG dependencyDAG) {
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
			
			for (double i = 0; i < weight * 100; ++i) {
//				System.out.println(loc);
			}
		}
	}
	
	public static boolean verifyCore(DependencyDAG dependencyDAG, TreeSet<String> testCore) {
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
