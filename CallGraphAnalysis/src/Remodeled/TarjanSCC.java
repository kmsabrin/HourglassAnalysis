package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/*************************************************************************
 *  Compute the strongly-connected components of a digraph using 
 *  Tarjan's algorithm.
 *
 *  Runs in O(E + V) time.
 *
 *  toy_dag_1.txt
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
	private HashMap<String, Integer> id; // id[v] = id of strong componentcontaining v
	private HashMap<String, Integer> low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private Stack<String> stack;

	private TarjanSCC(DependencyDAG G) {
		marked = new HashSet();
		stack = new Stack();
		id = new HashMap();
		low = new HashMap();
		for (String v : G.functions) {
			if (!marked.contains(v))
				dfs(G, v);
		}
	}

	private void dfs(DependencyDAG G, String v) {
		marked.add(v);
		low.put(v, pre++);
		int min = low.get(v);
		stack.push(v);
		if (G.serves.containsKey(v)) {
			for (String w : G.serves.get(v)) {
				if (!marked.contains(w))
					dfs(G, w);
				if (low.get(w) < min)
					min = low.get(w);
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
			low.put(w, G.functions.size());
		} while (!w.equals(v));
		count++;
	}

	
//	Returns the number of strong components.
	private int count() {
		return count;
	}

	
//	Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
	private boolean stronglyConnected(String v, String w) {
		return id.get(v) == id.get(w);
	}
	
	public static void main(String[] args) throws Exception {
		PrintWriter pw = new PrintWriter(new File("metabolic_networks//rat-consolidated.txt"));
		
		DependencyDAG G = new DependencyDAG(); 
		G.loadCallGraph("metabolic_networks//rat-links.txt"); // "metabolic_networks//rat-links.txt"
		
		TarjanSCC scc = new TarjanSCC(G);
		// number of connected components
		int M = scc.count();
		System.out.println(M + " components from " + G.functions.size() + " metabolites");

		HashMap<String, String> sccIdMap = new HashMap();
		for (String v : G.functions) {
			String sccId = String.valueOf(scc.id.get(v));
			sccIdMap.put(v, sccId);
			System.out.println(v + "\t" + sccId);
		}
		
		for (String v: G.functions) {
			String srcSCCId = sccIdMap.get(v);
			if (G.serves.containsKey(v)) {
				for (String r: G.serves.get(v)) {
					String tgtSCCId = sccIdMap.get(r);
					if (!srcSCCId.equals(tgtSCCId)) {
						pw.println(srcSCCId + "\t" + tgtSCCId); 
					}
				}
			}
		}
		
		pw.close();
	}
}
