package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Entry;
import model.UserEntryFrequency;
import repository.EntryRepository;
import repository.EntryRepositoryImpl;

public class EntryManagerImpl implements EntryManager{
	
	EntryRepository entryRepository = new EntryRepositoryImpl();
	
	ExportManager exportManager = new ExportManagerImpl();
	
	@Override
	public List<Entry> getAllEntries() {
		return entryRepository.getAllEntries();
	}
	
	@Override
	public List<Entry> getAllEntriesWithOnlyForeignKeys() {
		return entryRepository.getAllEntriesWithOnlyForeignKeys();
	}
	
	@Override
	public boolean addEntry(Entry entry) {
		return entryRepository.addEntry(entry);
	}
	
	@Override
	public int getEntryWithEntryLink(String entryLink)  {
		return entryRepository.getEntryWithEntryLink(entryLink);
	}
	
	@Override
	public List<Entry> getEntriesWithTitleId(int titleId) {
		return entryRepository.getEntriesWithTitleId(titleId);
	}
	
	@Override
	public void updateEntryTitle(int entryId, int newTitleId) {
		entryRepository.updateEntryTitle(entryId, newTitleId);
	}
	
	@Override
	public List<Entry> getAllEntriesOrderByDate() {
		return entryRepository.getAllEntriesOrderByDate();
	}
	
	@Override
	public void getSimilarUsersThatWriteTheSameTitle() {
		List<Entry> allEntries = getAllEntriesWithOnlyForeignKeys();
		Map<Integer, List<Entry>> entryListOfTitles = new HashMap<Integer, List<Entry>>();
		for (Entry entry : allEntries) {
			if (! entryListOfTitles.containsKey(entry.getTitleId())) {
				List<Entry> newList = new ArrayList<Entry>();
				newList.add(entry);
				entryListOfTitles.put(entry.getTitleId(), newList);
			} else {
				List<Entry> list = new ArrayList<Entry>();
				list = entryListOfTitles.get(entry.getTitleId());
				list.add(entry);
				entryListOfTitles.put(entry.getTitleId(), list);
			}
		}
		List<UserEntryFrequency> result = new ArrayList<UserEntryFrequency>();
		for (Map.Entry<Integer, List<Entry>> data : entryListOfTitles.entrySet()) {
			List<Entry> entryListOfTitle = data.getValue();
			for (int i = 0 ; i < entryListOfTitle.size(); i++) {
				for (int j = 0 ; j < entryListOfTitle.size(); j++) {
					if (entryListOfTitle.get(i).getId() != entryListOfTitle.get(j).getId()) {
						boolean foundForTitle = false;
						for (UserEntryFrequency freq : result) {
							if (freq.getUser1Id() == entryListOfTitle.get(i).getUserId()
									&& freq.getUser2Id() == entryListOfTitle.get(j).getUserId()) {
								for (Integer titleId : freq.getTitleIdList()) {
									if (titleId == data.getKey()) {
										foundForTitle = true;
										break;
									}
								}
								if (foundForTitle) {
									break;
								}
							}
						}
						if (! foundForTitle) {
							boolean foundForUserEntryFrequency = false;
							for (UserEntryFrequency freq : result) {
								if (freq.getUser1Id() == entryListOfTitle.get(i).getUserId()
										&& freq.getUser2Id() == entryListOfTitle.get(j).getUserId()) {
									freq.getTitleIdList().add(data.getKey()); 
									foundForUserEntryFrequency = true;
									break;
								}
							}
							if (! foundForUserEntryFrequency) {								
								UserEntryFrequency userEntry = new UserEntryFrequency();
								userEntry.setUser1Id(entryListOfTitle.get(i).getUserId());
								userEntry.setUser2Id(entryListOfTitle.get(j).getUserId());
								ArrayList<Integer> titleIdList = new ArrayList<Integer>();
								titleIdList.add(data.getKey());
								userEntry.setTitleIdList(titleIdList);
								result.add(userEntry);
							}
						}
					}
				}
			}
		}
		//Comparator yazarak en çok benzeyen X kadar modeli çek.
//		Collections.sort(result, new ComparatorClassForTitleSize());
		List<UserEntryFrequency> writeableList = new ArrayList<UserEntryFrequency>();
		for (int i = 0; i < 100; i++) {
			writeableList.add(result.get(i));
		}
		// Bu modelleri yazdýr.
		exportManager.createTxtFileForUserEntryFrequency(writeableList);
	}
}
