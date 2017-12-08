package utilityhg;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class RunExe {

	public static void main(String[] args) throws Exception {
//		Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
//		Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
		
		Process p1 = Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
		int status1 = p1.waitFor();
		Process p2 = Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
		int status2 = p2.waitFor();
		
		System.out.println(status1 + "\t" + status2);
		
//		Process p3 = Runtime.getRuntime().exec("socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
		Process p3 = Runtime.getRuntime().exec("cmd /c socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
		
		boolean status3 = p3.waitFor(10L, TimeUnit.SECONDS);
		System.out.println(status3);

	}
}
