package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Entry;
import viewmodel.PMIValueIndexes;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserEntryFrequency;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;
import viewmodel.WordIndex;

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
	
	List<UserEntry> getUserEntryList (Set<Integer> userIdList);

	List<TitleEntry> getEntriesByTitleIdList(List<Integer> splittedIdList);
	
	List<UserUserTitle> getSimilarTitleCountWithIds(int u1Id, List<Integer> u2IdList);
	
	void saveToWrongWordTable(String origin, String correctValue);
	
	Map<String, String> getWrongCorrectWordMap();
	
	void saveWordIndexListToDatabase(List<WordIndex> wordIndexList);
	
	PMIValueIndexes getPMIValueIndexes(int index1, int index2);

	void updateStorageIndex(PMIValueIndexes storageIndex);

	void saveStorageIndex(PMIValueIndexes ind);
	
	void savePMIValueIndexes(Map<PMIValueIndexes, BigDecimal> matrixData);

	int getTotalCountWithProcessIdPMIValueIndex(int process_id);

	List<PMIValueIndexes> getPMIValueIndexListWithIndex1(int index1);

	List<PMIValueIndexes> getPMIValueIndexListWithProcessId(int process_id);
	
	List<PMIValueIndexes> getPMIValueIndexAllValueWithIndex1(int index1);

	void updatePmiValues(Map<PMIValueIndexes, BigDecimal> matrixData);
	
	List<PMIValueIndexes> getPMIValueIndexAllValueWithIndex1(List<Integer> index1List);
}
