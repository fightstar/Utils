

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
 

public class UtilsHTML {

	public static void TransInfoHtml( String stylesheet, String xml, String reportHTML)
		{
		try {
		        TransformerFactory tFactory=TransformerFactory.newInstance();
//		        stylesheet = "stylesheet/Report.xsl";
//		        xml = "stylesheet/file.xml";
//		        reportHTML = "E:/res.html";
		        Source xslDoc=new StreamSource(stylesheet);
		        Source xmlDoc=new StreamSource(xml);
		        Transformer trasform=tFactory.newTransformer(xslDoc);
		        StreamResult sr = new StreamResult(new File(reportHTML));
		        trasform.transform(xmlDoc, sr);
		        
		    } 
		    catch (TransformerFactoryConfigurationError | TransformerException e) 
		    {
		    	Common.print("XML to HTML transformation failed");
		        e.printStackTrace();
		    }
		}
		
	public static void formResultXML(String xmlFileForResult, TestReport report)
	{
		try {
			File file = new File(xmlFileForResult);
			JAXBContext jaxbContext = JAXBContext.newInstance(TestReport.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(report, file);
		      } catch (JAXBException e) {
			e.printStackTrace();
		      }
	}
	/*
	public static void main(String[] args) {
		TestTA newTest = new TestTA();
		TestStep newStep = new TestStep();
		TestReport report = new TestReport();
		
		newStep.setStepName("1");
		newStep.setStatus("2");
		newStep.setComments("3");
		
		newTest.setTestName("Test 1");
		newTest.setTestDuration("5");
		newTest.getTestStep().add(newStep);
		
		newStep.setStepName("1");
		newStep.setStatus("1");
		newStep.setComments("1");
		newTest.getTestStep().add(newStep);
		report.getTest().add(newTest);

		try {
			 
			File file = new File("stylesheet/file.xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(TestReport.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(report, file);
		      } catch (JAXBException e) {
			e.printStackTrace();
		      }
        String stylesheet = "stylesheet/Report.xsl";
        String xml = "stylesheet/file.xml";
        String reportHTML = "Reports/html/res.html";

		TransInfoHtml(stylesheet, 
		        xml, 
		        reportHTML
				);
	}
	*/
}