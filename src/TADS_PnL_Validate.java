

import jsonProcessingForPnL.*;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


public class TADS_PnL_Validate  extends TADSValidateCommonActions implements TADSValidator{
	private static final String BookID = null;
	static String Source = null;
	static String BL = null;
	public static String eol = System.getProperty("line.separator");  
	static String IDxpath = "//accountable_id/text()";
	static String system = null;

	int print_xpath_plain = 0;
	int print_xpath_source = 0;
	int print_xpath_logic = 0;
	int print_xpath_static = 0;
	int print_xpath_sql = 0;
	int print_xpath_ccy = 0;
	static String path = null;

	static String staticSource = null;
	static String staticSourceSystem = null;
	static HashMap <String,String> sourceMap = new HashMap <String,String>();

	public List<String> validate(String[] args, Properties master_props) {
		print_xpath_plain = 0;
		print_xpath_source = 0;
		print_xpath_logic = 0;
		print_xpath_static = 0;
		print_xpath_sql = 0;
		print_xpath_ccy = 0;

		TADSValidateCommonActions.init (args,master_props);
		output.clear();
		props = master_props;
		Common.print("Validation starts");
		staticSource = args[2];
		String folderWithTADS = args[1];
		print("folder with TADS file: " + folderWithTADS);
		String arguments = "";

//		Source = formFidessaSource(new File (args[0]));
//		print("Source file: " + Source);
		path = formPath(new File (args[0]));
		print("Source file path: " + path);

//		String testFolderWithJsons = args[0];
		formJsonSource Json = new formJsonSource (path);

		ArrayList<PnLReport> listWithJsonsObjects = Json.formReportsObjectsFromJson();

		master_props.put("path", path);

		//get list of formed xml files with TADS messages - e.g. from e:\TransactionAccountingSVNAuto\TA\TestData\PnL\temp\33382\SRV_FILPUB\data\ 		
		File[] foundFiles = filesList(new File(folderWithTADS));

		for (int j = 3; j < args.length; j++)
		{
			arguments = arguments+args[j]+";";
		}
		print("Verifications to perform: " + arguments);

		try
		{
			print("listWithJsonsObjects contains " + listWithJsonsObjects.size() + " elements; there are " + foundFiles.length + " TADS files generated");
			for (int i = 0; i < foundFiles.length; i++)
			{
				String[] xmls = readAndFormXML (foundFiles[i].toString());
				for(String n: xmls)
				{
					if (arguments.contains("PlainXpath"))
					{
						List<String> xpaths_to_form = xpaths (path+"\\PlainXpath");
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
						List<String> xpaths_to_form = xpaths (path+"\\SourceXpath");
						if (print_xpath_source==0)
						{
							print("Source xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithSourceXpath(n,xpaths_to_form,listWithJsonsObjects);
						print("SourceXpath finished");
						print_xpath_source++;
					}

					if (arguments.contains("LogicXpath"))
					{
						List<String> xpaths_to_form = xpaths (path+"\\LogicXpath");
						if (print_xpath_logic==0)
						{
							print("Logic xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithLogicXpath(n,xpaths_to_form,listWithJsonsObjects);
						print("LogicXpath finished");
						print_xpath_logic++;
					}

					if (arguments.contains("StaticXpath"))
					{
						List<String> xpaths_to_form = xpaths (path+"\\StaticXpath");
						if (print_xpath_logic==0)
						{
							print("Static xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithStaticXpath(n,xpaths_to_form,listWithJsonsObjects);
						print("StaticXpath finished");
						print_xpath_logic++;
					}

					if (arguments.contains("ProductEnrichmentXpath"))
					{
						List<String> xpaths_to_form = xpaths (path+"\\ProductEnrichmentXpath");
						if (print_xpath_logic==0)
						{
							print("EnrichmentXpath xpaths to check for each TADS:");
							printList(xpaths_to_form);
						}
						checkXMLWithProductEnrichmentXpath(n,xpaths_to_form,listWithJsonsObjects);
						print("EnrichmentXpath finished");
						print_xpath_logic++;
					}

				}
				print("TADS finished - " + foundFiles[i].toString());
			}
			if (foundFiles.length==0)
				print("TADS verification fails - no TADS to verify or correct JSONs");
		}
		catch (NullPointerException e)
		{
			print("TADS verification fails - no TADS to verify or correct JSONs");
		}
		return output;
	}

	@Override
	void checkXMLWithSourceXpath(String filePath,List<String> xpaths_with_static, ArrayList<?> arraylist) {
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(filePath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlAccountableId = xpathEvaluate(expr,doc);
		PnLReport jsonObject = new PnLReport(); 
/* !!! old version  - works for json with 1 trade
		for (Object object_map : arraylist) {
			jsonObject = (PnLReport) object_map;		    	
			String jsonAccountableId = formPnLAccountableID(jsonObject,0);
			if (xmlAccountableId.equals(jsonAccountableId))
			{
				print("jsonAccountableId: "+jsonAccountableId);
				List<String> xpaths_dynamic = formXpathsWithSourceReference(xpaths_with_static,jsonObject);
				xmlXpathValidation (filePath, xpaths_dynamic);
				RICmarker = true;
			}
		}
*/		
		String jsonAccountableId = "";
		int trade_num= 0;
		for (Object object_map : arraylist) {
			jsonObject = (PnLReport) object_map;
			Common.print("Current json: " + jsonObject.getTraderBook().get(0).getTraderBookCode());
			for (Object trade : jsonObject.getTraderBook().get(0).getTrades()) {
				jsonAccountableId = formPnLAccountableID(jsonObject,trade_num);
				if (xmlAccountableId.equals(jsonAccountableId))
				{
					print("jsonAccountableId used in checkXMLWithSourceXpath: "+jsonAccountableId);
					List<String> xpaths_dynamic = formXpathsWithSourceReference(xpaths_with_static,jsonObject,trade_num);
					xmlXpathValidation (filePath, xpaths_dynamic);
					RICmarker = true;
					break;
				}
				trade_num++;
			}
			trade_num=0;
		}

		if (!RICmarker)	print("SourceXpath fail: no source json record suits TADS for " + xmlAccountableId);
	}

	@Override
	void checkXMLWithLogicXpath(String xml, List<String> xpaths, ArrayList<?> listWithJsonsObjects) {
		String finalXpath = null;
		for (String oneXpath : xpaths)
		{
			if (oneXpath.contains("<source>"))
			{
				//find and substitute source value
				String[] static_xpath_array = StringUtils.substringsBetween(oneXpath,"<source>","</source>");
				List<String> sourcesList = Arrays.asList(static_xpath_array); 
				List<String> sourceValue_arr = new ArrayList<String>();

				for (String everySource : static_xpath_array)
				{
					ArrayList<String> conversion = new ArrayList<String>(Arrays.asList(everySource.split("\\.")));
					switch (conversion.get(0))
					{
					case "Report": 
						for (Object object_map : listWithJsonsObjects) 
						{
							sourceValue_arr.add(getCorrespondedObject("PnLReport",conversion.get(1),(PnLReport) object_map,0));
						}
						break;
					case "TraderBook":  
						break;
					case "Trades":  
						for (Object object_map : listWithJsonsObjects) 
						{
							sourceValue_arr.add(getCorrespondedObject("Trades",conversion.get(1),(PnLReport) object_map,0));
						}
						break;
					case "TradeLegs":  
						break;
					case "Measures":  
						break;
					}
				}

				try
				{
					finalXpath = replaceData(oneXpath,sourcesList,sourceValue_arr,"<source>","</source>");
					Common.print("static_xpath: " + finalXpath);
				}
				catch (NullPointerException e)
				{
					print ("LogicXpath fail: no source record suits TADS");
				}
			}
			else
				finalXpath = oneXpath;
			logicXpathEvaluator (xml, finalXpath);
		}

	}

//!!!! remove code copy-paste	
	static void logicXpathEvaluator (String xml, String finalXpath)
	{
		String logicValue = takeLogicXpathValue(xml, finalXpath);
		if (!takeLogicXpathValue(xml, finalXpath).equals("true")) 
			print("Logic fails: "+ finalXpath + " with result " + logicValue);
	}
	
	static String takeLogicXpathValue (String xml, String finalXpath)
	{
		String value = null;
		try {
			Processor proc = new Processor(false);
			XPathCompiler xpath = proc.newXPathCompiler();
			net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
			StringReader reader = new StringReader(xml);
			XdmNode docXdmNode = builder.build(new StreamSource(reader));
			print("logic Xpath : " + finalXpath);
			XPathSelector selector = xpath.compile(finalXpath).load();
			selector.setContextItem(docXdmNode);
			XdmValue logicXpathValue = selector.evaluate();
			value = logicXpathValue.toString();
			Common.print("logicXpathValue: " + logicXpathValue);
		} catch (Exception e) {
			print("error: "+ e.getLocalizedMessage());
		}
		return value;
	}
	
	@Override
	void checkXMLWithStaticXpath(String TADSpath, List<String> xpaths_with_static_values, ArrayList<?> arraylist) {
		/*
		static                                   json
		TRADER_BOOK_NAME = TraderBookCode
		TRADER_BK_SRC_SYS = TraderBookSourceSystemName

		hadoop fs -ls /user/tst_fimta/uat3/equities/1.2.15/app/refdata/999 - take reference data
		hadoop fs -ls /user/tst_fimta/uat3/equities/1.2.15/app/mappings - MAP_ISSUER
		 */		
		//BOOK[BOOK_NAME[text()="Report.TraderBookCode"] and TRADER_BK_SRC_SYS[text()="Report.TraderBookSourceSystemName"]]/BOOK_TYPE/text()
		//curl --insecure "https://dap0088j.uk.db.com:25872/dbpost_StaticData_webServices/staticdata/service/search/BOOK?param=BMOPTMSN&param=RMS"
		//<SOURCE>BRDS</SOURCE> or not BRDS

		String static_folder = path.substring(0,path.substring(0,path.lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static";
		
		String static_file = static_folder +"\\"+staticSource;

		print ("Static data folder: " + static_folder);
		print ("Static data file: " + static_file);


		List<String> xpaths_static_after_static_values_replace = null;
		String key = null;
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(TADSpath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlAccountableId = xpathEvaluate(expr,doc);
		PnLReport jsonObject = new PnLReport(); 
		for (Object object_map : arraylist) {
			jsonObject = (PnLReport) object_map;		    	
			String jsonAccountableId = formPnLAccountableID(jsonObject,0);
			if (xmlAccountableId.equals(jsonAccountableId))
			{
				print("jsonAccountableId: "+jsonAccountableId);
				xpaths_static_after_static_values_replace = formListOfXpathWithStaticValues (TADSpath, xpaths_with_static_values, static_file, jsonObject);
				Common.print(xpaths_static_after_static_values_replace);
				RICmarker = true;
				xmlXpathValidation (TADSpath, xpaths_static_after_static_values_replace);
			}
		}
		if (!RICmarker)
			print("fail: no JSON source record suits TADS");
	}

	void checkXMLWithProductEnrichmentXpath(String TADSpath, List<String> xpaths_with_static_values, ArrayList<?> arraylist) {
		XPathExpression expr = null;
		Boolean RICmarker = false;
		Document doc = formXMLdocument(TADSpath);
		XPath xpath = createXpath();
		expr = xpathCompile(xpath,IDxpath);
		String xmlAccountableId = xpathEvaluate(expr,doc);
		PnLReport jsonObject = new PnLReport(); 
		Integer trade_num = 0;
		for (Object object_map : arraylist) {
			jsonObject = (PnLReport) object_map;
			for (Object trade : jsonObject.getTraderBook().get(0).getTrades()) {
				String jsonAccountableId = formPnLAccountableID(jsonObject,trade_num);				
				if (xmlAccountableId.equals(jsonAccountableId))
				{
					print("jsonAccountableId: "+jsonAccountableId);
					List<String> xpaths_dynamic = formXpathsWithProductEnrich(xpaths_with_static_values,jsonObject, trade_num);
					xmlXpathValidation (TADSpath, xpaths_dynamic);
					RICmarker = true;
				}
				trade_num++;
			}
		}
		if (!RICmarker)
			print("Enrichment fail: no csv record suits TADS");
	}

	public static List<String> formXpathsWithProductEnrich (List<String> lines, PnLReport pnlObject, Integer trade_num){	
		List<String> dynamic_xpaths = new ArrayList<String>();
		String paramCombination = "";
		for (int i=0; i<lines.size(); i++)
		{
			ArrayList<String> arrayWithXpaths = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
			ArrayList<String> productConversion = new ArrayList<String>(Arrays.asList(arrayWithXpaths.get(1).split("-")));
			for (String productConversionItem : productConversion)
			{
				ArrayList<String> conversion = new ArrayList<String>(Arrays.asList(productConversionItem.split("\\.")));
//				Common.print("conversion: " + conversion);
				switch (conversion.get(0))
				{
				case "Report":  paramCombination += getCorrespondedObject("PnLReport",conversion.get(1),pnlObject,trade_num);
				break;
				case "TraderBook":  paramCombination += getCorrespondedObject("TraderBook",conversion.get(1),pnlObject,trade_num);
				break;
				case "Trades":  paramCombination += getCorrespondedObject("Trades",conversion.get(1),pnlObject,trade_num);
				break;
				case "TradeLegs":  paramCombination += getCorrespondedObject("TradeLegs",conversion.get(1),pnlObject,trade_num);
				break;
				case "Measures":  paramCombination += getCorrespondedObject("Measures",conversion.get(1),pnlObject,trade_num);
				break;
				}
			}
				Common.print("paramCombination: " + paramCombination);
				String conver = getCorrespondedProduct(paramCombination);
				Common.print("conver: " + conver);
				if ((!conver.isEmpty()) && !(paramCombination.equals("FixedCashflows") || paramCombination.equals("FxSpot") || paramCombination.equals("FxForward")))
					{
					dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedProduct(paramCombination));
//					Common.print("dynamic_xpaths before break: " + dynamic_xpaths);
					break;
					}
//			}
				
//			dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedProduct(paramCombination));
//			Common.print("dynamic_xpaths: " + dynamic_xpaths);
		}
		Common.print("dynamic_xpaths: " + dynamic_xpaths);
		return dynamic_xpaths;
	}

	public static String formListOfXpathValuesFromStaticSourceForFiltering (String xpaths_with_static_values, String pathToStaticData, PnLReport pnlObject)
	{
		String foundStaticValue = null;
		props.put("TRADERBOOKCODE", getCorrespondedObject("TraderBook","TraderBookCode",pnlObject,0));
		props.put("TRADERBOOKSOURCESYSTEMNAME", getCorrespondedObject("TraderBook","TraderBookSourceSystemName",pnlObject,0));
		props.put("TraderBook.TraderBookCode", getCorrespondedObject("TraderBook","TraderBookCode",pnlObject,0));
		props.put("TraderBook.TraderBookSourceSystemName", getCorrespondedObject("TraderBook","TraderBookSourceSystemName",pnlObject,0));
		//		Common.print("Filtration with static xpath: " + xpaths_with_static_values);

		String xpath = substitute(xpaths_with_static_values);
		print("Filtration with static xpath: " + xpath);

		//----added
		String staticBook = getStaticRecordFromSericeRequest();
		Common.print("staticBook from formListOfXpathWithStaticValues: " + staticBook);
		if (staticBook.contains("exit-status: 0"))
		{
			XPathExpression expr = null;
			Document doc = formXMLdocument(StringUtils.substringBetween(staticBook, "<RD_BOOK>", "</RD_BOOK>"));
			XPath xpath1 = createXpath();
			Common.print("xpathAfterReplacement: " + xpath);
			expr = xpathCompile(xpath1,xpath);
			foundStaticValue = xpathEvaluate(expr,doc);
		}
		//---ended
		else
		{
			List<String> static_data = getStaticXMLvalue (pathToStaticData, new ArrayList<String>(Arrays.asList(xpath)));
			try
			{
				foundStaticValue = static_data.get(0);
				print("Formed xpaths with static sources: " + static_data.get(0));
			}
			catch (IndexOutOfBoundsException e)
			{
			}
		}
		return foundStaticValue;
	}

	public static List<String> formListOfXpathWithStaticValues (String TADSpath, List<String> xpaths_with_static_values, String pathToStaticData, PnLReport pnlObject)
	{
		List<String> dynamic_xpaths = new ArrayList();
		//new way with service usage		
		props.put("TRADERBOOKCODE", getCorrespondedObject("TraderBook","TraderBookCode",pnlObject,0));
		props.put("TRADERBOOKSOURCESYSTEMNAME", getCorrespondedObject("TraderBook","TraderBookSourceSystemName",pnlObject,0));
		props.put("TraderBook.TraderBookCode", getCorrespondedObject("TraderBook","TraderBookCode",pnlObject,0));
		props.put("TraderBook.TraderBookSourceSystemName", getCorrespondedObject("TraderBook","TraderBookSourceSystemName",pnlObject,0));

		String staticBook = getStaticRecordFromSericeRequest();
		Common.print("staticBook from formListOfXpathWithStaticValues: " + staticBook);
		if (staticBook.contains("exit-status: 0"))
		{
			String bookXML = formXMLfromResponse(staticBook);

			XPathExpression expr = null;
			Document doc = formXMLdocument(StringUtils.substringBetween(staticBook, "<RD_BOOK>", "</RD_BOOK>"));
			XPath xpath1 = createXpath();
//			expr = xpathCompile(xpath1,"//BOOK[TRADER_BOOK_NAME[text()=\"ARMFFCOR\"] and TRADER_BK_SRC_SYS[text()=\"RMS\"]]/BOOK_TYPE/text()");//"//BOOK/BOOK_ID/text()");
//			String xmlAccountableId = xpathEvaluate(expr,doc);
//			Common.print("static: " + xmlAccountableId);
			Boolean isContainFunction = false; 
			for (String xpath : xpaths_with_static_values)
			{
				ArrayList<String> arrayWithXpaths = new ArrayList<String>(); 
				if (xpath.contains(";"))
				{
					arrayWithXpaths = new ArrayList<String>(Arrays.asList(xpath.split(";")));
					isContainFunction = true;
				}
				else
					arrayWithXpaths = new ArrayList<String>(Arrays.asList(xpath.split(",")));
//				Common.print("isContainFunction: " + isContainFunction);
					
				String TADSXpath = arrayWithXpaths.get(0);
				String staticXpath = arrayWithXpaths.get(1);
				String xpathAfterReplacement = substitute(staticXpath);
				Common.print("xpathAfterReplacement: " + xpathAfterReplacement);
				if (xpathAfterReplacement.contains("static"))
				{
					String[] allXpathsForStatic = StringUtils.substringsBetween(xpathAfterReplacement, "<static>", "</static>");
					for (String eachStaticXpath : allXpathsForStatic)
						{
						expr = xpathCompile(xpath1,eachStaticXpath);
						String valueFromStatic = xpathEvaluate(expr,doc);
//						Common.print("valueFromStatic: "+valueFromStatic);
						xpathAfterReplacement = xpathAfterReplacement.replace("<static>"+eachStaticXpath+"</static>", valueFromStatic);
//						Common.print("xpathAfterReplacement: " + xpathAfterReplacement);
						}
//					Common.print(takeLogicXpathValue (TADSpath, xpathAfterReplacement));
					if (isContainFunction)
						dynamic_xpaths.add(TADSXpath+";"+takeLogicXpathValue (TADSpath, xpathAfterReplacement));
					else
						dynamic_xpaths.add(TADSXpath+","+takeLogicXpathValue (TADSpath, xpathAfterReplacement));
				}
				else
				{
				expr = xpathCompile(xpath1,xpathAfterReplacement);
				String middleValue = xpathEvaluate(expr,doc);
				
//				middleValue = TADSXpath.contains("legal_entity/id/text()")&(middleValue.equals("0840")) ? "0"+middleValue : middleValue;
				dynamic_xpaths.add(TADSXpath+"," + middleValue);
				}
			}
			// end of new way
		}
		else
		{
			//use old way
			String temp = null;
			for (String xpath : xpaths_with_static_values)
			{
				ArrayList<String> arrayWithXpaths = new ArrayList<String>(Arrays.asList(xpath.split(",")));
				ArrayList<String> jsonFields = new ArrayList<String>(Arrays.asList(StringUtils.substringsBetween(arrayWithXpaths.get(1), "=\"", "\"]")));
				ArrayList<String> jsonValues = new ArrayList<String>();

				for (String eachJsonField : jsonFields)
				{
					ArrayList<String> conversion = new ArrayList<String>(Arrays.asList(eachJsonField.split("\\.")));
					switch (conversion.get(0))
					{
					case "Report":  jsonValues.add(getCorrespondedObject("PnLReport",conversion.get(1),pnlObject,0));
					break;
					case "TraderBook":  jsonValues.add(getCorrespondedObject("TraderBook",conversion.get(1),pnlObject,0));
					break;
					case "Trades":  jsonValues.add(getCorrespondedObject("Trades",conversion.get(1),pnlObject,0));
					break;
					case "TradeLegs":  jsonValues.add(getCorrespondedObject("TradeLegs",conversion.get(1),pnlObject,0));
					break;
					case "Measures":  jsonValues.add(getCorrespondedObject("Measures",conversion.get(1),pnlObject,0));
					break;
					}
				}

				int i=0;
				temp = arrayWithXpaths.get(1);
				for (String eachJsonField : jsonFields)
				{
					try{
						temp = temp.replace(eachJsonField, jsonValues.get(i));
						i++;
					}
					catch (IndexOutOfBoundsException e)
					{
						break;
					}
				}
				Common.print("temp: " + temp);
				List<String> static_data = getStaticXMLvalue (pathToStaticData, new ArrayList<String>(Arrays.asList(temp)));
				try{
					dynamic_xpaths.add(arrayWithXpaths.get(0)+","+static_data.get(0));
				}
				catch (IndexOutOfBoundsException e)
				{
					dynamic_xpaths.add(arrayWithXpaths.get(0)+",ZERO_STATIC_VALUE");
				}
			}
			print("Formed xpaths with static sources" + dynamic_xpaths);
			printList(dynamic_xpaths);
		}
		return dynamic_xpaths;
	}

	public static String formXMLfromResponse(String str)
	{
		return str;
	}

	public static String formPnLAccountableID (PnLReport PnLRecord, Integer tradeNum)
	{
		//owner_book/front_office_prod_type/ctpy_id/trade_id/currency
		//TraderBook.TraderBookCode/TradeLegs.RiskProductType/Trades.PartyId/Trades.TradeId/Measures.CurrencyCode
//		Common.print("tradeNum: " + tradeNum);
		
		String AccountableID = "/" + PnLRecord.getTraderBook().get(0).getTraderBookCode()
				+"/"
				+PnLRecord.getTraderBook().get(0).getTrades().get(tradeNum).getTradeLegs().get(0).getRiskProductType()
				+"/"
				+PnLRecord.getTraderBook().get(0).getTrades().get(tradeNum).getPartyId()
				+"/"
				+PnLRecord.getTraderBook().get(0).getTrades().get(tradeNum).getTradeId()
				+"/"
				+PnLRecord.getTraderBook().get(0).getTrades().get(tradeNum).getTradeLegs().get(0).getMeasures().get(0).getCurrencyCode();
//		print("Formed Accountable ID: " + AccountableID);
		return AccountableID;
	}

	public static String replaceData(String static_xpath, List<String> static_xpath_array, List<String> sourceValue_arr, String left, String right)
	{
		for (int ii = 0; ii<static_xpath_array.size(); ii++)
		{
			static_xpath = static_xpath.replace(left+static_xpath_array.get(ii)+right, sourceValue_arr.get(ii).replaceAll("[0]*$", "").replaceAll("\\.$", ""));
		}
		return static_xpath;
	}

	public static List<String> formXpathsWithSourceReference (List<String> lines, PnLReport pnlObject, Integer tradeNum)
	{
		List<String> dynamic_xpaths = new ArrayList();
		for (int i=0; i<lines.size(); i++)
		{
			ArrayList<String> arrayWithXpaths = new ArrayList<String>(Arrays.asList(lines.get(i).split(",")));
			if(arrayWithXpaths.get(1).contains("."))
			{
				ArrayList<String> conversion = new ArrayList<String>(Arrays.asList(arrayWithXpaths.get(1).split("\\.")));
				switch (conversion.get(0))
				{
				case "Report":  dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedObject("PnLReport",conversion.get(1),pnlObject,0));
				break;
				case "TraderBook":  dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedObject("TraderBook",conversion.get(1),pnlObject,0));
				break;
				case "Trades":  dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedObject("Trades",conversion.get(1),pnlObject,tradeNum));
				break;
				case "TradeLegs":  dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedObject("TradeLegs",conversion.get(1),pnlObject,tradeNum));
				break;
				case "Measures":  dynamic_xpaths.add(arrayWithXpaths.get(0)+","+getCorrespondedObject("Measures",conversion.get(1),pnlObject,tradeNum));
				break;
				}
			}	
		}
		return dynamic_xpaths;
	}

	public static String getStaticRecordFromSericeRequest ()
	{
		/*
		 * 1. read request for record
		 * 2. fulfil it with data
		 * 3. establish connection to UNIX
		 * 4. run command
		 * 5. get result as xml piece 
		 * 6.  
		 */
			long duration = Long.parseLong("10000");
			long startTime = System.currentTimeMillis(); //fetch starting time
//			Common.print("startTime: " + startTime + "; duration: " + duration);
//			Common.print("sql_result: " + sql_result);
			String result = "";
			UtilsRemoteHost rh = new UtilsRemoteHost(props.get("HDFS_USER").toString(),props.get("HDFS_PASS").toString(), props.get("HDFS_URL").toString());

			// 2
			String fileName = props.getProperty("PATH")+"Scripts\\getRecordFromRD_BOOKStatic.sh";
			Common.print(fileName);
			String curlForStaticData = TemplateActions.read_file(fileName);
			Common.print(Common.replaceGlobalTags(curlForStaticData, props));
			String command = Common.replaceGlobalTags(curlForStaticData, props); 
			while ((System.currentTimeMillis()-startTime) < duration)
			{
				result = rh.shellExecutorViaShellChannel(command);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!result.contains("exit-status: 0"))
				result = "exit-status: 1";
		Common.print("service request result: " + result);
		return result;
		
	}

	public static String getCorrespondedObject(String className, String objectField, PnLReport object, Integer tradeNum)
	{
		String objectValue = null;
		//		Common.print("objectField: " + objectField + " ::: className :" + className+":::");
		if (className.equals("Trades"))
		{
			Trades obj = object.getTraderBook().get(0).getTrades().get(tradeNum);
			//!!!!!!!!!! CHECK THIS!!!!!
			//			for (Trades obj : object.getTraderBook().get(tradeNum).getTrades())
			objectValue =  getElementValue("Trades",obj,objectField);
		}
		else if (className.equals("TradeLegs"))
		{
			TradeLegs obj = object.getTraderBook().get(0).getTrades().get(tradeNum).getTradeLegs().get(0);
			//!!!!!!!!!! CHECK THIS!!!!!
			//			for (Trades obj : object.getTraderBook().get(tradeNum).getTrades())
			objectValue =  getElementValue("TradeLegs",obj,objectField);
		}
		else if (className.equals("TraderBook"))
		{
			for (TraderBook obj : object.getTraderBook())
				objectValue =  getElementValue("TraderBook",obj,objectField);
		}

		else if (className.equals("Measures"))
		{
			for (Measures obj : object.getTraderBook().get(0).getTrades().get(0).getTradeLegs().get(0).getMeasures())
				objectValue =  getElementValue("Measures",obj,objectField);
		}
		else
			objectValue =  getElementValue(className,object,objectField);
		//	Common.print("objectValue: " + objectValue);

		return convertDate(objectValue, objectField);
	}

	public static String substitute (String withProps)
	{
		for (Object key : props.keySet())
		{
			withProps = withProps.replace(key.toString(), (CharSequence) props.get(key));
		}
		return withProps;
	}

	public static String convertDate(String fullField, String fieldName)
	{
		String formattedField = null;
		if (fieldName.toLowerCase().contains("date"))
		{
			formattedField = fullField.substring(0, 10);
		}
		else 
			if (fieldName.toLowerCase().contains("stamp"))
			{
				formattedField = fullField.substring(0, 10) + "T" + fullField.substring(11);
				Common.print("formattedField: " + formattedField); 
			}
			else return fullField;
		return formattedField;
	}

	public static String getElementValue (String className, Object obj, String objectName) 
	{
		String value = null;
		try {
			Class<?> cls = Class.forName("jsonProcessingForPnL."+className);
			Method method = cls.getDeclaredMethod("get"+ objectName);
			value = (String) method.invoke(obj);
			if (value == null) value = "";
			//			Common.print("got value: " +value);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return value;
	}


	//***************************************************************************************
	@Override
	void checkXMLWithSQL(String filePath, List<String> xpaths_with_static) {
		// TODO Auto-generated method stub

	}

	@Override
	void checkXMLWithCurrency(String filePath, ArrayList<?> source,
			List<String> xpaths_ccy, String CurrencyConvertion) {
		// TODO Auto-generated method stub

	}	

	@Override
	ArrayList<String> formCSVSource() {
		return null;
	}

	private static String applyTransformation (String filterField, PnLReport jsonObject, Integer trade_num)
	{
		String result = "";
		String[] fieldsCombination = filterField.split("-");
		for (String field : fieldsCombination)
		{
			Common.print("field: " + field);
			result += getCorrespondedObject(field.split("\\.")[0],field.split("\\.")[1],jsonObject,trade_num) + "-";
		}
		return result.substring(0, result.length()-1);
	}

	private static String applyComplexTransformation (String filterFileWithPath, PnLReport jsonObject, Integer trade_num)
	{
		String result = "";
		Common.print(filterFileWithPath);
		List<String> filterFileContent = Common.readLines(filterFileWithPath);
		String[] filterFiledsCombination = filterFileContent.get(0).split("%%");

		String static_folder = path.substring(0,path.substring(0,path.lastIndexOf(File.separator)).lastIndexOf(File.separator))+"\\Static";
		String static_file = static_folder +"\\"+staticSource;

		print ("Static data folder: " + static_folder);
		print ("Static data file: " + static_file);

		for (String field : filterFiledsCombination)
		{
			print("Complex filter field: " + field);
			if (field.contains("<stat>"))
			{
				field = StringUtils.substringBetween(field, "<stat>", "</stat>");
				String staticXpathValue = formListOfXpathValuesFromStaticSourceForFiltering(field, static_file, jsonObject );
//				formListOfXpathWithStaticValues
				print("staticXpathValue: " + staticXpathValue);
				result += staticXpathValue;
			}
			else
				result += getCorrespondedObject(field.split("\\.")[0],field.split("\\.")[1],jsonObject,trade_num);
		}
		print("Complex filter field: " + result);
		return result;
	}

	public static List<String> checkFiltering (String[] args, Properties master_props)
	{
		/*
		 * Algorithm:
		 * 1. form JSON class
		 * 2. take trade
		 * 3. from its accountable_id
		 * 4. define condition of filtering
		 * 5. look it into downloaded TADS - SRV_PUB
		 * 6. look into filtered
		 * */
		system="PnL";
		output.clear();
		props = master_props;
		staticSource = args[2];
		String folderWithTADS = args[1];
		String filterField = args[3];
		String exceptionsCheck = "";
		try
		{
			exceptionsCheck = args[4];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			//nothing to do
		}

		String listOfAllowedValues = null;

		Source = formFidessaSource(new File (args[0]));
//		print("Source file: " + Source);
		path = formPath(new File (args[0]));
		print("Source file path: " + path);
		formJsonSource Json = new formJsonSource (path);
		print("folder with TADS files: " + folderWithTADS);
		ArrayList<PnLReport> listWithJsonsObjects = Json.formReportsObjectsFromJson();
		
		master_props.put("path", path);

		print("Filtering to perform: " + filterField);
		String filterFilePath = filterField.contains("NOT") ? filterField.substring(3) : filterField;
		listOfAllowedValues = Common.readFileToString(path+"//" + filterFilePath);
		print("listOfAllowedValues: " + listOfAllowedValues);
		print("listWithJsonsObjects contains " + listWithJsonsObjects.get(0).getTraderBook().get(0).getTrades().size() + " trades");

		PnLReport jsonObject = new PnLReport(); 
		Integer trade_num = 0;
		Boolean filterOrNot = filterField.startsWith("NOT") ? true : false;

		filterField = filterField.startsWith("NOT") ? filterField.substring(3): filterField; 

		for (Object object_map : listWithJsonsObjects) {
			jsonObject = (PnLReport) object_map;
			for (Object trade : jsonObject.getTraderBook().get(0).getTrades()) {
				String jsonAccountableId = formPnLAccountableID(jsonObject,trade_num);
				String jsonFilterFieldValue = null;
				if (filterField.contains("Static"))
					jsonFilterFieldValue = applyComplexTransformation (path+"//"+filterField,jsonObject,trade_num);
				else if (filterField.contains("-"))
					jsonFilterFieldValue = applyTransformation (filterField,jsonObject,trade_num);
				else
					if (filterField.contains("."))
						jsonFilterFieldValue = getCorrespondedObject(filterField.split("\\.")[0],filterField.split("\\.")[1],jsonObject,trade_num);
					else
						jsonFilterFieldValue = getCorrespondedObject("PnLReport",filterField,jsonObject,trade_num);

				print("jsonFilterFieldValue: " + jsonFilterFieldValue);
				EquityDSFilteringClass.FilteringInit(args[0], folderWithTADS);
//				output.addAll(ReturnResult());
				if (listOfAllowedValues.contains(jsonFilterFieldValue) && !(filterOrNot ) && !(jsonFilterFieldValue.length()==0))
				{
					/*
					 * Looking the trade in SRV_VAL 
					 */
					EquityDSFilteringClass.findRecord(jsonAccountableId, "Y");
					output.addAll(EquityDSFilteringClass.FilteringReturnResult());
				}
				else
				{
					/*
					 * Looking the trade in SRV_VAL and then in other folders 
					 */
					EquityDSFilteringClass.findRecord(jsonAccountableId, "N");
					output.addAll(EquityDSFilteringClass.FilteringReturnResult());
					if (exceptionsCheck.contains("checkFilterReason"))
					{
						print("exceptionsCheck: " + exceptionsCheck);
						output.addAll(checkFilterReason(jsonAccountableId,folderWithTADS,exceptionsCheck));
					}
				}
				trade_num++;
				EquityDSFilteringClass.clearingOutput();
			}
			trade_num=0;
		}
		//		Common.print(output);
		return output;
	}

	static List<String> checkFilterReason(String jsonAccountableId, String folderWithTADS, String exceptionsCheck)
	{
		/*
		 * 1a. read reason from test folder into lines 
		 * 1b.find xml in SRV_FILTER/disc folder
		 * 2. verify reason
		 */

		//		String SRV_FILTER_disc = folderWithTADS+"\\SRV_FILTER\\discarded"; 
		List<String> output = new ArrayList<String>();
		String fileWithPathToFilterReason = path + "\\" + exceptionsCheck;
		String s = exceptionsCheck.substring("checkFilterReason".length());//"FILTERdisc";
//		Common.print("folder name: " + s);
		String TADS = EquityDSFilteringClass.findTADSFile(s, jsonAccountableId,".*auto");
		if (TADS.length()>0)
		{
			output.add("true - expected filtered out file is found in specifies folder" + eol);
			List<String> filterEvidences = Common.readLines(fileWithPathToFilterReason);
			Common.print("filterEvidences: " + filterEvidences);
			Boolean marker = true;
			output.add("Verification of filter section in TADS message:" + eol);
			for (String ss : filterEvidences)
			{
				if (TADS.contains(ss))
					output.add("true - there is filter line " + ss + eol);
				else
					output.add("fail - there is no expected filter line " + ss + eol);
			}
			Common.print(output);
		}
		else
			output.add("fail - expected filtered out file is not found for futher processing in " + s + eol);
			
		return output;
	}
}