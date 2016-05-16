

import net.sf.saxon.s9api.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class UtilsXML {
	static String eol = System.getProperty("line.separator");  

	static DocumentBuilder formXMLbuilder () {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder;
	}

	public static Document formXMLdocument (String n) {
		Document doc = null;
		try {
			doc = formXMLbuilder().parse(new InputSource(new ByteArrayInputStream(n.getBytes("UTF-8"))));
		} catch (SAXException | IOException e) {
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

	static XPathExpression xpathCompile (XPath xpath, String exp)
	{
		XPathExpression expr = null;
		try {
			expr = xpath.compile(exp);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return expr;
	}

	static String xpathEvaluate (XPathExpression expr, Document doc)
	{
		Node str = null;
		String result = null;
		//		Common.print(expr);
		//		Common.print(doc);
		try {
			str = (Node) expr.evaluate(doc,XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			try {
				return expr.evaluate(doc);
			} catch (XPathExpressionException e1) {
				e1.printStackTrace();
			}
		}
		try {
			result = str.getNodeValue();
		} catch (NullPointerException e) {
			result = "";
		}
		return result;
	}

	public static String xpathEvaluateCount (XPathExpression expr, Document doc)
	{
		String str = null;
		String result = null;
		try {
			str = expr.evaluate(doc);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static Boolean xpathEvaluateTrue (XPathExpression expr, Document doc)
	{
		Boolean result = null;
		String result1 = null;
		try {
			result = expr.evaluate(doc).equals("true");
			result1 = expr.evaluate(doc);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String takeXmlValueByXpath(String xml, String xpathExp) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{
		//		Common.print(xml);
		Document doc = formXMLdocument(xml);
		//		Common.print(doc);
		XPath xpath = createXpath();
		XPathExpression expr = xpathCompile(xpath,xpathExp);
		String nodeValue = null;

		if (xpathExp.contains("boolean"))
			nodeValue = xpathEvaluateTrue(expr,doc).toString();
		else
			nodeValue = xpathEvaluate(expr,doc);
		return nodeValue;
	}

	public static String takeXmlValueByXpath(String xml, List<String> xpaths) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{
		//		Common.print(xml);
		Document doc = formXMLdocument(xml);
		//		Common.print(doc);
		XPath xpath = createXpath();
		String nodeValue = null;
		String commonValue = "";
		for (String xpathExp : xpaths)
		{
			XPathExpression expr = xpathCompile(xpath,xpathExp);

			if (xpathExp.contains("boolean"))
				nodeValue = xpathEvaluateTrue(expr,doc).toString();
			else
				nodeValue = xpathEvaluate(expr,doc);
			commonValue += "Result:" + nodeValue + " for xpath " + xpathExp + eol ; 
		}
		return commonValue;
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

	public static String takeManyLogicXpaths (String xml, List<String> xpaths)
	{
		String commonValue = "";
		Processor proc = new Processor(false);
		XPathCompiler xpath = proc.newXPathCompiler();
		net.sf.saxon.s9api.DocumentBuilder builder = proc.newDocumentBuilder();
		StringReader reader = new StringReader(xml);
		XdmNode docXdmNode = null;
		XdmValue logicXpathValue = null;
		try {
			docXdmNode = builder.build(new StreamSource(reader));
			for (String static_xpath : xpaths)
			{
				XPathSelector selector = xpath.compile(static_xpath).load();
				selector.setContextItem(docXdmNode);
				logicXpathValue = selector.evaluate();
				commonValue += "Result:" + logicXpathValue + " for xpath " + static_xpath + eol ; 
			}
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
		return commonValue;
	}

	public static void editXmlField(String filePath, String elementName, String elementNewValue ) 
	{
		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			//update Element value
			doc = updateElementValue(doc, elementName, elementNewValue);

			//write the updated document to file or console
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			System.out.println("XML file updated successfully");
		} catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
			e1.printStackTrace();
		}
	}

	public static void addXmlElement(String filePath, String elementName, String elementValue ) 
	{
		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			//update Element value
			doc = addElementValue(doc, elementName, elementValue);

			//write the updated document to file or console
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			System.out.println("XML file updated successfully");
		} catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) {
			e1.printStackTrace();
		}
	}

	private static Document updateElementValue(Document doc, String elementName, String elementNewValue) {
		try
		{
			Node element = doc.getElementsByTagName(elementName).item(0).getFirstChild();
			element.setNodeValue(elementNewValue);
		}
		catch (NullPointerException e)
		{
			Common.print("XML element's update fails");
		}
		return doc;
	}

	private static Document addElementValue(Document doc, String elementName, String elementNewValue) {
		/*		
		Node staff = doc.getElementsByTagName("feed_meta_data").item(0);
		Node next = doc.getElementsByTagName("remote_data").item(0);

	 	Element age = doc.createElement("legal_entity");
	 	age.appendChild(doc.createTextNode("0840"));
	 	staff.appendChild(age);
		 */

		NodeList nodes = doc.getElementsByTagName("remote_data");

		Text a = doc.createTextNode("0840"); 
		Element p = doc.createElement("legal_entity"); 
		p.appendChild(a); 

		nodes.item(0).getParentNode().insertBefore(p, nodes.item(0));
		return doc;
	}

	public static ArrayList<String> parseWithXpaths(String XMLSTRING, String xpathToEval) {

		Document doc = formXMLdocument(XMLSTRING);
		XPath xpath = createXpath();
		NodeList xTimeStampNodes = findElements(xpathToEval,doc, xpath);
		return convertToCollection(xTimeStampNodes);
	}

	private static NodeList findElements(final String xpathExpression,
			final Document doc, final XPath xpath) {
		NodeList nodes = null;
		if (doc != null) {
			try {
				final XPathExpression expr = xpath.compile(xpathExpression);
				final Object result = expr
						.evaluate(doc, XPathConstants.NODESET);
				nodes = (NodeList) result;
			} catch (final XPathExpressionException exception) {
			}
		}
		return nodes;
	}

	private static ArrayList<String> convertToCollection(final NodeList nodes) {
		final ArrayList<String> result = new ArrayList<String>();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				result.add(nodes.item(i).getNodeValue());
				//                Common.print(nodes.item(i).getNodeValue());
			}
		}
		return result;
	}

	public static void getElementsNamesFromXSDFile(String xsdFIleWithPath, ArrayList<String> elementsNames, String xpathToElements)
	{
		File stocks = new File(xsdFIleWithPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(stocks);
			doc.getDocumentElement().normalize();

			XPathExpression expr = null;
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			expr = xpath.compile(xpathToElements); 
			//		expr = xpath.compile("//xs:complexType[@name=\"dly_bal_posn\"]/xs:sequence/xs:element/@name"); //FIM-FDS-DATACLASS-DAILY-BALANCE-POSITION_2.4.3.2_Modified.xsd
			//		expr = xpath.compile("//xs:complexType[@name=\"ent_dly_bal_posn\"]/xs:choice/xs:element/@name");//FIM-FDS-DATACLASS-DAILY-BALANCE-POSITION_4.1.1.xsd
			NodeList nl = (NodeList) expr.evaluate(doc,XPathConstants.NODESET);
			int length = nl.getLength();
			for( int i=0; i<length; i++) 
			{
				Attr attr = (Attr) nl.item(i);
				String value = attr.getValue();
				elementsNames.add(value);
			}
		}
		catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) 
		{
			e.printStackTrace();
		}
	}
}
