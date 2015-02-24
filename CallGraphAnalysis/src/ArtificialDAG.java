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

	HashSet<Integer> topLayerNode;
	HashSet<Integer> bottomLayerNode;

	HashMap<Integer, Integer> nodeLayerMap;
	HashMap<Integer, HashSet<Integer>> nodePerLayer;

	HashMap<Integer, Integer> edges;
	
	Random random;
	
	HashSet<String> existingEdge;
	int edgeKount;

	public ArtificialDAG() {
		// dummy
//		nNode = 12;
//		nLayer = 4;
//		nEdge = (int)(nNode * 1.5);
		
		nNode = 2000;
		nLayer = 9;
		nEdge = (int)(nNode * 3);

//		nPath = (int)Math.ceil(nEdge / (nLayer - 1));  // (numberOfEdges / (numberOfLayer - 1))

		topLayerNode = new HashSet();
		bottomLayerNode = new HashSet();

		nodeLayerMap = new HashMap();
		nodePerLayer = new HashMap();
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayer.put(i, new HashSet());
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
				nodePerLayer.get(i).add(nodeID);
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
	}

	void assignLayerWeightFixedNodePerLayerDAG() { // fixed nodes per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * 1.0 / nLayer)); 
		}
		assignNodeByLayerWeight(nodePerLayerDistribution);
	}
	
	void assignLayerWeightVariableNodePerLayerDAG() { // random node per layer
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
	
		double wSize = 10.0;
		double alpha = 2.85;
		
		nodePerLayerDistribution[(nLayer + 1) / 2] = (int)wSize;

		for (int i = (nLayer + 1) / 2 - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		for (int i = (nLayer + 1) / 2 + 1, p = 1; i <= nLayer; ++i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
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
		int randomNodeIndex = random.nextInt(nodePerLayer.get(nextLayer).size());
		Integer[] nodeArray = nodePerLayer.get(nextLayer).toArray(new Integer[0]);
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
	
	public void getRandomShuffleArtificialDAG(String type) throws Exception {
		PrintWriter pwRandomShuffleArtificialDAG = new PrintWriter(new File("artificial_callgraphs//randomShuffle" + type + ".txt"));

		HashSet<String> duplicateCheck = new HashSet();
		
		for (String s: existingEdge) {
			String nodePair[] = s.split("#");
			int src = Integer.parseInt(nodePair[0]);
			int dst = Integer.parseInt(nodePair[1]);
			
			int srcLayer = nodeLayerMap.get(src);
			int dstLayer = nodeLayerMap.get(dst);
			
			int srcMax = Collections.max(nodePerLayer.get(srcLayer));
			int dstMin = Collections.min(nodePerLayer.get(dstLayer));
			
			while (true) {
				int newSrc = random.nextInt(srcMax) + 1;
				int newDst = random.nextInt(nNode - dstMin + 1) + dstMin;
				String r = newSrc + "+" + newDst;
				if (duplicateCheck.contains(r)) {
					continue;
				}
				else {
					duplicateCheck.add(r);
					pwRandomShuffleArtificialDAG.println(newSrc + " -> " + newDst + ";");
					break;
				}
			}
		}
		
		pwRandomShuffleArtificialDAG.close();
	}
	
	public void generateArtificialFixedNodePerLayerDAG() throws Exception {
		PrintWriter pwArtificialFixedNodePerLayer = new PrintWriter(new File("artificial_callgraphs//artificialFixedNodePerLayerDAG.txt"));
		
		assignLayerWeightFixedNodePerLayerDAG();
		traversePath(pwArtificialFixedNodePerLayer);

		pwArtificialFixedNodePerLayer.close();
		
		getRandomShuffleArtificialDAG("ArtificialFixedNodePerLayerDAG");
	}
	
	public void generateArtificialVariableNodePerLayerDAG() throws Exception {
		PrintWriter pwArtificialVariableNodePerLayer = new PrintWriter(new File("artificial_callgraphs//artificialVariableNodePerLayerDAG.txt"));
		
		assignLayerWeightVariableNodePerLayerDAG();
		traversePath(pwArtificialVariableNodePerLayer);
		
		pwArtificialVariableNodePerLayer.close();
		
		getRandomShuffleArtificialDAG("ArtificialVariableNodePerLayerDAG");
	}
	
	public void generateArtificialHourglassDAG() throws Exception {
		PrintWriter pwartificialHourglass = new PrintWriter(new File("artificial_callgraphs//artificialHourglassDAG.txt"));

		assignLayerWeightHourglassDAG();
		traversePath(pwartificialHourglass);
		
		pwartificialHourglass.close();
		
		getRandomShuffleArtificialDAG("ArtificialHourglassDAG");
	}
	
	public static void analyzeArtificialDAG(String version) throws Exception {
		CallDAG callDAG = new CallDAG("artificial_callgraphs//" + version + ".txt");
		CallDAG takeApartCallDAG = new CallDAG("artificial_callgraphs//" + version + ".txt");
		System.out.println("nFunctions: " + callDAG.functions.size());
		System.out.println("nEdges: " + callDAG.nEdges);
		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG, version);
	}
}
