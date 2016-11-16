package utilityhg;

import corehg.CoreDetection;
import corehg.DependencyDAG;

public class ToyTest {	
	private static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
		DependencyDAG.isCyclic = true;
//		String toyDAGName = "out-toy";
		String toyDAGName = "toy_dag_paper";
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		String netID = "toy_dag";
//		DependencyDAG.printNetworkStat(toyDependencyDAG);
//		toyDependencyDAG.printNetworkProperties();

//		CoreDetection.fullTraverse = false;
//		CoreDetection.getCore(toyDependencyDAG, netID);
//		double realCore = CoreDetection.minCoreSize;
		
		CoreDetection.getCentralEdgeSubgraph(toyDependencyDAG);

//		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
//		FlattenNetwork.makeAndProcessFlat(toyDependencyDAG);
//		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
//		System.out.println("[h-Score] " + CoreDetection.hScore);
	}

	public static void main(String[] args) throws Exception {
		doToyNetworkAnalysis();
	}
}
