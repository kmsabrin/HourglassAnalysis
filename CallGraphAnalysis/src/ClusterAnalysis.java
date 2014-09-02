import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

public class ClusterAnalysis {
	DBSCANClusterer dbScanClusterer;
	
	// wrapper class
	public static class PointCoordinates implements Clusterable {
	    private double[] points;
	   
	    public PointCoordinates(double x, double y) {
	        this.points = new double[] {x, y};
	    }

	    public double[] getPoint() {
	        return points;
	    }
	}

	public ClusterAnalysis(double eps, int minPts) {
		dbScanClusterer = new DBSCANClusterer(eps, minPts);
	}
	
	public void getClusters(CallDAG callDAG) throws Exception {
		List<PointCoordinates> pointsList = new ArrayList();
		
		for (String s: callDAG.functions) {
			PointCoordinates pointCoordianates = new PointCoordinates(callDAG.generality.get(s), callDAG.complexity.get(s));
			pointsList.add(pointCoordianates);
//			System.out.println("adding: " + pointCoordianates.getPoint().toString());
		}
		
		List<Cluster> clusterList = dbScanClusterer.cluster(pointsList);
		
		System.out.println("Number of Clusters Found: " + clusterList.size());
		
		int clusterIndex = 1;
		// output the clusters
		for (Cluster t: clusterList) {
			PrintWriter pw = new PrintWriter(new File("Results//cluster" + (clusterIndex++) + ".txt"));
			for (int i = 0; i < t.getPoints().size(); ++i) {
				PointCoordinates p = (PointCoordinates)t.getPoints().get(i);
		    	pw.println(p.points[0] + "\t" + p.points[1]);
		    }
			pw.close();		
		}
	}
}
