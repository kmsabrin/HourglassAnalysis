package Initial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class IteratedMaxCentralityCoverage {
	HashSet<String> coveredNodes;
	ArrayList<String> coreNodes;
	CallDAG callDAG;
	Random random;
	
	public IteratedMaxCentralityCoverage(CallDAG callDAG) {
		coveredNodes = new HashSet<String>();
		coreNodes = new ArrayList<String>();
		this.callDAG = callDAG;
		random = new Random(System.nanoTime());
	}
	
	// Edges (calls) going downward, 
	private boolean checkCandidateNodeValidity(String candidateNode) {
		double candidateNodeCentrality = callDAG.centrality.get(candidateNode);
		
		for (String s: callDAG.nodesReachableUpwards.get(candidateNode)) {
			if (callDAG.centrality.get(s) > candidateNodeCentrality) {
//				System.out.println("Upward violation by: " + s + " " + callDAG.centrality.get(s));
				return false;
			}
			
//			if (callDAG.prFromSource.get(s) > callDAG.prFromSource.get(candidateNode)) {
////				System.out.println("Upward violation by: " + s + " " + callDAG.numOfSourcePath.get(s));
//				return false;
//			}
		}
		
		for (String s: callDAG.nodesReachableDownwards.get(candidateNode)) {
			if (callDAG.centrality.get(s) > candidateNodeCentrality) {
//				System.out.println("Downward violation by: " + s + " " + callDAG.centrality.get(s));
				return false;
			}
			
//			if (callDAG.prToTarget.get(s) > callDAG.prToTarget.get(candidateNode)) {
////				System.out.println("Downward violation by: " + s + " " + callDAG.numOfTargetPath.get(s));
//				return false;
//			}
		}
		
		return true;
	}
	
	private void coverReachableNodes(String candidateNode) {
		for (String s: callDAG.nodesReachableUpwards.get(candidateNode)) {
			coveredNodes.add(s);
		}
		
		for (String s: callDAG.nodesReachableDownwards.get(candidateNode)) {
			coveredNodes.add(s);
		}
	}
	
	private String getMaxCentralityNode() {
		ArrayList<String> maxCentralityNodes = new ArrayList<String>();
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
		
//		System.out.print("Max Centrality Candidates: ");
//		for (String s: maxCentralityNodes) {
//			System.out.print(s + " ");
//		}
//		System.out.println();
		
		
		
		return maxCentralityNodes.get(random.nextInt(maxCentralityNodes.size()));
	}
	
	private boolean coverMaxCentralityNode() {
		String maxCentralityCandidateNode = getMaxCentralityNode();

		if (maxCentralityCandidateNode == null) return false;
		
//		System.out.println("Candidate: " + maxCentralityCandidateNode + " " + callDAG.centrality.get(maxCentralityCandidateNode));
		
//		if (!checkCandidateNodeValidity(maxCentralityCandidateNode)) {
//			coveredNodes.add(maxCentralityCandidateNode);
//		}		
		if (callDAG.centrality.get(maxCentralityCandidateNode) > 1.0) {
			coreNodes.add(maxCentralityCandidateNode);
			coveredNodes.add(maxCentralityCandidateNode);
			coverReachableNodes(maxCentralityCandidateNode);
//			System.out.println("Added to Core Node: " + maxCentralityCandidateNode);
			
//			System.out.print("Covered Nodes: ");
//			for (String s: coveredNodes) {
//				System.out.print(s + " ");
//			}
//			System.out.println();
//			System.out.println("Covered Size: " + coveredNodes.size());
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
			
			if (!callDAG.callTo.containsKey(s) || !callDAG.callFrom.containsKey(s)) flag = 0;
			
			if (flag == 1) {
				System.out.println(s + "\t" + callDAG.centrality.get(s) + "\t" + callDAG.location.get(s));
			}
		}
	}
}
