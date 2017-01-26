package corehg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.stat.StatUtils;

public class EdgeCopyingModel {
	static int nSources = 20000;
	static int nTargets = 20000;
	static int nIntermediates = 60000;
	static int nNodes = nSources + nIntermediates + nTargets;
	static double beta = 0.5;
	static int inDeg = 3;

	public static void generateModel() throws Exception {
		Random rand = new Random(System.nanoTime());
		HashMap<Integer, ArrayList<Integer>> substrateMap = new HashMap();
		PrintWriter pw = new PrintWriter(new File("copy_models//copy-dag.txt"));
		
		for (int i = (nTargets + nIntermediates - 1); i >= 0; --i) {
			substrateMap.put(i, new ArrayList());
			if (rand.nextDouble() < beta || i == (nTargets + nIntermediates - 1)) {
				HashSet<Integer> noRepeat = new HashSet();
				for (int j = 0; j < inDeg; ++j) {
					int substrateNode = (i + 1) + rand.nextInt(nNodes - i - 1);
					if (i < nTargets) {
						substrateNode = nTargets + rand.nextInt(nSources + nIntermediates);
					}
					if (noRepeat.contains(substrateNode)) {
						--j;
						continue;
					}
					noRepeat.add(substrateNode);
					substrateMap.get(i).add(substrateNode);
					pw.println(substrateNode + "\t" + i);
				}
			}
			else {
				int copyNode = (i + 1) + rand.nextInt(nIntermediates + nTargets - i - 1);
				if (i < nTargets) {
					copyNode = nTargets + rand.nextInt(nIntermediates);
				}
				for (int j: substrateMap.get(copyNode)) {
					substrateMap.get(i).add(j);
					pw.println(j + "\t" + i);
				}
			}
		}
		
		pw.close();
	}
	
	public static void statTest() throws Exception {
		int nRun = 5;
		for (double b = 0; b <= 1.0; b += 0.1) {
			double hScores[] = new double[nRun];
			for (int r = 0; r < nRun; ++r) {
				generateModel();
				SimpleModelDAG.initModelProperties(nTargets, nIntermediates, nSources, inDeg);
				DependencyDAG.isSynthetic= true;
				String copyDAGName = "copy-dag";
				DependencyDAG copyDependencyDAG = new DependencyDAG("copy_models//" + copyDAGName + ".txt");
				CoreDetection.fullTraverse = false;
				CoreDetection.getCore(copyDependencyDAG, copyDAGName);
				double realCore = CoreDetection.minCoreSize;
				FlatNetwork.makeAndProcessFlat(copyDependencyDAG);
				CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
				hScores[r] = CoreDetection.hScore;
			}
			System.out.println(b + "\t" + StatUtils.percentile(hScores, 50));
		}
	}
	
	public static void runTest() throws Exception {
//		generateModel();
		SimpleModelDAG.initModelProperties(nTargets, nIntermediates, nSources, inDeg);
		DependencyDAG.isSynthetic= true;
		String copyDAGName = "copy-dag";
		DependencyDAG copyDependencyDAG = new DependencyDAG("copy_models//" + copyDAGName + ".txt");
		
//		copyDependencyDAG.printNetworkStat();
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
//		neuroDependencyDAG.printNetworkProperties();

//		DistributionAnalysis.getPathLength(neuroDependencyDAG);
//		CoreDetection.getCentralEdgeSubgraph(neuroDependencyDAG);
//		DistributionAnalysis.getCentralityCCDF(copyDependencyDAG, netID, 1);
		
//		Visualization.printDOTNetwork(neuroDependencyDAG);
//		CoreDetection.pathCoverageTau = 1.0;
		System.out.println("Detecting core");
		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(copyDependencyDAG, copyDAGName);
		double realCore = CoreDetection.minCoreSize;

//		copyDependencyDAG = new DependencyDAG("copy_models//" + copyDAGName + ".txt");
		FlatNetwork.makeAndProcessFlat(copyDependencyDAG);
//		System.out.println(FlatNetwork.flatNetworkCoreSize);
		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlatNetwork.flatNetworkCoreSize));
		System.out.println(CoreDetection.hScore);
	}
	
	public static void main(String[] args) throws Exception {
//		statTest();
		runTest();
	}
}
