package initial_2;

import java.io.File;
import java.io.PrintWriter;
import java.io.ObjectInputStream.GetField;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import corehg.DependencyDAG;

public class WalktrapAnalysis {
//	static String cg = "commons-math";
	static String cg = "openssh-39";
	
	public static void getWalktrapModules(DependencyDAG callDAG, String versionNum) throws Exception {	
		Scanner scanner = new Scanner(new File("edgelist_graphs//w5"  + "-" + cg + ".txt"));
		PrintWriter pw = new PrintWriter(new File("Results//communities-" + cg + ".txt"));
		
		int communityID = 1;
		scanner.nextLine(); // skip first line
		while (scanner.hasNextLine()) {
			String str = scanner.nextLine();
			str = str.replaceAll(" ", "");
			str = str.substring(str.indexOf('{') + 1, str.indexOf('}'));
			String val[] = str.split(",");	
		
//			if (val.length < nCommunitySizeThreshold) continue;

			System.out.println(val.length);
			Set<String> communityFunctions = new HashSet<String>();
			String cID = "C" + communityID;
			
			pw.print(cID);
			
//			double avgModGen = 0;
//			double avgLoc = 0;
//			double avgGen = 0;
//			for(String r: val) {
//				int id = Integer.parseInt(r);
//				String f = callDAG.IDFunction.get(id);
//				communityFunctions.add(f);
//				pw.print("\t" + f);
//				
//				double loc = callDAG.location.get(f);
//				
//				double modGen = callDAG.generality.get(f); 
////				if (loc > 0.3 && loc < 0.5 && modGen > 0.1) {
////					System.out.print(f + "\t");
////				}
//				
//				avgModGen += modGen;
//				avgLoc += loc;
//				avgGen += callDAG.generality.get(f);
//			}
////			System.out.print((avgModGen / communityFunctions.size()) + "\t" + (avgLoc / communityFunctions.size()));
////			System.out.println("\t" + communityFunctions.size() + "\t" + cID);
//			pw.println();
//			
//			communities.put(cID, communityFunctions);
//			
//			avgLoc /= communityFunctions.size();
//			avgLoc = ((int) (avgLoc * 100.0)) / 100.0; // 2 decimal rounding
//			communitiesAvgLocation.put(cID, avgLoc);
//			
//			avgGen /= communityFunctions.size();
//			avgGen = ((int) (avgGen * 100.0)) / 100.0; // 2 decimal rounding
//			communitiesAvgGenerality.put(cID, avgGen);
			
			++communityID;
		}
		
//		nCommunityNetoworkEdge = 0;
//		for (String s: communities.keySet()) {
//			Set<String> currentComm = communities.get(s);
//			double inDeg = 0, outDeg = 0;
//			for (String r: currentComm) {
//				if (callDAG.callTo.containsKey(r)) {
//					for (String t: callDAG.callTo.get(r)) {
//						if (!currentComm.contains(t)) ++outDeg;
//					}
//				}
//				if (callDAG.callFrom.containsKey(r)) {
//					for (String t: callDAG.callFrom.get(r)) {
//						if (!currentComm.contains(t)) ++inDeg;
//					}
//				}
//			}
//			communitiesInDeg.put(s, inDeg);
//			communitiesOutDeg.put(s, outDeg);
//			nCommunityNetoworkEdge += outDeg;
//		}

		System.out.println("nCommunities: " + (communityID - 1));
		
//		getLeafStatistics(callDAG);
//		getCommunityHistogram(callDAG);
//		checkCommunityHourglassShape(callDAG, versionNum);
//		getCommunityNetworkStats(callDAG, versionNum);
//		getNonrepresentativeCommunityNode(callDAG, versionNum);
//		getNonrepresentativeCommunities(callDAG);
		
		scanner.close();
		pw.close();
	}
	
	public static void main(String[] args) throws Exception {
		getWalktrapModules(null, null);
	}
}
