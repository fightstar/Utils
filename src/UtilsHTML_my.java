

import java.io.*;
import java.util.List;

/**
 * Created by vshevchenko on 17/10/2014.
 */
public class UtilsHTML_my {

public static void addToHTML(List<String> content){
    try {
        StringBuilder buffer = new StringBuilder();
        for(String current : content) {
            buffer.append(current).append("\n");
        }

        BufferedReader txtfile = new BufferedReader(new StringReader(buffer.toString()));
        OutputStream htmlfile= new FileOutputStream(new File("c:\\test.html"));
        PrintStream printhtml = new PrintStream(htmlfile);

        String[] txtbyLine = new String[500];
        String temp = "";
        String txtfiledata = "";

        String htmlheader="<html><head>";
        htmlheader+="<title>Report</title>";
        htmlheader+="<style>\n" +
                "table, th, td {\n" +
                "    border: 1px solid black;\n" +
                "    border-collapse: collapse;\n" +
                "}\n" +
                "th, td {\n" +
                "    padding: 5px;\n" +
                "    text-align: left;    \n" +
                "}\n" +
                "</style>";
        htmlheader+="</head><body>";
        String h2 = "Test:"; //+ testName
        String htmlfooter="</body></html>";
        int linenum = 0 ;

        while ((txtfiledata = txtfile.readLine())!= null)
        {
            txtbyLine[linenum] = txtfiledata;
            linenum++;
        }
        for(int i=0;i<linenum;i++)
        {
            if(i == 0)
            {
                temp = htmlheader + txtbyLine[0];
                txtbyLine[0] = temp;
            }
            if(linenum == i+1)
            {
                temp = txtbyLine[i] + htmlfooter;
                txtbyLine[i] = temp;
            }
            printhtml.println(h2+txtbyLine[i]);
        }

        printhtml.close();
        htmlfile.close();
        txtfile.close();

    }

    catch (Exception e) {
        Common.printError("Failed to create HTML!");
    }

}
    public void createHTML(TestContainer testContainer) {


    }

}

