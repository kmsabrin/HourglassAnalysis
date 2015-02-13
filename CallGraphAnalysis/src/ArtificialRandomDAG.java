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
		nLayer = 50;
		nNode = 1000;
		nEdge = (int)(nNode * 1);
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
			for (int j = 1; j <= nodePerLayerDistribution[i]; ++j) {
				nodeLayerMap.put(nodeID, i);
				nodePerLayer.get(i).add(nodeID);
				System.out.println("Node: " + nodeID + "\t Layer: " + i) ;

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

	void assignLayerRandomOne() { // fixed nodes per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * 1.0 / nLayer)); 
		}
		assignLayer(nodePerLayerDistribution);
	}
	
	void assignLayerRandomTwo() { // random node per layer
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		double randomNumberArray[] = new double[nLayer + 1];
		double sum = 0;
		for (int i = 1; i <= nLayer; ++i) {
			randomNumberArray[i] = random.nextDouble();
			sum += randomNumberArray[i]; 
		}
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * randomNumberArray[i] / sum)); 
		}
		assignLayer(nodePerLayerDistribution);
	}
	
	void assignLayerHourglass() {
		int nodePerLayerDistribution[] = new int[nLayer + 1];
		double nodePerLayerWeight[] = new double[nLayer + 1];
	
		double sum = 1;
		nodePerLayerWeight[nLayer / 2] = 1;
		int j = 2;
		for (int i = nLayer / 2 - 1; i > 0; --i) {
			nodePerLayerWeight[i] = j;
			sum += j;
			++j;
		}
		j = 2;
		for (int i = nLayer / 2 + 1; i <= nLayer; ++i) {
			nodePerLayerWeight[i] = j;
			sum += j;
			++j;
		}
		
		for (int i = 1; i <= nLayer; ++i) {
			nodePerLayerDistribution[i] = (int)(Math.ceil(nNode * nodePerLayerWeight[i] / sum));
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

		while (true) {
			int nextLayer = nodeLayerMap.get(targetNode) + 1;
			int randomNodeIndex = random.nextInt(nodePerLayer.get(nextLayer).size());
			Integer[] nodeArray = nodePerLayer.get(nextLayer).toArray(new Integer[0]);
			int randomNextNode = nodeArray[randomNodeIndex];
			if (existingEdge.contains(targetNode + "+" + randomNextNode)) {
//				continue;
			}
			traversePath(targetNode, randomNextNode, pw);
			break;
		}
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
	
	public static void main(String[] args) throws Exception {
		PrintWriter pwRandomOne = new PrintWriter(new File("artificial_random_callgraphs//randomOne.txt"));
		PrintWriter pwRandomTwo = new PrintWriter(new File("artificial_random_callgraphs//randomTwo.txt"));
		PrintWriter pwRandomHG = new PrintWriter(new File("artificial_random_callgraphs//randomHG.txt"));
		
		ArtificialRandomDAG artificialRandomOneDAG = new ArtificialRandomDAG();
		artificialRandomOneDAG.assignLayerRandomOne();
		artificialRandomOneDAG.generatePath(pwRandomOne);
		artificialRandomOneDAG = null;

		ArtificialRandomDAG artificialRandomTwoDAG = new ArtificialRandomDAG();
		artificialRandomTwoDAG.assignLayerRandomTwo();
		artificialRandomTwoDAG.generatePath(pwRandomTwo);
		
		ArtificialRandomDAG artificialRandomHGDAG = new ArtificialRandomDAG();
		artificialRandomHGDAG.assignLayerHourglass();
		artificialRandomHGDAG.generatePath(pwRandomHG);
		
		pwRandomHG.close();
		pwRandomTwo.close();
		pwRandomOne.close();
		
		CallDAG callDAG = new CallDAG("artificial_random_callgraphs//randomHG.txt");
		CallDAG takeApartCallDAG = new CallDAG("artificial_random_callgraphs//randomHG.txt");
		System.out.println("nFunctions: " + callDAG.functions.size());
		System.out.println("nEdges: " + callDAG.nEdges);
		System.out.println("Roots: " + callDAG.nRoots + " Leaves: " + callDAG.nLeaves);
		CoreAnalysis coreAnalysis = new CoreAnalysis(callDAG, takeApartCallDAG);
	}
}
