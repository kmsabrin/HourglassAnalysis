package utilityhg;

import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlattenNetwork;

public class ToyTest {
	private static void doToyNetworkAnalysis() throws Exception {
		DependencyDAG.isToy = true;
//		DependencyDAG.isNeuro = true;
		String toyDAGName = "toy_dag_2";
		DependencyDAG toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		
		String netID = "toy_dag";
		DependencyDAG.printNetworkStat(toyDependencyDAG);
		toyDependencyDAG.printNetworkProperties();

		CoreDetection.fullTraverse = false;
		CoreDetection.getCore(toyDependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

		toyDependencyDAG = new DependencyDAG("toy_networks//" + toyDAGName + ".txt");
		FlattenNetwork.makeAndProcessFlat(toyDependencyDAG);
		CoreDetection.hScore = (1.0 - ((realCore - 1) / FlattenNetwork.flatNetworkCoreSize));
		System.out.println("[h-Score] " + CoreDetection.hScore);
	}

	public static void main(String[] args) throws Exception {
		doToyNetworkAnalysis();
	}
}
