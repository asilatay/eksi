package service;

import java.util.List;

import model.Entry;

public interface EntryManager {
	List<Entry> getAllEntries();
	
	boolean addEntry(Entry entry);
	
	int getEntryWithEntryLink(String entryLink);
	
	List<Entry> getEntriesWithTitleId(int titleId);
	
	void updateEntryTitle(int entryId, int newTitleId);
	
	List<Entry> getAllEntriesOrderByDate();
	
	void getSimilarUsersThatWriteTheSameTitle();

	List<Entry> getAllEntriesWithOnlyForeignKeys();
}
