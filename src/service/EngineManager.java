package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import model.PMIValueIndexes;
import model.WordIndex;

public interface EngineManager {

	
	void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount);
	
	List<String> splittedEntryDescription(List<String>retList,String  entryDescription);
	
	void createCoOccurenceMatrix(String readTextPath);
	
	void createVectors (Map<PMIValueIndexes, BigDecimal> filledMatrix);

	void createTxtForLink(List<String> linkList, String titleOfFile);

	void createOutputForWordsOccured(List<WordIndex> wordIndexList);

	void createTxtFilePMIIndexValues(List<PMIValueIndexes> indexList, boolean isFilled);

}
