package Final;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class UpstreamRandomize {
	
	static void randomizeDAG(DependencyDAG dependencyDAG) {
		
		Random random = new Random(System.nanoTime());
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) continue;
//			System.out.println("Processing: " + s);
			
			int sampleSize = dependencyDAG.serversReachable.get(s).size();
			ArrayList<String> ancestor = new ArrayList(dependencyDAG.serversReachable.get(s));
			HashSet<Integer> newAncestorID = new HashSet();
			
			int numDependents = dependencyDAG.depends.get(s).size();
			for (String originalAncestor: dependencyDAG.depends.get(s)) {
				dependencyDAG.serves.get(originalAncestor).remove(s);
			}
			dependencyDAG.depends.get(s).clear();
			
			while (--numDependents >= 0) {
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
		
//		for (String s: dependencyDAG.nodes) {
//			if (dependencyDAG.serves.containsKey(s)) {
//				if (dependencyDAG.serves.get(s).isEmpty()) {
//					dependencyDAG.serves.remove(s); // allow new targets
//				}
//			}
//		}
		
		regenerateDAGProperties(dependencyDAG);
	}
	
	static void regenerateDAGProperties(DependencyDAG dependencyDAG) {
		dependencyDAG.isRandomized = true;
		dependencyDAG.resetAuxiliary();
		dependencyDAG.removeIsolatedNodes();
		dependencyDAG.loadDegreeMetric();
		dependencyDAG.loadPathStatistics();
		dependencyDAG.loadLocationMetric();
		dependencyDAG.loadServerReachabilityAll();
		dependencyDAG.loadPathCentralityMetric();
//		dependencyDAG.printNetworkProperties();
	}
}
