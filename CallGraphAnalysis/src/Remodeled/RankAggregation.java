package Remodeled;

import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class RankAggregation {

	static HashMap<String, Integer> generalityRank;
	static HashMap<String, Integer> complexityRank;
	static String aggregateRank[];
	
	private static void swap(String a[], int i, int j) {
		String k = a[i];
		a[i] = a[j];
		a[j] = k;
	}

	private static int isMoreCentral(String u, String v) {
		int generalityRankU = generalityRank.get(u);
		int generalityRankV = generalityRank.get(v);
		
		int complexityRankU = complexityRank.get(u);
		int complexityRankV = complexityRank.get(v);
				
		if (generalityRankU < generalityRankV && complexityRankU < complexityRankV) {
			return -1;
		}
		else if (generalityRankU > generalityRankV && complexityRankU > complexityRankV) {
			return 1;
		}
		else {
			return 0;
		}
		
//		if (u.compareTo(v) < 0) return true;
//		return false;
	}

	private static int partition(String a[], int l, int r) {
		String x = a[r];
		int i = l - 1;
		for (int j = l; j < r; ++j) {
			int position = isMoreCentral(a[j], x); 
			if (position <= 0) {
				i = i + 1;
				swap(a, i, j);
			}
			
			if (position == 0) {
				System.out.println("Conflict between " + a[j] + " " + x);
			}
		}
		swap(a, i + 1, r);
		return i + 1;
	}

	private static void quickSort(String a[], int l, int r) // FROM CORMEN USES Partition()
	{
		if (l < r) {
			int q = partition(a, l, r);
			quickSort(a, l, q - 1);
			quickSort(a, q + 1, r);
		}
	}
	
//	complexity worst case: O( n^2 ) average case: O( nLOG(n) )
//	QuickSort( a, 0, n - 1 );
	
	private static void getRanks(DependencyDAG dependencyDAG) {
		TreeMultimap<Double, String> sortedNodes = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		int knt = 0;
		for (String s : dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
				sortedNodes.put(dependencyDAG.pagerankGenerality.get(s), s);
				++knt;
			}
		}

		int idx = 1;
		generalityRank = new HashMap();
		for (double prC: sortedNodes.keySet()) {
			Collection<String> nodes = sortedNodes.get(prC);
			for (String s: nodes) {
				generalityRank.put(s, idx);
			}
			++idx;
		}
		
		sortedNodes.clear();
		for (String s : dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
				sortedNodes.put(dependencyDAG.pagerankComplexity.get(s), s);
			}
		}

		idx = 1;
		complexityRank = new HashMap();
		for (double prC: sortedNodes.keySet()) {
			Collection<String> nodes = sortedNodes.get(prC);
			for (String s: nodes) {
				complexityRank.put(s, idx);
			}
			++idx;
		}
		
		idx = 0;
		aggregateRank = new String[knt];
		for (String s: dependencyDAG.functions) {
			if (dependencyDAG.depends.containsKey(s) && dependencyDAG.serves.containsKey(s)) {
				aggregateRank[idx++] = s;
			}
		}
	}
	
	public static void aggregateRanks(DependencyDAG dependencyDAG) {
		getRanks(dependencyDAG);
		
//		System.out.print("Generality Rank:");
//		for (String s: generalityRank.keySet()) System.out.print("\t" + s);
//		System.out.println();
//
//		System.out.print("Complexity Rank:");
//		for (String s: complexityRank) System.out.print("\t" + s);
//		System.out.println();

		quickSort(aggregateRank, 0, aggregateRank.length - 1);
		
		System.out.print("Aggregated Rank:");
		for (String s: aggregateRank) System.out.print("\t" + s);
		System.out.println();

	}

//	public static void main(String args[]) {
//		String arr[] = {"bb", "ff", "aa", "dd", "cc", "ee"};
//		quickSort(arr, 0, arr.length - 1);
//		
//		for (String s: arr) System.out.print(s + "\t");
//		System.out.println();
//	}
}
