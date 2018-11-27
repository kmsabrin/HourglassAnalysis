package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import corehg.DependencyDAG;

public class ShortestPathHourglass {	
	public static HashSet<String> source = new HashSet();
	public static HashSet<String> inter = new HashSet();
	public static HashSet<String> target = new HashSet();
	public static HashSet<String> nodes = new HashSet();
	public static HashMap<String, String> idNeuron = new HashMap();
	public static HashMap<String, String> neuronId = new HashMap();
	public static HashMap<String, Integer> chemicalWeights = new HashMap();
	public static HashSet<String> coreNeurons = new HashSet();
	public static ArrayList<String> orderedCore = new ArrayList();
	public static HashMap<String, Integer> edgePathWeights = new HashMap();
	public static HashMap<String, Integer> nodePathWeights = new HashMap();
	public static HashMap<String, Integer> inDeg = new HashMap();
	public static HashMap<String, Integer> outDeg = new HashMap();
	public static HashMap<String, Integer> sumDeg = new HashMap();
	public static HashMap<String, Integer> dummy = new HashMap();
	public static ArrayList<String[]> canonicalPaths = new ArrayList();
	public static HashMap<String, Integer> pairWeight = new HashMap();
	public static HashMap<String, Integer> hierarchy = new HashMap();
	public static HashMap<String, HashSet<String>> targetSourceDependence = new HashMap();
	public static HashMap<String, Integer> smPairSPLen = new HashMap();
	public static HashSet<String> finalPaths = new HashSet();
	public static HashMap<String, Double> sumStrength = new HashMap();
	public static HashMap<String, Double> inStrength = new HashMap();
	public static HashMap<String, Double> outStrength = new HashMap();
	static int flatCoreSize;
	static int realCoreSize;
	static double tau = 1.0;
	static String currentNet = "random.network";
	static String pathFile = "celegans//all_sp+2.txt";
//	static String pathFile = "celegans//all_sp+2_gap+chemical.txt";
//	static String pathFile = "data//kamal_paths_2.txt";
//	static String pathFile = "data//rat_paths.txt";
//	static String pathFile = "data//h4_paths.txt";
//	static String pathFile = "data//random.network_paths.txt";
	public static HashMap<String, HashSet<String>> numSourcePath = new HashMap();
	public static HashMap<String, HashSet<String>> numTargetPath = new HashMap();
	public static HashMap<String, Double> location = new HashMap();
	public static HashSet<String> ltEdges = new HashSet();
	public static HashSet<String> ffEdges = new HashSet();
	
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
		neuronId = new HashMap();
		
		chemicalWeights = new HashMap();
		inDeg = new HashMap();
		outDeg = new HashMap();
		sumDeg = new HashMap();
		dummy = new HashMap();

		coreNeurons = new HashSet();
		orderedCore = new ArrayList();
		edgePathWeights = new HashMap();
		nodePathWeights = new HashMap();
		canonicalPaths = new ArrayList();
		finalPaths = new HashSet();
		
		sumStrength = new HashMap();
		inStrength = new HashMap();
		outStrength = new HashMap();

		flatCoreSize = 0;
		realCoreSize = 0;
//		tau = 0.9;
		
		pairWeight = new HashMap();
		hierarchy = new HashMap();
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
	
	private static String getType(String neuron) {
//		if (source.contains(neuron) && target.contains(neuron)) return "s-m";
		if (source.contains(neuron)) return "s";
		else if (inter.contains(neuron)) return "i";
		else return "m";
	}
	
	private static int getNumericType(String neuron) {
//		if (source.contains(neuron) && target.contains(neuron)) return 4;
		if (source.contains(neuron)) return 1;
		else if (inter.contains(neuron)) return 2;
		else return 3;
	}
	
	private static void loadMetaNetwork() throws Exception {
		reset();
	
		String net = "h4";
		loadNodes(nodes, source, "data//" + net + "_sources.txt");
		loadNodes(nodes, inter, "data//" + net + "_inters.txt");
		loadNodes(nodes, target, "data//" + net + "_targets.txt");
	
		Scanner scan = new Scanner(new File("data//" + net + "_cores.txt"));
		while (scan.hasNext()) {			
			coreNeurons.add(scan.next());
		}
		scan.close();
//		System.out.println(coreNeurons);
		
		Scanner scanner = new Scanner(new File("data//" + net + "_paths.txt"));
		HashSet<String> sPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			sPaths.add(line);
		}
		scanner.close();
		
		
		
//		for (String line : sPaths) {
//			String[] tokens = line.split("\\s+");
//			canonicalPaths.add(Arrays.copyOf(tokens, tokens.length));
//			for (int i = 0; i < tokens.length - 1; ++i) {
//				String edg = tokens[i] + "#" + tokens[i + 1];
//				addFrequencyValuedMap(edgePathWeights, edg);
//				addFrequencyValuedMap(nodePathWeights, tokens[i]);
//			}
//			addFrequencyValuedMap(nodePathWeights, tokens[tokens.length - 1]);
//			if (targetSourceDependence.containsKey(tokens[tokens.length - 1])) {
//				targetSourceDependence.get(tokens[tokens.length - 1]).add(tokens[0]);
//			}
//			else {
//				HashSet<String> hset = new HashSet();
//				hset.add(tokens[0]);
//				targetSourceDependence.put(tokens[tokens.length - 1], hset);
//			}
//		}

		HashMap<Integer, Integer> pathLengthFrequencyMap = new HashMap();
		for (String line : sPaths) {
			String[] tokens = line.split("\\s+");
			addFrequencyValuedMap(pathLengthFrequencyMap, tokens.length);
//			canonicalPaths.add(Arrays.copyOf(tokens, tokens.length));
//			System.out.println(pathKount++);
			for (int i = 0; i < tokens.length; ++i) {
				if (i < tokens.length - 1) {
					String edg = tokens[i] + "#" + tokens[i + 1];
					addFrequencyValuedMap(edgePathWeights, edg);
				}
				addFrequencyValuedMap(nodePathWeights, tokens[i]);
				
				if (i > 0) {
					String srcPath = sumTokens(tokens, 0, i);
					if (numSourcePath.containsKey(tokens[i])) numSourcePath.get(tokens[i]).add(srcPath);
					else {
						HashSet<String> hset = new HashSet();
						hset.add(srcPath);
						numSourcePath.put(tokens[i], hset);
					}
				}
				if (i < tokens.length - 1) {
					String tgtPath = sumTokens(tokens, i, tokens.length - 1);
					if (numTargetPath.containsKey(tokens[i])) numTargetPath.get(tokens[i]).add(tgtPath);
					else {
						HashSet<String> hset = new HashSet();
						hset.add(tgtPath);
						numTargetPath.put(tokens[i], hset);
					}
				}
			}	
			
//			if (targetSourceDependence.containsKey(tokens[tokens.length - 1])) {
//				targetSourceDependence.get(tokens[tokens.length - 1]).add(tokens[0]);
//			}
//			else {
//				HashSet<String> hset = new HashSet();
//				hset.add(tokens[0]);
//				targetSourceDependence.put(tokens[tokens.length - 1], hset);
//			}
		}
		
		for (String s : nodes) {
			if (!numSourcePath.containsKey(s)) {
				location.put(s, 0.0);
			}
			else if (!numTargetPath.containsKey(s)) {
				location.put(s, 1.0);
			}
			else {
//				System.out.println(s + "\t" + numSourcePath.get(s).size() + "\t" + numTargetPath.get(s).size());
				if (numSourcePath.get(s).size() == 1 && numTargetPath.get(s).size() == 1) location.put(s, 0.5);
				else {
					double loc = (numSourcePath.get(s).size()) * 1.0 / ((numTargetPath.get(s).size()) + (numSourcePath.get(s).size()));
					location.put(s, loc);
				}
			}
			System.out.println(s + "\t" + location.get(s));
		}
	}
	
	private static String sumTokens(String[] tokens, int start, int end) {
		String s = "";
		for (int i = start; i <= end; ++i) {
			s += tokens[i];
		}
		return s;
	}
	
	private static void loadNeuroMetaNetwork() throws Exception {
		reset();
//		loadNodes(nodes, source, "celegans//sensory_neurons.txt");
//		loadNodes(nodes, inter, "celegans//inter_neurons.txt");
//		loadNodes(nodes, target, "celegans//motor_neurons.txt");
	
		loadNodes(nodes, source, "celegans//sensory_neurons_3.txt");
		loadNodes(nodes, inter, "celegans//inter_neurons_3.txt");
		loadNodes(nodes, target, "celegans//motor_neurons_3.txt");
		
		Scanner scanner = new Scanner(new File("celegans//ff_edges.txt"));
		while (scanner.hasNext()) {
			ffEdges.add(scanner.next());
		}
		scanner.close();
		
		scanner = new Scanner(new File("celegans//lt_edges.txt"));
		while (scanner.hasNext()) {
			ltEdges.add(scanner.next());
		}
		scanner.close();
	
		Scanner scan = new Scanner(new File("celegans//celegans_labels.txt"));
		while (scan.hasNext()) {
			String id = scan.next();
			String neuron = scan.next();
			idNeuron.put(id, neuron);
			neuronId.put(neuron, id);
		}
		scan.close();
		
		scan = new Scanner(new File("celegans//celegans_graph.txt"));
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			double wgt = scan.nextDouble();
			chemicalWeights.put(src + "#" + dst, (int)wgt);
			addFrequencyValuedMap(dummy, getType(src)+ "#" + getType(dst));
//			System.out.println((wgt + "\t" + getType(src)+ "#" + getType(dst)));
			addFrequencyValuedMap(inDeg, dst);
			addFrequencyValuedMap(outDeg, src);
			addFrequencyValuedMap(sumDeg, src);
			addFrequencyValuedMap(sumDeg, dst);
			
			addFrequencyValuedMap(inStrength, dst, wgt);
			addFrequencyValuedMap(sumStrength, src, wgt);
			addFrequencyValuedMap(outStrength, src, wgt);
			addFrequencyValuedMap(sumStrength, dst, wgt);
		}
		scan.close();
//		for (String s : dummy.keySet()) {
//			System.out.println(s + "\t" + dummy.get(s));
//		}		
		
//		for (String s : nodes) {
//			double iS = 0;
//			double oS = 0;
//			double iD = 0;
//			double oD = 0;
//			if (inDeg.containsKey(s)) iD = inDeg.get(s);
//			if (outDeg.containsKey(s)) oD = outDeg.get(s);
//			if (inStrength.containsKey(s)) iS = inStrength.get(s);
//			if (outStrength.containsKey(s)) oS = outStrength.get(s);
//			System.out.print(getType(s));
//			System.out.print("\t" + iD + "\t" + oD);
//			System.out.println("\t" + iS + "\t" + oS);
//		}
		
		
		scan = new Scanner(new File("celegans//all_core_tau_1.txt"));
//		scan = new Scanner(new File("celegans//core_neurons_sp+2.txt"));
		int knt = 0;
		while (scan.hasNext()) {
			String tmp = scan.next();
			if (knt < 10) coreNeurons.add(tmp);
			orderedCore.add(tmp);
			++knt;
//			if (knt > 18) break;
		}
		scan.close();
		
//		Scanner scanner = new Scanner(new File("celegans//all_5_path.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_4_path.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_SP+2.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_LTM_path.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_LTM_path_weighted.txt"));
//		HashSet<String>
		scanner = new Scanner(new File(pathFile));
		finalPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			finalPaths.add(line);
		}
		scanner.close();
//		System.out.println(finalPaths.size());
		/*
		int pathKount = 0;
		HashMap<Integer, Integer> pathLengthFrequencyMap = new HashMap();
		for (String line : finalPaths) {
			String[] tokens = line.split("\\s+");
			addFrequencyValuedMap(pathLengthFrequencyMap, tokens.length);
//			canonicalPaths.add(Arrays.copyOf(tokens, tokens.length));
//			System.out.println(pathKount++);
			for (int i = 0; i < tokens.length; ++i) {
				if (i < tokens.length - 1) {
					String edg = tokens[i] + "#" + tokens[i + 1];
					addFrequencyValuedMap(edgePathWeights, edg);
				}
				addFrequencyValuedMap(nodePathWeights, tokens[i]);
				
				if (i > 0) {
					String srcPath = sumTokens(tokens, 0, i);
					if (numSourcePath.containsKey(tokens[i])) numSourcePath.get(tokens[i]).add(srcPath);
					else {
						HashSet<String> hset = new HashSet();
						hset.add(srcPath);
						numSourcePath.put(tokens[i], hset);
					}
				}
				if (i < tokens.length - 1) {
					String tgtPath = sumTokens(tokens, i, tokens.length - 1);
					if (numTargetPath.containsKey(tokens[i])) numTargetPath.get(tokens[i]).add(tgtPath);
					else {
						HashSet<String> hset = new HashSet();
						hset.add(tgtPath);
						numTargetPath.put(tokens[i], hset);
					}
				}
			}	
			
//			if (targetSourceDependence.containsKey(tokens[tokens.length - 1])) {
//				targetSourceDependence.get(tokens[tokens.length - 1]).add(tokens[0]);
//			}
//			else {
//				HashSet<String> hset = new HashSet();
//				hset.add(tokens[0]);
//				targetSourceDependence.put(tokens[tokens.length - 1], hset);
//			}
		}
		
		for (String s : nodes) {
//			System.out.print(s);
//			if (numSourcePath.containsKey(s)) System.out.print("\t" + numSourcePath.get(s).size());
//			if (numTargetPath.containsKey(s)) System.out.print("\t" + numTargetPath.get(s).size());
//			System.out.println();
			
			if (!numSourcePath.containsKey(s)) {
				location.put(s, 0.0);
			}
			else if (!numTargetPath.containsKey(s)) {
				location.put(s, 1.0);
			}
			else {
//				System.out.println(s + "\t" + numSourcePath.get(s).size() + "\t" + numTargetPath.get(s).size());
				if (numSourcePath.get(s).size() == 1 && numTargetPath.get(s).size() == 1) location.put(s, 0.5);
				else {
					double loc = (numSourcePath.get(s).size()) * 1.0 / ((numTargetPath.get(s).size()) + (numSourcePath.get(s).size()));
					location.put(s, loc);
				}
			}
//			System.out.println(s + "\t" + location.get(s));
		}
		
		
		for (String s : nodePathWeights.keySet()) {
			if (sumDeg.containsKey(s)) {
//				System.out.println(nodePathWeights.get(s) + "\t" + sumDeg.get(s));
			}
			if (sumStrength.containsKey(s)) {
//				System.out.println(nodePathWeights.get(s) + "\t" + sumStrength.get(s));
			}
//			System.out.println(s + "\t" + (nodePathWeights.get(s) * 1.0 /finalPaths.size())); // path centrality of celegans
		}
		
		scanner = new Scanner(new File("celegans//sm_pair_sp_len.txt")); // this file is in hops
		while (scanner.hasNext()) {
			String smPair = scanner.next();
			int spLen = scanner.nextInt();
			smPairSPLen.put(smPair, spLen);
		}
		scanner.close();
		
		for (int i : pathLengthFrequencyMap.keySet()) {
//			System.out.println(i + "\t" + pathLengthFrequencyMap.get(i));
		}
		*/
	}
	
	private static void shortestPathAnalysis_1() throws Exception {
//		loadNeuroMetaNetwork();
		
//		double v1[] = new double[edgeSPWeights.size()];
//		double v2[] = new double[edgeSPWeights.size()];
//		int idx = 0;
//		for (String s : edgeSPWeights.keySet()) {
//			v1[idx] = edgeSPWeights.get(s);
//			if (!chemicalWeights.containsKey(s)) {
//				System.out.println(s + "\t" + edgeSPWeights.get(s));
//				System.exit(0);
//			}
//			v2[idx] = chemicalWeights.get(s);
//			System.out.println(v1[idx] + "\t" + v2[idx]);
//			System.out.println(v1[idx]);
//			idx++;
//		}
//		KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
//		System.out.println(kendallsCorrelation.correlation(v1, v2));
			
		Scanner scanner;
//		HashSet<String> fbEdges = new HashSet();
//		Scanner scanner = new Scanner(new File("celegans//fb_edges.txt"));
//		while (scanner.hasNext()) {
//			String edg = scanner.next();
//			fbEdges.add(edg);
//			System.out.println(weights.get(edg));
//		}
//		scanner.close();
		
//		scanner = new Scanner(new File("celegans//all_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_k_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_k_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_4_path.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path_weighted.txt"));
		
		
		scanner = new Scanner(new File(pathFile));
//		scanner = new Scanner(new File("celegans//all_4_path.txt"));
		
		HashMap<Integer, Integer> SPLengthFreq = new HashMap();
		HashSet<String> SPInter = new HashSet();
		HashSet<String> shortestPathEdge = new HashSet();
		HashMap<String, Integer> shortestPathEdgeFrequency = new HashMap();
		HashMap<String, Integer> lengthSP = new HashMap();
		int containsBackPath = 0;
		ArrayList<Integer> pathLengthList = new ArrayList();
		HashMap<String, Integer> spFreqByPair = new HashMap();
		int k = 0;
		HashSet<String> traversedFFEdges = new HashSet();
		HashSet<String> traversedLTEdges = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] tokens = line.split("\\s+");			
			int num = tokens.length - 1;
//			System.out.println(num);
			if (SPLengthFreq.containsKey(num)) {
				SPLengthFreq.put(num, SPLengthFreq.get(num) + 1);
			}
			else SPLengthFreq.put(num, 1);
			pathLengthList.add(num);
//			if (num > 4) continue;
//			System.out.println(line);
			boolean containsBack = false;
			String prev = "";
			String src = "";
			String tgt = "";
			for (int i = 0; i < tokens.length; ++i) {
				if (i == 0) {
					src = tokens[i];
				}
				if (i == tokens.length - 1) {
					tgt = tokens[i];
				}
				String r = tokens[i];
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
					if (ltEdges.contains(edg)) {
						traversedLTEdges.add(edg);
					}
					if (ffEdges.contains(edg)) {
						traversedFFEdges.add(edg);
					}
				}
				prev = r;
			}
			
//			if (containsBack) containsBackPath++;
//			break;
			
			String pairSM = src + "#" + tgt; 
			lengthSP.put(pairSM, num);
			addFrequencyValuedMap(spFreqByPair, pairSM);
//			if (!smPairSPLen.containsKey(pairSM)) {
//				System.out.println(line + " -- " + pairSM);
////				break;
//			}
			++k;
		}
		
//		System.out.println(smPairSPLen.size() + "\t" + lengthSP.size());
		
		for (int i : SPLengthFreq.keySet()) {
//			System.out.println(i + "\t" + SPLengthFreq.get(i));
		}
		
		for (String s : shortestPathEdgeFrequency.keySet()) {		
//			System.out.println(s + "\t" + shortestPathEdgeFrequency.get(s) + "\t" + weights.get(s));
//			ArrayList<String> edg = splitEdge(s);
//			System.out.println(edg.get(0) + "\t" + edg.get(1) + "\t" + shortestPathEdgeFrequency.get(s));
//			System.out.println(shortestPathEdgeFrequency.get(s));
		}
		
		for (String s : lengthSP.keySet()) {
//			System.out.println(s + "\t" + lengthSP.get(s));
		}
		
		double values[] = new double[pathLengthList.size()];
		int idx = 0;
		for (int v : pathLengthList) {
			values[idx++] = v;
		}
		System.out.println(k);
		System.out.println(StatUtils.percentile(values, 10) + "\t" + StatUtils.percentile(values, 50) + "\t" + StatUtils.percentile(values, 90));
		System.out.println(lengthSP.size() * 1.0 / 9592.0);
		System.out.println(shortestPathEdge.size() * 1.0 / 1899.0);
		System.out.println(traversedFFEdges.size() * 1.0 / ffEdges.size());
		System.out.println(traversedLTEdges.size() * 1.0 / ltEdges.size());
//		System.out.println(k);
//		System.out.println(SPInter.size());
//		System.out.println(containsBackPath);
		
		HashMap<Integer, Integer> dummy = new HashMap();
		for (String s : spFreqByPair.keySet()) {
			addFrequencyValuedMap(dummy, spFreqByPair.get(s));
		}
		for (int i : dummy.keySet()) {
//			System.out.println(i + "\t" + (dummy.get(i) / 9592.0));
		}
	}
	
	private static void computeFlatCore() throws Exception {
		Scanner scanner;
//		= new Scanner(new File("celegans//gap_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp_weighted.txt"));
//		Scanner scanner = new Scanner(new File("celegans//full_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost_sp_len_restrict.txt"));
		
		
		scanner = new Scanner(new File(pathFile));
//		scanner = new Scanner(new File("celegans//all_4_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_4_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_4_path.txt"));
//		scanner = new Scanner(new File("celegans//all_4_path_gap.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path_chem_gap.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path_weighted.txt"));
		
		HashSet<String> sPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] path = line.split("\\s+");
//			if (path.length > 21) continue;
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
						} 
						else {
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
	
	private static void pathHourglassAnalysis() throws Exception {
//		loadNeuroMetaNetwork(); // turned off for KAMAL
		
		Scanner scanner;
//		= new Scanner(new File("celegans//gap_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp_weighted.txt"));
//		Scanner scanner = new Scanner(new File("celegans//full_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost-2.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost_sp_len_restrict.txt"));
		
//		HashSet<String> largestWCCNodes = new HashSet();
//		scanner = new Scanner(new File("data/largestWCC-rat.txt"));
//		while (scanner.hasNext()) {
//			largestWCCNodes.add(scanner.next());
//		}
//		scanner.close();
		
		
//		scanner = new Scanner(new File("celegans//all_4_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_4_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_4_path.txt"));
//		scanner = new Scanner(new File("celegans//all_4_path_gap.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path_chem_gap.txt"));
//		scanner = new Scanner(new File("celegans//all_LTM_path_weighted.txt"));
		
		HashSet<String> checkNode = new HashSet();
		boolean checkNodeFlag = false;
		
		
		HashMap<String, Integer> pairShortestPathLengthMap = new HashMap();
		scanner = new Scanner(new File(pathFile));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] path = line.split("\\s+");
			String pathTarget = path[path.length - 1];
			String pathSource = path[0];
			String pair = pathSource + "#" + pathTarget;
			if (pairShortestPathLengthMap.containsKey(pair)) {
				int curLen = pairShortestPathLengthMap.get(pair);
				if (path.length < curLen) {
					pairShortestPathLengthMap.put(pair, path.length);
				}
			}
			else {
				pairShortestPathLengthMap.put(pair, path.length);
			}
		}
		
		HashSet<String> sPaths = new HashSet();
		HashMap<String, Integer> targetPathCountMap = new HashMap();
		HashMap<String, Integer> targetCoveredPathCountMap = new HashMap();
		int numPaths = 0;
		scanner = new Scanner(new File(pathFile));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] path = line.split("\\s+");
//			if (path.length > 21) continue;
			String pathTarget = path[path.length - 1];
			String pathSource = path[0];
			addFrequencyValuedMap(targetPathCountMap, pathTarget);
//			for (String r : path) {
//				if (largestWCCNodes.contains(r)) {
//					sPaths.add(line);
//					break;
//				}
//			}
//			String pair = pathSource + "#" + pathTarget;
//			if (path.length > pairShortestPathLengthMap.get(pair) + 1) {
//				continue;
//			}
			sPaths.add(line);
			++numPaths;
			
		}
		scanner.close();
		
//		int idx = 1;
//		int numPaths = 0;
////		int cutLen = 5;
//		for (String line : sPaths) {
//			String[] tokens = line.split("\\s+");
////			if (tokens.length >= cutLen) continue;
//			++numPaths;
//		}
//		int sizeSP = canonicalPaths.size();
//		int size = (int)(sizeSP * (1.0 - tau));
//		double startSize = sPaths.size();
		
		realCoreSize = 0;
		double cumPathCover = 0;
//		System.out.println(sPaths.size() + "\t" + numPaths);
//		if(true) System.exit(0);
		while (true) {
			HashMap<String, Integer> maxPCentrality = new HashMap();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				if (tokens.length >= cutLen) continue;
//				System.out.println(line + " ## " + tokens.length);
				for (String r : tokens) {
					if (maxPCentrality.containsKey(r)) {
						maxPCentrality.put(r, maxPCentrality.get(r) + 1);
					}
					else {
						maxPCentrality.put(r, 1);
					}
					if (!checkNodeFlag) {
						checkNode.add(r);
					}
				}
//				break;
			}
			
			if (!checkNodeFlag) {
//				System.out.println(checkNode.size());
				checkNodeFlag = true;
			}
			
//			double pathCentrality[] = new double[maxSPCentrality.size()];
//			int pCIdx = 0;
//			for (String k : maxSPCentrality.keySet()) {
//				pathCentrality[pCIdx++] = maxSPCentrality.get(k);
//			}
//			Util.getCCDF(pathCentrality);
			
//			for (String k : maxPCentrality.keySet()) {
////				System.out.println(maxPCentrality.get(k) + "\t" + sumStrength.get(k));
//				System.out.println(maxPCentrality.get(k) + "\t" + sumDeg.get(k));
//			}
//			if (true) break;
			
//			for (String[] tokens : canonicalPaths) {
//				for (String r : tokens) {
//					if (maxSPCentrality.containsKey(r)) {
//						maxSPCentrality.put(r, maxSPCentrality.get(r) + 1);
//					}
//					else {
//						maxSPCentrality.put(r, 1);
//					}
//				}
//			}
			
//			System.exit(0);
			int max = 0;
			HashSet<String> maxSPCNeurons = new HashSet();
			for (String v : maxPCentrality.keySet()) {
				if (maxPCentrality.get(v) > max) {
					maxSPCNeurons.clear();
					maxSPCNeurons.add(v);
					max = maxPCentrality.get(v);
				}
				else if (maxPCentrality.get(v) == max) {
					maxSPCNeurons.add(v);
				}
			}
			
			cumPathCover += max;
//			System.out.println((idx++) + "\t" + cumPathCover / sizeSP);
			for (String v : maxSPCNeurons) {
//				System.out.print(idNeuron.get(v) + "  " + (max * 1.0 / (numPaths * 0.9)) +  "\t");
//				System.out.print(v + "\t" + idNeuron.get(v) + "\t" + inDeg.get(v) + "\t" + outDeg.get(v));
//				System.out.print(idNeuron.get(v) + "\t" + getType(v) + "\t" + max);
//				System.out.print(v + "  " + (max * 1.0 / (numPaths * 0.9)) + "\t");
				System.out.println(v + "\t" + (cumPathCover / numPaths) + "\t" + sPaths.size());
//				System.out.print(nodePathWeights.get(v) * 1.0 / numPaths);
			}
//			System.out.println();
//			System.out.println("\n-- -- -- -- --");
			
			HashSet<String> removeSPaths = new HashSet();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				System.out.println(line);
				for (String r : tokens) {
					if (maxSPCNeurons.contains(r)) {
						String pathTarget = tokens[tokens.length - 1];
						addFrequencyValuedMap(targetCoveredPathCountMap, pathTarget);
						removeSPaths.add(line);
					}
				}
			}

			sPaths.removeAll(removeSPaths);
			++realCoreSize;
			
//			if (sPaths.size() <= size) break;
//			System.out.println(cumPathCover + "\t" + (sizeSP * tau));
			if (sPaths.size() <= 0) break;
			if (cumPathCover >= numPaths * tau) break;
		}
		
		for (String s : target) {
			int targetPathCount = 0;
			int targetCoveredPathCount = 0;
			if (targetPathCountMap.containsKey(s)) targetPathCount = targetPathCountMap.get(s);
			if (targetCoveredPathCountMap.containsKey(s)) targetCoveredPathCount = targetCoveredPathCountMap.get(s);
//			System.out.println(s + "\t" + targetPathCount + "\t" + targetCoveredPathCount + "\t" + (targetCoveredPathCount * 1.0 / targetPathCount));
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
		HashSet<String> spEdges = new HashSet();
		HashSet<String> fbEdges = new HashSet();
		
		Scanner scan;
//		Scanner scan = new Scanner(new File("celegans//sp_edges.txt"));
//		while (scan.hasNext()) {
//			String src = scan.next();
//			String dst = scan.next();
//			String edg = src + "#" + dst;
//			String spWeight = scan.next();
//			spEdges.add(edg);
//		}
//		scan.close();
		
//		scan = new Scanner(new File("celegans//fb_edges.txt"));
//		int knt = 0;
//		while (scan.hasNext()) {
//			String edg = scan.next();
//			fbEdges.add(edg);
//			ArrayList<String> edgeNodes = splitEdge(edg);
//			if (coreNeurons.contains(edgeNodes.get(0)) || coreNeurons.contains(edgeNodes.get(1))) {
//				System.out.println(edg);
//				++knt;
//			}
//		}
//		scan.close();
//		System.out.println(knt);
		
//		scan = new Scanner(new File("celegans//fb_clean_links.txt"));
		HashMap<String, Integer> coreIndeg = new HashMap();
		HashMap<String, Integer> coreOutdeg = new HashMap();
		scan = new Scanner(new File("celegans//all_links.txt"));
		int knt = 0;
		int sum1 = 0;
		int sum2 = 0;
		HashMap<String, Integer> srId = new HashMap();
		int idx = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			String edg = src + "#" + dst;
			if (coreNeurons.contains(src) && coreNeurons.contains(dst)) {
				
				if (edgePathWeights.containsKey(edg)) {
//					System.out.println(weights.get(edg));
//					++knt;
//					System.out.println(edg + "\t" + edgeSPWeights.get(edg));
//					System.out.println(src + "<" + dst + "\t" + edgeSPWeights.get(edg));
//					System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(dst) + "\t" + (edgeSPWeights.get(edg)));
//					System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(dst) + "\t" + (edgeSPWeights.get(edg) / 3168.0));
					if (hierarchy.get(dst) < hierarchy.get(src)) {
						System.out.println(edg);
						sum1 += edgePathWeights.get(edg);
						++knt;
					}
					else {
						sum2 += edgePathWeights.get(edg);
					}
				}
				else {
//					System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(dst));
					
				}
				
				if (fbEdges.contains(edg)) {
//					System.out.println(weights.get(edg));
//					++knt;
				}
//				++knt;
//				System.out.println(edg);
				addFrequencyValuedMap(coreIndeg, dst);
				addFrequencyValuedMap(coreOutdeg, src);
				
				if (!srId.containsKey(src)) {
					srId.put(src, idx++);
				}
				if (!srId.containsKey(dst)) {
					srId.put(dst, idx++);
				}
//				System.out.println(srId.get(src) + "\t" + srId.get(dst));
			}
		}
		scan.close();
		System.out.println(knt + "\t" + sum1 + "\t" + sum2);
		
//		for (String s : coreNeurons) {
//			System.out.println(idNeuron.get(s)  + "\t" + (coreIndeg.get(s) * 1.0 / coreOutdeg.get(s)));
//		}
		
		for (String s : srId.keySet()) {
//			System.out.println(srId.get(s) + "\t" + s + "\t" + idNeuron.get(s));
		}
	}
	
	private static void addFrequencyValuedMap(Map<String, Integer> hmap, String key) {
		if (hmap.containsKey(key)) {
			hmap.put(key, hmap.get(key) + 1);
		}
		else {
			hmap.put(key, 1);
		}
	}
	
	private static void addFrequencyValuedMap(Map<String, Double> hmap, String key, double value) {
		if (hmap.containsKey(key)) {
			hmap.put(key, hmap.get(key) + value);
		}
		else {
			hmap.put(key, value);
		}
	}
	
	private static void addFrequencyValuedMap(Map<Integer, Integer> hmap, int key) {
		if (hmap.containsKey(key)) {
			hmap.put(key, hmap.get(key) + 1);
		}
		else {
			hmap.put(key, 1);
		}
	}
	
	private static void feedbackHypothesis() throws Exception {
		loadNeuroMetaNetwork();
		
		HashSet<String> spEdges = new HashSet();
		HashSet<String> fbEdges = new HashSet();
		HashSet<String> ffEdges = new HashSet();
		Scanner scan = null;
		HashMap<String, Integer> neuronFbIn = new HashMap();
		HashMap<String, Integer> neuronFbOut = new HashMap();
		
		scan = new Scanner(new File("celegans//sp_edges.txt"));
		while (scan.hasNext()) {
			String edg = scan.next();
			spEdges.add(edg);
			ArrayList<String> edgeNodes = splitEdge(edg);
//			System.out.println(edgeNodes.get(1) + "#" + edgeNodes.get(0));
		}
		scan.close();
		
//		scan = new Scanner(new File("celegans//sp_edges.txt"));
//		int knt = 0;
//		while (scan.hasNext()) {
//			String edg = scan.next();
//			ArrayList<String> edgeNodes = splitEdge(edg);
//			if (spEdges.contains(edgeNodes.get(1) + "#" + edgeNodes.get(0))) {
//				System.out.println(edg);
//				++knt;
//			}
//		}
//		System.out.println(knt);
//		scan.close();
		
		scan = new Scanner(new File("celegans//fb_edges.txt"));
		while (scan.hasNext()) {
			String edg = scan.next();
			fbEdges.add(edg);
			ArrayList<String> edgeNodes = splitEdge(edg);
			/*
			addFrequencyValuedMap(neuronFbIn, edgeNodes.get(1));
			addFrequencyValuedMap(neuronFbOut, edgeNodes.get(0));
			*/
		}
		scan.close();
		
		scan = new Scanner(new File("celegans//fb_clean_links.txt"));
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			String edg = src + "#" + dst;
			ffEdges.add(edg);
			if (!spEdges.contains(edg)) {
//				System.out.println(edg);
//				addMap(neuronFbIn, dst);
//				addMap(neuronFbOut, src);
			}
//			System.out.println(src + "\t" + dst + "\t" + (1.0 / weights.get(edg)));
		}
		scan.close();
		
		for (String s : neuronFbIn.keySet()) {
//			System.out.println(s + "\t" + neuronFbIn.get(s));
		}
		
		for (String s : neuronFbOut.keySet()) {
			System.out.println(s + "\t" + neuronFbOut.get(s));
		}
	}
	
	private static void gapJunctionAnalysis() throws Exception {
		loadNeuroMetaNetwork();		
		HashMap<String, String> gapJunctions = new HashMap();
		HashMap<Integer, Integer> gapJWeightDist = new HashMap();
		HashMap<Integer, Integer> wMap = new HashMap();
		HashSet<String> gapJNeuron = new HashSet();
		Scanner scan = null;
		scan = new Scanner(new File("celegans//celegans_gap_junction.txt"));
		int kntBothWay = 0;
		int weightOneWay = 0;
		int onlyGap = 0;
		int sureFB = 0;
		int categoryKnt = 0;
		double gapWeights[] = new double[517];
		int idx = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			String srcId = neuronId.get(src);
			String dstId = neuronId.get(dst);
			String typ = scan.next();
			String weight = scan.next();
			String fEdge = src + "#" + dst;
			String bEdge = dst + "#" + src;
			String fEdgeId = srcId + "#" + dstId;
			String bEdgeId = dstId + "#" + srcId;
			gapJNeuron.add(src);
			gapJNeuron.add(dst);
			
//			System.out.println(srcId + "\t" + dstId);
			
			if (nodes.contains(srcId) && nodes.contains(dstId)) {
				// fb clean
				if (target.contains(srcId) && inter.contains(dstId)) ;
				else if (target.contains(srcId) && source.contains(dstId)) ;
				else if (inter.contains(srcId) && source.contains(dstId)) ;
				else System.out.println(srcId + "\t" + dstId);
			}
			
			
			
			int w = 0;
			if (chemicalWeights.containsKey(fEdgeId)) {
				w = chemicalWeights.get(fEdgeId);
			}
			if (wMap.containsKey(w)) {
				wMap.put(w,  wMap.get(w) + 1);
			}
			else {
				wMap.put(w, 1);
			}
			
			
			if (!gapJunctions.containsKey(bEdge)) {
				gapJunctions.put(fEdge, weight);
				int wgt = Integer.parseInt(weight);
				weightOneWay += wgt;
				if (!chemicalWeights.containsKey(fEdgeId) && !chemicalWeights.containsKey(bEdgeId)) {
//					System.out.println(fEdge);
					++onlyGap;
				}
				gapWeights[idx++] = wgt;
				if (gapJWeightDist.containsKey(wgt)) {
					gapJWeightDist.put(wgt, gapJWeightDist.get(wgt) + 1);
				}
				else gapJWeightDist.put(wgt, 1);
			}
			else {
				if (!gapJunctions.get(bEdge).equals(weight)) {
//					System.out.println(fEdge + "\t" + weight + "\t" + bEdge + "\t" + gapJunctions.get(bEdge));
				}
				++kntBothWay;
			}
			
			if (target.contains(srcId) && (inter.contains(dstId) || source.contains(dstId))) ++sureFB;
			if (inter.contains(srcId) && source.contains(dstId)) ++sureFB;
			
			
//			if (/*!nodes.contains(srcId) ||*/ !nodes.contains(dstId)) {
//				System.out.println(fEdge);
//				System.out.println(dst);
//			}
			
//			if (target.contains(srcId) && target.contains(dstId)) ++categoryKnt;
		}
		scan.close();
//		System.out.println("Both way: " + kntBothWay);
//		System.out.println("Weight one way: " + weightOneWay);
//		System.out.println("Total gap junctions: " + gapJunctions.size());
//		System.out.println("Only gap edge: " + onlyGap);
//		System.out.println("Feedback gap edge: " + sureFB);
//		System.out.println("Gap J Neurons: " + gapJNeuron.size());
//		System.out.println(categoryKnt);
//		System.out.println(StatUtils.percentile(gapWeights, 50) + "\t" + StatUtils.max(gapWeights));
		for (int i : gapJWeightDist.keySet()) {
//			System.out.println(i + "\t" + gapJWeightDist.get(i));
		}
		for (int i : wMap.keySet()) {
//			System.out.println(i + "\t" + wMap.get(i));
		}
	}
	
	private static void degreeAnalysis() throws Exception {
		loadNeuroMetaNetwork();
		
		for (String s : nodes) {
			if (target.contains(s)) {
				int deg = 0;
//				if (inDeg.containsKey(s)) deg = inDeg.get(s);
				if (outDeg.containsKey(s)) deg = outDeg.get(s);
//				System.out.println(deg);
			}
		}
	}
	
	private static void sevenLayerAnalysis() throws Exception {
		loadNeuroMetaNetwork();
		
		int knt = 0;
		for (String s : nodes) {
			if (!inDeg.containsKey(s) && source.contains(s)) {
//			if (!outDeg.containsKey(s) && target.contains(s)) {
				++knt;
			}
		}
//		System.out.println(knt);
		
		HashSet<String> reachableNodes = new HashSet();
		double sumCoreLoc = 0;
		double kntCoreLoc = 0;
		double coreLoc[] = new double[703860];
		int index_1 = 0;
		HashMap<String, ArrayList<Double>> coreSeparateLoc = new HashMap();
		
		for (String[] path : canonicalPaths) {
			boolean flag = false;
			int idx = 0;
			for (String r :  path) {
				if (coreNeurons.contains(r)) {
					flag = true;
					sumCoreLoc += idx;
					++kntCoreLoc;
					coreLoc[index_1++] = idx;
//					break;
					
					if (coreSeparateLoc.containsKey(r)) {
						coreSeparateLoc.get(r).add(idx * 1.0);
					}
					else {
						ArrayList<Double> aList = new ArrayList();
						aList.add(idx * 1.0);
						coreSeparateLoc.put(r, aList);
					}
				}
				++idx;
			}
			if (flag) {
				for (String r : path) {
					reachableNodes.add(r);
				}
			}
		}
		
//		System.out.println(reachableNodes.size());
//		System.out.println(sumCoreLoc / kntCoreLoc);
//		System.out.println(kntCoreLoc);
//		System.out.println(StatUtils.percentile(coreLoc, 90));
//		System.out.println(StatUtils.percentile(coreLoc, 50));
//		System.out.println(StatUtils.percentile(coreLoc, 10));
//		System.out.println(StatUtils.mean(coreLoc));
		
		for (String r : coreNeurons) {
			ArrayList<Double> aList = coreSeparateLoc.get(r);
			double sum = 0;
			for (double d : aList) {
				sum += d;
			}
			System.out.println(idNeuron.get(r) + "\t" + (sum/aList.size()) + "\t" + inDeg.get(r) + "\t" + outDeg.get(r));
//			System.out.
		}
	}
	
	private static void topEdgeNeuronBypass() throws Exception {
		loadNeuroMetaNetwork();
		
//		String[] topEdgeNeuron = {"PVCL", "PVCR", "AVAL", "AVAR", "AVEL", "AVER", "AVDL", "AVDR", "AVBL", "AVBR"};
		String[] topEdgeNeuron = {"262", "268", "48", "56", "59", "67", "119", "117", "97", "106"};
		int knt = 0;
		for (String[] p : canonicalPaths) {
			HashSet<String> hs = new HashSet(Arrays.asList(p));
			for (String s : topEdgeNeuron) {
				if (hs.contains(s)) {
					++knt;
					break;
				}
			}
		}
		
		System.out.println(knt / 433572.0);
	}
	
	private static void corePairwiseWeight() throws Exception {
		ArrayList<String> coreList = new ArrayList(coreNeurons);
		for (int i = 0; i < coreNeurons.size(); ++i) {
			for (int j = i + 1; j < coreNeurons.size(); ++j) {
				String p = coreList.get(i);
				String q = coreList.get(j);
				for (String[] path : canonicalPaths) {
					int pi = -1;
					int qi = -1;
					for (int k = 0; k < path.length; ++k) {
						if (path[k].equals(p)) pi = k;
						if (path[k].equals(q)) qi = k;
					}
					if (pi != -1 && qi !=- 1) {
						if (pi > qi) {
//							if (pi == qi + 1) {
								addFrequencyValuedMap(pairWeight, q + "#" + p);
//							}
						}
						else {
//							if (qi == pi + 1) {
								addFrequencyValuedMap(pairWeight, p + "#" + q);
//							}
						}
					}
				}
			}
		}
		
		for (String s : pairWeight.keySet()) {
//			System.out.println(s + "\t" + pairWeight.get(s));
		}
		
		
		double d = 1;
		int idx = 0;
		HashMap<String, Integer> orderedId = new HashMap();
		HashSet<String> relationships = new HashSet();
		int knt = 0;
		for (int i = 0; i < coreNeurons.size(); ++i) {
			for (int j = i + 1; j < coreNeurons.size(); ++j) {
				String p = coreList.get(i);
				String q = coreList.get(j);
				if (!orderedId.containsKey(p)) orderedId.put(p, idx++);
				if (!orderedId.containsKey(q)) orderedId.put(q, idx++);
				String pq = p + "#" + q;
				String qp = q + "#" + p;
				int pqw = 0;
				int qpw = 0;
				if (pairWeight.containsKey(pq)) pqw = pairWeight.get(pq);
				if (pairWeight.containsKey(qp)) qpw = pairWeight.get(qp);
				/*
				if (pqw != 0 && qpw != 0) {
					double pqByqp = pqw * 1.0 / qpw;
//					System.out.println(pq + "\t" + Math.max(pqByqp, 1.0 / pqByqp) + "\t" + Math.max(pqw, qpw));
					if (pqByqp >= (1.0 - d) && pqByqp <= (1 + d)) {
//						System.out.println(orderedId.get(p) + "  " + orderedId.get(q));
//						System.out.println(orderedId.get(q) + "  " + orderedId.get(p));
//						System.out.println(p + "=" + q + "\t" + Math.max(pqw, qpw));
//						System.out.println(p + "<" + q + "\t" + pqw);
//						System.out.println(q + "<" + p + "\t" + qpw);
						if (hierarchy.get(p) != hierarchy.get(q)) {
//							System.out.println(p + "=" + q);
							++knt;
						}
					}
					else if (pqByqp < (1.0 - d)) {
//						System.out.println(orderedId.get(q) + "  " + orderedId.get(p));
//						System.out.println(q + "<" + p + "\t" + qpw);
						if (hierarchy.get(p) <= hierarchy.get(q)) {
//							System.out.println(q + "<" + p);
							++knt;
						}
					}
					else {
//						System.out.println(orderedId.get(p) + "  " + orderedId.get(q));
//						System.out.println(p + "<" + q + "\t" + pqw);
						if (hierarchy.get(p) >= hierarchy.get(q)) {
//							System.out.println(p + "<" + q);
							++knt;
						}
					}
				}
				else if (pqw != 0) {
//					System.out.println(orderedId.get(p) + "  " + orderedId.get(q));
//					System.out.println(p + "<" + q + "\t" + pqw);
					if (hierarchy.get(p) >= hierarchy.get(q)) {
//						System.out.println(p + "<" + q);
						++knt;
					}
				}
				else if (qpw != 0) {
//					System.out.println(orderedId.get(q) + "  " + orderedId.get(p));
//					System.out.println(q + "<" + p + "\t" + qpw);
					if (hierarchy.get(p) <= hierarchy.get(q)) {
//						System.out.println(q + "<" + p);
						++knt;
					}
				}
				else {
					
				}
				*/
				if (pqw != 0 || qpw != 0) {
					double weightRatio = Math.min(pqw, qpw) * 1.0 / Math.max(pqw, qpw);
					if (pqw > qpw) {
						System.out.println(p + "<" + q + "\t" + weightRatio);
					}
					else {
						System.out.println(q + "<" + p + "\t" + weightRatio);
					}
				}
				
//				System.out.println(p + "#" + q + "\t" + pqw + "\t" + qpw);
			}
		}
		
		for (String s : orderedId.keySet()) {
//			System.out.println(orderedId.get(s) + "\t" + s);
		}
		
//		System.out.println(knt);
	}
	
	private static void getReducedCoreNetwork() throws Exception {
		loadNeuroMetaNetwork();
		corePairwiseWeight();
		Scanner scan = new Scanner(new File("celegans//fb_clean_links.txt"));
//		Scanner scan = new Scanner(new File("celegans//all_links.txt"));
		HashSet<String> bothWay =  new HashSet();
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			if (coreNeurons.contains(src) && coreNeurons.contains(dst)) {
				String fEdge = src + "#" + dst;
				String bEdge = dst + "#" + src;
				if (!bothWay.contains(bEdge)) {
					bothWay.add(fEdge);
//					continue;
				}
				int fWeight = 0;
				int bWeight = 0;
				if (pairWeight.containsKey(fEdge)) fWeight = pairWeight.get(fEdge);
				if (pairWeight.containsKey(bEdge)) bWeight = pairWeight.get(bEdge);
				if (bWeight != 0) {
					double t = bWeight * 1.0 / fWeight;
//					System.out.print(idNeuron.get(src) + "\t" + idNeuron.get(dst) 
//							+ "\t" + Math.max(bWeight * 1.0 / fWeight, fWeight * 1.0 / bWeight));
//					System.out.println("\t" + fWeight + "\t" + bWeight);
					if (t > 1.5) {
//						System.out.println(idNeuron.get(src) + "\t" + idNeuron.get(dst) + "\t" + t);
						continue;
					}
				}
//				System.out.println(idNeuron.get(src) + "#" + idNeuron.get(dst));
			}
		}
	}
	
	private static void getWeightCorrelation() throws Exception {
		loadNeuroMetaNetwork();
		for (String s : chemicalWeights.keySet()) {
			if (edgePathWeights.containsKey(s)) {
				ArrayList<String> nodes = splitEdge(s);
				System.out.print(chemicalWeights.get(s) + "\t" + edgePathWeights.get(s));
				System.out.println("\t" + nodes.get(0) + "\t" + nodes.get(1));
			}
		}
	}
	
	private static void addSetValuedMap(String key, String value, HashMap<String, HashSet<String>> setValueMap) {
		if (setValueMap.containsKey(key)) {
			setValueMap.get(key).add(value);
		}
		else {
			HashSet<String> hs = new HashSet();
			hs.add(value);
			setValueMap.put(key, hs);
		}
	}
	
	public static void computeDimensionalityReduction() throws Exception {
		loadNeuroMetaNetwork();	
		System.out.println(coreNeurons.size());
		HashMap<String, HashSet<String>> targetCorePathMap = new HashMap();
		HashMap<String, HashSet<String>> coreSourcePathMap = new HashMap();
		HashMap<String, HashSet<String>> targetSourceCoverMap = new HashMap();
		HashMap<String, HashSet<String>> targetCoreCoverMap = new HashMap();
		HashMap<String, HashSet<String>> targetNoCorePathMap = new HashMap();
		HashMap<String, Integer> targetPathCountMap = new HashMap();
		HashMap<String, HashSet<String>> targetPathMap = new HashMap();
		int numTargetNoCorePath = 0;
		for (String line : finalPaths) {
			String[] path = line.split("\\s+");
			if (targetPathMap.containsKey(path[path.length - 1])) {
				targetPathMap.get(path[path.length - 1]).add(line);
			}
			else {
				HashSet<String> hs = new HashSet();
				hs.add(line);
				targetPathMap.put(path[path.length - 1], hs);
			}
			String targetCorePath = path[path.length - 1];
			String coreSourcePath = "";
			String terminalCore = "";
			addFrequencyValuedMap(targetPathCountMap, path[path.length - 1]);
			boolean foundCore = false;
			for (int i = path.length - 2; i >= 0; --i) {
				if (foundCore == false) {
					targetCorePath += "#" + path[i];
				}
				
				if (foundCore == false && coreNeurons.contains(path[i])) {
					foundCore = true;
					terminalCore = path[i];
					coreSourcePath = path[i];
					continue;
				}
				
				if (foundCore == true) {
					coreSourcePath += "#" + path[i];
				}
				// path needs to be reversed if direction needed
				
				if (coreNeurons.contains(path[i])) {
					addSetValuedMap(path[path.length - 1], path[i], targetCoreCoverMap);
				}
				
				if (source.contains(path[i])) {
					addSetValuedMap(path[path.length - 1], path[i], targetSourceCoverMap);
				}
			}
			
			
			if (foundCore == false) { // path visits no core node
				++numTargetNoCorePath;
				addSetValuedMap(path[path.length - 1], targetCorePath, targetNoCorePathMap);
			}
			else {
				addSetValuedMap(terminalCore, coreSourcePath, coreSourcePathMap);
				addSetValuedMap(path[path.length - 1], targetCorePath, targetCorePathMap);
			}
		}
		
		int numCoreSourcePath = 0;
		for (String s : coreSourcePathMap.keySet()) {
			numCoreSourcePath += coreSourcePathMap.get(s).size();
		}
		
		int numTargetCorePath = 0;
		for (String s : targetCorePathMap.keySet()) {
			numTargetCorePath += targetCorePathMap.get(s).size();
		}
		
//		System.out.println(finalPaths.size() + "\t" + numCoreSourcePath + "\t" + numTargetCorePath + "\t" + numTargetNoCorePath);
		
		for (String s : target) {
//			System.out.print(targetPathCountMap.get(s) + "\t");
//			int noCorePath = 0;
//			if (targetNoCorePathMap.containsKey(s)) noCorePath = targetNoCorePathMap.get(s).size();
//			System.out.print(noCorePath + "\t");
//			int corePath = 0;
//			if (targetCorePathMap.containsKey(s)) corePath = targetCorePathMap.get(s).size();
//			System.out.print(corePath + "\t");
////			System.out.print(numCoreSourcePath * 1.0 / coreNeurons.size());
//			System.out.println();
			System.out.print(s + "\t");
			if (targetCoreCoverMap.containsKey(s)) {
				System.out.print(targetCoreCoverMap.get(s).size() + "\t"); 
			}
			else {
				System.out.print("0\t");
			}
			if (targetSourceCoverMap.containsKey(s)) {
				System.out.print(targetSourceCoverMap.get(s).size() + "\t");
			}
			else {
				System.out.print("0\t");
			}
			if (targetPathCountMap.containsKey(s)) {
				System.out.print((targetPathCountMap.get(s) / 3177835.0) + "\t");
			}
			else {
				System.out.print("0.0\t");
			}
			
			if (targetNoCorePathMap.containsKey(s)) {
//				System.out.println(targetNoCorePathMap.get(s).size() / targetPathCountMap.get(s));
				System.out.println(1.0 - (targetNoCorePathMap.get(s).size() * 1.0 / targetPathCountMap.get(s)));
			}
			else {
				if (targetPathCountMap.containsKey(s)) System.out.println("1.0");
				else System.out.println("0.0");
//				System.out.println("here");
			}
		}
		
		System.out.println("---- ---- ----- \n");
		
		for (String s : target) {
			int maxCoreIndex = -1;
			double targetPathCount = 0;
			if (targetPathCountMap.containsKey(s)) targetPathCount = targetPathCountMap.get(s);
			double coveredPathCount = 0;
			if (targetPathMap.containsKey(s)) {
				for (int i = 0; i < orderedCore.size(); ++i) {
					for (String line : targetPathMap.get(s)) {
						if (line.contains(orderedCore.get(i))) {
							++coveredPathCount;
						}
					}
					if (coveredPathCount >= targetPathCount * 0.9) {
						maxCoreIndex = i;
						break;
					}
				}
			}
			System.out.println(s + "\t" + (maxCoreIndex + 1));
		}
		
//		for (String s : coreNeurons) {
//			System.out.println(coreSourcePathMap.get(s).size());
//		}
	}
	
	private static void analyzeHierarchy() throws Exception {
		loadNeuroMetaNetwork();
		
//		System.out.println(source.size());
		HashSet<String> used = new HashSet();
		ArrayList<ArrayList<Integer>> allHeats = new ArrayList();
		int index = 0;
		while (used.size() < source.size()) {
			int max = Integer.MIN_VALUE;
			String maxNode = "##";
			ArrayList<Integer> maxHeats = new ArrayList();
			int usedKount = 0;
			for (String s : source) {
				if (used.contains(s)) {
					++usedKount;
					continue;
				}
//				System.out.print(s + "-");
				int sum = 0;
				ArrayList<Integer> heats = new ArrayList();
				for (String r : source) {
					int forward = 0;
					int backward = 0;
					if (edgePathWeights.containsKey(s + "#" + r)) {
						forward = edgePathWeights.get(s + "#" + r);
					}
					if (edgePathWeights.containsKey(r + "#" + s)) {
						backward = edgePathWeights.get(r + "#" + s);
					}
					sum += (forward - backward);
					heats.add(forward - backward);
				}
//				System.out.print(sum + " ");
				if (sum > max) {
					max = sum;
					maxNode = s;
					maxHeats = new ArrayList(heats);
//					System.out.println("  newmax " + s + "\t" + max);
				}
			}
//			System.out.println();
			used.add(maxNode);
			allHeats.add(maxHeats);
			System.out.println(max);
//			System.out.println(maxNode  + "\t" + max);
//			System.out.println(used.size() + "\t" + source.size());
		}
		
		for (ArrayList<Integer> aList : allHeats) {
//			System.out.print(aList.get(0));
			for (int i = 1; i < aList.size(); ++i) {
//				System.out.print(",");
//				System.out.print(aList.get(i));
			}
//			System.out.println();
		}
	}
	
	private static void traverseAllPathsHelper(String node, String targetNode, int len, DependencyDAG dependencyDAG, ArrayList<String> pathNodes) {
//		if (pathNodes.size() > len + 2) return; // +k hop than shortest path
		if (pathNodes.size() > 6) return; // special case length restriction
		
		if (node.equals(targetNode)) {
			for (String s: pathNodes) {
				System.out.print(s + " ");
//				randomPW.print(s + " ");
			}
			System.out.println();
//			randomPW.println();
			return;
		}
		

		if (!dependencyDAG.serves.containsKey(node)) return;

//		System.out.print("here for " + node + " target " + targetNode + " next " + dependencyDAG.serves.get(node).size());
//		System.out.println(" " + pathNodes.size() + " " + len);
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			pathNodes.add(s);
			traverseAllPathsHelper(s, targetNode, len, dependencyDAG, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private static void traverseAllPaths() throws Exception {
		DependencyDAG.isToy = true;
//		String net = "h6";
//		String net = "rat";
//		DependencyDAG dependencyDAG = new DependencyDAG("data//" + currentNet + "_links.txt");
		DependencyDAG dependencyDAG = new DependencyDAG("celegans//fb_clean_links.txt");
//		dependencyDAG.printNetworkProperties();
//		dependencyDAG.printNetworkStat();
		
//		loadMetaNetwork();
//		loadNeuroMetaNetwork();
		
		for (String s : source) {
			for (String r : target) {
				if (!smPairSPLen.containsKey(s + "#" + r)) {
//						System.out.println("Here for " + s + "#" + r);
						continue;
				}
//				if (smPairSPLen.get(s + "#" + r) > 7) continue;
				ArrayList<String> pathNodes = new ArrayList();
				pathNodes.add(s);
//				traverseAllPathsHelper(s, r, -1, dependencyDAG, pathNodes);
//				System.out.println("doing: " + s + " " + r);
				traverseAllPathsHelper(s, r, smPairSPLen.get(s + "#" + r) + 1, dependencyDAG, pathNodes);
//				break;
			}
//			break;
		}
		
//		for (String s : dependencyDAG.sources) {
//			for (String r : dependencyDAG.targets) {
//				if (!smPairSPLen.containsKey(s + "#" + r)) continue;
//				ArrayList<String> pathNodes = new ArrayList();
//				pathNodes.add(s);
//				traverseAllPathsHelper(s, r, smPairSPLen.get(s + "#" + r), dependencyDAG, pathNodes);
////				System.out.println("doing: " + s + " " + r);
//			}
//		}
	}
	
	private static void bfsSP() throws Exception {
		DependencyDAG.isToy = true;
//		String net = "celegans";
//		String net = "random.network";
		DependencyDAG dependencyDAG = new DependencyDAG("celegans//fb_clean_links.txt");
//		DependencyDAG dependencyDAG = new DependencyDAG("celegans//gap+chemical_clean_links.txt");
//		DependencyDAG dependencyDAG = new DependencyDAG("data//" + currentNet + "_links.txt");
//		loadNeuroMetaNetwork();
		HashSet<String> totalVisited = new HashSet();
		HashMap<String, Integer> minDepth = new HashMap();
		TreeMap<Integer, Integer> pathLenFreq = new TreeMap();
		smPairSPLen.clear();
		
		double sum = 0;
//		for (String s: dependencyDAG.sources) {
		for (String s : source) {
			Queue<String> bfsQ = new LinkedList();
			Queue<Integer> depthQ = new LinkedList();
			HashSet<String> visited = new HashSet();
			
			bfsQ.add(s);
			depthQ.add(0);
			visited.add(s);
			
			while (!bfsQ.isEmpty()) {
				String n = bfsQ.poll();
				int depth = depthQ.poll();
				
//				totalVisited.add(n);
//				if (minDepth.containsKey(n)) {
//					int cDepth = minDepth.get(n);
//					if (depth < cDepth) {
//						minDepth.put(n, depth);
//					}
//				}
//				else {
//					minDepth.put(n, depth);
//				}
				
//				if (dependencyDAG.targets.contains(n)) {
				if (target.contains(n)) {
					String pair = s + "#" + n;
					smPairSPLen.put(pair, depth);
					System.out.println(pair + "\t" + depth);
//					System.out.println(depth);
					addFrequencyValuedMap(pathLenFreq, depth);
					++sum;
				}
				
				if (dependencyDAG.serves.containsKey(n)) {
					for (String r : dependencyDAG.serves.get(n)) {
						if (!visited.contains(r)) {
							bfsQ.add(r);
							depthQ.add(depth + 1);
							visited.add(r);
						}
					}
				}
			}
		}
		
//		System.out.println(totalVisited.size());
//		for (String s : maxDepth.keySet()) {
//			System.out.println(s + "\t" + maxDepth.get(s));
//		}
		
		double cumSum = 0;
		for (int s : pathLenFreq.keySet()) {
//			System.out.println(s + "\t" + pathLenFreq.get(s));
			cumSum += pathLenFreq.get(s);
//			System.out.println(s + "\t" + (cumSum / sum));
		}
 	}
	
	private static void getDerivedPaths() throws Exception {
		Scanner scanner = new Scanner(new File("celegans//sm_pair_sp_len.txt")); // this file is in hops
		while (scanner.hasNext()) {
			String smPair = scanner.next();
			int spLen = scanner.nextInt();
			smPairSPLen.put(smPair, spLen);
		}
		scanner.close();
		
		scanner = new Scanner(new File("celegans//all_SP+2.txt"));
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] tokens = line.split("\\s+");
			int hopLen = tokens.length - 1;
			String prev = "";
			String src = "";
			String tgt = "";
			for (int i = 0; i < tokens.length; ++i) {
				if (i == 0) {
					src = tokens[i];
				}
				if (i == tokens.length - 1) {
					tgt = tokens[i];
				}
				String r = tokens[i];
				if (prev != "") {
					String edg = prev + "#" + r;
				}
				prev = r;
			}
			String pair = src + "#" + tgt;
//			if (smPairSPLen.containsKey(pair)) {
//				if (smPairSPLen.get(pair) == hopLen || (smPairSPLen.get(pair) + 1) == hopLen) {
//					System.out.println(line);
//				}
//			}
//			else {
//				System.out.println("error");
//			}
			if (hopLen <= 6) {
				System.out.println(line);
			}
		}		
	}
	
	private static void analyzeEdge() throws Exception {
		loadNeuroMetaNetwork();
		Scanner scanner = new Scanner(new File("celegans//celegans_edge_category.txt"));
		HashMap<String, String> edgeCategory = new HashMap();
		HashMap<String, Double> edgeWeight = new HashMap();
		while (scanner.hasNext()) {
			String edge = scanner.next();
			String category = scanner.next();
			double wgt = scanner.nextDouble();
			edgeCategory.put(edge, category);
			edgeWeight.put(edge,  wgt);
		}
		
		HashSet<String> processed = new HashSet();
		for (String s : nodes) {
			for (String r : nodes) {
				if (s.equals(r)) continue;
				String edgeA = s + "#" + r;
				String edgeB = r + "#" + s;
				if (processed.contains(edgeA) || processed.contains(edgeB)) continue;
				if (edgeCategory.containsKey(edgeA) && edgeCategory.containsKey(edgeB)) {
					String catA = edgeCategory.get(edgeA);
					String catB = edgeCategory.get(edgeB);
					double wgtA = edgeWeight.get(edgeA);
					double wgtB = edgeWeight.get(edgeB);
					if (catA.equals("lt") && catB.equals("lt")) {
//						System.out.println(Math.max(wgtA, wgtB) / Math.min(wgtA, wgtB));
					}
					else if (catA.equals("ff") && catB.equals("fb")) {
//						System.out.println(wgtA / wgtB);
						System.out.println((Math.max(wgtA, wgtB) - Math.min(wgtA, wgtB)) / Math.max(wgtA, wgtB));
					}
					else if (catA.equals("fb") && catB.equals("ff")) {
//						System.out.println(wgtB / wgtA);
						System.out.println((Math.max(wgtA, wgtB) - Math.min(wgtA, wgtB)) / Math.max(wgtA, wgtB));
					}
					else {
//						System.out.println("Error" + catA + '\t' + catB);
					}
					
					processed.add(edgeA);
					processed.add(edgeB);
				}
			}
		}
	}
	
	private static void getFbCleanNetwork() throws Exception {
		loadNeuroMetaNetwork();
		// for chemical
		Scanner scanner = new Scanner(new File("celegans//celegans_graph.txt"));
		// for gap+chemical
//		Scanner scanner = new Scanner(new File("celegans//gap+chemical_links.txt"));
		int lateralKount = 0;
		HashMap<String, Double> inProp = new HashMap();
		HashMap<String, Double> outProp = new HashMap();
		HashMap<String, Integer> edgeCount = new HashMap();
		int kount = 0;
		HashMap<String, Double> reverseFBWeights = new HashMap();
		HashMap<String, Double> FFWeights = new HashMap();
		while (scanner.hasNext()) {
			String from = scanner.next();
			String towards = scanner.next();
			String edge = from + "#" + towards;
			// for chemical
			double wgt = scanner.nextDouble();
			// for gap+chemical
//			double wgt = -1;
			
			/*
			if (target.contains(from)) {
				addFrequencyValuedMap(outProp, from, wgt);
			}
			
			if (target.contains(towards)) {
				addFrequencyValuedMap(inProp, towards, wgt);
			}
			*/
			addFrequencyValuedMap(outProp, from, 1.0);
			addFrequencyValuedMap(inProp, towards, 1.0);
			
			// handling dual cases
			/*
			boolean fromDual = false;
			boolean towardsDual = false;
			if (source.contains(from) && target.contains(from)) { 
				fromDual = true;
			}
			if (source.contains(towards) && target.contains(towards)) {
				towardsDual = true;
			}
			if (fromDual == true && towardsDual == true) {
				// SM -> SM
				// FB
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "fb" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "M->S");
				continue;
			}
			if (fromDual == true && (inter.contains(towards) || source.contains(towards))) {
				// SM -> I + S
				// FB
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "fb" + "\t" + wgt);
				if (inter.contains(towards)) addFrequencyValuedMap(edgeCount, "M->I");
				if (source.contains(towards)) addFrequencyValuedMap(edgeCount, "M->S");
				continue;
			}
			if (towardsDual == true && (inter.contains(from) || target.contains(from))) {
				// I + SM -> SM
				// FB
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "fb" + "\t" + wgt);
				if (inter.contains(from)) addFrequencyValuedMap(edgeCount, "I->S");
				if (target.contains(from)) addFrequencyValuedMap(edgeCount, "M->S");
				continue;
			}
			if (fromDual == true && target.contains(towards)) {
				// SM -> M
				// FF
//				System.out.println(from + " " + towards);
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "ff" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "S->M");
				continue;
			}
			if (towardsDual == true && source.contains(from)) {
				// S -> SM
				// FF
//				System.out.println(from + " " + towards);
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "ff" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "S->M");
				continue;
			}
			*/
			// done handling dual cases
			
			++kount;
			
			if (target.contains(from) && (inter.contains(towards) || source.contains(towards))) {
				// M -> I + S
				// FB
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "fb" + "\t" + wgt);
				if (inter.contains(towards)) {
					addFrequencyValuedMap(edgeCount, "M->I");
//					System.out.println("M->I" + "\t" + wgt);
				}
				if (source.contains(towards)) {
//					System.out.println("M->S" + "\t" + wgt);
					addFrequencyValuedMap(edgeCount, "M->S");
				}
				reverseFBWeights.put(towards + "#" + from, wgt);
				continue;
			}
			
			if (inter.contains(from) && source.contains(towards)) {
				// I -> S
				// FB
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "fb" + "\t" + wgt);
//				System.out.println("I->S" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "I->S");
				reverseFBWeights.put(towards + "#" + from, wgt);
				continue;
			}
			
			if (target.contains(from) && target.contains(towards)) {
				// LT
				++ lateralKount;
//				System.out.println(from + " " + towards);
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "lt" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "M->M");
//				System.out.println("M->M" + "\t" + wgt);
//				System.out.println(edge);
				continue;
			}
			
			if (inter.contains(from) && inter.contains(towards)) {
				// LT
				++ lateralKount;
//				System.out.println(from + " " + towards);
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "lt" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "I->I");
//				System.out.println("I->I" + "\t" + wgt);
//				System.out.println(edge);
				continue;
			}
			
			if (source.contains(from) && source.contains(towards)) {
				// LT
				++ lateralKount;
//				System.out.println(from + " " + towards);
//				System.out.println(wgt);
//				System.out.println(edge + "\t" + "lt" + "\t" + wgt);
				addFrequencyValuedMap(edgeCount, "S->S");
//				System.out.println("S->S" + "\t" + wgt);
//				System.out.println(edge);
				continue;
			}
			
			// REMAINING FF: S -> I + M, I -> M
//			System.out.println(from + " " + towards);
//			System.out.println(wgt);
//			System.out.println(edge + "\t" + "ff" + "\t" + wgt);
			System.out.println(edge);
			FFWeights.put(edge, wgt);
			if (source.contains(from) && inter.contains(towards)) {
				addFrequencyValuedMap(edgeCount, "S->I");
//				System.out.println("S->I" + "\t" + wgt);
				continue;
			}
			if (source.contains(from) && target.contains(towards)) {
				addFrequencyValuedMap(edgeCount, "S->M");
//				System.out.println("S->M" + "\t" + wgt);
				continue;
			}
			if (inter.contains(from) && target.contains(towards)) {
				addFrequencyValuedMap(edgeCount, "I->M");
//				System.out.println("I->M" + "\t" + wgt);
				continue;
			}
			
//			System.out.println("X: " + edge);
		}
		scanner.close();
//		System.out.println(kount);
//		System.out.println(lateralKount);
		
		for (String s : inProp.keySet()) {
//			System.out.println(inProp.get(s) + "\t" + getType(s));
		}
		
		for (String s : outProp.keySet()) {
//			System.out.println(outProp.get(s) + "\t" + getType(s));
		}
		
		for (String s : edgeCount.keySet()) {
//			System.out.println(s + "\t" + edgeCount.get(s));
		}
		
		for (String s : reverseFBWeights.keySet()) {
			if (FFWeights.containsKey(s)) {
//				System.out.println(FFWeights.get(s) + "\t" + reverseFBWeights.get(s));
//				System.out.println(FFWeights.get(s) - reverseFBWeights.get(s));
			}
		}
	}
	
	
	private static void traverseCost(String node, HashSet<String> visited, DependencyDAG dependencyDAG, HashSet<String> skipNodes) {
		if (visited.contains(node)) return; // visited before
		if (!dependencyDAG.depends.containsKey(node)) return; // end node
		if (skipNodes.contains(node)) return; // via core
		
		visited.add(node);
//		System.out.println("Processing: " + node);
		operationCost += Math.max(1, dependencyDAG.depends.get(node).size() - 1); 
		for (String s : dependencyDAG.depends.get(node)) {
			traverseCost(s, visited, dependencyDAG, skipNodes);
		}
	}
	
	public static int operationCost;
	
	public static void computeDimensionality2() throws Exception {
		for (int i = 1; i <= 92; ++i) {
		DependencyDAG.isToy = true;	
		loadNeuroMetaNetwork();
		HashSet<String> tempCore = new HashSet(orderedCore.subList(0, i));
		String net = "fb_clean";
		DependencyDAG dependencyDAG = new DependencyDAG("celegans//" + net + "_links.txt");
		
		/*  test */
//		String net = "h5";
//		loadNodes(nodes, source, "data//" + net + "_sources.txt");
//		loadNodes(nodes, inter, "data//" + net + "_inters.txt");
//		loadNodes(nodes, target, "data//" + net + "_targets.txt");
//		DependencyDAG dependencyDAG = new DependencyDAG("data//" + net + "_links.txt");
		/* test */
		
		operationCost = 0;
		for (String s : target) {
			HashSet<String> visited = new HashSet();
			traverseCost(s, visited, dependencyDAG, new HashSet());
//			System.out.println(s + "\t" + operationCost);
		}
		double targetCost = operationCost;

		operationCost = 0;
		for (String s : tempCore) {
			HashSet<String> visited = new HashSet();
			traverseCost(s, visited, dependencyDAG, new HashSet());
//			System.out.println(s + "\t" + operationCost);
		}
		double coreCost = operationCost;

		operationCost = 0;
		for (String s : target) {
			HashSet<String> visited = new HashSet();
			traverseCost(s, visited, dependencyDAG, tempCore);
//			System.out.println(s + "\t" + operationCost);
		}
		double targetCostViaCore = operationCost;

		
//		System.out.println(targetCost + "\t" + targetCostViaCore + "\t" + coreCost);
//		System.out.println((targetCostViaCore + coreCost) / targetCost);
		System.out.println(i + "\t" + ((targetCostViaCore + coreCost) / targetCost));
		}
	}
	
	private static void getLocationColorWeightedHistogram() {
		double binWidth = 0.1;
		int numBin = (int)(1.0 / binWidth) + 2;
		int binKount[] = new int[numBin];
		
		ArrayList< ArrayList<String> > colorValues = new ArrayList();
		for (int i = 0; i < numBin; ++i) {
			colorValues.add(new ArrayList<String>());
		}
		
		for (String s: nodes) {
			if (!nodePathWeights.containsKey(s)) continue;
			double loc = location.get(s);
//			System.out.println(s + "\t" + loc + "\t" + nodePathWeights.get(s));
			int binLocation = -1;
			if (loc < 0.0001) {
				binLocation = 0;
			}
			else if (Math.abs(loc - 1.0) < 0.0001) {
				binLocation = numBin - 1;
			}
			else {
				binLocation = 1 + (int)(loc / binWidth);
			}
			binKount[binLocation]++;
			
			colorValues.get(binLocation).add(s);
		}
//		System.out.println("--- --- ---");
		
		int matrixMaxHeight = 0;
		for (int i = 0; i < numBin; ++i) {
//			System.out.println((i + 1) + "\t" + binKount[i]);
			if (binKount[i] > matrixMaxHeight) {
				matrixMaxHeight = binKount[i];
			}
		}
		matrixMaxHeight++;
		
		double colorMatrixValue[][] = new double[matrixMaxHeight][numBin];
		String colorMatrixName[][] = new String[matrixMaxHeight][numBin];
		
		double nTotalPath = 3177835;
		int midIndex = matrixMaxHeight / 2;
		for (int i = 0; i < numBin; ++i) {
//			ArrayList<Double> aList = colorValues.get(i);
			TreeMultimap<Double, String> sortedStrings = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
			for (String s: colorValues.get(i)) {
//				System.out.println(s);
				sortedStrings.put(nodePathWeights.get(s) * 1.0 /nTotalPath, s);
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
//					double truncated = ((int)colorMatrixValue[i][j] * 1000) / 1000.0;
//					System.out.print(idNeuronMap.get(colorMatrixName[i][j]) + " (" + truncated + ")\t");
//					System.out.print(colorMatrixValue[i][j] + "\t");
					System.out.print(colorMatrixName[i][j] + "\t");
				}
				else {
					System.out.print(" " + "\t");
				}
			}
			System.out.println();
		}
	}

	private static PrintWriter randomPW;
	
	private static HashMap<String, HashSet<String>> getAncestorsFromPath() throws Exception {
		Scanner scanner = new Scanner(new File("celegans//all_sp+2.txt"));
		HashMap<String, HashSet<String>> ancestors = new HashMap();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] path = line.split("\\s+");
			HashSet<String> currentPrefix = new HashSet();
			currentPrefix.add(path[0]);
			for (int i = 1; i < path.length; ++i) {
				if (ancestors.containsKey(path[i])) {
					ancestors.get(path[i]).addAll(currentPrefix);
				}
				else {
					HashSet<String> hs = new HashSet();
					hs.addAll(currentPrefix);
					ancestors.put(path[i], hs);
				}
				currentPrefix.add(path[i]);
			}
		}
//		System.out.println("done");
		
		
		for (String s : nodes) {
			if (ancestors.containsKey(s)) {
				System.out.print(s + "\t" + getType(s) + "\t" + ancestors.get(s).size());
				int[] a = new int[5];
				for (String r : ancestors.get(s)) {
					a[getNumericType(r)]++;
				}
				System.out.println("\t" + a[1] + "\t" + a[2] + "\t" + a[3] + "\t" + a[4]);
			}
			else {
				System.out.println(s + "\t" + getType(s) + "\t" + 0 + "\t"  + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0);
			}
		}
		return ancestors;
		
		
	}
	
	private static void randomize() throws Exception {
//		loadNeuroMetaNetwork();
		DependencyDAG.isToy = true;		
		String net = "celegans";
		DependencyDAG dependencyDAG = new DependencyDAG("data//" + net + "_links.txt");
		
		/* from network */
//		Random random = new Random(System.nanoTime());		
//		randomPW = new PrintWriter(new File("data//" + currentNet + "_links.txt"));
//		for (String s: dependencyDAG.nodes) {
//			if (dependencyDAG.isSource(s)) continue; // works for isToy			
//			
//			int sampleSize = dependencyDAG.ancestors.get(s).size();
//			ArrayList<String> ancestor = new ArrayList(dependencyDAG.ancestors.get(s));
//			
//			HashSet<Integer> newSubstrateID = new HashSet();
//			int inDegree = dependencyDAG.depends.get(s).size();
//			while (--inDegree >= 0) {
//				int shuffleID = -1;
//				do {
//					shuffleID = random.nextInt(sampleSize);
//				}
//				while (newSubstrateID.contains(shuffleID));
//				
//				newSubstrateID.add(shuffleID);
//				String newSubstrate = ancestor.get(shuffleID);
//				randomPW.println(newSubstrate + " " + s);
//			}
//		}	
//		randomPW.close();
		/* end - from network */
		
		/* from path */
		HashMap<String, HashSet<String>> ancestors = getAncestorsFromPath();
		Random random = new Random(System.nanoTime());		
		randomPW = new PrintWriter(new File("data//" + currentNet + "_links.txt"));
		for (String s: dependencyDAG.nodes) {
			if (!ancestors.containsKey(s)) { // no ancestor
				if (!dependencyDAG.depends.containsKey(s)) { // zero in-degree node
					continue;
				}
				// a node with no paths going through it, retain its current dependencies as is
				// or not?
				continue;			
			}
			int numOfAncestors = ancestors.get(s).size();
			ArrayList<String> ancestorList = new ArrayList(ancestors.get(s));
			HashSet<Integer> newSubstrateID = new HashSet();
			int inDegree = dependencyDAG.depends.get(s).size();			
			if (inDegree > numOfAncestors) {
				inDegree = numOfAncestors;
			}
//			System.out.println("Processing " + s + " with indeg " + inDegree + " and ancestor " + ancestorList);
			while (--inDegree >= 0) {
				int shuffleID = -1;
				do {
					shuffleID = random.nextInt(numOfAncestors);
				}
				while (newSubstrateID.contains(shuffleID));
				
				newSubstrateID.add(shuffleID);
				String newSubstrate = ancestorList.get(shuffleID);
				randomPW.println(newSubstrate + " " + s);
//				System.out.println(newSubstrate + " " + s);
			}
		}	
		randomPW.close();
		/* end - from path */
		
		randomPW = new PrintWriter(new File("data//" + currentNet + "_paths.txt"));
		bfsSP();
		traverseAllPaths();
		randomPW.close();
		
		pathHourglassAnalysis();
		computeFlatCore();
		System.out.println(realCoreSize + "\t" + flatCoreSize);
		System.out.println(1.0 - (realCoreSize * 1.0 / flatCoreSize));
		
//		for (tau = 0.5; tau <= 0.99; tau += 0.02) {
//			pathHourglassAnalysis();
//			computeFlatCore();
//			System.out.println(tau + "\t" + (1.0 - (realCoreSize * 1.0 / flatCoreSize)));
//		}
	}
	
	
	public static void main(String[] args) throws Exception {
		loadNeuroMetaNetwork();
//		loadMetaNetwork();
//		doToyNetworkAnalysis();
		
//		shortestPathAnalysis_1();
		
//		pathHourglassAnalysis();
//		computeFlatCore();
//		System.out.println(realCoreSize + "\t" + flatCoreSize);
//		System.out.println(1.0 - (realCoreSize * 1.0 / flatCoreSize));
		
//		for (tau = 0.5; tau <= 0.99; tau += 0.02) {
//			pathHourglassAnalysis();
//			computeFlatCore();
//			System.out.println(tau + "\t" + (1.0 - (realCoreSize * 1.0 / flatCoreSize)));
//		}
		
//		createCoreNetwork();
		
//		feedbackHypothesis();
		
		// new?
		
//		gapJunctionAnalysis();
		
//		degreeAnalysis();
		
//		sevenLayerAnalysis();
		
//		topEdgeNeuronBypass();
		
//		getReducedCoreNetwork();
		
//		getWeightCorrelation();
		
		computeDimensionalityReduction();
		
//		computeDimensionality2();
		
//		traverseAllPaths();
		
//		analyzeHierarchy();
		
//		simpleHierarchy();
		
//		loadNeuroMetaNetwork();
//		Scanner scanner = new Scanner(new File("celegans/fb_clean_links.txt"));
//		while(scanner.hasNext()) {
//			int a = scanner.nextInt();
//			int b = scanner.nextInt();
//			System.out.println(a + "\t" + b + "\t" + chemicalWeights.get(a + "#" + b));
//		}
//		scanner.close();
		
//		bfsSP();
		
//		getFbCleanNetwork();
		
//		getDerivedPaths();
		
//		analyzeEdge();
		
//		getLocationColorWeightedHistogram();
		
		for (int run = 0; run < 1; ++run) {
//			randomize();
		}
	}
}
