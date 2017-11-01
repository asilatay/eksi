package service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import model.Entry;
import model.UserEntryFrequency;
import model.UserUserTitle;
import repository.EntryRepository;
import repository.EntryRepositoryImpl;

public class EntryManagerImpl implements EntryManager{
	
	EntryRepository entryRepository = new EntryRepositoryImpl();
	
	UserManager userManager = new UserManagerImpl();
	
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
//		List<Entry> allEntries = getAllEntriesWithOnlyForeignKeys();
//		Map<Integer, List<Integer>> titleUserIdListMap = new HashMap<Integer, List<Integer>>();
//		for (Entry entry : allEntries) {
//			if (!titleUserIdListMap.containsKey(entry.getTitleId())) {
//				List<Integer> newList = new ArrayList<Integer>();
//				newList.add(entry.getUserId());
//				titleUserIdListMap.put(entry.getTitleId(), newList);
//			} else {
//				List<Integer> list = new ArrayList<Integer>();
//				list = titleUserIdListMap.get(entry.getTitleId());
//				list.add(entry.getUserId());
//				titleUserIdListMap.put(entry.getTitleId(), list);
//			}
//		}
//		allEntries.clear();
//		List<UserEntryFrequency> list = new ArrayList<UserEntryFrequency>();
//		for (Map.Entry<Integer, List<Integer>> data : titleUserIdListMap.entrySet()) {
//			for (Integer firstUserId : data.getValue()) {
//				UserEntryFrequency freq = new UserEntryFrequency();
//				freq.setUserId(firstUserId);
//				ArrayList<Integer> titleList = new ArrayList<Integer>();
//				titleList.add(data.getKey());
//				for(Map.Entry<Integer, List<Integer>> data2 : titleUserIdListMap.entrySet()) {
//					if (data.getKey() != data2.getKey()) {
//						for (Integer secondUserId : data2.getValue()) {
//							if (firstUserId == secondUserId) {
//								titleList.add(data2.getKey());
//							}
//						}
//					}
//				}
//				freq.setTitleIdList(titleList);
//				list.add(freq);
//			}
//		}
//		titleUserIdListMap.clear();
//		List<UserUserTitle> resultList = new ArrayList<UserUserTitle>();
//		for (UserEntryFrequency freq1 : list) {
//			for(UserEntryFrequency freq2 : list) {
//				if (freq1.getUserId() != freq2.getUserId()) {
//					UserUserTitle newRecord = new UserUserTitle();
//					newRecord.setUser1Id(freq1.getUserId());
//					newRecord.setUser2Id(freq2.getUserId());
//					newRecord.setSimilarTitleIdList(intersection(freq1.getTitleIdList(), freq2.getTitleIdList()));
//					resultList.add(newRecord);
//				}
//			}
//		}
		
		//Comparator yazarak en çok benzeyen X kadar modeli çek.
//		Collections.sort(resultList, new ComparatorClassForTitleSize());
		List<UserUserTitle> list = entryRepository.getSimilarUsersForTitles();
		// Bu modelleri yazdýr.
		createTxtFileForUserUserTitle(list);
	}
	
	
	 private <T> ArrayList<Integer> intersection(List<Integer> list1, List<Integer> list2) {
	        ArrayList<Integer> list = new ArrayList<Integer>();

	        for (Integer t : list1) {
	            if(list2.contains(t)) {
	                list.add(t);
	            }
	        }

	        return list;
	    }

	private <T> ArrayList<Integer> union(List<Integer> list1, List<Integer> list2) {
		Set<Integer> set = new HashSet<Integer>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<Integer>(set);
	}

	@Override
	public void writeSpecificEntryCountToDocument(int entryCount) {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("eksi" + entryCount + ".txt"));
				if (entryCount < activeEntryList.size()) {
					for (int i = 0; i < entryCount; i++) {
						out.write(activeEntryList.get(i).getDescription());
					}
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} catch (IOException e) {
				System.err.println("TXT oluþturulurken hata oluþtu!");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void writeAllEntriesToDocument() {
		System.out.println("Entry dýþa aktarýmý baþlatýldý!");
		List<Entry> activeEntryList = getAllEntriesOrderByDate();
		if (activeEntryList != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("eksi.txt"));
				for (Entry e : activeEntryList) {
					out.write(e.getDescription());
				}
				out.close();
				System.out.println("TXT oluþturuldu.!");
			} catch (IOException e) {
				System.err.println("TXT oluþturulurken hata oluþtu!");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void createTxtFileForVocabs(Map<String, Integer> ranked) {
		try{
			 BufferedWriter out = new BufferedWriter(new FileWriter("vocab.txt"));
			 for(Map.Entry<String,Integer>  entrySet  : ranked.entrySet()){
				 out.write(entrySet.getKey() +"	"+ entrySet.getValue()+"\r\n");
			 }
			 out.close();
			 System.out.println("TXT oluþturuldu.!");
		}
		catch (IOException e) {
			System.err.println("TXT oluþturulurken hata oluþtu!");
       }
		
	}
	
	
	@Override
	public void createTxtFileForUserUserTitle(List<UserUserTitle> resultList) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("userUserTitles.txt"));
			for (UserUserTitle frequency : resultList) {
				out.write(userManager.getUserById(frequency.getUser1Id()).getNickname() +"	" + userManager.getUserById(frequency.getUser2Id()).getNickname() 
						+ "	" + frequency.getCountOfSimilarTitle() + "\r\n");
			}
			out.close();
			System.out.println("Kullanýcý - Title Frequency TXT dokümaný oluþturuldu.");
			
		} catch (Exception e) {
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage() );
		}
	}
}
