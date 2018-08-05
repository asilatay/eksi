package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import commons.DateUtil;
import model.Entry;
import model.KeyIndexOld;
import model.User;
import viewmodel.CommonCommunityResult;
import viewmodel.CosineSimilarityIndex;
import viewmodel.MostSimilarWord;
import viewmodel.PMIValueIndexes;
import viewmodel.TitleEntry;
import viewmodel.UserCommunity;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;
import viewmodel.UserWord;
import viewmodel.WordCommunity;
import viewmodel.WordIndex;

public class EngineManagerImpl implements EngineManager {
	
	TitleManager titleManager = new TitleManagerImpl();
	
	UserManager userManager = new UserManagerImpl();
	
	EntryManager entryManager = new EntryManagerImpl();
	
	ExportManager exportManager = new ExportManagerImpl();
	
	DateUtil dateUtil = new DateUtil();
	
	ImportManager importManager = new ImportManagerImpl();
	
	private final static int turningNumber = 15;
	
	private final static int turningNumberForNewCoOccurence = 10;
	
	private final static String directoryOfSimilarUsersThatWroteSameTitle = "userUserTitles.txt";
	
	
	//Matrix oluşturmada kullanılan parametreler
	private final static int globalColumnCount = 1000000;
	
	private final static int globalRowCount = 1000;
	
	private final static int globalRowCountSmall = 0;
	
	
	//PMI hesabı için kullanılan parametreler
	private final static int bigRowCountPMI = 10000;
	
	private final static int smallRowCountPMI = 4000;
	
	//ALTERNATE PMI hesabı için kullanılan parametreler
	private final static int bigRowCountAlternatePMI = 2000;
	
	private final static int smallRowCountAlternatePMI = 0;
	
	//BigCLAM algoritması inputunu oluşturacak parametre
	private final static int bigClamNumberOfOccurrences = 21;
	
	
	private final static int globalMostSimilarWordCount = 20;
	
	private final static int globalTotalUserEntryCount = 30;
	
	private final static String directoryOfTitleCountOfUsers = "userTitle.txt";
	
	public EngineManagerImpl() {
	}
	
	
	/**
	 * @param parameterForEntryCount = 0 ise tüm entryler üzerinden işlem yapılır
	 * CoOccurence matrix için gerekli hesaplamaları yaparak çıktı üreten metoddur.
	 * Bu metod eski modellerle üretilmiş bir metoddur ve geçmiş görülsün diye tutuluyor
	 */
	@Override
	public void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount) {
		System.out.println("Co-Occurence matrix operation is just started!");
		List<Entry> activeEntryList = entryManager.getAllEntriesOrderByDate();
		if (activeEntryList != null && activeEntryList.size() > 0) {
			if (parameterForEntryCount == 0) {
				parameterForEntryCount = activeEntryList.size();
			}
			Map<String, Integer> MOW = getMostOccuredWordMap(activeEntryList, parameterForEntryCount);
			//bütyükten küçüğe sıralanmış map
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
			entryManager.createTxtFileForVocabs(rankingOrdered);
			//TODO GarbageCollector Error
//				createExcelRankingWords(rankingOrdered);
			System.out.println("Ranking oluşturuldu!");
			List<String> retList = new ArrayList<String> ();
			for (int i=0; i < parameterForEntryCount; i++) {				
				retList = splittedEntryDescription(retList, activeEntryList.get(i).getDescription());
			}
			//Benim çözümüm(KeyIndex)
			Map<KeyIndexOld, Integer> matrixData = new  HashMap<KeyIndexOld, Integer>();
			System.out.println("KeyIndex çözümü başladı!");
			for (int i = 0; i < retList.size(); i++) {
				int countBack = i - 1;
				int countGo = i + 1;
				int numberOfCellForPivotWord = ranking.get(retList.get(i));
				String pivotWord = retList.get(i);
				for (int j = 0; j < turningNumber; j++) {
					if (countBack > -1) {
						int numberOfCellForAlternativeWord = ranking.get(retList.get(countBack));
						KeyIndexOld ind = new KeyIndexOld(numberOfCellForPivotWord, numberOfCellForAlternativeWord, pivotWord, retList.get(countBack));
						KeyIndexOld symIndex = new KeyIndexOld(numberOfCellForAlternativeWord, numberOfCellForPivotWord, retList.get(countBack), pivotWord);
						if(matrixData.get(ind) == null && matrixData.get(symIndex) == null){
							matrixData.put(ind, 1);
						}
						countBack--;
					}
					if (countGo < retList.size()) {
						int numberOfCellForAlternativeWord = ranking.get(retList.get(countGo));
						KeyIndexOld ind = new KeyIndexOld(numberOfCellForPivotWord, numberOfCellForAlternativeWord, pivotWord, retList.get(countGo));
						KeyIndexOld symIndex = new KeyIndexOld(numberOfCellForAlternativeWord, numberOfCellForPivotWord, retList.get(countGo), pivotWord);
						if(!matrixData.containsKey(ind) && !matrixData.containsKey(symIndex)){
							matrixData.put(ind, 1);
						}
						countGo++;
					}
				}
			}
			System.out.println("KexIndex hesaplandı. TXT oluşturuluyor!");
			createTxtForBigCLAMFromMap(matrixData, false);
			createTxtForBigCLAMFromMap(matrixData, true);
		}
	}
	/**
	 * 
	 * @param mapList
	 * @param forBigClam - BigClam algoritmasına input olarak verilecekse true gönderilmelidir
	 * BigClam algoritmasının input unu oluşturan metoddur.
	 */
	private static void createTxtForBigCLAMFromMap(Map<KeyIndexOld, Integer> mapList, boolean forBigClam){
		try{
			if (forBigClam) {				
				BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
				for(Map.Entry<KeyIndexOld,Integer>  entrySet  : mapList.entrySet()){
					KeyIndexOld ind = entrySet.getKey();
					out.write(ind.getRow()+"	"+ ind.getColumn()+"\r\n");
				}
				out.close();
				System.out.println("TXT oluşturuldu.!");
			} else {
				BufferedWriter out = new BufferedWriter(new FileWriter("neigbors.txt"));
				for(Map.Entry<KeyIndexOld,Integer>  entrySet  : mapList.entrySet()){
					KeyIndexOld ind = entrySet.getKey();
					out.write(ind.getRow()+"	"+"["+ind.getRowWord()+"]"+"     "+ ind.getColumn()+"["+ ind.getColumnWord()+"]"+"\r\n");
				}
				out.close();
				System.out.println("TXT oluşturuldu.!");
			}
		}
		catch (IOException e) {
			System.err.println("TXT oluşturulurken hata oluştu!");
        }
	}
	/**
	 * 
	 * @param map
	 * @param isASC
	 * @return
	 * Kelimeleri max geçenden min geçene göre sıralayan metoddur.
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
		System.out.println("Kelimelerin kaçar defa geçtiği hesaplandı!");
		return mostOccuredWords;
	}
	
	@Override
	public List<String> splittedEntryDescription(List<String> retList, String entryDescription) {
		String [] arr = entryDescription.split(" ");
		for (int i =0; i<arr.length; i++) {
			if (arr[i].equals(",") || arr[i].equals(".") || arr[i].equals("!") || arr[i].equals(":") 
					|| arr[i].equals(";") || arr[i].equals("...") ||arr[i].equals("?") || arr[i].equals("\"")) {
				continue;
			}
			
			retList.add(arr[i].toLowerCase());
		}
		return retList;
	}
	
	@Override
	public void saveCoOccurrenceMatrixToDatabase(String readTextPath, List<String> outputFromAnotherFunction) {
		System.out.println("Co-Occurrence matrix oluşturma operasyonu tetiklendi");
		
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		System.out.println("Bir kelimenin kaç defa sistemde görüldüğüyle ilgili liste oluşturuluyor");
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		createOutputForWordsOccured(wordsOccured);
		createOutputNodeIdNodeNameForBigClam(wordsOccured);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		
		List<String> splittedEntries = new ArrayList<String> ();
		for (String s : readFromTxtEntries) {				
			splittedEntries = splittedEntryDescription(splittedEntries, s);
		}
		
		readFromTxtEntries.clear();
		
		System.out.println("PMI Value Indexes çözümü başladı!");
		
		System.out.println("SplittedEntries Size : " + splittedEntries.size());
		
		//Read i value for resuming
		int newI = 0;
		try {
			BufferedReader  in = new BufferedReader(new FileReader("infoI.txt"));
			if (in != null) {			
				String line;
				while ((line = in.readLine()) != null) {
					newI = Integer.parseInt(line);
				}
				in.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int newICount = 0;
		
		for (int i = 0; i < splittedEntries.size(); i++) {
			if (newICount == 0) {
				i = newI;
				newICount++;
			}
			int countGo = i + 1;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					PMIValueIndexes ind = new PMIValueIndexes(numberOfCellForPivotWord, numberOfCellForAlternativeWord, BigDecimal.ZERO, BigDecimal.ZERO);
					PMIValueIndexes symIndex = new PMIValueIndexes(numberOfCellForAlternativeWord, numberOfCellForPivotWord, BigDecimal.ZERO, BigDecimal.ZERO);
					if (ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount && ind.getIndex1() != ind.getIndex2()) {
						PMIValueIndexes storageIndex = entryManager.getPMIValueIndexes(ind.getIndex1(), ind.getIndex2());
						if (storageIndex != null) {
							int freq = storageIndex.getFrequencyInTogether();
							freq++;
							storageIndex.setFrequencyInTogether(freq);
							
							entryManager.updateStorageIndex(storageIndex);
							
							if (symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount) {
								PMIValueIndexes symStorageIndex = new PMIValueIndexes();
								symStorageIndex.setIndex1(symIndex.getIndex1());
								symStorageIndex.setIndex2(symIndex.getIndex2());
								symStorageIndex.setFrequencyInTogether(freq);
								
								entryManager.updateStorageIndex(symStorageIndex);
							}
							
						} else {
							ind.setFrequencyInTogether(1);
							ind.setLogaritmicPmiValue(BigDecimal.ZERO);
							ind.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
							entryManager.saveStorageIndex(ind);
							
							if (symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount) {								
								symIndex.setFrequencyInTogether(1);
								symIndex.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
								symIndex.setLogaritmicPmiValue(BigDecimal.ZERO);
								
								
								entryManager.saveStorageIndex(symIndex);
							}
						}
					} else if (symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount && symIndex.getIndex1() != symIndex.getIndex2()) {
						PMIValueIndexes storageIndex = entryManager.getPMIValueIndexes(symIndex.getIndex1(), symIndex.getIndex2());
						if (storageIndex != null) {
							int freq = storageIndex.getFrequencyInTogether();
							freq++;
							storageIndex.setFrequencyInTogether(freq);
							
							entryManager.updateStorageIndex(storageIndex);
							
							if (ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount) {
								PMIValueIndexes symStorageIndex = new PMIValueIndexes();
								symStorageIndex.setIndex1(ind.getIndex1());
								symStorageIndex.setIndex2(ind.getIndex2());
								symStorageIndex.setFrequencyInTogether(freq);
								
								entryManager.updateStorageIndex(symStorageIndex);
							}
							
						} else {
							symIndex.setFrequencyInTogether(1);
							symIndex.setLogaritmicPmiValue(BigDecimal.ZERO);
							symIndex.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
							entryManager.saveStorageIndex(symIndex);
							
							if (ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount) {								
								ind.setFrequencyInTogether(1);
								ind.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
								ind.setLogaritmicPmiValue(BigDecimal.ZERO);
								
								
								entryManager.saveStorageIndex(ind);
							}
						}
					}
					countGo++;
				}
			}
			
			if (i % 200 == 0) {
				System.out.println("Matrix veritabanında oluşturuluyor -> i = " + i + " --- KALAN -> "
						+ (splittedEntries.size() - i));
				try {
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("infoI.txt"), "UTF8"));

					out.write(String.valueOf(i));
					out.flush();
					out.close();

					System.out.println("I nin yeni değeri kaydedildi!");
				}
				catch (IOException e) {
					System.err.println("TXT oluşturulurken hata oluştu!");
		       }
				
			}
		}
		
		createBigClamInputFromDatabase();
		
		splittedEntries.clear();
		ranking.clear();

		//Matrix oluşturulduğunda zaten verilen aralık kadar hesaplama yapılmışsa burayı atlayarak zaman kazan
		int multipliedValue = globalRowCount * globalColumnCount;
		int totalCalculatedCount = entryManager.getTotalCountWithProcessIdPMIValueIndex(1);
		
		if (totalCalculatedCount != multipliedValue) {			
			for (int i = 0; i < globalRowCount; i++) {
				int tempI = i;
				List<PMIValueIndexes> index1List = entryManager.getPMIValueIndexListWithIndex1(i);
				if (CollectionUtils.isNotEmpty(index1List)) {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						Optional<PMIValueIndexes> optIndex2 = index1List.stream().filter(a -> a.getIndex2() == tempJ)
								.findFirst();
						if (!optIndex2.isPresent()) {
							PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
							newIndex.setFrequencyInTogether(0);
							newIndex.setLogaritmicPmiValue(BigDecimal.ZERO);
							newIndex.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
							
							entryManager.saveStorageIndex(newIndex);
						}
					}
				} else {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
						newIndex.setFrequencyInTogether(0);
						newIndex.setLogaritmicPmiValue(BigDecimal.ZERO);
						newIndex.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
						
						entryManager.saveStorageIndex(newIndex);
					}
				}
				
				if (i % 100 == 0) {
					System.out.println("Eksik eşleşmeler tamamlanıyor. i = " + i);
				}
			}
		}
		totalCalculatedCount = entryManager.getTotalCountWithProcessIdPMIValueIndex(1);
		
		System.out.println("Matrix oluşturuldu... Size= " + totalCalculatedCount);
	}
	
	@Override
	public void createCoOccurenceMatrixWithMemoryAndDisk(String readTextPath, List<String> outputFromAnotherFunction) {
		System.out.println("Co-Occurrence matrix oluşturma operasyonu tetiklendi");
		System.out.println("Small Row Count : " + globalRowCountSmall + " --- Big Row Count : " + globalRowCount);
		
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		System.out.println("Bir kelimenin kaç defa sistemde görüldüğüyle ilgili liste oluşturuluyor");
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		createOutputForWordsOccured(wordsOccured);
		createOutputNodeIdNodeNameForBigClam(wordsOccured);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		
		List<String> splittedEntries = new ArrayList<String> ();
		for (String s : readFromTxtEntries) {				
			splittedEntries = splittedEntryDescription(splittedEntries, s);
		}
		
		readFromTxtEntries.clear();
		
		Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
		
		System.out.println("PMI Value Indexes çözümü başladı!");
		
		for (int i = 0; i < splittedEntries.size(); i++) {
			int countGo = i + 1;
			int countBack = i - 1 ;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countBack > -1) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countBack));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countBack--;
				}
				
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countGo++;
				}
			}
		}
		
		createBigClamInputFromMatrix(matrixData);
		
		splittedEntries.clear();
		ranking.clear();

		//Matrix oluşturulduğunda zaten verilen aralık kadar hesaplama yapılmışsa burayı atlayarak zaman kazan
		int multipliedValue = (globalRowCount - globalRowCountSmall) * globalColumnCount;
		if (matrixData.size() != multipliedValue) {			
			for (int i = globalRowCountSmall; i < globalRowCount; i++) {
				int tempI = i;
				List<PMIValueIndexes> index1List = matrixData.keySet().stream()
						.filter(a -> a.getIndex1() == tempI).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(index1List)) {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						Optional<PMIValueIndexes> optIndex2 = index1List.stream().filter(a -> a.getIndex2() == tempJ)
								.findFirst();
						if (!optIndex2.isPresent()) {
							PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
							matrixData.put(newIndex, BigDecimal.ZERO);
						}
					}
				} else {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
						matrixData.put(newIndex, BigDecimal.ZERO);
					}
				}
			}
		}
		System.out.println("Matrix oluşturuldu... Size= " + matrixData.size());
		
		entryManager.savePMIValueIndexes(matrixData);
	}


	private void createMatrixData(Map<PMIValueIndexes, BigDecimal> matrixData, int numberOfCellForPivotWord,
			int numberOfCellForAlternativeWord) {
		PMIValueIndexes ind = new PMIValueIndexes(numberOfCellForPivotWord, numberOfCellForAlternativeWord, BigDecimal.ZERO, BigDecimal.ZERO);
		PMIValueIndexes symIndex = new PMIValueIndexes(numberOfCellForAlternativeWord, numberOfCellForPivotWord, BigDecimal.ZERO, BigDecimal.ZERO);
		if (matrixData.containsKey(ind) 
				&& (ind.getIndex1() >= globalRowCountSmall && ind.getIndex1() < globalRowCount) 
				&& ind.getIndex2() < globalColumnCount
				&& ind.getIndex1() != ind.getIndex2()) {
			matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
			if ((symIndex.getIndex1() >= globalRowCountSmall && symIndex.getIndex1() < globalRowCount) && symIndex.getIndex2() < globalColumnCount) {							
				if (matrixData.get(symIndex) != null) {							
					matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
				} else {
					matrixData.put(symIndex, BigDecimal.ONE);
				}
			}
		} else if (matrixData.containsKey(symIndex) 
				&& (symIndex.getIndex1() >= globalRowCountSmall && symIndex.getIndex1() < globalRowCount) 
				&& symIndex.getIndex2() < globalColumnCount
				&& symIndex.getIndex1() != symIndex.getIndex2()) {
			matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
			if ((ind.getIndex1() >= globalRowCountSmall && ind.getIndex1() < globalRowCount) && ind.getIndex2() < globalColumnCount) {							
				if (matrixData.get(ind) != null) {							
					matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
				} else {
					matrixData.put(ind, BigDecimal.ONE);
				}
			}
		} else if ((ind.getIndex1() >= globalRowCountSmall && ind.getIndex1() < globalRowCount) 
				&& ind.getIndex2() < globalColumnCount
				&& ind.getIndex1() != ind.getIndex2()){
			matrixData.put(ind, BigDecimal.ONE);
			if ((symIndex.getIndex1() >= globalRowCountSmall && symIndex.getIndex1() < globalRowCount) && symIndex.getIndex2() < globalColumnCount) {
				matrixData.put(symIndex, BigDecimal.ONE);
			}
		}
	}
	
	@Override
	public void createCoOccurenceMatrix(String readTextPath, List<String> outputFromAnotherFunction) {
		System.out.println("Co-Occurrence matrix oluşturma operasyonu başladı!");
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		System.out.println("Bir kelimenin kaç defa sistemde görüldüğüyle ilgili liste oluşturuluyor");
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		createOutputForWordsOccured(wordsOccured);
		createOutputNodeIdNodeNameForBigClam(wordsOccured);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		
		List<String> splittedEntries = new ArrayList<String> ();
		for (String s : readFromTxtEntries) {				
			splittedEntries = splittedEntryDescription(splittedEntries, s);
		}
		
		readFromTxtEntries.clear();
		Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
		System.out.println("PMI Value Indexes çözümü başladı!");
		
		for (int i = 0; i < splittedEntries.size(); i++) {
			int countGo = i + 1;
			int countBack = i - 1;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countBack > - 1) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countBack));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countBack--;
				}
				
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countGo++;
				}
			}
		}
		
		createBigClamInputFromMatrix(matrixData);
		
		splittedEntries.clear();
		int rankingSize = ranking.size();
		ranking.clear();

		//Matrix oluşturulduğunda zaten verilen aralık kadar hesaplama yapılmışsa burayı atlayarak zaman kazan
		int multipliedValue = globalRowCount * globalColumnCount;
		if (matrixData.size() != multipliedValue) {			
			for (int i = 0; i < globalRowCount; i++) {
				int tempI = i;
				List<PMIValueIndexes> index1List = matrixData.keySet().stream()
						.filter(a -> a.getIndex1() == tempI).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(index1List)) {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						Optional<PMIValueIndexes> optIndex2 = index1List.stream().filter(a -> a.getIndex2() == tempJ)
								.findFirst();
						if (!optIndex2.isPresent()) {
							PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
							matrixData.put(newIndex, BigDecimal.ZERO);
						}
					}
				} else {
					for (int j = 0; j < globalColumnCount; j++) {
						int tempJ = j;
						PMIValueIndexes newIndex = new PMIValueIndexes(tempI, tempJ, BigDecimal.ZERO, BigDecimal.ZERO);
						matrixData.put(newIndex, BigDecimal.ZERO);
					}
				}
			}
		}
		System.out.println("Matrix oluşturuldu... Size= " + matrixData.size());
		//Co occurence matrix oluşturma tamamlandı, PMI Değerini hesaplayacağız.
		matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, rankingSize);
		
		//TODO alternate i açarken map of indexes aç
		Map<Integer, List<String>> mapOfIndexes = getMapOfIndexes(matrixData);
		
		// Alternate PMI değerini hesaplayacağız.
		matrixData = calculateAndSetAlternatePMIValues (matrixData, mapOfIndexes, rankingSize);
		mapOfIndexes.clear();
		
		//TODO burada totalWordNumberBigData yerine tüm veri çalıştığında ranking size yazar!!
		createVectorsOneByOneWithVirtualMatrix(matrixData, globalRowCount, globalColumnCount);
		
		findMostSimilarWords(matrixData, globalRowCount);
		
	}
	
	@Override
	public void calculatePMIValuesWithMemoryAndDisk(String readTextPath, List<String> outputFromAnotherFunction) {
		System.out.println("PMI ve Alternate PMI değerlerini hesaplama operasyonu tetiklendi");
		System.out.println("Small Row Count : " + smallRowCountPMI + " --- Big Row Count : " + bigRowCountPMI);
		System.out.println("Dosyalar okunarak memory e alınıyor");
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		int rankingSize = ranking.size();
		ranking.clear();
		List<Integer> sequenceList = new ArrayList<Integer>();
		for (int i = smallRowCountPMI; i < bigRowCountPMI; i++) {
			if (i % 100 == 0 && CollectionUtils.isNotEmpty(sequenceList)) {
				sequenceList.add(i);
				
				List<PMIValueIndexes> indexList = entryManager.getPMIValueIndexAllValueWithIndex1(sequenceList);
				Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
				//matrixData oluşturuldu.
				for (PMIValueIndexes ind : indexList) {
					BigDecimal freqWith = new BigDecimal(ind.getFrequencyInTogether());
					matrixData.put(ind, freqWith);
				}
				// Co occurence matrix oluşturma tamamlandı, PMI Değerini hesaplayacağız.
				matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, rankingSize);
				
				System.out.println("PMI Hesaplaması değer için bitti -> " + i);
				
				//Hesaplamalar bitti veritabanına update geliyor
				entryManager.updatePmiValues(matrixData);
				
				System.out.println("PMI Hesaplaması veritabanına yazımı değer için bitti -> " + i);
				
				sequenceList.clear();
				
			} else {
				sequenceList.add(i);
			}
		}
		
		if (CollectionUtils.isNotEmpty(sequenceList)) {
			List<PMIValueIndexes> indexList = entryManager.getPMIValueIndexAllValueWithIndex1(sequenceList);
			Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
			//matrixData oluşturuldu.
			for (PMIValueIndexes ind : indexList) {
				BigDecimal freqWith = new BigDecimal(ind.getFrequencyInTogether());
				matrixData.put(ind, freqWith);
			}
			// Co occurence matrix oluşturma tamamlandı, PMI Değerini hesaplayacağız.
			matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, rankingSize);
			
			System.out.println("PMI Hesaplaması değer için bitti -> ");
			
			//Hesaplamalar bitti veritabanına update geliyor
			entryManager.updatePmiValues(matrixData);
			
			System.out.println("PMI Hesaplaması veritabanına yazımı değer için bitti -> ");
			
			sequenceList.clear();
		}
		
	}
	
	@Override
	public void calculateAlternatePMIValuesWithMemoryAndDisk(String readTextPath, List<String> outputFromAnotherFunction) {
		System.out.println("Alternate PMI değerlerini hesaplama operasyonu tetiklendi");
		System.out.println("Dosyalar okunarak memory e alınıyor");
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		int rankingSize = ranking.size();
		ranking.clear();
		
		
		Map<Integer, Integer> rowFrequencyInTogetherSumMap = entryManager.getRowAndFrequencyInTogetherSumMap(bigRowCountAlternatePMI);
		
		List<Integer> sequenceList = new ArrayList<Integer>();
		for (int i = smallRowCountAlternatePMI; i < bigRowCountAlternatePMI; i++) {
			if (i % 100 == 0 && CollectionUtils.isNotEmpty(sequenceList)) {
				sequenceList.add(i);
				
				List<PMIValueIndexes> indexList = entryManager.getPMIValueIndexAllValueWithIndex1(sequenceList);
				Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
				
				//matrixData oluşturuldu.
				for (PMIValueIndexes ind : indexList) {
					BigDecimal freqWith = new BigDecimal(ind.getFrequencyInTogether());
					matrixData.put(ind, freqWith);
				}
				
				matrixData = calculateAndSetAlternatePMIValuesWithoutMapOfIndexes(matrixData, rowFrequencyInTogetherSumMap, rankingSize);
				
				System.out.println("Alternate PMI Hesaplaması değer için bitti -> " + i);
				
				//Hesaplamalar bitti veritabanına update geliyor
				entryManager.updateAlternatePmiValues(matrixData);
				
				System.out.println("Alternate PMI Hesaplaması veritabanına yazımı değer için bitti -> " + i);
				
				sequenceList.clear();
				
			} else {
				sequenceList.add(i);
			}
		}
		
		if (CollectionUtils.isNotEmpty(sequenceList)) {
			List<PMIValueIndexes> indexList = entryManager.getPMIValueIndexAllValueWithIndex1(sequenceList);
			Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
			//matrixData oluşturuldu.
			for (PMIValueIndexes ind : indexList) {
				BigDecimal freqWith = new BigDecimal(ind.getFrequencyInTogether());
				matrixData.put(ind, freqWith);
			}
			// Co occurence matrix oluşturma tamamlandı, Alternate PMI Değerini hesaplayacağız.
			matrixData = calculateAndSetAlternatePMIValuesWithoutMapOfIndexes(matrixData, rowFrequencyInTogetherSumMap, rankingSize);
			
			System.out.println("Alternate PMI Hesaplaması değer için bitti -> ");
			
			//Hesaplamalar bitti veritabanına update geliyor
			entryManager.updateAlternatePmiValues(matrixData);
			
			System.out.println("Alternate PMI Hesaplaması veritabanına yazımı değer için bitti -> ");
			
			sequenceList.clear();
		}
		
	}
	
	private void createOutputNodeIdNodeNameForBigClam(List<WordIndex> wordsOccured) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("nodeName.txt"));
			 int count = 0;
			 for(WordIndex  word  : wordsOccured){
				 if (count == globalRowCount) {
					 break;
				 }
				 out.write(word.getIndex() + "	" + String.valueOf(word.getWord()) + "\r\n");
				 count++;
			 }
			 out.close();
			 System.out.println("BigCLAM algoritması için nodeId ve nodeName bilgilerini içeren TXT dosyası oluşturuldu!");
		}
		catch (IOException e) {
			System.err.println("TXT oluşturulurken hata oluştu!");
       }
		
	}


	/**
	 * 
	 * @param matrixData
	 * Bu metod bigclam algoritmasına verilecek veriyi oluşturan dosyadır. 
	 * Unweighted network oluşturacak!!
	 */
	private void createBigClamInputFromMatrix(Map<PMIValueIndexes, BigDecimal> matrixData) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
			for(Map.Entry<PMIValueIndexes, BigDecimal>  entry  : matrixData.entrySet()){
				PMIValueIndexes index = entry.getKey();
				BigDecimal numberOfOccurredSameWindow = entry.getValue();
				if (numberOfOccurredSameWindow.signum() > 0)  {
					out.write(index.getIndex1()+"	"+ index.getIndex2()+"\r\n");
				}
			}
			out.close();
			System.out.println("BigClam algoritmasına verilecek input oluşturuldu!!");
		} catch (IOException e) {
			System.err.println("BigClam input için oluşturulacak dosya açılırken bir hata oluştu");
		}
	}
	
	
	/**
	 * 
	 * @param matrixData
	 * Bu metod bigclam algoritmasına verilecek veriyi oluşturan dosyadır. 
	 * Unweighted network oluşturacak!!
	 */
	private void createBigClamInputFromDatabase() {
		try {
			List<PMIValueIndexes> indexList = entryManager.getPMIValueIndexListWithProcessId(1);
			
			BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
			for(PMIValueIndexes index  :indexList){
				out.write(index.getIndex1()+"	"+ index.getIndex2()+"\r\n");
			}
			out.close();
			System.out.println("BigClam algoritmasına verilecek input oluşturuldu!!");
		} catch (IOException e) {
			System.err.println("BigClam input için oluşturulacak dosya açılırken bir hata oluştu");
		}
	}

	
	private Map<Integer, List<String>> getMapOfIndexes (Map<PMIValueIndexes, BigDecimal> matrixData) {
		Map<Integer, List<String>> returnData = new HashMap<Integer, List<String>>();
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			PMIValueIndexes valueObject = data.getKey();
			List<String> valueList = null;
			if (returnData.containsKey(valueObject.getIndex1())) {
				valueList = returnData.get(valueObject.getIndex1());
				valueList.add(valueObject.getIndex2() + "-" + data.getValue());
				returnData.put(valueObject.getIndex1(), valueList);
			} else {
				valueList = new ArrayList<String>();
				valueList.add(valueObject.getIndex2() + "-" + data.getValue());
				returnData.put(valueObject.getIndex1(), valueList);
			}
		}
		return returnData;
	}
	
	private Map<PMIValueIndexes, BigDecimal> calculateAndSetPMIValues(Map <PMIValueIndexes, BigDecimal> matrixData, Map<Integer, BigDecimal> wordFrequencyMap, int totalWordSize) {
		System.out.println("PMI calculation is just started!!");
		
		BigDecimal totalWSize = new BigDecimal(totalWordSize);
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			calculationOfPMI(wordFrequencyMap, totalWSize, data.getValue(), data.getKey());
			
			writeToDocumentPMIValue(data.getKey());
			
			writeToDocumentPMIOnlyTempValue(data.getKey());
		}
		
		System.out.println("PMI calculation is just finished");
		
		return matrixData;
	}

	private void writeToDocumentPMIValue(PMIValueIndexes key) {
		try {
			String filename= "pmiValues.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    fw.write(key.getIndex1() + "-" + key.getIndex2() + "-" + key.getLogaritmicPmiValue() +"\r\n");//appends the string to the file
		    
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}
	
	private void writeToDocumentPMIOnlyTempValue(PMIValueIndexes key) {
		try {
			String filename= "pmiValuesTemp.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    fw.write(key.getIndex1() + "-" + key.getIndex2() + "-" + key.getPmiValue() +"\r\n");//appends the string to the file
		    
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}


	/**
	 * 
	 * @param wordFrequencyMap
	 * @param totalWSize (Toplam Kelime Sayısı)
	 * @param togetherValue (İki kelimenin beraber görülme sayısı)
	 * @param calculativeValue (Tüm veriyi tutan değer)
	 */
	private void calculationOfPMI(Map<Integer, BigDecimal> wordFrequencyMap, BigDecimal totalWSize,
			BigDecimal togetherValue, PMIValueIndexes calculativeValue) {
		
		//SHIFTING (Birbirleriyle hiç görülmemiş kelimeler için log alma işleminde sonsuz değerden kaçmak için yapılıyor)
		togetherValue = togetherValue.add(BigDecimal.ONE);
		
		//kompleks hesap
//		BigDecimal probW1AndW2 = togetherValue.divide(totalWSize, 10, RoundingMode.HALF_UP);
//		
//		BigDecimal frequencyIndex1 = wordFrequencyMap.get(calculativeValue.getIndex1()).divide(totalWSize, 10, RoundingMode.HALF_UP);
//		BigDecimal frequencyIndex2 = wordFrequencyMap.get(calculativeValue.getIndex2()).divide(totalWSize, 10, RoundingMode.HALF_UP);
//		
//		BigDecimal multipliedValue = frequencyIndex1.multiply(frequencyIndex2);
		
//		BigDecimal pmiValue = probW1AndW2.divide(multipliedValue, 10, RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
		
		//sadeleştirilmiş hesap
		BigDecimal top = togetherValue.multiply(totalWSize).setScale(10, RoundingMode.HALF_UP);
		BigDecimal down = wordFrequencyMap.get(calculativeValue.getIndex1()).multiply(wordFrequencyMap.get(calculativeValue.getIndex2())).setScale(10, RoundingMode.HALF_UP);
		
		BigDecimal pmiValue = top.divide(down, 10, RoundingMode.HALF_UP).setScale(5, RoundingMode.HALF_UP);
		
		// pmiValue değerinin logaritmasını alıp tekrar üstüne set et. (Logaritma 0 çıkacak senaryoya dikkat et)
		calculativeValue.setPmiValue(pmiValue);
		try {
			BigDecimal logarithmicValue; 
			double logValue = Math.log(calculativeValue.getPmiValue().doubleValue());
			if (logValue == Double.NaN || logValue < 0) {
				logarithmicValue = BigDecimal.ZERO;
			} else {
				logarithmicValue = new BigDecimal(logValue);
			}
			
			calculativeValue.setLogaritmicPmiValue(logarithmicValue);
		} catch (ArithmeticException e) {
			// logaritma 0 geldiğinde exception fırlatılıp yakalandı ve bir değer set edildi. Değeri değiştirebiliriz.
			// Değer son karardan sonra 0 set edildi. (23 Ocak 2018) Daha sonrasında operasyonel hesaplamalarda değerler +1 shift edilecek
			calculativeValue.setLogaritmicPmiValue(BigDecimal.ZERO);
		}
	}
	
	/*
	 *  PMI (w, c) = log (((w,c)* D )/ w*c) formülünü açıklayacak olursak ;
		index w = 2
		index c = 3
		(w,c) değeri (2,3) cell inde yazan değer
		D değeri birbirleriyle ilişki olan ikililerin toplam sayısı (Şu andaki matrixData nın size ı)
		w değeri 2.satırdaki (2,3) dışındaki tüm değerlerin toplamı
		c değeri 3.satırdaki (3,2) dışındaki tüm değerlerin toplamıdır. 

	 */	
	private Map<PMIValueIndexes, BigDecimal> calculateAndSetAlternatePMIValues (Map <PMIValueIndexes, BigDecimal> matrixData
			, Map<Integer, List<String>> mapOfIndexes, int D) {
		System.out.println("Alternate PMI calculation is just started");
		
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			calculationOfAlternatePMI(mapOfIndexes, D, data.getValue(), data.getKey());
			
			writeToDocumentAlternatePMIValue(data.getKey());
			
			writeToDocumentAlternatePMIOnlyTempValue(data.getKey());
		}
		
		System.out.println("Alternate PMI calculation is just finished");
		
		return matrixData;
	}
	
	private Map<PMIValueIndexes, BigDecimal> calculateAndSetAlternatePMIValuesWithoutMapOfIndexes (Map <PMIValueIndexes, BigDecimal> matrixData
			, Map<Integer, Integer> rowFrequencyInTogetherSumMap, int D) {
		
		System.out.println("Alternate PMI calculation is just started");
		
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			calculationOfAlternatePMIWithoutMapOfIndexes(rowFrequencyInTogetherSumMap, D, data.getValue(), data.getKey());
			
			writeToDocumentAlternatePMIValue(data.getKey());
			
			writeToDocumentAlternatePMIOnlyTempValue(data.getKey());
		}
		
		System.out.println("Alternate PMI calculation is just finished");
		
		return matrixData;
	}


	private void writeToDocumentAlternatePMIOnlyTempValue(PMIValueIndexes key) {
		try {
			String filename= "alternatePmiValuesTemp.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    fw.write(key.getIndex1() + "-" + key.getIndex2() + "-" + key.getAlternatePmiValue() +"\r\n");//appends the string to the file
		    
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}


	private void writeToDocumentAlternatePMIValue(PMIValueIndexes key) {
		try {
			String filename= "alternatePmiValues.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    fw.write(key.getIndex1() + "-" + key.getIndex2() + "-" + key.getLogarithmicAlternatePmiValue() +"\r\n");//appends the string to the file
		    
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}

	private void calculationOfAlternatePMIWithoutMapOfIndexes(
			Map<Integer, Integer> rowFrequencyInTogetherSumMap, int D, BigDecimal probW1AndW2,
			PMIValueIndexes calculativeValue) {
		int index1W = calculativeValue.getIndex1();
		int index2C = calculativeValue.getIndex2();
		
		// SHIFTING (Log alma işleminde birbirleriyle hiç görülmemiş kelimeleri
		// hesaplarken sonsuz değerden kaçmak için yapılıyor)
		probW1AndW2 = probW1AndW2.add(BigDecimal.ONE);
		
		BigDecimal w = BigDecimal.ZERO;
		w = new BigDecimal(rowFrequencyInTogetherSumMap.get(index1W));
		w = w.subtract(probW1AndW2);
		
		BigDecimal c = BigDecimal.ZERO;
		c = new BigDecimal(rowFrequencyInTogetherSumMap.get(index2C));
		c = c.subtract(probW1AndW2);
		
		// TOP (w,c) * D
		BigDecimal top = probW1AndW2.multiply(new BigDecimal(D));
		// BOTTOM w*c
		BigDecimal bottom = w.multiply(c);
		// TOTAL
		BigDecimal total = top.divide(bottom, 10, RoundingMode.HALF_UP);
		calculativeValue.setAlternatePmiValue(total);
		try {
			double logValue = Math.log(calculativeValue.getAlternatePmiValue().doubleValue());
			BigDecimal logarithmicValue;
			if (logValue == Double.NaN || logValue < 0) {
				logarithmicValue = BigDecimal.ZERO;
			} else {
				logarithmicValue = new BigDecimal(logValue);
			}

			calculativeValue.setLogarithmicAlternatePmiValue(logarithmicValue);
		} catch (ArithmeticException ex) {
			// Değer son karardan sonra 0 set edildi. (23 Ocak 2018) Daha sonrasında
			// operasyonel hesaplamalarda değerler +1 shift edilecek
			calculativeValue.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param mapOfIndexes
	 * @param D (tüm kelime sayısı)
	 * @param calculativeValue (Tüm veriyi tutan değer)
	 * @param probW1AndW2 (iki kelimenin beraber görülme sayısı)
	 */
	private void calculationOfAlternatePMI(Map<Integer, List<String>> mapOfIndexes, int D,
			 BigDecimal probW1AndW2, PMIValueIndexes calculativeValue) {
	     // (w,c) probW1AndW2
		int index1W = calculativeValue.getIndex1();
		int index2C = calculativeValue.getIndex2();
		List <String> valueList1 = mapOfIndexes.get(index1W);
		BigDecimal w = BigDecimal.ZERO;
		if (CollectionUtils.isNotEmpty(valueList1)) {
			for (String s : valueList1) {
				String [] arr = s.split("-");
				if (arr.length == 2 && Integer.parseInt(arr[0]) != index2C) {
					w = w.add( new BigDecimal(arr[1]));
				}
			}
		}
		BigDecimal c = BigDecimal.ZERO;
		List <String> valueList2 = mapOfIndexes.get(index2C);
		if (CollectionUtils.isNotEmpty(valueList2)) {
			for (String s : valueList2) {
				String [] arr = s.split("-");
				if (arr.length == 2 && Integer.parseInt(arr[0]) != index1W) {
					c = c.add(new BigDecimal(arr[1]));
				}
			}
		}
		//SHIFTING (Log alma işleminde birbirleriyle hiç görülmemiş kelimeleri hesaplarken sonsuz değerden kaçmak için yapılıyor)
		probW1AndW2 = probW1AndW2.add(BigDecimal.ONE);
		//TOP (w,c) * D
		BigDecimal top = probW1AndW2.multiply(new BigDecimal(D));
		//BOTTOM w*c
		BigDecimal bottom = w.multiply(c);
		//TOTAL
		BigDecimal total = top.divide(bottom, 10, RoundingMode.HALF_UP);
		calculativeValue.setAlternatePmiValue(total);
		try {
			double logValue = Math.log(calculativeValue.getAlternatePmiValue().doubleValue());
			BigDecimal logarithmicValue;
			if (logValue == Double.NaN || logValue < 0) {
				logarithmicValue = BigDecimal.ZERO;
			} else {
				logarithmicValue = new BigDecimal(logValue);
			}
			
			calculativeValue.setLogarithmicAlternatePmiValue(logarithmicValue);
		} catch (ArithmeticException ex) {
			// Değer son karardan sonra 0 set edildi. (23 Ocak 2018) Daha sonrasında operasyonel hesaplamalarda değerler +1 shift edilecek
			calculativeValue.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
		}
	}
	
	/**
	 * Bu fonksiyon matrix data nın içindeki tüm değerleri teker teker dolaşıp cosinüs benzerliğini hesaplamada kullanılır.
	 * 
	 * @param matrixData -> oluşturulan matrix data
	 * @param totalSize -> toplam unique kelime sayısı
	 */
	private void createVectorsOneByOneWithVirtualMatrix (Map <PMIValueIndexes, BigDecimal> matrixData, int globalRowCount, int globalColumnCount) {
		List<CosineSimilarityIndex> cosList = new ArrayList<CosineSimilarityIndex>();
		for (int i = 0; i < globalRowCount; i++) {
			int tempI = i;
			List<PMIValueIndexes> listIndex1EqualsI = matrixData.keySet().stream().filter(a -> a.getIndex1() == tempI).sorted((x,y) -> Integer.compare(x.getIndex2(), y.getIndex2())).collect(Collectors.toList());
			
			for(int j = 0; j < globalColumnCount; j++) {
				if (i != j) {
					int tempJ = j;
					List<PMIValueIndexes> listIndex2EqualsJ = matrixData.keySet().stream().filter(b -> b.getIndex1() == tempJ).sorted((x,y) -> Integer.compare(x.getIndex2(), y.getIndex2())).collect(Collectors.toList());
					
					//2 index için değerler sort edilerek hazırlandı. Şimdi operasyon başlıyor.
					double[] array1 = new double[globalColumnCount];
					double[] array2 = new double[globalColumnCount];
					
					BigDecimal index1Total = BigDecimal.ZERO;
					for (PMIValueIndexes index1 : listIndex1EqualsI) {
						array1[index1.getIndex2()] = index1.getLogaritmicPmiValue().doubleValue();
						index1Total = index1Total.add(index1.getLogaritmicPmiValue());
					}
					
					BigDecimal index2Total = BigDecimal.ZERO;
					for (PMIValueIndexes index2 : listIndex2EqualsJ) {
						array2[index2.getIndex2()] = index2.getLogaritmicPmiValue().doubleValue();
						index2Total = index2Total.add(index2.getLogaritmicPmiValue());
					}
					
					CosineSimilarityIndex cos = new CosineSimilarityIndex();
					cos.setIndex1(i);
					cos.setIndex2(j);
					cos.setIndex1Total(index1Total);
					cos.setIndex2Total(index2Total);
					cos.setIndex1Array(array1);
					cos.setIndex2Array(array2);
					
					double cosSimilarity = cosineSimilarity(cos.getIndex1Array(), cos.getIndex2Array());
					
					cos.setCosineSimilarity(cosSimilarity);
					
					Optional<PMIValueIndexes> isFindIndexes = matrixData.keySet().stream().filter(a -> a.getIndex1() == tempI && a.getIndex2() == tempJ).findFirst();
					
					if (isFindIndexes.isPresent()) {
						PMIValueIndexes specificIndex = isFindIndexes.get();
						specificIndex.setCosineSimilarityData(cos);
					}
					
					cosList.add(cos);
					
					if (cosList.size() % 1000 == 0) {
						appendCosineSimilarityWithList(cosList);
						cosList.clear();
					}
					//Bu metod teker teker ekleme yapıyor, daha az IO yapalım diye şimdilik 1000 er 1000 er ekliyorum
//					appendCosineSimilarityOneByOne(cos);
				}
			}
			System.out.println("I is successfully finished : " + i + " Tarih : " + new Date());
		}
		//İşlem bittiğinde cosList içinde değer varsa onu da yaz ve çık
		if (CollectionUtils.isNotEmpty(cosList)) {
			appendCosineSimilarityWithList(cosList);
			cosList.clear();
		}
	}
	
	 @Override
	 public void calculateCosineSimilarityMemoryAndDisk() {
		// Read i value for resuming
		int newI = 0;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader("infoI.txt"));
			if (in != null) {
				String line;
				while ((line = in.readLine()) != null) {
					newI = Integer.parseInt(line);
				}
				in.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<CosineSimilarityIndex> cosList = new ArrayList<CosineSimilarityIndex>();
		// Anaveriyi veritabanından al
		Map<Integer, List<PMIValueIndexes>> matrixData = entryManager.getDataOrdered();
		System.out.println("Veri geldi. Row-Array Map hesabı başlıyor. Tarih : " + new Date());

		Map<Integer, double[]> rowArrayMap = new HashMap<Integer, double[]>();
		for (int i = 0; i < globalRowCount; i++) {
			List<PMIValueIndexes> listIndex1EqualsI = matrixData.get(i);

			double[] array1 = new double[globalColumnCount];
			for (PMIValueIndexes index1 : listIndex1EqualsI) {
				array1[index1.getIndex2()] = index1.getLogaritmicPmiValue().doubleValue();
			}

			rowArrayMap.put(i, array1);
		}
		
		matrixData.clear();
		
		System.out.println("Row - Array map oluşturuldu. Benzerlik hesabı başlıyor");
		int newICount = 0;
		for (int i = 0; i < globalRowCount; i++) {
			// Nerede kaldıysak oradan devam ediyoruz
			if (newICount == 0) {
				i = newI;
				newICount++;
			}

			for(int j = 0; j < globalRowCount; j++) {
				if (i != j) {

					CosineSimilarityIndex cos = new CosineSimilarityIndex();
					cos.setIndex1(i);
					cos.setIndex2(j);
					cos.setIndex1Array(rowArrayMap.get(i));
					cos.setIndex2Array(rowArrayMap.get(j));

					double cosSimilarity = cosineSimilarity(cos.getIndex1Array(), cos.getIndex2Array());
					
					cos.setCosineSimilarity(cosSimilarity);

					cosList.add(cos);
				}
			}
			
			appendCosineSimilarityWithList(cosList, i);
			cosList.clear();
			
			System.out.println("I is successfully finished : " + i + " Tarih : " + new Date());
			// Hesaplanan en son değerin ne olduğunu kaydet
			try {
				Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("infoI.txt"), "UTF8"));

				out.write(String.valueOf(i));
				out.flush();
				out.close();

				System.out.println("I nin yeni değeri kaydedildi!");
			}
			catch (IOException e) {
				System.err.println("TXT oluşturulurken hata oluştu!");
	       }
		}
	}
	 
	 @Override
	 public void findMostSimilarWords() {
		 Map<Integer, String> indexWordMap = readFromWordIndexFrequency();

		// Directory içinde ne kadar dosya varsa bunların path ini bir listeye doldurur
		List<Path> filesInDirectory = new ArrayList<Path>();
		try (Stream<Path> paths = Files.walk(Paths.get("cosineCalculation\\"))) {
			paths.filter(Files::isRegularFile).forEach(filesInDirectory::add);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Veri okunurken problem oluştu");
		}

		for (Path p : filesInDirectory) {
			List<CosineSimilarityIndex> cosIndexList = turnIntoCosineSimilarityIndexList(p);
			
			calculateMostSimilartWords(cosIndexList, indexWordMap);
		}
	 }
	
	private void calculateMostSimilartWords(List<CosineSimilarityIndex> cosIndexList, Map<Integer, String> indexWordMap) {
		List<CosineSimilarityIndex> sortedList = cosIndexList
				.stream()
				.sorted((o1, o2)-> 
						Double.compare(o2.getCosineSimilarity(), o1.getCosineSimilarity()))
						.collect(Collectors.toList());
		
		List<CosineSimilarityIndex> filteredList = new ArrayList<CosineSimilarityIndex>();
		int size = sortedList.size();
		for (int i = 0; i < globalMostSimilarWordCount; i++) {
			if (i < size) {				
				filteredList.add(sortedList.get(i));
			}
		}
		
		//Liste yazdırılıyor
		try {
			String filename= "mostSimilarWords.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    String globalWord = null;
		    for (CosineSimilarityIndex ind : filteredList) {
		    	if (StringUtils.isBlank(globalWord)) {
		    		globalWord = indexWordMap.get(ind.getIndex1());
		    	}
		    	String word1 = indexWordMap.get(ind.getIndex1());
				String word2 = indexWordMap.get(ind.getIndex2());
				Double cosineSim = ind.getCosineSimilarity();
				
		    	fw.write(word1 + "-" + word2 + "-" + cosineSim +"\r\n");//appends the string to the file
		    }
		    
		    fw.write("----------------------------------------------------------------------------------- WORD = " + globalWord +"\r\n");
		    
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}


	private List<CosineSimilarityIndex> turnIntoCosineSimilarityIndexList(Path p) {
		List<CosineSimilarityIndex> indexList = new ArrayList<CosineSimilarityIndex>();
		BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(p.toString()));
            String line;
			while ((line = br.readLine()) != null) {
				String splitWord = line;
				String[] arr = splitWord.split("-");
				
				if (arr[2].contains("E") || arr[2].contains("e") || arr[2].contains("NaN")) {
					continue;
				}
				
				CosineSimilarityIndex index = new CosineSimilarityIndex();
				index.setIndex1(Integer.parseInt(arr[0]));
				index.setIndex2(Integer.parseInt(arr[1]));
				index.setCosineSimilarity(Double.parseDouble(arr[2]));
				
				indexList.add(index);
			}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return indexList;
	}


	private Map<Integer, String> readFromWordIndexFrequency() {
		Map<Integer, String> indexWordMap = new HashMap<Integer, String>();
		
		BufferedReader br = null;
        try {
        	int count = 0;
            br = new BufferedReader(new FileReader("wordIndexFrequency.txt"));
            String line;
            while ((line = br.readLine()) != null) {
            	if (count <= globalRowCount) {
            		String splitWord = line;
            		
            		String [] arr = splitWord.split("	");
            		
            		indexWordMap.put(Integer.parseInt(arr[1]), arr[0]);
            		
            		count++;
            		
            	} else {
            		break;
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return indexWordMap;
	}


	/**
	 * Benzerlik hesapları bittikten sonra index leri tek tek dolaşarak o indexdeki kelimenin hangi index deki kelimeyle
	 * en fazla benzerliğe sahip olduğuyla ilgili çıktı üreten metoddur.
	 * @param matrixData
	 */
	private void findMostSimilarWords(Map <PMIValueIndexes, BigDecimal> matrixData, int globalRowCount) {
		for (int i = 0; i < globalRowCount; i++) {
			int tempI = i;
			//index1 ve index2 sinde istediğimiz sayı olan liste
			List<PMIValueIndexes> listIndex1EqualsI = matrixData.keySet().stream().filter(a -> (a.getIndex1() == tempI || a.getIndex2() == tempI) && a.getIndex1() != a.getIndex2())
					.sorted((o1, o2)-> Double.compare(o2.getCosineSimilarityData().getCosineSimilarity(), o1.getCosineSimilarityData().getCosineSimilarity())).collect(Collectors.toList());
			
			List<PMIValueIndexes> filteredList = new ArrayList<PMIValueIndexes>();
			
			for (PMIValueIndexes index : listIndex1EqualsI) {
				Optional<PMIValueIndexes> isAnyFind = filteredList.stream().filter(a -> (a.getIndex1() == index.getIndex1() && a.getIndex2() == index.getIndex2()) || (a.getIndex2() == index.getIndex1() && a.getIndex1() == index.getIndex2())).findFirst();
				
				if (isAnyFind.isPresent()) {
					continue;
				}
				
				if (! Double.isNaN(index.getCosineSimilarityData().getCosineSimilarity())) {				    
					filteredList.add(index);
				}
				
				if (filteredList.size() == globalMostSimilarWordCount) {
					break;
				}
			}
			
			//Liste yazdırılıyor
			try {
				String filename= "mostSimilarWords.txt";
			    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			    
			    for (PMIValueIndexes ind : filteredList) {
			    	fw.write(ind.getIndex1() + "-" + ind.getIndex2() + "-" + ind.getCosineSimilarityData().getCosineSimilarity() +"\r\n");//appends the string to the file
			    }
			    
			    fw.write("----------------------------------------------------------------------------------- INDEX = " +tempI +"\r\n");
			    
			    fw.close();
			} catch (IOException ioe) {
				System.err.println("IOException: " + ioe.getMessage());
			}
			
			System.out.println("I is successfully finished to calculate MOST SIMILAR WORDS : " + i + " Tarih : " + new Date());
		}
	}
	
	@Override
	public void saveWordIndexListToDatabase(String readTextPath, List<String> outputFromAnotherFunction) {
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(readTextPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		System.out.println("Bir kelimenin kaç defa sistemde görüldüğüyle ilgili liste oluşturuluyor");
		
		writeWordIndexListToDatabase(readFromTxtEntries);
	}
	
	private void writeWordIndexListToDatabase (List<String> readFromTxtEntries) {
		// kelimelerin değerleri hesaplanıyor.
		Map<String, Integer> mostOccuredWords = getMostOccurredWordMap(readFromTxtEntries);
		// Hesaplama bitti sıralama yapılıyor.
		if (mostOccuredWords.size() > 0) {
			List<WordIndex> wordIndexList = getAndOrderWordIndexList(mostOccuredWords);
			
			entryManager.saveWordIndexListToDatabase(wordIndexList);
			
		} else {
			System.err.println("Hesaplanacak MAP bulunamadı");
		}
	}


	private List<WordIndex> getAndOrderWordIndexList(Map<String, Integer> mostOccuredWords) {
		Map<String, Integer> orderedDESC = sortByValue(mostOccuredWords, false);
		List<WordIndex> wordIndexList = new ArrayList<WordIndex>();
		int count = 0;
		for (Map.Entry<String, Integer> entrySet : orderedDESC.entrySet()) {
			WordIndex word = new WordIndex(count, entrySet.getKey(), new BigDecimal(entrySet.getValue()));
			
			wordIndexList.add(word);
			count++;
		}
		return wordIndexList;
	}
	
	
	private List<WordIndex> getWordIndexList (List<String> readFromTxtEntries) {
		//kelimelerin değerleri hesaplanıyor.
		Map<String, Integer> mostOccuredWords = getMostOccurredWordMap(readFromTxtEntries);
		//Hesaplama bitti sıralama yapılıyor.
		if (mostOccuredWords.size() > 0) {			
			List<WordIndex> wordIndexList = getAndOrderWordIndexList(mostOccuredWords);
			return wordIndexList;
		} else {
			System.err.println("Hesaplanacak MAP bulunamadı");
		}
		return null;
	}


	private Map<String, Integer> getMostOccurredWordMap(List<String> readFromTxtEntries) {
		Map<String, Integer> mostOccuredWords = new HashMap<String, Integer>();
		for (String s : readFromTxtEntries) {
			List<String> retList = new ArrayList<String>(); 
			retList = splittedEntryDescription(retList, s);
			for (int i=0; i<retList.size(); i++) {
				if (retList.get(i).equals(",") || retList.get(i).equals(".") ||retList.get(i).equals("!") || retList.get(i).equals(":") 
						|| retList.get(i).equals(";") || retList.get(i).equals("...") || retList.get(i).equals("?") 
						|| retList.get(i).equals("\"")) {
					continue;
				}
				
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
	public void createVectors (Map<PMIValueIndexes, BigDecimal> filledMatrix) {
		final Comparator<PMIValueIndexes> comp = (p1, p2) -> Integer.compare( p1.getIndex1(), p2.getIndex1());
		PMIValueIndexes biggestIndex = filledMatrix.keySet().stream()
                .max(comp)
                .get();
//		List<CosineSimilarityIndex> indexList = new ArrayList<CosineSimilarityIndex>();
		for (int i = 0; i < biggestIndex.getIndex1(); i++) {
			CosineSimilarityIndex cos = new CosineSimilarityIndex();
			cos.setIndex1(i);
			List<PMIValueIndexes> index1List = filledMatrix.keySet().stream().filter(a -> a.getIndex1() == cos.getIndex1()).sorted((x,y) -> Integer.compare( x.getIndex2(), y.getIndex2())).collect(Collectors.toList());
			
			for (int j = 0; j < biggestIndex.getIndex1(); j++) {				
				cos.setIndex2(j);
				if (i != j) {
					cos.setIndex1Total(BigDecimal.ZERO);
					cos.setIndex2Total(BigDecimal.ZERO);
					double[] array1 = new double[biggestIndex.getIndex1() + 1];
					double[] array2 = new double[biggestIndex.getIndex1() + 1];
					cos.setIndex1Array(array1);
					cos.setIndex2Array(array2);
					
					List<PMIValueIndexes> index2List = filledMatrix.keySet().stream().filter(a -> a.getIndex1() == cos.getIndex2()).sorted((x,y) -> Integer.compare( x.getIndex2(), y.getIndex2())).collect(Collectors.toList());
					
					for (PMIValueIndexes index1 : index1List) {
						BigDecimal totIndex1 = cos.getIndex1Total();
						
						//NEDEN Soralım
						if (index1.getLogaritmicPmiValue().signum() < 0) {
							index1.setLogaritmicPmiValue(BigDecimal.ZERO);
						}
						
						totIndex1 = totIndex1.add(index1.getLogaritmicPmiValue());
						cos.setIndex1Total(totIndex1);
						
						double[] arr1 = cos.getIndex1Array();
						arr1[index1.getIndex2()] = index1.getLogaritmicPmiValue().doubleValue();
						
						cos.setIndex1Array(arr1);
					}
					
					for (PMIValueIndexes index2 : index2List) {
						BigDecimal totIndex2 = cos.getIndex2Total();
						
						//NEDEN SORALIM
						if (index2.getLogaritmicPmiValue().signum() < 0) {
							index2.setLogaritmicPmiValue(BigDecimal.ZERO);
						}
						
						totIndex2 = totIndex2.add(index2.getLogaritmicPmiValue());
						cos.setIndex2Total(totIndex2);
						
						double[] arr2 = cos.getIndex2Array();
						arr2[index2.getIndex2()] = index2.getLogaritmicPmiValue().doubleValue();
						cos.setIndex2Array(arr2);
						
					}
					
					//shifting operation
//					Map<Integer,double[]>  shiftedArrayMap = shiftAllValuesBeforeCosineSimilarityCalculation(cos.getIndex1Array(), cos.getIndex2Array(), true);
//					cos.setIndex1Array(shiftedArrayMap.get(1));
//					cos.setIndex2Array(shiftedArrayMap.get(2));
					
					double cosSimilarity = cosineSimilarity(cos.getIndex1Array(), cos.getIndex2Array());
					cos.setCosineSimilarity(cosSimilarity);
					appendCosineSimilarityOneByOne(cos);
//					indexList.add(cos);
//					if (indexList.size() % 1000 == 0) {
//						System.out.println("IndexList size = " + indexList.size());
//					}
				}
			}
		}
		
		// TXT
//		createCosineSimilarityTxt(indexList);
		// EXCEL
//		createCosineSimilarityExcel(indexList);
	}
	
	
	/**
	 * Calculates cosine similarity of two vectors
	 * @param vectorA
	 * @param vectorB
	 * @return cosine similarity value
	 */
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
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
			System.err.println("Linklerin txt dosyasına yazımı sırasında beklenmeyen bir hata oluştu!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void createOutputForWordsOccured(List<WordIndex> wordIndexList) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("wordIndexFrequency.txt"));
			 for(WordIndex  word  : wordIndexList){
				 out.write(String.valueOf(word.getWord()) +"	"+ word.getIndex()+"	"+word.getFrequency()+"\r\n");
			 }
			 out.close();
			 System.out.println("Bir kelimenin hangi index de kaç defa sistemde geçtiğiyle ilgili TXT dosyası oluşturuldu!");
		}
		catch (IOException e) {
			System.err.println("TXT oluşturulurken hata oluştu!");
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
			out.write("INDEX-1" + "	" + "INDEX-2" + "	" + "TMP-PMI-VALUE" + "	" + "PMI-VALUE" + "	" + "TMP-ALT-PMI-VALUE" + "	" + "ALT-PMI-VALUE" +"\r\n");
			for (PMIValueIndexes index : indexList) {
				out.write(index.getIndex1() + "	" + index.getIndex2() + "	" + index.getPmiValue() + "	" 
							+ index.getLogaritmicPmiValue() + "	" + index.getAlternatePmiValue() + "	" + index.getLogarithmicAlternatePmiValue()+"\r\n");
			}
			out.close();
			System.out.println("PMI Index Value nesnesi için çıktı oluşturuldu");
			
		} catch (Exception e) {
			System.err.println("TXT oluşturulurken hata oluştu! " + e.getMessage() );
		}
	}
	
	@Override
	public void calculateJaccardSimilarityAndSave() {
		List<UserUserTitle> userUserTitleList = getUserUserTitleListFromFile();
		List<UserTitle> userTitleList = getUserTitleListFromFile();
		if (userUserTitleList == null || userUserTitleList.isEmpty()) {
			System.err.println("UserUserTitle dosyasına veri ekleyin" );
			return;
		}
		if (userTitleList == null || userTitleList.isEmpty()) {
			System.err.println("UserTitle dosyasına veri ekleyin" );
			return;
		}
		
		Set <User> userList = new HashSet<User>();
		
		for (UserUserTitle uut : userUserTitleList) {
			User user1 = uut.getUser1();
			User user2 = uut.getUser2();
			int user1TotalCount = 0;
			int user2TotalCount = 0;
			for (UserTitle ut : userTitleList) {
				if (ut.getUsername().equals(user1.getNickname())) {
					user1TotalCount = ut.getCountOfTitleThatWrote();
				}
				if (ut.getUsername().equals(user2.getNickname())) {
					user2TotalCount = ut.getCountOfTitleThatWrote();
				}
				if (user1TotalCount != 0 && user2TotalCount != 0) {
					//calculation Of JaccardSimilarity
					BigDecimal similarCount = new BigDecimal(uut.getCountOfSimilarTitle());
					BigDecimal firstUserCount = new BigDecimal(user1TotalCount);
					BigDecimal secondUserCount = new BigDecimal(user2TotalCount);
					BigDecimal result = similarCount.divide((firstUserCount.add(secondUserCount)).subtract(similarCount), 10, RoundingMode.HALF_UP) ;
					uut.setJaccardSimilarity(result);
					break;
				}
			}
			userList.add(user1);
			userList.add(user2);
		}
		//Threshold
		calculateThreshold(userUserTitleList);
		//Çıktı üret
		//1 - TXT
		createJaccardSimilarityTxt(userUserTitleList);
		//2 - Excel
		createJaccardSimilarityExcel(userUserTitleList);
		//Bir yazarın en çok kullandığı kelimeleri hesapla.
		Set<String> userNameList = userList.stream().map(User::getNickname).collect(Collectors.toSet());
		findWordsByAuthorFromTxtFile(userNameList);
		
	}
	
	private void calculateThreshold(List<UserUserTitle> userUserTitleList) {
		Map<BigDecimal, List<UserUserTitle>> thresholdMap = createThresHoldMap();
		for (UserUserTitle t : userUserTitleList) {
			BigDecimal jaccSim = t.getJaccardSimilarity().setScale(2, RoundingMode.HALF_UP);
			List<UserUserTitle> tempList = thresholdMap.get(jaccSim);
			tempList.add(t);
			thresholdMap.put(jaccSim, tempList);
		}
		
		//Sort By Key
		Map<BigDecimal, List<UserUserTitle>> sorted =
				thresholdMap.entrySet().stream()
			       .sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(
			    	          Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));;
		
		createExcelForThresholdJaccardSimilarity(sorted);
		
		createTxtForThresholdJaccardSimilarity(sorted);
	}
	
	private Map<BigDecimal, List<UserUserTitle>> createThresHoldMap() {
		//Başlangıç noktası
		BigDecimal startPoint = new BigDecimal ("0.00");
		BigDecimal addNumber = new BigDecimal("0.01");
		// Dönecek map
		Map<BigDecimal, List<UserUserTitle>> returnedMap = new HashMap<BigDecimal, List<UserUserTitle>>();
		for (int i = 0; i < 101; i++) {
			returnedMap.put(startPoint, new ArrayList<UserUserTitle>());
			startPoint = startPoint.add(addNumber);
		}
		
		return returnedMap;
	}
	
	private void createExcelForThresholdJaccardSimilarity(Map<BigDecimal, List<UserUserTitle>> thresholdMap) {
		System.out.println("Jaccard benzerliği threshold excel oluşturma işlemi başladı");
		try {
			FileOutputStream fileOut = new FileOutputStream("jaccardSimilarityThreshold.xlsx");

			XSSFWorkbook workbook = new XSSFWorkbook();
			SXSSFWorkbook wb = new SXSSFWorkbook(workbook);
			SXSSFSheet worksheet = wb.createSheet("POI Worksheet");

			// index from 0,0... cell A1 is cell(0,0)
			SXSSFRow row1 = worksheet.createRow((int) 0);
			// Create Header of Excel
			SXSSFCell cell = row1.createCell((int) 0);
			cell.setCellValue("Number");
			cell = row1.createCell((int) 1);
			cell.setCellValue("Size");
			int rowNum = 1;
			// Create Body of Table
			for (Map.Entry<BigDecimal, List<UserUserTitle>> e : thresholdMap.entrySet()) {
				row1 = worksheet.createRow((int) rowNum);
				cell = row1.createCell((int) 0);
				cell.setCellValue(e.getKey().doubleValue());
				cell = row1.createCell((int) 1);
				cell.setCellValue(e.getValue().size());
				rowNum++;
			}

			wb.write(fileOut);
			fileOut.flush();
			fileOut.close();
			workbook.close();
			wb.close();
			System.out.println("Jaccard benzerliği excel oluşturma işlemi başarıyla tamamlandı");
		} catch (FileNotFoundException e) {
			System.err.println("Jaccard benzerliği Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
			
		} catch (IOException e) {
			System.err.println("Jaccard benzerliği Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
		}
	}
	
	private void createTxtForThresholdJaccardSimilarity(Map<BigDecimal, List<UserUserTitle>> thresholdMap) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("jaccardSimilarityThreshold.txt"));
			for (Map.Entry<BigDecimal, List<UserUserTitle>> e : thresholdMap.entrySet()) {
					out.write(e.getKey() + "-" + e.getValue().size() +"\r\n");
			}
			out.close();
			System.out.println("Jaccard benzerliği threshold TXT dosyası oluşturuldu!");
			
		} catch (Exception e) {
			System.err.println("Jaccard benzerliği threshold TXT dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
		}
	}
	
	private List<UserUserTitle> getUserUserTitleListFromFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(directoryOfSimilarUsersThatWroteSameTitle));
			String line;
			List<UserUserTitle> userUserTitleList = new ArrayList<UserUserTitle>();
			Map<String, User> usernameUserMap = new HashMap<String, User>();
			while((line = in.readLine()) != null) {
				UserUserTitle uut = new UserUserTitle();
				String[] splitWithLine = line.split("-");
				User user1 = null;
				User user2 = null;
				if (usernameUserMap.containsKey(splitWithLine[0])) {
					user1 = usernameUserMap.get(splitWithLine[0]);
				} else {
					user1 = userManager.getUserByUsername(splitWithLine[0]);
					usernameUserMap.put(user1.getNickname(), user1);
				}
				if (usernameUserMap.containsKey(splitWithLine[1])) {
					user2 = usernameUserMap.get(splitWithLine[1]);
				} else {
					user2 = userManager.getUserByUsername(splitWithLine[1]);
					usernameUserMap.put(user2.getNickname(), user2);
				}
				uut.setUser1(user1);
				uut.setUser2(user2);
				uut.setCountOfSimilarTitle(Integer.parseInt(splitWithLine[2]));
				userUserTitleList.add(uut);

 			}
			in.close();
			return userUserTitleList;
			
		} catch (Exception e) {
			System.err.println("UserUserTitle dosyası okunurken bir hata oluştu " + e.getMessage() );
			return null;
		}
	}
	
	private List<UserTitle> getUserTitleListFromFile() {
		try {			
			BufferedReader in = new BufferedReader(new FileReader(directoryOfTitleCountOfUsers));
			String line;
			List<UserTitle> userTitleList = new ArrayList<UserTitle>();
			while((line = in.readLine()) != null) {
				UserTitle ut = new UserTitle();
				String[] splitWithLine = line.split("-");
				ut.setUsername(splitWithLine[0]);
				ut.setCountOfTitleThatWrote(Integer.parseInt(splitWithLine[1]));
				userTitleList.add(ut);
			}
			in.close();
			return userTitleList;
		} catch (Exception e) {
			System.err.println("UserTitle dosyası okunurken bir hata oluştu " + e.getMessage() );
			return null;
		}
	}
	
	private void createJaccardSimilarityTxt(List<UserUserTitle> userUserTitleList) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("jaccardSimilarity.txt"));
			for (UserUserTitle uut : userUserTitleList) {
				if (uut.getJaccardSimilarity() != null && uut.getJaccardSimilarity().compareTo(BigDecimal.ZERO) != 0) {
					out.write(uut.getUser1().getNickname() + "-" + uut.getUser2().getNickname() + "-" + uut.getJaccardSimilarity() +"\r\n");
				}
			}
			out.close();
			System.out.println("Jaccard benzerliği TXT dosyası oluşturuldu!");
			
		} catch (Exception e) {
			System.err.println("Jaccard benzerliği TXT dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
		}
	}
	
	private void createJaccardSimilarityExcel(List<UserUserTitle> userUserTitleList) {
		System.out.println("Jaccard benzerliği excel oluşturma işlemi başladı");
		try {
			FileOutputStream fileOut = new FileOutputStream("jaccardSimilarity.xlsx");

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet worksheet = workbook.createSheet("POI Worksheet");

			// index from 0,0... cell A1 is cell(0,0)
			XSSFRow row1 = worksheet.createRow((int) 0);
			// Create Header of Excel
			XSSFCell cell = row1.createCell((int) 0);
			cell.setCellValue("1Username");
			cell = row1.createCell((int) 1);
			cell.setCellValue("2Username");
			cell = row1.createCell((int) 2);
			cell.setCellValue("JaccardSimilarity");
			int rowNum = 1;
			// Create Body of Table
			for (UserUserTitle a : userUserTitleList) {
				if (a.getJaccardSimilarity() != null && a.getJaccardSimilarity().compareTo(BigDecimal.ZERO) != 0) {					
					row1 = worksheet.createRow((int) rowNum);
					cell = row1.createCell((int) 0);
					cell.setCellValue(a.getUser1().getNickname());
					cell = row1.createCell((int) 1);
					cell.setCellValue(a.getUser2().getNickname());
					cell = row1.createCell((int) 2);
					cell.setCellValue(a.getJaccardSimilarity().doubleValue());
					rowNum++;
				}
			}

			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			workbook.close();
			System.out.println("Jaccard benzerliği excel oluşturma işlemi başarıyla tamamlandı");
		} catch (FileNotFoundException e) {
			System.err.println("Jaccard benzerliği Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
			
		} catch (IOException e) {
			System.err.println("Jaccard benzerliği Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
		}
	}
	
	
	private void appendCosineSimilarityOneByOne(CosineSimilarityIndex cos) {
		try
		{
		    String filename= "cosineSimilarity.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" + cos.getIndex1Total() + "-" + cos.getIndex2Total() 
		    + "-" +cos.getCosineSimilarity() +"\r\n");//appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	private void appendCosineSimilarityWithList(List<CosineSimilarityIndex> cosList, int indexNum) {
		try {
			String filename= "cosineCalculation/cosineSimilarityWithPMI-"+indexNum +".txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    for (CosineSimilarityIndex cos : cosList) {
		    	fw.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" +cos.getCosineSimilarity() +"\r\n");//appends the string to the file
		    }
		    
		    fw.close();
			
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	private void appendCosineSimilarityWithList(List<CosineSimilarityIndex> cosList) {
		try {
			String filename= "cosineSimilarityWithPMI.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    for (CosineSimilarityIndex cos : cosList) {
		    	fw.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" +cos.getCosineSimilarity() +"\r\n");//appends the string to the file
		    }
		    
		    fw.close();
			
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	@Override
	public void runBilkentData(String readXmlPath) {
		//read XML file
		// XML den okunan bilgiyi string içine doldur
		NodeList docNodeList = readXML(readXmlPath);
		List<String> allDataContent = new ArrayList<String>();
		for (int i = 0; i < docNodeList.getLength(); i++) {
			Node text = docNodeList.item(i).getChildNodes().item(9);
			String[] txtOneContent = text.getTextContent().split("\n");
			if (txtOneContent.length > 0) {
				for (int j = 0; j < txtOneContent.length; j++) {
					if (StringUtils.isNotEmpty(txtOneContent[j])) {
						allDataContent.add(txtOneContent[j].toLowerCase());
					}
				}
			}
		}
		// Veri artık toplandı. CoOccurrence Matrix fonksiyonu çağrılıyor.
		createCoOccurenceMatrix(null, allDataContent);

	}

	@Override
	public void runBilkentDataWithTxt(String txtPath) {
		List<String> allDataContent = importManager.readBilkentDataTxt(txtPath);
		//Veri artık toplandı. CoOccurrence Matrix fonksiyonu çağrılıyor.
		createCoOccurenceMatrix(null, allDataContent);
	}
	
	private NodeList readXML(String filePath) {
		InputSource is;
		try {
			InputStream inputStream = new FileInputStream(filePath);
			Reader reader = new InputStreamReader(inputStream, "Cp1252");
			is = new InputSource(reader);
			is.setEncoding("Cp1252");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			is = null;
		}
		
//        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("DOC");
            return nodeList;
        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
            return null;
        }
	}
	
	@Override
	public void runEnglishContent(String xmlFilePath) {
		//Directory içinde ne kadar dosya varsa bunların path ini bir listeye doldurur
		List<Path> filesInDirectory = new ArrayList<Path>();
		try (Stream<Path> paths = Files.walk(Paths.get(xmlFilePath))) {
		    paths
		        .filter(Files::isRegularFile)
		        .forEach(filesInDirectory :: add);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Veri okunurken problem oluştu");
		}
		
		List<String> globalWordList = new ArrayList<String>();
		
		for (Path p : filesInDirectory) {
			List<String> wordListFromFile = filterEnglishDocument(p.toString());
			globalWordList.addAll(wordListFromFile);
		}
		
		createCoOccurenceMatrix(null, globalWordList);
	}
	
	private List<String> filterEnglishDocument(String path) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String line;
			List<String> wordList = new ArrayList<String>();
			while((line = in.readLine()) != null){
				if (! StringUtils.isBlank(line)) {
					line = line.trim();
					String[] splittedLine = line.split(" ");
					for (String word : splittedLine) {
						if (word.contains(",")) {
							word = word.replace(",", "");
						} 
						if (word.contains("!")) {
							word = word.replace("!", "");
						}
						if (word.contains(".")) {
							word = word.replace(".", "");
						}
						if (word.contains(":")) {
							word = word.replace(":", "");
						}
						if (word.contains(";")) {
							word = word.replace(";", "");
						}
						if (word.contains("@")) {
							word = word.replace("@", "");
						}
						if (word.contains("<section>")) {
							word = word.replace("<section>", "");
						}
						if (word.contains("</section>")) {
							word = word.replace("</section>", "");
						}
						if (word.contains("<title>")) {
							word = word.replace("<title>", "");
						}
						if (word.contains("(")) {
							word = word.replace("(", "");
						}
						if (word.contains(")")) {
							word = word.replace(")", "");
						}
						
						if (! StringUtils.isBlank(word)) {
							wordList.add(word.toLowerCase());
						}
					}
				}
			}
			in.close();
			System.out.println("TXT den İngilizce veri okuması tamamlandı. Veri Büyüklüğü : " + wordList.size());
			return wordList;
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Bir kullanıcının en fazla hangi kelimeleri kullandığını gösteren metoddur
	 */
	@Override
	public void findWordsByAuthorFromTxtFile(Set <String> usernameList) {
		System.out.println("Kullanıcıların en çok kullandıkları kelimeler bulunuyor.");
		
		for (String username : usernameList) {			
			List<String> entryForUser = readEntryFromTxt(username);
			
			List<WordIndex>  wordIndexList = getWordIndexList(entryForUser);
			
			createTxtForUserEntryOperation(username, wordIndexList);
		}
		
		System.out.println("Kullanıcıların en çok kullandıkları kelimelerin hesabı tamamlandı");
	}
	
	private List<String> readEntryFromTxt(String username) {
		List<String> entryList = new ArrayList<String>();
		BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("D:\\Yuksek Lisans\\YL_DATA\\Zemberek\\users_deneme2\\analiz_edildi\\" +username + ".txt"));
            String line;
            while ((line = br.readLine()) != null) {
            	entryList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return entryList;
    }


	/**
	 * Bir kullanıcının en fazla hangi kelimeleri kullandığını gösteren metoddur
	 */
	@Override
	public void findWordsByAuthorFromDatabase(Set <Integer> userIdList) {
		System.out.println("Kullanıcıların en çok kullandıkları kelimeler bulunuyor.");
		
		List<UserEntry> userEntryList = entryManager.getUserEntryList(userIdList);
		if (CollectionUtils.isEmpty(userEntryList)) {
			return;
		}
		
		Map<String, List<UserEntry>> usernameEntryList = userEntryList.stream().collect(Collectors.groupingBy(a-> a.getUsername()));
		
		userIdList.clear();
		
		System.out.println("Hesaplanacak veri -> " + usernameEntryList.size());
		
		for (Map.Entry<String, List<UserEntry>> entrySet : usernameEntryList.entrySet()) {
			String username = entrySet.getKey();
			System.out.println("Kullanıcı hesaplama başladı -> " + username);
			
			List<String> entryList = entrySet.getValue().stream().map(a-> a.getEntryDescription()).collect(Collectors.toList());
			
			List<WordIndex>  wordIndexList = getWordIndexList(entryList);
			
			createTxtForUserEntryOperation(username, wordIndexList);
		}
		
		System.out.println("Kullanıcıların en çok kullandıkları kelimelerin hesabı tamamlandı");
	}


	private void createTxtForUserEntryOperation(String username, List<WordIndex> wordIndexList) {
		int count = 0;
		try {
			String filename= "userWordList.txt";
		    
		    FileOutputStream fileStream = new FileOutputStream(new File(filename), true);
		    OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");
		    
		    for (WordIndex word : wordIndexList) {
		    	if (globalTotalUserEntryCount == count) {
		    		break;
		    	}
		    	writer.write(username + "-" + word.getWord() + "-" + word.getIndex() + "-" + word.getFrequency() +"\r\n");//appends the string to the file
		    	
		    	count ++;
		    }
		    writer.write("----------------------------------------------------------------------------------- username = " + username +"\r\n");
		    
		    writer.close();
			
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	/**
	 * Entryleri title a göre gruplayıp zemberek için dışarı aktaran metoddur.
	 */
	@Override
	public void exportEntriesGroupByTitle() {
		System.out.println("Başlığa göre gruplanmış dışa aktarım başladı");
		
		List<Integer> titleIdList = titleManager.getTitleIdList();
		
		System.out.println("Toplam Title Sayısı -> " + titleIdList.size());
		int logCount = 0;
		List<Integer> splittedIdList = new ArrayList<Integer>();
		for (Integer titleId : titleIdList) {
			splittedIdList.add(titleId);
			
			if (splittedIdList.size() % 60 == 0) {
				List<TitleEntry> titleEntryList = entryManager.getEntriesByTitleIdList(splittedIdList);
				
				Map<String, List<TitleEntry>> titleIdEntryList = titleEntryList.stream().collect(Collectors.groupingBy(a-> a.getTitleName()));

				for (Map.Entry<String, List<TitleEntry>> entrySet : titleIdEntryList.entrySet()) {

					String titleName = entrySet.getKey();

					List<String> entryList = entrySet.getValue().stream().map(a -> a.getEntryDescription())
							.collect(Collectors.toList());

					createTxtForGroupingTitle(titleName, entryList);
				}
				
				splittedIdList.clear();

			}
			
			logCount++;
			
			if (logCount % 10 == 0) {
				System.out.println("Çalışılan toplam veri sayısı -> " + titleIdList.size() + " --- Kalan Veri Sayısı -> " + (titleIdList.size() - logCount));
			}
		}
		
		System.out.println("Title a göre gruplanmış çıktı alma tamamlandı");

	}
	
	private void createTxtForGroupingTitle (String titleName, List<String> entryList) {
		try {
			String path="D:\\YL_DATA\\titles\\" +titleName +".txt";
            File file = new File(path);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            for (String entry : entryList) {
		    	fw.write(entry + " ");//appends the string to the file
		    }

            // Close connection
            bw.close();
			
		    fw.close();
		    
		    System.out.println("TXT dosyası şu başlık için oluşturuldu -> " + titleName);
			
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}


	/**
	 * Entryleri kullanıcılara göre gruplayıp dışarı aktaran metoddur.
	 */
	@Override
	public void exportEntriesGroupByUser() {
		System.out.println("Kullanıcılara göre gruplanmış entry dışa aktarım başladı");
		
		List<Integer> userIdList = userManager.getUserIdList();
		
		System.out.println("Toplam Kullanıcı Sayısı -> " + userIdList.size());
		int logCount = 0;

		Set<Integer> splittedIdList = new HashSet<Integer>();

		for (Integer userId : userIdList) {
			splittedIdList.add(userId);

			if (splittedIdList.size() % 300 == 0) {
				List<UserEntry> userEntryList = entryManager.getUserEntryList(splittedIdList);

				Map<String, List<UserEntry>> usernameEntryList = userEntryList.stream()
						.collect(Collectors.groupingBy(a -> a.getUsername()));

				for (Map.Entry<String, List<UserEntry>> entrySet : usernameEntryList.entrySet()) {
					String username = entrySet.getKey();
					
					List<String> entryList = entrySet.getValue().stream().map(a -> a.getEntryDescription())
							.collect(Collectors.toList());

					createTxtForGroupingUser(username, entryList);
				}
				
				splittedIdList.clear();

			}
			
			logCount++;
			
			if (logCount % 40 == 0) {
				System.out.println("Çalışılan toplam veri sayısı -> " + userIdList.size() + " --- Kalan Veri Sayısı -> " + (userIdList.size() - logCount));
			}
		}
	}


	private void createTxtForGroupingUser(String username, List<String> entryList) {
		try {
			String path="D:\\YL_DATA\\users\\" +username +".txt";
            File file = new File(path);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            for (String entry : entryList) {
		    	fw.write(entry + " ");//appends the string to the file
		    }

            // Close connection
            bw.close();
			
		    fw.close();
		    
		    System.out.println("TXT dosyası şu kullanıcı için oluşturuldu -> " + username);
			
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}
	
	@Override
	public void exportWrongVocabs() {
		// Directory içinde ne kadar dosya varsa bunların path ini bir listeye doldurur
		List<Path> filesInDirectory = new ArrayList<Path>();

		try (Stream<Path> paths = Files.walk(Paths.get("D:\\YL_DATA\\users\\"))) {
			paths.filter(Files::isRegularFile).forEach(filesInDirectory::add);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Veri okunurken problem oluştu");
		}
		
		//Sessiz harfler -> b, c, ç, d, f, g, ğ, h, j, k, I, m, n, p, r, s, ş, t, v, y, z
		
		List<String> silentLetters = new ArrayList<String>();
		silentLetters.add("b");
		silentLetters.add("c");
		silentLetters.add("ç");
		silentLetters.add("d");
		silentLetters.add("f");
		silentLetters.add("g");
		silentLetters.add("ğ");
		silentLetters.add("h");
		silentLetters.add("j");
		silentLetters.add("k");
		silentLetters.add("l");
		silentLetters.add("m");
		silentLetters.add("n");
		silentLetters.add("p");
		silentLetters.add("r");
		silentLetters.add("s");
		silentLetters.add("ş");
		silentLetters.add("t");
		silentLetters.add("v");
		silentLetters.add("y");
		silentLetters.add("z");
		

		Scanner scn = new Scanner(System.in);
		
		Map<String, String> map = entryManager.getWrongCorrectWordMap();

		for (Path p : filesInDirectory) {
			try {
				List<String> entryList = readToTxt(p.toString());

				for (String paragraph : entryList) {

					String[] arr = paragraph.split(" ");

					for (String word : arr) {
						if (word.contains("?")) {
							if (word.contains(".")) {
								word = word.replace(".", "");
							}
							if (word.contains("!")) {
								word = word.replace("!", "");
							}
							if (word.contains(",")) {
								word = word.replace(",", "");
							}
							if (word.contains("(")) {
								word = word.replace("(", "");
							}
							if (word.contains(")")) {
								word = word.replace(")", "");
							}
							if (word.contains("...")) {
								word = word.replace("...", "");
							}
							if (word.contains(";")) {
								word = word.replace(";", "");
							}
							if (word.contains("*")) {
								word = word.replace("*", "");
							}
							if (word.contains(":")) {
								word = word.replace(":", "");
							}
							if (word.contains("-")) {
								word = word.replace("-", "");
							}
							if (word.contains("+")) {
								word = word.replace("+", "");
							}
							if (word.contains("http")) {
								continue;
							}
							word = word.trim();
							
							if (! map.containsKey(word)) {
								System.out.println("KELİME : " + word);
								String firstWordStatus = word;
								for (int i = 1; i < word.length(); i++) {
									String tmpWord = word.substring(0, i);
									if (map.containsKey(tmpWord)) {
										word = word.replace(tmpWord, map.get(tmpWord));
									}
								}
								System.out.println("KELİME DÜZENLENDİ: " + word);
//								int count = StringUtils.countMatches(word, "?");
//								if (count == 1) {
//									int questionMarkIndex = word.indexOf("?");
//									String word1 = word.substring(questionMarkIndex-1, questionMarkIndex);
//									
//									boolean equality1 = silentLetters.stream().filter(a -> a.equals(word1)).findFirst().isPresent();
//									if (equality1) {
//										int control = questionMarkIndex+1;
//										if (control < word.length()) {											
//											String word2 = word.substring(questionMarkIndex+1, control + 1);
//											boolean equality2 =  silentLetters.stream().filter(a -> a.equals(word2)).findFirst().isPresent();
//											if (equality2) {
//												word = word.replace("?", "ı");
//												
//											}
//										}
//									}
//									System.out.println("HARF GİRİNİZ : ");
//									String onlyCharacter = scn.nextLine();
//									if (! StringUtils.isBlank(onlyCharacter)) {										
//										word = word.replace("?", onlyCharacter);
//									}
//								}
								
//								if (! word.contains("?")) {
									System.out.println("OTOMATİK KAYIT : " + word);
									entryManager.saveToWrongWordTable(firstWordStatus, word);
									map.put(firstWordStatus, word);
//								} 
//									else {									
//									if (! map.containsKey(word)) {									
//										System.out.println(word);
//										
//										System.out.println("Yeni Değeri Giriniz : ");
//										
//										String newWord = scn.nextLine();
//										if (!newWord.equals("")) {
//											entryManager.saveToWrongWordTable(word, newWord);
//											map.put(word, newWord);
//										}
//									}
//								}
							}

						}
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//MOVING
			moveToAnotherDirectory(p.toString(), "D:\\YL_DATA\\WordCorrection\\users\\");
		}
		
		
		scn.close();
		
	}
    
    private void moveToAnotherDirectory(java.lang.String filePath, java.lang.String tmpFilePath) {
    	try{
    		
      	   File afile =new File(filePath);
      		
      	   if(afile.renameTo(new File(tmpFilePath + afile.getName()))){
      		System.out.println("Dosya başka bir dizine taşındı!");
      	   }else{
      		System.out.println("Taşıma HATASI!");
      	   }
      	    
      	}catch(Exception e){
      		e.printStackTrace();
      	}
		
	}


	private List<String> readToTxt(String path) {		
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String line;
			List<String> wordList = new ArrayList<String>();
			while ((line = in.readLine()) != null) {
				wordList.add(line);
			}
			in.close();
			
			return wordList;
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void createBigClamInput() {
		List<PMIValueIndexes> list = entryManager.getBigClamInput(bigClamNumberOfOccurrences);
		if (CollectionUtils.isNotEmpty(list)) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
				for (PMIValueIndexes ind : list) {
					out.write(ind.getIndex1() + "	" + ind.getIndex2() + "\r\n");
				}
				out.close();
				System.out.println("BigCLAM TXT oluşturuldu.!");
			} catch (IOException e) {
				System.err.println("BigCLAM TXT oluşturulurken hata oluştu!");
			}

		} else {
			System.out.println("Liste boş, tabloyu kontrol et");
		}
	}
	
	@Override
	public void createBigClamInputForCollaborationNetwork(String collaborationNetworkPath) {
		List<UserUserTitle> userUserTitleList = getBigClamInputForCollaborationNetwork(collaborationNetworkPath);
		
		Map<String, Integer> userNameSembolicIdMap = new HashMap<String, Integer>();
		int idCount = 1;
		for (UserUserTitle us : userUserTitleList) {
			if (! userNameSembolicIdMap.containsKey(us.getUserName1())) {
				userNameSembolicIdMap.put(us.getUserName1(), idCount);
				idCount++;
			}
			
			if (! userNameSembolicIdMap.containsKey(us.getUserName2())) {
				userNameSembolicIdMap.put(us.getUserName2(), idCount);
				idCount++;
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("collaborationNetwork_userNameId.txt"));
			for (Map.Entry<String, Integer> entry : userNameSembolicIdMap.entrySet()) {
				out.write(entry.getValue() + "	" + entry.getKey() + "\r\n");
			}
			
			out.close();
			System.out.println("username Id Map TXT oluşturuldu.!");
		} catch (IOException e) {
			System.err.println("username Id Map TXT oluşturulurken hata oluştu!");
		}
		
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam_collaborationNetwork.txt"));
			for (UserUserTitle us : userUserTitleList) {
				//Hesaplanan quantile değerine göre karar verildi.
				if (us.getJaccardSimilarity().compareTo(new BigDecimal(0.01)) > 0 && us.getJaccardSimilarity().compareTo(new BigDecimal(0.13)) < 0) {					
					int id1 = userNameSembolicIdMap.get(us.getUserName1());
					int id2 = userNameSembolicIdMap.get(us.getUserName2());
					
					out.write(id1 + "	" + id2 + "\r\n");
				}
			}
			out.close();
			System.out.println("Collaboration Network için BigCLAM TXT oluşturuldu.!");
		} catch (IOException e) {
			System.err.println("Collaboration Network için BigCLAM TXT oluşturulurken hata oluştu!");
		}
		
		
	}
	
	private List<UserUserTitle> getBigClamInputForCollaborationNetwork(String collaborationNetworkPath) {
		List<UserUserTitle> userUserTitleList = new ArrayList<UserUserTitle>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(collaborationNetworkPath));
			String line;
			while ((line = in.readLine()) != null) {
				String arr[] = line.split("-");
				UserUserTitle us = new UserUserTitle();
				us.setUserName1(arr[0]);
				us.setUserName2(arr[1]);
				us.setJaccardSimilarity(new BigDecimal(arr[2]));
				
				userUserTitleList.add(us);
			}
			in.close();
			
			return userUserTitleList;
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public void getAllInputIntoTxt() {
		List<PMIValueIndexes> list = entryManager.getAllInputToTxt();
		if (CollectionUtils.isNotEmpty(list)) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("allData.txt"));
				for (PMIValueIndexes ind : list) {
					out.write(ind.getIndex1() + ";" + ind.getIndex2() + ";" + ind.getFrequencyInTogether() + "\r\n");
				}
				out.close();
				System.out.println("FrequencyInTogether TXT oluşturuldu.!");
			} catch (IOException e) {
				System.err.println("FrequencyInTogether TXT oluşturulurken hata oluştu!");
			}

		} else {
			System.out.println("Liste boş, tabloyu kontrol et");
		}
	}
	
	@Override
	public void findSumOfRowsForAlternatePMI(String fileReadingPath, List<String> outputFromAnotherFunction) {
		System.out.println("Alternate PMI Summation oluşturma operasyonu tetiklendi");
		System.out.println("Small Row Count : " + globalRowCountSmall + " --- Big Row Count : " + globalRowCount);
		
		List<String> readFromTxtEntries = new ArrayList<String>();
		if (outputFromAnotherFunction == null) {			
			readFromTxtEntries = importManager.readFromTxt(fileReadingPath);
			if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
				System.err.println("Okunmaya çalışılan dosya boş veya okuma işlemi sırasında hata alındı.");
				System.err.println("Program kapatılıyor.");
				System.exit(0);
			}
		} else {
			readFromTxtEntries.addAll(outputFromAnotherFunction);
			outputFromAnotherFunction.clear();
		}
		
		System.out.println("Bir kelimenin kaç defa sistemde görüldüğüyle ilgili liste oluşturuluyor");
		
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		wordsOccured.clear();
		
		List<String> splittedEntries = new ArrayList<String> ();
		for (String s : readFromTxtEntries) {				
			splittedEntries = splittedEntryDescription(splittedEntries, s);
		}
		
		readFromTxtEntries.clear();
		
		Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
		
		System.out.println("PMI Value Indexes çözümü başladı!");
		
		for (int i = 0; i < splittedEntries.size(); i++) {
			int countGo = i + 1;
			int countBack = i - 1 ;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countBack > -1) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countBack));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countBack--;
				}
				
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					createMatrixData(matrixData, numberOfCellForPivotWord, numberOfCellForAlternativeWord);
					countGo++;
				}
			}
		}
		
		System.out.println("Matrix oluşturuldu, toplamlar yazılıyor");
		
		Map<Integer, BigDecimal> rowFrequencySumMap = new HashMap<Integer, BigDecimal> ();
		for (Map.Entry<PMIValueIndexes, BigDecimal> entry : matrixData.entrySet()) {
			if (rowFrequencySumMap.containsKey(entry.getKey().getIndex1())) {
				rowFrequencySumMap.put(entry.getKey().getIndex1(), rowFrequencySumMap.get(entry.getKey().getIndex1()).add(entry.getValue()));
			} else {
				rowFrequencySumMap.put(entry.getKey().getIndex1(), entry.getValue());
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("summationForAlternatePMI.txt"));
			for (Map.Entry<Integer, BigDecimal> ind : rowFrequencySumMap.entrySet()) {
				out.write(ind.getKey() + "-" + ind.getValue() + "\r\n");
			}
			out.close();
			System.out.println("AlternatePMISummation TXT oluşturuldu.!");
		} catch (IOException e) {
			System.err.println("AlternatePMISUmmation TXT oluşturulurken hata oluştu!");
		}
	}
	
	@Override
	public void searchNetworkLinks(String linkFilePath) {
		//Dosyaları okuma 
		// 1- mostSimilarWord dosyasını oku.
		Map<String, List<MostSimilarWord>> mostSimilarWordMap = readMostSimilarWordForNetworkLink(linkFilePath);
		
		// 2- jaccardSimilarity dosyasını oku
		Map<String, List<UserUserTitle>> jaccardSimilarityMap = readJaccardSimilarityForNetworkLink(linkFilePath);
		
		// 3- userWordList dosyasını oku
		Map<String, List<UserWord>> userWordMap = readUserWordListForNetworkLink(linkFilePath);
		
		int count = 0;
		for (Map.Entry<String, List<UserUserTitle>> entrySet : jaccardSimilarityMap.entrySet()) {
			System.out.println("Analiz edilen kullanıcı sayısı : " + count);
			
			String originUser = entrySet.getKey();
			List<UserUserTitle> jaccardSimilarityValues = entrySet.getValue();
			
			List<MostSimilarWord> writeableList = new ArrayList<MostSimilarWord>();
			
			for (UserUserTitle jsv : jaccardSimilarityValues) {
				String otherUser = jsv.getUserName2();
				
				List<UserWord> originUserWords = userWordMap.get(originUser);
				List<UserWord> otherUserWords = userWordMap.get(otherUser);
				
				// İlk işlem
				for (UserWord originWord : originUserWords) {
					List<MostSimilarWord> originSimilarWordList = mostSimilarWordMap.get(originWord.getWord());
					if (CollectionUtils.isNotEmpty(originSimilarWordList)) {						
						for (UserWord otherWord : otherUserWords) {
							Optional<MostSimilarWord> opt = originSimilarWordList.stream().filter(a -> a.getOtherWord().equals(otherWord.getWord())).findAny();
							if (opt.isPresent()) {							
								//sonucu bir yerlere yazdıracağız
								MostSimilarWord msw = opt.get();
								writeableList.add(msw);
							}
						}
					}
				}
				writeSimilarityResult(writeableList, jsv);
			}
			
			count++;
		}
		
	}
	
	private void writeSimilarityResult(List<MostSimilarWord> writeableList, UserUserTitle jsv) {
		try {
			String filename= "D:\\Yuksek Lisans\\YL_DATA\\networkLinks\\" + jsv.getUserName1() + ".txt";
		    
		    FileOutputStream fileStream = new FileOutputStream(new File(filename), true);
		    OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");

		    for (MostSimilarWord msw : writeableList) {		    	
		    	writer.write(msw.getOriginWord() + "-" + msw.getOtherWord() + "-" + msw.getSimilarityRate() + "------ Kullanıcı Bilgisi ------"+ jsv.getUserName1() + "-" + jsv.getUserName2() + "-" + jsv.getJaccardSimilarity() +"\r\n");
		    }

			writer.close();

		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}


	private Map<String, List<UserWord>> readUserWordListForNetworkLink(java.lang.String linkFilePath) {
		try {
			List<UserWord> userWordList = new ArrayList<UserWord>();
			BufferedReader in = new BufferedReader(new FileReader(linkFilePath +"\\userWordList.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				if (! line.contains("-----------------------------------------------------------------------------------")) {					
					UserWord uw = new UserWord();
					
					String arr[] = line.split("-");
					uw.setUserName(arr[0]);
					
					arr[1] = arr[1].toLowerCase();
					arr[1] = encodingConverter(arr[1]);
					uw.setWord(arr[1]);
					
					uw.setPriority(Integer.parseInt(arr[2]));
					uw.setCount(Integer.parseInt(arr[3]));
					
					userWordList.add(uw);
				}
			}
			
			in.close();
			
			Map<String, List<UserWord>> returnMap = new HashMap<>();
			
			returnMap = userWordList.stream().collect(Collectors.groupingBy(UserWord :: getUserName));
			
			return returnMap;
			
		}  catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}


	private Map<String, List<UserUserTitle>> readJaccardSimilarityForNetworkLink(String linkFilePath) {
		try {
			List<UserUserTitle> userUserTitleList = new ArrayList<UserUserTitle>();
			BufferedReader in = new BufferedReader(new FileReader(linkFilePath +"\\jaccardSimilarity.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				UserUserTitle uut = new UserUserTitle();
				String arr[] = line.split("-");
				
				uut.setUserName1(arr[0]);
				uut.setUserName2(arr[1]);
				if (arr[2].contains("E")) {							
					uut.setJaccardSimilarity(BigDecimal.ZERO);
				} else {					
					uut.setJaccardSimilarity(new BigDecimal(arr[2]));
				}
				
				if (uut.getJaccardSimilarity().compareTo(new BigDecimal("0.031")) < 0) {
					continue;
				}
				
				userUserTitleList.add(uut);
			}
			in.close();
			
			Map<String, List<UserUserTitle>> returnMap = new HashMap<>();
			
			returnMap = userUserTitleList.stream().sorted(Comparator.comparing(UserUserTitle :: getJaccardSimilarity).reversed()).collect(Collectors.groupingBy(UserUserTitle :: getUserName1));
			
			return returnMap;
			
		}  catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}


	private Map<String, List<MostSimilarWord>> readMostSimilarWordForNetworkLink(String linkFilePath) {
		try {
			List<String> mostSimilarWords = new ArrayList<String>();
			List<MostSimilarWord> mostSimilarWordList = new ArrayList<MostSimilarWord>();
			BufferedReader in = new BufferedReader(new FileReader(linkFilePath +"\\mostSimilarWords.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains("-----------------------------------------------------------------------------------")) {
					for (String s : mostSimilarWords) {
						MostSimilarWord msw = new MostSimilarWord();
						
						String arr[] = s.split("-");
						if (arr.length > 3) {
							continue;
						}
						
						arr[0] = arr[0].toLowerCase();
						arr[0] = encodingConverter(arr[0]);
						msw.setOriginWord(arr[0]);
						
						arr[1] = arr[1].toLowerCase();
						arr[1] = encodingConverter(arr[1]);
						msw.setOtherWord(arr[1]);
						
						if (arr[2].contains("E")) {							
							msw.setSimilarityRate(BigDecimal.ZERO);
						} else {
							msw.setSimilarityRate(new BigDecimal(arr[2]));
						}
						
						mostSimilarWordList.add(msw);
						
					}
					mostSimilarWords.clear();
				} else {					
					mostSimilarWords.add(line);
				}
			}
			in.close();
			
			Map<String, List<MostSimilarWord>> returnMap = new HashMap<>();
			
			returnMap = mostSimilarWordList.stream().collect(Collectors.groupingBy(MostSimilarWord::getOriginWord));
			
			return returnMap;
			
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}
	
	private String encodingConverter(String word) {
		if (word.contains("ä±")) {
			word = word.replace("ä±", "ı");
		}
		if (word.contains("ã¼")) {
			word = word.replace("ã¼", "ü");
		}
		if (word.contains("åÿ")) {
			word = word.replace("åÿ", "ş");
		}
		if (word.contains("ã§")) {
			word = word.replace("ã§", "ç");
		}
		if (word.contains("ã¶")) {
			word = word.replace("ã¶", "ö");
		}
		if (word.contains("äÿ")) {
			word = word.replace("äÿ", "ğ");
		}
		if (word.contains("ä°")) {
			word = word.replace("ä°", "i");
		}
		if (word.contains("ã–")) {
			word = word.replace("ã–", "ö");
		}
		
		return word;
	}
	
	@Override
	public void searchNetworkCommunitiesLinks(String linkDataPath) {
		List<UserCommunity> userCommunityList = createUserCommunityList(linkDataPath);
		
		List<WordCommunity> wordCommunityList = createWordCommunityList(linkDataPath);
		
		Map<String, List<WordCommunity>> wordCommunityMap = wordCommunityList.stream().collect(Collectors.groupingBy(WordCommunity :: getWord));

		Map<String, List<UserWord>> userWordMap = readUserWordListForNetworkLink(linkDataPath);
		
		Map<String, List<MostSimilarWord>> mostSimilarWordMap = readMostSimilarWordForNetworkLink(linkDataPath);
		
		int count = 0;
		for (UserCommunity uc1 : userCommunityList) {
			List<String> comList1 = uc1.getCommunityList();
			List<CommonCommunityResult> resultSet = new ArrayList<CommonCommunityResult>();
			
			for (UserCommunity uc2 : userCommunityList) {
				CommonCommunityResult result = new CommonCommunityResult();
				//Eğer iki kullanıcı adı eşitse bu değeri geçelim
				if (uc2.getUserName().equals(uc1.getUserName())) {
					continue;
				}
				
				result.setUserName1(uc1.getUserName());
				result.setUserName2(uc2.getUserName());
				
				// Eğer bu iki kullanıcının collaboration network de ortak kümesi varsa onları bulalım
				List<String> comList2 = uc2.getCommunityList();
				for (String s1 : comList1) {
					Optional<String> findCommonCommunity = comList2.stream().filter(s -> s.equals(s1)).findAny();
					if(findCommonCommunity.isPresent()) {
						// Burada birşey yapacağız. ResultSet vs
						result.getCommonCommunitiesCN().add(findCommonCommunity.get());
					}
				}
				
				// Eğer ortak küme bulunduysa ortak kelime arayacağız. Yoksa bir sonraki
				// kullanıcıdan devam
				if (CollectionUtils.isNotEmpty(result.getCommonCommunitiesCN())) {
					List<UserWord> originUserWords = userWordMap.get(result.getUserName1());
					List<UserWord> otherUserWords = userWordMap.get(result.getUserName2());

					// İlk işlem
					for (UserWord originWord : originUserWords) {
						List<MostSimilarWord> originSimilarWordList = mostSimilarWordMap.get(originWord.getWord());
						if (CollectionUtils.isNotEmpty(originSimilarWordList)) {
							for (UserWord otherWord : otherUserWords) {
								Optional<MostSimilarWord> opt = originSimilarWordList.stream()
										.filter(a -> a.getOtherWord().equals(otherWord.getWord())).findAny();
								if (opt.isPresent()) {
									// Bu noktada benzer kelimeler yakaladık. Bu benzer kelimelerden community ortaklığı yapanlar var mı diye bakıyoruz
									MostSimilarWord msw = opt.get();
									List<WordCommunity> list = wordCommunityMap.get(msw.getOriginWord());
									if (CollectionUtils.isNotEmpty(list)) {
										List<String> originCommunities = list.get(0).getCommunityList();
										for (String community : originCommunities) {
											List<WordCommunity> list2 = wordCommunityMap.get(msw.getOtherWord());
											if (CollectionUtils.isNotEmpty(list2)) {
												Optional<String> commonCommunityWAN = list2.get(0).getCommunityList()
														.stream().filter(s -> s.equals(community)).findAny();
												
												if (commonCommunityWAN.isPresent()) {
													result.getCommonCommunitesWAN().add(msw.getOriginWord() + "-" + msw.getOtherWord() + "-" + commonCommunityWAN.get());
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if (CollectionUtils.isNotEmpty(result.getCommonCommunitiesCN()) && CollectionUtils.isNotEmpty(result.getCommonCommunitesWAN())) {					
					resultSet.add(result);
				}
			}
			
			//İşlemlerin hepsi bittiğinde bu kullanıcı için bir çıktı üretelim.
			if (CollectionUtils.isNotEmpty(resultSet)) {				
				createOutputForNetworkSimilarities(resultSet);
			}
			count++;
			System.out.println("Analiz edilen kullanıcı sayısı : " + count);
		}
	}


	private void createOutputForNetworkSimilarities(List<CommonCommunityResult> resultSet) {
		try {
			String filename= "D:\\Yuksek Lisans\\YL_DATA\\networkLinks\\communitySearch\\" + resultSet.get(0).getUserName1() + ".txt";
		    
		    FileOutputStream fileStream = new FileOutputStream(new File(filename), true);
		    OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8");

		    for (CommonCommunityResult result : resultSet) {
		    	String resultBuilder = new String();
		    	
		    	resultBuilder +=  result.getUserName1() + "-" + result.getUserName2() + "-";
		    	
		    	
		    	writer.write("Kullanıcı Ortak CM[");
		    	for (String s : result.getCommonCommunitiesCN()) {
		    		resultBuilder += s + ",";
		    	}
		    	
		    	resultBuilder = resultBuilder.substring(0, resultBuilder.length() - 1);
		    	resultBuilder += "] ----------- Kelime Ortak CM[";
		    	
		    	Map<String, String> mapResultWordResult = new HashMap<String, String>();
		    	for (String s : result.getCommonCommunitesWAN()) {
		    		String [] arr = s.split("-");
		    		String key = arr[0] + "-" + arr[1];
		    		if (mapResultWordResult.containsKey(key)) {
		    			String r = "," + arr[2];
		    			mapResultWordResult.put(key, mapResultWordResult.get(key) + r);
		    		} else {
		    			mapResultWordResult.put(key, key + "-" + arr[2]);
		    		}
		    		
		    	}
		    	
		    	for (Map.Entry<String, String> entrySet : mapResultWordResult.entrySet()) {
		    		resultBuilder += "!!!!";
		    		resultBuilder += entrySet.getValue();
		    		resultBuilder += "!!!!";
		    	}
		    
		    	resultBuilder += "]";
		    	
		    	writer.write(resultBuilder + "\r\n");
		    }

			writer.close();

		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}


	private List<WordCommunity> createWordCommunityList(String linkDataPath) {
		try {
			List<WordCommunity> resultSet = new ArrayList<WordCommunity>();
			
			BufferedReader in = new BufferedReader(new FileReader(linkDataPath +"\\wan_communities.txt"));
			String line;
			boolean first = true;
			Map<Integer, String> headerMap = new HashMap<Integer, String>();
			while ((line = in.readLine()) != null) {
				String arr[] = line.split(",");
				if (first) {
					for (int i = 0; i < arr.length; i++) {
						headerMap.put(i, arr[i]);
					}
					first = false;
					continue;
				}
				
				WordCommunity wc = new WordCommunity();
				for (int i = 0; i < arr.length; i++) {
					if (headerMap.get(i) != null) {						
						if (headerMap.get(i).equals("Label")) {
							arr[i] = arr[i].toLowerCase();
							arr[i] = encodingConverter(arr[i]);
							wc.setWord(arr[i]);
							continue;
						}
						
						if (arr[i].equals("true")) {
							List<String> communityList = wc.getCommunityList();
							communityList.add(headerMap.get(i));
							wc.setCommunityList(communityList);
						}
					}
				}
				
				resultSet.add(wc);
			}
			in.close();
			
			return resultSet;
			
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}


	private List<UserCommunity> createUserCommunityList(String linkDataPath) {
		try {
			List<UserCommunity> resultSet = new ArrayList<UserCommunity>();
			BufferedReader in = new BufferedReader(new FileReader(linkDataPath +"\\cn_communities.txt"));
			String line;
			boolean first = true;
			Map<Integer, String> headerMap = new HashMap<Integer, String>();
			while ((line = in.readLine()) != null) {
				String arr[] = line.split(",");
				if (first) {
					for (int i = 0; i < arr.length; i++) {
						headerMap.put(i, arr[i]);
					}
					first = false;
					continue;
				}
				
				UserCommunity uc  = new UserCommunity();
				for (int i = 0; i < arr.length; i++) {
					if (headerMap.get(i).equals("Label")) {
						uc.setUserName(arr[i]);
						continue;
					}
					
					if (arr[i].equals("true")) {
						List<String> communityList = uc.getCommunityList();
						communityList.add(headerMap.get(i));
						uc.setCommunityList(communityList);
					}
				}
				
				resultSet.add(uc);
			}
			in.close();
			
			return resultSet;
			
		} catch (Exception e) {
			System.err.println("TXT dosyası okunurken kritik bir hata oluştu.");
			e.printStackTrace();
			return null;
		}
	}
}
