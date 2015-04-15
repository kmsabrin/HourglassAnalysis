package Initial;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class EvolutionAnalysis {	
	public static void getWalktrapCallDAGForEachVersion() throws Exception {
		for (int i = Driver.versiontStart + 2; i < Driver.versionEnd; i++) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			String versionNum = Driver.networkType + "-" + i;
			ModularityAnalysis.getCallDAGforWalktrap(callDAG, versionNum);
		}
	}
	
	public static void getAverageGenCmpPerLocationForEachVersion() throws Exception {
		for (int i = Driver.versiontStart; i < Driver.versionEnd; i += 12) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();			
//			generalityAnalysis.getLocationVsAvgGenerality(callDAG, "v" + i);
			generalityAnalysis.getLocationVsAvgComplexity(callDAG, "v" + i);
		}
	}
	
	public void getGenCmpScatterForAllVersions() throws Exception {
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			String versionNum = Driver.networkType + "-" + i;
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			GeneralityAnalysis genAnalysis = new GeneralityAnalysis();
			genAnalysis.getGeneralityVsComplexity(callDAG, versionNum);
		}
	}

	public void getLocationHistogramForEachVersion() throws Exception { // fig:loc-vs-evo-siz
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			LocationAnalysis locationAnalysis = new LocationAnalysis();
			locationAnalysis.getLocationHistogram(callDAG, Driver.networkType + i);
		}
	}
	
	public void getViolationMetricForEachVersion() { // tab:loc-viol
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			LocationAnalysis locationAnalysis = new LocationAnalysis();
			locationAnalysis.getCallViolationMetric(callDAG);
		}
	}
	
	public void getEvolutionaryDeathBirthTrend() throws Exception {
		PrintWriter pwD = new PrintWriter(new File("Results//" + Driver.networkType + "-evo-death.txt"));
		PrintWriter pwB = new PrintWriter(new File("Results//" + Driver.networkType + "-evo-birth.txt"));
		CallDAG callDAGFrom = new CallDAG(Driver.networkPath + Driver.versiontStart); 
		
		for (int i = Driver.versiontStart + 1; i < Driver.versionEnd; ++i) {
			CallDAG callDAGTo = new CallDAG(Driver.networkPath + i);
			
			Set<String> sF = new HashSet<String>(callDAGFrom.functions);
			Set<String> sT = new HashSet<String>(callDAGTo.functions);

			Set<String> dead = new HashSet<String>(sF);
			dead.removeAll(sT); // in sF but not in sT, dead nodes;
			
			Set<String> born = new HashSet<String>(sT);
			born.removeAll(sF); // in sT but not in sF, born nodes;

			int counterD[] = new int[101];
			int counterB[] = new int[101];
			
//			where are nodes dying
			for (String s: dead) {
				int loc = (int)(callDAGFrom.location.get(s) * 100);
				counterD[loc]++;
			}
			
//			where are are nodes being born
			for (String s: born) {
				int loc = (int)(callDAGTo.location.get(s) * 100);
				counterB[loc]++;
			}
			
			for (int j = 0; j < 101; j++) {
				pwD.print(counterD[j] + "\t");
				pwB.print(counterB[j] + "\t");
			}
			pwD.println();
			pwB.println();
			
			callDAGFrom = callDAGTo;
		}
		
		pwD.close();
		pwB.close();
	}
		
	public void getLocationVsSizeTrend() throws Exception { // fig:loc-vs-evo-siz
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkType + "-loc-vs-evo-size.txt"));
		int nVersions = 5;
		int sample[] = new int[]{1, 9, 19, 29, 39}; // change for different network
		int result[][] = new int[101][nVersions];
		
		int j = 0;
		for (int i: sample) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			for (String f: callDAG.functions) {
				double location = callDAG.location.get(f);
				int loc = (int)(location * 100);
				result[loc][j]++;
			}
			++j;
		}
		
		for (int i = 0; i <= 100; ++i) {
			pw.print((i/100.0));
			for (j = 0; j < nVersions; ++j) {
				pw.print("\t" + result[i][j]);
			}
			pw.println();
		}
		
		pw.close();
	}
	
	public void getNetworkGrowthTrend() throws Exception { //
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkType + "-network-growth.txt"));
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			pw.print(i);
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			pw.print("\t" + callDAG.functions.size());
			pw.println("\t" + callDAG.nEdges);
		}
		pw.close();
	}
	
	public void getClusterSizeTrend() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkType + "-cluster-vs-network-growth.txt"));

		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);

			double generalitySeparator, complexitySeparator;
			double gS = 0, cS = 0;
			for (String s: callDAG.location.keySet()) {
				gS += callDAG.generality.get(s);
				cS += callDAG.complexity.get(s);
			}
			
			generalitySeparator = gS / callDAG.location.size();
			complexitySeparator = cS / callDAG.location.size();
			
			int kGC = 0, kgC = 0, kgc = 0, kGc = 0;
			for (String s: callDAG.location.keySet()) {			
				double m = callDAG.location.get(s);
				double g = callDAG.generality.get(s);
				double c = callDAG.complexity.get(s);
				
				if (g > generalitySeparator && c > complexitySeparator) kGC++;
				else if (g <= generalitySeparator && c > complexitySeparator) kgC++;
				else if (g <= generalitySeparator && c <= complexitySeparator) kgc++;
				else if (g > generalitySeparator && c <= complexitySeparator) kGc++;
			}
			
			pw.println(i + "\t" + callDAG.location.size() + "\t" + kGC + "\t" + kgC + "\t" + kgc + "\t" + kGc);
		}
		
		pw.close();
	}
	
//	public void getNumClustersForEachVersion() throws Exception {
//		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
//			System.out.print(Driver.networkUsed + i + "\t");
//			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
//			ClusterAnalysis clusterAnalysis = new ClusterAnalysis(0.03,  30);
//			clusterAnalysis.getClusters(callDAG);
//		}
//	}
	
	private double getJaccard(Set<String> s1, Set<String> s2) {
		Set<String> capSet = new HashSet<String>(s1);
		Set<String> cupSet = new HashSet<String>(s1);
		
		capSet.retainAll(s2);
		cupSet.addAll(s2);
		
		return capSet.size() * 1.0 / cupSet.size();
	}
	
	public void compareConsecutiveVersionFunctionsNeighborhood(String vA, String vB) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkType + "-" + vA + "-" + vB + "-module-cdf.txt"));
		
		CallDAG callDAGvA = new CallDAG(Driver.networkPath + vA);
		ModularityAnalysis modularityAnalysisvA = new ModularityAnalysis();
		modularityAnalysisvA.getWalktrapModules(callDAGvA, Driver.networkType + "-" + vA);
		
		CallDAG callDAGvB = new CallDAG(Driver.networkPath + vB);		
		ModularityAnalysis modularityAnalysisvB = new ModularityAnalysis();
		modularityAnalysisvB.getWalktrapModules(callDAGvB, Driver.networkType + "-" + vB);
		
		Set<String> vAFunctions = new HashSet<String>();
		Set<String> vBFunctions = new HashSet<String>();
		
		for (String s: modularityAnalysisvA.communities.keySet()) {
			vAFunctions.addAll(modularityAnalysisvA.communities.get(s));
		}
		
		for (String s: modularityAnalysisvB.communities.keySet()) {
			vBFunctions.addAll(modularityAnalysisvB.communities.get(s));
		}
		
		double distanceHisto[] = new double[110];
		double kount = 0;
		for (String s: modularityAnalysisvA.communities.keySet()) {			
			for (String r: modularityAnalysisvA.communities.get(s)) {
				Set<String> funcRneighborhoodvA = new HashSet<String>(modularityAnalysisvA.communities.get(s));		
				Set<String> funcRneighborhoodvB = null;
				for (String t: modularityAnalysisvB.communities.keySet()) {
					if (modularityAnalysisvB.communities.get(t).contains(r)) {
						funcRneighborhoodvB = new HashSet<String>(modularityAnalysisvB.communities.get(t));
						break;
					}
				}
				
				if (funcRneighborhoodvB == null) {
					continue; // function R was removed in v-B
				}
								
				// remove functions that were not present in both versions
				List<String> stringList = new ArrayList<String>();
				for (String t: funcRneighborhoodvA) {
					if (!vBFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				funcRneighborhoodvA.removeAll(stringList);
				
				stringList = new ArrayList<String>();
				for (String t: funcRneighborhoodvB) {
					if (!vAFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				funcRneighborhoodvB.removeAll(stringList);
				
				double jaccardDistance = getJaccard(funcRneighborhoodvA, funcRneighborhoodvB);
				int v = (int) (jaccardDistance * 100.0);
				distanceHisto[v]++;
				kount++;
//				System.out.println(r + "\t" + jaccardDistance + "\t" + funcRneighborhoodvA.size() + "\t" + funcRneighborhoodvB.size());
			}
		}
		
		double sum = 0;
		for (int i = 0; i <= 100; ++i) {
			sum += distanceHisto[i];
			pw.println( (i / 100.0) + "\t" + (sum / kount));
		}
		
		pw.close();
	}
	
	public void compareConsecutiveVersionModules(String vA, String vB) throws Exception {
		
		class Info implements Comparable<Info> {
			int size;
			double dist;
			
			Info(int size, double dist) {
				this.size = size;
				this.dist = dist;
			}
			
			public int compareTo(Info compareInfo) {
				 
				double compareQuantity = ((Info) compareInfo).dist; 
		 
				//ascending order
//				return this.quantity - compareQuantity?;
		 
				//descending order
				return  (int)(100 * (compareQuantity - this.dist));
		 
			}	
		}
		
		PrintWriter pw0 = new PrintWriter(new File("Results//" + Driver.networkType + "-" + vA + "-" + vB + "-module-to-module-closest-diff-cdf.txt"));
		PrintWriter pw1 = new PrintWriter(new File("Results//" + Driver.networkType + "-" + vA + "-" + vB + "-module-to-module-closest-cdf.txt"));
		
		PrintWriter pw2 = new PrintWriter(new File("Results//" + Driver.networkType + "-" + vA + "-" + vB + "-community-matching.txt"));
		
		CallDAG callDAGvA = new CallDAG(Driver.networkPath + vA);
		ModularityAnalysis modularityAnalysisvA = new ModularityAnalysis();
		modularityAnalysisvA.getWalktrapModules(callDAGvA, Driver.networkType + "-" + vA);
		
		CallDAG callDAGvB = new CallDAG(Driver.networkPath + vB);		
		ModularityAnalysis modularityAnalysisvB = new ModularityAnalysis();
		modularityAnalysisvB.getWalktrapModules(callDAGvB, Driver.networkType + "-" + vB);
		
		Set<String> vAFunctions = new HashSet<String>();
		Set<String> vBFunctions = new HashSet<String>();
		
		for (String s: modularityAnalysisvA.communities.keySet()) {
			vAFunctions.addAll(modularityAnalysisvA.communities.get(s));
		}
		
		for (String s: modularityAnalysisvB.communities.keySet()) {
			vBFunctions.addAll(modularityAnalysisvB.communities.get(s));
		}
		
		double diffHisto[] = new double[110];
		double closestHisto[] = new double[110];
		double kount = 0;
		
		for (String r: modularityAnalysisvA.communities.keySet()) {
			
			List<Double> distanceList = new ArrayList<Double>();
			Set<Info> tset = new TreeSet<Info>();
		
			int val = 0;
			String vBMaxMatchId = "-";
			double maxJaccardSimilarity = 0.0;
			double maxSizeDelta = 0;
			double maxLocDelta = 0;
			double functionAdded = 0;
			double functionRemoved = 0;
			double medianLocationDispersion = 0;
			
			for (String s: modularityAnalysisvB.communities.keySet()) {
				Set<String> comRverA = new HashSet<String>(modularityAnalysisvA.communities.get(r));
				Set<String> comSverB = new HashSet<String>(modularityAnalysisvB.communities.get(s));
				
				int fA;
				int fR;
				int sizeDelta = comSverB.size() - comRverA.size();
				double locDelta = modularityAnalysisvB.communitiesAvgLocation.get(s) - modularityAnalysisvA.communitiesAvgLocation.get(r);
				
				List<String> stringList = new ArrayList<String>();
				for (String t: comRverA) {
					if (!vBFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				comRverA.removeAll(stringList);
				
				stringList = new ArrayList<String>();
				for (String t: comSverB) {
					if (!vAFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				comSverB.removeAll(stringList);
				
				double jaccardSimilarity = getJaccard(comRverA, comSverB);
				distanceList.add(jaccardSimilarity);
				
				if (jaccardSimilarity > maxJaccardSimilarity) {
					maxJaccardSimilarity = jaccardSimilarity;
					maxSizeDelta = sizeDelta;
					maxLocDelta = locDelta;
					vBMaxMatchId = s;
				}
				
				
				tset.add(new Info(comSverB.size(), jaccardSimilarity));
				val = comRverA.size();
			}
			
			pw2.println(r + " " + vBMaxMatchId + " " + maxJaccardSimilarity + " " + maxSizeDelta + " " + maxLocDelta);
			
			Collections.sort(distanceList, Collections.reverseOrder());
			
//			System.out.println(distanceList.get(0) + "\t" + distanceList.get(1));
			
			int v = (int) (((distanceList.get(0) - distanceList.get(1)) / distanceList.get(0)) * 100);
			diffHisto[v]++;
			closestHisto[(int)(distanceList.get(0) * 100)]++;
			kount++;
			
			if (v < 80) {
				System.out.println(val + "\t" + distanceList.get(0) + "\t" + distanceList.get(1));
				int idx = 0; 
				for (Info ifo: tset) {
					++idx;
					if (idx > 2) break;
					System.out.print("\t" + ifo.dist + "\t" + ifo.size);
				}
				System.out.println();
			}
		}
		
		double sum = 0;
		for (int i = 0; i <= 100; ++i) {
			sum += diffHisto[i];
			pw0.println(i + "\t" + (sum / kount));
		}
		
		sum = 0;
		for (int i = 0; i <= 100; ++i) {
			sum += closestHisto[i];
			pw1.println(i + "\t" + (sum / kount));
		}
		
		pw0.close();
		pw1.close();
		pw2.close();

	}
	
	public void getCommunityEvolutionData() throws Exception {
		
//		CallDAG callDAGvA = new CallDAG(Driver.networkPath + "0");
//		ModularityAnalysis modularityAnalysis = new ModularityAnalysis();
//		modularityAnalysis.getWalktrapModules(callDAGvA, Driver.networkUsed + "-0");
//		for (String s: modularityAnalysis.communities.keySet()) {
//			System.out.println(s + "\t" + modularityAnalysis.communities.get(s).size());
//		}
		
		String largeTen[] = { "C48", "C100", "C72", "C99", "C92", "C85", "C155", "C156", "C34", "C81" };

		for (int k = 0; k < 10; ++k) {
			PrintWriter pw = new PrintWriter(new File("Results//Com-" + k + "evo-data.txt"));
			
			for (int i = 0; i < 39; ++i) {
				String vA = Integer.toString(i);
				String vB = Integer.toString(i + 1);
//				 compareConsecutiveVersionModules(Integer.toString(i), Integer.toString(i + 1));

				Scanner scan = new Scanner(new File("Results//" + Driver.networkType + "-" + vA + "-" + vB + "-community-matching.txt"));

				while (scan.hasNext()) {
					String strFrom = scan.next();
					String strTo = scan.next();					
					double js = scan.nextDouble();
					double dSz = scan.nextDouble();
					double dML = scan.nextDouble();
					
					// System.out.println(strFrom + "--" + strTo + " " + i);
					
					if (!strFrom.equals(largeTen[k])) {	
						continue;
					}					
					
					pw.println(js + "\t" + dSz + "\t" + dML);
					break;
				}
				
				scan.close();
			}
			
			pw.close();
		}
	}
}
