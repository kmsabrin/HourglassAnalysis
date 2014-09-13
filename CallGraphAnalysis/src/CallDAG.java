import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class CallDAG {
	int nEdges;
	Set<String> functions;
	Map<String, Set<String>> callFrom; // who called me // reverse adjacency list
	Map<String, Set<String>> callTo; // who I called // adjacency list

	Map<String, Double> numOfPath;
	Map<String, Double> sumOfPath;
	Map<String, Double> avgLeafDepth;
	Map<String, Double> avgRootDepth;
	Map<String, Double> location;
	
	Map<String, Double> numOfReachableNodes;
	Map<String, Double> generality;
	Map<String, Double> complexity;
	
	Map<String, Integer> outDegree;
	Map<String, Integer> inDegree;

	Set<String> visited;
	Map<String, String> cycleEdges;
	Set<String> cycleVisited;
	double kount;
	
	CallDAG() { // for test graph
		functions = new HashSet();
		callFrom = new HashMap();
		callTo = new HashMap();
		
		cycleEdges = new HashMap();
		
		numOfPath = new HashMap();
		sumOfPath = new HashMap();
		avgLeafDepth = new HashMap();
		avgRootDepth = new HashMap();
		location = new HashMap();
		
		numOfReachableNodes = new HashMap();
		generality = new HashMap();
		complexity = new HashMap();
		
		outDegree = new HashMap();
		inDegree = new HashMap();
	}
	
	CallDAG(String callGraphFileName) {
		functions = new HashSet();
		callFrom = new HashMap();
		callTo = new HashMap();
		
		cycleEdges = new HashMap();
		
		numOfPath = new HashMap();
		sumOfPath = new HashMap();
		avgLeafDepth = new HashMap();
		avgRootDepth = new HashMap();
		location = new HashMap();
		
		numOfReachableNodes = new HashMap();
		generality = new HashMap();
		complexity = new HashMap();
		
		outDegree = new HashMap();
		inDegree = new HashMap();
		
		// load & initialize the attributes of the call graph
		loadCallGraph(callGraphFileName);
		removeCycles(); // or should I only ignore cycles?
		loadDegreeMetric();
		loadLocationMetric(); // must load degree metric before
		loadGeneralityMetric(); 
		loadComplexityMetric();
	}

	public void loadCallGraph(String fileName) {
		try {
			Scanner scanner = new Scanner(new File(fileName));

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String tokens[] = line.split("\\s+");
				if (tokens.length < 2)
					continue;

				if (tokens[1].equals("->")) {
					String callF = tokens[0];
					String callT = tokens[2].substring(0, tokens[2].length() - 1); // for cobjdump
					
					/******************/
					/******************/
					if (callT.equals("mcount")) 
						continue;
					
					if (callF.equals(callT)) // loop
						continue;
					/******************/
					/******************/
					
					++nEdges;
					functions.add(callF);
					functions.add(callT);
					
					if (callFrom.containsKey(callT)) {
						callFrom.get(callT).add(callF);
					} else {
						Set<String> l = new HashSet();
						l.add(callF);
						callFrom.put(callT, l);
					}

					if (callTo.containsKey(callF)) {
						callTo.get(callF).add(callT);
					} else {
						Set<String> l = new HashSet();
						l.add(callT);
						callTo.put(callF, l);
					}
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeCyclesTraverse(String node) {
		if (!callTo.containsKey(node) || visited.contains(node))
			return;

		cycleVisited.add(node); // cycle check

		for (String s : callTo.get(node)) {
			if (cycleVisited.contains(s)) {
//				cycle found, recording edge for removal
				cycleEdges.put(node, s);
				continue;
			}
			removeCyclesTraverse(s);
		}

		visited.add(node);
		cycleVisited.remove(node);
	}

	public void removeCycles() {
//		go through roots
		boolean loop = true;
		int k = 0;
		while (loop) {
			visited = new HashSet();
			loop = false;
			for (String s : functions) {
				if (!visited.contains(s)) {
					cycleEdges = new HashMap();
					cycleVisited = new HashSet();
					removeCyclesTraverse(s);
					
					for (String source : cycleEdges.keySet()) {
						String target = cycleEdges.get(source);
						callTo.get(source).remove(target);
						callFrom.get(target).remove(source);
						if (callTo.get(source).size() < 1) callTo.remove(source);
						if (callFrom.get(target).size() < 1) callFrom.remove(target);
						if (!callTo.containsKey(source) && !callFrom.containsKey(source)) functions.remove(source);
						if (!callTo.containsKey(target) && !callFrom.containsKey(target)) functions.remove(target);
						--nEdges;
						++k;
						loop = true;
					}
				}
			}
		}
//		System.out.println(k + " cycle edges removed!");
	}
	
	public void loadDegreeMetric() {
//		get the fanIn/Out
		for (String s : functions) {
			int in = 0;
			int out = 0;
			
			if (callTo.containsKey(s)) {
				out = callTo.get(s).size();
			}
			
			if (callFrom.containsKey(s)) {
				in = callFrom.get(s).size();
			}
			
			outDegree.put(s, out);
			inDegree.put(s, in);
		}		
	}
	
	public void leafPath(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!callTo.containsKey(node)) { // is leaf
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgLeafDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		for (String s: callTo.get(node)) {
				leafPath(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgLeafDepth.put(node, sPath / nPath);
	}
	
	public void rootPath(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!callFrom.containsKey(node)) { // is root
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgRootDepth.put(node, 0.0);
			return;
		}
				
		visited.add(node);
		
		double nPath = 0;
		double sPath = 0;
		for (String s: callFrom.get(node)) {
				rootPath(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgRootDepth.put(node, sPath / nPath);
		
		visited.remove(node);
	}
	
	public void loadLocationMetric() {
//		go through roots
		for (String s: functions) {
			if (!callFrom.containsKey(s)) {
				leafPath(s);
			}
		}
		
//		reset data containers
		numOfPath = new HashMap();
		sumOfPath = new HashMap();
//		go through leaves
		for (String s: functions) {
			if (!callTo.containsKey(s)) {
				visited = new HashSet();
				rootPath(s);
			}
		}
		
		for (String s : functions) {
			double m = avgLeafDepth.get(s) / (avgLeafDepth.get(s) + avgRootDepth.get(s));
			m = ((int) (m * 100.0)) / 100.0; // round up to 2 decimal point
			location.put(s, m);
		}		
	}
		
	public void reachableUpwardsNodes(String node) { // towards root
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		++kount;
		
		if (!callFrom.containsKey(node)) { // is a root
			return;
		}
		
		for (String s : callFrom.get(node)) {
			reachableUpwardsNodes(s);
		}
	}

	public void loadGeneralityMetric() {
		int greaterLocNode[] = new int[110]; // how many nodes above a location
		int locCount[] = new int[110]; // how many nodes in a location
		
		for (String s: functions) {
			int loc = (int)(location.get(s) * 100);
			locCount[loc]++;
		}
		
		greaterLocNode[100] = 0;
		for (int i = 99; i >= 0; --i) {
			greaterLocNode[i] = greaterLocNode[i + 1] + locCount[i + 1];
		}
		
		for (String s : functions) {
			kount = -1.0; // for excluding itself, note kount is global
			visited = new HashSet();
			reachableUpwardsNodes(s); // how many nodes are using her
			
			double g = 0;
			int loc = Math.max((int)(location.get(s) * 100), 0);
			if (loc < 100) {
				g = kount / greaterLocNode[loc];
			}
			
			g = ((int) (g * 1000.0)) / 1000.0;
			generality.put(s, g);
		}
	}
	
	public void reachableDownwardsNodes(String node) { // towards leaves
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		++kount;
		
		if (!callTo.containsKey(node)) { // is a leaf
			return;
		}
		
		for (String s : callTo.get(node)) {
			reachableDownwardsNodes(s);
		}
	}
	
	public void loadComplexityMetric() {
		int lessLocNode[] = new int[110];
		int locCount[] = new int[110];
		
		for (String s: functions) {
			int loc = (int)(location.get(s) * 100);
			locCount[loc]++;
		}
		
		lessLocNode[0] = 0;
		for (int i = 1; i <= 100; ++i) {
			lessLocNode[i] = lessLocNode[i - 1] + locCount[i - 1];
		}
		
		for (String s : functions) {
			kount = -1.0; // for excluding itself
			visited = new HashSet();
			reachableDownwardsNodes(s);
			
			int loc = (int)(location.get(s) * 100);
			double c = 0;
			if (loc != 0) {
				c = kount / lessLocNode[loc];
			}
			c = ((int) (c * 1000.0)) / 1000.0;
			complexity.put(s, c);
		}
	}
}
