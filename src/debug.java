

public class debug {
/*
	public static void main(String[] args) {

		String file = "e:\\TransactionAccountingSVNAuto\\TA\\TestData\\DataSourcing\\ESS\\Adjustment_Book\\PLESR_FinConAdj_LN_20131101_PM.txt";
		String parentFolder = new File (file).getParent();
		Common.print(parentFolder.substring(parentFolder.lastIndexOf("\\")+1,parentFolder.length()));
		String xmlFileWithPath = "e:\\TransactionAccountingSVNAuto\\TAAcS\\TA\\TestData\\Accounting\\CR_Control message processing_several messages from different systems\\" +
		"Control_CBIOM_T.xml";
		UtilsXML.editXmlField(xmlFileWithPath, "digest", "123");
	}
	*/

	public static void main(String[] s)
	{
	}

/*	
	public static void main(String[] s) throws Exception
	{
	VTDGenHuge vg = new VTDGenHuge();
	Common.print(System.getProperty("sun.arch.data.model") );
    if (vg.parseFile("e:\\1\\ref\\RD_COUNTERPARTY.xml",true)){
//        if (vg.parseFile("e:\\TransactionAccountingSVNAuto\\TAAcS\\TA\\TestData\\DataSourcing\\Static\\RD_COUNTERPARTY.xml",true)){
        VTDNavHuge vnh = vg.getNav();
	AutoPilotHuge aph = new AutoPilotHuge(vnh);
	
	aph.selectXPath("//COUNTERPARTY[CPTY_ACC_ID='DBO-102062030525' and CPTY_SRC_SYS_NM='521']/CPTY_SRC_SYS_NM/text()");
//	<COUNTERPARTY>	<ACS_UID_CPTY>663721</ACS_UID_CPTY>	<CPTY_SRC_SYS_NM>691</CPTY_SRC_SYS_NM>	<CPTY_ST_NM>DEUTSCHE BANK AG</CPTY_ST_NM>	<CPTY_ACC_ID>0010</CPTY_ACC_ID>	<VERSION>1</VERSION>	<SRC_CPTY_ID>157</SRC_CPTY_ID>	<ACTIVE_INDIC>Y</ACTIVE_INDIC>	<LAST_MOD_TS>2014-07-31T08:36:47.583+01:00</LAST_MOD_TS>	<SOURCE>PARAGON</SOURCE>	<CPTY_LEGAL_NM>Deutsche Bank Aktiengesellschaft</CPTY_LEGAL_NM>	<ACS_IS_ACTIVE>Y</ACS_IS_ACTIVE></COUNTERPARTY>
//	<INSTRUMENT>	<INSTRUMENT_ID>56080193</INSTRUMENT_ID>	<INSTRUMENT_VERSION>1</INSTRUMENT_VERSION>	<RD_INSTRUMENT_IS_ACTIVE>Y</RD_INSTRUMENT_IS_ACTIVE>	<ISIN>XS0877820422</ISIN>	<LAST_MOD_TS>2014-07-04T11:04:29.532+02:00</LAST_MOD_TS>	<RDS_PRODUCT_TYPE>Convertible Bond</RDS_PRODUCT_TYPE>	<RD_INSTRUMENT_SOURCE>IRDS</RD_INSTRUMENT_SOURCE>	<ISSUER_ID>35089</ISSUER_ID>	<RD_INSTRUMENT_ID>54757107</RD_INSTRUMENT_ID></INSTRUMENT>

	int i = 0;
	while ((i=aph.evalXPath())!=-1){
		System.out.println(" element name is "+vnh.toString(i));
	}
    }
    else
		Common.print("else element name is ");
}
	
	/*
	public static void main(String[] args) throws Exception {
	    String separator;
	    Path path;
	 
	    path = Paths.get(".");
	    System.out.println("Absolute Path: " + path.toAbsolutePath());
	 
	    path = FileSystems.getDefault().getPath(".");
	    System.out.println("Absolute Path: " + path.toAbsolutePath());
	 
	  }
	*/
/*
	public static void main(String[] args) {
		VariableStorage testVariables = new VariableStorage();
		Properties props = new Properties();
		props = WorkWithProperties.LoadProps(".\\TestData\\parameters_PnL_CM_AC_INT3.properties");//"parameters_AC.properties");
		testVariables.set(props);
		testVariables.set("PNL_MESSAGES_IN_ODINEOD", "3");
		testVariables.set("QUEUE_TO_READ","ODInEOD");
		try {
//			UtilsJMS.recieveMessageFromQueueTest(testVariables.getAll(), 3);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	*/
}



//Reporter.log( "Message", true );