import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

public class ClusterAnalysis {
//	DBSCANClusterer dbScanClusterer;
//	
//	// wrapper class
//	public static class PointCoordinates implements Clusterable {
//	    private double[] points;
//	   
//	    public PointCoordinates(double x, double y) {
//	        this.points = new double[] {x, y};
//	    }
//
//	    public double[] getPoint() {
//	        return points;
//	    }
//	}
//
//	public ClusterAnalysis(double eps, int minPts) {
//		dbScanClusterer = new DBSCANClusterer(eps, minPts);
//	}
//	
//	public void getClusters(CallDAG callDAG) throws Exception {
//		List<PointCoordinates> pointsList = new ArrayList();
//		
//		for (String s: callDAG.functions) {
//			PointCoordinates pointCoordianates = new PointCoordinates(callDAG.generality.get(s), callDAG.complexity.get(s));
//			pointsList.add(pointCoordianates);
////			System.out.println("adding: " + pointCoordianates.getPoint().toString());
//		}
//		
//		List<Cluster> clusterList = dbScanClusterer.cluster(pointsList);
//		
//		System.out.println("Number of Clusters Found: " + clusterList.size());
//		
//		int clusterIndex = 1;
//		// output the clusters
//		for (Cluster t: clusterList) {
//			PrintWriter pw = new PrintWriter(new File("Results//cluster" + (clusterIndex++) + ".txt"));
//			for (int i = 0; i < t.getPoints().size(); ++i) {
//				PointCoordinates p = (PointCoordinates)t.getPoints().get(i);
//		    	pw.println(p.points[0] + "\t" + p.points[1]);
//		    }
//			pw.close();		
//		}
//	}
	
	public static void demonstrateClustersForVersionX() throws Exception {
		CallDAG callDAG = new CallDAG("kernel_callgraphs//full.graph-2.6.33");
		
		double generalitySeparator, complexitySeparator;
		double gS = 0, cS = 0;
		for (String s: callDAG.functions) {
			gS += callDAG.generality.get(s);
			cS += callDAG.complexity.get(s);
		}
		generalitySeparator = gS / callDAG.functions.size();
		complexitySeparator = cS / callDAG.functions.size();
		
		generalitySeparator = 0.3;
		complexitySeparator = 0.065; // hard code
		
		PrintWriter pwGC = new PrintWriter(new File("Results//v33-gen-cmp-cluster1-GC.txt"));
		PrintWriter pwgC = new PrintWriter(new File("Results//v33-gen-cmp-cluster2-gC.txt"));
		PrintWriter pwgc = new PrintWriter(new File("Results//v33-gen-cmp-cluster3-gc.txt"));
		PrintWriter pwGc = new PrintWriter(new File("Results//v33-gen-cmp-cluster4-Gc.txt"));
		
		for (String s: callDAG.functions) {			
			double g = callDAG.generality.get(s); 
			double c = callDAG.complexity.get(s);
			
			if (g > generalitySeparator && c > complexitySeparator) {
				pwGC.println(g + "\t" + c);
			}
			else if (g <= generalitySeparator && c > complexitySeparator) { 
				pwgC.println(g + "\t" + c);
			}
			else if (g <= generalitySeparator && c <= complexitySeparator) { 
				pwgc.println(g + "\t" + c);
			}
			else if (g > generalitySeparator && c <= complexitySeparator) { 
				pwGc.println(g + "\t" + c);
			}
		}
		
		pwGC.close();
		pwgC.close();
		pwgc.close();
		pwGc.close();
	}
}
