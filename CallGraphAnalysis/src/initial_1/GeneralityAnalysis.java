package initial_1;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GeneralityAnalysis {
	public void getGeneralityHistogram(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//gen-hist-" + filePath + ".txt"));
		for (String s: callDAG.generality.keySet()) {
			double g = callDAG.generality.get(s);
			pw.println(g);
		}
		pw.close();
	}
	
	public void getComplexityHistogram(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//cmp-hist-" + filePath + ".txt"));
		for (String s: callDAG.complexity.keySet()) {
			double c = callDAG.complexity.get(s);
			pw.println(c);
		}
		pw.close();
	}
	
	public void getLocationVsAvgGenerality(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-gen-" + filePath + ".txt"));
		Map<Double, List<Double>> locationVSGenerality = new TreeMap<Double, List<Double>>();
		
		for (String s: callDAG.functions) {
			double l = callDAG.location.get(s);
			double g = callDAG.generality.get(s);
			
			if (locationVSGenerality.containsKey(l)) {
				locationVSGenerality.get(l).add(g);
			}
			else {
				List<Double> list = new ArrayList<Double>();
				list.add(g);
				locationVSGenerality.put(l, list);
			}
		}
		
		for (double d: locationVSGenerality.keySet()) {
			double sum = 0;
			for (double e: locationVSGenerality.get(d)) {
				sum += e;
			}			
			double avg = sum / locationVSGenerality.get(d).size();
			pw.println(d + "\t" + avg); 
		}
		
		pw.close();
	}
	
	public void getLocationVsAvgComplexity(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-cmp-" + filePath + ".txt"));
		Map<Double, List<Double>> locationVSComplexity = new TreeMap<Double, List<Double>>();
		
		for (String s: callDAG.functions) {
			double l = callDAG.location.get(s);
			double g = callDAG.complexity.get(s);
			
			if (locationVSComplexity.containsKey(l)) {
				locationVSComplexity.get(l).add(g);
			}
			else {
				List<Double> list = new ArrayList<Double>();
				list.add(g);
				locationVSComplexity.put(l, list);
			}
		}
		
		for (double d: locationVSComplexity.keySet()) {
			double sum = 0;
			for (double e: locationVSComplexity.get(d)) {
				sum += e;
			}
			
			double avg = sum / locationVSComplexity.get(d).size();
			pw.println(d + "\t" + avg); 
		}
		
		pw.close();
	}
	
	public void getGeneralityVsComplexity(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//gen-vs-cmp-" + filePath + ".txt"));
		for (String s: callDAG.functions) {
			pw.println(callDAG.generality.get(s) + "\t" + callDAG.complexity.get(s));
		}
		pw.close();
	}
	
	public void getCentralNodes(CallDAG callDAG) { 
		// CUSTOMIZED FOR DIFFERENT NETWORK
		for (String s: callDAG.functions) {
			if (callDAG.generality.get(s) > 0.30 && callDAG.complexity.get(s) > 0.1) {
				System.out.println(s + "\t" + callDAG.location.get(s));
			}
		}
	}
}