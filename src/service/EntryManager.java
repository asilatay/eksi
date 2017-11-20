package service;

import java.util.List;
import java.util.Map;

import model.Entry;
import viewmodel.UserEntryFrequency;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;

public interface EntryManager {
	List<Entry> getAllEntries();
	
	boolean addEntry(Entry entry);
	
	int getEntryWithEntryLink(String entryLink);
	
	List<Entry> getEntriesWithTitleId(int titleId);
	
	void updateEntryTitle(int entryId, int newTitleId);
	
	List<Entry> getAllEntriesOrderByDate();
	
	void getSimilarUsersThatWriteTheSameTitle();

	List<Entry> getAllEntriesWithOnlyForeignKeys();

	void writeSpecificEntryCountToDocument(int entryCount);

	void writeAllEntriesToDocument();

	void createTxtFileForVocabs(Map<String, Integer> ranked);

	void createTxtFileForUserUserTitle(List<UserUserTitle> resultList, List<Integer> idList);
	
	void getTitleCountOfUsers();
}
