package Remodeled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class WaistDetection {
	static HashSet<String> topKNodes = new HashSet();

	public static void runPCWaistDetection(DependencyDAG dependencyDAG) {
		TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
//			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
//				pathCentralitySortedNodes.put(dependencyDAG.harmonicMeanPathCentrality.get(s), s);
				centralitySortedNodes.put(dependencyDAG.geometricMeanPathCentrality.get(s), s);
//			}
		}
		
		int k = 100;
		ArrayList<String> tempTopKNodes;
		double tPath = dependencyDAG.nTotalPath;
		while (true) {			
			topKNodes = new HashSet();
			tempTopKNodes = new ArrayList();
			
			for (double pC: centralitySortedNodes.keySet()) {
				Collection<String> nodes = centralitySortedNodes.get(pC);
				for (String s: nodes) {
//					topKNodes.add(s);
					tempTopKNodes.add(s);
//					System.out.print(s + " " + pC); 
//					System.out.println(" " + dependencyDAG.numOfTargetPath.get(s) + " " + dependencyDAG.numOfSourcePath.get(s));
					if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
						System.out.println(s + "\t" + dependencyDAG.geometricMeanPathCentrality.get(s) + "\t" + dependencyDAG.geometricMeanPagerankCentrality.get(s));
					}
				}
				
				if (tempTopKNodes.size() >= k) {
					break;
				}
			}
			
			double individualCumulativePaths = 0;
			for (String s: tempTopKNodes) {
//				topKNodes.remove(s);
				dependencyDAG.numOfTargetPath.clear();
				dependencyDAG.numOfSourcePath.clear();
				dependencyDAG.loadPathStatistics();
				dependencyDAG.loadPathCentralityMetric();
				individualCumulativePaths += dependencyDAG.numOfTargetPath.get(s) * dependencyDAG.numOfSourcePath.get(s);
				topKNodes.add(s);
//				System.out.println(s + "\t" + dependencyDAG.numOfTargetPath.get(s) + "\t" + dependencyDAG.numOfSourcePath.get(s) + "\t" + individualCumulativePaths);
				System.out.println("Top-" + topKNodes.size() + " " + (individualCumulativePaths/tPath));			
			}
			
//			System.out.println("Top-" + topKNodes.size() + " " + (individualCumulativePaths/tPath));
//			System.out.println(individualCumulativePaths/tPath);
//			System.out.println("### ### ###");
			
//			if (individualizedCumulativeTargetPaths >= 0.9 && individualizedCumulativeSourcePaths >= 0.9) {
//				break;
//			}
			
//			if (k >=100 ) {
////				System.out.println("No waist");
//				break;
//			}
			
//			k += 1;
			break;
		}
	}
	
	public static void runPRWaistDetection(DependencyDAG dependencyDAG) {
		TreeMultimap<Double, String> pagerankSortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
				pagerankSortedNodes.put(dependencyDAG.harmonicMeanPagerankCentrality.get(s), s);
			}
		}
		
		int k = 100;
		ArrayList<String> tempTopKNodes;
		while (true) {			
			topKNodes = new HashSet();
			tempTopKNodes = new ArrayList();
			
			for (double prC: pagerankSortedNodes.keySet()) {
				Collection<String> nodes = pagerankSortedNodes.get(prC);
				for (String s: nodes) {
					topKNodes.add(s);
					tempTopKNodes.add(s);
					System.out.print(s + " " + prC); 
					System.out.println(" " + dependencyDAG.pagerankTargetCompression.get(s) + " " + dependencyDAG.pagerankSourceCompression.get(s));
				}
				
				if (topKNodes.size() >= k) {
					break;
				}
			}
			
			double individualizedCumulativeTargetCompression = 0;
			double individualizedCumulativeSourceCompression = 0;
			for (String s: tempTopKNodes) {
				topKNodes.remove(s);
				dependencyDAG.pagerankTargetCompression.clear();
				dependencyDAG.pagerankSourceCompression.clear();
				dependencyDAG.loadPagerankCentralityMetric();
				individualizedCumulativeTargetCompression += dependencyDAG.pagerankTargetCompression.get(s);
				individualizedCumulativeSourceCompression += dependencyDAG.pagerankSourceCompression.get(s);
				topKNodes.add(s);
//				System.out.println(s + " " + dependencyDAG.pagerankTargetCompression.get(s) + " " + dependencyDAG.pagerankSourceCompression.get(s));
			}
			
			System.out.println("Top-" + k + " " + individualizedCumulativeTargetCompression + "\t" + individualizedCumulativeSourceCompression);
//			System.out.println("### ### ###");
			
			if (individualizedCumulativeTargetCompression >= 0.9 && individualizedCumulativeSourceCompression >= 0.9) {
				break;
			}
			
			if (k >= 100 ) {
				System.out.println("No waist");
				break;
			}
			
			k += 10;
		}
	}
}
