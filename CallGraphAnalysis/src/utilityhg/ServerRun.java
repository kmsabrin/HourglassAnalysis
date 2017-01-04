package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import corehg.DependencyDAG;

public class ServerRun {
	public static double numPaths = 0;
	public static HashMap<String, Double> pCentrality80;
	public static HashMap<String, Double> pCentrality70;
	public static Random random;
	public static int discardValue = 0;
	
	private static void traverseAllPathsHelper(String node, DependencyDAG dependencyDAG, HashSet<String> pathNodes, int depth, HashMap<String, Double> pCentrality) {
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
		
//		if (depth > 10) {
//			return;
//		}
		
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			
			if (random.nextInt(100) < discardValue) {
				continue;
			}
			
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes, depth + 1, pCentrality);
			pathNodes.remove(s);
		}
	}
	
	private static void traverseAllPaths() throws Exception {
		PrintWriter pw = new PrintWriter(new File("serverRun.txt"));
		random = new Random(System.nanoTime());
		
		DependencyDAG.isCyclic = true;
		
		String neuroDAGName = "celegans_network_clean";
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		dependencyDAG.printNetworkProperties();
		
//		DependencyDAG.isToy = true;
//		String toyDAGName = "toy_cyclic_2";
//		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		final long startTime = System.nanoTime();
		
		pCentrality80 = new HashMap();
		pCentrality70 = new HashMap();
		for (String s: dependencyDAG.nodes) {
			pCentrality80.put(s, 0.0);
			pCentrality70.put(s, 0.0);
		}
		
		HashSet<String> pathNodes = new HashSet();
		numPaths = 0;
		discardValue = 65;
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.isSource(s)) continue;
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes, 1, pCentrality80);
			pathNodes.remove(s);
//			System.out.println(s + "\t" + numPaths);
		}
		
		pathNodes = new HashSet();
		numPaths = 0;
		discardValue = 55;
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.isSource(s)) continue;
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes, 1, pCentrality70);
			pathNodes.remove(s);
//			System.out.println(s + "\t" + numPaths);
		}
		
		double l2norm80 = 0;
		double l2norm70 = 0;
		double l2multiply = 0;
		for (String s: dependencyDAG.nodes) {
//			System.out.println(s + "\t" + pCentrality80.get(s));
			l2norm80 += pCentrality80.get(s) * pCentrality80.get(s);
			l2norm70 += pCentrality70.get(s) * pCentrality70.get(s);
			l2multiply += pCentrality80.get(s) * pCentrality70.get(s);
		}
		
		l2norm80 = Math.sqrt(l2norm80);
		l2norm70 = Math.sqrt(l2norm70);
		
//		System.out.println("Total paths: " + numPaths);
		System.out.println(l2multiply / (l2norm80 * l2norm70));
		System.out.println("Cosine diff: " + Math.acos(l2multiply / (l2norm80 * l2norm70)));
		
		final long duration = System.nanoTime() - startTime;
		System.out.println((duration / 1000000000.0) + "\t seconds");
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		traverseAllPaths();
	}
}
