

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestStep {
	String stepName;
	String status;
	String comments;
 
	public String getStepName() {
		return stepName;
	}
 
	@XmlElement
	public void setStepName(String name) {
		this.stepName = name;
	}
 
	public String getStatus() {
		return status;
	}
 
	@XmlElement
	public void setStatus(String status) {
		this.status = status;
	}
 
	public String getComments () {
		return comments;
	}
 
	@XmlElement
	public void setComments(String comments) {
		this.comments = comments;
	}
 
}
