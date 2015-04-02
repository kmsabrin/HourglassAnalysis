import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class ConfigurationRandomNetwork {
	String i_deg_nm[];
	int i_deg_val[];
	
	String o_deg_nm[];
	int o_deg_val[];
	
	Random random;
	
	HashSet<String> visited;
	Boolean isReachable;
	
	// Method X: cycle check based on the fly ordering, same as configuration model
	// Method Y: global ordering of nodes to start with
	
	public void init(CallDAG callDAG) {
		i_deg_nm = new String[callDAG.functions.size()];
		i_deg_val = new int[callDAG.functions.size()];
		o_deg_nm = new String[callDAG.functions.size()];
		o_deg_val = new int[callDAG.functions.size()];
		
		int i_idx = 0;
		int o_idx = 0;
		for (String s: callDAG.functions) {
			int i_deg = callDAG.inDegree.get(s);
			int o_deg = callDAG.outDegree.get(s);
			
			if (i_deg > 0) {
				i_deg_nm[i_idx] = s;
				i_deg_val[i_idx] = i_deg;
				++i_idx;
			}
			
			if (o_deg > 0) {
				o_deg_nm[o_idx] = s;
				o_deg_val[o_idx] = o_deg;
				++o_idx;
			}
			
			if(callDAG.callTo.containsKey(s)) callDAG.callTo.get(s).clear();
			if(callDAG.callFrom.containsKey(s)) callDAG.callFrom.get(s).clear();
		}
		
		i_deg_nm = Arrays.copyOf(i_deg_nm, i_idx);
		i_deg_val = Arrays.copyOf(i_deg_val, i_idx);
		
		o_deg_nm = Arrays.copyOf(o_deg_nm, o_idx);
		o_deg_val = Arrays.copyOf(o_deg_val, o_idx);
		
		random = new Random(System.nanoTime());
	}
	
	public void generateDegreeDistributionPreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		
		while (o_deg_val.length > 0 && looped < 1000) {
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
			
			int src_edg_idx = random.nextInt(o_deg_val.length);
			int tgt_edg_idx = random.nextInt(i_deg_val.length);
			
			String src_edg_nm = o_deg_nm[src_edg_idx];
			String tgt_edg_nm = i_deg_nm[tgt_edg_idx];
			
//			System.out.println("Trying " + src_edg_nm + " to " + tgt_edg_nm);
			++looped; 
			
			if (src_edg_nm.equals(tgt_edg_nm)) {
				continue;
			}
			
			checkReachablity(tgt_edg_nm, src_edg_nm, callDAG);
			if (isReachable) {
//				try reverse direction
				int nSrcIdx = ArrayUtils.indexOf(o_deg_nm, tgt_edg_nm);
				int nTgtIdx = ArrayUtils.indexOf(i_deg_nm, src_edg_nm);

//				check feasibility, continue if not feasible
				if (nSrcIdx < 0 || nTgtIdx < 0) {
					continue;
				}

//				feasible, so swap
				src_edg_idx = nSrcIdx;
				tgt_edg_idx = nTgtIdx;
				
				src_edg_nm = o_deg_nm[src_edg_idx];
				tgt_edg_nm = i_deg_nm[tgt_edg_idx];
			}
			
//			is this correct?
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			--o_deg_val[src_edg_idx];
			if (o_deg_val[src_edg_idx] < 1) {
				o_deg_val = ArrayUtils.remove(o_deg_val, src_edg_idx);
				o_deg_nm = ArrayUtils.remove(o_deg_nm, src_edg_idx);
			}
			
			--i_deg_val[tgt_edg_idx];
			if (i_deg_val[tgt_edg_idx] < 1) {
				i_deg_val = ArrayUtils.remove(i_deg_val, tgt_edg_idx);
				i_deg_nm = ArrayUtils.remove(i_deg_nm, tgt_edg_idx);
			}
			
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
			
//			System.out.println(Arrays.toString(i_deg_nm));
//			System.out.println(Arrays.toString(o_deg_nm));
		}
	}

	public void generateDegreeDistributionPreserveMethodY(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		
		while (o_deg_val.length > 0 && looped < 10000) {
			int src_edg_idx = random.nextInt(o_deg_val.length);
			int tgt_edg_idx = random.nextInt(i_deg_val.length);
			
			String src_edg_nm = o_deg_nm[src_edg_idx];
			String tgt_edg_nm = i_deg_nm[tgt_edg_idx];
			
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
				int nSrcIdx = ArrayUtils.indexOf(o_deg_nm, tgt_edg_nm);
				int nTgtIdx = ArrayUtils.indexOf(i_deg_nm, src_edg_nm);

//				check feasibility, continue if not feasible
				if (nSrcIdx < 0 || nTgtIdx < 0) {
					continue;
				}

//				feasible, so swap
				src_edg_idx = nSrcIdx;
				tgt_edg_idx = nTgtIdx;
				
				src_edg_nm = o_deg_nm[src_edg_idx];
				tgt_edg_nm = i_deg_nm[tgt_edg_idx];
			}
			
//			if (existingEdge.contains(src_edg_nm + "#" + tgt_edg_nm)) continue;
			
			looped = 0;
			
//			existingEdge.add(src_edg_nm + "#" + tgt_edg_nm);
//			System.out.println("Adding " + src_edg_nm + " to " + tgt_edg_nm);
						
			--o_deg_val[src_edg_idx];
			if (o_deg_val[src_edg_idx] < 1) {
				o_deg_val = ArrayUtils.remove(o_deg_val, src_edg_idx);
				o_deg_nm = ArrayUtils.remove(o_deg_nm, src_edg_idx);
			}
			
			--i_deg_val[tgt_edg_idx];
			if (i_deg_val[tgt_edg_idx] < 1) {
				i_deg_val = ArrayUtils.remove(i_deg_val, tgt_edg_idx);
				i_deg_nm = ArrayUtils.remove(i_deg_nm, tgt_edg_idx);
			}
			
			callDAG.callTo.get(src_edg_nm).add(tgt_edg_nm);
			callDAG.callFrom.get(tgt_edg_nm).add(src_edg_nm);
			
//			System.out.println(Arrays.toString(i_deg_nm));
//			System.out.println(Arrays.toString(o_deg_nm));
		}
	}
	
	public void generateOutDegreeDistributionPreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet();
		
		int looped = 0;
		
		ArrayList<String> functionNameList = new ArrayList(callDAG.functions);
		
		while (o_deg_val.length > 0 && looped < 10000) {
			int src_edg_idx = random.nextInt(o_deg_val.length);
//			int tgt_edg_idx = random.nextInt(i_deg_val.length);
			
			String src_edg_nm = o_deg_nm[src_edg_idx];
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
						
			--o_deg_val[src_edg_idx];
			if (o_deg_val[src_edg_idx] < 1) {
				o_deg_val = ArrayUtils.remove(o_deg_val, src_edg_idx);
				o_deg_nm = ArrayUtils.remove(o_deg_nm, src_edg_idx);
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

	public void generateNumEdgePreserveMethodX(CallDAG callDAG) {
		HashSet<String> existingEdge = new HashSet();
		int looped = 0;
		ArrayList<String> functionNameList = new ArrayList(callDAG.functions);
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
		HashSet<String> existingEdge = new HashSet();		
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
		HashSet<String> existingEdgeX = new HashSet();
		HashSet<String> existingEdgeY = new HashSet();
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
			
			ArrayList<Integer> inStubsID = new ArrayList();
			ArrayList<Integer> inStubsDegree = new ArrayList();
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

	public void generateLayeredNewRandomization(CallDAG callDAG) {
		
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
		visited =  new HashSet();
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
