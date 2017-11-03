package corehg;

import java.io.File;
import java.io.PrintWriter;

import neuro.ManagerNeuro;
import utilityhg.DistributionAnalysis;

public class FlatNetwork {
	public static double flatNetworkCoreSize = -1;
	public static boolean isProcessingFlat = false;
	private static void writeFlatten(DependencyDAG dependencyDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("flat_networks//current_flat.txt"));
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				dependencyDAG.loadReachability(s);
				for (String r: dependencyDAG.successors.get(s)) {
					if (dependencyDAG.isTarget(r)) {
						pw.println(s + "\t" + r);
					}
				}
			}
		}
		pw.close();
	}
	
	private static void writeFlattenWeighted(DependencyDAG dependencyDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("flat_networks//current_flat.txt"));
		
//		System.out.println(DependencyDAG.isCelegans);
//		System.out.println(ManagerNeuro.source);
//		System.out.println(dependencyDAG.nodes);
		
		
		CoreDetection.topRemovedWaistNodes.clear();
		for (String s: dependencyDAG.nodes) {
			if ((!DependencyDAG.isCelegans && dependencyDAG.isSource(s)) || (DependencyDAG.isCelegans && ManagerNeuro.source.contains(s))) {
				CoreDetection.topRemovedWaistNodes.add(s);
//				System.out.println("Adding " + s);
			}
			else {
//				System.out.println(DependencyDAG.isCelegans + " " +  ManagerNeuro.source.contains(s) + " " + s);
			}
		}
		
		for (String s: dependencyDAG.nodes) {
			if ((!DependencyDAG.isCelegans && dependencyDAG.isSource(s)) || (DependencyDAG.isCelegans && ManagerNeuro.source.contains(s))) {
				dependencyDAG.initPathStat();
				CoreDetection.topRemovedWaistNodes.remove(s);
				dependencyDAG.loadPathStatistics();
				for (String r: dependencyDAG.nodes) {
					if (!DependencyDAG.isCelegans && dependencyDAG.isTarget(r)) {
						if (dependencyDAG.nodePathThrough.get(r) > 0) {
							pw.println(s + "\t" + r + "\t" + dependencyDAG.nodePathThrough.get(r));
						}
					}
					if (DependencyDAG.isCelegans && ManagerNeuro.target.contains(r)) {
						if(dependencyDAG.numOfSourcePath.get(r) > 0) {
							pw.println(s + "\t" + r + "\t" + dependencyDAG.numOfSourcePath.get(r));
						}
					}
				}
				CoreDetection.topRemovedWaistNodes.add(s);
			}
		}
		pw.close();
	}
	
	public static void makeAndProcessFlat(DependencyDAG dependencyDAG) throws Exception {
//		System.out.println("Flattening");
		
		writeFlattenWeighted(dependencyDAG);
//		writeFlatten(dependencyDAG);
		if (DependencyDAG.isSynthetic == false) { // for real networks only
			DependencyDAG.resetFlags();
			DependencyDAG.isToy = true;
		}
		
//		DependencyDAG.isWeighted = false;
		DependencyDAG.isWeighted = true;
		DependencyDAG.isCelegans = false;
		DependencyDAG flatDAG = new DependencyDAG("flat_networks//current_flat.txt");
//		flatDAG.printNetworkStat();
//		flatDAG.printNetworkProperties();
//		DistributionAnalysis.getDistributionCCDF(flatDAG, "celegans-flat", 1);
//		DistributionAnalysis.getDistributionCCDF(flatDAG, "celegans-flat", 3);
		CoreDetection.fullTraverse = false;
		isProcessingFlat = true;
		CoreDetection.getCore(flatDAG, "flatDAG");
		isProcessingFlat = false;
		DependencyDAG.isWeighted = false;
		FlatNetwork.flatNetworkCoreSize = CoreDetection.minCoreSize;
	}
}
