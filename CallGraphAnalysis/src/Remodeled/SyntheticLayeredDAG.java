package Remodeled;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class SyntheticLayeredDAG {
	int nLayer;
	int nNode;
	int nEdge;
	int nPath;

	double rewirePrb = 1;
	
	HashSet<Integer> topLayerNode;
	HashSet<Integer> bottomLayerNode;

	HashMap<Integer, Integer> nodeLayerMap;
	HashMap<Integer, HashSet<Integer>> nodeIDsPerLayer; 

	HashMap<Integer, Integer> edges;
	
	Random random;
	
	HashSet<String> existingEdge;
	int edgeKount;

	public SyntheticLayeredDAG() {
//		dummy
//		nNode = 15;
//		nLayer = 5;
//		nEdge = (int)(nNode * 2);
		
		nNode = 10000;
		nLayer = 19;
		nEdge = nNode * 3;

//		nPath = (int)Math.ceil(nEdge / (nLayer - 1));  // (numberOfEdges / (numberOfLayer - 1))

		topLayerNode = new HashSet();
		bottomLayerNode = new HashSet();

		nodeLayerMap = new HashMap();
		nodeIDsPerLayer = new HashMap();
		for (int i = 1; i <= nLayer; ++i) {
			nodeIDsPerLayer.put(i, new HashSet());
		}

		edges = new HashMap();
		
		random = new Random();
		
		existingEdge = new HashSet();
	}
	
	private void assignNodeByLayerWeight(int nodePerLayerDistribution[]) {
		int nodeID = 1;
		for (int i = 1; i <= nLayer; ++i) {
//			System.out.println("Node Count: " + nodePerLayerDistribution[i] + "\t Layer: " + i) ;
			for (int j = 1; j <= nodePerLayerDistribution[i]; ++j) {
				nodeLayerMap.put(nodeID, i);
				nodeIDsPerLayer.get(i).add(nodeID);
//				System.out.println("Node: " + nodeID + "\t Layer: " + i) ;

				if (i == 1) {
					topLayerNode.add(nodeID);
				}

				if (i == nLayer) {
					bottomLayerNode.add(nodeID);
				}

				++nodeID;
			}
			System.out.print((nodeID - 1) + ",");
		}
		System.out.println();
		
		nNode = nodeID - 1;
		System.out.println("Total Nodes used: " + nNode);
	}

	private void assignLayerWeightRectangleDAG() { // fixed nodes per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * 1.0 / nLayer)); 
		}
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	private void assignLayerWeightNoisyRectangleDAG() { // random node per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		double randomNumberArray[] = new double[nLayer + 1];
		double sum = 0;
		for (int i = 1; i <= nLayer; ++i) {
			randomNumberArray[i] = random.nextDouble() + 0.5;
			sum += randomNumberArray[i]; 
		}
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * randomNumberArray[i] / sum)); 
		}
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	private void assignLayerWeightHourglassDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 2;
		double alpha = 2.24; // 10,1.85,narrow // 100,1.35,fat // 2,1.5,dummy // for 10K,19L,3Ne

//		dummy
//		double wSize = 2;
//		double alpha = 1.5; // 10,1.85,narrow // 100,1.35,fat // 2,1.5,dummy // for 10K,19L,3Ne

		nodePerLayerDistribution[(nLayer + 1) / 2] = (int)wSize;

		for (int i = (nLayer + 1) / 2 - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		for (int i = (nLayer + 1) / 2 + 1, p = 1; i <= nLayer; ++i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	private void assignLayerWeightDiamondDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 2;
		double alpha = 2.33; // 10,1.9 // 2, 2.33
		
//		narrow top trapezoid
		nodePerLayerDistribution[nLayer] = (int)wSize;
		for (int i = nLayer - 1, p = 1; i > nLayer / 2; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
//		fat top trapezoid
		nodePerLayerDistribution[1] = (int)wSize;
		for (int i = 2, p = 1; i <= nLayer / 2; ++i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	private void assignLayerWeightTrapezoidDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 2;
		double alpha = 1.515; // 10, 1.37 // 2, 1.515
		
//		fat top trapezoid
		nodePerLayerDistribution[nLayer] = (int)wSize;
		for (int i = nLayer - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
//		narrow top trapezoid
//		nodePerLayerDistribution[1] = (int)wSize;
//		for (int i = 2, p = 1; i <= nLayer; ++i, ++p) {
//			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
//		}
		
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}

	private void traversePathRecurse(int sourceNode, int targetNode, PrintWriter pw) {
		if (sourceNode > 0) {
			if (!existingEdge.contains(sourceNode + "#" + targetNode)) {
				pw.println(sourceNode + " -> " + targetNode + ";");
				edges.put(sourceNode, targetNode);
				existingEdge.add(sourceNode + "#" + targetNode);
				++edgeKount;
			}
		}

		if (bottomLayerNode.contains(targetNode)) {
//			System.out.println("+-+-+-+-+-+");
			return;
		}

		int nextLayer = nodeLayerMap.get(targetNode) + 1;
		int randomNodeIndex = random.nextInt(nodeIDsPerLayer.get(nextLayer).size());
		Integer[] nodeArray = nodeIDsPerLayer.get(nextLayer).toArray(new Integer[0]);
		int randomNextNode = nodeArray[randomNodeIndex];
		traversePathRecurse(targetNode, randomNextNode, pw);
	}

	private void traversePath(PrintWriter pw) {
		edgeKount = 0;
		while(edgeKount < nEdge) {
			int randomNodeIndex = random.nextInt(topLayerNode.size());
			Integer[] nodeArray = topLayerNode.toArray(new Integer[0]);
			int randomNode = nodeArray[randomNodeIndex];
			traversePathRecurse(-1, randomNode, pw);
		}
	}
	
	public void generateRectangleDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//rectangleDAG.txt"));
		
		assignLayerWeightRectangleDAG();
		traversePath(pw);

		pw.close();
	}
	
	public void generateNoisyRectangleDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//noisyRectangleDAG.txt"));
		
		assignLayerWeightNoisyRectangleDAG();
		traversePath(pw);
		
		pw.close();
	}
	
	public void generateHourglassDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//hourglassDAG.txt"));

		assignLayerWeightHourglassDAG();
		traversePath(pw);
		
		pw.close();
	}
	
	public void generateTrapezoidsDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//trapezoidDAG.txt"));

		assignLayerWeightTrapezoidDAG();
		traversePath(pw);

		pw.close();
	}
	
	public void generateDiamondDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//diamondDAG.txt"));

		assignLayerWeightDiamondDAG();
		traversePath(pw);

		pw.close();
	}
}
