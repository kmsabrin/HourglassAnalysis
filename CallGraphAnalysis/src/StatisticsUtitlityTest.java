import org.apache.commons.math3.stat.regression.SimpleRegression;


public class StatisticsUtitlityTest {

//	static double[] x = new double[]{1.47,1.50,1.52,1.55,1.57,1.60,1.63,1.65,1.68,1.70,1.73,1.75,1.78,1.80,1.83};
//	static double[] y = new double[]{52.21,53.12,54.48,55.84,57.20,58.57,59.93,61.29,63.11,64.47,66.28,68.10,69.92,72.19,74.46};

//	static double[] x = new double[]{0, -1, -1, -2, -2};
//	static double[] y = new double[]{1, 0.66, 0.33, 0.33, 0.33};
	
	static double[] x = new double[]{0, -1, -2, -3, -4};
	static double[] y = new double[]{4, 3, 2, 1, 0};

	public static void main(String[] args) {
		
		SimpleRegression simpleRegression = new SimpleRegression();
		
		for (int i = 0; i < x.length; ++i) {
			simpleRegression.addData(x[i], y[i]);
		}
		
//		System.out.println("Intercept: " + simpleRegression.getIntercept());
//		System.out.println("Intercept: " + simpleRegression.getInterceptStdErr());
		
		System.out.println(simpleRegression.getR());
		
		System.out.println("Slope: " + simpleRegression.getSlope());
		System.out.println(simpleRegression.getSlopeConfidenceInterval());
		System.out.println(simpleRegression.getInterceptStdErr());
		
	}

}
