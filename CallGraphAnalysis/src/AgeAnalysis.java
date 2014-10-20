import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;

public class AgeAnalysis {
	Map<String, Integer> birthVersion;
	Map<String, Double> birthLocation;
	Map<String, Double> birthComplexity;
	Map<String, Double> birthGenerality;

	Map<String, Integer> lastVersion;
	Map<String, Double> lastLocation;
	Map<String, Double> lastComplexity;
	Map<String, Double> lastGenerality;

	Map<String, Double> avgLocation;
	Map<String, Double> avgComplexity;
	Map<String, Double> avgGenerality;
	
	Map<String, Integer> functionPersistence;
		
	AgeAnalysis() {
		birthVersion = new HashMap();
		birthLocation = new HashMap();
		birthComplexity = new HashMap();
		birthGenerality = new HashMap();

		lastVersion = new HashMap();
		lastLocation = new HashMap();
		lastComplexity = new HashMap();
		lastGenerality = new HashMap();

		avgLocation = new HashMap();
		avgComplexity = new HashMap();
		avgGenerality = new HashMap();

		functionPersistence = new HashMap();
		
		/****
		 * Think about using avgLocation, avgComplexity and avgGenerality
		 */
		Set<String> totalFunction = new HashSet();
		Set<String> rebornFunction = new HashSet();
		
		for (int i = Driver.versiontStart; i < Driver.versionEnd; ++i) {	
			CallDAG callDAG = new CallDAG(Driver.networkPath + i);
			for (String s: callDAG.functions) {
				if (!birthVersion.containsKey(s)) {
					birthVersion.put(s, i);
					birthLocation.put(s, callDAG.location.get(s));
					birthGenerality.put(s, callDAG.generality.get(s));
					birthComplexity.put(s, callDAG.complexity.get(s));
				}
					
//				check for death followed by rebirth
				totalFunction.add(s);
//				if (mostRecentVersion.containsKey(s) && mostRecentVersion.get(s) != i - 1) {
//					System.out.println("Function Name: " + s 
//							+ " Death Version: " + mostRecentVersion.get(s) + " Rebirth Version: " + i);
//					rebornFunction.add(s);
//				}
				
				lastVersion.put(s, i);
				lastLocation.put(s, callDAG.location.get(s));
				lastComplexity.put(s, callDAG.complexity.get(s));
				lastGenerality.put(s, callDAG.generality.get(s));
				
				if (avgLocation.containsKey(s)) { // approximating average
					double avgLoc = 0.5 * (callDAG.location.get(s) + avgLocation.get(s));
					double avgGen = 0.5 * (callDAG.generality.get(s) + avgGenerality.get(s));
					double avgCmp = 0.5 * (callDAG.complexity.get(s) + avgComplexity.get(s));
					avgLoc = ((int)(avgLoc * 100.0)) / 100.0; // rounding down
					avgGen = ((int)(avgGen * 100.0)) / 100.0; // rounding down
					avgCmp = ((int)(avgCmp * 100.0)) / 100.0; // rounding down
					avgLocation.put(s, avgLoc);
					avgComplexity.put(s, avgCmp);
					avgGenerality.put(s, avgGen);
				}
				else {
					avgLocation.put(s, callDAG.location.get(s));
					avgComplexity.put(s, callDAG.complexity.get(s));
					avgGenerality.put(s, callDAG.generality.get(s));
				}
				
				if (functionPersistence.containsKey(s)) {
					int persistence = functionPersistence.get(s) + 1;
					functionPersistence.put(s, persistence);
				}
				else {
					functionPersistence.put(s, 1);
				}
			}
		}
		
//		System.out.println("nFunctions: " + totalFunction.size() + " nRebornFunctions: " + rebornFunction.size());
	}
		
//	public void getAgeHistogram() {
//		int ages[] = new int[50];
//		int cumulativeHistogram[] = new int[50];
//			
//		for (String s: birthVersion.keySet()) {
//			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
////			if (age >= 39 )
//				ages[age]++;
//		}		
//		
////		for (int i = 1; i < 31; ++i) {
////			System.out.println(i +"\t" + ages[i] * 100.0 / birthVersion.size());
////		}
//		
//		for (int i = 39; i >= 0; --i) {
//			cumulativeHistogram[i] = cumulativeHistogram[i + 1] + ages[i + 1];
//			System.out.println(i + "\t" + cumulativeHistogram[i]);
//		}
//	}
//	
//	public double getMode(List<Double> list) {
//		Map<Double, Integer> count = new HashMap();
//		for (Double d: list) {
//			if (count.containsKey(d)) {
//				int v = count.get(d) + 1;
//				count.put(d, v);
//			}
//			else {
//				count.put(d, 1);
//			}
//		}
//		
//		int max = -1;
//		double mode = -1;
//		
//		for (Double d: count.keySet()) {
//			int v = count.get(d);
//			if (v > max) {
//				max = v;
//				mode = d;
//			}
//		}
//		
//		System.out.println(mode + "--" + max + "-- total " + list.size());
//		
//		return mode;
//	}
//	
//	// for alive nodes
//	public void getLocationModeVSAge() {
//		Map<Integer, List<Double>> locationModeVSAge = new TreeMap();
//			
//		for (String s: birthVersion.keySet()) {
//			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
//			double location = birthLocation.get(s);
//			if (mostRecentVersion.get(s) < 29) continue; // dead node
//			
//			if (locationModeVSAge.containsKey(age)) {
//				locationModeVSAge.get(age).add(location);
//			}
//			else {
//				List<Double> list = new ArrayList();
//				list.add(location);
//				locationModeVSAge.put(age, list);
//			}
//		}		
//		
//		for (int i = 1; i < 31; ++i) {
//			System.out.println(i +"\t" + getMode(locationModeVSAge.get(i)));
//		}
//	}
//	
//	public void getLastLocationVSDeathPercentage() {
//		Map<Double, Integer> locationVSDeathFrequency = new TreeMap();
//		Map<Double, Integer> locationFrequency = new TreeMap();
//		
//		for (String s: birthVersion.keySet()) {
//			double location = mostRecentLocation.get(s); 
//			if (mostRecentVersion.get(s) < 29) {
//				if (locationVSDeathFrequency.containsKey(location)) {
//					int v = locationVSDeathFrequency.get(location);
//					locationVSDeathFrequency.put(location, v + 1);
//				}
//				else locationVSDeathFrequency.put(location, 1);
//			}
//			
//			if (locationFrequency.containsKey(location)) {
//				int v = locationFrequency.get(location);
//				locationFrequency.put(location, v + 1);
//			}
//			else locationFrequency.put(location, 1);		
//		}
//		
//		System.out.println("Last Location VS Death Percentage");
//		for (Double d: locationVSDeathFrequency.keySet()) {
//			System.out.println(d + "\t" + locationVSDeathFrequency.get(d) * 100.0 / locationFrequency.get(d));
//		}
//	}
//	
//	public void getLastLocationVSPersistency() {
//		Map<Double, Integer> locationVSAliveFrequency = new TreeMap();
//		Map<Double, Integer> locationFrequency = new TreeMap();
//		
//		for (String s: birthVersion.keySet()) {
//			double location = mostRecentLocation.get(s); 
//			if (mostRecentVersion.get(s) >= 29) {
//				if (locationVSAliveFrequency.containsKey(location)) {
//					int v = locationVSAliveFrequency.get(location);
//					locationVSAliveFrequency.put(location, v + 1);
//				}
//				else locationVSAliveFrequency.put(location, 1);
//			}
//			
//			if (locationFrequency.containsKey(location)) {
//				int v = locationFrequency.get(location);
//				locationFrequency.put(location, v + 1);
//			}
//			else locationFrequency.put(location, 1);		
//		}
//		
//		System.out.println("Last Location VS Alive Percentage");
//		for (Double d: locationVSAliveFrequency.keySet()) {
//			System.out.println(d + "\t" + locationVSAliveFrequency.get(d) * 100.0 / locationFrequency.get(d));
//		}
//	}
//	
//	public void getAgeVSDeathPercentage() {
//		int deathFrequencey[] = new int[50];
//		int nDeadNodes = 0;
//			
//		for (String s: birthVersion.keySet()) {
//			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
//			/*##########################################################*/
//			if (mostRecentVersion.get(s) < 29) { // hard code
//				++nDeadNodes;
//				deathFrequencey[age]++;
//			}
//		}
//		
//		System.out.println("Age VS Death Percentage");
//		for (int i = 1; i < 31; ++i) {
//			System.out.println(i + "\t" + deathFrequencey[i] * 100.0 / nDeadNodes);
//		}
//	}
//	
//	public void getLastLocationVSAverageAge() {
//		Map<Double, List<Integer>> locationVSAges = new TreeMap();
//		
//		for (String s: birthVersion.keySet()) {
//			if (mostRecentVersion.get(s) < 29) continue; // consider live nodes only
//			
//			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
//			double location = mostRecentLocation.get(s);
//			if (locationVSAges.containsKey(location)) {
//				locationVSAges.get(location).add(age);
//			}
//			else {
//				List<Integer> ages = new ArrayList();
//				ages.add(age);
//				locationVSAges.put(location, ages);
//			}
//		}
//		
//		for (double d: locationVSAges.keySet()) {
////			AVERAGE
//			double sum = 0;
//			for (int i: locationVSAges.get(d)) {
//				sum += i;
//			}
//			double avg = sum / locationVSAges.get(d).size();
//			System.out.println(d + "\t" + avg);
//			
////			MEDIAN
////			List<Integer> ages = locationVSAges.get(d);
////			Collections.sort(ages);
////			double median = ages.get(ages.size() / 2);
////			System.out.println(d + "\t" + median);
//		}
//	}
//	
//	public void getAgeVSLastLocation() {
//		Map<Double, Integer> locationAgeMap = new HashMap();
//		for (String s: birthVersion.keySet()) {
//			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
//			double location = mostRecentLocation.get(s);
//			// hard code
//			if (mostRecentVersion.get(s) >= 29 && age > 20) { 
//				System.out.println(age + "\t" + location);				
//			}	
//		}
//	}
//	
//	public void getClusterAgeDistribution() { // visually separated clusters
//	double count1 = 0;
//	double count2 = 0;
//	
//	Map<Integer, Integer> ageHistogram = new TreeMap();
//	for (String s: birthVersion.keySet()) {			
//		int a = mostRecentVersion.get(s) - birthVersion.get(s) + 1; // age
////		if (a < 40) continue; // consider live nodes only
//
//		double m = mostRecentLocation.get(s);
//		double g = mostRecentGenerality.get(s);
//		double c = mostRecentComplexity.get(s);
//		
//		if (g > 0.25) continue;
//		if (c > 0.05) continue;
//		
//		if (mostRecentVersion.get(s) >= 29) ++count1;
//		if (a == 40) ++count2;
//		
////		System.out.println(s);
//		
////		++count;
//		
//		if (ageHistogram.containsKey(a)) {
//			int f = ageHistogram.get(a);
//			ageHistogram.put(a, f + 1);
//		}
//		else {
//			ageHistogram.put(a, 1);
//		}
//	}
//	
//	System.out.println(count1 + "\t" + count2);
//	
////	in percentage
////	for (int i: ageHistogram.keySet()) {
////		System.out.println(i + "\t" + ageHistogram.get(i) * 100.0 / count);
////	}
//}
	
//	transient and stable distribution with location
	public void getLocationVsTransientStable() { // fig:loc-vs-stable & fig:loc-vs-transient
		Map<Double, Integer> locationVsNumNodesWithAgeX = new TreeMap();
		Map<Double, Integer> locationFrequency = new HashMap(); // for percentage
		
		for (String s: birthVersion.keySet()) {
			int persistence = functionPersistence.get(s);
			double location = avgLocation.get(s);
			
			if (locationFrequency.containsKey(location)) {
				int v = locationFrequency.get(location);
				locationFrequency.put(location, v + 1);
			}
			else {
				locationFrequency.put(location, 1);
			}
			
			if (persistence < 38) continue; // get the stable nodes
//			if (persistence > 2) continue; // get the transient nodes
			
			if (locationVsNumNodesWithAgeX.containsKey(location)) {
				int v = locationVsNumNodesWithAgeX.get(location);
				locationVsNumNodesWithAgeX.put(location, v + 1);
			}
			else {
				locationVsNumNodesWithAgeX.put(location, 1);
			}
		}
		
		for (double d: locationVsNumNodesWithAgeX.keySet()) {
			System.out.println(d + "\t" + locationVsNumNodesWithAgeX.get(d) * 100.0 / locationFrequency.get(d));
		}
	}
	
	public void getLocationDispersion() {
		Map<Double, Integer> locationDispersionCount = new TreeMap();
		Map<Double, Integer> locationFrequency = new TreeMap();
		Map<Double, Double> locationAverageDispersion = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			double bLocation = birthLocation.get(s);
			double rLocation = lastLocation.get(s); 
			if (bLocation != rLocation) {
				double dispersion = Math.abs(bLocation - rLocation);
				if (locationDispersionCount.containsKey(bLocation)) {
					int v = locationDispersionCount.get(bLocation);
					locationDispersionCount.put(bLocation, v + 1);
					
					double w = locationAverageDispersion.get(bLocation);
					locationAverageDispersion.put(bLocation, w + dispersion);
				}
				else {
					locationDispersionCount.put(bLocation, 1);
					locationAverageDispersion.put(bLocation, dispersion);
				}
			}
			
			if (locationFrequency.containsKey(bLocation)) {
				int v = locationFrequency.get(bLocation);
				locationFrequency.put(bLocation, v + 1);
			}
			else locationFrequency.put(bLocation, 1);		
		}
		
		for (double d: locationDispersionCount.keySet()) {
//			System.out.println(d + "\t" + locationDispersionCount.get(d) * 100.0 / locationFrequency.get(d));			
			System.out.println(d + "\t" + locationAverageDispersion.get(d) / locationDispersionCount.get(d));
		}
	}
	
	// mean separated clusters 
	public void getClusterPersistencePercentiles() throws Exception { // fig:cluster-lifespan, fig:cluster-persistence
		PrintWriter pwts = new PrintWriter(new File("Results//" + Driver.networkUsed + "-cluster-transient-stable.txt"));
		PrintWriter pwls = new PrintWriter(new File("Results//" + Driver.networkUsed + "-cluster-life-span.txt"));
		
		List<Integer> persistenceGC = new ArrayList();
		List<Integer> persistencegC = new ArrayList();
		List<Integer> persistencegc = new ArrayList();
		List<Integer> persistenceGc = new ArrayList();
		
		double sGC = 0, sgC = 0, sgc = 0, sGc = 0; // stable node counters
		double tGC = 0, tgC = 0, tgc = 0, tGc = 0; // transient node counters
		
//		CHANGE FOR DIFFERENT NETWORKS
		int transientAge = 3;
		int stableAge = 38;
		
		double generalitySeparator, complexitySeparator;
		double gS = 0, cS = 0;
		for (String s: birthVersion.keySet()) {
			gS += avgGenerality.get(s); //
			cS += avgComplexity.get(s);
		}
		generalitySeparator = gS / birthVersion.size();
		complexitySeparator = cS / birthVersion.size();
		
		for (String s: birthVersion.keySet()) {			
			int persistence = functionPersistence.get(s);
			double m = avgLocation.get(s);
			double g = avgGenerality.get(s); // ? are you sure
			double c = avgComplexity.get(s);
			
			if (g > generalitySeparator && c > complexitySeparator) { 
				persistenceGC.add(persistence);
				if (persistence >= stableAge) ++sGC;
				else if (persistence <= transientAge) ++tGC;				 
			}
			else if (g <= generalitySeparator && c > complexitySeparator) { 
				persistencegC.add(persistence); 
				if (persistence >= stableAge) ++sgC;
				else if (persistence <= transientAge) ++tgC;				 
			}
			else if (g <= generalitySeparator && c <= complexitySeparator) { 
				persistencegc.add(persistence);
				if (persistence >= stableAge) ++sgc;
				else if (persistence <= transientAge) ++tgc;				 
			}
			else if (g > generalitySeparator && c <= complexitySeparator) { 
				persistenceGc.add(persistence);
				if (persistence >= stableAge) ++sGc;
				else if (persistence <= transientAge) ++tGc;				 
			}
		}
		
		// percentage of persistent nodes
		pwts.println((sGC * 100.0 / persistenceGC.size()) + "\t" + (tGC * 100.0 / persistenceGC.size()));
		pwts.println((sgC * 100.0 / persistencegC.size()) + "\t" + (tgC * 100.0 / persistencegC.size()));
		pwts.println((sgc * 100.0 / persistencegc.size()) + "\t" + (tgc * 100.0 / persistencegc.size()));
		pwts.println((sGc * 100.0 / persistenceGc.size()) + "\t" + (tGc * 100.0 / persistenceGc.size()));
		
		// persistence percentiles
		Object a[] = persistenceGC.toArray();
		getPercentiles("GC", a, pwls);
		a = persistencegC.toArray();
		getPercentiles("gC", a, pwls);
		a = persistencegc.toArray();
		getPercentiles("gc", a, pwls);
		a = persistenceGc.toArray();
		getPercentiles("Gc", a, pwls);
		
		pwts.close();
		pwls.close();
	}
	
	void getPercentiles(String id, Object a[], PrintWriter pw) {
		double b[] = new double[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = (int)a[i];
		}
		double q1 = StatUtils.percentile(b, 25.0);
		double qm = StatUtils.percentile(b, 50.0);
		double q3 = StatUtils.percentile(b, 75.0);
		pw.println(id + "\t" + (int)q1 + "\t" + (int)qm + "\t" + (int)q3);
	}
	
	public void getLocationVsPersistencePercentiles() throws Exception { // fig:loc-vs-evo-age
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-loc-vs-evo-persistence.txt"));
		Map<Double, List<Integer>> locationPersistenceMap = new TreeMap();
		
		for (String s: birthVersion.keySet()) {			
			int persistence = functionPersistence.get(s);
			double avgLoc = avgLocation.get(s);
			
			if (locationPersistenceMap.containsKey(avgLoc)) {
				locationPersistenceMap.get(avgLoc).add(persistence);
			}
			else {
				List<Integer> list = new ArrayList();
				list.add(persistence);
				locationPersistenceMap.put(avgLoc, list);
			}
		}
		
		for (double d: locationPersistenceMap.keySet()) {
			Object a[] = locationPersistenceMap.get(d).toArray();
			double b[] = new double[a.length];
			for (int i = 0; i < a.length; ++i) {
				b[i] = (Integer)a[i];
			}
			double q1 = StatUtils.percentile(b, 25.0);
			double qm = StatUtils.percentile(b, 50.0);
			double q3 = StatUtils.percentile(b, 75.0);
			pw.println(d + "\t" + q1 + "\t" + qm + "\t" + q3);
		}
		pw.close();
	}
	
	public void getLocationVsAvgGeneralityDelta() throws Exception {
		PrintWriter pw = new PrintWriter(new File("Results//" + Driver.networkUsed + "-loc-vs-avg-delta-gen.txt"));
		Map<Double, Double> locVsAvGenDelta = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			double l = avgLocation.get(s);
			double dg = lastGenerality.get(s) - birthGenerality.get(s); 
			if (locVsAvGenDelta.containsKey(l)) {
				double newDG = (dg + locVsAvGenDelta.get(l)) * 0.5; 
				locVsAvGenDelta.put(l, newDG);
			}
			else {
				locVsAvGenDelta.put(l, dg);
			}
		}
		
		for (Double d: locVsAvGenDelta.keySet()) {
			pw.println(d + "\t" + locVsAvGenDelta.get(d));
		}		
		pw.close();
	}
}