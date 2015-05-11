package Remodeled;
import java.io.File;
import java.util.ArrayList;
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

	Set<String> functions;
	Map<String, Set<String>> serves; 
	Map<String, Set<String>> depends; 
	
	Map<String, Double> numOfPath;
	Map<String, Double> sumOfPath;
	Map<String, Double> avgTargetDepth;
	Map<String, Double> avgSourceDepth;
	Map<String, Double> location;
	
	Map<String, Integer> outDegree;
	Map<String, Integer> inDegree;

	Set<String> visited;
	Map<String, String> cycleEdges;
	Set<String> cycleVisited;
	List<String> cycleList;
	ArrayList<ArrayList<String>> detectedCycles;
	
	HashMap<String, Double> geometricMeanPagerankCentrality;
	HashMap<String, Double> harmonicMeanPagerankCentrality;
	HashMap<String, Double> pagerankSourceCompression;
	HashMap<String, Double> pagerankTargetCompression;
	
	HashMap<String, Set<String>> dependentsReachable;
	HashMap<String, Set<String>> serversReachable;
	
	double nTotalPath;
	HashMap<String, Double> numOfSourcePath;
	HashMap<String, Double> numOfTargetPath;
	HashMap<String, Double> nodePathThrough;
	HashMap<String, Double> pathCentrality;
	
	String dependencyGraphID;
	
	DependencyDAG() { 
		functions = new TreeSet();
		serves = new HashMap();
		depends = new HashMap();
				
		numOfPath = new HashMap();
		sumOfPath = new HashMap();
		avgTargetDepth = new HashMap();
		avgSourceDepth = new HashMap();
		location = new HashMap();
		
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
		
		numOfSourcePath = new HashMap();
		numOfTargetPath = new HashMap();
		nodePathThrough = new HashMap();
		pathCentrality = new HashMap();
	}
	
	DependencyDAG(String dependencyGraphID) {
		this();
		
		this.dependencyGraphID = dependencyGraphID;
			
		// load & initialize the attributes of the dependency graph
		loadCallGraph(dependencyGraphID);
		removeCycles(); // or should I only ignore cycles?
		removeIsolatedNodes();
		
		loadDegreeMetric();
		loadLocationMetric(); // must load degree metric before
		loadPagerankCentralityMetric();
//		loadRechablity();
//		loadPathCentralityMetric();
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
					String dependent = tokens[0];
					String server = tokens[2].substring(0, tokens[2].length() - 1); // for cobjdump
//					String server = tokens[2].substring(0, tokens[2].length()); // for cdepn/bio nets
					
					if (server.equals("mcount"))  // no more location metric noise! // compiler generated
						continue;
					
					functions.add(dependent);
					functions.add(server);
					
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
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeIsolatedNodes() {
		HashSet<String> removable = new HashSet<String>();
		for (String s: functions) {
			if (!serves.containsKey(s) && !depends.containsKey(s)) {
				removable.add(s);
			}
		}
		functions.removeAll(removable);
	}

	private void removeCyclesTraverse(String node) {
		if (!serves.containsKey(node) || visited.contains(node))
			return;

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
			for (String s : functions) {
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
						functions.remove(server);
					}
					if (!depends.containsKey(dependent) && !serves.containsKey(dependent)) {
						functions.remove(dependent);
					}
					--nEdges;
					++nCycles;
//					loop = true;
				}
			}
		}

		// System.out.println(k + " cycle edges removed!");
	}
	
	public void loadDegreeMetric() {
//		get the fanIn/Out
		for (String s : functions) {
			int in = 0;
			int out = 0;
			
			if (depends.containsKey(s)) {
				in = depends.get(s).size();
			}
			
			if (serves.containsKey(s)) {
				out = serves.get(s).size();
			}
			
			outDegree.put(s, out);
			inDegree.put(s, in);
		}
		
		nSources = nTargets = nEdges = 0;
		for (String s: functions) {
			if (!serves.containsKey(s)) ++nTargets;
			if (!depends.containsKey(s)) {
				++nSources;
			}
			else {
				nEdges += depends.get(s).size();
			}
		}
	}
		
	private void sourcePathDepth(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!depends.containsKey(node)) { // is source
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgSourceDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		for (String s: depends.get(node)) {
				sourcePathDepth(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
	}
	
	private void targetPathDepth(String node) {
		if (numOfPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!serves.containsKey(node)) { // is target
			numOfPath.put(node, 1.0);
			sumOfPath.put(node, 0.0);
			avgTargetDepth.put(node, 0.0);
			return;
		}
		
		double nPath = 0;
		double sPath = 0;
		for (String s: serves.get(node)) {
				targetPathDepth(s);
				nPath += numOfPath.get(s);
				sPath += numOfPath.get(s) + sumOfPath.get(s);
		}
		
		numOfPath.put(node, nPath);
		sumOfPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	public void loadLocationMetric() {
//		go through targets
		for (String s: functions) {
			if (!serves.containsKey(s)) {
				sourcePathDepth(s);
			}
		}
		
		numOfSourcePath.putAll(numOfPath);
				
		numOfPath.clear();
		sumOfPath.clear();
		
//		go through sources
		for (String s: functions) {
			if (!depends.containsKey(s)) {
				targetPathDepth(s);
			}
		}
		
		numOfTargetPath.putAll(numOfPath);
		
		for (String s : functions) {
			double m = avgSourceDepth.get(s) / (avgTargetDepth.get(s) + avgSourceDepth.get(s));
			m = ((int) (m * 1000.0)) / 1000.0; // round up to 2 decimal point
			location.put(s, m);
		}		
	}
		
	private void reachableUpwardsNodes(String node) { // towards root
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
				
		if (!serves.containsKey(node)) { // is a target
			return;
		}
		
		for (String s : serves.get(node)) {
			reachableUpwardsNodes(s);
		}
	}
	
	private void reachableDownwardsNodes(String node) { // towards leaves
		if (visited.contains(node)) { // node already traversed
			return;
		}
		
		visited.add(node);
		
		if (!depends.containsKey(node)) { // is a source
			return;
		}
		
		for (String s : depends.get(node)) {
			reachableDownwardsNodes(s);
		}
	}
	
	public void loadRechablity() {
		visited = new HashSet();
		for (String s : functions) {
			visited.clear();
			reachableUpwardsNodes(s); // how many nodes are using her
			visited.remove(s); // remove ifself
			dependentsReachable.put(s, new HashSet(visited));
			
			visited.clear();
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
			serversReachable.put(s, new HashSet(visited));
		}
	}
	
	private void recursePagerankTargetToSource(String node) {
		if (pagerankTargetCompression.containsKey(node)) {
			return;
		}

		double nodePRCentrality = 0;
		if (!WaistDetection.topKNodes.contains(node)) {
			for (String s : serves.get(node)) {
				recursePagerankTargetToSource(s);
				nodePRCentrality += pagerankTargetCompression.get(s) / inDegree.get(s);
			}
		}
		pagerankTargetCompression.put(node, nodePRCentrality);
	}

	private void recursePagerankSourceToTarget(String node) {
		if (pagerankSourceCompression.containsKey(node)) {
			return;
		}
		
		double nodePRCentrality = 0;
		if (!WaistDetection.topKNodes.contains(node)) {
			for (String s : depends.get(node)) {
				recursePagerankSourceToTarget(s);
				nodePRCentrality += pagerankSourceCompression.get(s) / outDegree.get(s);
			}
		}
		pagerankSourceCompression.put(node, nodePRCentrality);
	}
	
	public void loadPagerankCentralityMetric() {		
		// initialize target pr
		for (String s : functions) {
			if (!serves.containsKey(s)) {
				pagerankTargetCompression.put(s, 1.0);
			}
		}

		for (String s : functions) {
			recursePagerankTargetToSource(s);
		}
		
		// initialize source pr
		for (String s: functions) {
			if (!depends.containsKey(s)) {
				pagerankSourceCompression.put(s, 1.0);
			}
		}
		
		for (String s: functions) {
			recursePagerankSourceToTarget(s);
		}
		
		for (String s: functions) {
			pagerankSourceCompression.put(s, pagerankSourceCompression.get(s) / nSources);
			pagerankTargetCompression.put(s, pagerankTargetCompression.get(s) / nTargets);
			
			double harmonicMeanPagerank = 2.0 * pagerankSourceCompression.get(s) * pagerankTargetCompression.get(s) / (pagerankSourceCompression.get(s) + pagerankTargetCompression.get(s));
			double geometricMeanPagerank = Math.sqrt(pagerankSourceCompression.get(s) * pagerankTargetCompression.get(s));
			
			harmonicMeanPagerankCentrality.put(s, harmonicMeanPagerank);
			geometricMeanPagerankCentrality.put(s, geometricMeanPagerank);
		}

		for (String s: functions) {
//			if (depends.containsKey(s) && serves.containsKey(s)) {
//				System.out.print(s + "\t" + location.get(s) + "\t" + pagerankTargetCompression.get(s) + "\t" + pagerankSourceCompression.get(s));
//				System.out.print("\t" + harmonicMeanPagerankCentrality.get(s));
//				System.out.println();
//			}
//			System.out.println(s + "\t" + location.get(s) + "\t" + harmonicMeanPagerankCentrality.get(s));
		}
//		System.out.println("###### ###### ######");
//		System.out.println("HERE!");
	}
	
	public void loadPathCentralityMetric() {
		nTotalPath = 0;
		for (String s: location.keySet()) {
			double nPath = 1;
//			P-Centrality
			nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
			nodePathThrough.put(s, nPath);
			if (!serves.containsKey(s)) { // is a target
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
			double pCentrality = nodePathThrough.get(s) / nTotalPath;
//			pCentrality = ((int) (cntr * 1000.0)) / 1000.0;
			pathCentrality.put(s, pCentrality);
//			pathCentrality.put(s, nodePathThrough.get(s)); // non-normalized
//			System.out.println(s + " pCentrality: " + pCentrality);
		}		
		
//		System.out.println("Total Paths: " + nTotalPath + "\n" + "####################");
	}

	
	public void printNetworkMetrics() {
		for (String s: functions) {
			System.out.print(s + "\t");
			System.out.print(inDegree.get(s) + "\t");
			System.out.print(outDegree.get(s) + "\t");
			System.out.print(location.get(s) + "\t");
			System.out.print(geometricMeanPagerankCentrality.get(s) + "\t");
			System.out.println();
		}
		
		for (String s: functions) {
			System.out.print(s + " serves to");
			for (String r: dependentsReachable.get(s)) {
				System.out.print(" " + r);
			}
			System.out.println();
			
			System.out.print(s + " depends on");
			for (String r: serversReachable.get(s)) {
				System.out.print(" " + r);
			}
			System.out.println();
		}
	}
}
