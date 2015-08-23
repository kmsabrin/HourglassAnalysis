package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class WaistDetection {
	static HashSet<String> topKNodes = new HashSet();
	static double pathCoverageTau = 0.95;

	public static void runPCWaistDetection(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//path-cover-" + filePath + ".txt"));
		
		TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) 
			{
				centralitySortedNodes.put(dependencyDAG.normalizedPathCentrality.get(s), s);
			}
		}
		
		int k = 1000;
		ArrayList<String> tempTopKNodes;
		double tPath = dependencyDAG.nTotalPath;
		topKNodes = new HashSet();
		tempTopKNodes = new ArrayList();

		for (double pC : centralitySortedNodes.keySet()) {
			Collection<String> nodes = centralitySortedNodes.get(pC);
			for (String s : nodes) {
				tempTopKNodes.add(s);
			}
			if (tempTopKNodes.size() >= k) {
				break;
			}
		}

		TreeMultimap<Double, String> pathCoverageSortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		double individualCumulativePaths = 0;
		for (String s : tempTopKNodes) {
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			double individualPaths = dependencyDAG.numOfTargetPath.get(s) * dependencyDAG.numOfSourcePath.get(s);
			individualCumulativePaths += individualPaths;
			topKNodes.add(s);
			double pathCoverage = individualCumulativePaths / tPath;
//			pw.println(topKNodes.size() + " " + pathCoverage);
			
			pathCoverageSortedNodes.put(individualPaths / tPath, s);
			
			if (pathCoverage > pathCoverageTau) {
				break;
			}
			
//			if (topKNodes.size() > 100) {
//				break;
//			}
		}
		
		System.out.println("Centrality Sorted Path Coverage Waist Size: " + topKNodes.size());
		
		tempTopKNodes.clear();
		topKNodes.clear();
		
		for (double pC : pathCoverageSortedNodes.keySet()) {
			Collection<String> nodes = pathCoverageSortedNodes.get(pC);
			for (String s : nodes) {
				tempTopKNodes.add(s);
			}
			if (tempTopKNodes.size() >= k) {
				break;
			}
		}

		individualCumulativePaths = 0;
		for (String s : tempTopKNodes) {
			dependencyDAG.numOfTargetPath.clear();
			dependencyDAG.numOfSourcePath.clear();
			dependencyDAG.loadPathStatistics();
			double individualPaths = dependencyDAG.numOfTargetPath.get(s) * dependencyDAG.numOfSourcePath.get(s);
			individualCumulativePaths += individualPaths;
			topKNodes.add(s);
			double pathCoverage = individualCumulativePaths / tPath;
			pw.println(topKNodes.size() + " " + pathCoverage);	
			
//			System.out.println(s);

			if (pathCoverage > pathCoverageTau) {
				break;
			}
			
//			if (individualPaths < 0.000001) {
//				break;
//			}
			
//			if (topKNodes.size() > 100) {
//				break;
//			}
		}
				
		getONodes(dependencyDAG);
		pw.close();
	}
	
	public static void getONodes(DependencyDAG dependencyDAG) {
		HashSet<String> waistNodeCoverage = new HashSet();

		int STNodes = 0;
		
		for (String s: topKNodes) {
			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableUpwardsNodes(s); // how many nodes are using her
			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);
			
			dependencyDAG.visited.clear();
			dependencyDAG.kounter = 0;
			dependencyDAG.reachableDownwardsNodes(s); // how many nodes are using her
			dependencyDAG.visited.remove(s); // remove ifself
			waistNodeCoverage.addAll(dependencyDAG.visited);
			
			if (!dependencyDAG.serves.containsKey(s) || !dependencyDAG.depends.containsKey(s)) {
				++STNodes;
			}
		}
		
		System.out.println("Waist Size: " + topKNodes.size());
		System.out.print("Waist Node Coverage: " + waistNodeCoverage.size() + " of " + dependencyDAG.functions.size() + " i.e. ");
		System.out.println(waistNodeCoverage.size() * 100.0 / dependencyDAG.functions.size() + "%%");
		 
		double nonSTinWaist = (topKNodes.size() - STNodes) * 1.0 / topKNodes.size();
		double minST = Math.min(dependencyDAG.nSources, dependencyDAG.nTargets);
		System.out.println("nonSTinWaist: " + (topKNodes.size() - STNodes));
		double hourglassnessScore = nonSTinWaist * ((minST - Math.min(topKNodes.size() - STNodes, minST) + 1.0) / minST);
		System.out.println("Hourglassness: " + hourglassnessScore);
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
