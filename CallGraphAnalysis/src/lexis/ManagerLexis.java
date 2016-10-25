package lexis;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;

import utilityhg.DistributionAnalysis;
import corehg.CoreDetection;
import corehg.DependencyDAG;
import corehg.FlattenNetwork;
import corehg.SimpleModelDAG;

public class ManagerLexis {
	private static void loadLargestWCC(String filePath) throws Exception {
		DependencyDAG.largestWCCNodes = new HashSet();
		Scanner scanner = new Scanner(new File("analysis//largestWCC-" + filePath + ".txt"));
		while (scanner.hasNext()) {
			String line = scanner.next();
//			System.out.println(line);
			DependencyDAG.largestWCCNodes.add(line);
		}
		scanner.close();
	}
	
	private static void doRealNetworkAnalysis(String netPath, String netID) throws Exception {
		DependencyDAG.resetFlags();
//		loadLargestWCC(netID);
				

//		DependencyDAG.isCallgraph = true;
		DependencyDAG.isLexis = true;
		
		DependencyDAG.isWeighted = true;
		
//		DependencyDAG.isSynthetic = true;
//		DependencyDAG.isSimpleModel = true;
		
		DependencyDAG dependencyDAG = new DependencyDAG(netPath + "//" + netID);
//		DependencyDAG.printNetworkStat(dependencyDAG);
//		dependencyDAG.printNetworkProperties();
		
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.getAverageInOutDegree(dependencyDAG);
//		DistributionAnalysis.getPathLength(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogram(dependencyDAG);
//		DistributionAnalysis.getDegreeHistogramSpecialized(dependencyDAG);
//		DistributionAnalysis.findWeaklyConnectedComponents(dependencyDAG, netID);		
//		DistributionAnalysis.printCentralityRanks(dependencyDAG, netID);
//		int centralityIndex = 1;
//		DistributionAnalysis.getCentralityCCDF(dependencyDAG, netID, 1);
//		DistributionAnalysis.printCentralityDistribution(dependencyDAG, netID);
//		DistributionAnalysis.printEdgeList(dependencyDAG, netID);
//		DistributionAnalysis.printAllCentralities(dependencyDAG, netID);
//		DistributionAnalysis.findNDirectSrcTgtBypasses(dependencyDAG, netID);
		
//		Core Detection
//		CoreDetection.fullTraverse = true;
		CoreDetection.getCore(dependencyDAG, netID);
		double realCore = CoreDetection.minCoreSize;

//		Flattening
		FlattenNetwork.makeAndProcessFlat(dependencyDAG);	
		CoreDetection.hScore = (1.0 - (realCore / FlattenNetwork.flatNetworkCoreSize));
		System.out.println("H-Score: " + CoreDetection.hScore);
	}
	
	public static void analyzeModelLexisDAG() throws Exception {
		String DAGType = "SimpleModelDAG";
		SimpleModelDAG.isMultigraph = true;
		int din = 2;
		double alpha = 0.01;
		int nT = 20;
		int nI = 30;
		int nS = 20;
		String ratio = "-1";
		SimpleModelDAG.generateSimpleModel(alpha, din, nT, nI, nS, -1);
		SimpleModelDAG.initModelProperties(nT, nI, nS, din);
		String networkID = DAGType + "r" + ratio + "a" + alpha + "d" + din;
//		System.out.println("synthetic_callgraphs//" + networkID + ".txt");
		doRealNetworkAnalysis("synthetic_callgraphs", networkID + ".txt");
	}
	
	public static void main(String[] args) throws Exception {		
//		ManagerLexis.doRealNetworkAnalysis("lexis_graphs//Protein", "yeast.txt");
//		ManagerLexis.doRealNetworkAnalysis("lexis_graphs//Text", "Cogall.txt");
//		ManagerLexis.doRealNetworkAnalysis("lexis_graphs//iGEM", "iGEM_All.txt");
		
		ManagerLexis.doRealNetworkAnalysis("lexis_graphs/hgDAG-payam/Lexis-DAGs", "L-a101.txt");
		ManagerLexis.doRealNetworkAnalysis("lexis_graphs/hgDAG-payam/Kaeser-DAGs", "K-a101.txt");
		
//		ManagerLexis.analyzeModelLexisDAG();
		
//		System.out.println("Done!");
	}
}