package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import commons.DateUtil;
import model.Entry;
import model.KeyIndexOld;
import model.User;
import viewmodel.CosineSimilarityIndex;
import viewmodel.PMIValueIndexes;
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
	
	private final static String directoryOfTitleCountOfUsers = "userTitle.txt";
	
	public EngineManagerImpl() {
	}
	
	
	/**
	 * @param parameterForEntryCount = 0 ise tüm entryler üzerinden iþlem yapýlýr
	 * CoOccurence matrix için gerekli hesaplamalarý yaparak çýktý üreten metoddur.
	 * Bu metod eski modellerle üretilmiþ bir metoddur ve geçmiþ görülsün diye tutuluyor
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
			entryManager.createTxtFileForVocabs(rankingOrdered);
			//TODO GarbageCollector Error
//				createExcelRankingWords(rankingOrdered);
			System.out.println("Ranking oluþturuldu!");
			List<String> retList = new ArrayList<String> ();
			for (int i=0; i < parameterForEntryCount; i++) {				
				retList = splittedEntryDescription(retList, activeEntryList.get(i).getDescription());
			}
			//Benim çözümüm(KeyIndex)
			Map<KeyIndexOld, Integer> matrixData = new  HashMap<KeyIndexOld, Integer>();
			System.out.println("KeyIndex çözümü baþladý!");
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
	private static void createTxtForBigCLAMFromMap(Map<KeyIndexOld, Integer> mapList, boolean forBigClam){
		try{
			if (forBigClam) {				
				BufferedWriter out = new BufferedWriter(new FileWriter("forBigClam.txt"));
				for(Map.Entry<KeyIndexOld,Integer>  entrySet  : mapList.entrySet()){
					KeyIndexOld ind = entrySet.getKey();
					out.write(ind.getRow()+"	"+ ind.getColumn()+"\r\n");
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} else {
				BufferedWriter out = new BufferedWriter(new FileWriter("neigbors.txt"));
				for(Map.Entry<KeyIndexOld,Integer>  entrySet  : mapList.entrySet()){
					KeyIndexOld ind = entrySet.getKey();
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
	
	@Override
	public void createCoOccurenceMatrix(String readTextPath) {
		System.out.println("Co-Occurence matrix operation is just started!");
		List<String> readFromTxtEntries = importManager.readFromTxt(readTextPath);
		if (readFromTxtEntries == null || readFromTxtEntries.size() <= 0) {
			System.err.println("Okunmaya çalýþýlan dosya boþ veya okuma iþlemi sýrasýnda hata alýndý.");
			System.err.println("Program kapatýlýyor.");
			System.exit(0);
		}
		List<WordIndex> wordsOccured = getWordIndexList(readFromTxtEntries);
		createOutputForWordsOccured(wordsOccured);
		
		Map<String, Integer> ranking = new HashMap<String, Integer>();
		Map<Integer, BigDecimal> wordFrequencyMap = new HashMap<Integer, BigDecimal>();
		for (WordIndex w : wordsOccured) {
			ranking.put(w.getWord(), w.getIndex());
			wordFrequencyMap.put(w.getIndex(), w.getFrequency());
		}
		
		List<String> splittedEntries = new ArrayList<String> ();
		for (String s : readFromTxtEntries) {				
			splittedEntries = splittedEntryDescription(splittedEntries, s);
		}
		Map<PMIValueIndexes, BigDecimal> matrixData = new  HashMap<PMIValueIndexes, BigDecimal>();
		System.out.println("PMI Value Indexes çözümü baþladý!");
		for (int i = 0; i < splittedEntries.size(); i++) {
			int countGo = i + 1;
			int numberOfCellForPivotWord = ranking.get(splittedEntries.get(i));
			for (int j = 0; j < turningNumberForNewCoOccurence; j++) {
				if (countGo < splittedEntries.size()) {
					int numberOfCellForAlternativeWord = ranking.get(splittedEntries.get(countGo));
					PMIValueIndexes ind = new PMIValueIndexes(numberOfCellForPivotWord, numberOfCellForAlternativeWord, BigDecimal.ZERO, BigDecimal.ZERO);
					PMIValueIndexes symIndex = new PMIValueIndexes(numberOfCellForAlternativeWord, numberOfCellForPivotWord, BigDecimal.ZERO, BigDecimal.ZERO);
					if (matrixData.containsKey(ind)) {
						matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
						if (matrixData.get(symIndex) != null) {							
							matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
						} else {
							matrixData.put(symIndex, BigDecimal.ONE);
						}
					} else if (matrixData.containsKey(symIndex)) {
						matrixData.put(symIndex, matrixData.get(symIndex).add(BigDecimal.ONE));
						if (matrixData.get(ind) != null) {							
							matrixData.put(ind, matrixData.get(ind).add(BigDecimal.ONE));
						} else {
							matrixData.put(ind, BigDecimal.ONE);
						}
					} else {
						matrixData.put(symIndex, BigDecimal.ONE);
						matrixData.put(ind, BigDecimal.ONE);
					}
					countGo++;
				}
			}
		}
		//Co occurence matrix oluþturma tamamlandý, PMI Deðerini hesaplayacaðýz.
		matrixData = calculateAndSetPMIValues(matrixData, wordFrequencyMap, ranking.size());
		
		Map<Integer, List<String>> mapOfIndexes = getMapOfIndexes(matrixData);
		
		// Alternate PMI deðerini hesaplayacaðýz.
		matrixData = calculateAndSetAlternatePMIValues (matrixData, mapOfIndexes);
		//Matrix de 2 farklý row un benzerliði hesaplanmak istendiðinde uzunluklarý eþit olmalý
		// Bu nedenle mesela index1 = 20 ve index2 = 3 için matrix data da bir kayýt yoksa,
		// bu kayýt yaratýlýp deðeri 0 yazýlmalýdýr. PMI Value deðerini de 0 ata.
		// Bu noktada bu iþ yapýlmalý
		Map<PMIValueIndexes, BigDecimal> clonedMatrixData = matrixData;
		Map<PMIValueIndexes, BigDecimal> filledMatrix = fillMissingMatrixCell(matrixData, mapOfIndexes, ranking.size());
		
		createTxtFilePMIIndexValues(convertToListPMIValueIndexes(filledMatrix), true);
		createTxtFilePMIIndexValues(convertToListPMIValueIndexes(clonedMatrixData), false);
		
		createVectors(filledMatrix);
		
	}
	
	private List<PMIValueIndexes> convertToListPMIValueIndexes (Map<PMIValueIndexes, BigDecimal> filledMatrix) {
		List<PMIValueIndexes> indexList = new ArrayList<PMIValueIndexes>();
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : filledMatrix.entrySet()) {
			indexList.add(data.getKey());
		}
		return indexList;
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
		BigDecimal totalWSize = new BigDecimal(totalWordSize);
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			BigDecimal probW1AndW2 = data.getValue().divide(totalWSize, 10, RoundingMode.HALF_UP);
			
			PMIValueIndexes valueObject = data.getKey();
			BigDecimal frequencyIndex1 = wordFrequencyMap.get(valueObject.getIndex1()).divide(totalWSize, 10, RoundingMode.HALF_UP);
			BigDecimal frequencyIndex2 = wordFrequencyMap.get(valueObject.getIndex2()).divide(totalWSize, 10, RoundingMode.HALF_UP);
			
			BigDecimal pmiValue = probW1AndW2.divide(frequencyIndex1.multiply(frequencyIndex2),10 ,RoundingMode.HALF_UP)
					.setScale(10, RoundingMode.HALF_UP);
			// pmiValue deðerinin logaritmasýný alýp tekrar üstüne set et. (Logaritma 0 çýkacak senaryoya dikkat et)
			valueObject.setPmiValue(pmiValue);
			try {				
				valueObject.setLogaritmicPmiValue(log10(valueObject.getPmiValue(), 10));
			} catch (ArithmeticException e) {
				// logaritma 0 geldiðinde exception fýrlatýlýp yakalandý ve bir deðer set edildi. Deðeri deðiþtirebiliriz.
				valueObject.setLogaritmicPmiValue(new BigDecimal("999999"));
			}
			
		}
		return matrixData;
	}
	
	/*
	 *  PMI (w, c) = log (((w,c)* D )/ w*c) formülünü açýklayacak olursak ;
		index w = 2
		index c = 3
		(w,c) deðeri (2,3) cell inde yazan deðer
		D deðeri birbirleriyle iliþki olan ikililerin toplam sayýsý (Þu andaki matrixData nýn size ý)
		w deðeri 2.satýrdaki (2,3) dýþýndaki tüm deðerlerin toplamý
		c deðeri 3.satýrdaki (3,2) dýþýndaki tüm deðerlerin toplamýdýr. 

	 */	
	private Map<PMIValueIndexes, BigDecimal> calculateAndSetAlternatePMIValues (Map <PMIValueIndexes, BigDecimal> matrixData
			, Map<Integer, List<String>> mapOfIndexes) {
		int D = matrixData.size();
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			PMIValueIndexes object = data.getKey();
			BigDecimal probW1AndW2 = data.getValue(); // (w,c)
			int index1W = object.getIndex1();
			int index2C = object.getIndex2();
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
			//TOP (w,c) * D
			BigDecimal top = probW1AndW2.multiply(new BigDecimal(D));
			//BOTTOM w*c
			BigDecimal bottom = w.multiply(c);
			//TOTAL
			BigDecimal total = top.divide(bottom, 10, RoundingMode.HALF_UP);
			object.setAlternatePmiValue(total);
			try {
				object.setLogarithmicAlternatePmiValue(log10(object.getAlternatePmiValue(), 10));
			} catch (ArithmeticException ex) {
				object.setLogarithmicAlternatePmiValue(new BigDecimal("999999"));
			}
		}
		return matrixData;
	}
	
	private Map<PMIValueIndexes, BigDecimal> fillMissingMatrixCell (Map <PMIValueIndexes, BigDecimal> matrixData 
			,Map<Integer, List<String>> mapOfIndexes, int totalSize) {
		Map<Integer, Integer> indexSizeMap = new HashMap<Integer, Integer>();
		Map<PMIValueIndexes, BigDecimal> filledNewMatrixData  = new HashMap<>();
		for (Map.Entry<PMIValueIndexes, BigDecimal> data : matrixData.entrySet()) {
			PMIValueIndexes value = data.getKey();
			int index1 = value.getIndex1();
			if (! indexSizeMap.containsKey(index1)) {
				List<String> valueableIndexes  = mapOfIndexes.get(index1);
				if (!valueableIndexes.isEmpty()) {				
					List<Integer> indexes = new ArrayList<Integer>();
					for (String s : valueableIndexes) {
						String [] arr = s.split("-");
						if (arr.length == 2) {
							indexes.add(Integer.parseInt(arr[0]));
						}
					}
					for (int i = 0; i < totalSize; i++) {
						boolean found = false;
						for (Integer ind : indexes) {
							if (ind == i) {
								found = true;
								break;
							}
						}
						if (!found) {
							PMIValueIndexes newIndex = new PMIValueIndexes();
							newIndex.setIndex1(index1);
							newIndex.setIndex2(i);
							newIndex.setPmiValue(BigDecimal.ZERO);
							newIndex.setLogaritmicPmiValue(new BigDecimal("999999"));
							newIndex.setAlternatePmiValue(BigDecimal.ZERO);
							newIndex.setLogarithmicAlternatePmiValue(new BigDecimal("999999"));
							filledNewMatrixData.put(newIndex, BigDecimal.ZERO);
							// matrixData.put(newIndex, BigDecimal.ZERO);
						} else {
							for (PMIValueIndexes x : matrixData.keySet()) {
								if (x.getIndex1() == index1 && x.getIndex2() == i) {
									filledNewMatrixData.put(x, matrixData.get(x));
									break;
								}
							}
						}
						if (indexSizeMap.containsKey(index1)) {
							indexSizeMap.put(index1, indexSizeMap.get(index1) + 1);
						} else {
							indexSizeMap.put(index1, 1);
						}
					}
				}
			}
		}
		return filledNewMatrixData;
	}
	
	private List<WordIndex> getWordIndexList (List<String> readFromTxtEntries) {
		//kelimelerin deðerleri hesaplanýyor.
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
		//Hesaplama bitti sýralama yapýlýyor.
		if (mostOccuredWords.size() > 0) {			
			Map<String, Integer> orderedDESC = sortByValue(mostOccuredWords, false);
			//Hesaplanan deðerler nesneye atýlýyor.
			List<WordIndex> wordIndexList = new ArrayList<WordIndex>();
			int count = 0;
			for(Map.Entry<String,Integer>  entrySet  : orderedDESC.entrySet()){
				WordIndex word = new WordIndex(count, entrySet.getKey(), new BigDecimal(entrySet.getValue()));
				wordIndexList.add(word);
				count++;
			}
			return wordIndexList;
		} else {
			System.err.println("Hesaplanacak MAP bulunamadý");
		}
		return null;
	}
	
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
		List<CosineSimilarityIndex> indexList = new ArrayList<CosineSimilarityIndex>();
		for (int i = 0; i < biggestIndex.getIndex1(); i++) {
			CosineSimilarityIndex cos = new CosineSimilarityIndex();
			cos.setIndex1(i);
			for (int j = 0; j < biggestIndex.getIndex1(); j++) {				
				cos.setIndex2(j);
				if (i != j) {
					int array1Size = 0;
					int array2Size = 0;
					double[] array1 = new double[biggestIndex.getIndex1() + 1];
					double[] array2 = new double[biggestIndex.getIndex1() + 1];
					cos.setIndex1Array(array1);
					cos.setIndex2Array(array2);
					for (PMIValueIndexes a : filledMatrix.keySet()) {
						if (a.getIndex1() == cos.getIndex1()) {
							BigDecimal totIndex1 = cos.getIndex1Total();
							if (a.getLogaritmicPmiValue().signum() < 0) {
								a.setLogaritmicPmiValue(BigDecimal.ZERO);
							}
//							if (a.getLogaritmicPmiValue().compareTo(new BigDecimal("999999")) != 0) {								
								totIndex1 = totIndex1.add(a.getLogaritmicPmiValue());
//							}
							cos.setIndex1Total(totIndex1);
							
							double[] arr1 = cos.getIndex1Array();
							arr1[array1Size] = a.getLogaritmicPmiValue().doubleValue();
							cos.setIndex1Array(arr1);
							array1Size++;
							
						} else if (a.getIndex1() == cos.getIndex2()) {
							BigDecimal totIndex2 = cos.getIndex2Total();
							if (a.getLogaritmicPmiValue().signum() < 0) {
								a.setLogaritmicPmiValue(BigDecimal.ZERO);
							}
//							if (a.getLogaritmicPmiValue().compareTo(new BigDecimal("999999")) != 0) {								
								totIndex2 = totIndex2.add(a.getLogaritmicPmiValue());
//							}
							cos.setIndex2Total(totIndex2);
							
							double[] arr2 = cos.getIndex2Array();
							arr2[array2Size] = a.getLogaritmicPmiValue().doubleValue();
							cos.setIndex2Array(arr2);
							array2Size++;
						}
					}
					double cosSimilarity = cosineSimilarity(cos.getIndex1Array(), cos.getIndex2Array());
					cos.setCosineSimilarity(cosSimilarity);
					indexList.add(cos);
				}
			}
		}
		
		// TXT
		createCosineSimilarityTxt(indexList);
		// EXCEL
		createCosineSimilarityExcel(indexList);
	}
	
	
	//Method
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
			System.err.println("Linklerin txt dosyasýna yazýmý sýrasýnda beklenmeyen bir hata oluþtu!");
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
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage() );
		}
	}
	
	@Override
	public void calculateJaccardSimilarityAndSave() {
		List<UserUserTitle> userUserTitleList = getUserUserTitleListFromFile();
		List<UserTitle> userTitleList = getUserTitleListFromFile();
		if (userUserTitleList == null || userUserTitleList.isEmpty()) {
			System.err.println("UserUserTitle dosyasýna veri ekleyin" );
			return;
		}
		if (userTitleList == null || userTitleList.isEmpty()) {
			System.err.println("UserTitle dosyasýna veri ekleyin" );
			return;
		}
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
		}
		//Çýktý üret
		//1 - TXT
		createJaccardSimilarityTxt(userUserTitleList);
		//2 - Excel
		createJaccardSimilarityExcel(userUserTitleList);
		
	}
	
	private List<UserUserTitle> getUserUserTitleListFromFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(directoryOfSimilarUsersThatWroteSameTitle));
			String line;
			List<UserUserTitle> userUserTitleList = new ArrayList<UserUserTitle>();
			while((line = in.readLine()) != null) {
				UserUserTitle uut = new UserUserTitle();
				String[] splitWithLine = line.split("-");
				uut.setUser1(userManager.getUserByUsername(splitWithLine[0]));
				uut.setUser2(userManager.getUserByUsername(splitWithLine[1]));
				uut.setCountOfSimilarTitle(Integer.parseInt(splitWithLine[2]));
				userUserTitleList.add(uut);
 			}
			in.close();
			return userUserTitleList;
			
		} catch (Exception e) {
			System.err.println("UserUserTitle dosyasý okunurken bir hata oluþtu " + e.getMessage() );
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
			System.err.println("UserTitle dosyasý okunurken bir hata oluþtu " + e.getMessage() );
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
			System.out.println("Jaccard benzerliði TXT dosyasý oluþturuldu!");
			
		} catch (Exception e) {
			System.err.println("Jaccard benzerliði TXT dosyasý oluþturulurken bir hata oluþtu " + e.getMessage() );
		}
	}
	
	private void createJaccardSimilarityExcel(List<UserUserTitle> userUserTitleList) {
		System.out.println("Jaccard benzerliði excel oluþturma iþlemi baþladý");
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
			System.out.println("Jaccard benzerliði excel oluþturma iþlemi baþarýyla tamamlandý");
		} catch (FileNotFoundException e) {
			System.err.println("Jaccard benzerliði Excel dosyasý oluþturulurken bir hata oluþtu " + e.getMessage() );
			
		} catch (IOException e) {
			System.err.println("Jaccard benzerliði Excel dosyasý oluþturulurken bir hata oluþtu " + e.getMessage() );
		}
	}
	
	private void createCosineSimilarityTxt(List<CosineSimilarityIndex> cosSimilarityList) {
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("cosineSimilarity.txt"));
			 for(CosineSimilarityIndex  cos  : cosSimilarityList){
				 out.write(cos.getIndex1() + "-" + cos.getIndex2() + "-" + cos.getIndex1Total() + "-" + cos.getIndex2Total() + "-" +cos.getCosineSimilarity() +"\r\n");
			 }
			 out.close();
			 System.out.println("Cosine Similarity TXT oluþturuldu.!");
		}
		catch (IOException e) {
			System.err.println("Cosine Similarity TXT oluþturulurken hata oluþtu!");
       }
	}
	
	private void createCosineSimilarityExcel(List<CosineSimilarityIndex> cosSimilarityList) {
		System.out.println("Cosine similarity excel oluþturma iþlemi baþladý");
		try {
			FileOutputStream fileOut = new FileOutputStream("cosineSimilarity.xlsx");

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet worksheet = workbook.createSheet("POI Worksheet");

			// index from 0,0... cell A1 is cell(0,0)
			XSSFRow row1 = worksheet.createRow((int) 0);
			// Create Header of Excel
			XSSFCell cell = row1.createCell((int) 0);
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

			workbook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			workbook.close();
			System.out.println("Cosine Similarity excel oluþturma iþlemi baþarýyla tamamlandý");
		} catch (FileNotFoundException e) {
			System.err.println("Cosine Similarity Excel dosyasý oluþturulurken bir hata oluþtu " + e.getMessage() );
			
		} catch (IOException e) {
			System.err.println("Cosine Similarity Excel dosyasý oluþturulurken bir hata oluþtu " + e.getMessage() );
		}
	}
	
}
