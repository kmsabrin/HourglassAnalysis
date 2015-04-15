package Remodeled;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.analysis.solvers.PolynomialSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class StatMathUtilTest {
	
	static public void regressionTest() {		
//		double[] x = new double[]{1.47,1.50,1.52,1.55,1.57,1.60,1.63,1.65,1.68,1.70,1.73,1.75,1.78,1.80,1.83};
//		double[] y = new double[]{52.21,53.12,54.48,55.84,57.20,58.57,59.93,61.29,63.11,64.47,66.28,68.10,69.92,72.19,74.46};

//		static double[] x = new double[]{0, -1, -1, -2, -2};
//		static double[] y = new double[]{1, 0.66, 0.33, 0.33, 0.33};
		
		double[] x = new double[]{0, -1, -2, -3, -4};
		double[] y = new double[]{4, 3, 2, 1, 0};

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
	
	static public void polynomialSolverTest() {
		LaguerreSolver laguerreSolver = new LaguerreSolver();
		 
		int aSz = 6; // (nLayer + 1) / 2 + 1
		double coefficients[] = new double[aSz];
		coefficients[0] = 99; // (N / (2 * W)) - 1
		coefficients[1] = -100; // (N / (2 * W))
		coefficients[aSz - 1] = 1;
		
//		double coefficients[] = new double[]{-5.5, 2, 2};
//		double coefficients[] = new double[]{-6, -1, 1}; // x^2 - x - 6 = 0
		
//		Complex complex2 = laguerreSolver.solveComplex(coefficients, 1);
//		System.out.println(complex2);

		Complex complex[] = laguerreSolver.solveAllComplex(coefficients, 1);
		for (Complex c: complex) {
			System.out.println(c);
		}
		
//		System.out.println(laguerreSolver.doSolve());
	}
}
