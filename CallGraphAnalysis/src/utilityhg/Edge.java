package utilityhg;

public class Edge implements Comparable<Edge> {
	public String source;
	public String target;
	public double pathCentrality;
	
	public Edge(String source, String target, double pathCentrality) {
		this.source = source;
		this.target = target;
		this.pathCentrality = pathCentrality;
	}
	
	public int compareTo(Edge other) {
	    return -1 * Double.compare(this.pathCentrality, other.pathCentrality);
	}
}