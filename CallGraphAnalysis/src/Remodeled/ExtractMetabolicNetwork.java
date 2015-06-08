package Remodeled;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtractMetabolicNetwork {
	public static void parseMetabolicNetworkXML(String filePath) throws Exception {
		File inputFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputFile);
		doc.getDocumentElement().normalize();
		// System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		NodeList reactionList = doc.getElementsByTagName("reaction");
		// System.out.println("Number of reactions: " + reactionList.getLength());
		for (int reactionIndex = 0; reactionIndex < reactionList.getLength(); reactionIndex++) {
			Element reaction = (Element) reactionList.item(reactionIndex);
			// System.out.println("Reaction id: " + reaction.getAttribute("id"));
			ArrayList<String> substrate = new ArrayList();
			ArrayList<String> product = new ArrayList();
			NodeList compoundList = reaction.getChildNodes();
			for (int compoundIndex = 0; compoundIndex < compoundList.getLength(); ++compoundIndex) {
				Node node = compoundList.item(compoundIndex);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element compound = (Element) node;
					if (compound.getNodeName().equals("substrate")) {
						substrate.add(compound.getAttribute("name"));
					} 
					else {
						product.add(compound.getAttribute("name"));
					}
					// System.out.println(compound.getNodeName() + "\t" + compound.getAttribute("id") + "\t" + compound.getAttribute("name"));
				} 
				else {
					// System.out.println(node.getTextContent());
				}
			}

			for (String s : substrate) {
				for (String p : product) {
					System.out.println(s + " " + p);
				}
			}

			if (reaction.getAttribute("type").equals("reversible")) {
				for (String s : substrate) {
					for (String p : product) {
						System.out.println(p + " " + s);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		parseMetabolicNetworkXML("metabolic_networks//rat-kgml.xml");
	}
}
