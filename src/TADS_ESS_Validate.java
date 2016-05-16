

import au.com.bytecode.opencsv.CSVReader;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map.Entry;

public class TADS_ESS_Validate extends TADSValidateCommonActions implements TADSValidator{
	//**************************************************************************	
	public static String formRIC (HashMap<String, String> map)
	{
		String RIC = null;
		if (globalVariables.get("source_folder").contains("Valuation") && !globalVariables.get("source_folder").contains("Non_Trading"))
			RIC = "ESS" + "/"+ map.get("SwapNum");
		else 
			RIC = "ESS-PLESR" + "/"+ map.get("SwapNum");
		return RIC;
	}

	public static String getCorrespondedProduct(String DBSecType, HashMap<String, String> map)
	{
		String AcSProductType = null;
		List<String> conversions = null;
		try {
			conversions = readLines(globalVariables.get("global_folder")+"\\ProductConversion");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] combination = formESSProduct(DBSecType,map);
		for (int i = 0; i < conversions.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(conversions.get(i).split(",")));
			if (productAnalyzer (thearray.get(0),combination))
			{
				AcSProductType = thearray.get(1);
				break;
			}
		}
		return AcSProductType;
	}

	public static String getCorrespondedAdjustment(String DBSecType, HashMap<String, String> map)
	{
		String AcSProductType = null;
		List<String> conversions = null;
		try {
			conversions = readLines(globalVariables.get("global_folder")+"\\AdjustmentConversion");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] combination = formESSProduct(DBSecType,map);
		for (int i = 0; i < conversions.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(conversions.get(i).split(",")));
			if (productAnalyzer (thearray.get(0),combination))
			{
				AcSProductType = thearray.get(1);
				break;
			}
		}
		return AcSProductType;
	}

	public static String getCorrespondedSchema(String DBSecType, HashMap<String, String> map)
	{
		String AcSProductType = null;
		List<String> conversions = null;
		try {
			conversions = readLines(globalVariables.get("global_folder")+"\\ProductConversion");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] combination = formESSProduct(DBSecType,map);
		//			print("DBSecType: " + DBSecType);
		//			printList (combination);
		for (int i = 0; i < conversions.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(conversions.get(i).split(",")));
			//				print ("thearray.get(0): " + thearray.get(0));
			if (productAnalyzer (thearray.get(0),combination))
			{
				AcSProductType = thearray.get(2);
				break;
			}
		}
		//			print("AcSProductType: " + AcSProductType);
		return AcSProductType;
	}

	public static Boolean productAnalyzer (String fieldsCombination, String[] record)
	{
		String[] parts = fieldsCombination.split("/");
		String recordKey = "";
		for (int i = 0; i < parts.length; i++)
		{
			if  (parts[i].equals("X"))
				recordKey+="X/";
			else
				recordKey+=record[i]+"/";
		}
		recordKey = recordKey.substring(0, recordKey.length()-1);
//					print("recordKey: "+recordKey);
//					print("fieldsCombination: "+fieldsCombination);
		if (recordKey.equals(fieldsCombination))
			return true;
		else
			return false;
	}

	public static String[] formESSProduct (String prodVariants, HashMap<String, String> map)
	{
		String[] parts = prodVariants.split("/");
		String[] values = new String[parts.length];
		String product = "";
		//				printHashmap(map);
		for (int i = 0; i < parts.length; i++)
		{
			product += map.get(parts[i])+"/";
			values[i] = map.get(parts[i]);
		}
		product = product.substring(0, product.length()-1);
		//				print ("product: "+product);
		return values;
	}

	public static List<String> formDynamicXpaths (List<String> lines, HashMap<String, String> map)
	{
		List<String> dynamic_xpaths = new ArrayList();
		//			print("printlist");
		//			printList(lines);
		//			print("printlist size: "+lines.size());
		for (int i=0; i<lines.size(); i++)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
			//	    		print("Isin - formDynamicXpaths: "+ thearray.get(1));
			if(thearray.get(1).contains("-"))
			{
				ArrayList<String> conversion = new ArrayList<String>(Arrays.asList(thearray.get(1).split("-")));
				switch (conversion.get(0))
				{
				case "ProductConversion":  dynamic_xpaths.add(thearray.get(0)+","+getCorrespondedProduct(conversion.get(1),map));
				break;
				case "IsOwnBond":  dynamic_xpaths.add(thearray.get(0)+","+checkIsOwnBond(checkOldInstrument(map.get(conversion.get(1)))));
				break;
				case "SchemaConversion":  dynamic_xpaths.add(thearray.get(0)+","+getCorrespondedSchema(conversion.get(1),map));
				break;
				case "AdjustmentConversion":  dynamic_xpaths.add(thearray.get(0)+","+getCorrespondedAdjustment(conversion.get(1),map));
				break;
				}
			}	
			else if ((thearray.get(1).contains("Isin"))) 
			{
				dynamic_xpaths.add(thearray.get(0)+","+checkOldInstrument(map.get(thearray.get(1))));
				//		    		print("Isin - formDynamicXpaths: "+ dynamic_xpaths.get(i));
			}
			else {
				dynamic_xpaths.add(thearray.get(0)+","+map.get(thearray.get(1)));
				//		    		print("simple - formDynamicXpaths: "+ dynamic_xpaths.get(i));
			}
			//	    		print("iteration "+i+" - formDynamicXpaths: "+ dynamic_xpaths.get(i));
		}
		return dynamic_xpaths;
	}

	public static String evaluateCalcValues(String filePath, String expression, ArrayList arraylist)
	{
		String xpath_line_ready = null;
		String xpath_to_calc = "";
		//			print(expression);
		ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(expression.split(",")));
		//			printList(thearray);

		//			print(key);
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
			//				print("xml RIC : " + xmlRIC.equals(csvRIC));
			if (compareRICs(xmlRIC,csvRIC))
			{
				for (int j=0; j < thearray.size(); j++)
				{
					String string = replaceCSVTags(thearray.get(j),(HashMap<String, String>) object_map);
//					print("string: " + string);
					if ((string.charAt(0) == '0') && !(string.startsWith("0.")) && (string.length()!= 1))
					    string = string.substring(1);
					xpath_to_calc += string; 
				}
//						print("xpath_to_calc: " + xpath_to_calc);
				xpath_line_ready=evaluateMathExpression(xpath_to_calc);//.toString();
//						print("xpath_line_ready: " + xpath_line_ready);
			}
		}
		return xpath_line_ready;
	}

	public static String replaceCSVTags (String text, HashMap<String, String> csv_source)
	{
		String ccy = null;

		for (Entry<String, String> entry  : csv_source.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace(entry.getKey(), entry.getValue());
				//					print("after replacement: " + text);
			}
			else if (text.equals("rate(CCY)"))
			{
				ccy = findCcyRate(csv_source.get("CCY"));
				text = text.replace("rate(CCY)", ccy);
				//					print("after replacement: " + text);
			}
		}
		return text;
	}

	public static String findCcyRate (String ccy)
	{
		List<String> curr_conv = xpaths (globalVariables.get("global_folder")+"\\CurrencyConvertion");
		String found_conversion = findNonStandardCurrency(ccy,curr_conv); 
		String rate = null;
		if (found_conversion!=null)
		{
			ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
			rate = curr_conv_values.get(2);
			//		    	print("Ccy conv: " + ccy_converted);
			//		    	print("Ccy rate: " + rate);
		}
		else
		{
			rate = "1";
			//		    	print("Ccy real: " + ccy_converted);
		}
		return rate;
	}

	public static String findCcyConv (String ccy)
	{
		List<String> curr_conv = xpaths (globalVariables.get("global_folder")+"\\CurrencyConvertion");
		String found_conversion = findNonStandardCurrency(ccy,curr_conv); 
		String rate = null;
		if (found_conversion!=null)
		{
			ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
			rate = curr_conv_values.get(1);
		}
		else
		{
			rate = ccy;
		}
		return rate;
	}

	public static String findNonStandardCurrency(String csvCurr, List<String> curr_conv)
	{
		for (int i = 0; i < curr_conv.size(); i ++)
		{
			//				print("curr_conv.get(i).contains(csvCurr): "+curr_conv.get(i)+"; "+curr_conv.get(i).split(",")[0]+" ::: "+csvCurr);
			if (curr_conv.get(i).split(",")[0].contains(csvCurr))
				return curr_conv.get(i);
		}
		return null;
	}

	public static String[] takeArraySubstringsBetween (String exp, String leftSeparator, String rightSeparator)
	{
		return StringUtils.substringsBetween(exp, leftSeparator, rightSeparator);
	}

	public static String takeStaticXMLValueByXpath(String filePath, String xpaths_with_static, HashMap<String, String> row_map)
	{
		String staticValue = null;
		String xpaths_static = null;
		String key = null;

		//			print("omnis_or_not: "+omnis_or_not);
		if (globalVariables.get("source_folder").contains("Product"))
		{
			key = "ISIN";
		}
		else if (globalVariables.get("source_folder").contains("Book"))
		{
			key = "Book";
		}
		else if (globalVariables.get("source_folder").contains("Counterparty"))
		{
			key = "BICCode";
		}
		/*		
			switch (omnis_or_not) {
	        case "Product":  key = "ISIN";
	                 break;
	        case "Book":  key = "Book";
	        		break;
	        case "Counterparty":  key = "BICCode";
	        		break;
			}
		 */
		//			print(key);
		String BookID = null;
		BookID = takevalueInCSV(row_map,key);
		//						print("Key : " + BookID);
		if(globalVariables.get("source_folder").contains("Product"))
			xpaths_static = getStaticValueByXpath(xpaths_with_static.replace("Isin", BookID));
		else
		{
			//							print("xpaths_with_static.replace(key, BookID): "+xpaths_with_static.replace(key, BookID));
			xpaths_static = getStaticValueByXpath(xpaths_with_static.replace(key, BookID));
		}
		//							print("xpaths_static value: "+xpaths_static);
		return xpaths_static;
	}

	public static String findCorrespondingCSVRecord (String filePath, String key, ArrayList arraylist)
	{
		String BookID = null;
		//			print("key: "+key);
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
			if (compareRICs(xmlRIC,csvRIC))
			{
				BookID = takevalueInCSV((HashMap<String, String>) object_map,key);

				if (key.equals("CCY"))
				{
					BookID = findCcyConv(BookID);
					break;
				}
				//						print("Key : " + BookID);
				if (key.equals("ISIN"))
				{
					BookID = checkOldInstrument(BookID);
					//						print("Key : " + BookID);
					break;
				}
			}
		}
		return BookID;
	}

	public static String replaceData(String static_xpath, String[] static_xpath_array,String[] sourceValue_arr, String left, String right)
	{
		for (int ii = 0; ii<static_xpath_array.length; ii++)
		{
//			print("test:" + static_xpath_array[ii]);
			if (!left.contains("math"))
				static_xpath = static_xpath.replace(left+static_xpath_array[ii]+right, sourceValue_arr[ii]);
			else
				static_xpath = static_xpath.replace(left+static_xpath_array[ii]+right, sourceValue_arr[ii].replaceAll("[0]*$", "").replaceAll("\\.$", ""));
		}
		return static_xpath;
	}

	//************************************************************************************************************	
	public void checkXMLWithStaticXpath	(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist)
	{
		//				print("xpaths_with_static: "+xpaths_with_static.get(0));
		List<String> xpaths_static = null;
		String key = null;
		//				String omnis_or_not = globalVariables.get("source_file").contains("OMNIS") ? globalVariables.get("source_file").substring("OMNIS_".length()) : globalVariables.get("source_file");
		String file = StringUtils.substringAfterLast(globalVariables.get("source_folder"), "_"); 
		//						globalVariables.get("source_file").contains("Valuation_") ? globalVariables.get("source_file").substring("Valuation_".length()) : globalVariables.get("source_file").substring("Adjustment_".length())
		//				print(file);
		switch (file) {
		case "Product":  key = "Isin";
		break;
		case "Book":  key = "Book";
		break;
		case "Counterparty":  key = "Customer";
		break;
		}
		//				print("key: "+key);
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		//				print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) 
		{
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			//					print("csv RIC : " + csvRIC);
			if (compareRICs(xmlRIC, csvRIC))
			{
				String BookID = null;
				BookID = takevalueInCSV(row_map,key);
				xpaths_static = formListOfStaticValues (xpaths_with_static, key, BookID, globalVariables.get("static_file"));
				//								printList(xpaths_static);
				xmlXpathValidation (filePath, xpaths_static);
				RICmarker = true;
			}
		}
		if (!RICmarker)
			print("fail: no csv record suits TADS");
		//			print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
	//************************************************************************************************************	
	public void checkXMLWithLogicXpath (String xml, List<String> xpaths, ArrayList<?> csv_array)
	{
		//			printList(xpaths);
		String static_xpath = null;
		XPathExpression exprToFindRecord = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(xml);
		XPath xpathToFindRecord = createXpath();
		exprToFindRecord = xpathCompile(xpathToFindRecord,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(exprToFindRecord,doc);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : csv_array) {
			row_map = (HashMap<String, String>) object_map;
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			if (compareRICs(xmlRIC, csvRIC))
			{
				print("RIC from logic xpath verification: " + xmlRIC);
				for (int i=0; i<xpaths.size(); i++)
				{
					static_xpath = xpaths.get(i);
					//	        	print("current xpath: "+xpaths.get(i));
					if (static_xpath.contains("<static>"))
					{
						//find and substitute static value
//						String sub_static_xpath = takeSubstringsBetween(static_xpath,"<static>","</static>");
						String[] static_xpath_array = takeArraySubstringsBetween(static_xpath,"<static>","</static>");
						//	        	print("static_xpath: "+static_xpath);
						String[] sourceValue_arr = new String[static_xpath_array.length];
						
						for (int ii = 0; ii<static_xpath_array.length; ii++)
						{
							sourceValue_arr[ii] = takeStaticXMLValueByXpath(xml, static_xpath_array[ii],row_map);
						}

						try
						{
							static_xpath = replaceData(static_xpath,static_xpath_array,sourceValue_arr,"<static>","</static>");
						}
						catch (NullPointerException e)
						{
							print ("LogicXpath fail: no csv record suits TADS");
						}
						print("static xpath value after static: "+static_xpath);
					}
					
					if (static_xpath.contains("<source>"))
					{
						//find and substitute static value
						String[] static_xpath_array = takeArraySubstringsBetween(static_xpath,"<source>","</source>");
						//	        	static_xpath = takeSubstringsBetween(static_xpath,"<source>","</source>");
						//      	print("static xpath value: "+static_xpath);
						//	        	String sourceValue = findCorrespondingCSVRecord(xml, static_xpath,csv_array);
						String[] sourceValue_arr = new String[static_xpath_array.length];
						for (int ii = 0; ii<static_xpath_array.length; ii++)
						{
							//	                	print("static_xpath_array[ii]: "+static_xpath_array[ii]);
							sourceValue_arr[ii] = findCorrespondingCSVRecord(xml, static_xpath_array[ii],csv_array);
							//	                	print("sourceValue_arr[ii]: "+sourceValue_arr[ii]);
						}
						//	        	print("sourceValue : "+sourceValue);
						/*
	        		if (sourceValue.contains("."))
	        	{
	            	sourceValue = sourceValue.replaceAll("[0]*$", "").replaceAll("\\.$", "");
	        	}
//	        	print("sourceValue : "+sourceValue);
//	        	if (sourceValue.contains(".000000")){sourceValue = sourceValue.substring(0,sourceValue.indexOf(".000000"));	}
	        	if (sourceValue.isEmpty())
	        	{
	        		sourceValue="0";
	        	}
//	        	static_xpath = static_xpath.replaceAll("<source>.*?</source>", sourceValue);
						 */
						try
						{
							static_xpath = replaceData(static_xpath,static_xpath_array,sourceValue_arr,"<source>","</source>");
						}
						catch (NullPointerException e)
						{
							print ("LogicXpath fail: no csv record suits TADS");
						}
						print("static_xpath after source: "+static_xpath);
					}
					if (static_xpath.contains("<math>"))
					{
						String[] static_xpath_array = takeArraySubstringsBetween(static_xpath,"<math>","</math>");
						String[] sourceValue_arr = new String[static_xpath_array.length];

						for (int j = 0; j < static_xpath_array.length; j++)
						{
							sourceValue_arr[j] = evaluateCalcValues(xml,static_xpath_array[j],csv_array);
//							print("sourceValue_arr[j]: "+sourceValue_arr[j]);
						}        	

						static_xpath = replaceData(static_xpath,static_xpath_array,sourceValue_arr,"<math>","</math>");
						print("static_xpath after math: "+static_xpath);
					}
					if (static_xpath.contains("IsOwnBond-ISIN"))
					{
						String sourceValue = null;
						sourceValue = checkIsOwnBond(checkOldInstrument(row_map.get("ISIN")));
						if (sourceValue.isEmpty())
						{
							sourceValue="false";
						}
						static_xpath = static_xpath.replaceAll("IsOwnBond-ISIN", sourceValue);
					}
					if (static_xpath.contains("isFungible"))
					{
						String sourceValue = null;
						sourceValue = checkIsFungible(row_map);
						if (sourceValue.isEmpty())
						{
							sourceValue="Y";
						}
						static_xpath = static_xpath.replaceAll("isFungible", sourceValue);
						print("static_xpath after isFungible: "+static_xpath);
					}
					else
					{
						static_xpath = static_xpath;
					}
					try {
						Processor proc = new Processor(false);
						XPathCompiler xpath = proc.newXPathCompiler();
						net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
						StringReader reader = new StringReader(xml);
						XdmNode docXdmNode = builder.build(new StreamSource(reader));
						//	            print("logic Xpath : " + static_xpath);
						XPathSelector selector = xpath.compile(static_xpath).load();
						selector.setContextItem(docXdmNode);

						XdmValue logicXpathValue = selector.evaluate();
						//	            print(logicXpathValue.toString());
						if (!logicXpathValue.toString().equals("true"))
							print("Logic fails: "+ static_xpath);
					} catch (Exception e) {
						print("error: "+ e.getLocalizedMessage());
					}
				}
				RICmarker = true;
				break;
			}
		}
		if (!RICmarker)
			print("LogicXpath fail: no csv record suits TADS");
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
			Common.print("temp: " + temp);
			if (isFungibleFormedKey.matches(temp))
			{
				isFungibleFlag = line.split(",")[1];
				Common.print("bingo isFungibleFlag: " + isFungibleFlag);
				break;
			}
		}
		return isFungibleFlag;
	}
	
	//************************************************************************************************************	
	public void checkXMLWithSourceXpath (String xml, List<String> xpaths_to_form, ArrayList<?> arraylist)
	{
		//			List<String> xpaths_to_form = xpaths (fileXPath_to_form);
		//			print("arraylist size: "+arraylist.size());
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(xml);
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
				print("Source csvRIC: "+csvRIC);
				List<String> xpaths_dynamic = formDynamicXpaths(xpaths_to_form,row_map);
				//							printList(xpaths_dynamic);
				xmlXpathValidation (xml, xpaths_dynamic);
				RICmarker = true;
				break;
			}
		}
		if (!RICmarker)
			print("SourceXpath fails: no csv record suits TADS found");
		//		print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
	//************************************************************************************************************	
	public void checkXMLWithSQL(String filePath,List<String> xpaths_to_sql)
	{
		//			print(xpaths_to_sql.size()+" :"+ xpaths_to_sql.get(0));
		List<String> xpaths_to_sql_formed = new ArrayList<String>();
		for (String xpath : xpaths_to_sql)
		{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpath.split(";")));
//							print(thearray.size()+" :"+ thearray.get(0)+" :"+ thearray.get(1));
			String expectedValue = formListValuesFromSQL(replaceGlobalTags(thearray.get(1)));
//							print("SQL expectedValue: "+expectedValue);
			xpaths_to_sql_formed.add(thearray.get(0)+","+expectedValue);
		}
		String res_static = xmlXpathValidation (filePath, xpaths_to_sql_formed);
	}
	//************************************************************************************************************	
	public void checkXMLWithCurrency (String filePath, ArrayList<?> source, List<String> xpaths_ccy, String CurrencyConvertion)
	{
		//			List<String> curr_conv = new ArrayList<String>();
		String ccy = null;
		String ccy_converted = null;
		String rate = null;
		List<String> curr_conv = xpaths (CurrencyConvertion);
		List<String> xpaths_amt = xpaths (globalVariables.get("path")+"\\AmountXpath");
		if (print_xpath_ccy==0)
		{
			print("Amount xpaths to check for each TADS:");
			printList(xpaths_amt);
		}

		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		//			print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : source) {
			row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
			//				print("csv RIC : " + csvRIC);
			if (compareRICs(xmlRIC, csvRIC))
			{
				for (int i = 0; i < xpaths_ccy.size(); i++)
				{
					ArrayList<String> ccy_array = new ArrayList<String>(Arrays.asList(xpaths_ccy.get(i).split(",")));
					ArrayList<String> amt_array = new ArrayList<String>(Arrays.asList(xpaths_amt.get(i).split(",")));
					//				    	print("ccy xpath to process: " + xpaths_ccy.get(i));
					ccy = takevalueInCSV(row_map,ccy_array.get(1)); 
					//				    	print("ccy to process: " + ccy);
					String found_conversion = findNonStandardCurrency(ccy,curr_conv); 
					if (found_conversion!=null)
					{
						ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
						ccy_converted = curr_conv_values.get(1);
						rate = curr_conv_values.get(2);
						//					    	print("Ccy conv: " + ccy_converted);
						//					    	print("Ccy rate: " + rate);
					}
					else
					{
						ccy_converted = ccy;
						rate = "1";
						//					    	print("Ccy real: " + ccy_converted);
					}
					//				    	print("Ccy: " + ccy_converted);
					//				    	print("Rate: " + rate);

					String amt = takevalueInCSV(row_map,amt_array.get(1)); 

					BigDecimal rateBD= new BigDecimal(rate);
					BigDecimal amtBD= new BigDecimal(amt);
					//				    	print("Amt initial: " + amtBD);
					amtBD = amtBD.divide(rateBD);
					//				    	print("Amt final: " + amtBD);

					List<String> dynamic_xpaths = new ArrayList();
					dynamic_xpaths.add(ccy_array.get(0)+","+ccy_converted);
					dynamic_xpaths.add(amt_array.get(0)+","+amtBD);

					String res_dynamic = xmlXpathValidation (filePath, dynamic_xpaths);
				}
				RICmarker = true;
				break;
			}
		}
		if (!RICmarker)
			print("CurrencyXpath fail: no csv record suits TADS");
	}	
	//************************************************************************************************************	

	public ArrayList formCSVSource ()
	{
		String OMNIS_csvFilename = globalVariables.get("path")+"\\"+globalVariables.get("source_file_initial_name");
		//		print("OMNIS_csvFilename: "+OMNIS_csvFilename);
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
			return arraylist;
		}
		catch (IOException e)
		{
			print ("test fail - There is no source file");
		}
		return arraylist;
	}	

}
