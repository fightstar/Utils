

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
	public static String eol = System.getProperty("line.separator");  
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	public static void print(Object st)
	{
		try{
		System.out.println(st.toString());}
		catch (NullPointerException e){
			System.out.println("There is no object to print." + eol + e);
		}
	}

    public static void printError(Object st) {
        try{
            System.out.println( "ERROR :  " + st.toString());}
        catch (NullPointerException e){
            System.out.println("There is no object to print." + eol + e);
        }
    }

	public static void print (HashMap<String, String> row_map)
	{
    	for (Entry<String, String> entry  : row_map.entrySet()) 
		System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
		
	}

	public	static void print (List<String> data)
	{
		for (int i=0; i<data.size(); i++)
			Common.print(data.get(i));
	}
	
	public	static void print (String[] xpaths_dynamic)
	{
		for (int i=0; i<xpaths_dynamic.length; i++)
			System.out.println(xpaths_dynamic[i]);
	}
	
	public static String formPath (File filepath)
	{
		String absolutePath = filepath.getAbsolutePath();
	    return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
	}
	
	public static String replaceGlobalTags (String text, HashMap<String, String> globalVariables)
	{
//		Common.print(globalVariables);
		for (Entry<String, String> entry  : globalVariables.entrySet()) 
		{
//			Common.print(entry.getKey());
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace("%%"+entry.getKey()+"%%", entry.getValue());
			}
		}
	return text;
	}

	public static String replaceLocalTags (String text, HashMap<String, String> globalVariables)
	{
		for (Entry<String, String> entry  : globalVariables.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace(entry.getKey(), entry.getValue());
			}
		}
	return text;
	}

	public static String replaceGlobalTags (String text, Properties globalVariables)
	{
//		Common.print(globalVariables);
		String textWithReplacedTags = text;
		for (Entry<Object, Object> entry  : globalVariables.entrySet()) 
		{
//			Common.print(entry.getKey());
			if (text.contains(entry.getKey().toString()))
			{
				textWithReplacedTags = textWithReplacedTags.replace("%%"+entry.getKey()+"%%", (CharSequence) entry.getValue());
			}
		}
	return textWithReplacedTags;
	}

	public static String replaceGlobalTags (String text, VariableStorage globalVariables)
	{
//		Common.print(globalVariables);
		for (Entry<Object, Object> entry  : globalVariables.entrySet()) 
		{
//			Common.print(entry.getKey());
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace("%%"+entry.getKey()+"%%", (CharSequence) entry.getValue());
			}
		}
	return text;
	}

	
	public static String readFileToString(String filePath)
	{
		BufferedReader reader = null;
	    String line, results = "";
		try {
			reader = new BufferedReader(new FileReader(filePath));
		    try {
				while( ( line = reader.readLine() ) != null)
				{
				    results += line;// + eol;
				}
			} catch (IOException e1) {
				Common.printError("File " + filePath + " can't be read");
				e1.printStackTrace();
			}
		    try {
				reader.close();
			} catch (IOException e) {
				Common.printError("File " + filePath + " can't be closed");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e2) {
			Common.printError("File " + filePath + " not found");
			e2.printStackTrace();
		}
		    return results;
	}

	public static List<String> getByRegex (String from, String pattern)
	   {
	       List<String> result = null;
	       Matcher m =  Pattern.compile(pattern).matcher(from);
	       if (m.find())
	       {
	           result = new ArrayList<String>();
	           result.add(m.group());
	           while(m.find())
	           {
	               result.add(m.group());
	           }
	       }
	       return result;
	   }

	public static List<String> getByRegexWithGroup (String from, String pattern)
	   {
	       List<String> result = null;
	       Matcher m =  Pattern.compile(pattern).matcher(from);
	       if (m.find())
	       {
	           result = new ArrayList<String>();
	           for (int i=0; i < m.groupCount(); i++)
	           {
	           result.add(m.group(i+1));
	           }
	           while(m.find())
	           {
	               result.add(m.group(1));
	           }
	       }
	       return result;
	   }

	
	public	static ArrayList<String> split (String str)
		{
		return new ArrayList<String>(Arrays.asList(str.split(",")));
		}

	public	static ArrayList<String> splitBy (String str, String delim)
		{
		return new ArrayList<String>(Arrays.asList(str.split(delim)));
		}

	public static String[] increaseArray(String[] theArray, String increaseBy)  
		{  
		    int i = theArray.length;  
		    int n = ++i;  
		    String[] newArray = new String[n];  
		    for(int cnt=0;cnt<theArray.length;cnt++)
		    {  
		        newArray[cnt] = theArray[cnt];  
		    }  
		    newArray[n-1] = increaseBy;
		    return newArray;  
		}  

	public	static String passFailAnalysis (String result)
	{
	return result.contains("<false>") ? "FAIL" : "PASS";
	}

	public static List<String> listFilesForFolder(File folder, String mask) {
		List<String> filesList = new ArrayList<String>();
        Pattern pattern = Pattern.compile(mask);
		for (File fileEntry : folder.listFiles()) {
	      if (fileEntry.isDirectory()) 
	      {
	    	  filesList.addAll(listFilesForFolder(fileEntry, mask));
	    	 /* Common.print(fileEntry.getName());
	    	  for(File file : fileEntry.listFiles()){
	    		  if((pattern.matcher(fileEntry.getName()).matches())){
	    			  filesList.add(folder.getAbsolutePath()+ "\\" + fileEntry.getName());
	    		  }
	    	  }*/

	      } 
	      else 
	      {
	        if (fileEntry.isFile()) 
	        {
	          if (
//	        		  (fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1, fileEntry.getName().length()).toLowerCase()).equals("xml") && 
	        		  (pattern.matcher(fileEntry.getName()).matches()))
	        	  filesList.add(folder.getAbsolutePath()+ "\\" + fileEntry.getName());
	        }
	      }
	    }
	return filesList;
	}

	public static Integer countFilesForFolder(File folder, String mask) {
		List<String> filesList = new ArrayList<String>();
        Pattern pattern = Pattern.compile(mask);
        for (File fileEntry : folder.listFiles()) {
	      if (fileEntry.isDirectory()) 
	      {
	      } 
	      else 
	      {
	        if (fileEntry.isFile()) 
	        {
	          if (
//	        		  (fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1, fileEntry.getName().length()).toLowerCase()).equals("xml") && 
	        		  (pattern.matcher(fileEntry.getName()).matches()))
	        	  filesList.add(folder.getAbsolutePath()+ "\\" + fileEntry.getName());
	        }
	      }
	    }
	return filesList.size();
	}

	/** Method realizes reading file with TADS messages and taking 1st tads xml from it. TADS is the xml enclosed with <ta_message> tag.
	 * @param filePath - path to file with TADS
	 * @return - string with 1st xml
	 */
	public static String readAndFormFirstXML(String filePath)
	{
		BufferedReader reader = null;
		String[] causes = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line, results = "";
			try {
				while( ( line = reader.readLine() ) != null)
				{
					results += line;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				Common.printError("Failed to read lines from XML");
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				Common.print("Failed to close XML");
			}
			causes = StringUtils.substringsBetween(results, 
					"<?xml", 
					"</ta_message>");
			try {
				Common.print("TADS in xml: " + causes.length);
				for(int i=0; i<causes.length; i++)
				{
					causes[i]= "<?xml" + causes[i]+"</ta_message>";
					//			print(causes[i]);
				}
			}
			catch (NullPointerException e3) 
			{
				causes = StringUtils.substringsBetween(results, 
						"<ta_message", 
						"</ta_message>");
				Common.print("TADS in xml: " + causes.length);
				for(int i=0; i<causes.length; i++)
				{
					causes[i]= "<ta_message" + causes[i]+"</ta_message>";
					//				print(causes[i]);
				}
			}
		}
			catch (FileNotFoundException e2) {
				Common.printError("XML File Not Found");
                //System.out.println("XML File Not Found");
                e2.printStackTrace();
			}
			return causes[0];		
		}

	public static String[] readAndFormAllXML(String filePath)
	{
		 BufferedReader reader = null;
		 String[] causes = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		    String line, results = "";
		    try {
				while( ( line = reader.readLine() ) != null)
				{
				    results += line;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				Common.print("Failed to read lines from XML");
			}
		    try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				Common.print("Failed to close XML");
			}
		causes = StringUtils.substringsBetween(results, 
               "<?xml", 
               "</ta_message>");
		for(int i=0; i<causes.length; i++)
		{
			causes[i]= "<?xml" + causes[i]+"</ta_message>";
//			print(causes[i]);
		}
		} 
		catch (FileNotFoundException e2) {
			Common.print("XML File Not Found");
			e2.printStackTrace();
		}
		return causes;		
	}

	public static String getFileNameFromAbsPath (String fileWithAbsPath)
	{
		return new File (fileWithAbsPath).getName();
	}
	
	public static String updateFileName (String fileName, String newPart)
	{
		String extension = FilenameUtils.getExtension(fileName);
		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
		return fileNameWithOutExt+newPart+"."+extension;
	}

	public static List<String> readLines(String filename) {
		FileReader fileReader;
		List<String> lines = new ArrayList<String>();
		try {
			fileReader = new FileReader(filename);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static void fileRewriterFileOutputStream (String filenameWithPath, String content)
	{
	File myFoo = new File(filenameWithPath);
	try {
		FileOutputStream fooStream = new FileOutputStream(myFoo, false); // true to append, false to overwrite.
		fooStream.write(content.getBytes());
		fooStream.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public static void fileRewriterFileWriter (String filenameWithPath, String content)
	{
	File myFoo = new File(filenameWithPath);
	FileWriter fooWriter;
	try {
		fooWriter = new FileWriter(myFoo, false);  // true to append, false to overwrite.
		fooWriter.write(content);
		fooWriter.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}

	public static void sleep (Integer time)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        Common.print("N: " + N);
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
//                Common.print("row.get(i): " + row.get(i) + "; " + i);
            }
            ret.add(col);
//            Common.print(col+ "; " + i);
        }
//        Common.print(ret);
        return ret;
    }

	public static String evaluateMathExpression (String expression)
	{
		//	    print(expression);
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
//		print("expression: " + expression);
		double result = 0;
		try {
			result = (Double) engine.eval(expression);
			//			print(result);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		if (result==-0.0)
			result=Math.abs(result);
		
		int i = (int) result;
		  return result == i ? String.valueOf(i) : String.valueOf(result);
//		return String.format("%.10f", result);
	}
	
		
	public static Date subtractDay(Date date, int shift) {

		SimpleDateFormat formatter_days_names = new SimpleDateFormat("EE");

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DAY_OF_MONTH, shift);
	    String dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,Locale.UK);
	    if (dayOfWeek.equals("Sun"))
	    	    cal.add(Calendar.DAY_OF_MONTH, -2);
	    		else if (dayOfWeek.equals("Sat"))
	    		cal.add(Calendar.DAY_OF_MONTH, -1);
	    print(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,Locale.UK));
	    return cal.getTime();
	}
	
	public static Date dateStringToDateConverter(String dateInString) {
	
	Date date = null;
	try {
 
		date = formatter.parse(dateInString);
//		print(formatter.format(date));
 
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return date;
	}

	public static String dateDateToStringConverter(Date date) {
		return formatter.format(date);
	}

}
	