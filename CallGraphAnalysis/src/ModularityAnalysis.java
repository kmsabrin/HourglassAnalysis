import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

public class ModularityAnalysis {
	
	public void getModuleGeneralityVsComplexity(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//module-gen-vs-cmp-" + filePath + ".txt"));
		for (String s: callDAG.functions) {
			pw.println(callDAG.moduleGenerality.get(s) + "\t" + callDAG.moduleComplexity.get(s));
		}
		pw.close();
	}
	
	public void getAvgModuleGeneralityVsLocation(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//avg-module-gen-vs-loc-" + filePath + ".txt"));		
		Map<Double, Double> avgModuleGeneralityVsLocation = new TreeMap();
		for (String s: callDAG.functions) {
			double loc = callDAG.location.get(s);
			double mG = callDAG.moduleGenerality.get(s);
			
			if ( avgModuleGeneralityVsLocation.containsKey(loc) ) {
				double oldMG = avgModuleGeneralityVsLocation.get(loc);
				avgModuleGeneralityVsLocation.put(loc, (mG + oldMG) / 2.0);
			}
			else {
				avgModuleGeneralityVsLocation.put(loc, mG);
			}
		}
		
		for (double l: avgModuleGeneralityVsLocation.keySet()) {
			pw.println(l + "\t" + avgModuleGeneralityVsLocation.get(l));
		}
		
		pw.close();
	}
	
//	public void getInfo() throws Exception {
//		PrintWriter pw = new PrintWriter(new File("Results//module-core-percentage.txt"));
//
//		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
//			String versionNum = Driver.networkUsed + i;
//			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
//			
//			double a[] = new double[callDAG.moduleGenerality.size()];
//			int index  = 0;
//			for (String s: callDAG.moduleGenerality.keySet()) {
//				a[index++] = callDAG.moduleGenerality.get(s);
//			}
//	
//			double coreCutOff = StatUtils.percentile(a, 75.0);
//			
//			double coreCount = 0;
//			PrintWriter pw2 = new PrintWriter(new File("Results//module-core-location-hist-" + versionNum + ".txt"));
//			for (String s: callDAG.functions) {
//				if (callDAG.moduleGenerality.get(s) > coreCutOff) {
//					// core node
//					++coreCount;
//					pw2.println(callDAG.location.get(s));
//				}
//			}
//			
//			pw2.close();
//			
//			pw.println(coreCount / (callDAG.functions.size() * 1.0));
//		}
//		
//		pw.close();
//	}
	
	public void getRandomModularNetwork() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//random-modular-network_100x100.txt"));

		HashMap<Integer, HashSet<Integer>> adjacencyList = new HashMap();
		
		int blockSize = 100;
		int nBlocks = 100;
		int nNodes = blockSize * nBlocks;
				
		for (int i = 0; i < nNodes; ++i) {
			HashSet<Integer> hset = new HashSet();
			adjacencyList.put(i, hset);
		}
		
		// create intra-module edges
		int kount;
		for (int i = 0; i < nBlocks; ++i) {
			Random rand = new Random(System.nanoTime());
			kount = (int)(blockSize * 2);
			while (kount-- > 0) {
				int source = rand.nextInt(blockSize);
				int target = rand.nextInt(blockSize);
				if (source == target) continue;
				if (source > target) {
					int temp = source;
					source  = target;
					target = temp;
				}
				source += i * blockSize;
				target += i * blockSize;
				adjacencyList.get(source).add(target);
			}
		}
		
//		int[] layerWidths = new int[]{4, 2, 1, 3, 5, 5};
//		int[] layerWidths = new int[]{5, 5, 3, 2, 1, 4};
//		int[] layerWidths = new int[]{50, 30, 20, 20, 30, 50};
		int[] layerWidths = new int[]{25, 15, 10, 10, 15, 25};

		int[] layerPrecedence = new int[nNodes];

		int k = 0;
		int precedence = 1;
		for (int i: layerWidths) {
			for (int j = 0; j < i * blockSize; ++j) {
				layerPrecedence[k++] = precedence;
			}
			++precedence;
		}
		
		// create inter-module edges
		kount = nNodes / 5;
		Random rand = new Random(System.nanoTime());
		while (kount > 0) {
			int source = rand.nextInt(nNodes);
			int target = rand.nextInt(nNodes);			
			if (source / blockSize == target / blockSize) continue;
			if (layerPrecedence[source] > layerPrecedence[target]) continue;
			adjacencyList.get(source).add(target);
			kount--;
		}
		
		boolean[] notZeroDegree = new boolean[nNodes];
		
		for (int i = 0; i < nNodes; ++i) {
			for (int j: adjacencyList.get(i)) {
				pw.println(i + "  " + j);
				notZeroDegree[i] = true;
				notZeroDegree[j] = true;
			}
		}
		
		for (int i = 0; i < nNodes; ++i) {
			if (notZeroDegree[i] == false) {
				pw.println("0" + "  " + i);
			}
		}
		
		pw.close();
		
//		CallDAG callDAG = new CallDAG("Results//random-modular-network.txt");
	}
	
	public void getWalktrapModules(CallDAG callDAG) throws Exception {		
		Scanner scanner = new Scanner(new File("test5.txt"));
		PrintWriter pw = new PrintWriter(new File("Results//communities.txt"));

		int nodesUsed = 0;
		int commID = 0;
		
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			String val[] = str.split(",");
			double locations[] = new double[val.length];
			int i = 0;
			for(String r: val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				pw.print(id + "-" + f + " " + "-" + callDAG.location.get(f) + "  ");
//				pw.print(id + " ");
				locations[i++] = callDAG.location.get(f);
				if (callDAG.moduleGenerality.get(f) > 0.4) {
					System.out.println(f + "\t" + commID + "\t" + locations.length);
				}
			}
			pw.println();
			
			nodesUsed += locations.length;
			commID++;
			
//			System.out.println(locations.length + "\t" + StatUtils.mean(locations) + "\t" + StatUtils.percentile(locations, 50));
			
			pw.println(
			  "  Size: " + locations.length 	
			+ "  Max: " + StatUtils.max(locations)
			+ "  Min: " + StatUtils.min(locations)
			+ "  Mean: " + StatUtils.mean(locations)
			+ "  Median: " + StatUtils.percentile(locations, 50)
			+ "  StdDev: " + Math.sqrt(StatUtils.variance(locations)));
		}
		
		pw.println("Total nodes used: " + nodesUsed + " out of " + callDAG.functions.size());
		
	}
}