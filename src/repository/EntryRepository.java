package repository;

import java.util.List;

import model.Entry;

public interface EntryRepository {
	
	List<Entry> getAllEntries();

	boolean addEntry(Entry entry);
	
	int getEntryWithEntryLink(String entryUrl);
	
}
