
public class Driver {
	public static void main(String[] args) throws Exception {		
/*****************************************************************************/
		String versionNum = "v15";
		CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6.15");
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
//		AgeAnalysis ageAnalysis = new AgeAnalysis();
		DegreeAnalysis degreeAnalysis = new DegreeAnalysis();		
		LocationAnalysis locationAnalysis = new LocationAnalysis();
		PersistenceAnalysis persistenceAnalysis = new PersistenceAnalysis();
		GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();
		EvolutionAnalysis evolutionaryAnalysis = new EvolutionAnalysis();
		DiameterAnalysis diameterAnalysis = new DiameterAnalysis();
		RandomNetworkGenerator randomNetworkGenerator = new RandomNetworkGenerator(callDAG);
/*****************************************************************************/
		
/*****************************************************************************/
//		degreeAnalysis.getInDegreeCCDF(callDAG, versionNum);
//		degreeAnalysis.getOutDegreeCCDF(callDAG, versionNum);
//		degreeAnalysis.getLocationVSAvgInDegree(callDAG, versionNum);
//		degreeAnalysis.getLocationVSAvgOutDegree(callDAG, versionNum);
//		degreeAnalysis.getIndegreeVsOutDegree(callDAG);
//		degreeAnalysis.getInDegreeDistribution(callDAG, versionNum);
//		degreeAnalysis.getOutDegreeDistribution(callDAG, versionNum);
//		degreeAnalysis.getIndegreeVsOutDegreeCorrelationCoefficient(callDAG);
//		degreeAnalysis.getSpearmanCoefficientForAllVersions();
/*****************************************************************************/
		
/*****************************************************************************/
//		locationAnalysis.getLocationHistogram(callDAG, versionNum);
//		locationAnalysis.getLocationVsCallDirection(callDAG);
//		locationAnalysis.getClusterLocationDistribution();
//		locationAnalysis.getLeafCallerLocationHistogram(callDAG);
//		locationAnalysis.getLocationHistogramForEachVersion();
//		locationAnalysis.getLeafAnomalyForMcount();
//		locationAnalysis.getWineGlassGroupsGrowth();
//		locationAnalysis.getCallViolationMetric(callDAG);
/*****************************************************************************/

/*****************************************************************************/		
//		ageAnalysis.getLastLocationVSAverageAge();
//		ageAnalysis.getAgeHistogram();
//		ageAnalysis.getLastLocationVSDeathPercentage();
//		ageAnalysis.getAgeVSDeathPercentage();
//		ageAnalysis.getLocationModeVSAge(); /* ?!? */
//		ageAnalysis.getAgeVSLastLocation();
//		ageAnalysis.getLastLocationVSAlivePercentage();
//		ageAnalysis.getlocationDispersion();
//		ageAnalysis.getLocationVSNumNodesWithAgeX();
//		ageAnalysis.getClusterAgeDistribution();
//		ageAnalysis.getClusterLifeTimeDistribution();
//		ageAnalysis.getLocationLifeTimeDistribution();
/*****************************************************************************/

/*****************************************************************************/		
//		CallDAG callDAGFrom = new CallDAG("callGraphs//full.graph-2.6.26");
//		CallDAG callDAGTo = new CallDAG("callGraphs//full.graph-2.6.27");
//		persistenceAnalysis.getContiguousFunctionPersistance(callDAGFrom, callDAGTo);
/*****************************************************************************/

/*****************************************************************************/		
//		evolutionaryAnalysis.getAverageGeneralityPerLocationForEachVersion();
//		evolutionaryAnalysis.getEvolutionaryDeathBirthTrend();
//		evolutionaryAnalysis.getLocationHistogramForEachVersion();
//		evolutionaryAnalysis.getViolationMetricForEachVersion();
//		evolutionaryAnalysis.getNumClustersForEachVersion();
//		evolutionaryAnalysis.getLocationVsSizeTrend();
//		evolutionaryAnalysis.getNetworkGrwothTrend();
//		evolutionaryAnalysis.getClusterSizeTrend();
//		evolutionaryAnalysis.getGenCmpScatterForAllVersions();
/*****************************************************************************/

/*****************************************************************************/		
//		generalityAnalysis.getGeneralityHistogram(callDAG, versionNum);
//		generalityAnalysis.getComplexityHistogram(callDAG, versionNum);
//		generalityAnalysis.getLocationVSAvgGenerality(callDAG, versionNum);
//		generalityAnalysis.getLocationVSAvgComplexity(callDAG, versionNum);
//		generalityAnalysis.getGeneralityVSComplexity(callDAG, versionNum);
//		generalityAnalysis.getCentralNodes(callDAG);
/*****************************************************************************/

/*****************************************************************************/		
//		diameterAnalysis.getEffectiveDiameter(callDAG);
//		diameterAnalysis.getEffectiveDiameterForAllVersions();
/*****************************************************************************/

/*****************************************************************************/		
//		ClusterAnalysis clusterAnalysis = new ClusterAnalysis(0.03, 30);
//		clusterAnalysis.getClusters(callDAG);
//		ClusterAnalysis.demonstrateClusters();
/*****************************************************************************/		
		
/*****************************************************************************/		
		String randVersionNum = versionNum + "rX";
		randomNetworkGenerator.generateRandomNetwork(randVersionNum);
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