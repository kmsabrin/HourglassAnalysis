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
		SimpleModelDAG.inDegreeHistogram = DistributionAnalysis.getDegreeHistogram(dependencyDAG);
		SimpleModelDAG.numOfNonzeroIndegreeNodes = 0;
		for (int i: SimpleModelDAG.inDegreeHistogram.keySet()) {
			SimpleModelDAG.numOfNonzeroIndegreeNodes += SimpleModelDAG.inDegreeHistogram.get(i);
		}
		SimpleModelDAG.dependencyDAG = dependencyDAG;		
		SimpleModelDAG.nS = (int)dependencyDAG.nSources;
		SimpleModelDAG.nT = (int)dependencyDAG.nTargets;
		SimpleModelDAG.nI = (int)(dependencyDAG.nodes.size() - dependencyDAG.nSources - dependencyDAG.nTargets);		
		SimpleModelDAG.sT = 1;
		SimpleModelDAG.sI = SimpleModelDAG.nS + 1;
		SimpleModelDAG.sS = SimpleModelDAG.nS + SimpleModelDAG.nI + 1;
//		double d = 0.6;
		for (double d = 0.0; d < 0.6; d += 0.2) 
		{
			if (d < 0) {
				SimpleModelDAG.alphaNegative = true;
			}
			else {
				SimpleModelDAG.alphaNegative = false;
			}
			SimpleModelDAG.alpha = Math.abs(d);
			SimpleModelDAG.random = new Random(System.nanoTime());
			System.out.println("Alpha: " + d);
			SimpleModelDAG.getSimpleModelDAG();
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
//		String netID = "monkey";
		
		String netID = "commons-math";
//		String netID = "openssh-39";
//		String netID = "apache-commons-3.4";
		
//		String netID = "court";
		
		if (netID.equals("rat") || netID.equals("monkey")) {
			loadLargestWCC(netID);
		}
		
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
//			CourtCaseCornellParser.caseTopic = "abortion";
			CourtCaseCornellParser.caseTopic = "pension";
			CourtCaseCornellParser.loadCuratedCaseIDs();
			netPath = "supremecourt_networks//court.txt";
			DependencyDAG.isCourtcase = true;
		}
//		else if (netID.equals("kernel-21")) {
//			netPath = "kernel_callgraphs//full.graph-2.6.21";
//			DependencyDAG.isCallgraph = true;
//		}
//		else if (netID.equals("kernel-31")) {
//			netPath = "kernel_callgraphs//full.graph-2.6.31";
//			DependencyDAG.isCallgraph = true;
//		}
		else if (netID.equals("openssh-39")) {
			netPath = "openssh_callgraphs//full.graph-openssh-39";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("apache-commons-3.4")) {
			netPath = "jdk_class_dependency//apache-commons-3.4-callgraph-consolidated.txt";
			DependencyDAG.isClassDependency = true;
		}
//		else if (netID.equals("jdk1.7")) {
////			netPath = "jdk_class_dependency//jdk1.7-callgraph-callgraph-consolidated.txt";
//			DependencyDAG.isClassDependency = true;			
//			netPath = "jdk_class_dependency//jdk1.7-callgraph.txt";
////			DependencyDAG.isCallgraph = true;
//		}
//		else if (netID.equals("google-guava")) {
//			netPath = "jdk_class_dependency//google-guava-callgraph-consolidated.txt";
//			DependencyDAG.isClassDependency = true;
//		}
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
		
//		DistributionAnalysis.crossCheck(dependencyDAG, netID);
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.getAverageInOutDegree(dependencyDAG);
//		DistributionAnalysis.getAveragePathLenth(dependencyDAG);
		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogramSpecialized(dependencyDAG);
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
		DependencyDAG.isWeighted = true;
		
//		String DAGType = "ComplexModelDAG";
//		DependencyDAG.isComplexModel = true;
//		ComplexModelDAG.loadLayerIndex();
		
		String DAGType = "SimpleModelDAG";
		DependencyDAG.isSimpleModel = true;
		
//		String alpha[] = {"-1.0", "-0.8", "-0.6", "-0.4", "-0.2", "0.0", "0.2", "0.4", "0.6", "0.8", "1.0"};
//		String alpha[] = {"-5.0", "-1.0", "0.0", "1.0", "5.0"};
		String alpha[] = {"0.49"};
		
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
			
			WaistDetection.randomizedWaistDetection(dependencyDAG, networkID);
//			WaistDetection.pathCoverageThresholdDetection(dependencyDAG, networkID);
//			new GradientFilterAnalysis().getSampleGradientsQuartileInterval(dependencyDAG, networkID);
//			WaistDetection.runPCWaistDetection(dependencyDAG, networkID);
			System.out.println("#$#$#$#$#$ \n");
		}
	}
	
	public static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG.isWeighted = true;
		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//toy_dag_weighted.txt");
		String netID = "toy_dag";
		printNetworkStat(dependencyDAG);
		dependencyDAG.printNetworkMetrics();
		
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);

//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
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
