package utilityhg;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ConfidenceInterval {
	public static double getConfidenceInterval(double data[]) {
		// Build summary statistics of the dataset "data"
		SummaryStatistics stats = new SummaryStatistics();
		for (double val : data) {
			stats.addValue(val);
		}
		// Calculate 95% confidence interval
		double ci = calcMeanCI(stats, 0.95);
		// System.out.println(String.format("Mean: %f", stats.getMean()));
		double lower = stats.getMean() - ci;
		double upper = stats.getMean() + ci;
		// System.out.println(String.format("Confidence Interval 95%%: %f, %f",
		// lower, upper));
		return ci;
	}

	private static double calcMeanCI(SummaryStatistics stats, double level) {
		try {
			// Create T Distribution with N-1 degrees of freedom
			TDistribution tDist = new TDistribution(stats.getN() - 1);
			// Calculate critical value
			double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
			// Calculate confidence interval
//			System.out.println(critVal);
			return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
		} catch (MathIllegalArgumentException e) {
			return Double.NaN;
		}
	}
}
