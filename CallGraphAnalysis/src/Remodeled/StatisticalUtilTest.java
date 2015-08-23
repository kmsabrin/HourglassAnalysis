package Remodeled;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class StatisticalUtilTest {
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
		
//		tryWilcoxonRankSumTest();
		
//		System.out.println(Math.log(0.13));
//		System.out.println(Math.exp(-2.0402208285265546));
		
//		Random random = new Random(System.nanoTime());
//		
//		int i = 20;
//		while (i-- > 0) {
//			System.out.println(random.nextDouble());
//		}
		
		int n = 100;
		ZipfDistribution zipfDistribution = new ZipfDistribution(10, 1.0);
		for (int i = 1; i <= n; ++i) {
//			System.out.println(i + "\t" + zipfDistribution.probability(n - i + 1));
			System.out.println(zipfDistribution.sample());
		}
//		
//		System.out.println("----------");
//		
//		int n = 1;
//		UniformIntegerDistribution uniformIntegerDistribution = new UniformIntegerDistribution(1, n + 1);
//		for (int i = 1; i <= n; ++i) {
//			System.out.println(i + "\t" + uniformIntegerDistribution.probability(i));
//		}

//		NormalDistribution normalDistribution = new NormalDistribution(4, 1);
//		
//		for (int i = 1; i < 1000; ++i) {
//			System.out.println(normalDistribution.sample());
//		}
		
//		NormalDistribution normalDistribution = new NormalDistribution(4, 1);
//		
//		for (int i = 1; i < 1000; ++i) {
//			System.out.println(normalDistribution.sample());
//		}
	}
}
