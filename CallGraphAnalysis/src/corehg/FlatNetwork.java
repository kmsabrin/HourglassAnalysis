package corehg;

import java.io.File;
import java.io.PrintWriter;

public class FlatNetwork {
	public static double flatNetworkCoreSize = -1;
	public static boolean isProcessingFlat = false;
	private static void writeFlatten(DependencyDAG dependencyDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("flat_networks//current_flat.txt"));
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				dependencyDAG.loadRechablity(s);
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
		
		CoreDetection.topRemovedWaistNodes.clear();
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				CoreDetection.topRemovedWaistNodes.add(s);
			}
		}
		
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				dependencyDAG.initPathStat();
				CoreDetection.topRemovedWaistNodes.remove(s);
				dependencyDAG.loadPathStatistics();
				for (String r: dependencyDAG.nodes) {
					if (dependencyDAG.isTarget(r)) {
						if (dependencyDAG.nodePathThrough.get(r) > 0) {
							pw.println(s + "\t" + r + "\t" + dependencyDAG.nodePathThrough.get(r));
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
		DependencyDAG flatDAG = new DependencyDAG("flat_networks//current_flat.txt");
//		flatDAG.printNetworkProperties();
		CoreDetection.fullTraverse = false;
		isProcessingFlat = true;
		CoreDetection.getCore(flatDAG, "flatDAG");
		isProcessingFlat = false;
		DependencyDAG.isWeighted = false;
		FlatNetwork.flatNetworkCoreSize = CoreDetection.minCoreSize;
	}
}
