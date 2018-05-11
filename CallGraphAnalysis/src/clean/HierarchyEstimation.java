package clean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class HierarchyEstimation {
	private class DependencyGraph{
		private HashSet<String> nodes;
		private HashSet<String> targets;
		private HashSet<String> sources;
		private HashMap<String, HashSet<String>> serves; 
		private HashMap<String, HashSet<String>> depends;		
		
		public DependencyGraph() { 
			nodes = new HashSet();
			serves = new HashMap();
			depends = new HashMap();
			targets = new HashSet();
			sources = new HashSet();
		}
		
		public DependencyGraph(String dependencyGraphFilePath, String sourceFilePath, String targetFilePath) throws Exception {
			this();
			loadNetwork(dependencyGraphFilePath);
			loadSources(sourceFilePath);
			loadTargets(targetFilePath);
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
				addEdge(server, dependent);
			}
			scanner.close();
		}
		
			
		private void printNetworkProperties() throws Exception {
//			PrintWriter pw = new PrintWriter(new File("hourglassAnalysis.txt"));
			for (String s: nodes) {
				System.out.print("[node] "  + s);
				if (serves.containsKey(s)) {
					System.out.print("   [serves]");
					for (String r : serves.get(s)) {
						System.out.print("  " + r);
					}
				}
				if (depends.containsKey(s)) {
					System.out.print("   [depends]");
					for (String r : depends.get(s)) {
						System.out.print("  " + r);
					}
					
				}
				System.out.println();
			}
	
			System.out.print("[sources]");
			for (String s : sources) {
				System.out.print("  " + s);
			}
			System.out.println();
			
			System.out.print("[targets]");
			for (String s : targets) {
				System.out.print("  " + s);
			}
			System.out.println();
		}
	}
	
	private DependencyGraph dependencyDAG;
	private int  maxPathLength = 4;
	private ArrayList<ArrayList<String>> allPaths;
	HashMap<String, Integer>  nodePairPathFrequency = new HashMap();
	private ArrayList<Relationship> allRelationships;
	private double proximityThreshold = 0.8;
	private HashMap<String, NodeHierarchy> allNodeHierarchy;
	private ArrayList<ArrayList<String>> removedPaths;
	private ArrayList<Relationship> ffRelationship;
	private ArrayList<Relationship> fbRelationship;
	private ArrayList<Relationship> ltRelationship;
	private HashSet<String> usedPair = new HashSet();
	
	private class Relationship implements Comparable<Relationship> {
		String start;
		String end;
		int weight;
		int mx;
		
		public Relationship(String start, String end, int weight, int mx) {
			this.start = start;
			this.end = end;
			this.weight = weight;
			this.mx = mx;
		}
		
		public int compareTo(Relationship r) {
			if (weight != r.weight) {
				return r.weight - weight;
			}
			return r.mx - mx;
		}
		
		public String toString() {
			return start + "  " + end + "  " + weight;
		}
		
		public String toString(boolean noWeight) {
			return start + "  " + end;
		}
	}
	
	private class NodeHierarchy {
		HashSet<String> above;
		HashSet<String> same;
		HashSet<String> below;
		
		public NodeHierarchy() {
			above = new HashSet();
			same = new HashSet();
			below = new HashSet();
		}
	}
	
	private void getPathsHelper(String node, ArrayList<String> pathNodes) {
//		System.out.println("Here  " + node);
		
		if (pathNodes.size() > maxPathLength + 1) return; // hop-size in edges
		
		if (dependencyDAG.targets.contains(node)) {
			allPaths.add(new ArrayList(pathNodes));
		}
		
		if (!dependencyDAG.serves.containsKey(node)) return;		
		for (String s: dependencyDAG.serves.get(node)) {
			if (pathNodes.contains(s)) {
				continue;
			}
			pathNodes.add(s);
			getPathsHelper(s, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private void getPaths() throws Exception {
		allPaths = new ArrayList();
		ArrayList<String> pathNodes = new ArrayList();
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.sources.contains(s)) continue;
			pathNodes.add(s);
			getPathsHelper(s, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private void getRelationships() throws Exception {
		for (ArrayList aList : allPaths) {
			for (int i = 0; i < aList.size(); ++i) {
				for (int j = i + 1; j < aList.size(); ++j) {
					String start = (String)aList.get(i);
					String end = (String)aList.get(j);
					String key = start + '#' + end;
					if (usedPair.contains(key)) continue;
					if (nodePairPathFrequency.containsKey(key)) {
						nodePairPathFrequency.put(key, nodePairPathFrequency.get(key) + 1);
					}
					else {
						nodePairPathFrequency.put(key, 1);
					}
				}
			}
		}
		
		allRelationships = new ArrayList();
		for (String s : nodePairPathFrequency.keySet()) {
			String start = s.substring(0, s.indexOf("#"));
			String end = s.substring(s.indexOf("#") + 1);
//			System.out.println(start + "#" + end + "\t" + nodePairPathFrequency.get(s));
			int forward = nodePairPathFrequency.get(s);
			int reverse = 0;
			if (nodePairPathFrequency.containsKey(end + "#" + start)) reverse = nodePairPathFrequency.get(end + "#" + start);
			allRelationships.add(new Relationship(start, end, Math.abs(forward - reverse), Math.max(forward, reverse)));
		}
		
		Collections.sort(allRelationships);
	}
	
	private void printPathsAndRelationships() {
		System.out.println("\nCurrent Paths");
		for (ArrayList aList : allPaths) {
			System.out.println(aList);
		}
		System.out.println();
		
		System.out.println("Relationship Matrix");
		printRelationshipMatrix();
		System.out.println();
		
		System.out.println("Relationship Diff Matrix");
		printRelationshipDifferenceMatrix();
		System.out.println();
	}
	
	private void printRelationshipMatrix() {
		System.out.printf("   ");
		for (String s : dependencyDAG.nodes) {
			System.out.printf("%3s", s);
		}
		System.out.println();
		for (String s : dependencyDAG.nodes) {
			System.out.printf("%3s", s);
			for (String r : dependencyDAG.nodes) {
				String key = s + "#" + r;
				if (nodePairPathFrequency.containsKey(key)) {
					System.out.printf("%3d", nodePairPathFrequency.get(key));
				}
				else {
					System.out.printf("  .");
				}
			}
			System.out.println();
		}
	}
	
	private void printRelationshipDifferenceMatrix() {
		System.out.printf("   ");
		for (String s : dependencyDAG.nodes) {
			System.out.printf("%3s", s);
		}
		System.out.println();
		for (String s : dependencyDAG.nodes) {
			System.out.printf("%3s", s);
			for (String r : dependencyDAG.nodes) {
				String fKey = s + "#" + r;
				String rKey = r + "#" + s;
				int forward = 0;
				int reverse = 0;
				if (nodePairPathFrequency.containsKey(fKey)) forward = nodePairPathFrequency.get(fKey);
				if (nodePairPathFrequency.containsKey(rKey)) reverse = nodePairPathFrequency.get(rKey);
				if (forward == 0 && reverse == 0) {
					System.out.printf("  .");
				}
				else {
					System.out.printf("%3d", forward - reverse);
				}
			}
			System.out.println();
		}
	}
	
	private boolean validateFF(String start, String end) {
//		p < q
//		If q is in p.B or p.S, return false
//		If p is in q.A or q.S, return false
//		Return true
		if (allNodeHierarchy.get(start).below.contains(end)) return false;
		if (allNodeHierarchy.get(start).same.contains(end)) return false;
		if (allNodeHierarchy.get(end).above.contains(start)) return false;
		if (allNodeHierarchy.get(end).same.contains(start)) return false;
		return true;
	}
	
	private boolean validateLT(String start, String end) {
//		p = q
//		If q is in p.A or p.B, return false
//		If p is in q.A or q.B, return false
//		Return true
		if (allNodeHierarchy.get(start).below.contains(end)) return false;
		if (allNodeHierarchy.get(start).above.contains(end)) return false;
		if (allNodeHierarchy.get(end).below.contains(start)) return false;
		if (allNodeHierarchy.get(end).above.contains(start)) return false;
		return true;		
	}
	
	private void updateFF(String start, String end) {
//		p < q
//		For each member y in q.A or q.S
//			Add p to y.B
//			Add y to p.A 
//		For each member y in p.B or p.S
//			Add q to y.A
//			Add y to q.B
//		Add p to q.B 
//		Add q to p.A
		for (String s : allNodeHierarchy.get(end).above) {
			allNodeHierarchy.get(s).below.add(start);
			allNodeHierarchy.get(start).above.add(s);
		}
		
		for (String s : allNodeHierarchy.get(end).same) {
			allNodeHierarchy.get(s).below.add(start);
			allNodeHierarchy.get(start).above.add(s);
		}
		
		for (String s : allNodeHierarchy.get(start).below) {
			allNodeHierarchy.get(s).above.add(end);
			allNodeHierarchy.get(end).below.add(s);
		}
		
		for (String s : allNodeHierarchy.get(start).same) {
			allNodeHierarchy.get(s).above.add(end);
			allNodeHierarchy.get(end).below.add(s);
		}
		
		allNodeHierarchy.get(end).below.add(start);
		allNodeHierarchy.get(start).above.add(end);
	}
	
	private void updateLT(String start, String end) {
//		p = q
//		For each member y in q.A
//			Add y to p.A
//			Add p to y.B
//		For each member y in q.B
//			Add y to p.B
//			Add p to y.A
//		For each member y in p.A
//			Add y to q.A
//			Add q to y.B
//		For each member y in p.B
//			Add y to q.B
//			Add q to y.A
//		Add p.S to q.S
//		Add q.S to p.S
//		Add q to p.S
//		Add p to q.S
		for (String s : allNodeHierarchy.get(end).above) {
			allNodeHierarchy.get(start).above.add(s);
			allNodeHierarchy.get(s).below.add(start);
		}
		
		for (String s : allNodeHierarchy.get(end).below) {
			allNodeHierarchy.get(start).below.add(s);
			allNodeHierarchy.get(s).above.add(start);
		}
		
		for (String s : allNodeHierarchy.get(start).above) {
			allNodeHierarchy.get(end).above.add(s);
			allNodeHierarchy.get(s).below.add(end);
		}
		
		for (String s : allNodeHierarchy.get(start).below) {
			allNodeHierarchy.get(end).below.add(s);
			allNodeHierarchy.get(s).above.add(end);
		}

		allNodeHierarchy.get(start).same.addAll(allNodeHierarchy.get(end).same);
		allNodeHierarchy.get(end).same.addAll(allNodeHierarchy.get(start).same);

		
		allNodeHierarchy.get(start).same.add(end);
		allNodeHierarchy.get(end).same.add(start);
	}
	
	private boolean pathContains(ArrayList<String> path, String start, String end) {
		int startIdx = path.indexOf(start);
		int endIdx = path.indexOf(end);
		if (startIdx == -1 || endIdx == -1 || (startIdx > endIdx)) {
			return false;
		}
		return true;
	}
	
	private void buildIterativeRelationshipNetwork() throws Exception {
		getPaths();
		getRelationships();
		printPathsAndRelationships();
		
		ffRelationship = new ArrayList();
		fbRelationship = new ArrayList();
		ltRelationship = new ArrayList();
		removedPaths = new ArrayList();
		
		allNodeHierarchy = new HashMap();
		for (String s : dependencyDAG.nodes) {
			allNodeHierarchy.put(s, new NodeHierarchy());
		}
		
		int index = 0;
		while (index < allRelationships.size()) {
			Relationship r = allRelationships.get(index);
			usedPair.add(r.start + "#" + r.end);
			double proximity = r.weight / r.mx;
			boolean skip = true;
			if (proximity > proximityThreshold) {
				// FF
				if (validateFF(r.start, r.end)) {
					updateFF(r.start, r.end);
					skip = false;
					ffRelationship.add(new Relationship(r.start, r.end, r.weight, r.mx));
					System.out.println("Adding FF  " + r  + "  " + proximity);
				}
				else {
					// LT
					if (validateLT(r.start, r.end)) {
						updateLT(r.start, r.end);
						skip = false;
						ltRelationship.add(new Relationship(r.start, r.end, r.weight, r.mx));
						System.out.println("Adding LT  " + r + "  " + proximity);
					}
				}
			}
			else {
				// LT
				if (validateLT(r.start, r.end)) {
					updateLT(r.start, r.end);
					skip = false;
					ltRelationship.add(new Relationship(r.start, r.end, r.weight, r.mx));
					System.out.println("Adding LT  " + r + "  " + proximity);
				}
			}
			
			index++;
			if (skip) {
				// FB
				System.out.println("Adding FB  " + r);
				fbRelationship.add(new Relationship(r.start, r.end, r.weight, r.mx));
				// update paths
				ArrayList<ArrayList<String>> toRemove = new ArrayList();
				for (ArrayList aList : allPaths) {
					if (pathContains(aList, r.start, r.end)) {
						toRemove.add(new ArrayList(aList));
					}
				}
				allPaths.removeAll(toRemove);
				nodePairPathFrequency.clear();
				getRelationships();
				index = 0;
				printPathsAndRelationships();
			}
			
			printNodeHierarchy();
		}
		
		printFinalRelationships();
	}
	
	private void printNodeHierarchy() {
		System.out.println("Node Hierarchy");
		for (String s : allNodeHierarchy.keySet()) {
			System.out.print(s);
			System.out.print(" {above: " + allNodeHierarchy.get(s).above + "} ");
			System.out.print(" {same: " + allNodeHierarchy.get(s).same + "} ");
			System.out.print(" {below: " + allNodeHierarchy.get(s).below + "} ");
			System.out.println();
		}
		System.out.println();
	}
	
	private void printFinalRelationships() throws Exception {
		System.out.println("\nFinal Relationships");
		System.out.println("FF");
		for (Relationship r : ffRelationship) {
			System.out.println(r.toString(false));
		}
		System.out.println();
		
		System.out.println("LT");
		for (Relationship r : ltRelationship) {
			System.out.println(r.toString(false));
		}
		System.out.println();
		
		System.out.println("FB");
		for (Relationship r : fbRelationship) {
			System.out.println(r.toString(false));
		}
		System.out.println();
	}
	
	private void runAnalysis(String data) throws Exception {
		String dependencyDAGFile = "data//" + data + "_links.txt";
		String sourceFile = "data//" + data + "_sources.txt";
		String targetFile = "data//" + data + "_targets.txt";
		dependencyDAG = new DependencyGraph(dependencyDAGFile, sourceFile, targetFile);
		dependencyDAG.printNetworkProperties();
		buildIterativeRelationshipNetwork();
	}
	
	public static void main(String[] args) throws Exception {
		HierarchyEstimation he = new HierarchyEstimation();
		he.runAnalysis("h1");
//		he.runAnalysis(args[0]);
	}
}
