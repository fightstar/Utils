

import au.com.bytecode.opencsv.CSVReader;
import com.ximpleware.extended.*;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public abstract class TADSValidateCommonActions {
	static String Source = null;
	static String BL = null;
	static HashMap<String, String> globalVariables = new HashMap<String, String> (); 
	static List<String> output = new ArrayList<String>();
	public static String eol = System.getProperty("line.separator");  
	static String IDxpath = "//accountable_id/text()";
	static Properties props = new Properties();

	static int print_xpath_plain = 0;
	static int print_xpath_source = 0;
	static int print_xpath_logic = 0;
	static int print_xpath_static = 0;
	static int print_xpath_sql = 0;
	static int print_xpath_ccy = 0;
	static String path = null;

	static String staticSource = null;
	static String staticSourceSystem = null;
	static HashMap <String,String> sourceMap = new HashMap <String,String>();

	public static List<String> getStaticXMLvalue (String staticXMLFilepath, List<String> xpaths)  
	{
//		Common.print("File with static data:" + staticXMLFilepath);
		Common.print("File with static data:" + xpaths);
		List<String> foundValue = new ArrayList<String>(); ;
		VTDGenHuge vgh = new VTDGenHuge();
		VTDNavHuge vnh = null; 
		if (vgh.parseFile(staticXMLFilepath,false,VTDGenHuge.MEM_MAPPED))
		{
			try
			{
			vnh = vgh.getNav();
			AutoPilotHuge aph = new AutoPilotHuge(vnh);
			for (int i = 0; i < xpaths.size(); i++)
			{
				Common.print("get Static XML value xpaths: " + xpaths.get(i));
				try {
					aph.selectXPath(xpaths.get(i));
					int result = -1;
					while ((result=aph.evalXPath())!=-1){
						if (xpaths.get(i).matches(".*/@.*$"))
							foundValue.add(vnh.toNormalizedString(result+1));
						else
							foundValue.add(vnh.toString(result));
//						Common.print(" element name is "+vnh.toString(i));
//						Common.print("Element name ==> "+ vnh.toNormalizedString(i+1));
					}
				} catch (XPathParseExceptionHuge e) {
					Common.print("XPathParseExceptionHuge");
					e.printStackTrace();
				} catch (XPathEvalExceptionHuge e) {
					Common.print("XPathEvalExceptionHuge");
					e.printStackTrace();
				} catch (NavExceptionHuge e) {
					Common.print("NavExceptionHuge");
					e.printStackTrace();
				}
			}
			}
			catch (IllegalArgumentException e)
			{
				Common.print("IllegalArgumentException - seems that there is no file on the disk: " + staticXMLFilepath);
			}
		}
		else
			Common.print("Parsing of static file fails: " + staticXMLFilepath);
			
		return foundValue; 
	}

	public static String takeLogicXpath (String xml, String static_xpath)
	{
		Processor proc = new Processor(false);
		XPathCompiler xpath = proc.newXPathCompiler();
		net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
		StringReader reader = new StringReader(xml);
		XdmNode docXdmNode = null;
		XdmValue logicXpathValue = null;
		try {
			docXdmNode = builder.build(new StreamSource(reader));
			XPathSelector selector = xpath.compile(static_xpath).load();
			selector.setContextItem(docXdmNode);
			logicXpathValue = selector.evaluate();
			//		    Common.print("Xpath value: " + logicXpathValue.toString());
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		return logicXpathValue.toString();
	}

	//read csv file with CSVreader and fill arraylist of hashmaps from values linked to header values row 
	public static ArrayList<?> readCSVByLines (String csvFilename) throws IOException
	{
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		ArrayList arraylist=new ArrayList();
		Integer marker = 0;
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
				row = (String[]) object;
				for (int i=0; i<row.length; i++)
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
					Common.print ("field: "+ names[i]+"; value: "+row[i]);
				}
				arraylist.add(map);
			}
			j++;
		}
		return arraylist;
	}

	//read file to list - used in reading csv files with xpaths and conversions
	public static List<String> readLines(String filename) throws IOException {
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
		bufferedReader.close();
		return lines;
	}

	public static String[] readAndFormXML(String filePath)
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
			/* - old version
			for(int i=0; i<causes.length; i++)
			{
				causes[i]= "<?xml" + causes[i]+"</ta_message>";
				//			print(causes[i]);
			}
			*/
			
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
			Common.print("XML File Not Found");
			e2.printStackTrace();
		}
		return causes;		
	}

	//read file with xpaths for verification	
	public static List<String> xpaths (String filePath)
	{
		List<String> lines = null;
		try {
			lines = readLines(filePath);
		} catch (IOException e1) {
			Common.print("Impossible to read file with xpaths");
			lines = Arrays.asList(filePath.split(";"));
		}
		return lines;
	}

	//function for work with XML - document builder, xpath evaluation etc	
	public static DocumentBuilder formXMLbuilder () {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Common.print("Failed to form XML builder");
			e.printStackTrace();
		}
		return builder;
	}

	public static Document formXMLdocument (String n) {
		Document doc = null;
		try {
			doc = formXMLbuilder().parse(new InputSource(new ByteArrayInputStream(n.getBytes("UTF-8"))));
		} catch (SAXException | IOException e) {
			Common.print("Failed to form XML document");
			e.printStackTrace();
		}
		return doc;
	}

	public static XPath createXpath ()
	{
		XPathFactory xFactory = XPathFactory.newInstance();
		XPath xpath = xFactory.newXPath();
		return xpath;
	}

	public static XPathExpression xpathCompile (XPath xpath, String exp)
	{
		XPathExpression expr = null;
		try {
			expr = xpath.compile(exp);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			Common.print("Failed to compile XPATH");
		}
		return expr;
	}

	public static String xpathEvaluate (XPathExpression expr, Document doc)
	{
		Node str = null;
		String result = null;
		try {
			str = (Node) expr.evaluate(doc,XPathConstants.NODE);
			try {
				result = str.getNodeValue();
			} catch (NullPointerException e) {
				result = "";
			}
		} catch (javax.xml.xpath.XPathExpressionException e) {
			try {
				//				print((String) expr.evaluate(doc, XPathConstants.STRING));
				result = (String) expr.evaluate(doc, XPathConstants.STRING);
			} catch (XPathExpressionException e1) {
				Common.print("Failed to evaluate xpath ");
								e1.printStackTrace();
			}
			//			e.printStackTrace();
		}
		//		print(result);
		return result;
	}

	public static String xpathEvaluateCount (XPathExpression expr, Document doc)
	{
		String str = null;
		try {
			str = expr.evaluate(doc);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			Common.print("Failed to xpath for count");
		}
		return str;
	}

	public static Boolean xpathEvaluateTrue (XPathExpression expr, Document doc)
	{
		Boolean result = null;
		try {
			result = expr.evaluate(doc,XPathConstants.STRING).equals("true");
		} catch (XPathExpressionException e) {
			Common.print("Failed to evaluate XPATH for true");
			e.printStackTrace();
		}
		return result;
	}

	//the Instrumennt should consists of 12 symbols, however fake instruments starts with ZZ	
	public static String checkOldInstrument(String RealSecNumber)
	{
		//		print(RealSecNumber);
		if (RealSecNumber != null)
		{
			if (RealSecNumber.startsWith("ZZ"))
				return RealSecNumber.substring("ZZ".length());
			else
				return RealSecNumber;
		}
		else
			return "";
	}

	public static Boolean compareRICs (String RIC_xml, String RIC_csv)
	{
//		Common.print("RIC_csv: " + RIC_csv +" ::: " +RIC_xml + "BL: " + BL);
		if (BL.contains("ESS"))
			return RIC_xml.startsWith(RIC_csv+"/");
		else
			return RIC_xml.equals(RIC_csv);
	}

	public static List<String> formDynamicXpaths (List<String> lines_xpath, List<String> lines_values)
	{
		List<String> dynamic_xpaths = new ArrayList<String>();
		for (int i=0; i<lines_xpath.size(); i++)
		{
			dynamic_xpaths.add(lines_xpath.get(i)+","+lines_values.get(i));
		}
		return dynamic_xpaths;

	}

	public static String formDynamicXpaths (String xpath,String value)
	{
		return xpath+","+value;
	}

	public static String takeBookIdInCSV (HashMap<String, String> map)
	{
		//take book_id from csv
		return map.get("Portfolio No");
	}

	public static String takevalueInCSV (HashMap<String, String> map, String value)
	{
//		Common.print("takevalueInCSV value: " + value + "; map: " + map); 
		if (value.equals("ISIN_ID"))
			return map.get("Real Security No");
		else
			if(value.equals("book_id"))
				return map.get("Portfolio No");
			else
				if(value.equals("Isin") && (BL.equals("EUS")))
					return map.get("ISIN");
			else
				return map.get(value);
	}

	public static String prepareXpathForStatic (String xpath, String whatToReplace, String value)
	{
		return xpath.replace(whatToReplace, value);
	}

	public static String getStaticValueByXpath(String xpath)
	{
		List<String> xpathFormed = new ArrayList<String>(); 
		xpathFormed.add(xpath);
		//		print("xpathFormed getStaticValueByXpath");
//		Common.print("xpath for getting static value: " + xpathFormed);
		List<String> value = null;
		value = getStaticXMLvalue(globalVariables.get("static_file"),xpathFormed);
		if (value.size()==0)
			value.add("false");
		return value.get(0);
	}

	public static List<String> formListOfStaticValues (List<String> xpaths, String whatToReplace, String valueForReplace, String StaticFilePath) 
	{
		String formedXpath = null;
		List<String> xpathFormed = new ArrayList<String>(); 
		List<String> xpathFormed_final = new ArrayList<String>(); 
		List<String> thearray = new ArrayList<String>(); 
		for (int i = 0; i < xpaths.size(); i++)
		{
			if (xpaths.get(i).contains(";"))
				thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(";")));
			else
				thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(",")));

//		print(thearray.get(1)+"; "+whatToReplace+"; "+valueForReplace);
			
			formedXpath = replaceGlobalTags(prepareXpathForStatic(thearray.get(1),whatToReplace,valueForReplace));
			xpathFormed.add(formedXpath);
//			print("formedXpath in formListOfStaticValues: " + formedXpath);
		}
		xpathFormed = getStaticXMLvalue(StaticFilePath,xpathFormed);
		printList(xpathFormed);

		for (int i = 0; i < xpaths.size(); i++)
		{
			try
			{
				print("i: " + i + "; xpaths(i): " + xpathFormed.get(i));
				if (xpaths.get(i).contains(";"))
				{
					thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(";")));
					xpathFormed_final.add(thearray.get(0)+";"+xpathFormed.get(i));
				}
				else
				{
					thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(",")));
					xpathFormed_final.add(thearray.get(0)+","+xpathFormed.get(i));
				}
			}
			catch (IndexOutOfBoundsException e)
			{
				//			xpathFormed_final.add("boolean(//valuation)");
				xpathFormed_final.add("boolean(not("+thearray.get(0)+"))");
			}
		}
			printList(xpathFormed_final);
		return xpathFormed_final;
	}

	public static String evaluateMathExpression (String expression)
	{
		//	    print(expression);
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		Double result = null;
		try {
			result = (Double) engine.eval(expression);
			//			print(result);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		if (result==-0.0)
			result=Math.abs(result);
		return String.format("%.2f", result);
	}

	public static String formFidessaSource (File filepath)
	{
		return filepath.getName();
	}

	public static String formPath (File filepath)
	{
		String absolutePath = filepath.getAbsolutePath();
		return absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
	}

	public static String formListValuesFromSQL (String xpath_to_sql)
	{
		print("xpath_to_sql to process during xpath to sql verification: " + xpath_to_sql);
		return UtilsSQL.getSQLvalue(xpath_to_sql);//sqlProcessing.getSQLvalue(props, xpath_to_sql);
	}

	public static File[] filesList (File dir)
	{
		File[] foundFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("");//name.matches("data.*auto");//.endsWith("_auto");
			}
		});
		return foundFiles;
	}

	public static String formListValuesWithCcy (String xpath_to_cur, List<String> curr_conv)
	{
		//FidSource,
		String Proxy_Factor = null;
		for (int i=0; i < curr_conv.size(); i++ )
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(curr_conv.get(i).split(",")));
			if (xpath_to_cur.equals(thearray.get(0)))
			{
				Proxy_Factor = thearray.get(1)+","+thearray.get(2);
			}
			else
			{
				return null;
			}
		}
		return Proxy_Factor;
	}

	public static String decode(String expression, String search, String result, String instead)
	{
		if (expression.equals(search))
			return result;
		else
			return instead;
	}

	public static String takeSubstringsBetween (String exp, String leftSeparator, String rightSeparator)
	{
		return StringUtils.substringBetween(exp, leftSeparator, rightSeparator);
	}

	public static String[] xpathParser(String toParse)
	{
		String[] afterParse = new String[3];
		//valuation/product/front_office_product_subtype/text()=static://INDEX1_ISIN[@id="Isin"]/INSTRUMENT/RDS_PRODUCT_TYPE/text()
		/* 1. general form of string: xpath=marker:xpath
		 * 
		 */
		afterParse[0] = toParse.substring(0, toParse.indexOf("="));
		afterParse[1] = StringUtils.substringBetween(toParse, "=", ":");
		afterParse[2] = toParse.substring(toParse.indexOf(":")+1);
		return afterParse;
	}

	public static String replaceGlobalTags (String text)
	{
		for (Entry<String, String> entry  : globalVariables.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace("{"+entry.getKey()+"}", entry.getValue());
				//				print("after replacement: " + text);
			}
		}
		return text;
	}

	public static Integer getNumberOfFeatures(String filePath)
	{
		XPathExpression expr = null;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,"count(//feature)");
		return Integer.parseInt(xpathEvaluateCount(expr,doc));
	}

	public static List<String> updateFeatureXpaths (List<String> xpaths_to_form, String number)
	{
		List<String> xpaths_formed = new ArrayList<String>();
		for (String temp : xpaths_to_form)
		{
			temp = temp.replace("feature_number", number);
			xpaths_formed.add(temp);
		}
//		Common.print("feature xpaths: " + xpaths_formed);
		return xpaths_formed;
	}

	public static String checkIsOwnBond (String isin)
	{
		/*
		 * 1. take isin
		 * 2. find issuer_id in IRDS - StaticXMLInstrument
		 * 3. find legal_entity_id in Map_Issuer.xml
		 * 4. check if-else condition
		 */
		//		print("isin : *" + isin+"*");

//		String RD_INSTRUMENT_Xpath = ("//INDEX1_ISIN[@id=\'ISIN_ID\']/INSTRUMENT/ISSUER_ID/text()");
		String RD_INSTRUMENT_Xpath = ("//INSTRUMENT[ISIN[text()=\"ISIN_ID\"]]/ISSUER_ID/text()");
		//INSTRUMENT[ISIN[text()="Isin"]]/RDS_PRODUCT_TYPE/text()
		//INDEX1_ISIN[@id='GZZZZ0008706128']/INSTRUMENT/ISSUER_ID/text()

		String MAP_ISSUER_Xpath =("//MAP_ISSUER/VALUE[@ISSUER_ID='IS_ID_VALUE']/@LEGAL_ENTITY_ID");
		List<String> xpathFormed = new ArrayList<String>(); 

		String RD_INSTRUMENT = globalVariables.get("static_folder")+"\\RD_INSTRUMENT.xml";
		String MAP_ISSUER = globalVariables.get("static_folder")+"\\MAP_ISSUER.xml";
		String RD_BOOK = globalVariables.get("static_folder")+"\\RD_BOOK.xml";
		String is_own_bond = null;
		if (!isin.equals("")&&!isin.equals(" "))
		{
			xpathFormed.add(0, RD_INSTRUMENT_Xpath.replace("ISIN_ID", isin));
			//						print("xpathFormed: " + xpathFormed.get(0));
			xpathFormed = getStaticXMLvalue(RD_INSTRUMENT,xpathFormed);
			//						print("xpathFormed: " + xpathFormed.get(0));
			if (xpathFormed.size()==0)
			{
				is_own_bond = "false";
				//						print("xpathFormed.size()==0 ");
			}
			else
			{
				String issuer = xpathFormed.get(0);
				//							print("xml issuer : " + issuer);
				xpathFormed.clear();
				if (!issuer.equals(""))
				{
					xpathFormed.add(0, MAP_ISSUER_Xpath.replace("IS_ID_VALUE", issuer));
					//								print("xpathFormed after issuer: " + xpathFormed.get(0));
					xpathFormed = getStaticXMLvalue(MAP_ISSUER,xpathFormed);
					String legal_entity_id = null;
					try{
						legal_entity_id = xpathFormed.get(0);
						//							print("xml legal_entity_id : " + legal_entity_id);
					}
					catch (IndexOutOfBoundsException e)
					{
						legal_entity_id = "0";
						//								print("xml legal_entity_id : " + legal_entity_id);
					}

					if (legal_entity_id.equals("0840"))
						is_own_bond = "true";
					else
						is_own_bond = "false";
				}
				else
					is_own_bond = "false";
			}
		}
		else
			is_own_bond = "false";
		return is_own_bond;
	}

	public static void printHashmap (HashMap<String, String> row_map)
	{
		for (Entry<String, String> entry  : row_map.entrySet()) 
			print("Key : " + entry.getKey() + " Value : " + entry.getValue());

	}
	public	static void printList (List<String> xpaths_dynamic)
	{
		for (String xp: xpaths_dynamic)
			print(xp);
	}
	public	static void printList (String[] xpaths_dynamic)
	{
		for (String xp: xpaths_dynamic)
			print(xp);
	}
	public	static void print (String value)
	{
		Common.print(value);
		output.add(eol + value);
	}

	public static List<String> ReturnResult ()
	{
		return output;
	}

	//***************************************************************************************************************************************
	public static String xmlXpathValidation(String n, List<String> lines)
	{
		String fullresult = "";
		Document doc = formXMLdocument(n);
		XPath xpath = createXpath();
		XPathExpression expr = null;
		List<String> thearray = new ArrayList<String>(); 

		for (int i=0; i<lines.size(); i++)
		{
//			Common.print("lines.get(i): " + lines.get(i));
			if (lines.get(i).contains(";") || lines.get(i).contains("boolean(not"))
				thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(";")));
			else
//				if (lines.get(i).contains(",") && !lines.get(i).contains("boolean(not"))
				thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
//			Common.print("thearray: " + thearray.get(0));

			expr = xpathCompile(xpath,thearray.get(0));
			Boolean result = null;
			String nodeValue = null;
			Integer marker = 0;
			if (lines.get(i).contains(",") && !lines.get(i).contains("substring")) marker=1;
			if (thearray.size()==2||marker==1)
			{
//				Common.print("thearray.get(0): " + thearray.get(0) + "; thearray.get(1): " + thearray.get(1));
				nodeValue = xpathEvaluate(expr,doc);
				try
				{
//					print("nodeValue: "+ nodeValue);
					result = nodeValue.equals(thearray.get(1));
				}
				catch (IndexOutOfBoundsException e)
				{
					result = nodeValue.equals("");
				}
//				print("Result of processing 1 type: "+ result +"; NodeValue: "+ nodeValue);
				if (!result)
				{
					try
					{
						print("Result of processing 1 type fails: "+ result + "; Supposed: " + thearray.get(0) + ": " + thearray.get(1) + "<>" +nodeValue);
						fullresult = fullresult + "Result of processing 1 type fails: "+ result + "; Supposed: " + thearray.get(0) + ": " + thearray.get(1) + "<>" +nodeValue +";";
					}
					catch (IndexOutOfBoundsException e)
					{
						print("Result of processing 1 type fails "+ result + " due to abcence of value from source files; Supposed: " + thearray.get(0) + ": TADS result " +nodeValue + " but expected nothing");
						fullresult = fullresult + "Result of processing 1 type fails "+ result + " due to abcence of value from source files; Supposed: " + thearray.get(0) + ": TADS result " +nodeValue + " but expected nothing;";
					}

				}
			}
			else
				if (thearray.size()==1)
				{
					result = xpathEvaluateTrue(expr,doc);
					//					print("Result of processing 2 type: "+result);
					if (!result)
					{
						print("Result of processing 2 type fails: "+ result + "; Supposed: " + thearray.get(0));
						fullresult = fullresult + "Result of processing 2 type fails: "+ result + "; Supposed: " + thearray.get(0) + ";";
					}
				}
		}
		return fullresult;
	}

	public static void checkXMLWithPlainXpath(String filePath,List<String> xpaths)
	{
		xmlXpathValidation (filePath, xpaths);
	}	

	public static String getCorrespondedProduct(String DBSecType)
	{
		String AcSProductType = null;
		List<String> conversions = null;
		//		print("DBSecType: "+DBSecType);
		try {
			conversions = readLines(globalVariables.get("path")+"\\ProductConversion");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//		printList(conversions);
		for (int i = 0; i < conversions.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(conversions.get(i).split(",")));
//			print("DBSecType: "+DBSecType + ":" + thearray.get(0) + " ::: " + thearray.get(0).equals(DBSecType) +" ::: " + thearray.get(1));
			if (thearray.get(0).equals(DBSecType))
			{
				AcSProductType = thearray.get(1);
				print("DBSecType: "+DBSecType+"; "+thearray.get(0)+"; AcSProductType: "+AcSProductType);
				break;
			}
		}
		conversions.clear();
		return AcSProductType;
	}

	public static String replaceCSVTags (String text, HashMap<String, String> csv_source)
	{
		for (Entry<String, String> entry  : csv_source.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace(entry.getKey(), entry.getValue());
				//				print("after replacement: " + text);
			}
		}
		return text;
	}

	public static String findNonStandardCurrency(String csvCurr, List<String> curr_conv)
	{
		String ccy = null;
		for (String currencyConversionLine : curr_conv)
		{
			if (currencyConversionLine.contains(csvCurr))
			{
				ccy = currencyConversionLine;
				break;
			}
		}
		return ccy;
	}

	abstract ArrayList<String> formCSVSource();

	abstract void checkXMLWithSourceXpath(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist);
	abstract void checkXMLWithStaticXpath(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist);
	abstract void checkXMLWithLogicXpath(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist);
	abstract void checkXMLWithSQL(String filePath,List<String> xpaths_with_static);
	abstract void checkXMLWithCurrency(String filePath, ArrayList<?> source, List<String> xpaths_ccy,  String CurrencyConvertion);

	public void checkXMLFeature (String n, String arguments, ArrayList<?> arraylist)
	{
		Integer numOfFeatures = getNumberOfFeatures(n);
		print(numOfFeatures+" Feature(s) to process");
/*
 		print_xpath_plain = 0;
		print_xpath_source = 0;
		print_xpath_logic = 0;
		print_xpath_static = 0;
		print_xpath_sql = 0;
		print_xpath_ccy = 0;
*/
		for (int i = 0; i < numOfFeatures; i++)
		{
			if (arguments.contains("PlainXpath"))
			{
				List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\PlainXpath");
				if (print_xpath_plain==0)
				{
					print("Plain xpaths to check for each TADS feature:");
					printList(xpaths_to_form);
				}
				checkXMLWithPlainXpath(n,updateFeatureXpaths(xpaths_to_form,String.valueOf(i+1)));
				print("PlainXpath finished");
				print_xpath_plain++;
			}
			if (arguments.contains("SourceXpath"))
			{
				List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\SourceXpath");
				if (print_xpath_source==0)
				{
					print("Source xpaths to check for each TADS feature:");
					printList(xpaths_to_form);
				}
				print_xpath_source++;
				checkXMLWithSourceXpath(n,updateFeatureXpaths(xpaths_to_form,String.valueOf(i+1)),arraylist);
				print("SourceXpath finished");
				print_xpath_source++;
			}
			if (arguments.contains("StaticXpath"))
			{
				List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\StaticXpath");
				if (print_xpath_static==0)
				{
					print("Static xpaths to check for each TADS feature:");
					printList(xpaths_to_form);
				}

				checkXMLWithStaticXpath(n,updateFeatureXpaths(xpaths_to_form,String.valueOf(i+1)),arraylist);
				print("StaticXpath finished");
				print_xpath_static++;
			}
			if (arguments.contains("LogicXpath"))
			{
				List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\LogicXpath");
				if (print_xpath_logic==0)
				{
					print("Logic xpaths to check for each TADS feature:");
					printList(xpaths_to_form);
				}
				checkXMLWithLogicXpath(n,updateFeatureXpaths(xpaths_to_form,String.valueOf(i+1)),arraylist);
				print("LogicXpath finished");
				print_xpath_logic++;
			}
			if (arguments.contains("SQLXpath"))
			{
				List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\SQLXpath");
				if (print_xpath_sql==0)
				{
					print("SQL xpaths to check for each TADS feature:");
					printList(xpaths_to_form);
				}

				checkXMLWithSQL(n,updateFeatureXpaths(xpaths_to_form,String.valueOf(i+1)));
				print("SQLXpath finished");
				print_xpath_sql++;
			}

			if (arguments.contains("CurrencyXpath"))
			{
				List<String> xpaths_ccy = xpaths (globalVariables.get("path")+"\\CurrencyXpath");

				if (print_xpath_ccy==0)
				{
					print("Currency xpaths to check for each TADS feature:");
					printList(xpaths_ccy);
				}
				checkXMLWithCurrency(n,arraylist,updateFeatureXpaths(xpaths_ccy,String.valueOf(i+1)),globalVariables.get("global_folder")+"\\CurrencyConvertion");
				print("CurrencyXpath finished");
				print_xpath_ccy++;
			}
		}
	}

	public static void init (String[] args, Properties master_props)
	{
		props = master_props;
		Source = formFidessaSource(new File (args[0]));
		print("Source file in init of TADSValidateCommonActions: " + Source);
		path = formPath(new File (args[0]));
		print("Source file path in init of TADSValidateCommonActions: " + path);
		BL = props.getProperty("BL");
		globalVariables.put("source_file",Source);
		globalVariables.put("path",path);
	}
	
	public String checkIsFungible(HashMap<String, String> map)
	{
		List<String> conversions = null;
		try {
			conversions = readLines(globalVariables.get("global_folder")+"\\isFungible");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String isFungibleCombination = conversions.get(0).split(",")[0];
		String isFungibleFlag = null;
		String isFungibleFormedKey = "";
		ArrayList<String> isFungibleFields = new ArrayList<String>(Arrays.asList(isFungibleCombination.split("-")));
		for (String field : isFungibleFields)
		{
			isFungibleFormedKey  += map.get(field);
		}
		Common.print("isFungibleFormedKey: " + isFungibleFormedKey);
		for (String line : conversions)
		{
			String temp = line.split(",")[0];
//			Common.print("temp: " + temp);
			if (isFungibleFormedKey.matches(temp))
			{
				isFungibleFlag = line.split(",")[1];
				Common.print("bingo isFungibleFlag: " + isFungibleFlag);
				break;
			}
		}
		return isFungibleFlag;
	}
	
	public List<String> validate(String[] args, Properties master_props) {
		print_xpath_plain = 0;
		print_xpath_source = 0;
		print_xpath_logic = 0;
		print_xpath_static = 0;
		print_xpath_sql = 0;
		print_xpath_ccy = 0;

		props = master_props;

		//		printList(args);
		String staticSource = args[2];
		String arguments = "";

		Source = formFidessaSource(new File (args[0]));
		print("Source file: " + Source);
		path = formPath(new File (args[0]));
		print("Source file path: " + path);
		print("TADS path: " + args[1]);

		File[] foundFiles = filesList(new File(args[1]));

		for (int j = 3; j < args.length; j++)
		{
			arguments=arguments+args[j]+";";
		}
		BL = props.getProperty("BL");
		globalVariables.put("source_file",props.getProperty("PROCFILE")); //contains name of the source file formed to processing in DB - with datetime for uniqueness
		globalVariables.put("source_file_initial_name",Source);
		String parentFolder = new File (args[0]).getParent();
		globalVariables.put("source_folder",parentFolder.substring(parentFolder.lastIndexOf("\\")+1,parentFolder.length()));
		globalVariables.put("CBIOMNISMASK",props.getProperty("CBIOMNISMASK"));
		globalVariables.put("CBICBEUMASK",props.getProperty("CBICBEUMASK"));
		globalVariables.put("path",path);
		//path before global refactoring
//		globalVariables.put("static_folder",globalVariables.get("path").substring(0,globalVariables.get("path").substring(0,globalVariables.get("path").lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static");
		globalVariables.put("static_folder",globalVariables.get("path").substring(0,path.substring(0,path.lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static");
		globalVariables.put("static_file",globalVariables.get("static_folder")+"\\"+staticSource);
		globalVariables.put("global_folder",globalVariables.get("path").substring(0,globalVariables.get("path").lastIndexOf(File.separator)));
		print ("Source folder aka Test name: "+globalVariables.get("source_folder"));
		print("Static files path: " + globalVariables.get("static_folder"));
		print("Static file path: "+globalVariables.get("static_file"));
		print("Verifications to perform: " + arguments);
		ArrayList<?> arraylist = formCSVSource();
		//		print("arraylist: " + arraylist.size());

		try
		{
		if (foundFiles.length != 0)
		{
			for (int i=0;i<foundFiles.length; i++)
			{
				String[] xmls = readAndFormXML (foundFiles[i].toString());
				for(String n: xmls)
				{
					if (arguments.contains("Feature"))
					{
						checkXMLFeature(n, arguments, arraylist);
						print("Feature finished");
						print("TADS finished - " + foundFiles[i].toString());
						continue;
					}
					
					if (arguments.contains("PlainXpath"))
					{
						List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\PlainXpath");
						if (print_xpath_plain==0)
						{
							print("Plain xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithPlainXpath(n,xpaths_to_form);
						print("PlainXpath finished");
						print_xpath_plain++;
					}
					if (arguments.contains("SourceXpath"))
					{
						List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\SourceXpath");
						if (print_xpath_source==0)
						{
							print("Source xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithSourceXpath(n,xpaths_to_form,arraylist);
						print("SourceXpath finished");
						print_xpath_source++;
					}
					if (arguments.contains("StaticXpath"))
					{
						List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\StaticXpath");
						if (print_xpath_static==0)
						{
							print("Static xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithStaticXpath(n,xpaths_to_form,arraylist);
						print("StaticXpath finished");
						print_xpath_static++;
					}
					if (arguments.contains("LogicXpath"))
					{
						List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\LogicXpath");
						if (print_xpath_logic==0)
						{
							print("Logic xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithLogicXpath(n,xpaths_to_form,arraylist);
						print("LogicXpath finished");
						print_xpath_logic++;
					}
					if (arguments.contains("SQLXpath"))
					{
						List<String> xpaths_to_form = xpaths (globalVariables.get("path")+"\\SQLXpath");
						if (print_xpath_sql==0)
						{
							print("SQL xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithSQL(n,xpaths_to_form);
						print("SQLXpath finished");
						print_xpath_sql++;
					}

					if (arguments.contains("CurrencyXpath"))
					{
						List<String> xpaths_ccy = xpaths (globalVariables.get("path")+"\\CurrencyXpath");
						if (print_xpath_ccy==0)
						{
							print("Currency xpaths to check for each TADS:");
							printList(xpaths_ccy);
						}
						checkXMLWithCurrency(n,arraylist,updateFeatureXpaths(xpaths_ccy,String.valueOf(i+1)),globalVariables.get("global_folder")+"\\CurrencyConvertion");
						print("CurrencyXpath finished");
						print_xpath_ccy++;
					}

					print("TADS finished - " + foundFiles[i].toString());
				}
			}
		}
		else
			print("TADS verification fails - no TADS to verify");
		}
		catch (NullPointerException e)
		{
			print("TADS verification fails - no TADS to verify : NullPointerException");
		}
		//		xpathParser("//valuation/product/front_office_product_subtype/text()=static://INDEX1_ISIN[@id=\"Isin\"]/INSTRUMENT/RDS_PRODUCT_TYPE/text()");
		return output;

	}

}
