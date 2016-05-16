

/**
 * Created by vshevchenko on 17/10/2014.
 */
public class StepInfoContainer {
    private String testName;

    private  String testStep;
    private  String testStatus;
    private  String testComment;

    public StepInfoContainer(String testStep, String testStatus, String testComment ){
        this.testStep = testStep;
        this.testStatus = testStatus;
        this.testComment = testComment;
    }

    public StepInfoContainer(String testName){
        this.testName = testName;
    }
    public StepInfoContainer() {
    }

    public String getTestStep() {
        return testStep;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public String getTestComment() {
        return testComment;
    }
}
