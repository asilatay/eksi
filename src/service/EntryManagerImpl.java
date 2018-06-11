package service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import model.Entry;
import repository.EntryRepository;
import repository.EntryRepositoryImpl;
import viewmodel.PMIValueIndexes;
import viewmodel.TitleEntry;
import viewmodel.UserEntry;
import viewmodel.UserTitle;
import viewmodel.UserUserTitle;
import viewmodel.WordIndex;

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
		List<Integer> idList = new ArrayList<Integer>();
		for (UserUserTitle a : list) {
			idList.add(a.getUser1Id());
			idList.add(a.getUser2Id());
		}
		// Bu viewmodelleri yazdýr.
		createTxtFileForUserUserTitle(list, idList);
	}
	
	@Override
	public void getTitleCountOfUsers() {
		List<UserTitle> list = entryRepository.getTitleCountOfUsers();
		System.out.println("Kullanýcýlarýn kaçar tane title a giriþ yaptýklarý hesaplandý");
		//Bu viewmodelleri yazdýr
		createTxtFileForUserTitle(list);
		
		Map<Integer, String> idUserNameMap = list.stream().collect(Collectors.toMap(a -> a.getUserId(), a -> a.getUsername()));
		
		int totalCount = 0;
		for (UserTitle ut1 : list) {
			List<Integer> user2IdList = new ArrayList<Integer>();
			for (UserTitle ut2 : list) {
				if (ut1.getUserId() != ut2.getUserId()) {
					user2IdList.add(ut2.getUserId());
				}
				totalCount++;
				
				if (totalCount % 100 == 0) {
					List<UserUserTitle> databaseList = getSimilarTitleCountWithIds(ut1.getUserId(), user2IdList);
					
					createTextFileForUserUserTitle(databaseList, idUserNameMap);
					System.out.println("Toplam yapýlan benzerlik hesabý" + totalCount);
					
					user2IdList.clear();
				}
				
			}
			
			if (CollectionUtils.isNotEmpty(user2IdList)) {
				List<UserUserTitle> databaseList = getSimilarTitleCountWithIds(ut1.getUserId(), user2IdList);
				createTextFileForUserUserTitle(databaseList, idUserNameMap);
				user2IdList.clear();
			}
		}
	}
	
	public void createTextFileForUserUserTitle(List<UserUserTitle> resultList, Map<Integer, String> idUserNameMap) {
		try {
			FileWriter fw = new FileWriter("userUserTitles.txt",true);
			for (UserUserTitle frequency : resultList) {
				fw.write(idUserNameMap.get(frequency.getUser1Id()) +"-" + idUserNameMap.get(frequency.getUser2Id()) 
						+ "-" + frequency.getCountOfSimilarTitle() + "\r\n");
			}
			fw.close();
			
		} catch (Exception e) {
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage());
		}
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
	public void createTxtFileForUserUserTitle(List<UserUserTitle> resultList, List<Integer> idList) {
		try {
			Map<Integer, String> idUserNameMap = userManager.getIdUserNameMap(idList);
			BufferedWriter out = new BufferedWriter(new FileWriter("userUserTitles.txt"));
			for (UserUserTitle frequency : resultList) {
				out.write(idUserNameMap.get(frequency.getUser1Id()) +"-" + idUserNameMap.get(frequency.getUser2Id()) 
						+ "-" + frequency.getCountOfSimilarTitle() + "\r\n");
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
				out.write(ut.getUsername() + "-" + ut.getCountOfTitleThatWrote() + "\r\n");
			}
			out.close();
			System.out.println("Kullanýcý kaç tane title a entry girmiþ dokümaný oluþturuldu.");
			
		} catch (Exception e) {
			System.err.println("TXT oluþturulurken hata oluþtu! " + e.getMessage());
		}
	}
	
	@Override
	public List<UserEntry> getUserEntryList (Set<Integer> userIdList) {
		return entryRepository.getUserEntryList(userIdList);
	}
	
	@Override
	public List<TitleEntry> getEntriesByTitleIdList(List<Integer> splittedIdList) {
		return entryRepository.getEntriesByTitleIdList(splittedIdList);
	}
	
	@Override
	public void saveToWrongWordTable(String origin, String correctValue) {
		entryRepository.saveToWrongWordTable(origin, correctValue);
	}
	
	@Override
	public Map<String, String> getWrongCorrectWordMap() {
		return entryRepository.getWrongCorrectWordMap();
	}
	
	@Override
	public List<UserUserTitle> getSimilarTitleCountWithIds(int u1Id, List<Integer> u2IdList) {
		return entryRepository.getSimilarTitleCountWithIds(u1Id, u2IdList);
	}
	
	@Override
	public void saveWordIndexListToDatabase(List<WordIndex> wordIndexList) {
		entryRepository.saveWordIndexListToDatabase(wordIndexList);
	}
	
	@Override
	public PMIValueIndexes getPMIValueIndexes(int index1, int index2) {
		return entryRepository.getPMIValueIndexes(index1, index2);
	}
	
	@Override
	public void updateStorageIndex(PMIValueIndexes storageIndex) {
		entryRepository.updateStorageIndex(storageIndex);
	}
	
	@Override
	public void saveStorageIndex(PMIValueIndexes ind) {
		entryRepository.saveStorageIndex(ind);
	}
	
	@Override
	public int getTotalCountWithProcessIdPMIValueIndex(int process_id) {
		return entryRepository.getTotalCountWithProcessIdPMIValueIndex(process_id);
	}
	
	@Override
	public List<PMIValueIndexes> getPMIValueIndexListWithIndex1(int index1) {
		return entryRepository.getPMIValueIndexListWithIndex1(index1);
	}
	
	@Override
	public List<PMIValueIndexes> getPMIValueIndexListWithProcessId(int process_id) {
		return entryRepository.getPMIValueIndexListWithProcessId(process_id);
	}
	
	@Override
	public void savePMIValueIndexes(Map<PMIValueIndexes, BigDecimal> matrixData) {
		entryRepository.savePMIValueIndexes(matrixData);
	}
	
	@Override
	public List<PMIValueIndexes> getPMIValueIndexAllValueWithIndex1(int index1) {
		return entryRepository.getPMIValueIndexAllValueWithIndex1(index1);
	}
	
	@Override
	public List<PMIValueIndexes> getPMIValueIndexAllValueWithIndex1(List<Integer> index1List) {
		return entryRepository.getPMIValueIndexAllValueWithIndex1(index1List);
	}
	
	@Override
	public void updatePmiValues(Map<PMIValueIndexes, BigDecimal> matrixData) {
		entryRepository.updatePmiValues(matrixData);
	}
	
	@Override
	public Map<Integer, Integer> getRowAndFrequencyInTogetherSumMap(int index1) {
		return entryRepository.getRowAndFrequencyInTogetherSumMap(index1);
	}
	
	@Override
	public void updateAlternatePmiValues(Map<PMIValueIndexes, BigDecimal> matrixData) {
		entryRepository.updateAlternatePmiValues(matrixData);
	}
	
	@Override
	public Map<Integer, List<PMIValueIndexes>> getDataOrdered() {
		return entryRepository.getDataOrdered();
	}
}
