import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EvolutionAnalysis {	
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
			String versionNum = Driver.networkUsed + "-" + i;
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			GeneralityAnalysis genAnalysis = new GeneralityAnalysis();
			genAnalysis.getGeneralityVsComplexity(callDAG, versionNum);
		}
	}

	public void getLocationHistogramForEachVersion() throws Exception { // fig:loc-vs-evo-siz
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			LocationAnalysis locationAnalysis = new LocationAnalysis();
			locationAnalysis.getLocationHistogram(callDAG, Driver.networkUsed + i);
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
		PrintWriter pwD = new PrintWriter(new File("Results//" + Driver.networkUsed + "-evo-death.txt"));
		PrintWriter pwB = new PrintWriter(new File("Results//" + Driver.networkUsed + "-evo-birth.txt"));
		CallDAG callDAGFrom = new CallDAG(Driver.networkPath + Driver.versiontStart); 
		
		for (int i = Driver.versiontStart + 1; i < Driver.versionEnd; ++i) {
			CallDAG callDAGTo = new CallDAG(Driver.networkPath + i);
			
			Set<String> sF = new HashSet(callDAGFrom.functions);
			Set<String> sT = new HashSet(callDAGTo.functions);

			Set<String> dead = new HashSet(sF);
			dead.removeAll(sT); // in sF but not in sT, dead nodes;
			
			Set<String> born = new HashSet(sT);
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
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-loc-vs-evo-size.txt"));
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
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-network-growth.txt"));
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {
			pw.print(i);
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			pw.print("\t" + callDAG.functions.size());
			pw.println("\t" + callDAG.nEdges);
		}
		pw.close();
	}
	
	public void getClusterSizeTrend() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-cluster-vs-network-growth.txt"));

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
		Set<String> capSet = new HashSet(s1);
		Set<String> cupSet = new HashSet(s1);
		
		capSet.retainAll(s2);
		cupSet.addAll(s2);
		
		return capSet.size() * 1.0 / cupSet.size();
	}
	
	public void compareConsecutiveVersionFunctionsNeighborhood(String vA, String vB) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-" + vA + "-" + vB + "-module-cdf.txt"));
		
		CallDAG callDAGvA = new CallDAG(Driver.networkPath + vA);
		ModularityAnalysis modularityAnalysisvA = new ModularityAnalysis();
		modularityAnalysisvA.getWalktrapModules(callDAGvA, Driver.networkUsed + "-" + vA);
		
		CallDAG callDAGvB = new CallDAG(Driver.networkPath + vB);		
		ModularityAnalysis modularityAnalysisvB = new ModularityAnalysis();
		modularityAnalysisvB.getWalktrapModules(callDAGvB, Driver.networkUsed + "-" + vB);
		
		Set<String> vAFunctions = new HashSet();
		Set<String> vBFunctions = new HashSet();
		
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
				Set<String> funcRneighborhoodvA = new HashSet(modularityAnalysisvA.communities.get(s));		
				Set<String> funcRneighborhoodvB = null;
				for (String t: modularityAnalysisvB.communities.keySet()) {
					if (modularityAnalysisvB.communities.get(t).contains(r)) {
						funcRneighborhoodvB = new HashSet(modularityAnalysisvB.communities.get(t));
						break;
					}
				}
				
				if (funcRneighborhoodvB == null) {
					continue; // function R was removed in v-B
				}
								
				// remove functions that were not present in both versions
				List<String> stringList = new ArrayList();
				for (String t: funcRneighborhoodvA) {
					if (!vBFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				funcRneighborhoodvA.removeAll(stringList);
				
				stringList = new ArrayList();
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
		
		
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-" + vA + "-" + vB + "-module-to-module.txt"));
		
		CallDAG callDAGvA = new CallDAG(Driver.networkPath + vA);
		ModularityAnalysis modularityAnalysisvA = new ModularityAnalysis();
		modularityAnalysisvA.getWalktrapModules(callDAGvA, Driver.networkUsed + "-" + vA);
		
		CallDAG callDAGvB = new CallDAG(Driver.networkPath + vB);		
		ModularityAnalysis modularityAnalysisvB = new ModularityAnalysis();
		modularityAnalysisvB.getWalktrapModules(callDAGvB, Driver.networkUsed + "-" + vB);
		
		Set<String> vAFunctions = new HashSet();
		Set<String> vBFunctions = new HashSet();
		
		for (String s: modularityAnalysisvA.communities.keySet()) {
			vAFunctions.addAll(modularityAnalysisvA.communities.get(s));
		}
		
		for (String s: modularityAnalysisvB.communities.keySet()) {
			vBFunctions.addAll(modularityAnalysisvB.communities.get(s));
		}
		
		double diffHisto[] = new double[110];
		double kount = 0;
		
		for (String r: modularityAnalysisvA.communities.keySet()) {
			
			List<Double> distanceList = new ArrayList();
			Set<Info> tset = new TreeSet();
		
			int val = 0;
			
			for (String s: modularityAnalysisvB.communities.keySet()) {
				Set<String> comRverA = new HashSet(modularityAnalysisvA.communities.get(r));
				Set<String> comSverB = new HashSet(modularityAnalysisvB.communities.get(s));
				
				List<String> stringList = new ArrayList();
				for (String t: comRverA) {
					if (!vBFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				comRverA.removeAll(stringList);
				
				stringList = new ArrayList();
				for (String t: comSverB) {
					if (!vAFunctions.contains(t)) {
						stringList.add(t);
					}
				}
				comSverB.removeAll(stringList);
				
				double jaccardDistance = getJaccard(comRverA, comSverB);
				distanceList.add(jaccardDistance);
				
				tset.add(new Info(comSverB.size(), jaccardDistance));
				val = comRverA.size();
			}
			
			Collections.sort(distanceList, Collections.reverseOrder());
			
//			System.out.println(distanceList.get(0) + "\t" + distanceList.get(1));
			
			int v = (int) (((distanceList.get(0) - distanceList.get(1)) / distanceList.get(0)) * 100);
			diffHisto[v]++;
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
			pw.println(i + "\t" + (sum / kount));
		}
		
		pw.close();
	}
}
