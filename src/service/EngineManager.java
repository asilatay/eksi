package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import viewmodel.PMIValueIndexes;
import viewmodel.WordIndex;

public interface EngineManager {

	
	void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount);
	
	List<String> splittedEntryDescription(List<String>retList,String  entryDescription);
	
	void createCoOccurenceMatrix(String readTextPath, List<String> outputFromAnotherFunction);
	
	void createVectors (Map<PMIValueIndexes, BigDecimal> filledMatrix);

	void createTxtForLink(List<String> linkList, String titleOfFile);

	void createOutputForWordsOccured(List<WordIndex> wordIndexList);

	void createTxtFilePMIIndexValues(List<PMIValueIndexes> indexList, boolean isFilled);
	
	void calculateJaccardSimilarityAndSave();
	
	void runBilkentData(String readXmlPath);
	
	void runBilkentDataWithTxt(String txtPath);
	
	void runEnglishContent(String xmlFilePath);

}
