package Remodeled;

public class Manager {	
	private static void printNetworkStat(DependencyDAG dependencyDAG) {
		System.out.println("S: " + dependencyDAG.nSources);
		System.out.println("T: " + dependencyDAG.nTargets);
		System.out.println("E: " + dependencyDAG.nEdges);
		System.out.println("N: " + dependencyDAG.functions.size());
	}
	
	public static void doRealNetworkAnalysis() throws Exception {
//		String netPath = "other_callgraphs//out.linux";
//		String netPath = "metabolic_networks//rat.txt";
//		String netID = "rat";
		String netPath = "supremecourt_networks//court.txt";
		String netID = "court";
//		String netPath = "kernel_callgraphs//full.graph-2.6.11";
//		String netID = "kernel-21";
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		printNetworkStat(dependencyDAG);
		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID);
		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
	}
	
	public static void doSyntheticNetworkAnalysis() throws Exception {
//		new SyntheticLayeredDAG().generateRectangleDAG();
//		new SyntheticLayeredDAG().generateHourglassDAG();
//		new SyntheticLayeredDAG().generateTrapezoidsDAG();
//		new SyntheticLayeredDAG().generateNoisyRectangleDAG();
//		new SyntheticLayeredDAG().generateDiamondDAG();

//		String versions[] = {"rectangleDAG", 
//							 "noisyRectangleDAG",
//							 "hourglassDAG", 
//							 "trapezoidDAG",
//							 "diamondDAG",
//		};
		
		String versions[] = {"NLHGDAG", "NLNHGDAG"};
		
		for (int i = 0; i < versions.length; ++i) {
			if (i < 1) continue;			
			String networkID = versions[i];
			System.out.println("Working on: " + networkID);
			DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
//			dependencyDAG.printNetworkMetrics();
			
			DistributionAnalysis.getAveragePathLenth(dependencyDAG);
//			DistributionAnalysis.printCentralityRanks(dependencyDAG, networkID);
//			DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID);
//			new GradientFilter().getWilcoxonRankSum(dependencyDAG, networkID);
			
//			IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(dependencyDAG);
//			iteratedMaxCentralityCoverage.runLinkCoverage(networkID);		
//			iteratedMaxCentralityCoverage.runIMCC();

//			WaistDetection.runPCWaistDetection(dependencyDAG, networkID);
		}
	}

	public static void main(String[] args) throws Exception {		
		Manager.doSyntheticNetworkAnalysis();
//		Manager.doRealNetworkAnalysis();
		System.out.println("Done!");
	}
}
