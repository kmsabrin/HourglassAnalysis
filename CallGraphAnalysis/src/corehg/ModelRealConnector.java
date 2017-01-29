package corehg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

import utilityhg.ZipfDistributionWrapper;

public class ModelRealConnector {
	private TreeMap<Integer, ArrayList<String>> levelNodeMap;
	private HashMap<String, Integer> nodeLevelMap;
	private HashMap<String, Integer> nodeIdMap;
	private int targetLevel;
	
	public ModelRealConnector(DependencyDAG dependencyDAG) {
		getTopoMap(dependencyDAG);
	}
	
	private void getTopoMapHelper(DependencyDAG dependencyDAG, String s) {
		if (dependencyDAG.isSource(s)) {
			nodeLevelMap.put(s, 0);
			levelNodeMap.get(0).add(s);
			return;
		}
		
		int maxLevel = -1;
		for (String r: dependencyDAG.depends.get(s)) {
			if (!nodeLevelMap.containsKey(r)) {
				getTopoMapHelper(dependencyDAG, r);
			}
			
			if (nodeLevelMap.get(r) > maxLevel) {
				maxLevel = nodeLevelMap.get(r); 
			}
		}
		if (maxLevel > targetLevel) {
			targetLevel = maxLevel;
		}
		
		if (dependencyDAG.isTarget(s)) return;
		
		++maxLevel;
		nodeLevelMap.put(s, maxLevel);
		if (!levelNodeMap.containsKey(maxLevel)) {
			levelNodeMap.put(maxLevel, new ArrayList());
		}
		levelNodeMap.get(maxLevel).add(s);
	}
	
	private void getTopoMap(DependencyDAG dependencyDAG) {
		levelNodeMap = new TreeMap();
		nodeLevelMap = new HashMap();
		levelNodeMap.put(0, new ArrayList());
		targetLevel = -1;
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isTarget(s)) {
				getTopoMapHelper(dependencyDAG, s);
			}
		}
		
		++targetLevel;
		levelNodeMap.put(targetLevel, new ArrayList());
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isTarget(s)) {
				nodeLevelMap.put(s, targetLevel);
				levelNodeMap.get(targetLevel).add(s);
			}
		}
		
//		for (int i: levelNodeMap.keySet()) {
//			System.out.print(i);
//			for (String r: levelNodeMap.get(i)) {
//				System.out.print("\t" + r);
//			}
//			System.out.println();
//		}
	}
	
	private int getLevel(int itemIndex, int level) {
		int itemCount = 0;
		for (int i = level - 1;  i >= 0; --i) {
			itemCount += levelNodeMap.get(i).size();
			if (itemIndex <= itemCount) {
				return i;
			}
		}
		return -1;
	}
	
	public void generateModelNetwork(DependencyDAG dependencyDAG, double alpha) throws Exception {
		PrintWriter pw = new PrintWriter(new File("real_model_networks//real-model-test.txt"));
		Random random = new Random(System.nanoTime());
		HashSet<String> uniqueEdge = new HashSet();
		
		int nodeId = dependencyDAG.nodes.size() - 1;
		nodeIdMap = new HashMap();
		
		int itemCount = 0;
		for (int i: levelNodeMap.keySet()) {
			if (i == 0) {
				itemCount += levelNodeMap.get(i).size();
				
				for (String n: levelNodeMap.get(i)) {
					nodeIdMap.put(n, nodeId--);
				}
				continue; // sources
			}

//			int startLevel = 0;
//			int endLevel = i - 1;
//			ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(endLevel - startLevel + 1, alpha);

			ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(itemCount, alpha);

			for (String product: levelNodeMap.get(i)) {
				nodeIdMap.put(product, nodeId--);
				int inDegree = dependencyDAG.inDegree.get(product);
				for (int d = 0; d < inDegree; ++d) {
//					int substrateLevel = zipfDistributionWrapper.getNodeFromZipfDistribution2(startLevel, endLevel);
					
					int substrateIndex = zipfDistributionWrapper.getNodeFromZipfDistribution2(0);
					int substrateLevel = getLevel(substrateIndex, i);
					
					ArrayList<String> candidateList = levelNodeMap.get(substrateLevel);
					String substrate = candidateList.get(random.nextInt(candidateList.size()));
					String edge = substrate + "#" + product;
					if (uniqueEdge.contains(edge)) {
						--d;
						continue;
					}
					uniqueEdge.add(edge);
//					System.out.println(substrate + "\t" + product);
					pw.println(nodeIdMap.get(substrate) + "\t" + nodeIdMap.get(product));
				}
			}
			itemCount += levelNodeMap.get(i).size();
		}
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		DependencyDAG.isToy = true;
		String toyDAGName = "real-model-test";
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		String netID = "toy_dag";
		toyDependencyDAG.printNetworkStat();
//		toyDependencyDAG.printNetworkProperties();

		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(toyDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;
		
//		CoreDetection.getCentralEdgeSubgraph(toyDependencyDAG);

//		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		FlatNetwork.makeAndProcessFlat(toyDependencyDAG);
		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
		System.out.println("[h-Score] " + CoreDetection.hScore);
	}
}
