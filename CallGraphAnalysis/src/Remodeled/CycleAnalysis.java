package Remodeled;
import java.util.ArrayList;

public class CycleAnalysis {
	public void analyzeCycle(DependencyDAG dependencyDAG) {
		for (ArrayList<String> cycleString: dependencyDAG.detectedCycles) {
			if (cycleString.size() < 5) continue;
			int extIn = 0;
			int extOut = 0;
			for (String s: cycleString) {
				int fIn = 0;
				int fOut = 0;
				if (dependencyDAG.depends.containsKey(s)) {
					for (String r: dependencyDAG.depends.get(s)) {
						if (!cycleString.contains(r)) {
							++extOut;
							++fOut;
						}
					}
				}
				if (dependencyDAG.serves.containsKey(s)) {
					for (String r: dependencyDAG.serves.get(s)) {
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
