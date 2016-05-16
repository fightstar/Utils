

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class VariableStorage extends Properties{
	public HashMap<String, String> globalVariables;

	public VariableStorage()
	  {
		  globalVariables = new HashMap<String, String> ();
	  }

	public VariableStorage(String pathToPropFile)
	  {
		globalVariables = new HashMap<String, String> ();
		Map properties = WorkWithProperties.LoadProps(".\\TestData\\" + pathToPropFile);
		globalVariables.putAll(properties);
	  }
	
	public void set(String key, String value)
	{
//		Common.print("set DataStorage key: " + key +"; Value: " + value);
		globalVariables.put(key, value);	
	}

	public void set(Properties props)
	{
		for (Entry<Object, Object> entry  : props.entrySet()) 
		{
			globalVariables.put(entry.getKey().toString(), entry.getValue().toString());	
		}
	}

	public void set(String propertiesFileName)
	{
		Properties props = WorkWithProperties.LoadProps(propertiesFileName);

		for (Entry<Object, Object> entry  : props.entrySet()) 
		{
			globalVariables.put(entry.getKey().toString(), entry.getValue().toString());	
		}
	}

	public String get(String key)
	{
//		Common.print("Get DataStorage value for key: " + key);
		return globalVariables.get(key);
	}
	
	public HashMap<String, String> getAll()
	{
		return globalVariables;
	}

	public Properties getAllAsProperties()
	{
		Properties properties = new Properties();
		properties.putAll(globalVariables);
		return properties;
	}

	public Boolean exists(String name)
	{
		if (globalVariables.containsKey(name))
			return true;
		else
			return false;
	}

	public void print()
	{
		Common.print(globalVariables);
	}

	public Properties VariableStorageToProperties() {
		   Properties props = new Properties();
		   Set<Map.Entry<String,String>> set = globalVariables.entrySet();
		   for (Map.Entry<String,String> entry : set) {
		     props.put(entry.getKey(), entry.getValue());
		   }
		   return props;
		 }
}
