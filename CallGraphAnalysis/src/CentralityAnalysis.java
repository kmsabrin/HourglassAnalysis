import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class CentralityAnalysis {
	void getLocationVsAvgCentrality(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//location-vs-centrality-" + filePath + ".txt"));
		Map<Double, Double> avgLocationCentrality = new TreeMap();
		
		double nTotalPath = 0;
		double nMaxPath = Double.MIN_VALUE;
		double nMinPath = Double.MAX_VALUE;
		double nodePerLocation[] = new double[101];
		for (String s: callDAG.numOfLeafPath.keySet()) {
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				nTotalPath += nPath;
			}
			if (nPath > nMaxPath) {
				nMaxPath = nPath;	
			}
			if (nPath < nMinPath) {
				nMinPath = nPath;
			}
			
			nodePerLocation[(int)(callDAG.location.get(s) * 100)]++;
		}
		
//		double highCentralityNodePerLocation[] = new double[101];
//		for (String s: callDAG.numOfLeafPath.keySet()) {
//			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
//			if (nPath / nTotalPath > 0.1) {
////				System.out.println(callDAG.location.get(s));
//				highCentralityNodePerLocation[(int)(callDAG.location.get(s) * 100)]++;
//			}			
//		}
//		
//		for (int i = 0; i <= 100; ++i) {
//			System.out.println((i * 1.0 / 100.0) + "\t" + (highCentralityNodePerLocation[i] * 1.0 / nodePerLocation[i]));
//		}
		
//		System.out.println(nMinPath + "\t" + nMaxPath + "\t" + nTotalPath);
		
		for (String s: callDAG.location.keySet()) {
			double loc = callDAG.location.get(s);
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			double centrality = nPath / nTotalPath;
			
			if (avgLocationCentrality.containsKey(loc)) {
				double c = (avgLocationCentrality.get(loc) + centrality ) / 2.0;
				avgLocationCentrality.put(loc, c);
			}
			else {
				avgLocationCentrality.put(loc, centrality);
			}
			
			pw.println(loc + "\t" + centrality);
		}
		
//		for (double d: avgLocationCentrality.keySet()) {
//			pw.println(d + "\t" + avgLocationCentrality.get(d));
//		}
		
		pw.close();
	}
}
