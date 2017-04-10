package service;

import java.util.List;

import model.Entry;

public interface EntryManager {
	List<Entry> getAllEntries();
	
	boolean addEntry(Entry entry);
	
	int getEntryWithEntryLink(String entryLink);
}
