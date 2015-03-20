import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;

public class RandomNetworkGenerator {
	CallDAG randomCallDAG;
	Map<String, Integer> functionLevel;
	Set<String> visited;
	Set<String> cycleVisited;
	boolean isReachable;
	String randomVersionNumber;
	int numOfIteration = 1;
	Random random;
	
	public RandomNetworkGenerator(CallDAG callDAG) {
		this.randomCallDAG = callDAG;
		functionLevel = new HashMap();
		visited = new HashSet();
		cycleVisited = new HashSet();
		isReachable = false;
		random = new Random(System.nanoTime()); 
	}
	
	public void getNodeLevelTraverse(String function) {
		if (!randomCallDAG.callTo.containsKey(function)) { // is Leaf
			functionLevel.put(function, 1);
			return;
		}
		
		if (functionLevel.containsKey(function)) { // has been Traversed
			return;
		}
				
		int level = 1;
		for (String f: randomCallDAG.callTo.get(function)) {
			getNodeLevelTraverse(f);
			int childLevel = functionLevel.get(f);
			level = Math.max(level, childLevel + 1);
		}
		
		functionLevel.put(function, level);
	}
	
	public void getNodeLevel() {
		for (String f: randomCallDAG.functions) {
			if (!randomCallDAG.callFrom.containsKey(f)) { // is Root
				getNodeLevelTraverse(f);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
		
//		compute the median level of all nodes
		double a[] = new double[functionLevel.values().size()];
		int j = 0;
		for (int i : functionLevel.values()) {
			a[j++] = i;
		}
		System.out.println("Intial Median Level: " + StatUtils.percentile(a, 50.0));
	}
	
	public void updateFunctionLevelWithCutOffTraverse(String function, int cutOffLevel) {		
		if (visited.contains(function)) { // is Revisit 
			return;
		}		
		
		if (functionLevel.get(function) < cutOffLevel) { // is at the cutOff update level
			return;
		}
		
		int level = 1;
		for (String f: randomCallDAG.callTo.get(function)) {
			updateFunctionLevelWithCutOffTraverse(f, cutOffLevel);
			int childLevel = functionLevel.get(f);
			level = Math.max(level, childLevel + 1);
		}
		
		functionLevel.put(function, level);
		visited.add(function);
	}
	
	public void updateFunctionLevelWithCutOff(int cutOffLevel) {
		visited = new HashSet();
		for (String f: randomCallDAG.functions) {
			if (!randomCallDAG.callFrom.containsKey(f)) { // is Root
				updateFunctionLevelWithCutOffTraverse(f, cutOffLevel);
			}
		}
		
//		for (String f: functionLevel.keySet()) {
//			System.out.println("Function: " + f + " Level: " + functionLevel.get(f));
//		}
	}
	
	public void checkReachablityTraverse(String node, String target, int targetLevel) {
		if (node.equals(target)) { // target Found, reachable, cycle created
			isReachable = true;
			return;
		}
		
		if (isReachable) return; // target Already Found
		if (visited.contains(node)) return; // already Traversed
		visited.add(node);
		
		if (functionLevel.containsKey(node) && functionLevel.get(node) <= targetLevel) return; // below the Target Level
		if (!randomCallDAG.callTo.containsKey(node)) return; // a leaf
		
		for (String f: randomCallDAG.callTo.get(node)) {
			checkReachablityTraverse(f, target, targetLevel);
		}
	}
	
	public void checkReachablity(String source, String target, int targetLevel) {
		visited.clear();
		isReachable = false;
		checkReachablityTraverse(source, target, targetLevel);
	}
	
	public void checkCycleExistenceTraverse(String node) {
		if (!randomCallDAG.callTo.containsKey(node) || visited.contains(node))
			return;

		visited.add(node);
		cycleVisited.add(node); // cycle check

		for (String s : randomCallDAG.callTo.get(node)) {
			if (cycleVisited.contains(s)) {
				isReachable = true;
				continue;
			}
			checkCycleExistenceTraverse(s);
		}
		
		cycleVisited.remove(node);
	}
	
	public void checkCycleExistence() {
		visited.clear();
		cycleVisited.clear();
		isReachable = false;
		for (String s: randomCallDAG.functions) {
			if (!visited.contains(s)) {
				checkCycleExistenceTraverse(s);
			}
		}
	}
	
	public void chooseEdgePairsAndSwap() throws Exception {
		int nFunctions = functionLevel.size();
		Object[] functionNames = functionLevel.keySet().toArray();
		Random random = new Random(System.nanoTime()); 
		int kount = 0;
		int nAttempts = 0;
		int nEventA = 0, nEventB = 0;
		
		PrintWriter pw1 = new PrintWriter(new File("Results//random-level-medians-" + randomVersionNumber + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//rewiring-events-" + randomVersionNumber + ".txt"));		
		
		System.out.println("Making " + (randomCallDAG.nEdges * numOfIteration) + " swaps");
		while(kount < randomCallDAG.nEdges * numOfIteration) {
//			Random random = new Random(System.nanoTime());
			int rs1, rs2; // random_index_source_1 = rs1, random_index_source_2 = rs2
			String fs1, fs2; // function-name_source_1 = fs1, function-name_source_2 = fs2
			int ls1, ls2; // level_source_1 = ls1, level_source_2 = ls2
			
			do {
				rs1 = random.nextInt(nFunctions);
				fs1 = (String)functionNames[rs1];
			} 
			while (!randomCallDAG.callTo.containsKey(fs1));	
			
			do {
				rs2 = random.nextInt(nFunctions);
				fs2 = (String)functionNames[rs2];	
			} 
			while (!randomCallDAG.callTo.containsKey(fs2));
						
			ls1 = functionLevel.get(fs1);
			ls2 = functionLevel.get(fs2);
						
			List<String> callToListS1 = new ArrayList(randomCallDAG.callTo.get(fs1));
			List<String> callToListS2 = new ArrayList(randomCallDAG.callTo.get(fs2));
			
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
			if (randomCallDAG.callTo.get(fs1).contains(ft2) || randomCallDAG.callTo.get(fs2).contains(ft1)) {
				continue;
			}
			
			++nAttempts; // skipping the already existing edge attempts and leaf chosen as source attempts			
			
//			cycle check
			if (ls1 <= lt2) {
//				check if s1 is reachable from t2
				checkReachablity(ft2, fs1, ls1);
				if (isReachable) {
					continue;
				}
			}
			else if (ls2 <= lt1) {
//				check if s2 is reachable from t1
				checkReachablity(ft1, fs2, ls2);
				if (isReachable) {
					continue;
				}
			}
			
//			swap ...
			++kount;
//			System.out.println("Swap count: " + kount);
//			System.out.println("Swapped (" + fs1 + "," + ft1 + ") with (" + fs2 + "," + ft2 + ")");	

//			these are being measured for statistical significance
			if ((ls1 > lt2) && (ls2 > lt1)) ++nEventA;
			else ++nEventB;
			
//			should the callTo/callFrom be made Set! (fixed,done!)
			randomCallDAG.callTo.get(fs1).remove(ft1);
			randomCallDAG.callTo.get(fs1).add(ft2);
			
			randomCallDAG.callTo.get(fs2).remove(ft2);
			randomCallDAG.callTo.get(fs2).add(ft1);
			
			randomCallDAG.callFrom.get(ft1).remove(fs1);
			randomCallDAG.callFrom.get(ft1).add(fs2);
			
			randomCallDAG.callFrom.get(ft2).remove(fs2);
			randomCallDAG.callFrom.get(ft2).add(fs1);
		
			// level propagation
			int updatedls1 = 1; // updated_level_source_1 = updatedls1
			for (String s: randomCallDAG.callTo.get(fs1)) {
				updatedls1 = Math.max(updatedls1, functionLevel.get(s) + 1);
			}
			
			if (updatedls1 != ls1) { // if change in level of source 1
				functionLevel.put(fs1, updatedls1);
				updateFunctionLevelWithCutOff(Math.min(updatedls1, ls1) + 1);
			}
			
			int updatedls2 = 1; // updated_level_source_2 = updatedls2
			for (String s: randomCallDAG.callTo.get(fs2)) {
				updatedls2 = Math.max(updatedls2, functionLevel.get(s) + 1);
			}
			
			if (updatedls2 != ls2) { // if change  in level of source 2
				functionLevel.put(fs2, updatedls2);
				updateFunctionLevelWithCutOff(Math.min(updatedls2, ls2) + 1);
			}
			
			
//			level median updates computed
			if (kount % 5000 == 0) {
//				print level median
				double a[] = new double[functionLevel.values().size()];
				int j = 0;
				for (int i : functionLevel.values()) a[j++] = i;
				pw1.println("Random Median of Levels: " + StatUtils.percentile(a, 50.0));
				pw2.println(nAttempts + "\t" + nEventA + "\t" + nEventB);
				nAttempts = nEventA = nEventB = 0;
			}
		}
		
		pw1.close();
		pw2.close();
		
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
	
	public void rewireRandomEdge() throws Exception {
		int nFunctions = randomCallDAG.functions.size();
		Object[] functionNames = randomCallDAG.functions.toArray();
		int nRewiring = 0;
		
		System.out.println("Making " + (randomCallDAG.nEdges * numOfIteration) + " edge rewirings");
		
		while(nRewiring < randomCallDAG.nEdges * numOfIteration) {
			int indexCurrentSource; 
			String nameCurrentSource;
			int levelCurrentSource; 
			do {
				indexCurrentSource = random.nextInt(nFunctions);
				nameCurrentSource = (String)functionNames[indexCurrentSource];
			} 
			while (!randomCallDAG.callTo.containsKey(nameCurrentSource));	
			levelCurrentSource = functionLevel.get(nameCurrentSource);
						
			List<String> callToListS1 = new ArrayList(randomCallDAG.callTo.get(nameCurrentSource));
			int indexCurrentTarget; 
			String nameCurrentTarget;
			int levelCurrentTarget; 
			indexCurrentTarget = random.nextInt(callToListS1.size()); 
			nameCurrentTarget = callToListS1.get(indexCurrentTarget); 
			levelCurrentTarget = functionLevel.get(nameCurrentTarget); 
			
			int indexNextTarget = random.nextInt(nFunctions);
			String nameNextTarget = (String)functionNames[indexNextTarget];
			int levelNextTarget = functionLevel.get(nameNextTarget); 
			
//			check if already exists, then no rewiring, start over
			if (randomCallDAG.callTo.get(nameCurrentSource).contains(nameNextTarget)) {
				continue;
			}

			/* SKIP LEVEL BASED OPTIMIZATION
//			cycle check
			if (levelNextTarget > levelCurrentSource) {
//				check if currentSource is reachable from nextTarget
				checkReachablity(nameNextTarget, nameCurrentSource, levelCurrentSource);
				if (isReachable) {
					continue;
				}
			}
			*/
			
			checkReachablity(nameNextTarget, nameCurrentSource, levelCurrentSource);
			if (isReachable) {
				continue;
			}
			
//			swap ...
			++nRewiring;

//			change call graph adjacency list
			randomCallDAG.callTo.get(nameCurrentSource).remove(nameCurrentTarget);
			randomCallDAG.callTo.get(nameCurrentSource).add(nameNextTarget);
						
			randomCallDAG.callFrom.get(nameCurrentTarget).remove(nameCurrentSource);

			if (!randomCallDAG.callFrom.containsKey(nameNextTarget)) {
				randomCallDAG.callFrom.put(nameNextTarget, new HashSet());
			}
			randomCallDAG.callFrom.get(nameNextTarget).add(nameCurrentSource);
		
//			level propagation SKIP FOR NOW
//			int updateLevelCurrentSource = 1; 
//			for (String s: randomCallDAG.callTo.get(nameCurrentSource)) {
//				updateLevelCurrentSource = Math.max(updateLevelCurrentSource, functionLevel.get(s) + 1);
//			}
//			
//			if (updateLevelCurrentSource != levelCurrentSource) { // if change in level of current source
//				functionLevel.put(nameCurrentSource, updateLevelCurrentSource);
//				updateFunctionLevelWithCutOff(Math.min(updateLevelCurrentSource, levelCurrentSource) + 1);
//			}
			
//			periodic snapshot
			if (nRewiring % 5000 == 0) {
				
			}
		}
	}
	
	public void checkDAGValidity() {
		checkCycleExistence();
		if (isReachable) {
			System.out.println("Cycle Found # Error!");
		}
		
//		SKIP FOR NOW
//		CallDAG originalCallDAG = new CallDAG(Driver.networkPath + Driver.currentVersion);
//		
//		for (String f: originalCallDAG.functions) {
//			if(!randomCallDAG.functions.contains(f)) {
//				System.out.println("Function Vanished # Error!");
//			}
//			
//			if(randomCallDAG.callFrom.containsKey(f)) {
//				if(randomCallDAG.callFrom.get(f).size() != originalCallDAG.callFrom.get(f).size()) { 
//					System.out.println("Indegree Destroyed (1) # Error!");
//				}
//			}
//			else {
//				if(originalCallDAG.callFrom.containsKey(f)) {
//					System.out.println("Indegree Destryed (2) # Error!");
//				}
//			}
//			
//			if(randomCallDAG.callTo.containsKey(f)) {
//				if(randomCallDAG.callTo.get(f).size() != originalCallDAG.callTo.get(f).size()) {
//					System.out.println("Outdegree Destryoed (1) # Error!");
//				}
//			}
//			else {
//				if(originalCallDAG.callTo.containsKey(f)) {
//					System.out.println("Outdegree Destryed (2) # Error!");
//				}
//			}
//			
//			if(randomCallDAG.inDegree.get(f) != originalCallDAG.inDegree.get(f)) System.out.println("Error!");
//			if(randomCallDAG.outDegree.get(f) != originalCallDAG.outDegree.get(f)) System.out.println("Error!");
//		}
		
		System.out.println("OK !!!");
	}
	
	public void generateRandomNetwork(String rndmVrsnNum) throws Exception {
		randomVersionNumber = rndmVrsnNum;
		getNodeLevel();
		rewireRandomEdge();
//		chooseEdgePairsAndSwap();
		checkDAGValidity();
	}
}
