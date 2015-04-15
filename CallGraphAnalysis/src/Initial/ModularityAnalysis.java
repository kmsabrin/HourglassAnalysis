package Initial;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.StatUtils;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class ModularityAnalysis {
	Map<String, Set<String>> communities;
	Map<String, Double> communitiesInDeg;
	Map<String, Double> communitiesOutDeg;
	Map<String, Double> communitiesAvgLocation;
	Map<String, Double> communitiesAvgGenerality;
	Map<String, Double> communitiesInWeight;
	Map<String, Double> communitiesOutWeight;
	
	int nCommunityNetoworkEdge;
	static int nCommunitySizeThreshold = 5;
	static int walkLength = 5;
	
	ModularityAnalysis() {
		communities = new HashMap<String, Set<String>>();
		communitiesInDeg = new HashMap<String, Double>();
		communitiesOutDeg = new HashMap<String, Double>();
		communitiesAvgLocation = new HashMap<String, Double>();
		communitiesInWeight = new HashMap<String, Double>();
		communitiesOutWeight = new HashMap<String, Double>();
		communitiesAvgGenerality = new HashMap<String, Double>();
	}
	
	public static void getCallDAGforWalktrap(CallDAG callDAG, String versionNum) throws Exception {
		PrintWriter pw = new PrintWriter(new File("module_graphs//module-callDAG-" + versionNum + ".txt")); //??
		for (String s: callDAG.functions) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r: callDAG.callTo.get(s)) {
					int src = callDAG.functionID.get(s);
					int dst = callDAG.functionID.get(r);
					pw.println(src + " " + dst);
				}
			}
		}
		pw.close();
	}
	
	public void getModuleGeneralityVsComplexity(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//module-gen-vs-cmp-" + filePath + ".txt"));
		for (String s: callDAG.functions) {
			pw.println(callDAG.moduleGenerality.get(s) + "\t" + callDAG.moduleComplexity.get(s));
		}
		pw.close();
	}
	
	public void getAvgModuleGeneralityVsLocation(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//avg-module-gen-vs-loc-" + filePath + ".txt"));		
		Map<Double, Double> avgModuleGeneralityVsLocation = new TreeMap<Double, Double>();
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
	
	public void doCentralityAnalysisWithModuleGenerality() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//module-core-percentage.txt"));

		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			String versionNum = Driver.networkType + i;
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			
			double a[] = new double[callDAG.moduleGenerality.size()];
			int index  = 0;
			for (String s: callDAG.moduleGenerality.keySet()) {
				a[index++] = callDAG.moduleGenerality.get(s);
			}
	
			double coreCutOff = StatUtils.percentile(a, 75.0);
			
			double coreCount = 0;
			PrintWriter pw2 = new PrintWriter(new File("Results//module-core-location-hist-" + versionNum + ".txt"));
			for (String s: callDAG.functions) {
				if (callDAG.moduleGenerality.get(s) > coreCutOff) {
					// core node
					++coreCount;
					pw2.println(callDAG.location.get(s));
				}
			}
			
			pw2.close();
			
			pw.println(coreCount / (callDAG.functions.size() * 1.0));
		}
		
		pw.close();
	}
	
	/***************************************************************************************************/
	/* GENERATE RANDOM MODULAR NETWORK TO TEST WALK-TRAP COMMUNITY DETECTION ALGORITHM'S EFFECTIVENESS */
	/***************************************************************************************************/
	
	public void getArtificialModularNetwork() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//random-modular-network_100x100-50n.txt"));

		HashMap<Integer, HashSet<Integer>> adjacencyList = new HashMap<Integer, HashSet<Integer>>();
		
		int blockSize = 100;
		int nBlocks = 100;
		int nNodes = blockSize * nBlocks;
		double nInterModuleEdges = 0;
				
		for (int i = 0; i < nNodes; ++i) {
			HashSet<Integer> hset = new HashSet<Integer>();
			adjacencyList.put(i, hset);
		}
		
		// create intra-module edges
		int kount;
		for (int i = 0; i < nBlocks; ++i) {
			Random rand = new Random(System.nanoTime());
			kount = (int)(blockSize * 2.7);
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
				++nInterModuleEdges;
			}
		}
				
		int[] layerWidths = new int[]{25, 15, 10, 10, 15, 25}; // AN1 100x100
//		int[] layerWidths = new int[]{31, 18, 14, 13, 18, 31}; // AN2 80x125
//		int[] layerWidths = new int[]{50, 30, 20, 20, 30, 50}; // AN3 50x200
		
		int[] layerPrecedence = new int[nNodes];

		int k = 0;
		int precedence = 1;
		for (int i: layerWidths) {
			for (int j = 0; j < i * blockSize; ++j) {
				layerPrecedence[k++] = precedence;
			}
			++precedence;
		}
		
//		create inter-module edges
//		kount = nNodes / 5;
		kount = (int)(nInterModuleEdges * 0.50); // percentage of inter-module edges as intra-module edges
		
		Random rand = new Random(System.nanoTime());
		while (kount > 0) {
			int source = rand.nextInt(nNodes);
			int target = rand.nextInt(nNodes);			
			if (source / blockSize == target / blockSize) continue; // same block node
			if (layerPrecedence[source] > layerPrecedence[target]) continue; // layer precedence violation
			adjacencyList.get(source).add(target);
			kount--;
		}
		
		// identify isolated nodes and connect them
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
	
	public void getArtificialModularNetworkCommunityDetectionPerformance() throws Exception {
		Scanner scanner = new Scanner(new File("module_graphs//random_100x100-50n_w5_result.txt"));
		
		double averageJS = 0; // JS - Jaccard Similarity
		int averageKnt = 0;
		
		ArrayList<Double> jSValueList = new ArrayList<Double>();

		int blockSize = 100;
		int nBlocks = 100;
		
		scanner.nextLine(); // skip first line
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.replaceAll(" ", "");
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			String val[] = str.split(",");	
		
			if (val.length < (blockSize / 5)) continue; // wrongly detected community, skip it
			
			HashSet<Integer> communityMembers = new HashSet<Integer>();
			for(String r: val) {
				int id = Integer.parseInt(r);
				communityMembers.add(id);
			}
			
			double maxJS = 0;
			for (int i = 0; i < blockSize * nBlocks; i += blockSize) {
				double knt = 0;
				for (int j = i; j < i + blockSize; ++j) {
					if (communityMembers.contains(j)) {
						++knt;
					}
				}
				double jS = knt / ((blockSize - knt) + knt + (communityMembers.size() - knt));
				
				if (jS > maxJS) maxJS = jS;
			}
			
			jSValueList.add(maxJS);
			averageJS += maxJS;
			averageKnt++;
		}
		

		double jSValueArray[] = new double[jSValueList.size()];
		int idx = 0;
		for (double d: jSValueList) {
			jSValueArray[idx++] = d;
		}
		
		System.out.println("Average Jaccard Similarity: " + StatUtils.mean(jSValueArray));
		System.out.println("Jaccard Similarity STD-DEV: " + Math.sqrt(StatUtils.variance(jSValueArray)));
		System.out.println("Detected Communities: " + averageKnt);
	}
	
	/*********************************************************************************************************************/
	/*********************************************************************************************************************/
	/************************************* WALK TRAP COMMUNITY DETECTION ALGORITHM ***************************************/
	/****************** ANALYSIS OF COMMUNITY NETWORK GENERATED WITH WALK TRAP ALGORITHM *********************************/
	/*********************************************************************************************************************/
	/*********************************************************************************************************************/
	
	public void getWalktrapModules(CallDAG callDAG, String versionNum) throws Exception {	
		Scanner scanner = new Scanner(new File("module_graphs//w" + walkLength + "-" + versionNum + ".txt"));
		PrintWriter pw = new PrintWriter(new File("Results//communities-" + versionNum + ".txt"));
		
		int communityID = 1;
		scanner.nextLine(); // skip first line
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.replaceAll(" ", "");
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			String val[] = str.split(",");	
		
//			if (val.length < nCommunitySizeThreshold) continue;

			Set<String> communityFunctions = new HashSet<String>();
			String cID = "C" + communityID;
			
			pw.print(cID);
			
			double avgModGen = 0;
			double avgLoc = 0;
			double avgGen = 0;
			for(String r: val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				communityFunctions.add(f);
				pw.print("\t" + f);
				
				double loc = callDAG.location.get(f);
				
				double modGen = callDAG.generality.get(f); 
//				if (loc > 0.3 && loc < 0.5 && modGen > 0.1) {
//					System.out.print(f + "\t");
//				}
				
				avgModGen += modGen;
				avgLoc += loc;
				avgGen += callDAG.generality.get(f);
			}
//			System.out.print((avgModGen / communityFunctions.size()) + "\t" + (avgLoc / communityFunctions.size()));
//			System.out.println("\t" + communityFunctions.size() + "\t" + cID);
			pw.println();
			
			communities.put(cID, communityFunctions);
			
			avgLoc /= communityFunctions.size();
			avgLoc = ((int) (avgLoc * 100.0)) / 100.0; // 2 decimal rounding
			communitiesAvgLocation.put(cID, avgLoc);
			
			avgGen /= communityFunctions.size();
			avgGen = ((int) (avgGen * 100.0)) / 100.0; // 2 decimal rounding
			communitiesAvgGenerality.put(cID, avgGen);
			
			++communityID;
		}
		
		nCommunityNetoworkEdge = 0;
		for (String s: communities.keySet()) {
			Set<String> currentComm = communities.get(s);
			double inDeg = 0, outDeg = 0;
			for (String r: currentComm) {
				if (callDAG.callTo.containsKey(r)) {
					for (String t: callDAG.callTo.get(r)) {
						if (!currentComm.contains(t)) ++outDeg;
					}
				}
				if (callDAG.callFrom.containsKey(r)) {
					for (String t: callDAG.callFrom.get(r)) {
						if (!currentComm.contains(t)) ++inDeg;
					}
				}
			}
			communitiesInDeg.put(s, inDeg);
			communitiesOutDeg.put(s, outDeg);
			nCommunityNetoworkEdge += outDeg;
		}

		System.out.println("nCommunities: " + (communityID - 1));
		
//		getLeafStatistics(callDAG);
//		getCommunityHistogram(callDAG);
//		checkCommunityHourglassShape(callDAG, versionNum);
//		getCommunityNetworkStats(callDAG, versionNum);
		getNonrepresentativeCommunityNode(callDAG, versionNum);
//		getNonrepresentativeCommunities(callDAG);
		
		scanner.close();
		pw.close();
	}
	
	public void getNonrepresentativeCommunityNode(CallDAG callDAG, String versionNum) throws Exception {
		double nodeOutRatio[] = new double[callDAG.location.size() + 1];
		double nodeInRatio[] = new double[callDAG.location.size() + 1];
		Map<String, Double> nodeInRatioMap = new HashMap<String, Double>();
		Map<String, Double> nodeOutRatioMap = new HashMap<String, Double>();
		
		int idx = 0;
		for (String s: communities.keySet()) {
			for (String r: communities.get(s)) {
				double intraIn = 1, interIn = 0, intraOut = 1, interOut = 0;
				
				if (callDAG.callFrom.containsKey(r)) {
					for (String p: callDAG.callFrom.get(r)) {
						if (communities.get(s).contains(p)) {
							++intraIn;
						}
						else {
							++interIn;
						}
					}
				}
				
				if (callDAG.callTo.containsKey(r)) {
					for (String p: callDAG.callTo.get(r)) {
						if (communities.get(s).contains(p)) {
							++intraOut;
						}
						else {
							++interOut;
						}
					}
				}
				
				double inRatio = interIn / intraIn;
				double outRatio = interOut / intraOut;
				nodeInRatio[idx] = inRatio;
				nodeOutRatio[idx] = outRatio;
				++idx;
				
				nodeInRatioMap.put(r, inRatio);
				nodeOutRatioMap.put(r, outRatio);
//				System.out.println(inRatio); // + "\t" + outRatio);
			}
		}
		
//		cut-off creation by distribution sampling
		double sampleInRatio[] = new double[nodeInRatio.length / 10];
		double sampleOutRatio[] = new double[nodeOutRatio.length / 10];
		idx = 0;
		for (int i = 0; i < nodeInRatio.length / 10; ++i) {
			Random random = new Random(System.nanoTime());
			int j = random.nextInt(nodeInRatio.length);
			int k = random.nextInt(nodeOutRatio.length);
			sampleInRatio[idx] = nodeInRatio[j];
			sampleOutRatio[idx] = nodeOutRatio[k];
			++idx;
			System.out.println(nodeInRatio[j]);// + "\t" + nodeOutRatio[k]);
		}
		
		for (int i = 75; i <= 95; ++i) {
//			System.out.print("iRatio" + i + "p = " + StatUtils.percentile(sampleInRatio, i)  + "\t");
//			System.out.print("oRatio" + i + "p = " + StatUtils.percentile(sampleOutRatio, i) );
//			System.out.println();
		}
		
		double iRatio90p = StatUtils.percentile(sampleInRatio, 91);
		double oRatio90p = StatUtils.percentile(sampleOutRatio, 83);		
		
//		cut-off creation by distribution 
//		double iRatio90p = StatUtils.percentile(nodeInRatio, 90);
//		double oRatio90p = StatUtils.percentile(nodeOutRatio, 90);
		
		double cupInOutlier = 0;
		double cupOutOutlier = 0;
		double cupTotal = 0;
		double neckInOutlier = 0;
		double neckOutOutlier = 0;
		double neckTotal = 0;
		double baseInOutlier = 0;
		double baseOutOutlier = 0;
		double baseTotal = 0;
		
		for (String s : callDAG.location.keySet()) {
			if (!nodeInRatioMap.containsKey(s))
				continue;

			double iRatio = nodeInRatioMap.get(s);
			double oRatio = nodeOutRatioMap.get(s);
			double loc = callDAG.location.get(s);

			if (loc < 0.2) {
				baseTotal++;
				if (iRatio > iRatio90p) {
					baseInOutlier++;
				}
				if (oRatio > oRatio90p) {
					baseOutOutlier++;
				}
			} else if (loc > 0.8) {
				cupTotal++;
				if (iRatio > iRatio90p) {
					cupInOutlier++;
				}
				if (oRatio > oRatio90p) {
					cupOutOutlier++;
				}
			} else {
				neckTotal++;
				if (iRatio > iRatio90p) {
					neckInOutlier++;
				}
				if (oRatio > oRatio90p) {
					neckOutOutlier++;
				}
			}
		}
		
//		System.out.println("Base Inratio Outlier Percentage" + "\t" + baseInOutlier * 100.0 / baseTotal);
//		System.out.println("Neck Inratio Outlier Percentage" + "\t" + neckInOutlier * 100.0 / neckTotal);
//		System.out.println("Cup Inratio Outlier Percentage" + "\t" + cupInOutlier * 100.0 / cupTotal);
//		
//		System.out.println("Base Outratio Outlier Percentage" + "\t" + baseOutOutlier * 100.0 / baseTotal);
//		System.out.println("Neck Outratio Outlier Percentage" + "\t" + neckOutOutlier * 100.0 / neckTotal);
//		System.out.println("Cup Outratio Outlier Percentage" + "\t" + cupOutOutlier * 100.0 / cupTotal);
		
//		pw1.close();
//		pw2.close();
	}
	
	public void getNonrepresentativeCommunities(CallDAG callDAG) {
		Multimap<Integer, String> sizeSortedCommunities = TreeMultimap.create();
		
		for (String s: communities.keySet()) {
			sizeSortedCommunities.put(-1 * communities.get(s).size(), s);
		}
		
		double largeMoudlesLocationDistribution[] = new double[100 + 1];
		
		int knt = 10;
		double largeSampleKnt = 0;
		for (int i: sizeSortedCommunities.keySet()) {
			Collection<String> communityIds = sizeSortedCommunities.get(i);
			
			for (String s: communityIds) {
				for (String r: communities.get(s)) {
					int loc = (int)(callDAG.location.get(r) * 100);
					largeMoudlesLocationDistribution[loc]++;
				}
				largeSampleKnt += communities.get(s).size();
			}
			
			knt -= communityIds.size();
			if (knt < 1) break;
		}
		
		for (int i = 0; i < 101; ++i) {
			largeMoudlesLocationDistribution[i] /= largeSampleKnt;
		}
		
		for (String s: communities.keySet()) {
			int individualSampleKnt = communities.get(s).size();
			if (individualSampleKnt < 10) continue;
			
			int individualModuleLocationDistribution[] = new int[100 + 1];
			for (String r: communities.get(s)) {
				int loc = (int)(callDAG.location.get(r) * 100);
				individualModuleLocationDistribution[loc]++;
			}
			
			double avgFit = 0; // resemblance metric
			for (int i = 0; i < 101; ++i) {
				BinomialDistribution binomialDistribution = new BinomialDistribution(individualSampleKnt, largeMoudlesLocationDistribution[i]);
				avgFit += binomialDistribution.probability(individualModuleLocationDistribution[i]);
			}
			avgFit /= 101;
			
			System.out.println(avgFit);
		}
	}
	
	public void checkCommunityHourglassShape(CallDAG callDAG, String versionNum) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//hmetric-" + versionNum + ".txt"));
		
		double hMetricFrequencies[] = new double[150];
//		int hMetricCDF[] = new int[150];
		
		for (String s: communities.keySet()) {
			TreeMap<Double, Integer> communityShape = new TreeMap<Double, Integer>();
			for (String r: communities.get(s)) {
				double loc = callDAG.location.get(r);
				if (communityShape.containsKey(loc)) {
					int v = communityShape.get(loc);
					communityShape.put(loc, v + 1);
				}
				else {
					communityShape.put(loc, 1);
				}
			}
			
			int minVal = 111111;
			int minValIdx = 111111;
			int values[] = new int[communityShape.size()];
			int idx = 0;
			for (double d: communityShape.keySet()) {
				int v = communityShape.get(d);
				values[idx] = v;
				if (v <= minVal) {
					minVal = v;
					minValIdx = idx;
				}
				++idx;
			}
						
			int a = 0, b = 0, k = 0;
			for (int i = 0; i <= minValIdx; ++i) {
				++k;
				for (int j = i + 1; j <= minValIdx; ++j) {
					if (values[j] <= values[i]) ++a;
					else ++b;
				}
			}
			double n = k * (k - 1.0) * 0.5;
			if (n < 1) n = 1;
			double x = (a - b) / n;
			
			a = b = k = 0;
			for (int i = minValIdx; i < values.length; ++i) {
				++k;
				for (int j = i + 1; j < values.length; ++j) {
					if (values[j] > values[i]) ++a;
					else ++b;
				}
			}
			n = k * (k - 1.0) * 0.5;
			if (n < 1) n = 1;
			double y = (a - b) / n;

			double h = (x + y) / 2.0;
			
			int hInt = (int)(h * 100);
			hMetricFrequencies[hInt]++;
//			System.out.println(h + "\t" + communities.get(s).size());
		}
		
		pw.println(0 + "\t" + hMetricFrequencies[0] / communities.keySet().size());
		for (int i = 1; i <= 100; ++i) {
			hMetricFrequencies[i] += hMetricFrequencies[i - 1];
			pw.println(i / 100.0 + "\t" + hMetricFrequencies[i] / communities.keySet().size());
		}
		
		pw.close();
	}
	
	public void getCommunityHistogram(CallDAG callDAG) throws Exception {
		PrintWriter pw0 = new PrintWriter(new File("Results//gnuplot.command.txt"));
		TreeMap<Integer, ArrayList<String>> sizeSortedCommunities = new TreeMap<Integer, ArrayList<String>>();
		
		for (String s: communities.keySet()) {
			int sz = communities.get(s).size() * -1;
			if (sizeSortedCommunities.containsKey(sz)) {
				sizeSortedCommunities.get(sz).add(s);
			}
			else {
				ArrayList<String> aList = new ArrayList<String>();
				aList.add(s);
				sizeSortedCommunities.put(sz, aList);
			}
		}
		
		int idx = 0;
		for (int i: sizeSortedCommunities.keySet()) {
			for (String s: sizeSortedCommunities.get(i)) {
				PrintWriter pw = new PrintWriter(new File("Results//histo-community-" + idx + ".txt"));
				for (String r: communities.get(s)) {
					pw.println(callDAG.location.get(r));
				}
				pw.close();
//				pw0.println("\"Module/histo-community-" + idx + ".txt\" u 1:(1./(" + communities.get(s).size() + "*10.)):(0.03) title \"c" + idx + "\" smooth kdensity w l, \\");
				pw0.println("\"Module/histo-community-" + idx + ".txt\" u 1:(1./(" + communities.get(s).size() + "*10.)):(0.03) notitle smooth kdensity w l, \\");
				++idx;
				if (idx > 99) break;
			}
			if (idx > 99) break;
		}
		
		pw0.close();
	}
	
	public void getLeafStatistics(CallDAG callDAG) {
		System.out.println("Total Leaves: " + callDAG.nTargets);
		int engulfed = 0;
		int knt = 0;
		for (String s: communities.keySet()) {
			double communitySz = communities.get(s).size();
			double leafCount = 0;
			double GCCount = 0;
			for (String r: communities.get(s)) {
				if (callDAG.location.get(r) < 0.1) {
					leafCount++;
				}
				if (callDAG.generality.get(r) > 0.4 /*&& callDAG.complexity.get(r) > 0.00*/) {
					GCCount++;
				}
			}
			
//			if (GCCount < 1) continue;
			if (GCCount < communitySz * 0.2) {
				engulfed += GCCount;
				continue;
			}
			//System.out.println("Total GC nodes: " + GCCount);
			
			double leafRatio = leafCount / communitySz;			
			if (leafRatio > 0.33) {
//				System.out.println(leafRatio + "\t" + communitySz);
				++knt;
			}
//			System.out.println(leafCount + "\t" + GCCount + "\t" + communitySz);
		}
		
		System.out.println(knt * 1.0 / communities.keySet().size());
//		System.out.println("Engulfed into bigger community: " + engulfed);
	}
	
	public double getCommunityDistanceMetric(String comXID, String comYID, CallDAG callDAG) {
		Set<String> comX = communities.get(comXID);
		Set<String> comY = communities.get(comYID);
		
		double nComXOutgoingEdge = communitiesOutDeg.get(comXID);
		double nComYIncomingEdge = communitiesInDeg.get(comYID);
		double nComYOutgoingEdge = communitiesOutDeg.get(comYID);
		double nComXToComYEdge = 0;
		
		double dist = 0;

		for (String s: comX) {
			if (!callDAG.callTo.containsKey(s)) continue; // why this is not handled by Java?			
			for (String r: callDAG.callTo.get(s)) {				
				if (comY.contains(r)) ++nComXToComYEdge;
			}
		}
		
		if (nComXOutgoingEdge > 0 && nComYIncomingEdge > 0) {
			double expecedtedXYEdge = nComXOutgoingEdge * nComYIncomingEdge / (nCommunityNetoworkEdge - nComYOutgoingEdge); 
			dist = nComXToComYEdge / expecedtedXYEdge;		
//			dist = nComXToComYEdge / (nComXOutgoingEdge * nComYInComingEdge);
		}
		
//		return dist * callDAG.nEdges;
		return dist;
	}
	
	public void getCommunityNetworkWeightCDF(String versionNum) throws Exception {
		PrintWriter pw1 = new PrintWriter(new File("Results//module-inweight-cdf-" + versionNum + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//module-outweight-cdf-" + versionNum + ".txt"));
		
		Map<Integer, Integer> cdfMap = new TreeMap<Integer, Integer>();
		for (String s: communitiesInWeight.keySet()) {
			int w = communitiesInWeight.get(s).intValue();
			if (cdfMap.containsKey(w)) {
				int v = cdfMap.get(w);
				cdfMap.put(w, v + 1);
			}
			else cdfMap.put(w, 1);
		}
		
		int knt = 0;
		int tot = communitiesInWeight.size();
		for (int i: cdfMap.keySet()) {
			knt += cdfMap.get(i);
			pw1.println(i + "\t" + (knt * 1.0 / tot));
		}
		
		cdfMap.clear();
		for (String s: communitiesOutWeight.keySet()) {
			int w = communitiesOutWeight.get(s).intValue();
			if (cdfMap.containsKey(w)) {
				int v = cdfMap.get(w);
				cdfMap.put(w, v + 1);
			}
			else cdfMap.put(w, 1);
		}
		
		knt = 0;
		tot = communitiesOutWeight.size();
		for (int i: cdfMap.keySet()) {
			knt += cdfMap.get(i);
			pw2.println(i + "\t" + (knt * 1.0 / tot));
		}
		
		pw1.close();
		pw2.close();
	}
	
	public void getCommunityNetworkStats(CallDAG callDAG, String versionNum) throws Exception {
		PrintWriter pw0 = new PrintWriter(new File("Results//module_heatmap-" + versionNum + ".txt"));
		PrintWriter pw1 = new PrintWriter(new File("Results//module-loc-vs-inweight-" + versionNum + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//module-loc-vs-outweight-" + versionNum + ".txt"));
		PrintWriter pw3 = new PrintWriter(new File("Results//module-loc-vs-size-" + versionNum + ".txt"));
		PrintWriter pw4 = new PrintWriter(new File("Results//module-loc-vs-gen-" + versionNum + ".txt"));
		PrintWriter pw5 = new PrintWriter(new File("Results//module-loc-hist-" + versionNum + ".txt"));
		PrintWriter pw6 = new PrintWriter(new File("Results//module-inweight-gen-scatter-" + versionNum + ".txt"));

		
		///////////////////////////////////////////////////////
		for (String s: communities.keySet()) {
			communitiesInWeight.put(s, 0.0);
			communitiesOutWeight.put(s, 0.0);
		}
		for (String s: communities.keySet()) {
			for (String r: communities.keySet()) {
				if (!s.equals(r)) {
					double d = getCommunityDistanceMetric(s, r, callDAG);
					double v = d + communitiesOutWeight.get(s);
					communitiesOutWeight.put(s, (v + d) / 1.0);
					v = d + communitiesInWeight.get(r);
					communitiesInWeight.put(r, (v + d) / 1.0);
				}
//				pw0.print(String.format( "%.4f", d ) + "\t");
			}			
			pw0.println();
		}		
		
		getCommunityNetworkWeightCDF(versionNum);
		
//		///////////////////////////////////////////////////////
		Map<Double, Double> averages = new HashMap<Double, Double>();
//		for (String s: communitiesInWeight.keySet()) {
//			double iw = communitiesInWeight.get(s);
//			double l = communitiesAvgLocation.get(s);
//			
//			if (averages.containsKey(l)) {
//				double v = averages.get(l);
//				averages.put(l, (v + iw) / 2.0);
//			}
//			else averages.put(l, iw);
//		}
//		
//		for (double d: averages.keySet()) {
//			pw1.println(d + "\t" + averages.get(d));
//		}
//		
//		///////////////////////////////////////////////////////
//		averages.clear();
//		for (String s: communitiesOutWeight.keySet()) {
//			double ow = communitiesOutWeight.get(s);
//			double l = communitiesAvgLocation.get(s);
//			
//			if (averages.containsKey(l)) {
//				double v = averages.get(l);
//				averages.put(l, (v + ow) / 2.0);
//			}
//			else averages.put(l, ow);
//		}
//		
//		for (double d: averages.keySet()) {
//			pw2.println(d + "\t" + averages.get(d));
//		}
		
		///////////////////////////////////////////////////////
		averages.clear();
		double communityProperties[] = new double[communities.size()];
		int idx = 0;
		for (String s: communities.keySet()) {
			double sz = communities.get(s).size();
			double l = communitiesAvgLocation.get(s);
			communityProperties[idx++] = sz;
			
			if (averages.containsKey(l)) {
				double v = averages.get(l);
				averages.put(l, (v + sz) / 2.0);
			}
			else averages.put(l, sz);
		}
		
		for (double d: averages.keySet()) {
			pw3.println(d + "\t" + averages.get(d));
		}
		
		System.out.println("Num Communities: " + communities.size());
		System.out.println("Max Size: " + StatUtils.max(communityProperties));
		System.out.println("Min Size: " + StatUtils.min(communityProperties));
		System.out.println("Median Size: " + StatUtils.percentile(communityProperties, 50));
		System.out.println("STD DEV of Size: " + Math.sqrt(StatUtils.variance(communityProperties)));
		
		///////////////////////////////////////////////////////
		averages.clear();
		for (String s: communities.keySet()) {
			double g = communitiesAvgGenerality.get(s);
			double l = communitiesAvgLocation.get(s);
			
			if (averages.containsKey(l)) {
				double v = averages.get(l);
				averages.put(l, (v + g) / 2.0);
			}
			else averages.put(l, g);
		}
		
		for (double d: averages.keySet()) {
			pw4.println(d + "\t" + averages.get(d));
		}
		
		///////////////////////////////////////////////////////
		for (String s: communities.keySet()) {
			double l = communitiesAvgLocation.get(s);
			pw5.println(l);
		}
		
		///////////////////////////////////////////////////////
		for (String s: communities.keySet()) {
			pw6.println(communitiesInWeight.get(s) + "\t" + communitiesAvgGenerality.get(s));
		}
		
		pw0.close();
		pw1.close();
		pw2.close();
		pw3.close();
		pw4.close();
		pw5.close();
		pw6.close();
	}
	
	/**************************** COMMUNITY VISUALIZATION ***************************************************/

	public void getCommunityNetworkLayoutDOTStyle(CallDAG callDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//community-call-network.dot"));
		pw.println("digraph G {");
		
		Map<String, Double> communityGenerality = new TreeMap<String, Double>();
		
		for (String s: communities.keySet()) {
			double d = 0;
			for (String r: communities.keySet()) {
				if (!s.equals(r)) {
					d = getCommunityDistanceMetric(s, r, callDAG);
					
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
					pw.println(s + " -> " + r + " [label=" + String.format( "%.4f", d ) + "];");
				}
			}
		}
		
		for (String s: communityGenerality.keySet()) {
			System.out.println(s + "\t in-weight " + communityGenerality.get(s));
		}
		
		pw.println("}");
		pw.close();
	}
		
	public void getCommunityLocationHistogram(CallDAG callDAG, String versionNum) throws Exception {		
		Scanner scanner = new Scanner(new File("module_graphs//w" + walkLength + "-" + versionNum + ".txt"));
		PrintWriter pw = new PrintWriter(new File("Results//com_loc_histo.txt"));

		int commID = 0;
		
		List<String> communityList = new ArrayList<String>();
		
		scanner.nextLine(); // skip first line
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.replaceAll(" ", "");
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

			Map<Integer, Double> locationCounter = new TreeMap<Integer, Double>();
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
				
				pw.println(l + "\t" + (yStep + v));
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
	
	/********************************************************************************************************/
	/********************************************************************************************************/
	/**************************** COMMUNITY VISUALIZATION FOR JAVA-2D ***************************************/
	/********************************************************************************************************/
	/********************************************************************************************************/

	public void getDataToDrawCommunityNetworkEdges(Map<String, Set<String>> tmpCommunities, Map<String, Integer> communityMidX, 
			Map<String, Integer> communityMidY, CallDAG callDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//community_edges_javadraw.txt"));

		for (String s: tmpCommunities.keySet()) {
			for (String r: tmpCommunities.keySet()) {
				double d = 0;
				if (!s.equals(r)) {
					
					String s1 = "";
					String r1 = "";
					for (String t: communities.keySet()) {
						Set<String> Tset = communities.get(t);
						if (Tset.equals(tmpCommunities.get(s))) s1 = t;
						if (Tset.equals(tmpCommunities.get(r))) r1 = t;
					}
					
					d = getCommunityDistanceMetric(s1, r1, callDAG);
					if (d > 0) {
						pw.print(communityMidX.get(s) + "\t");
						pw.print(communityMidY.get(s) + "\t");
						pw.print(communityMidX.get(r) + "\t");
						pw.print(communityMidY.get(r) + "\t");
						pw.print(d + "\t");
						pw.println();
					}
				}
			}			
		}
		
		pw.close();	
	}
	
	public void getDataToDrawCommunityShape2(CallDAG callDAG, List<String> communityList) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//community_shape2_javadraw.txt"));

		for (int idx = 0; idx < communityList.size(); ++idx) {
			String str = communityList.get(idx);
			String val[] = str.split(",");
			double loc[] = new double[100 + 10];

			for (String r : val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				int l = (int) (callDAG.location.get(f) * 100);
				loc[l]++;
			}

			for (int i = 0; i <= 100; ++i) {
				pw.print(loc[i] + "\t");
			}

			pw.println();
		}

		pw.close();
	}
	
	public void getDataToDrawCommunityShape(CallDAG callDAG, List<String> communityList) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//community_shape_javadraw.txt"));
		
		double xStep = 70; // java canvas
		
		boolean used[] = new boolean[communityList.size()];
		int notUsedKnt = communityList.size();
		
		Map<String, Integer> communityMidX = new HashMap<String, Integer>();
		Map<String, Integer> communityMidY = new HashMap<String, Integer>();
		Map<String, Set<String>> tmpCommunities = new HashMap<String, Set<String>>();
		int comId = 1;		
		while (notUsedKnt > 0) {
			boolean heightBits[] = new boolean[600];
			double maxRadius = 0;
			
			for (int idx = 0; idx < communityList.size(); ++idx) {
				if (used[idx]) continue;
				String str = communityList.get(idx);
				String val[] = str.split(",");
				double loc[] = new double[val.length];
				double gen[] = new double[val.length];

//				if (loc.length < nCommunitySizeThreshold)
//					continue;

				int i = 0;
				Set<String> commFunc = new HashSet<String>();
				for (String r : val) {
					int id = Integer.parseInt(r);
					String f = callDAG.IDFunction.get(id);
					int l = (int) (callDAG.location.get(f) * 100) / 10;
					loc[i] = callDAG.location.get(f);
					gen[i] = callDAG.generality.get(f);
					++i;
					commFunc.add(f);
				}
				
				double radius = Math.log10(loc.length) * 4.5; // HARD CODE // for shape
//				double radius = 2; // HARD CODE // for edges
				double xMid = xStep + radius;

				double yMin = StatUtils.percentile(loc, 75.0);
				double yMid = StatUtils.percentile(loc, 50.0);
				double yMax = StatUtils.percentile(loc, 25.0);
				yMin = 500 - ((yMin / 0.01) * 4.5);
				yMid = 500 - ((yMid / 0.01) * 4.5);
				yMax = 500 - ((yMax / 0.01) * 4.5);

				boolean flg = true;
				for (int k = (int)(yMid - radius); k <= (int)(yMid + radius); ++k) {
					if (heightBits[k]) {
						flg = false;
						break;
					}
				}
				if (!flg) continue;
				
//				System.out.println("Using community " + notUsedKnt + " xStep " + xStep + " radius " + (int)radius + " yMid " + yMid);
				notUsedKnt--;
				used[idx] = true;
				for (int k = (int)(yMid - radius); k <= (int)(yMid + radius); ++k) {
					heightBits[k] = true;			
				}
				
				if (radius > maxRadius) maxRadius = radius;
				
				double avgGen = StatUtils.mean(gen);
//				if (avgGen < 0.02 && radius < 1.5)
//					continue; // HARD CODE

//				if (loc.length < nCommunitySizeThreshold)
//					continue;
				
				String cId = "c" + comId; 
				tmpCommunities.put(cId, commFunc);
				++comId;
				communityMidX.put(cId, (int)xMid);
				communityMidY.put(cId, (int)yMid);
				
				pw.print(xMid + "\t");
				pw.print(radius + "\t");

				pw.print(yMin + "\t");
				pw.print(yMid + "\t");
				pw.print(yMax + "\t");

				pw.print(avgGen + "\t");

				pw.print(loc.length);

				pw.println();
			}
			
//			System.out.println("One stripe complete");
			xStep += 2 * maxRadius + 3;
//			xStep += 2 * maxRadius + 20;
		}
		
		pw.close();
		
		getDataToDrawCommunityNetworkEdges(tmpCommunities, communityMidX, communityMidY, callDAG);
	}
	
	public void getDataToDrawCommunitySpread(CallDAG callDAG, List<String> communityList) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//community_spread_javadraw.txt"));
		
		double xStep = 70; // java canvas
		
		for (int idx = 0; idx < communityList.size(); ++idx) {
			String str = communityList.get(idx);
			String val[] = str.split(",");	
			double loc[] = new double[val.length];	
			double gen[] = new double[val.length];
			
//			if (loc.length < nCommunitySizeThreshold) continue;			

			int i = 0;
			for(String r: val) {
				int id = Integer.parseInt(r);
				String f = callDAG.IDFunction.get(id);
				int l = (int)(callDAG.location.get(f) * 100) / 10;
				loc[i] = callDAG.location.get(f);
				gen[i] = callDAG.generality.get(f);
				++i;
			}
			
			double radius = Math.log10(loc.length) * 2.10; //HARD CODE - w10
//			double radius = Math.log10(loc.length) * 1.92; //HARD CODE - w05
			double xMid = xStep + radius;

			double yMin = StatUtils.percentile(loc, 75.0);
			double yMid = StatUtils.percentile(loc, 50.0);
			double yMax = StatUtils.percentile(loc, 25.0);
			yMin = 500 - ((yMin / 0.01) * 4.5);
			yMid = 500 - ((yMid / 0.01) * 4.5);
			yMax = 500 - ((yMax / 0.01) * 4.5);
			
			double avgGen = StatUtils.mean(gen);
			
			if (avgGen < 0.01 && radius < 1.1) continue; // HARD CODE
			
			xStep = (xMid + radius) + 1;

			pw.print(xMid + "\t");
			pw.print(radius + "\t");

			pw.print(yMin + "\t");
			pw.print(yMid + "\t");
			pw.print(yMax + "\t");

			pw.print(avgGen + "\t");
			
			pw.print(loc.length);
			
			pw.println();
		}
		
		pw.close();
	}
	
	public void getCommunityAnalysisJavaDraw(CallDAG callDAG, String versionNum) throws Exception {		

		getWalktrapModules(callDAG, versionNum);
		
		List<String> communityList = new ArrayList<String>();	
		
		Scanner scanner = new Scanner(new File("module_graphs//w" + walkLength + "-" + versionNum + ".txt"));
		scanner.nextLine(); // skip first line
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.replaceAll(" ", "");
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			communityList.add(str);
		}
		scanner.close();
		
		Collections.sort(communityList, new Comparator<String>() {
			public int compare(String left, String right) {
				return right.length() - left.length();
			}
		});
		
		getDataToDrawCommunityShape(callDAG, communityList);
		getDataToDrawCommunityShape2(callDAG, communityList);
		getDataToDrawCommunitySpread(callDAG, communityList);
		
	}
}
