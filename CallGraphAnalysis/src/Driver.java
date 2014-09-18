
public class Driver {
	
	static String networkPath = "kernel_callgraphs//full.graph-2.6.";
	static String networkUsed = "kernel";
	static int versiontStart = 0;
	static int versionEnd = 40;
	
//	static String networkPath = "openssh_callgraphs//full.graph-openssh-";
//	static String networkUsed = "ssh";
//	static int versiontStart = 1;
//	static int versionEnd = 40;
	
	public static void main(String[] args) throws Exception {		
/*****************************************************************************/
		String version = "9";
		String versionNum = networkUsed + version;
		CallDAG callDAG = new CallDAG(Driver.networkPath + version);
//		System.out.println("nFunctions: " + callDAG.functions.size());
//		System.out.println("nEdges: " + callDAG.nEdges);
//		int nRoots = 0, nLeaves = 0;
//		for (String s: callDAG.functions) {
//			if (!callDAG.callFrom.containsKey(s)) ++nRoots;
//			if (!callDAG.callTo.containsKey(s)) ++nLeaves;
//		}
//		System.out.println("Roots: " + nRoots + " Leaves: " + nLeaves);
/*****************************************************************************/

/*****************************************************************************/
		AgeAnalysis ageAnalysis = new AgeAnalysis();
		DegreeAnalysis degreeAnalysis = new DegreeAnalysis();		
		LocationAnalysis locationAnalysis = new LocationAnalysis();
		PersistenceAnalysis persistenceAnalysis = new PersistenceAnalysis();
		GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();
		EvolutionAnalysis evolutionaryAnalysis = new EvolutionAnalysis();
		DiameterAnalysis diameterAnalysis = new DiameterAnalysis();
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
		ageAnalysis.getLocationVsPersistencePercentiles();
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
//		generalityAnalysis.getLocationVSAvgGenerality(callDAG, versionNum);
//		generalityAnalysis.getLocationVSAvgComplexity(callDAG, versionNum);
//		generalityAnalysis.getGeneralityVSComplexity(callDAG, versionNum);
//		generalityAnalysis.getCentralNodes(callDAG); // CUSTOMIZED FOR DIFFERENT NETWORK
/*****************************************************************************/

/*****************************************************************************/		
//		diameterAnalysis.getEffectiveDiameter(callDAG);
//		diameterAnalysis.getEffectiveDiameterForAllVersions();
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
//		randomNetworkGenerator.callDAG.loadLocationMetric(); //		degree already loaded
//		randomNetworkGenerator.callDAG.loadGeneralityMetric(); 
//		randomNetworkGenerator.callDAG.loadComplexityMetric();
//		locationAnalysis.getLocationHistogram(randomNetworkGenerator.callDAG, randVersionNum);
//		generalityAnalysis.getGeneralityVSComplexity(randomNetworkGenerator.callDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgComplexity(randomNetworkGenerator.callDAG, randVersionNum);
//		generalityAnalysis.getLocationVSAvgGenerality(randomNetworkGenerator.callDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgInDegree(randomNetworkGenerator.callDAG, randVersionNum);
//		degreeAnalysis.getLocationVSAvgOutDegree(randomNetworkGenerator.callDAG, randVersionNum);
//		locationAnalysis.getCallViolationMetric(randomNetworkGenerator.callDAG);
/*****************************************************************************/
	}
}
