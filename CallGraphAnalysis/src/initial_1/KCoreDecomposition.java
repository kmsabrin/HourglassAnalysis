package initial_1;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.collect.TreeMultimap;

public class KCoreDecomposition {
	HashMap<String, Integer> nodeCore = new HashMap<String, Integer>();
	HashMap<String, Integer> nodeDegree = new HashMap<String, Integer>();
	
	public KCoreDecomposition() {
		nodeCore = new HashMap<String, Integer>();
		nodeDegree = new HashMap<String, Integer>();
	}
	
	public void getCores(CallDAG callDAG) {
		TreeMultimap<Integer, String> degreeSortedNodes = TreeMultimap.create();
		
		for (String f: callDAG.inDegree.keySet()) {
			int degree = callDAG.inDegree.get(f) + callDAG.outDegree.get(f);
			nodeDegree.put(f, degree);
			degreeSortedNodes.put(degree, f);
		}
		
		int core = 1;
		int kount = 0;
		while (kount < callDAG.location.size()) {
			if (!degreeSortedNodes.containsKey(core)) continue;
			Queue<String> nodeQ = new LinkedList<String>(degreeSortedNodes.get(core));			
			while (!nodeQ.isEmpty()) {
				String f = nodeQ.poll();
				nodeCore.put(f, core);
				++kount;
				
				if (callDAG.callFrom.containsKey(f)) {
					for (String iN: callDAG.callFrom.get(f)) {
						int d = nodeDegree.get(iN);
						if (d > core) {
							degreeSortedNodes.remove(d, iN);
							nodeDegree.put(iN, d - 1);
							if (d == core + 1) {
								nodeQ.add(iN);
							}
							else {
								degreeSortedNodes.put(d - 1, iN);
							}
						}
					}
				}
				
				if (callDAG.callTo.containsKey(f)) {
					for (String oN: callDAG.callTo.get(f)) {
						int d = nodeDegree.get(oN);
						if (d > core) {
							degreeSortedNodes.remove(d, oN);
							nodeDegree.put(oN, d - 1);
							if (d == core + 1) {
								nodeQ.add(oN);
							} else {
								degreeSortedNodes.put(d - 1, oN);
							}
						}
					}
				}				
			}
			
			++core;
		}
		
		for (String f: nodeCore.keySet()) {
//				System.out.println(f + "\t" + nodeCore.get(f));
		}
	}
}
