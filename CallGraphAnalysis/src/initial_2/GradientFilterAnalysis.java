package initial_2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import utilityhg.DistributionAnalysis;
import corehg.DependencyDAG;

public class GradientFilterAnalysis {
	TreeMap<Double, Double> ccdfMap;
	
	double xList[];
	
	double bodyStartX;
	double bodyEndX;
	double tailStartX;
	double tailEndX;

	double[] bodyXPoints;
	double[] tailXPoints;
	
	int nSample = 40; // 50?
	double bodyGradients[];
	double tailGradients[];

	private void getPercentileRangeForBodyAndTail(DependencyDAG dependencyDAG, String networkID) throws Exception {
		ccdfMap = DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID, 1);		
		HashMap<Double, Double> ccdfReverseMapLogScale = new HashMap();
		xList = new double[ccdfMap.size() - 1]; // one entry less for the log(O) one
		double[] yList = new double[ccdfMap.size() - 1];
		int index = 0;
		for (double k: ccdfMap.keySet()) {
			double v = ccdfMap.get(k);
			if (v > 0) {
				xList[index] = k;
				yList[index] = Math.log(v);
				++index;
				ccdfReverseMapLogScale.put(Math.log(v), k);		
			}
			
//			System.out.println(k + "\t" + v + "\t" + Math.log(v));
		}
		
		Arrays.sort(xList);
		Arrays.sort(yList);
		bodyStartX = xList[0];
		double bodyEndLogY = yList[(int)(yList.length * 0.1)];
		bodyEndX = ccdfReverseMapLogScale.get(bodyEndLogY);
		double tailStartLogY = yList[(int)(yList.length * 0.01)];
		tailStartX = ccdfReverseMapLogScale.get(tailStartLogY);
		tailEndX = xList[xList.length - 1];
		System.out.println("Body-Tail spectrum: " + bodyStartX + "\t" + bodyEndX + "\t" + tailStartX + "\t" + tailEndX);
	}
	
	private double getGradient(double x1, double y1, double x2, double y2) {
		
//		y1 = (y1 > 0) ? Math.log(y1) : 0;
//		y2 = (y2 > 0) ? Math.log(y2) : 0;
		
		y1 = Math.log(y1);
		y2 = Math.log(y2);
		
//		System.out.print(x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
//		System.out.println("\t" + (y2 - y1) / (x2 - x1));
		
		return (y2 - y1) / (x2 - x1);
	}
	
	private double closestMatch(double[] a, double v) {
		double minDiff = 1.1;
		double closestVal = -1;
		
		for (double d: a) {
			if (Math.abs(d - v) < minDiff) {
				minDiff = Math.abs(d - v);
				closestVal = d;
			}
		}
		
		return closestVal;
	}
	
	private void getPairWiseGradients() {
		int p = 0, q = 0;
		double epsilon = 10e-6;
		bodyGradients = new double[nSample * (nSample + 1) / 2];
		tailGradients = new double[nSample * (nSample + 1) / 2];
		for (int i = 0; i < nSample; ++i) {
			for (int j = i + 1; j < nSample; ++j) {
				if (Math.abs(bodyXPoints[i] - bodyXPoints[j]) < epsilon) ; // skip random repeats
				else bodyGradients[p++] = getGradient(bodyXPoints[i], ccdfMap.get(bodyXPoints[i]), bodyXPoints[j], ccdfMap.get(bodyXPoints[j]));	
		
				if (Math.abs(tailXPoints[i] - tailXPoints[j]) < epsilon) ;  // skip random repeats
				else tailGradients[q++] = getGradient(tailXPoints[i], ccdfMap.get(tailXPoints[i]), tailXPoints[j], ccdfMap.get(tailXPoints[j]));					
			}
		}
	}
	
	private void getSampleGradientsWithPoints(DependencyDAG dependencyDAG, String networkID) throws Exception {		
		getPercentileRangeForBodyAndTail(dependencyDAG, networkID);
		
		Random random = new Random(System.nanoTime());
		bodyXPoints = new double[nSample];
		tailXPoints = new double[nSample];
		for (int i = 0; i < nSample; ++i) {
			int bodyEndXIndex = Arrays.binarySearch(xList, bodyEndX);
			bodyXPoints[i] = xList[random.nextInt(bodyEndXIndex + 1)];
			
			int tailStartXIndex = Arrays.binarySearch(xList, tailStartX);
			tailXPoints[i] = xList[tailStartXIndex + random.nextInt(xList.length - tailStartXIndex)];
		}
		
		getPairWiseGradients();
	}
	
	private void getSampleGradientsWithRange(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getPercentileRangeForBodyAndTail(dependencyDAG, networkID);
		
		Random random = new Random(System.nanoTime());
		bodyXPoints = new double[nSample];
		tailXPoints = new double[nSample];
		for (int i = 0; i < nSample; ++i) {
			double randomSample = random.nextDouble() * (bodyEndX - bodyStartX) + bodyStartX;
			bodyXPoints[i] = closestMatch(xList, randomSample);
			
			randomSample = random.nextDouble() * (tailEndX - tailStartX) + tailStartX;
			tailXPoints[i] = closestMatch(xList, randomSample);
			
//			System.out.println(bodyXPoints[i] + "\t" + tailXPoints[i]);
		}
		
		getPairWiseGradients();
	}
		
	public void getSampleGradientsQuartileInterval(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getSampleGradientsWithPoints(dependencyDAG, networkID);
//		getSampleGradientsWithRange(dependencyDAG, networkID);

//		for (int i = 0; i < bodyGradients.length; ++i) System.out.print(bodyGradients[i] + "\t"); System.out.println();
//		for (int i = 0; i < tailGradients.length; ++i) System.out.print(tailGradients[i] + "\t"); System.out.println();

		double bodyQ1 = StatUtils.percentile(bodyGradients, 25.0);
		double bodyQ3 = StatUtils.percentile(bodyGradients, 75.0);
		
		double tailQ1 = StatUtils.percentile(tailGradients, 25.0);
		double tailQ3 = StatUtils.percentile(tailGradients, 75.0);
		
		System.out.println("Body-Tail Interquartile Range: [" + bodyQ1 + " , " + bodyQ3 + "]\t[" + tailQ1 + "\t" + tailQ3 + "]");
	}

	public void getWilcoxonRankSum(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getSampleGradientsWithPoints(dependencyDAG, networkID);

//		for (int i = 0; i < headGradientSample.length; ++i) System.out.print(headGradientSample[i] + "\t"); System.out.println();
//		for (int i = 0; i < tailGradientSample.length; ++i) System.out.print(tailGradientSample[i] + "\t"); System.out.println();
		
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		System.out.println(mannWhitneyUTest.mannWhitneyU(bodyGradients, tailGradients));
		System.out.println(mannWhitneyUTest.mannWhitneyUTest(bodyGradients, tailGradients) * 0.5);		
	}

}
