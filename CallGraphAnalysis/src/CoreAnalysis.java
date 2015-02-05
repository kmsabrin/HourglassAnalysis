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
	
	CoreAnalysis(CallDAG callDAG, double percentile) {
		this.percentile = percentile;
		coreNodes = new HashSet();
		coreDownReachableNodes = new HashSet();
		coreUpReachableNodes = new HashSet();
		coreNotReachableNodes = new HashSet();
		shortestHopDistance = new HashMap();
		getCoreNodes(callDAG);
		getNonCoreNodes(callDAG);
		getHScore(callDAG);
		
//		System.out.println(avgWaistCentrality + "\t" + waistWidth + "\t" + coverage + "\t" + hTrend);
	}
	
	private int getAverageCentralityOnHopCount(CallDAG callDAG, HashSet<String> stringSet, int idx) {
		double hopCentrality[] = new double[10000];
		double hopCentralityFrequency[] = new double[10000];
		
		int maxHopCount = 0;
		for (String s: stringSet) {
			int hopCount = shortestHopDistance.get(s);
			double cen = callDAG.centrality.get(s);
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
		
		int maxIndex = getAverageCentralityOnHopCount(callDAG, coreUpReachableNodes, 0);
		averageCentralityTrend[maxIndex] = averageCoreCentrality;
		maxIndex = getAverageCentralityOnHopCount(callDAG, coreDownReachableNodes, maxIndex + 1);
//		for (int i = 0; i < maxIndex; ++i) System.out.println(i + "\t" + averageCentralityTrend[i]);
		hTrend = CentralityAnalysis.getHScore(Arrays.copyOfRange(averageCentralityTrend, 0, maxIndex));
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
				if (callDAG.callTo.containsKey(s)) {// not a leaf 
					callDAG.callTo.put(s, new HashSet<String>()); // clear it
				}
			}
			
			getConnectedPair(callDAG);
			nodesRemoved += nodes.size();
			pw.println(nodesRemoved + "\t" + (connectedPair / networkConnetedPair));			
		}
		
		pw.close();
	}
}
