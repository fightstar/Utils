

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UtilsSQL {
	public static String eol = System.getProperty("line.separator");  
	public static Connection dbConnection = null;
	private static Properties prop = new Properties();
	boolean connEst = false;

	public static void setProps (Properties props)
	{
		prop = props;
	}

	public UtilsSQL(String DB_CONNECTION, String DB_USER, String DB_PASSWORD) {
		String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
		String result = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(
					DB_CONNECTION, DB_USER,DB_PASSWORD);
			dbConnection.setAutoCommit(true);
			result = "PASS: Connection is established";
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (dbConnection==null)
		{		
			Common.print("Connection failed to establish!");
			result =  "FAIL: Connection failed";
		}
		else
			Common.print("Connection established!" + dbConnection);
	}

	public static String getDBConnection(Properties props) {
		prop = props;
		String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
		//		Properties props = WorkWithProperties.LoadProps("e:\\Automation\\workspace\\TA\\TestData\\ESS\\DB.properties");
		String DB_CONNECTION = props.getProperty("DB_CONNECTION");//"jdbc:oracle:thin:@acsu2.uk.db.com:1524:LNACSU3";
		//		System.out.println(DB_CONNECTION);
		String DB_USER = props.getProperty("DBUSER");
		String DB_PASSWORD = props.getProperty("DBPASS");
		String DB_OWNER = props.getProperty("DBOWNER");
		String result = null;
		//Common.print(DB_PASSWORD+" " + DB_USER + " "+ DB_CONNECTION);
		//		Connection dbConnection = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(
					DB_CONNECTION, DB_USER,DB_PASSWORD);
			dbConnection.setAutoCommit(true);
			//			Common.print("Connection established!");
			String ls_sql_alter = "alter session set current_schema = "+DB_OWNER;
			Statement stmt = dbConnection.createStatement();
			stmt.executeUpdate(ls_sql_alter);
			result = "PASS: Connection is established";
			Common.print("Schema was changed to "+ DB_OWNER);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (dbConnection==null)
		{		
			Common.print("Connection failed to establish!");
			result =  "FAIL: Connection failed";
		}
		else
			Common.print("Connection established!" + dbConnection);

		return result;
	}

	
	public static void getDBConnection() {
		String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
		String DB_CONNECTION = prop.getProperty("DB_CONNECTION");//"jdbc:oracle:thin:@acsu2.uk.db.com:1524:LNACSU3";
		String DB_USER = prop.getProperty("DBUSER");
		String DB_PASSWORD = prop.getProperty("DBPASS");
		String result = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(
					DB_CONNECTION, DB_USER,DB_PASSWORD);
			dbConnection.setAutoCommit(true);
			result = "PASS: Connection is established";
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if (dbConnection==null)
		{		
			Common.print("Connection failed to establish!");
			result =  "FAIL: Connection failed";
		}
		else
			Common.print("Connection established!" + dbConnection);
	}

	
	public static void closeConnection()
	{
		if (dbConnection != null) {
			try {
				dbConnection.close();
				Common.print("Connection closed");
			} catch (SQLException e) {
				Common.print("Connection is not closed");
				e.printStackTrace();
			}
		}
	}

	public static String getSQLvalue (String query)
	{
		//		getDBConnection();
		String field = "RES";
		int counter = 0;
		String result = "";
		Statement stmt = null;
		try {
			//			        stmt = con.createStatement();
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			//						        System.out.println(rs.getRow());
			while (rs.next()) {
				if (counter>0)
					result += ";" +eol+ " Result:"+rs.getString(1);//.getString(field);
				else
					result += rs.getString(1);//getString(field);
				//				        Common.print("result 1: "+result);
				counter++;    
			}
		} catch (SQLException e ) {
			System.out.print(e);
			closeConnection();
			getDBConnection(prop);
			return "Incorrect query or connection lost " + eol + e;
		} finally {
			if (stmt != null) 
			{ 
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return "FAILED to close statement";
				} 
			}
		}
		//			    closeConnection();
		return result;
	}

	public static ArrayList<String> getListOfSQLvalue (String query)
	{
		ArrayList<String> column = new ArrayList<String>(); 

		String field = "RES";
		int counter = 0;
		String result = "";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				column.add(rs.getString(field));
			}
		} catch (SQLException e ) {
			System.out.print(e);
			getDBConnection(prop);
		} finally {
			if (stmt != null) 
			{ 
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		}
		return column;
	}

	public static List<List<String>> getSQLTable (String query)
	{
		List<List<String>> table = new ArrayList<List<String>>(); 
		Common.print(query);

		List<String> fields = getFieldsFormRequest (query);
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				List<String> row = new ArrayList<String>();
				for (String fieldName : fields)
				{
					row.add(rs.getString(fieldName));
					//			            	Common.print(fieldName + " ::: " + rs.getString(fieldName));
				}
				table.add(row);
			}			    
		} catch (SQLException e ) {
			System.out.print(e);
			return table;
		} finally {
			if (stmt != null) 
			{ 
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return table;
				} 
			}
		}
		return table;
	}

	static List<String> getFieldsFormRequest (String request)
	{
		String fieldsAll = StringUtils.substringBetween(request.toLowerCase(), "select ", " from");
		return Common.splitBy(fieldsAll.trim(),",");
	}

	public static String executeSQLStatement (String final_query)
	{
		Statement statement = null;
		Integer rs = null;
		String result = null;
		try {
			statement = dbConnection.createStatement();
			Common.print("Query for update: " + final_query);
			if (final_query.contains(";"))
			{
				ArrayList<String> commands_list = Common.splitBy(final_query,";");
				for (String command : commands_list)
				{
					Common.print("Update from cycle " + command);
					try
					{
						rs = statement.executeUpdate(command);//.executeQuery(command);
						Common.print("Update from cycle done");
						result = "Result:true: Execution done " + rs.toString();
					}
					catch (SQLException | NullPointerException e) 
					{
						System.out.println(e.getMessage());
						result = "Result:false: Update execution failed" + eol +e.getMessage();
					}
				}
				dbConnection.commit();
			}
			else
			{
				rs = statement.executeUpdate(final_query);//rs = statement.executeQuery(final_query);
				result = "Result:true: Execution done " + rs.toString();
				dbConnection.commit();
			}
		} 
		catch (SQLException | NullPointerException e) {
			System.out.println(e.getMessage());
			result = "Result:false: Update execution failed" + eol +e.getMessage();
			// 			System.exit(0);
		} 
		finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					Common.print("Closure of statement fails");
					e.printStackTrace();
				}
			}
		}
		//		closeConnection();
		return result;
	}

	public String runSQLStatement (String final_query)
	{
		CallableStatement cstmt = null;
		String result = "";
		try {
			cstmt = dbConnection.prepareCall("{call dbms_output.enable(500000) }");
			cstmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		CallableStatement cs;
		try {
			cs = dbConnection.prepareCall(final_query);
			cs.execute();
			//			         System.out.println("Output parameter was = '" + cs.getObject(1) + "'");
			cs.close();
		} catch (SQLException e) {
			Common.print("Execution of pl/sql anonymous block failed");
			e.printStackTrace();
		}

		try {
			cstmt = dbConnection.prepareCall("{call dbms_output.get_line(?,?)}");
			cstmt.registerOutParameter(1,java.sql.Types.VARCHAR);
			cstmt.registerOutParameter(2,java.sql.Types.NUMERIC);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		int status = 0;
		while (status == 0)
		{
			try {
				cstmt.execute();
				String line = cstmt.getString(1);
				status = cstmt.getInt(2);
				if (line != null && status == 0)
				{
					result+= "Result:" + line + eol;
					Common.print(line);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static Boolean isTableExists(String table) {
		Boolean result = false;
		try {
			DatabaseMetaData dbm = dbConnection.getMetaData();
			ResultSet rs = dbm.getTables(null, null, table, null);
			if (rs.next()) {
				result = true;
			} else {
				result = false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	

}
