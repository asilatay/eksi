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
import repository.EntryRepository;
import repository.EntryRepositoryImpl;
import viewmodel.UserEntryFrequency;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;

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
		List<UserUserTitle> list = entryRepository.getSimilarUsersForTitles();
		// Bu viewmodelleri yazdýr.
		createTxtFileForUserUserTitle(list);
	}
	
	@Override
	public void getTitleCountOfUsers() {
		List<UserTitle> list = entryRepository.getTitleCountOfUsers();
		//Bu viewmodelleri yazdýr
		createTxtFileForUserTitle(list);
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
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage());
		}
	}
	
	private void createTxtFileForUserTitle(List<UserTitle> resultList) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("userTitle.txt"));
			for (UserTitle ut : resultList) {
				out.write(ut.getUsername() + "	" + ut.getCountOfTitleThatWrote() + "\r\n");
			}
			out.close();
			System.out.println("Kullanýcý kaç tane title a entry girmiþ dokümaný oluþturuldu.");
			
		} catch (Exception e) {
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage());
		}
	}
}
