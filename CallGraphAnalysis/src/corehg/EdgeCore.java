package corehg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import neuro.ManagerNeuro;
import utilityhg.Edge;

public class EdgeCore {
	public static HashMap<String, String> idNeuron = new HashMap();
	public static String netPath = "neuro_networks//";
	public static String netID = "celegans";

	private static void loadNeuronNames() throws Exception {
		idNeuron = new HashMap();
		Scanner scanner = new Scanner(new File(netPath + netID + "_labels.txt"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String name = scanner.next();
			idNeuron.put(id, name);
		}
		scanner.close();
	}
	
	private static void getEdgeCore() throws Exception {
		ManagerNeuro.loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		DependencyDAG.isToy = true;
		DependencyDAG.isWeighted = true;
		DependencyDAG neuroDependencyDAG = new DependencyDAG(netPath + netID + ".socialrank.network.weighted");

		double startingTotalPath = neuroDependencyDAG.nTotalPath;
		double cumulativePath = 0;
		ArrayList<Edge> startingOrderedEdgeList = new ArrayList(neuroDependencyDAG.edgePathCentrality);
		double tau = 0.98;
		HashSet<String> coreEdges = new HashSet();
		HashSet<String> coreEdgeVertices = new HashSet();
		double kPathCentrality = -1;
		int k = 10;
		for (Edge e: startingOrderedEdgeList) {
			System.out.println(e);
			neuroDependencyDAG.edgeSkip.add(e.source + "," + e.target);
			neuroDependencyDAG.loadPathStatistics();
			double currentTotalPath = neuroDependencyDAG.nTotalPath;
			cumulativePath = startingTotalPath - currentTotalPath;
			System.out.println(startingTotalPath + "\t" + currentTotalPath + "\t" + cumulativePath);
			double weight = (startingTotalPath - currentTotalPath - cumulativePath) / startingTotalPath;
//			System.out.println(idNeuron.get(e.source) + "\t" + idNeuron.get(e.target) + "\t" + weight);
			weight = (int)(weight * 1000) / 1000.0;
//			System.out.println(e + "\t" + weight);
			coreEdges.add(e.source + "," + e.target);
			if ((startingTotalPath - currentTotalPath) / startingTotalPath >= tau) {
				kPathCentrality = e.pathCentrality;
				break;
			}
			coreEdgeVertices.add(e.source);
			coreEdgeVertices.add(e.target);
			if (k < 0) break; --k;
		}
		
		Scanner scanner = new Scanner(new File(netPath + netID + "_graph.txt"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			String wgt = scanner.next();
//			System.out.println(src + "\t" + tgt + "\t" + wgt);
//			System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(tgt) + "\t" + wgt);
			
			if (coreEdges.contains(src + "," + tgt)) continue;
			if (coreEdgeVertices.contains(src) && coreEdgeVertices.contains(tgt)) {
//				System.out.println(src + "\t" + tgt + "\t" + " ");
//				System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(tgt));
			}
		}
		scanner.close();
	}
	
	public static void main(String[] args) throws Exception {
		loadNeuronNames();
		getEdgeCore();
	}
}
