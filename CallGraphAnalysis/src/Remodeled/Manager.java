package Remodeled;

public class Manager {	
	private static void printNetworkStat(DependencyDAG dependencyDAG) {
		System.out.println("S: " + dependencyDAG.nSources);
		System.out.println("T: " + dependencyDAG.nTargets);
		System.out.println("E: " + dependencyDAG.nEdges);
		System.out.println("N: " + dependencyDAG.functions.size());
	}
	
	public static void doRealNetworkAnalysis() throws Exception {	
//		String netPath = "metabolic_networks//rat-consolidated.txt";
//		String netID = "rat";
		
//		String netPath = "metabolic_networks//monkey-consolidated.txt";
//		String netID = "monkey";
		
		String netPath = "supremecourt_networks//court.txt";
		String netID = "court";

//		String netPath = "kernel_callgraphs//full.graph-2.6.21";
//		String netID = "kernel-21";
		
//		String netPath = "kernel_callgraphs//full.graph-2.6.31";
//		String netID = "kernel-31";
		
//		String netPath = "openssh_callgraphs//full.graph-openssh-39";
//		String netID = "openssh-39";
//		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
//		printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkMetrics();
		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 2);
		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 3);
		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 4);
		
//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
//		DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, netID);
	}
	
	public static void doSyntheticNetworkAnalysis() throws Exception {
		String versions[] = {"NLHGDAG", "NLNHGDAGa0", "NLNHGDAGa1"};
//		String versions[] = {"rectangleDAG", "noisyRectangleDAG", "trapezoidDAG", "diamondDAG", "hourglassDAG"};
		
		for (int i = 0; i < versions.length; ++i) {
			if (i < 1) continue;			
			String networkID = versions[i];
			System.out.println("Working on: " + networkID);
			DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
			printNetworkStat(dependencyDAG);
			
//			DistributionAnalysis.targetEdgeConcentration(dependencyDAG);
//			DistributionAnalysis.getAveragePathLenth(dependencyDAG);
			DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
//			DistributionAnalysis.getReachabilityCount(dependencyDAG);
//			DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, networkID);
			
//			new GradientFilterAnalysis().getSampleGradientsQuartileInterval(dependencyDAG, networkID);
//			WaistDetection.runPCWaistDetection(dependencyDAG, networkID);
		}
	}
	
	public static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//toy_dag.txt");
		String netID = "toy_dag";
//		printNetworkStat(dependencyDAG);
		dependencyDAG.printNetworkMetrics();
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);
//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
//		DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, netID);
	}

	public static void main(String[] args) throws Exception {		
		Manager.doSyntheticNetworkAnalysis();
//		Manager.doRealNetworkAnalysis();
//		Manager.doToyNetworkAnalysis();
		System.out.println("Done!");
	}
}
