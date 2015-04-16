package Remodeled;
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
	double nSources; // source =  simple module, zero in degree, depends on none
	double nTargets; // target = complex module, zero out degree, serves none (yeah right)

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
		
	HashMap<String, Double> centrality;
	HashMap<String, Double> prSource;
	HashMap<String, Double> prTarget;
	
	Map<String, Set<String>> dependentsReachable;
	Map<String, Set<String>> serversReachable;
	
	CallDAG() { 
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
		
		centrality = new HashMap();
		prSource = new HashMap();
		prTarget = new HashMap();
		
		detectedCycles = new ArrayList();
		cycleEdges = new HashMap();

		dependentsReachable = new HashMap();
		serversReachable = new HashMap();
	}
	
	CallDAG(String callGraphFileName) {
		this();
			
		// load & initialize the attributes of the call graph
		loadCallGraph(callGraphFileName);
		removeCycles(); // or should I only ignore cycles?
		
		loadDegreeMetric();
		loadLocationMetric(); // must load degree metric before
		loadPagerankCentralityMetric();
		loadRechablity();
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
				
		numOfPath.clear();
		sumOfPath.clear();
		
//		go through sources
		for (String s: functions) {
			if (!depends.containsKey(s)) {
				targetPathDepth(s);
			}
		}
		
		for (String s : functions) {
			double m = avgSourceDepth.get(s) / (avgTargetDepth.get(s) + avgSourceDepth.get(s));
			m = ((int) (m * 100.0)) / 100.0; // round up to 2 decimal point
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
		if (prTarget.containsKey(node)) {
			return;
		}

		double nodePRCentrality = 0;
		for (String s : serves.get(node)) {
			recursePagerankTargetToSource(s);
			nodePRCentrality += prTarget.get(s) / inDegree.get(s);
		}

		prTarget.put(node, nodePRCentrality);
	}

	private void recursePagerankSourceToTarget(String node) {
		if (prSource.containsKey(node)) {
			return;
		}
		
		double nodePRCentrality = 0;
		for (String s: depends.get(node)) {
			recursePagerankSourceToTarget(s);
			nodePRCentrality += prSource.get(s) / outDegree.get(s);
		}
		
		prSource.put(node, nodePRCentrality);
	}
	
	public void loadPagerankCentralityMetric() {
		// initialize source pr
		for (String s: functions) {
			if (!depends.containsKey(s)) {
				prSource.put(s, 1.0);
			}
		}
		
		for (String s: functions) {
			recursePagerankSourceToTarget(s);
		}
		
		// initialize target pr
		for (String s : functions) {
			if (!serves.containsKey(s)) {
				prTarget.put(s, 1.0);
			}
		}

		for (String s : functions) {
			recursePagerankTargetToSource(s);
		}
		
		for (String s: functions) {
//			centrality.put(s, prSource.get(s));
			centrality.put(s, prTarget.get(s));
//			centrality.put(s, prSource.get(s) * prTarget.get(s));
		}

		for (String s: functions) {
//			if (depends.containsKey(s) && serves.containsKey(s)) {
				System.out.println(s + "\t" + location.get(s) + "\t" + centrality.get(s));
//			}
		}
//		System.out.println("###### ###### ######");
	}
	
	public void printNetworkMetrics() {
		for (String s: functions) {
			System.out.print(s + "\t");
			System.out.print(inDegree.get(s) + "\t");
			System.out.print(outDegree.get(s) + "\t");
			System.out.print(location.get(s) + "\t");
			System.out.print(centrality.get(s) + "\t");
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
