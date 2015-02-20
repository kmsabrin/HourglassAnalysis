import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class ArtificialRandomDAG {
	int nLayer;
	int nNode;
	int nEdge;
	int nPath;

	HashSet<Integer> topLayerNode;
	HashSet<Integer> bottomLayerNode;

	HashMap<Integer, Integer> nodeLayerMap;
	HashMap<Integer, HashSet<Integer>> nodePerLayer;

	Random random;
	
	HashSet<String> existingEdge;
	int edgeKount;

	public ArtificialRandomDAG() {
		nNode = 4000;
//		nLayer = nNode / 700;
		nLayer = 9;
		nEdge = (int)(nNode * 3);
		nPath = (int)Math.ceil(nEdge / (nLayer - 1));  // (numberOfEdges / (numberOfLayer - 1))

		topLayerNode = new HashSet();
		bottomLayerNode = new HashSet();

		nodeLayerMap = new HashMap();
		nodePerLayer = new HashMap();
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayer.put(i, new HashSet());
		}

		random = new Random();
		
		existingEdge = new HashSet();
	}
	
	void assignLayer(int nodePerLayerDistribution[]) {
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

	void assignLayerFixedNode() { // fixed nodes per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * 1.0 / nLayer)); 
		}
		assignLayer(nodePerLayerDistribution);
	}
	
	void assignLayerVariableNode() { // random node per layer
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
		assignLayer(nodePerLayerDistribution);
	}
	
	void assignLayerHourglass() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
	
		double wSize = 20.0;
		double alpha = 2.841;
		nodePerLayerDistribution[(nLayer + 1) / 2] = (int)wSize;

		for (int i = (nLayer + 1) / 2 - 1, p = 1; i > 0; --i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		for (int i = (nLayer + 1) / 2 + 1, p = 1; i <= nLayer; ++i, ++p) {
			nodePerLayerDistribution[i] = (int)(Math.pow(alpha, p) * wSize); 
		}
		
		assignLayer(nodePerLayerDistribution);
	}

	void traversePath(int sourceNode, int targetNode, PrintWriter pw) {
		if (sourceNode > 0) {
			if (!existingEdge.contains(sourceNode + "+" + targetNode)) {
				pw.println(sourceNode + " -> " + targetNode + ";");
				existingEdge.add(sourceNode + "+" + targetNode);
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
		traversePath(targetNode, randomNextNode, pw);
	}

	void generatePath(PrintWriter pw) {
		edgeKount = 0;
		while(edgeKount < nEdge) {
			int randomNodeIndex = random.nextInt(topLayerNode.size());
			Integer[] nodeArray = topLayerNode.toArray(new Integer[0]);
			int randomNode = nodeArray[randomNodeIndex];
			traversePath(-1, randomNode, pw);
		}
	}
	
	public void generateArtificialRandomDAG() throws Exception {
		PrintWriter pwRandomOne = new PrintWriter(new File("artificial_random_callgraphs//randomOne.txt"));
		PrintWriter pwRandomTwo = new PrintWriter(new File("artificial_random_callgraphs//randomTwo.txt"));
		PrintWriter pwRandomHG = new PrintWriter(new File("artificial_random_callgraphs//randomHG.txt"));
		
		ArtificialRandomDAG artificialRandomOneDAG = new ArtificialRandomDAG();
		artificialRandomOneDAG.assignLayerFixedNode();
		artificialRandomOneDAG.generatePath(pwRandomOne);
		artificialRandomOneDAG = null;

		ArtificialRandomDAG artificialRandomTwoDAG = new ArtificialRandomDAG();
		artificialRandomTwoDAG.assignLayerVariableNode();
		artificialRandomTwoDAG.generatePath(pwRandomTwo);
		
		ArtificialRandomDAG artificialRandomHGDAG = new ArtificialRandomDAG();
		artificialRandomHGDAG.assignLayerHourglass();
		artificialRandomHGDAG.generatePath(pwRandomHG);
		
		pwRandomHG.close();
		pwRandomTwo.close();
		pwRandomOne.close();
	}
	
	public void analyzeArtificialRandomDAG(String version) throws Exception {
		CallDAG callDAG = new CallDAG("artificial_random_callgraphs//" + version + ".txt");
		CallDAG takeApartCallDAG = new CallDAG("artificial_random_callgraphs//" + version + ".txt");
		System.out.println("nFunctions: " + callDAG.functions.size());
		System.out.println("nEdges: " + callDAG.nEdges);
		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG, version);
	}
}
