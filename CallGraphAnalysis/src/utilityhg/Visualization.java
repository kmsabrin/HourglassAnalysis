package utilityhg;

import corehg.DependencyDAG;

public class Visualization {

	public static void printDOTNetwork(DependencyDAG dependencyDAG) {
		//PIK3CA_5 [shape=box, style=filled, fillcolor=greenyellow, label="PIK3CA"];
		//CREB1_4 -> SOX9_5;
		
		System.out.println("digraph G {");
		//System.out.println("graph [nodesep=0.1, ranksep=3];");
		System.out.println("node [label=\"\"];");
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				System.out.println(s + " [width=0.35, shape=circle, style=filled, fillcolor=orange, penwidth=0.4];");
			}
			else if (dependencyDAG.isTarget(s)) {
				System.out.println(s + " [width=0.35, shape=circle, style=filled, fillcolor=blue, penwidth=0.4];");
			}
			else {
				System.out.println(s + " [width=0.35, shape=circle, style=filled, fillcolor=green, penwidth=0.4];");
			}
			
			if (dependencyDAG.isSource(s)) {
				continue;
			}
			
			for (String r: dependencyDAG.depends.get(s)) {
				System.out.println(s + " -> " + r  + " [dir=back];");
			}
		}
		
		System.out.println("}");
	}
}
