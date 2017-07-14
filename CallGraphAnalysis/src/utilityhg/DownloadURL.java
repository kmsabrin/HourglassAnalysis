package utilityhg;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class DownloadURL {    
	public static HashMap<String, String> neuronNames = new HashMap();
	public static HashSet<String> neuronGroups = new HashSet();
	public static HashMap<String, Integer> groupLOF = new HashMap();
	public static HashMap<String, String> neuronLoc = new HashMap();
	public static HashMap<String, String> neuronPCen = new HashMap();
	public static HashMap<String, String> neuronRank = new HashMap();
	public static HashMap<String, String> neuronAgony = new HashMap();
	
	public static void getLocPCen() throws Exception {
		Scanner scanner = new Scanner(new File("neuro_networks//aggregate_1.txt"));
		while (scanner.hasNext()) {
			String neuron = scanner.next();
			String rank = scanner.next();
			String agony = scanner.next();
			String loc = scanner.next();
			String pCen = scanner.next();
			if (neuron.length() == 4 && neuron.charAt(2) == '0') {
				neuron = neuron.substring(0, 2) + neuron.charAt(3);
			}
			neuronLoc.put(neuron, loc);
			neuronPCen.put(neuron, pCen);
			neuronRank.put(neuron, rank);
			neuronAgony.put(neuron, agony);
		}
		scanner.close();
	}
	
	public static void getGroupLOF() throws Exception {
		Scanner scanner = new Scanner(new File("neuro_networks//group_lof.txt"));
		while (scanner.hasNext()) {
			String group = scanner.next();
			int lof = Integer.parseInt(scanner.next());
			groupLOF.put(group, lof);
		}
		scanner.close();
		
		for (String s: neuronGroups) {
//			System.out.println(s);
		}
	}
	
	public static void getNeurons() throws Exception {
		getGroupLOF();
		getLocPCen();
		
		Scanner scanner = new Scanner(new File("neuro_networks//celegans_neurons.txt"));
		
		while (scanner.hasNext()) {
			String neuron = scanner.next();
			String group = scanner.next();
			String type = scanner.next();
			
			if (type.contains("in") || type.contains("se") || type.contains("mo")) {
				neuronNames.put(neuron, group);
				neuronGroups.add(group);
			}
			
			int lof = 0;
			if (groupLOF.containsKey(group)) lof = groupLOF.get(group);
			System.out.println(neuron + "\t" + group + "\t" + type + "\t" + lof 
					+ "\t" + neuronLoc.get(neuron) + "\t" + neuronPCen.get(neuron)
					+ "\t" + neuronRank.get(neuron) + "\t" + neuronAgony.get(neuron));
		}
		
		for (String s: neuronLoc.keySet()) {
			if (!neuronNames.containsKey(s)) {
				System.out.println(s);
			}
		}
		

		System.out.println(neuronNames.size());
	}
	
	public static void extractLOF() throws Exception {
		for (String s: neuronGroups) {
			Scanner scanner = new Scanner(new File("wormweb//" + s + ".html"));	
			boolean found = false;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("losses-of-function") || line.contains("loss-of-function")) {
					String words[] = line.split("\\s+");
					if (words[1].equals("losses-of-function") || words[1].equals("loss-of-function")) {
						System.out.println(s + "\t" + words[0]);
						found = true;
					}
				}
			}
			scanner.close();
			if (!found) {
				System.out.println(s);
			}
		}
	}
	
	public static void getURL() throws Exception {
//		for (String s : neuronGroups) {
//			try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
//				final HtmlPage page = webClient.getPage("http://www.wormweb.org/neuralnet#c=" + s + "&m=1");
//				page.save(new File("wormweb//" + s));
//			}
//		}
		
		String s = "ASE";
		try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
			final HtmlPage page = webClient.getPage("http://www.wormweb.org/neuralnet#c=" + s + "&m=1");
			page.save(new File("wormweb//" + s));
		}
	}

	public static void main(String[] args) throws Exception {
		getNeurons();
//		getURL();
//		extractLOF();
	}
}
