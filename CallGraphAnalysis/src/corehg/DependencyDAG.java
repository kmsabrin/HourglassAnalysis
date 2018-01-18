package corehg;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import neuro.ManagerNeuro;

import org.apache.commons.lang3.StringUtils;

import utilityhg.CourtCaseCornellParser;
import utilityhg.Edge;

public class DependencyDAG {
	public int nEdges;
	public double nSources; // source =  simple module, zero in degree, depends on none
	public double nTargets; // target = complex module, zero out degree, serves none (or external api)

	public Set<String> nodes;
	public Map<String, Set<String>> serves; 
	public Map<String, Set<String>> depends; 
	
	public HashSet<String> targets;
	public HashSet<String> sources;
	
	public Map<String, Double> numOfTargetPath;
	public Map<String, Double> sumOfTargetPath;
	public Map<String, Double> numOfSourcePath;
	public Map<String, Double> sumOfSourcePath;	
	public Map<String, Double> avgTargetDepth;
	public Map<String, Double> avgSourceDepth;
	public Map<String, Double> lengthPathLocation;
	
	public double nTotalPath;
//	public BigDecimal nTotalPath;
	public HashMap<String, Double> nodePathThrough;
//	public HashMap<String, Double> geometricMeanPathCentrality;
//	public HashMap<String, Double> harmonicMeanPathCentrality;
	public HashMap<String, Double> normalizedPathCentrality;
	public HashMap<String, Integer> centralityRank;
//	public HashMap<String, Double> iCentrality;
	public HashMap<String, Double> lengthWeightedPathCentrality;
	
	public Map<String, Integer> outDegree;
	public Map<String, Integer> inDegree;

	public Set<String> visited;
	public Map<String, String> cycleEdges;
	public Set<String> cycleVisited;
	public List<String> cycleList;
	public ArrayList<ArrayList<String>> detectedCycles;

	public HashMap<String, Set<String>> successors;
	public HashMap<String, Set<String>> ancestors;
	public HashMap<String, HashSet<String>> nodesReachable;
	
	public HashMap<String, Integer> targetsReachableKount; // for iCentrality
	public HashMap<String, Integer> sourcesReachableKount; // for iCentrality
	public HashMap<String, HashSet<String>> targetsReachable; // for iCentrality
	public HashMap<String, HashSet<String>> sourcesReachable; // for iCentrality
	public int connectedSourceTargetPair; // for iCentrality
	
//	public HashMap<String, Double> pagerankSourceCompression; // for prCentrality
//	public HashMap<String, Double> pagerankTargetCompression; // for prCentrality
	
//	public HashMap<String, Double> geometricMeanPagerankCentrality;
//	public HashMap<String, Double> harmonicMeanPagerankCentrality;
		
	public String dependencyGraphID;
	
	public int kounter;
	
	public boolean canReachTarget;
	public boolean canReachSource;
	
	public static boolean isSynthetic = false;
	public static boolean isCallgraph = false;
	public static boolean isMetabolic = false;
	public static boolean isCourtcase = false;
	public static boolean isToy = false;
	public static boolean isClassDependency = false;
	public static boolean isSimpleModel = false;
	public static boolean isComplexModel = false;
	public static boolean isWeighted = false;	
	public static boolean isRandomized = false;
	public static boolean isLexis = false;
	public static boolean isCyclic = false;
	public static boolean isCelegans = false;
	
	public int nDirectSourceTargetEdges = 0;
	
	public static HashSet<String> largestWCCNodes;
	
	public HashMap<String, Double> edgeWeights;
	
//	public HashSet<String> goodEdgeToSource;
//	public HashSet<String> goodEdgeToTarget;
//	public HashSet<String> visitedGray;
//	public HashSet<String> visitedBlack;
//	public HashMap<String, Double> cyclicNumSourcePath;
//	public HashMap<String, Double> cyclicNumTargetPath;
//	public HashMap<String, Double> cyclicAvgSourceDepth;
//	public HashMap<String, Double> cyclicAvgTargetDepth;
	
	public HashMap<String, Double> numPathLocation;

	public ArrayList<Edge> edgePathCentrality;
	public HashSet<String> edgeSkip;
	
	public int disconnectedKount; 
	
	public DependencyDAG() { 
		init();
	}
	
	public DependencyDAG(String dependencyGraphID) throws Exception {
		this();
		
		this.dependencyGraphID = dependencyGraphID;
			
		// load & initialize the attributes of the dependency graph
		loadGraph(dependencyGraphID);
		
		if (isCallgraph || isClassDependency /*|| isToy || isMetabolic || isCourtcase*/) {
			removeCycles(); // or should I only ignore cycles?
		}
		
		if (isSynthetic) {
//			removeDisconnectedNodesForSyntheticNetworks();
		}		
		
		loadNetworkAttributes();
	}
	
	private void loadNetworkAttributes() {
		loadDegreeMetric();		
		
		/* applicable on DAGs only */
//		loadPathStatistics();
//		loadLocationMetric(); // must load degree metric before
//		loadServerReachabilityAll();
//		loadPathCentralityMetric();
		
		/* not used anymore */
//		loadReachablityAll();
//		countDisconnectedNodes();
//		removeIsolatedNodes(); 
//		loadPagerankCentralityMetric();		
//		DistributionAnalysis.rankNodeByCentrality(this, this.normalizedPathCentrality);
	}
	
	private void checkTargetReachability(String node) {
		if (isTarget(node)) {
			canReachTarget = true;
			return;
		}
		
		if (visited.contains(node)) {
			return;
		}
		visited.add(node);

		if (canReachTarget) return;
		
		if (serves.containsKey(node)) {
			for (String s : serves.get(node)) {
				if (CoreDetection.topRemovedWaistNodes.contains(s)) continue;
				checkTargetReachability(s);
			}
		}
	}
	
	private void checkSourceReachability(String node) {
		if (isSource(node)) {
			canReachSource = true;
			return;
		}
		
		if (visited.contains(node)) {
			return;
		}
		visited.add(node);
		
		if (canReachSource) return;
		
		if (depends.containsKey(node)) {
			for (String s : depends.get(node)) {
				if (CoreDetection.topRemovedWaistNodes.contains(s)) continue;
				checkSourceReachability(s);
			}
		}
	}
	
	public void checkReach(String node) {
		canReachTarget = false;
		canReachSource = false;
		visited = new HashSet();
		checkTargetReachability(node);
		visited = new HashSet();
		checkSourceReachability(node);
		visited.clear();
	}
	
	public void removeNode(String node) {
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
	
	public void removeNodeAndReload(String node) {
		removeNode(node);
		initAuxiliary();
		loadNetworkAttributes();
	}
	
	public void removeEdge(String source, String target) {
		serves.get(source).remove(target);
		depends.get(target).remove(source);
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
	
	private int countDisconnectedNodes() {
		disconnectedKount = 0;
		for (String s: nodes) {
			checkReach(s);
			if (isSource(s) && !canReachTarget) {
				++disconnectedKount;
			}
			else if (isTarget(s) && !canReachSource) {
				++disconnectedKount;
			}
			else if (!canReachTarget || !canReachSource) {
				++disconnectedKount;
			}
		}
		return disconnectedKount;
	}

	public void loadGraph(String fileName) throws Exception {
		Scanner scanner = new Scanner(new File(fileName));

		int violation = 0;
		int lexisTargetKount = 0;
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String tokens[] = line.split("\\s+");
			if (tokens.length < 2 /*|| tokens.length > 3*/) {
				continue;
			}

			int directionIndex = -1;
			int idx = 0;
			for (String s: tokens) {
				if (s.equals("->")) {
					directionIndex = idx;
				}
				++idx;
			}
				
			String server = "", dependent = "";

			if (isCallgraph) {
				if (directionIndex > 0) {
					dependent = tokens[directionIndex - 1].substring(0, tokens[directionIndex - 1].length());
					server = tokens[directionIndex + 1].substring(0, tokens[directionIndex + 1].length()); // for dot: a -> b;
					if (server.charAt(server.length() - 1) == ';') {
						server = server.substring(0, server.length() - 1);
					}
				}
				else {
					continue;
				}
				
				// for call graphs
//				if (tokens[1].equals("->")) {
//					dependent = tokens[0].substring(0, tokens[0].length());
//					server = tokens[2].substring(0, tokens[2].length() - 1); // for cobjdump: a-> b;
//					// String server = tokens[2].substring(0, tokens[2].length()); // for cdepn: a -> b
//				}
				
//				else {
//					// for scc-consolidated graph
//					dependent = tokens[0].substring(0, tokens[0].length());
//					server = tokens[1].substring(0, tokens[1].length()); // for scc-consolidation: a b
//				}
				
				if (/*dependent.equals("do_exec") || server.equals("do_exec") ||*/
//					dependent.contains("exec") || server.contains("exec")	
					dependent.contains("exit") || server.contains("exit")
					|| dependent.contains("_log") || server.contains("_log")
					|| dependent.contains("main") || server.contains("main")
					|| dependent.contains("fatal") || server.contains("fatal")
					|| dependent.contains("error") || server.contains("error")
					|| dependent.contains("_fail") || server.contains("_fail")
					|| dependent.contains("debug") || server.contains("debug")
//					dependent.contains("@plt") || server.contains("@plt")
					
//					|| dependent.contains("xrealloc") || server.contains("xrealloc")
//					|| dependent.contains("xmalloc") || server.contains("xmalloc")
//					|| dependent.contains("xfree") || server.contains("xfree")
					/*dependent.equals("fatal") || server.equals("fatal")
					|| dependent.equals("debug") || server.equals("debug")
					|| dependent.equals("error") || server.equals("error")
					|| dependent.equals("__stack_chk_fail@plt") || server.equals("__stack_chk_fail@plt")
					|| dependent.equals("__errno_location@plt") || server.equals("__errno_location@plt")
					|| dependent.equals("debug3") || server.equals("debug3")
					|| dependent.equals("debug2") || server.equals("debug2")
					|| dependent.equals("strerror@plt") || server.equals("strerror@plt")
					||dependent.equals("main") || server.equals("main")*/
					
						) { 
					// no more location metric noise! 
					// compiler generated
//					System.out.println(dependent + "\t" + server);
					continue;
				}
				
//				if (dependent.equals("sqliteRunParser") || server.equals("sqliteRunParser")
//					|| dependent.equals("sqliteVdbeExec") || server.equals("sqliteVdbeExec") 
//					|| dependent.equals("sqliteRunParser") || server.equals("sqlite3Init")
//					|| dependent.equals("sqlite3_prepare") || server.equals("sqlite3_prepare")
//					|| dependent.equals("sqlite_exec") || server.equals("sqlite_exec")
//					|| dependent.equals("sqliteParser") || server.equals("sqliteParser")) {
//					continue;
//				}
					
//				if (dependent.endsWith("@plt") || server.endsWith("@plt")) {
//					continue;
//				}
				
//				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
//					continue;
//				}
				

				// for JetUML 
				if (dependent.contains("Test") || server.contains("Test")) {
					continue;
				}
			}
			else if (isClassDependency) {
				// for metabolic and synthetic networks
				server = tokens[0];
				dependent = tokens[1];
				
//				if (StringUtils.containsIgnoreCase(server, "java.")) continue;
//				if (StringUtils.containsIgnoreCase(dependent, "java.")) continue;
//				if (StringUtils.containsIgnoreCase(server, "int[]")) continue;
//				if (StringUtils.containsIgnoreCase(dependent, "int[]")) continue;
//				if (StringUtils.containsIgnoreCase(server, "double[]")) continue;
//				if (StringUtils.containsIgnoreCase(dependent, "double[]")) continue;
				
				if (StringUtils.containsIgnoreCase(server, "exception")) continue;
				if (StringUtils.containsIgnoreCase(dependent, "exception")) continue;
				
				if (StringUtils.containsIgnoreCase(server, "object")) continue;
				if (StringUtils.containsIgnoreCase(dependent, "object")) continue;
				
//				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
//					continue;
//				}
			}
			else if (isMetabolic) {
				// for metabolic and synthetic networks
				server = tokens[0];
				dependent = tokens[1];
				if (largestWCCNodes.contains(server) == false || largestWCCNodes.contains(dependent) == false) {
					continue;
				}
			}
			else if (isSynthetic || isToy || isCyclic) {
				if (isWeighted) {
					server = tokens[0];
					dependent = tokens[1];
					double weight = Double.parseDouble(tokens[2]);
//					if (weight > 1) {
						edgeWeights.put(server + "#" + dependent, weight);
//						System.out.println("putting " + server + "#" + dependent + " w:" + weight);
//						System.out.println(weight);
//					}
				} 
				else {
					server = tokens[0];
					dependent = tokens[1];
					
//					if (server.equals("miR429")) {
//						continue;
//					}
					
//					if (dependent.equals("do_log") || server.equals("do_log")
//							|| dependent.equals("main") || server.equals("main")	
//							|| dependent.equals("do_exec") || server.equals("do_exec")
//							) {
//							continue;
//					}
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
				
//				System.out.println(server + "\t" + dependent);
			}
			else if (isLexis) {
				// temporary fix
//				dependent = tokens[0];
//				if (dependent.equals("N0")) {
//					dependent += "_" + lexisTargetKount++;
//				}
//				nodes.add(dependent);
//				
//				for (int i = 2; i < tokens.length; ++i) {
//					server = tokens[i]; 
//					nodes.add(server);
//
//					if (serves.containsKey(server)) {
//						serves.get(server).add(dependent);
//					} else {
//						HashSet<String> hs = new HashSet();
//						hs.add(dependent);
//						serves.put(server, hs);
//					}
//					
//					if (depends.containsKey(dependent)) {
//						depends.get(dependent).add(server);
//					} else {
//						HashSet<String> hs = new HashSet();
//						hs.add(server);
//						depends.put(dependent, hs);
//					}
//					
//					String weightKey = server + "#" + dependent;
//					if (edgeWeights.containsKey(weightKey)) {
//						edgeWeights.put(weightKey, edgeWeights.get(weightKey) + 1);
//					}
//					else {
//						edgeWeights.put(weightKey, 1.0);
//					}
//				}
//				continue;
//				
				server = tokens[0];
				dependent = tokens[1];
				double weight = Double.parseDouble(tokens[2]);
				edgeWeights.put(server + "#" + dependent, weight);
			}

//			System.out.println(dependent + " - " + dependent.length() + "\t" + server + " - " + server.length());
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
	
	public void addEdge(String server, String dependent) {
		nodes.add(dependent);
		nodes.add(server);

		if (dependent.equals(server)) { // loop, do not add the edge
			return;
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
	
	public void addEdgeAndReload(String server, String dependent) {
		addEdge(server, dependent);	
		initAuxiliary();
		loadNetworkAttributes();
	}
	
	public void removeEdgeAndReload(String server, String dependent) {
		removeEdge(server, dependent);
		initAuxiliary();
		loadNetworkAttributes();
	}
	
	public void reload() {
		initAuxiliary();
		loadNetworkAttributes();
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
		for (String s: nodes) {
			if (isTarget(s)) {
				++nTargets;
			}
			
			if (isSource(s)) {
				++nSources;
			}
			
			
			if (depends.containsKey(s)) {
				if (!isWeighted) {
					nEdges += depends.get(s).size();
				}
				else {
					for (String r: depends.get(s)) {
						nEdges += edgeWeights.get(r + "#" + s); 
					}
				}
			}
		}
	}
	
	public boolean isSource(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) >= SimpleModelDAG.sS) {
//			if (Integer.parseInt(node) <= 4) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return sources.contains(node);
		}
		else if (isCelegans) {
//			return ManagerNeuro.source.contains(node);
			return node.equals("1000");
		}
		else {
			return !depends.containsKey(node);
		}
	}
	
	public boolean isTarget(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) < SimpleModelDAG.sI) {
//			if (Integer.parseInt(node) > 8) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return targets.contains(node);
		}
		else if (isCelegans) {
//			return ManagerNeuro.target.contains(node);
			return node.equals("2000");
		}
		else {
			return !serves.containsKey(node);
		}
	}
	
	public boolean isIntermediate(String node) {
		if (isSynthetic) {
			if (Integer.parseInt(node) >= SimpleModelDAG.sI && Integer.parseInt(node) < SimpleModelDAG.sS) {
//			if (Integer.parseInt(node) > 4 && Integer.parseInt(node) < 9) {
				return true;
			}
			return false;
		}
		else if (isRandomized){
			return !targets.contains(node) && !sources.contains(node);
		}
		else if (isCelegans) {
//			return ManagerNeuro.intermediate.contains(node);
//			return !ManagerNeuro.source.contains(node) && !ManagerNeuro.target.contains(node);
			return !isTarget(node) && !isSource(node);
		}
		else {
			return depends.containsKey(node) && serves.containsKey(node);
		}
	}
		
	private void sourcePathsTraverse(String node) {
//		System.out.println(node);
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
			if (depends.containsKey(node)) {
				for (String s : depends.get(node)) {
					// if ((goodEdgeToSource.contains(node + "#" + s) || // !isCyclic)) // only for neuro
					{
						if (edgeSkip.contains(s + "," + node)) continue;
						sourcePathsTraverse(s);
						nPath += numOfSourcePath.get(s);
						sPath += numOfSourcePath.get(s) + sumOfSourcePath.get(s);
					}
				}
			}
		}
		
//		System.out.println("[S] " + node + "\t" + nPath + "\t" + sPath );
		numOfSourcePath.put(node, nPath);
		sumOfSourcePath.put(node, sPath);
		avgSourceDepth.put(node, sPath / nPath);
	}
	
	private void targetPathsTraverse(String node) {
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
//					if ((goodEdgeToTarget.contains(node + "#" + s) || !isCyclic)) 
					{		
						if (edgeSkip.contains(node + "," + s)) continue;
						targetPathsTraverse(s);
						nPath += numOfTargetPath.get(s);
						sPath += numOfTargetPath.get(s) + sumOfTargetPath.get(s);
					}
				}
			}
		}
		
//		System.out.println("[T] " + node + "\t" + nPath + "\t" + sPath );
		numOfTargetPath.put(node, nPath);
		sumOfTargetPath.put(node, sPath);
		avgTargetDepth.put(node, sPath / nPath);
	}
	
	private double getEdgeWeight(String n1, String n2) {
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
			if (depends.containsKey(node)) {
				for (String s : depends.get(node)) {
					if (edgeSkip.contains(s + "," + node)) {
						System.out.println("Skipped: " + s + "," + node);
						continue;
					}
					weightedSourcePathDepth(s);
					nPath += numOfSourcePath.get(s) * getEdgeWeight(s, node);
					sPath += (numOfSourcePath.get(s) + sumOfSourcePath.get(s)) * getEdgeWeight(s, node);
				}
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
					if (edgeSkip.contains(node + "," + s)) {
						System.out.println("Skipped: " + node + "," + s);
						continue;
					}
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
	}
	
	/*
	private void traverseFromSource(String node) {
		if (isTarget(node)) { // is target
			return;
		}
		
		visitedGray.add(node);
		if (serves.containsKey(node)) { 
			ArrayList<String> shuffledNodes = new ArrayList(serves.get(node));
			// do a random shuffling of nodes
			Collections.shuffle(shuffledNodes);
			for (String s : shuffledNodes) {
				if (!visitedGray.contains(s) || visitedBlack.contains(s)) {
//					System.out.println("Adding: " + node + "\t" + s);
					goodEdgeToTarget.add(node + "#" + s);
					goodEdgeToSource.add(s + "#" + node);
				}
				if (!visitedGray.contains(s) && !visitedBlack.contains(s)) {	
//					System.out.println("Visiting: " + s);
					traverseFromSource(s);
				}
			}
		}
		visitedBlack.add(node);
		visitedGray.remove(node);
	}
	
	private void traverseFromTarget(String node) {
		if (isSource(node)) { // is source
			return;
		}
		
		visitedGray.add(node);
		if (depends.containsKey(node)) { 
			ArrayList<String> shuffledNodes = new ArrayList(depends.get(node));
			// do a random shuffling of nodes
			Collections.shuffle(shuffledNodes);
			for (String s : shuffledNodes) {
				if (!visitedGray.contains(s) || visitedBlack.contains(s)) {
//					System.out.println("Adding: " + node + "\t" + s);
					goodEdgeToTarget.add(s + "#" + node);
					goodEdgeToSource.add(node + "#" + s);
				}
				if (!visitedGray.contains(s) && !visitedBlack.contains(s)) {	
//					System.out.println("Visiting: " + s);
					traverseFromTarget(s);
				}
			}
		}		
		visitedBlack.add(node);
		visitedGray.remove(node);
	}
	
	private void loadGoodEdge(String node, String direction) {
		goodEdgeToSource = new HashSet();
		goodEdgeToTarget = new HashSet();
		
		visitedGray = new HashSet();
		visitedBlack = new HashSet();
		
		if (direction.equals("fromSource")) {
			traverseFromSource(node);
		}
		
		if (direction.equals("fromTarget")) {
			traverseFromTarget(node);
		}
		
//		System.out.println(goodEdgeToTarget);
//		System.out.println(goodEdgeToSource);
	}
	*/
	
	private void initPathTraversalDS() {
		numOfTargetPath.clear();
		numOfSourcePath.clear();
		sumOfSourcePath.clear();
		sumOfTargetPath.clear();
		avgTargetDepth.clear();
		avgSourceDepth.clear();
	}
	
	public void loadCyclicPathStatistics() {
//		HashMap<String, Double> edgeCentralityMap = new HashMap(); // for edge centrality
//		nTotalPath = 0;
//		nodePathThrough.clear();
//		
//		for (String s: nodes) { // for every source DAG
//			if (!isSource(s)) continue;
//			initPathTraversalDS();
//			
//			loadGoodEdge(s, "fromSource");
////			System.out.println("Starting towards target traversal from " + s);
//			targetPathsTraverse(s);
//		
//			for (String r: nodes) { // from targets to source for this DAG, needed for total paths in this DAG
//				if (!isTarget(r)) continue;
//				sourcePathsTraverse(r);
//			}
//			
//			for (String r : nodes) {
//				double numSourcePath = 0;
//				if (numOfSourcePath.containsKey(r)) {
//					numSourcePath = numOfSourcePath.get(r);
//				}
//				double numTargetPath = 0;
//				if (numOfTargetPath.containsKey(r)) {
//					numTargetPath = numOfTargetPath.get(r);
//				}
////				System.out.println(r + "\t" + tPath + "\t" + sPath);
//				double numPath = numSourcePath * numTargetPath;
//				if (nodePathThrough.containsKey(r)) {
//					numPath += nodePathThrough.get(r);
//				}
//				nodePathThrough.put(r, numPath);
//				
//				if (cyclicNumSourcePath.containsKey(r)) {
//					cyclicNumSourcePath.put(r, numSourcePath + cyclicNumSourcePath.get(r));
//				}
//				else {
//					cyclicNumSourcePath.put(r, numSourcePath);
//				}
//				
//				// for weighted path centrality
//				if (avgSourceDepth.containsKey(r) && !Double.isNaN(avgSourceDepth.get(r))) {
//					if (cyclicAvgSourceDepth.containsKey(r)) {
//						cyclicAvgSourceDepth.put(r, (cyclicAvgSourceDepth.get(r) + avgSourceDepth.get(r)) * 0.5);
//					}
//					else {
//						cyclicAvgSourceDepth.put(r, avgSourceDepth.get(r));
//					}
//				}
//			}
//			
//			nTotalPath += numOfTargetPath.get(s);		
//		
//			/* edge centrality - begin */
////			for (String r : nodes) {
////				if (isTarget(r)) continue;
////				for (String t : serves.get(r)) {
////					double numSourcePath = 0;
////					if (numOfSourcePath.containsKey(r)) {
////						numSourcePath = numOfSourcePath.get(r);
////					}
////					double numTargetPath = 0;
////					if (numOfTargetPath.containsKey(t)) {
////						numTargetPath = numOfTargetPath.get(t);
////					}
////					double numEdgePath = numSourcePath * numTargetPath;
////					String edge = r + "#" + t;
////					if (edgeCentralityMap.containsKey(edge)) {
////						edgeCentralityMap.put(edge, edgeCentralityMap.get(edge) + numEdgePath);
////					}
////					else {
////						edgeCentralityMap.put(edge, numEdgePath);
////					}
////				}
////			}
//			/* edge centrality - end */
//		
//		}
		
		/* edge centrality - begin */
//		for (String e : edgeCentralityMap.keySet()) {
//			String r = e.substring(0, e.indexOf('#'));
//			String t = e.substring(e.indexOf('#') + 1);
//			Edge edge = new Edge(r, t, edgeCentralityMap.get(e));
//			edgePathCentrality.add(edge);
//		}	
		/* edge centrality - end */
		
		/* number of paths from targets - begin */
//		for (String s: nodes) {
//			if (!isTarget(s)) continue;
//			initPathTraversalDS();
//			loadGoodEdge(s, "fromTarget");
//			
//			for (String r: nodes) {
//				if (!isSource(r)) continue;
//				targetPathsTraverse(r);
//			}
//			
//			for (String r: nodes) {
//				double numTargetPath = 0;
//				if (numOfTargetPath.containsKey(r)) {
//					numTargetPath = numOfTargetPath.get(r);
//				}
//				
//				if (cyclicNumTargetPath.containsKey(r)) {
//					cyclicNumTargetPath.put(r, numTargetPath + cyclicNumTargetPath.get(r));
//				}
//				else {
//					cyclicNumTargetPath.put(r, numTargetPath);
//				}
//				
//				if (avgTargetDepth.containsKey(r)  && !Double.isNaN(avgTargetDepth.get(r))) {
//					if (cyclicAvgTargetDepth.containsKey(r)) {
//						cyclicAvgTargetDepth.put(r, (cyclicAvgTargetDepth.get(r) + avgTargetDepth.get(r)) * 0.5);
//					}
//					else {
//						cyclicAvgTargetDepth.put(r, avgTargetDepth.get(r));
//					}
//				}
//			}
//		}
//		/* number of target paths - end */
//		
////		System.out.println("Total path: " + nTotalPath);		
////		for (String r: nodes) {
////			System.out.println(r + "\t" + nodePathThrough.get(r) + "\t" + cyclicNumSourcePath.get(r) + "\t" + cyclicNumTargetPath.get(r));
////			System.out.println(r + "\t" + nodePathThrough.get(r) + "\t" + cyclicAvgSourceDepth.get(r) + "\t" + cyclicAvgTargetDepth.get(r));
////			if (isSource(r)) {
////				System.out.println(r + "\t" + avgTargetDepth.get(r));
////			}
////		}
////		System.out.println("--------");
	}
	
	public void loadRegularPathStatistics() {
		numOfTargetPath.clear();
		numOfSourcePath.clear();

		visited = new HashSet();
		for (String s: nodes) {
			sourcePathsTraverse(s);
		}
				
		visited = new HashSet();
		for (String s: nodes) {
			targetPathsTraverse(s);
		}		
	}

	public void loadPathStatistics() {
//		System.out.println("In path stat: " + isWeighted + "\t" + CoreDetection.topRemovedWaistNodes);
		if (isWeighted) {
			loadWeightedPathStatistics();
		}
		else if (isCyclic) {
//			loadCyclicPathStatistics();
			return;
		}
		else {
			loadRegularPathStatistics();
		}

		nTotalPath = 0;
		for (String s : nodes) {
			double nPath = 0;
			if (numOfTargetPath.containsKey(s) && numOfSourcePath.containsKey(s)) {
				nPath = numOfTargetPath.get(s) * numOfSourcePath.get(s);
//				System.out.println(s + "\t" + numOfTargetPath.get(s) + "\t" + numOfSourcePath.get(s));
			}
			else {
//				System.out.println("Disconnected: " + s);
			}
			
			nodePathThrough.put(s, nPath);
			if (isSource(s)) { // is a target
				nTotalPath += nPath;
//				nTotalPath = nTotalPath.add(new BigDecimal(nPath));
			}
		}
		
		/* edge path computation */
		for (String s : nodes) {
			if (!serves.containsKey(s)) continue;
			for (String r : serves.get(s)) {
				if (isCelegans) {
					if (s.equals("1000") || r.equals("1000")) continue;
					if (s.equals("2000") || r.equals("2000")) continue;
				}
				double numEdgePath = numOfSourcePath.get(s) * numOfTargetPath.get(r);
				Edge e = new Edge(s, r, numEdgePath);
				edgePathCentrality.add(e);
//				System.out.println(s + "\t" + r + "\t" + numEdgePath);
			}
		}	
		Collections.sort(edgePathCentrality);
		
//		System.out.println("Total path: " + nTotalPath);
		
//		if (DependencyDAG.isCelegans) {
//			nTotalPath = 2197933; // wrong
//		}
	}
		
	public void loadLocationMetric() {
		for (String s : nodes) {
//			System.out.println(s + "\t" + avgSourceDepth.get(s) + "\t" + avgTargetDepth.get(s));
			if (!isCyclic) // old style
			{
				double m = avgSourceDepth.get(s) / (avgTargetDepth.get(s) + avgSourceDepth.get(s));
				m = ((int) (m * 1000.0)) / 1000.0; // round up to 2 decimal point
				lengthPathLocation.put(s, m);
			}
			
			
			double n = 0;
			if (isCyclic) {
//				n = (cyclicNumSourcePath.get(s) - 1) / ((cyclicNumSourcePath.get(s) - 1) + (cyclicNumTargetPath.get(s) - 1));
			}
			else {
				n = (numOfSourcePath.get(s) - 1) / ((numOfSourcePath.get(s) - 1) + (numOfTargetPath.get(s) - 1));
			}
			n = ((int) (n * 100.0)) / 100.0; // round up to 2 decimal point
			numPathLocation.put(s, n);			
		}		
	}
		
	public void reachableUpwardsNodes(String node) { // towards targets
		if (visited.contains(node)) { // node already traversed
			return;
		}
		visited.add(node);

//		++kounter;
//		if (isTarget(node)) { // is a target
//			return;
//		}
		
		if (!serves.containsKey(node)) {
			return;
		}
		
		for (String s : serves.get(node)) {
			reachableUpwardsNodes(s);
		}
	}
	
	public void reachableDownwardsNodes(String node) { // towards sources
		if (visited.contains(node)) { // node already traversed
			return;
		}
		visited.add(node);
		
//		++kounter;
//		if (isSource(node)) { // is a source
//			return;
//		}
		
		if (!depends.containsKey(node)) {
			return;
		}
		
		for (String s : depends.get(node)) {
			reachableDownwardsNodes(s);
		}
	}
	
	public void loadReachablityAll() {
		visited = new HashSet();
		connectedSourceTargetPair = 0;
		for (String s : nodes) {
			/*
			visited.clear();
			kounter = 0;
			reachableUpwardsNodes(s); // how many nodes are using her
			visited.remove(s); // remove ifself
//			dependentsReachable.put(s, new HashSet(visited)); // too heavy for court case
			targetsReachableKount.put(s, kounter);
			
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
//			serversReachable.put(s, new HashSet(visited)); // too heavy for court case
			sourcesReachableKount.put(s, kounter);
			*/
			
			loadReachability(s);
			if (isSource(s)) {
				connectedSourceTargetPair += targetsReachableKount.get(s);
			}
		}
	}
	
	public void loadServerReachabilityAll() {
		visited = new HashSet();
		for (String s : nodes) {
			visited.clear();
			kounter = 0;
			reachableDownwardsNodes(s); // how many nodes she is using
			visited.remove(s); // remove itself
			ancestors.put(s, new HashSet(visited)); // too heavy for court case
		}
	}
	
	public void loadReachability(String node) {
			visited = new HashSet();
			
			visited.clear();
			reachableUpwardsNodes(node); // how many nodes are using her
			nodesReachable.put(node, new HashSet(visited));
//			visited.remove(node); // remove ifself
			successors.put(node, new HashSet(visited)); // too heavy for court case
			kounter = 0;
			targetsReachable.put(node, new HashSet());
			for (String r: successors.get(node)) {
				if (isTarget(r)) {
					++kounter;
					targetsReachable.get(node).add(r);
				}
			}
			targetsReachableKount.put(node, kounter);
//			System.out.println("Targets reached: " + kounter);
			
			visited.clear();
			reachableDownwardsNodes(node); // how many nodes she is using
			nodesReachable.get(node).addAll(visited);
//			visited.remove(node); // remove itself
			ancestors.put(node, new HashSet(visited)); // too heavy for court case
			kounter = 0;
			sourcesReachable.put(node, new HashSet());
			for (String r: ancestors.get(node)) {
				if (isSource(r)) {
					++kounter;
					sourcesReachable.get(node).add(r);
				}
			}
			sourcesReachableKount.put(node, kounter);
//			System.out.println("Source reached: " + kounter);
	}
		
	public void loadPathCentralityMetric() {
		int tubes = 0;
		for (String s: nodes) {			
//			P-Centrality
//			double npc = numOfTargetPath.get(s) * numOfSourcePath.get(s) * 1.0 / nTotalPath;
			
			double npc = nodePathThrough.get(s) / nTotalPath;
//			double npc = new BigDecimal(nodePathThrough.get(s)).divide(nTotalPath).doubleValue();
			
//			npc = ((int) npc * 1000.0) / 1000.0;
			normalizedPathCentrality.put(s, npc);			
			
//			if (isSource(s) || isTarget(s)) { 
//				manually reset source and targets to zero
//				normalizedPathCentrality.put(s, 0.0);
//			}
			
			if (isCyclic) {
//				lengthWeightedPathCentrality.put(s, nodePathThrough.get(s) / (cyclicAvgSourceDepth.get(s) + cyclicAvgTargetDepth.get(s)));
			}
		}			
		
//		System.out.println("Tubes: " + tubes);
	}
	
	public void printNetworkProperties() {
		for (String s: nodes) {
			if (DependencyDAG.isCelegans && (s.equals("1000") || s.equals("2000"))) continue;
//			if (normalizedPathCentrality.get(s) < 0.4) continue;
//			System.out.print("Node: " + s + "\t");
//			System.out.print("Complexity: " + numOfSourcePath.get(s) + "\t");
//			System.out.print("Generality: " + numOfTargetPath.get(s) + "\t");
//			System.out.print("Location: " + numPathLocation.get(s) + "\t");
//			System.out.print("Path Centrality: " + normalizedPathCentrality.get(s) * nTotalPath + "\t");
//			System.out.print(inDegree.get(s) + "\t");
//			System.out.print(outDegree.get(s) + "\t");
//			System.out.print(pagerankTargetCompression.get(s) + "\t");
//			System.out.print(pagerankSourceCompression.get(s) + "\t");
//			System.out.print(harmonicMeanPagerankCentrality.get(s) + "\t");
//			System.out.print(iCentrality.get(s) + "\t");
//			System.out.print(sourcesReachable.get(s) + "\t");
//			System.out.print(targetsReachable.get(s) + "\t");
//			System.out.println(s + "\t" + normalizedPathCentrality.get(s));
//			System.out.println();
			System.out.println(s + "\t" + nodePathThrough.get(s));
//			System.out.println(s + "\t" + numPathLocation.get(s) + "\t" + lengthPathLocation.get(s));
//			System.out.println(s + "\t" + numPathLocation.get(s) + "\t" + normalizedPathCentrality.get(s));
//			System.out.println(s + "\t" + numPathLocation.get(s));
//			System.out.println(numPathLocation.get(s) + "\t" + Math.random() + "\t" + Math.log10(nodePathThrough.get(s)));
//			if (isCyclic) {
//				System.out.println(s + "\t" + cyclicAvgSourceDepth.get(s) + "\t" + cyclicAvgTargetDepth.get(s));
//				System.out.println(s + "\t" + lengthWeightedPathCentrality.get(s) + "\t" + nodePathThrough.get(s));
//				System.out.println(s + "\t" + nodePathThrough.get(s));				
//			}
//			System.out.println(nodePathThrough.get(s));
//			System.out.println(s + "\t" + numPathLocation.get(s) + "\t" + normalizedPathCentrality.get(s));
//			System.out.println(s + "\t" + lengthPathLocation.get(s) + "\t" + normalizedPathCentrality.get(s) + "\t" + outDegree.get(s));
			
//			System.out.println();
			if (isIntermediate(s)) {
//				System.out.println(s + "\t" + outDegree.get(s) + "\t" + normalizedPathCentrality.get(s));
//				System.out.println(s);
			}
			
			
//			System.out.print(s + "\t" + sourcesReachableKount.get(s) + "\t" + targetsReachableKount.get(s));
//			System.out.println("\t" + sourcesReachableKount.get(s) * targetsReachableKount.get(s));
		}
		
//		System.out.println("Total path: " + nTotalPath);
//		System.out.println("Total ST pair: " + connectedSourceTargetPair);
		
//		for (String s : nodes) {
//			if (depends.containsKey(s)) {
//				System.out.print(s + " depends on ");
//				for (String r : depends.get(s)) {
//					System.out.print("\t" + r);
//				}
//				System.out.println();
//			}
//			
//			if (serves.containsKey(s)) {
//				System.out.print(s + " serves ");
//				for (String r : serves.get(s)) {
//					System.out.print("\t" + r);
//				}
//				System.out.println();
//			}
//		}
		
		
//		String[] jetumlCore = new String[]{"ca.mcgill.cs.stg.jetuml.TestUMLEditor", "ca.mcgill.cs.stg.jetuml.graph.Node",
//				"ca.mcgill.cs.stg.jetuml.graph.CallNode", "ca.mcgill.cs.stg.jetuml.graph.Graph", 
//				"ca.mcgill.cs.stg.jetuml.framework.EditorFrame", "ca.mcgill.cs.stg.jetuml.diagrams.ClassDiagramGraph"};
//			
//		for (String r: jetumlCore) {
//			System.out.println(r + "\t" + normalizedPathCentrality.get(r) + "\t" + numPathLocation.get(r));
//		}
//		System.out.println();
	}
	
	public class PathCentralityComparator<String> implements Comparator<String> {
		@Override
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
		isLexis = false;
		isCyclic = false;
//		nDirectSourceTargetEdges = 0;
	}

	public void initPathStat() {
		nTotalPath = 0;

		numOfTargetPath = new HashMap();
		sumOfTargetPath = new HashMap();
		avgTargetDepth = new HashMap();
		numOfSourcePath = new HashMap();
		sumOfSourcePath = new HashMap();
		avgSourceDepth = new HashMap();

		lengthPathLocation = new HashMap();
		numPathLocation = new HashMap();

		nodePathThrough = new HashMap();
		normalizedPathCentrality = new HashMap();

		successors = new HashMap();
		ancestors = new HashMap();
		
//		targetsReachable = new HashMap();
//		sourcesReachable = new HashMap();
//		nodesReachable = new HashMap();	

		visited = new HashSet();
		
		edgeWeights = new HashMap();
		
		edgePathCentrality = new ArrayList();
	}
	
	public void initAuxiliary() {
		targets = new HashSet();
		sources = new HashSet();
				
		numOfTargetPath = new HashMap();
		sumOfTargetPath = new HashMap();
		avgTargetDepth = new HashMap();
		numOfSourcePath = new HashMap();
		sumOfSourcePath = new HashMap();
		avgSourceDepth = new HashMap();
		lengthPathLocation = new HashMap();

		nodePathThrough = new HashMap();
		normalizedPathCentrality = new HashMap();
		centralityRank = new HashMap();
		lengthWeightedPathCentrality = new HashMap();
		
		outDegree = new HashMap();
		inDegree = new HashMap();
				
		detectedCycles = new ArrayList();
		cycleEdges = new HashMap();

		successors = new HashMap();
		ancestors = new HashMap();
		
		targetsReachableKount = new HashMap();
		sourcesReachableKount = new HashMap();
		targetsReachable = new HashMap();
		sourcesReachable = new HashMap();

		
		nodesReachable = new HashMap();	
		visited = new HashSet();
		
		CoreDetection.topRemovedWaistNodes.clear();
		
		edgeWeights = new HashMap();		
		numPathLocation = new HashMap();
		
		edgePathCentrality = new ArrayList();
		edgeSkip = new HashSet();
//		goodEdgeToSource = new HashSet();
//		goodEdgeToTarget = new HashSet();
//		cyclicNumSourcePath = new HashMap();
//		cyclicNumTargetPath = new HashMap();
//		cyclicAvgSourceDepth = new HashMap();
//		cyclicAvgTargetDepth = new HashMap();
//		largestWCCNodes = new HashSet();
//		geometricMeanPagerankCentrality = new HashMap();
//		harmonicMeanPagerankCentrality = new HashMap();
//		pagerankSourceCompression = new HashMap();
//		pagerankTargetCompression = new HashMap();
//		geometricMeanPathCentrality = new HashMap();
//		harmonicMeanPathCentrality = new HashMap();		
//		iCentrality = new HashMap();
	}
	
	public void init() {		
		nodes = new TreeSet();
		serves = new HashMap();
		depends = new HashMap();
		initAuxiliary();
	}
	
	public void printNetworkStat() {
		System.out.println(" S: " + nSources);
		System.out.println(" T: " + nTargets);
		System.out.println(" E: " + nEdges);
		System.out.println(" N: " + nodes.size());
//		System.out.println(" Toal Path: " + nTotalPath);	
		
		if (DependencyDAG.isCelegans) {
//			System.out.println(nodePathThrough.get("1000") + "\t" + nodePathThrough.get("2000"));
		}
	}
	
	public String getNodeType(String node) {
		if (isSource(node)) return "source";
		else if (isTarget(node)) return "target";
		return "intermediate";
	}
	
	public void printLinks(String id) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("data//" + id + "_links.txt"));
		for (String s : nodes) {
			if (serves.containsKey(s)) {
				for (String r : serves.get(s)) {
					pw1.println(s + "\t" + r);
				}
			}
		}
		pw1.close();
	}
	
	public void printNetwork(String id) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("data//" + id + "_links.txt"));
		PrintWriter pw2 = new PrintWriter(new File("data//" + id + "_sources.txt"));
		PrintWriter pw3 = new PrintWriter(new File("data//" + id + "_targets.txt"));
		for (String s : nodes) {
			if (serves.containsKey(s)) {
				for (String r : serves.get(s)) {
					pw1.println(s + "\t" + r);
				}
			}
			else {
				pw3.println(s);
			}
			
			if (!depends.containsKey(s)) {
				pw2.println(s);
			}
		}
		pw1.close();
		pw2.close();
		pw3.close();
	}
}
