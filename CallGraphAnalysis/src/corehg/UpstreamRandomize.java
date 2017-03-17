package corehg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class UpstreamRandomize {

	public static void hieararchyDistanceRandomizeDAG(DependencyDAG dependencyDAG) {
		Random random = new Random(System.nanoTime());
		ModelRealConnector modelRealConnector = new ModelRealConnector(dependencyDAG);
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) continue;
			
			HashSet<String> ancestors = new HashSet(dependencyDAG.ancestors.get(s));
			HashSet<String> substrates = new HashSet(dependencyDAG.depends.get(s));
			HashSet<String> newSubstrates = new HashSet();
			
			for (String originalSubstrate: substrates) {
				dependencyDAG.serves.get(originalSubstrate).remove(s);
			}
			
			dependencyDAG.depends.get(s).clear();
			
//			System.out.println("For " + s );
			for (String r: substrates) {
				int substrateLevel = modelRealConnector.nodeLevelMap.get(r);
				ArrayList<String> sameLevelAncestors = new ArrayList();
				boolean allAncestor = true;
				for (String w: modelRealConnector.levelNodeMap.get(substrateLevel)) {
					if (ancestors.contains(w) || allAncestor) {
						sameLevelAncestors.add(w);
					}
				}
				if (sameLevelAncestors.size() > 1) {
					sameLevelAncestors.remove(r);
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
	
	public static void randomizeDAG(DependencyDAG dependencyDAG) {
		
		Random random = new Random(System.nanoTime());
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) continue;
//			System.out.println("Processing: " + s);
			
			int sampleSize = dependencyDAG.ancestors.get(s).size();
			ArrayList<String> ancestor = new ArrayList(dependencyDAG.ancestors.get(s));
			HashSet<Integer> newAncestorID = new HashSet();
			
			int inDegree = dependencyDAG.depends.get(s).size();
			for (String originalAncestor: dependencyDAG.depends.get(s)) {
				dependencyDAG.serves.get(originalAncestor).remove(s);
			}
			dependencyDAG.depends.get(s).clear();
			
			while (--inDegree >= 0) {
				int shuffleID = -1;
				do {
					shuffleID = random.nextInt(sampleSize);
				}
				while (newAncestorID.contains(shuffleID));
				
				newAncestorID.add(shuffleID);
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
