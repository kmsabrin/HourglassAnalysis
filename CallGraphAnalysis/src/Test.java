import java.util.Arrays;
import java.util.HashSet;

public class Test {	
	CallDAG callDAG;
	
	Test() {
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

		callDAG.callTo.put("a",  new HashSet<String>(Arrays.asList("b", "c")));
		callDAG.callTo.put("b", new HashSet<String>(Arrays.asList("d", "f")));
		callDAG.callTo.put("c", new HashSet<String>(Arrays.asList("d", "g", "e")));
		callDAG.callTo.put("d", new HashSet<String>(Arrays.asList("f", "g")));
		callDAG.callTo.put("e", new HashSet<String>(Arrays.asList("h")));
		callDAG.callTo.put("f", new HashSet<String>(Arrays.asList("i")));
		callDAG.callTo.put("j", new HashSet<String>(Arrays.asList("e", "k")));

		callDAG.callFrom.put("b", new HashSet<String>(Arrays.asList("a")));
		callDAG.callFrom.put("c", new HashSet<String>(Arrays.asList("a")));
		callDAG.callFrom.put("d", new HashSet<String>(Arrays.asList("b", "c")));
		callDAG.callFrom.put("e", new HashSet<String>(Arrays.asList("c", "j")));
		callDAG.callFrom.put("f", new HashSet<String>(Arrays.asList("b", "d")));
		callDAG.callFrom.put("g", new HashSet<String>(Arrays.asList("d", "c")));
		callDAG.callFrom.put("h", new HashSet<String>(Arrays.asList("e")));
		callDAG.callFrom.put("i", new HashSet<String>(Arrays.asList("f")));
		callDAG.callFrom.put("k", new HashSet<String>(Arrays.asList("j")));

		callDAG.nEdges = 13;
		for (String s: callDAG.functions) {
			if (!callDAG.callFrom.containsKey(s)) ++callDAG.nRoots;
			if (!callDAG.callTo.containsKey(s)) ++callDAG.nLeaves;
		}
		callDAG.removeCycles();
		callDAG.loadDegreeMetric();
		callDAG.loadLocationMetric(); // must load degree metric before
		callDAG.loadGeneralityMetric(); // approximated
		callDAG.loadComplexityMetric();
	}
			
	public static void main(String[] args) throws Exception {
		Test test = new Test();
		test.loadCallGraph();
		
//		DegreeAnalysis degreeAnalysis = new DegreeAnalysis();
//		degreeAnalysis.getInDegreeCCDF(test.callDAG);
//		degreeAnalysis.getOutDegreeCCDF(test.callDAG);
//		degreeAnalysis.getLocationVSInDegree(test.callDAG);
//		degreeAnalysis.getLocationVSOutDegree(test.callDAG);

//		for (String s: test.callDAG.location.keySet()) {
//			System.out.println(s + "  " + test.callDAG.location.get(s));
//		}
		
//		LocationAnalysis locationAnalysis = new LocationAnalysis();
//		locationAnalysis.getLocationHistogram(test.callDAG);
//		locationAnalysis.getLocationVsCallDirection(test.callDAG);
		
		GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();
		for (String s: test.callDAG.functions) {
			System.out.print(s);
			System.out.print("\t Location " + test.callDAG.location.get(s));
			System.out.print("\t Generality " + test.callDAG.generality.get(s));
			System.out.print("\t Complexity " + test.callDAG.complexity.get(s));
			System.out.print("\t ModuleGenerality " + test.callDAG.moduleGenerality.get(s));
			System.out.println("\t ModuleComplexity " + test.callDAG.moduleComplexity.get(s));
		}
		
//		generalityAnalysis.getGeneralityHistogram(test.callDAG);
//		generalityAnalysis.getLocationVSAvgGenerality(test.callDAG);
//		generalityAnalysis.getGeneralityVSComplexity(test.callDAG);
		
//		DiameterAnalysis da = new DiameterAnalysis();
//		da.getEffectiveDiameter(test.callDAG);
		
//		RandomNetworkGenerator randomeNetworkGenerator = new RandomNetworkGenerator(test.callDAG);
//		randomeNetworkGenerator.generateRandomNetwork("Test");
		
//		ClusterAnalysis clusterAnalysis = new ClusterAnalysis(0.05, 1);
//		clusterAnalysis.getClusters(test.callDAG);
	}
}

