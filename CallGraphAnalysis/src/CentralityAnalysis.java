import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

public class CentralityAnalysis {
	double values[];
	int index;
	double nTotalPath;
	Map<String, Double> nodeCentrality;
	double nodePerLocation[];
	
	CentralityAnalysis(CallDAG callDAG) {
		getCentralityValues(callDAG);
	}
	
	void getCentralityValues(CallDAG callDAG) {
		nTotalPath = 0;
		nodePerLocation = new double[100 + 1];
		for (String s: callDAG.numOfLeafPath.keySet()) {
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			if (!callDAG.callFrom.containsKey(s)) { // is a root
				nTotalPath += nPath;
				nodePerLocation[(int)(callDAG.location.get(s) * 100)]++;
			}
		}
				
		nodeCentrality = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double loc = callDAG.location.get(s);
			double nPath = callDAG.numOfLeafPath.get(s) * callDAG.numOfRootPath.get(s);
			double centrality = nPath / nTotalPath;
			nodeCentrality.put(s, centrality);
		}
	}
	
	void getLocationVsAvgCentrality(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-centrality-" + filePath + ".txt"));
		
		Map<Double, Double> avgLocationCentrality = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double loc = callDAG.location.get(s);
			double centrality = nodeCentrality.get(s);
			
			if (avgLocationCentrality.containsKey(loc)) {
				double c = (avgLocationCentrality.get(loc) + centrality ) / 2.0;
				avgLocationCentrality.put(loc, c);
			}
			else {
				avgLocationCentrality.put(loc, centrality);
			}			
		}
		
		for (double d: avgLocationCentrality.keySet()) {
			pw.println(d + "\t" + avgLocationCentrality.get(d));
		}
		
		pw.close();
	}
	
	void getLocationCentralityScatter(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-centrality" + filePath + ".txt"));
		
		for (String s: nodeCentrality.keySet()) {
			double loc = callDAG.location.get(s);
			double centrality = nodeCentrality.get(s);
			pw.println(loc + "\t" + centrality);
		}
		
		pw.close();
	}
	
	void getCentralityCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw = new PrintWriter(new File("Results//centrality-cdf-" + filePath + ".txt"));
		
		Map<Double, Double> centralityFrequency = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double centrality = nodeCentrality.get(s);
			if (centralityFrequency.containsKey(centrality)) {
				double v = centralityFrequency.get(centrality);
				centralityFrequency.put(centrality, v + 1.0);
			}
			else {
				centralityFrequency.put(centrality, 1.0);
			}
		}
		
		double sum = 0;
		for (double d: centralityFrequency.keySet()) {
			sum += centralityFrequency.get(d);
			pw.println(d + "\t" + (sum / callDAG.location.size()));
		}
		
		pw.close();
	}
	
	void traverse(String node, CallDAG callDAG) {
		if (!callDAG.callTo.containsKey(node)) {
			values[index++] = nodeCentrality.get(node);
			return;
		}
		
		values[index++] = nodeCentrality.get(node);
		
		for (String s: callDAG.callTo.get(node)) {
			traverse(s, callDAG);
			break;
		}
	}
	
	void getSamplePathKurtosis(CallDAG callDAG) {
		double kSum = 0;
		double knt = 0;
		for (String s: callDAG.location.keySet()) {
			if (callDAG.callFrom.containsKey(s)) continue;
			
			values = new double[callDAG.location.size()];
			index = 0;
			traverse(s, callDAG);
			if (index < 4) continue;
			
			Kurtosis kurtosis = new Kurtosis();
			double k = kurtosis.evaluate(values, 0, index);	
//			System.out.println(k);
			kSum += k;
			++knt;
		}
		System.out.println(kSum / knt);
	}
}
