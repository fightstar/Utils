

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class TestReport {
    List<TestTA> test;

@XmlElement
    public List<TestTA> getTest() {
        if (test == null) {
        	test = new ArrayList<TestTA>();
        }
        return this.test;
    }

}
