package clean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;

import utilityhg.TarjanSCC;

public class HierarchyEstimation {
	private DependencyGraph dependencyGraph;
	private DependencyGraph relationshipGraph = new DependencyGraph();
	private int maxPathLength = 4;
	private ArrayList<ArrayList<String>> allPaths;
	HashMap<String, Integer>  nodePairPathFrequency = new HashMap();
	private ArrayList<Relationship> allRelationships;
	private double proximityThreshold = 0.5;
	private HashMap<String, NodeHierarchy> allNodeHierarchy;
	private ArrayList<ArrayList<String>> removedPaths;
	private ArrayList<Relationship> ffRelationship;
	private ArrayList<Relationship> fbRelationship;
	private ArrayList<Relationship> ltRelationship;
	private HashSet<String> usedPair = new HashSet();
	public HashSet<String> coreNeurons = new HashSet();
	public HashSet<String> source = new HashSet();
	public HashSet<String> inter = new HashSet();
	public HashSet<String> target = new HashSet();
	public HashSet<String> nodes = new HashSet();

	
	private class Relationship implements Comparable<Relationship> {
		String start;
		String end;
		int sumWeight;
		int diffWeight;
		int mx;
		
		public Relationship(String start, String end, int sumWeight, int diffWeight, int mx) {
			this.start = start;
			this.end = end;
			this.sumWeight = sumWeight;
			this.diffWeight = diffWeight;
			this.mx = mx;
		}
		
		@Override
		public int compareTo(Relationship r) {
//			if (weight != r.weight) {
//				return r.weight - weight;
//			}
//			return r.mx - mx;
//			return r.sumWeight - sumWeight;
//			return sumWeight - r.sumWeight;
			return r.diffWeight - diffWeight;
//			return diffWeight - r.diffWeight;
		}
		
		@Override
		public String toString() {
			return start + "  " + end + "  " + sumWeight + " " + diffWeight;
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
		
		if (dependencyGraph.targets.contains(node)) {
			allPaths.add(new ArrayList(pathNodes));
		}
		
		if (!dependencyGraph.serves.containsKey(node)) return;		
		for (String s: dependencyGraph.serves.get(node)) {
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
		for (String s: dependencyGraph.nodes) {
			if (!dependencyGraph.sources.contains(s)) continue;
			pathNodes.add(s);
			getPathsHelper(s, pathNodes);
			pathNodes.remove(s);
		}
	}
	
	private void loadRelationships() throws Exception {
		int threshold = 0;
//		Scanner scanner = new Scanner(new File("data//h6_paths.txt"));
		Scanner scanner = new Scanner(new File("celegans//all_p_5.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_p_5.txt"));
		HashSet<String> howManyNodes = new HashSet();
		while (scanner.hasNext()) {
			String path = scanner.nextLine();
			String nodes[] = path.split("\\s+");
			for (int i = 0; i < nodes.length; ++i) {
				for (int j = i + 1; j < nodes.length; ++j) {
					String start = nodes[i];
					String end = nodes[j];
					String key = start + '#' + end;
					if (nodePairPathFrequency.containsKey(key)) {
						nodePairPathFrequency.put(key, nodePairPathFrequency.get(key) + 1);
					}
					else {
						nodePairPathFrequency.put(key, 1);
					}
//					howManyNodes.add(start);
//					howManyNodes.add(end);
				}
			}
		}
		
		System.out.println(nodePairPathFrequency.size());
		
		HashSet<String> visited = new HashSet();
		HashSet<String> filter = new HashSet();
		double kountC = 0;
		double kountB = 0;
		for (String s : nodePairPathFrequency.keySet()) {
//			System.out.println(s + "\t" + nodePairPathFrequency.get(s));
//			System.out.println(nodePairPathFrequency.get(s));
			
			// build relationship network, check if necessary to prune
			ArrayList<String> aList = splitEdge(s);
			if (nodePairPathFrequency.get(s) >= threshold) {
				relationshipGraph.addEdge(aList.get(0), aList.get(1));
			}
			else {
				filter.add(s);
				continue;
			}
			
			
			String f = aList.get(0) + "#" + aList.get(1);
			String b = aList.get(1) + "#" + aList.get(0);
//			if (!visited.contains(b)) {
//				if (nodePairPathFrequency.containsKey(f) && nodePairPathFrequency.containsKey(b)) {
////					System.out.println(nodePairPathFrequency.get(f) + "\t" + nodePairPathFrequency.get(b));
//				}
//				visited.add(f);
//			}
			
			if (nodePairPathFrequency.containsKey(f) && nodePairPathFrequency.containsKey(b)) {
				if (!visited.contains(b)) {
//					System.out.println(nodePairPathFrequency.get(f) + "\t" + nodePairPathFrequency.get(b));
				}
			}
			visited.add(f);
			
			if (nodePairPathFrequency.containsKey(b)) {
				++kountC;
			}
			else {
				++kountB;
			}
			
			howManyNodes.add(aList.get(0));
			howManyNodes.add(aList.get(1));
		}
		
		for (String s : filter) {
			nodePairPathFrequency.remove(s);
		}
//		System.out.println(filter.size() + "\t" + nodePairPathFrequency.size() + "\t" + kount);
//		System.out.println(relationshipGraph.nodes.size() + "\t" + howManyNodes.size());
		kountC /= 2;
		double maxPair = 37781;
		double kountA = maxPair - kountC - kountB; 
		System.out.println(kountA/maxPair); // pairs not connected at all
		System.out.println(kountB/maxPair); // pairs connected in both directions
		System.out.println(kountC/maxPair); // pairs connected in one direction
		System.out.println((279.0 - howManyNodes.size())/279.0);
		System.out.println("Finished Loading Path Matrix");
	}
	
	
	private void loadPaths() throws Exception {
		allPaths = new ArrayList();
//		Scanner scanner = new Scanner(new File("data//h4_paths.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_LTM_path.txt"));
//		Scanner scanner = new Scanner(new File("celegans//all_4_path.txt"));
		Scanner scanner = new Scanner(new File("celegans//all_sp+2.txt"));
		while (scanner.hasNext()) {
			String path = scanner.nextLine();
			String nodes[] = path.split("\\s+");
			allPaths.add(new ArrayList(Arrays.asList(nodes)));
		}
//		System.out.println(allPaths.size());
	}
		
	private void getRelationships() throws Exception {
		for (ArrayList aList : allPaths) {
			for (int i = 0; i < aList.size(); ++i) {
				for (int j = i + 1; j < aList.size(); ++j) {
					String start = (String)aList.get(i);
					String end = (String)aList.get(j);
					String key = start + '#' + end;
//					if (usedPair.contains(key)) continue;
					if (nodePairPathFrequency.containsKey(key)) {
						nodePairPathFrequency.put(key, nodePairPathFrequency.get(key) + 1);
					}
					else {
						nodePairPathFrequency.put(key, 1);
					}
				}
			}
		}
		
		/*
		allRelationships = new ArrayList();
		HashSet<String> hset = new HashSet();
		HashMap<String, Integer> nodeCover = new HashMap();
		for (String s : nodePairPathFrequency.keySet()) {
			String start = s.substring(0, s.indexOf("#"));
			String end = s.substring(s.indexOf("#") + 1);
			if (hset.contains(end + "#" + start)) {
				continue;
			}
			addMap(nodeCover, start);
			addMap(nodeCover, end);
//			System.out.println(start + "#" + end + "\t" + nodePairPathFrequency.get(s));
			int forward = nodePairPathFrequency.get(s);
			int reverse = 0;
			if (nodePairPathFrequency.containsKey(end + "#" + start)) reverse = nodePairPathFrequency.get(end + "#" + start);
//			allRelationships.add(new Relationship(start, end, forward - reverse, Math.max(forward, reverse)));
			
			if (forward >= reverse) {
				allRelationships.add(new Relationship(start, end, forward + reverse, forward - reverse, forward));
			}
			else {
				allRelationships.add(new Relationship(end, start, reverse + forward, reverse - forward, reverse));
			}
			hset.add(start + "#" + end);
		}
		
		Collections.sort(allRelationships);
		
		for (String s : nodeCover.keySet()) {
//			System.out.println(s + "\t" + nodeCover.get(s));
		}
		
		for (Relationship r : allRelationships) {
//			System.out.println(r + " " + (r.diffWeight * 1.0 / r.mx));
//			System.out.println((r.diffWeight * 1.0 / r.mx));
		}
		*/
		
		HashSet<String> visited = new HashSet();
		for (String s : nodePairPathFrequency.keySet()) {
//			System.out.println(s + "\t" + nodePairPathFrequency.get(s));
			System.out.println(nodePairPathFrequency.get(s));
			
			// build relationship network
			ArrayList<String> aList = splitEdge(s);
			// check if necessary to prune
			
			if (nodePairPathFrequency.get(s) >= 1) {
				relationshipGraph.addEdge(aList.get(0), aList.get(1));
			}
			
			String f = aList.get(0) + "#" + aList.get(1);
			String b = aList.get(1) + "#" + aList.get(0);
			if (!visited.contains(b)) {
				if (nodePairPathFrequency.containsKey(f) && nodePairPathFrequency.containsKey(b)) {
//					System.out.println(nodePairPathFrequency.get(f) + "\t" + nodePairPathFrequency.get(b));
				}
				visited.add(f);
			}
		}
	}
	
	public ArrayList<String> splitEdge(String edge) {
		int idx = edge.indexOf("#");
		ArrayList<String> nodes = new ArrayList();
		nodes.add(edge.substring(0, idx));
		nodes.add(edge.substring(idx + 1));
		return nodes;
	}

	private void addMap(HashMap<String, Integer> hmap, String key) {
		if (hmap.containsKey(key)) {
			hmap.put(key, hmap.get(key) + 1);
		}
		else {
			hmap.put(key, 1);
		}
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
		for (String s : dependencyGraph.nodes) {
			System.out.printf("%3s", s);
		}
		System.out.println();
		for (String s : dependencyGraph.nodes) {
			System.out.printf("%3s", s);
			for (String r : dependencyGraph.nodes) {
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
		for (String s : dependencyGraph.nodes) {
			System.out.printf("%3s", s);
		}
		System.out.println();
		for (String s : dependencyGraph.nodes) {
			System.out.printf("%3s", s);
			for (String r : dependencyGraph.nodes) {
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
	
	private void simpleHierarchy() throws Exception {
//		getPaths();
		loadPaths();
		getRelationships();
//		printPathsAndRelationships();
		
		int lowLevel = 0;
		int highLevel = 100;
		HashSet<String> candidates = new HashSet(dependencyGraph.nodes);
		TreeMap<Integer, HashSet<String>> hierarchy = new TreeMap();
		while (candidates.size() > 0) {
			hierarchy.put(lowLevel, new HashSet());
			hierarchy.put(highLevel, new HashSet());
			
			// try low level first
			for (String s : candidates) {
//				System.out.println("For " + s);
				boolean valid = true;
				for (String r : candidates) {
//					System.out.println("Trying " + r);
					int forward = 0;
					int backward = 0;
					if (nodePairPathFrequency.containsKey(s + "#" + r)) {
						forward = nodePairPathFrequency.get(s + "#" + r);
					}
					if (nodePairPathFrequency.containsKey(r + "#" + s)) {
						backward = nodePairPathFrequency.get(r + "#" + s);
					}
					
//					System.out.println("F: " + forward + " B: " + backward);
					
					if (forward == 0 && backward == 0) {
//						System.out.println("No Relation");
						continue;
					}
					
					if (backward >= forward){ // check if lateral
						double proximity = (Math.max(forward, backward) - Math.min(forward, backward)) / Math.max(forward, backward);
//						System.out.println(proximity);
						if (proximity > proximityThreshold) {
//							System.out.println("Violating Relation " + proximity);
							valid = false;
							break;
						}
					}
					else {
						// fine
					}
					
//					System.out.println("Valid Relation");
				}
				if (valid) {
					hierarchy.get(lowLevel).add(s);
				}
//				System.out.println("Done\n");
			}
			
			if (hierarchy.get(lowLevel).size() == 0) {
//			    find high level now
				HashSet<String> above = new HashSet();
				for (String s : candidates) {
					boolean valid = true;
					for (String r : candidates) {
						int forward = 0;
						int backward = 0;
						if (nodePairPathFrequency.containsKey(s + "#" + r)) {
							forward = nodePairPathFrequency.get(s + "#" + r);
						}
						if (nodePairPathFrequency.containsKey(r + "#" + s)) {
							backward = nodePairPathFrequency.get(r + "#" + s);
						}
						
						if (forward == 0 && backward == 0) {
							continue;
						}
						
						if (forward >= backward){ // check if lateral
							double proximity = (Math.max(forward, backward) - Math.min(forward, backward)) / Math.max(forward, backward);
//							System.out.println(proximity);
							if (proximity > proximityThreshold) {
								System.out.println(s + " invalidated by " + r + " at layer " + lowLevel + "\t" + highLevel);
								valid = false;
								break;
							}
						}
						else {
							// fine
						}
					}
					if (valid) {
						above.add(s);
					}
				}
				
				if (above.size() == 0) {
					// nothing to do
					hierarchy.put(lowLevel, new HashSet(candidates));
					break;
				}
				else {
					HashSet<String> temp = new HashSet();					
					for (String t : candidates) {
						if (above.contains(t)) {
							hierarchy.get(highLevel).add(t);
						}
						else {
							temp.add(t);
						}
					
					}
					candidates = new HashSet(temp);
					--highLevel;
				}
			}
			else {
				candidates.removeAll(hierarchy.get(lowLevel));
				++lowLevel;
			}		
		}
		
		
		for (int i : hierarchy.keySet()) {
			int k = 0;
			if (hierarchy.get(i).size() > 0) {
				for (String s : hierarchy.get(i)) {
//					System.out.print(s + " ");
					if (coreNeurons.contains(s)) ++k;
				}
//				System.out.println();
			}
			System.out.println(i + "\t" + hierarchy.get(i).size() + "\t" + k);

		}
//		System.out.println(proximityThreshold + "\t" + k);
	}
		
	private void simpleHierarchy_2() throws Exception {
		loadRelationships();
		loadCoreNeurons();
		loadNeuroMetaNetwork();
		
		int level = 0;
		HashSet<String> candidates = new HashSet(relationshipGraph.nodes);
		TreeMap<Integer, HashSet<String>> hierarchy = new TreeMap();
//		System.out.println(candidates.size());
		ArrayList<String> aList = new ArrayList();
		while (candidates.size() > 0) {
			hierarchy.put(level, new HashSet());
			// compute SCC
			TarjanSCC tarjanSCC = new TarjanSCC(relationshipGraph);
			tarjanSCC.getSCCs_2();
			int maxSCCSize = -1;
			int kountSCCPut = 0;
//			System.out.println("SCC size: " + tarjanSCC.SCCs.size());
//			System.out.println("Current Level: " + level);
//			System.out.println("SCCs: ");
			for (String s : tarjanSCC.SCCs.keySet()) {
				HashSet<String> sccNodes = tarjanSCC.SCCs.get(s);
//				System.out.println(sccNodes);
//				if (sccNodes.size() > maxSCCSize) maxSCCSize = sccNodes.size();
				for (String r : sccNodes) {
					boolean valid = true;
					for (String t : candidates) {
						if (sccNodes.contains(t)) continue; 
						int forward = 0;
						int backward = 0;
						if (nodePairPathFrequency.containsKey(r + "#" + t)) {
							forward = nodePairPathFrequency.get(r + "#" + t);
						}
						if (nodePairPathFrequency.containsKey(t + "#" + r)) {
							backward = nodePairPathFrequency.get(t + "#" + r);
						}
						
						if (forward == 0 && backward == 0) {
							continue;
						}
						
						if (forward > backward) {
//							System.out.println(forward + "\t" + backward);
//							continue;
						}
						
						if (forward > 0 && backward == 0) {
							continue;
						}
						
						if (forward > backward) {
//							continue;
						}
						
						
						valid = false;
						break;
					}
					if (valid) {
						hierarchy.get(level).addAll(sccNodes);
						for (String t : sccNodes) {
							relationshipGraph.removeNode(t);
						}
						kountSCCPut++;
						if (sccNodes.size() > maxSCCSize) {
							maxSCCSize = sccNodes.size();
						}
						break;
					}
				}
			}
			aList.add("[" + kountSCCPut + "," + maxSCCSize + "]");
//			System.out.println("[" + kountSCCPut + "," + maxSCCSize + "]");
//			System.out.println(tarjanSCC.SCCs.size() + "," + maxSCCSize);
//			System.out.println(level + "\t" + hierarchy.get(level).size());
//			System.out.println("Added to this level: " + hierarchy.get(level));
			candidates.removeAll(hierarchy.get(level));
			++level;
		}
		
		int j = 0;
		for (int i : hierarchy.keySet()) {
			int nC = 0;
			int nS = 0;
			int nI = 0;
			int nM = 0;
			if (hierarchy.get(i).size() > 0) {
				for (String s : hierarchy.get(i)) {
//					System.out.print(s + " ");
					if (coreNeurons.contains(s)) ++nC;
					if (source.contains(s)) ++nS;
					if (inter.contains(s)) ++nI;
					if (target.contains(s)) ++nM;
				}
//				System.out.println();
			}
//			System.out.println(i + "\t" + hierarchy.get(i).size() + "\t" + k);
//			System.out.println(i + "\t" + hierarchy.get(i) + "\t" + nC);
			System.out.println(hierarchy.get(i).size() + "=" + nS + "+" + nI + "+" + nM + "(" + nC + ")" + " " + aList.get(j++));
		}
	}

	private void buildIterativeRelationshipNetwork_2() throws Exception {
//		getPaths();
		loadPaths();
		getRelationships();
//		printPathsAndRelationships();
		
		System.out.println(allRelationships.size());
		ffRelationship = new ArrayList();
		fbRelationship = new ArrayList();
		ltRelationship = new ArrayList();
//		removedPaths = new ArrayList();
		
		allNodeHierarchy = new HashMap();
		for (String s : dependencyGraph.nodes) {
			allNodeHierarchy.put(s, new NodeHierarchy());
		}
		
		int kFF = 0, kLT = 0, kFB = 0;
		int index = 0;
		while (index < allRelationships.size()) {
			Relationship r = allRelationships.get(index);
//			System.out.println(r);
//			if (r.diffWeight < 0) break;
			usedPair.add(r.start + "#" + r.end);
			double proximity = r.diffWeight * 1.0 / r.mx;
			boolean skip = true;
			if (proximity > proximityThreshold) {
				// FF
				if (validateFF(r.start, r.end)) {
					updateFF(r.start, r.end);
					skip = false;
					ffRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
//					System.out.println("Adding FF  " + r  + "  " + proximity);
					++kFF;
				}
				else {
//					++kFB;
//					System.out.println("Conflict  " + r  + "  " + proximity);
					break;
					// LT
//					if (validateLT(r.start, r.end)) {
//						updateLT(r.start, r.end);
//						skip = false;
//						ltRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
////						System.out.println("Adding LT  " + r + "  " + proximity);
//						++kLT;
//					}
//					else {
////						break;
//					}
				}
			}
			else {
				// LT
				if (validateLT(r.start, r.end)) {
					updateLT(r.start, r.end);
					skip = false;
					ltRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
//					System.out.println("Adding LT  " + r + "  " + proximity);
					++kLT;
				}
				else {
//					++kFB;
//					System.out.println("Conflict  " + r  + "  " + proximity);
					break;
				}
			}
			
			index++;
//			System.out.println(index);
			/*
			if (skip) {
				// FB
				System.out.println("Adding FB  " + r);
				fbRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
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
//				printPathsAndRelationships();
				++kFB;
			}
			*/
//			System.out.println(allPaths.size() + "\t" + allRelationships.size() + "\t" + index + "\t" + nodePairPathFrequency.size());
//			printNodeHierarchy();
		}
		
//		printFinalRelationships();
//		printFinalHierarchy();
		System.out.println(kFF + "\t" + kLT + "\t" + kFB);
	}

	private void buildIterativeRelationshipNetwork_1() throws Exception {
		getPaths();
		getRelationships();
//		printPathsAndRelationships();
		
		ffRelationship = new ArrayList();
		fbRelationship = new ArrayList();
		ltRelationship = new ArrayList();
		removedPaths = new ArrayList();
		
		allNodeHierarchy = new HashMap();
		for (String s : dependencyGraph.nodes) {
			allNodeHierarchy.put(s, new NodeHierarchy());
		}
		
		int kFF = 0, kLT = 0, kFB = 0;
		int index = 0;
		while (index < allRelationships.size()) {
			Relationship r = allRelationships.get(index);
			if (r.sumWeight < 0) break;
			usedPair.add(r.start + "#" + r.end);
			double proximity = r.sumWeight * 1.0 / r.mx;
			boolean skip = true;
			if (proximity > proximityThreshold) {
				// FF
				if (validateFF(r.start, r.end)) {
					updateFF(r.start, r.end);
					skip = false;
					ffRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
					System.out.println("Adding FF  " + r  + "  " + proximity);
					++kFF;
				}
				else {
					// LT
					if (validateLT(r.start, r.end)) {
						updateLT(r.start, r.end);
						skip = false;
						ltRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
						System.out.println("Adding LT  " + r + "  " + proximity);
						++kLT;
					}
				}
			}
			else {
				// LT
				if (validateLT(r.start, r.end)) {
					updateLT(r.start, r.end);
					skip = false;
					ltRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
					System.out.println("Adding LT  " + r + "  " + proximity);
					++kLT;
				}
			}
			
			index++;
			if (skip) {
				// FB
				System.out.println("Adding FB  " + r);
				fbRelationship.add(new Relationship(r.start, r.end, r.sumWeight, r.diffWeight, r.mx));
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
//				printPathsAndRelationships();
				++kFB;
			}
//			System.out.println(allPaths.size() + "\t" + allRelationships.size() + "\t" + index + "\t" + nodePairPathFrequency.size());
//			printNodeHierarchy();
		}
		
//		printFinalRelationships();
		printFinalHierarchy();
		System.out.println(kFF + "\t" + kLT + "\t" + kFB);
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
	
	private void printFinalHierarchy() {
		HashMap<String, Integer> nodeLevel = new HashMap();
		HashSet<String> tempNodes = new HashSet(dependencyGraph.nodes);
//		System.out.println("Here");
		int maxLevel = 0;
//		int tryKount = 1000;
		while (nodeLevel.size() < tempNodes.size() /*&& tryKount-- > 0*/) {
			for (String s : tempNodes) {
				if (nodeLevel.containsKey(s)) {
					continue;
				}
				else if (allNodeHierarchy.get(s).below.size() == 0) {
					nodeLevel.put(s, 1);
				}
				else {
					int level = 0;
					boolean complete = true;
					for (String r : allNodeHierarchy.get(s).below) {
						if (!nodeLevel.containsKey(r)) {
							complete = false;
							break;
						}
						level = Math.max(level, nodeLevel.get(r));
					}
					if (complete) {
						nodeLevel.put(s, level + 1);
					}
				}
				if (nodeLevel.containsKey(s) && nodeLevel.get(s) > maxLevel) {
					maxLevel = nodeLevel.get(s);
				}
			}
		}
		
		System.out.println("\nFinal Hierarchy");
		for (int i = 1; i <= maxLevel; ++i) {
			System.out.print("Level " + i + ":");
			int k = 0;
			for (String s : nodeLevel.keySet()) {
				if (nodeLevel.get(s) == i) {
					System.out.print("  " + s);
					++k;
				}
			}
			System.out.print(" " + k);
			System.out.println();
		}
	}
	
	private void loadCoreNeurons() throws Exception {
		Scanner scan = new Scanner(new File("celegans//core_neurons_sp+2.txt"));
		coreNeurons = new HashSet();
		while (scan.hasNext()) {			
			coreNeurons.add(scan.next());
		}
		scan.close();
	}
	
	private  void loadNodes(HashSet<String> nodes, HashSet<String> typeNode, String fileName) throws Exception {
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext()) {
			String i = scan.next();
			typeNode.add(i);
			nodes.add(i);
		}
		scan.close();
	}
	
	public void loadNeuroMetaNetwork() throws Exception {
		loadNodes(nodes, source, "celegans//sensory_neurons_3.txt");
		loadNodes(nodes, inter, "celegans//inter_neurons_3.txt");
		loadNodes(nodes, target, "celegans//motor_neurons_3.txt");		
	}
	
	private void runAnalysis(String data) throws Exception {
//		String dependencyDAGFile = "data//" + data + "_links.txt";
//		String sourceFile = "data//" + data + "_sources.txt";
//		String targetFile = "data//" + data + "_targets.txt";
//		dependencyGraph = new DependencyGraph(dependencyDAGFile, sourceFile, targetFile);
//		dependencyDAG.printNetworkProperties();
//		buildIterativeRelationshipNetwork_1());
//		buildIterativeRelationshipNetwork_2();
		

//		simpleHierarchy();
		simpleHierarchy_2();
//		loadRelationships();
		
//		loadPaths();
//		getRelationships();
	}
	
	public static void main(String[] args) throws Exception {
		HierarchyEstimation he = new HierarchyEstimation();
		he.runAnalysis("celegans");
//		he.runAnalysis("h6");
//		he.runAnalysis(args[0]);
		
//		for (double d = 0.9; d < 1.01; d += 0.01) {
//			HierarchyEstimation he = new HierarchyEstimation();
//			he.proximityThreshold = d;
//			he.runAnalysis("celegans");
//		}
	}
}
