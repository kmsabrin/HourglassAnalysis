package utilityhg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import neuro.ManagerNeuro;

public class SocialRankAnalysis {
//	static String directory = "metabolic_networks";
//	static String network_file = "rat-links.txt";
//	static String network_id = "rat";
	
	static String directory = "neuro_networks";;
	static String network_file = "celegans_network_clean.txt";
	static String network_id = "celegans";
	
//	static String network_file = "celegans_graph.txt"
//	static String network_id = "celegans_no_filter";
	
	static boolean randomizationTest = false;
	static Random random;
	
	static boolean addBackEdge = true;
	
	public static void getDataForSocialrankAnalysis() throws Exception {
		HashMap<String, Integer> labelIdMap = new HashMap();
		
		Scanner scanner = new Scanner(new File(directory + "//" + network_file));
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".edges"));
		int id = 0;
		ManagerNeuro.loadNeuroMetaNetwork();
		
//		labelIdMap.put("ss", id++);
//		labelIdMap.put("st", id++);
		while (scanner.hasNext()) {
			String server = scanner.next();
			String dependent = scanner.next();
			
			/* create network for sensory, inter, motor individually */
//			if (ManagerNeuro.source.contains(server)) {
//				if (!ManagerNeuro.source.contains(dependent)) {
//					if (!labelIdMap.containsKey(server)) {
//						labelIdMap.put(server, id++);
//					}
//					pw.println(labelIdMap.get(server) + "\t" + labelIdMap.get("st"));
//					continue;
//				}
//			}
//			else {
//				continue;
//			}
			
//			if (ManagerNeuro.inter.contains(server)) {
//				if (!ManagerNeuro.inter.contains(dependent)) {
//					if (!labelIdMap.containsKey(server)) {
//						labelIdMap.put(server, id++);
//					}
//					pw.println(labelIdMap.get(server) + "\t" + labelIdMap.get("st"));
//					continue;
//				}
//			}
//			else {
//				if (ManagerNeuro.inter.contains(dependent)) {
//					if (!labelIdMap.containsKey(dependent)) {
//						labelIdMap.put(dependent, id++);
//					}
//					pw.println(labelIdMap.get("ss") + "\t" + labelIdMap.get(dependent));
//				}
//				continue;
//			}
//			
//			if (ManagerNeuro.target.contains(server)) {
//				if (!ManagerNeuro.target.contains(dependent)) {
//					continue;
//				}
//			}
//			else {
//				if (ManagerNeuro.target.contains(dependent)) {
//					if (!labelIdMap.containsKey(dependent)) {
//						labelIdMap.put(dependent, id++);
//					}
//					pw.println(labelIdMap.get("ss") + "\t" + labelIdMap.get(dependent));
//				}
//				continue;
//			}
			/* end */
			
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
	
	public static int getNeuronCategory(String id) {
		if (ManagerNeuro.source.contains(id)) return 0;
		else if (ManagerNeuro.target.contains(id)) return 2;
		else return 1;
	}
	
	public static void getSocialrankCompliantNetwork() throws Exception {
		HashMap<String, String> idLabelMap = new HashMap();
		HashMap<String, Integer> idRankMap = new HashMap();
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));

		Scanner scanner = new Scanner(new File(directory + "//" + network_id + ".nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
//			System.out.println(idLabelMap.get(id) + "\t" + rank + "\t" + agony);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".edges"));
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();

			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			
			if (substrateRank < productRank) {
//				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
				pw.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
			}
			else {
				/* rank violation */
				System.out.println(substrate + "#" + product);
//				System.out.println(idLabelMap.get(substrate) + "#" + idLabelMap.get(product));
//				System.out.println(substrateRank + "\t" + productRank);
			}
		}
		pw.close();
		scanner.close();
	}
	
	public static void getSocialrankCompliantNetworkNeuro_1() throws Exception {
		HashMap<String, String> idLabelMap = new HashMap();
		HashMap<String, Integer> idRankMap = new HashMap();
		int nLevels = 35;
		int levelCounter[] = new int[nLevels];
		double levelIn[] = new double[nLevels];
		double levelOut[] = new double[nLevels];
		double levelEdgeDirection[][] = new double[nLevels][nLevels];
		double levelDemography[][] = new double[nLevels][3];
		ManagerNeuro.loadNeuroMetaNetwork(); // for finding sensory, inter and motor neurons
		HashMap<String, Double> nodeOutMap = new HashMap();
		HashMap<String, Double> nodeInMap = new HashMap();
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));

		Scanner scanner = new Scanner(new File(directory + "//" + network_id + ".nodes"));
//		System.out.println(ManagerNeuro.source.size() + "\t" + ManagerNeuro.target.size());
//		System.out.println(ManagerNeuro.source);
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			idLabelMap.put(id, label);
			/* special case */
//			System.out.println(label + "\t" + ManagerNeuro.source.contains(Integer.parseInt(label)));
			
//			if (ManagerNeuro.source.contains(label)) {
//				pw.println("1000" + "\t" + label);
//			}
//			if (ManagerNeuro.target.contains(label)) {
//				pw.println(label + "\t" + "2000");
//			}
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".ranks"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
			levelCounter[rank]++;
//			int demoCategory = getDemoCategory(Integer.parseInt(idLabelMap.get(id))); String vs int error, convert to string
//			levelDemography[rank][demoCategory]++;
//			System.out.println(idLabelMap.get(id) + "\t" + rank + "\t" + agony);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".edges"));
		HashSet<String> edges = new HashSet();
		while (scanner.hasNext()) {
			edges.add(scanner.next() + "#" + scanner.next());	
		}
		scanner.close();
		
		scanner = new Scanner(new File("neuro_networks//celegans_graph.txt"));
		HashMap<String, Double> weights = new HashMap();
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			double wgt = scanner.nextDouble();
			weights.put(src + "#" + tgt, wgt);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//" + network_id + ".edges"));
//		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));
		int tiedRankKount = 0;
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();

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
				
				if (edges.contains(product + "#" + substrate)) {
					String oSubstrate = idLabelMap.get(substrate);
					String oProduct = idLabelMap.get(product);
					double aW = weights.get(oSubstrate + "#" + oProduct);
					double bW = weights.get(oProduct + "#" + oSubstrate);
//					System.out.println((productRank - substrateRank) + "\t" + (aW - bW));
//					++tiedRankKount;
				}
				else {
					
				}
			}
			else {
//				System.out.println(idLabelMap.get(substrate) + "\t" + idLabelMap.get(product));
//				System.out.println(substrateRank + "\t" + productRank);
//				levelEdgeDirection[substrateRank][productRank]++;
				if (substrateRank == productRank) {
//					++tiedRankKount;
					String oSubstrate = idLabelMap.get(substrate);
					String oProduct = idLabelMap.get(product);
					if (edges.contains(product + "#" + substrate)) {
//						++tiedRankKount;
						double aW = weights.get(oSubstrate + "#" + oProduct);
						double bW = weights.get(oProduct + "#" + oSubstrate);
						if (aW != bW) {
//							++tiedRankKount;
						}
						
						if (aW > bW) {
							pw.println(oSubstrate + "\t" + oProduct);
						}
						else if (aW < bW) {
//							pw.println(oProduct + "\t" + oSubstrate);
						}
						else {
//							System.out.println("Double tied: " + oSubstrate + "\t" + oProduct);
							pw.println(oSubstrate + "\t" + oProduct);
						}
					}
					else {
//						System.out.println(oSubstrate + "\t" + oProduct  + "\t" + weights.get(oSubstrate + "#" + oProduct));
					}
				}
				else {
					if (!edges.contains(product + "#" + substrate)) {
						++tiedRankKount;
					}
				}
			}
			
			levelEdgeDirection[substrateRank][productRank]++;
		}
		scanner.close();
		
		System.out.println("Tied rank: " + tiedRankKount);
		
		if (addBackEdge) {
			scanner = new Scanner(new File(directory + "//" + "celegansEdgeAddBack.txt"));
			while (scanner.hasNext()) {
				String substrate = scanner.next();
				String product = scanner.next();
				pw.println(substrate + "\t" + product);
			}
		}		
		pw.close();
		
		for (int i = 0; i < nLevels; ++i) {
//			System.out.println(i + "\t" + levelCounter[i]);
		}
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
			for (String j: idRankMap.keySet()) {
				if (idRankMap.get(j) != i) continue;
				double v = 0;
				if (nodeInMap.containsKey(j)) v = nodeInMap.get(j);
				ins[knt] = v;
				
				v = 0;
				if (nodeOutMap.containsKey(j)) v = nodeOutMap.get(j);
				outs[knt] = v;
				
				++knt;
			}
			
//			System.out.println("Level\t" + i);
//			System.out.println(StatUtils.mean(ins) + "\t" + Math.sqrt(StatUtils.variance(ins)));
//			System.out.println(StatUtils.mean(outs) + "\t" + Math.sqrt(StatUtils.variance(outs)));
		}
		
		
//		
//		System.out.println();
//		
		for (int i = 0; i < nLevels; ++i) {
			for (int j = 0; j < nLevels; ++j) {
//				System.out.print(levelEdgeDirection[i][j] + "\t");
//				System.out.print((levelCounter[i] * levelCounter[j]) + "\t");
			}
//			System.out.println();
		}
		
//		for (int i = 0; i < nLevels; ++i) {
//			System.out.println(levelCounter[i] + "\t" + levelDemography[i][0]/levelCounter[i] 
//					+ "\t" + levelDemography[i][1]/levelCounter[i]
//					+ "\t" + levelDemography[i][2]/levelCounter[i]);
//		}
	}

	public static void getSocialrankCompliantNetworkNeuro_2() throws Exception {
		HashMap<String, String> idLabelMap = new HashMap();
		HashMap<String, Integer> idRankMap = new HashMap();
		ManagerNeuro.loadNeuroMetaNetwork(); // for finding sensory, inter and motor neurons
//		System.out.println(ManagerNeuro.source);
//		System.out.println(ManagerNeuro.inter);
//		System.out.println(ManagerNeuro.target);
		PrintWriter pw = new PrintWriter(new File(directory + "//" + network_id + ".socialrank.network"));

		Scanner scanner = new Scanner(new File(directory + "//" + network_id + ".nodes"));
		while (scanner.hasNext()) {
			String id = scanner.next();
			String label = scanner.next();
			idLabelMap.put(id, label);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//edges-ranks//" + network_id + ".ranks.1"));
		HashMap<Integer, Integer> rankHistogram = new HashMap();
		while (scanner.hasNext()) {
			String id = scanner.next();
			int rank = scanner.nextInt();
			int agony = scanner.nextInt();
			idRankMap.put(id, rank);
			if (rankHistogram.containsKey(rank)) {
				rankHistogram.put(rank, rankHistogram.get(rank) + 1);
			}
			else {
				rankHistogram.put(rank, 1);
			}
//			System.out.println(getNeuronCategory(idLabelMap.get(id)) + "\t" + rank);
		}
		scanner.close();
//		System.out.println("# # # # # #");
		for (int rank : rankHistogram.keySet()) {
//			System.out.println(rank + "\t" + rankHistogram.get(rank));
		}
		
		int kount = 0;
		scanner = new Scanner(new File(directory + "//edges-ranks//" + network_id + ".edges.1"));
		HashSet<String> edges = new HashSet();
		while (scanner.hasNext()) {
			edges.add(scanner.next() + "#" + scanner.next());	
			++kount;
		}
		scanner.close();
//		System.out.println(kount);
		
		scanner = new Scanner(new File("neuro_networks//celegans_graph.txt"));
		HashMap<String, Double> weights = new HashMap();
		while (scanner.hasNext()) {
			String src = scanner.next();
			String tgt = scanner.next();
			double wgt = scanner.nextDouble();
			weights.put(src + "#" + tgt, wgt);
		}
		scanner.close();
		
		scanner = new Scanner(new File(directory + "//edges-ranks//" + network_id + ".edges.1"));
		HashSet<String> printed = new HashSet();
		while (scanner.hasNext()) {
			String substrate = scanner.next();
			String product = scanner.next();
			int substrateRank = idRankMap.get(substrate);
			int productRank = idRankMap.get(product);
			String oSubstrate = idLabelMap.get(substrate);
			String oProduct = idLabelMap.get(product);
			
			/**********************************/
			if (edges.contains(product + "#" + substrate) && edges.contains(substrate + "#" + product)) {
				System.out.println(oSubstrate + "#" + oProduct);
				double currentDirectionWeight = weights.get(oSubstrate + "#" + oProduct);
				double backDirectionWeight = weights.get(oProduct + "#" + oSubstrate);
				if (substrateRank == productRank) {
					if (currentDirectionWeight > backDirectionWeight) {
//						System.out.println(substrate + "#" + product);
					}
					else if (currentDirectionWeight == backDirectionWeight) {
//						System.out.println(substrate + "\t" + product);
//						System.out.println(oSubstrate + "#" + oProduct);
					}
				}
				else {
//					System.out.println(oSubstrate + "#" + oProduct);
					if (substrateRank < productRank) {
//						System.out.println(substrate + "#" + product);
					}
//					if (!printed.contains(product + "#" + substrate)) {
//						System.out.print(substrate + "#" + product);
//						System.out.print("\t" + (substrateRank - productRank));
//						System.out.println("\t" + (currentDirectionWeight - backDirectionWeight));
//						printed.add(substrate + "#" + product);
//					}
				}
			}
			else {
//				System.out.println(substrate + "\t" + product);
			}
//			System.out.println(oSubstrate + " " + oProduct);
			/********************************/
			
			
			if (getNeuronCategory(oSubstrate) == getNeuronCategory(oProduct)) {
//				System.out.println(productRank - substrateRank);
			}
//			System.out.println(oSubstrate + "#" + oProduct);
//			System.out.println(getNeuronCategory(oSubstrate) + "\t" + getNeuronCategory(oProduct));
			
			if (substrateRank < productRank) {
				pw.println(oSubstrate + "\t" + oProduct);
				/*
				if (edges.contains(product + "#" + substrate)) {
					String oSubstrate = idLabelMap.get(substrate);
					String oProduct = idLabelMap.get(product);
					double aW = weights.get(oSubstrate + "#" + oProduct);
					double bW = weights.get(oProduct + "#" + oSubstrate);
					System.out.println((productRank - substrateRank) + "\t" + (aW - bW));
				}
				*/
//				System.out.println(oSubstrate + " " + oProduct);

			}
			else if (substrateRank == productRank) {
//				System.out.println(oSubstrate + "\t" + oProduct);
//				System.out.println(substrate + "\t" + product);
				if (edges.contains(product + "#" + substrate)) {
					double currentDirectionWeight = weights.get(oSubstrate + "#" + oProduct);
					double backDirectionWeight = weights.get(oProduct + "#" + oSubstrate);
					if (currentDirectionWeight != backDirectionWeight) {
						if (currentDirectionWeight > backDirectionWeight) {
							pw.println(oSubstrate + "\t" + oProduct);
//							System.out.println(oSubstrate + "\t" + oProduct);
						}
						else {
							// conjugate case, skip
//							System.out.println(oSubstrate + "\t" + oProduct);
						}
					}
					else {
						// 12 case, add back
//						System.out.println(oSubstrate + "\t" + oProduct);
					}
				}
				else {
					// 54 of 58 case, add back
//					System.out.println(oSubstrate + "\t" + oProduct);
				}
				
//				System.out.println(getNeuronCategory(Integer.parseInt(oProduct)) - getNeuronCategory(Integer.parseInt(oSubstrate)));
			}
			else {
//				try to add back in increasing rank-difference order
//				System.out.println(oSubstrate + "\t" + oProduct + "\t" + (substrateRank - productRank));
//				System.out.println(oSubstrate + "\t" + oProduct);
//				System.out.println(substrate + "\t" + product);
				
//				System.out.println(getNeuronCategory(Integer.parseInt(oProduct)) - getNeuronCategory(Integer.parseInt(oSubstrate)));
			}
		}
		scanner.close();
		
		
		/*
		if (addBackEdge) {
			scanner = new Scanner(new File(directory + "//" + "celegansEdgeAddBack.txt"));
			while (scanner.hasNext()) {
				String substrate = scanner.next();
				String product = scanner.next();
				pw.println(substrate + "\t" + product);
			}
		}
		*/		
		
		pw.close();		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Start: ");
//		randomizationTest = true;
//		random = new Random();
//		getDataForSocialrankAnalysis();

//		getSocialrankCompliantNetwork();
//		getSocialrankCompliantNetworkNeuro_1();
		getSocialrankCompliantNetworkNeuro_2();
	}
}
