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
	
	public static void makeAndProcessFlat(DependencyDAG dependencyDAG) throws Exception {
//		System.out.println("Flattening");
		writeFlatten(dependencyDAG);
		if (DependencyDAG.isSynthetic == false) { // for real networks only
			DependencyDAG.isClassDependency = true;
			DependencyDAG.isMetabolic = DependencyDAG.isCallgraph = DependencyDAG.isCourtcase = DependencyDAG.isToy = DependencyDAG.isCyclic = false;
		}
		
		DependencyDAG.isWeighted = false;
		DependencyDAG flatDAG = new DependencyDAG("flat_networks//current_flat.txt");
//		flatDAG.printNetworkProperties();
		CoreDetection.fullTraverse = false;
		isProcessingFlat = true;
		CoreDetection.getCore(flatDAG, "flatDAG");
		isProcessingFlat = false;
		FlatNetwork.flatNetworkCoreSize = CoreDetection.minCoreSize;
	}
}
