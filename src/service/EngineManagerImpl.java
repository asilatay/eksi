package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
	
	@Override
	public void createCoOccurenceMatrix() {
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null && activeEntryList.size() > 0) {
			Map<String, Integer> MOW = getMostOccuredWordMap(activeEntryList);
			//bütyükten küçüðe sýralanmýþ map
			Map<String, Integer> ordered = sortByValue(MOW, false);
			Set<String> strSet = ordered.keySet();
			Map<String, Integer> ranking = new HashMap<String, Integer>();
			int orderCount = 0;
			for (String abc : strSet) {
				ranking.put(abc, orderCount);
				orderCount++;
			}
			//Benim çözümüm(KeyIndex)
			Map<KeyIndex, Integer> matrixData = new  HashMap<KeyIndex, Integer>();
			for (int i = 0; i < activeEntryList.size(); i++) {
				int countBack = i - 1;
				int countGo = i + 1;
				int numberOfCellForPivotWord = ranking.get(activeEntryList.get(i));
				for (int j = 0; j < turningNumber; j++) {
					if (countBack > -1) {
						int numberOfCellForAlternativeWord = ranking.get(activeEntryList.get(countBack));
						KeyIndex ind = new KeyIndex(numberOfCellForPivotWord, numberOfCellForAlternativeWord);
						KeyIndex symIndex = new KeyIndex(numberOfCellForAlternativeWord, numberOfCellForPivotWord);
						if(matrixData.get(ind) == null && matrixData.get(symIndex) == null){
							matrixData.put(ind, 1);
						}
						countBack--;
					}
					if (countGo < activeEntryList.size()) {
						int numberOfCellForAlternativeWord = ranking.get(activeEntryList.get(countGo));
						KeyIndex ind = new KeyIndex(numberOfCellForPivotWord, numberOfCellForAlternativeWord);
						KeyIndex symIndex = new KeyIndex(numberOfCellForAlternativeWord, numberOfCellForPivotWord);
						if(matrixData.get(ind) == null && matrixData.get(symIndex) == null){
							matrixData.put(ind, 1);
						}
						countGo++;
					}
				}
			}
			createTxtForBigCLAMFromMap(matrixData);
		}
	}
	
	private static void createTxtForBigCLAMFromMap(Map<KeyIndex, Integer> mapList){
		try{
			 BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
			 for(Map.Entry<KeyIndex,Integer>  entrySet  : mapList.entrySet()){
				 KeyIndex ind = entrySet.getKey();
				 out.write(ind.getRow()+"	"+ ind.getColumn()+"\r\n");
			 }
			 out.close();
		}
		catch (IOException e) {
        	
        }
	}
	
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

	private Map<String, Integer> getMostOccuredWordMap(List<Entry> activeEntryList) {
		Map<String, Integer> mostOccuredWords = new HashMap<String, Integer>();
		for (Entry e : activeEntryList) {
			List<String> retList = splittedEntryDescription(e.getDescription());
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
		return mostOccuredWords;
	}
	
	@Override
	public List<String> splittedEntryDescription(String entryDescription) {
		List<String> returnList = new ArrayList<String>();
		String [] arr = entryDescription.split(" ");
		for (int i =0; i<arr.length; i++) {
			returnList.add(arr[i].toLowerCase());
		}
		return returnList;
	}
}
