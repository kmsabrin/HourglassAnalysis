import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class EvolutionAnalysis {

	public static void getAverageGeneralityPerLocationForEachVersion() throws Exception {
		for (int i = 0; i < 40; i += 12) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			GeneralityAnalysis generalityAnalysis = new GeneralityAnalysis();			
//			generalityAnalysis.getLocationVSAvgGenerality(callDAG, "v" + i);
			generalityAnalysis.getLocationVSAvgComplexity(callDAG, "v" + i);
		}
	}
	
	public void getClusterSizeTrend() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//cluster-vs-network-growth.txt"));

		for (int i = 0; i < 40; ++i) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);

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
	
	public void getEvolutionaryDeathBirthTrend() throws Exception {
		PrintWriter pwD = new PrintWriter(new File("Results//evo-death.txt"));
		PrintWriter pwB = new PrintWriter(new File("Results//evo-birth.txt"));
		CallDAG callDAGFrom = new CallDAG("callGraphs//full.graph-2.6.0");
		
		for (int i = 1; i < 40; ++i) {
			CallDAG callDAGTo = new CallDAG("callGraphs//full.graph-2.6." + i);
			
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
	
	public void getLocationHistogramForEachVersion() throws Exception { // fig:loc-vs-evo-siz
		for (int i = 0; i < 40; ) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			LocationAnalysis locationAnalysis = new LocationAnalysis();
			locationAnalysis.getLocationHistogram(callDAG, "v" + i);
		}
	}
	
	public void getViolationMetricForEachVersion() { // tab:loc-viol
		for (int i = 0; i < 40; ++i) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			LocationAnalysis locationAnalysis = new LocationAnalysis();
			locationAnalysis.getCallViolationMetric(callDAG);
		}
	}
	
	public void getNumClustersForEachVersion() throws Exception {
		for (int i = 0; i < 40; ++i) {
			System.out.print("Version 2.6." + i + "\t");
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			ClusterAnalysis clusterAnalysis = new ClusterAnalysis(0.03,  30);
			clusterAnalysis.getClusters(callDAG);
		}
	}
	
	public void getLocationVsSizeTrend() throws Exception { // fig:loc-vs-evo-siz
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-evo-siz.txt"));
		int nVersions = 5;
		int sample[] = new int[]{0, 9, 19, 29, 39};
		int result[][] = new int[101][nVersions];
		
		int j = 0;
		for (int i: sample) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
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
	
	public void getNetworkGrwothTrend() throws Exception { //
		PrintWriter pw = new PrintWriter(new File("Results//network-growth.txt"));
		for (int i = 0; i < 40; ++i) {
			pw.print(i);
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			pw.print("\t" + callDAG.functions.size());
			pw.println("\t" + callDAG.nEdges);
		}
		pw.close();
	}
	
	public void getGenCmpScatterForAllVersions() throws Exception {
		for (int i = 0; i < 40; ++i) {
			String versionNum = "v" + i;
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			GeneralityAnalysis genAnalysis = new GeneralityAnalysis();
			genAnalysis.getGeneralityVSComplexity(callDAG, versionNum);
		}

	}
}
