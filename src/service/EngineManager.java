package service;

import java.util.List;

public interface EngineManager {

	void createCrudeLinks(String dir);
	
	void createTxtForLink(List<String> linkList, String titleOfFile);
	
	void getDocumentWithjSoup(String url, String dir);
	
	void getLinksFromMainPage(String url);
	
	void removeZeroCountTitles();
	
	void findDuplicateTitlesAndMerge();
}
