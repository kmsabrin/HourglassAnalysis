package corehg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import utilityhg.ZipfDistributionWrapper;

public class UpstreamRandomize {
	public static ArrayList<Double> alphaEstimates;

	public static void hieararchyDistanceRandomizeDAG(DependencyDAG dependencyDAG) {
		Random random = new Random(System.nanoTime());
		ModelRealConnector modelRealConnector = new ModelRealConnector(dependencyDAG);
		alphaEstimates = new ArrayList();
		HashMap<Integer, HashMap<Integer, Integer>> levelInputDistanceFrequencyMap = new HashMap();
		
		HashMap<Integer, Integer> biasCount = new HashMap();
		int total = 0;
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) continue;
//			if (dependencyDAG.isTarget(s)) continue;
			
			int currentNodeLevel = modelRealConnector.nodeLevelMap.get(s);
			HashSet<String> ancestors = new HashSet(dependencyDAG.ancestors.get(s));
			HashSet<String> substrates = new HashSet(dependencyDAG.depends.get(s));
			HashSet<String> newSubstrates = new HashSet();
			
			if (modelRealConnector.nodeLevelMap.get(s) > 1 && substrates.size() > 1) {
				// only considering layer 2 and above (layering start at source with 0)
				// if a node has only 1 substrate skip it
//				System.out.println("Processing: " + s);
//				System.out.println("For " + s );
				alphaEstimates.add(getMLAlpha(s, modelRealConnector, dependencyDAG));
			}
			
			for (String originalSubstrate: substrates) {
				dependencyDAG.serves.get(originalSubstrate).remove(s);
			}
			
			dependencyDAG.depends.get(s).clear();
			
//			System.out.println("For " + s );
			for (String r: substrates) {
				int substrateLevel = modelRealConnector.nodeLevelMap.get(r);
				int distance = currentNodeLevel - substrateLevel;
//				System.out.print("  " + r + "," + distance);
				
				if (biasCount.containsKey(distance)) {
					biasCount.put(distance, biasCount.get(distance) + 1);
				}
				else {
					biasCount.put(distance, 1);
				}
				
				if (!levelInputDistanceFrequencyMap.containsKey(currentNodeLevel)) {
					levelInputDistanceFrequencyMap.put(currentNodeLevel, new HashMap<Integer, Integer>());
				}	
				
				HashMap<Integer, Integer> frequencyMap = levelInputDistanceFrequencyMap.get(currentNodeLevel);
				if (frequencyMap.containsKey(distance)) {
					frequencyMap.put(distance, frequencyMap.get(distance) + 1);
				}
				else {
					frequencyMap.put(distance, 1);
				}
				
				++total;
				
				
				/* randomization start */
				ArrayList<String> sameLevelAncestors = new ArrayList();
				boolean allAncestor = false; // all or subtree only ancestors
				for (String w: modelRealConnector.levelNodeMap.get(substrateLevel)) {
					if (ancestors.contains(w) || allAncestor) {
						sameLevelAncestors.add(w);
					}
				}
				if (sameLevelAncestors.size() > 1) {
//					sameLevelAncestors.remove(r); // creates deadlock
				}
				
//				System.out.println("For sub " + r + " candidate " + sameLevelAncestors);
				
				String newSub = null;
				do {
					newSub = sameLevelAncestors.get(random.nextInt(sameLevelAncestors.size()));
				}
				while (newSubstrates.contains(newSub));
				
				newSubstrates.add(newSub);
				dependencyDAG.depends.get(s).add(newSub);
				dependencyDAG.serves.get(newSub).add(s);
				/* end randomization */
			}
//			System.out.println();
		}	
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				if (dependencyDAG.serves.get(s).isEmpty()) {
					dependencyDAG.serves.remove(s); // allow new targets
				}
			}
		}
		
		for (double d: alphaEstimates) {
//			System.out.println(d);
		}
		
		for (int i: biasCount.keySet()) {
//			System.out.println(i + "\t" + (biasCount.get(i) * 1.0 / total));
		}
		
		for (int i: levelInputDistanceFrequencyMap.keySet()) {
			System.out.println(i);
			HashMap<Integer, Integer> frequencyMap = levelInputDistanceFrequencyMap.get(i);
			double sum = 0;
			for (int j: frequencyMap.keySet()) {
				sum += frequencyMap.get(j);
			}
			
			for (int j: frequencyMap.keySet()) {
				System.out.println(j + "\t" + (frequencyMap.get(j) / sum));
//				System.out.print((frequencyMap.get(j) / sum) + "\t");
			}
			
			System.out.println("## ## ##");
//			System.out.println();
		}
		
		regenerateDAGProperties(dependencyDAG);
	}
	
	private static double getMLAlpha(String node, ModelRealConnector modelRealConnector, DependencyDAG dependencyDAG) {
		HashSet<String> ancestors = new HashSet(dependencyDAG.ancestors.get(node));
		HashSet<String> substrates = new HashSet(dependencyDAG.depends.get(node));
		int maxLevel = modelRealConnector.nodeLevelMap.get(node);
		
		double maxLogLikelihood = Double.NEGATIVE_INFINITY;
		double alphaEstimate = 0;
//		System.out.println(maxLogLikelihood);
		
		for (double alpha = -1.02; alpha <= 1.01; alpha += 0.05) {
//			if (alpha == 0) continue; // zipf can't handle alpha <= 0, < 0 is taken care of specially
			ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(maxLevel, alpha);
//			System.out.println(zipfDistributionWrapper.alphaNegative);
			double logLikelihood = 0;
			boolean skipOneNextLayerSubstrateFlag = true;
			boolean firstTerm = true;
			for (String r: substrates) {
				int substrateLevel = modelRealConnector.nodeLevelMap.get(r);
				int substrateRank = maxLevel - substrateLevel;
				if (substrateRank == 1 && skipOneNextLayerSubstrateFlag) {
					skipOneNextLayerSubstrateFlag = false;
					continue;
				}
				
				double substrateProbabilty = zipfDistributionWrapper.getProbabilityFromZipfDistribution(maxLevel, substrateRank);
				if (firstTerm) {
					logLikelihood = Math.log(substrateProbabilty);
					firstTerm = false;
				}
				else {
					logLikelihood += Math.log(substrateProbabilty); 
				}
//				System.out.println(r + "\t" + substrateProbabilty + "\t" + Math.log(substrateProbabilty) + "\t" + substrateLevel);
			}
			
//			System.out.println("Alpha: " + alpha + "\t" + logLikelihood);
			if (logLikelihood > maxLogLikelihood) {
				maxLogLikelihood = logLikelihood;
				alphaEstimate = alpha;
//				System.out.println("Max LL " + maxLogLikelihood + "\t" + alpha);
			}
		}
		
//		System.out.println(alphaEstimate + "\t" + dependencyDAG.nodePathThrough.get(node));
		
		return alphaEstimate;
	}
	
	public static void randomizeDAG(DependencyDAG dependencyDAG) {
		
		Random random = new Random(System.nanoTime());
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) continue;
//			System.out.println("Processing: " + s);
			
			int sampleSize = dependencyDAG.ancestors.get(s).size();
			ArrayList<String> ancestor = new ArrayList(dependencyDAG.ancestors.get(s));
			HashSet<Integer> newSubstrateID = new HashSet();
			
			int inDegree = dependencyDAG.depends.get(s).size();
			for (String originalSubstrate: dependencyDAG.depends.get(s)) {
				dependencyDAG.serves.get(originalSubstrate).remove(s);
			}
			dependencyDAG.depends.get(s).clear();
			
			while (--inDegree >= 0) {
				int shuffleID = -1;
				do {
					shuffleID = random.nextInt(sampleSize);
				}
				while (newSubstrateID.contains(shuffleID));
				
				newSubstrateID.add(shuffleID);
				String shuffledAncestor = ancestor.get(shuffleID);
				dependencyDAG.depends.get(s).add(shuffledAncestor);
				dependencyDAG.serves.get(shuffledAncestor).add(s);
//				System.out.println("Adding: " + shuffledAncestor);
			}
		}	
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				if (dependencyDAG.serves.get(s).isEmpty()) {
					dependencyDAG.serves.remove(s); // allow new targets
				}
			}
		}
		
		regenerateDAGProperties(dependencyDAG);
	}
	
	static void regenerateDAGProperties(DependencyDAG dependencyDAG) {
		DependencyDAG.isRandomized = true;
		
		dependencyDAG.init();
//		dependencyDAG.removeIsolatedNodes();
		dependencyDAG.loadDegreeMetric();
		dependencyDAG.loadPathStatistics();
		dependencyDAG.loadLocationMetric();
		dependencyDAG.loadServerReachabilityAll();
		dependencyDAG.loadPathCentralityMetric();
//		dependencyDAG.printNetworkProperties();
//		dependencyDAG.printNetworkStat();
		
//		DependencyDAG.isRandomized = false;
	}
}
