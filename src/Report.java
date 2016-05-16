

import java.util.Arrays;
import java.util.List;

public class Report {

//	static Logger logger = Logger.getLogger("GLOBAL");
//	static log4jHtmlLayout layout = new log4jHtmlLayout();

	static String name = null;
	static String TestName;
//	TestReport report;
//	TestTA currentTest;
	
	
	public Report (String reportName) {
		
        String nameForHTML = getNameForHtmlReport(reportName);

		name = reportName;
		UtilsXLS.addToXslx(name, "Summary", Arrays.asList("Test Name", "Status", "Duration, min", "Comments"));
		Common.print (name);
		/*logger.info("constructor");
		try {
		FileOutputStream output = new FileOutputStream(nameForHTML+ ".html");
		appender = new WriterAppender(layout,output);

        } catch(Exception e) {}
		logger.addAppender(appender);
		logger.setLevel((Level) Level.INFO);*/

		/*
		// new xml reporting		
		report = new TestReport();
		*/
	
	
	}
	/**
	 * added new list in excel report
	 * @author shevvla
	 * @param SheetName
	 */
	public void addHeader (String SheetName)
	{
		TestName = SheetName;
		UtilsXLS.addToXslx(name, TestName, Arrays.asList(TestName));
		UtilsXLS.addToXslx(name, TestName, Arrays.asList("Test Step", "Status", "Comments"));
		//logger.info(TestName);
		/*
		// new xml reporting		
		currentTest = new TestTA();
		currentTest.setTestName(TestName);
*/
		}

	public void add (List<String> data)
	{
		try{
    		UtilsXLS.addToXslx(name, TestName, data);
		}
		catch (NullPointerException e)
		{
			Common.print("Report was not updated!");
		}
//		logger.info(data);

		/*
		// new xml reporting		
		TestStep newStep = new TestStep();
		formStepInfo(data, newStep);
		currentTest.getTestStep().add(newStep);
*/
		}

	public void addFinal (String status, long startTime)
	{
        long endTime = TimeCounter.getCurrentTime();
//		UtilsXLS.addToXslx(name, "Summary", Arrays.asList(TestName, status));
		UtilsXLS.addToXslxFinal(name, "Summary", Arrays.asList(TestName, status, TimeCounter.getDifference(endTime-startTime)));

		//logger.info("test end");

		/*
		// new xml reporting		
		currentTest.setTestDuration(TimeCounter.getDifference(endTime-startTime));
		currentTest.setTestStatus(status);
		report.getTest().add(currentTest);
//		formHTMLReport();
*/
		}

	/*
	// new xml reporting - wait for betetr times or needs

	void formStepInfo (List<String> data, TestStep newStep)
	{
		newStep.setStepName(data.get(0));
		try
		{
		newStep.setStatus(data.get(1));
		newStep.setComments(data.get(2));
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
		}
	}

	public void formHTMLReport ()
	{
		String stylesheet = "stylesheet/Report.xsl";
        String xml = "stylesheet/"+TestName+".xml";
        String reportHTML = "Reports/html/"+TestName+".html";
        UtilsHTML.formResultXML(xml, report);
//		UtilsHTML.TransInfoHtml(stylesheet, xml, reportHTML);
//		report.getTest().clear();
//		currentTest.getTestStep().clear();
	}
	*/
    public String getNameForHtmlReport(String name) {
        if (name.length() > 0 && name.charAt(name.length() - 1) == 'x') {
            name = name.substring(0, name.length() - 5);
        }
        return name;
    }
}
