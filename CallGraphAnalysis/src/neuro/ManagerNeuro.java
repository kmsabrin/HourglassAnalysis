package neuro;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;

import utilityhg.DistributionAnalysis;
import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlattenNetwork;

public class ManagerNeuro {

	private static void loadNodes(HashSet<Integer> nodes, HashSet<Integer> typeNode, String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			int i = scan.nextInt();
			typeNode.add(i);
			nodes.add(i);
		}
		scan.close();
	}
	
	private static void removeDuplicate(HashSet<Integer> from, HashSet<Integer> to1, HashSet<Integer> to2) {
		HashSet<Integer> toRemove = new HashSet();
		for (int i: from) {
			if (to1.contains(i)) {
				to1.remove(i);
				toRemove.add(i);
			}
			if (to2.contains(i)) {
				to2.remove(i);
				toRemove.add(i);
			}
		}
//		System.out.println(toRemove.size());
		from.removeAll(toRemove);
	}
	
	private static void writeFile(String edgeFileName, HashSet<Integer> source, HashSet<Integer> target) throws Exception {
		Scanner scan = new Scanner(new File(edgeFileName));
		PrintWriter pw = new PrintWriter(new File("neuro_networks//celegans_network_clean.txt"));
		
		while (scan.hasNext()) {
			int src = scan.nextInt();
			int dst = scan.nextInt();
			double weight = scan.nextDouble();
			
			if (target.contains(src)) {
				continue;
			}
			
			if (source.contains(dst)) {
				continue;
			}
			
			pw.println(src + "\t" + dst);
		}
		
		scan.close();
		pw.close();
	}
	
	private static void getCleanNeuroNetwork() throws Exception {
		HashSet<Integer> source = new HashSet();
		HashSet<Integer> intermediate = new HashSet();
		HashSet<Integer> target = new HashSet();
		HashSet<Integer> nodes = new HashSet();
		
		loadNodes(nodes, source, "neuro_networks//sensory_neurons.txt");
		loadNodes(nodes, intermediate, "neuro_networks//inter_neurons.txt");
		loadNodes(nodes, target, "neuro_networks//motor_neurons.txt");
		
		removeDuplicate(source, intermediate, target);
		removeDuplicate(intermediate, source, target);
		removeDuplicate(target, source, intermediate);
		
		writeFile("neuro_networks//celegans_graph.txt", source, target);
	}
	
	private static void doNeuroNetworkAnalysis() throws Exception {
		DependencyDAG.isCyclic = true;
		String neuroDAGName = "celegans_network_clean";
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		
		String netID = "neuro_network";
		DependencyDAG.printNetworkStat(neuroDependencyDAG);
//		DistributionAnalysis.getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();

		CoreDetection.pathCoverageTau = 0.9999;
		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(neuroDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;
//
//		neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(neuroDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}
	
	public static void main(String[] args) throws Exception {
//		getCleanNeuroNetwork();
		doNeuroNetworkAnalysis();
	}
}
