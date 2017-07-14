package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.stat.StatUtils;

import neuro.ManagerNeuro;

public class SocialRankAnalysis {
//	static String directory = "metabolic_networks";
//	static String network_file = "rat-links.txt";
//	static String network_id = "rat";
	
	static String directory = "neuro_networks";
//	static String network_file = "celegans_graph.txt";
	static String network_file = "celegans_network_clean.txt";
	static String network_id = "celegans";
//	static String network_id = "celegans_no_filter";
	
	static boolean randomizationTest = false;
	static Random random;
	
	public static void getDataForSocialrankAnalysis() throws Exception {
		HashMap<String, Integer> labelIdMap = new HashMap();
		
		Scanner scanner = new Scanner(new File(directory + "//" + network_file));
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".edges"));
		int id = 0;
		while (scanner.hasNext()) {
			String server = scanner.next();
			String dependent = scanner.next();
//			String weight = scanner.next();
			if (!labelIdMap.containsKey(server)) {
				labelIdMap.put(server, id++);
			}
			if (!labelIdMap.containsKey(dependent)) {
				labelIdMap.put(dependent, id++);
			}
			
			if (!randomizationTest) {
//				System.out.println(labelIdMap.get(server) + "\t" + labelIdMap.get(dependent));
				pw.println(labelIdMap.get(server) + "\t" + labelIdMap.get(dependent));
			}
			else {
				if (random.nextDouble() > 0.5){
					pw.println(labelIdMap.get(server) + "\t" + labelIdMap.get(dependent));
				}
				else {
					pw.println(labelIdMap.get(dependent) + "\t" + labelIdMap.get(server));
				}
			}
		}
		scanner.close();
		pw.close();

		pw = new PrintWriter(new File(directory + "//" + network_id + ".nodes"));
		for (String s: labelIdMap.keySet()) {
//			System.out.println(labelIdMap.get(s) + "\t" + s);
			pw.println(labelIdMap.get(s) + "\t" + s);
		}
		pw.close();
	}
	
	public static int getDemoCategory(int id) {
		if (ManagerNeuro.source.contains(id)) return 0;
		else if (ManagerNeuro.intermediate.contains(id)) return 1;
		else return 2;
	}
	
	public static void getSocialrankCompliantNetwork() throws Exception {
		HashMap<Integer, String> idLabelMap = new HashMap();
		HashMap<Integer, Integer> idRankMap = new HashMap();
		int nLevels = 17;
		int levelCounter[] = new int[nLevels];
		double levelIn[] = new double[nLevels];
		double levelOut[] = new double[nLevels];
		double levelEdgeDirection[][] = new double[nLevels][nLevels];
		double levelDemography[][] = new double[nLevels][3];
		ManagerNeuro.loadNeuroMetaNetwork(); // for finding sensory, inter and motor neurons
		HashMap<Integer, Double> nodeOutMap = new HashMap();
		HashMap<Integer, Double> nodeInMap = new HashMap();
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));

		Scanner scanner = new Scanner(new File(directory + "//" + network_id + ".nodes"));
//		System.out.println(ManagerNeuro.source.size() + "\t" + ManagerNeuro.target.size());
//		System.out.println(ManagerNeuro.source);
		while (scanner.hasNext()) {
			int id = scanner.nextInt();
			String label = scanner.next();
			idLabelMap.put(id, label);
			/* special case */
//			System.out.println(label + "\t" + ManagerNeuro.source.contains(Integer.parseInt(label)));
			if (ManagerNeuro.source.contains(Integer.parseInt(label))) {
				pw.println("1000" + "\t" + label);
			}
			if (ManagerNeuro.target.contains(Integer.parseInt(label))) {
				pw.println(label + "\t" + "2000");
			}
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".ranks"));
		while (scanner.hasNext()) {
			int id = scanner.nextInt();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
			levelCounter[rank]++;
			int demoCategory = getDemoCategory(Integer.parseInt(idLabelMap.get(id)));
			levelDemography[rank][demoCategory]++;
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".edges"));
//		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));
		while (scanner.hasNext()) {
			int substrate = scanner.nextInt();
			int product = scanner.nextInt();

			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			
			if (substrateRank < productRank) {
//				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				pw.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				
				
				
//				levelEdgeDirection[substrateRank][productRank]++;
				levelOut[substrateRank]++;
				levelIn[productRank]++;
				
				if (nodeOutMap.containsKey(substrate)) {
					nodeOutMap.put(substrate, nodeOutMap.get(substrate) + 1.0);
				}
				else {
					nodeOutMap.put(substrate, 1.0);
				}
				
				if (nodeInMap.containsKey(product)) {
					nodeInMap.put(product, nodeInMap.get(product) + 1.0);
				}
				else {
					nodeInMap.put(product, 1.0);
				}
			}
			else {
//				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
//				System.out.println(substrateRank + "\t" + productRank);
//				levelEdgeDirection[substrateRank][productRank]++;
			}
			
			levelEdgeDirection[substrateRank][productRank]++;
		}
		pw.close();
		scanner.close();
		
//		for (int i = 0; i < nLevels; ++i) {
//			System.out.println(i + "\t" + levelCounter[i]);
//		}
//		
//		System.out.println();
//		
//		for (int i = 0; i < nLevels; ++i) {
//			System.out.println((levelIn[i]/levelCounter[i]) + "\t" + (levelOut[i]/levelCounter[i]));
//		}
		
		for (int i = 0; i < nLevels; ++i) {
			double ins[] = new double[levelCounter[i]];
			double outs[] = new double[levelCounter[i]];
			int knt = 0;
			for (int j: idRankMap.keySet()) {
				if (idRankMap.get(j) != i) continue;
				double v = 0;
				if (nodeInMap.containsKey(j)) v = nodeInMap.get(j);
				ins[knt] = v;
				
				v = 0;
				if (nodeOutMap.containsKey(j)) v = nodeOutMap.get(j);
				outs[knt] = v;
				
				++knt;
			}
			
			System.out.println("Level\t" + i);
			System.out.println(StatUtils.mean(ins) + "\t" + Math.sqrt(StatUtils.variance(ins)));
			System.out.println(StatUtils.mean(outs) + "\t" + Math.sqrt(StatUtils.variance(outs)));
		}
		
		
//		
//		System.out.println();
//		
		for (int i = 0; i < nLevels; ++i) {
			for (int j = 0; j < nLevels; ++j) {
//				System.out.print(levelEdgeDirection[i][j] + "\t");
				System.out.print((levelCounter[i] * levelCounter[j]) + "\t");
			}
			System.out.println();
		}
		
//		for (int i = 0; i < nLevels; ++i) {
//			System.out.println(levelCounter[i] + "\t" + levelDemography[i][0]/levelCounter[i] 
//					+ "\t" + levelDemography[i][1]/levelCounter[i]
//					+ "\t" + levelDemography[i][2]/levelCounter[i]);
//		}
	}
	
	public static void main(String[] args) throws Exception {
//		randomizationTest = true;
//		random = new Random();
//		getDataForSocialrankAnalysis();
		getSocialrankCompliantNetwork();
	}
}
