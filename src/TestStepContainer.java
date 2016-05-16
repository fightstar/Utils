

import java.util.List;

public class TestStepContainer {

	String Name = null;                  //Step
	String Parameter = null;             //Value
	String getVariableForResult = null;  //Result
	String Description = null;           //Description

	public TestStepContainer(List<String> s)
	{
		Name = s.get(0);
//		Parameter = s.get(1);

        try{
            Parameter = s.get(1);
        }
        catch (IndexOutOfBoundsException e){
            Parameter = null;
        }
		try{
			getVariableForResult = s.get(2);
		}
		catch(IndexOutOfBoundsException e){
			getVariableForResult=null;
		}
		try{
			Description = s.get(3);
		}
		catch(IndexOutOfBoundsException e){
			Description=null;
		}
	}

	public TestStepContainer() {
	}

	public String getName()	{
	return Name;
	}

	public String getParameter()	{
	return Parameter;
	}
	
	public String getVariableForResult()	{
	return getVariableForResult;
	}

	public String getDescription()	{
		return Description;
	}

	public Boolean isDescriptionEmpty()	{
	return Description.equals("");
	}

	public void setParameter(String parameter)
	{
		this.Parameter = parameter;
	}
	public void setVariableForResult(String parameter)
	{
		this.getVariableForResult = parameter;
	}
	public void setDescription(String description)
	{
		this.Description = description;
	}
	
	public void setStep(List<String> s)
	{

        try{
            Parameter = s.get(0);
        }
        catch (IndexOutOfBoundsException e){
            Parameter = null;
        }
		try{
			getVariableForResult = s.get(1);
		}
		catch(IndexOutOfBoundsException e){
			getVariableForResult=null;
		}
		try{
			Description = s.get(2);
		}
		catch(IndexOutOfBoundsException e){
			Description = null;
		}
	}
	
}
