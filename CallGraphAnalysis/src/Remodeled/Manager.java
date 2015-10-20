package Remodeled;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Manager {	
	private static void printNetworkStat(DependencyDAG dependencyDAG) {
		System.out.println(" S: " + dependencyDAG.nSources);
		System.out.println(" T: " + dependencyDAG.nTargets);
		System.out.println(" E: " + dependencyDAG.nEdges);
		System.out.println(" N: " + dependencyDAG.nodes.size());
		System.out.println("Toal Path: " + dependencyDAG.nTotalPath);
	}
	
	private static void generateSyntheticFromReal(DependencyDAG dependencyDAG) throws Exception {
		SyntheticNLDAG2.inDegreeHistogram = DistributionAnalysis.getDegreeHistogram(dependencyDAG);
		SyntheticNLDAG2.numOfNonzeroIndegreeNodes = 0;
		for (int i: SyntheticNLDAG2.inDegreeHistogram.keySet()) {
			SyntheticNLDAG2.numOfNonzeroIndegreeNodes += SyntheticNLDAG2.inDegreeHistogram.get(i);
		}
		SyntheticNLDAG2.dependencyDAG = dependencyDAG;		
		SyntheticNLDAG2.nS = (int)dependencyDAG.nSources;
		SyntheticNLDAG2.nT = (int)dependencyDAG.nTargets;
		SyntheticNLDAG2.nI = (int)(dependencyDAG.nodes.size() - dependencyDAG.nSources - dependencyDAG.nTargets);		
		SyntheticNLDAG2.sT = 1;
		SyntheticNLDAG2.sI = SyntheticNLDAG2.nS + 1;
		SyntheticNLDAG2.sS = SyntheticNLDAG2.nS + SyntheticNLDAG2.nI + 1;
//		double d = 0.6;
		for (double d = 0.0; d < 0.6; d += 0.2) 
		{
			if (d < 0) {
				SyntheticNLDAG2.alphaNegative = true;
			}
			else {
				SyntheticNLDAG2.alphaNegative = false;
			}
			SyntheticNLDAG2.alpha = Math.abs(d);
			SyntheticNLDAG2.random = new Random(System.nanoTime());
			System.out.println("Alpha: " + d);
			SyntheticNLDAG2.getNLNHGDAG();
		}		
	}
	
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
	
	public static void doRealNetworkAnalysis() throws Exception {
		String netPath = "";
//		String netID = "rat";
		String netID = "court";
//		String netID = "monkey";
//		String netID = "commons-math";
		
//		loadLargestWCC(netID);
		
		if (netID.equals("rat")) {
			netPath = "metabolic_networks//rat-consolidated.txt";
//			netPath = "metabolic_networks//rat-links.txt";
			DependencyDAG.isMetabolic = true;
		}
		else if (netID.equals("monkey")) {
			netPath = "metabolic_networks//monkey-consolidated.txt";
			DependencyDAG.isMetabolic = true;
		}
		else if (netID.equals("court")) {
			CourtCaseCornellParser.caseTopic = "monopoly";
			CourtCaseCornellParser.loadCuratedCaseIDs();
			netPath = "supremecourt_networks//court.txt";
			DependencyDAG.isCourtcase = true;
		}
		else if (netID.equals("kernel-21")) {
			netPath = "kernel_callgraphs//full.graph-2.6.21";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("kernel-31")) {
			netPath = "kernel_callgraphs//full.graph-2.6.31";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("openssh-39")) {
			netPath = "openssh_callgraphs//full.graph-openssh-39";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("apache-commons-3.4")) {
			netPath = "jdk_class_dependency//apache-commons-3.4-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}
		else if (netID.equals("jdk1.7")) {
//			netPath = "jdk_class_dependency//jdk1.7-callgraph-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;			
			netPath = "jdk_class_dependency//jdk1.7-callgraph.txt";
//			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("google-guava")) {
			netPath = "jdk_class_dependency//google-guava-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}
		else if (netID.equals("commons-math")) {
			netPath = "jdk_class_dependency//commons-math-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}

		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		
//		generateSyntheticFromReal(dependencyDAG);
		
		printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkMetrics();
		
//		for (String s: dependencyDAG.nodes) {
//			if (dependencyDAG.outDegree.get(s) == 248) {
//				System.out.println(s + "\t" + dependencyDAG.normalizedPathCentrality.get(s));
//			}
//		}
		
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.getAverageInOutDegree(dependencyDAG);
//		DistributionAnalysis.getAveragePathLenth(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, netID);		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		int centralityIndex = 1;
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);
//		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.printAllCentralities(dependencyDAG, netID);
//		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, netID);
//		DistributionAnalysis.createSubnetwork(dependencyDAG, netID);
		
//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
		
//		WaistDetection.pathCoverageThresholdDetection(dependencyDAG, netID);
		
//		MaxFlowReduction.reduceToMaxFlowMinCutNetwork(dependencyDAG, netID);
//		MaxFlowReduction.analyzeMinCut(dependencyDAG, "reduced_maxflow_graphs//" + netID + "_min_cut.txt");
	}
	
	public static void doSyntheticNetworkAnalysis() throws Exception {
		DependencyDAG.isSynthetic = true;
		String DAGType = "NLNHGDAG";
//		String alpha[] = {"-1.0", "-0.8", "-0.6", "-0.4", "-0.2", "0.0", "0.2", "0.4", "0.6", "0.8", "1.0"};
		String alpha[] = {"0.0", "-1.0", "1.0"};
//		String versions[] = {"rectangleDAG", "noisyRectangleDAG", "trapezoidDAG", "diamondDAG", "hourglassDAG"};

		for (String a: alpha) {
//			if (!a.equals("-1.0")) continue;			
			String networkID = DAGType + "a" + a;
			System.out.println("Working on: " + networkID);
			DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
			printNetworkStat(dependencyDAG);
//			dependencyDAG.printNetworkMetrics();
			
//			DistributionAnalysis.printSyntheticPC(dependencyDAG, networkID);
//			DistributionAnalysis.targetEdgeConcentration(dependencyDAG);
//			DistributionAnalysis.getAveragePathLenth(dependencyDAG);
//			DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
//			DistributionAnalysis.getReachabilityCount(dependencyDAG);
//			DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, networkID);
			
//			new GradientFilterAnalysis().getSampleGradientsQuartileInterval(dependencyDAG, networkID);
//			WaistDetection.runPCWaistDetection(dependencyDAG, networkID);
		}
	}
	
	public static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//toy_dag.txt");
		String netID = "toy_dag";
		printNetworkStat(dependencyDAG);
		dependencyDAG.printNetworkMetrics();
		
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);

		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);		
//		DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, netID);

//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);

//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
		
//		MaxFlowReduction.reduceToMaxFlowMinCutNetwork(dependencyDAG, netID);
	}

	public static void main(String[] args) throws Exception {		
		Manager.doRealNetworkAnalysis();
//		Manager.doSyntheticNetworkAnalysis();
//		Manager.doToyNetworkAnalysis();
		System.out.println("Done!");
	}
}
