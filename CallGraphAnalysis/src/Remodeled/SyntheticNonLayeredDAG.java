package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

public class SyntheticNonLayeredDAG {
	static int nE = 30000; // no. of Edges
	static int nN = 10050; // no. of Nodes
	
	static double pSL, pSW, pSU, pST;
	static double pLL, pLW, pLU, pLT;
	static double pWW, pWU, pWT;
	static double pUU, pUT;
	
	static double pSI, pII, pIT; // pST defined earlier
	
	public static void setHG5TierParameters() {
		pSL = 0.69; pSW = 0.29; pSU = 0.01; pST = 0.01;
				
		pLL = 0.54; pLW = 0.44; pLU = 0.01; pLT = 0.01;
		
		pWW = 0.4; pWU = 0.5; pWT = 0.1;
		
		pUU = 0.7; pUT = 0.3;
	}
	
	public static void setNHG5TierParameters() {
		pSL = 0.44; pSW = 0.02; pSU = 0.44; pST = 0.1;
				
		pLL = 0.44; pLW = 0.02; pLU = 0.44; pLT = 0.1;
		
		pWW = 0.02; pWU = 0.75; pWT = 0.23;
		
		pUU = 0.8; pUT = 0.2;
	}
	
	public static void setNHG3TierParameters() {
		pST = 0.1; pSI = 0.9;
		pII = 0.98; pIT = 0.02;
	}
	
	public static void getNLHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//NLHGDAG.txt"));
		setHG5TierParameters();
		generate5TierDAG(pw);
	}
	
	public static void getNLNHGDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//NLNHGDAG.txt"));
		setNHG3TierParameters();
		generate3TierDAG(pw);
	}
	
	public static void generate5TierDAG(PrintWriter pw) throws Exception {
		int nT = 1000; // no. of T(arget) nodes
		int nU = 4000; // no. of U(pper) nodes
		int nW = 50; // no. of W(aist) nodes
		int nL = 4000; // no. of L(ower) nodes
		int nS = 1000; // no. of S(ource) nodes

		int sT = 1; // start of Target
		int sU = 1001; // start of U
		int sW = 5001; // start of Waist
		int sL = 5051; // start of L
		int sS = 9051; // start of source
		
//		toy
//		int nT = 3; // no. of T(arget) nodes
//		int nU = 6; // no. of U(pper) nodes
//		int nW = 2; // no. of W(aist) nodes
//		int nL = 6; // no. of L(ower) nodes
//		int nS = 3; // no. of S(ource) nodes
//	
//		int sT = 1; // start of Target
//		int sU = 4; // start of U
//		int sW = 10; // start of Waist
//		int sL = 12; // start of L
//		int sS = 18; // start of source
//	
//		int nE = 30; // no. of Edges
//		int nN = 20; // no. of Nodes
		
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
					if (st == sL) p = random.nextDouble() * (1.0 - pLL) + pLL;
					
					if (p < pLL) {
						en = sL + random.nextInt(st - sL);
						startingFrom = 'L';
//						if (st < en) { // edge goes from large index to small index
//							int tmp = st;
//							st = en;
//							en = tmp;
//						}
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
					if (st == sW) p = random.nextDouble() * (1.0 - pWW) + pWW;
					
					if (p < pWW) {
						en = sW + random.nextInt(st - sW);
						startingFrom = 'W';
//						if (st < en) {
//							int tmp = st;
//							st = en;
//							en = tmp;
//						}
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
					if (st == sU) p = random.nextDouble() * (1.0 - pUU) + pUU;
					
					if (p < pUU) {
						en = sU + random.nextInt(st - sU);
						startingFrom = 'U';
//						if (st < en) {
//							int tmp = st;
//							st = en;
//							en = tmp;
//						}
					} 
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				}
				
				if (!edgeHash.contains(st + "#" + en)) {
					pw.println(st + " " + en);
					edgeHash.add(st + "#" + en);
				}
				
				st = en;
			}
		}
		pw.close();
	}
	
	public static void generate3TierDAG(PrintWriter pw) throws Exception {
		int nT = 1000; // no. of T(arget) nodes
		int nI = 8050; // no. of I(ntermediate) nodes
		int nS = 1000; // no. of S(ource) nodes

		int sT = 1; // start of Target
		int sI = 1001; // start of Intermediate
		int sS = 9051; // start of source
		
//		toy
//		int nT = 4; // no. of T(arget) nodes
//		int nI = 8; // no. of I(ntermediate) nodes
//		int nS = 4; // no. of S(ource) nodes
//
//		int sT = 1; // start of Target
//		int sI = 5; // start of Intermediate
//		int sS = 13; // start of source
//
//		int nE = 20; // no. of Edges
//		int nN = 16; // no. of Nodes
		
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
					if (p < pSI) {
						en = sI + random.nextInt(nI);
						startingFrom = 'I';
					}
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'I') {
					if (st == sI) p = random.nextDouble() * (1.0 - pII) + pII;
					
					if (p < pII) {
						en = sI + random.nextInt(st - sI);
						
//						en = sI + random.nextInt(nI);
//						if (st < en) { // edge goes from large index to small index
//							int tmp = st;
//							st = en;
//							en = tmp;
//						}
						startingFrom = 'I';
					}
					else {
						en = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				
				if (!edgeHash.contains(st + "#" + en)) {
					pw.println(st + " " + en);
					edgeHash.add(st + "#" + en);
				}
				
				st = en;
			}
		}
		pw.close();
	}

	public static void main(String[] args) throws Exception {
//		getNLHGDAG();
		getNLNHGDAG();
		System.out.println("Done!");
	}
}
