package utility;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import corehg.DependencyDAG;

/*************************************************************************
 *  Compute the strongly-connected components of a digraph using 
 *  Tarjan's algorithm.
 *
 *  Runs in O(E + V) time.
 *
 *  toy_dg.txt
 *  5 components
 *  1 
 *  0 2 3 4 5
 *  9 10 11 12
 *  6
 *  7 8 
 *
 *************************************************************************/

public class TarjanSCC {
	private HashSet<String> marked; // marked[v] = has v been visited?
	private HashMap<String, Integer> id; // id[v] = id of strong component containing v
	private HashMap<String, Integer> low; // low[v] = low number of v
	private int pre; // pre-order number counter
	private int count; // number/id of strongly-connected components
	private Stack<String> stack;

	TarjanSCC(DependencyDAG G) {
		marked = new HashSet();
		stack = new Stack();
		id = new HashMap();
		low = new HashMap();
		
		for (String v : G.nodes) {
			if (!marked.contains(v)) {
				dfs(G, v);
			}
		}
	}

	private void dfs(DependencyDAG G, String v) {
		marked.add(v);
		low.put(v, pre++);
		int min = low.get(v);
		stack.push(v);
		
		if (G.serves.containsKey(v)) {
			for (String w : G.serves.get(v)) {
				if (!marked.contains(w)) {
					dfs(G, w);
				}
				
				if (low.get(w) < min) {
					min = low.get(w);
				}
			}
		}
		
		if (min < low.get(v)) {
			low.put(v, min);
			return;
		}
		
		String w = "";
		do {
			w = stack.pop();
			id.put(w, count);
			low.put(w, G.nodes.size());
		} while (!w.equals(v));
		count++;
	}
	
//	Returns the number of strong components.
	private int count() {
		return count;
	}
	
//	Are vertices v and w in the same strong component?
	private boolean stronglyConnected(String v, String w) {
		return id.get(v) == id.get(w);
	}
	
	public static void main(String[] args) throws Exception {
		DependencyDAG G = new DependencyDAG(); 
		
//		PrintWriter pw = new PrintWriter(new File("metabolic_networks//rat-consolidated.txt"));		
//		G.isMetabolic = true;
//		G.loadCallGraph("metabolic_networks//rat-links.txt"); // "metabolic_networks//rat-links.txt"
		
		PrintWriter pw = new PrintWriter(new File("jdk_class_dependency//commons-math-callgraph-consolidated.txt"));		
		G.isClassDependency = true;
		G.loadCallGraph("jdk_class_dependency//commons-math-callgraph.txt"); 
		
//		openssh_callgraphs//full.graph-openssh-39
//		String graphFile = "sw_callgraphs//full-graph-Sqlite";
//		PrintWriter pw = new PrintWriter(new File(graphFile + "-consolidated"));		
//		G.isCallgraph = true;
//		G.loadCallGraph(graphFile); 
		
		TarjanSCC scc = new TarjanSCC(G);
		// number of connected components
		int M = scc.count();
//		for (int i = 1; i <= M; ++i) {
//			System.out.println(i + " \"scc-" + i + "\"");
//		}
		System.out.println(M + " components from " + G.nodes.size() + " initial nodes");

		HashMap<String, String> sccIdMap = new HashMap();
		HashMap<String, HashSet<String>> sccs = new HashMap();
		
		for (String v : G.nodes) {
			String sccId = String.valueOf(scc.id.get(v) + 1);
			sccIdMap.put(v, sccId);
//			System.out.println(v + "\t" + sccId);
			
			if (sccs.containsKey(sccId)) {
				sccs.get(sccId).add(v);
			}
			else {
				HashSet hashSet = new HashSet();
				hashSet.add(v);
				sccs.put(sccId, hashSet);
			}
		}
		
		int kNonTrivialSCC = 0;
		ArrayList<Double> numbers = new ArrayList();
		for (String s: sccs.keySet()) {
			if (sccs.get(s).size() < 2) continue; // show the non-trivial SCCs only
			++kNonTrivialSCC;
//			System.out.print(s + "\t");
//			for (String r: sccs.get(s)) {
//				System.out.print(r + ", ");
//			}
//			System.out.println();
//			System.out.println(sccs.get(s).size());
			numbers.add(sccs.get(s).size() * 1.0);
		}
		System.out.println("Number of Non-trivial SCCs: " + kNonTrivialSCC);
		
		double stat[] = StatisticalUtilTest.getMeanSTD(numbers);
		System.out.println(stat[0] + "\t" + stat[1]);
		
		for (String v: G.nodes) {
			String srcSCCId = sccIdMap.get(v);
			if (G.serves.containsKey(v)) {
				for (String r: G.serves.get(v)) {
					String tgtSCCId = sccIdMap.get(r);
					if (!srcSCCId.equals(tgtSCCId)) {
						pw.println(srcSCCId + "\t" + tgtSCCId);
//						System.out.println(srcSCCId + " x " + tgtSCCId);
					}
				}
			}
		}
		
		pw.close();
	}
}
