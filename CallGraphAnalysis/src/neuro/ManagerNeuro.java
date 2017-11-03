package neuro;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlatNetwork;

public class ManagerNeuro {
	public static HashMap<String, String> idNeuronMap = new HashMap();
	public static double numPaths = 0;
	public static HashSet<String> source = new HashSet();
	public static HashSet<String> intermediate = new HashSet();
	public static HashSet<String> target = new HashSet();
	public static HashSet<String> nodes = new HashSet();
//	public static HashSet<String> dualNodes = new HashSet();
	public static HashMap<String, Integer> nodeVariance = new HashMap();
	public static HashMap<Double, Integer> hscoreVariance = new HashMap();
	public static HashMap<String, Integer> coreVarianceMembers = new HashMap();
	public static HashMap<String, Double> hscoreCoreVarianceMembers = new HashMap();
	public static HashMap<String, Double> locationVariance = new HashMap();
	public static HashMap<String, Double> pcenVariance = new HashMap();
	public static HashMap<String, Integer> coreVarianceOrdered = new HashMap();
	public static double restoredEdgesVariance[];
	public static Random random  = new Random(System.nanoTime());
	public static int nRun = 1;
	public static int method = 1;
	public static int maxEdgeBack = 106;
	public static int maxKount;
	public static HashSet<String> maxRedundantOrder;
	
	private static void loadNodes(HashSet<String> nodes, HashSet<String> typeNode, String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			String i = scan.next();
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
	
	private static void removeDuplicate(HashSet<String> from, HashSet<String> to1, HashSet<String> to2, HashSet<String> nodes) {
		HashSet<String> toRemove = new HashSet();
		for (String i: from) {
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
	
	private static int getTypeIndex(String node) {
		if (source.contains(node)) return 0;
		if (intermediate.contains(node)) return 1;
		return 2;
	}
	
	public static String getType(String node) {
		if (source.contains(node)) return "sensory";
		if (intermediate.contains(node)) return "inter";
		return "motor";
	}
	
	private static void writeFile(String edgeFileName, HashSet<String> source, HashSet<String> target, HashSet<String> intermediate, HashSet<String> nodes) throws Exception {
		Scanner scan = new Scanner(new File(edgeFileName));
		PrintWriter pw = new PrintWriter(new File("neuro_networks//celegans_network_clean.txt"));
		int nRemovedInedge = 0;
		int nRemovedOutedge = 0;
		int totalEdge = 0;
		HashMap<String, Integer> indeg = new HashMap();
		HashMap<String, Integer> outdeg = new HashMap();
		HashSet<String> retainedNode = new HashSet();
		HashMap<String, Integer> forwardEdges = new HashMap();
		HashMap<String, Integer> backwardEdges = new HashMap();
		
//		System.out.println("Sizes " + nodes.size() + "\t" + source.size() + "\t" + intermediate.size() + "\t" + target.size());
//		int edgeConsidered = 0;
		int dualNodeRemovedEdge = 0;
		int totalRemovedEdge = 0;
		int a[][] = new int[3][3];
		double feedForwardCount = 0;
		double feedBackCount = 0;
		double feedForwardSum = 0;
		double feedBackSum = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			double weight = scan.nextDouble();
			
			int srcType = getTypeIndex(src);
			int dstType = getTypeIndex(dst);
			a[srcType][dstType]++;
			
			if (!nodes.contains(src) || !nodes.contains(dst)) {
				++dualNodeRemovedEdge;
				continue;
			}
			
			if (target.contains(src) && (intermediate.contains(dst) || source.contains(dst))) {
//				++nRemovedOutedge;
//				System.out.println(target.contains(dst) + "\t" + source.contains(dst) + "\t" + intermediate.contains(dst));
				feedBackSum += weight;
				feedBackCount++;
				backwardEdges.put(src + "," + dst, (int)weight);
				continue;
			}
			
			if (intermediate.contains(src) && source.contains(dst)) {
//				++nRemovedInedge;
//				System.out.println(target.contains(src) + "\t" + source.contains(src) + "\t" + intermediate.contains(src));
				feedBackSum += weight;
				feedBackCount++;
				backwardEdges.put(src + "," + dst, (int)weight);
				continue;
			}
			
			if (target.contains(src) || source.contains(dst)) {
//				++totalRemovedEdge;
			}
			
			if (target.contains(src) || source.contains(dst)) {
//				System.out.println(src + "\t" + dst);
//				continue;
			}
			
			feedForwardSum += weight;
			feedForwardCount++;
			forwardEdges.put(src + "," + dst, (int)weight);
						
			pw.println(src + "\t" + dst);
			++totalEdge;
			retainedNode.add(src);
			retainedNode.add(dst);
			
			if (indeg.containsKey(dst)) {
				indeg.put(dst, indeg.get(dst) + 1);
			}
			else {
				indeg.put(dst, 1);
			}
			
			if (outdeg.containsKey(src)) {
				outdeg.put(src, outdeg.get(src) + 1);
			}
			else {
				outdeg.put(src, 1);
			}
		}
		
//		System.out.println("Removed in-edges " + nRemovedInedge);
//		System.out.println("Removed out-edges " + nRemovedOutedge);
//		System.out.println("Removed total edge" + totalRemovedEdge);
		System.out.println("Total edge " + totalEdge);
		System.out.println("Total node " + retainedNode.size());
//		System.out.println("Dual Node Removed Edge " + dualNodeRemovedEdge);
//		System.out.println((feedForwardSum/feedForwardCount) + "\t" + (feedBackSum/feedBackCount));
		
		HashMap<Integer, Integer> diffFrequencey = new HashMap();
		for (String s: forwardEdges.keySet()) {
			String src = s.substring(0, s.indexOf(","));
			String tgt = s.substring(s.indexOf(",") + 1);
			String rev = tgt + "," + src;
			if (backwardEdges.containsKey(rev)) {
//				System.out.println("Weight diff: " + "\t" + (forwardEdges.get(s) - backwardEdges.get(rev)));
				int diffWeight = forwardEdges.get(s) - backwardEdges.get(rev);
				if (diffFrequencey.containsKey(diffWeight)) {
					diffFrequencey.put(diffWeight, diffFrequencey.get(diffWeight) + 1);
				}
				else {
					diffFrequencey.put(diffWeight, 1);
				}
			}
		}
		for (int i: diffFrequencey.keySet()) {
			System.out.println(i + "\t" + diffFrequencey.get(i));
		}
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				System.out.print(a[i][j] + "\t");
			}
			System.out.println();
		}
		
		int disS = 0;
		int disT = 0;
		int disI = 0;
		int isolated = 0;
		for (String i: nodes) {
			if (source.contains(i) && !outdeg.containsKey(i)) {
				++disS;
			}
			else if (target.contains(i) && !indeg.containsKey(i)) {
				++disT;
//				System.out.println(i + "\t" + outdeg.containsKey(i) + "\t" + disT);
//				if (retainedNode.contains(i)) {
//					System.out.println("1node " + i + "\t" + indeg.containsKey(i) + "\t" + outdeg.containsKey(i));
//				}
			}
			else if (intermediate.contains(i) && (!indeg.containsKey(i) || !outdeg.containsKey(i))) {
				++disI;
//				if (!indeg.containsKey(i) && outdeg.containsKey(i)) System.out.println("Inter no in " + i);
//				if (!outdeg.containsKey(i) && indeg.containsKey(i)) System.out.println("Inter no out " + i);
//				if (!outdeg.containsKey(i) && !indeg.containsKey(i)) System.out.println("Inter no in/out " + i);
			}
			
			if (!indeg.containsKey(i) && !outdeg.containsKey(i)) {
				++isolated;
//				if (source.contains(i)) System.out.println("isolated source");
//				if (target.contains(i)) System.out.println("isolated target");
//				if (intermediate.contains(i)) System.out.println("isolated intermediate");
			}
			
			if (source.contains(i)) {
//				System.out.println(i);
			}
		}
		
//		System.out.println("Disconnected nodes " + disS + "\t" + disI + "\t" + disT);
//		System.out.println("Isolated " + isolated);
		scan.close();
		pw.close();
	}
	
	public static void loadNeuroMetaNetwork() throws Exception {
		loadNodes(nodes, source, "neuro_networks//sensory_neurons.txt");
		loadNodes(nodes, intermediate, "neuro_networks//inter_neurons.txt");
		loadNodes(nodes, target, "neuro_networks//motor_neurons.txt");
		loadNeurons("neuro_networks//celegans_labels.txt");
	}
	
	public static void getCleanNeuroNetwork() throws Exception {		
		loadNodes(nodes, source, "neuro_networks//sensory_neurons.txt");
		loadNodes(nodes, intermediate, "neuro_networks//inter_neurons.txt");
		loadNodes(nodes, target, "neuro_networks//motor_neurons.txt");
		
		int maxLabel = 279;
		for (int i = 1; i <= maxLabel; ++i) {
			int k = 0;
			if (source.contains(i)) ++k;
			if (intermediate.contains(i)) ++k;
			if (target.contains(i)) ++k;
			if (k > 1) {
//				System.out.println(i + "\t" + k);
//				System.out.println(source.contains(i) + "\t" + intermediate.contains(i) + "\t" + target.contains(i));
			}
//			dualNodes.add(Integer.toString(i));
		}
		
		loadNeurons("neuro_networks//celegans_labels.txt");
		
		/* temporary turn off */
		/*
		removeDuplicate(source, intermediate, target, nodes);
		removeDuplicate(intermediate, source, target, nodes);
		removeDuplicate(target, source, intermediate, nodes);
		*/
		removeDuplicate(target, source, source, nodes); // only removing dual definition source-target nodes
		
		System.out.println("Total nodes: " + nodes.size());
		System.out.println("Sources: " + source.size());
		System.out.println("Intermediate: " + intermediate.size());
		System.out.println("Target: " + target.size());
		
		writeFile("neuro_networks//celegans_graph.txt", source, target, intermediate, nodes);
		writeSourceTarget();
	}
	
	private static void writeSourceTarget() throws Exception {
		PrintWriter pw = new PrintWriter(new File("neuro_networks//celegans_sources.txt"));
		for (String s: source) {
			pw.println(s);
		}
		pw.close();
		
		pw = new PrintWriter(new File("neuro_networks//celegans_targets.txt"));
		for (String s: target) {
			pw.println(s);
		}
		pw.close();
	}
	
	
	/*
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
			FlatNetwork.makeAndProcessFlat(neuroDependencyDAG);
			CoreDetection.hScore = (1.0 - (coreSize / FlatNetwork.flatNetworkCoreSize));
			System.out.println(coreSize + "\t" + CoreDetection.hScore);
		}
	}
	*/
	
	private static void getWeightedNetwork() throws Exception {
		Scanner scanner = new Scanner(new File("neuro_networks//celegans_graph.txt"));
		HashMap<String, String> weights = new HashMap();
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			String wgt = scanner.next();
			weights.put(src + "," + tgt, wgt);
		}
		scanner.close();
		PrintWriter pw = new PrintWriter(new File("neuro_networks//celegans.socialrank.network.weighted"));
		scanner = new Scanner(new File("neuro_networks//celegans.socialrank.network"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			String wgt = "1.00";
			if (weights.containsKey(src + "," + tgt)) {
				wgt = weights.get(src + "," + tgt);
			}
			pw.println(src + " " + tgt + " " + wgt);
		}
		pw.close();
		scanner.close();
	}
	
	private static void reduceNetwork_2() throws Exception {
		loadNeuroMetaNetwork();
//		DependencyDAG.isCelegans = true;
		DependencyDAG.isToy = true;
		String netPath = "toy_networks//toy_cyclic_2.txt";
//		String netPath = "neuro_networks//celegans_full.txt";
//		String netPath = "metabolic_networks//rat-links.txt";
		DependencyDAG neuroDependencyDAG = new DependencyDAG(netPath);
				
//		neuroDependencyDAG.printNetworkProperties();
//		neuroDependencyDAG.printNetworkStat();		
		int originSTPair = neuroDependencyDAG.connectedSourceTargetPair;
		
		class CouplingCentralitySorted implements Comparable<CouplingCentralitySorted> {
			String name;
			int couplingCentrality;
			int totalDegree;
			
			CouplingCentralitySorted(String node, int couplingCentrality, int totalDegree) {
				this.name = node;
				this.couplingCentrality = couplingCentrality;
				this.totalDegree = totalDegree;
			}
			
			@Override
			public int compareTo(CouplingCentralitySorted another) {
				if (this.couplingCentrality < another.couplingCentrality) return -1;
				else if (this.couplingCentrality > another.couplingCentrality) return 1;
				else {
					if (this.totalDegree < another.totalDegree) return -1;
					else if (this.totalDegree > another.totalDegree) return 1;
					else return 0;
				}
			}
		}
		
		HashSet<String> removed = new HashSet();
		while (true) {	
			TreeSet<CouplingCentralitySorted> ts = new TreeSet();
			for (String s: neuroDependencyDAG.nodes) {
				if (neuroDependencyDAG.isSource(s) || neuroDependencyDAG.isTarget(s)) continue;
				int couplintCentrality = neuroDependencyDAG.sourcesReachableKount.get(s) * neuroDependencyDAG.targetsReachableKount.get(s);
				int totalDegree = neuroDependencyDAG.inDegree.get(s) + neuroDependencyDAG.outDegree.get(s);
				ts.add(new CouplingCentralitySorted(s, couplintCentrality, totalDegree));
			}
			
			String toRemove = "";
			DependencyDAG tempDAG = new DependencyDAG(netPath);
			for (String r: removed) {
				tempDAG.removeNode(r);
			}
			for (CouplingCentralitySorted node: ts) {	
				HashSet<String> serves;
				if (tempDAG.serves.containsKey(node.name)) serves = new HashSet(tempDAG.serves.get(node.name));
				else serves = new HashSet();
				HashSet<String> depends;
				if (tempDAG.depends.containsKey(node.name)) depends = new HashSet(tempDAG.depends.get(node.name));
				else depends = new HashSet();
				
				tempDAG.removeNode(node.name);
				tempDAG.reload();
				
				if (tempDAG.connectedSourceTargetPair == originSTPair) {
					toRemove = node.name;
					removed.add(toRemove);
					break;
				}
				
				for (String r: serves) {
					tempDAG.addEdge(node.name, r);
				}
				for (String r: depends) {
					tempDAG.addEdge(r, node.name);
				}
			}
			
			if (toRemove.equals("")) break;
			System.out.println(toRemove);
			neuroDependencyDAG.removeNodeAndReload(toRemove);
		}
		
		System.out.println(removed);
	}
	
	private static void reduceNetwork_1() throws Exception {
		loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		DependencyDAG.isToy = true;
//		String netID = "celegans";
//		String netPath = "toy_networks//toy_dag_paper.txt";
		String netPath = "neuro_networks//celegans_full.txt";
//		String netPath = "metabolic_networks//rat-links.txt";
		DependencyDAG neuroDependencyDAG = new DependencyDAG(netPath);
		
		
//		neuroDependencyDAG.printNetworkProperties();
//		neuroDependencyDAG.printNetworkStat();
		
		int originSTPair = neuroDependencyDAG.connectedSourceTargetPair;
		
//		System.out.println(originSTPair);
		ArrayList<String> redundant = new ArrayList();
		/*
		HashSet<String> tempNodes = new HashSet(neuroDependencyDAG.nodes);
		for (String s: tempNodes) {
			DependencyDAG tempDAG = new DependencyDAG(netPath);
			tempDAG.removeNodeAndReload(s);
//			if (tempDAG.connectedSourceTargetPair >= originSTPair) {
//				System.out.println(s);
//			}
//			System.out.println(s + "\t" + tempDAG.connectedSourceTargetPair);
//			tempDAG.printNetworkProperties();
			if (tempDAG.connectedSourceTargetPair == originSTPair) {
				redundant.add(s);
			}
		}
		*/
		Scanner scanner = new Scanner(new File("neuro_networks//coupling_redundant.txt"));
		while (scanner.hasNext()) {
			redundant.add(scanner.next());
		}
		scanner.close();
		
		Collections.shuffle(redundant);
		int kount = 0;
		HashSet<String> redundantOrder = new HashSet();
		for (String s: redundant) {
			neuroDependencyDAG.removeNodeAndReload(s);
			if (neuroDependencyDAG.connectedSourceTargetPair < originSTPair) {
				break;
			}
			redundantOrder.add(s);
			++kount;
		}
		if (kount > maxKount) {
			maxKount = kount;
			maxRedundantOrder = new HashSet(redundantOrder);
		}
//		neuroDependencyDAG.reload();
//		System.out.println(kount + "\t" + neuroDependencyDAG.connectedSourceTargetPair);
	}	
	
	private static void doNeuroNetworkAnalysis_2() throws Exception {
		loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		DependencyDAG.isToy = true;
		String netID = "celegans";
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//celegans_full.txt");
//		DependencyDAG neuroDependencyDAG = new DependencyDAG("toy_networks//toy_dag_paper.txt");
//		DependencyDAG neuroDependencyDAG = new DependencyDAG("metabolic_networks//rat-links.txt");
		
		
//		int disconnectedInterNeurons[] = {4,5,7,9,19,20,21,22,34,36,44,53,85,93,125,272};
//		for (int i: disconnectedInterNeurons) {
//			CoreDetection.topRemovedWaistNodes.add(Integer.toString(i));
//		}
		
		Scanner scanner = new Scanner(new File("neuro_networks//coupling_redundant_ari.txt"));
		while (scanner.hasNext()) {
			String s = scanner.next();
			neuroDependencyDAG.removeNode(s);
		}
		neuroDependencyDAG.reload();
		scanner.close();
		
		neuroDependencyDAG.printNetworkProperties();
		neuroDependencyDAG.printNetworkStat();
//		DistributionAnalysis.getDistributionCCDF(neuroDependencyDAG, netID, 1);
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();
//		DistributionAnalysis.printAllCentralities(neuroDependencyDAG, netID);

//		DistributionAnalysis.getPathLength(neuroDependencyDAG);
//		CoreDetection.getCentralEdgeSubgraph(neuroDependencyDAG);
//		DistributionAnalysis.getDistributionCCDF(neuroDependencyDAG, netID, 1);
		
//		Visualization.printDOTNetwork(neuroDependencyDAG);
//		CoreDetection.pathCoverageTau = 1.0;
//		CoreDetection.fullTraverse = false;
//		CoreDetection.getCore(neuroDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;

//		neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		FlatNetwork.makeAndProcessFlat(neuroDependencyDAG);
//		CoreDetection.hScore = (1.0 - (realCore / FlatNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}

	private static void doNeuroNetworkAnalysis_1() throws Exception {
//		DependencyDAG.isCyclic = true;
//		String neuroDAGName = "celegans_network_clean";
//		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		
//		getWeightedNetwork();
		loadNeuroMetaNetwork();
		DependencyDAG.isToy = true;
		DependencyDAG.isCelegans = true;
		String netID = "celegans";
//		DependencyDAG.isWeighted = true;
//		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//celegans.socialrank.network");
//		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//celegans.socialrank.network.weighted");
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//toy_links.txt");
		
		
//		int disconnectedInterNeurons[] = {4,5,7,9,19,20,21,22,34,36,44,53,85,93,125,272};
//		for (int i: disconnectedInterNeurons) {
//			CoreDetection.topRemovedWaistNodes.add(Integer.toString(i));
//		}
		
//		neuroDependencyDAG.printNetworkStat();
		neuroDependencyDAG.printNetworkProperties();
//		DistributionAnalysis.getDistributionCCDF(neuroDependencyDAG, netID, 1);
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();
//		DistributionAnalysis.printAllCentralities(neuroDependencyDAG, netID);

//		DistributionAnalysis.getPathLength(neuroDependencyDAG);
//		CoreDetection.getCentralEdgeSubgraph(neuroDependencyDAG);
//		DistributionAnalysis.getDistributionCCDF(neuroDependencyDAG, netID, 1);
		
//		Visualization.printDOTNetwork(neuroDependencyDAG);
		/*
		CoreDetection.pathCoverageTau = 0.98;
		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(neuroDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

//		neuroDependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
		FlatNetwork.makeAndProcessFlat(neuroDependencyDAG);
		CoreDetection.hScore = (1.0 - (realCore / FlatNetwork.flatNetworkCoreSize));
		System.out.println("[h-Score] " + CoreDetection.hScore);
		*/
	}
	
	private static void optimizeAcyclicity() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG.isCelegans = true;
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//celegans.socialrank.network");
		
//		Scanner scanner = new Scanner(new File("toy_networks//violation_edge_1.txt"));
//		int totalRemoved = 0;
//		int causeCycle = 0;
//		while (scanner.hasNext()) {
//			String substrate = scanner.next();
//			String product = scanner.next();
//			dependencyDAG.loadReachability(product);
////			System.out.println(substrate + "\t" + product);
////			System.out.println(dependencyDAG.successors.get(product));
//			if (dependencyDAG.successors.get(product).contains(substrate)) {
//				++causeCycle;
//			}
//			++totalRemoved;
//		}
//		scanner.close();
//		System.out.println(totalRemoved + "\t" + causeCycle);
		
		int restoredEdge = 0;
		Scanner scanner = new Scanner(new File("neuro_networks//violation_edge_2.txt"));
		ArrayList<String> violationEdges = new ArrayList();
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();
			violationEdges.add(substrate + "#" + product);
		}
		Collections.shuffle(violationEdges);
		for (String s: violationEdges) {
			String substrate = s.substring(0, s.indexOf("#"));
			String product = s.substring(s.indexOf("#") + 1);
			dependencyDAG.loadReachability(product);
			if (!dependencyDAG.successors.get(product).contains(substrate)) {
				dependencyDAG.addEdgeAndReload(substrate, product);
				++restoredEdge;
//				System.out.println(substrate + "\t" + product);
			}
			else {
				System.out.println(substrate + "\t" + product);
			}
		}
		scanner.close();
		
		System.out.println(restoredEdge);
	}
	
	private static void optimizeAcyclicityNeuro() throws Exception {
		DependencyDAG.resetFlags();
		loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		String netPath = "neuro_networks//celegans";
		String netID = "celegans";
//		String netPath = "metabolic_networks//rat";
//		String netID = "rat";
		DependencyDAG.isToy = true;
		DependencyDAG neuroDependencyDAG = new DependencyDAG(netPath + ".socialrank.network");
		
		HashMap<String, Integer> idRankMap = new HashMap();
		HashMap<String, String> idLabelMap = new HashMap();
		Scanner scanner = new Scanner(new File(netPath + ".nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		scanner = new Scanner(new File(netPath + ".ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
		}
		scanner.close();
		
		int totalRemoved = 0;
		int causeCycle = 0;
		scanner = new Scanner(new File(netPath + ".edges"));
		TreeMap<Integer, HashSet<String>> agonySorted = new TreeMap();
		HashMap<Integer, Integer> agonyHist = new HashMap();
//		Random random = new Random(System.nanoTime());
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();
			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			if (substrateRank >= productRank) {
				if (substrateRank == productRank) System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				String start = idLabelMap.get(substrate);
				String end = idLabelMap.get(product);
				neuroDependencyDAG.loadReachability(end);
//				System.out.println(substrateRank - productRank + 1);
//				System.out.println(neuroDependencyDAG.successors.get(end));
				if (neuroDependencyDAG.successors.get(end).contains(start)) {
					++causeCycle;
				}
				++totalRemoved;
				
				int agony = substrateRank - productRank + 1;
				if (method == 2) {
					agony *= -1;
				}
				if (method == 3) {
					agony = random.nextInt(1000);
				}
				if (agonySorted.containsKey(agony)) {
					agonySorted.get(agony).add(substrate + "," + product);
				}
				else {
					HashSet<String> hs = new HashSet();
					hs.add(substrate + "," + product);
					agonySorted.put(agony, hs);
				}
				
				if (agonyHist.containsKey(agony)) {
					agonyHist.put(agony, agonyHist.get(agony) + 1);
				}
				else {
					agonyHist.put(agony, 1);
				}
			}
		}
		scanner.close();
		for (int key: agonyHist.keySet()) {
//			System.out.println(key + "\t" + agonyHist.get(key));
		}
		
		int restoredEdge = 0;
		HashSet<String> restoredEdgeSet = new HashSet();
		for (int agony: agonySorted.keySet()) {
			ArrayList<String> alist = new ArrayList(agonySorted.get(agony));
			Collections.shuffle(alist, random);
			for (String s: alist) {
				int index = s.indexOf(",");
				String substrate = s.substring(0, index);
				String product = s.substring(index + 1);
				String start = idLabelMap.get(substrate);
				String end = idLabelMap.get(product);
				neuroDependencyDAG.loadReachability(end);
				if (!neuroDependencyDAG.successors.get(end).contains(start)) {
					// no cycle -- edge restored
					neuroDependencyDAG.addEdgeAndReload(start, end);
					++restoredEdge;
					restoredEdgeSet.add(start + ", " + end);
					
//					CoreDetection.pathCoverageTau = 0.98;
//					CoreDetection.fullTraverse = false;
//					CoreDetection.getCore(neuroDependencyDAG, netID);
//					double realCore = CoreDetection.minCoreSize;
//					FlatNetwork.makeAndProcessFlat(neuroDependencyDAG);
//					CoreDetection.hScore = (1.0 - (realCore / FlatNetwork.flatNetworkCoreSize));
//					for (String r: CoreDetection.realCores) {
//						System.out.println(r + "\t" + restoredEdge);
//					}
				}
			}
		}
		
		if (restoredEdge >= maxEdgeBack) {
//			System.out.println(restoredEdge);
			for (String s: restoredEdgeSet) {
//				System.out.println(s);
			}
		}
		
		if (true) return;
		
//		System.out.println(totalRemoved + "\t" + causeCycle + "\t" + restoredEdge);
//		neuroDependencyDAG.printNetworkProperties();
		CoreDetection.pathCoverageTau = 0.96;
		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(neuroDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;
		HashMap<String, Double> realLocation = new HashMap(neuroDependencyDAG.numPathLocation);
//		FlatNetwork.makeAndProcessFlat(neuroDependencyDAG);
//		CoreDetection.hScore = (1.0 - (realCore / FlatNetwork.flatNetworkCoreSize));
		for (String r: CoreDetection.realCores) {
			if (nodeVariance.containsKey(r)) {
				nodeVariance.put(r, nodeVariance.get(r) + 1);
			}
			else {
				nodeVariance.put(r, 1);
			}
			
			if (locationVariance.containsKey(r)) {
				locationVariance.put(r, (locationVariance.get(r) + realLocation.get(r)) * 0.5);
			}
			else {
				locationVariance.put(r, realLocation.get(r));
			}
		}
		
		for (String r: neuroDependencyDAG.nodes) {
			if (pcenVariance.containsKey(r)) {
				pcenVariance.put(r, (pcenVariance.get(r) + neuroDependencyDAG.normalizedPathCentrality.get(r)) * 0.5);
			}
			else {
				pcenVariance.put(r, neuroDependencyDAG.normalizedPathCentrality.get(r));
			}
		}
		
		if (hscoreVariance.containsKey(CoreDetection.hScore)) {
			hscoreVariance.put(CoreDetection.hScore, hscoreVariance.get(CoreDetection.hScore) + 1);
		}
		else {
			hscoreVariance.put(CoreDetection.hScore, 1);
		}
		
		restoredEdgesVariance[nRun] = restoredEdge;
		
//		System.out.println(realCore + "\t" + CoreDetection.hScore);
		String members = "";
		String rankOrdered[] = new String[CoreDetection.realCores.size()];
		for (String r: CoreDetection.realCores) {
			members += r + "#";
			int index = CoreDetection.averageCoreRank.get(r).intValue();
			rankOrdered[index - 1] = r;
		}
		members = members.substring(0, members.length() - 1);
		if (coreVarianceMembers.containsKey(members)) {
			coreVarianceMembers.put(members, coreVarianceMembers.get(members) + 1);
		}
		else {
			coreVarianceMembers.put(members, 1);
		}
		
		if (hscoreCoreVarianceMembers.containsKey(members)) {
			hscoreCoreVarianceMembers.put(members, (CoreDetection.hScore + hscoreCoreVarianceMembers.get(members)) * 0.5);
		}
		else {
			hscoreCoreVarianceMembers.put(members, CoreDetection.hScore);
		}
		
		String ordered = rankOrdered[0];
		for (int i = 1; i < rankOrdered.length; ++i) {
			ordered += "\t" + rankOrdered[i];
		}
		if (coreVarianceOrdered.containsKey(ordered)) {
			coreVarianceOrdered.put(ordered, coreVarianceOrdered.get(ordered) + 1);
		}
		else {
			coreVarianceOrdered.put(ordered, 1);
		}
	}
	
	private static void randomSimulations() throws Exception {
		random = new Random(System.nanoTime());
		nRun = 100;
		maxKount = 0;
		
		/*
		restoredEdgesVariance = new double[nRun];
		
		for (int i = 1; i <= 3; ++i) {
			method = i;
			while (--nRun >= 0) {
				optimizeAcyclicityNeuro();
			}
			nRun = 500;
//			System.out.println(StatUtils.mean(restoredEdgesVariance) + "\t" + Math.sqrt(StatUtils.variance(restoredEdgesVariance)));
		}
		
		for (String s: nodeVariance.keySet()) {
//			System.out.println(s + "\t" + nodeVariance.get(s) + "\t" + locationVariance.get(s));
		}
		for (double d: hscoreVariance.keySet()) {
//			System.out.println(d + "\t" + hscoreVariance.get(d));
		}
//		System.out.println("###\n");
		for (String s: hscoreCoreVarianceMembers.keySet()) {
//			System.out.println(s + "\t" + hscoreCoreVarianceMembers.get(s));
		}
		
		
		for (String s: coreVarianceMembers.keySet()) {
//			System.out.println((coreVarianceMembers.get(s) / 900.0) + "\t" + s);
		}
//		System.out.println(" -------- \n");
		for (String s: coreVarianceOrdered.keySet()) {
//			System.out.println((coreVarianceOrdered.get(s) / 300.0) + "\t" + s);
		}
		
//		System.out.println(StatUtils.mean(restoredEdgesVariance) + "\t" + Math.sqrt(StatUtils.variance(restoredEdgesVariance)));
		for (String r: pcenVariance.keySet()) {
//			System.out.println(r + "\t" + pcenVariance.get(r));
		}
		*/
		
		for (int i = 1; i <= nRun; ++i) {
			reduceNetwork_1();
		}
		
		System.out.println(" -- " + maxKount);
		for (String s: maxRedundantOrder) {
			System.out.println(s);
		}
	}
	
	private static void optimizeAcyclicityBasic() throws Exception {
		loadNeuroMetaNetwork();
		DependencyDAG.isCelegans = true;
		DependencyDAG.isToy = true;
		DependencyDAG neuroDependencyDAG = new DependencyDAG("neuro_networks//celegans.socialrank.network");
		
		HashMap<String, Integer> idRankMap = new HashMap();
		HashMap<String, String> idLabelMap = new HashMap();
		Scanner scanner = new Scanner(new File("neuro_networks//celegans.nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		scanner = new Scanner(new File("neuro_networks//celegans.ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
		}
		scanner.close();
		
		int totalRemoved = 0;
		int causeCycle = 0;
		scanner = new Scanner(new File("neuro_networks//celegans.edges"));
		TreeMap<Integer, HashSet<String>> agonySorted = new TreeMap();
		Random random = new Random(System.nanoTime());
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();
			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			if (substrateRank >= productRank) {
//				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				String start = idLabelMap.get(substrate);
				String end = idLabelMap.get(product);
				neuroDependencyDAG.loadReachability(end);
//				System.out.println();
//				System.out.println(neuroDependencyDAG.successors.get(end));
				if (neuroDependencyDAG.successors.get(end).contains(start)) {
					++causeCycle;
				}
				++totalRemoved;
				int agony = substrateRank - productRank + 1;
//				agony *= -1;
				agony = random.nextInt(1000);
				if (agonySorted.containsKey(agony)) {
					agonySorted.get(agony).add(substrate + "," + product);
				}
				else {
					HashSet<String> hs = new HashSet();
					hs.add(substrate + "," + product);
					agonySorted.put(agony, hs);
				}
			}
		}
		scanner.close();
		
		int restoredEdge = 0;
		for (int agony: agonySorted.keySet()) {
			HashSet<String> hs = agonySorted.get(agony);
			for (String s: hs) {
				int index = s.indexOf(",");
				String substrate = s.substring(0, index);
				String product = s.substring(index + 1);
				String start = idLabelMap.get(substrate);
				String end = idLabelMap.get(product);
				neuroDependencyDAG.loadReachability(end);
				if (!neuroDependencyDAG.successors.get(end).contains(start)) {
					// no cycle
					// edge restored
					neuroDependencyDAG.addEdgeAndReload(start, end);
					++restoredEdge;
				}
			}
		}
		
		System.out.println(totalRemoved + "\t" + causeCycle + "\t" + restoredEdge);
	}
	
	private static void getFeedbackAndSimpleCycleRemovedNetwork() throws Exception {
		Scanner scanner = new Scanner(new File("neuro_networks//celegans_network_clean.txt"));
		HashSet<String> edges = new HashSet();
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			edges.add(src + "#" + dst);
		}
		scanner.close();
		
		HashMap<String, Double> weightedEdges = new HashMap();
		scanner = new Scanner(new File("neuro_networks//celegans_graph.txt"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			double wgt = scanner.nextDouble();
			weightedEdges.put(src + "#" + dst, wgt);
		}
		scanner.close();
		
		HashSet<String> remove = new HashSet();
		for (String s: edges) {
			int index = s.indexOf("#");
			String src = s.substring(0, index);
			String dst = s.substring(index + 1);
			String a = src + "#" + dst;
			String b = dst + "#" + src;
			if (edges.contains(dst + "#" + src)) {
				double aW = weightedEdges.get(a);
				double bW = weightedEdges.get(b);
				if (aW < bW) {
					remove.add(a);
				}
				else if (bW < aW) {
					remove.add(b);
				}
			}
		}
		edges.removeAll(remove);
		
		System.out.println(edges.size());
		for (String s: edges) {
			int index = s.indexOf("#");
			String src = s.substring(0, index);
			String dst = s.substring(index + 1);
			System.out.println(src + "\t" + dst);
		}
	}
	
	public static void main(String[] args) throws Exception {
//		getCleanNeuroNetwork();
//		getFeedbackAndSimpleCycleRemovedNetwork();
		
//		doNeuroNetworkAnalysis_1();
//		doNeuroNetworkAnalysis_2();
		
//		reduceNetwork_1();
//		reduceNetwork_2();
	
//		optimizeAcyclicityBasic();
//		optimizeAcyclicityNeuro();
		optimizeAcyclicity();
		
//		randomSimulations();
		
//		statisticalRun();
//		traverseAllPaths();
	}
}
