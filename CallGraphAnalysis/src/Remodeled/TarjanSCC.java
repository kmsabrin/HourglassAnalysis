package Remodeled;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

/*************************************************************************
 *  Compilation:  javac TarjanSCC.java
 *  Execution:    Java TarjanSCC V E
 *  Dependencies: Digraph.java Stack.java TransitiveClosure.java StdOut.java
 *
 *  Compute the strongly-connected components of a digraph using 
 *  Tarjan's algorithm.
 *
 *  Runs in O(E + V) time.
 *
 *  % java TarjanSCC tinyDG.txt
 *  5 components
 *  1 
 *  0 2 3 4 5
 *  9 10 11 12
 *  6 8
 *  7 
 *
 *************************************************************************/

/**
 * The <tt>TarjanSCC</tt> class represents a data type for determining the
 * strong components in a digraph. The <em>id</em> operation determines in which
 * strong component a given vertex lies; the <em>areStronglyConnected</em>
 * operation determines whether two vertices are in the same strong component;
 * and the <em>count</em> operation determines the number of strong components.
 * 
 * The <em>component identifier</em> of a component is one of the vertices in
 * the strong component: two vertices have the same component identifier if and
 * only if they are in the same strong component.
 * 
 * <p>
 * This implementation uses Tarjan's algorithm. The constructor takes time
 * proportional to <em>V</em> + <em>E</em> (in the worst case), where <em>V</em>
 * is the number of vertices and <em>E</em> is the number of edges. Afterwards,
 * the <em>id</em>, <em>count</em>, and <em>areStronglyConnected</em> operations
 * take constant time. For alternate implementations of the same API, see
 * {@link KosarajuSharirSCC} and {@link GabowSCC}.
 * <p>
 * For additional documentation, see <a href="/algs4/42digraph">Section 4.2</a>
 * of <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 * 
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class TarjanSCC {

	private HashSet<String> marked; // marked[v] = has v been visited?
	private HashMap<String, Integer> id; // id[v] = id of strong component
											// containing v
	private HashMap<String, Integer> low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private Stack<String> stack;

	/**
	 * Computes the strong components of the digraph <tt>G</tt>.
	 * 
	 * @param G
	 *            the digraph
	 */
	public TarjanSCC(DependencyDAG G) {
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

	/**
	 * Returns the number of strong components.
	 * 
	 * @return the number of strong components
	 */
	public int count() {
		return count;
	}

	/**
	 * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
	 * 
	 * @param v
	 *            one vertex
	 * @param w
	 *            the other vertex
	 * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the
	 *         same strong component, and <tt>false</tt> otherwise
	 */
	public boolean stronglyConnected(String v, String w) {
		return id.get(v) == id.get(w);
	}

	/**
	 * Unit tests the <tt>TarjanSCC</tt> data type.
	 */
	public static void main(String[] args) {
		DependencyDAG G = new DependencyDAG("metabolic_networks//rat_new.txt");
		TarjanSCC scc = new TarjanSCC(G);

		// number of connected components
		int M = scc.count();
		System.out.println(M + " components from " + G.functions.size() + " metabolites");

		// compute list of vertices in each strong component
		HashMap<Integer, HashSet<String>> components = new HashMap();
		for (int i = 0; i < M; i++) {
			components.put(i, new HashSet<String>());
		}
		for (String v : G.functions) {
			components.get(scc.id.get(v)).add(v);
		}

		// print results
		for (int i : components.keySet()) {
			for (String v : components.get(i)) {
				System.out.print(v + " ");
			}
			System.out.println();
		}
	}
}
