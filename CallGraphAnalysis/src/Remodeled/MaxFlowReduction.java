package Remodeled;

import java.io.File;
import java.io.PrintWriter;

public class MaxFlowReduction {
	
	public static void reduceToMaxFlowMinCutNetwork(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("reduced_maxflow_graphs//" + filePath + "_maxflow_reduced.txt"));
		
		// add super source
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.depends.containsKey(s)) { // is source
				pw.println("super_source" + "\t" + s + "\t" + "capacity=" + dependencyDAG.outDegree.get(s));
			}
		}
		
		// add super target
		for (String t: dependencyDAG.nodes) {
			if (!dependencyDAG.serves.containsKey(t)) { // is target
				pw.println("super_target" + "\t" + t + "\t" + "capacity=" + dependencyDAG.inDegree.get(t));
			}
		}
		
		// add in-edges for all nodes except targets
		for (String n: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(n) && dependencyDAG.depends.containsKey(n)) { // is intermediate
				pw.println(n + "-1" + "\t" + n + "-2" + "\t" + "capacity=1"); // split node
				
				for (String m: dependencyDAG.depends.get(n)) {
					if (!dependencyDAG.depends.containsKey(m)) { // is source
						pw.println(m + "\t" + n + "-1" + "\t" + "capacity=INF"); 
					}
					else { // is intermediate
						pw.println(m + "-2" + "\t" + n + "-1" + "\t" + "capacity=INF"); 	
					}
				}
			}
		}
		
		// add in-edges for targets
		for (String n: dependencyDAG.nodes) {
			if (!dependencyDAG.serves.containsKey(n)) { // is target
				for (String m: dependencyDAG.depends.get(n)) {
					if (!dependencyDAG.depends.containsKey(m)) { // is a source
						pw.println(m + "\t" + n + "\t" + "capacity=INF"); 
					}
					else { // is intermediate
						pw.println(m + "-2" + "\t" + n + "\t" + "capacity=INF"); 	
					}
				}
			}
		}
		
		pw.close();
	}
}
