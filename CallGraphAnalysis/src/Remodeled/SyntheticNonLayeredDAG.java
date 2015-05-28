package Remodeled;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

public class SyntheticNonLayeredDAG {
	static int nE = 30000; // no. of Edges
	static int nN = 10050; // no. of Nodes
	
	static double pSL, pSW, pSU, pST;
	static double pLL, pLW, pLU, pLT;
	static double pWW, pWU, pWT;
	static double pUU, pUT;
	
	static double pSI, pII, pIT; // pST defined earlier
	
	public static void setHG5TierParameters() {
		pSL = 0.60; pSW = 0.20; pSU = 0.1; pST = 0.1;
				
		pLL = 0.45; pLW = 0.35; pLU = 0.1; pLT = 0.1;
		
		pWW = 0.3; pWU = 0.6; pWT = 0.1;
		
		pUU = 0.7; pUT = 0.3;
	}
	
//	public static void setNHG5TierParameters() {
//		pSL = 0.44; pSW = 0.02; pSU = 0.44; pST = 0.1;
//				
//		pLL = 0.44; pLW = 0.02; pLU = 0.44; pLT = 0.1;
//		
//		pWW = 0.02; pWU = 0.75; pWT = 0.23;
//		
//		pUU = 0.8; pUT = 0.2;
//	}
	
	public static void setNHG3TierParameters() {
		pSI = 0.99; pST = 0.01;
		pII = 0.86; pIT = 0.14;
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
			int nxtNode = -1;
			double p = -1;
			ArrayList<Integer> tracedPathNodes = new ArrayList();
			
			while (notReachedTarget) {
				p = random.nextDouble();
				
				if (startingFrom == 'S') {
					int start = sS + random.nextInt(nS);
					tracedPathNodes.add(start);					
					if (p < pSL) {
						nxtNode = sL + random.nextInt(nL);
						startingFrom = 'L';
					} 
					else if (p < pSL + pSW) {
						nxtNode = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					else if (p < pSL + pSW + pSU) {
						nxtNode = sU + random.nextInt(nU);
						startingFrom = 'U';
					}
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'L') {
					if (p < pLL) {
						nxtNode = sL + random.nextInt(nL);
						startingFrom = 'L';
					}
					else if (p < pLL + pLW) {
						nxtNode = sW + random.nextInt(nW);
						startingFrom = 'W';
					}
					else if (p < pLL + pLW + pLU) {
						nxtNode = sU + random.nextInt(nU);
						startingFrom = 'U';
					}
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'W') {					
					if (p < pWW) {
						nxtNode = sW + random.nextInt(nW);
						startingFrom = 'W';
					} 
					else if (p < pWW + pWU) {
						nxtNode = sU + random.nextInt(nU);
						startingFrom = 'U';
					} 
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'U') {
					if (p < pUU) {
						nxtNode = sU + random.nextInt(nU);
						startingFrom = 'U';
					} 
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				}
				
				tracedPathNodes.add(nxtNode);
			}
			
			tracedPathNodes = new ArrayList(new TreeSet<Integer>(tracedPathNodes));
			for (int i = 1; i < tracedPathNodes.size(); ++i) {
				int src = tracedPathNodes.get(i - 1);
				int tgt = tracedPathNodes.get(i);
				if (!edgeHash.contains(src + "#" + tgt)) {
					pw.println(src + " " + tgt);
					edgeHash.add(src + "#" + tgt);
				}
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
			int nxtNode = -1;
			double p = -1;
			ArrayList<Integer> tracedPathNodes = new ArrayList();

			while (notReachedTarget) {
				p = random.nextDouble();
				
				if (startingFrom == 'S') {
					int start = sS + random.nextInt(nS);	
					tracedPathNodes.add(start);
					
					if (p < pSI) {
						nxtNode = sI + random.nextInt(nI);
						startingFrom = 'I';
					}
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				else if (startingFrom == 'I') {
					if (p < pII) {
						nxtNode = sI + random.nextInt(nI);
						startingFrom = 'I';
					}
					else {
						nxtNode = sT + random.nextInt(nT);
						notReachedTarget = false;
					}
				} 
				
				tracedPathNodes.add(nxtNode);
			}
			
			tracedPathNodes = new ArrayList(new TreeSet<Integer>(tracedPathNodes));
			for (int i = 1; i < tracedPathNodes.size(); ++i) {
				int src = tracedPathNodes.get(i - 1);
				int tgt = tracedPathNodes.get(i);
				if (!edgeHash.contains(src + "#" + tgt)) {
					pw.println(src + " " + tgt);
					edgeHash.add(src + "#" + tgt);
				}
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
