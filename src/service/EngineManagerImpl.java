package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import model.Entry;
import model.KeyIndex;
import model.Title;
import model.User;

public class EngineManagerImpl implements EngineManager {
	
	TitleManager titleManager = new TitleManagerImpl();
	UserManager userManager = new UserManagerImpl();
	EntryManager entryManager = new EntryManagerImpl();
	
	private final static int turningNumber = 15;
	
	public EngineManagerImpl() {
	}
	
	@Override
	public void createCrudeLinks(String dir) {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null && listOfFiles.length > 0) {					
			for(int i = 0; i<listOfFiles.length; i++){	//	listOfFiles.length
				FileReader inputFile = null;
				try {
					inputFile = new FileReader(listOfFiles[i]);
				} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				//Instantiate the BufferedReader Class
		        BufferedReader bufferReader = new BufferedReader(inputFile);
		        //Variable to hold the one line data
		        String line;
		        // Read file line by line and print on the console
		        try {
		        	List<String> linkList = new ArrayList<String>();
		        	while ((line = bufferReader.readLine()) != null)   {
		        		if(line.contains("a=popular")) {
		        			if(line.contains("https")) {		        				
		        				line =  line.substring(22, line.length()-10);
		        			} else {
		        				line =  line.substring(0, line.length()-10);
		        			}
		        			linkList.add(line);
		        		}
		        	}
		        	createTxtForLink(linkList, listOfFiles[i].getName());
		        } catch (Exception e) {
		        	// TODO Auto-generated catch block
					e.printStackTrace();
		        }
		        
		      //Close the buffer reader
		        try {
		        	bufferReader.close();
				}catch (IOException e) {
					System.err.println("BufferReader ýn kapatýlmasý sýrasýnda beklenmeyen bir hata oluþtu!");
					e.printStackTrace();
				}
			}
		} 
	}
	
	@Override
	public void createTxtForLink(List<String> linkList, String titleOfFile){
		try {
			BufferedWriter out;
			if(titleOfFile.contains(".txt")) {
				out = new BufferedWriter(new FileWriter(titleOfFile));
			} else {				
				out = new BufferedWriter(new FileWriter(titleOfFile+".txt"));
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
	public void getDocumentWithjSoup(String url, String dir) {
		Document doc;
		
			File folder = new File(dir);
			File[] listOfFiles = folder.listFiles();
			if (listOfFiles != null && listOfFiles.length > 0) {
				for(int i = 0; i<listOfFiles.length; i++){	//	listOfFiles.length
					FileReader inputFile = null;
					try {
						inputFile = new FileReader(listOfFiles[i]);
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					String[] dayArray = listOfFiles[0].getName().split("---");
					//Instantiate the BufferedReader Class
			        BufferedReader bufferReader = new BufferedReader(inputFile);
			        //Variable to hold the one line data
			        String line;
			        // Read file line by line and print on the console
			        try {
			        	while ((line = bufferReader.readLine()) != null)   {			        		
			        		String[] seperateLine = line.split("--");
			        		String titleName = seperateLine[0].replace("-", " ");
			        		titleName = titleName.substring(1, titleName.length());
			        		Title title = new Title();
			        		title.setName(titleName);
			        		title.setLink(line);
			        		title.setDate(dayArray[0]);
			        		titleManager.addTitle(title);
			        		
			        		Title idTitle = titleManager.getLastSavedTitle();
			        		for(int a = 1; a < 300; a++) {
			        			try {
				        			if (a != 1) {			        				
				        				doc = Jsoup.connect(url + line +"?p="+a).get();
				        				      		
				        			} else  {
				        				doc = Jsoup.connect(url + line).get();
				        			}
				        			Elements ulIds = doc.select("ul[id='entry-list']");
			        				for(Element element : ulIds) {
			        					Elements lis = element.select("li");
			        					for (Element liElement : lis) {
			        						shredTheHTML(liElement, idTitle);
			        					}
			        				}
			        			} catch (Exception e) {
			        				System.err.println("HATA");
			        				break;
			        			}		        					  
			        		}
			        	}
			        	
			        } catch (Exception e) {
			        	System.err.println("Veri okuma sýrasýnda beklenmeyen bir hata oluþtu");
						e.printStackTrace();
			        }
			        
			      //Close the buffer reader
			        try {
			        	bufferReader.close();
					}catch (IOException e) {
						System.err.println("Buffer Reader ýn kapatýlmasý sýrasýnda kritik bir hata oluþtu");
						e.printStackTrace();
					}
				}
			}				
		
	}
	
	@Override
	public void getLinksFromMainPage(String url) {
		Document doc;
		String title = getToday();
		try {
			doc = Jsoup.connect(url).get();
			Elements links = doc.select("a[href]");
			List<String> todayPopularUrlList = new ArrayList<String>();
			for(Element element : links) {
				if(element.toString().contains("?a=popular")) {
					todayPopularUrlList.add(element.attr("href"));
				}
			}
			if (todayPopularUrlList != null && todayPopularUrlList.size() > 0) {
				createTxtForLink(todayPopularUrlList, "urlList---"+title);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getToday() {
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		Date date = new Date();
		System.out.println(dateFormat.format(date)); //2016/11/16 12:08:43
		return dateFormat.format(date);
	}
	
	private void shredTheHTML(Element liElement, Title idTitle) {
		//Entry nin author bilgisi
		String dataAuthor = liElement.attr("data-author");
		if(dataAuthor == null) {
			System.err.println("Yazar bulunamadý. Kontrol ediniz!! ---" + idTitle.getName());
		}
		Elements contentElement = liElement.select("div[class='content']");
		//Entry içeriði
		if(contentElement != null && contentElement.size() > 0) {
			String content = contentElement.text();			
			Elements infoElement = liElement.select("div[class='info']");
			Elements elementDateInfo = infoElement.select("a[class='entry-date permalink']");
			//entry Linki
			String entryLink = elementDateInfo.attr("href");
			//Entry Tarihi
			if(elementDateInfo != null && elementDateInfo.size() > 0) {				
				String entryDate = elementDateInfo.text();
				Entry entry = new Entry();
				entry.setTitle(idTitle);
				if(content.contains("'")) {
					content = content.replace("'", " ");
				}
				entry.setDescription(content);
				entry.setEntryLink(entryLink);
				int entryId = entryManager.getEntryWithEntryLink(entry.getEntryLink()) ;
				if (entryId == 0) {					
					User user = userManager.getUserByUsername(dataAuthor);
					if(user != null) {		        							
						entry.setUser(user);
					} else {
						User newUser = new User();
						newUser.setName("-");
						newUser.setSurname("-");
						newUser.setNickname(dataAuthor);
						userManager.addUser(newUser);
						newUser = userManager.getUserByUsername(dataAuthor);
						entry.setUser(newUser);
					}
					entryDate = entryDate.substring(0, 10);
					String[] entryDateArray = entryDate.split("\\.");
					entryDate = entryDateArray[2]+"-"+entryDateArray[1]+"-"+entryDateArray[0];
					entry.setDate(entryDate);
					entry.setTitleId(idTitle.getId());
					entry.setUserId(entry.getUser().getId());
					boolean entrySaved = entryManager.addEntry(entry);
					if(entrySaved) {
						System.out.println("Entry kaydedildi." + entry.getDescription());
					}
				}
			} else {
				System.err.println("Entry tarih bilgisi bulunamadýðý için iþleme devam edilemiyor. Kontrol ediniz!---" + idTitle.getName());
			}
		} else {
			System.err.println("Entry content i bulunamadýðý için iþleme devam edilemiyor. Kontrol ediniz!---" + idTitle.getName());
		}
	}
	
	@Override
	public void removeZeroCountTitles() {
		
	}
	
	@Override
	public void findDuplicateTitlesAndMerge() {
		List<Title> allTitles = titleManager.getAllTitles();
		if (allTitles != null && allTitles.size() > 0) {
			Map<String, List<Integer>> titleDescriptionIdList = new HashMap<String , List<Integer>>();
			// Sistemdeki tüm title larýn arasýnda gez ve ismi ayný olan title larý id lere grupla.
			for (Title title : allTitles) {
				if (titleDescriptionIdList.containsKey(title.getName())) {
					List<Integer> idList = titleDescriptionIdList.get(title.getName());
					idList.add(title.getId());
					titleDescriptionIdList.put(title.getName(), idList);
				} else {
					List<Integer> idList = new ArrayList<Integer>();
					idList.add(title.getId());
					titleDescriptionIdList.put(title.getName(), idList);
				}
			}
			//gruplama tamam, þimdi her baþlýk için entrylist leri al ve fazla olana gönder.
			for (Map.Entry<String, List<Integer>> entry : titleDescriptionIdList.entrySet()) {
				if (!entry.getValue().isEmpty() && entry.getValue().size() > 1) {
					List<Integer> idList   = entry.getValue();
					Map<Integer, List<Entry>> idEntryList = new HashMap<Integer, List<Entry>>();
					for (Integer i : idList) {
						List<Entry> entryList = entryManager.getEntriesWithTitleId(i);
						idEntryList.put(i, entryList);
					}
					int count = 1;
					int centralTitleId = 0;
					for (Map.Entry<Integer, List<Entry>> d : idEntryList.entrySet()) {
						if (count == 1) {
							centralTitleId = d.getKey();
						} else {
							List<Entry> entryList = d.getValue();
							for (Entry e : entryList) {
								entryManager.updateEntryTitle(e.getId(), centralTitleId);
							}
							titleManager.removeTitleWithId(d.getKey());
						}
						count++;
					}
					
				}
			}
		}
	}
	
	/**
	 * @param parameterForEntryCount = 0 ise tüm entryler üzerinden iþlem yapýlýr
	 * CoOccurence matrix için gerekli hesaplamalarý yaparak çýktý üreten metoddur.
	 */
	@Override
	public void createCoOccurenceMatrix(int parameterForEntryCount) {
		System.out.println("Co-Occurence matrix operation is just started!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null && activeEntryList.size() > 0) {
			if (parameterForEntryCount == 0) {
				parameterForEntryCount = activeEntryList.size();
			}
			Map<String, Integer> MOW = getMostOccuredWordMap(activeEntryList, parameterForEntryCount);
			//bütyükten küçüðe sýralanmýþ map
			Map<String, Integer> ordered = sortByValue(MOW, false);
			Set<String> strSet = ordered.keySet();
			Map<String, Integer> ranking = new HashMap<String, Integer>();
			int orderCount = 0;
			for (String abc : strSet) {
				ranking.put(abc, orderCount);
				orderCount++;
			}
			Map<String, Integer> rankingOrdered = new HashMap<String, Integer>();
			rankingOrdered = sortByValue(ranking, true);
			createTxtFileForVocabs(rankingOrdered);
			//TODO GarbageCollector Error
//				createExcelRankingWords(rankingOrdered);
			System.out.println("Ranking oluþturuldu!");
			List<String> retList = new ArrayList<String> ();
			for (int i=0; i < parameterForEntryCount; i++) {				
				retList = splittedEntryDescription(retList, activeEntryList.get(i).getDescription());
			}
			//Benim çözümüm(KeyIndex)
			Map<KeyIndex, Integer> matrixData = new  HashMap<KeyIndex, Integer>();
			System.out.println("KeyIndex çözümü baþladý!");
			for (int i = 0; i < retList.size(); i++) {
				int countBack = i - 1;
				int countGo = i + 1;
				int numberOfCellForPivotWord = ranking.get(retList.get(i));
				String pivotWord = retList.get(i);
				for (int j = 0; j < turningNumber; j++) {
					if (countBack > -1) {
						int numberOfCellForAlternativeWord = ranking.get(retList.get(countBack));
						KeyIndex ind = new KeyIndex(numberOfCellForPivotWord, numberOfCellForAlternativeWord, pivotWord, retList.get(countBack));
						KeyIndex symIndex = new KeyIndex(numberOfCellForAlternativeWord, numberOfCellForPivotWord, retList.get(countBack), pivotWord);
						if(matrixData.get(ind) == null && matrixData.get(symIndex) == null){
							matrixData.put(ind, 1);
						}
						countBack--;
					}
					if (countGo < retList.size()) {
						int numberOfCellForAlternativeWord = ranking.get(retList.get(countGo));
						KeyIndex ind = new KeyIndex(numberOfCellForPivotWord, numberOfCellForAlternativeWord, pivotWord, retList.get(countGo));
						KeyIndex symIndex = new KeyIndex(numberOfCellForAlternativeWord, numberOfCellForPivotWord, retList.get(countGo), pivotWord);
						if(!matrixData.containsKey(ind) && !matrixData.containsKey(symIndex)){
							matrixData.put(ind, 1);
						}
						countGo++;
					}
				}
			}
			System.out.println("KexIndex hesaplandý. TXT oluþturuluyor!");
			createTxtForBigCLAMFromMap(matrixData, false);
			createTxtForBigCLAMFromMap(matrixData, true);
		}
	}
	/**
	 * 
	 * @param mapList
	 * @param forBigClam - BigClam algoritmasýna input olarak verilecekse true gönderilmelidir
	 * BigClam algoritmasýnýn input unu oluþturan metoddur.
	 */
	private static void createTxtForBigCLAMFromMap(Map<KeyIndex, Integer> mapList, boolean forBigClam){
		try{
			if (forBigClam) {				
				BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
				for(Map.Entry<KeyIndex,Integer>  entrySet  : mapList.entrySet()){
					KeyIndex ind = entrySet.getKey();
					out.write(ind.getRow()+"	"+ ind.getColumn()+"\r\n");
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} else {
				BufferedWriter out = new BufferedWriter(new FileWriter("neigbors.txt"));
				for(Map.Entry<KeyIndex,Integer>  entrySet  : mapList.entrySet()){
					KeyIndex ind = entrySet.getKey();
					out.write(ind.getRow()+"	"+"["+ind.getRowWord()+"]"+"     "+ ind.getColumn()+"["+ ind.getColumnWord()+"]"+"\r\n");
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			}
		}
		catch (IOException e) {
			System.err.println("TXT oluþturulurken hata oluþtu!");
        }
	}
	/**
	 * 
	 * @param map
	 * @param isASC
	 * @return
	 * Kelimeleri max geçenden min geçene göre sýralayan metoddur.
	 */
	@SuppressWarnings("hiding")
	private static <String, Integer> Map<String, Integer> sortByValue(Map<String, Integer> map, boolean isASC) {
		List<java.util.Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
		if (!isASC) {
			Collections.sort(list, new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
					return ((Comparable<Integer>) ((Map.Entry<String, Integer>) (o2)).getValue())
							.compareTo(((Map.Entry<String, Integer>) (o1)).getValue());
				}
			});
		} else {
			Collections.sort(list, new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
					return ((Comparable<Integer>) ((Map.Entry<String, Integer>) (o1)).getValue())
							.compareTo(((Map.Entry<String, Integer>) (o2)).getValue());
				}
			});
		}
	    Map<String,Integer> result = new LinkedHashMap<>();
	    for (Iterator<java.util.Map.Entry<String,Integer>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<String,Integer> entry = (Map.Entry<String,Integer>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}

	private Map<String, Integer> getMostOccuredWordMap(List<Entry> activeEntryList, int count) {
		Map<String, Integer> mostOccuredWords = new HashMap<String, Integer>();
		if (count == 0) {
			count = activeEntryList.size();
		}
		for (int a = 0; a < count; a++) {
			List<String> retList = new ArrayList<String>(); 
			retList = splittedEntryDescription(retList, activeEntryList.get(a).getDescription());
			for (int i=0; i<retList.size(); i++) {					
				if (mostOccuredWords != null && mostOccuredWords.size() > 0) {
					if (mostOccuredWords.containsKey(retList.get(i))) {
						mostOccuredWords.put(retList.get(i), mostOccuredWords.get(retList.get(i)) + 1);
					} else {
						mostOccuredWords.put(retList.get(i), 1);
					}
				} else {
					mostOccuredWords.put(retList.get(i), 1);
				}
			}
		}
		System.out.println("Kelimelerin kaçar defa geçtiði hesaplandý!");
		return mostOccuredWords;
	}
	
	@Override
	public List<String> splittedEntryDescription(List<String> retList, String entryDescription) {
		String [] arr = entryDescription.split(" ");
		for (int i =0; i<arr.length; i++) {
			retList.add(arr[i].toLowerCase());
		}
		return retList;
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
	
	
	private void createTxtFileForVocabs(Map<String, Integer> ranked) {
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
	public void writeAllEntriesToDocument() {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("outputTxt.txt"));
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
	
	@Override
	public void writeSpecificEntryCountToDocument(int entryCount) {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("outputTxt.txt"));
				if (entryCount < activeEntryList.size()) {					
					for (int i =0; i< entryCount; i++) {
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
}
