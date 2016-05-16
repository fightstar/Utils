

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UtilsXLS {
	

	public static List<List<String>> readXSLX (String filename) throws IOException
	{
		List<String> keys = new ArrayList<String>();
		List<List<String>> runData = new ArrayList<List<String>>();

		XSSFWorkbook workbook = null;
		File f = new File(filename);
		if(f.exists())
		{
			FileInputStream file = new FileInputStream(f);
			workbook = new XSSFWorkbook(file);
			file.close();
		}
		//		  Common.print("xls filename: " + filename);
		try
		{
			XSSFSheet sheet = workbook.getSheetAt(0);//.getSheet(sheet_name);
			Row row;

			row = sheet.getRow(0);
			for (int j = 0; j < row.getPhysicalNumberOfCells(); j++)
			{
				keys.add(row.getCell(j).getStringCellValue());
			}

			Integer rows_number = sheet.getPhysicalNumberOfRows();
			//		Common.print("rows_number: " + rows_number);
			for (Integer j = 1; j < rows_number; j++)
			{
				ArrayList<String> values = new ArrayList<String>();
				row = sheet.getRow(j);
				//				Common.print("row.getLastCellNum(): " + row.getLastCellNum());
				for(int cn=0; cn < row.getLastCellNum(); cn++) {
					Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
					cell.setCellType(Cell.CELL_TYPE_STRING);
					values.add(row.getCell(cn).getStringCellValue());
					//					Common.print(row.getCell(cn).getStringCellValue());
					//					Common.print("values.get("+cn+"): "+values.get(cn));
				}
				//					Common.print("iteration J: "+j);
				//					Common.print(values.get(0));
				runData.add(values);
				//					values.clear();
			}
		}
		catch (NullPointerException e)
		{
			Common.print("Failed to read excel file: " + filename);
		}
		return runData;
	}

	public static void addToXslx(String filename, String sheetname, List<String> content) {

		XSSFSheet sheet=null;
		XSSFWorkbook workbook = null;
		Row row=null;
		Integer last_row;
		Integer phis_row;

		File f = new File(filename);
		if(f.exists())
		{
			FileInputStream file = null;
			try {
				file = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				Common.print("FileNotFoundException: excel file " + filename + e);
			}
			try {
				workbook = new XSSFWorkbook(file);
			} catch (IOException e) {
				Common.print("IOException: excel file workbook creation fails for " + filename + e);
			}
			try {
				file.close();
			} catch (IOException e) {
				Common.print("IOException: excel file failed to close " + filename + e);
			}
		}
		else
		{
			workbook = new XSSFWorkbook();
		}
		sheetname = (sheetname.length() < 30)?sheetname:sheetname.substring(0, 29);
		sheet = workbook.getSheet(sheetname);
		if (sheet==null)
		{
			sheet = workbook.createSheet(sheetname);
		}
		last_row = sheet.getLastRowNum();
		phis_row = sheet.getPhysicalNumberOfRows();
		//	  		System.out.println(last_row);
		//	  		System.out.println(phis_row);
		if (last_row == 0 && phis_row == 0 )
		{
			row = sheet.createRow(last_row);
		}
		else
		{
			row = sheet.createRow(phis_row);
		}
		int cellnum = 0;
		for (String s : content)
		{
			List<String> textToWriteToCell = splitEqually(s,30000);
			for (String ss : textToWriteToCell)
			{
				Cell cell = row.createCell(cellnum);
				cell.setCellValue(ss);
				cellnum++;
			}
		}

		last_row = sheet.getLastRowNum();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			workbook.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void  addToXslxFinal (String filename, String sheetname, List<String> content) {

		XSSFSheet sheet=null;
		XSSFWorkbook workbook = null;
		Row row=null;
		Integer last_row;
		Integer phis_row;

		File f = new File(filename);
		if(f.exists())
		{
			FileInputStream file = null;
			try {
				file = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				Common.print("FileNotFoundException: excel file " + filename + e);
			}
			try {
				workbook = new XSSFWorkbook(file);
			} catch (IOException e) {
				Common.print("IOException: excel file workbook creation fails for " + filename + e);
			}
			try {
				file.close();
			} catch (IOException e) {
				Common.print("IOException: excel file failed to close " + filename + e);
			}
		}
		else
		{
			workbook = new XSSFWorkbook();
		}
		sheetname = (sheetname.length() < 30)?sheetname:sheetname.substring(0, 29);
		sheet = workbook.getSheet(sheetname);
		if (sheet==null)
		{
			sheet = workbook.createSheet(sheetname);
		}
		last_row = sheet.getLastRowNum();
		phis_row = sheet.getPhysicalNumberOfRows();
		if (last_row == 0 && phis_row == 0 )
		{
			row = sheet.createRow(last_row);
		}
		else
		{
			row = sheet.createRow(phis_row);
		}
		int cellnum = 0;
		Boolean linkToSheetWithTestIsCreated = false;
		XSSFCellStyle style = createLinkStyle(workbook);
		for (String s : content)
		{
			if (!linkToSheetWithTestIsCreated)
			{
				Hyperlink linkToSheetWithTest = createLink (workbook, s);
				linkToSheetWithTestIsCreated = true;
				Cell cell = row.createCell(cellnum);
				cell.setCellValue(s);
				cell.setHyperlink(linkToSheetWithTest);
				cell.setCellStyle(style);
				cellnum++;
			}
			else
			{
				List<String> textToWriteToCell = splitEqually(s, 30000);
				for (String ss : textToWriteToCell)
				{
					Cell cell = row.createCell(cellnum);
					cell.setCellValue(ss);
					//logger.info(ss);
					addColorToFailedResultCell(workbook);
					sheet.autoSizeColumn(0);
					cellnum++;
				}
			}
		}

		last_row = sheet.getLastRowNum();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			workbook.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> splitEqually(String text, int size) {
		// Give the list the right capacity to start with. You could use an array
		// instead if you wanted.
		List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

		for (int start = 0; start < text.length(); start += size) {
			ret.add(text.substring(start, Math.min(text.length(), start + size)));
		}
		return ret;
	}

	public static void insertLinkToSheet ()
	{
		XSSFWorkbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
		XSSFCreationHelper createHelper = wb.getCreationHelper();

		//cell style for hyperlinks
		//by default hyperlinks are blue and underlined
		XSSFCellStyle hlink_style = wb.createCellStyle();
		XSSFFont hlink_font = wb.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(IndexedColors.BLUE.getIndex());
		hlink_style.setFont(hlink_font);

		Cell cell;
		XSSFSheet sheet = wb.createSheet("Hyperlinks");

		//link to a place in this workbook
		//create a target sheet and cell
		XSSFSheet sheet2 = wb.createSheet("Target Sheet");
		sheet2.createRow(0).createCell((short)0).setCellValue("Target Cell");

		cell = sheet.createRow(3).createCell((short)0);
		cell.setCellValue("Worksheet Link");
		Hyperlink link2 = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
		link2.setAddress("'Target Sheet'!A1");
		cell.setHyperlink(link2);
		cell.setCellStyle(hlink_style);

		FileOutputStream out;
		try {
			out = new FileOutputStream("hyperinks.xlsx");
			wb.write(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Hyperlink createLink (XSSFWorkbook wb, String sheetName)
	{		
		XSSFCreationHelper createHelper = wb.getCreationHelper();
		Hyperlink link2 = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
		sheetName = (sheetName.length() < 30)?sheetName:sheetName.substring(0, 29);
		link2.setAddress("'"+sheetName+"'!A1");
		return link2; 
	}

	public static XSSFCellStyle createLinkStyle (XSSFWorkbook wb)
	{		
		//cell style for hyperlinks
		//by default hyperlinks are blue and underlined
		XSSFCellStyle hlink_style = wb.createCellStyle();
		XSSFFont hlink_font = wb.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(IndexedColors.BLUE.getIndex());
		hlink_style.setFont(hlink_font);
		return hlink_style;
	}

	public static List<List<String>> readDataArray (String filename) throws IOException
	{
		List<String> keys = new ArrayList<String>();
		List<List<String>> runData = new ArrayList<List<String>>();

		XSSFWorkbook workbook = null;
		File f = new File(filename);
		if(f.exists())
		{
			FileInputStream file = new FileInputStream(f);
			workbook = new XSSFWorkbook(file);
			file.close();
		}
		//		  Common.print("xls filename: " + filename);
		XSSFSheet sheet = workbook.getSheetAt(0);//.getSheet(sheet_name);
		Row row;

		row = sheet.getRow(0);
		for (int j = 0; j < row.getPhysicalNumberOfCells(); j++)
		{
			keys.add(row.getCell(j).getStringCellValue());
		}

		Integer rows_number = sheet.getPhysicalNumberOfRows();
		//		Common.print("rows_number: " + rows_number);
		for (Integer j = 0; j < rows_number; j++)
		{
			ArrayList<String> values = new ArrayList<String>();
			row = sheet.getRow(j);
			//				Common.print("row.getLastCellNum(): " + row.getLastCellNum());
			for(int cn=0; cn<row.getLastCellNum(); cn++) {
				Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
				cell.setCellType(Cell.CELL_TYPE_STRING);
				values.add(row.getCell(cn).getStringCellValue().trim());
				//					Common.print(row.getCell(cn).getStringCellValue());
				//					Common.print("values.get("+cn+"): "+values.get(cn));
			}
			//					Common.print("iteration J: "+j);
			//					Common.print(values.get(0));
			runData.add(values);
			//					values.clear();
		}
		return runData;
	}

	public static void addColorToFailedResultCell(Workbook wb){
		Sheet sheet = wb.getSheetAt(0);
		for (Row row : sheet) {
			for (Cell cell : row) {
				if(cell.getStringCellValue().contains("FAIL")){
					XSSFCellStyle failedResultCellStyle = (XSSFCellStyle) wb.createCellStyle();
					failedResultCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 200, 200)));
					failedResultCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
					cell.setCellStyle(failedResultCellStyle);
				}
			}
		}
	}

	public static Integer CountXSLXsheets (String filename, String mask) throws IOException
	{
		Integer count = 0;
		XSSFWorkbook workbook = null;
		
		File f = new File(filename);
		
		  if(f.exists())
		  {
			  FileInputStream file = new FileInputStream(f);
			  workbook = new XSSFWorkbook(file);
			  file.close();
			  }
		  for (int i = 0; i <workbook.getNumberOfSheets(); i++ )
		  {
			  if (workbook.getSheetName(i).matches(mask+".*"))
			  {
				  count++;
			  }
		  }
		  return count;
	}
	
	public static Integer rowCountXSLX (String filename)
	{
		Integer count;
		XSSFWorkbook workbook = null;
		
		File f = new File(filename);
		
		  if(f.exists())
		  {
			  FileInputStream file;
			try {
				file = new FileInputStream(f);
				workbook = new XSSFWorkbook(file);
				file.close();
			} catch (IOException e) {
				Common.print("Impossible to read excel file (rowCountXSLX) " + filename);
				e.printStackTrace();
			}
			  }
		  count = workbook.getSheetAt(0).getLastRowNum();
		  return count;
	}

	public static ArrayList<HashMap<String, String>> readXSLXToListOfHashmaps (String filename)
	{
		XSSFWorkbook workbook = null;
		ArrayList<HashMap<String, String>> configuration = new ArrayList<HashMap<String, String>> ();
//		List<ArrayList<HashMap<String, String>>> configurationsList = new ArrayList<ArrayList<HashMap<String, String>>> ();

		File f = new File(filename);
		FileInputStream file = null;

		if(f.exists())
		{
			try {
				file = new FileInputStream(f);
				workbook = new XSSFWorkbook(file);
				file.close();
				XSSFSheet sheet = workbook.getSheetAt(0);
				Row row;

				Integer count = rowCountXSLX(filename);
				for (int i = 1; i <=count; i++)
				{
					try 
					{
						row = sheet.getRow(i);
						HashMap<String, String> map = new HashMap<String, String>();
						for (Cell cell : row) 
						{
							cell.setCellType(Cell.CELL_TYPE_STRING);
							map.put(sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue(),cell.getStringCellValue());
						}
						configuration.add(map);
					}
					catch (NullPointerException e)
					{
					}
//					configurationsList.add(configuration);
				}
			} catch (IOException e) {
				Common.print("Impossible to read excel file (readXSLXToListOfHashmaps) " + filename);
				e.printStackTrace();
			}
		}
		return configuration;
	}

}

