import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.StatUtils;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;


public class CoreAnalysis {
	
	HashSet<String> waistNodes; // W
	HashSet<String> waistDownwardReachableNodes; // L
	HashSet<String> waistUpwardReachableNodes; // U
	HashSet<String> waistNotReachableNodes; // O
	HashMap<String, Integer> shortestHopDistance; // hop count from core
	double averageCentralityByHopDistance[] = new double[10000]; // size estimated
	
	HashSet<String> startingRoots;
	HashSet<String> startingLeaves;
	
	double waistWidth;
	double averageWaistCentrality;
	double coverage;
	double hourGlassTrend;
	double percentile;
	
	double connectedPair;
	HashSet<String> visited;
	
	TreeMultimap<Integer, String> centralityDistributionByHopDistance;
	
	// for 5 layer centrality
	double averageLBCentrality;
	double averageLNBCentrality;
	double averageUTCentrality;
	double averageUNTCentrality;
	
	CoreAnalysis(CallDAG callDAG, CallDAG takeApartCallDAG) throws Exception {
		this.percentile = percentile;
		waistNodes = new HashSet();
		waistDownwardReachableNodes = new HashSet();
		waistUpwardReachableNodes = new HashSet();
		waistNotReachableNodes = new HashSet();
		shortestHopDistance = new HashMap();
		centralityDistributionByHopDistance = TreeMultimap.create();
		
		startingRoots = new HashSet();
		startingLeaves = new HashSet();
		for (String s: callDAG.functions) {
			if (!callDAG.callTo.containsKey(s))
				startingLeaves.add(s);
			if (!callDAG.callFrom.containsKey(s))
				startingRoots.add(s);
		}
		
		getCoreNodes(takeApartCallDAG);
		waistWidth = waistNodes.size() * 1.0 / callDAG.functions.size();		

		getNonCoreNodes(callDAG);

		getHScore(callDAG);
//		get5LayerHScore(callDAG);
		
		System.out.println(coverage + "\t" + averageWaistCentrality + "\t" + waistWidth + "\t" + hourGlassTrend);
	}
	
	private void get5LayerHScore(CallDAG callDAG) {
		for (String s: waistNodes) {
			averageWaistCentrality += callDAG.centrality.get(s);
		}
		averageWaistCentrality /= waistNodes.size();

		int kntLB = 0;
		int kntLNB = 0;
		for (String s: waistDownwardReachableNodes) {
			if (callDAG.callTo.containsKey(s)) {
				averageLNBCentrality += callDAG.centrality.get(s);
				++kntLNB;
			}
			else {
				averageLBCentrality += callDAG.centrality.get(s);
				++kntLB;
			}
		}
		averageLBCentrality /= kntLB;
		averageLNBCentrality /= kntLNB;

		int kntUT = 0;
		int kntUNT = 0;
		for (String s: waistUpwardReachableNodes) {
			if (callDAG.callFrom.containsKey(s)) {
				averageUNTCentrality += callDAG.centrality.get(s);
				++kntUNT;
			}
			else {
				averageUTCentrality += callDAG.centrality.get(s);
				++kntUT;
			}
		}
		averageUTCentrality /= kntUT;
		averageUNTCentrality /= kntUNT;

		double cTrend[] = new double[]{averageUTCentrality, averageUNTCentrality, averageWaistCentrality, averageLNBCentrality, averageLBCentrality};
		hourGlassTrend = CentralityAnalysis.getHScore(cTrend);
		for (double d: cTrend) {
			System.out.print(d + "\t");
		}
		System.out.println();
	}
	
	private int getAverageCentralityOnHopCount(CallDAG callDAG, HashSet<String> stringSet, int idx, int direction) {
		double hopCentrality[] = new double[10000];
		double hopCentralityFrequency[] = new double[10000];

		int maxHopCount = 0;
		for (String s : stringSet) {
			int hopCount = shortestHopDistance.get(s);
			double cen = callDAG.centrality.get(s);
			centralityDistributionByHopDistance.put(hopCount * direction, s);
//			System.out.println(s + "\t" + (direction * hopCount) + "\t" + cen);

			hopCentrality[hopCount] += cen;
			++hopCentralityFrequency[hopCount];
			if (hopCount > maxHopCount)
				maxHopCount = hopCount;
		}

		for (int i = 1; i <= maxHopCount; ++i) {
//			System.out.println(idx + "\t" + hopCentrality[i] + "\t" + hopCentralityFrequency[i]);
			hopCentrality[i] /= hopCentralityFrequency[i];
			if (direction > 0)
				averageCentralityByHopDistance[idx] = hopCentrality[i];
			else
				averageCentralityByHopDistance[maxHopCount - i] = hopCentrality[i];
			++idx;
		}

		return idx;
	}
	
	private void getHScore(CallDAG callDAG) {
		for (String s: waistNodes) {
			averageWaistCentrality += callDAG.centrality.get(s);
			centralityDistributionByHopDistance.put(0, s);
		}
		averageWaistCentrality /= waistNodes.size();
		
		int maxIndex = getAverageCentralityOnHopCount(callDAG, waistUpwardReachableNodes, 0, -1);
		averageCentralityByHopDistance[maxIndex] = averageWaistCentrality;
		maxIndex = getAverageCentralityOnHopCount(callDAG, waistDownwardReachableNodes, maxIndex + 1, 1);
//		for (int i = 0; i < maxIndex; ++i) System.out.println(i + "\t" + averageCentralityTrend[i]);
		hourGlassTrend = CentralityAnalysis.getHScore(Arrays.copyOfRange(averageCentralityByHopDistance, 0, maxIndex));
		
		for (int i: centralityDistributionByHopDistance.keySet()) {
			Collection<String> centralityDistribution = centralityDistributionByHopDistance.get(i);
			double cDistribution[] = new double[centralityDistribution.size()];
			int idx = 0;
			for (String s: centralityDistribution) {
				cDistribution[idx++] = callDAG.centrality.get(s);
			}
			System.out.print(i + "\t");
			System.out.print(idx + "\t");
			System.out.print(StatUtils.max(cDistribution) + "\t");
			System.out.print(StatUtils.min(cDistribution) + "\t");
			System.out.print(StatUtils.mean(cDistribution) + "\t");
			System.out.print(Math.sqrt(StatUtils.variance(cDistribution)) + "\t");
			System.out.println();
		}
	}
	
	private void traverseDownward(CallDAG callDAG, String node, HashSet<String> stringSet, int hopCount) {
		if (waistNodes.contains(node)) return;
		
		if (shortestHopDistance.containsKey(node)) {
			int hC = shortestHopDistance.get(node);
			if (hopCount < hC) shortestHopDistance.put(node, hopCount);
			else return;
		}
		else shortestHopDistance.put(node, hopCount);
		
		stringSet.add(node);
		
		if (!callDAG.callTo.containsKey(node)) return; // a leaf
		
		for (String s: callDAG.callTo.get(node)) {
			traverseDownward(callDAG, s, stringSet, hopCount + 1);
		}
	}
	
	private void traverseUpward(CallDAG callDAG, String node, HashSet<String> stringSet, int hopCount) {
		if (waistNodes.contains(node)) return;
				
		if (shortestHopDistance.containsKey(node)) {
			int hC = shortestHopDistance.get(node);
			if (hopCount < hC) shortestHopDistance.put(node, hopCount);
			else return;
		}
		else shortestHopDistance.put(node, hopCount);
		
		stringSet.add(node);

		if (!callDAG.callFrom.containsKey(node)) return; // a root
		
		for (String s: callDAG.callFrom.get(node)) {
			traverseUpward(callDAG, s, stringSet, hopCount + 1);
		}
	}
	
	private void getNonCoreNodes(CallDAG callDAG) {
		for (String s: waistNodes) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r: callDAG.callTo.get(s)) {
					traverseDownward(callDAG, r, waistDownwardReachableNodes, 1);
				}
			}
			
			if (callDAG.callFrom.containsKey(s)) {
				for (String r: callDAG.callFrom.get(s) ) {
					traverseUpward(callDAG, r, waistUpwardReachableNodes, 1);
				}
			}
		}
		
		for (String s: callDAG.functions) {
			if (waistNodes.contains(s) || waistUpwardReachableNodes.contains(s) || waistDownwardReachableNodes.contains(s)) 
				continue;
			waistNotReachableNodes.add(s);
		}
		
		coverage = 1.0 - (waistNotReachableNodes.size() * 1.0 / callDAG.functions.size());
	}
	
	private void getCoreNodes(CallDAG takeApartCallDAG) throws Exception {
		/*
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
		*/
		
		for(String s: takeApartCallDAG.functions) {
//			System.out.println(s + "\t" + takeApartCallDAG.centrality.get(s));
		}
		
		getWaistCentralityThreshold_2(takeApartCallDAG, "x");
		takeApartCallDAG = null;
	}
	
	/*
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
		}
		pw.close();
	}
	*/
	
	private void getConnectedPairTraverse(String node, CallDAG callDAG) {
		if (visited.contains(node)) 
			return;
		visited.add(node);
		
//		System.out.print(node + " ");
		
		if (startingLeaves.contains(node)) { // is leaf
			if (callDAG.functions.contains(node))
				++connectedPair;
			return;
		}
		
//		a node can become leaf-like if all its children was removed
		if (!callDAG.callTo.containsKey(node))
			return;
		
		for (String s: callDAG.callTo.get(node)) {
			getConnectedPairTraverse(s, callDAG);
		}
	}
	
	private void getConnectedPair(CallDAG callDAG) {
		connectedPair = 0;
		for (String s : startingRoots) {
			if (!callDAG.functions.contains(s))
				continue;
			visited = new HashSet();
//			System.out.println("Traversing " + s);
			getConnectedPairTraverse(s, callDAG);
//			System.out.println();
		}
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
		PrintWriter pw1 = new PrintWriter(new File("Results//centrality-threshold2-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//waist-nodes-" + filePath + ".txt"));
		
		getConnectedPair(callDAG);
		double networkConnetedPair = connectedPair;
		double nodesRemoved = 0;
		double xNodesRemoved = 0;
		double xConnectedFraction = 1;
		pw1.println(xNodesRemoved + "\t" + xConnectedFraction);

		while (callDAG.functions.size() > 0) {
			TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
			for (String s : callDAG.functions) {
				centralitySortedNodes.put(callDAG.centrality.get(s), s);
//				System.out.println(s + "\t" + callDAG.nodePathThrough.get(s) + "\t" + callDAG.nTotalPath);
			}
//			System.out.println("-----");

			Collection<String> nodes = centralitySortedNodes.get(centralitySortedNodes.asMap().firstKey());

			for (String s : nodes) {
				if (callDAG.callTo.containsKey(s)) {
					for (String r : callDAG.callTo.get(s)) {
						callDAG.callFrom.get(r).remove(s);
						if (callDAG.callFrom.get(r).size() < 1)
							callDAG.callFrom.remove(r);
					}
					callDAG.callTo.remove(s);
				}

				if (callDAG.callFrom.containsKey(s)) {
					for (String r : callDAG.callFrom.get(s)) {
						callDAG.callTo.get(r).remove(s);
						if (callDAG.callTo.get(r).size() < 1)
							callDAG.callTo.remove(r);
					}
					callDAG.callFrom.remove(s);
				}

				callDAG.functions.remove(s);

				waistNodes.add(s);
				pw2.println(s);

//				System.out.println("removing: " + s);
			}

			removeIsolatedNodes(callDAG);
			getConnectedPair(callDAG);
			nodesRemoved += nodes.size();
			double connectedFraction = connectedPair / networkConnetedPair;
			pw1.println(nodesRemoved + "\t" + connectedFraction);
//			System.out.println(nodesRemoved + "\t" + connectedPair + "\t" + networkConnetedPair);

			double slope = (connectedFraction - xConnectedFraction) / (nodesRemoved - xNodesRemoved);
//			System.out.println(slope);
//			if (slope < 1)
//				break;
			xConnectedFraction = connectedPair / networkConnetedPair;
			xNodesRemoved = nodesRemoved;

			callDAG.resetAuxiliary();
//			printCallDAG(callDAG);

			callDAG.loadDegreeMetric();
			callDAG.loadLocationMetric(); // must load degree metric before
			callDAG.loadGeneralityMetric();
			callDAG.loadComplexityMetric();
			callDAG.loadCentralityMetric();

			if (nodesRemoved > 40)
				break;
			
//			if (slope > -1)
//				break;
		}
		
		pw1.close();
		pw2.close();
	}
}
