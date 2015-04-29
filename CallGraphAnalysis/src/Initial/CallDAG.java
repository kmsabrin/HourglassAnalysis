package Initial;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class CallDAG {
	int nEdges;
	double nSources;
	double nTargets;
	double nTotalPath;

	Set<String> functions;
	Map<String, Set<String>> callFrom; // who called me i.e reverse adjacency list
	Map<String, Set<String>> callTo; // who I called i.e adjacency list

	Map<String, Double> numOfPath;
	Map<String, Double> sumOfPath;
	Map<String, Double> avgTargetDepth;
	Map<String, Double> avgSourceDepth;
	Map<String, Double> location;
	
	Map<String, Double> numOfTargetPath;
	Map<String, Double> numOfSourcePath;
	
	Map<String, Double> numOfReachableNodes;
	Map<String, Double> generality;
	Map<String, Double> complexity;
	
	/* ASSUMING EACH ROOT IS A STARTING A MODULE 
	 * GENERALITY IS DEFINED AS HOW MANY MODULES 
	 * ARE USING A NODE AND SIMILARLY FOR CMPLXT
	 */
	Map<String, Double> moduleGenerality; 
	Map<String, Double> moduleComplexity;
	Map<String, Double> sourcesReached; 
	Map<String, Double> targetsReached;
	
	Map<String, Integer> outDegree;
	Map<String, Integer> inDegree;

	Set<String> visited;
	ArrayList<String> visitedOrdered;
	Map<String, String> cycleEdges;
	Set<String> cycleVisited;
	List<String> cycleList;
	double reachableKount;
	double moduleKount;
		
	Map<String, Integer> functionID;
	Map<Integer, String> IDFunction;
	
	HashMap<String, Double> centrality;
	HashMap<String, Double> nodePathThrough;
	
	
	HashMap<String, Double> prCentrality;
	HashMap<String, Double> prFromSource;
	HashMap<String, Double> prToTarget;
	
	ArrayList<ArrayList<String>> detectedCycles;
	
	Map<String, Set<String>> nodesReachableUpwards;
	Map<String, Set<String>> nodesReachableDownwards;
	
	HashSet<String> thresholdedCore;
	
	CallDAG() { 
		functions = new TreeSet<String>();
		callFrom = new HashMap<String, Set<String>>();
		callTo = new HashMap<String, Set<String>>();
		
		cycleEdges = new HashMap<String, String>();
		
		numOfPath = new HashMap<String, Double>();
		sumOfPath = new HashMap<String, Double>();
		avgTargetDepth = new HashMap<String, Double>();
		avgSourceDepth = new HashMap<String, Double>();
		location = new HashMap<String, Double>();
		
		numOfTargetPath = new HashMap<String, Double>();
		numOfSourcePath = new HashMap<String, Double>();
		
		numOfReachableNodes = new HashMap<String, Double>();
		generality = new HashMap<String, Double>();
		complexity = new HashMap<String, Double>();
		moduleGenerality = new HashMap<String, Double>();
		moduleComplexity = new HashMap<String, Double>();
		sourcesReached = new HashMap<String, Double>();
		targetsReached = new HashMap<String, Double>();
		
		outDegree = new HashMap<String, Integer>();
		inDegree = new HashMap<String, Integer>();
		
		functionID = new HashMap<String, Integer>();
		IDFunction = new HashMap<Integer, String>();
		
		centrality = new HashMap<String, Double>();
		nodePathThrough = new HashMap<String, Double>();
		
		prCentrality = new HashMap<String, Double>();
		prFromSource = new HashMap<String, Double>();
		prToTarget = new HashMap<String, Double>();
		
		detectedCycles = new ArrayList<ArrayList<String>>();
		
		nodesReachableUpwards = new HashMap<String, Set<String>>();
		nodesReachableDownwards = new HashMap<String, Set<String>>();
		
		thresholdedCore = new HashSet<String>();
	}
	
	CallDAG(String callGraphFileName) {
		this();
			
		// load & initialize the attributes of the call graph
		loadCallGraph(callGraphFileName);

		removeCycles(); // or should I only ignore cycles?
		
//		assignFunctionID();
		
		loadDegreeMetric();
		
//		System.out.println(callFrom.get("do_lcall"));
//		System.out.println(callFrom.get("lcall27"));
//		System.out.println(callTo.get("do_lcall"));
//		System.out.println(callTo.get("lcall27"));
		
		loadLocationMetric(); // must load degree metric before
//		loadGeneralityMetric(); //CAREFUL 
//		loadComplexityMetric(); //CAREFUL
		loadCentralityMetric();
//		loadPagerankCentralityMetric();
		
		// for randomization
//		loadRechablity();
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
//					String callT = tokens[2].substring(0, tokens[2].length()); // for cdepn
					
//					String callT = tokens[2].substring(0, tokens[2].length()); // for bio nets
					
//					for running community detection on random DAGs
//					String callT = tokens[2]; 
			
					/******************/
					/******************/
					if (callT.equals("mcount"))  // no more location metric!
						continue;
					
					
						
					/******************/
					/******************/
					
					functions.add(callF);
					functions.add(callT);
					
					if (callF.equals(callT)) { // loop, do not add the edge
//						System.out.println(callF);
						continue;
					}
					
					if (callFrom.containsKey(callT)) {
						callFrom.get(callT).add(callF);
					} else {
						Set<String> l = new HashSet<String>();
						l.add(callF);
						callFrom.put(callT, l);
					}

					if (callTo.containsKey(callF)) {
						callTo.get(callF).add(callT);
					} else {
						Set<String> l = new HashSet<String>();
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
		cycleList.add(node);
		
		for (String s : callTo.get(node)) {
			if (cycleVisited.contains(s)) {
//				cycle found, recording edge for removal
				cycleEdges.put(node, s);
				for (int i = cycleList.size() - 1; ; --i) {
					if (cycleList.get(i).equals(s)) {
//						System.out.println(cycleList.size() - i);
						detectedCycles.add(new ArrayList<String>(cycleList.subList(i, cycleList.size())));
						break;
					}
				}
				
				continue;
			}
			removeCyclesTraverse(s);
		}

		visited.add(node);
		cycleVisited.remove(node);
		cycleList.remove(node);
	}

	public void removeCycles() {
//		go through roots
		boolean loop = true;
		int k = 0;
		while (loop) {
			visited = new HashSet<String>();
			loop = false;
			for (String s : functions) {
//				if (!visited.contains(s)) {
				if (!callFrom.containsKey(s)) { // start from roots only
					cycleEdges = new HashMap<String, String>();
					cycleVisited = new HashSet<String>();
					cycleList = new ArrayList<String>();
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
			
			for (String s : functions) { // cycles involving roots
				if (!visited.contains(s)) {
					cycleEdges = new HashMap<String, String>();
					cycleVisited = new HashSet<String>();
					cycleList = new ArrayList<String>();
					removeCyclesTraverse(s);
					
//					if (s.equals("do_lcall")) {
//						System.out.println("Traversing " + s);
//						System.out.println(cycleEdges);
//					}
					
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
//			System.out.println("Degree: " + s + " in: " + in + " out: " + out);
		}
		
		nSources = nTargets = nEdges = 0;
		for (String s: functions) {
			if (!callFrom.containsKey(s)) ++nSources;
			if (!callTo.containsKey(s)) {
				++nTargets;
			}
			else {
				nEdges += callTo.get(s).size();
			}
		}
	}
	
	public void assignFunctionID() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter("Results/index.txt");
		} catch(Exception e) {}
		
		int k = 0;
		for (String s: functions) {
			pw.println(k + "\t" + k);
			functionID.put(s, k);
			IDFunction.put(k, s);
			++k;
		}
		
		pw.close();
	}
	
	public void targetPath(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!callTo.containsKey(node)) { // is leaf
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgTargetDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		for (String s: callTo.get(node)) {
				targetPath(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	public void sourcePath(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!callFrom.containsKey(node)) { // is root
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgSourceDepth.put(node, 0.0);
			return;
		}
				
		visitedOrdered.add(node);
		
		double nPath = 0;
		double sPath = 0;
		for (String s: callFrom.get(node)) {
				if (visitedOrdered.contains(s)) {
					System.out.print("cycle found: ");
					for (int i = visitedOrdered.size() - 1; ; --i) {
						System.out.print("\t" + visitedOrdered.get(i));
						if (visitedOrdered.get(i).equals(s)) {
							break;
						}
					}
					
					System.out.println();
//					continue;
				}
			
				sourcePath(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
		
		visitedOrdered.remove(node);
	}
	
	public void loadLocationMetric() {
//		go through roots
		for (String s: functions) {
			if (!callFrom.containsKey(s)) {
				targetPath(s);
			}
		}
		
		numOfTargetPath.putAll(numOfPath);
		
//		reset data containers
		numOfPath = new HashMap<String, Double>();
		sumOfPath = new HashMap<String, Double>();
//		go through leaves
		for (String s: functions) {
			if (!callTo.containsKey(s)) {
				visited = new HashSet<String>();
				visitedOrdered = new ArrayList<String>();
				sourcePath(s);
			}
		}
		
		numOfSourcePath.putAll(numOfPath);
		
		for (String s : functions) {
			double m = avgTargetDepth.get(s) / (avgTargetDepth.get(s) + avgSourceDepth.get(s));
			m = ((int) (m * 100.0)) / 100.0; // round up to 2 decimal point
			location.put(s, m);
		}		
	}
		
	public void reachableUpwardsNodes(String node) { // towards root
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		++reachableKount;
				
		if (!callFrom.containsKey(node)) { // is a root
			++moduleKount;
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
			reachableKount = -1.0; // for excluding itself, note kount is global
			moduleKount = 0;
			visited = new HashSet<String>();
			reachableUpwardsNodes(s); // how many nodes are using her
			
			double g = 0;
			int loc = Math.max((int)(location.get(s) * 100), 0);
			if (loc < 100) {
				g = reachableKount / greaterLocNode[loc];
			}
			
			g = ((int) (g * 100.0)) / 100.0;
			generality.put(s, g);
			
			sourcesReached.put(s, moduleKount);
			double mG = moduleKount / nSources;
			mG = ((int) (mG * 100.0)) / 100.0;
			moduleGenerality.put(s, mG);
		}
	}
	
	public void reachableDownwardsNodes(String node) { // towards leaves
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		
		if (!callTo.containsKey(node)) { // is a leaf
			return;
		}
		
		for (String s : callTo.get(node)) {
			reachableDownwardsNodes(s);
		}
	}
	
	public void reachableDownwardsNodes(String node, String source, PrintWriter pw) { // towards leaves
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		++reachableKount;
		
		if (!node.equals(source)) {
			pw.println(functionID.get(source) + " " + functionID.get(node));
//			System.out.println("Here");
		}
			
		if (!callTo.containsKey(node)) { // is a leaf
			++moduleKount;
			return;
		}
		
		for (String s : callTo.get(node)) {
			reachableDownwardsNodes(s, source, pw);
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
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("Results//complete-DAG.txt"));
		}catch (Exception e) {}

		for (String s : functions) {
			reachableKount = -1.0; // for excluding itself
			moduleKount = 0;
			visited = new HashSet<String>();
			reachableDownwardsNodes(s, s, pw);
			
			int loc = (int)(location.get(s) * 100);
			double c = 0;
			if (loc != 0) {
				c = reachableKount / lessLocNode[loc];
			}
			c = ((int) (c * 100.0)) / 100.0;
			complexity.put(s, c);
			
			targetsReached.put(s, moduleKount);
			double mC = moduleKount / nTargets;
			mC = ((int) (mC * 100.0)) / 100.0;
			moduleComplexity.put(s, mC);
		}
		
		pw.close();
	}
	
	public void loadRechablity() {
		visited = new HashSet<String>();
		for (String s : functions) {
			visited.clear();
			reachableUpwardsNodes(s); // how many nodes are using her
			visited.remove(s); // remove ifself
			nodesReachableUpwards.put(s, new HashSet<String>(visited));
			
			visited.clear();
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
			nodesReachableDownwards.put(s, new HashSet<String>(visited));
		}
	}
	
	public void resetAuxiliary() {
		nTotalPath = 0;
		cycleEdges = new HashMap<String, String>();
		
		numOfPath = new HashMap<String, Double>();
		sumOfPath = new HashMap<String, Double>();
		avgTargetDepth = new HashMap<String, Double>();
		avgSourceDepth = new HashMap<String, Double>();
		location = new HashMap<String, Double>();
		
		numOfTargetPath = new HashMap<String, Double>();
		numOfSourcePath = new HashMap<String, Double>();
		
		numOfReachableNodes = new HashMap<String, Double>();
		generality = new HashMap<String, Double>();
		complexity = new HashMap<String, Double>();
		moduleGenerality = new HashMap<String, Double>();
		moduleComplexity = new HashMap<String, Double>();
		sourcesReached = new HashMap<String, Double>();
		targetsReached = new HashMap<String, Double>();
		
		outDegree = new HashMap<String, Integer>();
		inDegree = new HashMap<String, Integer>();
		
		functionID = new HashMap<String, Integer>();
		IDFunction = new HashMap<Integer, String>();
		
		centrality = new HashMap<String, Double>();
		nodePathThrough = new HashMap<String, Double>();
		
		nSources = nTargets = 0;
		for (String s: functions) {
			if (!callFrom.containsKey(s)) ++nSources;
			if (!callTo.containsKey(s)) ++nTargets;
		}
	}
	
	public void loadCentralityMetric() {
		nTotalPath = 0;
		for (String s: location.keySet()) {
			double nPath = 1;
			
//			P-Centrality
			nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
			nodePathThrough.put(s, nPath);
			if (!callFrom.containsKey(s)) { // is a root
				nTotalPath += nPath;
			}
			
//			I-Centrality
//			nPath = rootsReached.get(s) * leavesReached.get(s);
//			nodePathThrough.put(s, nPath); // equivalent to number of connected (t,b) pairs containing it
//			if (!callFrom.containsKey(s)) { // is a root
//				nTotalPath += nPath; // nTotalPath = nConnectedTopBottomPair
//			}
		}
		
		for (String s: functions) {
			double cntr = nodePathThrough.get(s) / nTotalPath;
//			cntr = ((int) (cntr * 1000.0)) / 1000.0;

			centrality.put(s, cntr);
//			centrality.put(s, nodePathThrough.get(s)); // non-normalized
			
			if (cntr > 0.7) {
//				System.out.println(s + "\t" + cntr + "\t" + numOfLeafPath.get(s) + "\t" + numOfRootPath.get(s));
				thresholdedCore.add(s);
			}
			
//			System.out.println(s + "\t" + cntr + "\t" + location.get(s) + "\t" + inDegree.get(s) + "\t" + outDegree.get(s));
//			System.out.println(s + "\t" + cntr);
		}		
		
		System.out.println("Total Paths: " + nTotalPath + "\n" + "####################");
		
	}

	private void recursePagerankTargetToSource(String node) {
		if (prToTarget.containsKey(node)) {
			return;
		}

		double nodePRCentrality = 0;
		for (String s : callTo.get(node)) {
			recursePagerankTargetToSource(s);
			nodePRCentrality += prToTarget.get(s) / inDegree.get(s);
		}

		prToTarget.put(node, nodePRCentrality);
	}

	private void recursePagerankSourceToTarget(String node) {
		if (prFromSource.containsKey(node)) {
			return;
		}
		
		double nodePRCentrality = 0;
		for (String s: callFrom.get(node)) {
			recursePagerankSourceToTarget(s);
			nodePRCentrality += prFromSource.get(s) / outDegree.get(s);
		}
		
		prFromSource.put(node, nodePRCentrality);
	}
	
	public void loadPagerankCentralityMetric() {
		// initialize source pr
		for (String s: functions) {
			if (!callFrom.containsKey(s)) {
				prFromSource.put(s, 1.0);
			}
		}
		
		for (String s: functions) {
			recursePagerankSourceToTarget(s);
		}
		
		// initialize target pr
		for (String s : functions) {
			if (!callTo.containsKey(s)) {
				prToTarget.put(s, 1.0);
			}
		}

		for (String s : functions) {
			recursePagerankTargetToSource(s);
		}
		
		for (String s: functions) {
//			prCentrality.put(s, prFromSource.get(s) * prToTarget.get(s));
//			prCentrality.put(s, prFromSource.get(s));
			prCentrality.put(s, prToTarget.get(s));
		}

		for (String s: functions) {
			System.out.println(s + "\t" + prToTarget.get(s) + "\t" + location.get(s));
//			System.out.println(s + "\t" + prCentrality.get(s) + "\t" + prFromSource.get(s) + "\t" + prToTarget.get(s));
		}
		
		System.out.println("###### ###### ######");
	}

	public void removeIsolatedNodes() {
		HashSet<String> removable = new HashSet<String>();
		for (String s : functions) {
			if (!callTo.containsKey(s) && !callFrom.containsKey(s)) {
				removable.add(s);
			}
		}
		
		functions.removeAll(removable);
	}
	
	public void printCallDAG() {
		for (String s: functions) {
			if (callTo.containsKey(s)) {
				System.out.print(s + " calling: ");
				for (String r : callTo.get(s)) {
					System.out.print(r + ", ");
				}
				System.out.println();
			}
		}
		System.out.println("#####################");
	}
}
