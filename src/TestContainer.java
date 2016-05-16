

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vshevchenko on 17/10/2014.
 */
public class TestContainer {
    private final String testName;
    private String testDuration;
    private final List<StepInfoContainer> stepInfoContainers = new ArrayList<StepInfoContainer>();

    public TestContainer(String testName) {
        this.testName = testName;
    }

    public String getTestDuration() {
        return testDuration;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestDuration(String testDuration) {
        this.testDuration = testDuration;
    }

    public List<StepInfoContainer> getStepInfoContainers() {
        return stepInfoContainers;
    }
}
