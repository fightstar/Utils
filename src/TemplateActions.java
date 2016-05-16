

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


public class TemplateActions {
	public static String eol = System.getProperty("line.separator");  

	
	public static String copyFileForUpload (String file_to_copy, String folder_to_copy)
	{
		List<String> places = Arrays.asList("cmd.exe","/c","echo","F","|","xcopy","/Y",file_to_copy, folder_to_copy);
		String result_details = UtilsBatch.BatchExecutorCommand(places);
		Common.print("result_details: "+result_details);
	return result_details;
	}
	
	public static String runBatchFile (String file_to_execute,String parameters) throws InterruptedException
	{
		String result_details = "";
		List<String> params = Arrays.asList(parameters.split(" "));
//		Common.print("runBatchFile params: "+params);
		result_details = UtilsBatch.BatchExecutorFromFile(file_to_execute,params);
//		Common.print("runBatchFile result_details: "+result_details);
	return result_details;
	}

	public static String waitProcessing (String query, Properties props) 
	{
		long duration = Long.parseLong(props.getProperty("TIMEOUT"));
		long startTime = System.currentTimeMillis(); //fetch starting time
//		Common.print("startTime: " + startTime + "; duration: " + duration);
		String sql_result = UtilsSQL.getSQLvalue(query);
//		Common.print("sql_result: " + sql_result);
		while (!(sql_result.equals("true") || sql_result.contains("false") || sql_result.contains("FAILED") || (System.currentTimeMillis()-startTime) > duration))
		{
			Common.print("sql_result: " + sql_result);
			sql_result = UtilsSQL.getSQLvalue(query);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return (sql_result.equals("true")) ? "PASS" : "FAIL: "+sql_result; 
	}

	public static String waitProcessingPnL (String query, Properties props) 
	{
		long duration = Long.parseLong(props.getProperty("TIMEOUT"));
		long startTime = System.currentTimeMillis(); //fetch starting time
//		Common.print("startTime: " + startTime + "; duration: " + duration);
		String sql_result = UtilsSQL.getSQLvalue(query);
//		Common.print("sql_result: " + sql_result);
		while (!(sql_result.contains("true:SRV_FILPUB") || sql_result.contains("FAILED")))
		{
			if ((System.currentTimeMillis()-startTime) > duration)
				sql_result += "FAILED : time is up";
			else
			{
			Common.print("sql_result: " + sql_result);
			sql_result = UtilsSQL.getSQLvalue(query);
                Common.print("sql_result: " + sql_result);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
		}
		return (/*sql_result.contains("false") ||*/ sql_result.contains("FAILED")) ? "FAIL: " + sql_result : "PASS"  + sql_result; 
	}

	public static String waitProcessingWithCustomConnection (UtilsOracleDB connectionToDS, String query, Properties props) 
	{
		long duration = Long.parseLong(props.getProperty("TIMEOUT"));
		long startTime = System.currentTimeMillis(); //fetch starting time
		String sql_result = connectionToDS.getSQLvalue(query);
		Common.print("waitProcessingResult: " + query);
		while (!(sql_result.contains("true:SRV_FILPUB") || sql_result.contains("FAILED")))
		{
			if ((System.currentTimeMillis()-startTime) > duration)
				sql_result += "FAILED : time is up";
			else
			{
			Common.print("sql_result: " + sql_result);
			sql_result = connectionToDS.getSQLvalue(query);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
		}
		return (sql_result.contains("FAILED")) ? "FAIL: " + sql_result : "PASS"  + sql_result; 
	}

	
	public static String getQueryResult (String query) 
	{
		return UtilsSQL.getSQLvalue(query); 
	}

	public static String deleteFileForUpload (String file_to_delete)
	{
		List<String> command = Arrays.asList("cmd.exe","/c","del",file_to_delete);
		String result_details = UtilsBatch.BatchExecutorCommand(command);
		Common.print("result_details: "+result_details);
	return result_details;
	}

	public static String run_sql_file(String bat_file) 
			{
		String result = null;
		return result;
			}
			
	public static String take_xpath_value(String xml, String xpath) 
		{
		String value = null;
if (xpath.contains("else"))
	value =  UtilsXML.takeLogicXpath(xml, xpath);
else
	try {
				value =  UtilsXML.takeXmlValueByXpath(xml, xpath);
			} catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException e) {
				e.printStackTrace();
			}
			return value;
		}

	public static void set_global(HashMap<String, String> globalVariable, String globName, String globValue) 
		{
			globalVariable.put(globName, globValue);
		}

	public static String read_file(String file) 
		{
			return Common.readFileToString (file);
		}

	public static String substitute(String text, HashMap<String, String> globalVariable) 
		{
		return Common.replaceGlobalTags (text, globalVariable);
		}

	public static String run_sql_command(String query) throws SQLException 
		{
			return UtilsSQL.executeSQLStatement(query);//getSQLvalue(query, props);
		}

	public static String run_bat(String shell_command, String username,String password, String hostname) 
		{
		String result = null;
		Common.print("shell_command: " + shell_command);
		UtilsRemoteHost rh = new UtilsRemoteHost(username,password, hostname);
		result = rh.shellExecutorViaShellChannel(shell_command);
		rh.closeSession();
	return result;
		}

	public static String run_bat_silent(String shell_command, String username,String password, String hostname) 
	{
	String result = null;
	Common.print("shell_command: " + shell_command);
	UtilsRemoteHost rh = new UtilsRemoteHost(username,password, hostname);
	result = rh.shellExecutorSilentMode(shell_command);
	rh.closeSession();
return result;
	}

	public static String run_bat_old(String bat_file) 
	{
	String result = null;
//	startAdaptors.bat acsd2.uk.db.com dbpost ACS10dev EQ INT3
	List<String> params = Arrays.asList(bat_file.split(" "));
	String file_to_execute = params.get(0);
	String parameters = "";
	for (int i = 1; i < params.size(); i ++)
		parameters += params.get(i) + " ";
	result = UtilsBatch.BatchExecutorFromFile(file_to_execute,Arrays.asList(parameters.split(" ")));
//	Common.print("result_details: "+result_details);

return result;
	}

	public static String runQueryForTrue (String query)
	{
		String sql_result = "Query: " + query + eol;
		Common.print(query);
		sql_result += "Result:" + UtilsSQL.getSQLvalue(query);
		Common.print(sql_result);
		return sql_result;
	}

	public static String runQueryVsXpath (String TADS, String temp)
	{

		ArrayList<String> checksList = Common.splitBy(temp,";");
//		Common.print(checksList);
		ArrayList<String> result = new ArrayList<String>();
		int i = 0;
		for (String s : checksList)
		{
			ArrayList<String> checksSpec = Common.splitBy(s,"&&");
//			Common.print(checksSpec);
			String query = checksSpec.get(0);
//			Common.print("query: " + query);
			String xpath = checksSpec.get(1);

			String queryValue = getQueryResult(query.replace("&", "'||'&'||'"));
			queryValue = queryValue.equals("null")?"":queryValue;
			String xpathValue = take_xpath_value(TADS, xpath);
			xpathValue = xpathValue.contains("XdmEmptySequence")?"":xpathValue; 
//			Common.print("Comparison result: " + queryValue.equals(xpathValue) + " ::: queryValue: " +queryValue+ "xpathValue: " + xpathValue);
			result.add(eol + "Query: " + query + eol +
					"Xpath: " + xpath + eol +
					"Result:" + queryValue.equals(xpathValue) + " ::: queryValue: " +queryValue+ "; xpathValue: " + xpathValue);
//			Common.print(result.get(i));
			i++;
		}
		return result.toString();
	}

	public static String updateReport (Report rep, String step, String result)
	{
		String res =  null;
		Common.print(step);		
		if (!step.equals(""))
		{
		if (result.contains("Result:false")||result.equals("")||result.equals("Result:")||result.contains("FAILED"))
			res="FAIL";
		else
			if (!result.contains("Result:true"))
				res="FAIL";
			else if(result.contains("PASS")){
				res = "PASS";
			}
			else
				res="PASS";
		rep.add(Arrays.asList(step,res,result));
//		return result.contains("<false>") ? "FAIL" : "PASS";
		}
		return res;
	}

	public static String updateReport (Report rep, String step, String result, String details)
	{
		rep.add(Arrays.asList(step,result,details));
		return result;
	}

// keyword test is the excel file with xlsx extension containing steps
	public static List<List<String>> loadKeywordTest(String testname)
	{
		List<List<String>> content = null;
		Common.print("testname: " + testname);
		try {
			content = UtilsXLS.readXSLX(testname+".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}
//		for (List<String> s : content)	{Common.printList(s);}
		return content;
	}

	public static String getPathOrMask (String path, String basicPath, Boolean path_Or_Mask)
	{
    	String pathFull = basicPath;
    	String regex_mask = null;
    	ArrayList<String> pathForm = Common.splitBy(path,"/");
    	if (path_Or_Mask)
    	{
        	for ( int i = 0; i < pathForm.size()-1; i++)
        		pathFull+=pathForm.get(i)+"/";
        	Common.print("pathFull: " + pathFull);
    		return pathFull;
    	}
    	else
    		{
    		regex_mask = pathForm.get(pathForm.size()-1);
        	Common.print("regex_mask: " + regex_mask);
    		return regex_mask;
    		}
	}
	
	public static String runUndoStatement ()
	{
		String query = "select count(*) RES from fw_in_msg fim where fim.bus_object_event_type=upper('undo')";
		return getQueryResult(query);
	}

	public static String runRevertStatement ()
	{
		String query = "select count(*) RES from fw_in_msg fim where fim.bus_object_event_type=upper('revert')";
		return getQueryResult(query);
	}
	
	public static String publishMessageToQueue (HashMap<String, String> props, String xmlmessage)
	{
		return UtilsJMS.queuePublisher(props , xmlmessage);
	}
	
	public static String publishMessageToTopic (HashMap<String, String> props, String xmlmessage)
	{
		return UtilsJMS.topicPublisher(props , xmlmessage);
	}

	public static void fileProcessingOnHadoop ()
	{
/*
 
 	try	{
		Assert.assertTrue(TemplateActions.copyFileForUpload(BLPath+TestFolder+TestName, ScriptsPath+TestName+"_"+DateFormat.format(date)).contains("1 File(s) copied"));
	status = "PASS";
//	TemplateActions.updateReport(reportFile, "File copying: "+TestName+"_"+DateFormat.format(date), status, "File is ready for hadoop upload"));
    TemplateActions.updateReport(reportFile,"File copying: "+TestName+"_"+DateFormat.format(date), status, "File is ready for hadoop upload");
  		} 
	catch (AssertionError e)
		{	
		status = "FAIL";
		globalStatus = false;
		TemplateActions.updateReport(reportFile, "File copying: "+TestName+"_"+DateFormat.format(date), status, "File is not ready for hadoop upload");
		}
	props.put("PROCFILE", TestName+"_"+DateFormat.format(date));
	
	if (globalStatus) 
	{
		Common.print("File to process: " +props.getProperty("PROCFILE"));//TestName+"_"+DateFormat.format(date));
//		uploadResult = TemplateActions.runBatchFile(ScriptsPath + "upload_to_hadoop.bat",TestName+"_"+DateFormat.format(date)+" "+UploadParams+" "+
//				HDFS_URL+" "+ HDFSappldir+" "+ HDFS_PASS+" "+ HDFS_USER+" "+ DBUSER+"/"+ DBPASS +"@"+ DBSID); 

		uploadResult = uploadToHadoop();
		Common.print("uploadResult: " + uploadResult);
		try	{
			oozie = Common.getByRegex(uploadResult, "job: .*W").get(0).substring("job: ".length());
			Common.print("oozie: " + oozie);
			globalVariables.put("oozie", oozie);
			Assert.assertTrue(oozie.length() != 0);
			TemplateActions.updateReport(reportFile, "Upload to Hadoop", status, "File is uploaded" + eol + uploadResult);
			} 
	catch (AssertionError | NullPointerException e)
		{	
		status = "FAIL";
		globalStatus = false;
		TemplateActions.updateReport(reportFile, "Upload to Hadoop", status, "File is failed to upload or oozie not started" + eol + uploadResult);
		}
	}

	if (globalStatus) 
	{
		query = Common.readFileToString(ScriptsPath + "check_status.sql");
		Common.print("query: " + query);
		query = Common.replaceGlobalTags(query, globalVariables);
		Common.print("query after replacement of global tags: " + query);
//		String waitProcessingResult = TemplateActions.waitProcessing(query, props);
		String waitProcessingResult = TemplateActions.waitProcessingPnL(query, props);
		Common.print("waitProcessingResult: " + waitProcessingResult);
		try	{
			Assert.assertTrue(waitProcessingResult.equals("PASS"));
			TemplateActions.updateReport(reportFile, "Wait for processing", status, "File is processed" + waitProcessingResult);
			}
		catch (AssertionError e)
		{	
			status = "FAIL";
			globalStatus = false;
			TemplateActions.updateReport(reportFile, "Wait for processing", status, "File is not processed" + waitProcessingResult);
		}
	}

// 	globalVariables.put("oozie","0014791-140428234102558-oozie-oozi-W");
//	if (globalStatus) 
//	{

  	query = Common.readFileToString(ScriptsPath + "get_hadoop_folder.sql");
	Common.print("query: " + query);
	query = Common.replaceGlobalTags(query, globalVariables);
	Common.print("query after replacement of global tags: " + query);
	String TADSfolder = TemplateActions.getQueryResult(query);

//	String TADSfolder = "3763";
	Common.print("TADSfolder: " + TADSfolder);
	props.put("TADSFOLDER",TADSfolder);
	String downloadResult = null;
	
	Common.print("TestName: " + TestName);
			try	{
				downloadResult = downloadFromHadoopToLocalViaUnix(props.getProperty("TADSFOLDER"));
				Common.print("downloadResult: " + downloadResult);
				downFolder = Common.getByRegex(downloadResult,"(?<=down)(.*?)(?=down)").get(0);
				Common.print("downFolder: "+downFolder);
				Assert.assertFalse(downloadResult.contains("Result:false"));
				TemplateActions.updateReport(reportFile, "Download from Hadoop", status, "TADS are downloaded" + eol + downloadResult);
				}
			catch (AssertionError|NullPointerException e)
			{	
				status = "FAIL";
				globalStatus = false;
				TemplateActions.updateReport(reportFile, "Download from Hadoop", status, "TADS are not downloaded or folder is not defined" + eol + downloadResult);
			}		
 */
	}

	
	public static String[] formXMLFromString(String xml)
	{
			String[] causes = StringUtils.substringsBetween(xml, 
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
				causes = StringUtils.substringsBetween(xml, 
						"<ta_message", 
						"</ta_message>");
				Common.print("TADS in xml: " + causes.length);
				for(int i=0; i<causes.length; i++)
				{
					causes[i]= "<ta_message" + causes[i]+"</ta_message>";
					//				print(causes[i]);
				}
			}
		return causes;		
	}
//****************************** PnL Accounting
//****************************** Balance Maintenance
//******************************
//******************************
//******************************
	
}

