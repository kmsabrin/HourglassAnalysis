package Remodeled;
import java.util.Arrays;
import java.util.HashSet;

public class ToyNetwork {	
	CallDAG callDAG;
	
	ToyNetwork() {
		callDAG = new CallDAG();
	}
	
	public void loadCallGraph() { // Test call graph constructor
		callDAG.functions.add("a");
		callDAG.functions.add("b");
		callDAG.functions.add("c");
		callDAG.functions.add("d");
		callDAG.functions.add("e");
		callDAG.functions.add("f");
		callDAG.functions.add("g");
		callDAG.functions.add("h");
		callDAG.functions.add("i");
		callDAG.functions.add("j");
		callDAG.functions.add("k");
		
		callDAG.depends.put("a", new HashSet<String>(Arrays.asList("d")));
		callDAG.depends.put("b", new HashSet<String>(Arrays.asList("d")));
		callDAG.depends.put("c", new HashSet<String>(Arrays.asList("f", "j")));
		callDAG.depends.put("d", new HashSet<String>(Arrays.asList("e", "f", "g")));
		callDAG.depends.put("e", new HashSet<String>(Arrays.asList("g")));
		callDAG.depends.put("f", new HashSet<String>(Arrays.asList("g", "k")));
		callDAG.depends.put("g", new HashSet<String>(Arrays.asList("h", "i")));
		callDAG.depends.put("j", new HashSet<String>(Arrays.asList("k")));

		callDAG.serves.put("d", new HashSet<String>(Arrays.asList("a", "b")));
		callDAG.serves.put("e", new HashSet<String>(Arrays.asList("d")));
		callDAG.serves.put("f", new HashSet<String>(Arrays.asList("d", "c")));
		callDAG.serves.put("g", new HashSet<String>(Arrays.asList("e", "d", "f")));
		callDAG.serves.put("j", new HashSet<String>(Arrays.asList("c")));
		callDAG.serves.put("k", new HashSet<String>(Arrays.asList("f", "j")));
		callDAG.serves.put("h", new HashSet<String>(Arrays.asList("g")));
		callDAG.serves.put("i", new HashSet<String>(Arrays.asList("g")));
		
		callDAG.removeCycles();
		callDAG.loadDegreeMetric();
		callDAG.loadLocationMetric(); // must load degree metric before

		callDAG.loadPagerankCentralityMetric();
		callDAG.loadRechablity();
	}
			
	public static void main(String[] args) throws Exception {
		ToyNetwork toyNetwork = new ToyNetwork();
		toyNetwork.loadCallGraph();
		toyNetwork.callDAG.printNetworkMetrics();
		
//		IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(toyNetwork.callDAG);
//		iteratedMaxCentralityCoverage.runIMCC();		
	}
}

