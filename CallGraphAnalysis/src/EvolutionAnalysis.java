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
	
	public void getDeathBirthTrendPerLocationForConsecutiveVersions() {
		CallDAG callDAGFrom = new CallDAG("callGraphs//full.graph-2.6.0");
		
		for (int i = 1; i < 40; ++i) {
			CallDAG callDAGTo = new CallDAG("callGraphs//full.graph-2.6." + i);
			
			Set<String> sF = new HashSet(callDAGFrom.functions);
			Set<String> sT = new HashSet(callDAGTo.functions);

			Set<String> dead = new HashSet(sF);
			dead.removeAll(sT); // in sF but not in sT, dead nodes;
			
			Set<String> born = new HashSet(sT);
			born.removeAll(sF); // in sT but not in sF, born nodes;

			int counter[] = new int[101];

//			where are nodes dying
//			for (String s: dead) {
//				int loc = (int)(callDAGFrom.location.get(s) * 100);
//				counter[loc]++;
//			}
			
//			where are are nodes being born
			for (String s: born) {
				int loc = (int)(callDAGTo.location.get(s) * 100);
				counter[loc]++;
			}
			
			for (int j = 0; j < 101; j += 10) {
				System.out.print(counter[j] + "\t");
			}
			System.out.println();
						
			callDAGFrom = callDAGTo;
		}
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
}
