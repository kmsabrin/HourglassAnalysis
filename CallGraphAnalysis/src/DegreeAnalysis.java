import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.correlation.*;

public class DegreeAnalysis {	
	
//	void getInDegreeDistribution(CallDAG callDAG, String filePath) throws Exception {		
//		PrintWriter pw = new PrintWriter(new File("Results//indeg-dist-" + filePath + ".txt"));
//		Map<Integer, Integer> inDegreeHistogram = new TreeMap();
//		
//		for (String s: callDAG.functions) {
//			int size;
//			if (callDAG.callFrom.containsKey(s)) {
//				size  = callDAG.callFrom.get(s).size();
//			}
//			else {
//				size = 0;
//			}
//			
//			if (inDegreeHistogram.containsKey(size)) {
//				inDegreeHistogram.put(size, inDegreeHistogram.get(size) + 1);
//			}
//			else {
//				inDegreeHistogram.put(size, 1);
//			}
//		}
//		
//		for (int i: inDegreeHistogram.keySet()) {
//			pw.println(Math.log10(i) + "\t" + Math.log10(inDegreeHistogram.get(i) * 1.0 / callDAG.functions.size()));
//		}
//		
//		pw.close();
//	}
//	
//	void getOutDegreeDistribution(CallDAG callDAG, String filePath) throws Exception {		
//		PrintWriter pw = new PrintWriter(new File("Results//outdeg-dist-" + filePath + ".txt"));
//		Map<Integer, Integer> outDegreeHistogram = new TreeMap();
//		
//		for (String s: callDAG.functions) {
//			int size;
//			if (callDAG.callTo.containsKey(s)) {
//				size  = callDAG.callTo.get(s).size();
//			}
//			else {
//				size = 0;
//			}
//			
//			if (outDegreeHistogram.containsKey(size)) {
//				outDegreeHistogram.put(size, outDegreeHistogram.get(size) + 1);
//			}
//			else {
//				outDegreeHistogram.put(size, 1);
//			}
//		}
//		
//		for (int i: outDegreeHistogram.keySet()) {
//			pw.println(Math.log10(i) + "\t" + Math.log10(outDegreeHistogram.get(i) * 1.0 / callDAG.functions.size()));
//		}
//		
//		pw.close();
//	}
	
	void getInDegreeCCDF(CallDAG callDAG, String filePath) throws Exception {		
		PrintWriter pw1 = new PrintWriter(new File("Results//indeg-CCDF-log-log-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//indeg-CCDF-" + filePath + ".txt"));
		Map<Integer, Integer> inDegreeHistogram = new TreeMap();
		Map<Integer, Double> inDegreeCDF = new TreeMap();
		
		for (String s: callDAG.functions) {
			int size;
			if (callDAG.callFrom.containsKey(s)) {
				size  = callDAG.callFrom.get(s).size();
			}
			else {
				size = 0;
			}
			
			if (inDegreeHistogram.containsKey(size)) {
				inDegreeHistogram.put(size, inDegreeHistogram.get(size) + 1);
			}
			else {
				inDegreeHistogram.put(size, 1);
			}
		}
		
		int cumulation = 0;
		for (int i: inDegreeHistogram.keySet()) {
			int inDeg = inDegreeHistogram.get(i);
			cumulation += inDeg;
			inDegreeCDF.put(i, cumulation * 1.0 / callDAG.functions.size());
		}
		
		// CCDF: Complementary Cumulative Distribution Function
		for (int i: inDegreeCDF.keySet()) {
			pw1.println(Math.log10(i+0.1) + "\t" + Math.log10((1.0 - inDegreeCDF.get(i)+0.001)));
			pw2.println(i + "\t" + (1.0 - inDegreeCDF.get(i)));

		}
		
		pw1.close();
		pw2.close();
	}
	
	void getOutDegreeCCDF(CallDAG callDAG, String filePath) throws Exception {					
		PrintWriter pw1 = new PrintWriter(new File("Results//outdeg-CCDF-log-log-" + filePath + ".txt"));
		PrintWriter pw2 = new PrintWriter(new File("Results//outdeg-CCDF-" + filePath + ".txt"));
		
		Map<Integer, Integer> outDegreeHistogram = new TreeMap();
		Map<Integer, Double> outDegreeCDF = new TreeMap();
		
		for (String s: callDAG.functions) {
			int size;
			if (callDAG.callTo.containsKey(s)) {
				size  = callDAG.callTo.get(s).size();
			}
			else {
				size = 0;
			}
		
			if (outDegreeHistogram.containsKey(size)) {
				outDegreeHistogram.put(size, outDegreeHistogram.get(size) + 1);
			}
			else {
				outDegreeHistogram.put(size, 1);
			}
		}
		
		int cumulation = 0;
		for (int i: outDegreeHistogram.keySet()) {
			int outDeg = outDegreeHistogram.get(i);
			cumulation += outDeg;
			outDegreeCDF.put(i, cumulation * 1.0 / callDAG.functions.size());
		}
		
		// CCDF: Complementary Cumulative Distribution Function
		for (int i: outDegreeCDF.keySet()) {
			pw1.println(Math.log10(i+0.1) + "\t" + Math.log10((1.0 - outDegreeCDF.get(i)+0.001)));
			pw2.println(i + "\t" + (1.0 - outDegreeCDF.get(i)));

		}
		
		pw1.close();
		pw2.close();
	}
		
	public void getLocationVSAvgOutDegree(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-outdeg-" + filePath + ".txt"));
//		getting average fanOut per location
		Map<Double, List<Integer>> result = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double m = callDAG.location.get(s); 
			
			if (result.containsKey(m)) {
				result.get(m).add(callDAG.outDegree.get(s));
			}
			else {
				List<Integer> list = new ArrayList();
				list.add(callDAG.outDegree.get(s));
				result.put(m, list);
			}
		}
		
		for (Double d: result.keySet()) {
			List<Integer> list = result.get(d);
			double average = 0;
			for (int i: list) {
				average += i;
			}
			average /= list.size();
			pw.println(d + "\t" + average);
		}
		pw.close();
	}
	
	public void getLocationVSAvgInDegree(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//loc-vs-avg-indeg-" + filePath + ".txt"));
//		getting average fanIN per location
		Map<Double, List<Integer>> result = new TreeMap();
		for (String s: callDAG.location.keySet()) {
			double m = callDAG.location.get(s); 
			
			if (result.containsKey(m)) {
				result.get(m).add(callDAG.inDegree.get(s));
			}
			else {
				List<Integer> list = new ArrayList();
				list.add(callDAG.inDegree.get(s));
				result.put(m, list);
			}
		}
		
		for (double d: result.keySet()) {
			List<Integer> degreeList = result.get(d);
			double average = 0;
			for (int i : degreeList) {
				average += i;
			}
			average /= degreeList.size();
			pw.println(d + " \t" + average);
		}
		pw.close();
	}
	
	public void getIndegreeVsOutDegree(CallDAG callDAG, String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//in-vs-out-" + filePath + ".txt"));
		for (String s: callDAG.functions) {
			pw.println(callDAG.inDegree.get(s) + "\t" + callDAG.outDegree.get(s));
		}
		pw.close();
	}
	
	public void getIndegreeVsOutDegreeCorrelationCoefficient(CallDAG callDAG) {
		double inDeg[] = new double[callDAG.functions.size()];
		double outDeg[] = new double[callDAG.functions.size()];
		int knt = 0;
		
		for (String s: callDAG.functions) {
			inDeg[knt] = callDAG.inDegree.get(s);
			outDeg[knt] = callDAG.outDegree.get(s);
			++knt;
		}
		
		SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
		System.out.println(spearmansCorrelation.correlation(inDeg, outDeg));
	}
	
	public void getSpearmanCoefficientForAllVersions() {
		for (int i = Driver.versiontStart; i < Driver.versionEnd; i++) {
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			getIndegreeVsOutDegreeCorrelationCoefficient(callDAG);
		}
	}
}
