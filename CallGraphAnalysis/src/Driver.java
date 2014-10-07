import java.io.File;
import java.io.PrintWriter;


public class Driver {
	
	static String networkPath = "kernel_callgraphs//full.graph-2.6.";
	static String networkUsed = "kernel";
	static int versiontStart = 0;
	static int versionEnd = 40;
	
//	static String networkPath = "openssh_callgraphs//full.graph-openssh-";
//	static String networkUsed = "ssh";
//	static int versiontStart = 1;
//	static int versionEnd = 40;
	
	static String version = "1";

	public static void main(String[] args) throws Exception {		
/*****************************************************************************/
		String versionNum = networkUsed + version;
		CallDAG callDAG = new CallDAG(Driver.networkPath + version);
		System.out.println("nFunctions: " + callDAG.functions.size());
		System.out.println("nEdges: " + callDAG.nEdges);
		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
//		getCallDAGSIF(callDAG, versionNum);
/*****************************************************************************/

/*****************************************************************************/
//		AgeAnalysis ageAnalysis = new AgeAnalysis();
//		DegreeAnalysis degreeAnalysis = new DegreeAnalysis();		
//		LocationAnalysis locationAnalysis = new LocationAnalysis();
//		PersistenceAnalysis persistenceAnalysis = new PersistenceAnalysis();
//		GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();
//		EvolutionAnalysis evolutionaryAnalysis = new EvolutionAnalysis();
//		DiameterAnalysis diameterAnalysis = new DiameterAnalysis();
		ModularityAnalysis modularityAnalysis  = new ModularityAnalysis();
//		RandomNetworkGenerator randomNetworkGenerator = new RandomNetworkGenerator(callDAG);
/*****************************************************************************/
		
/*****************************************************************************/
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
//		ageAnalysis.getClusterLifeTimeDistribution(); // CHANGE FOR DIFFERENT NETWORKS
//		ageAnalysis.getLocationVsPersistencePercentiles();
//		ageAnalysis.getLocationVsTransientStable(); // CHANGE FOR DIFFERENT NETWORKS
//		ageAnalysis.getlocationDispersion();
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
//		CallDAG callDAGFrom = new CallDAG("kernel_callgraphs//full.graph-2.6.26");
//		CallDAG callDAGTo = new CallDAG("kernel_callgraphs//full.graph-2.6.27");
//		persistenceAnalysis.getContiguousFunctionPersistance(callDAGFrom, callDAGTo);
/*****************************************************************************/

/*****************************************************************************/		
//		evolutionaryAnalysis.getAverageGeneralityPerLocationForEachVersion();
//		evolutionaryAnalysis.getGenCmpScatterForAllVersions();
//		evolutionaryAnalysis.getViolationMetricForEachVersion();
//		evolutionaryAnalysis.getLocationHistogramForEachVersion();
//		evolutionaryAnalysis.getEvolutionaryDeathBirthTrend(); 
//		evolutionaryAnalysis.getLocationVsSizeTrend();// CUSTOMIZED FOR DIFFERENT NETWORK
//		evolutionaryAnalysis.getNetworkGrowthTrend();
//		evolutionaryAnalysis.getNumClustersForEachVersion();
//		evolutionaryAnalysis.getClusterSizeTrend();
/*****************************************************************************/

/*****************************************************************************/		
//		generalityAnalysis.getGeneralityHistogram(callDAG, versionNum);
//		generalityAnalysis.getComplexityHistogram(callDAG, versionNum);
//		generalityAnalysis.getLocationVsAvgGenerality(callDAG, versionNum);
//		generalityAnalysis.getLocationVsAvgComplexity(callDAG, versionNum);
//		generalityAnalysis.getGeneralityVsComplexity(callDAG, versionNum);
//		generalityAnalysis.getCentralNodes(callDAG); // CUSTOMIZED FOR DIFFERENT NETWORK
/*****************************************************************************/

/*****************************************************************************/		
//		diameterAnalysis.getEffectiveDiameter(callDAG);
//		diameterAnalysis.getEffectiveDiameterForAllVersions();
/*****************************************************************************/

/*****************************************************************************/		
//		modularityAnalysis.getModuleGeneralityVsComplexity(callDAG, versionNum);
//		modularityAnalysis.getInfo();
//		modularityAnalysis.getAvgModuleGeneralityVsLocation(callDAG, versionNum);
//		modularityAnalysis.getRandomModularNetwork();
		modularityAnalysis.getWalktrapModules(callDAG);
/*****************************************************************************/

/*****************************************************************************/		
//		ClusterAnalysis clusterAnalysis = new ClusterAnalysis();
//		ClusterAnalysis.demonstrateClustersForVersionX();
//
//		clusterAnalysis.getClusters(callDAG);
/*****************************************************************************/		
		
/*****************************************************************************/		
//		String randVersionNum = versionNum + "rX";
//		randomNetworkGenerator.generateRandomNetwork(randVersionNum);
//		randomNetworkGenerator.randomCallDAG.loadDegreeMetric();
//		randomNetworkGenerator.randomCallDAG.loadLocationMetric(); 
//		randomNetworkGenerator.randomCallDAG.loadGeneralityMetric(); 
//		randomNetworkGenerator.randomCallDAG.loadComplexityMetric();
//		locationAnalysis.getLocationHistogram(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getGeneralityVSComplexity(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgComplexity(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgGenerality(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgInDegree(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgOutDegree(randomNetworkGenerator.randomCallDAG, randVersionNum);
//		locationAnalysis.getCallViolationMetric(randomNetworkGenerator.randomCallDAG);
/*****************************************************************************/
	}
	
	public static void getCallDAGSIF(CallDAG callDAG, String versionNum) throws Exception {
		PrintWriter pw = new PrintWriter(new File("module-callDAG-" + versionNum + ".txt"));
		for (String s: callDAG.functions) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r: callDAG.callTo.get(s)) {
					pw.println(callDAG.functionID.get(s) + "\t" + callDAG.functionID.get(r));
				}
			}
		}
		pw.close();
	}
}
