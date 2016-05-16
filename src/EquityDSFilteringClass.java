import au.com.bytecode.opencsv.CSVReader;
import com.ximpleware.*;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquityDSFilteringClass {
	static HashMap<String, String> globalVariables = new HashMap<String, String> (); 
	static String system = null;
	static String IDxpath = null;
	static List<String> output = new ArrayList<String>();
	public static String eol = System.lineSeparator();
	
	public static void FilteringInit(String fullJSONSourcePath, String TADSfolder)
	{
		String path = null;

		String Source = formSource(new File (fullJSONSourcePath));
		print("Source file: " + Source);
		path = formPath(new File (fullJSONSourcePath));
		print("Source file path: " + path);
		system = "PnL";

		IDxpath = "//accountable_id/text()";

		globalVariables.put("source_file",Source);
		globalVariables.put("path",path);
		globalVariables.put("TADS",TADSfolder);
		globalVariables.put("static_folder",globalVariables.get("path").substring(0,globalVariables.get("path").substring(0,globalVariables.get("path").lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static");

		print("Static files path: " + globalVariables.get("static_folder"));
		print("TADS files path: " + globalVariables.get("TADS"));
	}
	
	public static List<String> FilteringReturnResult ()
	{
		return output;
	}

	public static void clearingOutput ()
	{
		output.clear();
	}
	
	static String fakeXML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<ta_message>"+
			"<adjustment>"+
			"</adjustment>"+
			"</ta_message>";

	public static String readFileToString(String filePath)
	{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		String line, results = "";
		try {
			while( ( line = reader.readLine() ) != null)
			{
				results += line;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static String[] readFilter (String csvFilename)
	{
		String[] row = null;
		try {
			CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
			row = csvReader.readNext();
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//	printList(row);
		return row;
	}

	public static ArrayList formFIdessaCSVSource () throws IOException
	{
		String csvFilename = globalVariables.get("path")+"//"+globalVariables.get("source_file");
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		ArrayList arraylist=new ArrayList();
		CSVReader csvReader = null;
		int i =0;	
		csvReader = new CSVReader(new FileReader(csvFilename));
		while((row = csvReader.readNext()) != null) {
			container.add(row);
		}
		csvReader.close();

		//fill Hashmap
		names = (String[]) container.get(0);
		Integer j=0;
		for (Object object : container) {
			HashMap<String, String> map = new HashMap<String, String>();
			if (j>0&&j<container.size()-1)
			{
				int marker = 0;
				row = (String[]) object;
				for (i=0; i<row.length; i++)
				{
					map.put(names[i].trim(),row[i]);
					//printHashmap(map);
				}
				arraylist.add(map);
			}
			j++;
		}
		return arraylist;
	}

	public static ArrayList formCBICSVSource () throws IOException
	{
		String csvFilename = globalVariables.get("path")+"//"+globalVariables.get("source_file");
//		String OMNIS_csvFilename = globalVariables.get("path")+"//OMNIS_"+globalVariables.get("source_file");
		String OMNISfilename = null;
		
		String notOMNISfilename = null;

		try
		{
				notOMNISfilename = Common.listFilesForFolder(new File (globalVariables.get("path")), globalVariables.get("CBICBEUMASK")+".*").get(0);
				OMNISfilename = Common.listFilesForFolder(new File (globalVariables.get("path")), globalVariables.get("CBIOMNISMASK")+".*").get(0);

		}
		catch (Exception e)
		{
			// do nothing - this is not a CBI test
		}
		
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		ArrayList arraylist=new ArrayList();
		CSVReader csvReader = null;
		int i =0;	
		if (csvFilename.contains("OMNIS"))
		{
			csvReader = new CSVReader(new FileReader(csvFilename));
			while((row = csvReader.readNext()) != null) {
				container.add(row);
				i++;
			}
			//	System.out.println("csvReader: " + i);

			csvReader.close();
		}
		else
		{	
			csvReader = new CSVReader(new FileReader(OMNISfilename));
			container.add(csvReader.readNext());
			i++;
			csvReader.close();
			//	System.out.println("csvReader: " + i);
			csvReader = new CSVReader(new FileReader(csvFilename));
			i=0;
			while((row = csvReader.readNext()) != null) {
				//		System.out.println("csvReader: " + row[0]);

				container.add(row);
				i++;
			}
			//	System.out.println("csvReader: " + i);
			csvReader.close();
		}
		//fill Hashmap
		names = (String[]) container.get(0);
		Integer j=0;
		for (Object object : container) {
			HashMap<String, String> map = new HashMap<String, String>();
			if (j>0&&j<container.size())
			{
				int marker = 0;
				row = (String[]) object;
				for (i=0; i<row.length; i++)
				{
					if (names[i].equals("Avg Cost"))
					{
						map.put(names[i]+marker,row[i]);
						marker++;
					}
					else
					{
						map.put(names[i],row[i]);
					}
					//printHashmap(map);
				}
				arraylist.add(map);
			}
			j++;
		}
		return arraylist;
	}

	public static ArrayList formEUSCSVSource () throws IOException
	{
		String OMNIS_csvFilename = globalVariables.get("path")+"\\"+globalVariables.get("source_file");
		//	print("OMNIS_csvFilename: "+OMNIS_csvFilename);
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		ArrayList arraylist=new ArrayList();
		CSVReader csvReader = null;
		int i=0;	
		csvReader = new CSVReader(new FileReader(OMNIS_csvFilename));
		while((row = csvReader.readNext()) != null) {
			//		print(row[0]);
			if (i==0)
				names = row[0].split("\\|", -1);
			else
				container.add(row);
			i++;
		}
		csvReader.close();
		//	printList(names);
		//fill Hashmap
		//		print(String.valueOf("container.size: "+container.size()));
		int j = 0;
		for (Object object : container) {
			HashMap<String, String> map = new HashMap<String, String>();
			row = (String[]) object;
			row = row[0].toString().split("\\|", -1);
			for (i=0; i<row.length; i++)
			{
				map.put(names[i],row[i]);
				//			    printHashmap(map);
			}
			arraylist.add(map);
		}
		return arraylist;
	}

	public static String formSource (File filepath)
	{
		return filepath.getName();
	}

	public static String formPath (File filepath)
	{
		String absolutePath = filepath.getAbsolutePath();
		return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
	}

	public static File[] filesList (File dir, final String mask)
	{
		File[] foundFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				//		         return name.matches("data.*auto");//.endsWith("_auto");
				//		         return name.matches("Processed_TADS_File_.*xml");
				//		         Processed_TADS_File_.*xml; Discarded_TADS_File_2.xml
				return name.matches(mask);
			}
		});
		return foundFiles;
	}

	public static String checkOldInstrument(String RealSecNumber)
	{
		if (RealSecNumber.startsWith("ZZ"))
			return RealSecNumber.substring("ZZ".length());
		else
			return RealSecNumber;
	}

	public static String formRIC (HashMap<String, String> map)
	{
		String RIC = null;
//		printHashmap(map);
		if (system.equals("CBImagine"))
			RIC = "CBI" + "/"+ map.get("PortId");
		else if (system.equals("Fidessa"))
			RIC = "FIDESSA" + "/"+ map.get("Portfolio No") + "/"+ map.get("RIC") + "/"+ map.get("DBT Security No")+"/"+ checkOldInstrument(map.get("Real Security No"))+"/"+ map.get("Ccy")+"/"+ map.get("SecType")+"/"+map.get("Legal Entity");
		else if (system.equals("EUS"))
		{
			if (map.get("Source").equals("EUS-PMACLN"))
				RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("FO Book") + "/"+ map.get("ISIN") + "/"+ map.get("FO Currency") + "/"+ map.get("RIC")  + "/"+  map.get("BO Code");
			else if (map.get("Source").equals("EUS-IMGOMLN") || map.get("Source").equals("EUS-IMGCBLN"))
				RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("Position Description");
			else 
				RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("Position Description");
		}
		else if (system.equals("ESS"))
			RIC = "ESS" + "/"+ map.get("SwapNum");
		else  if (system.equals("ESS-PLESR"))
			RIC = "ESS-PLESR" + "/"+ map.get("SwapNum");
		return RIC;
	}

	public static String applyFilter (List<String> xpathFormed)
	{
		//		List<String> xpathValues = new ArrayList<String>();
		String xpathValues = null;
		//		printList(xpathFormed);
		try {
			xpathValues = getStaticXMLvalue(globalVariables.get("static_folder")+"\\RD_BOOK.xml",xpathFormed);
			//			print("xpathValues");
			//			printList(xpathValues);
			//			print("xpathValues");
		} catch (ParseException | XPathParseException | XPathEvalException
				| NavException | IOException e) {
			e.printStackTrace();
		}
		//		print("xpathValues: " + xpathValues);
		return xpathValues;
	}

	public static String prepareXpathForStatic (String xpath, String whatToReplace, String value)
	{
		return xpath.replace(whatToReplace, value);
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

	public static String replaceCSVTags (String text, HashMap<String, String> csv_source)
	{
		//print("EUSBookXpath text: " + text);
		//		staticSourceSystem = row_map.get("Source").substring("EUS-".length());


		//		if (map.get("Source").equals("EUS-PMACLN"))
		//			RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("FO Book") + "/"+ map.get("ISIN") + "/"+ map.get("FO Currency") + "/"+ map.get("RIC")  + "/"+  map.get("BO Code");
		//		else if (map.get("Source").equals("EUS-IMGOMLN") || map.get("Source").equals("EUS-IMGCBLN"))
		//			RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("Position Description");


		for (Entry<String, String> entry  : csv_source.entrySet()) 
		{
			if (system.equals("Fidessa")&&(text.contains(entry.getKey())))
			{
				if (!entry.getKey().equals("Portfolio"))
					text = text.replace(entry.getKey(), entry.getValue());
			}
			else
				if (system.equals("EUS")&&(text.contains(entry.getKey())))
				{
					print("FO Book: " + entry.getValue());
					//					print("csv_source.get(Source): "+ csv_source.get("Source"));
					if (entry.getKey().equals("FO Book"))
						try
					{
							//						print(entry.getValue().substring(0,entry.getValue().length()-csv_source.get("Source").substring("EUS-".length()).length()-"840".length()));
							//						String book = getByRegex(entry.getValue(),"^[0-9]+").get(0);
							//						text = text.replace(entry.getKey(),getByRegex(entry.getValue(),"^[0-9]+").get(0));
							text = text.replace(entry.getKey(),entry.getValue().substring(0,entry.getValue().length()-csv_source.get("Source").substring("EUS-".length()).length()-"840".length()));
					}
					catch (StringIndexOutOfBoundsException|NullPointerException e)
					{
						text = text.replace(entry.getKey(), "");
					}
				}
				else
					text = text.replace(entry.getKey(), entry.getValue());
		}
		return text;
	}

	public static String replaceRIC (String text, HashMap<String, String> map)
	{
		if(system.equals("Fidessa"))
			text = text.replace("Portfolio No", map.get("Portfolio No"));
		if(system.equals("CBImagine"))
			text = text.replace("Acct", map.get("Acct"));
		if(system.equals("EUS"))
			text = text.replace("FO Book", formStaticFO_Book(map.get("FO Book"),map));
		if(system.equals("ESS"))
			text = text.replace("Book", formStaticFO_Book(map.get("Book"),map));
		return text;
	}


	public static String takeValueInCSV (HashMap<String, String> map, String value)
	{
		return map.get(value);
	}

	public static String enrichFilter (String filter, HashMap<String, String> record)
	{
		return filter = replaceCSVTags(filter, record);
	}

	public static String logicXpathVerifier (String xml, String static_xpath)
	{
		XdmValue logicXpathValue = null;
		try {
			Processor proc = new Processor(false);
			XPathCompiler xpath = proc.newXPathCompiler();
			net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
			StringReader reader = new StringReader(xml);
			XdmNode docXdmNode = builder.build(new StreamSource(reader));
			//        System.out.println("logic Xpath : " + static_xpath);
			XPathSelector selector;
			selector = xpath.compile(static_xpath).load();
			selector.setContextItem(docXdmNode);

			logicXpathValue = selector.evaluate();
			//        print(logicXpathValue.toString());
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		return logicXpathValue.toString();
	}

	public static String getStaticXMLvalue (String staticXMLFilepath, List<String> xpaths) throws IOException, EncodingException, EOFException, EntityException, ParseException, XPathParseException, XPathEvalException, NavException 
	{
		List<String> foundValue = new ArrayList<String>(); ;
		File f = new File(staticXMLFilepath);
		FileInputStream fis = new FileInputStream(f);
		byte[] b = new byte[(int) f.length()];
		fis.read(b);
		VTDGen vg = new VTDGen();
		vg.setDoc(b);
		vg.parse(false);
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		for (int i = 0; i < xpaths.size(); i++)
		{
			ap.selectXPath(xpaths.get(i));
			int result = -1;
//					System.out.println("xpaths.get(i): "+xpaths.get(i));
			while((result = ap.evalXPath())!=-1)
			{
				if (xpaths.get(i).matches(".*/@.*$"))
					foundValue.add(vn.toNormalizedString(result+1));
				else
					foundValue.add(vn.toString(result));
			}
		}
//				printList(foundValue);
		//      System.out.println("printList foundValue");
		if (foundValue.size() == 0)
			foundValue.add("N");
		return foundValue.get(0); 
	}

	public static String[] readAndFormXML(String filePath)
	{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		String line, results = "";
		try {
			while( ( line = reader.readLine() ) != null)
			{
				results += line;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[]causes = StringUtils.substringsBetween(results, 
				"}	", 
				"</ta_message>");
		for(int i=0; i<causes.length; i++)
		{
			causes[i]=causes[i]+"</ta_message>";
			//			System.out.println(causes[i]);
		}
		return causes;		
	}

	public	static String formTADSpath (String baseFolder)
	{
		String folder = null;
		switch (baseFolder) {
		case "FILPUB":  folder = globalVariables.get("TADS")+"\\SRV_FILPUB\\data";
		break;
		case "VAL":  folder = globalVariables.get("TADS")+"\\SRV_VAL\\data";
		break;
		case "VALdisc": folder = globalVariables.get("TADS")+"\\SRV_VAL\\discarded";
		break;
		case "FILTER": folder = globalVariables.get("TADS")+"\\SRV_FILTER\\data";
		break;
		case "FILTERdisc": folder = globalVariables.get("TADS")+"\\SRV_FILTER\\discarded";
		break;
		case "PREFIL":  folder = globalVariables.get("TADS")+"\\SRV_PREFIL\\data";
		break;
		case "PREFILdisc": folder = globalVariables.get("TADS")+"\\SRV_PREFIL\\discarded";
		break;
		case "TRANS": folder = globalVariables.get("TADS")+"\\SRV_TRANS\\data";
		break;
		case "TRANSdisc": folder = globalVariables.get("TADS")+"\\SRV_TRANS\\discarded";
		break;
		case "ENRICH": folder = globalVariables.get("TADS")+"\\SRV_ENRICH\\data";
		break;
		case "ENRICHdisc": folder = globalVariables.get("TADS")+"\\SRV_ENRICH\\discarded";
		break;
		}                 
		return folder;
	}

	public static Boolean processFiles (String folder,String RIC, String mask)
	{
		File[] foundFiles = filesList(new File(formTADSpath(folder)),mask);
		Boolean marker = false;
		if (system.contains("ESS")) RIC = RIC+"/";
		
		try
		{
			if (foundFiles.length != 0)
			{
//				Common.print(foundFiles[0]);
				for (int i=0;i<foundFiles.length; i++)
				{
					String[] xmls = readAndFormXML (foundFiles[i].toString());
					for(String n: xmls)
					{
						if (n.contains(RIC))
						{
							//					print ("true - Correct filtering for "+RIC);
							marker = true;
							break;
						}
//						print ("RIC: " + RIC + " :::: " + n);
					} 
				}
			}
			else print ("No TADS to process for "+folder);
		}
		catch (NullPointerException e)
		{
			print ("No TADS to process for "+folder);
		}
		//		if(!marker)
		//			print ("fail - qualified record didn't found in "+folder);

		return marker;
	}

	public static String findTADSFile (String folder,String RIC, String mask)
	{
		File[] foundFiles = filesList(new File(formTADSpath(folder)),mask);
		String marker = "";
		try
		{
			if (foundFiles.length != 0)
			{
				for (int i=0;i<foundFiles.length; i++)
				{
					String[] xmls = readAndFormXML (foundFiles[i].toString());
					for(String n: xmls)
					{
						if (n.contains(RIC))
						{
							return n;
						}
					} 
				}
			}
			else print ("No TADS to process for "+folder);
		}
		catch (NullPointerException e)
		{
			print ("No TADS to process for "+folder);
		}
		return marker;
	}
		
	public static void findRecordPlace (String RIC)
	{
		Boolean marker = false;
		Boolean mark = false;
		String folders[] = new String[] {"FILTERdisc","ENRICHdisc", "TRANSdisc", "PREFILdisc"};

		for (String s: folders)
		{
			marker = processFiles(s, RIC,".*auto");
			if (!marker)
				print ("Record didn't found in "+s+" folder");
			else
			{
				print ("Record found in "+s+" folder");
				//				mark = true;
				break;
			}
		}
	}

	public static void findRecord(String RIC, String decision)
	{
		boolean marker = false;

		if (decision.equals("Y"))
		{
			print("record with id: "+RIC+" qualifies VALUATION");
			marker = processFiles("VAL", RIC,".*auto");
//			marker = processFiles("FILPUB", RIC,".*");
			if (!marker)
			{
				print ("fail - qualified record didn't found in VAL - data folder");
				marker = processFiles("VALdisc", RIC,".*auto");
				if (!marker)
				{
					print ("fail - qualified record didn't found in VAL - disc folder");
					findRecordPlace(RIC);
				}
				else
					print ("Qualified record found in VAL - disc folder");
			}
			else
				print ("true - qualified record found in VAL - data folder");
		}
		else
		{
			print("record with id: "+RIC+" qualifies FILTERING");
			marker = processFiles("VAL", RIC,".*auto");
//			marker = processFiles("FILPUB", RIC,".*");
			if (!marker)
			{
				print ("true - qualified FILTERING record didn't found in VAL - data folder");
				marker = processFiles("VALdisc", RIC,".*auto");
				if (!marker)
					print ("true - qualified FILTERING record didn't found in VAL - disc folder");
				else
					print ("fail - qualified FILTERING record found in VAL - disc folder");
			}
			else
			{
				print ("fail - qualified FILTERING record found in VAL - folder");
			}
			findRecordPlace(RIC);
		}		
	}

	public static String formStaticFO_Book (String FO_Book, HashMap<String, String> map)
	{
		print("FO_Book: "+FO_Book);
		String staticSourceSystem = map.get("Source").substring("EUS-".length());
		print("staticSourceSystem :"+staticSourceSystem);
		try
		{
			return FO_Book.substring(0,FO_Book.length()-(staticSourceSystem+"840").length());
		}
		catch (StringIndexOutOfBoundsException e)
		{
			return "";
		}
	}

	public static String getEUSfilterField(String field, HashMap<String, String> map)
	{
		String eusFilter = null;
		if (field.equals("Source"))
			return eusFilter = map.get("Source");
		if (map.get("Source").equals("EUS-PMACLN"))
			eusFilter = map.get("BO Code");
		else if (map.get("Source").equals("EUS-IMGOMLN") || map.get("Source").equals("EUS-IMGCBLN"))
			eusFilter = map.get("DBSecType");
		return eusFilter;
	}

	public static String takeSubstringsBetween (String exp, String leftSeparator, String rightSeparator)
	{
		return StringUtils.substringBetween(exp, leftSeparator, rightSeparator);
	}

	public static void filteringXpathBased (String filterFile)
	{
		ArrayList arraylist = new ArrayList<>();

		try {
			if (system.equals("Fidessa"))
				arraylist= formFIdessaCSVSource();
			if (system.equals("CBImagine"))
				arraylist= formCBICSVSource();
			if (system.equals("EUS")||system.contains("ESS"))
				arraylist= formEUSCSVSource();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
			row_map = (HashMap<String, String>) object_map;		    	
			//		    	printHashmap(row_map);
			String csvRIC = formRIC(row_map);
			//				print("csvRIC: "+csvRIC);
			String[] filter = readFilter (globalVariables.get("path")+filterFile);
			//				filter[0] = replaceRIC(filter[0], row_map);
			filter[0] = replaceCSVTags(filter[0], row_map);
			print("filter[0]: "+filter[0]);
			if (filter[0].contains("<static>"))
			{
				//find and substitute static value
				String static_xpath = takeSubstringsBetween(filter[0],"<static>","</static>");
				//		        	print("static_xpath: "+static_xpath);
				String staticValue = null;
				try {
					staticValue = getStaticXMLvalue(globalVariables.get("static_folder")+"\\RD_BOOK.xml",Arrays.asList(static_xpath.split("$$$")));
				} catch (ParseException | XPathParseException
						| XPathEvalException | NavException | IOException e1) {
					e1.printStackTrace();
				}
				System.out.println("static xpath value: "+staticValue);
				try
				{
					filter[0] = filter[0].replaceAll("<static>.*?</static>", staticValue);
				}
				catch (NullPointerException e)
				{
					print("There is no found Isin value for record: "+static_xpath);
					filter[0] = filter[0].replaceAll("<static>.*?</static>", "false");
				}

				//		        	print("static xpath value: "+filter[0]);
			}		
			List<String> filterList = new ArrayList<String>();
			filterList.add(filter[0]);
			//				print("filterList");
			//				printList(filterList);
			String decision = null;
			if (filterList.get(0).contains("if"))
				decision = logicXpathVerifier(fakeXML,filterList.get(0));
			else
				decision = applyFilter(filterList);
			print("decision: " + decision);
			//				printList(decision);
			try
			{
				findRecord(csvRIC, decision);
			}
			catch (IndexOutOfBoundsException e)
			{
				findRecord(csvRIC, "N");
				//					print("fails");					
			}
		}		
	}
	public static void filteringListbased (String CSVField, String filterFile)
	{
		ArrayList arraylist = new ArrayList<>();
		try {
			if (system.equals("Fidessa"))
				arraylist= formFIdessaCSVSource();
			if (system.equals("CBImagine"))
				arraylist= formCBICSVSource();
			if (system.equals("EUS") ||system.contains("ESS"))
				arraylist= formEUSCSVSource();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String filter = readFileToString(globalVariables.get("path")+filterFile);
		print("Filter file: "+ filter);


		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
			row_map = (HashMap<String, String>) object_map;
			//		    	printHashmap(row_map);
			String csvRIC = formRIC(row_map);
			//				print("csvRIC: "+csvRIC);
			String filterField = "";
			if (system.equals("EUS"))
				filterField = getEUSfilterField(CSVField,row_map);
			else if (system.contains("ESS") && CSVField.contains("-")) 
			{
				ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(CSVField.split("-")));
				for (int i=0; i< thearray.size(); i++)
				{
					//			    		print(row_map.get(thearray.get(i)));
					filterField += row_map.get(thearray.get(i))+"/";
				}
				filterField = filterField.substring(0, filterField.length()-1);
			}
			else
				filterField = row_map.get(CSVField);
			print("Filter field : "+filterField);
			String[] filterList = filter.split(",");
			if (system.contains("ESS") && CSVField.contains("-"))
			{
				if (ListContainsValue(filterList,filterField,"product"))
					findRecord(csvRIC, "Y");
				else
					findRecord(csvRIC, "N");
			}
			else
			{
				if (ListContainsValue(filterList,filterField,"xxx"))
					findRecord(csvRIC, "Y");
				else
					findRecord(csvRIC, "N");
			}
		}		
	}

	public static Boolean ListContainsValue (String[] filterList, String value, String marker)
	{
		Boolean decision = false;
		//	printList(fromCSVValue);
		ArrayList<String> bool = new ArrayList<String>();// = new Boolean [fromListValue.size()];
		if (!StringUtils.isNotBlank(value))
			value = "null";
			
		for (int i = 0; i < filterList.length; i++)	
		{
//			Common.print("filterList[i]: " + filterList[i] + " ; value: " +  StringUtils.isNotBlank(value));
			if (marker.equals("product"))
			{
				//filterList[i]=Swap PV/N/X
				//value=Swap PV/N/Comm
				String pattern = filterList[i].replace("X", ".*?");
				Pattern p = Pattern.compile(pattern);
				Matcher m = p.matcher(value);
				if (m.matches())
				{
					decision = true;
					break;
				}
				//				print(String.valueOf(b));
				//				print("romListValue.get(j): "+filterList[i]);
			}
			else 
				if (filterList[i].equals(value))
			{
				decision = true;
				break;
			}
		}
		return decision;
	}

	public static List<String> validateTADSFiltering(String[] args, Properties props) {
		String Source = null;
		String path = null;
		String arguments = "";

		Source = formSource(new File (args[0]));
		print("Source file: " + Source);
		path = formPath(new File (args[0]));
		print("Source file path: " + path);

		system = args[2];
		print("System: "+system);
		//		IDxpath = system.equals("EUS")?"//adjustment/accountable_id/text()":"//valuation/accountable_id/text()";
		IDxpath = "//accountable_id/text()";
//		CBIOMNISMASK=OMNIS-PLRISK
//		CBICBEUMASK=CB-EU-PLRISK
		globalVariables.put("CBIOMNISMASK",props.getProperty("CBIOMNISMASK"));
		globalVariables.put("CBICBEUMASK",props.getProperty("CBICBEUMASK"));
		globalVariables.put("source_file",Source);
		globalVariables.put("path",path);
		globalVariables.put("TADS",args[1]);
		globalVariables.put("static_folder",globalVariables.get("path").substring(0,globalVariables.get("path").substring(0,globalVariables.get("path").lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static");

		print("Static files path: " + globalVariables.get("static_folder"));
		print("TADS files path: " + globalVariables.get("TADS"));
		print("Verifications to perform: " + args[3]);

		if (args[3].contains("Xpath"))
		{
			filteringXpathBased("\\"+args[3]);
		}

		if (args[3].contains("List"))
		{
			if (system.equals("Fidessa"))
				filteringListbased("SecType","\\"+args[3]);
			if (system.equals("CBImagine"))
				filteringListbased("DBSecType","\\"+args[3]);
			if (system.equals("EUS"))
			{
				if (args[3].contains("Source"))
					filteringListbased("Source","\\"+args[3]);
				else
					filteringListbased("Product","\\"+args[3]);
			}
			if (system.contains("ESS"))
			{
				if (args[3].contains("_Combination_"))
				{
					filteringListbased(StringUtils.substringAfterLast(args[3],"_"),"\\"+args[3]);
				}
				else 
				{
					if (args[3].contains("BTBIndicator"))
						filteringListbased("BTBIndicator","\\"+args[3]);
					if (args[3].contains("PostToGL"))
						filteringListbased("PostToGL","\\"+args[3]);
					if (args[3].contains("FinConGrp"))
						filteringListbased("FinConGrp","\\"+args[3]);
				}
			}
		}
		return output;
	}


	public static void printHashmap (HashMap<String, String> row_map)
	{
		for (Entry<String, String> entry  : row_map.entrySet()) 
			print("Key : " + entry.getKey() + " Value : " + entry.getValue());

	}
	public	static void printList (List<String> xpaths_dynamic)
	{
		for (int i=0; i<xpaths_dynamic.size(); i++)
			print(xpaths_dynamic.get(i));
	}
	public	static void printList (String[] xpaths_dynamic)
	{
		for (int i=0; i<xpaths_dynamic.length; i++)
			print(xpaths_dynamic[i]);
	}

	public	static void print (String value)
	{
		Common.print(value);
		output.add(eol + value);
	}


}
