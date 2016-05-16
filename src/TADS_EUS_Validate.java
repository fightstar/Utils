

import au.com.bytecode.opencsv.CSVReader;
import net.sf.saxon.s9api.*;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TADS_EUS_Validate extends TADSValidateCommonActions implements TADSValidator{

	public static String formRIC (HashMap<String, String> map)
	{
//		for source_system = 'EUS-PMACLN' as  "EUS" + "/" + source (EUS-PMACLN) "/"+ FO Book +"/"+ISIN+"/"+FO Currency+"/"+ RIC+"/"+ BO Code
//		for source_system = 'EUS-IMGCBLN' OR Source = 'EUS-IMGOMLN' as	"EUS" + "/" + source(EUS-IMGCBLN or EUS-IMGOMLN) + "/"+ Position Description
		String RIC = null;
//		print("map.get(Source): "+map.get("Source"));
		if (map.get("Source").equals("EUS-PMACLN"))
			RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("FO Book") + "/"+ map.get("ISIN") + "/"+ map.get("FO Currency") + "/"+ map.get("RIC")  + "/"+  map.get("BO Code");
		else if (map.get("Source").equals("EUS-IMGOMLN") || map.get("Source").equals("EUS-IMGCBLN"))
			RIC = "EUS" + "/"+ map.get("Source") + "/"+ map.get("Position Description");
		return RIC;
	}
	
	public static String formEUSProductXpath (String prodVariants, HashMap<String, String> map)
	{
		String[] parts = prodVariants.split("/");
			String product = null;
//			print(String.valueOf(map.get(parts[0]).isEmpty()));
			
		if (staticSourceSystem.equals("PMACLN"))
			product = getCorrespondedProduct(map.get(parts[1]));
		else
			product = getCorrespondedProduct(map.get(parts[0]));
		print ("product: "+product + " ::: " + map.get(parts[0]) + " ; " + map.get(parts[1]));
		return product;
	}
	
	public static List<String> formDynamicXpaths (List<String> lines, HashMap<String, String> map)
	{
		List<String> dynamic_xpaths = new ArrayList();
//		print("printlist");
//		printList(lines);
//		print("printlist size: "+lines.size());
//		print("printhashmap");
//		printHashmap(map);
		
		for (int i=0; i<lines.size(); i++)
    	{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
//    		print("Isin - formDynamicXpaths: "+ thearray.get(1));
	    	if(thearray.get(1).contains("-"))
	    	{
	    		ArrayList<String> coversion = new ArrayList<String>(Arrays.asList(thearray.get(1).split("-")));
//	    		print("coversion.get(0): "+coversion.get(0));
	    		switch (coversion.get(0))
	    		{
	    		case "ProductConversion":  dynamic_xpaths.add(thearray.get(0)+","+formEUSProductXpath (coversion.get(1), map));
	    		break;
	    		case "IsOwnBond":  dynamic_xpaths.add(thearray.get(0)+","+checkIsOwnBond(checkOldInstrument(map.get(coversion.get(1)))));
	    		break;
	    		}
	    	}	
	    	else if ((thearray.get(1).contains("ISIN"))) 
	    	{
	    		dynamic_xpaths.add(thearray.get(0)+","+checkOldInstrument(map.get(thearray.get(1))));
//	    		print("Isin - formDynamicXpaths: "+ dynamic_xpaths.get(i));
	        }
	    	else if ((thearray.get(1).contains("FO Book"))) 
	    		dynamic_xpaths.add(thearray.get(0)+","+formStaticFO_Book(map.get("FO Book")));
	    	else if ((thearray.get(1).contains("/"))) 
	    	{
	    		String[] parts = thearray.get(1).split("/");
	    		dynamic_xpaths.add(thearray.get(0)+","+ (map.get("Source").equals("EUS-PMACLN") ? map.get(parts[1]) : map.get(parts[0])));
//	    		print("dynamic_xpaths.get(i): "+ dynamic_xpaths.get(i));
	        }
	    	else {
		    	dynamic_xpaths.add(thearray.get(0)+","+map.get(thearray.get(1)));
//	    		print("simple - formDynamicXpaths: "+ dynamic_xpaths.get(i));
	        }
//    		print("iteration "+i+" - formDynamicXpaths: "+ dynamic_xpaths.get(i));
    	}
		return dynamic_xpaths;
	}
	
	public static String evaluateCalcValues(String filePath, String expression, ArrayList arraylist)
	{
		String xpath_line_ready = null;
		String xpath_to_calc = "";
//		print(expression);
		ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(expression.split(",")));
//		printList(thearray);

//		print(key);
		XPathExpression expr = null;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		for (Object object_map : arraylist) 
		{
			String csvRIC = null;
			csvRIC = formRIC((HashMap<String, String>) object_map);
//			print("xml RIC : " + xmlRIC.equals(csvRIC));
				if (xmlRIC.equals(csvRIC))
				{
					for (int j=0; j < thearray.size(); j++)
						{
						xpath_to_calc += replaceCSVTags(thearray.get(j),(HashMap<String, String>) object_map); 
						}
//					print("xpath_to_calc: " + xpath_to_calc);
					xpath_line_ready=evaluateMathExpression(xpath_to_calc).toString();
//					print("xpath_line_ready: " + xpath_line_ready);
				}
		}
		return xpath_line_ready;
	}
	
	public static String takeStaticXMLValueByXpath(String filePath, String xpaths_with_static, ArrayList arraylist)
	{
		String staticValue = null;
		String xpaths_static = null;
		String key = null;
//		print(globalVariables.get("source_file"));
		
		if (globalVariables.get("source_folder").contains("Product"))
			key = "Isin";
		else 
				if (globalVariables.get("source_folder").contains("Counterparty"))
					 key = "Counterparty code";
/*
		switch (globalVariables.get("source_file")) {
        case "Product":  key = "ISIN";
                 break;
        case "Counterparty":  key = "Counterparty code";
        break;
		}
		print("key: "+key);
*/
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
//		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) 
		{
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
//			print("csv RIC : " + csvRIC);
				if (compareRICs(xmlRIC, csvRIC))
				{
					String BookID = null;
					BookID = takevalueInCSV(row_map,key);
/*
					print("Key : " + BookID);
					print("xpaths_with_static: "+xpaths_with_static.replace("Isin", BookID));
						switch (globalVariables.get("source_file")) {
				        case "Product":  xpaths_static = getStaticValueByXpath(xpaths_with_static.replace("Isin", BookID));
				                 break;
				        case "Counterparty":  xpaths_static = getStaticValueByXpath(xpaths_with_static.replace("Counterparty code", BookID));
				        break;
						}
*/
						if (globalVariables.get("source_folder").contains("Product"))
							xpaths_static = getStaticValueByXpath(xpaths_with_static.replace("Isin", BookID));
						else 
								if (globalVariables.get("source_folder").contains("Counterparty"))
									xpaths_static = getStaticValueByXpath(xpaths_with_static.replace("Counterparty code", BookID));
//					print("xpaths_static: "+xpaths_static);
				}
	    }
		return xpaths_static;
	}

	public static String findCorrespondingCSVRecord (String filePath, String key, ArrayList arraylist)
	{
		String BookID = null;
//		print(key);
		XPathExpression expr = null;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		for (Object object_map : arraylist) 
		{
			String csvRIC = null;
			csvRIC = formRIC((HashMap<String, String>) object_map);
//			print("xml RIC : " + xmlRIC.equals(csvRIC));
				if (xmlRIC.equals(csvRIC))
				{
					BookID = takevalueInCSV((HashMap<String, String>) object_map,key);
//					print("Key : " + BookID);
					break;
				}
	    }
		return BookID;
	}
	
	public static String defineStaticSource (String sourceValue)
	{
//	print("sourceValue: "+sourceValue);
		String definedStaticSource = null;
		if (sourceValue.equals("PMACLN"))
			definedStaticSource = "FIDESSA_LDN";
		if (sourceValue.equals("IMGCBLN"))
			definedStaticSource = "CB_IMAGINE_LDN";
		if (sourceValue.equals("IMGOMLN"))
			definedStaticSource = "CB_IMAGINE_LDN";
		return definedStaticSource;
	}
	
	public static String formStaticFO_Book (String FO_Book)
	{
//		print("FO_Book: "+FO_Book);
//		print("staticSource formStaticFO_Book: "+staticSource);
		return FO_Book.substring(0,FO_Book.length()-(staticSourceSystem+"840").length());
	}
//************************************************************************************************************	
	public void checkXMLWithStaticXpath	(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist)
		{
//			List<String> xpaths_with_static = xpaths (fileXPath_with_static);
//			print("xpaths_with_static: "+xpaths_with_static.get(0));
			List<String> xpaths_static = null;
			String key = null;
			if (globalVariables.get("source_folder").contains("Product"))
				key = "Isin";
			else 
				if (globalVariables.get("source_folder").contains("Book"))
					key = "FO Book";
				else 
					if (globalVariables.get("source_folder").contains("Counterparty"))
						 key = "Customer";
			XPathExpression expr = null;
			Boolean RICmarker = false;
			Document doc = formXMLdocument(filePath);
			XPath xpath = createXpath();
	    	expr = xpathCompile(xpath,IDxpath);
			String xmlRIC = null;
			xmlRIC = xpathEvaluate(expr,doc);
			HashMap<String, String> row_map = new HashMap<String, String>(); 
			for (Object object_map : arraylist) 
			{
		    	row_map = (HashMap<String, String>) object_map;		    	
				String csvRIC = null;
				csvRIC = formRIC(row_map);
//				print("csv RIC : " + csvRIC + "; xml RIC : " + xmlRIC);
					if (compareRICs(xmlRIC, csvRIC))
					{
						print("accountable_id: " + xmlRIC);
						String BookID = null;
						BookID = takevalueInCSV(row_map,key);
//						print("BookID: "+BookID);
//						print(row_map.get("Source"));
						staticSource = defineStaticSource (row_map.get("Source").substring("EUS-".length()));
						globalVariables.put("staticSource", staticSource);
//						print("staticSource: "+staticSource);
						staticSourceSystem = row_map.get("Source").substring("EUS-".length());
						BookID = formStaticFO_Book(BookID);
//						print("BookID: "+BookID);
						xpaths_static = formListOfStaticValues (xpaths_with_static, key, BookID, globalVariables.get("static_file"));
//							printList(xpaths_static);
							xmlXpathValidation (filePath, xpaths_static);
						RICmarker = true;
					}
		    }
	if (!RICmarker)
		print("fail: no csv record suits TADS");
//		print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
		}
//************************************************************************************************************	
	public void checkXMLWithLogicXpath (String xml, List<String> xpaths, ArrayList<?> csv_array)
	{
		String xmlRIC = null;
//		List<String> xpaths = xpaths (LogicXPath);
//		printList(xpaths);
		String static_xpath = null;
		for (int i=0; i<xpaths.size(); i++)
		{
        	static_xpath = xpaths.get(i);

//        	print("current xpath: "+xpaths.get(i));
			if (xpaths.get(i).contains("<static>"))
        {
        	//find and substitute static value
        	static_xpath = takeSubstringsBetween(xpaths.get(i),"<static>","</static>");
//        	print("static_xpath: "+static_xpath);
        	String staticValue = takeStaticXMLValueByXpath(xml, static_xpath,csv_array);
//        	print("static xpath value: "+staticValue);
        	try
        	{
        	static_xpath = xpaths.get(i).replaceAll("<static>.*?</static>", staticValue);
        	}
        	catch (NullPointerException e)
        	{
        		print("There is no found Isin value for record: "+static_xpath);
            	static_xpath = xpaths.get(i).replaceAll("<static>.*?</static>", "false");
        	}
//        	print("static xpath value: "+static_xpath);
        }
        else if (xpaths.get(i).contains("<source>"))
        {
        	//find and substitute static value
        	static_xpath = takeSubstringsBetween(xpaths.get(i),"<source>","</source>");
//        	print("static xpath value: "+static_xpath);
        	String sourceValue = findCorrespondingCSVRecord(xml, static_xpath,csv_array);
//        	print("sourceValue : "+sourceValue);
        	if (sourceValue.contains("."))
        	{
            	sourceValue = sourceValue.replaceAll("[0]*$", "").replaceAll("\\.$", "");
        	}
//        	print("sourceValue : "+sourceValue);
//        	if (sourceValue.contains(".000000")){sourceValue = sourceValue.substring(0,sourceValue.indexOf(".000000"));	}
        	if (sourceValue.isEmpty())
        	{
        		sourceValue="0";
        	}
        	static_xpath = xpaths.get(i).replaceAll("<source>.*?</source>", sourceValue);
        }
        else if (xpaths.get(i).contains("<math>"))
        {
        	//find and substitute static value
        	static_xpath = takeSubstringsBetween(xpaths.get(i),"<math>","</math>");
        	String sourceValue = evaluateCalcValues(xml,static_xpath,csv_array);
//        	print("sourceValue : "+sourceValue);
        	sourceValue = sourceValue.replaceAll("[0]*$", "").replaceAll("\\.$", "");
//        	print("sourceValue : "+sourceValue);
        	static_xpath = xpaths.get(i).replaceAll("<math>.*?</math>", sourceValue);
        }
        else if (xpaths.get(i).contains("IsOwnBond-Isin"))
        {
    		String sourceValue = null;
        	XPathExpression expr = null;
    		Document doc = formXMLdocument(xml);
    		XPath xpath = createXpath();
        	expr = xpathCompile(xpath,IDxpath);
    		xmlRIC = xpathEvaluate(expr,doc);
//    		print("xml RIC : " + xmlRIC);
    		HashMap<String, String> row_map = new HashMap<String, String>(); 
    		for (Object object_map : csv_array) {
    	    	row_map = (HashMap<String, String>) object_map;		    	
    			String csvRIC = null;
    			csvRIC = formRIC(row_map);
    				if (compareRICs(xmlRIC, csvRIC))
    				{
    	    			print("csv RIC : " + csvRIC);
        	sourceValue = checkIsOwnBond(checkOldInstrument(row_map.get("ISIN")));
    				}
    		}

    		if (sourceValue.isEmpty())
        	{
        		sourceValue="false";
        	}
        	static_xpath = xpaths.get(i).replaceAll("IsOwnBond-Isin", sourceValue);
        }
			if (static_xpath.contains("isFungible"))
			{
				String sourceValue = null;
				XPathExpression expr = null;
				Document doc = formXMLdocument(xml);
				XPath xpath = createXpath();
				expr = xpathCompile(xpath,IDxpath);
				xmlRIC = xpathEvaluate(expr,doc);
				//	print("xml RIC : " + xmlRIC);
				HashMap<String, String> row_map = new HashMap<String, String>(); 
				for (Object object_map : csv_array) {
					row_map = (HashMap<String, String>) object_map;		    	
					String csvRIC = null;
					csvRIC = formRIC(row_map);
					if (compareRICs(xmlRIC, csvRIC))
					{
						print("csv RIC from isFungible check: " + csvRIC);
				sourceValue = checkIsFungible(row_map);
				break;
					}
				}
					if (sourceValue.isEmpty())
				{
					sourceValue="Y";
				}
				
				static_xpath = static_xpath.replaceAll("isFungible", sourceValue);
//				print("static_xpath after isFungible: "+static_xpath);
			}

			try {
				XPathExpression expr = null;
				Boolean RICmarker = false;
				Document doc_ric = formXMLdocument(xml);
				XPath xpath_ric = createXpath();
		    	expr = xpathCompile(xpath_ric,IDxpath);
				xmlRIC = xpathEvaluate(expr,doc_ric);
				Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
            StringReader reader = new StringReader(xml);
            XdmNode doc = builder.build(new StreamSource(reader));
//            print("logic Xpath : " + static_xpath);
            XPathSelector selector = xpath.compile(static_xpath).load();
            selector.setContextItem(doc);
 
            XdmValue logicXpathValue = selector.evaluate();
            if (!logicXpathValue.toString().equals("true"))
				print("Logic fails: "+ static_xpath);
        } catch (Exception e) {
            print("error: "+ e.getLocalizedMessage());
        }
		}
		print("Current TADS: "+ xmlRIC);
    }
//************************************************************************************************************	
	public void checkXMLWithSourceXpath (String filePath, List<String> xpaths_to_form, ArrayList<?> arraylist)
	{
//		List<String> xpaths_to_form = xpaths (fileXPath_to_form);
//		print("arraylist size: "+arraylist.size());
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			staticSource = row_map.get("Source").substring("EUS-".length());
			csvRIC = formRIC(row_map);
//			print("csv RIC : " + csvRIC + "; xml RIC : " + xmlRIC);
				if (compareRICs(xmlRIC, csvRIC))
				{
					staticSourceSystem = row_map.get("Source").substring("EUS-".length());
					staticSource = defineStaticSource (staticSourceSystem);
//					print("staticSource: "+staticSource);
//					print("staticSourceSystem: "+staticSourceSystem);
					print("accountable_id: " + xmlRIC);
//					print(row_map.get("Source"));
						List<String> xpaths_dynamic = formDynamicXpaths(xpaths_to_form,row_map);
						xmlXpathValidation (filePath, xpaths_dynamic);
					RICmarker = true;
					break;
				}
	    }
if (!RICmarker)
	print("SourceXpath fail: no csv record suits TADS");
//	print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
//************************************************************************************************************	
	public void checkXMLWithSQL(String filePath,List<String> xpaths_to_sql)
	{
//		print(xpaths_to_sql.size()+" :"+ xpaths_to_sql.get(0));
		List<String> xpaths_to_sql_formed = new ArrayList<String>();
			for (int i = 0; i < xpaths_to_sql.size(); i++)
			{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths_to_sql.get(i).split(";")));
//			print(thearray.size()+" :"+ thearray.get(0)+" :"+ thearray.get(1));
			String expectedValue = formListValuesFromSQL(replaceGlobalTags(thearray.get(1)));
			xpaths_to_sql_formed.add(thearray.get(0)+","+expectedValue);
			}
//		printList(xpaths_to_sql_formed);
			String res_static = xmlXpathValidation (filePath, xpaths_to_sql_formed);
	}	
//************************************************************************************************************	
	public void checkXMLWithCurrency (String filePath, ArrayList<?> source, List<String> xpaths_ccy,  String CurrencyConvertion)
	{
//		List<String> curr_conv = new ArrayList<String>();
		String ccy = null;
		String ccy_converted = null;
		String rate = null;
//		print("CurrencyConvertion: " + CurrencyConvertion);
		List<String> curr_conv = xpaths (CurrencyConvertion);
//		print("CurrencyConvertion: " + curr_conv);
		
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
//		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : source) {
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
				if (compareRICs(xmlRIC, csvRIC))
				{
					print("csv RIC : " + csvRIC);
			    	for (int i = 0; i < xpaths_ccy.size(); i++)
			    	{
					ArrayList<String> ccy_array = new ArrayList<String>(Arrays.asList(xpaths_ccy.get(i).split(",")));
			    	print("ccy xpath to process: " + xpaths_ccy.get(i));
					ccy = takevalueInCSV(row_map,ccy_array.get(1)); 
					print("ccy from csv: "+ccy);
					String found_conversion = findNonStandardCurrency(ccy,curr_conv); 
					print("found_conversion: "+found_conversion);
					if (found_conversion!=null)
					{
						ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
						ccy_converted = curr_conv_values.get(1);
						rate = curr_conv_values.get(2);
					}
					else
					{
						ccy_converted = ccy;
						rate = "1";
					}
			    	print("Ccy converted: " + ccy_converted);
			    	print("Rate: " + rate);
			    	BigDecimal rateBD= new BigDecimal(rate);

			    	List<String> dynamic_xpaths = new ArrayList();
			    	dynamic_xpaths.add(ccy_array.get(0)+","+ccy_converted);
					String res_dynamic = xmlXpathValidation (filePath, dynamic_xpaths);
					RICmarker = true;
			    	}
				}
	    }
if (!RICmarker)
	print("fail: no csv record suits TADS");
}	
//************************************************************************************************************	
	
	public ArrayList formCSVSource ()
	{
	String OMNIS_csvFilename = globalVariables.get("path")+"\\"+globalVariables.get("source_file_initial_name");
//	print("OMNIS_csvFilename: "+OMNIS_csvFilename);
	String[] row = null;
	String[] names = null;
	List container = new ArrayList <String> ();
	ArrayList arraylist=new ArrayList();
	CSVReader csvReader = null;
	int i=0;	
	try {
		csvReader = new CSVReader(new FileReader(OMNIS_csvFilename));
		while((row = csvReader.readNext()) != null) {
//			print(row[0]);
		    if (i==0)
			names = row[0].split("\\|", -1);
		    else
			container.add(row);
		    i++;
				}
		csvReader.close();
//		printList(names);
		//fill Hashmap
//			print(String.valueOf("container.size: "+container.size()));
		int j = 0;
		    for (Object object : container) {
			    HashMap<String, String> map = new HashMap<String, String>();
			    row = (String[]) object;
			    row = row[0].toString().split("\\|", -1);
				for (i=0; i<row.length; i++)
				{
				    map.put(names[i],row[i]);
//				    printHashmap(map);
				}
				arraylist.add(map);
			}
	} catch (IOException e) {
		e.printStackTrace();
	}
		return arraylist;
}

	public static void fillSource ()
	{
		sourceMap.put("CBI", "EUS-IMGCBLN");
		sourceMap.put("CBI", "EUS-IMGOMLN");
		sourceMap.put("Fidessa", "EUS-PMACLN");
	}
}
