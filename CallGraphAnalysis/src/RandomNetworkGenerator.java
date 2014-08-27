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
	boolean isCycle;
	
	public RandomNetworkGenerator(CallDAG callDAG) {
		this.callDAG = callDAG;
		functionLevel = new HashMap();
		visited = new HashSet();
		isCycle = false;
	}
	
	public void getFunctionLevelTraverse(String function) {
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
			getFunctionLevelTraverse(f);
			int childLevel = functionLevel.get(f);
			level = Math.max(level, childLevel + 1);
		}
		
		functionLevel.put(function, level);
		visited.remove(function);
	}
	
	public void getFunctionLevel() {
		visited = new HashSet();
		for (String f: callDAG.functions) {
			if (!callDAG.callFrom.containsKey(f)) { // is Root
				getFunctionLevelTraverse(f);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
	}
	
	public void getFunctionLevelWithCutOffTraverse(String function, int cutOffLevel) {		
		if (visited.contains(function)) { // is Cycle or Revisit, 
			return; // is Wrong Anyway
		}		
		visited.add(function);
		
		if (functionLevel.get(function) < cutOffLevel) { // is at the cutOff update level
			return;
		}
		
		int level = 1;
		for (String f: callDAG.callTo.get(function)) {
			getFunctionLevelTraverse(f);
			int childLevel = functionLevel.get(f);
			level = Math.max(level, childLevel + 1);
		}
		
		functionLevel.put(function, level);
	}
	
	public void getFunctionLevelWithCutOff(int cutOffLevel) {
		visited = new HashSet();
		for (String f: callDAG.functions) {
			if (!callDAG.callFrom.containsKey(f)) { // is Root
				getFunctionLevelWithCutOffTraverse(f, cutOffLevel);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
	}
	
	public void cycleCheckTraverse(String node, String target, int targetLevel) {
		if (node.equals(target)) { // target Found, cycle Exists
			isCycle = true;
			return;
		}
		
		if (isCycle) return; // target Already Found
		if (visited.contains(node)) return; // already Traversed
		visited.add(node);
		
		if (functionLevel.containsKey(node) && functionLevel.get(node) <= targetLevel) return; // below the Target Level
		if (!callDAG.callTo.containsKey(node)) return; // a leaf
		
		for (String f: callDAG.callTo.get(node)) {
			cycleCheckTraverse(f, target, targetLevel);
		}
	}
	
	public void cycleCheck(String source, String target, int targetLevel) {
		visited.clear();
		isCycle = false;
		cycleCheckTraverse(source, target, targetLevel);
	}
	
	public void chooseEdgePairsAndSwap() {
		int nFunctions = functionLevel.size();
		Object[] functionNames = functionLevel.keySet().toArray();
		Random random = new Random();
//		Random random = new Random(1221388376679119L); //113355, 335577, 557789
		int kount = 0;
		
		while(kount < callDAG.nEdges * 1) {
			int rs1, rs2; // random_index_source_1 = rs1, random_index_source_2 = rs2
			String fs1, fs2; // function-name_source_1 = fs1, function-name_source_2 = fs2
			int ls1, ls2; // level_source_1 = ls1, level_source_2 = ls2
			
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
						
			ls1 = functionLevel.get(fs1); 
			ls2 = functionLevel.get(fs2);
						
			List<String> callToListS1 = new ArrayList(callDAG.callTo.get(fs1));
			List<String> callToListS2 = new ArrayList(callDAG.callTo.get(fs2));
			
			int rt1, rt2; // random_index_target_1 = rt1, random_index_target_2 = rt2
			String ft1, ft2; // function-name_target_1 = ft1, function-name_target_2 = ft2
			int lt1, lt2; // level_target_1 = lt1, level_target_2 = lt2
			
			rt1 = random.nextInt(callToListS1.size()); 
			ft1 = callToListS1.get(rt1); 
			lt1 = functionLevel.get(ft1); 
			
			rt2 = random.nextInt(callToListS2.size());
			ft2 = callToListS2.get(rt2);
			lt2 = functionLevel.get(ft2);
			
//			check if already exists, then no swap, start over
			if (callDAG.callTo.get(fs1).contains(ft2) || callDAG.callTo.get(fs2).contains(ft1)) {
				continue;
			}
			
//			cycle check
			if (ls1 <= lt2) {
//				check if s1 is reachable from t2
				cycleCheck(ft2, fs1, ls1);
				if (isCycle) {
					continue;
				}
			}
			else if (ls2 <= lt1) {
//				check if s2 is reachable from t1
				cycleCheck(ft1, fs2, ls2);
				if (isCycle) {
					continue;
				}
			}
			
//			swap ...
			++kount;
			System.out.println("Swap count: " + kount);
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
			
			if (updatedls1 != ls1) { // if change in level of source 1
				functionLevel.put(fs1, updatedls1);
				getFunctionLevelWithCutOff(Math.min(updatedls1, ls1) + 1);
			}
			
			int updatedls2 = 1; // updated_level_source_2 = updatedls2
			for (String s: callDAG.callTo.get(fs2)) {
				if (functionLevel.get(s) + 1 > updatedls2) {
					updatedls2 = functionLevel.get(s) + 1; 
				}
			}
			
			if (updatedls2 != ls2) { // if change  in level of source 2
				functionLevel.put(fs2, updatedls2);
				getFunctionLevelWithCutOff(Math.min(updatedls2, ls2) + 1);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
//		
//		for (String f: callDAG.functions) {
//			System.out.print(f + " calling ");
//			if (!callDAG.callTo.containsKey(f)) {
//				System.out.println();
//				continue;
//			}
//			for (String s: callDAG.callTo.get(f)) {
//				System.out.print(s + " ");
//			}
//			System.out.println();
//		}
	}
	
	public void generateRandomNetwork() {
		getFunctionLevel();
		chooseEdgePairsAndSwap();
	}
}
