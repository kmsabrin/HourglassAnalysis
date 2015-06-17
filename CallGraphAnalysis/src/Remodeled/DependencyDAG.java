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
	HashMap<String, Integer> targetsReachable;
	HashMap<String, Integer> sourcesReachable;
	
	HashMap<String, Double> pagerankSourceCompression;
	HashMap<String, Double> pagerankTargetCompression;
	HashMap<String, Double> geometricMeanPagerankCentrality;
	HashMap<String, Double> harmonicMeanPagerankCentrality;
		
	String dependencyGraphID;
	
	int kounter;
	
	DependencyDAG() { 
		functions = new TreeSet();
		serves = new HashMap();
		depends = new HashMap();
				
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
		
		WaistDetection.topKNodes.clear();
	}
	
	DependencyDAG(String dependencyGraphID) {
		this();
		
		this.dependencyGraphID = dependencyGraphID;
			
		// load & initialize the attributes of the dependency graph
		loadCallGraph(dependencyGraphID);
//		removeCycles(); // or should I only ignore cycles?
//		removeIsolatedNodes();
		
		loadDegreeMetric();
		loadPathStatistics();
		loadLocationMetric(); // must load degree metric before
		loadRechablity();
		
		loadPathCentralityMetric();
		loadPagerankCentralityMetric();
	}

	public void loadCallGraph(String fileName) {
		try {
			Scanner scanner = new Scanner(new File(fileName));

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String tokens[] = line.split("\\s+");
//				String tokens[] = line.split("[,]");
				if (tokens.length < 2)
					continue;

//				if (tokens[1].equals("->")) // for call graphs
				{
					String server = tokens[0]; // for space separated DAG: a b or a,b
					String dependent = tokens[1]; // for space separated DAG: a b or a,b
					
//					desparetely need to clean all the datasets
//					if (Integer.parseInt(dependent) < Integer.parseInt(server)) { // for court cases
//						System.out.println(dependent + " citing " + server);
//						continue;
//					}
					
//					String dependent = tokens[0].substring(0, tokens[0].length()); // for cobjdump: a -> b;
//					String server = tokens[2].substring(0, tokens[2].length() - 1); // for cobjdump: a -> b;
					
//					String server = tokens[2].substring(0, tokens[2].length()); // for cdepn: a -> b
//					System.out.println(dependent + "\t" + server);
					
//					if (server.equals("mcount"))  // no more location metric noise! // compiler generated
//						continue;
					
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
	
	public void removeIsolatedNodes() {
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
						
						for (String r: detectedCycles.get(detectedCycles.size() - 1)) {
							System.out.print(r + " ");
						}
						System.out.println();
						
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
					System.out.println("cycle found");
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
		if (numOfSourcePath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!depends.containsKey(node) && !WaistDetection.topKNodes.contains(node)) { // is source
			numOfSourcePath.put(node, 1.0);
			sumOfSourcePath.put(node, 0.0);
			avgSourceDepth.put(node, 0.0);
			return;
		}
				
		double nPath = 0;
		double sPath = 0;
		if (!WaistDetection.topKNodes.contains(node)) { // special condition for waist detection
			for (String s : depends.get(node)) {
				sourcePathDepth(s);
				nPath += numOfSourcePath.get(s);
				sPath += numOfSourcePath.get(s) + sumOfSourcePath.get(s);
			}
		}
		numOfSourcePath.put(node, nPath);
		sumOfSourcePath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
	}
	
	private void targetPathDepth(String node) {
		if (numOfTargetPath.containsKey(node)) { // node already traversed
			return;
		}
		
		if (!serves.containsKey(node) && !WaistDetection.topKNodes.contains(node)) { // is target
			numOfTargetPath.put(node, 1.0);
			sumOfTargetPath.put(node, 0.0);
			avgTargetDepth.put(node, 0.0);
			return;
		}
		
		double nPath = 0;
		double sPath = 0;
		if (!WaistDetection.topKNodes.contains(node)) { // special condition for waist detection
			for (String s : serves.get(node)) {
				targetPathDepth(s);
				nPath += numOfTargetPath.get(s);
				sPath += numOfTargetPath.get(s) + sumOfTargetPath.get(s);
			}
		}
		numOfTargetPath.put(node, nPath);
		sumOfTargetPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	public void loadPathStatistics() {
//		int knt = 0;
		for (String s: functions) {
//			if (!numOfSourcePath.containsKey(s)) ++knt;
			sourcePathDepth(s);
		}
				
		for (String s: functions) {
			targetPathDepth(s);
		}		

		nTotalPath = 0;
		for (String s : functions) {
			double nPath = 1;
			nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
			nodePathThrough.put(s, nPath);
			if (!serves.containsKey(s)) { // is a target
				nTotalPath += nPath;
			}
		}
		
//		System.out.println("Components " + knt);
//		System.out.println("Total Paths: " + nTotalPath + "\n" + "####################");
	}
	
	public void loadLocationMetric() {
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
			++kounter;
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
			++kounter;
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
			kounter = 0;
			reachableUpwardsNodes(s); // how many nodes are using her
			visited.remove(s); // remove ifself
			dependentsReachable.put(s, new HashSet(visited));
			targetsReachable.put(s, kounter);
			
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
			serversReachable.put(s, new HashSet(visited));
			sourcesReachable.put(s, kounter);
		}
	}
	
	private void computeTargetPagerankCompression(String node) {
		if (pagerankTargetCompression.containsKey(node)) {
			return;
		}

		double nodePRCentrality = 0;
//		if (!WaistDetection.topKNodes.contains(node)) {
			for (String s : serves.get(node)) {
				computeTargetPagerankCompression(s);
				nodePRCentrality += pagerankTargetCompression.get(s) / inDegree.get(s);
			}
//		}
		pagerankTargetCompression.put(node, nodePRCentrality);
	}

	private void computeSourcePagerankCompression(String node) {
		if (pagerankSourceCompression.containsKey(node)) {
			return;
		}
		
		double nodePRCentrality = 0;
//		if (!WaistDetection.topKNodes.contains(node)) {
			for (String s : depends.get(node)) {
				computeSourcePagerankCompression(s);
				nodePRCentrality += pagerankSourceCompression.get(s) / outDegree.get(s);
			}
//		}
		pagerankSourceCompression.put(node, nodePRCentrality);
	}
	
	public void loadPagerankCentralityMetric() {		
		// initialize target pr
		for (String s : functions) {
			if (!serves.containsKey(s)) {
				pagerankTargetCompression.put(s, 1.0 / nTargets);
			}
		}

		for (String s : functions) {
			computeTargetPagerankCompression(s);
		}
		
		// initialize source pr
		for (String s: functions) {
			if (!depends.containsKey(s)) {
				pagerankSourceCompression.put(s, 1.0 / nSources);
			}
		}
		
		for (String s: functions) {
			computeSourcePagerankCompression(s);
		}
		
		for (String s: functions) {
//			pagerankSourceCompression.put(s, pagerankSourceCompression.get(s) / nSources);
//			pagerankTargetCompression.put(s, pagerankTargetCompression.get(s) / nTargets);
			
			double harmonicMeanPagerank = 2.0 * pagerankSourceCompression.get(s) * pagerankTargetCompression.get(s) / (pagerankSourceCompression.get(s) + pagerankTargetCompression.get(s));
			double geometricMeanPagerank = Math.sqrt(pagerankSourceCompression.get(s) * pagerankTargetCompression.get(s));
			
			harmonicMeanPagerankCentrality.put(s, harmonicMeanPagerank);
			geometricMeanPagerankCentrality.put(s, geometricMeanPagerank);
		}
	}
	
	public void loadPathCentralityMetric() {
		double connectedSTPairs = 0;
		for (String s: functions) {
			if (!serves.containsKey(s)) { // is a target
				connectedSTPairs += sourcesReachable.get(s);
			}
		}
		
		for (String s: functions) {			
//			P-Centrality
			double harmonicMean = 2.0 * numOfTargetPath.get(s) * numOfSourcePath.get(s) / (numOfTargetPath.get(s) + numOfSourcePath.get(s));
			double geometricMean = Math.sqrt(numOfTargetPath.get(s) * numOfSourcePath.get(s));
			harmonicMeanPathCentrality.put(s, harmonicMean);
			geometricMeanPathCentrality.put(s, geometricMean);	
			normalizedPathCentrality.put(s, numOfTargetPath.get(s) * numOfSourcePath.get(s) / nTotalPath);
			
//			I-Centrality
//			double targetsReachedNormalized = targetsReachable.get(s) * 1.0 / nTargets;
//			double sourcesReachedNormalized = sourcesReachable.get(s) * 1.0 / nSources;
			double stPairsConnected = targetsReachable.get(s) * sourcesReachable.get(s);
			iCentrality.put(s, stPairsConnected / connectedSTPairs);
		}				
	}
	
	public void printNetworkMetrics() {
		for (String s: functions) {
			System.out.print(s + "\t");
//			System.out.print(inDegree.get(s) + "\t");
//			System.out.print(outDegree.get(s) + "\t");
//			System.out.print(location.get(s) + "\t");
			System.out.print(normalizedPathCentrality.get(s) + "\t");
//			System.out.print(pagerankTargetCompression.get(s) + "\t");
//			System.out.print(pagerankSourceCompression.get(s) + "\t");
			System.out.print(harmonicMeanPagerankCentrality.get(s) + "\t");
			System.out.print(iCentrality.get(s) + "\t");
			System.out.println();
		}
	}
}
