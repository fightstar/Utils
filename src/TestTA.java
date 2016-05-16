

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class TestTA {
	String testName;
	String testDuration;
	String testStatus;
    List<TestStep> testStep;

	public String getTestName() {
		return testName;
	}
 
	@XmlElement
	public void setTestName(String name) {
		this.testName = name;
	}
	
	public String getTestStatus() {
		return testStatus;
	}
 
	@XmlElement
	public void setTestStatus(String name) {
		this.testStatus = name;
	}

	public String getTestDuration() {
		return testDuration;
	}
 
	@XmlElement
	public void setTestDuration(String name) {
		this.testDuration = name;
	}
	
	@XmlElement
    public List<TestStep> getTestStep() {
        if (testStep == null) {
        	testStep = new ArrayList<TestStep>();
        }
        return this.testStep;
    }

	
}
