import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LineOfCodeGenerator {
	static List<String> sourceFilePath = new ArrayList();

	// directory traverse
	public static void showFiles(File[] files) {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            showFiles(file.listFiles()); // Calls same method again.
	        } else {
	            sourceFilePath.add(file.getAbsolutePath());
	        }
	    }
	}
	
	public static void main(String[] args) {
		File[] files = new File("C:/Users/Lupu/workspace/CallGraphAnalysis").listFiles();
	    showFiles(files);
	    for (String s: sourceFilePath) {
			System.out.println(s);
			// if ends with .c
			// count line number
		}    
	}
}
