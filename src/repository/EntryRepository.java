package repository;

import java.util.List;

import model.Entry;
import model.UserUserTitle;

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
	
}
