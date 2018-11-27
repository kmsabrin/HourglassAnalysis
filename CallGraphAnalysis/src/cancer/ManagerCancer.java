package cancer;

import java.util.HashMap;
import java.util.HashSet;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ManagerCancer {
	private static void printCancerNetwork(DependencyDAG cancerDAG) {
		HashSet<String> miR429OnlyTarget = new HashSet();
		for (String s: cancerDAG.nodes) {
			if (cancerDAG.isTarget(s)) {
				if (cancerDAG.depends.get(s).size() == 1 && cancerDAG.depends.get(s).iterator().next().equals("miR429")) {
					miR429OnlyTarget.add(s);
				}
			}
		}
		
		HashMap<String, Integer> edgeWeight = new HashMap();
		for (String s: cancerDAG.nodes) {
			if (cancerDAG.isTarget(s)) continue;
			for (String r: cancerDAG.serves.get(s)) {
//				if (miR429OnlyTarget.contains(r)) {
//					continue;
//				}
				
				String t = "";
				if (cancerDAG.isTarget(r) && cancerDAG.depends.get(r).size() < 2) {
//					System.out.println(s + "\t" + "target-" + s);
					t = s + "\t" + "t" + s;
				}
				else {
//					System.out.println(s + "\t" + r);
					t = s + "\t" + r;
				}
				
				if (edgeWeight.containsKey(t)) {
					edgeWeight.put(t, edgeWeight.get(t) + 1);
				}
				else {
					edgeWeight.put(t, 1);
				}
			}
		}
		
		for (String s: edgeWeight.keySet()) {
			System.out.println(s + "\t" + edgeWeight.get(s));
		}
		
//		System.out.println("miR429" + "\t" + "target-miR429");
	}
	
	private static void doCancerNetworkAnalysis() throws Exception {
		DependencyDAG.isCallgraph = true;
//		DependencyDAG.isCyclic = true;
		String cancerDAGName = "cancer_2.5";
		DependencyDAG cancerDependencyDAG = new DependencyDAG("cancer_networks//" + cancerDAGName + ".dot");
		
//		printCancerNetwork(toyDependencyDAG);
		
		String netID = "cancer_dag";
		cancerDependencyDAG.printNetworkStat();
		cancerDependencyDAG.printNetworkProperties();

		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(cancerDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;
//
//		cancerDependencyDAG = new DependencyDAG("toy_networks//" + cancerDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(cancerDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}

	public static void main(String[] args) throws Exception {
		doCancerNetworkAnalysis();
	}
}
