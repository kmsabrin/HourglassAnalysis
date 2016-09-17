package swpaper;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import utilityhg.Util;

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
		CoreDetection.fullTraverse = true;
		CoreDetection.getCore(dependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

//		Flattening
		//FlattenNetwork.makeAndProcessFlat(dependencyDAG);	
		//CoreDetection.hScore = (1.0 - (realCore / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("H-Score: " + CoreDetection.hScore);
	}
	
	private static void analyzeNetworks() throws Exception {
		HashMap<String, Integer> appearanceFrequency = new HashMap();
		HashMap<String, Integer> firstAppearance = new HashMap();
		HashMap<String, Double> lifeSpan = new HashMap();
		HashMap<Integer, Double> meanLifeSpanPerVersion = new HashMap();
		for (int i = 1; i <= 39; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			for (String s: dependencyDAG.nodes) {
				if (appearanceFrequency.containsKey(s)) {
					appearanceFrequency.put(s, appearanceFrequency.get(s) + 1);
				}
				else {
					appearanceFrequency.put(s, 1);
				}
				
				if (!firstAppearance.containsKey(s)) {
					firstAppearance.put(s, i);
				}
			}
		}
		
		for (String s: appearanceFrequency.keySet()) {
			lifeSpan.put(s, appearanceFrequency.get(s) / (39.0 - firstAppearance.get(s) + 1));
		}
		
		for (int i = 1; i <= 39; ++i) {
			DependencyDAG.resetFlags();
			DependencyDAG.isCallgraph = true;
			DependencyDAG dependencyDAG = new DependencyDAG("openssh_callgraphs" + "//" + "full.graph-openssh-" + i);
			double nodeLifeSpan[] = new double[dependencyDAG.nodes.size()];
			int idx = 0;
			for (String s: dependencyDAG.nodes) {
				nodeLifeSpan[idx++] = lifeSpan.get(s);
			}
			meanLifeSpanPerVersion.put(i, StatUtils.mean(nodeLifeSpan));
//			System.out.println(dependencyDAG.nodes.size() + "\t" + dependencyDAG.nEdges);
		}
		
		HashMap<String, Integer> waistFrequency = new HashMap();
		HashMap<Integer, Double> meanCoreLifeSpanPerVersion = new HashMap();
		TreeSet<String> previousCore = null;
		for (int i = 1; i <= 39; ++i) {
			CoreDetection.pathCoverageTau = 0.95;
			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
//			System.out.println(CoreDetection.hScore);
//			System.out.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
			double coreLifeSpan[] = new double[CoreDetection.sampleCore.size()];
			int idx = 0;
			for (String s: CoreDetection.sampleCore) {
				if (waistFrequency.containsKey(s)) {
					waistFrequency.put(s, waistFrequency.get(s) + 1);
				}
				else {
					waistFrequency.put(s, 1);
				}
				coreLifeSpan[idx++] = lifeSpan.get(s);
			}
			
			meanCoreLifeSpanPerVersion.put(i, StatUtils.mean(coreLifeSpan));
//			System.out.println(CoreDetection.sampleCore.size()); 
			
			if (i > 1) {
//				System.out.println(Util.getJaccardDistance(previousCore, CoreDetection.sampleCore));
			}
//			System.out.println(CoreDetection.sampleCore);
			previousCore = new TreeSet(CoreDetection.sampleCore);
		}
		
		for (String s: waistFrequency.keySet()) {
//			System.out.println(s + "\t" + waistFrequency.get(s) + "\t" + appearanceFrequency.get(s));
//			System.out.println(waistFrequency.get(s) / appearanceFrequency.get(s));
		}
		
		for (int i = 1; i <= 39; ++i) {
			System.out.println(meanLifeSpanPerVersion.get(i) + "\t" + meanCoreLifeSpanPerVersion.get(i));
		}
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerSWPaper.doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-39");
		ManagerSWPaper.analyzeNetworks();
		System.out.println("Done!");
	}
}
