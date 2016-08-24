package initial_1;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class CoreAnalysis {
	int nodeThreshold;
	
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
	
	double connectedPair;
	HashSet<String> visited;
	
	TreeMultimap<Integer, String> centralityDistributionByHopDistance;
	
	// for 5 layer centrality
	double averageLBCentrality;
	double averageLNBCentrality;
	double averageUTCentrality;
	double averageUNTCentrality;
	
	String version;
	
	CoreAnalysis(CallDAG callDAG, CallDAG takeApartCallDAG, String versionNum) throws Exception {
		nodeThreshold = (int)(callDAG.functions.size() * 0.02);
//		nodeThreshold = 100;
		
		this.version = versionNum;
		
		waistNodes = new HashSet<String>();
		waistDownwardReachableNodes = new HashSet<String>();
		waistUpwardReachableNodes = new HashSet<String>();
		waistNotReachableNodes = new HashSet<String>();
		shortestHopDistance = new HashMap<String, Integer>();
		centralityDistributionByHopDistance = TreeMultimap.create();
			
		startingRoots = new HashSet<String>();
		startingLeaves = new HashSet<String>();
		for (String s: callDAG.functions) {
			if (!callDAG.callTo.containsKey(s))
				startingLeaves.add(s);
			if (!callDAG.callFrom.containsKey(s))
				startingRoots.add(s);
		}
		
		getCoreNodes(takeApartCallDAG);
		waistWidth = waistNodes.size() * 1.0 / callDAG.functions.size();		

		getNonCoreNodes(callDAG);

//		getHScore(callDAG);
//		get5LayerHScore(callDAG);
//		getCentralityTrendRegression(callDAG);
		System.out.println(coverage + "\t" + averageWaistCentrality + "\t" + waistWidth + "\t" + hourGlassTrend);
	}
	
	private void getCentralityTrendRegression(CallDAG callDAG) {
		SimpleRegression upTrendRegression = new SimpleRegression();
		SimpleRegression downTrendRegression = new SimpleRegression();
		
		for (String s: waistNodes) {
			upTrendRegression.addData(0, callDAG.centrality.get(s));
			downTrendRegression.addData(0, callDAG.centrality.get(s));
//			System.out.println(s + "\t" + 0 + "\t" + callDAG.centrality.get(s));
		}
		
		for (String s: waistUpwardReachableNodes) {
			upTrendRegression.addData(shortestHopDistance.get(s) * -1, callDAG.centrality.get(s));
//			System.out.println(s + "\t" + shortestHopDistance.get(s) * -1 + "\t" + callDAG.centrality.get(s));

		}
		
		System.out.println("Upward Slope: " + upTrendRegression.getSlope());
		System.out.println("Upward Slope Confidence Interval: " + upTrendRegression.getSlopeConfidenceInterval());
		System.out.println("Upward Pearson's r: " + upTrendRegression.getR());
		
		for (String s: waistDownwardReachableNodes) {
			downTrendRegression.addData(shortestHopDistance.get(s), callDAG.centrality.get(s));
		}
		
		System.out.println("Downward Slope: " + downTrendRegression.getSlope());
		System.out.println("Downward Slope Confidence Interval: " + downTrendRegression.getSlopeConfidenceInterval());
		System.out.println("Downward Pearson's r: " + downTrendRegression.getR());
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
	
	private void traverse(CallDAG callDAG, String node, HashSet<String> stringSet) {
		if (stringSet.contains(node)) return;
		
		stringSet.add(node);
		
		if (callDAG.callFrom.containsKey(node)) {
			for (String s : callDAG.callFrom.get(node)) {
				traverse(callDAG, s, stringSet);
			}
		}

		if (callDAG.callTo.containsKey(node)) {
			for (String s : callDAG.callTo.get(node)) {
				traverse(callDAG, s, stringSet);
			}
		}
	}
	
	private void getNonCoreNodes(CallDAG callDAG) throws Exception {
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
		
		/** turn off later **/
//		printONodesCallGraph(waistNotReachableNodes, callDAG);
		
		coverage = 1.0 - (waistNotReachableNodes.size() * 1.0 / callDAG.functions.size());
	}
	
	private void printONodesCallGraph(HashSet<String> ONodes, CallDAG callDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + version + "-O-Graph.txt"));
		
		HashSet<String> OReachableNodes = new HashSet<String>();
		
		for (String s: ONodes) {
			if (OReachableNodes.contains(s)) continue;
			traverse(callDAG, s, OReachableNodes);
		}
		
		for (String s: callDAG.callTo.keySet()) {
			for (String r: callDAG.callTo.get(s)) {
				if (OReachableNodes.contains(s) && OReachableNodes.contains(r)) {
					pw.println(s + " -> " + r + ";");
				}
			}
		}
		
		pw.close();
	}
	
	private void getCoreNodes(CallDAG takeApartCallDAG) throws Exception {
//		for(String s: takeApartCallDAG.functions) {
//			System.out.println(s + "\t" + takeApartCallDAG.centrality.get(s));
//		}
		
//		System.out.println(this.version);
		getWaistCentralityThreshold_2(takeApartCallDAG, this.version);
		takeApartCallDAG = null;
	}
	
	/*	
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
			visited = new HashSet<String>();
//			System.out.println("Traversing " + s);
			getConnectedPairTraverse(s, callDAG);
//			System.out.println();
		}
	}
	
	
	
//	public void getWaistCentralityThreshold_1(CallDAG callDAG, String filePath) throws Exception {
//		PrintWriter pw1 = new PrintWriter(new File("Results//centrality-threshold1-" + filePath + ".txt"));
//		PrintWriter pw2 = new PrintWriter(new File("Results//waist-nodes-" + filePath + ".txt"));
//		
//		getConnectedPair(callDAG);
//		double networkConnetedPair = connectedPair;
//		double nodesRemoved = 0;
//		double xNodesRemoved = 0;
//		double xConnectedFraction = 1;
//		pw1.println(xNodesRemoved + "\t" + xConnectedFraction);
//
//		while (callDAG.functions.size() > 0) {
//			TreeMultimap<Double, String> centralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
//			for (String s : callDAG.functions) {
//				centralitySortedNodes.put(callDAG.centrality.get(s), s);
////				System.out.println(s + "\t" + callDAG.nodePathThrough.get(s) + "\t" + callDAG.nTotalPath);
//			}
////			System.out.println("-----");
//
//			Collection<String> nodes = centralitySortedNodes.get(centralitySortedNodes.asMap().firstKey());
//
//			for (String s : nodes) {
//				if (callDAG.callTo.containsKey(s)) {
//					for (String r : callDAG.callTo.get(s)) {
//						callDAG.callFrom.get(r).remove(s);
//						if (callDAG.callFrom.get(r).size() < 1)
//							callDAG.callFrom.remove(r);
//					}
//					callDAG.callTo.remove(s);
//				}
//
//				if (callDAG.callFrom.containsKey(s)) {
//					for (String r : callDAG.callFrom.get(s)) {
//						callDAG.callTo.get(r).remove(s);
//						if (callDAG.callTo.get(r).size() < 1)
//							callDAG.callTo.remove(r);
//					}
//					callDAG.callFrom.remove(s);
//				}
//
//				callDAG.functions.remove(s);
//
//				waistNodes.add(s);
//				pw2.println(s);
//
////				System.out.println("removing: " + s);
//			}
//
//			removeIsolatedNodes(callDAG);
//			getConnectedPair(callDAG);
//			nodesRemoved += nodes.size();
//			double connectedFraction = connectedPair / networkConnetedPair;
//			pw1.println(nodesRemoved + "\t" + connectedFraction);
////			System.out.println(nodesRemoved + "\t" + connectedPair + "\t" + networkConnetedPair);
//
////			double slope = (connectedFraction - xConnectedFraction) / (nodesRemoved - xNodesRemoved);
////			System.out.println(slope);
//
//			xConnectedFraction = connectedPair / networkConnetedPair;
//			xNodesRemoved = nodesRemoved;
//
////			if (nodesRemoved > nodeThreshold)
////				break;
//			
////			if (slope > -1)
////				break;
//			
//			if (xConnectedFraction < 0.05) break;
//		}
//		
//		pw1.close();
//		pw2.close();
//	}
		
	public void getWaistCentralityThreshold_2(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("Results//centrality-threshold2-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//waist-nodes-" + filePath + ".txt"));
		
		getConnectedPair(callDAG);
		double networkConnetedPair = connectedPair;
		double nodesRemoved = 0;
		double xNodesRemoved = 0;
		double xConnectedFraction = 1;
		pw1.println(xNodesRemoved + "\t" + xConnectedFraction);
		System.out.println(xNodesRemoved + "\t" + xConnectedFraction);
		
		ArrayList<Double> xValues = new ArrayList<Double>();
		ArrayList<Double> yValues = new ArrayList<Double>();

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

			callDAG.removeIsolatedNodes();
			getConnectedPair(callDAG);
			nodesRemoved += nodes.size();
			double connectedFraction = connectedPair / networkConnetedPair;
			pw1.println(nodesRemoved + "\t" + connectedFraction);
			xValues.add(nodesRemoved);
			yValues.add(connectedFraction);
			System.out.println(nodesRemoved + "\t" + connectedFraction);

			double slope = (connectedFraction - xConnectedFraction) / (nodesRemoved - xNodesRemoved);
//			System.out.println(slope);

			xConnectedFraction = connectedPair / networkConnetedPair;
			xNodesRemoved = nodesRemoved;

//			callDAG.printCallDAG();
			callDAG.resetAuxiliary();
			callDAG.loadDegreeMetric();
			callDAG.loadLocationMetric(); // must load degree metric before
			callDAG.loadCentralityMetric();

			if (nodesRemoved > nodeThreshold)
				break;
			
			if (xConnectedFraction < 0.15) 
				break;
			
//			if (slope > -1)
//				break;
			
		}
		
//		System.out.println("+++++++++++++++++++++++++");
//		
//		for (int i = xValues.size() - 3; i >= 0; --i) {
//			double x1 = xValues.get(i);
//			double y1 = yValues.get(i);
//			double x2 = xValues.get(i + 1);
//			double y2 = yValues.get(i + 1);
//			double x3 = xValues.get(i + 2);
//			double y3 = yValues.get(i + 2);
//			
//			double d1 = Math.abs((y2 - y1) / (x2 - x1));
//			double d2 = Math.abs((y3 - y2) / (x3 - x2));
//			double d = (d1 + d2) * 0.5;
//			double dd = Math.abs((d2 - d1) / ((x3 - x1) * .05)); 
//			double R = Math.pow((1 + (d * d)) / dd, 1.5);
//			
//			System.out.println(x2 + "\tD2: " + dd + "\tR: " + R);
//		}
		
		pw1.close();
		pw2.close();
	}
}
