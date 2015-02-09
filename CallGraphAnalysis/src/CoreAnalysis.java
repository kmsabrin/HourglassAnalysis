import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;


public class CoreAnalysis {
	
	HashSet<String> coreNodes; // W
	HashSet<String> coreDownReachableNodes; // L
	HashSet<String> coreUpReachableNodes; // U
	HashSet<String> coreNotReachableNodes; // O
	HashMap<String, Integer> shortestHopDistance; // hop count from core
	double averageCentralityTrend[] = new double[10000]; // size estimated
	
	double waistWidth;
	double avgWaistCentrality;
	double coverage;
	double hTrend;
	double percentile;
	
	double connectedPair;
	HashSet<String> visited;
	
	TreeMultimap<Integer, String> hopDistanceCentralityDistribution;
	
	CoreAnalysis(CallDAG callDAG, double percentile) {
		this.percentile = percentile;
		coreNodes = new HashSet();
		coreDownReachableNodes = new HashSet();
		coreUpReachableNodes = new HashSet();
		coreNotReachableNodes = new HashSet();
		shortestHopDistance = new HashMap();
		hopDistanceCentralityDistribution = TreeMultimap.create();
		getCoreNodes(callDAG);
		getNonCoreNodes(callDAG);
		getHScore(callDAG);
		
//		System.out.println(avgWaistCentrality + "\t" + waistWidth + "\t" + coverage + "\t" + hTrend);
	}
	
	private int getAverageCentralityOnHopCount(CallDAG callDAG, HashSet<String> stringSet, int idx, int direction) {
		double hopCentrality[] = new double[10000];
		double hopCentralityFrequency[] = new double[10000];
		
		int maxHopCount = 0;
		for (String s: stringSet) {
			int hopCount = shortestHopDistance.get(s);
			double cen = callDAG.centrality.get(s);
			hopDistanceCentralityDistribution.put(hopCount * direction, s);
//			System.out.println(s + "\t" + (direction * hopCount));
	
			hopCentrality[hopCount] += cen;
			++hopCentralityFrequency[hopCount];
			if (hopCount > maxHopCount) maxHopCount = hopCount;
		}
		
		for (int i = 1; i <= maxHopCount; ++i) {
				hopCentrality[i] /= hopCentralityFrequency[i];
				averageCentralityTrend[idx++] = hopCentrality[i];
		}
		
		return idx;
	}
	
	private void getHScore(CallDAG callDAG) {
		double averageCoreCentrality = 0;		
		for (String s: coreNodes) {
			averageCoreCentrality += callDAG.centrality.get(s);
		}
		averageCoreCentrality /= coreNodes.size();
		avgWaistCentrality = averageCoreCentrality;
		
		int maxIndex = getAverageCentralityOnHopCount(callDAG, coreUpReachableNodes, 0, -1);
		averageCentralityTrend[maxIndex] = averageCoreCentrality;
		maxIndex = getAverageCentralityOnHopCount(callDAG, coreDownReachableNodes, maxIndex + 1, 1);
//		for (int i = 0; i < maxIndex; ++i) System.out.println(i + "\t" + averageCentralityTrend[i]);
		hTrend = CentralityAnalysis.getHScore(Arrays.copyOfRange(averageCentralityTrend, 0, maxIndex));
		
//		for (int i: hopDistanceCentralityDistribution.keySet()) {
//			Collection<String> centralityDistribution = hopDistanceCentralityDistribution.get(i);
//			System.out.println("\"Distance: " + i + " Nodes: " + centralityDistribution.size() + "\"");
//			for (String s: centralityDistribution) {
//				System.out.println(callDAG.centrality.get(s));
//			}
//			
//			System.out.println("\n");
//		}
	}
	
	private void traverseDown(CallDAG callDAG, String node, HashSet<String> stringSet, int hopCount) {
		if (coreNodes.contains(node)) return;
		
		if (shortestHopDistance.containsKey(node)) {
			int hC = shortestHopDistance.get(node);
			if (hopCount < hC) shortestHopDistance.put(node, hopCount);
			else return;
		}
		else shortestHopDistance.put(node, hopCount);
		
		stringSet.add(node);
		
		if (!callDAG.callTo.containsKey(node)) return; // a leaf
		
		for (String s: callDAG.callTo.get(node)) {
			traverseDown(callDAG, s, stringSet, hopCount + 1);
		}
	}
	
	private void traverseUp(CallDAG callDAG, String node, HashSet<String> stringSet, int hopCount) {
		if (coreNodes.contains(node)) return;
				
		if (shortestHopDistance.containsKey(node)) {
			int hC = shortestHopDistance.get(node);
			if (hopCount < hC) shortestHopDistance.put(node, hopCount);
			else return;
		}
		else shortestHopDistance.put(node, hopCount);
		
		stringSet.add(node);

		if (!callDAG.callFrom.containsKey(node)) return; // a root
		
		for (String s: callDAG.callFrom.get(node)) {
			traverseUp(callDAG, s, stringSet, hopCount + 1);
		}
	}
	
	private void getNonCoreNodes(CallDAG callDAG) {
		for (String s: coreNodes) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r: callDAG.callTo.get(s)) {
					traverseDown(callDAG, r, coreDownReachableNodes, 1);
				}
			}
			
			if (callDAG.callFrom.containsKey(s)) {
				for (String r: callDAG.callFrom.get(s) ) {
					traverseUp(callDAG, r, coreUpReachableNodes, 1);
				}
			}
		}
		
		for (String s: callDAG.functions) {
			if (coreUpReachableNodes.contains(s) && coreDownReachableNodes.contains(s)) {
//				System.out.println(s);
			}
			if (coreNodes.contains(s) || coreUpReachableNodes.contains(s) || coreDownReachableNodes.contains(s)) continue;
			coreNotReachableNodes.add(s);
		}
		
		coverage = 1.0 - (coreNotReachableNodes.size() * 1.0 / callDAG.functions.size());
	}
	
	private void getCoreNodes(CallDAG callDAG) {
		double centralityValues[] = new double[callDAG.functions.size()];
		int idx = 0;		
		for(String s: callDAG.functions) {
			centralityValues[idx++] = callDAG.centrality.get(s);
		}
		Arrays.sort(centralityValues);
		double coreCentralityThreshold = centralityValues[(int)(Math.floor(percentile * centralityValues.length))];
//		double coreCentralityThreshold = centralityValues[(int)(centralityValues.length - 650)];
//		System.out.println(coreCentralityThreshold);
		
		for(String s: callDAG.functions) {
			if (callDAG.centrality.get(s) < coreCentralityThreshold) continue;
			coreNodes.add(s);
		}
		
//		System.out.println("Core Width: " + (coreNodes.size() * 1.0 / callDAG.functions.size()));
		waistWidth = coreNodes.size() * 1.0 / callDAG.functions.size();
	}
	
	private void getConnectedPairTraverse(String node, CallDAG callDAG) {
		if (visited.contains(node)) {
			return;
		}
		visited.add(node);
		
//		System.out.print(node + " ");
		
		if (!callDAG.callTo.containsKey(node)) { // is leaf
//			System.out.println("Found leaf " + node);
			++connectedPair;
			return;
		}
		
		for (String s: callDAG.callTo.get(node)) {
			getConnectedPairTraverse(s, callDAG);
		}
	}
	
	private void getConnectedPair(CallDAG callDAG) {
		connectedPair = 0;
		for (String s: callDAG.functions) {
			visited = new HashSet();
			if (!callDAG.callFrom.containsKey(s)) { // root
//				System.out.println("Traversing " + s);
				getConnectedPairTraverse(s, callDAG);
//				System.out.println();
			}
		}
	}
	
	public void getWaistCentralityThreshold(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//centrality-threshold-" + filePath + ".txt"));
		
		TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s: callDAG.functions) {
			centralitySortedNodes.put(callDAG.centrality.get(s), s);
		}
		
		getConnectedPair(callDAG);
		double networkConnetedPair = connectedPair;
		double nodesRemoved = 0;
		pw.println(nodesRemoved + "\t" + (networkConnetedPair / networkConnetedPair));
		
		for (double d: centralitySortedNodes.keySet()) {
			Collection<String> nodes = centralitySortedNodes.get(d);
			
			for (String s: nodes) {
//				if (callDAG.callTo.containsKey(s)) {// not a leaf 
//					callDAG.callTo.put(s, new HashSet<String>()); // clear it
//				}
				
				if (callDAG.callTo.containsKey(s)) { 
					for (String r: callDAG.callTo.get(s)) {
						callDAG.callFrom.get(r).remove(s);
					}
					callDAG.callTo.remove(s);
				}
				
				if (callDAG.callFrom.containsKey(s)) { 
					for (String r: callDAG.callFrom.get(s)) {
						callDAG.callTo.get(r).remove(s);
					}
					callDAG.callFrom.remove(s);
				}
				
				callDAG.functions.remove(s);
			}
			
			getConnectedPair(callDAG);
			nodesRemoved += nodes.size();
			pw.println(nodesRemoved + "\t" + (connectedPair / networkConnetedPair));			
		}
		
		pw.close();
	}
	
	public void getCentralityVsCutProperty(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//centrality-vs-cutproperty-" + filePath + ".txt"));
		
		getConnectedPair(callDAG);
		double networkConnetedPair = connectedPair;
		System.out.println(networkConnetedPair);
		/*
		TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s: callDAG.functions) {
			centralitySortedNodes.put(callDAG.centrality.get(s), s);
		}
		
		int knt = 0;
		for (double d : centralitySortedNodes.keySet()) {
			Collection<String> nodes = centralitySortedNodes.get(d);

			for (String s : nodes) {
				if (!callDAG.callFrom.containsKey(s)
						|| !callDAG.callTo.containsKey(s)) {
					continue;
				}

				HashSet<String> store = new HashSet(callDAG.callTo.get(s));
				callDAG.callTo.put(s, new HashSet<String>());

				// if (callDAG.callTo.containsKey(s)) { // disconnect it by removing children
				// callDAG.callTo.put(s, new HashSet<String>());
				// }

				getConnectedPair(callDAG);

				pw.println(callDAG.centrality.get(s) + "\t" + (connectedPair / networkConnetedPair));

				callDAG.callTo.put(s, store);
				++knt;
			}
			
			if (knt > 2000) break;
		}*/
		pw.close();
	}
	
	private void printCallDAG(CallDAG callDAG) {
		for (String s: callDAG.functions) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r : callDAG.callTo.get(s)) {
					System.out.println("(" + s + " calling " + r + ")");
				}
			}
			
			if (callDAG.callFrom.containsKey(s)) {
				for (String r : callDAG.callFrom.get(s)) {
					System.out.println("(" + s + " called by " + r + ")");
				}
			}
		}
	}
	
	private void removeIsolatedNodes(CallDAG callDAG) {
		HashSet<String> removable = new HashSet();
		for (String s : callDAG.functions) {
			if (!callDAG.callTo.containsKey(s) && !callDAG.callFrom.containsKey(s)) {
				removable.add(s);
			}
		}
		
		callDAG.functions.removeAll(removable);
	}
		
	public void getWaistCentralityThreshold_2(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//centrality-threshold2-" + filePath + ".txt"));
		
		getConnectedPair(callDAG);
		double networkConnetedPair = connectedPair;
		double nodesRemoved = 0;
		pw.println(nodesRemoved + "\t" + 1.0);

		
		while (callDAG.functions.size() > 0) {
			TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
			for (String s : callDAG.functions) {
				centralitySortedNodes.put(callDAG.centrality.get(s), s);
//				System.out.println(s + "\t" + callDAG.nodePathThrough.get(s) + "\t" + callDAG.nTotalPath);
			}
//			System.out.println("-----");


			for (double d : centralitySortedNodes.keySet()) {
				Collection<String> nodes = centralitySortedNodes.get(d);

				for (String s : nodes) {
					if (callDAG.callTo.containsKey(s)) { 
						for (String r: callDAG.callTo.get(s)) {
							callDAG.callFrom.get(r).remove(s);
							if (callDAG.callFrom.get(r).size() < 1) callDAG.callFrom.remove(r);
						}
						callDAG.callTo.remove(s);
					}
					
					if (callDAG.callFrom.containsKey(s)) { 
						for (String r: callDAG.callFrom.get(s)) {
							callDAG.callTo.get(r).remove(s);
							if (callDAG.callTo.get(r).size() < 1) callDAG.callTo.remove(r);
						}
						callDAG.callFrom.remove(s);
					}
					
					callDAG.functions.remove(s);
					
					System.out.println("removing: " + s);
				}

				removeIsolatedNodes(callDAG);
				getConnectedPair(callDAG);
				nodesRemoved += nodes.size();
				pw.println(nodesRemoved + "\t" + (connectedPair / networkConnetedPair));
				System.out.println(nodesRemoved + "\t" + connectedPair + "\t" + networkConnetedPair);
				
				callDAG.resetAuxiliary();
//				printCallDAG(callDAG);
				
				callDAG.loadDegreeMetric();
				callDAG.loadLocationMetric(); // must load degree metric before
				callDAG.loadGeneralityMetric(); 
				callDAG.loadComplexityMetric();
				callDAG.loadCentralityMetric();
				break;
			}
			
			
			if (nodesRemoved > 2500) break;
		}
		
		pw.close();
	}
}
