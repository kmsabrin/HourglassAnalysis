package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

public class SyntheticNonLayeredDAG {
	static int nT = 1000; // no. of T(arget) nodes
	static int nU = 4000; // no. of U(pper) nodes
	static int nW = 50; // no. of W(aist) nodes
	static int nL = 4000; // no. of L(ower) nodes
	static int nS = 1000; // no. of S(ource) nodes

	static int sT = 1; // start of Target
	static int sU = 1001; // start of U
	static int sW = 5001; // start of Waist
	static int sL = 5051; // start of L
	static int sS = 9051; // start of source

	static int nE = 30000; // no. of Edges
	static int nN = 10050; // no. of Nodes

//	toy
//	static int nT = 3; // no. of T(arget) nodes
//	static int nU = 6; // no. of U(pper) nodes
//	static int nW = 2; // no. of W(aist) nodes
//	static int nL = 6; // no. of L(ower) nodes
//	static int nS = 3; // no. of S(ource) nodes
//
//	static int sT = 1; // start of Target
//	static int sU = 4; // start of U
//	static int sW = 10; // start of Waist
//	static int sL = 12; // start of L
//	static int sS = 18; // start of source
//
//	static int nE = 30; // no. of Edges
//	static int nN = 20; // no. of Nodes
	
	
	static double pSL, pSW, pSU, pST;
	static double pLL, pLW, pLU, pLT;
	static double pWW, pWU, pWT;
	static double pUU, pUT;
	
	public static void setHGParameters() {
		pSL = 0.60; pSW = 0.20; pSU = 0.1; pST = 0.1;
				
		pLL = 0.40; pLW = 0.40; pLU = 0.1; pLT = 0.1;
		
		pWW = 0.3; pWU = 0.5; pWT = 0.2;
		
		pUU = 0.4; pUT = 0.6;
	}
	
	public static void setNHGParameters() {
		pSL = 0.44; pSW = 0.02; pSU = 0.44; pST = 0.1;
				
		pLL = 0.44; pLW = 0.02; pLU = 0.44; pLT = 0.1;
		
		pWW = 0.02; pWU = 0.75; pWT = 0.23;
		
		pUU = 0.8; pUT = 0.2;
	}
	
	public static void getNLHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//NLHGDAG.txt"));
		setHGParameters();
		generateDAG(pw);
	}
	
	public static void getNLNHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//NLNHGDAG.txt"));
		setNHGParameters();
		generateDAG(pw);
	}
	
	public static void generateDAG(PrintWriter pw) throws Exception {
		Random random = new Random(System.nanoTime());
		HashSet<String> edgeHash = new HashSet();

		while (edgeHash.size() < nE) {
			char startingFrom = 'S';
			boolean notReachedTarget = true;
			int st = -1;
			int en = -1;
			double p = -1;
			
			while (notReachedTarget) {
				p = random.nextDouble();
				
				if (startingFrom == 'S') {
					st = sS + random.nextInt(nS);					
					if (p < pSL) {
						en = sL + random.nextInt(nL);
						startingFrom = 'L';
					} 
					else if (p < pSL + pSW) {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					else if (p < pSL + pSW + pSU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
					}
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'L') {
					if (p < pLL) {
						en = sL + random.nextInt(nL);
						startingFrom = 'L';
						if (st < en) { // edge goes from large index to small index
							int tmp = st;
							st = en;
							en = tmp;
						}
					}
					else if (p < pLL + pLW) {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					else if (p < pLL + pLW + pLU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
					}
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'W') {
					if (p < pWW) {
						en = sW + random.nextInt(nW);
						startingFrom = 'W';
						if (st < en) {
							int tmp = st;
							st = en;
							en = tmp;
						}
					} 
					else if (p < pWW + pWU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
					} 
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'U') {
					if (p < pUU) {
						en = sU + random.nextInt(nU);
						startingFrom = 'U';
						if (st < en) {
							int tmp = st;
							st = en;
							en = tmp;
						}
					} 
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				}
				
				if (!edgeHash.contains(st + "#" + en)) {
					pw.println(st + " -> " + en + ";");
					edgeHash.add(st + "#" + en);
				}
				
				st = en;
			}
		}
		pw.close();
	}

	public static void main(String[] args) throws Exception {
		getNLHGDAG();
		getNLNHGDAG();
		System.out.println("Done!");
	}
}
