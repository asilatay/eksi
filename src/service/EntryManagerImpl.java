package service;

import java.util.List;

import model.Entry;
import repository.EntryRepository;
import repository.EntryRepositoryImpl;

public class EntryManagerImpl implements EntryManager{
	
	EntryRepository entryRepository = new EntryRepositoryImpl();
	
	@Override
	public List<Entry> getAllEntries() {
		return entryRepository.getAllEntries();
	}
	
	@Override
	public boolean addEntry(Entry entry) {
		return entryRepository.addEntry(entry);
	}
	
	@Override
	public int getEntryWithEntryLink(String entryLink)  {
		return entryRepository.getEntryWithEntryLink(entryLink);
	}
}
