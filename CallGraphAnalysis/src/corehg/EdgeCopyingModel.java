package corehg;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.StatUtils;

import utilityhg.ConfidenceInterval;

public class EdgeCopyingModel {
	static int nSources = 400;
	static int nTargets = 400;
	static int nIntermediates = 400;
//	static int nSources = 3;
//	static int nTargets = 3;
//	static int nIntermediates = 4;
	static int nNodes = nSources + nIntermediates + nTargets;
	static double beta = 0.01;
	static double poissonMean = 1.5;
	static PoissonDistribution poissonDistribution;

	public static void generateModel() throws Exception {
		Random rand = new Random(System.nanoTime());
		HashMap<Integer, ArrayList<Integer>> substrateMap = new HashMap();
		PrintWriter pw = new PrintWriter(new File("copy_models//copy-dag.txt"));
		poissonDistribution = new PoissonDistribution(poissonMean);
		for (int i = nNodes - 1; i >= 0; --i) {
			substrateMap.put(i, new ArrayList());
			if (i >= (nTargets + nIntermediates)) {
				substrateMap.get(i).add(i);
				continue;
			}
			int inDeg = poissonDistribution.sample() + 1;
			inDeg = Math.min(inDeg, nNodes - i - 1);
			HashSet<Integer> noRepeat = new HashSet();
			for (int j = 0; j < inDeg; ++j) {
				int substrateNode = -1;
				if (rand.nextDouble() < beta) {				
					substrateNode = (i + 1) + rand.nextInt(nNodes - i - 1);
					if (i < nTargets) {
						substrateNode = nTargets + rand.nextInt(nSources + nIntermediates);
					}
				}
				else {
					int copyNode = (i + 1) + rand.nextInt(nNodes - i - 1);
					if (i < nTargets) {
						copyNode = nTargets + rand.nextInt(nSources + nIntermediates);
					}
					int size = substrateMap.get(copyNode).size();
					substrateNode = substrateMap.get(copyNode).get(rand.nextInt(size));
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
		
		pw.close();
	}
	
	public static void statTest() throws Exception {
		int nRun = 20;
		for (double b = 0; b <= 1.01; b += 0.1) {
			double hScores[] = new double[nRun];
			beta = b;
			for (int r = 0; r < nRun; ++r) {
				generateModel();
				SimpleModelDAG.initModelProperties(nTargets, nIntermediates, nSources, -1);
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
//			System.out.println(b + "\t" + StatUtils.percentile(hScores, 50));
			System.out.println(b + "\t" + StatUtils.mean(hScores) + "\t" + ConfidenceInterval.getConfidenceInterval(hScores));
//			System.out.println(b + "\t" + StatUtils.mean(hScores) + "\t" + Math.sqrt(StatUtils.variance(hScores)));
		}
	}
	
	public static void runTest() throws Exception {
		generateModel();
		SimpleModelDAG.initModelProperties(nTargets, nIntermediates, nSources, -1);
		DependencyDAG.isSynthetic= true;
		String copyDAGName = "copy-dag";
		DependencyDAG copyDependencyDAG = new DependencyDAG("copy_models//" + copyDAGName + ".txt");
		
//		copyDependencyDAG.printNetworkStat();
//		getLocationColorWeightedHistogram(neuroDependencyDAG);
		copyDependencyDAG.printNetworkProperties();

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
		statTest();
//		runTest();
	}
}
