import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LocationAnalysis {
	double violatingPair;
	double totalPair;
	Set<String> visited;
		
	public Map<Double, Integer> getLocationHistogram(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//loc-hist-" + filePath + ".txt"));
		Map<Double, Integer> locHistogram = new TreeMap();
		for (String s: callDAG.location.keySet()) { // location is a map with location of each function
			double m = callDAG.location.get(s);
			pw.println(m); // for kernel density estimation with gnuplot
			if (locHistogram.containsKey(m)) {
				int f = locHistogram.get(m);
				locHistogram.put(m, f + 1);
			}
			else {
				locHistogram.put(m, 1);
			}
		}
		
//		for (double d: locHistogram.keySet()) {
//			pw.println(d + "\t" + locHistogram.get(d)); // actual histogram
//		}
		pw.close();

		return locHistogram;
	}
	
//	going towards the root
	public void reachableNodes(String node, String sourceNode, double sourceLocation, CallDAG callDAG) { 
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node); // full traversal for all the nodes
		
		if (!callDAG.callFrom.containsKey(node)) { // is a root
			return;
		}
		
		for (String s : callDAG.callFrom.get(node)) {
			reachableNodes(s, sourceNode, sourceLocation, callDAG);
		}
		
		if (!node.equals(sourceNode)) {
			if(callDAG.cycleEdges.containsKey(node) && callDAG.cycleEdges.get(node) == sourceNode)
				;
			else if(callDAG.cycleEdges.containsKey(sourceNode) && callDAG.cycleEdges.get(sourceNode) == node)
				;
			else if (callDAG.location.get(node) + 0.01 < sourceLocation) {
				++violatingPair;
			}
		}
	}

	public void getCallViolationMetric(CallDAG callDAG) {
		violatingPair = 0;
		totalPair = 0;
		for (String s: callDAG.functions) {	
			visited = new HashSet();
			double sourceLocation = callDAG.location.get(s);
			reachableNodes(s, s, sourceLocation, callDAG);
			totalPair += visited.size() - 1;			
		}
		
		System.out.println("Violation Metric: " + violatingPair / totalPair);
	}
	
	public void getClusterLocationDistribution() throws Exception { // mean separated clusters
		int versions[] = new int[] { 1, 9, 19, 29, 39 }; // change for different network

		for (int i : versions) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);

			double generalitySeparator, complexitySeparator;
			double gS = 0, cS = 0;
			for (String s: callDAG.location.keySet()) {
				gS += callDAG.generality.get(s);
				cS += callDAG.complexity.get(s);
			}
			generalitySeparator = gS / callDAG.location.size();
			complexitySeparator = cS / callDAG.location.size();
			
			PrintWriter pwGC = new PrintWriter(new File("Results//cluster1-GC-locations-v" + i + ".txt"));
			PrintWriter pwgC = new PrintWriter(new File("Results//cluster2-gC-locations-v" + i + ".txt"));
			PrintWriter pwgc = new PrintWriter(new File("Results//cluster3-gc-locations-v" + i + ".txt"));
			PrintWriter pwGc = new PrintWriter(new File("Results//cluster4-Gc-locations-v" + i + ".txt"));
			
			for (String s: callDAG.location.keySet()) {			
				double m = callDAG.location.get(s);
				double g = callDAG.generality.get(s);
				double c = callDAG.complexity.get(s);
				
				if (g > generalitySeparator && c > complexitySeparator) { 
					pwGC.println(m);
				}
				else if (g <= generalitySeparator && c > complexitySeparator) { 
					pwgC.println(m);				
				}
				else if (g <= generalitySeparator && c <= complexitySeparator) { 
					pwgc.println(m);					
				}
				else if (g > generalitySeparator && c <= complexitySeparator) {
					pwGc.println(m);				
				}
			}
			
			pwGC.close();
			pwgC.close();
			pwgc.close();
			pwGc.close();
		}
	}
	
	public void getLeafCallerLocationHistogram(CallDAG callDAG) {
		Map<Double, Integer> locHistogram = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callTo.containsKey(s)) continue;
			
			for (String r : callDAG.callFrom.get(s)) {
				double m = callDAG.location.get(r);
				if (locHistogram.containsKey(m)) {
					int f = locHistogram.get(m);
					locHistogram.put(m, f + 1);
				} else {
					locHistogram.put(m, 1);
				}
			}
		}
		
//		in percentage
		for (double d: locHistogram.keySet()) {
			System.out.println(d + "\t" + locHistogram.get(d));
		}
	}
	
	public void getLeafAnomalyForMcount() {
//		difference in number of leaves for without(v26)/with(v27) mcount
		CallDAG callDAGv26 = new CallDAG("kernel_callgraphs//full.graph-2.6.26g3");
		CallDAG callDAGv27 = new CallDAG("kernel_callgraphs//full.graph-2.6.27g3");
		
		for (String s: callDAGv26.location.keySet()) {
			double loc26 = callDAGv26.location.get(s);
			if (loc26 > 0) continue;
			if (callDAGv27.location.containsKey(s)) {
				System.out.println(s + " v26-loc: " + loc26 + " v27-loc: " + callDAGv27.location.get(s));
			}
		}
	}
	
	public static void getWineGlassGroupsGrowth() throws Exception {
		// CUSTOMIZED FOR DIFFERENT NETWORK
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkType + "-wineglass-fractions.txt"));
		for (int i = Driver.versiontStart; i < Driver.versionEnd; i++) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);

			double nBase = 0, nNeck = 0, nCup = 0, sz = callDAG.location.size();
			
			for (String s: callDAG.location.keySet()) {
				double loc = callDAG.location.get(s);
				if (loc <= 0.05) ++nBase;
				else if (loc >= 0.7) ++nCup;
				else ++nNeck;
			}
			
			pw.println((nBase / sz) + "\t" + (nNeck / sz) + "\t" + (nCup / sz) + "\t" + Driver.networkType + i);
		}
		
		pw.close();
	}
}
