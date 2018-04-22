package repository;

import java.util.List;
import java.util.Set;

import model.Entry;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;

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
	
}
