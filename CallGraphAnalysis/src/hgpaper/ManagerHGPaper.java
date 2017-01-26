package hgpaper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;

import utilityhg.ConfidenceInterval;
import utilityhg.CourtCaseCornellParser;
import utilityhg.DistributionAnalysis;
import utilityhg.Visualization;
import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlatNetwork;
import corehg.ModelRealConnector;
import corehg.SimpleModelDAG;

public class ManagerHGPaper {	
	public static String nID = "";
	public static String kTopic = "";
	
/*	private static void generateSyntheticFromReal(DependencyDAG dependencyDAG) throws Exception {
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
*/	
	
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
	
/*	public static void checkNewHGSampleNetworks() throws Exception {
		DependencyDAG.isCallgraph = true;
		String swName = "Sqlite";
		String netPath = "sw_callgraphs//full-graph-" + swName;
		String netID = swName;
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		printNetworkStat(dependencyDAG);
//		CoreDetection.randomizedWaistDetection(dependencyDAG, netID);
	}
*/	
	
	private static void getOptimalAlphaForModel(DependencyDAG dependencyDAG, double realHScore) throws Exception {
		int nRun = 10;
		double minMedianHScoreDiff = 10e10;
		double optimalAlpha = -10e10;
		double hScoreDiffArray[] = new double[nRun];
		double hScoreArray[] = new double[nRun];
		System.out.println(nID + "\t" + kTopic + "\t" + realHScore);
		
		for (double a = 0.35; a <= 1.2; a += 0.05) {
			for (int i = 0; i < nRun; ++i) {
				ModelRealConnector modelRealConnector = new ModelRealConnector(dependencyDAG);
				modelRealConnector.generateModelNetwork(dependencyDAG, a);
		
				DependencyDAG.isToy = true;
				String toyDAGName = "real-model-test";
				String netID = "toy_dag";
				DependencyDAG modelDependencyDAG = new DependencyDAG("real_model_networks//" + toyDAGName + ".txt");
//				toyDependencyDAG.printNetworkStat();
//				toyDependencyDAG.printNetworkProperties();

				CoreDetection.fullTraverse = false;
				CoreDetection.getCore(modelDependencyDAG, netID);
				double realCore = CoreDetection.minCoreSize;
		
				FlatNetwork.makeAndProcessFlat(modelDependencyDAG);
				CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
			
				hScoreDiffArray[i] = Math.abs(realHScore - CoreDetection.hScore);
				hScoreArray[i] = CoreDetection.hScore;
//				System.out.println(CoreDetection.hScore);
			}
		
			double medianDiff = StatUtils.percentile(hScoreDiffArray, 50);
			if (medianDiff < minMedianHScoreDiff) {
				minMedianHScoreDiff = medianDiff;
				optimalAlpha = a;
			}
			
			System.out.println(a + "\t" + StatUtils.min(hScoreArray) + "\t" + StatUtils.percentile(hScoreArray, 50) + "\t" 
							   + StatUtils.max(hScoreArray));
		}
		
		System.out.println("Optimal Alpha: " + optimalAlpha + " closest HScoreDiff: " + minMedianHScoreDiff);
	}
	
	private static void doRealNetworkAnalysis() throws Exception {
		String netPath = "";
		
//		String netID = "rat";
//		String netID = "monkey";
		
//		String netID = "commons-math";
//		String netID = "openssh-39";
//		String netID = "apache-commons-3.4";
		
//		String netID = "court";		
//		String netID = "jetuml";
		
		String netID = nID;
		
		DependencyDAG.resetFlags();
		
		if (netID.equals("rat") || netID.equals("monkey")) {
			loadLargestWCC(netID);
		}
		
		if (netID.equals("rat")) {
			netPath = "metabolic_networks//rat-consolidated.txt";
//			netPath = "metabolic_networks//rat-links.txt";
			DependencyDAG.isMetabolic = true;
//			DependencyDAG.isCyclic = true;
		}
		else if (netID.equals("monkey")) {
			netPath = "metabolic_networks//monkey-consolidated.txt";
//			netPath = "metabolic_networks//monkey-links.txt";
			DependencyDAG.isMetabolic = true;
//			DependencyDAG.isCyclic = true;
		}
		else if (netID.equals("court")) {
//			CourtCaseCornellParser.caseTopic = "abortion";
//			CourtCaseCornellParser.caseTopic = "pension";
			CourtCaseCornellParser.caseTopic = kTopic;
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
		else if (netID.equals("jetuml")) {
			netPath = "jdk_class_dependency//jetuml-callgraph.txt";
			DependencyDAG.isClassDependency = true;
		}

		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		
//		generateSyntheticFromReal(dependencyDAG);
		
//		dependencyDAG.printNetworkStat();
//		dependencyDAG.printNetworkProperties();
//		DistributionAnalysis.getLocationColorWeightedHistogram(dependencyDAG);
		
//		Visualization.printDOTNetwork(dependencyDAG);
		
		
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
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
//		WaistDetection.heuristicWaistDetection(dependencyDAG, netID);
//		CoreDetection.pathCoverageThresholdDetection(dependencyDAG, netID);
		
		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(dependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

//		Randomization Experiments
//		UpstreamRandomize.randomizeDAG(dependencyDAG);
//		printNetworkStat(dependencyDAG);
//		netID += "-randomized";
//		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		WaistDetection.randomizedWaistDetection(dependencyDAG, netID);
//		randomHScores[index++] = WaistDetection.hourglassness;

//		Flattening
//		dependencyDAG.init(); // why even here
//		printNetworkStat(dependencyDAG);
		FlatNetwork.makeAndProcessFlat(dependencyDAG);	
		CoreDetection.hScore = (1.0 - (realCore / FlatNetwork.flatNetworkCoreSize));
//		System.out.println("H-Score: " + CoreDetection.hScore);
		
//		Get Real to Model Networks
		getOptimalAlphaForModel(dependencyDAG, CoreDetection.hScore);
//		ModelRealConnector modelRealConnector = new ModelRealConnector(dependencyDAG);
//		modelRealConnector.generateModelNetwork(dependencyDAG, 0.6);
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
	
//	private static void doToyNetworkAnalysis() throws Exception {
//		DependencyDAG.isToy = true;
//		String toyDAGName = "toy_dag_3";
//		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
////		DependencyDAG toyDependencyDAG = new DependencyDAG("synthetic_callgraphs//draw//SimpleModelDAGr-1a3d2.0.txt");
//
//		String netID = "toy_dag";
////		printNetworkStat(toyDependencyDAG);
//		toyDependencyDAG.printNetworkProperties();
//		
//		CoreDetection.fullTraverse = true;
//		CoreDetection.getCore(toyDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;
//		
//		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(toyDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
//	}

	private static void runSyntheticStatisticalSignificanceTestsForTau() throws Exception {
		DependencyDAG.isSynthetic = true;
		DependencyDAG.isSimpleModel = true;
		DependencyDAG.isWeighted = false;
		String DAGType = "SimpleModelDAG";
		
//		String alphas[] = { "-1", "-0.5", "0", "0.5", "1" };
		String alphas[] = { "1" };
		String dins[] = {"3"};
			
		for (String din : dins) {
			for (String a : alphas) {
				System.out.println("alpha=" + a + "\t" + "din=" + din );
				int nT = 100;
				int nI = 300;
				int nS = 100;
				String ratio = "-1";
				String networkID = DAGType + "r" + ratio + "a" + a + "d" + din;
				
				int nRun = 50;
				int minTau = 50;
				int maxTau = 98;
				int tauRange = maxTau - minTau + 1;
				double hScores[][] = new double[tauRange][nRun];
				
				for (int i = 0; i < nRun; ++i) {
					SimpleModelDAG.generateSimpleModel(Double.parseDouble(a), Integer.parseInt(din), nT, nI, nS, Double.parseDouble(ratio));
					SimpleModelDAG.initModelProperties(nT, nI, nS, Integer.parseInt(din));
					System.out.println("Model Generated for run " + i );
				
					
					for (int j = minTau; j <= maxTau; j += 2) {
						CoreDetection.pathCoverageTau = j / 100.0;
//						DependencyDAG.resetFlags();

						DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//						printNetworkStat(dependencyDAG);
//						dependencyDAG.printNetworkProperties();

						CoreDetection.getCore(dependencyDAG, networkID);
						double realCore = CoreDetection.minCoreSize;
											
						FlatNetwork.makeAndProcessFlat(dependencyDAG);	
						CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
						if (CoreDetection.hScore < 0) {
//							System.out.println("Found");
//							dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//							TreeSet<String> sampleFlatCore = new TreeSet(CoreDetection.coreSet.keySet().iterator().next());
//							System.out.println(CoreDetection.verifyCore(dependencyDAG, sampleFlatCore));
							CoreDetection.hScore = 0;
						}
//						System.out.println("[h-Score] " + CoreDetection.hScore);
						hScores[j - minTau][i] = CoreDetection.hScore;					
					}
				}
				
				for (int j = minTau; j <= maxTau; j += 2) {	
					double mHS = StatUtils.mean(hScores[j - minTau]);
					double ciHS = ConfidenceInterval.getConfidenceInterval(hScores[j - minTau]);
					System.out.println((j / 100.0) + " " + mHS + " " + ciHS);
				}
				System.out.println();
			}
		}
	}
	
	private static void runSyntheticStatisticalSignificanceTests(int nT, int nI, int nS, int din) throws Exception {
		DependencyDAG.isSynthetic = true;
		DependencyDAG.isWeighted = false;
		DependencyDAG.isSimpleModel = true;
		String DAGType = "SimpleModelDAG";

//		String alphas[] = { "-2", "-1.8", "-1.6", "-1.4", "-1.2",
//				            "-1", "-0.8", "-0.6", "-0.4", "-0.2", 
//				            "0", 
//				            "0.2", "0.4", "0.6", "0.8", "1", 
//				            "1.2", "1.4", "1.6", "1.8", "2"};
//		String alphas[] = {"-1", "0", "1"};
		String alphas[] = {"2"};

//		String dins[] = { "1", "2", "3", "4", "5" };
//		String dins[] = {"1"};
//		for (String din : dins) {
		
			for (String a : alphas) {
//				System.out.println("alpha=" + a + "\t" + "din=" + din );
//				int nT = 333;
//				int nI = 333;
//				int nS = 333;
				String ratio = "-1";
				String networkID = DAGType + "r" + ratio + "a" + a + "d" + din;
				
				int nRun = 100;
				double coreSizes[] = new double[nRun];
				double hScores[] = new double[nRun];
				double nodeCoverages[] = new double[nRun];
				double weightedCoreLocation[] = new double[nRun];
				double hScoreDenominaotors[] = new double[nRun];
				ArrayList<Double> coreLocations = new ArrayList();
				
				int idx = 0;
				for (int i = 0; i < nRun; ++i) {
					SimpleModelDAG.generateSimpleModel(Double.parseDouble(a), din, nT, nI, nS, Double.parseDouble(ratio));
					SimpleModelDAG.initModelProperties(nT, nI, nS, din);
//					System.out.println("Model Generated");
					
//					DependencyDAG.resetFlags();
					DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//					printNetworkStat(dependencyDAG);
//					dependencyDAG.printNetworkProperties();

//					WaistDetection.pathCoverageThresholdDetection(dependencyDAG, networkID);
					CoreDetection.getCore(dependencyDAG, networkID);
					double realCore = CoreDetection.minCoreSize;
					
					coreSizes[idx] = CoreDetection.minCoreSize;
					nodeCoverages[idx] = CoreDetection.nodeCoverage;
					weightedCoreLocation[idx] = CoreDetection.weightedCoreLocation;
						
//					System.out.println(CoreDetection.nodeCoverage + "\t" + CoreDetection.weightedCoreLocation);
					FlatNetwork.makeAndProcessFlat(dependencyDAG);	
					CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
					if (CoreDetection.hScore < 0) {
//						System.out.println("Found");
//						dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//						TreeSet<String> sampleFlatCore = new TreeSet(CoreDetection.coreSet.keySet().iterator().next());
//						System.out.println(CoreDetection.verifyCore(dependencyDAG, sampleFlatCore));
						CoreDetection.hScore = 0;
					}
//					System.out.println("[h-Score] " + CoreDetection.hScore);					
					hScores[idx] = CoreDetection.hScore;
					
//					System.out.println(idx);
					++idx;					
				}
				
				double mWS = StatUtils.mean(coreSizes);
				double mNC = StatUtils.mean(nodeCoverages);
				double mHS = StatUtils.mean(hScores);
				double mWCL = StatUtils.mean(weightedCoreLocation);
				double ciWS = ConfidenceInterval.getConfidenceInterval(coreSizes);
				double ciNC = ConfidenceInterval.getConfidenceInterval(nodeCoverages);
				double ciHS = ConfidenceInterval.getConfidenceInterval(hScores);
				double ciWCL = ConfidenceInterval.getConfidenceInterval(weightedCoreLocation);
				System.out.println(a + " " + din + " " + ratio + " " + mWS + " " + ciWS + " " 
				+ mNC + " " + ciNC + " " + mHS + " " + ciHS + " " + mWCL + " " + ciWCL);
			}
			System.out.println();
//		}
	}
	
//	static double randomHScores[] = new double[100];
//	static int index = 0;
/*	private static void randomizationTests() throws Exception {
	
		String data[] = {"openssh-39", "commons-math", "rat", "monkey", "court-abortion", "court-pension"};
		PrintWriter pw = new PrintWriter(new File("analysis//random-hscores-" + data[5] + ".txt"));
		for (int i = 0; i < 100; ++i) {
			DependencyDAG.isRandomized = false;
			doRealNetworkAnalysis();
			System.out.println(i + " done!\n");
		}
		
		for (int i = 0; i < 100; ++i) {
			pw.println(randomHScores[i]);
		}
		pw.close();
		
		double z = (StatUtils.mean(randomHScores) - 0.8544) * 10.0 / Math.sqrt(StatUtils.variance(randomHScores));
		System.out.println("Z Score: " + z);
		System.out.println(TestUtils.t(0.8544, randomHScores));
		System.out.println(TestUtils.tTest(0.8544, randomHScores)/2);
	}
	*/
	
	private static void measureTauEffectOnRealNetwork() throws Exception {
//		String data[] = {"openssh-39", "commons-math", "rat", "monkey", "court-abortion", "court-pension"};
//		PrintWriter pw = new PrintWriter(new File("analysis//hscore-vs-tau-" + data[5] + ".txt"));
		for (int i = 50; i <= 98; i += 2) {
			CoreDetection.pathCoverageTau = i / 100.0;
			DependencyDAG.resetFlags();
			doRealNetworkAnalysis();
//			System.out.println(CoreDetection.hScore);
			System.out.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
//			pw.println(CoreDetection.pathCoverageTau + "\t" + CoreDetection.hScore);
		}
//		pw.close();
	}
	
/*	private static void randomizationTestsBinned() throws Exception {
		String data[] = {"openssh-39", "commons-math", "rat", "monkey", "court-abortion", "court-pension"};
		
		for (int i = 0; i < 6; ++i) {
			PrintWriter pw = new PrintWriter(new File("analysis//random-hscores-binned-" + data[5] + ".txt"));
			Scanner scanner = new Scanner(new File("analysis//random-hscores-" + data[5] + ".txt"));
			
			int bins[] = new int[10];
			while (scanner.hasNext()) {
				double d = scanner.nextDouble();
				int b = (int)(d * 10);
			}
		}
	}
*/	
	
	private static void runThroughAllRealNets() throws Exception {
		String data[] = {"rat", "monkey", "court", "court", "openssh-39", "commons-math"};
		for (int i = 0; i < 6; ++i) {
			nID = data[i];
			if (i == 2) {
				kTopic = "abortion";
			}
			else if (i == 3) {
				kTopic = "pension";
			}
			else {
				kTopic = "";
			}
			doRealNetworkAnalysis();
		}
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerHGPaper.doRealNetworkAnalysis();
//		Manager.doToyNetworkAnalysis();
//		Manager.measureTauEffectOnRealNetwork();
		ManagerHGPaper.runThroughAllRealNets();
		
//		curve 1
//		Manager.runSyntheticStatisticalSignificanceTests(333, 333, 333, 1);
				
//		curve 2
//		SimpleModelDAG.isPoisson = false;
//		Manager.runSyntheticStatisticalSignificanceTests(333, 333, 333, 2);

//		curve 3
//		Manager.runSyntheticStatisticalSignificanceTests(333, 333, 333, 3);
		
//		curve 4
//		Manager.runSyntheticStatisticalSignificanceTests(250, 250, 500, 1);
		
//		curve 5
//		Manager.runSyntheticStatisticalSignificanceTests(500, 250, 250, 1);

//		curve 6
//		Manager.runSyntheticStatisticalSignificanceTests(250, 500, 250, 1);

//		curve 7
//		Manager.runSyntheticStatisticalSignificanceTests(400, 200, 400, 1);

//		curve Toy
//		Manager.runSyntheticStatisticalSignificanceTests(66, 66, 66, 1);
//		SimpleModelDAG.isPoisson = false;
//		Manager.runSyntheticStatisticalSignificanceTests(66, 66, 66, 2);

		System.out.println("Done!");
	}
}
