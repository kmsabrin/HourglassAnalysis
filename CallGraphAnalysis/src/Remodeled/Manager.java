package Remodeled;

public class Manager {	
	private static void printNetworkStat(DependencyDAG dependencyDAG) {
		System.out.println("S: " + dependencyDAG.nSources);
		System.out.println("T: " + dependencyDAG.nTargets);
		System.out.println("E: " + dependencyDAG.nEdges);
		System.out.println("N: " + dependencyDAG.functions.size());
	}
	
	public static void doRealNetworkAnalysis() throws Exception {
		String netPath = "";
		String netID = "rat";
		
		if (netID.equals("rat")) {
			netPath = "metabolic_networks//rat-consolidated.txt";
			DependencyDAG.isMetabolic = true;
		}
		else if (netID.equals("monkey")) {
			netPath = "metabolic_networks//monkey-consolidated.txt";
			DependencyDAG.isMetabolic = true;
		}
		else if (netID.equals("court")) {
			netPath = "supremecourt_networks//court.txt";
			DependencyDAG.isCourtcase = true;
		}
		else if (netID.equals("kernel-21")) {
			netPath = "kernel_callgraphs//full.graph-2.6.21";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("kernel-31")) {
			netPath = "kernel_callgraphs//full.graph-2.6.31";
			DependencyDAG.isCallgraph = true;
		}
		else if (netID.equals("openssh-39")) {
			netPath = "openssh_callgraphs//full.graph-openssh-39";
			DependencyDAG.isCallgraph = true;
		}

		DependencyDAG dependencyDAG = new DependencyDAG(netPath);
		printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkMetrics();
		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		int centralityIndex = 1;
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, centralityIndex);
		
		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
	}
	
	public static void doSyntheticNetworkAnalysis() throws Exception {
		DependencyDAG.isSynthetic = true;
		String versions[] = {"NLNHGDAGa0.0", "NLNHGDAGa0.5", "NLNHGDAGa1.0", "NLNHGDAGa-0.5", "NLNHGDAGa-1.0"};
//		String versions[] = {"rectangleDAG", "noisyRectangleDAG", "trapezoidDAG", "diamondDAG", "hourglassDAG"};
//		String versions[] = {"NLNHGDAGa0.5"};
		for (int i = 0; i < versions.length; ++i) {
//			if (i != 2) continue;			
			String networkID = versions[i];
			System.out.println("Working on: " + networkID);
			DependencyDAG dependencyDAG = new DependencyDAG("synthetic_callgraphs//" + networkID + ".txt");
			printNetworkStat(dependencyDAG);
//			dependencyDAG.printNetworkMetrics();
			
//			DistributionAnalysis.printSyntheticPC(dependencyDAG, networkID);
//			DistributionAnalysis.targetEdgeConcentration(dependencyDAG);
//			DistributionAnalysis.getAveragePathLenth(dependencyDAG);
			DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);
//			DistributionAnalysis.getReachabilityCount(dependencyDAG);
//			DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, networkID);
			
//			new GradientFilterAnalysis().getSampleGradientsQuartileInterval(dependencyDAG, networkID);
			WaistDetection.runPCWaistDetection(dependencyDAG, networkID);
		}
	}
	
	public static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isSynthetic = true;
		DependencyDAG dependencyDAG = new DependencyDAG("toy_networks//toy_dag.txt");
		String netID = "toy_dag";
//		printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkMetrics();
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);		
//		WaistDetection.runPCWaistDetection(dependencyDAG, netID);
//		DistributionAnalysis.printSourceVsTargetCompression(dependencyDAG, netID);
	}

	public static void main(String[] args) throws Exception {		
//		Manager.doSyntheticNetworkAnalysis();
		Manager.doRealNetworkAnalysis();
//		Manager.doToyNetworkAnalysis();
		System.out.println("Done!");
	}
}
