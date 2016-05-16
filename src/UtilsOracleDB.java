

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class UtilsOracleDB {
	String eol = System.getProperty("line.separator");
	String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private Connection dbConnection = null;
//	private static Properties prop = new Properties();
	boolean connEst = false;

	public UtilsOracleDB(String DB_CONNECTION, String DB_USER, String DB_PASSWORD, String DB_SESSION_USER) {
		establishConnection(DB_CONNECTION, DB_USER, DB_PASSWORD, DB_SESSION_USER);
	}
/**
 * Function used for establishing connection for default database from property file.
 * Default database storing parameters are: DB_CONNECTION, DBUSER,DBPASS
 * 
 * @param props
 */
	public UtilsOracleDB(Properties props) {
		String DB_CONNECTION = props.getProperty("DB_CONNECTION");
		String DB_USER = props.getProperty("DBUSER");
		String DB_PASSWORD = props.getProperty("DBPASS");
		String DB_SESSION_USER = props.getProperty("DBOWNER");
		establishConnection(DB_CONNECTION, DB_USER, DB_PASSWORD, DB_SESSION_USER);
	}

	void establishConnection(String DB_CONNECTION, String DB_USER, String DB_PASSWORD, String DB_SESSION_USER)
	{		
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
			
			String ls_sql_alter = "alter session set current_schema =" + DB_SESSION_USER;
			Statement stmt = dbConnection.createStatement();
            int rt = stmt.executeUpdate(ls_sql_alter);
            System.out.println("alter session result =" + rt);

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
	
	public void closeConnection()
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

	public String getSQLvalue (String query)
	{
		String field = "RES";
		int counter = 0;
		String result = "";
		Statement stmt = null;
		try {
			//			        stmt = con.createStatement();
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			//			        System.out.println(rs.getRow());
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

	public String executeSQLStatement (String final_query)
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
				try
				{
				Common.print("Update from cycle " + command);
				rs = statement.executeUpdate(command);
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
				rs = statement.executeUpdate(final_query);
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
			cstmt = dbConnection.prepareCall("{call dbms_output.enable(800000) }");
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

	public PreparedStatement prepareStatement(String statementText)
	{
		try {
			return dbConnection.prepareStatement(statementText);
		} catch (SQLException e) {
			Common.print("DB statement preparation fails: " + e);
			return null;
		}	
	}

	public void commit()	
	{
		try {
			dbConnection.commit();
		} catch (SQLException e) {
			Common.print("DB commit fails: " + e);
		}
	}
}
