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
	public TreeMap<Integer, ArrayList<String>> levelNodeMap;
	public HashMap<String, Integer> nodeLevelMap;
	public HashMap<String, Integer> nodeIdMap;
	public int targetLevel;
	private Random random;
	
	public ModelRealConnector(DependencyDAG dependencyDAG) {
		random = new Random(System.nanoTime());
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
	
	private int getLevel2(int itemIndex, int level, HashMap<Integer, ArrayList<String>> topoSubtree) {
		int itemCount = 0;
		for (int i = level - 1;  i >= 0; --i) {
			if (!topoSubtree.containsKey(i)) {
				continue;
			}
			itemCount += topoSubtree.get(i).size();
			if (itemIndex < itemCount) {
				return i;
			}
		}
		return -1;
	}
	
	private String getDegreePreferredSubstrate(ArrayList<String> candidateList, HashMap<String, Integer> nodeOutdeg) {
		double sum = 0;
		for (String s: candidateList) {
			sum += nodeOutdeg.get(s) + 1;
		}
		
		double rnd = random.nextDouble();
		double runningSum = 0;
		for (String s : candidateList) {
			runningSum += nodeOutdeg.get(s) * 1.0 / sum;
			if (rnd <= runningSum) {
				return s;
			}
		}
		
		return candidateList.get(random.nextInt(candidateList.size()));
	}

	public void generateModelNetwork2(DependencyDAG dependencyDAG, double alpha) throws Exception {
		PrintWriter pw = new PrintWriter(new File("real_model_networks//real-model-test.txt"));
		HashMap<String, Integer> nodeOutdeg = new HashMap();
		int nodeId = dependencyDAG.nodes.size() - 1;
		nodeIdMap = new HashMap();
		
		for (int i: levelNodeMap.keySet()) {
			if (i == 0) {				
				for (String n: levelNodeMap.get(i)) {
					nodeIdMap.put(n, nodeId--);
					nodeOutdeg.put(n, 0);
				}
				continue; // sources
			}
			
			for (String product: levelNodeMap.get(i)) {
				dependencyDAG.visited.clear();
				dependencyDAG.reachableDownwardsNodes(product);
				dependencyDAG.visited.remove(product);
				
				HashSet<String> uniqueEdge = new HashSet();
				ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(dependencyDAG.visited.size(), alpha);

				HashMap<Integer, ArrayList<String>> topoSubtree = new HashMap();
				for (String s: dependencyDAG.visited) {
					int level = nodeLevelMap.get(s);
					if (topoSubtree.containsKey(level)) {
						topoSubtree.get(level).add(s);
					}
					else {
						ArrayList<String> aList = new ArrayList();
						aList.add(s);
						topoSubtree.put(level, aList);
					}
				}
				
				nodeIdMap.put(product, nodeId--);
				nodeOutdeg.put(product, 0);
				int inDegree = dependencyDAG.inDegree.get(product);
				for (int d = 0; d < inDegree; ++d) {
//					int substrateIndex = zipfDistributionWrapper.getNodeFromZipfDistribution2(0);
//					int substrateLevel = getLevel2(substrateIndex, i, topoSubtree);					
					String edge = "";
					String substrate = "";
					do {
						int substrateIndex = zipfDistributionWrapper.getNodeFromZipfDistribution2(0);
						int substrateLevel = getLevel2(substrateIndex, i, topoSubtree);
						ArrayList<String> candidateList = topoSubtree.get(substrateLevel);
						// random selection
						substrate = candidateList.get(random.nextInt(candidateList.size()));
						// outdeg preferential selection
//						substrate = getDegreePreferredSubstrate(candidateList, nodeOutdeg);
						edge = substrate + "#" + product;
//						substrateLevel = Math.max(0, substrateLevel - 1);
//						System.out.println(edge);
					} while(uniqueEdge.contains(edge));
					uniqueEdge.add(edge);
					
//					System.out.println(substrate + "\t" + product);
					int v = nodeOutdeg.get(substrate);
					nodeOutdeg.put(substrate, v + 1);
					pw.println(nodeIdMap.get(substrate) + "\t" + nodeIdMap.get(product));
				}
			}
		}
		pw.close();
	}
	
	public void generateModelNetwork(DependencyDAG dependencyDAG, double alpha) throws Exception {
		PrintWriter pw = new PrintWriter(new File("real_model_networks//real-model-test.txt"));
		HashSet<String> uniqueEdge = new HashSet();
		HashMap<String, Integer> nodeOutdeg = new HashMap();
		
		int nodeId = dependencyDAG.nodes.size() - 1;
		nodeIdMap = new HashMap();
		
		int itemCount = 0;
		for (int i: levelNodeMap.keySet()) {
			if (i == 0) {
				itemCount += levelNodeMap.get(i).size();
				
				for (String n: levelNodeMap.get(i)) {
					nodeIdMap.put(n, nodeId--);
					nodeOutdeg.put(n, 0);
				}
				continue; // sources
			}

//			int startLevel = 0;
//			int endLevel = i - 1;
//			ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(endLevel - startLevel + 1, alpha);

			ZipfDistributionWrapper zipfDistributionWrapper = new ZipfDistributionWrapper(itemCount, alpha);

			for (String product: levelNodeMap.get(i)) {
				nodeIdMap.put(product, nodeId--);
				nodeOutdeg.put(product, 0);
				int inDegree = dependencyDAG.inDegree.get(product);
				for (int d = 0; d < inDegree; ++d) {
//					int substrateLevel = zipfDistributionWrapper.getNodeFromZipfDistribution2(startLevel, endLevel);
					
//					int substrateIndex = zipfDistributionWrapper.getNodeFromZipfDistribution2(0);
//					int substrateLevel = getLevel(substrateIndex, i);
					
					String edge = "";
					String substrate = "";
					do {
						int substrateIndex = zipfDistributionWrapper.getNodeFromZipfDistribution2(0);
						int substrateLevel = getLevel(substrateIndex, i);
						ArrayList<String> candidateList = levelNodeMap.get(substrateLevel);
						// outdeg preferential selection
						substrate = getDegreePreferredSubstrate(candidateList, nodeOutdeg);
						edge = substrate + "#" + product;
//						substrateLevel = Math.max(0, substrateLevel - 1);
						System.out.println(edge);
					} while(uniqueEdge.contains(edge));
					uniqueEdge.add(edge);
					
//					System.out.println(substrate + "\t" + product);
					int v = nodeOutdeg.get(substrate);
					nodeOutdeg.put(substrate, v + 1);
					pw.println(nodeIdMap.get(substrate) + "\t" + nodeIdMap.get(product));
				}
			}
			itemCount += levelNodeMap.get(i).size();
		}
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		DependencyDAG.isToy = true;
		String toyDAGName = "toy_dag_paper";
//		String toyDAGName = "real-model-test";
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		ModelRealConnector modelRealConnector = new ModelRealConnector(toyDependencyDAG);
		modelRealConnector.generateModelNetwork2(toyDependencyDAG, 1);
//		String netID = "toy_dag";
//		toyDependencyDAG.printNetworkStat();
//		toyDependencyDAG.printNetworkProperties();

//		CoreDetection.fullTraverse = false;
//		CoreDetection.getCore(toyDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;
		
//		CoreDetection.getCentralEdgeSubgraph(toyDependencyDAG);

//		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
//		FlatNetwork.makeAndProcessFlat(toyDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}
}
