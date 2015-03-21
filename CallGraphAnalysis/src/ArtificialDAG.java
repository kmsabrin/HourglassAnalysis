import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class ArtificialDAG {
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

	public ArtificialDAG() {
//		dummy
//		nNode = 15;
//		nLayer = 5;
//		nEdge = (int)(nNode * 2);
		
		nNode = 10000;
		nLayer = 19;
		nEdge = (int)(nNode * 3);

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
	
	void assignNodeByLayerWeight(int nodePerLayerDistribution[]) {
		int nodeID = 1;
		for (int i = 1; i <= nLayer; ++i) {
			System.out.println("Node Count: " + nodePerLayerDistribution[i] + "\t Layer: " + i) ;
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
		}
		nNode = nodeID - 1;
		System.out.println("Total Nodes used: " + nNode);
	}

	void assignLayerWeightRectangleDAG() { // fixed nodes per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * 1.0 / nLayer)); 
		}
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	void assignLayerWeightNoisyRectangleDAG() { // random node per layer
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
	
	void assignLayerWeightHourglassDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 9;
		double alpha = 1.85; // 10,1.81,narrow // 100,1.35,fat // 2,1.5,dummy // for 10K,19L,3Ne
		
		nodePerLayerDistribution[(nLayer + 1) / 2] = (int)wSize;

		for (int i = (nLayer + 1) / 2 - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		for (int i = (nLayer + 1) / 2 + 1, p = 1; i <= nLayer; ++i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	void assignLayerWeightDiamondDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 10;
		double alpha = 1.9; // 10,1.9 //
		
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
	
	void assignLayerWeightTrapezoidDAG() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 10;
		double alpha = 1.37; // 10, 1.37 //
		
//		narrow top trapezoid
		nodePerLayerDistribution[nLayer] = (int)wSize;
		for (int i = nLayer - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
//		fat top trapezoid
//		nodePerLayerDistribution[1] = (int)wSize;
//		for (int i = 2, p = 1; i <= nLayer; ++i, ++p) {
//			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
//		}
		
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}

	void traversePathRecurse(int sourceNode, int targetNode, PrintWriter pw) {
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

	void traversePath(PrintWriter pw) {
		edgeKount = 0;
		while(edgeKount < nEdge) {
			int randomNodeIndex = random.nextInt(topLayerNode.size());
			Integer[] nodeArray = topLayerNode.toArray(new Integer[0]);
			int randomNode = nodeArray[randomNodeIndex];
			traversePathRecurse(-1, randomNode, pw);
		}
	}
	
	public int centralityWeightedSelection(int rangeStart, int rangeEnd, CallDAG callDAG) {
		double centralitySum = 0;
		for (int i = rangeStart; i <= rangeEnd; ++i) {
			if (!callDAG.functions.contains(String.valueOf(i))) continue;
			centralitySum += callDAG.centrality.get(String.valueOf(i));
		}

		double rn = random.nextDouble();
		double weightedCumulativeSum = 0;
		for (int i = rangeStart; i <= rangeEnd; ++i) {
			if (!callDAG.functions.contains(String.valueOf(i))) continue;
			weightedCumulativeSum += callDAG.centrality.get(String.valueOf(i)) / centralitySum;
			if (rn < weightedCumulativeSum) {
				return i;
			}
		}
		
		return -1;
	}

	public void getCentralityShuffleArtificialDAG(String type, Double rewiringProbablity) throws Exception {
		PrintWriter pwCentralityShuffleArtificialDAG = 
				new PrintWriter(new File("artificial_callgraphs//centralityShuffle-" + type + "-" + rewiringProbablity + ".txt"));

		HashSet<String> duplicateCheck = new HashSet();
		
		CallDAG callDAG = new CallDAG("artificial_callgraphs//" + type + ".txt");
//		callDAG.printCallDAG();

		for (String s: existingEdge) {
			String nodePair[] = s.split("#");
			int src = Integer.parseInt(nodePair[0]);
			int dst = Integer.parseInt(nodePair[1]);
			if (random.nextDouble() < rewiringProbablity) {
				int srcLayer = nodeLayerMap.get(src);
				int dstLayer = nodeLayerMap.get(dst);

				int srcMax = Collections.max(nodeIDsPerLayer.get(srcLayer));
				int dstMin = Collections.min(nodeIDsPerLayer.get(dstLayer));

				while (true) {
					int newSrc = centralityWeightedSelection(1, srcMax, callDAG);
					int newDst = centralityWeightedSelection(dstMin, nNode, callDAG);
					String r = newSrc + "+" + newDst;
//					if (duplicateCheck.contains(r)) {
					if (duplicateCheck.contains(r) || existingEdge.contains(newSrc + "#" + newDst)) {
						continue;
					} else {
						duplicateCheck.add(r);
						pwCentralityShuffleArtificialDAG.println(newSrc + " -> "+ newDst + ";");
						
						System.out.println(src + "\t" + dst + "\t" + newSrc + "\t" + newDst);
//						System.out.println("\t" + callDAG.centrality.get(String.valueOf(newSrc)) + "\t" + callDAG.centrality.get(String.valueOf(newDst)));
						
						System.out.print(callDAG.centrality.get(String.valueOf(src)) + "\t" + callDAG.centrality.get(String.valueOf(dst)));
						System.out.println("\t" + callDAG.centrality.get(String.valueOf(newSrc)) + "\t" + callDAG.centrality.get(String.valueOf(newDst)));

						String strSrc = String.valueOf(src);
						String strDst = String.valueOf(dst);
						String strNewSrc = String.valueOf(newSrc);
						String strNewDst = String.valueOf(newDst);
						
						callDAG.callTo.get(strSrc).remove(strDst);
						callDAG.callFrom.get(strDst).remove(strSrc);
						
						if (callDAG.callTo.get(strSrc).size() < 1) callDAG.callTo.remove(strSrc);
						if (callDAG.callFrom.get(strDst).size() < 1) callDAG.callFrom.remove(strDst);
						
						if (!callDAG.callTo.containsKey(strNewSrc)) callDAG.callTo.put(strNewSrc, new HashSet());
						callDAG.callTo.get(strNewSrc).add(strNewDst);
						if (!callDAG.callFrom.containsKey(strNewDst)) callDAG.callFrom.put(strNewDst, new HashSet());
						callDAG.callFrom.get(strNewDst).add(strNewSrc);
						
						callDAG.resetAuxiliary();
						callDAG.removeIsolatedNodes();
						callDAG.loadDegreeMetric();
						callDAG.loadLocationMetric(); // must load degree metric before
						callDAG.loadCentralityMetric();
						break;
					}
				}
			}
			else {
				String r = src + "+" + dst;
				duplicateCheck.add(r);
				pwCentralityShuffleArtificialDAG.println(src + " -> " + dst + ";");
			}
		}
		
		pwCentralityShuffleArtificialDAG.close();
	}
	
	public void getRandomShuffleArtificialDAG(String type, Double rewiringProbablity) throws Exception {
		PrintWriter pwRandomShuffleArtificialDAG = 
				new PrintWriter(new File("artificial_callgraphs//randomShuffle-" + type + "-" + rewiringProbablity + ".txt"));

		HashSet<String> duplicateCheck = new HashSet();
		
		for (String s: existingEdge) {
			String nodePair[] = s.split("#");
			int src = Integer.parseInt(nodePair[0]);
			int dst = Integer.parseInt(nodePair[1]);
			if (random.nextDouble() < rewiringProbablity) {
				int srcLayer = nodeLayerMap.get(src);
				int dstLayer = nodeLayerMap.get(dst);

				int srcMax = Collections.max(nodeIDsPerLayer.get(srcLayer));
				int dstMin = Collections.min(nodeIDsPerLayer.get(dstLayer));

				while (true) {
					int newSrc = random.nextInt(srcMax) + 1;
					int newDst = random.nextInt(nNode - dstMin + 1) + dstMin;
					String r = newSrc + "+" + newDst;
					if (duplicateCheck.contains(r)) {
						continue;
					} else {
						duplicateCheck.add(r);
						pwRandomShuffleArtificialDAG.println(newSrc + " -> "+ newDst + ";");
						break;
					}
				}
			}
			else {
				String r = src + "+" + dst;
				duplicateCheck.add(r);
				pwRandomShuffleArtificialDAG.println(src + " -> " + dst + ";");
			}
		}
		
		pwRandomShuffleArtificialDAG.close();
	}
	
	public void generateRectangleDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//rectangleDAG.txt"));
		
		assignLayerWeightRectangleDAG();
		traversePath(pw);

		pw.close();
		
		getRandomShuffleArtificialDAG("rectangleDAG", rewirePrb);
//		getCentralityShuffleArtificialDAG("rectangleDAG", rewirePrb);
	}
	
	public void generateNoisyRectangleDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//noisyRectangleDAG.txt"));
		
		assignLayerWeightNoisyRectangleDAG();
		traversePath(pw);
		
		pw.close();
		
		getRandomShuffleArtificialDAG("noisyRectangleDAG", rewirePrb);
//		getCentralityShuffleArtificialDAG("noisyRectangleDAG", rewirePrb);
	}
	
	public void generateHourglassDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//hourglassDAG.txt"));

		assignLayerWeightHourglassDAG();
		traversePath(pw);
		
		pw.close();
		
		getRandomShuffleArtificialDAG("hourglassDAG", rewirePrb);
//		getCentralityShuffleArtificialDAG("hourglassDAG", rewirePrb);
	}
	
	public void generateTrapezoidsDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//trapezoidDAG.txt"));

		assignLayerWeightTrapezoidDAG();
		traversePath(pw);

		pw.close();
		
		getRandomShuffleArtificialDAG("trapezoidDAG", rewirePrb);
	}
	
	public void generateDiamondDAG() throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//diamondDAG.txt"));

		assignLayerWeightDiamondDAG();
		traversePath(pw);

		pw.close();
		
		getRandomShuffleArtificialDAG("diamondDAG", rewirePrb);
	}
	
//	public static void getMaxCentralityDecompositionCurve(String version) throws Exception {
//		CallDAG callDAG = new CallDAG("artificial_callgraphs//" + version + ".txt");
//		CallDAG takeApartCallDAG = new CallDAG("artificial_callgraphs//" + version + ".txt");
//		System.out.println("nFunctions: " + callDAG.functions.size());
//		System.out.println("nEdges: " + callDAG.nEdges);
//		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
//		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG, version);
//	}
}
