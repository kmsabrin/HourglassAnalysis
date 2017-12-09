package utilityhg;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class RunExe {

	public static void testFunc() throws Exception {
//		Process p3 = Runtime.getRuntime().exec("cmd /c socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
		Process p3 = Runtime.getRuntime().exec("socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
		InputStream is = p3.getInputStream();
		is.close();
		p3.waitFor(2L, TimeUnit.SECONDS);
//		Thread.sleep(3000);
	}
	
	public static void main(String[] args) throws Exception {
//		Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
//		Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
		
//		Process p1 = Runtime.getRuntime().exec("cmd /c del celegans.edges", new String[0], new File("C:/MinGW/bin"));
//		int status1 = p1.waitFor();
//		Process p2 = Runtime.getRuntime().exec("cmd /c copy celegans.edges.1 celegans.edges", new String[0], new File("C:/MinGW/bin"));
//		int status2 = p2.waitFor();
//		System.out.println(status1 + "\t" + status2);
		
//		Process p3 = Runtime.getRuntime().exec("socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
//		Process p3 = Runtime.getRuntime().exec("cmd /c socialrank.exe summary_stats.txt celegans", new String[0], new File("C:/MinGW/bin"));
//		OutputStream os = p3.getOutputStream();
//		Thread.sleep(10000);
//		os.flush();
//		boolean status3 = p3.waitFor(10L, TimeUnit.SECONDS);
//		System.out.println(status3);
		testFunc();
		Scanner scanner = new Scanner(new File("C:/MinGW/bin/celegans.ranks"));
	}
}
