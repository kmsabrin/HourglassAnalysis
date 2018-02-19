package utilityhg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ShortestPathHourglass {	
	public static HashSet<String> source = new HashSet();
	public static HashSet<String> inter = new HashSet();
	public static HashSet<String> target = new HashSet();
	public static HashSet<String> nodes = new HashSet();
	public static HashMap<String, String> idNeuron = new HashMap();
	public static HashMap<String, Integer> weights = new HashMap();
	public static HashSet<String> coreNeurons = new HashSet();
	static int flatCoreSize;
	static int realCoreSize;
	static double tau = 0.85;
	
	private static class FlatEdge {
		String src;
		String dst;
		int weight;
	};
	
	private static void reset() {
		source = new HashSet();
		inter = new HashSet();
		target = new HashSet();
		nodes = new HashSet();
		idNeuron = new HashMap();
		weights = new HashMap();
		coreNeurons = new HashSet();
		flatCoreSize = 0;
		realCoreSize = 0;
		tau = 0.9;
	}
	
	private static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG.isCyclic = true;
//		String toyDAGName = "toy_dag_paper";
		String toyDAGName = "toy_cyclic_2";
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
//		ModelRealConnector modelRealConnector = new ModelRealConnector(toyDependencyDAG);
//		modelRealConnector.generateModelNetwork(toyDependencyDAG, 1);
		
		String netID = "toy_dag";
//		toyDependencyDAG.printNetworkStat();
		toyDependencyDAG.printNetworkProperties();

		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(toyDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;
		
//		CoreDetection.getCentralEdgeSubgraph(toyDependencyDAG);

//		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(toyDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}

	private static void loadNodes(HashSet<String> nodes, HashSet<String> typeNode, String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			String i = scan.next();
			typeNode.add(i);
			nodes.add(i);
		}
		scan.close();
	}
	
	private static void loadNeuroMetaNetwork() throws Exception {
		loadNodes(nodes, source, "celegans//sensory_neurons.txt");
		loadNodes(nodes, inter, "celegans//inter_neurons.txt");
		loadNodes(nodes, target, "celegans//motor_neurons.txt");
		
		Scanner scan = new Scanner(new File("celegans//celegans_labels.txt"));
		while (scan.hasNext()) {
			String id = scan.next();
			String neuron = scan.next();
			idNeuron.put(id, neuron);
		}
		scan.close();
		
		scan = new Scanner(new File("celegans//celegans_graph.txt"));
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			double wgt = scan.nextDouble();
			weights.put(src + "#" + dst, (int)wgt);
		}
		scan.close();
		
//		scan = new Scanner(new File("celegans//core_neurons.txt"));
		scan = new Scanner(new File("celegans//core_neurons_tau_1.txt"));
		int knt = 0;
		while (scan.hasNext()) {
			coreNeurons.add(scan.next());
			++knt;
			if (knt >= 12) break;
		}
		scan.close();
	}
	
	private static void shortestPathAnalysis_1() throws Exception {
//		doToyNetworkAnalysis();
		
		loadNeuroMetaNetwork();
		
		HashSet<String> fbEdges = new HashSet();
		Scanner scanner = new Scanner(new File("celegans//fb_edges.txt"));
		while (scanner.hasNext()) {
			String edg = scanner.next();
			fbEdges.add(edg);
//			System.out.println(weights.get(edg));
		}
		scanner.close();
		
//		Scanner scanner = new Scanner(new File("all_sp_kount.txt"));
//		scanner = new Scanner(new File("celegans//dual_clean_sp.txt"));
		scanner = new Scanner(new File("celegans//fb_clean_sp.txt"));
		HashMap<Integer, Integer> SPLengthFreq = new HashMap();
		HashSet<String> SPInter = new HashSet();
		HashSet<String> shortestPathEdge = new HashSet();
		HashMap<String, Integer> shortestPathEdgeFrequency = new HashMap();
		HashMap<String, Integer> lengthSP = new HashMap();
		int containsBackPath = 0;
		while (scanner.hasNext()) {
//			int num = scanner.nextInt();
			String line = scanner.nextLine();
			String[] tokens = line.split("\\s+");
			int num = 1;
			for (int i = 0; i < line.length(); ++i) {
				if (line.charAt(i) == ',') {
					++num;
				}
			}
			
			if (SPLengthFreq.containsKey(num)) {
				SPLengthFreq.put(num, SPLengthFreq.get(num) + 1);
			}
			else SPLengthFreq.put(num, 1);
			
//			System.out.println(line);
			boolean containsBack = false;
			String prev = "";
			String src = "";
			String tgt = "";
			for (String r : tokens) {
				if (r.startsWith("[")) {
					r = r.substring(1);
					src = r;
				}
				if (r.endsWith("]")) {
					r = r.substring(0, r.length() - 1);
					tgt = r;
				}
//				System.out.println(r);
				if (inter.contains(r)) SPInter.add(r);
				if (prev != "") {
					String edg = prev + "#" + r;
					
					if (!shortestPathEdge.contains(edg)) {
//						System.out.println(weights.get(edg));
						shortestPathEdge.add(edg);
//						System.out.println(edg);
					}
					
					if (shortestPathEdgeFrequency.containsKey(edg)) {
						shortestPathEdgeFrequency.put(edg, shortestPathEdgeFrequency.get(edg) + 1);
					}
					else {
						shortestPathEdgeFrequency.put(edg, 1);
					}
					
					/*
					if (fbEdges.contains(edg)) {
						containsBack = true;
					}
					*/
				}
				prev = r;
			}
			
//			if (containsBack) containsBackPath++;
//			break;
			
			String pairSM = src + "#" + tgt; 
			lengthSP.put(pairSM, tokens.length - 1);
		}
		
		for (int k : SPLengthFreq.keySet()) {
//			System.out.println(k + "\t" + freq.get(k));
		}
		
		for (String s : shortestPathEdgeFrequency.keySet()) {		
//			System.out.println(s + "\t" + shortestPathEdgeFrequency.get(s) + "\t" + weights.get(s));
//			System.out.println(s + "\t" + shortestPathEdgeFrequency.get(s));
		}
		
		for (String s : lengthSP.keySet()) {
			System.out.println(s + "\t" + lengthSP.get(s));
		}
		
//		System.out.println(SPInter.size());
//		System.out.println(containsBackPath);
	}
	
	private static void computeFlatCore() throws Exception {
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp.txt"));
		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
		HashSet<String> sPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			sPaths.add(line);
		}
		scanner.close();
		
		int size = (int)(sPaths.size() * (1.0 - tau));
		double startSize = sPaths.size();
		flatCoreSize = 0;
		while (true) {
			HashMap<String, Integer> maxSPCentrality = new HashMap();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
				for (int i = 0; i < tokens.length; ++i) {
//					if (r.startsWith("[")) r = r.substring(1);
//					else if (r.endsWith("]")) r = r.substring(0, r.length() - 1);
//					else continue;
					if (i == 0 || i == tokens.length - 1) {
						String r = tokens[i];
						// System.out.println(r);
						if (maxSPCentrality.containsKey(r)) {
							maxSPCentrality.put(r, maxSPCentrality.get(r) + 1);
						} else {
							maxSPCentrality.put(r, 1);
						}
					}
				}
			}
			
			int max = 0;
			HashSet<String> maxSPCNeurons = new HashSet();
			for (String v : maxSPCentrality.keySet()) {
				if (maxSPCentrality.get(v) > max) {
					maxSPCNeurons.clear();
					maxSPCNeurons.add(v);
					max = maxSPCentrality.get(v);
				}
				// skip for flat
//				else if (maxSPCentrality.get(v) == max) {
//					maxSPCNeurons.add(v);
//				}
			}
			
//			System.out.println(max / startSize);
//			for (String v : maxSPCNeurons) {
//				System.out.print(v + "\t");
//			}
//			System.out.println();
//			System.out.println("\n-- -- -- -- --");
			
			HashSet<String> removeSPaths = new HashSet();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				System.out.println(line);
				for (int i = 0; i < tokens.length; ++i) {
//					if (r.startsWith("[")) r = r.substring(1);
//					else if (r.endsWith("]")) r = r.substring(0, r.length() - 1);
//					else continue;
					if (i == 0 || i == tokens.length - 1) {
						String r = tokens[i];					
//						System.out.println(r);
						if (maxSPCNeurons.contains(r)) {
							removeSPaths.add(line);
						}
					}
				}
			}

			sPaths.removeAll(removeSPaths);
			++flatCoreSize;
			if (sPaths.size() <= size) break;
		}		
		
//		System.out.println(flatCoreSize);
	}
	
	private static void shortestPathHourglassAnalysis() throws Exception {
		loadNeuroMetaNetwork();
		
		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
		HashSet<String> sPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			sPaths.add(line);
		}
		scanner.close();
		
		
		int sizeSP = 0;
		int cutLen = 3;
		for (String line : sPaths) {
			String[] tokens = line.split("\\s+");
			if (tokens.length >= cutLen) continue;
			++sizeSP;
		}
		
		int size = (int)(sizeSP * (1.0 - tau));
		double startSize = sPaths.size();
		realCoreSize = 0;
		while (true) {
			HashMap<String, Integer> maxSPCentrality = new HashMap();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
				if (tokens.length >= cutLen) continue;
//				System.out.println(line + " ## " + tokens.length);
				for (String r : tokens) {
//					if (r.startsWith("[")) r = r.substring(1);
//					if (r.endsWith("]")) r = r.substring(0, r.length() - 1);
//					System.out.println(r);
					if (maxSPCentrality.containsKey(r)) {
						maxSPCentrality.put(r, maxSPCentrality.get(r) + 1);
					}
					else {
						maxSPCentrality.put(r, 1);
					}
				}
//				break;
			}
//			System.exit(0);
			int max = 0;
			HashSet<String> maxSPCNeurons = new HashSet();
			for (String v : maxSPCentrality.keySet()) {
				if (maxSPCentrality.get(v) > max) {
					maxSPCNeurons.clear();
					maxSPCNeurons.add(v);
					max = maxSPCentrality.get(v);
				}
				else if (maxSPCentrality.get(v) == max) {
					maxSPCNeurons.add(v);
				}
			}
			
//			System.out.println(max / startSize);
			for (String v : maxSPCNeurons) {
				System.out.print(v + "\t");
			}
			System.out.println();
//			System.out.println("\n-- -- -- -- --");
			
			HashSet<String> removeSPaths = new HashSet();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				System.out.println(line);
				for (String r : tokens) {
//					if (r.startsWith("[")) r = r.substring(1);
//					if (r.endsWith("]")) r = r.substring(0, r.length() - 1);
//					System.out.println(r);
					if (maxSPCNeurons.contains(r)) {
						removeSPaths.add(line);
					}
				}
			}

			sPaths.removeAll(removeSPaths);
			++realCoreSize;
			if (sPaths.size() <= size) break;
		}
	}
	
	public static ArrayList<String> splitEdge(String edge) {
		int idx = edge.indexOf("#");
		ArrayList<String> nodes = new ArrayList();
		nodes.add(edge.substring(0, idx));
		nodes.add(edge.substring(idx + 1));
		return nodes;
	}
	
	private static void createCoreNetwork() throws Exception {
		loadNeuroMetaNetwork();
		HashSet<String> ffEdges = new HashSet();
		HashSet<String> fbEdges = new HashSet();
		Scanner scan = new Scanner(new File("celegans//ff_edges.txt"));
		while (scan.hasNext()) {
			ffEdges.add(scan.next());
		}
		scan.close();
		scan = new Scanner(new File("celegans//fb_edges.txt"));
		int knt = 0;
		while (scan.hasNext()) {
			String edg = scan.next();
			fbEdges.add(edg);
			ArrayList<String> edgeNodes = splitEdge(edg);
			if (coreNeurons.contains(edgeNodes.get(0)) || coreNeurons.contains(edgeNodes.get(1))) {
//				System.out.println(edg);
				++knt;
			}
		}
		scan.close();
//		System.out.println(knt);
		
		scan = new Scanner(new File("celegans//dual_clean_links.txt"));
		knt = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			String edg = src + "#" + dst;
			if (coreNeurons.contains(src) && coreNeurons.contains(dst)) {
				if (ffEdges.contains(edg)) {
//					System.out.println(weights.get(edg));
//					++knt;
				}
				else if (fbEdges.contains(edg)) {
//					System.out.println(weights.get(edg));
					++knt;
				}
			}
		}
		scan.close();
		System.out.println(knt);
	}
	
	public static void main(String[] args) throws Exception {
//		doToyNetworkAnalysis();
//		shortestPathAnalysis_1();
		shortestPathHourglassAnalysis();
//		computeFlatCore();
//		System.out.println(realCoreSize + "\t" + flatCoreSize);
//		System.out.println(1.0 - (realCoreSize * 1.0 / flatCoreSize));
		
//		for (tau = 0.96; tau <= 0.99; tau += 0.02) {
//			shortestPathHourglassAnalysis();
//			computeFlatCore();
//			System.out.println(tau + "\t" + (1.0 - (realCoreSize * 1.0 / flatCoreSize)));
//		}
		
//		createCoreNetwork();
	}
}
