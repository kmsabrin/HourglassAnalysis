package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class WaistDetection {
	static HashSet<String> topKNodes = new HashSet();

	public static void runPCWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//path-cover-" + filePath + ".txt"));
		
		TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
//			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
//				centralitySortedNodes.put(dependencyDAG.harmonicMeanPathCentrality.get(s), s);
//			}
			centralitySortedNodes.put(dependencyDAG.normalizedPathCentrality.get(s), s);
		}
		
		int k = 1000;
		ArrayList<String> tempTopKNodes;
		double tPath = dependencyDAG.nTotalPath;
		System.out.println(tPath);
		topKNodes = new HashSet();
		tempTopKNodes = new ArrayList();

		for (double pC : centralitySortedNodes.keySet()) {
			Collection<String> nodes = centralitySortedNodes.get(pC);
			for (String s : nodes) {
				tempTopKNodes.add(s);
				if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
					// System.out.println(s + "\t" + dependencyDAG.geometricMeanPathCentrality.get(s) + "\t" + dependencyDAG.geometricMeanPagerankCentrality.get(s));
				}
			}

			if (tempTopKNodes.size() >= k) {
				break;
			}
		}

		double individualCumulativePaths = 0;
		for (String s : tempTopKNodes) {
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			individualCumulativePaths += dependencyDAG.numOfTargetPath.get(s) * dependencyDAG.numOfSourcePath.get(s);
			topKNodes.add(s);
			 System.out.println(s + "\t" + dependencyDAG.numOfTargetPath.get(s) + "\t" + dependencyDAG.numOfSourcePath.get(s) + "\t" + individualCumulativePaths);
			double pathCoverage = individualCumulativePaths / tPath;
			pw.println(topKNodes.size() + " " + pathCoverage);

			if (pathCoverage > 0.99) {
				break;
			}
		}
		
		pw.close();
	}
	
/*	
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
*/
}
