package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

public class SyntheticNonLayeredDAG {
	public static void getNLHGDAG() throws Exception {
		int nT = 1000;
		int nU = 4000;
		int nW = 50;
		int nL = 4000;
		int nS = 1000;

		int sT = 1;
		int sU = 1001;
		int sW = 5001;
		int sL = 5051;
		int sS = 9051;

		double pSL = 0.7;
		double pLL = 0.2;
		double pWW = 0.2;
		double pWU = 0.5;
		double pUU = 0.2;

		int nE = 30000;
		
		Random random = new Random(System.nanoTime());
		HashSet<String> edgeHash = new HashSet();
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//NLHGDAG.txt"));

		while (edgeHash.size() < nE) {
			char startingFrom = 'S';
			boolean flag = true;
			int st = -1;
			int en = -1;
			double p = -1;
			while (flag) {
				if (startingFrom == 'S') {
					st = sS + random.nextInt(nS);
					p = random.nextDouble();
					if (p < pSL) {
						en = sL + random.nextInt(nL);
						startingFrom = 'L';
					} else {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					if (!edgeHash.contains(st + "#" + en)) {
						pw.println(st + " -> " + en + ";");
						edgeHash.add(st + "#" + en);
					}
					st = en;
				} else if (startingFrom == 'L') {
					p = random.nextDouble();
					if (p < pLL) {
						en = sL + random.nextInt(nL);
						startingFrom = 'L';
						if (st < en) {
							int tmp = st;
							st = en;
							en = tmp;
						}
					} else {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					if (!edgeHash.contains(st + "#" + en)) {
						pw.println(st + " -> " + en + ";");
						edgeHash.add(st + "#" + en);
					}
					st = en;
				} else if (startingFrom == 'W') {
					p = random.nextDouble();
					if (p < pWW) {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
						if (st < en) {
							int tmp = st;
							st = en;
							en = tmp;
						}
					} else if (p < pWW + pWU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
					} else {
						en = sT + random.nextInt(nT);
						flag = false;
					}
					if (!edgeHash.contains(st + "#" + en)) {
						pw.println(st + " -> " + en + ";");
						edgeHash.add(st + "#" + en);
					}
					st = en;
				} else if (startingFrom == 'U') {
					p = random.nextDouble();
					if (p < pUU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
						if (st < en) {
							int tmp = st;
							st = en;
							en = tmp;
						}
					} else {
						en = sT + random.nextInt(nT);
						flag = false;
					}
					if (!edgeHash.contains(st + "#" + en)) {
						pw.println(st + " -> " + en + ";");
						edgeHash.add(st + "#" + en);
					}
					st = en;
				}
			}
		}
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		getNLHGDAG();
	}
}
