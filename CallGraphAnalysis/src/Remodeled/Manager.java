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
		
//		String netID = "commons-math";
//		String netID = "openssh-39";
//		String netID = "apache-commons-3.4";
		
		String netID = "court";
		
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
//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogramSpecialized(dependencyDAG);
//		DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, netID);		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		int centralityIndex = 1;
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);
		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.printAllCentralities(dependencyDAG, netID);
//		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, netID);
//		DistributionAnalysis.createSubnetwork(dependencyDAG, netID);
		
//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
		
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
		
//		String alphas[] = {"-1.0", "-0.8", "-0.6", "-0.4", "-0.2", "0.0", "0.2", "0.4", "0.6", "0.8", "1.0"};
//		String alphas[] = {"-5.0", "-1.0", "0.0", "1.0", "5.0"};
		String alphas[] = {"10.0"};
		
		String dins[] = {"1", "2", "3", "5", "7"};
//		String dins[] = {"5", "7"};
		
		String ratios[] = {"0.02", "0.08", "0.15", "0.22", "0.28", "0.35", "0.42", "0.48"};
		int startIs[] = {10, 50, 90, 130, 170, 210, 250, 290};
		int startSs[] = {590, 550, 510, 470, 430, 390, 350, 310};
		
//		String ratios[] = {"0.95", "0.75", "0.55", "0.35", "0.15"};
//		int startIs[] = {10, 50, 90, 130, 170};
//		int startSs[] = {410, 450, 490, 530, 570};
		
//		String ratios[] = {"0.15"};
		
//		String a = "0.6";
//		String din = "3";
		String ratio = "-";
		int index = 0;
		
		
		for (String a : alphas) {
			for (String din : dins) {
//				for (String ratio: ratios) {
//					SimpleModelDAG.sI = startIs[index];
//					SimpleModelDAG.sS = startSs[index];
//					++index;
					
//					String networkID = DAGType + "r" + ratio + "d" + din + "a" + a;
					String networkID = DAGType + "d" + din + "a" + a;
//					System.out.println("Working on: " + networkID);
					DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//					printNetworkStat(dependencyDAG);
//					dependencyDAG.printNetworkMetrics();

					// DistributionAnalysis.printSyntheticPC(dependencyDAG, networkID);
					// DistributionAnalysis.targetEdgeConcentration(dependencyDAG);
					// DistributionAnalysis.getAveragePathLenth(dependencyDAG);
					// DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
					// DistributionAnalysis.getReachabilityCount(dependencyDAG);
					// DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, networkID);
					double medianPathLength = DistributionAnalysis.getPathLength(dependencyDAG);

					WaistDetection.randomizedWaistDetection(dependencyDAG, networkID);
					// WaistDetection.pathCoverageThresholdDetection(dependencyDAG, networkID);
					// new GradientFilterAnalysis().getSampleGradientsQuartileInterval(dependencyDAG, networkID);
					// WaistDetection.runPCWaistDetection(dependencyDAG, networkID);

					System.out.println(a + "\t" + din + "\t" + WaistDetection.waistSize + "\t" + medianPathLength);
//					System.out.print(din + " " + a + " " + ratio + " " + WaistDetection.waistSize);
//					System.out.print(" " + WaistDetection.nodeCoverage + " " + WaistDetection.hourglassness);
//					System.out.println();
//				}
//				System.out.println();
			}
			System.out.println();
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
//		Manager.doRealNetworkAnalysis();
		Manager.doSyntheticNetworkAnalysis();
//		Manager.doToyNetworkAnalysis();
		System.out.println("Done!");
	}
}
