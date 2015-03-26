import java.io.File;
import java.io.PrintWriter;

public class Driver {	
	static String networkPath = "kernel_callgraphs//full.graph-2.6.";
	static String networkType = "kernel";
	static int versiontStart = 0;
	static int versionEnd = 40; // last + 1
	
//	static String networkPath = "openssh_callgraphs//full.graph-openssh-";
//	static String networkType = "ssh";
//	static int versiontStart = 1;
//	static int versionEnd = 40;
	
	static String currentVersion = "39";
	
	public static void doCoreAnalysis(CallDAG callDAG, CallDAG takeApartCallDAG, String v) throws Exception {
//		CentralityAnalysis centralityAnalysis = new CentralityAnalysis();		
//		centralityAnalysis.getCentralityCDF(callDAG, v);
//		centralityAnalysis.getCentralityCCDF(callDAG, v);
//		System.out.println("Centrality CDF/CCDF Done.");
		
//		centralityAnalysis.doUniformPathSampling = false;
//		centralityAnalysis.getSampledPathStatistics(callDAG, v);
//		System.out.println("Sampled Path HScore Distribution Done.");
		
//		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG, v);
//		System.out.println("Max Centrality Decomposition Curve Done.");	
	}
	
	public static void doKernelAnalysis() throws Exception {
		String versions[] = {"1", "11", "21", "31"};
		for (int i = 0; i < versions.length; ++i) {
			String v = networkType + "-" + versions[i];	
			CallDAG callDAG = new CallDAG(networkPath + versions[i]);
			CallDAG takeApartCallDAG = new CallDAG(networkPath + versions[i]);
			System.out.println("Loading " + v + " Done.");
	
			doCoreAnalysis(callDAG, takeApartCallDAG, v);
		}	
	}
	
	public static void doBioNetAnalysis() throws Exception {
		String versions[] = {"rat"};
		for (int i = 0; i < versions.length; ++i) {
			String v = versions[i];	
			CallDAG callDAG = new CallDAG("biological_networks//" + v + ".txt");
			System.out.println("nFunctions: " + callDAG.functions.size());
			System.out.println("nEdges: " + callDAG.nEdges);
			System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
			CallDAG takeApartCallDAG = new CallDAG("biological_networks//" + v + ".txt");
			System.out.println("Loading " + v + " Done.");
			doCoreAnalysis(callDAG, takeApartCallDAG, v);
		}	
	}
	
	public static void doONodeAnalysis() throws Exception {
		String versions[] = {"kernel-11-O-Graph", "kernel-21-O-Graph", "kernel-31-O-Graph"};
		for (int i = 0; i < versions.length; ++i) {
			String v = versions[i];	
			CallDAG callDAG = new CallDAG("Results//" + v + ".txt");
			CallDAG takeApartCallDAG = new CallDAG("Results//" + v + ".txt");
			System.out.println("Loading " + v + " Done.");
			doCoreAnalysis(callDAG, takeApartCallDAG, v);
		}	
	}
	
	private static void printNetworkStat(CallDAG callDAG) {
		System.out.println("R: " + callDAG.nRoots);
		System.out.println("L: " + callDAG.nLeaves);
		System.out.println("E: " + callDAG.nEdges);
		System.out.println("N: " + callDAG.functions.size());
	}
	
	public static void doArtificialNetworkAnalysis() throws Exception {
//		new ArtificialDAG().generateRectangleDAG();
		new ArtificialDAG().generateHourglassDAG();
//		new ArtificialDAG().generateTrapezoidsDAG();
//		new ArtificialDAG().generateNoisyRectangleDAG();
//		new ArtificialDAG().generateDiamondDAG();

		String versions[] = {"rectangleDAG", 
							 "noisyRectangleDAG",
							 "hourglassDAG", 
							 "trapezoidDAG",
							 "diamondDAG",
		};
		
//		String versions[] = {"hourglassDAG"/*, "randomShuffle-hourglassDAG-1.0"*/};
		
		for (int i = 0; i < versions.length; ++i) {
//			if (i == 2) continue;			
			String versionNumber = versions[i];
			CallDAG callDAG = new CallDAG("artificial_callgraphs//" + versionNumber + ".txt");
			CentralityAnalysis.getCentralityPDF(callDAG, versionNumber);
			printNetworkStat(callDAG);

//			String r1VersionNumber = "randomShuffle-" + versionNumber + "-1.0";
//			CallDAG r1callDAG = new CallDAG("artificial_callgraphs//" + r1VersionNumber + ".txt");
//			CentralityAnalysis.getCentralityPDF(r1callDAG, r1VersionNumber);
//			printNetworkStat(r1callDAG);
//			
//			String r2VersionNumber = versionNumber + "rX";
//			CallDAG r2CallDAG = new CallDAG("artificial_callgraphs//" + r2VersionNumber + ".txt");	
//			CentralityAnalysis.getCentralityPDF(r2CallDAG, r2VersionNumber);
//			printNetworkStat(r2CallDAG);
			
//			RandomNetworkGenerator randomNetworkGenerator = new RandomNetworkGenerator(callDAG);
//			randomNetworkGenerator.generateRandomNetwork(r2VersionNumber);
			
			String randomVersionNumber = versionNumber + "rX";
			ConfigurationRandomNetwork configurationRandomNetwork = new ConfigurationRandomNetwork();
//			callDAG.resetAuxiliary();
			configurationRandomNetwork.init(callDAG);
//			configurationRandomNetwork.generateNumEdgePreserve_3();
			configurationRandomNetwork.generateKNRandomDAG(callDAG);
			configurationRandomNetwork.writeRandomDAG(callDAG, randomVersionNumber);
			
			CallDAG randomDAG = new CallDAG("artificial_callgraphs//"+ randomVersionNumber +".txt");
			printNetworkStat(randomDAG);
			CentralityAnalysis.getCentralityPDF(randomDAG, randomVersionNumber);			
			
//			CallDAG randomDAG = new CallDAG("artificial_callgraphs//methodX.txt");
//			CentralityAnalysis.getCentralityPDF(randomDAG, "methodX");
//			
//			randomDAG = new CallDAG("artificial_callgraphs//methodY.txt");
//			CentralityAnalysis.getCentralityPDF(randomDAG, "methodY");
		}
	}

	public static void main(String[] args) throws Exception {		
/*****************************************************************************/
//		String versionNum = networkType + "-" + currentVersion;
//		CallDAG callDAG = new CallDAG(Driver.networkPath + currentVersion);
//		CallDAG takeApartCallDAG = new CallDAG(Driver.networkPath + currentVersion);
//		System.out.println("nFunctions: " + callDAG.functions.size());
//		System.out.println("nEdges: " + callDAG.nEdges);
//		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
/*****************************************************************************/

/*****************************************************************************/
		Driver.doArtificialNetworkAnalysis();
//		Driver.doKernelAnalysis();
//		Driver.doBioNetAnalysis();
//		Driver.doONodeAnalysis();
//		StatMathUtilTest.getToyGraph();
/*****************************************************************************/

/*****************************************************************************/
//		CycleAnalysis cycleAnalysis = new CycleAnalysis();
//		cycleAnalysis.analyzeCycle(callDAG);
/*****************************************************************************/
		
/*****************************************************************************/
//		PatchAnalysis patchAnalysis = new PatchAnalysis();
//		patchAnalysis.getPatchedFunctions("kernel_patches//patch-2.6.9.txt", callDAG.functions);
/*****************************************************************************/

/*****************************************************************************/
//		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG, versionNum);
//		coreAnalysis.getCentralityVsCutProperty(callDAG, versionNum);	
/*****************************************************************************/
		
/*****************************************************************************/
//		DegreeAnalysis degreeAnalysis = new DegreeAnalysis();		
//		degreeAnalysis.getInDegreeCCDF(callDAG, versionNum);
//		degreeAnalysis.getOutDegreeCCDF(callDAG, versionNum);
//		degreeAnalysis.getLocationVSAvgInDegree(callDAG, versionNum);
//		degreeAnalysis.getLocationVSAvgOutDegree(callDAG, versionNum);
//		degreeAnalysis.getIndegreeVsOutDegree(callDAG, versionNum);
//		degreeAnalysis.getIndegreeVsOutDegreeCorrelationCoefficient(callDAG);
//		degreeAnalysis.getSpearmanCoefficientForAllVersions();
//
//		degreeAnalysis.getInDegreeDistribution(callDAG, versionNum);
//		degreeAnalysis.getOutDegreeDistribution(callDAG, versionNum);
/*****************************************************************************/
		
/*****************************************************************************/
//		LocationAnalysis locationAnalysis = new LocationAnalysis();
//		locationAnalysis.getLocationHistogram(callDAG, versionNum);
//		locationAnalysis.getLocationVsCallDirection(callDAG);
//		locationAnalysis.getClusterLocationDistribution();
//		locationAnalysis.getLeafCallerLocationHistogram(callDAG);
//		locationAnalysis.getLocationHistogramForEachVersion();
//		locationAnalysis.getLeafAnomalyForMcount(); // CUSTOMIZED FOR DIFFERENT NETWORK
//		locationAnalysis.getWineGlassGroupsGrowth(); // CUSTOMIZED FOR DIFFERENT NETWORK
//		locationAnalysis.getCallViolationMetric(callDAG);
/*****************************************************************************/

/*****************************************************************************/	
//		AgeAnalysis ageAnalysis = new AgeAnalysis();
//		ageAnalysis.getClusterLifeTimeDistribution(); // CHANGE FOR DIFFERENT NETWORKS
//		ageAnalysis.getLocationVsPersistencePercentiles();
//		ageAnalysis.getLocationVsTransientStable(); // CHANGE FOR DIFFERENT NETWORKS
//		ageAnalysis.getlocationDispersion();
//		ageAnalysis.getLocationVsAvgGeneralityDelta();
//		
//		ageAnalysis.getLastLocationVSAverageAge();
//		ageAnalysis.getAgeHistogram();
//		ageAnalysis.getLastLocationVSDeathPercentage();
//		ageAnalysis.getAgeVSDeathPercentage();
//		ageAnalysis.getLocationModeVSAge(); /* ?!? */
//		ageAnalysis.getAgeVSLastLocation();
//		ageAnalysis.getLastLocationVSAlivePercentage();
//		ageAnalysis.getClusterAgeDistribution();
/*****************************************************************************/

/*****************************************************************************/	
//		PersistenceAnalysis persistenceAnalysis = new PersistenceAnalysis();		
//		CallDAG callDAGFrom = new CallDAG("kernel_callgraphs//full.graph-2.6.26");
//		CallDAG callDAGTo = new CallDAG("kernel_callgraphs//full.graph-2.6.27");
//		persistenceAnalysis.getContiguousFunctionPersistance(callDAGFrom, callDAGTo);
/*****************************************************************************/

/*****************************************************************************/
//		EvolutionAnalysis evolutionaryAnalysis = new EvolutionAnalysis();
//		evolutionaryAnalysis.getAverageGeneralityPerLocationForEachVersion();
//		evolutionaryAnalysis.getGenCmpScatterForAllVersions();
//		evolutionaryAnalysis.getViolationMetricForEachVersion();
//		evolutionaryAnalysis.getLocationHistogramForEachVersion();
//		evolutionaryAnalysis.getEvolutionaryDeathBirthTrend(); 
//		evolutionaryAnalysis.getLocationVsSizeTrend();// CUSTOMIZED FOR DIFFERENT NETWORK
//		evolutionaryAnalysis.getNetworkGrowthTrend();
//		evolutionaryAnalysis.getNumClustersForEachVersion();
//		evolutionaryAnalysis.getClusterSizeTrend();
		
//		evolutionaryAnalysis.compareConsecutiveVersionFunctionsNeighborhood("0", "1");
//		evolutionaryAnalysis.compareConsecutiveVersionModules("30", "31");
//		evolutionaryAnalysis.getWalktrapCallDAGForEachVersion();
//		evolutionaryAnalysis.getCommunityEvolutionData();
/*****************************************************************************/

/*****************************************************************************/	
//		CentralityAnalysis centralityAnalysis = new CentralityAnalysis();
//		centralityAnalysis.getCentralityCDF(callDAG, versionNum);
//		centralityAnalysis.getLocationVsCentrality(callDAG, versionNum);
//		centralityAnalysis.getKernelBoundaryCentralityCDF(callDAG, versionNum);
//		centralityAnalysis.getSamplePathStatistics(callDAG, versionNum);
//		centralityAnalysis.getSubtreeSizeCDF(callDAG, versionNum);
//		centralityAnalysis.test(callDAG);
/*****************************************************************************/
				
/*****************************************************************************/	
//		GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();
//		generalityAnalysis.getGeneralityHistogram(callDAG, versionNum);
//		generalityAnalysis.getComplexityHistogram(callDAG, versionNum);
//		generalityAnalysis.getLocationVsAvgGenerality(callDAG, versionNum);
//		generalityAnalysis.getLocationVsAvgComplexity(callDAG, versionNum);
//		generalityAnalysis.getGeneralityVsComplexity(callDAG, versionNum);
//		generalityAnalysis.getCentralNodes(callDAG); // CUSTOMIZED FOR DIFFERENT NETWORK
/*****************************************************************************/

/*****************************************************************************/		
//		DiameterAnalysis diameterAnalysis = new DiameterAnalysis();
//		diameterAnalysis.getEffectiveDiameter(callDAG);
//		diameterAnalysis.getEffectiveDiameterForAllVersions();
/*****************************************************************************/

/*****************************************************************************/		
//		ModularityAnalysis modularityAnalysis  = new ModularityAnalysis();
//		modularityAnalysis.getModuleGeneralityVsComplexity(callDAG, versionNum);
//		modularityAnalysis.getInfo();
//		modularityAnalysis.getAvgModuleGeneralityVsLocation(callDAG, versionNum);
		
//		modularityAnalysis.getArtificialModularNetwork();		
//		modularityAnalysis.getArtificialModularNetworkCommunityDetectionPerformance();

//		modularityAnalysis.getWalktrapModules(callDAG, versionNum);
//		modularityAnalysis.getCallDAGforWalktrap(callDAG, versionNum);
		
//		modularityAnalysis.getCommunityNetworkLayoutDOTStyle(callDAG);
//		modularityAnalysis.getCommunityLocationHistogram(callDAG, versionNum);
		
//		modularityAnalysis.getCommunityAnalysisJavaDraw(callDAG, versionNum);
/*****************************************************************************/

/*****************************************************************************/		
//		ClusterAnalysis clusterAnalysis = new ClusterAnalysis();
//		ClusterAnalysis.demonstrateClustersForVersionX();
//
//		clusterAnalysis.getClusters(callDAG);
/*****************************************************************************/		
		
/*****************************************************************************/		
//		RandomNetworkGenerator randomNetworkGenerator = new RandomNetworkGenerator(callDAG);
//		String randVersionNum = versionNum + "rX";
//		randomNetworkGenerator.generateRandomNetwork(randVersionNum);
//		randomNetworkGenerator.randomCallDAG.removeCycles();
//		randomNetworkGenerator.randomCallDAG.loadDegreeMetric();
//		randomNetworkGenerator.randomCallDAG.loadLocationMetric(); 
//		randomNetworkGenerator.randomCallDAG.loadGeneralityMetric(); 
//		randomNetworkGenerator.randomCallDAG.loadComplexityMetric();
//		RandomNetworkGenerator.randomCallDAG.loadCentralityMetric();
		
//		centralityAnalysis.getCentralityCDF(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		centralityAnalysis.getLocationVsCentrality(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		centralityAnalysis.getSamplePathStatistics(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		locationAnalysis.getLocationHistogram(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getGeneralityVSComplexity(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgComplexity(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgGenerality(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgInDegree(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgOutDegree(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		locationAnalysis.getCallViolationMetric(randomNetworkGenerator.randomCallDAG);
/*****************************************************************************/
	}
}
