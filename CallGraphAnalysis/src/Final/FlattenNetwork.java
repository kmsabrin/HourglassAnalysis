package Final;

import java.io.File;
import java.io.PrintWriter;

public class FlattenNetwork {
	static double flatNetworkCoreSize = -1;
	private static void writeFlatten(DependencyDAG dependencyDAG) throws Exception {
		PrintWriter pw = new PrintWriter(new File("flat_networks//current_flat.txt"));
		for (String s: dependencyDAG.nodes) {
			if (dependencyDAG.isSource(s)) {
				dependencyDAG.loadRechablity(s);
				for (String r: dependencyDAG.dependentsReachable.get(s)) {
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
		if (dependencyDAG.isSynthetic == false) { // for real networks only
			dependencyDAG.isClassDependency = true;
			dependencyDAG.isMetabolic = dependencyDAG.isCallgraph = dependencyDAG.isCourtcase = false;
		}
		
		DependencyDAG flatDAG = new DependencyDAG("flat_networks//current_flat.txt");
		CoreDetection.getCore(flatDAG, "flatDAG");
		FlattenNetwork.flatNetworkCoreSize = CoreDetection.minCoreSize;
	}
}
