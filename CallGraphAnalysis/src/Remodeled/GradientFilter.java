package Remodeled;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class GradientFilter {
	int nSample = 5;
	double headGradientSample[];
	double tailGradientSample[];
	
	private double getGradient(double x1, double y1, double x2, double y2) {
		
//		y1 = (y1 > 0) ? Math.log(y1) : 0;
//		y2 = (y2 > 0) ? Math.log(y2) : 0;
		
		y1 = Math.log(y1);
		y2 = Math.log(y2);
		
//		System.out.println(x1 + "\t" + y1 + "\t" + x2 + "\t" + y2);
//		System.out.println((y2 - y1) / (x2 - x1));
		
		return (y2 - y1) / (x2 - x1);
	}
	
	private void getSampleGradients(DependencyDAG dependencyDAG, String networkID) throws Exception {
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
		headGradientSample = new double[nSample * (nSample + 1) / 2];
		tailGradientSample = new double[nSample * (nSample + 1) / 2];
		for (int i = 0; i < nSample; ++i) {
			for (int j = i + 1; j < nSample; ++j) {
				headGradientSample[k] = getGradient(leftXValues[i], ccdfMap.get(leftXValues[i]), leftXValues[j], ccdfMap.get(leftXValues[j]));	
				tailGradientSample[k] = getGradient(rightXValues[i], ccdfMap.get(rightXValues[i]), rightXValues[j], ccdfMap.get(rightXValues[j]));					
//				System.out.println(headGradientSample[k] + "\t" + tailGradientSample[k]);
				++k;
			}
		}
	}
	
	public void getWilcoxonRankSum(DependencyDAG dependencyDAG, String networkID) throws Exception {
		getSampleGradients(dependencyDAG, networkID);

		for (int i = 0; i < headGradientSample.length; ++i) System.out.print(headGradientSample[i] + "\t"); System.out.println();
		for (int i = 0; i < tailGradientSample.length; ++i) System.out.print(tailGradientSample[i] + "\t"); System.out.println();
		
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		System.out.println(mannWhitneyUTest.mannWhitneyU(headGradientSample, tailGradientSample));
		System.out.println(mannWhitneyUTest.mannWhitneyUTest(headGradientSample, tailGradientSample) * 0.5);		
	}
}
