package Remodeled;
import java.io.File;
import java.io.PrintWriter;

public class Driver {	
	static String networkPath = "kernel_callgraphs//full.graph-2.6.";
	static String networkType = "kernel";
	static int versiontStart = 0;
	static int versionEnd = 40; // last + 1
	
//	static String networkPath = "openssh_callgraphs//full.graph-openssh-";
//	static String networkType = "ssh";
//	static int versiontStart = 1;
//	static int versionEnd = 40;
	
	private static void printNetworkStat(CallDAG callDAG) {
		System.out.println("S: " + callDAG.nSources);
		System.out.println("T: " + callDAG.nTargets);
		System.out.println("E: " + callDAG.nEdges);
		System.out.println("N: " + callDAG.functions.size());
	}
	
	public static void doKernelAnalysis() throws Exception {
//		String versions[] = {"1", "11", "21", "31"};
		String versions[] = {"11"};
		for (int i = 0; i < versions.length; ++i) {
			String v = networkType + "-" + versions[i];	
			CallDAG callDAG = new CallDAG(networkPath + versions[i]);
			printNetworkStat(callDAG);
			IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(callDAG);
			iteratedMaxCentralityCoverage.runIMCC();					
		}	
	}
	
	public static void doArtificialNetworkAnalysis() throws Exception {
//		new ArtificialDAG().generateRectangleDAG();
//		new ArtificialDAG().generateHourglassDAG();
//		new ArtificialDAG().generateTrapezoidsDAG();
//		new ArtificialDAG().generateNoisyRectangleDAG();
//		new ArtificialDAG().generateDiamondDAG();

		String versions[] = {"rectangleDAG", 
							 "noisyRectangleDAG",
							 "hourglassDAG", 
							 "trapezoidDAG",
							 "diamondDAG",
		};
		
//		String versions[] = {"hourglassDAG"/*, "randomShuffle-hourglassDAG-1.0"*/};
		
		
		for (int i = 0; i < versions.length; ++i) {
//			if (i < 2) continue;			
			String versionNumber = versions[i];
			CallDAG callDAG = new CallDAG("artificial_callgraphs//" + versionNumber + ".txt");
			IteratedMaxCentralityCoverage iteratedMaxCentralityCoverage = new IteratedMaxCentralityCoverage(callDAG);
			iteratedMaxCentralityCoverage.runIMCC();		
		}
	}

	public static void main(String[] args) throws Exception {		
//		Driver.doArtificialNetworkAnalysis();
		Driver.doKernelAnalysis();
	}
}
