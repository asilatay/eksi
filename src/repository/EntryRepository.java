package repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Entry;
import viewmodel.PMIValueIndexes;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;
import viewmodel.WordIndex;

public interface EntryRepository {
	
	List<Entry> getAllEntries();

	boolean addEntry(Entry entry);
	
	int getEntryWithEntryLink(String entryUrl);
	
	List<Entry> getEntriesWithTitleId(int titleId);
	
	void updateEntryTitle(int entryId, int newTitleId);
	
	List<Entry> getAllEntriesOrderByDate();
	
	List<Entry> getAllEntriesOrderByDateWithLimit(int limit);

	List<Entry> getAllEntriesWithOnlyForeignKeys();
	
	List<UserUserTitle> getSimilarUsersForTitles();
	
	List<UserTitle> getTitleCountOfUsers();

	List<UserEntry> getUserEntryList(Set<Integer> userIdList);

	List<TitleEntry> getEntriesByTitleIdList(List<Integer> splittedIdList);
	
	void saveToWrongWordTable(String origin, String correctValue);

	Map<String, String> getWrongCorrectWordMap();

	List<UserUserTitle> getSimilarTitleCountWithIds(int u1Id, List<Integer> u2IdList);

	void saveWordIndexListToDatabase(List<WordIndex> wordIndexList);

	PMIValueIndexes getPMIValueIndexes(int index1, int index2);

	void updateStorageIndex(PMIValueIndexes storageIndex);

	void saveStorageIndex(PMIValueIndexes ind);

	int getTotalCountWithProcessIdPMIValueIndex(int process_id);

	List<PMIValueIndexes> getPMIValueIndexListWithIndex1(int index1);

	List<PMIValueIndexes> getPMIValueIndexListWithProcessId(int process_id);

	void savePMIValueIndexes(Map<PMIValueIndexes, BigDecimal> matrixData);
	
}
