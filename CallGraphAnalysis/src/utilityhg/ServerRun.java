package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import corehg.DependencyDAG;

public class ServerRun {
	public static double numPaths = 0;
	public static HashMap<String, Double> pCentrality;
	
	private static void traverseAllPathsHelper(String node, DependencyDAG dependencyDAG, HashSet<String> pathNodes, int depth) {
		if (dependencyDAG.isTarget(node)) {
			numPaths++;
			for (String s: pathNodes) {
//				System.out.print(s + "\t");
				pCentrality.put(s, pCentrality.get(s) + 1.0);
			}
//			System.out.println();
//			System.out.println(numPaths);
			return;
		}
		
		if (depth > 10) {
			return;
		}
		
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes, depth + 1);
			pathNodes.remove(s);
		}
	}
	
	private static void traverseAllPaths() throws Exception {
		PrintWriter pw = new PrintWriter(new File("serverRun.txt"));
		
		DependencyDAG.isCyclic = true;
		
		String neuroDAGName = "celegans_network_clean";
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		dependencyDAG.printNetworkProperties();
		
//		DependencyDAG.isToy = true;
//		String toyDAGName = "toy_cyclic_2";
//		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		final long startTime = System.nanoTime();
		
		pCentrality = new HashMap();
		for (String s: dependencyDAG.nodes) {
			pCentrality.put(s, 0.0);
		}
		
		HashSet<String> pathNodes = new HashSet();
		numPaths = 0;
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.isSource(s)) continue;
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes, 1);
			pathNodes.remove(s);
			System.out.println(s + "\t" + numPaths);
		}
		
		for (String s: dependencyDAG.nodes) {
			pw.println(s + "\t" + pCentrality.get(s));
		}
		
		pw.println("Total paths: " + numPaths);
		
		final long duration = System.nanoTime() - startTime;
		pw.println((duration / 1000000000.0) + "\t seconds");
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		traverseAllPaths();
	}
}
