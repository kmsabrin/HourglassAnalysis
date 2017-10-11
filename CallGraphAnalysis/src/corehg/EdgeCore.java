package corehg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import neuro.ManagerNeuro;
import utilityhg.Edge;

public class EdgeCore {
	private static void getEdgeCore() throws Exception {
		ManagerNeuro.loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		String netPath = "neuro_networks//celegans";
		String netID = "celegans";
		DependencyDAG.isToy = true;
		DependencyDAG neuroDependencyDAG = new DependencyDAG(netPath + ".socialrank.network");

		double startingTotalPath = neuroDependencyDAG.nTotalPath;
		double cumulativePath = 0;
		ArrayList<Edge> startingOrderedEdgeList = new ArrayList(neuroDependencyDAG.edgePathCentrality);
		double tau = 0.98;
		HashSet<String> coreEdges = new HashSet();
		HashSet<String> coreEdgeVertices = new HashSet();
		double kPathCentrality = -1;
		for (Edge e: startingOrderedEdgeList) {
			neuroDependencyDAG.edgeSkip.add(e.source + "," + e.target);
			neuroDependencyDAG.loadPathStatistics();
			double currentTotalPath = neuroDependencyDAG.nTotalPath;
//			System.out.println(startingTotalPath + "\t" + currentTotalPath + "\t" + cumulativePath);
			double weight = (startingTotalPath - currentTotalPath - cumulativePath) / startingTotalPath;
			weight = (int)(weight * 1000) / 1000.0;
			System.out.println(e + "\t" + weight);
			cumulativePath = startingTotalPath - currentTotalPath;
			coreEdges.add(e.source + "," + e.target);
			if ((startingTotalPath - currentTotalPath) / startingTotalPath >= tau) {
				kPathCentrality = e.pathCentrality;
				break;
			}
			coreEdgeVertices.add(e.source);
			coreEdgeVertices.add(e.target);
		}
		
		Scanner scanner = new Scanner(new File(netPath + "_graph.txt"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			String wgt = scanner.next();
			
			if (coreEdges.contains(src + "," + tgt)) continue;
			if (coreEdgeVertices.contains(src) && coreEdgeVertices.contains(tgt)) {
				System.out.println(src + "\t" + tgt + "\t" + " ");
			}
		}
		scanner.close();
	}
	
	public static void main(String[] args) throws Exception {
		getEdgeCore();
	}
}
