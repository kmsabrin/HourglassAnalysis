package Remodeled;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class StatisticalTestbed {
	public static void getCCDF(AbstractRealDistribution distribution) {
		for (double i = 0.0; ; i += 0.02) {
			double ccdf = 1.0 - distribution.cumulativeProbability(i);
			System.out.println(i + "\t" + (1.0 - distribution.cumulativeProbability(i)));
			if (ccdf < 0.005) {
				break;
			}
		}
	}
	
	public static void tryWilcoxonRankSumTest() {
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		
		double x[] = {0.8, 0.83, 1.89, 1.04, 1.45, 1.38, 1.91, 1.64, 0.73, 1.46};
		double y[] = {1.15, 0.88, 0.90, 0.74, 1.21};
		
		System.out.println(mannWhitneyUTest.mannWhitneyU(x, y));
		System.out.println(mannWhitneyUTest.mannWhitneyUTest(x, y));		
	}
	
	
	
	public static void main(String[] args) {
//		getCCDF(new ExponentialDistribution(0.5));
//		getCCDF(new LogNormalDistribution());
//		getCCDF(new NormalDistribution(4.0, 1.0));
//		getCCDF(new ParetoDistribution());	
		
		tryWilcoxonRankSumTest();
	}
}