

import au.com.bytecode.opencsv.CSVReader;
import net.sf.saxon.s9api.*;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TADS_CBI_Validate extends TADSValidateCommonActions implements TADSValidator{

	public static String formRIC (HashMap<String, String> map)
	{
		String RIC = null;
		RIC = "CBI" + "/"+ map.get("PortId");
		return RIC;
	}

	public static List<String> formDynamicXpaths (List<String> lines, HashMap<String, String> map)
	{
		List<String> dynamic_xpaths = new ArrayList();
		//		print("printlist");
		//		printList(lines);
		//		print("printlist size: "+lines.size());
		for (int i=0; i<lines.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
			//    		print("Isin - formDynamicXpaths: "+ thearray.get(1));
			if(thearray.get(1).contains("-"))
			{
				ArrayList<String> coversion = new ArrayList<String>(Arrays.asList(thearray.get(1).split("-")));
				switch (coversion.get(0))
				{
				case "ProductConversion":  dynamic_xpaths.add(thearray.get(0)+","+getCorrespondedProduct(map.get(coversion.get(1))));
				break;
				case "IsOwnBond":  dynamic_xpaths.add(thearray.get(0)+","+checkIsOwnBond(checkOldInstrument(map.get(coversion.get(1)))));
				break;
				}
			}	
			else if ((thearray.get(1).contains("Isin"))) 
			{
				String instrumentId = checkOldInstrument(map.get(thearray.get(1)));
				dynamic_xpaths.add(thearray.get(0)+","+instrumentId);
					    		print("Isin - formDynamicXpaths: "+ dynamic_xpaths.get(i));
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
		List<String> curr_conv = xpaths (globalVariables.get("global_folder")+"\\CurrencyConvertion");
		String ccy_converted = null;
		String rate = null;

		for (Object object_map : arraylist) 
		{
			String csvRIC = null;
			csvRIC = formRIC((HashMap<String, String>) object_map);
			if (xmlRIC.equals(csvRIC))
			{
				print("acc id from math for currency conversion : " + xmlRIC);
				for (int j=0; j < thearray.size(); j++)
				{
					xpath_to_calc += replaceCSVTags(thearray.get(j),(HashMap<String, String>) object_map); 
				}
					print("xpath_to_calc: " + xpath_to_calc);
					if (xpath_to_calc.contains("/0"))
						xpath_to_calc = xpath_to_calc.replace("/0", "*0");
					String sourceCurrency = replaceCSVTags("Curr", (HashMap<String, String>)object_map);
					print("Currency: " + sourceCurrency);
					
					String found_conversion = findNonStandardCurrency(sourceCurrency,curr_conv); 
					print("found_conversion: "+found_conversion);
					if (found_conversion!=null)
					{
						ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
						ccy_converted = curr_conv_values.get(1);
						rate = curr_conv_values.get(2);
					}
					else
					{
						ccy_converted = found_conversion;
						rate = "1";
					}
			    	print("Ccy converted: " + ccy_converted);
			    	print("Rate: " + rate);

					String finalMathValue = "(" +xpath_to_calc + ")/"+ rate;
//					xpath_line_ready=evaluateMathExpression(xpath_to_calc).toString();
					xpath_line_ready=evaluateMathExpression(finalMathValue).toString();
					print("xpath_line_ready: " + xpath_line_ready);
			}
		}
		xpath_line_ready = xpath_line_ready.replace(",", ".");
		return xpath_line_ready;
	}

	public void checkXMLWithStaticXpath	(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist)
	{
		//	List<String> xpaths_with_static = xpaths (fileXPath_with_static);
		//	print("xpaths_with_static: "+xpaths_with_static.get(0));
		List<String> xpaths_static = null;
		String key = null;
		String keyIndicator = globalVariables.get("source_folder");//.contains("OMNIS") ? globalVariables.get("source_file").substring("OMNIS_".length()) : globalVariables.get("source_file");
		switch (keyIndicator) {
		case "Product":  key = "Isin";
		break;
		case "Book":  key = "Acct";
		break;
		case "Counterparty":  key = "Customer";
		break;
		}
		//	print(key);
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		//	print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) 
		{
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			//		print("csv RIC : " + csvRIC);
			if (compareRICs(xmlRIC, csvRIC))
			{
				String BookID = null;
				BookID = takevalueInCSV(row_map,key);
				//				print("Key : " + BookID);
				xpaths_static = formListOfStaticValues (xpaths_with_static, key, BookID, globalVariables.get("static_file"));
				//					printList(xpaths_static);
				xmlXpathValidation (filePath, xpaths_static);
				RICmarker = true;
			}
		}
		if (!RICmarker)
			print("fail: no csv record suits TADS");
		//print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
	//************************************************************************************************************	
	public void checkXMLWithLogicXpath (String xml, List<String> xpaths, ArrayList<?> csv_array)
	{
		//List<String> xpaths = xpaths (LogicXPath);
		//printList(xpaths);
		String static_xpath = null;
		for (int i=0; i<xpaths.size(); i++)
		{
			static_xpath = xpaths.get(i);
			//			print("current xpath: "+xpaths.get(i));
			if (xpaths.get(i).contains("<static>"))
			{
				//find and substitute static value
				static_xpath = takeSubstringsBetween(xpaths.get(i),"<static>","</static>");
//				print("static xpath value: "+static_xpath);
				String staticValue = takeStaticXMLValueByXpath(xml, static_xpath,csv_array);
//				print("static xpath value: "+staticValue);
				try
				{
					static_xpath = xpaths.get(i).replaceAll("<static>.*?</static>", staticValue);
				}
				catch (NullPointerException e)
				{
					print("There is no found Isin value for record: "+static_xpath);
					static_xpath = xpaths.get(i).replaceAll("<static>.*?</static>", "false");
				}
//					print("static xpath value: "+static_xpath);
			}
			else if (xpaths.get(i).contains("<source>"))
			{
				//find and substitute static value
				static_xpath = takeSubstringsBetween(xpaths.get(i),"<source>","</source>");
				//      	print("static xpath value: "+static_xpath);
				String sourceValue = findCorrespondingCSVRecord(xml, static_xpath,csv_array);
				//	print("sourceValue : "+sourceValue);
				if (sourceValue.contains("."))
				{
					sourceValue = sourceValue.replaceAll("[0]*$", "").replaceAll("\\.$", "");
				}
				//	print("sourceValue : "+sourceValue);
				//	if (sourceValue.contains(".000000")){sourceValue = sourceValue.substring(0,sourceValue.indexOf(".000000"));	}
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
				print("math static_xpath : "+static_xpath);
				String sourceValue = evaluateCalcValues(xml,static_xpath,csv_array);
//				print("sourceValue : "+sourceValue);
				sourceValue = sourceValue.replaceAll("[0]*$", "").replaceAll("\\.$", "");
				//	print("sourceValue : "+sourceValue);
				static_xpath = xpaths.get(i).replaceAll("<math>.*?</math>", sourceValue);
			}
			else if (xpaths.get(i).contains("IsOwnBond-Isin"))
			{
				String sourceValue = null;
				XPathExpression expr = null;
				Document doc = formXMLdocument(xml);
				XPath xpath = createXpath();
				expr = xpathCompile(xpath,IDxpath);
				String xmlRIC = null;
				xmlRIC = xpathEvaluate(expr,doc);
//				print("xml RIC in logic xpath for IsOwnBond check: " + xmlRIC);
				HashMap<String, String> row_map = new HashMap<String, String>(); 
				for (Object object_map : csv_array) {
					row_map = (HashMap<String, String>) object_map;		    	
					String csvRIC = null;
					csvRIC = formRIC(row_map);
					if (compareRICs(xmlRIC, csvRIC))
					{
						print("xml RIC in logic xpath for IsOwnBond check: " + xmlRIC);
						sourceValue = checkIsOwnBond(checkOldInstrument(row_map.get("Isin")));
					}
				}

				if (sourceValue.isEmpty())
				{
					print("IsOwnBond-Isin combination returned as empty and is_own_bond supposed to be false");
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
				String xmlRIC = null;
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
//				print("static_xpath final for logic verification: "+static_xpath);
				Processor proc = new Processor(false);
				XPathCompiler xpath = proc.newXPathCompiler();
				net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
				StringReader reader = new StringReader(xml);
				XdmNode doc = builder.build(new StreamSource(reader));
				//    print("logic Xpath : " + static_xpath);
				XPathSelector selector = xpath.compile(static_xpath).load();
				selector.setContextItem(doc);

				XdmValue logicXpathValue = selector.evaluate();
				if (!logicXpathValue.toString().equals("true"))
					print("Logic fails: "+ static_xpath);
			} catch (Exception e) {
				print("error: "+ e.getLocalizedMessage());
			}
		}
	}
	
	//************************************************************************************************************	
	public void checkXMLWithSourceXpath (String xmlString, List<String> xpaths_to_form, ArrayList<?> arraylist)
	{
		//List<String> xpaths_to_form = xpaths (fileXPath_to_form);
		//print("arraylist size: "+arraylist.size());
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(xmlString);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			if (compareRICs(xmlRIC, csvRIC))
			{
				//				print("csv RIC : " + csvRIC);
				List<String> xpaths_dynamic = formDynamicXpaths(xpaths_to_form,row_map);
				xmlXpathValidation (xmlString, xpaths_dynamic);
				RICmarker = true;
			}
		}
		if (!RICmarker)
			print("SourceXpath fail: no csv record suits TADS");
		//print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
	//************************************************************************************************************	
	public void checkXMLWithSQL(String filePath,List<String> xpaths_to_sql)
	{
		//print(xpaths_to_sql.size()+" :"+ xpaths_to_sql.get(0));
		List<String> xpaths_to_sql_formed = new ArrayList<String>();
		for (int i = 0; i < xpaths_to_sql.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths_to_sql.get(i).split(";")));
			//	print(thearray.size()+" :"+ thearray.get(0)+" :"+ thearray.get(1));
			String expectedValue = formListValuesFromSQL(replaceGlobalTags(thearray.get(1)));
			xpaths_to_sql_formed.add(thearray.get(0)+","+expectedValue);
		}
		//printList(xpaths_to_sql_formed);
		String res_static = xmlXpathValidation (filePath, xpaths_to_sql_formed);
	}	
	//************************************************************************************************************	
	public void checkXMLWithCurrency (String filePath, ArrayList<?> source, List<String> xpaths_ccy,  String CurrencyConvertion)
	{
		//List<String> curr_conv = new ArrayList<String>();
		String ccy = null;
		String ccy_converted = null;
		String rate = null;
		List<String> curr_conv = xpaths (CurrencyConvertion);

		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		//print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : source) {
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			//	print("csv RIC : " + csvRIC);
			if (compareRICs(xmlRIC, csvRIC))
			{
				for (int i = 0; i < xpaths_ccy.size(); i++)
				{
					ArrayList<String> ccy_array = new ArrayList<String>(Arrays.asList(xpaths_ccy.get(i).split(",")));
					print("ccy xpath to process: " + xpaths_ccy.get(i));
					ccy = takevalueInCSV(row_map,ccy_array.get(1)); 
					String found_conversion = findNonStandardCurrency(ccy,curr_conv); 
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
					print("Ccy: " + ccy_converted);
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
		String csvFilename = globalVariables.get("path")+"//"+globalVariables.get("source_file_initial_name");
		String notOMNISfilename = Common.listFilesForFolder(new File (globalVariables.get("path")), globalVariables.get("CBICBEUMASK")+".*").get(0);
		String OMNISfilename = Common.listFilesForFolder(new File (globalVariables.get("path")), globalVariables.get("CBIOMNISMASK")+".*").get(0);

		//		Common.print(notOMNISfilename);
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		ArrayList arraylist=new ArrayList();
		CSVReader csvReader = null;
		int i =0;	
		if (csvFilename.contains("OMNIS"))
		{
			try {
				csvReader = new CSVReader(new FileReader(csvFilename));
				while((row = csvReader.readNext()) != null) {
					container.add(row);
					i++;
				}
				//		print("csvReader: " + i);
				csvReader.close();
			} catch (IOException e) {
				print("Impossible to read csv file!");
				e.printStackTrace();
			}
		}
		else
		{	
			try {
				csvReader = new CSVReader(new FileReader(OMNISfilename));
				container.add(csvReader.readNext());
				i++;
				csvReader.close();
				//		print("csvReader: " + i);
				csvReader = new CSVReader(new FileReader(notOMNISfilename));
				i=0;
				while((row = csvReader.readNext()) != null) {
					//			print("csvReader: " + row[0]);
					container.add(row);
					i++;
				}
				//		print("csvReader: " + i);
				csvReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//fill Hashmap
		names = (String[]) container.get(0);
		Integer j=0;
		for (Object object : container) {
			if (j>0&&j<container.size())
			{
				HashMap<String, String> map = new HashMap<String, String>();
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
				}
				arraylist.add(map);
				//printHashmap(map);
			}
			j++;
		}
		return arraylist;
	}

	public static String takeStaticXMLValueByXpath(String filePath, String xpaths_with_static, ArrayList arraylist)
	{
		String staticValue = null;
		String xpaths_static = null;
		String key = null;

		switch (globalVariables.get("source_folder")) 
		{
		case "Product":  key = "Isin";
		break;
		case "Book":  key = "Acct";
		break;
		case "Counterparty":  key = "Customer";
		break;
		}
//		print(key);
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		//	print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) 
		{
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			//		print("csv RIC : " + csvRIC);
			if (compareRICs(xmlRIC, csvRIC))
			{
				String BookID = null;
				BookID = takevalueInCSV(row_map,key);
				xpaths_static = getStaticValueByXpath(xpaths_with_static.replace(key, BookID));
				//					printList(xpaths_static);
			}
		}
		return xpaths_static;
	}

	public static String findCorrespondingCSVRecord (String filePath, String key, ArrayList arraylist)
	{
		String BookID = null;
		//	print(key);
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
			//		print("xml RIC : " + xmlRIC.equals(csvRIC));
			if (xmlRIC.equals(csvRIC))
			{
				BookID = takevalueInCSV((HashMap<String, String>) object_map,key);
				//				print("Key : " + BookID);
				break;
			}
		}
		return BookID;
	}

}
