package utilityhg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import corehg.CoreDetection;
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
	public static HashMap<String, Integer> edgeSPWeights = new HashMap();
	public static HashMap<String, Integer> inDeg = new HashMap();
	public static HashMap<String, Integer> outDeg = new HashMap();
	public static HashMap<String, Integer> dummy = new HashMap();
	static int flatCoreSize;
	static int realCoreSize;
	static double tau = 0.9;
	
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
		chemicalWeights = new HashMap();
		coreNeurons = new HashSet();
		flatCoreSize = 0;
		realCoreSize = 0;
		tau = 0.9;
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
		if (source.contains(neuron)) return "sensory";
		else if (inter.contains(neuron)) return "inter";
		else return "motor";
	}
	
	private static void loadNeuroMetaNetwork() throws Exception {
//		loadNodes(nodes, source, "celegans//sensory_neurons.txt");
//		loadNodes(nodes, inter, "celegans//inter_neurons.txt");
//		loadNodes(nodes, target, "celegans//motor_neurons.txt");
	
		loadNodes(nodes, source, "celegans//sensory_neurons_3.txt");
		loadNodes(nodes, inter, "celegans//inter_neurons_3.txt");
		loadNodes(nodes, target, "celegans//motor_neurons_3.txt");

		
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
			addMap(dummy, getType(src)+ "#" + getType(dst));
//			System.out.println((wgt + "\t" + getType(src)+ "#" + getType(dst)));
			addMap(inDeg, dst);
			addMap(outDeg, src);
		}
		scan.close();
		for (String s : dummy.keySet()) {
			System.out.println(s + "\t" + dummy.get(s));
		}
		
		
//		scan = new Scanner(new File("celegans//core_neurons.txt"));
		scan = new Scanner(new File("celegans//core_neurons_tau_1.txt"));
		int knt = 0;
		while (scan.hasNext()) {
			coreNeurons.add(scan.next());
			++knt;
			if (knt > 18) break;
		}
		scan.close();
		
		Scanner scanner = new Scanner(new File("celegans//almost_sp.txt"));
		HashSet<String> sPaths = new HashSet();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			sPaths.add(line);
		}
		scanner.close();
		for (String line : sPaths) {
			String[] tokens = line.split("\\s+");
			for (int i = 0; i < tokens.length - 1; ++i) {
				String edg = tokens[i] + "#" + tokens[i + 1];
				addMap(edgeSPWeights, edg);
			}
		}
	}
	
	private static void shortestPathAnalysis_1() throws Exception {
		loadNeuroMetaNetwork();
		
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
			
		HashSet<String> fbEdges = new HashSet();
		Scanner scanner = new Scanner(new File("celegans//fb_edges.txt"));
		while (scanner.hasNext()) {
			String edg = scanner.next();
			fbEdges.add(edg);
//			System.out.println(weights.get(edg));
		}
		scanner.close();
		
		scanner = new Scanner(new File("celegans//all_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_k_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_k_sp.txt"));
		HashMap<Integer, Integer> SPLengthFreq = new HashMap();
		HashSet<String> SPInter = new HashSet();
		HashSet<String> shortestPathEdge = new HashSet();
		HashMap<String, Integer> shortestPathEdgeFrequency = new HashMap();
		HashMap<String, Integer> lengthSP = new HashMap();
		int containsBackPath = 0;
		ArrayList<Integer> pathLengthList = new ArrayList(); 
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
				}
				prev = r;
			}
			
//			if (containsBack) containsBackPath++;
//			break;
			
			String pairSM = src + "#" + tgt; 
			lengthSP.put(pairSM, num);
		}
		
		for (int k : SPLengthFreq.keySet()) {
//			System.out.println(k + "\t" + SPLengthFreq.get(k));
		}
		
		for (String s : shortestPathEdgeFrequency.keySet()) {		
//			System.out.println(s + "\t" + shortestPathEdgeFrequency.get(s) + "\t" + weights.get(s));
//			System.out.println(s + "\t" + shortestPathEdgeFrequency.get(s));
		}
		
		for (String s : lengthSP.keySet()) {
//			System.out.println(s + "\t" + lengthSP.get(s));
		}
		
//		double values[] = new double[pathLengthList.size()];
//		int idx = 0;
//		for (int v : pathLengthList) {
//			values[idx++] = v;
//		}
//		System.out.println(StatUtils.percentile(values, 10) + "\t" + StatUtils.percentile(values, 50) + "\t" + StatUtils.percentile(values, 90));
//		System.out.println(lengthSP.size() * 1.0 / 9492.0);
//		System.out.println(shortestPathEdge.size() * 1.0 / 1893.0);
		
//		System.out.println(SPInter.size());
//		System.out.println(containsBackPath);
	}
	
	private static void computeFlatCore() throws Exception {
		Scanner scanner;
//		= new Scanner(new File("celegans//gap_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp_weighted.txt"));
//		Scanner scanner = new Scanner(new File("celegans//full_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost_sp_len_restrict.txt"));
		
		
//		scanner = new Scanner(new File("celegans//all_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_k_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
		scanner = new Scanner(new File("celegans//almost_k_sp.txt"));
		
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
	
	private static void shortestPathHourglassAnalysis() throws Exception {
		loadNeuroMetaNetwork();
		
		Scanner scanner;
//		= new Scanner(new File("celegans//gap_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_sp_weighted.txt"));
//		Scanner scanner = new Scanner(new File("celegans//full_fb_clean_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//fb_clean_almost_sp.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost-2.txt"));
//		Scanner scanner = new Scanner(new File("celegans//almost_sp_len_restrict.txt"));
		
		
//		scanner = new Scanner(new File("celegans//all_sp.txt"));
//		scanner = new Scanner(new File("celegans//all_k_sp.txt"));
//		scanner = new Scanner(new File("celegans//almost_sp.txt"));
		scanner = new Scanner(new File("celegans//almost_k_sp.txt"));

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
//			if (tokens.length >= cutLen) continue;
			++sizeSP;
		}
		
//		int size = (int)(sizeSP * (1.0 - tau));
//		double startSize = sPaths.size();
		realCoreSize = 0;
		double cumPathCover = 0;
//		System.out.println(sizeSP + "\t" + size);
		while (true) {
			HashMap<String, Integer> maxSPCentrality = new HashMap();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				if (tokens.length >= cutLen) continue;
//				System.out.println(line + " ## " + tokens.length);
				for (String r : tokens) {
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
//					maxSPCNeurons.add(v);
				}
			}
			
			cumPathCover += max;
//			System.out.println(cumPathCover / sizeSP);
			for (String v : maxSPCNeurons) {
//				System.out.print(v + "," + max + "\t");
//				System.out.print(v + "\t");
//				System.out.print(idNeuron.get(v) + "\t");
			}
//			System.out.println();
//			System.out.println("\n-- -- -- -- --");
			
			HashSet<String> removeSPaths = new HashSet();
			for (String line : sPaths) {
				String[] tokens = line.split("\\s+");
//				System.out.println(line);
				for (String r : tokens) {
					if (maxSPCNeurons.contains(r)) {
						removeSPaths.add(line);
					}
				}
			}

			sPaths.removeAll(removeSPaths);
			++realCoreSize;
			
//			if (sPaths.size() <= size) break;
//			System.out.println(cumPathCover + "\t" + (sizeSP * tau));
			if (cumPathCover >= sizeSP * tau) break;
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
		
		Scanner scan = new Scanner(new File("celegans//sp_edges.txt"));
		while (scan.hasNext()) {
			spEdges.add(scan.next());
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
		
		scan = new Scanner(new File("celegans//fb_clean_links.txt"));
		knt = 0;
		while (scan.hasNext()) {
			String src = scan.next();
			String dst = scan.next();
			String edg = src + "#" + dst;
			if (coreNeurons.contains(src) && coreNeurons.contains(dst)) {
				if (spEdges.contains(edg)) {
//					System.out.println(weights.get(edg));
//					++knt;
//					System.out.println(edg + "\t" + edgeSPWeights.get(edg));
					System.out.println(src + "\t" + dst + "\t" + edgeSPWeights.get(edg));
				}
				else if (fbEdges.contains(edg)) {
//					System.out.println(weights.get(edg));
//					++knt;
				}
			}
		}
		scan.close();
//		System.out.println(knt);
	}
	
	private static void addMap(HashMap<String, Integer> hmap, String key) {
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
			addMap(neuronFbIn, edgeNodes.get(1));
			addMap(neuronFbOut, edgeNodes.get(0));
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
		scan = new Scanner(new File("celegans//celegans.gap.junction.txt"));
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
			
			
			if (nodes.contains(srcId) && nodes.contains(dstId)) {
				// fb clean
				if (target.contains(srcId) && inter.contains(dstId)) ;
				else if (target.contains(srcId) && source.contains(dstId)) ;
				else if (inter.contains(srcId) && source.contains(dstId)) ;
				else System.out.println(srcId + "\t" + dstId + "\t" + weight);
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
	
	public static void main(String[] args) throws Exception {
//		doToyNetworkAnalysis();
		
//		shortestPathAnalysis_1();
		
//		shortestPathHourglassAnalysis();
//		computeFlatCore();
//		System.out.println(realCoreSize + "\t" + flatCoreSize);
//		System.out.println(1.0 - (realCoreSize * 1.0 / flatCoreSize));
		
//		for (tau = 0.50; tau <= 0.99; tau += 0.02) {
//			shortestPathHourglassAnalysis();
//			computeFlatCore();
//			System.out.println(tau + "\t" + (1.0 - (realCoreSize * 1.0 / flatCoreSize)));
//		}
		
//		createCoreNetwork();
		
//		feedbackHypothesis();
		
//		gapJunctionAnalysis();
		
		degreeAnalysis();
	}
}
