package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

public class Util {
	public static void getCCDF(AbstractRealDistribution distribution) {
		for (double i = 0.0;; i += 0.02) {
			double ccdf = 1.0 - distribution.cumulativeProbability(i);
			System.out.println(i + "\t"
					+ (1.0 - distribution.cumulativeProbability(i)));
			if (ccdf < 0.005) {
				break;
			}
		}
	}

	public static double getJaccardDistance(Set<String> a, Set<String> b) {
		HashSet<String> union = new HashSet();
		union.addAll(a);
		union.addAll(b);
		HashSet<String> intersection = new HashSet(a);
		intersection.retainAll(b);
//		System.out.println(a + "\t" + b + "\t" + (intersection.size() * 1.0 / union.size()));
		return intersection.size() * 1.0 / union.size();
	}
	
	public static double[] getMeanSTD(ArrayList<Double> numbers) {
		double n[] = new double[numbers.size()];

		for (int i = 0; i < numbers.size(); ++i) {
			n[i] = numbers.get(i);
		}

		double r[] = new double[2];
		r[0] = StatUtils.mean(n);
		r[1] = Math.sqrt(StatUtils.variance(n));
		return r;
	}

	public static void tryWilcoxonRankSumTest() {
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();

		double x[] = { 0.8, 0.83, 1.89, 1.04, 1.45, 1.38, 1.91, 1.64, 0.73,
				1.46 };
		double y[] = { 1.15, 0.88, 0.90, 0.74, 1.21 };

		System.out.println(mannWhitneyUTest.mannWhitneyU(x, y));
		System.out.println(mannWhitneyUTest.mannWhitneyUTest(x, y));
	}

	public static void extractJavaClassDependency() throws Exception {
		Scanner scanner = new Scanner(new File(
				"jdk_class_dependency//jetuml-dependency.txt"));
		// PrintWriter pw = new PrintWriter(new
		// File("jdk_class_dependency//jdk1.7-class-dependency.txt"));
		PrintWriter pw = new PrintWriter(new File(
				"jdk_class_dependency//jetuml-callgraph.txt"));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (!line.startsWith("M:"))
				continue;
			String tokens[] = line.split("\\s+");
			String dependent = tokens[0].substring(2);
			String server = tokens[1].substring(3);
			pw.println(server + "\t" + dependent);
		}
		pw.close();
	}

	public static void pointLineDistance(double x1, double y1, double x2,
			double y2, double x0, double y0) {
		double crossX = -1;
		double crossY = -1;
		;
		System.out.println(x1 + " " + y1 + " " + x2 + " " + y2);
		double distance = Math.abs((x2 - x1) * (y1 - y0) - (x1 - x0)
				* (y2 - y1))
				/ Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
		// double distance = Math.abs((y2 - y1) * x0 - (x2 - x1) * y0 + x2 * y1
		// - y2 * x1) / Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 -
		// x1));
		double d12 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		double u = ((x0 - x1) * (x2 - x1) + (y0 - y1) * (y2 - y1)) / d12;
		crossX = x1 + u * (x2 - x1);
		crossY = y1 + u * (y2 - y1);
		System.out.println("U: " + u);
		System.out.println("Distance " + distance + " at " + crossX + ","
				+ crossY);
		double dv = Math.sqrt((crossX - x0) * (crossX - x0) + (crossY - y0)
				* (crossY - y0));
		System.out.println("Verify " + dv);
		double s1 = ((y2 - y1) / (x2 - x1)) * ((crossY - y0) / (crossX - x0));
		System.out.println(-1 / s1);
	}

	public static void main(String[] args) throws Exception {
		// pointLineDistance(1, 1, 5, 5, 3, 5);
		// pointLineDistance(1, 0.608621667612025, 302, 0.9659671015314805, 30,
		// 0.9381735677821894);

		// getCCDF(new ExponentialDistribution(0.5));
		// getCCDF(new LogNormalDistribution());
		// getCCDF(new NormalDistribution(4.0, 1.0));
		// getCCDF(new ParetoDistribution());

		// tryWilcoxonRankSumTest();

		// System.out.println(Math.log(0.13));
		// System.out.println(Math.exp(-2.0402208285265546));

		// Random random = new Random(System.nanoTime());
		//
		// int i = 20;
		// while (i-- > 0) {
		// System.out.println(random.nextDouble());
		// }

		extractJavaClassDependency();
		//
		
		int n = 3;
		PoissonDistribution poissonDistribution = new PoissonDistribution(7);
		ZipfDistribution zipfDistribution = new ZipfDistribution(n, 1.0);
		for (int i = 1; i <= n; ++i) {
			 System.out.println(i + "\t" + zipfDistribution.probability(n - i + 1));
//			 System.out.println(zipfDistribution.sample());
//			 System.out.println(i + "\t" + zipfDistribution.probability(i));
//			 System.out.println(i + "\t" + poissonDistribution.probability(i));
		}
		//
		// System.out.println("----------");
		//
		// int n = 1;
		// UniformIntegerDistribution uniformIntegerDistribution = new
		// UniformIntegerDistribution(1, n + 1);
		// for (int i = 1; i <= n; ++i) {
		// System.out.println(i + "\t" +
		// uniformIntegerDistribution.probability(i));
		// }

		// NormalDistribution normalDistribution = new NormalDistribution(4, 1);
		//
		// for (int i = 1; i < 1000; ++i) {
		// System.out.println(normalDistribution.sample());
		// }

		// NormalDistribution normalDistribution = new NormalDistribution(4, 1);
		//
		// for (int i = 1; i < 1000; ++i) {
		// System.out.println(normalDistribution.sample());
		// }

		// byte aBytes[] = new byte[100];
		//
		// for (int i = 0; i < 100; ++i) {
		// aBytes[i] = (byte)(i);
		// }
		//
		// Files.write(Paths.get("binfile.txt"), aBytes); //creates, overwrites

		// Scanner scanner = new Scanner(new File("ci-test.txt"));
		//
		// double a[] = new double[100];
		// double b[] = new double[100];
		// int i = 0;
		// while (scanner.hasNext()) {
		// // System.out.println(scanner.next());
		// // System.out.println(scanner.next());
		// a[i] = Double.valueOf(scanner.next());
		// b[i] = Double.valueOf(scanner.next());
		// ++i;
		// }
		//
		// double mA = StatUtils.mean(a);
		// double mB = StatUtils.mean(b);
		// double ciA = ConfidenceInterval.getConfidenceInterval(a);
		// double ciB = ConfidenceInterval.getConfidenceInterval(b);
		//
		// System.out.println(mA + "\t" + Math.sqrt(StatUtils.variance(a)) +
		// "\t" + ciA);
		// System.out.println(mB + "\t" + Math.sqrt(StatUtils.variance(b)) +
		// "\t" + ciB);
		// scanner.close();
	}

}
