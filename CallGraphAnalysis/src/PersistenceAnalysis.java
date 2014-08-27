import java.util.HashSet;
import java.util.Set;


public class PersistenceAnalysis {
	
	public void getContiguousFunctionPersistance(CallDAG cgDAG1, CallDAG cgDAG2) {		
		Set<String> s1 = new HashSet(cgDAG1.functions);
		Set<String> s2 = new HashSet(cgDAG2.functions);
		
		Set<String> s3 = new HashSet(s1);
		
		s3.retainAll(s2);
		System.out.println(s1.size());
		System.out.println(s2.size());
		System.out.println(s3.size());
		System.out.println("Function Removed:" + (s1.size() - s3.size()));
		System.out.println("Function Added:" + (s2.size() - s3.size()));
		
	}
	

}
