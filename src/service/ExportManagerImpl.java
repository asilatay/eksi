package service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportManagerImpl implements ExportManager {

	
	private static void createExcelRankingWords(Map<String, Integer> ranked) throws IOException {
		System.out.println("Excel oluþturma iþlemi baþladý");
		try {
			FileOutputStream fileOut = new FileOutputStream("resultTable.xlsx");

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet worksheet = workbook.createSheet("POI Worksheet");

			// index from 0,0... cell A1 is cell(0,0)
			XSSFRow row1 = worksheet.createRow((int) 0);
			// Create Header of Excel
			XSSFCell cell = row1.createCell((int) 0);
			cell.setCellValue("Rank");
			cell = row1.createCell((int) 1);
			cell.setCellValue("Name");
			int rowNum = 1;
			// Create Body of Table
			for (Map.Entry<java.lang.String, java.lang.Integer> a : ranked.entrySet()) {
				row1 = worksheet.createRow((int) rowNum);
				cell = row1.createCell((int) 0);
				cell.setCellValue(a.getValue());
				cell = row1.createCell((int) 1);
				cell.setCellValue(a.getKey());
				rowNum++;
			}

			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			workbook.close();
			System.out.println("Excel oluþturma iþlemi baþarýyla tamamlandý");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
