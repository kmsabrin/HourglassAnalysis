package Remodeled;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class IteratedMaxCentralityCoverage {
	HashSet<String> coveredNodes;
	HashSet<String> coveredLinks;
	HashSet<String> visited;
	ArrayList<String> coreNodes;
	DependencyDAG dependencyDAG;
	Random random;
	
	public IteratedMaxCentralityCoverage(DependencyDAG dependencyDAG) {
		coveredNodes = new HashSet();
		coreNodes = new ArrayList();
		this.dependencyDAG = dependencyDAG;
		random = new Random(System.nanoTime());
	}
	
	private void coverReachableNodes(String candidateNode) {
		for (String s: dependencyDAG.dependentsReachable.get(candidateNode)) {
			coveredNodes.add(s);
		}
		
		for (String s: dependencyDAG.serversReachable.get(candidateNode)) {
			coveredNodes.add(s);
		}
	}
	
	private String getMaxCentralityNode() {
		ArrayList<String> maxCentralityNodes = new ArrayList();
		double maxCentrality = -1;
		
		for (String s: dependencyDAG.functions) {
			if (coveredNodes.contains(s)) {
				continue;
			}
			
			double centrality = dependencyDAG.centrality.get(s);
			
			if (centrality > maxCentrality) {
				maxCentrality = centrality;
				maxCentralityNodes.clear();
				maxCentralityNodes.add(s);
			}
			else if (centrality < maxCentrality) {
			}
			else {
				maxCentralityNodes.add(s);				
			}
		}
		
		if (maxCentralityNodes.isEmpty()) {
			return null;
		}
		
		return maxCentralityNodes.get(random.nextInt(maxCentralityNodes.size()));
	}
	
	private boolean coverMaxCentralityNode() {
		String maxCentralityCandidateNode = getMaxCentralityNode();

		if (maxCentralityCandidateNode == null) return false;

		if (dependencyDAG.centrality.get(maxCentralityCandidateNode) > 1.0) {
			coreNodes.add(maxCentralityCandidateNode);
			coveredNodes.add(maxCentralityCandidateNode);
			coverReachableNodes(maxCentralityCandidateNode);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void runIMCC() {
		while (coverMaxCentralityNode()) {
			;
		}
		
		checkHourglassness();
	}
	
	private void checkHourglassness() {
		double S = dependencyDAG.nSources;
		
		double sumW = 0;
		for (String s: coreNodes) {
//			System.out.println(s + "\t" + callDAG.centrality.get(s) + "\t" + callDAG.location.get(s));
			sumW += dependencyDAG.centrality.get(s);
		}
		
		System.out.println("Hourglassness: " + (sumW / (S * coreNodes.size())));
	}
	
	public void runIMCC_new() {
		for (String s: dependencyDAG.functions) {
			int flag = 1;
			for (String r: dependencyDAG.serversReachable.get(s)) {
//				if (callDAG.numOfTargetPath.get(r) > callDAG.numOfTargetPath.get(s) || callDAG.numOfSourcePath.get(r) > callDAG.numOfSourcePath.get(s)) {
				if (dependencyDAG.prTarget.get(r) > dependencyDAG.prTarget.get(s) && dependencyDAG.prSource.get(r) > dependencyDAG.prSource.get(s)) {	
					flag = 0;
					break;
				}
			}
			
			for (String r: dependencyDAG.dependentsReachable.get(s)) {
//				if (callDAG.numOfTargetPath.get(r) > callDAG.numOfTargetPath.get(s) || callDAG.numOfSourcePath.get(r) > callDAG.numOfSourcePath.get(s)) {
				if (dependencyDAG.prTarget.get(r) > dependencyDAG.prTarget.get(s) && dependencyDAG.prSource.get(r) > dependencyDAG.prSource.get(s)) {	
					flag = 0;
					break;
				}
			}
			
			if (!dependencyDAG.depends.containsKey(s) || !dependencyDAG.serves.containsKey(s)) flag = 0;
			
			if (flag == 1) {
				System.out.println(s + "\t" + dependencyDAG.centrality.get(s) + "\t" + dependencyDAG.location.get(s));
			}
		}
	}
	
	private void coverDependents(String srcNode) {
		if (!dependencyDAG.serves.containsKey(srcNode) || visited.contains(srcNode)) {
			return;
		}
		
		visited.add(srcNode);
		
		for (String tgtNode: dependencyDAG.serves.get(srcNode)) {
//			System.out.println("Adding " + srcNode + "->" + tgtNode);
			coveredLinks.add(srcNode + "->" + tgtNode);
			coverDependents(tgtNode);
		}
	}
	
	private void coverServers(String tgtNode) {
		if (!dependencyDAG.depends.containsKey(tgtNode) || visited.contains(tgtNode)) {
			return;
		}
		
		visited.add(tgtNode);
		
		for (String srcNode: dependencyDAG.depends.get(tgtNode)) {
//			System.out.println("Adding " + srcNode + "->" + tgtNode);
			coveredLinks.add(srcNode + "->" + tgtNode);
			coverServers(srcNode);
		}
	}
	
	private double coverLinks(String maxCentralityNode) {
		visited = new HashSet();
		coverDependents(maxCentralityNode);
//		System.out.println("### ### ###");
		
		visited = new HashSet();
		coverServers(maxCentralityNode);
//		System.out.println("%%% %%% %%%");
		
//		for (String s: coveredLinks) {
//			System.out.print(s + " ");
//		}
//		System.out.println();
//		System.out.println(coveredLinks.size());
		
		return coveredLinks.size() * 1.0 / dependencyDAG.nEdges;
	}
	
	public void runLinkCoverage(String filePath) throws Exception {
		PrintWriter pw = new PrintWriter(new File("analysis//" + filePath + "_pr_cover.txt"));
		coveredLinks = new HashSet();
			
		TreeMultimap<Double, String> prCentralitySortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		for (String s : dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
				prCentralitySortedNodes.put(dependencyDAG.centrality.get(s), s);
			}
		}

		int idx = 1;
		for (double prC: prCentralitySortedNodes.keySet()) {
			Collection<String> nodes = prCentralitySortedNodes.get(prC);
			for (String s: nodes) {
				double linkCoverage = coverLinks(s);
				pw.println(idx++ + "\t" +  linkCoverage + "\t" + prC + "\t" + s);
//				System.out.println(idx++ + "\t" +  linkCoverage + "\t" + "\t" + prC + "\t" + s);
				if (linkCoverage > 0.9) {
//				if (idx > 50) {
					pw.close();
					return;
				};
			}
		}
		
		pw.close();
	}	
}
