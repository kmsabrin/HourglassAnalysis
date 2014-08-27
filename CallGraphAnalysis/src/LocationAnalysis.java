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
		
		for (double d: locHistogram.keySet()) {
//			pw.println(d + "\t" + locHistogram.get(d)); // actual histogram
		}
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
			else if (callDAG.location.get(node) + 0.05 < sourceLocation) {
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
		
		System.out.println(violatingPair / totalPair);
	}
	
	public void getClusterLocationDistribution(CallDAG callDAG) { // visually separated clusters
		double generalitySeparation = 0.2;
		double complexitySeparation = 0.05;
		double count = 0;
		
		Map<Double, Integer> locHistogram = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double m = callDAG.location.get(s);
			double g = callDAG.generality.get(s);
			double c = callDAG.complexity.get(s);

//			if (g > generalitySeparation && c > complexitySeparation) continue;
//			if (g < generalitySeparation && c > complexitySeparation) continue;
//			if (g < generalitySeparation && c < complexitySeparation) continue;
//			if (g > generalitySeparation && c < complexitySeparation) continue;
			
			if (g > 0.2) continue;
			if (c > 0.015) continue;
			
//			System.out.println(s);
			
			++count;
			
			if (locHistogram.containsKey(m)) {
				int f = locHistogram.get(m);
				locHistogram.put(m, f + 1);
			}
			else {
				locHistogram.put(m, 1);
			}
		}
		
		System.out.println(count);
//		in percentage
		for (double d = 0; d <= 100; d++) {
//			System.out.println(d + "\t" + locHistogram.get(d) * 100.0 / count);
//			System.out.println(d / 100.0);
			double v = 0;
			if (locHistogram.containsKey(d / 100.0)) {
				v = locHistogram.get(d / 100.0);
			}
			System.out.println(v * 100.0 / count);
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
		CallDAG callDAGv26 = new CallDAG("callGraphs//full.graph-2.6.26g3");
		CallDAG callDAGv27 = new CallDAG("callGraphs//full.graph-2.6.27g3");
		
		for (String s: callDAGv26.location.keySet()) {
			double loc26 = callDAGv26.location.get(s);
			if (loc26 > 0) continue;
			if (callDAGv27.location.containsKey(s)) {
				System.out.println(s + " v26-loc: " + loc26 + " v27-loc: " + callDAGv27.location.get(s));
			}
		}
	}
	
	public static void getWineGlassGroupsGrowth() {
		for (int i = 0; i < 40; i++) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);

			double nBase = 0, nNeck = 0, nCup = 0, sz = callDAG.location.size();
			
			for (String s: callDAG.location.keySet()) {
				double loc = callDAG.location.get(s);
				if (loc <= 0.05) ++nBase;
				else if (loc >= 0.7) ++nCup;
				else ++nNeck;
			}
			
			System.out.println((nBase / sz) + "\t" + (nNeck / sz) + "\t" + (nCup / sz) + "\t" + "v2.6." + i);
		}
	}
}
