package service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import viewmodel.PMIValueIndexes;
import viewmodel.WordIndex;

public interface EngineManager {

	
	void coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(int parameterForEntryCount);
	
	List<String> splittedEntryDescription(List<String>retList,String  entryDescription);
	
	void createCoOccurenceMatrix(String readTextPath, List<String> outputFromAnotherFunction);
	
	void createCoOccurenceMatrixWithMemoryAndDisk (String readTextPath, List<String> outputFromAnotherFunction);
	
	void saveWordIndexListToDatabase(String readTextPath, List<String> outputFromAnotherFunction);
	
	void saveCoOccurrenceMatrixToDatabase(String readTextPath, List<String> outputFromAnotherFunction);
	
	void createVectors (Map<PMIValueIndexes, BigDecimal> filledMatrix);

	void createTxtForLink(List<String> linkList, String titleOfFile);

	void createOutputForWordsOccured(List<WordIndex> wordIndexList);

	void createTxtFilePMIIndexValues(List<PMIValueIndexes> indexList, boolean isFilled);
	
	void calculateJaccardSimilarityAndSave();
	
	void runBilkentData(String readXmlPath);
	
	void runBilkentDataWithTxt(String txtPath);
	
	void runEnglishContent(String xmlFilePath);

	void findWordsByAuthorFromDatabase(Set <Integer> userList);
	
	void findWordsByAuthorFromTxtFile(Set <String> usernameList);

	void exportEntriesGroupByTitle();

	void exportEntriesGroupByUser();
	
	void exportWrongVocabs();

	void calculatePMIValuesWithMemoryAndDisk(String readTextPath,
			List<String> outputFromAnotherFunction);
	
	void calculateAlternatePMIValuesWithMemoryAndDisk(String readTextPath,
			List<String> outputFromAnotherFunction);

	void calculateCosineSimilarityMemoryAndDisk();

	void findMostSimilarWords();

	void createBigClamInput();

	void getAllInputIntoTxt();

	void findSumOfRowsForAlternatePMI(String fileReadingPath, List<String> outputFromAnotherFunction);

	void createBigClamInputForCollaborationNetwork(String collaborationNetworkPath);

	void searchNetworkLinks(String linkFilePath);

	void searchNetworkCommunitiesLinks(String linkDataPath);

	void findModularityOverlappingCommunityResults(String modularityOverlappingFilesPath);

	void exportWordAssociationNetworkCommunityByCommunity(String modularityOverlappingFilesPath);

	void exportWordAssociationNetworkModularity(String modularityFilePath);

	void removeUnassignedWordsFromCommunities(String modularityOverlappingFilesPath);

	void calculateSpecificCommunityWords(String globalDataPath);

	void getSpecificCommunityWordCount(String globalDataPath);

}
