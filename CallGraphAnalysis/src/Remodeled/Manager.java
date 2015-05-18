package Remodeled;

public class Manager {	
	static String networkPath = "kernel_callgraphs//full.graph-2.6.";
	static String networkType = "kernel";
	static int versiontStart = 0;
	static int versionEnd = 40; // last + 1
	
//	static String networkPath = "openssh_callgraphs//full.graph-openssh-";
//	static String networkType = "ssh";
//	static int versiontStart = 1;
//	static int versionEnd = 40;
	
	private static void printNetworkStat(DependencyDAG dependencyDAG) {
		System.out.println("S: " + dependencyDAG.nSources);
		System.out.println("T: " + dependencyDAG.nTargets);
		System.out.println("E: " + dependencyDAG.nEdges);
		System.out.println("N: " + dependencyDAG.functions.size());
	}
	
	public static void doMiscNetworkAnalysis() throws Exception {
		String networkID = "other_callgraphs//out.linux";
		DependencyDAG dependencyDAG = new DependencyDAG(networkID);
		WaistDetection.runPCWaistDetection(dependencyDAG);
	}
	
	public static void doKernelAnalysis() throws Exception {
//		String versions[] = {"1", "11", "21", "31"};
		String versions[] = {"11"};
		for (int i = 0; i < versions.length; ++i) {
			String networkID = networkType + "-" + versions[i];	
			DependencyDAG dependencyDAG = new DependencyDAG(networkPath + versions[i]);
			DistributionAnalysis.printCentralityRanks(dependencyDAG, networkID);
//			DistributionAnalysis.printCentralityCCDF(dependencyDAG, networkID);
			
//			printNetworkStat(dependencyDAG);
//			dependencyDAG.printNetworkMetrics();
//			DistributionAnalysis.printCentralityDistribution(dependencyDAG, networkID);
//			DistributionAnalysis.printLocationVsCentrality(dependencyDAG, networkID);
//			DistributionAnalysis.printTargetDependencyDistribution(dependencyDAG, networkID);
//			DistributionAnalysis.printPathCentralityVsPageRankScatter(dependencyDAG, networkID);
			
//			System.out.println(dependencyDAG.serversReachable.get("start_kernel").size());
//			RankAggregation.aggregateRanks(dependencyDAG);
			
//			IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(dependencyDAG);
//			iteratedMaxCentralityCoverage.runLinkCoverage(networkID);
//			iteratedMaxCentralityCoverage.runIMCC();
			
//			WaistDetection.runPCWaistDetection(dependencyDAG);
		}	
	}
	
	public static void doArtificialNetworkAnalysis() throws Exception {
//		new ArtificialDAG().generateRectangleDAG();
//		new ArtificialDAG().generateHourglassDAG();
//		new ArtificialDAG().generateTrapezoidsDAG();
//		new ArtificialDAG().generateNoisyRectangleDAG();
//		new ArtificialDAG().generateDiamondDAG();

//		String versions[] = {"rectangleDAG", 
//							 "noisyRectangleDAG",
//							 "hourglassDAG", 
//							 "trapezoidDAG",
//							 "diamondDAG",
//		};
		
		String versions[] = {"NLHGDAG"/*, "rectangleDAGKN", "noisyRectangleDAGKN"*/};
		
		for (int i = 0; i < versions.length; ++i) {
//			if (i != 2) continue;			
			String networkID = versions[i];
			System.out.println("Working on: " + networkID);
			DependencyDAG dependencyDAG = new DependencyDAG("artificial_callgraphs//" + networkID + ".txt");
			
			DistributionAnalysis.printCentralityRanks(dependencyDAG, networkID);
			DistributionAnalysis.printCentralityCCDF(dependencyDAG, networkID);
			
//			IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(dependencyDAG);
//			iteratedMaxCentralityCoverage.runLinkCoverage(networkID);		
//			iteratedMaxCentralityCoverage.runIMCC();

//			WaistDetection.runWaistDetection(dependencyDAG);
		}
	}

	public static void main(String[] args) throws Exception {		
//		Manager.doArtificialNetworkAnalysis();
//		Manager.doKernelAnalysis();
		Manager.doMiscNetworkAnalysis();
//		System.out.println("Done!");
	}
}
