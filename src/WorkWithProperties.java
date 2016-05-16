
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WorkWithProperties {
    public static Properties LoadProps(String name)
    {
//    	Common.print(name);
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(name));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

}