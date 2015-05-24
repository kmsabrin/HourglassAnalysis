package Remodeled;
import java.util.Arrays;
import java.util.HashSet;

public class ToyNetwork {	
	DependencyDAG dependencyDAG;
	
	ToyNetwork() {
		dependencyDAG = new DependencyDAG();
	}
	
	public void loadCallGraph() { // Test call graph constructor
		dependencyDAG.functions.add("a");
		dependencyDAG.functions.add("b");
		dependencyDAG.functions.add("c");
		dependencyDAG.functions.add("d");
		dependencyDAG.functions.add("e");
		dependencyDAG.functions.add("f");
		dependencyDAG.functions.add("g");
		dependencyDAG.functions.add("h");
		dependencyDAG.functions.add("i");
		dependencyDAG.functions.add("j");
		dependencyDAG.functions.add("k");
		
		dependencyDAG.depends.put("a", new HashSet<String>(Arrays.asList("d")));
		dependencyDAG.depends.put("b", new HashSet<String>(Arrays.asList("d")));
		dependencyDAG.depends.put("c", new HashSet<String>(Arrays.asList("f", "j")));
		dependencyDAG.depends.put("d", new HashSet<String>(Arrays.asList("e", "f", "g")));
		dependencyDAG.depends.put("e", new HashSet<String>(Arrays.asList("g")));
		dependencyDAG.depends.put("f", new HashSet<String>(Arrays.asList("g", "k")));
		dependencyDAG.depends.put("g", new HashSet<String>(Arrays.asList("h", "i")));
		dependencyDAG.depends.put("j", new HashSet<String>(Arrays.asList("k")));

		dependencyDAG.serves.put("d", new HashSet<String>(Arrays.asList("a", "b")));
		dependencyDAG.serves.put("e", new HashSet<String>(Arrays.asList("d")));
		dependencyDAG.serves.put("f", new HashSet<String>(Arrays.asList("d", "c")));
		dependencyDAG.serves.put("g", new HashSet<String>(Arrays.asList("e", "d", "f")));
		dependencyDAG.serves.put("j", new HashSet<String>(Arrays.asList("c")));
		dependencyDAG.serves.put("k", new HashSet<String>(Arrays.asList("f", "j")));
		dependencyDAG.serves.put("h", new HashSet<String>(Arrays.asList("g")));
		dependencyDAG.serves.put("i", new HashSet<String>(Arrays.asList("g")));
		
		dependencyDAG.removeCycles();
		dependencyDAG.removeIsolatedNodes();
		dependencyDAG.loadDegreeMetric();
		dependencyDAG.loadPathStatistics();
		dependencyDAG.loadLocationMetric(); // must load degree metric before
		dependencyDAG.loadPagerankCentralityMetric();
		dependencyDAG.loadPathCentralityMetric();
//		dependencyDAG.loadRechablity();
	}
			
	public static void main(String[] args) throws Exception {
		ToyNetwork toyNetwork = new ToyNetwork();
		WaistDetection.topKNodes = new HashSet();
		toyNetwork.loadCallGraph();
//		toyNetwork.dependencyDAG.printNetworkMetrics();
		
		new GradientFilter().getWilcoxonRankSum(toyNetwork.dependencyDAG, "toyDAG");
		
//		DistributionAnalysis.printCentralityRanks(toyNetwork.dependencyDAG, "toyDAG");
//		DistributionAnalysis.printCentralityCCDF(toyNetwork.dependencyDAG, "toyDAG");
//		RankAggregation.aggregateRanks(toyNetwork.dependencyDAG);
		
//		IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(toyNetwork.dependencyDAG);
//		iteratedMaxCentralityCoverage.runLinkCoverage("toyNetowrk");
		/*iteratedMaxCentralityCoverage.runIMCC();*/		
		
//		WaistDetection.runPCWaistDetection(toyNetwork.dependencyDAG, "toyDAG");
		

	}
}

