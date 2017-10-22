package service;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.Entry;
import model.PMIValueIndexes;
import model.WordIndex;

public class ExportManagerImpl implements ExportManager {

	EntryManager entryManager = new EntryManagerImpl();

	@Override
	public void writeSpecificEntryCountToDocument(int entryCount) {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("eksi" + entryCount + ".txt"));
				if (entryCount < activeEntryList.size()) {
					for (int i = 0; i < entryCount; i++) {
						out.write(activeEntryList.get(i).getDescription());
					}
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} catch (IOException e) {
				System.err.println("TXT oluþturulurken hata oluþtu!");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createTxtForLink(List<String> linkList, String titleOfFile) {
		try {
			BufferedWriter out;
			if (titleOfFile.contains(".txt")) {
				out = new BufferedWriter(new FileWriter(titleOfFile));
			} else {
				out = new BufferedWriter(new FileWriter(titleOfFile + ".txt"));
			}
			for (String link : linkList) {
				out.write(link + "\r\n");
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Linklerin txt dosyasýna yazýmý sýrasýnda beklenmeyen bir hata oluþtu!");
			e.printStackTrace();
		}
	}

	@Override
	public void writeAllEntriesToDocument() {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("eksi.txt"));
				for (Entry e : activeEntryList) {
					out.write(e.getDescription());
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} catch (IOException e) {
				System.err.println("TXT oluþturulurken hata oluþtu!");
				e.printStackTrace();
			}
		}
	}
	
	
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
	
	@Override
	public void createTxtFileForVocabs(Map<String, Integer> ranked) {
		try{
			 BufferedWriter out = new BufferedWriter(new FileWriter("vocab.txt"));
			 for(Map.Entry<String,Integer>  entrySet  : ranked.entrySet()){
				 out.write(entrySet.getKey() +"	"+ entrySet.getValue()+"\r\n");
			 }
			 out.close();
			 System.out.println("TXT oluþturuldu.!");
		}
		catch (IOException e) {
			System.err.println("TXT oluþturulurken hata oluþtu!");
       }
		
	}
	
	@Override
	public void createOutputForWordsOccured(List<WordIndex> wordIndexList) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("wordIndexFrequency.txt"));
			 for(WordIndex  word  : wordIndexList){
				 out.write(word.getWord() +"	"+ word.getIndex()+"	"+word.getFrequency()+"\r\n");
			 }
			 out.close();
			 System.out.println("TXT oluþturuldu.!");
		}
		catch (IOException e) {
			System.err.println("TXT oluþturulurken hata oluþtu!");
        }
	}
	
	@Override
	public void createTxtFilePMIIndexValues(List<PMIValueIndexes> indexList, boolean isFilled) {
		try {
			BufferedWriter out;
			if (isFilled) {				
				out = new BufferedWriter(new FileWriter("filledWithEmptyPmiValueIndexes.txt"));
			} else {
				out = new BufferedWriter(new FileWriter("pmiValueIndexes.txt"));
			}
			out.write("INDEX-1" + "	" + "INDEX-2" + "	" + "PMI-VALUE" + "	" + "LOG-PMI-VALUE" + "	" + "ALT-PMI-VALUE" + "	" + "LOG-ALT-PMI-VALUE" +"\r\n");
			for (PMIValueIndexes index : indexList) {
				out.write(index.getIndex1() + "	" + index.getIndex2() + "	" + index.getPmiValue() + "	" 
							+ index.getLogaritmicPmiValue() + "	" + index.getAlternatePmiValue() + "	" + index.getLogarithmicAlternatePmiValue()+"\r\n");
			}
			out.close();
			System.out.println("PMI Index Value nesnesi için çýktý oluþturuldu");
			
		} catch (Exception e) {
			System.err.println("TXT oluþturulurken hata oluþtu!");
		}
	}

}
