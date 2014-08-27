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
	Map<String, Integer> mostRecentVersion;
	Map<String, Double> birthLocation;
	Map<String, Double> mostRecentLocation;
	Map<String, Double> mostRecentComplexity;
	Map<String, Double> mostRecentGenerality;
		
	AgeAnalysis() {
		birthVersion = new HashMap();
		mostRecentVersion = new HashMap();
		birthLocation = new HashMap();
		mostRecentLocation = new HashMap();
		mostRecentComplexity = new HashMap();
		mostRecentGenerality = new HashMap();

		// be careful with versions used for calculation check the hard codes (i.e 40) for number of versions used
		Set<String> totalFunction = new HashSet();
		Set<String> rebornFunction = new HashSet();
		for (int i = 0; i < 40; ++i) {
			CallDAG callDAG = new CallDAG("callGraphs//full.graph-2.6." + i);
			
//			int q1 = 0, q2 = 0, q3 = 0, q4 = 0;
			
			for (String s: callDAG.functions) {
				if (!birthVersion.containsKey(s)) {
					birthVersion.put(s, i);
					birthLocation.put(s, callDAG.location.get(s));					
				}
					
//				check for death followed by rebirth
				totalFunction.add(s);
//				if (mostRecentVersion.containsKey(s) && mostRecentVersion.get(s) != i - 1) {
//					System.out.println("Function Name: " + s 
//							+ " Death Version: " + mostRecentVersion.get(s) + " Rebirth Version: " + i);
//					rebornFunction.add(s);
//				}
				
				mostRecentVersion.put(s, i);
				mostRecentLocation.put(s, callDAG.location.get(s));
				mostRecentComplexity.put(s, callDAG.complexity.get(s));
				mostRecentGenerality.put(s, callDAG.generality.get(s));
				
//				double m = mostRecentLocation.get(s);
//				double g = mostRecentGenerality.get(s);
//				double c = mostRecentComplexity.get(s);
//				
//				if (g > 0.250 && c > 0.090) { ++q1; continue; }
//				if (g < 0.250 && c > 0.055) { ++q2; continue; }
//				if (g < 0.250 && c < 0.055) { ++q3; continue; }
//				if (g > 0.250 && c < 0.090) { ++q4; continue; }
			}
			
//			System.out.println("version_" + i + "\t" + q1 + "\t" + q2 + "\t" + q3 + "\t" + q4);
		}
		
//		System.out.println("nFunctions: " + totalFunction.size() + " nRebornFunctions: " + rebornFunction.size());
	}
		
	public void getAgeHistogram() {
		int ages[] = new int[50];
		int cumulativeHistogram[] = new int[50];
			
		for (String s: birthVersion.keySet()) {
			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
//			if (age >= 39 )
				ages[age]++;
		}		
		
//		for (int i = 1; i < 31; ++i) {
//			System.out.println(i +"\t" + ages[i] * 100.0 / birthVersion.size());
//		}
		
		for (int i = 39; i >= 0; --i) {
			cumulativeHistogram[i] = cumulativeHistogram[i + 1] + ages[i + 1];
			System.out.println(i + "\t" + cumulativeHistogram[i]);
		}
	}
	
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
	
	public void getLastLocationVSDeathPercentage() {
		Map<Double, Integer> locationVSDeathFrequency = new TreeMap();
		Map<Double, Integer> locationFrequency = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			double location = mostRecentLocation.get(s); 
			if (mostRecentVersion.get(s) < 29) {
				if (locationVSDeathFrequency.containsKey(location)) {
					int v = locationVSDeathFrequency.get(location);
					locationVSDeathFrequency.put(location, v + 1);
				}
				else locationVSDeathFrequency.put(location, 1);
			}
			
			if (locationFrequency.containsKey(location)) {
				int v = locationFrequency.get(location);
				locationFrequency.put(location, v + 1);
			}
			else locationFrequency.put(location, 1);		
		}
		
		System.out.println("Last Location VS Death Percentage");
		for (Double d: locationVSDeathFrequency.keySet()) {
			System.out.println(d + "\t" + locationVSDeathFrequency.get(d) * 100.0 / locationFrequency.get(d));
		}
	}
	
	public void getLastLocationVSPersistency() {
		Map<Double, Integer> locationVSAliveFrequency = new TreeMap();
		Map<Double, Integer> locationFrequency = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			double location = mostRecentLocation.get(s); 
			if (mostRecentVersion.get(s) >= 29) {
				if (locationVSAliveFrequency.containsKey(location)) {
					int v = locationVSAliveFrequency.get(location);
					locationVSAliveFrequency.put(location, v + 1);
				}
				else locationVSAliveFrequency.put(location, 1);
			}
			
			if (locationFrequency.containsKey(location)) {
				int v = locationFrequency.get(location);
				locationFrequency.put(location, v + 1);
			}
			else locationFrequency.put(location, 1);		
		}
		
		System.out.println("Last Location VS Alive Percentage");
		for (Double d: locationVSAliveFrequency.keySet()) {
			System.out.println(d + "\t" + locationVSAliveFrequency.get(d) * 100.0 / locationFrequency.get(d));
		}
	}
	
	public void getAgeVSDeathPercentage() {
		int deathFrequencey[] = new int[50];
		int nDeadNodes = 0;
			
		for (String s: birthVersion.keySet()) {
			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
			/*##########################################################*/
			if (mostRecentVersion.get(s) < 29) { // hard code
				++nDeadNodes;
				deathFrequencey[age]++;
			}
		}
		
		System.out.println("Age VS Death Percentage");
		for (int i = 1; i < 31; ++i) {
			System.out.println(i + "\t" + deathFrequencey[i] * 100.0 / nDeadNodes);
		}
	}
	
	public void getLastLocationVSAverageAge() {
		Map<Double, List<Integer>> locationVSAges = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			if (mostRecentVersion.get(s) < 29) continue; // consider live nodes only
			
			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
			double location = mostRecentLocation.get(s);
			if (locationVSAges.containsKey(location)) {
				locationVSAges.get(location).add(age);
			}
			else {
				List<Integer> ages = new ArrayList();
				ages.add(age);
				locationVSAges.put(location, ages);
			}
		}
		
		for (double d: locationVSAges.keySet()) {
//			AVERAGE
			double sum = 0;
			for (int i: locationVSAges.get(d)) {
				sum += i;
			}
			double avg = sum / locationVSAges.get(d).size();
			System.out.println(d + "\t" + avg);
			
//			MEDIAN
//			List<Integer> ages = locationVSAges.get(d);
//			Collections.sort(ages);
//			double median = ages.get(ages.size() / 2);
//			System.out.println(d + "\t" + median);
		}
	}
	
	public void getAgeVSLastLocation() {
		Map<Double, Integer> locationAgeMap = new HashMap();
		for (String s: birthVersion.keySet()) {
			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
			double location = mostRecentLocation.get(s);
			// hard code
			if (mostRecentVersion.get(s) >= 29 && age > 20) { 
				System.out.println(age + "\t" + location);				
			}	
		}
	}
	
	// transient and stable distribution
	// extreme life-span distribution with location
	public void getLocationVSNumNodesWithAgeX() { // fig:loc-vs-stable & fig:loc-vs-transient
		Map<Double, Integer> locationVsNumNodesWithAgeX = new TreeMap();
		Map<Double, Integer> locationFrequency = new HashMap(); // for percentage
		
		for (String s: birthVersion.keySet()) {
			int age = mostRecentVersion.get(s) - birthVersion.get(s) + 1;
			double location = mostRecentLocation.get(s);
			
			if (locationFrequency.containsKey(location)) {
				int v = locationFrequency.get(location);
				locationFrequency.put(location, v + 1);
			}
			else locationFrequency.put(location, 1);
			
//			if (age > 1 ) continue; // get the min aged nodes
//			if (age < 40) continue; // get the max aged nodes
			
//			if (age < 35) continue; // get the stable nodes
			if (age > 5) continue; // get the transient nodes
			
//			System.out.println(location);
			
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
	
	public void getlocationDispersion() {
		Map<Double, Integer> locationDispersionCount = new TreeMap();
		Map<Double, Integer> locationFrequency = new TreeMap();
		Map<Double, Double> locationAverageDispersion = new TreeMap();
		
		for (String s: birthVersion.keySet()) {
			double bLocation = birthLocation.get(s);
			double rLocation = mostRecentLocation.get(s); 
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
	
	public void getClusterAgeDistribution() { // visually separated clusters
		double count1 = 0;
		double count2 = 0;
		
		Map<Integer, Integer> ageHistogram = new TreeMap();
		for (String s: birthVersion.keySet()) {			
			int a = mostRecentVersion.get(s) - birthVersion.get(s) + 1; // age
//			if (a < 40) continue; // consider live nodes only

			double m = mostRecentLocation.get(s);
			double g = mostRecentGenerality.get(s);
			double c = mostRecentComplexity.get(s);
			
			if (g > 0.25) continue;
			if (c > 0.05) continue;
			
			if (mostRecentVersion.get(s) >= 29) ++count1;
			if (a == 40) ++count2;
			
//			System.out.println(s);
			
//			++count;
			
			if (ageHistogram.containsKey(a)) {
				int f = ageHistogram.get(a);
				ageHistogram.put(a, f + 1);
			}
			else {
				ageHistogram.put(a, 1);
			}
		}
		
		System.out.println(count1 + "\t" + count2);
		
//		in percentage
//		for (int i: ageHistogram.keySet()) {
//			System.out.println(i + "\t" + ageHistogram.get(i) * 100.0 / count);
//		}
	}
	
	// done manually
	// visually separated clusters
	public void getClusterLifeTimeDistribution() { // fig:clusters-lifespan, fig:clusters-persistence
		List<Integer> lifeTimeListQuad1 = new ArrayList();
		List<Integer> lifeTimeListQuad2 = new ArrayList();
		List<Integer> lifeTimeListQuad3 = new ArrayList();
		List<Integer> lifeTimeListQuad4 = new ArrayList();
		
		double p1 = 0, p2 = 0, p3 = 0, p4 = 0;
		
		for (String s: birthVersion.keySet()) {			
			int lifeTime = mostRecentVersion.get(s) - birthVersion.get(s) + 1; // age
			double m = mostRecentLocation.get(s);
			double g = mostRecentGenerality.get(s);
			double c = mostRecentComplexity.get(s);
			
			if (g > 0.250 && c > 0.090) { 
				lifeTimeListQuad1.add(lifeTime);
				if (lifeTime > 39) ++p1;
				continue; 
			}
			
			if (g < 0.250 && c > 0.055) { 
				lifeTimeListQuad2.add(lifeTime); 
				if (lifeTime > 39) ++p2;
				continue; 
			}
			
			if (g < 0.250 && c < 0.055) { 
				lifeTimeListQuad3.add(lifeTime);
				if (lifeTime > 39) ++p3;
				continue; 
			}
			
			if (g > 0.250 && c < 0.090) { 
				lifeTimeListQuad4.add(lifeTime);
				if (lifeTime > 39) ++p4;
				continue; 
			}
		}
		
		// percentage of persistent nodes
		System.out.println(p1 * 100.0 / lifeTimeListQuad1.size());
		System.out.println(p2 * 100.0 / lifeTimeListQuad2.size());
		System.out.println(p3 * 100.0 / lifeTimeListQuad3.size());
		System.out.println(p4 * 100.0 / lifeTimeListQuad4.size());
		
		
		/*
		Object a[] = lifeTimeListQuad1.toArray();
		getPercentiles("Quad 1", a);
		a = lifeTimeListQuad2.toArray();
		getPercentiles("Quad 2", a);
		a = lifeTimeListQuad3.toArray();
		getPercentiles("Quad 3", a);
		a = lifeTimeListQuad4.toArray();
		getPercentiles("Quad 4", a);
		*/
	}
	
	void getPercentiles(String id, Object a[]) {
		double b[] = new double[a.length];
		for (int i = 0; i < a.length; ++i) {
			b[i] = (int)a[i];
		}
		double q1 = StatUtils.percentile(b, 25.0);
		double qm = StatUtils.percentile(b, 50.0);
		double q3 = StatUtils.percentile(b, 75.0);
		System.out.println(id + "\t" + q1 + "\t" + qm + "\t" + q3);
	}
	
	public void getLocationLifeTimeDistribution() { // fig:loc-vs-evo-age
		Map<Double, List<Integer>> lifeSpanLocationMap = new TreeMap();
		
		for (String s: birthVersion.keySet()) {			
			int lifeTime = mostRecentVersion.get(s) - birthVersion.get(s) + 1; // age (life-span)
			double m = mostRecentLocation.get(s);
			
			if (lifeSpanLocationMap.containsKey(m)) {
				lifeSpanLocationMap.get(m).add(lifeTime);
			}
			else {
				List<Integer> list = new ArrayList();
				list.add(lifeTime);
				lifeSpanLocationMap.put(m, list);
			}
		}
		
		for (double d: lifeSpanLocationMap.keySet()) {
			Object a[] = lifeSpanLocationMap.get(d).toArray();
			getPercentiles("Location " + d, a);
		}
	}
}


