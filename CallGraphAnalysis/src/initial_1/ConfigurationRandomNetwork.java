package initial_1;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class ConfigurationRandomNetwork {
	String inDegreeName[];
	int inDegreeValue[];
	
	String outDegreeName[];
	int outDegreeValue[];
	
	HashMap<String, Integer> inDegreeCurrent;
	HashMap<String, Integer> outDegreeCurrent;
	
	Random random;
	
	String availableFunctionName[];
	
	HashSet<String> visited;
	Boolean isReachable;
	
	// Method X: cycle check based on the fly ordering, same as configuration model
	// Method Y: global ordering of nodes to start with
	
	public void init(CallDAG callDAG) {
		inDegreeName = new String[callDAG.functions.size()];
		inDegreeValue = new int[callDAG.functions.size()];
		outDegreeName = new String[callDAG.functions.size()];
		outDegreeValue = new int[callDAG.functions.size()];
		availableFunctionName = new String[callDAG.functions.size()];
		
		inDegreeCurrent = new HashMap<String, Integer>();
		outDegreeCurrent = new HashMap<String, Integer>();
		
		int i_idx = 0;
		int o_idx = 0;
		int f_idx = 0;
		for (String s: callDAG.functions) {
			availableFunctionName[f_idx++] = s;
			
			int inDeg = callDAG.inDegree.get(s);
			int outDeg = callDAG.outDegree.get(s);
			
			if (inDeg > 0) {
				inDegreeName[i_idx] = s;
				inDegreeValue[i_idx] = inDeg;
				++i_idx;
				inDegreeCurrent.put(s, inDeg);
			}
			
			if (outDeg > 0) {
				outDegreeName[o_idx] = s;
				outDegreeValue[o_idx] = outDeg;
				++o_idx;
				outDegreeCurrent.put(s, outDeg);
			}
			
			if(callDAG.callTo.containsKey(s)) callDAG.callTo.get(s).clear();
			if(callDAG.callFrom.containsKey(s)) callDAG.callFrom.get(s).clear();
		}
		
		inDegreeName = Arrays.copyOf(inDegreeName, i_idx);
		inDegreeValue = Arrays.copyOf(inDegreeValue, i_idx);
		
		outDegreeName = Arrays.copyOf(outDegreeName, o_idx);
		outDegreeValue = Arrays.copyOf(outDegreeValue, o_idx);
		
		random = new Random(System.nanoTime());
	}
	
	public void generateDegreeDistributionPreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet<String>();
		
		int looped = 0;
		
		while (outDegreeValue.length > 0 && looped < 1000) {
//			System.out.print("Outdeg availability: ");
//			for (int i = 0; i < o_deg_val.length; ++i) {
//				System.out.print(o_deg_nm[i] + "," + o_deg_val[i] + " ");
//			}
//			System.out.println();
//
//			System.out.print("Indeg availability: ");
//			for (int i = 0; i < i_deg_val.length; ++i) {
//				System.out.print(i_deg_nm[i] + "," + i_deg_val[i] + " ");
//			}
//			System.out.println();
			
			int src_edg_idx = random.nextInt(outDegreeValue.length);
			int tgt_edg_idx = random.nextInt(inDegreeValue.length);
			
			String src_edg_nm = outDegreeName[src_edg_idx];
			String tgt_edg_nm = inDegreeName[tgt_edg_idx];
			
//			System.out.println("Trying " + src_edg_nm + " to " + tgt_edg_nm);
			++looped; 
			
			if (src_edg_nm.equals(tgt_edg_nm)) {
				continue;
			}
			
			checkReachablity(tgt_edg_nm, src_edg_nm, callDAG);
			if (isReachable) {
//				try reverse direction
				int nSrcIdx = ArrayUtils.indexOf(outDegreeName, tgt_edg_nm);
				int nTgtIdx = ArrayUtils.indexOf(inDegreeName, src_edg_nm);

//				check feasibility, continue if not feasible
				if (nSrcIdx < 0 || nTgtIdx < 0) {
					continue;
				}

//				feasible, so swap
				src_edg_idx = nSrcIdx;
				tgt_edg_idx = nTgtIdx;
				
				src_edg_nm = outDegreeName[src_edg_idx];
				tgt_edg_nm = inDegreeName[tgt_edg_idx];
			}
			
//			is this correct?
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			--outDegreeValue[src_edg_idx];
			if (outDegreeValue[src_edg_idx] < 1) {
				outDegreeValue = ArrayUtils.remove(outDegreeValue, src_edg_idx);
				outDegreeName = ArrayUtils.remove(outDegreeName, src_edg_idx);
			}
			
			--inDegreeValue[tgt_edg_idx];
			if (inDegreeValue[tgt_edg_idx] < 1) {
				inDegreeValue = ArrayUtils.remove(inDegreeValue, tgt_edg_idx);
				inDegreeName = ArrayUtils.remove(inDegreeName, tgt_edg_idx);
			}
			
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
			
//			System.out.println(Arrays.toString(i_deg_nm));
//			System.out.println(Arrays.toString(o_deg_nm));
		}
	}

	public void generateDegreeDistributionPreserveMethodY(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet<String>();
		
		int looped = 0;
		
		while (outDegreeValue.length > 0 && looped < 10000) {
			int src_edg_idx = random.nextInt(outDegreeValue.length);
			int tgt_edg_idx = random.nextInt(inDegreeValue.length);
			
			String src_edg_nm = outDegreeName[src_edg_idx];
			String tgt_edg_nm = inDegreeName[tgt_edg_idx];
			
			int src_val = Integer.parseInt(src_edg_nm);
			int tgt_val = Integer.parseInt(tgt_edg_nm);
			
//			System.out.println("Trying " + src_val + " to " + tgt_val);
			++looped; 
			
			if (src_val == tgt_val) {
				continue;
			}
			
			if (src_val > tgt_val) {
//				continue;
				
//				try reverse direction
				int nSrcIdx = ArrayUtils.indexOf(outDegreeName, tgt_edg_nm);
				int nTgtIdx = ArrayUtils.indexOf(inDegreeName, src_edg_nm);

//				check feasibility, continue if not feasible
				if (nSrcIdx < 0 || nTgtIdx < 0) {
					continue;
				}

//				feasible, so swap
				src_edg_idx = nSrcIdx;
				tgt_edg_idx = nTgtIdx;
				
				src_edg_nm = outDegreeName[src_edg_idx];
				tgt_edg_nm = inDegreeName[tgt_edg_idx];
			}
			
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			--outDegreeValue[src_edg_idx];
			if (outDegreeValue[src_edg_idx] < 1) {
				outDegreeValue = ArrayUtils.remove(outDegreeValue, src_edg_idx);
				outDegreeName = ArrayUtils.remove(outDegreeName, src_edg_idx);
			}
			
			--inDegreeValue[tgt_edg_idx];
			if (inDegreeValue[tgt_edg_idx] < 1) {
				inDegreeValue = ArrayUtils.remove(inDegreeValue, tgt_edg_idx);
				inDegreeName = ArrayUtils.remove(inDegreeName, tgt_edg_idx);
			}
			
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
			
//			System.out.println(Arrays.toString(i_deg_nm));
//			System.out.println(Arrays.toString(o_deg_nm));
		}
	}
	
	/*
	public void generateOutDegreeDistributionPreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		
		ArrayList<String> functionNameList = new ArrayList(callDAG.functions);
		
		while (outDegreeValue.length > 0 && looped < 10000) {
			int src_edg_idx = random.nextInt(outDegreeValue.length);
//			int tgt_edg_idx = random.nextInt(i_deg_val.length);
			
			String src_edg_nm = outDegreeName[src_edg_idx];
			String tgt_edg_nm = functionNameList.get(random.nextInt(functionNameList.size()));
//			String tgt_edg_nm = i_deg_nm[tgt_edg_idx];
			
//			System.out.println("Trying " + src_edg_nm + " to " + tgt_edg_nm);
			
			++looped; 
			if (src_edg_nm.equals(tgt_edg_nm)) {
				continue;
			}
			
			checkReachablity(tgt_edg_nm, src_edg_nm, callDAG);
			if (isReachable) {
				continue;
			}
			
			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			// extra check preserve roots
			if (callDAG.inDegree.get(tgt_edg_nm) < 1) continue;
			
			
			looped = 0;
			
			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			--outDegreeValue[src_edg_idx];
			if (outDegreeValue[src_edg_idx] < 1) {
				outDegreeValue = ArrayUtils.remove(outDegreeValue, src_edg_idx);
				outDegreeName = ArrayUtils.remove(outDegreeName, src_edg_idx);
			}
			
//			--i_deg_val[tgt_edg_idx];
//			if (i_deg_val[tgt_edg_idx] < 1) {
//				i_deg_val = ArrayUtils.remove(i_deg_val, tgt_edg_idx);
//				i_deg_nm = ArrayUtils.remove(i_deg_nm, tgt_edg_idx);
//			}
			
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			
			if (!callDAG.callFrom.containsKey(tgt_edg_nm)) callDAG.callFrom.put(tgt_edg_nm, new HashSet());
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
			
//			System.out.println(Arrays.toString(i_deg_nm));
//			System.out.println(Arrays.toString(o_deg_nm));
		}
	}
*/
	
	public void generateNumEdgePreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet<String>();
		int looped = 0;
		ArrayList<String> functionNameList = new ArrayList<String>(callDAG.functions);
		int allowedEdges = callDAG.nEdges;
		
		System.out.println(functionNameList.size() + "\t" + allowedEdges);
		
		while (allowedEdges > 0 && looped < 10000) {
			String src_edg_nm = functionNameList.get(random.nextInt(functionNameList.size()));
			String tgt_edg_nm = functionNameList.get(random.nextInt(functionNameList.size()));

//			System.out.println("Trying " + src_edg_nm + " to " + tgt_edg_nm);
			
			++looped;
			
			// check self loop
			if (src_edg_nm.equals(tgt_edg_nm)) {
				continue;
			}
			
			// check acyclicity
			checkReachablity(tgt_edg_nm, src_edg_nm, callDAG);
			if (isReachable) {
				continue;
//				String tmp_nm = src_edg_nm;
//				src_edg_nm = tgt_edg_nm;
//				tgt_edg_nm = tmp_nm;
			}
			
			// check multi edge
			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
//			// check preserve roots
//			if (callDAG.inDegree.get(tgt_edg_nm) < 1) continue;
//			
//			// check preserve leaves
//			if (callDAG.outDegree.get(src_edg_nm) < 1) continue;
						
			--allowedEdges;
			looped = 0;
			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			if (!callDAG.callTo.containsKey(src_edg_nm)) callDAG.callTo.put(src_edg_nm, new HashSet());
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			if (!callDAG.callFrom.containsKey(tgt_edg_nm)) callDAG.callFrom.put(tgt_edg_nm, new HashSet());
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
		}
	}
	
	public void generateNumEdgePreserveMethodY(CallDAG callDAG) throws Exception {
		HashSet<String> existingEdge = new HashSet<String>();		
		int allowedEdges = callDAG.nEdges;
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//hourglassDAGrX.txt"));
//		allowedEdges = 6000;
		int n = 7756;
		while (allowedEdges > 0) {
			int src = random.nextInt(n);
			int tgt = random.nextInt(n);
		
			// check self loop
			if (src == tgt) {
				continue;
			}
			
			// check acyclicity
			if (src > tgt) { int tmp = src; src = tgt; tgt = tmp; }
			
			// check multi edge
			if (existingEdge.contains(src + "#" + tgt)) continue;
					
			--allowedEdges;
			existingEdge.add(src + "#" + tgt);
//			System.out.println("Adding " + src + " to " + tgt);
			
			pw.println(src + " -> " + tgt + ";");
		}
		pw.close();		
	}
	
	public void generateNumEdgePreserveMethodXY() throws Exception {
		CallDAG callDAG = new CallDAG();
		HashSet<String> existingEdgeX = new HashSet<String>();
		HashSet<String> existingEdgeY = new HashSet<String>();
		PrintWriter pwX = new PrintWriter(new File("artificial_callgraphs//methodX.txt"));
		PrintWriter pwY = new PrintWriter(new File("artificial_callgraphs//methodY.txt"));
		
		Random rnd = new Random(System.nanoTime());
		
		int allowedEdges = 10;
		int n = 6;
		
		while (allowedEdges > 0) {
			int src = rnd.nextInt(n);
			int tgt = rnd.nextInt(n);
			String src_str = src + "";
			String tgt_str = tgt + "";
		
			// check self loop
			if (src == tgt) {
				continue;
			}
		
//			check acyclicity
			if (src > tgt) { 
				int tmp = src; src = tgt; tgt = tmp; 
			
			}
		
			// check acyclicity
			checkReachablity(tgt_str, src_str, callDAG);
			if (isReachable) {
				String tmp_nm = src_str;
				src_str = tgt_str;
				tgt_str = tmp_nm;
			}
						
			// check multi edge
			if (existingEdgeX.contains(src + "#" + tgt)) {
				if (!existingEdgeY.contains(src_str + "#" + tgt_str)) {
					System.out.println("ERROR !!!");
				}
				continue;
			}
			
			existingEdgeX.add(src + "#" + tgt);
			
			existingEdgeY.add(src_str + "#" + tgt_str);
			if (!callDAG.callTo.containsKey(src_str)) callDAG.callTo.put(src_str, new HashSet());
			callDAG.callTo.get(src_str).add(tgt_str);
			if (!callDAG.callFrom.containsKey(tgt_str)) callDAG.callFrom.put(tgt_str, new HashSet());
			callDAG.callFrom.get(tgt_str).add(src_str);

//			System.out.println("Adding " + src + " to " + tgt);
			--allowedEdges;
			pwX.println(src + " -> " + tgt + ";");
			pwY.println(src_str + " -> " + tgt_str + ";");
		}
		
		pwX.close();
		pwY.close();
	}
	
	public void generateKNRandomDAG(CallDAG callDAG) {
		int maxN = 10000 + 1000;
		
		for (int srcIdx = maxN; srcIdx > 0; --srcIdx) {
			String src = srcIdx + "";
			if (!callDAG.functions.contains(src)) continue;
			
			int oDegSrc = callDAG.outDegree.get(src);
			if (oDegSrc < 1) continue;
			
//			System.out.println("Working on " + srcIdx + " with outDeg " + oDegSrc);
			
			ArrayList<Integer> inStubsID = new ArrayList<Integer>();
			ArrayList<Integer> inStubsDegree = new ArrayList<Integer>();
			for (int inStubIdx = srcIdx + 1; inStubIdx <= maxN; ++inStubIdx) {
				String node = inStubIdx + "";
				if (!callDAG.functions.contains(node)) continue;
				int iDegNode = callDAG.inDegree.get(node);
				if (iDegNode < 1) continue;
				inStubsID.add(inStubIdx);
				inStubsDegree.add(iDegNode);
//				System.out.println("Available " + inStubIdx + " with inDeg " + iDegNode);
			}
			
			for (int j = 1; j <= oDegSrc; ++j) {
				int idx = random.nextInt(inStubsID.size());
				int tgtId = inStubsID.get(idx);
				String tgt = tgtId + "";
				
				int iDegTgt = inStubsDegree.get(idx);
				inStubsDegree.set(idx, iDegTgt - 1);
				callDAG.inDegree.put(tgt, iDegTgt - 1);
				
				callDAG.callTo.get(src).add(tgt);
				
				if (iDegTgt == 1) {
					inStubsID.remove(idx);
					inStubsDegree.remove(idx);
				}
//				System.out.println("Adding: " + src + " to " + tgt);
//				System.out.println("Making: " + tgt + " inDeg " + (iDegTgt - 1));
				
			}
		}
	}

	private int getLayer(int id) {
		
//		Total Nodes used: 10013 Rectangle
//		int layerDistribution[] = {527,1054,1581,2108,2635,3162,3689,4216,4743,5270,5797,6324,6851,7378,7905,8432,8959,9486,10013};

//		Total Nodes used: 10668 NoisyRectangle
		int layerDistribution[] = {2890,4999,6539,7663,8483,9081,9518,9837,10069,10239,10363,10453,10519,10567,10602,10627,10645,10658,10668};

//		Total Nodes used: 10244 Hourglass
//		int layerDistribution[] = {2839,4106,4671,4923,5035,5085,5107,5117,5121,5123,5127,5137,5159,5209,5321,5573,6138,7405,10244};
				
//		Total Nodes used: 10010 Trapezoid
//		int layerDistribution[] = {3535,5868,7408,8424,9095,9537,9829,10022,10149,10233,10288,10324,10348,10363,10373,10379,10383,10386,10388};
		
//		Total Nodes used: 10368 Diamond
//		int layerDistribution[] = {2,6,16,41,99,236,556,1301,3038,7085,8822,9567,9887,10024,10082,10107,10117,10121,10123};
		
		int layer = 1;
		for (int val: layerDistribution) {
			if (id < val) {
				return layer;
			}
			else ++layer;
		}
		
		return -1;
	}
	
	public void generateLayeredNewRandomization(CallDAG callDAG) {
//		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		int edgeKount = 0;
		
		while (availableFunctionName.length > 0 && looped < 10000) {
			++looped; 
			
			int seedIndex = random.nextInt(availableFunctionName.length);
			String seedName = availableFunctionName[seedIndex];
			
			double seedInDegree = 0;
			double seedOutDegree = 0;
			if (inDegreeCurrent.containsKey(seedName)) seedInDegree = inDegreeCurrent.get(seedName);
			if (outDegreeCurrent.containsKey(seedName)) seedOutDegree = outDegreeCurrent.get(seedName);
			
			if (seedInDegree == 0 && seedOutDegree == 0) { // function covered
				ArrayUtils.remove(availableFunctionName, seedIndex);
				continue;
			}
			
			double directionIndicatorRatio = seedInDegree / (seedInDegree + seedOutDegree);
			
//			System.out.println("Trying Seed " + seedName + " with ratio " + directionIndicatorRatio);
			
			String sourceEdgeName;
			String targetEdgeName;
			
			if (random.nextDouble() < directionIndicatorRatio) { // make seed a target
				// find source if any
				ArrayList<Integer> potentialSources = new ArrayList<Integer>();
				for (int i = 1; i < Integer.parseInt(seedName) && getLayer(i) != getLayer(Integer.parseInt(seedName)); ++i) {
					if (outDegreeCurrent.containsKey(Integer.toString(i))) {
						potentialSources.add(i);
					}
				}
				
				if (potentialSources.size() < 1) continue;
				
				sourceEdgeName = Integer.toString(potentialSources.get(random.nextInt(potentialSources.size()))); 
				targetEdgeName = seedName;
			}
			else { // make seed a source
				// find target if any
				ArrayList<Integer> potentialTargets = new ArrayList<Integer>();
				for (int i = 10000 + 1000; i > Integer.parseInt(seedName) && getLayer(i) != getLayer(Integer.parseInt(seedName)); --i) {
					if (inDegreeCurrent.containsKey(Integer.toString(i))) {
						potentialTargets.add(i);
					}
				}
				
				if (potentialTargets.size() < 1) continue;
				
				targetEdgeName = Integer.toString(potentialTargets.get(random.nextInt(potentialTargets.size()))); 
				sourceEdgeName = seedName;
			}
						
			
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
			
//			System.out.println("Adding " + sourceEdgeName + " to " + targetEdgeName);
//			System.out.println(edgeKount++);
			
			int updatedSourceOutDegree = outDegreeCurrent.get(sourceEdgeName) - 1;
			if (updatedSourceOutDegree < 1) outDegreeCurrent.remove(sourceEdgeName);
			else outDegreeCurrent.put(sourceEdgeName, updatedSourceOutDegree);
			
			int updatedTargetInDegree = inDegreeCurrent.get(targetEdgeName) - 1;
			if (updatedTargetInDegree < 1) inDegreeCurrent.remove(targetEdgeName);
			else inDegreeCurrent.put(targetEdgeName, updatedTargetInDegree);
			
			callDAG.callTo.get(sourceEdgeName).add(targetEdgeName);
			callDAG.callFrom.get(targetEdgeName).add(sourceEdgeName);
		}	
	}
	
	public void generateNonLayeredNewRandomization(CallDAG callDAG) {
//		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		int edgeKount = 0;
		
		while (availableFunctionName.length > 0 && looped < 10000) {
			++looped; 
			
			int seedIndex = random.nextInt(availableFunctionName.length);
			String seedName = availableFunctionName[seedIndex];
			
			double seedInDegree = 0;
			double seedOutDegree = 0;
			if (inDegreeCurrent.containsKey(seedName)) seedInDegree = inDegreeCurrent.get(seedName);
			if (outDegreeCurrent.containsKey(seedName)) seedOutDegree = outDegreeCurrent.get(seedName);
			
			if (seedInDegree == 0 && seedOutDegree == 0) { // function covered
				ArrayUtils.remove(availableFunctionName, seedIndex);
				continue;
			}
			
			double directionIndicatorRatio = seedInDegree / (seedInDegree + seedOutDegree);
			
//			System.out.println("Trying Seed " + seedName + " with ratio " + directionIndicatorRatio);
			
			String sourceEdgeName;
			String targetEdgeName;
			
			if (random.nextDouble() < directionIndicatorRatio) { // make seed a target
				// find source if any
//				System.out.print("Potential sources for " + seedName + ": ");
				ArrayList<String> potentialSources = new ArrayList<String>();
				for (String s: callDAG.nodesReachableUpwards.get(seedName)) {
//					System.out.print(s + "\t");
					if (outDegreeCurrent.containsKey(s)) {
						potentialSources.add(s);
					}
				}
//				System.out.println();
				
				if (potentialSources.size() < 1) continue;
				
				sourceEdgeName = potentialSources.get(random.nextInt(potentialSources.size())); 
				targetEdgeName = seedName;
			}
			else { // make seed a source
				// find target if any
//				System.out.print("Potential targets for " + seedName + ": ");
				ArrayList<String> potentialTargets = new ArrayList<String>();
				for (String s: callDAG.nodesReachableDownwards.get(seedName)) {
//					System.out.print(s + "\t");
					if (inDegreeCurrent.containsKey(s)) {
						potentialTargets.add(s);
					}
				}
//				System.out.println();
				
				if (potentialTargets.size() < 1) continue;
				
				targetEdgeName = potentialTargets.get(random.nextInt(potentialTargets.size())); 
				sourceEdgeName = seedName;
			}
						
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
			
//			System.out.println("Adding " + sourceEdgeName + " to " + targetEdgeName);
//			System.out.println(edgeKount++);
			
			int updatedSourceOutDegree = outDegreeCurrent.get(sourceEdgeName) - 1;
			if (updatedSourceOutDegree < 1) outDegreeCurrent.remove(sourceEdgeName);
			else outDegreeCurrent.put(sourceEdgeName, updatedSourceOutDegree);
			
			int updatedTargetInDegree = inDegreeCurrent.get(targetEdgeName) - 1;
			if (updatedTargetInDegree < 1) inDegreeCurrent.remove(targetEdgeName);
			else inDegreeCurrent.put(targetEdgeName, updatedTargetInDegree);
			
			callDAG.callTo.get(sourceEdgeName).add(targetEdgeName);
			callDAG.callFrom.get(targetEdgeName).add(sourceEdgeName);
		}	
	}
	
	public void checkReachablityTraverse(String node, String target, CallDAG callDAG) {
		if (node.equals(target)) { // target Found, reachable, cycle created
			isReachable = true;
			return;
		}
		
		if (isReachable) return; // target Already Found
		if (visited.contains(node)) return; // already Traversed
		
		visited.add(node);
				
		if (!callDAG.callTo.containsKey(node)) return; // a leaf
		
		for (String f: callDAG.callTo.get(node)) {
			checkReachablityTraverse(f, target, callDAG);
		}
	}
	
	public void checkReachablity(String source, String target, CallDAG callDAG) {
		visited =  new HashSet<String>();
		isReachable = false;
		checkReachablityTraverse(source, target, callDAG);
	}
	
	public void writeRandomDAG(CallDAG callDAG, String randomVersionNumber) throws Exception {
		PrintWriter pw = new PrintWriter(new File("artificial_callgraphs//" + randomVersionNumber + ".txt"));
		for (String s: callDAG.functions) {
			if (callDAG.callTo.containsKey(s)) {
				for (String r : callDAG.callTo.get(s)) {
					pw.println(s + " -> " + r + ";");
				}
			}
		}
		pw.close();
	}
}
