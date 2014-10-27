import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class ModularityAnalysis {
	
	Map<String, Set<String>> communities;
	static int nCommunitySizeThreshold = 10;
	
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
	
	public void getWalktrapModules(CallDAG callDAG, String versionNum) throws Exception {	
		Scanner scanner = new Scanner(new File("module_graphs//w10-" + versionNum + ".txt"));
		PrintWriter pw = new PrintWriter(new File("Results//communities-" + versionNum + ".txt"));
		communities = new TreeMap();

		int communityID = 1;
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			String val[] = str.split(",");	
		
			if (val.length < nCommunitySizeThreshold) continue;

			Set<String> communityFunctions = new HashSet();
			String cID = "C" + communityID;
			
			pw.print(cID);
			for(String r: val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				communityFunctions.add(f);
				pw.print("\t" + f);
			}
			pw.println();
			
			communities.put(cID, communityFunctions);
			++communityID;
		}

		System.out.println("nCommunities: " + (communityID - 1));
		scanner.close();
		pw.close();
	}
	
	public double getCommunityDistanceMetric(Set<String> communityFrom, Set<String> communityTo, CallDAG callDAG) {
		double dist = 0;
		double nOutgoingEdge = 0;
		double nComToOutgoingEdge = 0;
		double nInCommunityEdge = 0;
		
		for (String s: communityFrom) {
			if (!callDAG.callTo.containsKey(s)) continue; // why this is not handled by Java?
			
			for (String r: callDAG.callTo.get(s)) {
				if (communityFrom.contains(r)) ++nInCommunityEdge;
				else ++nOutgoingEdge;
				
				if (communityTo.contains(r)) ++nComToOutgoingEdge;
			}
		}
		
		dist = nComToOutgoingEdge / nInCommunityEdge;
		return dist;
	}
	
	public void getCommunityNetworkHeatMap(CallDAG callDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//communities_heat_map.txt"));
		
		for (String s: communities.keySet()) {
			pw.print("\t" + s);
		}
		pw.println();
		
		for (String s: communities.keySet()) {
			pw.print(s);
			double d = 0;
			int index = 0;
			for (String r: communities.keySet()) {
				if (!s.equals(r)) {
					d = getCommunityDistanceMetric(communities.get(s), communities.get(r), callDAG);
				}
				else d = 0;

				pw.print("\t" + String.format( "%.4f", d ));
				
				++index;
//				pw.print(String.format( "%.4f", d ));
//				if (index < communities.keySet().size()) pw.print("\t");
			}
			
			pw.println();
		}
		
		pw.close();
	}
	
	public void getCommunityNetworkLayoutDOTStyle(CallDAG callDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("community-callDAG.dot"));
		pw.println("digraph G {");
		
		Map<String, Double> communityGenerality = new TreeMap();
		
		for (String s: communities.keySet()) {
			double d = 0;
			for (String r: communities.keySet()) {
				if (!s.equals(r)) {
					d = getCommunityDistanceMetric(communities.get(s), communities.get(r), callDAG);
					
					if (communityGenerality.containsKey(r)) {
						double v = communityGenerality.get(r) + d;
						communityGenerality.put(r, v);
					}
					else {
						communityGenerality.put(r, d);
					}
				}
				else d = 0;

				if (d > 0.0009) {
					pw.println(s + " -> " + r + "[label=" + String.format( "%.4f", d ) + "];");
				}
			}
		}
		
		for (String s: communityGenerality.keySet()) {
			System.out.println(s + "\t in-weight " + communityGenerality.get(s));
		}
		
		pw.println("}");
		pw.close();
	}
		
	public void getCommunityLocationHistogram(CallDAG callDAG) throws Exception {		
		Scanner scanner = new Scanner(new File("test5.txt"));
		PrintWriter pw = new PrintWriter(new File("Results//com_loc_histo.txt"));

		int commID = 0;
		
		List<String> communityList = new ArrayList();
		
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			communityList.add(str);
		}
		
		Collections.sort(communityList, new Comparator<String>() {
			public int compare(String left, String right) {
				return right.length() - left.length();
			}
		});
		
		double yStep = 1;
		
		for (String str: communityList) {
			String val[] = str.split(",");	
			double locations[] = new double[val.length];		
			if (locations.length < nCommunitySizeThreshold) continue;

			Map<Integer, Double> locationCounter = new TreeMap();
			int i = 0;
			double maxHeight = 0;
			for(String r: val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				int l = (int)(callDAG.location.get(f) * 100) / 10;
				
				locations[i++] = callDAG.location.get(f);
				
				double v = 0.0;
				if (locationCounter.containsKey(l)) {
					v = locationCounter.get(l) + 10;
					locationCounter.put(l, v);
				}
				else {
					locationCounter.put(l, v);
				}
				
				if (v > maxHeight) maxHeight = v;
				
				pw.println((l / 10000.0) + "\t" + (yStep + v));
			}
					
			yStep += maxHeight + 500;
			commID++;
			
			pw.println("\n");
			
//			System.out.println(
//			  "Community" + commID		
//			+ "  Size: " + locations.length 	
//			+ "  Max: " + StatUtils.max(locations)
//			+ "  Min: " + StatUtils.min(locations)
//			+ "  Mean: " + StatUtils.mean(locations)
//			+ "  Median: " + StatUtils.percentile(locations, 50)
//			+ "  StdDev: " + Math.sqrt(StatUtils.variance(locations)));
		}
		
		System.out.println("nCommunities: " + commID);
		pw.close();
	}
	
	public static void getCallDAGforWalktrap(CallDAG callDAG, String versionNum) throws Exception {
		PrintWriter pw = new PrintWriter(new File("module_graphs//module-callDAG-" + versionNum + ".txt"));
		for (String s: callDAG.functions) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r: callDAG.callTo.get(s)) {
					pw.println(callDAG.functionID.get(s) + " " + callDAG.functionID.get(r));
				}
			}
		}
		pw.close();
	}
}
