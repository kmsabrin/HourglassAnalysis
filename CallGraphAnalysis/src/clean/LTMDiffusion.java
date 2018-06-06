package clean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class LTMDiffusion {
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
			System.out.println("Given Network");
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
	
	private void getPathsHelper(String node, ArrayList<String> pathNodes, DependencyGraph diffusionDAG) {
//		if (pathNodes.size() > maxPathLength + 1) return; // hop-size in edges
		
		if (diffusionDAG.targets.contains(node)) {
			allPaths.add(new ArrayList(pathNodes));
		}
		
		if (!diffusionDAG.serves.containsKey(node)) {
			return;	
		}
		
		for (String s: diffusionDAG.serves.get(node)) {
//			if (pathNodes.contains(s)) {
//				continue; // needed?
//			}
			pathNodes.add(s);
			getPathsHelper(s, pathNodes, diffusionDAG);
			pathNodes.remove(s);
		}
	}
	
	private void getPaths(String src, DependencyGraph diffusionDAG) throws Exception {
		ArrayList<String> pathNodes = new ArrayList();
		pathNodes.add(src);
		getPathsHelper(src, pathNodes, diffusionDAG);
	}
	
	private DependencyGraph dependencyGraph;
	private int maxPathLength = 4;
	private ArrayList<ArrayList<String>> allPaths;
	private HashMap<String, Integer> nodeActivationTimeMap;
	private double diffusionThreshold = 0.2;
	private HashSet<String> visited;
	
	private void diffuse() throws Exception {
		allPaths = new ArrayList();
		for (String src : dependencyGraph.sources) {
			nodeActivationTimeMap = new HashMap();
			Queue<String> bfsQ = new LinkedList();
			Queue<Integer> depthQ = new LinkedList();
			nodeActivationTimeMap.put(src, 0);
			bfsQ.add(src);
			depthQ.add(0);
			while (!bfsQ.isEmpty()) {
				String n = bfsQ.poll();
				int d = depthQ.poll();
//				System.out.println("-- -- " + n);
				// try to activate
				boolean isActive = false;
				if (n.equals(src)) {
					isActive = true;
				} 
				else {
					if (nodeActivationTimeMap.containsKey(n)) {
						continue;
					}
					double numActiveInput = 0;
					if (dependencyGraph.depends.containsKey(n)) {
						for (String r : dependencyGraph.depends.get(n)) {
							if (nodeActivationTimeMap.containsKey(r)) {
								++numActiveInput;
							}
						}
//						System.out.println("For " + n 
//								+ "\t" + numActiveInput 
//								+ "\t" + dependencyGraph.depends.get(n).size() 
//								+ "\t" + (dependencyGraph.depends.get(n).size() * diffusionThreshold));
						if (numActiveInput >= dependencyGraph.depends.get(n).size() * diffusionThreshold) {
							isActive = true;
						}
					}
				}
				if (isActive) {
					// gets activated and diffuse
					nodeActivationTimeMap.put(n, d);
					if (dependencyGraph.serves.containsKey(n)) {
						for (String r : dependencyGraph.serves.get(n)) {
							if (nodeActivationTimeMap.containsKey(r)) {
								continue;
							}
							bfsQ.add(r);
							depthQ.add(d + 1);
						}
					}
				}
			}
			
			for (String r : nodeActivationTimeMap.keySet()) {
//				System.out.println(r + "\t" + nodeActivationTimeMap.get(r));
			}
			
			// create diffusion DAG
			DependencyGraph diffusionDAG = new DependencyGraph();
			diffusionDAG.sources.add(src);
			for (String r : dependencyGraph.nodes) {
				if (nodeActivationTimeMap.containsKey(r)) {
					if (dependencyGraph.targets.contains(r)) {
						diffusionDAG.targets.add(r);
					}
					int rDepth = nodeActivationTimeMap.get(r);
					if (dependencyGraph.depends.containsKey(r)) {
						for (String t : dependencyGraph.depends.get(r)) {
							if (nodeActivationTimeMap.containsKey(t)) {
								int tDepth = nodeActivationTimeMap.get(t);
								if (tDepth < rDepth) {
									diffusionDAG.addEdge(t, r);
								}
							}
						}
					}
				}
			}
			
			// get paths from diffusion DAG
			getPaths(src, diffusionDAG);
//			System.out.println(src + "\t" + allPaths.size());
		}
		
		for (ArrayList<String> aList : allPaths) {
			for (String s : aList) {
//				System.out.print(s + "  ");
			}
//			System.out.println();
		}
		System.out.println(diffusionThreshold + "\t" + allPaths.size());
	}
	
	private void loadDependencyDAG(String data) throws Exception {
		String dependencyDAGFile = "data//" + data + "_links.txt";
		String sourceFile = "data//" + data + "_sources.txt";
		String targetFile = "data//" + data + "_targets.txt";
		dependencyGraph = new DependencyGraph(dependencyDAGFile, sourceFile, targetFile);
//		dependencyGraph.printNetworkProperties();
	}

	public static void main(String[] args) throws Exception {
		LTMDiffusion ltmDiffusion = new LTMDiffusion();
		ltmDiffusion.loadDependencyDAG("ltm1");
		ltmDiffusion.diffuse();
		
//		for (double d = 0; d <= 0.6; d += 0.01) {
//			LTMDiffusion ltmDiffusion = new LTMDiffusion();
//			ltmDiffusion.diffusionThreshold = d;
//			ltmDiffusion.loadDependencyDAG("celegans");
//			ltmDiffusion.diffuse();
//		}
	}
}
