package Remodeled;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

public class StandardDistributions {
	
//	static ExponentialDistribution exponentialDistribution;
//	static LogNormalDistribution logNormalDistribution;
//	static ParetoDistribution paretoDistribution;
//	static NormalDistribution normalDistribution;
	
	public static void getCCDF(AbstractRealDistribution distribution) {
		for (double i = 0.0; ; i += 0.02) {
			double ccdf = 1.0 - distribution.cumulativeProbability(i);
			System.out.println(i + "\t" + (1.0 - distribution.cumulativeProbability(i)));
			if (ccdf < 0.005) {
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		getCCDF(new ExponentialDistribution(0.5));
//		getCCDF(new LogNormalDistribution());
//		getCCDF(new NormalDistribution(4.0, 1.0));
//		getCCDF(new ParetoDistribution());
		
	}
}
