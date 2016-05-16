

import au.com.bytecode.opencsv.CSVReader;
import bsh.EvalError;
import bsh.Interpreter;
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
import java.util.Map.Entry;


public class TADS_FIDESSA_Validate extends TADSValidateCommonActions implements TADSValidator{
	public static List<String> formDynamicXpaths (List<String> lines, HashMap<String, String> map)
	{
		List<String> dynamic_xpaths = new ArrayList<String>();
		for (int i=0; i<lines.size(); i++)
    	{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
			if (thearray.get(1).equals("Real Security No"))
				dynamic_xpaths.add(thearray.get(0)+","+checkOldInstrument(map.get(thearray.get(1))));
			else
		    	dynamic_xpaths.add(thearray.get(0)+","+map.get(thearray.get(1)));
				
    	}
		return dynamic_xpaths;
	}

	public static String findNonStandardCurrency(String csvCurr, List<String> curr_conv)
	{
		for (int i = 0; i < curr_conv.size(); i ++)
		{
			if (curr_conv.get(i).contains(csvCurr))
			return curr_conv.get(i);
		}
		return null;
	}

	public static Double calculateExpression (String expression)
	{
		Double value = null;
//		print("Math expression: " + expression);
		Interpreter interpreter = new Interpreter();
		try {
			interpreter.eval("result = "+expression);
		} catch (EvalError e) {
			e.printStackTrace();
		}
/*
		try {
			print(interpreter.get("result"));
		} catch (EvalError e) {
			e.printStackTrace();
		}
*/		
		try {
			value = (Double) interpreter.get("result");
		} catch (EvalError e) {
			print("Math expression evaluation fails: " + expression);
			e.printStackTrace();
		}
		 catch (ClassCastException e) {
				try {
					value = (double) ((Integer)interpreter.get("result"));
				} catch (EvalError e1) {
					print("Math expression evaluation fails: " + expression);
					e1.printStackTrace();
				}
		 }
		return value;
	}

	public static List<String> formListOfCalcXpaths(List<String> xpaths, HashMap<String, String> csv_source)
	{
		List<String> xpaths_ready = new ArrayList<String> ();
		String xpath_line_ready = null;
		String xpath_to_calc = "";
		for (int i=0; i < xpaths.size(); i++)
		{
		ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(",")));
		xpath_line_ready=thearray.get(0); //added xpath to TADS
		xpath_to_calc += calculateExpression(replaceCSVTags(thearray.get(1),csv_source));		
		for (int j=2; j < thearray.size(); j++)
			{
			xpath_to_calc += replaceCSVTags(thearray.get(j),csv_source); 
			}

		xpath_line_ready+="," + evaluateMathExpression(xpath_to_calc); //added xpath to TADS
//		print("xpath_line_ready: " + xpath_line_ready);
		xpath_to_calc = "";
		xpaths_ready.add(xpath_line_ready);
		xpath_line_ready = "";
		}
	printList(xpaths_ready);
	return xpaths_ready;
	}

	/*
	public static List<String> formListOfStaticValues (List<String> xpaths, String whatToReplace, String valueForReplace, String StaticFilePath)
	{
	String formedXpath = null;
	List<String> xpathFormed = new ArrayList<String>(); 
	List<String> xpathFormed_final = new ArrayList<String>(); 
	for (int i = 0; i < xpaths.size(); i++)
	{
		ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(",")));
//		print(thearray.get(1)+"; "+whatToReplace+"; "+valueForReplace);
		formedXpath = prepareXpathForStatic(thearray.get(1),whatToReplace,valueForReplace);
		print("Xpath for data request from static files: " + formedXpath);
		xpathFormed.add(formedXpath);
	}
		xpathFormed = getStaticXMLvalue(StaticFilePath,xpathFormed);
//	printList(xpathFormed);

	for (int i = 0; i < xpaths.size(); i++)
	{
		ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths.get(i).split(",")));
		try
		{
		xpathFormed_final.add(thearray.get(0)+","+xpathFormed.get(i));
		print("Formed xpath from static data:" + xpathFormed.get(i));
		}
		catch (IndexOutOfBoundsException e)
		{
			xpathFormed_final.add("boolean(not("+thearray.get(0)+"))");
			print("No corresponding record is found for given data");
		}
	}
//	printList(xpathFormed_final);
	return xpathFormed_final;
	}
	
	*/
	public static String formRIC (HashMap<String, String> map)
	{
		String RIC = null;
		RIC = "FIDESSA" + "/"+ map.get("Portfolio No") + "/"+ map.get("RIC") + "/"+ map.get("DBT Security No")+"/"+ checkOldInstrument(map.get("Real Security No"))+"/"+ map.get("Ccy")+"/"+ map.get("SecType")+"/"+map.get("Legal Entity");
		return RIC;
	}
	
	public static String replaceGlobalTags (String text)
	{
		for (Entry<String, String> entry  : globalVariables.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace("{"+entry.getKey()+"}", entry.getValue());
			}
		}
		print("after replacement: " + text);
	return text;
	}

	public static String replaceCSVTags (String text, HashMap<String, String> csv_source)
	{
		for (Entry<String, String> entry  : csv_source.entrySet()) 
		{
			if (text.contains(entry.getKey().toString()))
			{
				text = text.replace("{"+entry.getKey()+"}", entry.getValue());
//				print("after replacement: " + text);
			}
		}
	return text;
	}
	
	public void checkXMLWithStaticXpath	(String filePath, List<String> xpaths_with_static, ArrayList<?> arraylist)
	{
//		print("xpaths_with_static: "+xpaths_with_static.get(0));
		List<String> xpaths_static = null;
		String key = null;

		switch (globalVariables.get("source_folder")) {
        case "Product":  key = "Real Security No";
                 break;
        case "Legal_Entity":  key = "book_id";
        break;
        case "Book":  key = "book_id";
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
//						xpaths_static = formListOfStaticValues (xpaths_with_static, key, BookID, globalVariables.get("static_folder")+"\\"+globalVariables.get("static_file"));
						xpaths_static = formListOfStaticValues (xpaths_with_static, key, BookID, globalVariables.get("static_file"));
						xmlXpathValidation (filePath, xpaths_static);
					RICmarker = true;
				}
	    }
if (!RICmarker)
	print("fail: no csv record suits TADS in staticXpath verification");
//	print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
//************************************************************************************************************	
	public void checkXMLWithSourceXpath (String xmlString, List<String> xpaths_to_form, ArrayList<?> arraylist)
	{
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(xmlString);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
//		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
//			print("csv RIC : " + csvRIC);
				if (compareRICs(xmlRIC, csvRIC))
				{
						List<String> xpaths_dynamic = formDynamicXpaths(xpaths_to_form,row_map);
						xmlXpathValidation (xmlString, xpaths_dynamic);
					RICmarker = true;
				}
	    }
if (!RICmarker)
	print("SourceXpath fail: no csv record suits TADS");
//	print("xml RIC: " + xmlRIC +"; TADS: " + filePath);
	}
//************************************************************************************************************	
	public void checkXMLWithSQL(String filePath, List<String> xpaths_to_sql)
	{
//		print(xpaths_to_sql.size()+" :"+ xpaths_to_sql.get(0));
		List<String> xpaths_to_sql_formed = new ArrayList<String>();
			for (int i = 0; i < xpaths_to_sql.size(); i++)
			{
			ArrayList<String> thearray = new ArrayList<String>(Arrays.asList(xpaths_to_sql.get(i).split(";")));
//			print(thearray.size()+" :"+ thearray.get(0)+" :"+ thearray.get(1));
			String expectedValue = formListValuesFromSQL(replaceGlobalTags(thearray.get(1)));
			print("expectedValue: " + expectedValue);
			xpaths_to_sql_formed.add(thearray.get(0)+","+expectedValue);
			}
		printList(xpaths_to_sql_formed);
			String res_static = xmlXpathValidation (filePath, xpaths_to_sql_formed);
	}
//************************************************************************************************************	
	public void checkXMLWithCurrency (String filePath, ArrayList<?> source,List<String> xpaths_ccy_fake, String CurrencyConvertion)
	{
		List<String> xpaths_ccy = xpaths (globalVariables.get("path")+"\\CurencyXpath");
		List<String> xpaths_amt = xpaths (globalVariables.get("path")+"\\AmountXpath");
		List<String> curr_conv = xpaths (CurrencyConvertion);
		String ccy = null;
		String amt = null;
		String ccy_converted = null;
		String rate = null;
		
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : source) {
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
				if (compareRICs(xmlRIC, csvRIC))
				{
			    	for (int i = 0; i < xpaths_ccy.size(); i++)
			    	{
					ArrayList<String> ccy_array = new ArrayList<String>(Arrays.asList(xpaths_ccy.get(i).split(",")));
					ArrayList<String> amt_array = new ArrayList<String>(Arrays.asList(xpaths_amt.get(i).split(",")));
//			    	print("ccy xpath to process: " + xpaths_ccy.get(i));
//			    	print("amt xpath to process: " + xpaths_amt.get(i));
					ccy = takevalueInCSV(row_map,ccy_array.get(1)); 
					String found_conversion = findNonStandardCurrency(ccy,curr_conv);
//					print(found_conversion);
					if (found_conversion!=null)
					{
						ArrayList<String> curr_conv_values = new ArrayList<String>(Arrays.asList(found_conversion.split(",")));
//						printList(curr_conv_values);
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
					amt = takevalueInCSV(row_map,amt_array.get(1)); 
//			    	print("amt before double parsing: " + amt);
//			    	print("amt format parsing: " + new BigDecimal(amt));
			    	BigDecimal amtBD= new BigDecimal(amt);
			    	BigDecimal rateBD= new BigDecimal(rate);
			    	amtBD = amtBD.divide(rateBD);
//			    	print("Amt: " + amtBD.toPlainString());
			    	List<String> dynamic_xpaths = new ArrayList();
			    	dynamic_xpaths.add(ccy_array.get(0)+","+ccy_converted);
			    	dynamic_xpaths.add(amt_array.get(0)+","+amtBD.toPlainString());
			    	
						String res_dynamic = xmlXpathValidation (filePath, dynamic_xpaths);

					RICmarker = true;
			    	}
				}
	    }
if (!RICmarker)
	print("fail: no csv record suits TADS");
//	print("xml RIC: " + xmlRIC +"; TADS: " + n);
}	
//************************************************************************************************************
	public void checkXMLWithMathCalculations (String filePath, String fileCSV, String fileXpathCalc)
	{
/*
 * 1. read xpath
 * 2. take and replace values from csv
 * 3. form formula
 * 4. evaluate formula
 * 5. verify condition
 */
		List<String> xpaths_with_formula = xpaths (fileXpathCalc);
//		print("xpaths_with_static: "+xpaths_with_static.get(0));
		List<String> xpaths_ready = null;
		ArrayList arraylist = new ArrayList();
		try {
			arraylist = readCSVByLines(fileCSV);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
					xpaths_ready = formListOfCalcXpaths (xpaths_with_formula,row_map);
						xmlXpathValidation (filePath, xpaths_ready);
					RICmarker = true;
				}
	    }
if (!RICmarker)
	print("fail: no csv record suits TADS");
	}
//************************************************************************************************************	
	public void checkXMLWithLogicXpath (String xmlString, List<String> logic_xpaths, ArrayList<?> arraylist)
	{
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(xmlString);
		XPath xpath = createXpath();
    	expr = xpathCompile(xpath,IDxpath);
		String xmlRIC = null;
		xmlRIC = xpathEvaluate(expr,doc);
//		print("xml RIC : " + xmlRIC);
		HashMap<String, String> row_map = new HashMap<String, String>(); 
		for (Object object_map : arraylist) {
	    	row_map = (HashMap<String, String>) object_map;		    	
			String csvRIC = null;
			csvRIC = formRIC(row_map);
//			print("csv RIC : " + csvRIC);
				if (compareRICs(xmlRIC, csvRIC))
				{
					print("csv RIC : " + csvRIC);
						for (int i = 0; i < logic_xpaths.size(); i++)
						{
								String xpath_ready = replaceCSVTags(logic_xpaths.get(i),row_map);
								try
								{
									if (xpath_ready.contains("<static>"))
									{
										//find and substitute static value
										String static_xpath = takeSubstringsBetween(xpath_ready,"<static>","</static>");
										print("static xpath value: "+static_xpath);
										String staticValue = getStaticValueByXpath(static_xpath);
										print("static xpath value: "+staticValue);
										try
										{
											xpath_ready = xpath_ready.replaceAll("<static>.*?</static>", staticValue);
										}
										catch (NullPointerException e)
										{
											print("There is no found Isin value for record: "+static_xpath);
											xpath_ready = xpath_ready.replaceAll("<static>.*?</static>", "false");
										}
										//	print("static xpath value: "+static_xpath);
									}
					            Processor proc = new Processor(false);
					            XPathCompiler xpath20 = proc.newXPathCompiler();
					            net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
					            StringReader reader = new StringReader(xmlString);
					            XdmNode doc20 = builder.build(new StreamSource(reader));
					            print("logic Xpath : " + xpath_ready);
					            XPathSelector selector = xpath20.compile(xpath_ready).load();
					            selector.setContextItem(doc20);
					 
					            XdmValue logicXpathValue = selector.evaluate();
					            if (!logicXpathValue.toString().equals("true"))
									print("Logic fails: "+ xpath_ready);
					        } catch (Exception e) {
					            print("error: "+ e.getLocalizedMessage());
					        }
						}
//						print("SourceXpath verification fails");
					RICmarker = true;
				}
	    }
if (!RICmarker)
	print("LogicXpath fail: no csv record suits TADS");
}
//************************************************************************************************************	

	@Override
	ArrayList<String> formCSVSource() {
		String csvFilename = globalVariables.get("path")+"\\"+globalVariables.get("source_file_initial_name");
		CSVReader csvReader;
		ArrayList arraylist=new ArrayList();
		try {
			csvReader = new CSVReader(new FileReader(csvFilename));
		String[] row = null;
		String[] names = null;
		List container = new ArrayList <String> ();
		while((row = csvReader.readNext()) != null) {
			container.add(row);
				}
		csvReader.close();
//		Common.print(container);
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
			    map.put(names[i].trim(),row[i]);
//			    print ("field: "+ names[i]+"; value: "+row[i]);
				}
				arraylist.add(map);
			}
			j++;
		    }
		} catch (IOException e) {
			print ("Source file with data is not found");
			e.printStackTrace();
		}
			return arraylist;
	}

}
