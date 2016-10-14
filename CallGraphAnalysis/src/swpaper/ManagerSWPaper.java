package swpaper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ManagerSWPaper {	
	private static void loadLargestWCC(String filePath) throws Exception {
		DependencyDAG.largestWCCNodes = new HashSet();
		Scanner scanner = new Scanner(new File("analysis//largestWCC-" + filePath + ".txt"));
		while (scanner.hasNext()) {
			String line = scanner.next();
//			System.out.println(line);
			DependencyDAG.largestWCCNodes.add(line);
		}
		scanner.close();
	}
	
	private static void doRealNetworkAnalysis(String netPath, String netID) throws Exception {
		DependencyDAG.resetFlags();
//		loadLargestWCC(netID);
		
		/*
		if (netID.equals("openssh-39")) {
			netPath = "openssh_callgraphs//full.graph-openssh-39";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("apache-commons-3.4")) {
			netPath = "jdk_class_dependency//apache-commons-3.4-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}
		else if (netID.equals("commons-math")) {
			netPath = "jdk_class_dependency//commons-math-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}
		else if (netID.equals("jetuml")) {
			netPath = "jdk_class_dependency//jetuml-callgraph.txt";
			DependencyDAG.isClassDependency = true;
		}
		*/

		DependencyDAG.isCallgraph = true;
//		DependencyDAG.isClassDependency = true;
		DependencyDAG dependencyDAG = new DependencyDAG(netPath + "//" + netID);
//		DependencyDAG.printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkProperties();
		
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.getAverageInOutDegree(dependencyDAG);
//		DistributionAnalysis.getPathLength(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogramSpecialized(dependencyDAG);
//		DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, netID);		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		int centralityIndex = 1;
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);
//		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.printAllCentralities(dependencyDAG, netID);
//		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, netID);
		
//		Core Detection
//		CoreDetection.fullTraverse = true;
		CoreDetection.getCore(dependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

//		Flattening
		//FlattenNetwork.makeAndProcessFlat(dependencyDAG);	
		//CoreDetection.hScore = (1.0 - (realCore / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("H-Score: " + CoreDetection.hScore);
	}
	
	private static void getNodeInsertDeleteInfo(Set<String> previous, Set<String> current, int transition) {
		double deleted = 0;
		double kept = 0;
		for (String s: previous) {
			if (!current.contains(s)) ++deleted;
			else ++kept;
		}
		double inserted = current.size() - kept;
		System.out.println(previous + "\t" + current);
		System.out.println(transition + "\t" + (inserted/previous.size()) + "\t" + (deleted/previous.size()));
	}
	
	private static void getNodeInsertDeleteInfo2(Set<String> previousNodes, Set<String> currentNodes, 
			Set<String> previousCore, Set<String> currentCore, int transition) {
			double birth = 0;
			double death = 0;
			double promotion = 0;
			double demotion = 0;
			
			for (String s: currentCore) {
				if (!previousCore.contains(s) && previousNodes.contains(s)) {
					++promotion;
				}	
				if (!previousCore.contains(s) && !previousNodes.contains(s)) {
					++birth;
				}
			}
		
			for (String s: previousCore) {
				if (!currentCore.contains(s) && currentNodes.contains(s)) {
					++demotion;
				}
				if (!currentCore.contains(s) && !currentNodes.contains(s)) {
					++death;
				}
			}
			
//			System.out.println(birth + "\t" + death + "\t" + promotion + "\t" + demotion);
			System.out.println((birth/previousCore.size()) + "\t" + (death/previousCore.size()) 
					+ "\t" + (promotion/previousCore.size()) + "\t" + (demotion/previousCore.size()));
	}
	
	private static void getStats(List<Double> numList) {
		double numArr[] = new double[numList.size()]; 
		int idx = 0;
		for (double d: numList) {
			numArr[idx++] = d;
		}
		System.out.println(StatUtils.mean(numArr) + "\t" + Math.sqrt(StatUtils.variance(numArr)));
	}
	
	private static void getPathCentralityDeviations(Map<String, Double> previousCentrality, Map<String, Double> currentCentrality, 
			Set<String> previousCore, Set<String> currentCore, List<Double> centralityDeviation, boolean getCore) {		
		for (String s: currentCentrality.keySet()) {
			if (previousCentrality.containsKey(s)) {
				if (!getCore) {
					if (!currentCore.contains(s) && !previousCore.contains(s)) {
						double change = currentCentrality.get(s) - previousCentrality.get(s);
						centralityDeviation.add(change);
					}
				}
				else {
					if (currentCore.contains(s) || previousCore.contains(s)) {
						double change = currentCentrality.get(s) - previousCentrality.get(s);
						centralityDeviation.add(change);
					}
				}
			}
		}
	}
			
	private static void analyzeNetworks() throws Exception {
//		HashMap<String, Integer> appearanceFrequency = new HashMap();
//		HashMap<String, Integer> firstAppearance = new HashMap();
//		HashMap<String, Double> lifeSpan = new HashMap();
		HashSet<String> previousNodes = null;
		TreeSet<String> previousCore = null;
		TreeSet<String> previousGrayCore = null;
		HashMap<String, Double> previouseNormalizedPathCentrality = null;
		ArrayList<Double> coreDeviation = new ArrayList();
		ArrayList<Double> nonCoreDeviation = new ArrayList();
		for (int i = 1; i <= 39; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			
//			for (String s: dependencyDAG.nodes) {
//				if (appearanceFrequency.containsKey(s)) {
//					appearanceFrequency.put(s, appearanceFrequency.get(s) + 1);
//				}
//				else {
//					appearanceFrequency.put(s, 1);
//				}
//				
//				if (!firstAppearance.containsKey(s)) {
//					firstAppearance.put(s, i);
//				}
//			}
//			
//			CoreDetection.pathCoverageTau = 0.9999;
//			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
			
			CoreDetection.pathCoverageTau = 0.95;
			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
			
			if (i > 1) {
				
//				getNodeInsertDeleteInfo(previousNodes, dependencyDAG.nodes, i);
//				getNodeInsertDeleteInfo2(previousNodes, dependencyDAG.nodes, previousCore, CoreDetection.sampleCore, i);
				
//				getPathCentralityDeviations(previouseNormalizedPathCentrality, dependencyDAG.normalizedPathCentrality, 
//						previousGrayCore, CoreDetection.sampleCore, nonCoreDeviation, false);
//				getStats(nonCoreDeviation);
				
				getPathCentralityDeviations(previouseNormalizedPathCentrality, dependencyDAG.normalizedPathCentrality, 
						previousCore, CoreDetection.sampleCore, coreDeviation, true);
				getStats(coreDeviation);
			}
		
//			previousGrayCore = new TreeSet(CoreDetection.sampleCore);
			previousCore = new TreeSet(CoreDetection.sampleCore);
			previousNodes = new HashSet(dependencyDAG.nodes);
			previouseNormalizedPathCentrality = new HashMap(dependencyDAG.normalizedPathCentrality);
		}
//		
//		for (String s: appearanceFrequency.keySet()) {
//			lifeSpan.put(s, appearanceFrequency.get(s) / (39.0 - firstAppearance.get(s) + 1));
//		}
		
//		HashMap<Integer, Double> meanLifeSpanPerVersion = new HashMap();
//		HashMap<Integer, Double> meanNonCoreLifeSpanPerVersion = new HashMap();
//		for (int i = 1; i <= 39; ++i) {
//			DependencyDAG.resetFlags();
//			DependencyDAG.isCallgraph = true;
//			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
//			double nodeLifeSpan[] = new double[dependencyDAG.nodes.size()];
//			int idx = 0;
//			for (String s: dependencyDAG.nodes) {
//				nodeLifeSpan[idx++] = lifeSpan.get(s);
//			}
//			meanLifeSpanPerVersion.put(i, StatUtils.mean(nodeLifeSpan));
//			System.out.println(dependencyDAG.nodes.size() + "\t" + dependencyDAG.nEdges);
			
//			HashMap<String, Integer> patchedFunctions = null;
//			if (i < 39) {
//				patchedFunctions = PatchAnalysis.getPatchedFunctions("openssh_patches//patch-" + (i + 1) + ".txt", dependencyDAG.nodes);
//			
//				CoreDetection.pathCoverageTau = 0.9999;
//				doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
//				double nonCorePatched = 0;
//				double nonCore = 0;
//				double linePatchedNoncore = 0;
//				for (String s : dependencyDAG.nodes) {
//					if (CoreDetection.sampleCore.contains(s)) {
//						continue; // only consider strictly non core nodes
//					}
//					if (patchedFunctions.containsKey(s)) {
//						++nonCorePatched;
//						linePatchedNoncore += patchedFunctions.get(s);
//					}
//					++nonCore;
//				}
//
//				CoreDetection.pathCoverageTau = 0.95;
//				doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
//				double corePatched = 0;
//				double linePatchedCore = 0;
//				for (String s : CoreDetection.sampleCore) {
//					if (patchedFunctions.containsKey(s)) {
//						++corePatched;
//						linePatchedCore += patchedFunctions.get(s);
//					}
//				}
//				
////				System.out.println(patchedFunctions.size() + "\t" + nonCorePatched + "\t" + nonCore + "\t" + corePatched);
////				System.out.println((nonCorePatched / nonCore) + "\t" + (corePatched / CoreDetection.sampleCore.size()));
////				System.out.println((linePatchedNoncore / nonCore) + "\t" + (linePatchedCore / CoreDetection.sampleCore.size()));
//			}
			
//			CoreDetection.pathCoverageTau = 0.9999;
//			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
//			double nonCoreNodeLifeSpan[] = new double[dependencyDAG.nodes.size()];
//			idx = 0;
//			for (String s: dependencyDAG.nodes) {
//				if (CoreDetection.sampleCore.contains(s)) {
//					continue; // only consider strictly non core nodes
//				}
//				nonCoreNodeLifeSpan[idx++] = lifeSpan.get(s);
//			}
//			nonCoreNodeLifeSpan = Arrays.copyOfRange(nonCoreNodeLifeSpan, 0, idx);
//			meanNonCoreLifeSpanPerVersion.put(i, StatUtils.mean(nonCoreNodeLifeSpan));	
//		}
		
//		HashMap<String, Integer> waistFrequency = new HashMap();
//		HashMap<Integer, Double> meanCoreLifeSpanPerVersion = new HashMap();
//		previousCore = null;
//		for (int i = 1; i <= 39; ++i) {
//			CoreDetection.pathCoverageTau = 0.95;
//			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
////			System.out.println(CoreDetection.hScore);
////			System.out.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
//			double coreLifeSpan[] = new double[CoreDetection.sampleCore.size()];
//			int idx = 0;
//			for (String s: CoreDetection.sampleCore) {
//				if (waistFrequency.containsKey(s)) {
//					waistFrequency.put(s, waistFrequency.get(s) + 1);
//				}
//				else {
//					waistFrequency.put(s, 1);
//				}
//				coreLifeSpan[idx++] = lifeSpan.get(s);
//			}
//			
//			meanCoreLifeSpanPerVersion.put(i, StatUtils.mean(coreLifeSpan));
////			System.out.println(CoreDetection.sampleCore.size()); 
//			
//			if (i > 1) {
////				System.out.println(Util.getJaccardDistance(previousCore, CoreDetection.sampleCore));
//				getNodeInsertDeleteInfo(previousCore, CoreDetection.sampleCore, i);
//			}
////			System.out.println(CoreDetection.sampleCore);
//			previousCore = new TreeSet(CoreDetection.sampleCore);
//		}
		
//		for (String s: waistFrequency.keySet()) {
//			System.out.println(s + "\t" + waistFrequency.get(s) + "\t" + appearanceFrequency.get(s));
//			System.out.println(waistFrequency.get(s) / appearanceFrequency.get(s));
//		}
		
//		for (int i = 1; i <= 39; ++i) {
//			System.out.println(meanLifeSpanPerVersion.get(i) + "\t" + meanCoreLifeSpanPerVersion.get(i));
//			System.out.println(meanNonCoreLifeSpanPerVersion.get(i) + "\t" + meanCoreLifeSpanPerVersion.get(i));
//		}
	}
	
//	private static void analyzePatch() {
//		for (int i = 2; i <= 39; ++i) {
//			DependencyDAG.resetFlags();
//			DependencyDAG.isCallgraph = true;
//			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
//			for (String s: dependencyDAG.nodes) {
//			}
//	}
	
	private static void getWeightedJaccard(HashMap<String, Double> previous, HashMap<String, Double> current, int stage) {
		double denominator = 0;
		double numerator = 0;
		
		for (String s: previous.keySet()) {
			if (current.containsKey(s)) {
				double w = (previous.get(s) + current.get(s)) * 0.5;
				numerator += w;
			}
		}
		
		for (String s: previous.keySet()) {
			denominator += previous.get(s);
		}
		
		for (String s: current.keySet()) {
			denominator += current.get(s);
		}
		
		denominator -= numerator;
		
		System.out.println(stage + "\t" + (numerator/denominator));
	}
	
	public static void analyzeNetworks2() throws Exception {
		int nVersions = 39;
		HashMap<String, Double[]> nodeRankByVersion = new HashMap();
		HashMap<String, Double> previousCoreWeights = null;
		for (int i = 1; i <= nVersions; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			
			CoreDetection.pathCoverageTau = 0.91;
			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
			
			for (String s: CoreDetection.averageCoreRank.keySet()) {
//				System.out.println(s + "\t" + CoreDetection.averageCoreRank.get(s));
				if (nodeRankByVersion.containsKey(s)) {
//					nodeRankByVersion.get(s)[i] = CoreDetection.averageCoreRank.get(s);
					nodeRankByVersion.get(s)[i] = CoreDetection.coreWeights.get(s);
				}
				else {
					Double arr[] = new Double[nVersions + 1];
//					arr[i] = CoreDetection.averageCoreRank.get(s);
					arr[i] = CoreDetection.coreWeights.get(s);
					nodeRankByVersion.put(s, arr);
				}
			}
			
			if (i > 1) {
//				getWeightedJaccard(previousCoreWeights, CoreDetection.coreWeights, i);
			}
			
			previousCoreWeights = new HashMap(CoreDetection.coreWeights);
		}
		
		for (int i = 1; i <= nVersions; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			
			for (String s: nodeRankByVersion.keySet()) {
				Double arr[] = nodeRankByVersion.get(s);
				
				if (arr[i] == null) {
					if (dependencyDAG.nodes.contains(s)) {
						arr[i] = 0.0;
					}
					else {
						arr[i] = -1.0;
					}
				}
			}	
		}
		
		for (String s: nodeRankByVersion.keySet()) {
			Double arr[] = nodeRankByVersion.get(s);
			int i = 1;
			for (; i < nVersions + 1; ++i) {
				if (arr[i] > 0.03) {
					break;
				}
			}
			if (i > nVersions) continue;
			
			System.out.print(s);
			for (i = 1; i < nVersions + 1; ++i) {
				System.out.print("\t");
				if (arr[i] == 0) System.out.print(" ");
				else if (arr[i] < 0) System.out.print(" ");
				else System.out.print(arr[i]);
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerSWPaper.doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-39");
//		ManagerSWPaper.analyzeNetworks();
//		ManagerSWPaper.analyzePatch();
		ManagerSWPaper.analyzeNetworks2();
		System.out.println("Done!");
	}
}
