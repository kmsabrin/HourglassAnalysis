package Remodeled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class GradientFilterAnalysis {
	int nSample = 20; // 50?
	double bodyGradients[];
	double tailGradients[];
	
	private double getGradient(double x1, double y1, double x2, double y2) {
		
//		y1 = (y1 > 0) ? Math.log(y1) : 0;
//		y2 = (y2 > 0) ? Math.log(y2) : 0;
		
		y1 = Math.log(y1);
		y2 = Math.log(y2);
		
		System.out.print(x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
		System.out.println("\t" + (y2 - y1) / (x2 - x1));
		
		return (y2 - y1) / (x2 - x1);
	}
	
	private void getSampleGradients_1(DependencyDAG dependencyDAG, String networkID) throws Exception {
		TreeMap<Double, Double> ccdfMap = DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID);
		
		Random random = new Random(System.nanoTime());
		
		double leftXValues[] = new double[nSample];
		double rightXValues[] = new double[nSample];
		ArrayList<Double> xIndexList= new ArrayList(ccdfMap.keySet());
		for (int i = 0; i < nSample; ++i) {
			int randomIndex = random.nextInt(1000);
			leftXValues[i] = xIndexList.get(randomIndex);
			
			randomIndex = random.nextInt(50);
			rightXValues[i] = xIndexList.get(xIndexList.size() - 2 - randomIndex);
		}
		
		int k = 0;
		bodyGradients = new double[nSample * (nSample + 1) / 2];
		tailGradients = new double[nSample * (nSample + 1) / 2];
		for (int i = 0; i < nSample; ++i) {
			for (int j = i + 1; j < nSample; ++j) {
				bodyGradients[k] = getGradient(leftXValues[i], ccdfMap.get(leftXValues[i]), leftXValues[j], ccdfMap.get(leftXValues[j]));	
				tailGradients[k] = getGradient(rightXValues[i], ccdfMap.get(rightXValues[i]), rightXValues[j], ccdfMap.get(rightXValues[j]));					
//				System.out.println(headGradientSample[k] + "\t" + tailGradientSample[k]);
				++k;
			}
		}
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
	
	private void getSampleGradients_2(DependencyDAG dependencyDAG, String networkID) throws Exception {
		TreeMap<Double, Double> ccdfMap = DistributionAnalysis.getCentralityCCDF(dependencyDAG, networkID);		
		Random random = new Random(System.nanoTime());
		
		double[] centralityList = new double[ccdfMap.size()];
		int index = 0;
		for (double d: ccdfMap.keySet()) {
			centralityList[index++] = d;
		}
		// skip the last item for avoiding log(0)
		centralityList = Arrays.copyOf(centralityList, centralityList.length - 1);
		
		double bodyStart = centralityList[0];
		double bodyEnd = (StatUtils.percentile(centralityList, 90.0));
		double tailStart = (StatUtils.percentile(centralityList, 99.0));
		double tailEnd = centralityList[centralityList.length - 1];
		System.out.println("Body-Tail spectrum: " + bodyStart + "\t" + bodyEnd + "\t" + tailStart + "\t" + tailEnd);

		double bodyPoints[] = new double[nSample];
		double tailPoints[] = new double[nSample];
		for (int i = 0; i < nSample; ++i) {
			double randomSample = random.nextDouble() * (bodyEnd - bodyStart) + bodyStart;
			bodyPoints[i] = closestMatch(centralityList, randomSample);
			
			randomSample = random.nextDouble() * (tailEnd - tailStart) + tailStart;
			tailPoints[i] = closestMatch(centralityList, randomSample);
			
//			System.out.println(bodyPoints[i] + "\t" + tailPoints[i]);
		}
		
		int p = 0, q = 0;
		double epsilon = 10e-6;
		bodyGradients = new double[nSample * (nSample + 1) / 2];
		tailGradients = new double[nSample * (nSample + 1) / 2];
		for (int i = 0; i < nSample; ++i) {
			for (int j = i + 1; j < nSample; ++j) {
				if (Math.abs(bodyPoints[i] - bodyPoints[j]) < epsilon) ; // skip random repeats
				else bodyGradients[p++] = getGradient(bodyPoints[i], ccdfMap.get(bodyPoints[i]), bodyPoints[j], ccdfMap.get(bodyPoints[j]));	
		
				if (Math.abs(tailPoints[i] - tailPoints[j]) < epsilon) ;  // skip random repeats
				else tailGradients[q++] = getGradient(tailPoints[i], ccdfMap.get(tailPoints[i]), tailPoints[j], ccdfMap.get(tailPoints[j]));					
				
//				System.out.println(bodyGradients[p - 1] + "\t" + tailGradients[q - 1]);
			}
		}
	}
	
	public void getWilcoxonRankSum(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getSampleGradients_1(dependencyDAG, networkID);

//		for (int i = 0; i < headGradientSample.length; ++i) System.out.print(headGradientSample[i] + "\t"); System.out.println();
//		for (int i = 0; i < tailGradientSample.length; ++i) System.out.print(tailGradientSample[i] + "\t"); System.out.println();
		
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		System.out.println(mannWhitneyUTest.mannWhitneyU(bodyGradients, tailGradients));
		System.out.println(mannWhitneyUTest.mannWhitneyUTest(bodyGradients, tailGradients) * 0.5);		
	}
	
	public void getSampleGradientQuartileInterval(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getSampleGradients_2(dependencyDAG, networkID);

//		for (int i = 0; i < bodyGradients.length; ++i) System.out.print(bodyGradients[i] + "\t"); System.out.println();
//		for (int i = 0; i < tailGradients.length; ++i) System.out.print(tailGradients[i] + "\t"); System.out.println();

		double bodyQ1 = StatUtils.percentile(bodyGradients, 25.0);
		double bodyQ3 = StatUtils.percentile(bodyGradients, 75.0);
		
		double tailQ1 = StatUtils.percentile(tailGradients, 25.0);
		double tailQ3 = StatUtils.percentile(tailGradients, 75.0);
		
		System.out.println("Body-Tail Interquartile Range: [" + bodyQ1 + " , " + bodyQ3 + "]\t[" + tailQ1 + "\t" + tailQ3 + "]");
	}
}
