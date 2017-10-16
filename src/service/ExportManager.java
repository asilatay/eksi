package service;

import java.util.List;
import java.util.Map;

import model.WordIndex;

public interface ExportManager {

	void writeSpecificEntryCountToDocument(int entryCount);

	void createTxtForLink(List<String> linkList, String titleOfFile);
	
	void writeAllEntriesToDocument();
	
	public void createTxtFileForVocabs(Map<String, Integer> ranked);

	void createOutputForWordsOccured(List<WordIndex> wordIndexList);
}
