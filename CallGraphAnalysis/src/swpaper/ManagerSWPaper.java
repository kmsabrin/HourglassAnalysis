package swpaper;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlattenNetwork;

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
		FlattenNetwork.makeAndProcessFlat(dependencyDAG);	
		CoreDetection.hScore = (1.0 - (realCore / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("H-Score: " + CoreDetection.hScore);
	}
	
	private static void measureTauEffectOnRealNetwork() throws Exception {
//		String data[] = {"openssh-39", "commons-math"};
//		PrintWriter pw = new PrintWriter(new File("analysis//hscore-vs-tau-" + data[5] + ".txt"));
		
		HashMap<String, Double> waistFrequency = new HashMap();
		for (int i = 1; i <= 39; ++i) {
			CoreDetection.pathCoverageTau = 0.9;
			DependencyDAG.resetFlags();
			doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-" + i);
//			System.out.println(CoreDetection.hScore);
//			System.out.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
//			pw.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
			for (String s: CoreDetection.sampleCore) {
				if (waistFrequency.containsKey(s)) {
					waistFrequency.put(s, waistFrequency.get(s) + 1.0);
				}
				else {
					waistFrequency.put(s, 1.0);
				}
			}
		}
		
		for (String s: waistFrequency.keySet()) {
			System.out.println(waistFrequency.get(s) / 39.0);
		}
//		pw.close();
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerSWPaper.doRealNetworkAnalysis("openssh_callgraphs", "full.graph-openssh-39");
		ManagerSWPaper.measureTauEffectOnRealNetwork();
		System.out.println("Done!");
	}
}
