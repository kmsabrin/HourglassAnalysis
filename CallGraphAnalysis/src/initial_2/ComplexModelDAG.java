package initial_2;

import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;

public class ComplexModelDAG {
	static Random random = new Random(System.nanoTime());
	static double alpha = 0.0;
	static boolean alphaNegative = false;
	
	static int nLayers = 4;
	static int nodesPerLayer = 5;
	static int[] layerStartNode = new int[nLayers];
	static int[] layerEndNode = new int[nLayers];

	static ZipfDistribution zipfDistribution;
	static double normalMean = 7.0;
	static double normalSD = 3.0;
	static NormalDistribution normalDistribution = new NormalDistribution(normalMean, normalSD);

	public static void getAlphaDAG() throws Exception {
		String negate = "";
		if (alphaNegative) negate += "-";
		PrintWriter pw = new PrintWriter(new File("synthetic_callgraphs//ComplexModelDAGa" + negate + alpha + ".txt"));
		generateLayeredDAG(pw);
	}
	
	public static int getInDegree() {
//		int values[] = {2, 3, 4, 5};
//		int values[] = {7, 8, 9, 10};
//		return values[random.nextInt(4)];

//		return (int)Math.ceil(normalDistribution.sample());

		return 2;		
	}
	
	public static int getNodeFromZipfDistribution(int startLayerIndex, int endLayerIndex) {
		int nElements = endLayerIndex - startLayerIndex + 1;		
		double epsilon = 0.000001;
		double p = random.nextDouble();
		double cumulativeProbability = 0;
		int selectedLayer = -1;
		for (int i = 1; i <= nElements; ++i) {
			if (alphaNegative == true) {
				cumulativeProbability += zipfDistribution.probability(nElements - i + 1);
			}
			else {
				cumulativeProbability += zipfDistribution.probability(i);
			}
			
			if (p < cumulativeProbability + epsilon) {
				selectedLayer = startLayerIndex + i - 1;
				return layerStartNode[selectedLayer] + random.nextInt(nodesPerLayer);
			}
		}
		
		selectedLayer = endLayerIndex;
		return layerStartNode[selectedLayer] + random.nextInt(nodesPerLayer);
	}
	
	public static int getNodeFromUniformDistribution(int startLayerIndex, int endLayerIndex) {
		double epsilon = 0.000001;
		double p = random.nextDouble();
		double eachLayerProbablity = 1.0 / (endLayerIndex - startLayerIndex + 1);
		double cumulativeProbability = 0;
		int selectedLayer = -1;
		for (int i = startLayerIndex; i <= endLayerIndex; ++i) {
			cumulativeProbability += eachLayerProbablity;
			if (p < cumulativeProbability + epsilon) {
				selectedLayer = i;
				return layerStartNode[selectedLayer] + random.nextInt(nodesPerLayer);
			}
		}
		
		selectedLayer = endLayerIndex;
		return layerStartNode[selectedLayer] + random.nextInt(nodesPerLayer);
	}
	
	public static void generateLayeredDAG(PrintWriter pw) throws Exception {
		for (int layer = nLayers - 1; layer >= 0; --layer) {
			for (int productIndex = layerEndNode[layer]; productIndex >= layerStartNode[layer]; --productIndex) {
				if (layer == nLayers - 1) {
					continue; // sources layer
				}

				int startLayerIndex = layer + 1;
				int endLayerIndex = nLayers - 1;

				if (Math.abs(alpha) > 0.00001) {
					zipfDistribution = new ZipfDistribution(endLayerIndex - startLayerIndex + 1, alpha);
				}

				int k = getInDegree();
//				System.out.println(k);

				for (int j = 0; j < k; ++j) {
					int substrateIndex;
					if (Math.abs(alpha) < 0.000001) {
						substrateIndex = getNodeFromUniformDistribution(startLayerIndex, endLayerIndex);
					} 
					else {
						substrateIndex = getNodeFromZipfDistribution(startLayerIndex, endLayerIndex);
					}
					
					pw.println(substrateIndex + " " + productIndex);
//					pw.println(productIndex + " -> " + substrateIndex + ";");
				}
			}
		}
		
		pw.close();
	}

	public static void loadLayerIndex() {
		// layer [0] = targets
		// layer [nLayer - 1] = sources 
		int startIndex = 0;
		for (int i = 0; i < nLayers; ++i) {
			layerStartNode[i] = startIndex;
			layerEndNode[i] = startIndex + nodesPerLayer - 1;
			startIndex = layerEndNode[i] + 1;
		}
	}

	public static void main(String[] args) throws Exception {
		loadLayerIndex();
		
		double alphaValues[] = {-10, -1, 0, 1, 10, 20};
		
		for (double d: alphaValues) {
			if (d < 0) {
				alphaNegative = true;
			}
			else {
				alphaNegative = false;
			}
			
			alpha = Math.abs(d);
			random = new Random(System.nanoTime());
			getAlphaDAG();
		}
		
		System.out.println("Done!");
	}
}
