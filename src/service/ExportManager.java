package service;

import java.util.List;
import java.util.Map;

import model.PMIValueIndexes;
import model.UserEntryFrequency;
import model.WordIndex;

public interface ExportManager {

	void writeSpecificEntryCountToDocument(int entryCount);

	void createTxtForLink(List<String> linkList, String titleOfFile);
	
	void writeAllEntriesToDocument();
	
	void createTxtFileForVocabs(Map<String, Integer> ranked);

	void createOutputForWordsOccured(List<WordIndex> wordIndexList);
	
	void createTxtFilePMIIndexValues(List<PMIValueIndexes> indexList, boolean isFilled); 
	
	void createTxtFileForUserEntryFrequency (List<UserEntryFrequency> frequencyList);
}
