package neuro;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import utilityhg.Visualization;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlattenNetwork;

public class ManagerNeuro {
	public static HashMap<String, String> idNeuronMap = new HashMap();
	public static double numPaths = 0;
	
	private static void loadNodes(HashSet<Integer> nodes, HashSet<Integer> typeNode, String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			int i = scan.nextInt();
			typeNode.add(i);
			nodes.add(i);
		}
		scan.close();
	}
	
	private static void loadNeurons(String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			String i = scan.next();
			String n = scan.next();
			idNeuronMap.put(i, n);
		}
		scan.close();
	}
	
	private static void removeDuplicate(HashSet<Integer> from, HashSet<Integer> to1, HashSet<Integer> to2, HashSet<Integer> nodes) {
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
		nodes.removeAll(toRemove);
	}
	
	private static void writeFile(String edgeFileName, HashSet<Integer> source, HashSet<Integer> target, HashSet<Integer> intermediate, HashSet<Integer> nodes) throws Exception {
		Scanner scan = new Scanner(new File(edgeFileName));
		PrintWriter pw = new PrintWriter(new File("neuro_networks//celegans_network_clean.txt"));
		int nRemovedInedges = 0;
		int nRemovedOutedges = 0;
		
		while (scan.hasNext()) {
			int src = scan.nextInt();
			int dst = scan.nextInt();
			double weight = scan.nextDouble();
			
			if (!nodes.contains(src) || !nodes.contains(dst)) {
				continue;
			}
			
			if (target.contains(src)) {
				++nRemovedInedges;
			}
			
			if (source.contains(dst)) {
				++nRemovedOutedges;
			}
			
			if (target.contains(src) || source.contains(dst)) {
				continue;
			}
						
			pw.println(src + "\t" + dst);
		}
		
		System.out.println("Removed in-edges " + nRemovedInedges);
		System.out.println("Removed out-edges " + nRemovedOutedges);
		
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
		
		loadNeurons("neuro_networks//celegans_labels.txt");
		
		removeDuplicate(source, intermediate, target, nodes);
		removeDuplicate(intermediate, source, target, nodes);
		removeDuplicate(target, source, intermediate, nodes);
		
		System.out.println("Total nodes: " + nodes.size());
		System.out.println("Sources: " + source.size());
		System.out.println("Intermediate: " + intermediate.size());
		System.out.println("Target: " + target.size());
		
		writeFile("neuro_networks//celegans_graph.txt", source, target, intermediate, nodes);
	}
	
	private static void getLocationColorWeightedHistogram(DependencyDAG dependencyDAG) {
		double binWidth = 0.1;
		int numBin = (int)(1.0 / binWidth) + 2;
		double binKount[] = new double[numBin];
		
		ArrayList< ArrayList<String> > colorValues = new ArrayList();
		for (int i = 0; i < numBin; ++i) {
			colorValues.add(new ArrayList<String>());
		}
		
		for (String s: dependencyDAG.nodes) {
			double loc = dependencyDAG.numPathLocation.get(s);
			int binLocation = -1;
			if (dependencyDAG.isSource(s)) {
				binLocation = 0;
			}
			else if (dependencyDAG.isTarget(s)) {
				binLocation = numBin - 1;
			}
			else {
				binLocation = 1 + (int)(loc / binWidth);
			}
			binKount[binLocation]++;
			
			colorValues.get(binLocation).add(s);
		}
		
//		for (int i = 0; i < numBin; ++i) {
//			System.out.println((i + 1) + "\t" + binKount[i]);
//		}

		int matrixMaxHeight = 106 + 1; // to be computed in first run
		double colorMatrixValue[][] = new double[matrixMaxHeight][numBin];
		String colorMatrixName[][] = new String[matrixMaxHeight][numBin];
		
		int midIndex = matrixMaxHeight / 2;
		for (int i = 0; i < numBin; ++i) {
//			ArrayList<Double> aList = colorValues.get(i);
			TreeMultimap<Double, String> sortedStrings = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
			for (String s: colorValues.get(i)) {
				sortedStrings.put(dependencyDAG.normalizedPathCentrality.get(s), s);
			}
			if (sortedStrings.size() < 1) continue;
			
			ArrayList<Double> aListValue = new ArrayList(sortedStrings.keys());
			ArrayList<String> aListName = new ArrayList(sortedStrings.values());
			int k = 0;
			colorMatrixValue[midIndex + k][i] = aListValue.get(0);
			colorMatrixName[midIndex + k][i] = aListName.get(0);
			++k;
			for (int j = 1; j < aListValue.size(); ++j) {
				colorMatrixValue[midIndex + k][i] = aListValue.get(j);
				colorMatrixName[midIndex + k][i] = aListName.get(j);
				if (j + 1 < aListValue.size()) {
					colorMatrixValue[midIndex - k][i] = aListValue.get(j + 1);
					colorMatrixName[midIndex - k][i] = aListName.get(j + 1);
					++k;
					++j;
				}
				else {
					break;
				}
			}
		}
		
		for (int i = 0; i < matrixMaxHeight; ++i) {
			for (int j = 0; j < numBin; ++j) {
				if (colorMatrixValue[i][j] != 0) {
					double truncated = ((int)colorMatrixValue[i][j] * 1000) / 1000.0;
					System.out.print(idNeuronMap.get(colorMatrixName[i][j]) + " (" + truncated + ")\t");
				}
				else {
					System.out.print(" " + "\t");
				}
			}
			System.out.println();
		}
	}
	
	private static void traverseAllPathsHelper(String node, DependencyDAG dependencyDAG, HashSet<String> pathNodes) {
		if (dependencyDAG.isTarget(node)) {
			numPaths++;
//			for (String s: pathNodes) {
//				System.out.print(s + "\t");
//			}
//			System.out.println();
			System.out.println(numPaths);
			return;
		}
		
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private static void traverseAllPaths() throws Exception {
		DependencyDAG.isCyclic = true;
		
//		String neuroDAGName = "celegans_network_clean";
//		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		dependencyDAG.printNetworkProperties();
		
		DependencyDAG.isToy = true;
		String toyDAGName = "toy_cyclic_2";
		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");

		HashSet<String> pathNodes = new HashSet();
		numPaths = 0;
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.isSource(s)) continue;
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes);
			pathNodes.remove(s);
			System.out.println(s + "\t" + numPaths);
		}
		
		System.out.println("Total paths: " + numPaths);
	}
	
	private static void statisticalRun() throws Exception {
		int nRun = 10;
		
		while (nRun-- >= 0) {
			DependencyDAG.isCyclic = true;
			String neuroDAGName = "celegans_network_clean";
			DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
			
			String netID = "neuro_network";
			neuroDependencyDAG.printNetworkStat();
			neuroDependencyDAG.printNetworkProperties();

			CoreDetection.pathCoverageTau = 0.999;
			CoreDetection.fullTraverse = false;
			CoreDetection.getCore(neuroDependencyDAG, netID);
			double coreSize = CoreDetection.minCoreSize;
	
			neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
			FlattenNetwork.makeAndProcessFlat(neuroDependencyDAG);
			CoreDetection.hScore = (1.0 - ((coreSize - 1) / FlattenNetwork.flatNetworkCoreSize));
			System.out.println(coreSize + "\t" + CoreDetection.hScore);
		}
	}
	
	private static void doNeuroNetworkAnalysis() throws Exception {
		DependencyDAG.isCyclic = true;
		String neuroDAGName = "celegans_network_clean";
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		
		String netID = "neuro_network";
//		neuroDependencyDAG.printNetworkStat();
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();

//		DistributionAnalysis.getPathLength(neuroDependencyDAG);
//		CoreDetection.getCentralEdgeSubgraph(neuroDependencyDAG);
		
//		Visualization.printDOTNetwork(neuroDependencyDAG);
		CoreDetection.pathCoverageTau = 1.0;
//		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(neuroDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;
//
//		neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(neuroDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}
	
	public static void main(String[] args) throws Exception {
//		getCleanNeuroNetwork();
		doNeuroNetworkAnalysis();
//		statisticalRun();
//		traverseAllPaths();
	}
}
