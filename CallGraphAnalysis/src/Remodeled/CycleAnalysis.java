package Remodeled;
import java.util.ArrayList;

public class CycleAnalysis {
	public void analyzeCycle(CallDAG callDAG) {
		for (ArrayList<String> cycleString: callDAG.detectedCycles) {
			if (cycleString.size() < 5) continue;
			int extIn = 0;
			int extOut = 0;
			for (String s: cycleString) {
				int fIn = 0;
				int fOut = 0;
				if (callDAG.depends.containsKey(s)) {
					for (String r: callDAG.depends.get(s)) {
						if (!cycleString.contains(r)) {
							++extOut;
							++fOut;
						}
					}
				}
				if (callDAG.serves.containsKey(s)) {
					for (String r: callDAG.serves.get(s)) {
						if (!cycleString.contains(r)) {
							++extIn;
							++fIn;
						}
					}
				}
				System.out.println(s + "\t" + fIn + "\t" + fOut);
				
			}
			
//			System.out.println(cycleString.size() + "\t" + extIn + "\t" + extOut);
			System.out.println("#######################");
		}
	}
}
