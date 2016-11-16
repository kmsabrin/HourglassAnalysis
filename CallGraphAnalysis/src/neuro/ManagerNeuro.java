package neuro;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ManagerNeuro {
	public static HashMap<String, String> idNeuronMap = new HashMap();
	
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
		int nRemovedInedges = 0;
		int nRemovedOutedges = 0;
		
		while (scan.hasNext()) {
			int src = scan.nextInt();
			int dst = scan.nextInt();
			double weight = scan.nextDouble();
			
			if (target.contains(src)) {
				++nRemovedInedges;
				continue;
			}
			
			if (source.contains(dst)) {
				++nRemovedOutedges;
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
		
		removeDuplicate(source, intermediate, target);
		removeDuplicate(intermediate, source, target);
		removeDuplicate(target, source, intermediate);
		
		writeFile("neuro_networks//celegans_graph.txt", source, target);
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
	
	private static void doNeuroNetworkAnalysis() throws Exception {
		DependencyDAG.isCyclic = true;
		String neuroDAGName = "celegans_network_clean";
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		
		String netID = "neuro_network";
//		DependencyDAG.printNetworkStat(neuroDependencyDAG);
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();

		CoreDetection.getCentralEdgeSubgraph(neuroDependencyDAG);
		
//		CoreDetection.pathCoverageTau = 0.9999;
//		CoreDetection.fullTraverse = false;
//		CoreDetection.getCore(neuroDependencyDAG, netID);
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
	}
}
