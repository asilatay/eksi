package service;

public interface CrawlerManager {
	
	void createCrudeLinks(String dir);
	
	void getDocumentWithjSoup(String url, String dir);
	
	void getLinksFromMainPage(String url);
	
	void removeZeroCountTitles();
	
	void findDuplicateTitlesAndMerge();
	
}
