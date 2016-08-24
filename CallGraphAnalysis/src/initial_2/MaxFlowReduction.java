package initial_2;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.TreeMap;

import corehg.DependencyDAG;

public class MaxFlowReduction {
	
	public static void reduceToMaxFlowMinCutNetwork(DependencyDAG dependencyDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("reduced_maxflow_graphs//" + filePath + "_maxflow_reduced.txt"));
		
		int INF = 1000000;
		
		// add super source
		for (String s: dependencyDAG.nodes) {
			if (!dependencyDAG.depends.containsKey(s)) { // is source
				pw.println("super_source" + "\t" + s  + "\t" + "{\'capacity\':" + INF + "}"); 
			}
		}
		
		// add super target
		for (String t: dependencyDAG.nodes) {
			if (!dependencyDAG.serves.containsKey(t)) { // is target
				pw.println(t + "\t" + "super_target"  + "\t" + "{\'capacity\':" + INF + "}");
			}
		}
		
		// add in-edges for all nodes except targets
		for (String n: dependencyDAG.nodes) {
			if (dependencyDAG.serves.containsKey(n) && dependencyDAG.depends.containsKey(n)) { // is intermediate
				pw.println(n + "-1" + "\t" + n + "-2" + "\t" + "{\'capacity\':1}"); 
				
				for (String m: dependencyDAG.depends.get(n)) {
					if (!dependencyDAG.depends.containsKey(m)) { // is source
						pw.println(m + "\t" + n + "-1" + "\t" + "{\'capacity\':" + INF + "}");  
					}
					else { // is intermediate
						pw.println(m + "-2" + "\t" + n + "-1" + "\t" + "{\'capacity\':" + INF + "}"); 					}
				}
			}
		}
		
		// add in-edges for targets
		for (String n: dependencyDAG.nodes) {
			if (!dependencyDAG.serves.containsKey(n)) { // is target
				for (String m: dependencyDAG.depends.get(n)) {
					if (!dependencyDAG.depends.containsKey(m)) { // is a source
						pw.println(m + "\t" + n + "\t" + "{\'capacity\':1}"); // why put them in? they will be removed for sure
					}
					else { // is intermediate
						pw.println(m + "-2" + "\t" + n + "\t" + "{\'capacity\':" + INF + "}"); 	
					}
				}
			}
		}
		
		pw.close();
	}
	
	public static void analyzeMinCut(DependencyDAG dependencyDAG, String filePath) throws Exception {
		Scanner scanner = new Scanner(new File(filePath));
		
		int cutValue = scanner.nextInt();
		
		int directBypasses = 0;
		
		while(scanner.hasNext()) {
			String srcNode = scanner.next();
			String tgtNode = scanner.next();
			
			if(srcNode.endsWith("-1") && tgtNode.endsWith("-2")) {
				srcNode = srcNode.substring(0, srcNode.length() - 2);
				tgtNode = tgtNode.substring(0, tgtNode.length() - 2);
				System.out.println(srcNode + "\t" + dependencyDAG.normalizedPathCentrality.get(srcNode));
			}
			else if (!srcNode.endsWith("-1") && !tgtNode.endsWith("-1") && !srcNode.endsWith("-2") && !tgtNode.endsWith("-2")) {
				++directBypasses;
			}
			else {
//				if(srcNode.endsWith("-1")) srcNode = srcNode.substring(0, srcNode.length() - 2);
//				if(tgtNode.endsWith("-1")) tgtNode = tgtNode.substring(0, tgtNode.length() - 2);
//				System.out.println("Why: " + srcNode + "\t" + tgtNode);
			}
		}
		
		System.out.println("Minimum cut vertex: " + cutValue);
		System.out.println("Direct source-to-target bypasses: " + directBypasses);
	}
}
