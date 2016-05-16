

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class UtilsDBPPublicHandler extends DefaultHandler {

	private static StringBuilder query = new StringBuilder();
	private StringBuilder sb;
	private boolean element = false;
	private int elements_ind;
	private int total;
	private int dailyPositions_ind = 0;
	private boolean to_read = false;
	private String ID = new String();
	private String elementName = null;
	private Boolean head_or_body = false;
	private ArrayList<String> elements= new ArrayList<String>();
	private HashMap<String, String> dailyPositions = new HashMap<String, String>();
	private String tablename = null;
	private String value;
	private Locator documentLocator;
	public static Properties props = new Properties();
	
	public UtilsDBPPublicHandler(ArrayList<String> elementsNames, String file, String table_name, Properties props) 
	{
		this.elements = elementsNames;

		/*
		elements = (ArrayList<String>) elementsNames.clone();
		for (int i=0; i < elementsNames.size(); i++)
	{
		elements.add(elementsNames.get(i));
	}
		 */
		ID = file;
		Common.print(ID);
		elementName = "dly_bal_posn";
		tablename = table_name;
		this.props = props;
	}

	public void startElement(String uri, String localName,String qName, 
			Attributes attributes) throws SAXException {
//		Common.print("START: qName: " + qName);
		if (qName.equalsIgnoreCase(elementName))
		{
			elements_ind = 0;
			to_read=true;
		}
		if (to_read)
		{
			for (int i=0; i < elements.size(); i++)
			{
				if (qName.equalsIgnoreCase(elements.get(i)))
				{
					element = true;
					elements_ind = i;
					sb = new StringBuilder();
				}
			}
		}
	}

	public void processingInstruction(String target, String data, String tablename)
			throws SAXException {
		dailyPositions_ind++;
		total++;
//		for (Entry<String, String> entry  : dailyPositions.entrySet()) {Common.print("dailyPositions::: Key: " + entry.getKey() + " Value: " + entry.getValue());}
		if (dailyPositions.size() != 0)
		{
			insertQuery(dailyPositions, ID, tablename);
			to_read=false;
			if ( dailyPositions_ind==30 && dailyPositions.size()!=0)
			{
				Common.print("processingInstruction: "+ dailyPositions_ind+"; Total: " + total);

				insertTable();

				dailyPositions_ind = 0;
				dailyPositions.clear();
			}
			dailyPositions.clear();
		}
		else
			Common.print("Nothing to insert!");
	}

	public void endElement(String uri, String localName,
			String qName) throws SAXException {
//		Common.print("END: qName: " + qName);
		element = false;
		if (qName.equalsIgnoreCase(elementName))
		{
			processingInstruction(qName,qName,tablename);
		}
//		Common.print("elementName endElement: " + qName);
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		//for (Entry<String, String> entry  : dailyPositions.entrySet()) {Common.print("dailyPositions::: Key: " + entry.getKey() + " Value: " + entry.getValue());}
		//		Common.print("CHAR");

		if (element) 
		{
			if (sb != null) 
			{
				for (int i=start; i<start+length; i++)
				{
					sb.append(ch[i]);
					//					Common.print(ch[i]);
				}
			}
			dailyPositions.put(elements.get(elements_ind),sb.toString());
			//			element = false;
		}
	}

	public void endDocument() throws SAXException {
//		Common.print("endDocument processingInstruction: "+ dailyPositions_ind+"; Total: " + total);
		insertTable();
	}

	public void setDocumentLocator(Locator locator) {
		documentLocator = locator;
//		Common.print("(Saving " + locator.getLineNumber() + ")");
	}

	public static void insertQuery (HashMap<String, String> dailyPositions, String ID, String tablename)
	{

		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
//		Common.print("dailyPositions.size(): " + dailyPositions.size());
		for (Entry<String, String> entry  : dailyPositions.entrySet()) 
		{
			columns.append(entry.getKey()+ ",");
			values.append("'" + entry.getValue() + "'" + ",");
		}
		query.append("INTO QA_OWNER."+tablename+" ( ID,"	+ columns.toString().substring(0, columns.length()-1) + ") VALUES ('"+ ID + "'," + values.toString().substring(0, values.length()-1) + ") ");
		//			Common.print(query);
	}


	public static void insertTable () 
	{
		String final_query = "INSERT ALL ";
		final_query += query.toString();
		final_query += " SELECT * FROM dual";

		if (query.length() != 0 )
		{
			UtilsSQL dbConnection = new UtilsSQL(props.getProperty("QA_DB_CONNECTION"), props.getProperty("QADBUSER"), props.getProperty("QADBPASS"));
			String sql_result = dbConnection.executeSQLStatement(final_query);
			Common.print(sql_result);
			dbConnection.closeConnection();
			query.setLength(0);
			final_query = null;
		}
	}

}

