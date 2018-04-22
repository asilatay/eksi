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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import viewmodel.CosineSimilarityIndex;
import viewmodel.PMIValueIndexes;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;
import viewmodel.WordIndex;

public class EngineManagerImpl implements EngineManager {
	
	TitleManager titleManager = new TitleManagerImpl();
	
	UserManager userManager = new UserManagerImpl();
	
	EntryManager entryManager = new EntryManagerImpl();
	
	ExportManager exportManager = new ExportManagerImpl();
	
	DateUtil dateUtil = new DateUtil();
	
	ImportManager importManager = new ImportManagerImpl();
	
	private final static int turningNumber = 15;
	
	private final static int turningNumberForNewCoOccurence = 5;
	
	private final static String directoryOfSimilarUsersThatWroteSameTitle = "userUserTitles.txt";
	
	private final static int globalColumnCount = 400;
	
	private final static int globalRowCount = 1000;
	
	private final static int globalMostSimilarWordCount = 15;
	
	private final static int globalTotalUserEntryCount = 20;
	
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
			retList.add(arr[i].toLowerCase());
		}
		return retList;
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
		//Bu değer ekşi verisi çalıştırılırken splittedEntries.size() olarak değiştirilecek
		int allWordOfCorpusSize = readFromTxtEntries.size();
		readFromTxtEntries.clear();
		Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
		System.out.println("PMI Value Indexes çözümü başladı!");
		for (int i = 0; i < splittedEntries.size(); i++) {
			int countGo = i + 1;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					PMIValueIndexes ind = new PMIValueIndexes(numberOfCellForPivotWord, numberOfCellForAlternativeWord, BigDecimal.ZERO, BigDecimal.ZERO);
					PMIValueIndexes symIndex = new PMIValueIndexes(numberOfCellForAlternativeWord, numberOfCellForPivotWord, BigDecimal.ZERO, BigDecimal.ZERO);
					if (matrixData.containsKey(ind) && ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount) {
						matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
						if (symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount) {							
							if (matrixData.get(symIndex) != null) {							
								matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
							} else {
								matrixData.put(symIndex, BigDecimal.ONE);
							}
						}
					} else if (matrixData.containsKey(symIndex) && symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount) {
						matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
						if (ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount) {							
							if (matrixData.get(ind) != null) {							
								matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
							} else {
								matrixData.put(ind, BigDecimal.ONE);
							}
						}
					} else if (ind.getIndex1() < globalRowCount && ind.getIndex2() < globalColumnCount){
						matrixData.put(ind, BigDecimal.ONE);
						if (symIndex.getIndex1() < globalRowCount && symIndex.getIndex2() < globalColumnCount) {
							matrixData.put(symIndex, BigDecimal.ONE);
						}
					}
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
//		matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, rankingSize);
		matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, rankingSize);
		
		//TODO alternate i açarken map of indexes aç
//		Map<Integer, List<String>> mapOfIndexes = getMapOfIndexes(matrixData);
		
		// Alternate PMI değerini hesaplayacağız.
//		matrixData = calculateAndSetAlternatePMIValues (matrixData, mapOfIndexes, rankingSize);
//		mapOfIndexes.clear();
		
		//TODO burada totalWordNumberBigData yerine tüm veri çalıştığında ranking size yazar!!
		createVectorsOneByOneWithVirtualMatrix(matrixData, globalRowCount, globalColumnCount);
		
		findMostSimilarWords(matrixData, globalRowCount);
		
	}
	
	private void createOutputNodeIdNodeNameForBigClam(List<WordIndex> wordsOccured) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("nodeName.txt"));
			 int count = 0;
			 for(WordIndex  word  : wordsOccured){
				 if (count == globalRowCount) {
					 break;
				 }
				 out.write(word.getIndex() + "	" + word.getWord() + "\r\n");
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
		}
		
		System.out.println("PMI calculation is just finished");
		
		return matrixData;
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
		//SHIFTING
		//togetherValue = togetherValue.add(BigDecimal.ONE);
		
		BigDecimal probW1AndW2 = togetherValue.divide(totalWSize, 10, RoundingMode.HALF_UP);
		
		BigDecimal frequencyIndex1 = wordFrequencyMap.get(calculativeValue.getIndex1()).divide(totalWSize, 10, RoundingMode.HALF_UP);
		BigDecimal frequencyIndex2 = wordFrequencyMap.get(calculativeValue.getIndex2()).divide(totalWSize, 10, RoundingMode.HALF_UP);
		
		BigDecimal pmiValue = probW1AndW2.divide(frequencyIndex1.multiply(frequencyIndex2),10 ,RoundingMode.HALF_UP)
				.setScale(10, RoundingMode.HALF_UP);
		// pmiValue değerinin logaritmasını alıp tekrar üstüne set et. (Logaritma 0 çıkacak senaryoya dikkat et)
		calculativeValue.setPmiValue(pmiValue);
		try {
			BigDecimal logarithmicValue = log10(calculativeValue.getPmiValue(), 10);
			if (logarithmicValue.signum() < 0) {				
				calculativeValue.setLogaritmicPmiValue(BigDecimal.ZERO);
			} else {
				calculativeValue.setLogaritmicPmiValue(logarithmicValue);
			}
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
//		int D = matrixData.size();
		System.out.println("Alternate PMI calculation is just started");
		
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			calculationOfAlternatePMI(mapOfIndexes, D, data.getValue(), data.getKey());
		}
		
		System.out.println("Alternate PMI calculation is just finished");
		
		return matrixData;
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
		if (!valueList1.isEmpty()) {
			for (String s : valueList1) {
				String [] arr = s.split("-");
				if (arr.length == 2 && Integer.parseInt(arr[0]) != index2C) {
					w = w.add( new BigDecimal(arr[1]));
				}
			}
		}
		BigDecimal c = BigDecimal.ZERO;
		List <String> valueList2 = mapOfIndexes.get(index2C);
		if (!valueList2.isEmpty()) {
			for (String s : valueList2) {
				String [] arr = s.split("-");
				if (arr.length == 2 && Integer.parseInt(arr[0]) != index1W) {
					c = c.add(new BigDecimal(arr[1]));
				}
			}
		}
		//SHIFTING
		//probW1AndW2 = probW1AndW2.add(BigDecimal.ONE);
		//TOP (w,c) * D
		BigDecimal top = probW1AndW2.multiply(new BigDecimal(D));
		//BOTTOM w*c
		BigDecimal bottom = w.multiply(c);
		//TOTAL
		BigDecimal total = top.divide(bottom, 10, RoundingMode.HALF_UP);
		calculativeValue.setAlternatePmiValue(total);
		try {
			BigDecimal logarithmicValue = log10(calculativeValue.getAlternatePmiValue(), 10);
			if (logarithmicValue.signum() < 0) {				
				calculativeValue.setLogarithmicAlternatePmiValue(BigDecimal.ZERO);
			} else {
				calculativeValue.setLogarithmicAlternatePmiValue(logarithmicValue);
			}
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
	
	
	private List<WordIndex> getWordIndexList (List<String> readFromTxtEntries) {
		//kelimelerin değerleri hesaplanıyor.
		Map<String, Integer> mostOccuredWords = new HashMap<String, Integer>();
		for (String s : readFromTxtEntries) {
			List<String> retList = new ArrayList<String>(); 
			retList = splittedEntryDescription(retList, s);
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
		//Hesaplama bitti sıralama yapılıyor.
		if (mostOccuredWords.size() > 0) {			
			Map<String, Integer> orderedDESC = sortByValue(mostOccuredWords, false);
			//Hesaplanan değerler nesneye atılıyor.
			List<WordIndex> wordIndexList = new ArrayList<WordIndex>();
			int count = 0;
			for(Map.Entry<String,Integer>  entrySet  : orderedDESC.entrySet()){
				WordIndex word = new WordIndex(count, entrySet.getKey(), new BigDecimal(entrySet.getValue()));
				wordIndexList.add(word);
				count++;
			}
			return wordIndexList;
		} else {
			System.err.println("Hesaplanacak MAP bulunamadı");
		}
		return null;
	}
	
	/**
	 * Calculates log10
	 * @param b
	 * @param dp
	 * @return
	 */
	private BigDecimal log10(BigDecimal b, int dp)
	{
	    final int NUM_OF_DIGITS = dp+2; // need to add one to get the right number of dp
	                                    //  and then add one again to get the next number
	                                    //  so I can round it correctly.

	    MathContext mc = new MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN);

	    //special conditions:
	    // log(-x) -> exception
	    // log(1) == 0 exactly;
	    // log of a number lessthan one = -log(1/x)
	    if(b.signum() <= 0)
	        throw new ArithmeticException("log of a negative number! (or zero)");
	    else if(b.compareTo(BigDecimal.ONE) == 0)
	        return BigDecimal.ZERO;
	    else if(b.compareTo(BigDecimal.ONE) < 0)
	        return (log10((BigDecimal.ONE).divide(b,mc),dp)).negate();

	    StringBuffer sb = new StringBuffer();
	    //number of digits on the left of the decimal point
	    int leftDigits = b.precision() - b.scale();

	    //so, the first digits of the log10 are:
	    sb.append(leftDigits - 1).append(".");

	    //this is the algorithm outlined in the webpage
	    int n = 0;
	    while(n < NUM_OF_DIGITS)
	    {
	        b = (b.movePointLeft(leftDigits - 1)).pow(10, mc);
	        leftDigits = b.precision() - b.scale();
	        sb.append(leftDigits - 1);
	        n++;
	    }

	    BigDecimal ans = new BigDecimal(sb.toString());

	    //Round the number to the correct number of decimal places.
	    ans = ans.round(new MathContext(ans.precision() - ans.scale() + dp, RoundingMode.HALF_EVEN));
	    return ans;
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
	
	private Map<Integer, double[]> shiftAllValuesBeforeCosineSimilarityCalculation(double[] arr1, double[] arr2, boolean shiftAllValues)  {
		if (shiftAllValues)  {
			// Sistemdeki tüm deðerler +1 shift edilir
			for (int i = 0; i < arr1.length; i++) {
				arr1[i] = arr1[i] + 1;
				arr2[i] = arr2[i] + 1;
			}
			return setArraysToMap(arr1, arr2);
		} else {
			// Sistemde sadece 0 olan deðerler +1 shift edilir
			for (int i = 0; i < arr1.length; i++) {
				if (arr1[i] == 0) {
					arr1[i] = arr1[i] + 1;
				}
				if (arr2[i] == 0) {
					arr2[i] = arr2[i] + 1; 
				}
			}
			return setArraysToMap(arr1, arr2);
		}
	}


	private Map<Integer, double[]> setArraysToMap(double[] arr1, double[] arr2) {
		Map<Integer, double[]> returnedList = new HashMap<Integer, double[]>();
		returnedList.put(1, arr1);
		returnedList.put(2, arr2);
		return returnedList;
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
				 out.write(word.getWord() +"	"+ word.getIndex()+"	"+word.getFrequency()+"\r\n");
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
//		Set<Integer> userIdList = userList.stream().map(User::getId).collect(Collectors.toSet());
//		findWordsByAuthorFromDatabase(userIdList);
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
			int count = 0 ;
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
				if (count % 200 == 0) {					
					TimeUnit.SECONDS.sleep(1);
				}
				count++;
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
	
	private void createCosineSimilarityTxt(List<CosineSimilarityIndex> cosSimilarityList) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("cosineSimilarityAll.txt"));
			 for(CosineSimilarityIndex  cos  : cosSimilarityList){
				 out.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" + cos.getIndex1Total() + "-" + cos.getIndex2Total() + "-" +cos.getCosineSimilarity() +"\r\n");
			 }
			 out.close();
			 System.out.println("Cosine Similarity TXT oluşturuldu.!");
		}
		catch (IOException e) {
			System.err.println("Cosine Similarity TXT oluşturulurken hata oluştu!");
       }
	}
	
	private void createCosineSimilarityExcel(List<CosineSimilarityIndex> cosSimilarityList) {
		System.out.println("Cosine similarity excel oluşturma işlemi başladı");
		try {
			FileOutputStream fileOut = new FileOutputStream("cosineSimilarity.xlsx");

			XSSFWorkbook workbook = new XSSFWorkbook();
			SXSSFWorkbook wb = new SXSSFWorkbook(workbook);
			SXSSFSheet worksheet = wb.createSheet("POI Worksheet");

			// index from 0,0... cell A1 is cell(0,0)
			SXSSFRow row1 = worksheet.createRow((int) 0);
			// Create Header of Excel
			SXSSFCell cell = row1.createCell((int) 0);
			cell.setCellValue("Index1");
			cell = row1.createCell((int) 1);
			cell.setCellValue("Index2");
			cell = row1.createCell((int) 2);
			cell.setCellValue("Index1Total");
			cell = row1.createCell((int) 3);
			cell.setCellValue("Index2Total");
			cell = row1.createCell((int) 4);
			cell.setCellValue("CosineSimilarity");
			int rowNum = 1;
			// Create Body of Table
			for (CosineSimilarityIndex a : cosSimilarityList) {
					row1 = worksheet.createRow((int) rowNum);
					cell = row1.createCell((int) 0);
					cell.setCellValue(a.getIndex1());
					cell = row1.createCell((int) 1);
					cell.setCellValue(a.getIndex2());
					cell = row1.createCell((int) 2);
					cell.setCellValue(a.getIndex1Total().doubleValue());
					cell = row1.createCell((int) 3);
					cell.setCellValue(a.getIndex2Total().doubleValue());
					cell = row1.createCell((int) 4);
					cell.setCellValue(a.getCosineSimilarity());
					rowNum++;
			}

			wb.write(fileOut);
			fileOut.flush();
			fileOut.close();
			workbook.close();
			wb.close();
			System.out.println("Cosine Similarity excel oluşturma işlemi başarıyla tamamlandı");
		} catch (FileNotFoundException e) {
			System.err.println("Cosine Similarity Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
			
		} catch (IOException e) {
			System.err.println("Cosine Similarity Excel dosyası oluşturulurken bir hata oluştu " + e.getMessage() );
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
	
	private void appendCosineSimilarityWithList(List<CosineSimilarityIndex> cosList) {
		try {
			String filename= "cosineSimilarity.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    for (CosineSimilarityIndex cos : cosList) {
		    	fw.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" + cos.getIndex1Total() + "-" + cos.getIndex2Total() 
			    + "-" +cos.getCosineSimilarity() +"\r\n");//appends the string to the file
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


	private Document readXmlFileFromPath(String path) {
		File file = new File(path);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
		        .newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			System.err.println(e1.getMessage());
		}
		Document document = null;
		try {
			document = documentBuilder.parse(file);
		} catch (SAXException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return document;
				
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
            br = new BufferedReader(new FileReader("D:\\YL_DATA\\users\\" +username + ".txt"));
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
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    
		    for (WordIndex word : wordIndexList) {
		    	if (globalTotalUserEntryCount == count) {
		    		break;
		    	}
		    	fw.write(username + "-" + word.getWord() + "-" + word.getIndex() + "-" + word.getFrequency() +"\r\n");//appends the string to the file
		    	
		    	count ++;
		    }
		    fw.write("----------------------------------------------------------------------------------- username = " + username +"\r\n");
		    
		    fw.close();
			
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
			
			if (splittedIdList.size() % 5 == 0) {
				List<TitleEntry> titleEntryList = entryManager.getEntriesByTitleIdList(splittedIdList);
				
				splittedIdList.clear();

				Map<String, List<TitleEntry>> titleIdEntryList = titleEntryList.stream().collect(Collectors.groupingBy(a-> a.getTitleName()));

				for (Map.Entry<String, List<TitleEntry>> entrySet : titleIdEntryList.entrySet()) {

					String titleName = entrySet.getKey();

					List<String> entryList = entrySet.getValue().stream().map(a -> a.getEntryDescription())
							.collect(Collectors.toList());

					createTxtForGroupingTitle(titleName, entryList);
				}

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

			if (splittedIdList.size() % 10 == 0) {
				List<UserEntry> userEntryList = entryManager.getUserEntryList(splittedIdList);

				Map<String, List<UserEntry>> usernameEntryList = userEntryList.stream()
						.collect(Collectors.groupingBy(a -> a.getUsername()));

				for (Map.Entry<String, List<UserEntry>> entrySet : usernameEntryList.entrySet()) {
					String username = entrySet.getKey();
					
					List<String> entryList = entrySet.getValue().stream().map(a -> a.getEntryDescription())
							.collect(Collectors.toList());

					createTxtForGroupingUser(username, entryList);
				}

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
}
