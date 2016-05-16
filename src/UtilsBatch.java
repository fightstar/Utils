

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UtilsBatch {

	public static String BatchExecutorFromFile (String file, List<String> parameters)
	{
		List<String> commands = new ArrayList<String>();                
	    commands.add(file);
	    commands.addAll(parameters);  

	    StringBuilder sb = new StringBuilder(); 
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(Common.formPath(new File(file))));
		pb.redirectErrorStream(true);
		Process p;
		try {
			p = pb.start();
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
			for ( String line = br.readLine(); line != null; line = br.readLine() )
			{
			    sb.append(line);    
//				Common.print( ">" + line );
			}
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			Common.print("Batch file not found");
			e.printStackTrace();
		}
		return sb.toString();
	}
	public static String BatchExecutorCommand (List<String> parameters)
	{
//	    System.out.println(parameters);
    
	    StringBuilder sb = new StringBuilder(); 
//		ProcessBuilder pb = new ProcessBuilder("xcopy", "e:\\filtering.txt", "e:\\1");
		ProcessBuilder pb = new ProcessBuilder(parameters);
		pb.redirectErrorStream(true);
		Process p;
		try {
			p = pb.start();
			InputStream is = p.getInputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
			for ( String line = br.readLine(); line != null; line = br.readLine() )
			{
			    sb.append(line);    
//				Common.print( ">" + line );
			}
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
/*
 			 ProcessBuilder pb = new ProcessBuilder("ipconfig");
			    Process p = pb.start();
			        BufferedReader reader = new BufferedReader(new InputStreamReader(
			                p.getInputStream()));
			        String readline;
			        while ((readline = reader.readLine()) != null) {
			            System.out.println(readline);
			        }

 */
		
		return sb.toString();
	}
//	public static void main(String[] args) {}

}
