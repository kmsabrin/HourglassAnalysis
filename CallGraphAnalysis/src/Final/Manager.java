package Final;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;

import Final.ConfidenceInterval;

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
	
	public static void checkNewHGSampleNetworks() throws Exception {
		DependencyDAG.isCallgraph = true;
		String swName = "Sqlite";
		String netPath = "sw_callgraphs//full-graph-" + swName;
		String netID = swName;
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		printNetworkStat(dependencyDAG);
		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
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
			CourtCaseCornellParser.caseTopic = "abortion";
//			CourtCaseCornellParser.caseTopic = "pension";
			CourtCaseCornellParser.loadCuratedCaseIDs();
			netPath = "supremecourt_networks//court.txt";
			DependencyDAG.isCourtcase = true;
		}
		else if (netID.equals("openssh-39")) {
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

		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		
//		generateSyntheticFromReal(dependencyDAG);
		
//		printNetworkStat(dependencyDAG);
		dependencyDAG.printNetworkProperties();
		
		
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
//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
//		WaistDetection.pathCoverageThresholdDetection(dependencyDAG, netID);

//		Randomization Experiments
		UpstreamRandomize.randomizeDAG(dependencyDAG);
//		printNetworkStat(dependencyDAG);
		netID += "-randomized";
		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
	}
	
	/*
	public static void doSyntheticNetworkAnalysis() throws Exception {
		DependencyDAG.isSynthetic = true;
		DependencyDAG.isWeighted = true;
		
//		String DAGType = "ComplexModelDAG";
//		DependencyDAG.isComplexModel = true;
//		ComplexModelDAG.loadLayerIndex();
		
		String DAGType = "SimpleModelDAG";
		DependencyDAG.isSimpleModel = true;
		
//		String alphas[] = {"-1.0", "-0.5", "0.0", "0.5", "1.0"};
//		String alphas[] = {"-1.0", "-0.8", "-0.6", "-0.4", "-0.2", "0.0", "0.2", "0.4", "0.6", "0.8", "1.0"};
//		String alphas[] = {"-0.5"};
//		String alphas[] = {"-0.5", "0.0", "0.5"};
		
//		String dins[] = {"1", "2", "3", "5", "7"};
		String dins[] = {"2"};
		
//		String ratios[] = {"0.02", "0.08", "0.15", "0.22", "0.28", "0.35", "0.42", "0.48"};
//		int startIs[] = {10, 50, 90, 130, 170, 210, 250, 290};
//		int startSs[] = {590, 550, 510, 470, 430, 390, 350, 310};
		
//		String ratios[] = {"0.95", "0.75", "0.55", "0.35", "0.15", "0.05"};
//		int startIs[] = {10, 50, 90, 130, 170, 190};
//		int startSs[] = {410, 450, 490, 530, 570, 590};
								
//		String real[] = {"openssh", "javamath", "rat", "monkey", "abortion", "pension"};
//		int startIs[] = {172, 0, 131, 0, 0, 323};
//		int startSs[] = {932, 0, 433, 0, 0, 453};
//		0.53,-,0.56,-,-,1.01

		String a = "1.08";
		String din = "1.255"; //4.2223. 1.1418, 1.255
		String ratio = "-1";
//		String ratios[] = {"0.15"};
		
//		for (String a : alphas) {
//			for (String din : dins) {		
//				int index = 2;
		
//				int nT = 172; int nI = 760; int nS = 435; // openssh
//				int nT = 131; int nI = 302; int nS = 103; // rat
				int nT = 323; int nI = 130; int nS = 837; // pension
				SimpleModelDAG.isMultigraph = false;
				SimpleModelDAG.generateSimpleModel(Double.parseDouble(a), Double.parseDouble(din), nT, nI, nS, Double.parseDouble(ratio));

//				for (String ratio: ratios) {
//					SimpleModelDAG.sI = startIs[index];
//					SimpleModelDAG.sS = startSs[index];
//					++index;					
					String networkID = DAGType + "r" + ratio + "a" + a + "d" + din;
					
//					String networkID = DAGType + "d" + din + "a" + a;
//				    String networkID = DAGType + "-" + real[index];
					
					System.out.println("Working on: " + networkID);
					DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
					printNetworkStat(dependencyDAG);
//					dependencyDAG.printNetworkMetrics();

					DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
//					double medianPathLength = DistributionAnalysis.getPathLength(dependencyDAG);
//					DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, networkID);

					WaistDetection.randomizedWaistDetection(dependencyDAG, networkID);
//					WaistDetection.pathCoverageThresholdDetection(dependencyDAG, networkID);

//					System.out.println("Waist size: " + WaistDetection.waistSize);
//					System.out.println(a + "\t" + din + "\t" + WaistDetection.waistSize + "\t" + medianPathLength);
//					System.out.print(din + " " + a + " " + ratio + " " + WaistDetection.waistSize);
//					System.out.print(" " + WaistDetection.nodeCoverage + " " + WaistDetection.hourglassness);
//					System.out.println();
					
//				}
//				System.out.println();
//			}
//			System.out.println();
//		}
	}
	*/
	
	public static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
//		DependencyDAG.isWeighted = true;
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//toy_dag.txt");
		String netID = "toy_dag";
		printNetworkStat(toyDependencyDAG);
		toyDependencyDAG.printNetworkProperties();
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);

//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);		
//		DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, netID);

//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);

//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(toyDependencyDAG, netID);
//		System.out.println("\n###\n");
		
//		MaxFlowReduction.reduceToMaxFlowMinCutNetwork(dependencyDAG, netID);
		
		UpstreamRandomize.randomizeDAG(toyDependencyDAG);
//		printNetworkStat(toyDependencyDAG);
		toyDependencyDAG.printNetworkProperties();
//		WaistDetection.randomizedWaistDetection(toyDependencyDAG, netID);
	}
	
	public static void runSyntheticStatisticalSignificanceTests() throws Exception {
		DependencyDAG.isSynthetic = true;
		DependencyDAG.isWeighted = false;
		String DAGType = "SimpleModelDAG";
		DependencyDAG.isSimpleModel = true;
		
//		String alphas[] = {"-1", "-0.5", "0", "0.5", "1"};
		String alphas[] = {"-1", "-0.8", "-0.6", "-0.4", "-0.2", "0", "0.2", "0.4", "0.6", "0.8", "1"};
//		String alphas[] = {"0", "0.2", "0.4", "0.6", "0.8", "1"};
//		String alphas[] = {"0"};
//		String alphas[] = {"-0.5", "0", "0.5"};
		
		String dins[] = {"2", "3"};
//		String dins[] = {"7"};
		
//		String ratios[] = {"0.02", "0.08", "0.15", "0.22", "0.28", "0.35", "0.42", "0.48"};
//		int startIs[] = {10, 50, 90, 130, 170, 210, 250, 290};
//		int startSs[] = {590, 550, 510, 470, 430, 390, 350, 310};
		
//		String ratios[] = {"0.95", "0.75", "0.55", "0.35", "0.15", "0.05"};
//		int startIs[] = {10, 50, 90, 130, 170, 190};
//		int startSs[] = {410, 450, 490, 530, 570, 590};
								
//		String real[] = {"openssh", "javamath", "rat", "monkey", "abortion", "pension"};
//		int startIs[] = {172, 0, 131, 0, 0, 323};
//		int startSs[] = {932, 0, 433, 0, 0, 453};
//		0.53,-,0.56,-,-,1.01

//		String a = "0.0";
//		String din = "2";
//		String ratio = "-";
//		String ratios[] = {"0.15"};
		
		for (String din : dins) {
			for (String a : alphas) {
//				int  index = 0;
//				for (String ratio: ratios) {	
//					SimpleModelDAG.sI = startIs[index];
//					SimpleModelDAG.sS = startSs[index];
											
					int nT = 200; int nI = 600; int nS = 200; String ratio = "-1";
					
//					int nT = startIs[index]; int nS = startIs[index]; int nI = 600 - nT - nS;
//					int nT = startIs[index]; int nI = startSs[index] - nT; int nS = 600 - nT - nI;
//					++index;
					
					String networkID = DAGType + "r" + ratio + "a" + a + "d" + din;
//				    String networkID = DAGType + "-" + real[index];
					
//					System.out.println("Working on: " + networkID);
					int nRun = 10;
					double waistSizes[] = new double[nRun];
					double hScores[] = new double[nRun];
					double nodeCoverages[] = new double[nRun];
					int idx = 0;
					for (int i = 0; i < nRun; ++i) {
						SimpleModelDAG.generateSimpleModel(Double.parseDouble(a), Integer.parseInt(din), nT, nI, nS, Double.parseDouble(ratio));
					
						DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//						printNetworkStat(dependencyDAG);
//						dependencyDAG.printNetworkMetrics();

//						DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
//						double medianPathLength = DistributionAnalysis.getPathLength(dependencyDAG);
//						DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, networkID);

						WaistDetection.randomizedWaistDetection(dependencyDAG, networkID);
//						WaistDetection.pathCoverageThresholdDetection(dependencyDAG, networkID);
						
						if (WaistDetection.waistSize > 0) {
							waistSizes[idx] = WaistDetection.waistSize;
							nodeCoverages[idx] = WaistDetection.nodeCoverage;
							hScores[idx] = WaistDetection.hourglassness;
							++idx;
						}
					}
					
					if (idx > nRun / 2) {
						double mWS = StatUtils.mean(waistSizes);
						double mNC = StatUtils.mean(nodeCoverages);
						double mHS = StatUtils.mean(hScores);
						double ciWS = ConfidenceInterval.getConfidenceInterval(waistSizes);
						double ciNC = ConfidenceInterval.getConfidenceInterval(nodeCoverages);
						double ciHS = ConfidenceInterval.getConfidenceInterval(hScores);
						System.out.println(a + " " + din + " " + ratio + " " + mWS + " " + ciWS + " " + mNC + " " + ciNC + " " + mHS + " " + ciHS );
					}
					else {
						System.out.println(a + " " + din + " " + ratio + " - - - - - -");
					}
//				}
//				System.out.println();
			}
			System.out.println();
		}		
	}

	public static void main(String[] args) throws Exception {		
		
		for (int i = 0; i < 1; ++i) {
			DependencyDAG.isRandomized = false;
//			Manager.doRealNetworkAnalysis();
			System.out.println("\n");
		}
		
//		Manager.doSyntheticNetworkAnalysis();
//		Manager.runSyntheticStatisticalSignificanceTests();
		Manager.doToyNetworkAnalysis();
//		Manager.checkNewHGSampleNetworks();
		System.out.println("Done!");
	}
}
