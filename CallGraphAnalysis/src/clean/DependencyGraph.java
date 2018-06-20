package clean;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class DependencyGraph {
	public HashSet<String> nodes;
	public HashSet<String> targets;
	public HashSet<String> sources;
	public HashSet<String> inters;
	public HashMap<String, HashSet<String>> serves; 
	public HashMap<String, HashSet<String>> depends;		
	
	public DependencyGraph() { 
		nodes = new HashSet();
		serves = new HashMap();
		depends = new HashMap();
		targets = new HashSet();
		sources = new HashSet();
		inters = new HashSet();
	}
	
	public DependencyGraph(String dependencyGraphFilePath, String sourceFilePath, String targetFilePath) throws Exception {
		this();
		loadNetwork(dependencyGraphFilePath);
		loadSources(sourceFilePath);
		loadTargets(targetFilePath);
		loadInters();
	}
	
	public void addEdge(String server, String dependent) {
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
	
	public void removeEdge(String server, String dependent) {
		serves.get(server).remove(dependent);
		depends.get(dependent).remove(server);
	}
	
	public void removeNode(String node) {
		nodes.remove(node);
		targets.remove(node);
		sources.remove(node);
		inters.remove(nodes);
		if (serves.containsKey(node)) {
			for (String s : serves.get(node))  {
				depends.get(s).remove(node);
			}
			serves.remove(node);
		}
		if (depends.containsKey(node)) {
			for (String s : depends.get(node)) {
				serves.get(s).remove(node);
			}
			depends.remove(node);
		}
	}
	
	public void loadTargets(String fileName) throws Exception {
		Scanner scanner = new Scanner(new File(fileName));
		while (scanner.hasNext()) {
			targets.add(scanner.next());
		}
		scanner.close();
	}
	
	public void loadSources(String fileName) throws Exception {
		Scanner scanner = new Scanner(new File(fileName));
		while (scanner.hasNext()) {
			sources.add(scanner.next());
		}
		scanner.close();
	}
	
	public void loadNetwork(String fileName) throws Exception {
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
	
	public void loadInters() {
		for (String s : nodes) {
			if (!sources.contains(s) && !targets.contains(s)) {
				inters.add(s);
			}
		}
	}
	
	public void printNetworkProperties() throws Exception {
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
