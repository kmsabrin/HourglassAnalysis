import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;

public class DiameterAnalysis {
	Map<String, Integer> maxDepthToLeaf;
	Set<String> visited;
	
	public int getMaxLeafDepth(String node, int len, CallDAG callDAG) {
		if (!callDAG.callTo.containsKey(node)) { // is a leaf
			maxDepthToLeaf.put(node, 0);
			return 0;
		}
		
		if (visited.contains(node)) {
			if (!maxDepthToLeaf.containsKey(node)) 
				return //len;
						0;
			return maxDepthToLeaf.get(node);
		}
		
		visited.add(node);
		
		int maxD = -1;
		for (String s: callDAG.callTo.get(node)) {
			int d = getMaxLeafDepth(s, len + 1, callDAG) + 1;
			if (d > maxD) maxD = d;
		}
		
		maxDepthToLeaf.put(node, maxD);
		return maxD;
	}
	
	public void getEffectiveDiameter(CallDAG callDAG) {
		List<Integer> d = new ArrayList();
		visited = new HashSet();
		maxDepthToLeaf = new HashMap();

		for (String s: callDAG.functions) {
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				int maxD = getMaxLeafDepth(s, 0, callDAG); 
				d.add(maxD);
//				System.out.println("Root: " + s + " Max Depth: " + maxD);
			}
		}
		
		double b[] = new double[d.size()];
		for (int i = 0; i < d.size(); ++i) {
			b[i] = d.get(i);
		}
		double diaP90 = StatUtils.percentile(b, 90.0);
		
		System.out.println(diaP90);
	}
	
	public void getEffectiveDiameterForAllVersions() {
		for (int i = Driver.versiontStart; i < Driver.versionEnd; i++) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			getEffectiveDiameter(callDAG);
		}
	}
}
