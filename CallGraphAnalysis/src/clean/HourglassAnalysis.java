package clean;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import corehg.DependencyDAG;

//import corehg.DependencyDAG;

public class HourglassAnalysis {
	public class DependencyGraph{
		private HashSet<String> nodes;
		private HashSet<String> targets;
		private HashSet<String> sources;
		private HashMap<String, HashSet<String>> serves; 
		private HashMap<String, HashSet<String>> depends;
		private HashMap<String, Integer> nodeNumberMap;
		private int nodeNumber = 0;
		
		private HashMap<String, Double> numOfTargetPath;
		private HashMap<String, Double> numOfSourcePath;
		private HashMap<String, Double> nodePathThrough;
		private double nTotalPath;
		
		private double pathCoverageTau = 1.0;
		public HashSet<String> coreNodes;
		private HashSet<String> skipNodes;
		
		private boolean isWeighted;
		private HashMap<String, Double> edgeWeights;
		
		private HashMap<String, Double> corePathContribution;
		private HashMap<String, Double> location;
		private HashSet<String> visited;
		private boolean canReachSource;
		private boolean canReachTarget;
		
		private int wccSize;
		
		public DependencyGraph(boolean isWeighted) { 
			nodes = new HashSet();
			serves = new HashMap();
			depends = new HashMap();
			targets = new HashSet();
			sources = new HashSet();
			numOfTargetPath = new HashMap();
			numOfSourcePath = new HashMap();
			nodePathThrough = new HashMap();
			skipNodes = new HashSet();
			edgeWeights = new HashMap();
			this.isWeighted = isWeighted;
			location = new HashMap();
			corePathContribution = new HashMap();
			nodeNumberMap = new HashMap();
			nodeNumber = 0;
		}
		
		private void WCCHelper(String s) {
			// if (visited.contains(s)) return;
			++wccSize;
			visited.add(s);

			if (serves.containsKey(s)) {
				for (String r : serves.get(s)) {
					if (!visited.contains(r)) {
						WCCHelper(r);
					}
				}
			}

			if (depends.containsKey(s)) {
				for (String r : depends.get(s)) {
					if (!visited.contains(r)) {
						WCCHelper(r);
					}
				}
			}
		}

		private void findWeaklyConnectedComponents(String filePath) throws Exception {
			PrintWriter pw = new PrintWriter(new File("data//largestWCC-" + filePath + ".txt"));
			int largestWCCSize = 0;
			String largestWCCSeed = "";
			visited = new HashSet();
			int nWCC = 0;
			for (String s : nodes) {
				if (!visited.contains(s)) {
					wccSize = 0;
					++nWCC;
					WCCHelper(s);
					System.out.println("Component " + nWCC + " with size " + wccSize);
					if (wccSize > largestWCCSize) {
						largestWCCSize = wccSize;
						largestWCCSeed = s;
					}
				}
			}

			System.out.println("Largest WCC size: " + largestWCCSize);
			visited.clear();
			WCCHelper(largestWCCSeed);
			for (String s : visited) {
				pw.println(s);
			}
			pw.close();
		}
		
		public DependencyGraph(String dependencyGraphFilePath, String sourceFilePath, String targetFilePath, boolean isWeighted) throws Exception {
			this(isWeighted);
			loadNetwork(dependencyGraphFilePath);
			loadSources(sourceFilePath);
			loadTargets(targetFilePath);
			// special case
			if (!isWeighted) {
//				mergeNodes("107", "156", 1);
//				mergeNodes("82", "99", 1);
//				mergeNodes("126", "154", 1);
//				mergeNodes("164", "169", 1);
//				mergeNodes("177", "278", 1);
//				mergeNodes("263", "265", 1);
			}
			// end special case
			
			findWeaklyConnectedComponents("rat");
			/*
			addSuperSourceTarget();
			getPathStats();
			getCore();
			if (!isWeighted) {
				getFlatNetwork();
			}
			*/
		}
		
		private void mergeHelper(String n1, String m) {
			if (depends.containsKey(n1)) {
				HashSet<String> temp = new HashSet(depends.get(n1));
				for (String s: temp) {
					addEdge(s, m);
					removeEdge(s, n1);
				}
			}
			if (serves.containsKey(n1)) {
				HashSet<String> temp = new HashSet(serves.get(n1));
				for (String s: temp) {
					addEdge(m, s);
					removeEdge(n1, s);
				}
			}
			nodes.remove(n1);
			if (sources.contains(n1)) {
				sources.remove(n1);
				sources.add(m);
			}
			if (targets.contains(n1)) {
				targets.remove(n1);
				targets.add(m);
			}
		}
		
		private void mergeNodes(String n1, String n2, int removeFlag) {
			if (removeFlag == 1) {
				removeEdge(n1, n2);
				removeEdge(n2, n1);
				return;
			}
			String m = n1+"#"+n2;
			mergeHelper(n1, m);
			mergeHelper(n2, m);
		}
		
		private void addEdge(String server, String dependent) {
			nodes.add(dependent);
			nodes.add(server);
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
		
		private void removeEdge(String server, String dependent) {
			serves.get(server).remove(dependent);
			depends.get(dependent).remove(server);
		}
		
		private void addSuperSourceTarget() {
			for (String s: sources) {
				addEdge("superSource", s);
				if (isWeighted) {
					edgeWeights.put("superSource#" + s, 1.0);
				}
			}
			for (String t: targets) {
				addEdge(t, "superTarget");
				if (isWeighted) {
					edgeWeights.put(t + "#superTarget", 1.0);
				}
			}
		}
		
		private void loadTargets(String fileName) throws Exception {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNext()) {
				targets.add(scanner.next());
			}
			scanner.close();
		}
		
		private void loadSources(String fileName) throws Exception {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNext()) {
				sources.add(scanner.next());
			}
			scanner.close();
		}
		
		private void loadNetwork(String fileName) throws Exception {
			Scanner scanner = new Scanner(new File(fileName));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String tokens[] = line.split("\\s+");
				String server = tokens[0];
				String dependent = tokens[1];
				if (isWeighted) {
					double weight = Double.parseDouble(tokens[2]);
					edgeWeights.put(server + "#" + dependent, weight);
				}
				addEdge(server, dependent);
				if (!nodeNumberMap.containsKey(server)) nodeNumberMap.put(server, nodeNumber++);
				if (!nodeNumberMap.containsKey(dependent)) nodeNumberMap.put(dependent, nodeNumber++);
			}
			scanner.close();
		}
		
		private void sourcePathsTraverse(String node) {
			if (numOfSourcePath.containsKey(node)) { // node already traversed
				return;
			}
			double nPath = 0;
			if (!skipNodes.contains(node)) {
				if (node.equals("superSource")) {
					++nPath;
				}
				if (depends.containsKey(node)) {
					for (String s : depends.get(node)) {
						sourcePathsTraverse(s);
						double weight = 1;
						if (isWeighted) {
							weight = edgeWeights.get(s + "#" + node);
						}
						nPath += numOfSourcePath.get(s) * weight;
					}
				}
			}
			numOfSourcePath.put(node, nPath);
		}
		
		private void targetPathsTraverse(String node) {
			if (numOfTargetPath.containsKey(node)) { // node already traversed
				return;
			}
			double nPath = 0;
			if (!skipNodes.contains(node)) {
				if (node.equals("superTarget")) {
					++nPath;
				}
				if (serves.containsKey(node)) {
					for (String s : serves.get(node)) {
						targetPathsTraverse(s);
						double weight = 1;
						if (isWeighted) {
							weight = edgeWeights.get(node + "#" + s);
						}
						nPath += numOfTargetPath.get(s) * weight;
					}
				}
			}
			numOfTargetPath.put(node, nPath);
		}
		
		private void pathStatisticsHelper() {
			numOfSourcePath.clear();
			for (String s: nodes) {
				sourcePathsTraverse(s);
			}
			numOfTargetPath.clear();
			for (String s: nodes) {
				targetPathsTraverse(s);
			}		
		}

		private void getPathStats() {
			pathStatisticsHelper();
			nTotalPath = 0;
			for (String s : nodes) {
				double nPath = 0;
				if (numOfSourcePath.containsKey(s) && numOfTargetPath.containsKey(s)) {
					nPath = numOfSourcePath.get(s) * numOfTargetPath.get(s);
				}
				nodePathThrough.put(s, nPath);
				
				double loc  = (numOfSourcePath.get(s) - 1) / ((numOfSourcePath.get(s) - 1) + (numOfTargetPath.get(s) - 1));
				loc = ((int) (loc * 100.0)) / 100.0; // round up to 2 decimal point
				location.put(s, loc);
			}
			nTotalPath = nodePathThrough.get("superSource");
		}
		
		private boolean isSuperNode(String n) {
			if (n.equals("superSource") || n.equals("superTarget")) {
				return true;
			}
			return false;
		}
			
		public void printNetworkProperties() throws Exception {
//			PrintWriter pw = new PrintWriter(new File("hourglassAnalysis.txt"));
			for (String s: nodes) {
				if (isSuperNode(s)) continue;
//				System.out.println(s + "\tComplexity: " + numOfSourcePath.get(s) + "\tGenerality: " + numOfTargetPath.get(s) + "\tPath_centrality: " + nodePathThrough.get(s));
				System.out.println(s + "\t" + nodePathThrough.get(s));
			}
			System.out.println("Total_paths: " + nTotalPath);
//			System.out.println("Core_size: " + coreNodes.size() + "\t" + "Core_set: " + coreNodes);
//			pw.close();
		}
		
		private void getCore() {
			greedyTraverse(0, nTotalPath);
			// resetting everything
			skipNodes.clear();
			getPathStats(); 
		}
		
		private void greedyTraverse(double cumulativePathCovered, double nPath) {			
			if (!(cumulativePathCovered < nPath * pathCoverageTau)) {
				coreNodes = new HashSet(skipNodes);
				return;
			}
			double maxPathCovered = -1;
			String maxPathCoveredNode = "";
			boolean tied = false;
			for (String s : nodes) {
				if (isSuperNode(s)) continue;
				double numPathCovered = nodePathThrough.get(s);				
				if (numPathCovered > maxPathCovered) {
					maxPathCovered = numPathCovered;
					maxPathCoveredNode = s;
				}
				else if (numPathCovered == maxPathCovered) {
					tied = true;
				}
			}				
			skipNodes.add(maxPathCoveredNode);
			getPathStats();
			if (!isWeighted) {
//				System.out.println(maxPathCoveredNode + "\t" + ((cumulativePathCovered + maxPathCovered) / nPath));
				if (tied) {
//					System.out.println(tied);
				}
			}
			corePathContribution.put(maxPathCoveredNode, maxPathCovered);
			greedyTraverse(cumulativePathCovered + maxPathCovered, nPath);
		}
		
		private void getFlatNetwork() throws Exception {
			PrintWriter pw = new PrintWriter(new File("data//flat.txt"));			
			for (String s: sources) {
				removeEdge("superSource", s);
			}
			for (String s: sources) {
				addEdge("superSource", s);
				getPathStats();
				for (String r: targets) {
					if (nodePathThrough.get(r) > 0) {
						pw.println(s + "\t" + r + "\t" + numOfSourcePath.get(r));
					}
				}
				removeEdge("superSource", s);
			}
			pw.close();
			// resetting everything
			for (String s: sources) {
				addEdge("superSource", s);
			}
			getPathStats(); 
		}
		
		private double getCoreWeightedLocation() {
			double weightedCoreLocation = 0;
			double totalCoreContribution = 0;
			for (String s: coreNodes) {
				weightedCoreLocation += location.get(s) * corePathContribution.get(s);
				totalCoreContribution += corePathContribution.get(s);
			}
			weightedCoreLocation /= totalCoreContribution;
			return weightedCoreLocation;
		}
		
		private double getCoreNodeCoverage() {
			HashSet<String> coreNodeCoverage = new HashSet();
			for (String s : coreNodes) {
				visited = new HashSet();
				reachableUpwardsNodes(s); // how many nodes are using her
				coreNodeCoverage.addAll(visited);
				visited = new HashSet();
				reachableDownwardsNodes(s); // how many nodes is used by her
				coreNodeCoverage.addAll(visited);
			}
		
			double numerator = 0;
			double denominator = 0;
			for (String s: nodes) {
				canReachSource = false;
				canReachTarget = false;
				visited.clear();
				reachableUpwardsNodes(s);
				reachableDownwardsNodes(s);
				visited.clear();
				// check if a node is in a source/target path (for model networks)
				if (canReachSource && canReachTarget) {
					++denominator;
					if (coreNodeCoverage.contains(s)) {
						++numerator;
					}
				}
			}
			double nodeCoverage = numerator / denominator;
			return nodeCoverage;
		}
		
		private void reachableUpwardsNodes(String node) { // towards targets
			if (visited.contains(node)) { // node already traversed
				return;
			}
			if (sources.contains(node)) canReachSource = true;
			if (targets.contains(node)) canReachTarget = true;
			visited.add(node);
			if (!serves.containsKey(node)) {
				return;
			}
			for (String s : serves.get(node)) {
				reachableUpwardsNodes(s);
			}
		}
		
		private void reachableDownwardsNodes(String node) { // towards sources
			if (visited.contains(node)) { // node already traversed
				return;
			}
			visited.add(node);
			if (!depends.containsKey(node)) {
				return;
			}
			for (String s : depends.get(node)) {
				reachableDownwardsNodes(s);
			}
		}
	}
	
	public DependencyGraph dependencyDAG;
	public DependencyGraph flatDAG;
	public void runAnalysis(String data) throws Exception {
		String dependencyDAGFile = "data//" + data + "_links.txt";
		String sourceFile = "data//" + data + "_sources.txt";
		String targetFile = "data//" + data + "_targets.txt";
		dependencyDAG = new DependencyGraph(dependencyDAGFile, sourceFile, targetFile, false);
		System.out.println("Original_network");
		dependencyDAG.printNetworkProperties();
//		System.out.println(dependencyDAG.coreNodes);
		for (String s : dependencyDAG.corePathContribution.keySet()) {
			System.out.println(s + "\t" + (dependencyDAG.corePathContribution.get(s)/dependencyDAG.nTotalPath));
		}
		String flatDAGFile = "data//flat.txt";
		flatDAG = new DependencyGraph(flatDAGFile, sourceFile, targetFile, true);
//		System.out.println("Flat_network");
//		flatDAG.printNetworkProperties();
		double hScore = (1.0 - dependencyDAG.coreNodes.size() * 1.0 / flatDAG.coreNodes.size());
//		System.out.println(flatDAG.coreNodes);
		System.out.println(dependencyDAG.coreNodes.size() + "\t" + hScore);
//		System.out.println(dependencyDAG.getCoreWeightedLocation() + "\t" + dependencyDAG.getCoreNodeCoverage());
	}
	
	public static void main(String[] args) throws Exception {
		HourglassAnalysis hgAnalysis = new HourglassAnalysis();
		hgAnalysis.runAnalysis("rat");
	}
}
