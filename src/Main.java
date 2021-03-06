
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import model.Title;
import model.User;
import repository.IPathConstants;
import service.CrawlerManager;
import service.CrawlerManagerImpl;
import service.EngineManager;
import service.EngineManagerImpl;
import service.EntryManager;
import service.EntryManagerImpl;
import service.TitleManager;
import service.TitleManagerImpl;
import service.UserManager;
import service.UserManagerImpl;

public class Main {
//	static final String url ="https://eksisozluk.com/sitemap.xml";
	static final String url ="https://eksisozluk.com/";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EngineManager engineManager = new EngineManagerImpl();
		
		TitleManager titleManager = new TitleManagerImpl();
		
		UserManager userManager = new UserManagerImpl();
		
		CrawlerManager crawlerManager = new CrawlerManagerImpl();
		
		EntryManager entryManager = new EntryManagerImpl();
		
		IPathConstants pathConstants = new IPathConstants();
		
		String operationSelect ="-1";
		Scanner scanIn = new Scanner(System.in);		
		do {
			System.out.println("��k                               -> Press 0");
			System.out.println("Linkleri ��kar    	  			  -> Press 1");
			System.out.println("Yeni Ba�l�k Ekle                  -> Press 2");
			System.out.println("T�m Ba�l�klar� Listele            -> Press 3");
			System.out.println("Yeni Kullan�c� Ekle               -> Press 4");
			System.out.println("T�m Kullan�c�lar� Listele         -> Press 5");
			System.out.println("Bug�n�n pop�ler linklerini kaydet -> Press 6");
			System.out.println("Local linkleri ayr��t�r           -> Press 7");
			System.out.println("Ba�l�klar� birle�tir              -> Press 8");
			System.out.println("Co-Occurence Matrix(ESK�)         -> Press 9");
			System.out.println("T�m Entry leri D��a Aktar         -> Press 10");
			System.out.println("Parametrik Entry D��a Aktar�m     -> Press 11");
			System.out.println("Co-Occurence Matrix (YEN� - F�LE) -> Press 12");
			System.out.println("Benzer Kullan�c� - Title Hesaplama-> Press 13");
			System.out.println("Kullan�c� - Title Hesaplama       -> Press 14");
			System.out.println("Jaccard Benzerli�ini Hesaplama    -> Press 15");
			System.out.println("Bilkent �rnek Veriyi �al��t�r     -> Press 16");
			System.out.println("Bilkent �rnek Veriyi �al��t�r TXT -> Press 17");
			System.out.println("�ngilizce Veriyi �al��t�r         -> Press 18");
			System.out.println("Yazara g�re kelimeleri ayr��t�r(TEST)-> Press 19");
			System.out.println("Entry leri Title a g�re gruplay�p d��a aktar -> Press 20");
			System.out.println("Entryleri User a g�re gruplay�p d��a aktar -> Press 21");
			System.out.println("Vocab d�zelt data gir -> Press 22");
			System.out.println("Co-Occurence Matrix (YEN� - Disk & Memory Beraber) -> Press 23");
			System.out.println("Matrix i Veritaban�nda Olu�tur   -> Press 24");
			System.out.println("PMI de�erlerini hesapla (Disk & Memory Beraber) -> Press 25");
			System.out.println("Alternate PMI de�erlerini hesapla (Disk & Memory Beraber) -> Press 26");
			System.out.println("Cosine Similarity Calculation (Disk & Memory Beraber) -> Press 27");
			System.out.println("Most Similar Words (Disk & Memory Beraber) -> Press 28");
			System.out.println("BigCLAM algoritmas�na input olu�tur        -> Press 29");
			System.out.println("Beraber g�r�lme say�lar�n� R i�in d��ar� aktar -> Press 30");
			System.out.println("Alternate PMI i�in sat�r toplam� bul           -> Press 31");
			System.out.println("Collaboration Network i�in BigCLAM ��kt�s� �ret -> Press 32");
			System.out.println("Network aras�nda ba�lant�lar� tespit et -> Press 33");
			System.out.println("Community ba�lant�lar� tespit et -> Press 34");
			System.out.println("Modularity - Overlapping Community Detection Sonu�lar� -> Press 35");
			System.out.println("Overlapping Community d��ar� aktar�m -> Press 36");
			System.out.println("Modularity d��ar� aktar�m -> Press 37");
			System.out.println("Community sadele�tirmesi yap -> Press 38");
			System.out.println("Spesifik community - kelime hesab� -> Press 39");
			System.out.println("Spesifik Community - Kelime Say�s� Hesab� -> Press 40");
			operationSelect = scanIn.nextLine();
			if (operationSelect.equals("1")) {
				crawlerManager.createCrudeLinks(pathConstants.ham);
				
			} else if (operationSelect.equals("2")) {
				Title title = new Title();
				System.out.println("Ba�l��� giriniz");
				title.setName(scanIn.nextLine());
				System.out.println("Tarihi Giriniz");
				String dateS = scanIn.nextLine();
				title.setDate(dateS);
				boolean isItOk = titleManager.addTitle(title);
				if(isItOk) {
					System.out.println("Ekleme ba�ar�l�!");
				} else {
					System.err.println("��lem s�ras�nda kritik bir hata olu�tu!");
				}
			} else if (operationSelect.equals("3")) {
				List<Title> titleList = titleManager.getAllTitles();
				if(titleList != null && titleList.size() > 0) {					
					for (Title t : titleList) {
						System.out.println("ID : " + t.getId());
						System.out.println("�sim : "+t.getName());
						System.out.println("Tarih : "+t.getDate());
						if(t.getEntryList() != null && t.getEntryList().size() > 0) {							
							System.out.println("Girilen Entry Say�s� : " + t.getEntryList().size());
						} else {
							System.out.println("Girilen Entry Say�s� : " +0);
						}
						System.out.println("----------------------------");
					}
				} else {
					System.err.println("Hi� ba�l�k bulunamad�!");
				}
			} else if (operationSelect.equals("4")) {
				User user = new User();
				System.out.println("�sim giriniz");
				user.setName(scanIn.nextLine());
				System.out.println("Soyisim giriniz");
				user.setSurname(scanIn.nextLine());
				System.out.println("Nickname giriniz");
				user.setNickname(scanIn.nextLine());
				boolean isItOk = userManager.addUser(user);
				if (isItOk) {
					System.out.println("Ekleme ba�ar�l�!");
				} else {
					System.err.println("��lem s�ras�nda kritik bir hata olu�tu!");
				}
			} else if (operationSelect.equals("5")) {
				List<User> userList = userManager.getAllUsers();
				if (userList != null && userList.size() > 0) {
					for (User u : userList) {
						System.out.println("ID : " + u.getId());
						System.out.println("�sim : "+u.getName());
						System.out.println("Soyisim : "+u.getSurname());
						System.out.println("Nickname : "+u.getNickname());
						System.out.println("----------------------------");
					}
					
				} 
				
				else {
					System.err.println("Hi� kullan�c� bulunamad�!");
				}
			} else if (operationSelect.equals("6")) {
				crawlerManager.getLinksFromMainPage(url);
			} else if (operationSelect.equals("7")) {
				crawlerManager.getDocumentWithjSoup(url, pathConstants.dir);
			} else if (operationSelect.equals("8")) {
				crawlerManager.findDuplicateTitlesAndMerge();
			} else if (operationSelect.equals("9")) {
				System.out.println("Hesaplanmas�n� istedi�iniz entry say�s�n� giriniz : ");
				int parameterForEntryCount = scanIn.nextInt();
				engineManager.coOccurenceMatrixWithEntryObjectAndReturnWindowSizeOLD(parameterForEntryCount);
			} else if (operationSelect.equals("10")) {
				entryManager.writeAllEntriesToDocument();
			} else if (operationSelect.equals("11")) {
				System.out.println("�ekilmesini istedi�iniz entry say�s�n� giriniz : ");
				int parameterForEntryCount = scanIn.nextInt();
				entryManager.writeSpecificEntryCountToDocument(parameterForEntryCount);
			} else if (operationSelect.equals("12")) {
				engineManager.createCoOccurenceMatrix(pathConstants.fileReadingPath, null);
			} else if (operationSelect.equals("13")) {
				entryManager.getSimilarUsersThatWriteTheSameTitle();
			} else if (operationSelect.equals("14")) {
				entryManager.getTitleCountOfUsers();
			} else if (operationSelect.equals("15")) {
				engineManager.calculateJaccardSimilarityAndSave();
			} else if (operationSelect.equals("16")) {
				engineManager.runBilkentData(pathConstants.bilkentXmlPath);
			} else if (operationSelect.equals("17")) {
				engineManager.runBilkentDataWithTxt(pathConstants.bilkentTxtPath);
			} else if (operationSelect.equals("18")) {
				engineManager.runEnglishContent(pathConstants.englishPath);
			} else if (operationSelect.equals("19")) {
				Set<String> set = new HashSet<String>();
				set.add("the wade");
				set.add("sia");
				engineManager.findWordsByAuthorFromTxtFile(set);
				
			} else if (operationSelect.equals("20")) {
				engineManager.exportEntriesGroupByTitle();
				
			} else if (operationSelect.equals("21")) {
				engineManager.exportEntriesGroupByUser();
			} else if (operationSelect.equals("22")) {
				engineManager.exportWrongVocabs();
			} else if (operationSelect.equals("23")) {
				engineManager.createCoOccurenceMatrixWithMemoryAndDisk(pathConstants.fileReadingPath, null);
			} else if (operationSelect.equals("24")) {
				engineManager.saveCoOccurrenceMatrixToDatabase(pathConstants.fileReadingPath, null);
			} else if (operationSelect.endsWith("25")) {
				engineManager.calculatePMIValuesWithMemoryAndDisk(pathConstants.fileReadingPath, null);
			} else if (operationSelect.equals("26")) {
				engineManager.calculateAlternatePMIValuesWithMemoryAndDisk(pathConstants.fileReadingPath, null);
			} else if (operationSelect.equals("27")) {
				engineManager.calculateCosineSimilarityMemoryAndDisk();
			} else if (operationSelect.equals("28")) {
				engineManager.findMostSimilarWords();
			} else if (operationSelect.equals("29")) {
				engineManager.createBigClamInput();
			} else if (operationSelect.equals("30")) {
				engineManager.getAllInputIntoTxt();
			} else if (operationSelect.equals("31")) {
				engineManager.findSumOfRowsForAlternatePMI(pathConstants.fileReadingPath, null);
			} else if (operationSelect.equals("32")) {
				engineManager.createBigClamInputForCollaborationNetwork(pathConstants.collaborationNetworkPath);
			} else if (operationSelect.equals("33")) {
				engineManager.searchNetworkLinks(pathConstants.linkFilePath);
			} else if (operationSelect.equals("34")) {
				engineManager.searchNetworkCommunitiesLinks(pathConstants.linkDataPath);
			} else if (operationSelect.equals("35")) {
				engineManager.findModularityOverlappingCommunityResults(pathConstants.modularityOverlappingFilesPath);
			} else if (operationSelect.equals("36")) {
				engineManager.exportWordAssociationNetworkCommunityByCommunity(pathConstants.modularityOverlappingFilesPath);
			} else if (operationSelect.equals("37")) {
				engineManager.exportWordAssociationNetworkModularity(pathConstants.modularityFilePath);
			} else if (operationSelect.equals("38")) {
				engineManager.removeUnassignedWordsFromCommunities(pathConstants.modularityFilePath);
			} else if (operationSelect.equals("39")) {
				engineManager.calculateSpecificCommunityWords(pathConstants.globalDataPath);
			} else if (operationSelect.equals("40")) {
				engineManager.getSpecificCommunityWordCount(pathConstants.globalDataPath);
			}
			
			
		} while(!operationSelect.equals("0"));
		
		scanIn.close();

	}	
}
