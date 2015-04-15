package Remodeled;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class IteratedMaxCentralityCoverage {
	HashSet<String> coveredNodes;
	ArrayList<String> coreNodes;
	CallDAG callDAG;
	Random random;
	
	public IteratedMaxCentralityCoverage(CallDAG callDAG) {
		coveredNodes = new HashSet();
		coreNodes = new ArrayList();
		this.callDAG = callDAG;
		random = new Random(System.nanoTime());
	}
	
	private void coverReachableNodes(String candidateNode) {
		for (String s: callDAG.dependentsReachable.get(candidateNode)) {
			coveredNodes.add(s);
		}
		
		for (String s: callDAG.serversReachable.get(candidateNode)) {
			coveredNodes.add(s);
		}
	}
	
	private String getMaxCentralityNode() {
		ArrayList<String> maxCentralityNodes = new ArrayList();
		double maxCentrality = -1;
		
		for (String s: callDAG.functions) {
			if (coveredNodes.contains(s)) {
				continue;
			}
			
			double centrality = callDAG.centrality.get(s);
			
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

		if (callDAG.centrality.get(maxCentralityCandidateNode) > 1.0) {
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
		double S = callDAG.nSources;
		
		double sumW = 0;
		for (String s: coreNodes) {
//			System.out.println(s + "\t" + callDAG.centrality.get(s) + "\t" + callDAG.location.get(s));
			sumW += callDAG.centrality.get(s);
		}
		
		System.out.println("Hourglassness: " + (sumW / (S * coreNodes.size())));
	}
	
	/*
	public void runIMCC_new() {
		for (String s: callDAG.functions) {
			int flag = 1;
			for (String r: callDAG.nodesReachableDownwards.get(s)) {
//				if (callDAG.numOfTargetPath.get(r) > callDAG.numOfTargetPath.get(s) || callDAG.numOfSourcePath.get(r) > callDAG.numOfSourcePath.get(s)) {
				if (callDAG.prToTarget.get(r) > callDAG.prToTarget.get(s) && callDAG.prFromSource.get(r) > callDAG.prFromSource.get(s)) {	
					flag = 0;
					break;
				}
			}
			
			for (String r: callDAG.nodesReachableUpwards.get(s)) {
//				if (callDAG.numOfTargetPath.get(r) > callDAG.numOfTargetPath.get(s) || callDAG.numOfSourcePath.get(r) > callDAG.numOfSourcePath.get(s)) {
				if (callDAG.prToTarget.get(r) > callDAG.prToTarget.get(s) && callDAG.prFromSource.get(r) > callDAG.prFromSource.get(s)) {	
					flag = 0;
					break;
				}
			}
			
			if (!callDAG.depends.containsKey(s) || !callDAG.serves.containsKey(s)) flag = 0;
			
			if (flag == 1) {
				System.out.println(s + "\t" + callDAG.centrality.get(s) + "\t" + callDAG.location.get(s));
			}
		}
	}
	*/
}
