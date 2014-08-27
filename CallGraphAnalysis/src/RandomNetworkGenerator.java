import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RandomNetworkGenerator {
	CallDAG callDAG;
	Map<String, Integer> functionLevel;
	Set<String> visited;
	
	public RandomNetworkGenerator(CallDAG callDAG) {
		this.callDAG = callDAG;
		functionLevel = new HashMap();
		visited = new HashSet();
	}
	
	public void traverse(String function) {
		if (!callDAG.callTo.containsKey(function)) { // is Leaf
			functionLevel.put(function, 1);
			return;
		}
		
		if (functionLevel.containsKey(function)) { // has been Traversed
			return;
		}
		
		if (visited.contains(function)) { // is Cycle
			functionLevel.put(function, 1); // is Wrong
			return;
		}
		
		visited.add(function);
		
		int level = 1;
		for (String f: callDAG.callTo.get(function)) {
			traverse(f);
			int childLevel = functionLevel.get(f);
			level = Math.max(level, childLevel + 1);
		}
		
		functionLevel.put(function, level);
	}
	
	public void getFunctionLevel() {
		for (String f: callDAG.functions) {
			if (!callDAG.callFrom.containsKey(f)) { // is Root
				traverse(f);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
	}
	
	public void propagateUpward(String fn) {
		if (visited.contains(fn)) return; // is Cycle
		if (!functionLevel.containsKey(fn)) return; // w-t-f
		
		int updatedlfn = 1; // updated level of function n
		for (String s: callDAG.callTo.get(fn)) {
			if (functionLevel.containsKey(s) && functionLevel.get(s) + 1 > updatedlfn) {
				updatedlfn = functionLevel.get(s) + 1; 
			}
		}
		
		visited.add(fn);
		
		if (updatedlfn != functionLevel.get(fn)) { // change in level
			// propagate upwards
			functionLevel.put(fn, updatedlfn);
			if (callDAG.callFrom.containsKey(fn)) {
				for (String s : callDAG.callFrom.get(fn)) {
					propagateUpward(s);
				}
			}
		}
	}
	
	public void chooseEdgePairsAndSwap() {
		int nFunctions = functionLevel.size();
		Object[] functionNames = functionLevel.keySet().toArray();
		Random random = new Random();
//		Random random = new Random(1221388376679119L); //113355, 335577, 557789
		int kount = 0;
		
		while(kount < callDAG.nEdges * 10) {
			int rs1, rs2; // random_index_source_1 = rs1, random_index_source_2 = rs2
			String fs1, fs2; // function-name_source_1 = fs1, function-name_source_2 = fs2
			
			do {
				rs1 = random.nextInt(nFunctions);
				fs1 = (String)functionNames[rs1];
			} 
			while (!callDAG.callTo.containsKey(fs1));	
			
			do {
				rs2 = random.nextInt(nFunctions);
				fs2 = (String)functionNames[rs2];	
			} 
			while (!callDAG.callTo.containsKey(fs2));
			
			int ls1 = functionLevel.get(fs1); // level_source_1 = ls1, level_source_2 = ls2
			int ls2 = functionLevel.get(fs2);
			
			if (rs1 == rs2) {
				continue; // same source, no swap will occur
			}
						
			List<String> callToListS1 = new ArrayList();
			for (String s: callDAG.callTo.get(fs1)) {
				if (functionLevel.get(s) < ls2) {
					callToListS1.add(s);
				}
			}
			
			List<String> callToListS2 = new ArrayList();
			for (String s: callDAG.callTo.get(fs2)) {
				if (functionLevel.get(s) < ls1) {
					callToListS2.add(s);
				}
			}
			
			if (callToListS1.size() < 1 || callToListS2.size() < 1) {
				continue; // no suitable target with expected level is found at least for one of the sources
			}
			
			int rt1 = random.nextInt(callToListS1.size()); // random_index_target_1 = rt1, random_index_target_2 = rt2
			String ft1 = callToListS1.get(rt1); // function-name_target_1 = ft1, function-name_target_2 = ft2
			int lt1 = functionLevel.get(ft1); // level_target_1 = lt1, level_target_2 = lt2
			
			int rt2 = random.nextInt(callToListS2.size());
			String ft2 = callToListS2.get(rt2);
			int lt2 = functionLevel.get(ft2);
			
//			check if already exists, then no swap, start over
			if (callDAG.callTo.get(fs1).contains(ft2) || callDAG.callTo.get(fs2).contains(ft1)) {
				continue;
			}
			
//			swap ...
			++kount;
//			System.out.println("Swapped (" + fs1 + "," + ft1 + ") with (" + fs2 + "," + ft2 + ")");	

//			should the callTo/callFrom be made Set! (done!)
			callDAG.callTo.get(fs1).remove(ft1);
			callDAG.callTo.get(fs1).add(ft2);
			
			callDAG.callTo.get(fs2).remove(ft2);
			callDAG.callTo.get(fs2).add(ft1);
			
			callDAG.callFrom.get(ft1).remove(fs1);
			callDAG.callFrom.get(ft1).add(fs2);
			
			callDAG.callFrom.get(ft2).remove(fs2);
			callDAG.callFrom.get(ft2).add(fs1);
		
			// level propagation
			int updatedls1 = 1; // updated_level_source_1 = updatedls1
			for (String s: callDAG.callTo.get(fs1)) {
				if (functionLevel.get(s) + 1 > updatedls1) {
					updatedls1 = functionLevel.get(s) + 1; 
				}
			}
			
			if (updatedls1 != ls1) { // if change (decrease) in level of source 1
				// propagate upwards;
				functionLevel.put(fs1, updatedls1);
				if (callDAG.callFrom.containsKey(fs1)) {
					for (String s : callDAG.callFrom.get(fs1)) {
						visited.clear();
						propagateUpward(s);
					}
				}
				continue; // change of level is exclusive for sources, only one can happen at a time
			}
			
			int updatedls2 = 1; // updated_level_source_2 = updatedls2
			for (String s: callDAG.callTo.get(fs2)) {
				if (functionLevel.get(s) + 1 > updatedls2) {
					updatedls2 = functionLevel.get(s) + 1; 
				}
			}
			
			if (updatedls2 != ls2) { // if change (decrease) in level of source 2
				// propagate upwards;
				functionLevel.put(fs2, updatedls2);
				if (callDAG.callFrom.containsKey(fs2)) {
					for (String s : callDAG.callFrom.get(fs2)) {
						visited.clear();
						propagateUpward(s);
					}
				}
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
	}
	
	public void generateRandomNetwork() {
		getFunctionLevel();
		chooseEdgePairsAndSwap();
	}
}
