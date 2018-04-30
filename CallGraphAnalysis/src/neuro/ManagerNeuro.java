package neuro;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.StatUtils;

import utilityhg.ShortestPathHourglass;
import utilityhg.TarjanSCC;
import clean.HourglassAnalysis;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlatNetwork;

public class ManagerNeuro {
	public static HashMap<String, String> idNeuronMap = new HashMap();
	public static double numPaths = 0;
	public static HashSet<String> source = new HashSet();
	public static HashSet<String> inter = new HashSet();
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
	public static HashMap<String, Integer> addBackCounter = new HashMap();
	public static HashMap<String, Integer> removedCounter = new HashMap();
	public static double restoredEdgesVariance[];
	public static Random random  = new Random(System.nanoTime());
	public static int nRun = 1;
	public static int method = 1;
	public static int maxEdgeBack = 106;
	public static int maxKount;
	public static HashSet<String> maxRedundantOrder;
	public static HashSet<String> keptEdges;
	public static ArrayList<HashSet<String>> allDAG;
	
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
		if (inter.contains(node)) return 1;
		return 2;
	}
	
	public static String getType(String node) {
		if (source.contains(node)) return "sensory";
		if (inter.contains(node)) return "inter";
		return "motor";
	}
	
	private static void writeFile(String edgeFileName, HashSet<String> source, HashSet<String> target, HashSet<String> intermediate, HashSet<String> nodes) throws Exception {
		Scanner scan = new Scanner(new File(edgeFileName));
		PrintWriter pw = new PrintWriter(new File("neuro_networks//full_fb_clean_links.txt"));
		int nRemovedInedge = 0;
		int nRemovedOutedge = 0;
		int totalEdge = 0;
		HashMap<String, Integer> indeg = new HashMap();
		HashMap<String, Integer> outdeg = new HashMap();
		HashSet<String> retainedNode = new HashSet();
		HashMap<String, Integer> nonBackwardEdges = new HashMap();
		HashMap<String, Integer> backwardEdges = new HashMap();
		HashMap<String, Integer> forwardEdges = new HashMap();
		
//		System.out.println("Sizes " + nodes.size() + "\t" + source.size() + "\t" + intermediate.size() + "\t" + target.size());
//		int edgeConsidered = 0;
		int dualNodeRemovedEdge = 0;
		int totalRemovedEdge = 0;
		int a[][] = new int[3][3];
		double nonBackwardCount = 0;
		double feedBackCount = 0;
		double nonBackwardSum = 0;
		double feedBackSum = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			double weight = scan.nextDouble();
			
			int srcType = getTypeIndex(src);
			int dstType = getTypeIndex(dst);
			a[srcType][dstType]++;
			
			if (!nodes.contains(src) || !nodes.contains(dst)) {
				++dualNodeRemovedEdge; //dual sensory + motor neurons
				System.out.println(src + "\t" + dst);
//				continue;
			}
			
			if (target.contains(src) && (intermediate.contains(dst) || source.contains(dst))) {
//				++nRemovedOutedge;
//				System.out.println(target.contains(dst) + "\t" + source.contains(dst) + "\t" + intermediate.contains(dst));
				feedBackSum += weight;
				feedBackCount++;
				backwardEdges.put(src + "#" + dst, (int)weight);
				continue;
			}
			
			if (intermediate.contains(src) && source.contains(dst)) {
//				++nRemovedInedge;
//				System.out.println(target.contains(src) + "\t" + source.contains(src) + "\t" + intermediate.contains(src));
				feedBackSum += weight;
				feedBackCount++;
				backwardEdges.put(src + "#" + dst, (int)weight);
				continue;
			}
			
			if (target.contains(src) || source.contains(dst)) {
//				++totalRemovedEdge;
			}
			
			if (target.contains(src) || source.contains(dst)) {
//				System.out.println(src + "\t" + dst);
//				continue;
			}
			
			nonBackwardSum += weight;
			nonBackwardCount++;
			nonBackwardEdges.put(src + "," + dst, (int)weight);

			if ((source.contains(src) && (intermediate.contains(dst) || target.contains(dst))) 
					|| (intermediate.contains(src) && target.contains(dst))) {
				forwardEdges.put(src + "," + dst, (int)weight);
			}
			
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
		
		for (String s: nonBackwardEdges.keySet()) {
			String src = s.substring(0, s.indexOf(","));
			String tgt = s.substring(s.indexOf(",") + 1);
			String rev = tgt + "," + src;
			if (backwardEdges.containsKey(rev)) {
//				System.out.println("Weight diff: " + "\t" + (forwardEdges.get(s) - backwardEdges.get(rev)));
				int diffWeight = nonBackwardEdges.get(s) - backwardEdges.get(rev);
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
		
//		System.out.println(backwardEdges.size() + "\t" + forwardEdges.size());
		for (String s : backwardEdges.keySet()) {
//			System.out.println(backwardEdges.get(s));
			System.out.println(s);
		}
		for (String s : forwardEdges.keySet()) {
//			System.out.println(forwardEdges.get(s));
		}
		
		
		/*
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
		*/
		
		scan.close();
		pw.close();
	}
	
	public static void loadNeuroMetaNetwork() throws Exception {
//		loadNodes(nodes, source, "celegans//sensory_neurons.txt");
//		loadNodes(nodes, inter, "celegans//inter_neurons.txt");
//		loadNodes(nodes, target, "celegans//motor_neurons.txt");
		loadNodes(nodes, source, "celegans//sensory_neurons_3.txt");
		loadNodes(nodes, inter, "celegans//inter_neurons_3.txt");
		loadNodes(nodes, target, "celegans//motor_neurons_3.txt");		
		loadNeurons("neuro_networks//celegans_labels.txt");	
	}
	
	public static void getCleanNeuroNetwork() throws Exception {		
//		loadNodes(nodes, source, "neuro_networks//sensory_neurons.txt");
//		loadNodes(nodes, inter, "neuro_networks//inter_neurons.txt");
//		loadNodes(nodes, target, "neuro_networks//motor_neurons.txt");
		
//		int maxLabel = 269;
//		for (int i = 1; i <= maxLabel; ++i) {
//			int k = 0;
//			if (source.contains(i)) ++k;
//			if (inter.contains(i)) ++k;
//			if (target.contains(i)) ++k;
//			if (k > 1) {
//				System.out.println(i + "\t" + k);
//				System.out.println(source.contains(i) + "\t" + intermediate.contains(i) + "\t" + target.contains(i));
//			}
//			dualNodes.add(Integer.toString(i));
//		}
		
//		loadNeurons("neuro_networks//celegans_labels.txt");
		loadNeuroMetaNetwork();
		
		/* temporary turn off 
		removeDuplicate(source, intermediate, target, nodes);
		removeDuplicate(intermediate, source, target, nodes);
		removeDuplicate(target, source, intermediate, nodes);
		*/
//		removeDuplicate(target, source, source, nodes); // only removing dual definition source-target nodes
		// convert inter to source or target
		/*
		for (String s : source) {
			if (inter.contains(s)) {
				inter.remove(s);
			}
		}
		for (String s : target) {
			if (inter.contains(s)) {
				inter.remove(s);
			}
		}
		*/
//		for (String s : inter) {
//			if (source.contains(s)) source.remove(s);
//			if (target.contains(s)) target.remove(s);
//		}
		
		
		System.out.println("Total nodes: " + nodes.size());
		System.out.println("Sources: " + source.size());
		System.out.println("Intermediate: " + inter.size());
		System.out.println("Target: " + target.size());
		
		writeFile("neuro_networks//celegans_graph.txt", source, target, inter, nodes);
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
		if (target.contains(node)) {
			numPaths++;
//			for (String s: pathNodes) {
//				System.out.print(s + "\t");
//			}
//			System.out.println();
//			System.out.println(numPaths);
		}
		
		if (pathNodes.size() > 10) return;
		if (!dependencyDAG.serves.containsKey(node)) return;
		
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
//		DependencyDAG.isCyclic = true;
		
		DependencyDAG.isToy = true;
		String neuroDAGName = "celegans_network_full";
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//" + neuroDAGName + ".txt");
//		dependencyDAG.printNetworkProperties();
//		dependencyDAG.printNetworkStat();
		
//		DependencyDAG.isToy = true;
//		String toyDAGName = "toy_cyclic_2";
//		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");

		loadNeuroMetaNetwork();
		HashSet<String> pathNodes = new HashSet();
		numPaths = 0;
		for (String s: dependencyDAG.nodes) {
			if (!source.contains(s)) continue;
			pathNodes.add(s);
			traverseAllPathsHelper(s, dependencyDAG, pathNodes);
			pathNodes.remove(s);
			System.out.println(s + "\t" + numPaths);
		}
		
		System.out.println("Total paths: " + numPaths);
	}
	
	private static void traverseAlmostShortestPathsHelper(String node, String targetNode, int len, DependencyDAG dependencyDAG, ArrayList<String> pathNodes) {
//		if (pathNodes.size() > len + 1) return; // +1 hop than shortest path
		if (pathNodes.size() > 5) return; // special case length restriction
		
		if (node.equals(targetNode)) {
			for (String s: pathNodes) {
				System.out.print(s + " ");
			}
			System.out.println();
			return;
		}
		
		if (!dependencyDAG.serves.containsKey(node)) return;
		
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			pathNodes.add(s);
			traverseAlmostShortestPathsHelper(s, targetNode, len, dependencyDAG, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private static void traverseAlmostShortestPaths() throws Exception {
		DependencyDAG.isToy = true;
//		String neuroDAGName = "full_fb_clean_links";
		String neuroDAGName = "gap_fb_clean_links";
		DependencyDAG dependencyDAG = new DependencyDAG("celegans//" + neuroDAGName + ".txt");
//		dependencyDAG.printNetworkProperties();
//		dependencyDAG.printNetworkStat();
		
		loadNeuroMetaNetwork();
		/*
		Scanner scanner = new Scanner(new File("celegans//sm_pair_sp_len.txt"));
		while (scanner.hasNext()) {
			String smPair = scanner.next();
			int sp = scanner.nextInt();
			ArrayList<String> nodes = ShortestPathHourglass.splitEdge(smPair);
//			System.out.println(nodes.get(0) + "\t" + nodes.get(1) + "\t" + sp);
			ArrayList<String> pathNodes = new ArrayList();
			pathNodes.add(nodes.get(0));
			traverseAlmostShortestPathsHelper(nodes.get(0), nodes.get(1), sp + 1, dependencyDAG, pathNodes);
//			break;
		}
		scanner.close();
		*/
		
		for (String s : source) {
			for (String r : target) {
				ArrayList<String> pathNodes = new ArrayList();
				pathNodes.add(s);
				traverseAlmostShortestPathsHelper(s, r, -1, dependencyDAG, pathNodes);
			}
		}
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
	
	private static void addEdgeBack(DependencyDAG dependencyDAG, String addBackFile) throws Exception {
		int restoredEdge = 0;
		Scanner scanner = new Scanner(new File("neuro_networks//" + addBackFile + ".txt"));
		ArrayList<String> violationEdges = new ArrayList();
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();
			violationEdges.add(substrate + "#" + product);
		}
		/* v.v.i */
		Collections.shuffle(violationEdges);
		for (String s: violationEdges) {
			String substrate = s.substring(0, s.indexOf("#"));
			String product = s.substring(s.indexOf("#") + 1);
			dependencyDAG.loadReachability(product);
			if (!dependencyDAG.successors.get(product).contains(substrate)) {
//				dependencyDAG.addEdgeAndReload(substrate, product);
				dependencyDAG.addEdge(substrate, product);
				++restoredEdge;
				String add = substrate + "#" + product;
				if (addBackCounter.containsKey(add)) {
					addBackCounter.put(add, addBackCounter.get(add) + 1);
				}
				else {
					addBackCounter.put(add, 1);
				}
				keptEdges.add(add);
			}
			else {
//				System.out.println(substrate + "\t" + product);
			}
		}
		scanner.close();
		maxKount += restoredEdge;
//		System.out.println(restoredEdge);
	}
	
	private static void optimizeAcyclicity() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//celegans.socialrank.network");

//		maxKount = 0;
		keptEdges = new HashSet();
		addEdgeBack(dependencyDAG, "add_back_1");
		addEdgeBack(dependencyDAG, "add_back_2");
		addEdgeBack(dependencyDAG, "add_back_3");
		addEdgeBack(dependencyDAG, "add_back_4");
		addEdgeBack(dependencyDAG, "add_back_5");
		allDAG.add(new HashSet(keptEdges));
		
//		addEdgeBack(dependencyDAG, "add_back_combined");
//		addEdgeBack(dependencyDAG, "add_back_majority");
		
//		dependencyDAG.printLinks("celegans");
//		HourglassAnalysis hourglassAnalysis = new HourglassAnalysis();
//		hourglassAnalysis.runAnalysis("celegans");
		/*
		for (String s: hourglassAnalysis.savedCores) {
			if (coreVarianceMembers.containsKey(s)) {
				coreVarianceMembers.put(s, coreVarianceMembers.get(s) + 1);
			}
			else {
				coreVarianceMembers.put(s, 1);
			}
		}
		*/
//		System.out.println(maxKount);
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
		nRun = 10;
		maxKount = 0;
		allDAG = new ArrayList();
		
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
			System.out.println((coreVarianceMembers.get(s) / 900.0) + "\t" + s);
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
		
		HashSet<String> allKeptEdges = new HashSet();
		for (int i = 1; i <= nRun; ++i) {
//			reduceNetwork_1();
//			optimizeAcyclicity();
			breakSCCs();
			
//			DependencyDAG.isToy = true;
//			DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//celegans_network_clean_2.txt");
//			keptEdges = new HashSet();
//			addEdgeBack(dependencyDAG, "add_back_y");
//			allKeptEdges.addAll(keptEdges);
		}
//		System.out.println(allKeptEdges.size());
		
//		System.out.println(" -- " + maxKount);
//		for (String s: maxRedundantOrder) {
//			System.out.println(s);
//		}
		
		for (String s: coreVarianceMembers.keySet()) {
			System.out.println(s + "\t" + (coreVarianceMembers.get(s) * 1.0 / nRun));
		}
		
		for (String s: addBackCounter.keySet()) {
//			System.out.println(s + "\t" + (addBackCounter.get(s) * 1.0 / nRun));
//			int index = s.indexOf("#");
//			System.out.println(s.substring(0, index) + "\t" + s.substring(index + 1) + "\t" + addBackCounter.get(s));
		}
		
		for (String s: removedCounter.keySet()) {
			System.out.println(s + "\t" + (removedCounter.get(s) * 1.0 / nRun));
		}
		
		// find Majority DAG
//		boolean used[] = new boolean[nRun];
//		for (int i = 0; i < allDAG.size(); ++i) {
////			if (used[i]) continue;
//			used[i] = true;
//			int knt = 1;
//			for (int j = i + 1; j < allDAG.size(); ++j) {
//				if (sameDAG(allDAG.get(i), allDAG.get(j))) {
//					++knt;
//					used[j] = true;
//				}
//			}
//			System.out.println(i + "\t" + knt);
//		}
	}
	
	private static boolean sameDAG(HashSet<String> a, HashSet<String> b) {
		double knt = 0;
//		if (a.size() != b.size()) return false;
		for (String s: a) {
			if (!b.contains(s)) {
//				System.out.println("not found: " + s);
//				return false;
				++knt;
			}
		}
		if (knt > a.size() * 0.1) return false;
		return true;
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
	
	
	private static boolean isSCC(HashSet<String> scc, DependencyDAG dependencyDAG) {
		for (String s: scc) {
			dependencyDAG.visited = new HashSet();
			dependencyDAG.reachableUpwardsNodes(s);
			for (String r: scc) {
				if (!dependencyDAG.visited.contains(r)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void getAllShortestDistance(int nNodes, double dist[][], DependencyDAG dependencyDAG, boolean upward) {
		for (int i = 0; i < nNodes; ++i) {
			for (int j = 0; j < nNodes; ++j) {
				dist[i][j] = Double.MAX_VALUE;
			}
		}
		
		for (int i = 0; i < nNodes; ++i) {
			dist[i][i] = 0.0;
		}
		
		if (upward) {
			for (String s : dependencyDAG.nodes) {
				if (!dependencyDAG.serves.containsKey(s))
					continue;
				for (String r : dependencyDAG.serves.get(s)) {
					int si = Integer.parseInt(s);
					int ri = Integer.parseInt(r);
					dist[si][ri] = 1.0;
				}
			}
		} 
		else {
			for (String s : dependencyDAG.nodes) {
				if (!dependencyDAG.depends.containsKey(s))
					continue;
				for (String r : dependencyDAG.depends.get(s)) {
					int si = Integer.parseInt(s);
					int ri = Integer.parseInt(r);
					dist[si][ri] = 1.0;
				}
			}
		}
		
		for (int k = 0; k < nNodes; ++k) {
			for (int i = 0; i < nNodes; ++i) {
				for (int j = 0; j < nNodes; ++j) {
					if (dist[i][j] > dist[i][k] + dist[k][j]) { 
//						dist[i][j] = dist[i][k] + dist[k][j];
					}
				}
			}
		}
		
		for (int i = 0; i < nNodes; ++i) {
			for (int j = 0; j < nNodes; ++j) {
//				System.out.println(i + "\t" + j + "\t" + dist[i][j]);
			}
		}
	}
	
	public static double getAverageShortestDistance(int nNodes, String node, double dist[][], DependencyDAG dependencyDAG, boolean upward) {
		int ni = Integer.parseInt(node);	
		double sum = 0.0;
		double kount = 0;
		double maxDistance = 1000;
		for (int i = 0; i < nNodes; ++i) {
			if (upward) {
				if (!dependencyDAG.serves.containsKey(i) && dist[ni][i] < maxDistance) { // is target and is reachable
					sum += dist[ni][i];
					++kount;
				}
			} 
			else {
				if (!dependencyDAG.depends.containsKey(i) && dist[ni][i] < maxDistance) { // is source and is reachable
					sum += dist[ni][i];
					++kount;
				}
			}
		}
		
		if (sum == 0) System.out.println(node + "\t" + sum + "\t" + kount + "\t" + upward);
		return sum / kount;
	}
	
	public static void breakCycles() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//celegans.edges");
		
		String netPath = "neuro_networks//celegans";
		HashMap<String, String> labelIdMap = new HashMap();
		HashMap<String, String> idLabelMap = new HashMap();
		Scanner scanner = new Scanner(new File(netPath + ".nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			labelIdMap.put(label, id);
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		HashMap<String, Integer> idRankMap = new HashMap();
		scanner = new Scanner(new File(netPath + ".ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
		}
		scanner.close();
		
		HashSet<String> cleanEdge = new HashSet();
		scanner = new Scanner(new File(netPath + ".edges"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			cleanEdge.add(src + "#" + dst);
		}
		scanner.close();
		
		HashMap<String, Double> edgeWeight = new HashMap();
		HashMap<String, Integer> edgeRank = new HashMap();
		scanner = new Scanner(new File(netPath + "_graph.txt"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			double weight = scanner.nextDouble();
			String srcId = labelIdMap.get(src);
			String dstId = labelIdMap.get(dst);
			if (!cleanEdge.contains(srcId + "#" + dstId)) {
				continue;
			}
			int srcRank = idRankMap.get(srcId);
			int dstRank = idRankMap.get(dstId);
			edgeRank.put(srcId + "#" + dstId, dstRank - srcRank);
			edgeWeight.put(srcId + "#" + dstId, weight);
		}
		scanner.close();
		
		class SCCEdge implements Comparable<SCCEdge>{
			String edge;
			int rankDiff;
			double weight;
			double sl;
			double tl;
			
			public SCCEdge(String edge, int rank, double weight, double sl, double tl) {
				this.edge = edge;
				this.rankDiff = rank;
				this.weight = weight;
				this.sl = sl;
				this.tl = tl;
			}
		
			public int compareTo(SCCEdge other) {
				if (this.rankDiff != other.rankDiff) return this.rankDiff - other.rankDiff;
				if (this.weight != other.weight) return (int)(this.weight - other.weight);
				// average shortest path distance
				double thisLocationAgony = this.tl - this.sl;
				double otherLocationAgony = other.tl - other.sl;
				if (thisLocationAgony != otherLocationAgony) return (int)(thisLocationAgony - otherLocationAgony);
				return 0;
			}
		}
		
		// get shortest path based location
		int nNodes = 269;
		double distUp[][] = new double[nNodes][nNodes];
		double distDown[][] = new double[nNodes][nNodes];
		getAllShortestDistance(nNodes, distUp, dependencyDAG, true);
		getAllShortestDistance(nNodes, distDown, dependencyDAG, false);
		
		int kount = 0;
		for (String e: cleanEdge) {
			String src = e.substring(0, e.indexOf("#"));
			String dst = e.substring(e.indexOf("#") + 1);
			int srcRank = idRankMap.get(src);
			int dstRank = idRankMap.get(dst);
			if (srcRank >= dstRank) {
				++kount;
			}
		}
		System.out.println(kount);
		
		
		HashSet<String> processed = new HashSet();
		for (String e: cleanEdge) {
			String src = e.substring(0, e.indexOf("#"));
			String dst = e.substring(e.indexOf("#") + 1);
			if (!cleanEdge.contains(dst + "#" + src)) {
				System.out.println(src + "\t" + dst);
				continue;
			}
			if (processed.contains(src + "#" + dst)) continue;
			processed.add(dst + "#" + src);
			int srcRank = idRankMap.get(src);
			int dstRank = idRankMap.get(dst);
			double currentDirectionWeight = edgeWeight.get(src + "#" + dst);
			double backDirectionWeight = edgeWeight.get(dst + "#" + src);
			double sU = getAverageShortestDistance(nNodes, src, distUp, dependencyDAG, true);
			double sD = getAverageShortestDistance(nNodes, src, distDown, dependencyDAG, false);
			double dU = getAverageShortestDistance(nNodes, dst, distUp, dependencyDAG, true);
			double dD = getAverageShortestDistance(nNodes, dst, distDown, dependencyDAG, false);
			double sL = sU / (sU + sD);
			double dL = dU / (dU + dD);
			double currentDirectionLocationAgony = dL - sL; 
			if (srcRank != dstRank) {
				if (srcRank < dstRank) {
					// keep
					System.out.println(src + "\t" + dst);
				}
				else {
					System.out.println(dst + "\t" + src);
				}
			}
			else {
				if (currentDirectionWeight != backDirectionWeight) {
					// keep
					if (currentDirectionWeight > backDirectionWeight) {
						System.out.println(src + "\t" + dst);
					}
					else {
						System.out.println(dst + "\t" + src);
					}
				}
				else {
					if (currentDirectionLocationAgony != 0) {
						// keep
						if (currentDirectionLocationAgony > 0) {
							System.out.println(src + "\t" + dst);
						}
						else {
							System.out.println(dst + "\t" + src);
						}
					}
					else {
						// keep
						System.out.println(src + "\t" + dst);
						System.out.println(dst + "\t" + src);
//						System.out.println("Still tied for: " + src + "\t" + dst);
					}
				}
			}	
		}
	}
		
	public static void breakSCCs() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG dependencyDAG = new DependencyDAG("neuro_networks//celegans.edges");
		
		String netPath = "neuro_networks//celegans";
		HashMap<String, String> labelIdMap = new HashMap();
		HashMap<String, String> idLabelMap = new HashMap();
		Scanner scanner = new Scanner(new File(netPath + ".nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			labelIdMap.put(label, id);
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		HashMap<String, Integer> idRankMap = new HashMap();
		scanner = new Scanner(new File(netPath + ".ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
		}
		scanner.close();
		
		HashSet<String> cleanEdge = new HashSet();
		scanner = new Scanner(new File(netPath + ".edges"));
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			cleanEdge.add(idLabelMap.get(src) + "#" + idLabelMap.get(dst));
		}
		scanner.close();
		
		HashMap<String, Double> edgeWeight = new HashMap();
		HashMap<String, Integer> edgeRankDiff = new HashMap();
		scanner = new Scanner(new File(netPath + "_graph.txt"));
		TreeMap<Integer, ArrayList<Integer>> rankDiffWeightMap = new TreeMap();
		while (scanner.hasNext()) {
			String src = scanner.next();
			String dst = scanner.next();
			double weight = scanner.nextDouble();
			if (!cleanEdge.contains(src + "#" + dst)) {
				continue;
			}
			String srcId = labelIdMap.get(src);
			String dstId = labelIdMap.get(dst);
			int srcRank = idRankMap.get(srcId);
			int dstRank = idRankMap.get(dstId);
			int rankDiff = dstRank - srcRank;
			edgeRankDiff.put(srcId + "#" + dstId, rankDiff);
			edgeWeight.put(srcId + "#" + dstId, weight);
			if (rankDiffWeightMap.containsKey(rankDiff)) {
				rankDiffWeightMap.get(rankDiff).add((int)weight);
			}
			else {
				ArrayList<Integer> aList = new ArrayList();
				aList.add((int)weight);
				rankDiffWeightMap.put(rankDiff, aList);
			}
		}
		scanner.close();
		
		class SCCEdge implements Comparable<SCCEdge>{
			String edge;
			int rankDiff;
			double weight;
			double sl;
			double tl;
			
			public SCCEdge(String edge, int rank, double weight, double sl, double tl) {
				this.edge = edge;
				this.rankDiff = rank;
				this.weight = weight;
				this.sl = sl;
				this.tl = tl;
			}
		
			public int compareTo(SCCEdge other) {
				if (this.rankDiff != other.rankDiff) return this.rankDiff - other.rankDiff;
				if (this.weight != other.weight) return (int)(this.weight - other.weight);
				
				/* skip
//				// average shortest path distance
				double thisLocationAgony = this.tl - this.sl;
				double otherLocationAgony = other.tl - other.sl;
//				if (thisLocationAgony == otherLocationAgony) System.out.println(thisLocationAgony + "\t" + otherLocationAgony);
				if (thisLocationAgony < 0 && otherLocationAgony < 0) {
					if (thisLocationAgony < otherLocationAgony) {
						return -1;
					}
					else if (otherLocationAgony < thisLocationAgony) {
						return +1;
					}
				}
				else if (thisLocationAgony > 0 && otherLocationAgony > 0) {
					if (thisLocationAgony < otherLocationAgony) {
						return +1;
					}
					else if (otherLocationAgony < thisLocationAgony) {
						return -1;
					}
				}
				else if (thisLocationAgony < 0) {
					return -1;
				}
				else if (otherLocationAgony < 0) {
					return +1;
				}
				end skip */
				
				return 0;
			}
		}
		
		// get shortest path based location
		int nNodes = 269;
		double distUp[][] = new double[nNodes][nNodes];
		double distDown[][] = new double[nNodes][nNodes];
		getAllShortestDistance(nNodes, distUp, dependencyDAG, true);
		getAllShortestDistance(nNodes, distDown, dependencyDAG, false);
		
		int removed = 0;
		HashMap<Integer, Integer> removedAgony = new HashMap();
		HashMap<Double, Integer> removedWeight = new HashMap();
		while (true) {
			TarjanSCC tarjanSCC = new TarjanSCC(dependencyDAG);
			tarjanSCC.getSCCs();
			
			if(tarjanSCC.count() == dependencyDAG.nodes.size()) {
				break;
			}
			
			int minSCCSize = Integer.MAX_VALUE;
			for (String s: tarjanSCC.SCCs.keySet()) {
				int size = tarjanSCC.SCCs.get(s).size();
				if (size < 2) {
					continue;
				}
				if (size < minSCCSize) {
					minSCCSize = size;
				}
			}
			
			for (String s: tarjanSCC.SCCs.keySet()) {
				int size = tarjanSCC.SCCs.get(s).size();
				if (size < 2) {
					continue;
				}
//				if (size != minSCCSize) {
//					continue; // usage: break in order of  increasing SCC size
//				}
				HashSet<String> scc = tarjanSCC.SCCs.get(s);
				
//				System.out.println(size);
//				System.out.println(minSCCSize);
				ArrayList<SCCEdge> sortedSCCEdge = new ArrayList();
				for(String n: scc) {
					for (String v: dependencyDAG.serves.get(n)) {
						if (!scc.contains(v)) {
							continue;
						}
						String edge = n + "#" + v;
//						System.out.println(edge);
						int rank = edgeRankDiff.get(edge);
						double weight = edgeWeight.get(edge);
						double sU = getAverageShortestDistance(nNodes, n, distUp, dependencyDAG, true);
						double sD = getAverageShortestDistance(nNodes, n, distDown, dependencyDAG, false);
						double tU = getAverageShortestDistance(nNodes, v, distUp, dependencyDAG, true);
						double tD = getAverageShortestDistance(nNodes, v, distDown, dependencyDAG, false);
//						System.out.println(edge + "\t" + sU + "\t" + sD + "\t" + tU + "\t" + tD);
						sortedSCCEdge.add(new SCCEdge(edge, rank, weight, sU / (sU + sD), tU / (tU + tD)));
					}
				}
				Collections.shuffle(sortedSCCEdge);
				Collections.sort(sortedSCCEdge);
				
				if (size == 3) {
				for (String n : scc) {	
					System.out.print(n + "\t");
				}
				System.out.println();
					
				for (SCCEdge sccEdge: sortedSCCEdge) {
					System.out.println(sccEdge.edge + "\t" + sccEdge.rankDiff + "\t" + sccEdge.weight + "\t" + sccEdge.sl + "\t" + sccEdge.tl);
				}
				}

				/*
				for (SCCEdge sccEdge: sortedSCCEdge) {
					String e = sccEdge.edge;
					String src = e.substring(0, e.indexOf("#"));
					String dst = e.substring(e.indexOf("#") + 1);
					dependencyDAG.removeEdge(src, dst);
					++removed;
//					System.out.println(e + "\t" + sccEdge.rank + "\t" + sccEdge.weight + "\t" + sccEdge.sl + "\t" + sccEdge.tl + "\t" + (sccEdge.tl - sccEdge.sl));
					if (removedCounter.containsKey(e)) {
						removedCounter.put(e, removedCounter.get(e) + 1);
					} else {
						removedCounter.put(e, 1);
					}
					if (!isSCC(scc, dependencyDAG)) {
						break;
					}
				}
				*/
				
				int sccBreak = 0;
				for (int i = 0; i < sortedSCCEdge.size(); ++i) {
					SCCEdge sccEdgeX = sortedSCCEdge.get(i);
					int j = i;
					int xKount = 0;
					int yKount = 0;
					int zKount = 0;
					for (; j < sortedSCCEdge.size(); ++j) {
						SCCEdge sccEdgeY = sortedSCCEdge.get(j);
						
						if (sccEdgeX.rankDiff != sccEdgeY.rankDiff) {
//							if (zKount == 1) System.out.println("x");
							break;
						}
						if (Math.abs(sccEdgeX.weight - sccEdgeY.weight) > 1) {
//							if (zKount == 1) System.out.println("y");
							break;
						}
						String e = sccEdgeY.edge;
						String src = e.substring(0, e.indexOf("#"));
						String dst = e.substring(e.indexOf("#") + 1);
						dependencyDAG.removeEdge(src, dst);
						++removed;
						++sccBreak;
//						System.out.println(sccEdgeY.rankDiff + "\t" + sccEdgeY.weight);
						if (removedCounter.containsKey(e)) {
							removedCounter.put(e, removedCounter.get(e) + 1);
						} else {
							removedCounter.put(e, 1);
						}
						++zKount;
						if (zKount > 1) {
//							System.out.println("z");
						}
						if (removedAgony.containsKey(sccEdgeY.rankDiff)) {
							removedAgony.put(sccEdgeY.rankDiff, removedAgony.get(sccEdgeY.rankDiff) + 1);
						}
						else {
							removedAgony.put(sccEdgeY.rankDiff, 1);
						}
						
						if (removedWeight.containsKey(sccEdgeY.weight)) {
							removedWeight.put(sccEdgeY.weight, removedWeight.get(sccEdgeY.weight) + 1);
						}
						else {
							removedWeight.put(sccEdgeY.weight, 1);
						}
					}
//					if (zKount > 1) System.out.println(zKount);
//					if (zKount == 1) System.out.println(zKount);
					if (!isSCC(scc, dependencyDAG)) {
						break;
					}
					i = j - 1;
				}
				
//				System.out.println(size + "\t" + sortedSCCEdge.size() + "\t" + sccBreak);
			}
			System.out.println("Iteration complete");
//			break;
		}
		
//		for (int i : removedAgony.keySet()) {
//			System.out.println(i + "\t" + removedAgony.get(i));
//		}
//		System.out.println("# # #");
//		for (double i : removedWeight.keySet()) {
//			System.out.println(i + "\t" + removedWeight.get(i));
//		}
		
		System.out.println("Edges removed: " + removed);
//		dependencyDAG.printLinks("celegans");
		/* begin special case */
		PrintWriter pw1 = new PrintWriter(new File("data//" + "celegans" + "_links.txt"));
		for (String s : dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				for (String r : dependencyDAG.serves.get(s)) {
					pw1.println(idLabelMap.get(s) + "\t" + idLabelMap.get(r));
				}
			}
		}
		pw1.close();
		/* end special case */
		HourglassAnalysis hourglassAnalysis = new HourglassAnalysis();
		hourglassAnalysis.runAnalysis("celegans");
		
		for (String s: hourglassAnalysis.dependencyDAG.coreNodes) {
			if (coreVarianceMembers.containsKey(s)) {
				coreVarianceMembers.put(s, coreVarianceMembers.get(s) + 1);
			}
			else {
				coreVarianceMembers.put(s, 1);
			}
		}
		
		for (int k : rankDiffWeightMap.keySet()) {
			int sz = rankDiffWeightMap.get(k).size();
			double[] a = new double[sz];
			int i = 0;
			for (int j : rankDiffWeightMap.get(k)) {
				a[i++] = j * 1.0;
			}
			System.out.println(k + "\t" + StatUtils.min(a) + "\t" + StatUtils.percentile(a, 50) + "\t" + StatUtils.max(a) + "\t" + sz);
		}
	}

	private static void initRunSocialRank() throws Exception {
		Process p0 = Runtime.getRuntime().exec("cmd /c del celegans.ranks", new String[0], new File("C:/MinGW/bin"));
		p0.waitFor();
		Process p1 = Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
		p1.waitFor();
		Process p2 = Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
		p2.waitFor();
		Process p3 = Runtime.getRuntime().exec("cmd /c socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
		InputStream is = p3.getInputStream();
		is.close();
		p3.waitFor(3L, TimeUnit.SECONDS);
	}

	public static void breakCyclesAgain() throws Exception {		
		class NeuroEdge implements Comparable<NeuroEdge>{
			String edge;
			int rank;
			double weight;
			double srcLoc;
			double tgtLoc;
			
			public NeuroEdge(String edge, int rank, double weight, double sl, double tl) {
				this.edge = edge;
				this.rank = rank;
				this.weight = weight;
				this.srcLoc = sl;
				this.tgtLoc = tl;
			}
		
			public int compareTo(NeuroEdge other) {
				if (this.rank != other.rank) return this.rank - other.rank;
				if (this.weight != other.weight) return (int)(this.weight - other.weight);
				
//				// average shortest path distance
				double thisLocationAgony = this.tgtLoc - this.srcLoc;
				double otherLocationAgony = other.tgtLoc - other.srcLoc;
//				if (thisLocationAgony == otherLocationAgony) System.out.println(thisLocationAgony + "\t" + otherLocationAgony);
//				if (thisLocationAgony < 0 && otherLocationAgony < 0) {
//					if (thisLocationAgony < otherLocationAgony) {
//						return -1;
//					}
//					else if (otherLocationAgony < thisLocationAgony) {
//						return +1;
//					}
//				}
//				else if (thisLocationAgony > 0 && otherLocationAgony > 0) {
//					if (thisLocationAgony < otherLocationAgony) {
//						return +1;
//					}
//					else if (otherLocationAgony < thisLocationAgony) {
//						return -1;
//					}
//				}
//				else if (thisLocationAgony < 0) {
//					return -1;
//				}
//				else if (otherLocationAgony < 0) {
//					return +1;
//				}
//				
//				return 0;
				
				if (thisLocationAgony < otherLocationAgony) return -1;
				else if (thisLocationAgony > otherLocationAgony) return +1;
				else return 0;
			}
		}
		
		int removed = 0;
		String netPath = "C:/MinGW/bin/";
		
		HashMap<String, String> labelIdMap = new HashMap();
		HashMap<String, String> idLabelMap = new HashMap();
		Scanner scanner = new Scanner(new File(netPath + "celegans.nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			labelIdMap.put(label, id);
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		Process p0 = Runtime.getRuntime().exec("cmd /c del celegans.ranks", new String[0], new File("C:/MinGW/bin"));
		p0.waitFor();
		Process p1 = Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
		p1.waitFor();
		Process p2 = Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
		p2.waitFor();
//		p0.destroyForcibly();
//		p1.destroyForcibly();
//		p2.destroyForcibly();
//		if (true) System.exit(0);
		
		int kount = 0;
		int thirdCriterion = 0;
		int a = 0, b = 0, c = 0;
		while (true) {
			DependencyDAG.isToy = true;
			DependencyDAG dependencyDAG = new DependencyDAG(netPath + "celegans.edges");
//			dependencyDAG.printNetworkStat();
			
			// compute shortest path based location
			int nNodes = 269;
			double distUp[][] = new double[nNodes][nNodes];
			double distDown[][] = new double[nNodes][nNodes];
			getAllShortestDistance(nNodes, distUp, dependencyDAG, true);
			getAllShortestDistance(nNodes, distDown, dependencyDAG, false);

			// compute socialrank
			Process p3 = Runtime.getRuntime().exec("cmd /c socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
//			Process p3 = Runtime.getRuntime().exec("socialrank.exe summary_stats.txt celegans", new String[0], new File(netPath));
//			int status3 = p3.waitFor();
//			boolean status3 = p3.waitFor(10L, TimeUnit.SECONDS);
//			p3.destroyForcibly();
//			System.out.println(status3);
//			Thread.sleep(3000);
			InputStream is = p3.getInputStream();
			is.close();
			p3.waitFor(3L, TimeUnit.SECONDS);

			// load updated ranks and edges and edge weights
			HashMap<String, Integer> idRankMap = new HashMap();
			scanner = new Scanner(new File(netPath + "celegans.ranks"));
			scanner.nextLine(); // skip first line
			while (scanner.hasNext()) {
				String id = scanner.next();
				int rank = scanner.nextInt();
				int agony = scanner.nextInt();
				if (id.contains(".")) break; // skip last line
				idRankMap.put(id, rank);
			}
			scanner.close();
			
			// get original edges
			HashSet<String> cleanEdge = new HashSet();
			scanner = new Scanner(new File(netPath + "celegans.edges"));
			while (scanner.hasNext()) {
				String src = scanner.next();
				String dst = scanner.next();
				cleanEdge.add(idLabelMap.get(src) + "#" + idLabelMap.get(dst));
			}
			scanner.close();
			
			// get edge weights (weight source has original id)
			HashMap<String, Double> edgeWeight = new HashMap();
			HashMap<String, Integer> edgeRank = new HashMap();
			scanner = new Scanner(new File(netPath + "celegans.graph"));
			while (scanner.hasNext()) {
				String src = scanner.next();
				String dst = scanner.next();
				double weight = scanner.nextDouble();
				if (!cleanEdge.contains(src + "#" + dst)) {
					continue;
				}
				String srcId = labelIdMap.get(src);
				String dstId = labelIdMap.get(dst);
				int srcRank = idRankMap.get(srcId);
				int dstRank = idRankMap.get(dstId);
				edgeRank.put(srcId + "#" + dstId, dstRank - srcRank);
				edgeWeight.put(srcId + "#" + dstId, weight);
			}
			scanner.close();		
			
			ArrayList<NeuroEdge> sortedCycleEdges = new ArrayList();
			for(String n: dependencyDAG.serves.keySet()) {
				for (String v: dependencyDAG.serves.get(n)) {
//					check if cause cycle	
					if (!dependencyDAG.successors.containsKey(v)) {
						dependencyDAG.loadReachability(v);
					}
					if (!dependencyDAG.successors.get(v).contains(n)) {
						// is not part of a cycle
						continue;
					}
					String edge = n + "#" + v;
//					System.out.println(edge);
					int rank = edgeRank.get(edge);
					double weight = edgeWeight.get(edge);
					double sU = getAverageShortestDistance(nNodes, n, distUp, dependencyDAG, true);
					double sD = getAverageShortestDistance(nNodes, n, distDown, dependencyDAG, false);
					double tU = getAverageShortestDistance(nNodes, v, distUp, dependencyDAG, true);
					double tD = getAverageShortestDistance(nNodes, v, distDown, dependencyDAG, false);
//					System.out.println(edge + "\t" + sU + "\t" + sD + "\t" + tU + "\t" + tD);
					sortedCycleEdges.add(new NeuroEdge(edge, rank, weight, sU / (sU + sD), tU / (tU + tD)));
//					System.out.println(edge + "\t" + rank);
				}
			}
			Collections.sort(sortedCycleEdges);
				
//			int print5 = 2;
//			for (NeuroEdge neuroEdge: sortedCycleEdges) {
//				System.out.println(neuroEdge.edge + "\t" + neuroEdge.rank + "\t" + neuroEdge.weight + "\t" + neuroEdge.srcLoc + "\t" + neuroEdge.tgtLoc);
//				if (print5-- < 0) break;
//			}
			if (sortedCycleEdges.size() > 1) {
				NeuroEdge nE1 = sortedCycleEdges.get(0);
				NeuroEdge nE2 = sortedCycleEdges.get(1);
				if (nE1.rank != nE2.rank) ++a;
				else if (nE1.weight != nE2.weight) ++b;
				else ++c;
				if (nE1.rank == nE2.rank && nE1.weight == nE2.weight) {
					double nE1LocationAgony = nE1.tgtLoc - nE1.srcLoc;
					double nE2LocationAgony = nE2.tgtLoc - nE2.srcLoc;
					if (nE1LocationAgony > 0 && nE2LocationAgony > 0) {
						++thirdCriterion;
					}
//					++thirdCriterion;
				}
			}
			
//			if (true) break;
				
			for (NeuroEdge neuroEdge: sortedCycleEdges) {
				String e = neuroEdge.edge;
				String src = e.substring(0, e.indexOf("#"));
				String dst = e.substring(e.indexOf("#") + 1);
				dependencyDAG.removeEdge(src, dst);
				++removed;
				System.out.println(e + "\t" + neuroEdge.rank + "\t" + neuroEdge.weight + "\t" + neuroEdge.srcLoc + "\t" + neuroEdge.tgtLoc + "\t" + (neuroEdge.tgtLoc - neuroEdge.srcLoc));
				if (removedCounter.containsKey(e)) {
					removedCounter.put(e, removedCounter.get(e) + 1);
				} else {
					removedCounter.put(e, 1);
				}
				
//				write celegans.edges back
				PrintWriter pw = new PrintWriter(new File(netPath + "celegans.edges"));
				for (String s : dependencyDAG.nodes) {
					if (dependencyDAG.serves.containsKey(s)) {
						for (String r : dependencyDAG.serves.get(s)) {
							pw.println(s + "\t" + r);
						}
					}
				}
				pw.close();
				break;
			}
			
			if (sortedCycleEdges.isEmpty()) {
				break;
			}
			
			if (++kount > 0) {
//				break;
			}
			System.out.println(" -- -- -- -- -- -- ");
		}

		System.out.println("Edges removed: " + removed);
		System.out.println("Third criterion used: " + thirdCriterion);
		System.out.println(a + "\t" + b + "\t" + c);
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath + "celegans.edges");		
		PrintWriter pw = new PrintWriter(new File("data//" + "celegans" + "_links.txt"));
		for (String s : dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(s)) {
				for (String r : dependencyDAG.serves.get(s)) {
					pw.println(idLabelMap.get(s) + "\t" + idLabelMap.get(r));
				}
			}
		}
		pw.close();
		HourglassAnalysis hourglassAnalysis = new HourglassAnalysis();
		hourglassAnalysis.runAnalysis("celegans");
		
		for (String s: hourglassAnalysis.dependencyDAG.coreNodes) {
			if (coreVarianceMembers.containsKey(s)) {
				coreVarianceMembers.put(s, coreVarianceMembers.get(s) + 1);
			}
			else {
				coreVarianceMembers.put(s, 1);
			}
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
//		optimizeAcyclicity();

//		initRunSocialRank();
//		breakCycles();
//		breakSCCs();
//		breakCyclesAgain();
		
//		randomSimulations();
		
//		statisticalRun();
//		traverseAllPaths();
		traverseAlmostShortestPaths();
	}
}
