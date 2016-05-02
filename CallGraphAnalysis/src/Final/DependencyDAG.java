package Final;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class DependencyDAG {
	int nEdges;
	double nSources; // source =  simple module, zero in degree, depends on none
	double nTargets; // target = complex module, zero out degree, serves none (or external api)

	public Set<String> nodes;
	Map<String, Set<String>> serves; 
	Map<String, Set<String>> depends; 
	
	HashSet<String> targets;
	HashSet<String> sources;
	
	Map<String, Double> numOfTargetPath;
	Map<String, Double> sumOfTargetPath;
	Map<String, Double> numOfSourcePath;
	Map<String, Double> sumOfSourcePath;	
	Map<String, Double> avgTargetDepth;
	Map<String, Double> avgSourceDepth;
	Map<String, Double> location;
	
	double nTotalPath;
	HashMap<String, Double> nodePathThrough;
	HashMap<String, Double> geometricMeanPathCentrality;
	HashMap<String, Double> harmonicMeanPathCentrality;
	HashMap<String, Double> normalizedPathCentrality;
	HashMap<String, Integer> centralityRank;
	
	HashMap<String, Double> iCentrality;
	
	Map<String, Integer> outDegree;
	Map<String, Integer> inDegree;

	Set<String> visited;
	Map<String, String> cycleEdges;
	Set<String> cycleVisited;
	List<String> cycleList;
	ArrayList<ArrayList<String>> detectedCycles;

	HashMap<String, Set<String>> dependentsReachable;
	HashMap<String, Set<String>> serversReachable;
	HashMap<String, Integer> targetsReachable; // for iCentrality
	HashMap<String, Integer> sourcesReachable; // for iCentrality
	HashMap<String, HashSet<String>> nodesReachable;
	
	HashMap<String, Double> pagerankSourceCompression; // for prCentrality
	HashMap<String, Double> pagerankTargetCompression; // for prCentrality
	HashMap<String, Double> geometricMeanPagerankCentrality;
	HashMap<String, Double> harmonicMeanPagerankCentrality;
		
	String dependencyGraphID;
	
	int kounter;
	
	boolean canReachTarget;
	boolean canReachSource;
	
	static boolean isSynthetic = false;
	static boolean isCallgraph = false;
	static boolean isMetabolic = false;
	static boolean isCourtcase = false;
	static boolean isToy = false;
	static boolean isClassDependency = false;
	static boolean isSimpleModel = false;
	static boolean isComplexModel = false;
	static boolean isWeighted = false;	
	static boolean isRandomized = false;
	
	static int nDirectSourceTargetEdges = 0;
	
	static HashSet<String> largestWCCNodes;
	
	HashMap<String, Integer> edgeWeights;
	
	DependencyDAG() { 
		nodes = new TreeSet();
		serves = new HashMap();
		depends = new HashMap();
		
		targets = new HashSet();
		sources = new HashSet();
				
		numOfTargetPath = new HashMap();
		sumOfTargetPath = new HashMap();
		avgTargetDepth = new HashMap();
		numOfSourcePath = new HashMap();
		sumOfSourcePath = new HashMap();
		avgSourceDepth = new HashMap();
		location = new HashMap();

		nodePathThrough = new HashMap();
		geometricMeanPathCentrality = new HashMap();
		harmonicMeanPathCentrality = new HashMap();
		normalizedPathCentrality = new HashMap();
		centralityRank = new HashMap();
		
		iCentrality = new HashMap();
		
		outDegree = new HashMap();
		inDegree = new HashMap();
		
		geometricMeanPagerankCentrality = new HashMap();
		harmonicMeanPagerankCentrality = new HashMap();
		pagerankSourceCompression = new HashMap();
		pagerankTargetCompression = new HashMap();
		
		detectedCycles = new ArrayList();
		cycleEdges = new HashMap();

		dependentsReachable = new HashMap();
		serversReachable = new HashMap();
		
		targetsReachable = new HashMap();
		sourcesReachable = new HashMap();
		
		nodesReachable = new HashMap();
	
//		largestWCCNodes = new HashSet();
		visited = new HashSet();
		
		CoreDetection.topRemovedWaistNodes.clear();
		
		edgeWeights = new HashMap();
	}
	
	DependencyDAG(String dependencyGraphID) throws Exception {
		this();
		
		this.dependencyGraphID = dependencyGraphID;
			
		// load & initialize the attributes of the dependency graph
		loadCallGraph(dependencyGraphID);
		
		if (isCallgraph || isClassDependency || isToy || isMetabolic || isCourtcase) {
			removeCycles(); // or should I only ignore cycles?
		}
		
		if (isSynthetic) {
//			removeDisconnectedNodesForSyntheticNetworks();
		}
		
		removeIsolatedNodes();
		
		loadDegreeMetric();
		
		loadPathStatistics();
		
		loadLocationMetric(); // must load degree metric before
		
//		loadReachablityAll();		
		loadServerReachabilityAll();
		
		loadPathCentralityMetric();
//		loadPagerankCentralityMetric();		
		
//		DistributionAnalysis.rankNodeByCentrality(this, this.normalizedPathCentrality);
	}
	
	private void checkTargetReachability(String node) {
		if (isTarget(node)) {
			canReachTarget = true;
			return;
		}
		
		if (canReachTarget) return;
		
		if (serves.containsKey(node)) {
			for (String s : serves.get(node)) {
				checkTargetReachability(s);
			}
		}
	}
	
	private void checkSourceReachability(String node) {
		if (isSource(node)) {
			canReachSource = true;
			return;
		}
		if (canReachSource) return;
		
		if (depends.containsKey(node)) {
			for (String s : depends.get(node)) {
				checkSourceReachability(s);
			}
		}
	}
	
	public void checkReach(String node) {
		canReachTarget = false;
		canReachSource = false;
		checkTargetReachability(node);
		checkSourceReachability(node);
	}
	
	private void removeNode(String node) {
//		System.out.println("Removing: " + node);
		
		nodes.remove(node);
		
		if (serves.containsKey(node)) {
			for (String s: serves.get(node)) {
				depends.get(s).remove(node);
			}
			serves.remove(node);
		}
		
		if (depends.containsKey(node)) {
			for (String s: depends.get(node)) {
				serves.get(s).remove(node);
			}
			depends.remove(node);
		}
	}
	
	private void removeDisconnectedNodesForSyntheticNetworks() {
		HashSet<String> tempFunctions = new HashSet(nodes);
		for (String s: tempFunctions) {
			int index = Integer.parseInt(s);
//			System.out.println("Is " + index + " disconnected?");
//			System.out.println("If in between " + ComplexModelDAG.layerEndNode[0] + " and " + ComplexModelDAG.layerStartNode[ComplexModelDAG.nLayers - 1]);
			if (isIntermediate(s) 
				/*|| (isComplexModel && index > ComplexModelDAG.layerEndNode[0] && index < ComplexModelDAG.layerStartNode[ComplexModelDAG.nLayers - 1])*/) { // so bad, so so bad
				checkReach(s);
//				System.out.println("Checking reach of " + s);
				if (!canReachTarget || !canReachSource) {
					removeNode(s);
//					System.out.println("YES!");
				}
			}
		}
	}

	public void loadCallGraph(String fileName) throws Exception {
		Scanner scanner = new Scanner(new File(fileName));

		int violation = 0;
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String tokens[] = line.split("\\s+");
			if (tokens.length < 2) {
				continue;
			}

			String server = "", dependent = "";

			if (isCallgraph) {
				if (tokens[1].equals("->")) {
					dependent = tokens[0].substring(0, tokens[0].length());
					server = tokens[2].substring(0, tokens[2].length() - 1); // for cobjdump: a-> b;
					// String server = tokens[2].substring(0, tokens[2].length()); // for cdepn: a -> b
				}
				
//				else {
//					// for scc-consolidated graph
//					dependent = tokens[0].substring(0, tokens[0].length());
//					server = tokens[1].substring(0, tokens[1].length()); // for scc-consolidation: a b
//				}
				
				if (dependent.equals("do_log") || server.equals("do_log")
					|| dependent.equals("main") || server.equals("main")	
					|| dependent.equals("_exit@plt") || server.equals("_exit@plt")
//					|| dependent.equals("do_exec") || server.equals("do_exec")
						) { 
					// no more location metric noise! 
					// compiler generated
					continue;
				}
				
//				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
//					continue;
//				}
			}
			else if (isClassDependency) {
				// for metabolic and synthetic networks
				server = tokens[0];
				dependent = tokens[1];
				
//				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
//					continue;
//				}
			}
			else if (isSynthetic || isToy) {
				if (isWeighted) {
					server = tokens[0];
					dependent = tokens[1];
					int weight = Integer.parseInt(tokens[2]);
					if (weight > 1) {
						edgeWeights.put(server + "#" + dependent, weight);
					}
				} 
				else {
					server = tokens[0];
					dependent = tokens[1];
				}
			}
			else if (isMetabolic) {
				// for metabolic and synthetic networks
				server = tokens[0];
				dependent = tokens[1];
				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
					continue;
				}
			}
			else if (isCourtcase) {
				server = tokens[1];
				dependent = tokens[0]; // for space separated DAG: a b or a,b
				
//				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
//					continue;
//				}
				
//				if (CourtCaseCornellParser.caseIDs.contains(server) == false || CourtCaseCornellParser.caseIDs.contains(dependent) == false) {
//					continue;
//				}
				if (CourtCaseCornellParser.caseIDs.contains(server) == false && CourtCaseCornellParser.caseIDs.contains(dependent) == false) {
					continue;
				}
//				if (!CourtCaseCornellParser.caseIDs.contains(dependent)) continue;
				
				if (Integer.parseInt(dependent) < Integer.parseInt(server)) { // for court cases
				// System.out.println(dependent + " citing " + server); // cycle
					++violation;
					continue;
				}
			}

			nodes.add(dependent);
			nodes.add(server);

			if (dependent.equals(server)) { // loop, do not add the edge
				continue;
			}

			if (serves.containsKey(server)) {
				serves.get(server).add(dependent);
			} else {
				HashSet<String> hs = new HashSet();
				hs.add(dependent);
				serves.put(server, hs);
			}

			if (depends.containsKey(dependent)) {
				depends.get(dependent).add(server);
			} else {
				HashSet<String> hs = new HashSet();
				hs.add(server);
				depends.put(dependent, hs);
			}
		}

//		System.out.println(violation);
		
//		if (isCourtcase) {
////			fix Leaves
//			HashSet<String> toRemove = new HashSet();
//			for (String s: nodes) {
//				if(!depends.containsKey(s) && serves.containsKey(s) && CourtCaseCornellParser.caseIDs.contains(s)) {
////					HashSet<String> hs = new HashSet();
////					hs.add("-1");
////					depends.put(s, hs);
//					toRemove.add(s);
//				}
//				else if (depends.containsKey(s) && !serves.containsKey(s) && CourtCaseCornellParser.caseIDs.contains(s)) {
//					toRemove.add(s);
//				}
//			}
//			
//			for (String s: toRemove) {
//				removeNode(s);
//			}
//		}
	}
		
	public void removeIsolatedNodes() {
		HashSet<String> removable = new HashSet<String>();
		for (String s: nodes) {
			if (!serves.containsKey(s) && !depends.containsKey(s)) {
				removable.add(s);
			}
		}
		nodes.removeAll(removable);
	}
	
	private void removeCyclesTraverse(String node) {
		if (!serves.containsKey(node) || visited.contains(node)) {
			return;
		}

		cycleVisited.add(node); // cycle check
		cycleList.add(node);
		
		for (String s : serves.get(node)) {
			if (cycleVisited.contains(s)) {
//				cycle found, recording edge for removal
				cycleEdges.put(node, s);
				
				for (int i = cycleList.size() - 1; ; --i) {
					if (cycleList.get(i).equals(s)) {
//						System.out.println(cycleList.size() - i);
						detectedCycles.add(new ArrayList(cycleList.subList(i, cycleList.size())));
						
						for (String r: detectedCycles.get(detectedCycles.size() - 1)) {
//							System.out.print(r + " ");
						}
//						System.out.println();
//						System.out.println(detectedCycles.get(detectedCycles.size() - 1).size());
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
		// go through sources
		boolean loop = true;
		int nCycles = 0;
		while (loop) {
			loop = false;
			for (String s : nodes) {
				visited = new HashSet();
				cycleEdges = new HashMap();
				cycleVisited = new HashSet();
				cycleList = new ArrayList();
				removeCyclesTraverse(s);

				for (String server : cycleEdges.keySet()) {
					String dependent = cycleEdges.get(server);
					depends.get(dependent).remove(server);
					serves.get(server).remove(dependent);
					if (depends.get(dependent).size() < 1) {
						depends.remove(dependent);
					}
					if (serves.get(server).size() < 1) {
						serves.remove(server);
					}
					if (!depends.containsKey(server) && !serves.containsKey(server)) {
						nodes.remove(server);
					}
					if (!depends.containsKey(dependent) && !serves.containsKey(dependent)) {
						nodes.remove(dependent);
					}
					--nEdges;
					++nCycles;
//					loop = true;
//					System.out.println("cycle found");
				}
			}
		}

		// System.out.println(k + " cycle edges removed!");
	}
	
	public void loadDegreeMetric() {
		if (isRandomized == false) {
			for (String s : nodes) {
				if (isTarget(s)) {
					targets.add(s);
				}
				if (isSource(s)) {
					sources.add(s);
				}
			}
		}
		
//		get the fanIn/Out
		for (String s : nodes) {
			int in = 0;
			int out = 0;
			
			if (isTarget(s) || isIntermediate(s)) {
				in = depends.get(s).size();
			}
			
			if (isSource(s) || isIntermediate(s)) {
				if (isSynthetic) {
					if (serves.containsKey(s)) {
						out = serves.get(s).size();
					}
				}
				else {
					out = serves.get(s).size();
				}
			}
			
			outDegree.put(s, out);
			inDegree.put(s, in);
		}
		
		nSources = nTargets = nEdges = 0;
		for (String s: nodes) {
			if (isTarget(s)) {
				++nTargets;
			}
			if (isSource(s)) {
				++nSources;
			}
			else {
				nEdges += depends.get(s).size();
			}
		}
	}
	
	public boolean isSource(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) >= SimpleModelDAG.sS) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return sources.contains(node);
		}
		else {
			return !depends.containsKey(node);
		}
	}
	
	public boolean isTarget(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) < SimpleModelDAG.sI) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return targets.contains(node);
		}
		else {
			return !serves.containsKey(node);
		}
	}
	
	public boolean isIntermediate(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) >= SimpleModelDAG.sI && Integer.parseInt(node) < SimpleModelDAG.sS) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return !targets.contains(node) && !sources.contains(node);
		}
		else {
			return depends.containsKey(node) && serves.containsKey(node);
		}
	}
		
	private void sourcePathDepth(String node) {
		if (numOfSourcePath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (isSource(node) && !CoreDetection.topRemovedWaistNodes.contains(node)) { // is source
			numOfSourcePath.put(node, 1.0);
			sumOfSourcePath.put(node, 0.0);
			avgSourceDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		if (!CoreDetection.topRemovedWaistNodes.contains(node)) { // special condition for waist detection
			for (String s : depends.get(node)) {
				sourcePathDepth(s);
				nPath += numOfSourcePath.get(s);
				sPath += numOfSourcePath.get(s) + sumOfSourcePath.get(s);
			}
		}
		
//		System.out.println("[S] " + node + "\t" + nPath + "\t" + sPath );
		numOfSourcePath.put(node, nPath);
		sumOfSourcePath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
	}
	
	private void targetPathDepth(String node) {
		if (numOfTargetPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (isTarget(node) && !CoreDetection.topRemovedWaistNodes.contains(node)) { // is target
			numOfTargetPath.put(node, 1.0);
			sumOfTargetPath.put(node, 0.0);
			avgTargetDepth.put(node, 0.0);
			return;
		}
		
		double nPath = 0;
		double sPath = 0;
		if (!CoreDetection.topRemovedWaistNodes.contains(node)) { // special condition for waist detection
			if (serves.containsKey(node)) { // for synthetic disconnected nodes
				for (String s : serves.get(node)) {
					targetPathDepth(s);
					nPath += numOfTargetPath.get(s);
					sPath += numOfTargetPath.get(s) + sumOfTargetPath.get(s);
				}
			}
		}
		
//		System.out.println("[T] " + node + "\t" + nPath + "\t" + sPath );
		numOfTargetPath.put(node, nPath);
		sumOfTargetPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	private int getEdgeWeight(String n1, String n2) {
		if (edgeWeights.containsKey(n1 + "#" + n2)) {
			return edgeWeights.get(n1 + "#" + n2);
		}
		
		return 1;
	}
	
	private void weightedSourcePathDepth(String node) {
		if (numOfSourcePath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (isSource(node) && !CoreDetection.topRemovedWaistNodes.contains(node)) { // is source
			numOfSourcePath.put(node, 1.0);
			sumOfSourcePath.put(node, 0.0);
			avgSourceDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		if (!CoreDetection.topRemovedWaistNodes.contains(node)) { // special condition for waist detection
			for (String s : depends.get(node)) {
				weightedSourcePathDepth(s);
				nPath += numOfSourcePath.get(s) * getEdgeWeight(s, node);
				sPath += (numOfSourcePath.get(s) + sumOfSourcePath.get(s)) * getEdgeWeight(s, node);
			}
		}
		
//		System.out.println(node + "\t" + nPath + "\t" + sPath );
		
		numOfSourcePath.put(node, nPath);
		sumOfSourcePath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
	}
	
	private void weightedTargetPathDepth(String node) {
		if (numOfTargetPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (isTarget(node) && !CoreDetection.topRemovedWaistNodes.contains(node)) { // is target
			numOfTargetPath.put(node, 1.0);
			sumOfTargetPath.put(node, 0.0);
			avgTargetDepth.put(node, 0.0);
			return;
		}
		
		double nPath = 0;
		double sPath = 0;
		if (!CoreDetection.topRemovedWaistNodes.contains(node)) { // special condition for waist detection
			if (serves.containsKey(node)) { // for synthetic disconnected nodes
				for (String s : serves.get(node)) {
					weightedTargetPathDepth(s);
					nPath += numOfTargetPath.get(s) * getEdgeWeight(node, s);
					sPath += numOfTargetPath.get(s) * getEdgeWeight(node, s) + sumOfTargetPath.get(s);
				}
			}
		}
		
		numOfTargetPath.put(node, nPath);
		sumOfTargetPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	public void loadWeightedPathStatistics() {
		for (String s: nodes) {
			weightedSourcePathDepth(s);
		}
				
		for (String s: nodes) {
			weightedTargetPathDepth(s);
		}		

		nTotalPath = 0;
		for (String s : nodes) {
			double nPath = 1;
			nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
			nodePathThrough.put(s, nPath);
			if (isTarget(s)) { // is a target
				nTotalPath += nPath;
			}
		}
	}

	public void loadPathStatistics() {	
		if (isWeighted) {
			loadWeightedPathStatistics();
			return;
		}
		
		for (String s: nodes) {
			sourcePathDepth(s);
		}
				
		for (String s: nodes) {
			targetPathDepth(s);
		}		

		nTotalPath = 0;
		for (String s : nodes) {
			double nPath = 1;
//			System.out.println(s + "\t" + numOfTargetPath.get(s) + "\t" + numOfSourcePath.get(s));
			nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
			nodePathThrough.put(s, nPath);
			if (isTarget(s)) { // is a target
				nTotalPath += nPath;
			}
		}
//		System.out.println("Total path: " + nTotalPath);
	}
		
	public void loadLocationMetric() {
		for (String s : nodes) {
			double m = avgSourceDepth.get(s) / (avgTargetDepth.get(s) + avgSourceDepth.get(s));
			m = ((int) (m * 1000.0)) / 1000.0; // round up to 2 decimal point
			location.put(s, m);
		}		
	}
		
	public void reachableUpwardsNodes(String node) { // towards targets
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		++kounter;
		visited.add(node);
				
		if (isTarget(node)) { // is a target
			return;
		}
		
		if (serves.containsKey(node)) {
			for (String s : serves.get(node)) {
				reachableUpwardsNodes(s);
			}
		}
	}
	
	public void reachableDownwardsNodes(String node) { // towards sources
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		++kounter;
		visited.add(node);
		
		if (isSource(node)) { // is a source
			return;
		}
		
		for (String s : depends.get(node)) {
			reachableDownwardsNodes(s);
		}
	}
	
	public void loadReachablityAll() {
		visited = new HashSet();
		for (String s : nodes) {
			visited.clear();
			kounter = 0;
			reachableUpwardsNodes(s); // how many nodes are using her
			visited.remove(s); // remove ifself
//			dependentsReachable.put(s, new HashSet(visited)); // too heavy for court case
			targetsReachable.put(s, kounter);
			
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
//			serversReachable.put(s, new HashSet(visited)); // too heavy for court case
			sourcesReachable.put(s, kounter);
		}
	}
	
	public void loadServerReachabilityAll() {
		visited = new HashSet();
		for (String s : nodes) {
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
			serversReachable.put(s, new HashSet(visited)); // too heavy for court case
		}
	}
	
	public void loadRechablity(String s) {
			visited = new HashSet();
			visited.clear();
//			nodesReachable.clear();
			
			kounter = 0;
			reachableUpwardsNodes(s); // how many nodes are using her
			nodesReachable.put(s, new HashSet(visited));
			visited.remove(s); // remove ifself
			dependentsReachable.put(s, new HashSet(visited)); // too heavy for court case
			targetsReachable.put(s, kounter);
//			System.out.println("Targets reached: " + kounter);
			
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			nodesReachable.get(s).addAll(visited);
			visited.remove(s); // remove itself
			serversReachable.put(s, new HashSet(visited)); // too heavy for court case
			sourcesReachable.put(s, kounter);
//			System.out.println("Source reached: " + kounter);
	}
		
	public void loadPathCentralityMetric() {
		int tubes = 0;
		for (String s: nodes) {			
//			P-Centrality
			double npc = numOfTargetPath.get(s) * numOfSourcePath.get(s) * 1.0 / nTotalPath;
//			npc = ((int) npc * 1000.0) / 1000.0;
			normalizedPathCentrality.put(s, npc);			
			
//			if (isSource(s) || isTarget(s)) { 
//				manually reset source and targets to zero
//				normalizedPathCentrality.put(s, 0.0);
//			}
		}			
		
//		System.out.println("Tubes: " + tubes);
	}
	
	public void printNetworkProperties() {
		for (String s: nodes) {
//			if (normalizedPathCentrality.get(s) < 0.4) continue;
//			System.out.print("Node: " + s + "\t");
//			System.out.print("Complexity: " + numOfSourcePath.get(s) + "\t");
//			System.out.print("Generality: " + numOfTargetPath.get(s) + "\t");
//			System.out.print("Location: " + location.get(s) + "\t");
//			System.out.print("Path Centrality: " + normalizedPathCentrality.get(s) * nTotalPath + "\t");
//			System.out.print(inDegree.get(s) + "\t");
//			System.out.print(outDegree.get(s) + "\t");
//			System.out.print(pagerankTargetCompression.get(s) + "\t");
//			System.out.print(pagerankSourceCompression.get(s) + "\t");
//			System.out.print(harmonicMeanPagerankCentrality.get(s) + "\t");
//			System.out.print(iCentrality.get(s) + "\t");
//			System.out.print(sourcesReachable.get(s) + "\t");
//			System.out.print(targetsReachable.get(s) + "\t");
			System.out.println(s + "\t" + (normalizedPathCentrality.get(s) * nTotalPath));
//			System.out.println();
		}
		
		
		System.out.println("Total path: " + nTotalPath);
		
		for (String s : nodes) {
//			if (depends.containsKey(s)) {
//				System.out.print(s + " depends on ");
//				for (String r : depends.get(s)) {
//					System.out.print("\t" + r);
//				}
//				System.out.println();
//			}
			
//			if (serves.containsKey(s)) {
//				System.out.print(s + " serves ");
//				for (String r : serves.get(s)) {
//					System.out.print("\t" + r);
//				}
//				System.out.println();
//			}
		}
		
	}
	
	public class PathCentralityComparator<String> implements Comparator<String> {
		public int compare(String s1, String s2) {
			double nPathThroughs1 = numOfSourcePath.get(s1) * numOfTargetPath.get(s1);
			double nPathThroughs2 = numOfSourcePath.get(s2) * numOfTargetPath.get(s2);
			return (int)(nPathThroughs2 - nPathThroughs1);
		}
	}
	
	public static void resetFlags() {
		isSynthetic = false;
		isCallgraph = false;
		isMetabolic = false;
		isCourtcase = false;
		isToy = false;
		isClassDependency = false;
		isSimpleModel = false;
		isComplexModel = false;
		isWeighted = false;	
		isRandomized = false;
		nDirectSourceTargetEdges = 0;
	}
	
	public void init() {
		nTotalPath = 0;
		nDirectSourceTargetEdges = 0;

		numOfTargetPath = new HashMap();
		sumOfTargetPath = new HashMap();
		avgTargetDepth = new HashMap();
		numOfSourcePath = new HashMap();
		sumOfSourcePath = new HashMap();
		avgSourceDepth = new HashMap();
		location = new HashMap();

		nodePathThrough = new HashMap();
		geometricMeanPathCentrality = new HashMap();
		harmonicMeanPathCentrality = new HashMap();
		normalizedPathCentrality = new HashMap();
		centralityRank = new HashMap();
		
		iCentrality = new HashMap();
		
		outDegree = new HashMap();
		inDegree = new HashMap();
		
		geometricMeanPagerankCentrality = new HashMap();
		harmonicMeanPagerankCentrality = new HashMap();
		pagerankSourceCompression = new HashMap();
		pagerankTargetCompression = new HashMap();
		
		detectedCycles = new ArrayList();
		cycleEdges = new HashMap();

		dependentsReachable = new HashMap();
		serversReachable = new HashMap();
		
		targetsReachable = new HashMap();
		sourcesReachable = new HashMap();
	
//		largestWCCNodes = new HashSet();
		visited = new HashSet();
		
		CoreDetection.topRemovedWaistNodes.clear();
		
		edgeWeights = new HashMap();
	}
}
