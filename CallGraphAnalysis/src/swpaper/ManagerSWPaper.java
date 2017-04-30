package swpaper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.omg.CORBA.VersionSpecHelper;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ManagerSWPaper {	
	static String callgraphName = "sqlite";
	static int nVersions = 19;

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
//		DependencyDAG.isCyclic = true;
//		DependencyDAG.isClassDependency = true;
//		System.out.println(netPath + "//" + netID);
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath + "//" + netID);
//		dependencyDAG.printNetworkStat();
		dependencyDAG.printNetworkProperties();
		
//		DistributionAnalysis.getLocationColorWeightedHistogram(dependencyDAG);
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
//		DistributionAnalysis.getLocationColorWeightedHistogram(dependencyDAG);
		
//		LineOfCodeGenerator.parseFile(netPath + "//" + netID.substring(netID.indexOf('o')) + ".c", dependencyDAG.nodes);
		
//		System.out.print(dependencyDAG.nodes.size() + "\t" + dependencyDAG.nEdges + "\t");
		
		/* Central Edge Subgraph */
//		CoreDetection.getCentralEdgeSubgraph(dependencyDAG);
//		Visualization.printDOTNetwork(dependencyDAG);
		
		/* Core Detection */
		CoreDetection.fullTraverse = false;
		CoreDetection.pathCoverageTau = 0.97;
		CoreDetection.getCore(dependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;
//		System.out.print(CoreDetection.minCoreSize);
//		System.out.println();

		/* Flattening */
//		FlattenNetwork.makeAndProcessFlat(dependencyDAG);	
//		CoreDetection.hScore = (1.0 - (realCore / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("H-Score: " + CoreDetection.hScore);
//		System.out.println("\t" + CoreDetection.hScore);
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
		for (int i = 1; i <= nVersions; ++i) {
			if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = DependencyDAG.isCyclic = true;
			DependencyDAG dependencyDAG = new DependencyDAG(callgraphName + "_callgraphs" + "//" 
															+ "full.graph-" + callgraphName + "-" + i);
			
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
			
			CoreDetection.pathCoverageTau = 0.90;
			doRealNetworkAnalysis(callgraphName + "_callgraphs", "full.graph-" + callgraphName + "-" + i);
			
			if (i > 1) {
				
//				getNodeInsertDeleteInfo(previousNodes, dependencyDAG.nodes, i);
				getNodeInsertDeleteInfo2(previousNodes, dependencyDAG.nodes, previousCore, CoreDetection.sampleCore, i);
				
//				getPathCentralityDeviations(previouseNormalizedPathCentrality, dependencyDAG.normalizedPathCentrality, 
//						previousGrayCore, CoreDetection.sampleCore, nonCoreDeviation, false);
//				getStats(nonCoreDeviation);
				
//				getPathCentralityDeviations(previouseNormalizedPathCentrality, dependencyDAG.normalizedPathCentrality, 
//						previousCore, CoreDetection.sampleCore, coreDeviation, true);
//				getStats(coreDeviation);
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
//				System.out.println(patchedFunctions.size() + "\t" + nonCorePatched + "\t" + nonCore + "\t" + corePatched);
//				System.out.println((nonCorePatched / nonCore) + "\t" + (corePatched / CoreDetection.sampleCore.size()));
//				System.out.println((linePatchedNoncore / nonCore) + "\t" + (linePatchedCore / CoreDetection.sampleCore.size()));
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
	
	private static void sumValueMap(HashMap<String, Double> map, String key, double value) {
		if (map.containsKey(key)) {
			double current = map.get(key);
			map.put(key, current + value);
		}
		else {
			map.put(key, value);
		}
	}
	
	private static void avgValueMap(HashMap<String, Double> map, String key, double value) {
		if (map.containsKey(key)) {
			double current = map.get(key);
			map.put(key, (current + value) / 2.0);
		}
		else {
			map.put(key, value);
		}
	}
	
	private static double getCorrelation(HashMap<String, Double> amap, HashMap<String, Double> bmap) {
		int sz = Math.min(amap.size(), bmap.size());
		double a[] = new double[sz];
		double b[] = new double[sz];
		
		int idx = 0;
		for (String s: amap.keySet()) {
			if (bmap.containsKey(s)) {
				a[idx] = amap.get(s);
				b[idx] = bmap.get(s);
				++idx;
			}
		}
		
		if (idx < sz) {
			a = Arrays.copyOfRange(a, 0, idx);
			b = Arrays.copyOfRange(b, 0, idx);
		}
		
		double kendalT = new KendallsCorrelation().correlation(a, b);
		double spmanC = new SpearmansCorrelation().correlation(a, b);
		
		double t = spmanC * Math.sqrt((a.length - 2) / (1 - spmanC * spmanC));
		double p = 1 - new TDistribution(a.length - 1).cumulativeProbability(t);
//		if (numTail == 1)
//			 return pval < p;
//		else
//			 return pval < 2 * p;
		
		System.out.println(spmanC + "\t" + p + "\t" + kendalT);
		return spmanC;
	}
	
	private static void ignoreWrapperBlankNodes(DependencyDAG dependencyDAG, int version) throws Exception{
		CoreDetection.topRemovedWaistNodes.clear();
		LineOfCodeCount.parseFile("openssh_callgraphs" + "//" + "openssh-" + version + ".c", dependencyDAG.nodes);
		for (String s: dependencyDAG.nodes) {
			if (LineOfCodeCount.functionNumLines.containsKey(s) && LineOfCodeCount.functionNumLines.get(s) < 40) {
//				System.out.println("removing " + s);
				CoreDetection.topRemovedWaistNodes.add(s);
			}
		}
	}
	
	private static int getBucket(double feature) {
		double binBoundary[] = new double[]{0.00001, 0.0001, 0.001, 0.005, 
				                            0.01, 0.02, 0.08, 
				                            0.20, 1.0};
		
		int idx = 0;
		for (double d: binBoundary) {
			if (feature <= d) {
				return idx;
			}
			++idx;
		}
		
		return -1;
	}
	
	private static class CentralityVersionPatch implements Comparable<CentralityVersionPatch> {
		private double centrality;
		private String node;
		private boolean patched;
		private int version;
		
		CentralityVersionPatch(double centrality, String node, boolean patched, int version) {
			this.centrality = centrality;
			this.node = node;
			this.patched = patched;
			this.version = version;
		}
		
		public int compareTo(CentralityVersionPatch instance2) {
			if (this.centrality < instance2.centrality) return -1;
			else if (this.centrality > instance2.centrality) return 1;
			else return 0;
		}
	}
	
	private static void getBucket2(ArrayList<CentralityVersionPatch> list) {
		int index = 0;
		while (index < list.size()) {
			int kount = 0;
			int patchKount = 0;
			double centralityBoundary = -1;
			while (index < list.size() && kount < 1000) {
				centralityBoundary = list.get(index).centrality;
				if (list.get(index).patched) {
					++patchKount;
				}
				++kount;
				++index;
			}
			System.out.println(centralityBoundary + "\t" + (patchKount * 1.0 / kount));
		}
	}
	
	private static void analyzePatch() throws Exception {
		HashMap<String, Double> functionPatchFreq = new HashMap();
		HashMap<String, Double> avgFunctionPatchSize = new HashMap();
		HashMap<String, Double> avgCentralityPatchedFunction = new HashMap();
		HashMap<String, Double> avgLocationPatchedFunction = new HashMap();
		HashMap<String, Double> functionAppearanceCount = new HashMap();
		HashMap<String, Double> avgFunctionLOC = new HashMap();
		HashMap<String, Double> normFunctionPatchFreq = new HashMap();
		ArrayList<CentralityVersionPatch> patchCentralityVersionFunctionList = new ArrayList();
		
		int numBucket = 9;
		int pcBucketFreq[] = new int[numBucket];
		int locBucket[] = new int[numBucket];
		int pcBucketPatchCount[] = new int[numBucket];
		int locBucketPatchCount[] = new int[numBucket];
		int maxRank = 200;
		int rankPatchFreq[] = new int[maxRank + 1];
				
		for (int i = 1; i <= 39; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			
			LineOfCodeCount.parseFile("openssh_callgraphs" + "//" + "openssh-" + i + ".c", dependencyDAG.nodes);
			for (String s: dependencyDAG.nodes) {
				sumValueMap(functionAppearanceCount, s, 1);
				
				if (LineOfCodeCount.functionNumLines.containsKey(s)) {
					avgValueMap(avgFunctionLOC, s, LineOfCodeCount.functionNumLines.get(s));
				}
			}
		}
		
		for (int i = 2; i <= 39; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + (i - 1));
		

			HashMap<String, Integer> patchedFunctions = null;
			patchedFunctions = PatchAnalysis.getPatchedFunctions("openssh_patches//patch-" + i + ".txt", dependencyDAG.nodes);
		
			for (String s: dependencyDAG.nodes) {
//				if (appearanceCount.get(s) < 10) continue; // filter out in-frequent functions
				
				boolean patched = false;
				if (patchedFunctions.containsKey(s)) {
					sumValueMap(functionPatchFreq, s, 1);
					avgValueMap(avgFunctionPatchSize, s, patchedFunctions.get(s));
					patched = true;
				}
//				
//				avgValueMap(avgCentralityPF, s, dependencyDAG.normalizedPathCentrality.get(s));
//				avgValueMap(avgLocationPF, s, dependencyDAG.numPathLocation.get(s));
				
				double featurePC = dependencyDAG.normalizedPathCentrality.get(s);
				double featureLoc = dependencyDAG.numPathLocation.get(s);
				if (featureLoc > 0.99) featureLoc = 0.99; // for location
				double feature = featurePC;
				
//				int bucketIndex = ((int)(feature * 10)) % 10;
				int bucketIndex = getBucket(feature);
				
//				if (bucketIndex > 5) System.out.println(s + "\t" + feature + "\t" + (i - 1));
				
				pcBucketFreq[bucketIndex]++;
				if (patchedFunctions.containsKey(s)) {
					pcBucketPatchCount[bucketIndex]++;
//					System.out.println(featurePC);
				}
				
				patchCentralityVersionFunctionList.add(new CentralityVersionPatch(feature, s, patched, (i - 1)));
			}
			
			Collections.sort(patchCentralityVersionFunctionList);
			
			for (String s: functionAppearanceCount.keySet()) {
				if (functionPatchFreq.containsKey(s)) {
					normFunctionPatchFreq.put(s, functionPatchFreq.get(s) / functionAppearanceCount.get(s));
				}
				else {
					normFunctionPatchFreq.put(s, 0.0);
				}
			}
			
//			System.out.println((i - 1) + "\t" + (patchedFunctions.size() * 1.0 / dependencyDAG.nodes.size()));
			

//			ignoreWrapperBlankNodes(dependencyDAG, i - 1);
//			CoreDetection.fullTraverse = false;
//			CoreDetection.pathCoverageTau = 1.0;
//			CoreDetection.getCore(dependencyDAG, "full.graph-openssh-" + (i - 1));
//			System.out.println("Core Size: " + CoreDetection.minCoreSize);
//			for (String s: CoreDetection.averageCoreRank.keySet()) {
////				if (appearanceCount.get(s) < 10) continue; // filter out in-frequent functions
//				
//				int rank = CoreDetection.averageCoreRank.get(s).intValue();
//				if (rank > maxRank) continue;
//				
////				if (rank == 1) System.out.println(s + "\t" + i);
////				if (rank == 2) System.out.println(s + "\t" + i);
//				
//				if (patchedFunctions.containsKey(s)) {
//					rankPatchFreq[rank]++;
//				}
//			}
		}
		
		for (int i = 0; i < numBucket; ++i) {
//			System.out.println((i + 1) + "\t" + (pcBucketPatchCount[i] * 1.0 / pcBucketFreq[i]) + "\t" + pcBucketFreq[i]);
//			System.out.println((i + 1) + "\t" + pcBucketFreq[i]);
		}
		
		for (int i = 1; i <= maxRank; ++i) {
//			System.out.println(i + "\t" + (rankPatchFreq[i] / 39.0));
		}
		
//		getCorrelation(freqPF, avgCentralityPF);
//		getCorrelation(freqPF, avgLocationPF);
//		getCorrelation(normFunctionPatchFreq, avgFunctionLOC);
		
//		System.out.println(patchCentralityVersionFunctionList.size());
		getBucket2(patchCentralityVersionFunctionList);
	}
	
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
	
	public static void analyzeNetworks3() throws Exception {
		for (int i = 1; i <= nVersions; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG(
					callgraphName + "_callgraphs" + "//" + "full.graph-" + callgraphName + "-" + i);

			CoreDetection.pathCoverageTau = 0.90;
			doRealNetworkAnalysis(callgraphName + "_callgraphs", "full.graph-" + callgraphName + "-" + i);

			double meanCoreNumLine = 0;
			double meanCoreNumLineK = 0;
			double meanNonCoreNumLine = 0;
			double meanNonCoreNumLineK = 0;
			for (String s : dependencyDAG.nodes) {
				if (CoreDetection.sampleCore.contains(s)) {
					if (LineOfCodeCount.functionNumLines.containsKey(s)) {
						meanCoreNumLine += LineOfCodeCount.functionNumLines
								.get(s);
						++meanCoreNumLineK;
					}
				} else {
					if (LineOfCodeCount.functionNumLines.containsKey(s)) {
						meanNonCoreNumLine += LineOfCodeCount.functionNumLines
								.get(s);
						++meanNonCoreNumLineK;
					}
				}
			}
			System.out.println((meanCoreNumLine / meanCoreNumLineK) + "\t"
					+ (meanNonCoreNumLine / meanNonCoreNumLineK));
		}
	}
	
	public static void analyzeNetworks2() throws Exception {
		HashMap<String, Double[]> nodeWeightByVersion = new HashMap();
//		HashMap<String, Double> previousCoreWeights = null;
		for (int i = 1; i <= nVersions; ++i) {
			if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;
//			DependencyDAG.resetFlags();
//			DependencyDAG.isCallgraph = true;
//			DependencyDAG dependencyDAG = new DependencyDAG(sw + "_callgraphs" + "//" + "full.graph-" + sw + "-" + i);
//			CoreDetection.pathCoverageTau = 0.90;
			
			doRealNetworkAnalysis(callgraphName + "_callgraphs", "full.graph-" + callgraphName + "-" + i);
			for (String s: CoreDetection.sampleCore) {
				if (nodeWeightByVersion.containsKey(s)) {
					nodeWeightByVersion.get(s)[i] = CoreDetection.coreWeights.get(s);
				}
				else {
					Double arr[] = new Double[nVersions + 1];
					arr[i] = CoreDetection.coreWeights.get(s);
					nodeWeightByVersion.put(s, arr);
				}
			}
			
//			if (i > 1) {
//				getWeightedJaccard(previousCoreWeights, CoreDetection.coreWeights, i);
//			}		
//			previousCoreWeights = new HashMap(CoreDetection.coreWeights);
		}
		
		for (int i = 1; i <= nVersions; ++i) {
			if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;

			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = DependencyDAG.isCyclic = true;			
			DependencyDAG dependencyDAG = new DependencyDAG(callgraphName + "_callgraphs" + "//" + "full.graph-" + callgraphName + "-" + i);
			
			for (String s: nodeWeightByVersion.keySet()) {
				Double arr[] = nodeWeightByVersion.get(s);
				
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
		
		double others[] =  new double[nVersions + 1];
		for (String s: nodeWeightByVersion.keySet()) {
			Double arr[] = nodeWeightByVersion.get(s);
			int i = 1;
			for (; i < nVersions + 1; ++i) {
				if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;
				if (arr[i] > 0.03) {
					break;
				}
			}
			
			if (i > nVersions) {
				for (int j = 1; j < nVersions + 1; ++j) {
					if (callgraphName.equals("sqlite") && (j == 2 || j == 3)) continue;
					if (arr[j] > 0) {
						others[j] += arr[j];
					}
				}
				continue;
			}
			
			System.out.print(s);
			for (i = 1; i < nVersions + 1; ++i) {
				if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;
				System.out.print("\t");
				if (arr[i] == 0) System.out.print(" ");
				else if (arr[i] < 0) System.out.print(" ");
				else System.out.print(arr[i]);
			}
			System.out.println();
		}
		
		System.out.print("Others");
		for (int i = 1; i < nVersions + 1; ++i) {
			if (callgraphName.equals("sqlite") && (i == 2 || i == 3)) continue;
			System.out.print("\t");
			if (others[i] == 0) System.out.print(" ");
			else if (others[i] < 0) System.out.print(" ");
			else System.out.print(others[i]);
		}
		System.out.println();
	}
	
	private static void analyzeNetworks4() throws Exception {	
		for (int i = 1; i <= 13; ++i) {
			doRealNetworkAnalysis("jetuml_dependency", "JetUML-0." + i + ".jar.dot");
		}
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerSWPaper.doRealNetworkAnalysis("jetuml_dependency", "JetUML-0.13.jar.dot");
//		ManagerSWPaper.doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-39");
		
//		ManagerSWPaper.analyzeNetworks();
		ManagerSWPaper.analyzePatch();
//		ManagerSWPaper.analyzeNetworks2();
//		ManagerSWPaper.analyzeNetworks3();
		
//		ManagerSWPaper.analyzeNetworks4();
//		System.out.println("Done!");
	}
}
